/** package note
 *  config initial
 *  根据配置文件xxx.json初始化环境配置
 *  - 数据库登录配置
 *  - MySql语句配置
 */
package com.frame.base;
/** import plugins
 */
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mysql.jdbc.Statement;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.log4j.PropertyConfigurator;

/** class definition
 * 后续可优化为公共对象,便于继承
 * @version v1.0.160816
 */
public class EvnInit
{
    private static int hostId = 0;
    private static int state = 0; //0:启动中;1:运行时
    private static int iMaxDBConnTimes = 0;
    //private int    dbState = 0;//0:无连接;每增加一次连接,该成员自加1
    private static final String sEnviromentLibraryName = "lib_home";
    private static final String sEnviromentDataHome = "dat_Home";
    private static String sDataHome = "";
    private static String sServiceHome = "";
    private static String sLibararyHome = "";
    private static String consoleType = ""; // 0-automatic transaction;1-manual transaction
    private static String listenPort = "";
    private static String userName = "";
    private static String passWord = "";
    private static String configStr = ""; // do not delete any space
    private static Map < String, DMLList > cfgFile = null;
    private static Map < String, WhiteList > whtFile = null;
    static MysqlConn sqlConn = null;

    /*load config file*/
    private static boolean loadCfg()
    {
        // 2.load json config
        String FileName = sServiceHome + "/config/config.json";
        File file = new File(FileName);

        //System.out.println("Server init begin load config file:" + FileName);
        if (!file.exists())
        {
            System.out.println("[SERV_FILE_CONFIG]:" + FileName + " (does`t exsists)");
            return false;
        }
        else
        {
            if (!file.isFile())
            {
                System.out.println("[SERV_FILE_CONFIG]:" + FileName + " (not file)");
                return false;
            }
            else
            {
                System.out.println("[SERV_FILE_CONFIG]:" + FileName);
                FileReader fr = null;
                BufferedReader br = null;
                try
                {
                    fr = new FileReader(FileName);
                    br = new BufferedReader(fr);
                    String str = br.readLine();

                    while (str != null)
                    {
                        configStr += str;
                        str = br.readLine();
                    }
                }
                catch (FileNotFoundException e)
                {
                    System.out.println("[SERV_FILE_CONFIG]:" + FileName + " (" + e.getCause() + ")");
                    return false;
                }
                catch (IOException e)
                {
                    System.out.println("[SERV_FILE_CONFIG]:" + FileName + " (" + e.getCause() + ")");
                    return false;
                }
                finally
                {
                    try
                    {
                        if(null != br)
                        {
                            br.close();
                            br = null;
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    br = null;
                    try
                    {
                        if(null != fr)
                        {
                            fr.close();
                            fr = null;
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    fr = null;
                }
                return true;
            }
        }
    }

    /*load Host*/
    private static boolean loadHost(JsonElement elem)
    {
        if (elem.isJsonPrimitive())
        {
            hostId = elem.getAsInt();
            System.out.println("[HostCfg] HostId=" + hostId);
            return true;
        }
        else
        {
            System.out.println("[HostCfg] config invalide!!!");
            return false;
        }
    }

    /*load log*/
    private static boolean loadLog(JsonElement elem)
    {
    	if(null==elem)
    	{
    		System.out.println("[Log4jBoot] not configured!!!");
            return false;
    	}
    	
        if (elem.isJsonPrimitive())
        {
            String sLog4jBoot = null;
            sLog4jBoot = elem.getAsString();

            File file = new File(sLog4jBoot);
            if (file.exists() && file.isFile())
            {
                PropertyConfigurator.configure(sLog4jBoot);
            }
            else
            {
                System.out.println("[SERV_FILE_CONFIG]:" + sLog4jBoot + " (does`t exsists or not file)");
                return false;
            }

            System.out.println("[Log4jBoot] " + sLog4jBoot);
            return true;
        }
        else
        {
            System.out.println("[Log4jBoot] config invalide!!!");
            return false;
        }
    }

    /*load Enviroment*/
    private static boolean loadHome(JsonElement elem)
    {
        //File file=new File(filepath);
        if (elem.isJsonObject())
        {
            JsonObject obj = elem.getAsJsonObject();
            // Libarary_Home
            if (null == obj.get(sEnviromentLibraryName) || !obj.get(sEnviromentLibraryName).isJsonPrimitive())
            {
                System.out.println("[Envicfg] config of [" + sEnviromentLibraryName + "]invalide!!!");
                return false;
            }
            else
            {
                sLibararyHome = obj.get(sEnviromentLibraryName).getAsString();
                if (!(new File(sLibararyHome)).exists())
                {
                    System.out.println(String.format("[Envicfg] %s=%s(invalide,not exists)", sEnviromentLibraryName, sLibararyHome));
                    return false;
                }
            }

            // Data_Home
            if (null == obj.get(sEnviromentDataHome) || !obj.get(sEnviromentDataHome).isJsonPrimitive())
            {
                System.out.println("[Envicfg] config of [" + sEnviromentDataHome + "]invalide!!!");
                return false;
            }
            else
            {
                sDataHome = obj.get(sEnviromentDataHome).getAsString();
                if (!(new File(sDataHome)).exists())
                {
                    System.out.println(String.format("[Envicfg] %s=%s(invalide,not exists)", sEnviromentDataHome, sDataHome));
                    return false;
                }
                System.setProperty(sEnviromentDataHome, sDataHome);
            }
            System.out.println(String.format("[Envicfg] %s=%s;%s=%s",
                                             sEnviromentDataHome, sDataHome,
                                             sEnviromentLibraryName, sLibararyHome));
            System.out.println("[Envicfg] Resolved successfully!");
            return true;
        }
        else
        {
            System.out.println("[Envicfg] config invalide,useing default!!!");
            return false;
        }
    }

    /*load DML*/
    private static boolean loadDML(JsonElement elem)
    {
        ResultSet selectRes = null;
        Statement stmt = null;
        String sDMLList = "";
        if (!elem.isJsonPrimitive())
        {
            System.out.println("[DMLList] config invalide!!!");
            return false;
        }
        else
        {
            sDMLList = elem.getAsString();
            cfgFile = new HashMap < String, DMLList > ();
            cfgFile.clear();
        }
        stmt = (Statement) sqlConn.getStatement();
        if(null == stmt)
        {
            return false;
        }
        try
        {
            selectRes = stmt.executeQuery(sDMLList);
        }
        catch (SQLException e)
        {
            stmt = null;
            selectRes = null;
            return false;
        }

        try
        {
            while (selectRes.next())
            {
                DMLList dmlValue = new DMLList();
                dmlValue.reSet();
                dmlValue.setDmlId(selectRes.getInt(1));
                dmlValue.setSql(selectRes.getString(2));
                dmlValue.setDmlType(selectRes.getInt(3));
                dmlValue.setBundNum(selectRes.getInt(4));
                dmlValue.setColNum(selectRes.getInt(5));
                if (0 != dmlValue.getBundNum())
                {
                    dmlValue.setSqlExe(selectRes.getString(6));
                }
                cfgFile.put(String.valueOf(dmlValue.getDmlId()), dmlValue);
            }
        }
        catch (SQLException e)
        {
            try
            {
                selectRes.close();
            }
            catch (SQLException e1)
            {
                cfgFile.clear();
            }
            selectRes = null;
            stmt = null;
            cfgFile.clear();
            return false;
        }

        try
        {
            selectRes.close();
            selectRes = null;
            stmt = null;
        }
        catch (SQLException e1)
        {
            cfgFile.clear();
            selectRes = null;
            stmt = null;
            return false;
        }
        if(!loadBund())
        {
            return false;
        }
        System.out.println("[DMLList] DML loaded success,count:" + cfgFile.size());
        return true;
    }

    private static boolean loadBund()
    {
        for (Map.Entry < String, DMLList > entry : cfgFile.entrySet())
        {
            if (0 != entry.getValue().getBundNum())
            {
                Statement stmt = null;
                ResultSet rs = null;
                ResultSetMetaData md = null;
                stmt = (Statement) sqlConn.getStatement();

                try
                {
                    rs = stmt.executeQuery(entry.getValue().getSqlExe());
                }
                catch (SQLException e)
                {
                    rs = null;
                    stmt = null;
                    cfgFile.clear();
                    return false;
                }

                try
                {
                    md = rs.getMetaData();
                }
                catch (SQLException e)
                {
                    try
                    {
                        rs.close();
                    }
                    catch (SQLException e1)
                    {
                    }
                    rs = null;
                    stmt = null;
                    cfgFile.clear();
                    return false;
                }
                if(null == md)
                {
                    try
                    {
                        rs.close();
                    }
                    catch (SQLException e1)
                    {
                    }
                    rs = null;
                    stmt = null;
                    cfgFile.clear();
                    return false;
                }
                try
                {
                    while (rs.next())
                    {
                        for (int col = 1; col <= md.getColumnCount(); col++)
                        {
                            entry.getValue().setBindInput(col, md.getColumnType(col));
                        }
                        break;
                    }
                }
                catch (SQLException e)
                {
                    try
                    {
                        rs.close();
                    }
                    catch (SQLException e1)
                    {
                    }
                    rs = null;
                    stmt = null;
                    cfgFile.clear();
                    return false;
                }

                try
                {
                    rs.close();
                }
                catch (SQLException e1)
                {
                    rs = null;
                    stmt = null;
                    cfgFile.clear();
                    return false;
                }
                rs = null;
                stmt = null;
            }
            System.out.println(String.format("[DMLList] DML loaded [%04d]", entry.getValue().getDmlId()));
        }
        return true;
    }

    /*load DBRoute*/
    private static boolean loadDBCfg(JsonElement elem)throws SQLException
    {
        if (elem.isJsonArray())
        {
            int i = 0;
            JsonArray arry = elem.getAsJsonArray();
            for (i = 0; i < arry.size(); i++)
            {
                JsonObject obj = arry.get(i).getAsJsonObject();
                if (hostId != obj.get("HostId").getAsInt())
                {
                    continue;
                }
                else
                {
                    consoleType = obj.get("ConsoleType").getAsString();
                    listenPort = obj.get("ListenPort").getAsString();
                    userName = obj.get("UserName").getAsString();
                    passWord = obj.get("PassWord").getAsString();
                    iMaxDBConnTimes = obj.get("MAXCONNS").getAsInt();
                    break;
                }
            }
            if (i > arry.size())
            {
                System.out.println("[DBRoute] config for tartget host " + hostId + "not found!!!");
                return false;
            }
        }
        else if (elem.isJsonObject())
        {
            JsonObject obj = elem.getAsJsonObject();
            consoleType = obj.get("ConsoleType").getAsString();
            listenPort = obj.get("ListenPort").getAsString();
            userName = obj.get("UserName").getAsString();
            passWord = obj.get("PassWord").getAsString();
            iMaxDBConnTimes = obj.get("MAXCONNS").getAsInt();
        }
        else
        {
            System.out.println("[DBRoute] config invalide!!!");
            return false;
        }

        if (0 != Integer.parseInt(consoleType) && 1 != Integer.parseInt(consoleType))
        {
            System.out.println("[DBRoute] config-consoleType invalide: 0-automatic transaction;1-manual transaction!!!");
            return false;
        }
        System.out.println("[DBRoute] "
                           + "ConsoleType=" + consoleType + ";"
                           + "ListenPort=" + listenPort + ";"
                           + "UserName=" + userName + ";"
                           + "PassWord=" + passWord + ";"
                           + "MAXCONNS=" + iMaxDBConnTimes + ".");
        return true;
    }

    private static boolean loadBridge()
    {
        return true;
    }

    private static boolean loadWht(JsonElement elem)
    {
        ResultSet selectRes = null;
        Statement stmt = null;
        String sWhtSql = "";
        if(null == elem)
        {
            System.out.println("[WHTList] miss config !!!");
            return false;
        }
        if (!elem.isJsonPrimitive())
        {
            System.out.println("[WHTList] invalide config !!!");
            return false;
        }
        else
        {
            sWhtSql = elem.getAsString();
            whtFile = new HashMap < String, WhiteList > ();
            whtFile.clear();
        }

        stmt = (Statement) sqlConn.getStatement();
        if(null == stmt)
        {
            System.out.println("[WHTList] db error !!!");
            return false;
        }
        try
        {
            selectRes = stmt.executeQuery(sWhtSql);
        }
        catch (SQLException e)
        {
            stmt = null;
            selectRes = null;
            System.out.println("[WHTList] db exception: " + e.getCause() + "!!!");
            return false;
        }

        try
        {
            while (selectRes.next())
            {
                WhiteList whtValue = new WhiteList();
                whtValue.setWhtId(selectRes.getInt(1));
                whtValue.setCtrlCode(selectRes.getString(2));
                whtValue.setIpv4(selectRes.getString(3));
                whtValue.setValide(1 == selectRes.getInt(4));
                whtValue.setDesc(selectRes.getString(5));
                whtFile.put(selectRes.getString(2) + "|" + selectRes.getString(3), whtValue);
                System.out.println(String.format("[WHTList] WHT loaded [%04d]", whtValue.getWhtId()));
            }
        }
        catch (SQLException e)
        {
            try
            {
                selectRes.close();
            }
            catch (SQLException e1)
            {
                whtFile.clear();
            }
            selectRes = null;
            stmt = null;
            whtFile.clear();
            System.out.println("[WHTList] db_course exception: " + e.getCause() + "!!!");
            return false;
        }

        try
        {
            selectRes.close();
            selectRes = null;
            stmt = null;
        }
        catch (SQLException e1)
        {
            whtFile.clear();
            selectRes = null;
            stmt = null;
            System.out.println("[WHTList] dbset_close exception: " + e1.getCause() + "!!!");
            return false;
        }
        System.out.println("[WHTList] WHT loaded success,count:" + whtFile.size());
        return true;
    }

    public static boolean checkWhtCfg(String _sCtrlCode, String _sIpv4)
    {
        if(null == whtFile.get(_sCtrlCode + "|" + _sIpv4))
        {
            String[] ipv4s = _sIpv4.split("\\.");
            for(int i = 0; i < 4; i++)
            {
                ipv4s[3 - i] = "*";
                if(null != whtFile.get(_sCtrlCode + "|" + ipv4s[0] + "." + ipv4s[1] + "." + ipv4s[2] + "." + ipv4s[3]))
                {
                    return whtFile.get(_sCtrlCode + "|" + ipv4s[0] + "." + ipv4s[1] + "." + ipv4s[2] + "." + ipv4s[3]).getValide();
                }
            }
            return false;
        }
        else
        {
            return whtFile.get(_sCtrlCode + "|" + _sIpv4).getValide();
        }
    }

    public static boolean driver(String _sRootHomeName)throws SQLException
    {
        sqlConn = new MysqlConn();
        JsonObject jsonObj = null;

        // FPAY_SERVICE_HOME
        if (null == System.getenv(_sRootHomeName))
        {
            System.out.println("[" + _sRootHomeName + "]:not configured");
            return false;
        }
        else
        {
            sServiceHome = System.getenv(_sRootHomeName);
            if ((new File(sServiceHome)).exists())
            {
                System.out.println("[" + _sRootHomeName + "]:" + sServiceHome);
            }
            else
            {
                System.out.println(String.format("[%s] %s(invalide,not exists)", _sRootHomeName, sServiceHome));
                return false;
            }
        }

        if (!loadCfg())
        {
            return false;
        }

        try
        {
            jsonObj = new JsonParser().parse(configStr).getAsJsonObject();
            if (null == jsonObj)
            {
                System.out.println("[SERV_JSON_CONFIG]:" + configStr + " (invalide json string)");
                return false;
            }
        }
        catch (JsonSyntaxException e)
        {
            System.out.println("[SERV_JSON_CONFIG]:" + configStr + " (" + e.getCause() + ")");
            return false;
        }
        System.out.println("[SERV_JSON_CONFIG]:" + configStr);

        if (!loadHost(jsonObj.get("HostId")))
        {
            return false;
        }

        if (!loadLog(jsonObj.get("Log4jBoot")))
        {
            return false;
        }

        if (!loadHome(jsonObj.get("EnvConfig")))
        {
            return false;
        }

        if (!loadDBCfg(jsonObj.get("DBConfig")))
        {
            return false;
        }

        if (!sqlConn.getDBState())
        {
            System.out.println("[DBRoute] config invalide:can't be resolved");
            return false;
        }
        else
        {
            System.out.println("[DBRoute] Resolved successfully!");
        }

        if (!loadDML(jsonObj.get("DMLList")))
        {
            return false;
        }

        if (!loadWht(jsonObj.get("WHTList")))
        {
            return false;
        }

        sqlConn.delDBState();

        if (!loadBridge())
        {
            System.out.println("[WXBridg] config init failed");
            return false;
        }
        return true;
    }

    /*get DML object*/
    public static DMLList getDML(String sDMLId)
    {
        return cfgFile.get(sDMLId);
    }

    /** getDBObj</br>
     * -Get connection statements of mysql due tag
     * @version v1.0.160906
     * @param sDBCfgTag tag of mysql statements</br>
     *  -"URL"  :mysql listen port</br>
     *  -"PWD"  :mysql password</br>
     *  -"USR"  :mysql username</br>
     *  -"LIMIT":mysql limit concurrent connection-times of mysql
     * @return string-value of target tag
     */
    public static String getDBObj(String sDBCfgTag)
    {
        if (sDBCfgTag.equals("URL"))
        {
            return listenPort;
        }
        else if (sDBCfgTag.equals("PWD"))
        {
            return passWord;
        }
        else if (sDBCfgTag.equals("USR"))
        {
            return userName;
        }
        else if (sDBCfgTag.equals("LIMIT"))
        {
            return String.valueOf(iMaxDBConnTimes);
        }
        else
        {
            return null;
        }
    }

    /** getConsoleType</br>
     * -return mysql-connection console type : automatic transaction or not(manual transaction)
     * @version v1.0.160906
     * @param
     *  null
     * @return boolean
     */
    public static boolean getConsoleType()
    {
        return 0 == Integer.parseInt(consoleType);
    }

    /*get server state*/
    public static int getServState()
    {
        int iState = state;
        if (0 == state)
        {
            state = 1;
        }
        return iState;
    }

    /*get libarary home*/
    public static String getLibHome()
    {
        return sLibararyHome;
    }
    /*get Service home*/
    public static String getSvcHome()
    {
        return sServiceHome;
    }
    /*get Data home*/
    public static String getDatHome()
    {
        return sDataHome;
    }
    /*get Host Id*/
    public static int getHostID()
    {
        return hostId;
    }

    /*get configStr*/
    public static String getconfigStr()
    {
        return configStr;
    }
}
