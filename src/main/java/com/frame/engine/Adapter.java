package com.frame.engine;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import com.frame.base.EvnInit;

public class Adapter extends HttpServlet
{
    private static final long serialVersionUID = 0;
    private String reqStr = "";
    private String repStr = "";
    private boolean ifFalseGETorTruePOST  = true;

    public void doPost(HttpServletRequest request, HttpServletResponse response) //throws ServletException, IOException
    {
        reqStr = "";
        repStr = "";
        NDC.push(getLogId("A"));
        
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
        if(! EvnInit.checkWhtCfg(request.getRequestURI(), sUsrAddr))
        {
        	repStr = "request on ["+request.getRequestURI()+"] from ["+request.getRemoteAddr()+"] not in the white list of credit";/////???????????????????????
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
                repStr = "request is null";
            }
            else
            {
                response.setContentType("text/plain;charset=utf-8");
                WorkAdapter adapter = new WorkAdapter();
                repStr = adapter.run(reqStr);
                if(0 == adapter.getAdpType())
                {
                    response.setContentType("text/xml;charset=UTF-8");
                    response.setCharacterEncoding("UTF-8");
                    response.setHeader("Cache-Control", "no-cache");
                }
            }
        }
        (Logger.getLogger(Adapter.class)).debug(repStr);

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

    private static String getLogId(String _sHead)
    {
        return String.format("%s", _sHead)
               + String.format("%04d|", 1 + (int)(Math.random() * (9999))) //Four bit random code(Left zero fill)
               + String.format("%s"  , (new SimpleDateFormat("HHmmssSSS")).format(new Date())); //HHmmssSSS
    }

}