package com.orange.util.others;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class NestedMap {

	public static void main(String ad[]){
		new NestedMap();
	}
	private Map<String,LinkedHashMap<String,ArrayList<String[]>>> dedupMap;
	
public NestedMap(){
	
	
	dedupMap  = new LinkedHashMap<String,LinkedHashMap<String,ArrayList<String[]>>>();
	String arr[] = {"1","FALSE","Data1","Data2","Data3"};
	setDatainMap("1",arr);
	String arr1[] = {"1","TRUE","Data1","Data2","Data3"};
	setDatainMap("1",arr1);
}
	private void setDatainMap(String key,String ROW[]){
			
			
			
			String innerKey = ROW[1];
			/*
			 * This Method is to used to entery in Map
			 *  
			 * |-------------------------------|
			 * |  Data enters in Map           |
			 * |-------------------------------|
			 */
			
			
			if(dedupMap.containsKey(key))
			{
				    LinkedHashMap<String,ArrayList<String[]>> existingData = dedupMap.get(key);
				
					if(existingData.containsKey(innerKey))
					{
						ArrayList<String[]> exisitngValues = existingData.get(innerKey);
						exisitngValues.add(ROW);
						existingData.put(innerKey,exisitngValues);
						dedupMap.put(key, existingData);
						
					}
					else
					{
						
						ArrayList<String[]> exisitngValues = new ArrayList<String[]>();
						exisitngValues.add(ROW);
						/*
						LinkedHashMap<String,ArrayList<String[]>> newData = new LinkedHashMap<String,ArrayList<String[]>>();
						*/
						existingData.put(innerKey,exisitngValues);
						dedupMap.put(key, existingData);
					}
			}
			else
			{
				ArrayList<String[]> exisitngValues = new ArrayList<String[]>();
				exisitngValues.add(ROW);
				LinkedHashMap<String,ArrayList<String[]>> newData = new LinkedHashMap<String,ArrayList<String[]>>();
				newData.put(innerKey,exisitngValues);
				dedupMap.put(key, newData);
			}
		}
}
