/** package note
 */
package com.frame.protocol;

/** class definition
 * @version v1.0.160816
 */
public class UnitFrame 
{
	private int iSvcId;
	private int iRepit;
	private String sSvcCode;
	private String sVersion;
	private String sClassId;
	private String sClassPath;
	private String sClassDesc;
	private Class<? extends UnitFrameProtocol> obj;

	public void reSet()
	{
		iSvcId=0;
		iRepit=0;
		sSvcCode="";
		sVersion="";
		sClassId="";
		sClassPath="";
		sClassDesc="";
		obj=null;
	}
	
	public int getSvcId(){return iSvcId;}
	public int getRepit(){return iRepit;}
	public String getSvcCode(){return sSvcCode;}
	public String getVersion(){return sVersion;}
	public String getClassId(){return sClassId;}
	public String getClassPath(){return sClassPath;}
	public String getClassDesc(){return sClassDesc;}
	public Class<? extends UnitFrameProtocol> getObj(){return obj;}
	
	public void setSvcId(int _iSvcId){ iSvcId=_iSvcId;}
	public void setRepit(int _iRepit){ iRepit=_iRepit;}
	public void setSvcCode(String _sSvcCode){ sSvcCode=_sSvcCode;}
	public void setVersion(String _sVersion){ sVersion=_sVersion;}
	public void setClassId(String _sClassId){ sClassId=_sClassId;}
	public void setClassPath(String _sClassPath){ sClassPath=_sClassPath;}
	public void setClassDesc(String _sClassDesc){ sClassDesc=_sClassDesc;}
	public void setObj(Class<? extends UnitFrameProtocol> _obj){ obj=_obj;}
}
