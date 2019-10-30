/** package note
 */
package com.frame.base;


import java.util.HashMap;
import java.util.Map;

/** class definition
 * @version v1.0.160816
 */
public class DMLList {
	private int iDmlType;
	private int iDmlId;
	private int iColNum;
	private int iBundNum;
	private String sSql;
	private String sSqlExe;
	
	private Map<Integer, Integer> bindInput=new HashMap<Integer, Integer>();
	//private Map<String, String> BindOutPut;

	public void reSet()
	{
		iDmlType=iDmlId=iColNum=iBundNum=0;
		sSql=null;
		if(bindInput!=null)
		{
			bindInput.clear();
		}
	}
	
	public int getDmlId(){return 	iDmlId;}
	public int getDmlType(){return 	iDmlType;}
	public int getColNum(){return 	iColNum;}
	public int getBundNum(){return 	iBundNum;}
	public String getSql(){return 	sSql;}
	public String getSqlExe(){return 	sSqlExe;}
	
	public int setDmlId(int dmlid){return 	iDmlId=dmlid;}
	public int setDmlType(int dmltype){return 	iDmlType=dmltype;}
	public int setColNum(int colnum){return 	iColNum=colnum;}
	public int setBundNum(int bundnum){return 	iBundNum=bundnum;}
	public String setSql(String sql){return 	sSql=sql;}
	public String setSqlExe(String sql){return 	sSqlExe=sql;}
	
	public int getBindInput(int i)
	{
		if(null==bindInput.get(i))
		{
			return -99999;
		}
		else
		{
			return bindInput.get(i);
		}
	}

	public void setBindInput(Integer _idx,Integer _iType)
	{
		if(null==bindInput)
		{
			bindInput=new HashMap<Integer, Integer>();
		}
		bindInput.put(_idx, _iType);
	}
}
