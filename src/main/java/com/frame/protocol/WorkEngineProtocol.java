package com.frame.protocol;

import java.sql.SQLException;

import com.frame.base.MysqlConn;
import com.google.gson.JsonObject;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

public abstract class WorkEngineProtocol //extends StorageFrame
{
    // 1.param
    private String sOrignRequest = "";
    
    public  JsonObject reqTpFrame = new JsonObject();//request-frame json-object buffer
    private JsonObject repTpStore = new JsonObject();//response json-object buffer
    public  JsonObject reqTpStore = new JsonObject();//request-content json-object buffer
    public  JsonObject bufTpStore = new JsonObject();//cache json-object buffer
    public Logger logger  =  Logger.getLogger(WorkEngineProtocol.class);

    private MysqlConn dbinst = new MysqlConn();

    private String sBusCode          = ""; //buscode
    private String sBusVersion       = ""; //busversion
    private String sSourceSysId      = ""; //source system id
    private String sSourceSign       = ""; //source system sign
    private String sRequestDate      = ""; //request create date
    private String sValideDate       = ""; //limit date for response
    private String sDestinationSysId = ""; //destination system id
    private String sDescSign         = ""; //destination system sign
    private String sResponseDate     = ""; //response create date
    private String sReturnCode       = "0"; //return code
    private String sReturnMessage    = "ok"; //return message

    // 2.bus.set
    public void setReqOrgn          (String _sOrignRequest    )
    {
        sOrignRequest     = _sOrignRequest   ;
    }
    public void setBusCode          (String _BusCode          )
    {
        sBusCode          = _BusCode         ;
    }
    public void setBusVersion       (String _BusVersion       )
    {
        sBusVersion       = _BusVersion      ;
    }
    public void setSourceSysId      (String _SourceSysId      )
    {
        sSourceSysId      = _SourceSysId     ;
    }
    public void setSourceSign       (String _SourceSign       )
    {
        sSourceSign       = _SourceSign      ;
    }
    public void setRequestDate      (String _RequestDate      )
    {
        sRequestDate      = _RequestDate     ;
    }
    public void setValideDate       (String _ValideDate       )
    {
        sValideDate       = _ValideDate      ;
    }
    public void setDestinationSysId (String _DestinationSysId )
    {
        sDestinationSysId = _DestinationSysId;
    }
    public void setDescSign         (String _DescSign         )
    {
        sDescSign         = _DescSign        ;
    }
    public void setResponseDate     (String _ResponseDate     )
    {
        sResponseDate     = _ResponseDate    ;
    }
    public void setReturnCode       (String _ReturnCode       )
    {
        sReturnCode       = _ReturnCode      ;
    }
    public void setReturnMessage    (String _ReturnMessage    )
    {
        sReturnMessage    = _ReturnMessage   ;
    }

    // 2.bus.get
    public String getReqOrgn          ()
    {
        return sOrignRequest     ;
    }
    public String getBusCode          ()
    {
        return sBusCode          ;
    }
    public String getBusVersion       ()
    {
        return sBusVersion       ;
    }
    public String getSourceSysId      ()
    {
        return sSourceSysId      ;
    }
    public String getSourceSign       ()
    {
        return sSourceSign       ;
    }
    public String getRequestDate      ()
    {
        return sRequestDate      ;
    }
    public String getValideDate       ()
    {
        return sValideDate       ;
    }
    public String getDestinationSysId ()
    {
        return sDestinationSysId ;
    }
    public String getDescSign         ()
    {
        return sDescSign         ;
    }
    public String getResponseDate     ()
    {
        return sResponseDate     ;
    }
    public String getReturnCode       ()
    {
        return sReturnCode       ;
    }
    public String getReturnMessage    ()
    {
        return sReturnMessage    ;
    }

	public void exit()
    {
        if(null != dbinst)
        {
        	dbinst=null;
        }
        repTpStore=null;
        reqTpStore=null;
        bufTpStore=null;
        reqTpFrame=null;
    }

