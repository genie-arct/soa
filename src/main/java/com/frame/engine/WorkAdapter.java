package com.frame.engine;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.frame.protocol.BusFrame;
import com.frame.protocol.EngineFrame;
import com.frame.protocol.UnitFrameProtocol;
import com.frame.protocol.UnitFrame;
import com.frame.protocol.WorkEngineProtocol;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.dom4j.*;

public class WorkAdapter extends WorkEngineProtocol
{
    private int iAdpType = 0; // 0-xml;1-json
    final protected static String INGNORED_TAG_ONLY_VALUE = "INGNORED_TAG_ONLY_VALUE";

    @Override
    public String run(String strRequest)
    {
        int iOrder = 0;
        int iSvcId = 0;
        BusFrame bf = null;
        boolean if_DealSuccOrFail = true;

        // adapter
        if(!adapterRequset(strRequest))
        {
            return "Undefined-Adapter:" + strRequest;
        }
        setReqOrgn(strRequest);

        // init dbconn
        if(!getDBStat())
        {
            logger.error("System error :dbstate invalide");
            return "System error :dbstate invalide";
        }

        // begin bus
        bf = EngineFrame.getBusFrame(getBusCode(), getBusVersion());
        if(null == bf)
        {
            logger.error("Bus_Code:" + getBusCode() + " version of " + getBusVersion() + " without configuration");
            return "Bus_Code:" + getBusCode() + " version of " + getBusVersion() + " without configuration";
        }

        try
        {
            logger.info("=============Service[" + getBusCode() + "|" + getBusVersion() + "] BEGIN...");
            setRespHead();
            iOrder = bf.getBusId() * 100;
            do
            {
                iSvcId = 0;
                iOrder++;
                iSvcId = bf.getSvcId(iOrder);
                UnitFrame uf = EngineFrame.getFrameUnit(iSvcId);

                // 1.load class
                logger.info("-------------Asembly[" + uf.getSvcCode() + "|" + uf.getVersion()          + "] BEGIN...");
                UnitFrameProtocol engine = (UnitFrameProtocol) (uf.getObj()).newInstance();//new instance

                // 2.init instance
                engine.setMessage("0", "ok");      //init return message
                engine.setSvcId(iSvcId);           //set service-class id
                engine.setSvcCode(uf.getSvcCode());//set service-class code
                engine.setSvcVers(uf.getVersion());//set service-class version
                engine.setDBConn(getDBInst());     //init connection
                engine.setStore(getRequest(), getRespBody(), getBuffer(),getReqFrame()); // set context buffer
                engine.setReqOrign(strRequest);

                // 3.run
                if_DealSuccOrFail = (if_DealSuccOrFail && engine.getInput("-->Class " + iSvcId));
                if_DealSuccOrFail = (if_DealSuccOrFail && engine.deal());
                if_DealSuccOrFail = (if_DealSuccOrFail && engine.setOutput());
                setReturnCode(engine.getRetCode());
                setReturnMessage(engine.getMessage());

                // 4.quit
                if( if_DealSuccOrFail)
                {
                    logger.info("-------------Asembly[" + uf.getSvcCode() + "|" + uf.getVersion()      + "] FINISHED!!!");
                }
                else
                {
                    logger.error("-------------Asembly[" + uf.getSvcCode() + "|" + uf.getVersion()      + "] FAILED!!!");
                }
                if(engine.getSignal()!=0)
                {
                	iOrder=1000000;
                	logger.info("=============Service[" + getBusCode() + "|" + getBusVersion() + "] INTERRUPTED!!!");
                }
                engine.exit();
                engine = null;
                uf = null;
            }
            while( (iOrder < (bf.getBusId() * 100 + bf.getCount())) && if_DealSuccOrFail );
        }
        catch(InstantiationException e1)
        {
            if_DealSuccOrFail = false;
            setReturnCode("-9999");
            setReturnMessage("WorkEngine error due exception:InstantiationException");
        }
        catch(IllegalAccessException e)
        {
        	if_DealSuccOrFail = false;
            setReturnCode("-9999");
            setReturnMessage("WorkEngine error due exception:IllegalAccessException");
        }

        if(if_DealSuccOrFail)
        {
        	if(getReturnCode().equals("0"))
        	{
        		setReturnCode("-9998");
                setReturnMessage("WorkEngine error:unknow reason");
        	}
            this.dbCommit();
            getDBInst().delDBState();
            logger.info("=============Service[" + getBusCode() + "|" + getBusVersion() + "] SUCCESS!!!");
        }
        else
        {
            this.dbRollback();
            getDBInst().delDBState();
            logger.error("=============Service[" + getBusCode() + "|" + getBusVersion() + "] FAILED!!!");
        }
        setRespHead();
        String sRet = adapterResponse();
        exit();
        return sRet;
    }

