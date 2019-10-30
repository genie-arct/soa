package com.frame.engine;



import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.frame.base.EvnInit;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

public class Service extends HttpServlet
{
    private static final long serialVersionUID = 0;
    private String reqStr = "";
    private String repStr = "";
    private boolean ifFalseGETorTruePOST  = true;

    public void doPost(HttpServletRequest request, HttpServletResponse response) //throws ServletException, IOException
    {
        reqStr = "";
        repStr = "";
        NDC.push(getLogId("R"));
        
        String sUsrAddr="";
        if(null==request.getHeader("x-forwarded-for"))
        {
        	sUsrAddr=request.getRemoteAddr();
        }
        else
        {
        	sUsrAddr=request.getHeader("x-forwarded-for");
        }
        
        // request
        if(! EvnInit.checkWhtCfg(request.getRequestURI(),sUsrAddr))
        {
        	repStr =  "{\"ERROR\":\"request on ["+request.getRequestURI()+"] from ["+request.getRemoteAddr()+"] not in the white list of credit\"}";
        }
        else
        {
        	if(ifFalseGETorTruePOST)
            {
                reqStr = ParseRoute.parseRequest(request);
            }
            else
            {
                reqStr = request.getQueryString();
            }

            if(null == reqStr)
            {
                repStr = "{\"ERROR\":\"request is null\"}";
            }
            else
            {
                response.setContentType("text/plain;charset=utf-8");
                WorkService driver = new WorkService();
                reqStr = formatUTF(reqStr);
                repStr = driver.run(reqStr);
            }
        }
        (Logger.getLogger(Service.class)).debug(repStr);

        // response
        try
        {
            PrintWriter pw = response.getWriter();
            pw.write(repStr);
            pw.flush();
            pw.close();
            pw = null;
            reqStr = null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {

        }
        NDC.pop();
        NDC.remove();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException
    {
        ifFalseGETorTruePOST = false;
        doPost(request, response);
        ifFalseGETorTruePOST = true;
    }

    private String formatUTF(String _sOrgin)
    {
        JsonObject jObj = new JsonObject();
        String sOrignCharset = "";
        String sTargtCharset = "";

        try
        {
            if(ifFalseGETorTruePOST)
            {
                sOrignCharset = "UTF-8";
                sTargtCharset = "UTF-8";
            }
            else
            {
                sOrignCharset = "ISO8859-1";
                sTargtCharset = "GBK";
            }
            JsonObject jObjOrign = new JsonParser().parse(_sOrgin).getAsJsonObject();
            jObj.add("ServiceFrame", jObjOrign.get("ServiceFrame").getAsJsonObject());// head
            JsonObject jObj2 = new JsonObject(); // body
            JsonObject ServiceContent = jObjOrign.get("ServiceContent").getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> set = ServiceContent.entrySet();
            Iterator<Map.Entry<String, JsonElement>> it = set.iterator();
            while (it.hasNext())
            {
                Entry<String, JsonElement> unit = it.next();
                jObj2.addProperty(unit.getKey(), new String((unit.getValue().getAsString()).getBytes(sOrignCharset), sTargtCharset));
            }
            jObj.add("ServiceContent", jObj2);
            jObj2 = null;
        }
        catch(IllegalStateException e)
        {
            jObj = null;
            return null;
        }
        catch(JsonSyntaxException e)
        {
            jObj = null;
            return null;
        }
        catch (UnsupportedEncodingException e)
        {
            jObj = null;
            return null;
        }
        String s = jObj.toString();
        jObj = null;
        return s;
    }

    private static String getLogId(String _sHead)
    {
        return String.format("%s", _sHead)
               + String.format("%04d|", 1 + (int)(Math.random() * (9999))) //Four bit random code(Left zero fill)
               + String.format("%s"  , (new SimpleDateFormat("HHmmssSSS")).format(new Date())); //HHmmssSSS
    }

}