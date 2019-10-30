/** package note
 *  ServletContext Manager
 */
package com.frame.base;

import java.net.MalformedURLException;
import java.sql.SQLException;

/** import plugins
 */
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import com.frame.protocol.EngineFrame;

/** class definition
 * @version v1.0.160816
 */
public class ServletContextManager implements ServletContextListener
{
	private static String sWebRoot=null;
    // initialization
    public void contextInitialized(ServletContextEvent sce)
    {
    	String sEnviromentLableName = "FPAY_SERVICE_HOME";
        try 
        {
        	System.out.println("======================Init ServletContext Manager begin!!  ======================");
        	if(!EvnInit.driver(sEnviromentLableName)){
			System.out.println("----------------------Init Enviroment Config failed!!!!!!  ----------------------");}
        	else{
			System.out.println("----------------------Init Enviroment Config success!!!!!  ----------------------");}
			
			if(!EngineFrame.driver()){
			System.out.println("----------------------Init Engine Frame failed!!!!!!!!!!!  ----------------------");}
			else{
			System.out.println("----------------------Init Engine Frame success!!!!!!!!!!  ----------------------");}
			System.out.println("======================Init ServletContext Manager finish!  ======================");
		} 
        catch (SQLException e) 
        {
			e.printStackTrace();
		}
        catch (MalformedURLException e) 
        {
			e.printStackTrace();
		} 
        catch (ClassNotFoundException e) 
        {
			e.printStackTrace();
		}
        sWebRoot=sce.getServletContext().getRealPath("/");
    }

    // destroy
    public void contextDestroyed(ServletContextEvent sce)
    {
    	
    }
    
    public static String getWebRoot()
    {
    	return sWebRoot;
    }
}
