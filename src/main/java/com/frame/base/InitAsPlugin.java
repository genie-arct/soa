/** package note
 *  ServletContext Manager
 */
package com.frame.base;

import java.net.MalformedURLException;
import java.sql.SQLException;

import com.frame.protocol.EngineFrame;

/** class definition
 * @version v1.0.160816
 */
public class InitAsPlugin
{
	private static String sWebRoot=null;
    // initialization
    public static void Init(String _sRootHomeName)
    {
        try 
        {
        	System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<V1.0.0 @201609.STAFF00475>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        	System.out.println("======================Init ServletContext Manager begin!!  ======================");
        	if(!EvnInit.driver(_sRootHomeName)){
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
    }
    
    public static String getWebRoot()
    {
    	return sWebRoot;
    }
    
    public static void setWebRoot(String _sWebRoot_)
    {
    	sWebRoot=_sWebRoot_;
    }
}
