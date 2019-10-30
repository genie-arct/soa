package com.frame.protocol;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.frame.base.EvnInit;
import com.frame.base.MysqlConn;
import com.frame.protocol.BusFrame;
import com.frame.protocol.UnitFrame;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class EngineFrame 
{
	private static final int iDML_Load_Unit=9997;
	private static final int iDML_Load_Bus=9999;
	private static final int iDML_Load_Rel=9998;
	private static final int iDML_Load_Adapter=9996;
	private static MysqlConn sqlConn = null;
	private static int iAssemblyCount=0;
	
	private static HashMap<Integer,UnitFrame> mUnitMap=new HashMap<Integer,UnitFrame>(); //<svcid,UnitFrame>
	private static HashMap<String,BusFrame>  mBusMap =new HashMap<String,BusFrame >();//<sBusCode+sBusVers,BusFrame>
	private static HashMap<String,AdapterFrame>  mAdptMap =new HashMap<String,AdapterFrame >();//<adapter_id,AdapterFrame>
	
	public static HashMap<String,BusFrame> getAllBus()
	{
		return mBusMap;
	}
	
	public static HashMap<Integer,UnitFrame> getAllUnit()
	{
		return mUnitMap;
	}
	
	public static HashMap<String,AdapterFrame> getAllAdapter()
	{
		return mAdptMap;
	}
	
	// 1.query class
	private static int getAssembly(String _sSvcCode,String _sVersion) throws SQLException, MalformedURLException, ClassNotFoundException
	{
		int iSvcId=0;
		String sSvcCode="";
		String sSvcVers="";
		String sClassName="";
		String sClassFile="";
		String sSvcDesc="";
		ResultSet rs=null;
		UnitFrame uf=null;
		
		rs=sqlConn.execDML(iDML_Load_Unit,_sSvcCode,_sVersion);
		if(null==rs)
		{
			System.out.println(sqlConn.getMessage());
			return -1;
		}
		while(rs.next())
		{
			iSvcId=0;
			sSvcCode="";
			sSvcVers="";
			sClassName="";
			sClassFile="";
			sSvcDesc="";
			
			iSvcId=rs.getInt(1);
			sSvcCode=rs.getString(2);
			sSvcVers=rs.getString(3);
			sClassName=rs.getString(4);
			sClassFile=rs.getString(5);
			sSvcDesc=rs.getString(6);
			
			if(null!=mUnitMap.get(iSvcId))
			{
				uf=mUnitMap.get(iSvcId);
				if(1==uf.getRepit())
				{
					return iSvcId;
				}
				else
				{
					return -1;
				}
			}
			else
			{
				uf=new UnitFrame();
				try
				{
					String sLength="";
					File f = new File(EvnInit.getLibHome()+sClassFile);
					if (f.exists() && f.isFile())
					{
						sLength=String.valueOf(f.length());
						URL url = new URL("file:"+EvnInit.getLibHome()+sClassFile);
		                URLClassLoader myClassLoader = new URLClassLoader(new URL[] { url }, Thread.currentThread().getContextClassLoader());
		                @SuppressWarnings("unchecked")
						Class<? extends UnitFrameProtocol> myClass = (Class<? extends UnitFrameProtocol>) myClassLoader.loadClass(sClassName);

		            	uf.setSvcId(iSvcId);
		            	uf.setRepit(1);
		            	uf.setSvcCode(sSvcCode);
		            	uf.setVersion(sSvcVers);
		            	uf.setClassId(sClassName);
		            	uf.setClassPath(EvnInit.getLibHome()+sClassFile);
		            	uf.setClassDesc(sSvcDesc);
		            	uf.setObj(myClass);
		            	mUnitMap.put(iSvcId, uf);
		            	iAssemblyCount++;
		            	System.out.println("[FS][SUCCES]:S"+iSvcId+"."+sSvcCode+"."+sSvcVers+" Loaded ==> "+sClassName+"==>"+sClassFile+"("+sLength+" bytes) ");
		            	return iSvcId;
					}
					else
					{
						System.out.println("[FS][FAILED]:S"+iSvcId+"."+sSvcCode+"."+sSvcVers+" Loaded failed "+sClassName+"-->"+sClassFile+"(not exists)");
						return -1;
					}
				}
				catch (ClassNotFoundException e)
		        {
					System.out.println("[FS][FAILED]:S"+iSvcId+"."+sSvcCode+"."+sSvcVers+" Loaded failed "+sClassName+"|"+sClassFile+":ClassNotFoundException");
					return -1;
		        }
				catch (MalformedURLException e)
		        {
					System.out.println("[FS][FAILED]:S"+iSvcId+"."+sSvcCode+"."+sSvcVers+" Loaded failed "+sClassName+"|"+sClassFile+":MalformedURLException");
					return -1;
		        }
			}
		} 
		return -1;
	}
	
	// 2.query relationship with assembly
	private static HashMap<Integer,Integer> getRelation(int _iBusId) throws SQLException, MalformedURLException, ClassNotFoundException
	{
		int iOrdId=0;
		int iOrdIdx=_iBusId*100;
		int iSvcId=0;
		String sSvcCode="";
		String sSvcVers="";
		ResultSet rs=null;
		HashMap<Integer,Integer> mOrder=new HashMap<Integer,Integer>();
		mOrder.clear();
		
		rs=sqlConn.execDML(iDML_Load_Rel,String.valueOf(_iBusId));
		if(null==rs)
		{
			System.out.println(sqlConn.getMessage());
			return mOrder;
		}
		while(rs.next())
		{
			iOrdId=0;
			iSvcId=0;
			sSvcCode="";
			sSvcVers="";
			
			iOrdIdx++;
			iOrdId=rs.getInt(1);
			sSvcCode=rs.getString(3);
			sSvcVers=rs.getString(4);
			if(iOrdIdx!=iOrdId)
			{
				System.out.println("[TP][FAILED]:B"+_iBusId+"."+sSvcCode+"."+sSvcVers+" Invalide");
				mOrder.clear();
				break;
			}
			
			iSvcId=getAssembly(sSvcCode,sSvcVers);
			if(iSvcId>0)
			{
				mOrder.put(iOrdId,iSvcId);
				//System.out.println(mOrder.size()+":"+iOrdId+"-"+ iSvcId);
			}
			else
			{
				System.out.println("[TP][FAILED]:B"+_iBusId+"."+sSvcCode+"."+sSvcVers+" Incomplete configuration");
				mOrder.clear();
				break;
			}
		} 
		return mOrder;
	}
	
	// 3.DRIVER
	public static boolean driver() throws SQLException, MalformedURLException, ClassNotFoundException
	{
		int iRowCnt=0;
		int iBusId=0;
		String sBusCode="";
		String sBusVers="";
		String sBusDesc="";
		BusFrame bf=null;
		ResultSet rs=null;
		HashMap<Integer,Integer> mOrder=null;
		
		mUnitMap.clear();
		mBusMap.clear();
		sqlConn = new MysqlConn();
		if(!sqlConn.getDBState())
        {
            System.out.println("[DBRoute] config invalide:can't be resolved");
            return false;
        }
		
		rs=sqlConn.execDML(iDML_Load_Bus);
		if(null==rs)
		{
			System.out.println(sqlConn.getMessage());
			return false;
		}
		while(rs.next())
		{
			iBusId=0;
			sBusCode="";
			sBusVers="";
			sBusDesc="";
			mOrder=null;
			
			iBusId=rs.getInt(1);
			sBusCode=rs.getString(2);
			sBusVers=rs.getString(3);
			sBusDesc=rs.getString(4);
			mOrder=getRelation(iBusId);
			
			if(0!=mOrder.size())
			{
				bf=new BusFrame();
				bf.setBusCode(sBusCode);
				bf.setBusDesc(sBusDesc);
				bf.setBusId(iBusId);
				bf.setBusVers(sBusVers);
				for(Entry<Integer, Integer> entry : mOrder.entrySet())
				{
					//System.out.println(mOrder.size()+":"+entry.getKey()+"-"+ entry.getValue());
					bf.setOrder(entry.getKey(), entry.getValue());
				}
				
				mBusMap.put(sBusCode+sBusVers,bf);
				iRowCnt++;
				System.out.println("[TP][SUCCES]:B"+iBusId+"."+sBusCode+"."+sBusVers+" loaded");
			}
			else
			{
				System.out.println("[TP][FAILED]:B"+iBusId+"."+sBusCode+"."+sBusVers+" incomplete configuration");
			}
		} 
		sqlConn.delPstmt();
		if(0==iRowCnt)
		{
			System.out.println("[DR][FAILED]:None frame bus be configured !!!");
			return false;
		}
		else
		{
			System.out.println("[DR][SUCCES]:Bus counts "+iRowCnt+",Assembly counts "+iAssemblyCount);//Count
			boolean _if_=drAdapter();
			sqlConn.delDBState();
			return _if_;
		}
	}
	
	// 4.get bus frame
	public static BusFrame getBusFrame(String _sBusCode,String _sBusVersion)
	{
		return mBusMap.get(_sBusCode+_sBusVersion);
	}
	
	// 5.get frame unit
	public static UnitFrame getFrameUnit(int _iSvcId)
	{
		return mUnitMap.get(_iSvcId);
	}
	
	// 6.return adapter_id 
	public static int getAdapterId(JsonObject obj)
	{
		if(0==mAdptMap.size())
		{
			return -1;
		}
			
		for(Map.Entry<String,AdapterFrame> entry : mAdptMap.entrySet())
		{
			if(entry.getValue().checkApdt(obj))
			{
				return entry.getValue().getAdpId();
			}
		}
		return -1;
	}
	
	// 7.return adapter busid
	public static String getAdapterBusCode(int _iAdapterId)
	{
		return mAdptMap.get(String.valueOf(_iAdapterId)).getBusCode();
	}
	
	// 8.return adapter version
	public static String getAdapterBusVers(int _iAdapterId)
	{
		return mAdptMap.get(String.valueOf(_iAdapterId)).getBusVers();
	}
	
	// 9.ADAPTER
	private static boolean drAdapter() throws SQLException
	{
		ResultSet rs=null;
		int iRowCnt=0;
		int iAdptId=0;
		String sBusCode="";
		String sBusVers="";
		String sCondition="";
		AdapterFrame af=null;
		mAdptMap.clear();
		rs=sqlConn.execDML(iDML_Load_Adapter);
		if(null==rs)
		{
			System.out.println(sqlConn.getMessage());
			return false;
		}
		while(rs.next())
		{
			iAdptId=0;
			sCondition="";
			sBusCode="";
			sBusVers="";
			iAdptId=rs.getInt(1);
			sBusCode=rs.getString(2);
			sBusVers=rs.getString(3);
			sCondition=rs.getString(4);
			
			if(chkCondition(sCondition))
			{
				af=new AdapterFrame();
				af.setAdpCon(sCondition);
				af.setAdpTid(iAdptId);
				af.setBusCode(sBusCode);
				af.setBusVers(sBusVers);
				mAdptMap.put(String.valueOf(iAdptId), af);
				iRowCnt++;
				System.out.println("[AP][SUCCES]:A"+iAdptId+"|"+sBusCode+"."+sBusVers+" loaded");
			}
			else
			{
				System.out.println("[AP][FAILED]:A"+iAdptId+"|"+sBusCode+"."+sBusVers+" invalide condition");
			}
		} 
		sqlConn.delPstmt();
		System.out.println("[AR][SUCCES]:Adapter counts "+iRowCnt); //Count
		return true;
	}
	
	// 10 check adapter json-condition-string
	private static boolean chkCondition(String strJson)
	{
		try
        {
			JsonObject jsonObj= new JsonParser().parse(strJson).getAsJsonObject();
			Set<Entry<String,JsonElement>> set=jsonObj.entrySet();			
			Iterator<Entry<String,JsonElement>> it = set.iterator();		
			while(it.hasNext())
			{
				Entry<String,JsonElement> ent=it.next();
				if(!(ent.getValue().isJsonPrimitive()))
				{
					return false;
				}
			}
        }
        catch(IllegalStateException e)
        {
        	return false;
        }
		return true;
	}
	
	// 11 get adapter config
	public static AdapterFrame getAdapterCfg(String _sAdapterId)
	{
		return mAdptMap.get(_sAdapterId);
	}
}