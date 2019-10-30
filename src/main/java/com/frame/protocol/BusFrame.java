package com.frame.protocol;

import java.util.HashMap;


public class BusFrame 
{
	private int iBusId;
	private int iCount;
	private String sBusCode;
	private String sBusVers;
	private String sBusDesc;
	private HashMap<Integer,Integer> mOrder=null;// <SVC_ORDER,SVC_ID>
	
	public void reSet()
	{
		iBusId=0;
		iCount=0;
		sBusCode="";
		sBusVers="";
		sBusDesc="";
		if(null!=mOrder)
		{
			mOrder.clear();
		}
	}
	
	public void setBusId(int _iBusId){iBusId=_iBusId;}
	public void setBusCode(String _sBusCode){sBusCode=_sBusCode;}
	public void setBusVers(String _sBusVers){sBusVers=_sBusVers;}
	public void setBusDesc(String _sBusDesc){sBusDesc=_sBusDesc;}
	public void setOrder(int _iOrderId,int _iSvcId)
	{
		if(null==mOrder)
		{
			mOrder=new HashMap<Integer,Integer>();
		}
		mOrder.put(_iOrderId, _iSvcId);
		iCount++;
	}
	
	public int getBusId(){return iBusId;}
	public int getCount(){return iCount;}
	public String getBusCode(){return sBusCode;}
	public String getBusVers(){return sBusVers;}
	public String getBusDesc(){return sBusDesc;}
	
	public int getSvcId(int iOrderId)
	{
		if(iOrderId>(iBusId*100+iCount))
		{
			return -1;
		}
		return mOrder.get(iOrderId);
	}
	
}
