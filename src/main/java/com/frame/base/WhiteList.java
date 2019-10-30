package com.frame.base;

public class WhiteList 
{
	private int iWhtId=0;  
	private String sCtrlCode="";
	private String sIpv4="";
	private String sDesc="";
	private boolean ifValide=false;
	
	public int     getWhtId(){return 	iWhtId;}
	public String  getCtrlCode(){return sCtrlCode;}
	public String  getIpv4(){return 	sIpv4;}
	public String  getDesc(){return 	sDesc;}
	public boolean getValide(){return 	ifValide;}
	
	public void    setWhtId(int _iWhtId){iWhtId=new Integer(_iWhtId);}
	public void    setCtrlCode(String _sCtrlCode){sCtrlCode=new String(_sCtrlCode);}
	public void    setIpv4(String _sIpv4){sIpv4=new String(_sIpv4);}
	public void    setDesc(String _sDesc){sDesc=new String(_sDesc);}
	public void    setValide(boolean _ifValide){ifValide=new Boolean(_ifValide);}
	
}
