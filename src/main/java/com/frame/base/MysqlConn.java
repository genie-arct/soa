package com.frame.base;

import com.mysql.jdbc.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** Class definition</br>
 * -MySQL manager
 * @version v1.0.160906 
 * @see 
 * -01 {@link #initConn()}: init connection of mysql<br/>
 * -02 {@link #initConn(boolean auto_manul)}: init connection of mysql with auto_manul falg<br/>
 * -03 {@link #execDML(int iDML, String ... sBinds)}: excute query and return ResultSet of target dml<br/> 
 * -04 {@link #execDML(int iDML, int iCount,String ... sBinds)}: excute update/delete/insert and return number of effact rows <br/> 
 * -05 {@link #delPstmt() }: release a Query-Statement after excute<br/> 
 * -06 {@link #getDBState()}: init connection and get state<br/> 
 * -07 {@link #getDBState(boolean auto_manul)}: init connection with automatic transaction falg and get state<br/> 
 * -08 {@link #delDBState()}:close connection<br/> 
 * -09 {@link #getStatement()}:return current statement object<br/> 
 * -10 {@link #getConn()}:return current connection object<br/>
 * -11 {@link #getMessage()}:return current statements's resault message<br/>
 * -12 {@link #getExecCode()}:return current statements's resault code<br/>    
 */
public class MysqlConn
{
    private Connection connObj = null;
    private Statement  statMnt = null;
    private PreparedStatement pstmt = null;
    private DMLList dmlObj=null;
    private String  sReturnMsg="";
    private int iReturnCode=0;
    private static final String driver = "com.mysql.jdbc.Driver";
    public  static final int BUNDOCOURSERROR    = -999999;
    public  static final int EXECOCOURSERROR    = -999998;
    public  static final int PRESQLOCOURSEERROR = -999997;
    private static String url="";
    private static String username="";
    private static String password="";
    private static int iTimes=0;
    private static int iMaxConTimes=0;
    
    private static Lock lock = new ReentrantLock();
    

    /** execDML</br>
     * -mysql query dml excute
     * @version v1.0.160906
     * @param iDML Id of dml
     * @param iCol numbers of return colum(default 0)
     * @param sBinds Bind argcs
     * @return ResultSet sets of Target for dml query
     */
    public ResultSet execDML(int iDML, String ... sBinds) throws SQLException
    {
        int i = 0;
        ResultSet rs = null;
        dmlObj=null;
        iReturnCode=0;
                
        if(null!=EvnInit.getDML(String.valueOf(iDML)))
        {
        	dmlObj=EvnInit.getDML(String.valueOf(iDML));
        }
        else
        {
        	sReturnMsg="[EXECDML] [Error]invalide dml for [" + iDML+ "]";
        	iReturnCode=PRESQLOCOURSEERROR;
            return null;
        }
        
        if(dmlObj.getDmlType() != 1)
        {
        	sReturnMsg="[EXECDML] [Error]invalide dml type for [" + dmlObj.getDmlId() + "]";
        	iReturnCode=PRESQLOCOURSEERROR;
            return null;
        }
        String sSql = dmlObj.getSql();
        pstmt = connObj.prepareStatement(sSql);
        
        for (String sVal : sBinds)
        {
            i++;
            if(i > dmlObj.getBundNum())
            {
            	sReturnMsg="[EXECDML] [Error]Bind parameters number no matching [" + dmlObj.getDmlId() + "]";
            	iReturnCode=BUNDOCOURSERROR;
                return null;
            }
            try
            {
                switch (dmlObj.getBindInput(i))
                {
                case Types.BIGINT:
                case Types.BOOLEAN:
                case Types.DATE:
                case Types.TIME:
                case Types.TIMESTAMP:
                case Types.VARCHAR:
                case Types.NCHAR:
                case Types.NVARCHAR :
                    pstmt.setString(i, sVal);
                    break;

                case Types.DOUBLE:
                	if(!ifFloat(sVal))
                	{
                		sReturnMsg="[EXECDML] [Error] Value of "+sVal+" invalide for pos[" +i + "]";
                    	iReturnCode=BUNDOCOURSERROR;
                    	return null;
                	}
                	Double dv=Double.valueOf(sVal);
                    pstmt.setDouble(i, dv.doubleValue());
                    break;
                    
                case Types.FLOAT:
                	if(!ifFloat(sVal))
                	{
                		sReturnMsg="[EXECDML] [Error] Value of "+sVal+" invalide for pos[" +i + "]";
                    	iReturnCode=BUNDOCOURSERROR;
                    	return null;
                	}
                	Float fv=Float.valueOf(sVal);
                    pstmt.setFloat(i, fv.floatValue());
                    break;
                    
                case Types.INTEGER:
                case Types.SMALLINT:
                case Types.TINYINT:
                case Types.BIT :
                	if(!ifInteger(sVal))
                	{
                		sReturnMsg="[EXECDML] [Error] Value of "+sVal+" invalide for pos[" +i + "]";
                    	iReturnCode=BUNDOCOURSERROR;
                    	return null;
                	}
                	Integer iv=Integer.valueOf(sVal);
                    pstmt.setInt(i,iv.intValue());
                    break;
                default :
                    pstmt.setString(i, sVal);
                    break;
                }
            }
            catch (SQLException e)
            {
            	sReturnMsg="[EXECDML] [Error]Can't matching type while binding due to [" + dmlObj.getDmlId() + "]";
            	iReturnCode=BUNDOCOURSERROR;
            	return null;
            }
        }
        try
        {
        	rs = pstmt.executeQuery();
        }
        catch(SQLException e)
        {
        	sReturnMsg="[EXECDML] [Error]DMLID[" + dmlObj.getDmlId() + "] Cause:[" + e.getMessage() + "]";
        	iReturnCode=EXECOCOURSERROR;
        	return null;
        }
        sReturnMsg= String.format("[EXECDML] [Succs]DMLID[%04d] excute ok", dmlObj.getDmlId());
        iReturnCode=0;
        return rs;
    }

    /** delPstmt</br>
     * -release PreparedStatement after query-dml
     * @version v1.0.160906
     * @param null
     * @return boolean-true when success and boolean-fail after failed
     */
    public boolean delPstmt() throws SQLException
    {
        if(pstmt != null)
        {
            pstmt.close();
        }
        return true;
    }

    /** execDML</br>
     * -mysql query dml excute
     * @version v1.0.160906
     * @param iDML Id of dml
     * @param sBinds Bind argcs
     * @return counts of effective rows
     */
    public int execDML(int iDML, int iCount,String ... sBinds) throws NumberFormatException, Exception
    {
        int i = 0;
        int iRet = 0;
        dmlObj=null;
               
        if(null!=EvnInit.getDML(String.valueOf(iDML)))
        {
        	dmlObj=EvnInit.getDML(String.valueOf(iDML));
        }
        else
        {
        	sReturnMsg="[EXECDML] [Error]invalide dml for [" + iDML + "]";
        	iReturnCode=PRESQLOCOURSEERROR;
            return -1;
        }
        if(dmlObj.getDmlType() != 2 && dmlObj.getDmlType() != 3 && dmlObj.getDmlType() != 4)
        {
            sReturnMsg="[EXECDML] [Error]invalide dml type for [" + dmlObj.getDmlId() + "]";
            iReturnCode=PRESQLOCOURSEERROR;
            return -1;
        }
        String sSql = dmlObj.getSql();
        pstmt = connObj.prepareStatement(sSql);

        for (String sVal : sBinds)
        {
            i++;
            if(i > dmlObj.getBundNum())
            {
                sReturnMsg="[EXECDML] [Error]Bind parameters number no matching [" + dmlObj.getDmlId() + "][" + i + "." + dmlObj.getBundNum() + "]";
                iReturnCode=PRESQLOCOURSEERROR;
                return -1;
            }
            try
            {
                switch (dmlObj.getBindInput(i))
                {
                case Types.BIGINT:
                case Types.BOOLEAN:
                case Types.DATE:
                case Types.TIME:
                case Types.TIMESTAMP:
                case Types.VARCHAR:
                case Types.NCHAR:
                case Types.NVARCHAR :
                    pstmt.setString(i, sVal);
                    break;

                case Types.DOUBLE:
                	if(!ifFloat(sVal))
                	{
                		sReturnMsg="[EXECDML] [Error] Value of "+sVal+" invalide for pos[" +i + "]";
                    	iReturnCode=BUNDOCOURSERROR;
                    	return -999999;
                	}
                	Double dv=Double.valueOf(sVal);
                    pstmt.setDouble(i, dv.doubleValue());
                    break;
                    
                case Types.FLOAT:
                	if(!ifFloat(sVal))
                	{
                		sReturnMsg="[EXECDML] [Error] Value of "+sVal+" invalide for pos[" +i + "]";
                    	iReturnCode=BUNDOCOURSERROR;
                    	return -999999;
                	}
                	Float fv=Float.valueOf(sVal);
                    pstmt.setFloat(i, fv.floatValue());
                    break;
                    
                case Types.INTEGER:
                case Types.SMALLINT:
                case Types.TINYINT:
                case Types.BIT :
                	if(!ifInteger(sVal))
                	{
                		sReturnMsg="[EXECDML] [Error] Value of "+sVal+" invalide for pos[" +i + "]";
                    	iReturnCode=BUNDOCOURSERROR;
                    	return -999999;
                	}
                	Integer iv=Integer.valueOf(sVal);
                    pstmt.setInt(i,iv.intValue());
                    break;
                default :
                    pstmt.setString(i, sVal);
                    break;
                }
            }
            catch (SQLException e)
            {
                sReturnMsg="[EXECDML] [Error]DMLID[" + dmlObj.getDmlId() + "] Cause:[" + e.getMessage() + "]";
                iRet=-999999;
                iReturnCode=BUNDOCOURSERROR;
                break;
            }
        }
        try
        {
        	if(0==iRet)
        	{
        		iRet = pstmt.executeUpdate();
        	}
        }
        catch(SQLException e)
        {
        	sReturnMsg="[EXECDML] [Error]DMLID[" + dmlObj.getDmlId() + "] Cause:[" + e.getMessage() + "]";
        	iReturnCode=EXECOCOURSERROR;
        	iRet=-999998;
        }
        finally
        {
        	pstmt.close();
        	pstmt=null;
        }
        
        if(iRet > 0 || iRet == 0)
        {
        	sReturnMsg = String.format("[EXECDML] [Succs]DMLID[%04d] excute ok", dmlObj.getDmlId());
        }
        else
        {
            sReturnMsg="[EXECDML] [Error]DMLID[" + dmlObj.getDmlId() + "],excute error[" + iRet + "]";
        }
        return iRet;
    }

    /** initConn</br>
     * -init connection of mysql
     * @version v1.0.160906
     * @param null
     * @return void
     */
    private void initConn()
    {
        try
        {
            Class.forName(driver);
            connObj = (Connection) DriverManager.getConnection(url, username, password);
            connObj.setAutoCommit(EvnInit.getConsoleType());
        }
        catch (ClassNotFoundException e)
        {
        	connObj=null;
        }
        catch (SQLException e)
        {
        	connObj=null;
        }
    }
    
    /** initConn</br>
     * -init connection of mysql
     * @version v1.0.160906
     * @param 
     * auto_manul mysql-connection: automatic transaction or not(manual transaction)
     * @return void
     */
    private void initConn(boolean auto_manul)
    {
        try
        {
            Class.forName(driver);
            connObj = (Connection) DriverManager.getConnection(url, username, password);
            connObj.setAutoCommit(auto_manul);
        }
        catch (ClassNotFoundException e)
        {
        	connObj=null;
        }
        catch (SQLException e)
        {
        	connObj=null;
        }
    }
    
    private static boolean initConnCfg()
    {
    	String _sLIMIT=EvnInit.getDBObj("LIMIT");
    	url = EvnInit.getDBObj("URL");
        username = EvnInit.getDBObj("USR");
        password = EvnInit.getDBObj("PWD");
        
        if(null==_sLIMIT 
        || null==url 
        || null==username 
        || null==password )
        {
        	return false;
        }
        
        try
        {
        	iMaxConTimes=Integer.parseInt(_sLIMIT) ;
        }
        catch(NumberFormatException e)
        {
        	return false;
        }
    	return true;
    }
    
    private static int getSessNumber()
    {
    	return (iTimes<0?0:iTimes);
    }
    
    private static void setSessNumber(int _iTimes)
    {
    	iTimes=(_iTimes<0?0:_iTimes);
    }

    /** getDBState:</br>
     * Get connection state of mysql and init connection due zero connection
     * @version v1.0.160906
     * @return boolean-true after first connection or connection times less than max</br>boolean-false after connect faild or larger than max
     */
    public boolean getDBState()
    {
    	try
    	{
    		int times =0;
        	lock.lock();
        	times=getSessNumber();
        	connObj=null;
        	statMnt=null;
        	
            if(0 == times && !initConnCfg())
            {
            	return false;
            }
            if(times < iMaxConTimes)
            {
            	initConn();
            	if(null == connObj)
                {
                    return false;
                }
            	
            	try
            	{
            		statMnt = connObj.createStatement();
            	}
            	catch(SQLException e)
            	{
            		try
        			{
        				connObj.close();
        			}
        			catch(SQLException e1)
        			{
        				
        			}
            		
            		statMnt=null;
            		connObj=null;
                    return false;
            	}
            	
            	if(null == statMnt)
                {
        			try
        			{
        				connObj.close();
        			}
        			catch(SQLException e)
        			{
        				
        			}
        			connObj=null;
                    return false;
                }
            	times +=1;
            	setSessNumber(times);
            	return true;
            }
            else
            {
            	return false;
            }
    	}
    	finally
    	{
    		lock.unlock();
    	}
    }

    /** getDBState:</br>
     * Get connection state of mysql and init connection due zero connection
     * @version v1.0.160906
     * @param auto_manul mysql-connection: automatic transaction or not(manual transaction)
     * @return boolean-true after first connection or connection times less than max</br>boolean-false after connect faild or larger than max
     */
    public boolean getDBState(boolean auto_manul) throws SQLException
    {
    	try
    	{
    		lock.lock();
        	int times = getSessNumber();
        	connObj=null;
        	statMnt=null;
        	
        	if(0 == times && !initConnCfg())
            {
            	return false;
            }
            
            if(times < iMaxConTimes)
            {
            	initConn(auto_manul);
            	if(null == connObj)
                {
                    return false;
                }
            	
            	try
            	{
            		statMnt = connObj.createStatement();
            	}
            	catch(SQLException e)
            	{
            		try
        			{
        				connObj.close();
        			}
        			catch(SQLException e1)
        			{
        				
        			}
            		
            		statMnt=null;
            		connObj=null;
                    return false;
            	}
            	
            	if(null == statMnt)
                {
        			try
        			{
        				connObj.close();
        			}
        			catch(SQLException e)
        			{
        				
        			}
        			connObj=null;
                    return false;
                }
            	
            	times +=1;
            	setSessNumber(times);
            	return true;
            }
            else
            {
            	return false;
            }
    	}
    	finally
    	{
    		lock.unlock();
    	}
    }
    
    /** delDBState</br>
     * -close current session of mysql
     * @version v1.0.160906
     * @param null
     * @return boolean-true when success</br>boolean-fail after failed
     */
    public void delDBState()
    {
    	try
    	{
    		lock.lock();
    		if(statMnt != null)
            {
                try
                {
                    statMnt.close();
                }
                catch(SQLException e)
                {
                    e.printStackTrace();
                }
            }
            if(connObj != null)
            {
                try
                {
                    connObj.close();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            setSessNumber(getSessNumber()-1);
    	}
        finally
        {
        	lock.unlock();
        }
    }

    /** getStatement</br>
     * -return current Statement of mysql
     * @version v1.0.160906
     * @param null
     * @return Statement that already be init-ed and exists
     */
    public Statement getStatement()
    {
        return statMnt;
    }

    /** getStatement</br>
     * -return current connection of mysql
     * @version v1.0.160906
     * @param null
     * @return Connection that already be init-ed and exists
     */
    public Connection getConn()
    {
        return connObj;
    }
    
    /** getMessage</br>
     * -return current connection of mysql
     * @version v1.0.160906
     * @param null
     * @return sReturnMsg
     */
    public String getMessage()
    {
        return sReturnMsg;
    }
    
    /** getExecCode</br>
     * -return current connection of mysql
     * @version v1.0.160906
     * @param null
     * @return sReturnMsg
     */
    public int getExecCode()
    {
        return iReturnCode;
    }
    
    private static boolean ifInteger(String _sVal)
    {
    	int i = 0;
    	if(_sVal.charAt(0)=='-')
    	{
    		i=1;
    	}
    	for (; i < _sVal.length(); i++)
    	{
    		if("0123456789".indexOf(_sVal.charAt(i))<0)
    		{
    			return false;
    		}
    	}
    	return true;
    }
    
    private static boolean ifFloat(String _sVal)
    {
    	int i=0;
    	int cnt=0;
    	if(_sVal.charAt(0)=='-')
    	{
    		i=1;
    	}
    	
    	if(_sVal.indexOf('.')<0 || _sVal.indexOf('.')==i )
    	{
    		return false;
    	}
    	
    	for (; i < _sVal.length(); i++)
    	{
    		if('.'==_sVal.charAt(i) && (++cnt)>1)
    		{
    			return false;
    		}
    		if("0123456789.".indexOf(_sVal.charAt(i))<0)
    		{
    			return false;
    		}
    	}
    	return true;
    }
}
