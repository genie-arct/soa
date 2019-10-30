package com.frame.protocol;

import java.util.List;

import org.apache.log4j.Logger;

import com.frame.base.MysqlConn;
import com.frame.handler.Message;
import com.google.gson.JsonObject;

public abstract class UnitFrameProtocol
{
    // 1.param
    protected int iSvcId = 0;
    protected String sSvcCode = "";
    protected String sSvcVers = "";
    protected int iSignalType=0;//0-normal;9-interrupt;
    protected String sReturnCode;//reference WorkEngineProtocol.sReturnCode
    protected String sReturnMessage;//reference WorkEngineProtocol.sReturnMessage
    protected boolean if_ArgcsListInvalide = false;
    public final static String INGNORED_TAG_ONLY_VALUE = "INGNORED_TAG_ONLY_VALUE";
    public final static String DEFMD5_VALUE_TAG_OPTION = "390B7BA7BE53FBA20DEA3BB3CD010BAF";

    public MysqlConn dbinst;
    private List<Message> list=null;    //reference WorkEngineProtocol.MessageList
    private String sRequsetOrign = ""; //reference WorkEngineProtocol.sReturnMessage
    public JsonObject repStore = null; //reference WorkEngineProtocol.repTpStore
    public JsonObject reqStore = null; //reference WorkEngineProtocol.reqTpStore
    public JsonObject bufStore = null; //reference WorkEngineProtocol.bufTpStore
    public JsonObject reqFrame = null; //reference WorkEngineProtocol.reqFrame
    
    public Logger logger  =  Logger.getLogger(UnitFrameProtocol.class);

    // 2.frame-abstract
    public void setSvcId(int _iSvcId)
    {
        iSvcId = _iSvcId;
    }
    public void setSvcVers(String _sSvcVers)
    {
        sSvcVers = _sSvcVers;
    }
    public void setSvcCode(String _sSvcCode)
    {
        sSvcCode = _sSvcCode;
    }
    public void setReqOrign(String _sRequsetOrign)
    {
        sRequsetOrign = _sRequsetOrign;
    }

    public int getSvcId()
    {
        return iSvcId;
    }
    public String getSvcVers()
    {
        return sSvcVers;
    }
    public String getSvcCode()
    {
        return sSvcCode;
    }
    public String getReqOrign()
    {
        return sRequsetOrign;
    }
    
    public void regMessageList(Message e)
    {
        list.add(e);
    }
    
    public void setMessageList(List<Message> list)
    {
        this.list=list;
    }
    
    public void exit()
    {
        sSvcCode = null;
        sSvcVers = null;
        sReturnCode = null;
        sReturnMessage = null;
        sRequsetOrign = null;
        dbinst = null;
        repStore = null;
        reqStore = null;
        bufStore = null;
        reqFrame = null;
        list=null;
    }

    public void setStore(JsonObject _reqStore, JsonObject _repStore, JsonObject _bufStore, JsonObject _reqFrame)
    {
        repStore = _repStore;
        reqStore = _reqStore;
        bufStore = _bufStore;
        reqFrame = _reqFrame;
    }
    
    public void setSignal(int _iSignalType)
    {
    	this.iSignalType=_iSignalType;
    }
    
    public int getSignal()
    {
    	return this.iSignalType;
    }
    
    public void setMessage(String _sRetCode, String _sRetMessage)
    {
        sReturnCode = _sRetCode;
        sReturnMessage = _sRetMessage;
    }

    public void setDBConn(MysqlConn _dbinst)
    {
        dbinst = _dbinst;
    }

    public String getRetCode()
    {
        return sReturnCode;
    }

    public String getMessage()
    {
        return sReturnMessage;
    }

    public String getValue(String _sTag, boolean must_option, JsonObject ... _JsonBuff)
    {
        int iTag = 0;
        if(if_ArgcsListInvalide)
        {
            return null;
        }
        for (JsonObject obj : _JsonBuff)
        {
            if(null == obj.get(_sTag))
            {
                iTag = -1;
            }
            else if(obj.get(_sTag).isJsonArray() || obj.get(_sTag).isJsonObject())
            {
                iTag = -2;
            }
            else
            {
                String _sVal = obj.get(_sTag).getAsString();
                if(0 == _sVal.length())
                {
                    iTag = -3;
                }
                else
                {
                    logger.info("[" + _sTag + "]: " + _sVal);
                    return _sVal;
                }
            }
        }
        if(must_option)
        {
            if(-1 == iTag)
            {
                sReturnCode = "-1000";
                sReturnMessage = "[" + _sTag + "] <Not found>";
                if_ArgcsListInvalide = true;
                logger.info("[" + _sTag + "]:<Not found>");
            }
            else if(-2 == iTag)
            {
                sReturnCode = "-1001";
                sReturnMessage = "[" + _sTag + "] <Value type mismatch>";
                if_ArgcsListInvalide = true;
                logger.info("[" + _sTag + "]:<Value type mismatch>");
            }
            else if(-3 == iTag)
            {
                sReturnCode = "-1002";
                sReturnMessage = "[" + _sTag + "] <Value length 0>";
                if_ArgcsListInvalide = true;
                logger.info("[" + _sTag + "]:<Value length 0>");
            }
            else
            {
                sReturnCode = "-1003";
                sReturnMessage = "[" + _sTag + "] <Undefined>";
                if_ArgcsListInvalide = true;
                logger.info("[" + _sTag + "]:<Undefined>");
            }
            return null;
        }
        else
        {
            logger.info("[" + _sTag + "]: " + DEFMD5_VALUE_TAG_OPTION + "(default)");
            return DEFMD5_VALUE_TAG_OPTION;
        }
    }

    // 3.function-abstract
    public abstract boolean getInput(String sRequest);
    public abstract boolean deal();
    public abstract boolean setOutput();
}