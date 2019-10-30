package com.frame.engine;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.frame.handler.Message;
import com.frame.protocol.BusFrame;
import com.frame.protocol.EngineFrame;
import com.frame.protocol.UnitFrameProtocol;
import com.frame.protocol.UnitFrame;
import com.frame.protocol.WorkEngineProtocol;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;


public class WorkService extends WorkEngineProtocol
{
    public String run(String strJson)
    {
        int iOrder = 0;
        int iSvcId = 0;
        boolean if_DealSuccOrFail = true;

        BusFrame bf = null;
        JsonObject jsonObj = null;
        List<Message> list = null;

        // init request
        try
        {
            logger.info("ST201608F00475" + strJson);
            jsonObj = new JsonParser().parse(strJson).getAsJsonObject();
            if(!setRequest(jsonObj))
            {
                logger.error("[FAILED]Illegal Request:" + strJson);
                return "Illegal Request:" + strJson;
            }
        }
        catch(IllegalStateException e)
        {
            logger.error(e.getMessage() + strJson);
            return e.getMessage() + strJson;
        }
        catch(JsonSyntaxException e)
        {
            logger.error(e.getMessage() + strJson);
            return e.getMessage() + strJson;
        }

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
            return       "Bus_Code:" + getBusCode() + " version of " + getBusVersion() + " without configuration";
        }

        try
        {
            logger.info("=============Service[" + getBusCode() + "|" + getBusVersion() + "] BEGIN...");
            list = new LinkedList<Message>();
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
                engine.setStore(getRequest(), getRespBody(), getBuffer(), getReqFrame()); // set context buffer
                engine.setMessageList(list);

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
                if(engine.getSignal() != 0)
                {
                    iOrder = 1000000;
                    logger.info("=============Service[" + getBusCode() + "|" + getBusVersion() + "] INTERRUPTED!!!");
                }
                engine.exit();
                engine = null;
                uf = null;
            }
            while( (iOrder < (bf.getBusId() * 100 + bf.getCount()))  && if_DealSuccOrFail );
        }
        catch(InstantiationException e1)
        {
            if_DealSuccOrFail = false;
            setReturnCode("-9999");
            setReturnMessage("WorkEngine error due exception:InstantiationException,cause:" + e1.getCause());
        }
        catch(IllegalAccessException e)
        {
            if_DealSuccOrFail = false;
            setReturnCode("-9999");
            setReturnMessage("WorkEngine error due exception:IllegalAccessException,cause:" + e.getCause());
        }

        Iterator<Message> it = list.iterator();
        while (it.hasNext())
        {
            Message msg = it.next();
            try
            {
                msg.getHandler().onMessage(msg.getTopic(), msg.getConten());
            }
            catch (Exception e)
            {
                if_DealSuccOrFail = false;
                setReturnCode("-9999");
                setReturnMessage("WorkEngine error due Exception,cause:" + e.getCause());
            }
        }

        if(if_DealSuccOrFail)
        {
            this.dbCommit();
            getDBInst().delDBState();
            logger.info("=============Service[" + getBusCode() + "|" + getBusVersion() + "] SUCCESS!!!");
        }
        else
        {
            if(getReturnCode().equals("0"))
            {
                setReturnCode("-9998");
                setReturnMessage("WorkEngine error:unknow reason");
            }

            this.dbRollback();
            getDBInst().delDBState();
            logger.error("=============Service[" + getBusCode() + "|" + getBusVersion() + "] FAILED!!!");
        }
        setRespHead();
        String s = getResponse().toString();
        exit();
        return s;
    }
}