    // req-adapter
    private boolean adapterRequset(String strReq)
    {
        if(adapterReqXML(strReq))
        {
            iAdpType = 0; //xml
            logger.info("ST201608F00475 XML-Adapter:" + strReq);
            return true;
        }
        else if(adapterReqJSON(strReq))
        {
            iAdpType = 1; //json
            logger.info("ST201608F00475 JSON-Adapter:" + strReq);
            return true;
        }
        else if(adapterReqParam(strReq))
        {
            iAdpType = 2; //Param
            logger.info("ST201608F00475 Param-Adapter:" + strReq);
            return true;
        }
        else
        {
            logger.error("ST201608F00475 Undefined-Adapter:" + strReq);
            return false;
        }
    }

    // rep-adapter
    private String adapterResponse()
    {
        if(0 == iAdpType)
        {
            return adapterRepXML();
        }
        else if(1 == iAdpType)
        {
            return adapterRepJSON();
        }
        else
        {
            logger.error("Undefined-Adapter response");
            return "Undefined-Adapter response";
        }
    }

    // XML-Adapter
    private boolean adapterReqXML(String strXML)
    {
        int iAdapterId = 0;
        try
        {
            JsonObject objJson = new JsonObject();
            Document doc = DocumentHelper.parseText(strXML);
            Element root = doc.getRootElement();

            @SuppressWarnings("unchecked")
            List<Element> listAttr = root.elements();
            for(Element elem : listAttr)
            {
                objJson.addProperty(elem.getName(), elem.getTextTrim());
            }

            iAdapterId = EngineFrame.getAdapterId(objJson);
            if(iAdapterId < 0)
            {
                return false;
            }
            else
            {
                // ServiceFrame.BusCode
                setBusCode(EngineFrame.getAdapterBusCode(iAdapterId));

                // ServiceFrame.BusVersion v1.2.3
                setBusVersion(EngineFrame.getAdapterBusVers(iAdapterId));

                // ServiceFrame.SourceSysId
                setSourceSysId(String.format("A%04d",iAdapterId));

                // ServiceFrame.Sign
                setSourceSign("0");

                // ServiceFrame.RequestDate
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                setRequestDate(df.format(new Date()));

                // ServiceFrame.ValideDate
                setValideDate("20991231235959");

                //ServiceContent
                reqTpStore = objJson;
                iAdpType = 0;
                return true;
            }
        }
        catch (DocumentException e)
        {
            return false;
        }
    }

    // JSON-Adapter
    private boolean adapterReqJSON(String strJSON)
    {
        int iAdapterId = 0;
        try
        {
            JsonObject objJson = new JsonParser().parse(strJSON).getAsJsonObject();
            iAdapterId = EngineFrame.getAdapterId(objJson);
            if(iAdapterId < 0)
            {
                return false;
            }
            else
            {
                // ServiceFrame.BusCode
                setBusCode(EngineFrame.getAdapterBusCode(iAdapterId));

                // ServiceFrame.BusVersion v1.2.3
                setBusVersion(EngineFrame.getAdapterBusVers(iAdapterId));

                // ServiceFrame.SourceSysId
                setSourceSysId(String.format("A%04d",iAdapterId));

                // ServiceFrame.Sign
                setSourceSign("0");

                // ServiceFrame.RequestDate
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                setRequestDate(df.format(new Date()));

                // ServiceFrame.ValideDate
                setValideDate("20991231235959");

                //ServiceContent
                reqTpStore = objJson;
                iAdpType = 1;
                return true;
            }
        }
        catch (IllegalStateException e)
        {
            return false;
        }
        catch (JsonSyntaxException e)
        {
            return false;
        }
    }

    // PARAM-Adapter
    private boolean adapterReqParam(String strJSON)
    {
        return true;
    }

    // XML-Adapter
    private String adapterRepXML()
    {
        try
        {
            Document doc = DocumentHelper.parseText("<xml></xml>");
            Element root = doc.getRootElement();

            Set<Entry<String, JsonElement>> set = getResponse().get("ServiceContent").getAsJsonObject().entrySet();
            Iterator<Entry<String, JsonElement>> it = set.iterator();

            while(it.hasNext())
            {
                Entry<String, JsonElement> ent = it.next();
                Element enew = root.addElement(ent.getKey());
                enew.addCDATA(ent.getValue().getAsString());
            }
            String sRet=root.asXML();
            logger.debug(sRet);
            return sRet;
        }
        catch (DocumentException e)
        {
            return null;
        }
    }

    // JSON-Adapter
    private String adapterRepJSON()
    {
        JsonObject obj = getResponse().get("ServiceContent").getAsJsonObject();
        if(obj.get(INGNORED_TAG_ONLY_VALUE) != null)
        {
            return obj.get(INGNORED_TAG_ONLY_VALUE).getAsString();
        }
        else
        {
            return obj.toString();
        }
    }

    public int getAdpType()
    {
        return iAdpType;
    }
}