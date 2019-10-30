package com.frame.engine;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

import com.google.gson.JsonObject;

public class ParseRoute
{
    private static final String serialVersionDesc = "v1.0.0";

    public static String parseRequest(HttpServletRequest _Req)
    {
    	printVersion(_Req);
    	try 
        {
			_Req.setCharacterEncoding("UTF-8");
		}
        catch (UnsupportedEncodingException e) 
        {
        	(Logger.getLogger(ParseRoute.class)).error(String.format("Switch and Parse ContentType:text/xml failed,cause:%s", e.getCause()));
        	return null;
		}
        
        return switchContentType(_Req);
    }

    private static void printVersion(HttpServletRequest _Req)
    {
    	String sUsrAddr="";
        if(null==_Req.getHeader("x-forwarded-for"))
        {
        	sUsrAddr=_Req.getRemoteAddr();
        }
        else
        {
        	sUsrAddr=_Req.getHeader("x-forwarded-for");
        }
        (Logger.getLogger(ParseRoute.class)).info(String.format("%s|%s:%s%s Version:%s from %s:%s method:%s Content-Type:\"%s\" Charset:%s",
                _Req.getProtocol(),
                _Req.getServerName(),
                _Req.getServerPort(),
                _Req.getRequestURI(),
                serialVersionDesc,
                sUsrAddr,
                _Req.getRemotePort(),
                _Req.getMethod(),
                _Req.getContentType(),
                _Req.getCharacterEncoding()
        		));
    }

    private static String switchContentType(HttpServletRequest _Req)
    {
        String sContentType = _Req.getContentType();
        if(sContentType.indexOf("text/xml")!=-1)
        {
            try
            {
            	InputStream inStream = _Req.getInputStream();
                ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
                byte[] buffer = new byte[1];
                int len = 0;
                while ((len = inStream.read(buffer)) != -1)
                {
                  outSteam.write(buffer, 0, len);
                }
                outSteam.close();
                inStream.close();
                String reqStr = new String(outSteam.toByteArray(),"UTF-8");
                (Logger.getLogger(ParseRoute.class)).debug(String.format(reqStr));
                return reqStr;
            }
            catch (UnsupportedEncodingException e)
            {
            	(Logger.getLogger(ParseRoute.class)).error(String.format("Switch and Parse ContentType:text/xml failed,cause:%s", e.getCause()));
                e.printStackTrace();
                return null;
            }
            catch (IOException e)
            {
            	(Logger.getLogger(ParseRoute.class)).error(String.format("Switch and Parse ContentType:text/xml failed,cause:%s", e.getCause()));
                e.printStackTrace();
                return null;
            }
        }
        else if(sContentType.indexOf("text/html")!=-1)
        {
            StringBuilder sb = new StringBuilder();
            InputStreamReader ipt=null;
            BufferedReader br =null;
            ServletInputStream sipt=null;
            try
            {
                String line = null;
                sipt=_Req.getInputStream();
                if(null==sipt)
                {
                	(Logger.getLogger(ParseRoute.class)).error(String.format("Switch and Parse ContentType:ServletInputStream getInputStream failed,cause:null"));
                    return null;
                }
                ipt=new InputStreamReader(sipt,"UTF-8");
                br = new BufferedReader(ipt);
                while((line = br.readLine()) != null)
                {
                    sb.append(line);
                }
            }
            catch (IOException e)
            {
                (Logger.getLogger(ParseRoute.class)).error(String.format("Switch and Parse ContentType:text/html failed,cause:%s", e.getCause()));
                e.printStackTrace();
                return null;
            }
            finally
            {
            	try 
            	{
            		if(null!=br)
            		{
            			br.close();
            		}
				}
            	catch (IOException e)
            	{
					
				}
            	br=null;
                
                try
                {
					if(null!=ipt)
            		{
						ipt.close();
            		}
				}
                catch (IOException e)
                {
					
				}
            	ipt=null;
                
                try
                {
                	if(null!=sipt)
            		{
                		sipt.close();
            		}
				} catch (IOException e) {
					
				}
                sipt=null;
            }
            
            return sb.toString();
        }
        else if(sContentType.indexOf("application/x-www-form-urlencoded")!=-1)
        {
        	String _tag="";
        	JsonObject obj=new JsonObject();
        	Enumeration<String> en=_Req.getParameterNames();
        	while(en.hasMoreElements())
        	{
        		_tag=en.nextElement();
        		obj.addProperty(_tag, _Req.getParameter(_tag));
        	}
        	return obj.toString();
        }        
        else
        {
            try
            {
            	InputStream inStream = _Req.getInputStream();
                ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = inStream.read(buffer)) != -1)
                {
                  outSteam.write(buffer, 0, len);
                }
                outSteam.close();
                inStream.close();
                String reqStr = new String(outSteam.toByteArray(),"UTF-8");
                return reqStr;
            }
            catch (IOException e)
            {
                (Logger.getLogger(ParseRoute.class)).error(String.format("Switch and Parse ContentType:text/html failed,cause:%s", e.getCause()));
                e.printStackTrace();
                return null;
            }
        }
    }
}