    /** set request, <b>ServiceFrame</b> for Frame and <b>ServiceContent</b> for Data
     * @ServiceFrame
     *  "BusCode"     string must     </br>
     *  "BusVersion"  string must     "v{1}.{2}.{3}"</br>
     *  "SourceSysId" string must     </br>
     *  "Sign"        string optional </br>
     *  "RequestDate" string must     YYYYMMDDHH24MISS</br>
     *  "ValideDate"  string optional YYYYMMDDHH24MISS</br>
     * @ServiceContent
     *  is artificial
     *
     * */
    public boolean setRequest(JsonObject _obj)
    {
        String sVlaue = "";
        JsonObject req = null;
        
        if(null==_obj)
        {
        	return false;
        }
        
        if(null == _obj.get("ServiceFrame")  || false == _obj.get("ServiceFrame").isJsonObject()
        || null == _obj.get("ServiceContent")|| false == _obj.get("ServiceContent").isJsonObject()) //ServiceFrame && ServiceContent
        {
            return false;
        }
        
        // @else ServiceFrame
        // BusCode
        req = _obj.get("ServiceFrame").getAsJsonObject();
        if(null == req.get("BusCode"))
        {
            return false;
        }
        else
        {
            sVlaue = req.get("BusCode").getAsString();
            setBusCode(sVlaue);
        }

        // BusVersion v1.2.3
        sVlaue = "";
        if(null == req.get("BusVersion"))
        {
            return false;
        }
        else
        {
            sVlaue = req.get("BusVersion").getAsString();
            if(sVlaue.length() != 6)
            {
                return false;
            }

            if(sVlaue.charAt(0) != 'v'
                    || "0123456789".indexOf(sVlaue.charAt(1)) == -1
                    || sVlaue.charAt(2) != '.'
                    || "0123456789".indexOf(sVlaue.charAt(3)) == -1
                    || sVlaue.charAt(4) != '.'
                    || "0123456789".indexOf(sVlaue.charAt(5)) == -1)
            {
                return false;
            }
            setBusVersion(sVlaue);
        }

        // SourceSysId
        sVlaue = "";
        if(null == req.get("SourceSysId"))
        {
            return false;
        }
        else
        {
            sVlaue = req.get("SourceSysId").getAsString();
            setSourceSysId(sVlaue);
        }

        // Sign
        sVlaue = "";
        if(null == req.get("Sign"))
        {
            setSourceSign("null");
        }
        else
        {
            sVlaue = req.get("Sign").getAsString();
            setSourceSign(sVlaue);
        }

        // RequestDate
        sVlaue = "";
        if(null == req.get("RequestDate"))
        {
            return false;
        }
        else
        {
            sVlaue = req.get("RequestDate").getAsString();
            setRequestDate(sVlaue);
        }

        // ValideDate
        sVlaue = "";
        if(null == req.get("ValideDate"))
        {
            setSourceSign("20991231235959");
        }
        else
        {
            sVlaue = req.get("ValideDate").getAsString();
            setValideDate(sVlaue);
        }

        // ServiceContent
        req = null;
        reqTpFrame = _obj.get("ServiceFrame")  .getAsJsonObject();
        reqTpStore = _obj.get("ServiceContent").getAsJsonObject();
        return true;
    };

    public void setResponse         (JsonObject _obj)
    {
        repTpStore = _obj;
    }
    public void setBuffer           (JsonObject _obj)
    {
        bufTpStore = _obj;
    }

    // 3.buf.get
    public JsonObject getResponse()
    {
        return repTpStore;
    }
    public void setRespHead()
    {
        if(null == repTpStore.get("ServiceFrame"))
        {
            JsonObject objHead = new JsonObject();
            objHead.addProperty("BusCode", sBusCode);
            objHead.addProperty("BusVersion", sBusVersion);
            objHead.addProperty("DestinationSysId", sSourceSysId);
            objHead.addProperty("Sign", sSourceSign);
            objHead.addProperty("ResponseDate", (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date()));
            objHead.addProperty("ReturnCode", sReturnCode);
            objHead.addProperty("ReturnMessage", sReturnMessage);
            repTpStore.add("ServiceFrame", objHead);
            objHead=null;
        }
        else
        {
            repTpStore.get("ServiceFrame").getAsJsonObject().addProperty("ResponseDate", (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date()));
            repTpStore.get("ServiceFrame").getAsJsonObject().addProperty("ReturnCode", sReturnCode);
            repTpStore.get("ServiceFrame").getAsJsonObject().addProperty("ReturnMessage", sReturnMessage);
        }

        if(!sReturnCode.equals("0"))
        {
            repTpStore.get("ServiceFrame").getAsJsonObject().addProperty("RequesetContext", reqTpStore.toString());
        }
    };

    public JsonObject getRespBody()
    {
        if(null == repTpStore.get("ServiceContent"))
        {
            JsonObject objBody = new JsonObject();
            repTpStore.add("ServiceContent", objBody);
            objBody=null;
        }
        return repTpStore.get("ServiceContent").getAsJsonObject();
    };

    public JsonObject getReqFrame()
    {
        return reqTpFrame;
    };
    
    public JsonObject getRequest()
    {
        return reqTpStore;
    };
    public JsonObject getBuffer()
    {
        return bufTpStore;
    };

    public MysqlConn  getDBInst           ()
    {
        return dbinst;
    };
    public boolean    getDBStat           ()
    {
        return dbinst.getDBState();
    }
    public void    dbCommit()
    {
    	try
    	{
    		dbinst.getConn().commit();
    	}
    	catch(SQLException e)
    	{
    		e.printStackTrace();
    	}        
    }
    public void    dbRollback()
    {
    	try
    	{
    		dbinst.getConn().rollback();
    	}
    	catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    }

    // 4.run
    public abstract String run(String sRequest);
}