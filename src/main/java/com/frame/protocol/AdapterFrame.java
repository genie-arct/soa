package com.frame.protocol;


import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AdapterFrame 
{
	private int iAdpId;
	private String sBusCode;
	private String sBusVers;
	private JsonObject jAdpCondition=new JsonObject();
	
	public void setAdpTid(int _iAdpId) {iAdpId=_iAdpId;}
	public void setBusCode(String _sBusCode) {sBusCode=_sBusCode;}
	public void setBusVers(String _sBusVers) {sBusVers=_sBusVers;}
	public void setAdpCon(String adpJson) 
	{
		jAdpCondition=new JsonParser().parse(adpJson).getAsJsonObject();
	}
	
	public int getAdpId(){return iAdpId;}
	public String getBusCode(){return sBusCode;}
	public String getBusVers(){return sBusVers;}
	public JsonObject getAdpCondition(){return jAdpCondition;}
	
	public boolean checkApdt(JsonObject obj)
	{
		Set<Entry<String,JsonElement>> set=jAdpCondition.entrySet();
		
		if(!obj.isJsonObject())
		{
			return false;
		}
		
		Iterator<Entry<String,JsonElement>> it = set.iterator();		
		while(it.hasNext())
		{
			Entry<String,JsonElement> ent=it.next();
			if(null==obj.get(ent.getKey()) || !obj.get(ent.getKey()).equals(ent.getValue()))
			{
				return false;
			}
		}
		return true;
	}
}
