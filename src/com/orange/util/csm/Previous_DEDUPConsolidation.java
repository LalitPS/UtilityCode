package com.orange.util.csm;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.ui.component.SiteFixedViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.CustomCSVWriter;
import com.orange.util.ProgressMonitorPane;

public class Previous_DEDUPConsolidation 
{

	private Map<String,ArrayList<String[]>> addressIdsMap;
	private Map<String,ArrayList<ArrayList<String>>> addressIdsSubMap;
	private Map<String,LinkedHashMap<String,ArrayList<String[]>>> dedupMap,proxyDedupMap;
	private Map<String,ArrayList<String[]>> finalDedupMap ,finalDedupFilterMap,finalDedupFilterMap2,finalDedupFilterMap3,finalDedupMapProxy;
	private String inputFileLocation;
	private boolean isFileOkForProcess = true;
	private ArrayList<String[]> removeGIDs;
	private SiteFixedViewComponents siteFixViewComponents;
	
	public Previous_DEDUPConsolidation (SiteFixedViewComponents siteFixViewComponents) throws Exception
	
	{
		this.siteFixViewComponents = siteFixViewComponents;
		this.inputFileLocation = this.siteFixViewComponents.getFileToValidate().getText();
		dedupMap = new LinkedHashMap<String,LinkedHashMap<String,ArrayList<String[]>>>();
		proxyDedupMap = new LinkedHashMap<String,LinkedHashMap<String,ArrayList<String[]>>>();
		finalDedupMap = new LinkedHashMap<String,ArrayList<String[]>>();
		finalDedupFilterMap = new LinkedHashMap<String,ArrayList<String[]>>();
		finalDedupFilterMap2 = new LinkedHashMap<String,ArrayList<String[]>>();
		finalDedupFilterMap3 = new LinkedHashMap<String,ArrayList<String[]>>();
		finalDedupMapProxy = new LinkedHashMap<String,ArrayList<String[]>>();
		addressIdsSubMap = new LinkedHashMap<String,ArrayList<ArrayList<String>>>();
		addressIdsMap =new LinkedHashMap<String,ArrayList<String[]>>();
		removeGIDs = new ArrayList<String[]>();
		
		String path = siteFixViewComponents.getFileToValidate().getText();	
		int index = path.lastIndexOf(".");
		String sub = path.substring(0,index) ;
		String resultFileLoc=sub+"_Analysis.csv";
		String updatedResultFileLoc =sub+"_Updated.csv";
		
		// Read DEDUP File
		siteFixViewComponents.getQueryResult().append("Read input DEDUP file process started.\n");
		readColsolidatedDedupFile(this.inputFileLocation);
		siteFixViewComponents.getQueryResult().append("Read input DEDUP file process completed.\n");
		// check duplicate CORE IDs in GID's AM_RESPONSE
		siteFixViewComponents.getQueryResult().append("Validation of CORE IDS Process started.\n");
		validateDuplicateCOREIDsInAM_RESPONSE();
		siteFixViewComponents.getQueryResult().append("Validation of CORE IDS Process completed.\n");
		// check duplicate CORE IDs in GID
		siteFixViewComponents.getQueryResult().append("Duplicate CORE IDS Process started.\n");
		checkDuplicateCOREIDsinTrueandFalse();
		siteFixViewComponents.getQueryResult().append("Duplicate CORE IDS Process completed.\n");
		// check address id are linked with same site id
		siteFixViewComponents.getQueryResult().append("Check Core Ids combination process started.\n");
		checkAddressIdsSiteId();
		siteFixViewComponents.getQueryResult().append("Check Core Ids combination process completed.\n");
		
		if(removeGIDs.size()>1)
		{
		siteFixViewComponents.getQueryResult().append("Removing multiple core site ids against Address Id.\n");
		removeGIDsFromMap(updatedResultFileLoc);
		siteFixViewComponents.getQueryResult().append("Updated file "+updatedResultFileLoc+" has been created successfully.Please use this file for further process.\n");
		}
		if(isFileOkForProcess)
		{
			// arrange duplicate address Ids from file.
			// Now finalDedupMap : Known as Map 3 has been created
			siteFixViewComponents.getQueryResult().append("Arrange Duplicate Address Ids.\n");
			arrangeDuplicateAddressIds();
			/*
			 * Next step 
					1.	we will check all False address Id(s) and all the available found instance of address id in Database 
					    will be moved in same GID as False instance(s) with site-code. 
					2.	we will check all True addressed(s) and all available active instance from DB .
						1.	If One true and others inactive found
								a.	All others inactive will be added as False instances in GID with site-code.
								b.	Site-code of active address id will be placed for TRUE instance in file.
						2.	If No/Multiple active found
								a.	Need manual Investigation and such GID(s) will be parked or HOLD from execution.

			 * 
			 * 
			 */
			siteFixViewComponents.getQueryResult().append("Fixing the dedup file .\n");
			// The below methods will work with Data Base connection
			fixDEDUPFile();	
			siteFixViewComponents.getQueryResult().append("Writing Analysis file process started .\n");
			writeCSV(resultFileLoc);
			siteFixViewComponents.getQueryResult().append("Process completed succsssfully .\n");
			
		}
		else
		{
			siteFixViewComponents.getQueryResult().append("File is not found uptodate for further process , Please correct the file as above mebtioned error(s) and recompile again.\n");
		}
	
	}
	private ArrayList<String[]> addRowsOfGID(Map<String,ArrayList<String[]>> rowsOfGID){
		ArrayList<String[]> allRows = new ArrayList<String[]>();
		Iterator<Map.Entry<String, ArrayList<String[]>>> entries = rowsOfGID.entrySet().iterator();
		while (entries.hasNext()) 
		{
			Map.Entry<String, ArrayList<String[]>> entry = entries.next();
			ArrayList<String[]> values = entry.getValue();
			for(String arr[] : values)
			{
				allRows.add(arr);
			}
		}
		
		return allRows;
	}
	private void arrangeDuplicateAddressIds(){
		/*
		 * This Method is to check duplicate address Ids
		 * 1. If address Ids has different site id > already cover in checkAddressIdsSiteId method
		 * 2. If address Ids has same siteid the remove one , based on the following cases
		 * 
		 * |---------------------------------------------|
		 * |  Remove DUPLICATES from different GID(s)    |
		 * |---------------------------------------------|
		 * 
			Check Address ID Key and the biggest GID - 
				-Create a new Map-3 with Biggest GID Key and get all Instances of that GID from Map 1.
					-If another instance of Address ID in Map 2 is False - get the GID - Get all False instance(s) of that GID from Map1 and reomove that False row from Map1.
					-If another instance of Address ID in Map 2 is True   get the GID - Get all False instance(s) of that GID from Map1 and put into Map3 against the Biggest GID key.


		 */
		/*
		proxyDedupMap : Map 1  as below
		     |- Row 1 
		GID -|- Row 2
		     |- Row 3
		       
		addressIdsMap : Map 2 as below
		
		          |- GID 1
		AddressId-|- GID 2
		          |- GID 3
		          
		          
		finalDedupMap
						  |	GID  ROW
		               |--| GID  ROW
		               |  | GID  ROW
		               | 
		               |  | GID  ROW
		        GID -- |--| GID  ROW
		               |  | GID  ROW
		               |
		               |  | GID  ROW
		               |--| GID  ROW
		                  | GID  ROW
		*/
		
		// Get the Biggest GID for each address Id from Map2
		
		Iterator<Map.Entry<String, ArrayList<String[]>>> entries = addressIdsMap.entrySet().iterator();
		
		while (entries.hasNext()) 
		{
			Map.Entry<String, ArrayList<String[]>> entry = entries.next();
			/*
			String adressId = entry.getKey();
			*/
			ArrayList<String[]> gids = entry.getValue();
			
			//Check Address ID Key and the biggest GID - 
			String biggestGID = biggestGID(gids);
		
			//Create a new Map-3 with Biggest GID Key and get all Instances of that GID from Map 1.
			if(finalDedupMap.containsKey(biggestGID))
			{
				ArrayList<String[]> exisitingValues = finalDedupMap.get(biggestGID);
				exisitingValues.addAll(addRowsOfGID(proxyDedupMap.get(biggestGID)));
				finalDedupMap.put(biggestGID,exisitingValues);	
			}
			else if(!finalDedupMap.containsKey(biggestGID))
			{
				ArrayList<String[]> exisitingValues = new ArrayList<String[]>();
				exisitingValues.addAll(addRowsOfGID(proxyDedupMap.get(biggestGID)));
				finalDedupMap.put(biggestGID,exisitingValues);	
			}
			for(int x = 0; x< gids.size(); x++)
			{
				String[] row = gids.get(x);
				
				String gid = row[0];
				String amResponse = row[1].trim();
				
			
				
				// Here we are not comparing Biggest GID as its already put into the map , above this for loop
				if(!gid.equals(biggestGID))
				{
					//If another instance of Address ID in Map 2 is True then add all False of that gid in bigger GID	
					if(amResponse.equalsIgnoreCase("TRUE"))
					{
							Map<String,ArrayList<String[]>> submap = proxyDedupMap.get(gid);
							//Get all False instance(s) of that GID from Map1
							ArrayList<String[]> falseRows = submap.get("FALSE");
							/*
							 * If any GID is available only TRUE
							 * No False available. 
							 * 
							 */
							if(null != falseRows)
							{
								//put into Map3 against the Biggest GID key
								
								ArrayList<String[]> exisitngRows = finalDedupMap.get(biggestGID);
							    
								for(String[] falseRow : falseRows)
								{
									String preValue = falseRow[0];
									falseRow[0] = preValue+">>"+biggestGID;
									exisitngRows.add(falseRow);
								}
								finalDedupMap.put(biggestGID,exisitngRows);
							}
					}
					/*If another instance of Address ID in Map 2 is False: then remove that False from map. 
					*/
					if(amResponse.equalsIgnoreCase("FALSE"))
					{
						/*
							
						    LinkedHashMap<String,ArrayList<String[]>> submap = proxyDedupMap.get(gid);
							//Get all False instance(s) of that GID from Map1
							ArrayList<String[]> falseRows = submap.get("FALSE");
							falseRows.remove(row);
							submap.put("FALSE", falseRows);
							proxyDedupMap.put(gid, submap);
							finalDedupMap.put(gid,falseRows);
						*/
						
					}
					
				}
				
			}
		}
		filterFinalDedupMap();
	}

	private String biggestGID(ArrayList<String[]> rows){
		int big =0;
		for(String[] row : rows)
		{
			int Y = Integer.parseInt(row[0]);
			if(Y> big)
			{
				big = Y;
			}
		}
		return String.valueOf(big);
		
	}

	private String biggestGIDWithHash(ArrayList<String[]> rows){
		int big =0;
		for(String[] row : rows)
		{
			String S = row[0];
			if(row[0].contains(">>")){
				S = S.substring(S.indexOf(">>")+2);
			}
			int Y = Integer.parseInt(S);
			if(Y > big)
			{
				big = Y;
			}
		}
		return String.valueOf(big);
		
	}
		
	private void checkAddressIdsSiteId(){
		/*
		 * This Method is to check address Ids and linked siteId
		 * 1. If address Ids has different site id > show Errors
		 *  
		 * |---------------------------------------------|
		 * |  Address Id linked with Different Site Id   |
		 * |---------------------------------------------|
		 */
		Iterator<Map.Entry<String, ArrayList<ArrayList<String>>>> entries = addressIdsSubMap.entrySet().iterator();
		while (entries.hasNext()) 
		{
			Map.Entry<String, ArrayList<ArrayList<String>>> entry = entries.next();
			String key = entry.getKey();
			ArrayList<ArrayList<String>> values = entry.getValue();
			
			if(values.size()>1)
			{
				isFileOkForProcess = false;
				// address id is linked with multiple core site ids
				// hence error .. file can not be processed
				siteFixViewComponents.getQueryResult().append("ERROR >> Address Id "+key +" is linked with multiple SITE ID(s)\n");
				for(ArrayList<String> siteIds : values)
				{
					siteFixViewComponents.getQueryResult().append("SITE ID "+siteIds.get(0) +" GID(s) are "+siteIds.get(1)+" Status "+siteIds.get(2) +"\n");
					String[] gidsInfo = new String[3];
					gidsInfo[0] = siteIds.get(0);
					gidsInfo[1] = siteIds.get(1);
					gidsInfo[2] = siteIds.get(2);
					removeGIDs.add(gidsInfo);
				}
				
				siteFixViewComponents.getQueryResult().append("-----------------------------------------------------------\n");
			}
		}
		
		
		
		
		
		
	}
	
	private void checkDuplicateCOREIDsinTrueandFalse(){
		
		/*
		 * This Method is to check duplicate COREIDs in GIDs different AM Response
		 *  
		 * |-----------------------------------------------------|
		 * |  duplicate COREIDs in GIDs different AM Response    |
		 * |-----------------------------------------------------|
		 */
		Iterator<Map.Entry<String, LinkedHashMap<String,ArrayList<String[]>>>> entries = proxyDedupMap.entrySet().iterator();

		
		while (entries.hasNext()) 
		{
			Map.Entry<String, LinkedHashMap<String,ArrayList<String[]>>> entry = entries.next();
			String key = entry.getKey();
			Map<String,ArrayList<String[]>> values = entry.getValue();
			
			Iterator<Map.Entry<String, ArrayList<String[]>>> subentries = values.entrySet().iterator();
			
			ArrayList<String[]> coreids = new ArrayList<String[]>();
			
			while (subentries.hasNext()) 
			{
				Map.Entry<String, ArrayList<String[]>> subentry = subentries.next();
				
				ArrayList<String[]> subvalues = subentry.getValue();
		
				// If duplicate found in True and False : Hold this Case
				for(String[] subrow: subvalues)
				{
				  String response = subrow[1];	
				  String csid = subrow[3];
				  String addrid = subrow[10];
				  ArrayList<String> value = new ArrayList<String>();
				  value.add(csid);
				  value.add(key);
				  value.add(response);
				  fillAddressIDSubMap(addrid, value,addressIdsSubMap);
				  String[] arr = new String[2];
				  arr[0] = key;
				  arr[1]= subrow[1];
				  fillAddressIDMap(addrid, arr);
				  String[] ARR = new String[]{csid,addrid};
				  if(coreids.contains(ARR))
				  {
					  siteFixViewComponents.getQueryResult().append("ERROR >> Duplicate CORE IDS found in False|True, GID "+key+"\n");
					  isFileOkForProcess = false;
				  }
				  else{
					  coreids.contains(ARR);
				  }
			  }
			}
		}
	}
	
	private void fillAddressIDMap(String addressId,String[] value){
		if(addressIdsMap.containsKey(addressId))
		{
			ArrayList<String[]> exValues = addressIdsMap.get(addressId);
			exValues.add(value);
			addressIdsMap.put(addressId, exValues);
				
		}
		else
		{
			ArrayList<String[]> exValues = new ArrayList<String[]>();
			exValues.add(value);
			addressIdsMap.put(addressId, exValues);
		}
	}
	
	private void fillAddressIDSubMap(String addressId,ArrayList<String> value,Map<String,ArrayList<ArrayList<String>>> addressIdsSubMap){
		
		if(addressIdsSubMap.containsKey(addressId))
		{
			ArrayList<ArrayList<String>> exValues = addressIdsSubMap.get(addressId);
			for(ArrayList<String> exisitngSiteIds : exValues)
			{
				// Enter only if site id is different
				if(!exisitngSiteIds.get(0).equals(value.get(0)))
				{
					ArrayList<ArrayList<String>> proxyExValues = new ArrayList<ArrayList<String>>(exValues);
					proxyExValues.add(value);
					addressIdsSubMap.put(addressId, proxyExValues);
				}
			}
			
		}
		else{
			ArrayList<ArrayList<String>> exValues = new ArrayList<ArrayList<String>>();
			exValues.add(value);
			addressIdsSubMap.put(addressId, exValues);
		}
	
	}
	
	private void filterFinalDedupMap(){
		
		/*
		 * 1. Remove Duplicates form Same GID
		 * 2. Create a New Map as below Map1
		 *    
		     				|- Row 1 
				Address Id -|- Row 2
		     				|- Row 3
		     				
		     	And keep Highest GID row , rest will remove from Map1.
		     	
		   3. Create a New Map as below Map 2
		 *    				|- Row 1 
				GID -		|- Row 2
		     				|- Row 3
		     				
		     	
		     	Sort this Map 2 > In-terms of GID
		     	                > the sort in-terms of AM_RESPONSE
		 * 
		 */
		
		Iterator<Map.Entry<String, ArrayList<String[]>>> entries = finalDedupMap.entrySet().iterator();
		while (entries.hasNext()) 
		{
			Map.Entry<String, ArrayList<String[]>> entry = entries.next();
			/*
			String gid = entry.getKey();
			*/
			ArrayList<String[]> value = entry.getValue();
			for(String[] ARR : value)
			{
				String ADDR_ID = ARR[10];
				setFilterMap(ADDR_ID, ARR);
				
			}
		}
		
		
		Iterator<Map.Entry<String, ArrayList<String[]>>> entries1 = finalDedupFilterMap.entrySet().iterator();
		while (entries1.hasNext()) 
		{
			Map.Entry<String, ArrayList<String[]>> entry = entries1.next();
			String addressID = entry.getKey();
			ArrayList<String[]> value = entry.getValue();
			ArrayList<String[]> updatedValues = new ArrayList<String[]>();
			String biggestGID = biggestGIDWithHash(value);
			for(String[] ARR : value)
			{
				String GGID = ARR[0];
				if(GGID.contains(">>")){
					GGID = GGID.substring(GGID.lastIndexOf(">>")+2);
				}
				String GID = GGID ;//ARR[0];
				if(GID.equalsIgnoreCase(biggestGID))
				{
					updatedValues.add(ARR);
					// keep in Map
				}
			}
			if(finalDedupFilterMap2.containsKey(addressID)){
				//And keep Highest GID row , rest will remove from Map1.
				ArrayList<String[]> exisitingValues = finalDedupFilterMap2.get(addressID);
				exisitingValues.addAll(updatedValues);
				finalDedupFilterMap2.put(addressID, exisitingValues);
			}
			else{
				finalDedupFilterMap2.put(addressID, updatedValues);
			}
		}
		
		Iterator<Map.Entry<String, ArrayList<String[]>>> entries2 = finalDedupFilterMap2.entrySet().iterator();
		while (entries2.hasNext()) 
		{
			Map.Entry<String, ArrayList<String[]>> entry = entries2.next();
		   /*
			String addressID = entry.getKey();
			*/
			ArrayList<String[]> value = entry.getValue();
			for(String[] ARR : value)
			{
				//String GID = ARR[0];
				String GGID = ARR[0];
				if(GGID.contains(">>")){
					GGID = GGID.substring(GGID.indexOf(">>")+2);
				}
				String GID = GGID ;//ARR[0];
				
				setFilterMap3(GID,ARR);
			}
			
		}
		
		
	}
	
	private void fixDEDUPFile() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		
		finalDedupMapProxy = finalDedupFilterMap3;
		
		String BaseFalseSQL ="select distinct organ.OrganizationID ,site.Core_site_id ,site.orange_sitename ,site.customer_sitename ," +
		"site.status ,site.eq_comment ,address.phone ,address.fax ,site.address_id ,address.street2 ,address.street3 ," +
		"site.Room ,site.Floor ,address.Street1  ,address.zipcode,address.city,address.state_code,address.state," +
		"address.country_code,site.ISVALIDPRESALES,site.sitecode from "+ConnectionBean.getDbPrefix()+"sc_organization organ " +
				"join "+ConnectionBean.getDbPrefix()+"eq_site site on  site.eq_siteof=organ.tril_gid " +
				"join "+ConnectionBean.getDbPrefix()+"sc_address address on site.siteaddress=address.tril_gid	" +
				"left outer join "+ConnectionBean.getDbPrefix()+"sc_quote quote on site.tril_gid=quote.site OR site.tril_gid=quote.hotcutnewsite "+
				"where site.address_id in(?) " +
				"and (organ.OrganizationID <> '683512' " +
						
				"or (organ.OrganizationID = '683512' and site.status = '0') " +
				"or (organ.OrganizationID = '683512' and site.tril_gid = quote.site) " +
				"or (organ.OrganizationID = '683512' and site.tril_gid = quote.hotcutnewsite) "+
				")";
		
		
		String AlteredFalseSQL ="select distinct organ.OrganizationID ,site.Core_site_id ,site.orange_sitename ,site.customer_sitename ," +
				"site.status ,site.eq_comment ,address.phone ,address.fax ,site.address_id ,address.street2 ,address.street3 ," +
				"site.Room ,site.Floor ,address.Street1  ,address.zipcode,address.city,address.state_code,address.state," +
				"address.country_code,site.ISVALIDPRESALES,site.sitecode from "+ConnectionBean.getDbPrefix()+"sc_organization organ " +
						"join "+ConnectionBean.getDbPrefix()+"eq_site site on  site.eq_siteof=organ.tril_gid " +
						"join "+ConnectionBean.getDbPrefix()+"sc_address address on site.siteaddress=address.tril_gid	" +
						"left outer join "+ConnectionBean.getDbPrefix()+"sc_quote quote on site.tril_gid=quote.site OR site.tril_gid=quote.hotcutnewsite "+
						"where site.address_id in(?) " +
						"and (organ.OrganizationID <> '683512' " +
						
						// ADD
						// The below section added to get only those data which has Orders
						// In any case the original row should not be missed 
						//so sitecode condition as in below row has been added
						// this added section only works when given sitecode is not null
						
						"and site.status = '0'" +
						"or (organ.OrganizationID <> '683512' and site.sitecode = ?) " +
						"or (organ.OrganizationID <> '683512' and site.tril_gid = quote.site) " +
						"or (organ.OrganizationID <> '683512' and site.tril_gid = quote.hotcutnewsite) "+
						// condition added up to here : END of ADD
						
						"or (organ.OrganizationID = '683512' and site.status = '0') " +
						"or (organ.OrganizationID = '683512' and site.tril_gid = quote.site) " +
						"or (organ.OrganizationID = '683512' and site.tril_gid = quote.hotcutnewsite) "+
						")";
		
		String TrueSQL ="select distinct organ.OrganizationID ,site.Core_site_id ,site.orange_sitename ,site.customer_sitename ," +
		"site.status ,site.eq_comment ,address.phone ,address.fax ,site.address_id ,address.street2 ,address.street3 ," +
		"site.Room ,site.Floor ,address.Street1  ,address.zipcode,address.city,address.state_code,address.state," +
		"address.country_code,site.ISVALIDPRESALES,site.sitecode from "+ConnectionBean.getDbPrefix()+"sc_organization organ " +
				"join "+ConnectionBean.getDbPrefix()+"eq_site site on  site.eq_siteof=organ.tril_gid " +
				"join "+ConnectionBean.getDbPrefix()+"sc_address address on site.siteaddress=address.tril_gid	" +
				"where site.address_id in(?) "; 
			//	+ "and site.status <> '1'";

		final int totalDataCount = finalDedupFilterMap3.size();
		
		// Get All Rows
		Iterator<Map.Entry<String, ArrayList<String[]>>> entries = finalDedupFilterMap3.entrySet().iterator();
		double count = 0.0; 
		while (entries.hasNext()) 
		{
			count++;	
			ProgressMonitorPane.getInstance().setProgress(count,totalDataCount);	
			Map.Entry<String, ArrayList<String[]>> entry = entries.next();
			String gid = entry.getKey();
			ArrayList<String[]> mapValues = entry.getValue();
			ArrayList<String[]> exisitingMapValues = new ArrayList<String[]>(mapValues);
			
			// Get All Rows of GID
			for(String[] mapRow : mapValues)
			{
			    String AM_RESPONSE = mapRow[1];
			    String ADDRESS_ID  = mapRow[10];
			    String SITECODE  = mapRow[22];
			  
			    if(AM_RESPONSE.equalsIgnoreCase("FALSE"))
			    {
			    	ArrayList<String[]> queryResults ;
			    	
			    	if(CommonUtils.isNULL(SITECODE))
			    	{
			    	queryResults = CommonUtils.getQueryResult(BaseFalseSQL, siteFixViewComponents,ADDRESS_ID);
			    	}
			    	else
			    	{
			    		queryResults = CommonUtils.getQueryResult(AlteredFalseSQL, siteFixViewComponents,ADDRESS_ID, SITECODE);
			    	}
			    	// remove existing row , as this will be available in result set
					if(queryResults.size()== 0)
					{
			    		exisitingMapValues.remove(mapRow);
			    		mapRow[0] ="FAILED_NOT_FOUND";
						exisitingMapValues.add(mapRow);
						finalDedupMapProxy.put(gid,exisitingMapValues);
					}
			    	else if(queryResults.size() >= 1)
			    	{
			    		exisitingMapValues.remove(mapRow);
			    		for(int X = 0 ; X< queryResults.size(); X++)
							{
								/*
								 * in query result sitecode index is 20th
								 * in file sitecode index is 22nd 
								 */
			    				String queryresult[] = queryResults.get(X);
								String[] updatedQueryResult = new String[queryresult.length+2];
								// IF CORE IDS MATCH
								if(mapRow[3].equals(queryresult[1]) && mapRow[10].equals(queryresult[8]))
								{
									//if(X == 0)
									if(mapRow[22].equals(queryresult[20]))
									{
										updatedQueryResult[0] =mapRow[0];
										updatedQueryResult[1] =mapRow[1];
									}
									else
									{
										updatedQueryResult[0] ="SYSTEM_ADDED_ACTIVE_OR_HASQUOTES";
										updatedQueryResult[1] ="FALSE";
									}
									for(int x = 0 ; x< queryresult.length;x++)
									{
										updatedQueryResult[2+x] = queryresult[x];
									}
									exisitingMapValues.add(updatedQueryResult);
								}
								// IF CORE IDS DOESN'T MATCH
								else
								{
									mapRow[0] ="FAILED_NO_CORE_IDS_MATCH";
									exisitingMapValues.add(mapRow);
									finalDedupMapProxy.put(gid,exisitingMapValues);
								}
						    }
					finalDedupMapProxy.put(gid,exisitingMapValues);
			    	}
					
			    }// END OF AM RESPONSE FALSE
			    
			    if(AM_RESPONSE.equalsIgnoreCase("TRUE"))
			    {
			    	ArrayList<String[]> queryResults = CommonUtils.getQueryResult(TrueSQL, siteFixViewComponents,ADDRESS_ID);
			      
					exisitingMapValues.remove(mapRow);
				
					// If no results found
					if(queryResults.size()==0)
					{
						mapRow[0] ="FAILED_NOT_FOUND";
						exisitingMapValues.add(mapRow);
						finalDedupMapProxy.put(gid,exisitingMapValues);
					}
					else if(queryResults.size()==1)
					{
						String queryresult[] = queryResults.get(0);
						String[] updatedQueryResult = new String[queryresult.length+2];
						// IF CORE IDS MATCH
						if(mapRow[3].equals(queryresult[1]) && mapRow[10].equals(queryresult[8]))
						{
							
							// IF status active found
							if(queryresult[4].equalsIgnoreCase("0"))
							{
								updatedQueryResult[0] =mapRow[0];
								updatedQueryResult[1] =mapRow[1];
						
								/*
								mapRow[22] = queryresult[20];
								exisitingMapValues.add(mapRow);
								finalDedupMapProxy.put(gid,exisitingMapValues);
								*/
							}
							/*
							 OR if status 3 found , it means it should consider as this marked false in previous file 
							 but could not marked true in latter files executions.
							 EX : address id >> CI00011806
							 * */
							
							else if(queryresult[4].equalsIgnoreCase("3"))
							{
								updatedQueryResult[0] =mapRow[0];
								updatedQueryResult[1] =mapRow[1];
								
							}
							/*
							 OR if status 1 found , it means it should consider as this marked inactive in previous file 
							 but now needs to active in latter dedups.
							 * */
							else if(queryresult[4].equalsIgnoreCase("1"))
							{
								updatedQueryResult[0] =mapRow[0];
								updatedQueryResult[1] =mapRow[1];
								
							}
							
							
							else
							{
								mapRow[0] = "FAILED_NOT_ACTIVE_SITE_FOUND";
								updatedQueryResult[1] =mapRow[1];
						
							}
							
							
							for(int x = 0 ; x< queryresult.length;x++)
							{
								updatedQueryResult[2+x] = queryresult[x];
							}
							exisitingMapValues.add(updatedQueryResult);
							finalDedupMapProxy.put(gid,exisitingMapValues);
					} // End of CORE IDS Match
						
					else
					{
						mapRow[0] = "FAILED_NO_CORE_IDS_MATCH";
						exisitingMapValues.add(mapRow);
						finalDedupMapProxy.put(gid,exisitingMapValues);
					} // end of no core ids match
						
					}
					
					else if(queryResults.size() >1)
					{
						
						int truecount =0;
					    // True and False count True should be one
						// if no true or more then one true found : error
						
						for(String[] queryResult : queryResults)
						{
							
							if(queryResult[4].equalsIgnoreCase("0"))
							{
								truecount++;
							}
						}
					
						if(truecount == 0)
						{
							mapRow[0] = "FAILED_NOT_ACTIVE_SITE_FOUND";
							exisitingMapValues.add(mapRow);
							finalDedupMapProxy.put(gid,exisitingMapValues);
						}
						else if(truecount >1)
						{
							mapRow[0] = "FAILED_MULTIPLE_ACTIVE_SITE_FOUND";
							exisitingMapValues.add(mapRow);
							finalDedupMapProxy.put(gid,exisitingMapValues);
						}
						else if(truecount ==1)
						{
							// IF CORE IDS MATCH
							if(mapRow[3].equals(queryResults.get(0)[1]) && mapRow[10].equals(queryResults.get(0)[8]))
							{
							
								String[] queryResult = queryResults.get(0);
								String[] updatedQueryResult = new String[queryResult.length+2];
								
								updatedQueryResult[0] =mapRow[0];
								updatedQueryResult[1] =mapRow[1];
										
								for(int x = 0 ; x< queryResult.length;x++)
								{
									updatedQueryResult[2+x] = queryResult[x];
								}
								exisitingMapValues.add(updatedQueryResult);
								finalDedupMapProxy.put(gid,exisitingMapValues);
								
								
								}
							else
							{
								mapRow[0] = "FAILED_NO_CORE_IDS_MATCH";
								exisitingMapValues.add(mapRow);
								finalDedupMapProxy.put(gid,exisitingMapValues);
							} // end of no core ids match
							
							
						}// END OF TRUECOUNT 1
						else {
							mapRow[0] = "COULD_NOT_ANALYSIS_MANUAL_REQUIRED";
							exisitingMapValues.add(mapRow);
							finalDedupMapProxy.put(gid,exisitingMapValues);
						}
					}// END OF QUERY RESULT > 1
				}//END OF AM RESPONSE TRUE
			}// END OF MAPVALUES
		}// END OF WHILE
	}// END OF METHOD
	
	private void readColsolidatedDedupFile(String inputFileLocation)throws Exception
	{
		CSVReader br = new CSVReader(new FileReader(inputFileLocation));
		String[] row = null;
		br.readNext();
		
		while ((row = br.readNext()) != null) 
		{
			String GID = row[0];
			String AM_RESPONSE = row[1];
			String SITE_ID = row[3];
		    String ADDRESS_ID = row[10];
		    if(CommonUtils.isNULL(GID))
		    {
		    	siteFixViewComponents.getQueryResult().append("ERROR >> GID is Empty...\n");
		    	isFileOkForProcess = false;
		    	
		    }
		    else if(CommonUtils.isNULL(SITE_ID))
		    {
		    	siteFixViewComponents.getQueryResult().append("ERROR >> SITE_ID is Empty..."+GID+" \n");
		    	//isFileOkForProcess = false;
		    }
		    else if(CommonUtils.isNULL(ADDRESS_ID))
		    {
		    	siteFixViewComponents.getQueryResult().append("ERROR >> ADDRESS_ID is Empty..."+GID+" \n");
		    	//isFileOkForProcess = false;
		    }
		    else if(CommonUtils.isNULL(AM_RESPONSE))
		    {
		    	siteFixViewComponents.getQueryResult().append("ERROR >> AM_RESPONSE is Empty..."+GID+" \n");
		    	isFileOkForProcess = false;
		    }
		    else if(!AM_RESPONSE.trim().equalsIgnoreCase("TRUE") && !AM_RESPONSE.trim().equalsIgnoreCase("FALSE"))
		    {
		    	siteFixViewComponents.getQueryResult().append("ERROR >> UNKNOWN AM_RESPONSE TYPE.."+AM_RESPONSE+" on GID "+GID+" \n");
		    	isFileOkForProcess = false;
		    }
		 	// (GID,ROW)
			setDatainMap(GID,row);
		}
		br.close(); 
	}
	
	private void removeDuplicate_COREIDS(ArrayList<String[]> subvalues,String key,String subkey,Map<String,LinkedHashMap<String,ArrayList<String[]>>> proxyDedupMap){
		
		/*
		 * This Method is to remove duplicate COREIDs in GIDs different AM Response
		 *  
		 * |------------------------------------------------------------------------------|
		 * |  remove others and keep only one COREIDs in GIDs different AM Response       |
		 * |------------------------------------------------------------------------------|
		 */
	     ArrayList<String[]> coreids = new ArrayList<String[]>();
	   
		  for(String[] subrow: subvalues)
		  {
			  String csid = subrow[3];
			  String addrid = subrow[10];
			  String[] ARR = new String[]{csid,addrid};
			 
			  if(!coreids.contains(ARR))
			  {
				  coreids.add(ARR);  
			  }
			  else
			  {
				  siteFixViewComponents.getQueryResult().append("Duplicate CORE IDS found in GID "+key +" hence removing others and keeping one row..\n");
				 
				  ArrayList<String[]> proxysubvalues = subvalues;
				  int index = 0;
				  int keepFirst = 0;
				  for(String[] proxysubrow: proxysubvalues)
				  {
					  String pcsid = proxysubrow[3];
					  String paddrid = proxysubrow[10];
					  if(pcsid.equals(csid) && paddrid.equals(addrid))
					  {
						  // Keep single value if duplicate found... (Not should delete all)
						  if(keepFirst != 0)
						  {
							 proxysubvalues.remove(index);
						  }
						  keepFirst++;
					  }
					  index++;
				  }
				  LinkedHashMap<String,ArrayList<String[]>> pxsubmap =proxyDedupMap.get(key);
				
				  pxsubmap.put(subkey, proxysubvalues);
				  proxyDedupMap.put(key, pxsubmap);
			  }
		  }
	}
	private void removeGIDsFromMap(String updatedResultFileLoc) throws IOException{
		
		ArrayList<String[]> newRows = new ArrayList<String[]>();
		
		
		Iterator<Map.Entry<String, LinkedHashMap<String,ArrayList<String[]>>>> entries = proxyDedupMap.entrySet().iterator();

		
		while (entries.hasNext()) 
		{
			Map.Entry<String, LinkedHashMap<String,ArrayList<String[]>>> entry = entries.next();
			
			Map<String,ArrayList<String[]>> values = entry.getValue();
			
			Iterator<Map.Entry<String, ArrayList<String[]>>> subentries = values.entrySet().iterator();
			
		
			
			while (subentries.hasNext()) 
			{
				Map.Entry<String, ArrayList<String[]>> subentry = subentries.next();
				
				ArrayList<String[]> subvalues = subentry.getValue();
				for(String[] row : subvalues)
				{
				//String SITE_ID =row[3];
				String GID = row[0];
				//String STATUS= row[1];
				
				
				boolean toAdd = true;
				for(String arr [] : removeGIDs)
				{
					if(arr[1].equals(GID))
					{
						toAdd = false;
					}
				}
				if(toAdd) 
				{
					newRows.add(row);
				}
				toAdd = true;
				}
			}
		}
		
		String[] header = {"GID","AM Response","E_ICO1_CD","E_SITE_ID","E_ORANGE_SITE_NAME","E_CUSTOMER_SITE_NAME","E_SITE_STATUS_CD","E_SITE_COMMENT","E_PHONE","E_FAX","E_ADDRESS_ID","E_BUIDLING_NAME","E_LOCATION_NAME","E_ROOM","E_FLOOR","E_STREET_NAME","E_ZIP_CODE","E_CITY_NAME","E_STATE_CD","E_STATE_NAME","C_COUNTRY_ISO_3_CODE","E_IS_VALID_PRESALES_SALES","GOLD_SITECODE"};
		CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(updatedResultFileLoc),true);
		writer.writeNext(header);
		
		for(String[] row : newRows){
			writer.writeNext(row);

		}
		writer.close();
		
	}
	
	private void setDatainMap(String key,String ROW[]){
		
		String[] row = ROW;
		if(row[1].equalsIgnoreCase("TRUE")){
			row[1] = "TRUE";
		}
		if(row[1].equalsIgnoreCase("FALSE")){
			row[1] = "FALSE";
		}
		String innerKey = row[1];
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
					
					boolean isRowAvailable = false;
					for(String[] availabe : exisitngValues)
					{
						if(Arrays.deepEquals(availabe, row) || availabe == row)
						{
							isRowAvailable = true;
							break;
						}
					}
					if(isRowAvailable)
					{
						siteFixViewComponents.getQueryResult().append("DUPLICATE ROW : NOT CONSIDER SKIP for GID "+key+"\n");
					}
					else
					{
					
						if(innerKey.equalsIgnoreCase("TRUE") && innerKey.equalsIgnoreCase("FALSE"))
						{
							siteFixViewComponents.getQueryResult().append("ERROR >> AM RESPONSE is neither TRUE | FALSE  "+key+ "::"+innerKey+" \n");
							isFileOkForProcess = false;
						}
					exisitngValues.add(row);
					existingData.put(innerKey,exisitngValues);
					dedupMap.put(key, existingData);
					}
					
				}
				if(!existingData.containsKey(innerKey))
				{
					ArrayList<String[]> exisitngValues = new ArrayList<String[]>();
					if(innerKey.equalsIgnoreCase("TRUE") && innerKey.equalsIgnoreCase("FALSE"))
					{
						siteFixViewComponents.getQueryResult().append("ERROR >> AM RESPONSE is neither TRUE | FALSE  "+key+"::"+innerKey+" \n");
						isFileOkForProcess = false;
					}
					exisitngValues.add(row);
					existingData.put(innerKey,exisitngValues);
					dedupMap.put(key, existingData);
				}
		}
		else
		{
			
			ArrayList<String[]> exisitngValues = new ArrayList<String[]>();
			exisitngValues.add(row);
			LinkedHashMap<String,ArrayList<String[]>> newData = new LinkedHashMap<String,ArrayList<String[]>>();
			newData.put(innerKey,exisitngValues);
			dedupMap.put(key, newData);
		}
	}
	
	private void setFilterMap(String addrId,String[] rowValue){
		
		if(finalDedupFilterMap.containsKey(addrId))
		{
			
			ArrayList<String[]> exisitingValues = finalDedupFilterMap.get(addrId);
			boolean isAvailable = false;
			for(String[] existingRow : exisitingValues)
			{
				if(Arrays.deepEquals(existingRow, rowValue)|| existingRow == rowValue)
				{
					isAvailable = true;
					break;
				}
			}
			if(!isAvailable)
			{
			exisitingValues.add(rowValue);
			finalDedupFilterMap.put(addrId, exisitingValues);
			}
		}	
		
		else{
			
			ArrayList<String[]> exisitingValues = new ArrayList<String[]>();
			exisitingValues.add(rowValue);
			finalDedupFilterMap.put(addrId, exisitingValues);
		}
		}
	
	
	private void setFilterMap3(String GID,String[] rowValue){
		
		if(finalDedupFilterMap3.containsKey(GID))
		{
			ArrayList<String[]> exisitingValues = finalDedupFilterMap3.get(GID);
			boolean isAvailable = false;
			for(String[] existingRow : exisitingValues){
				if(Arrays.deepEquals(existingRow, rowValue) || existingRow == rowValue)
				{
					isAvailable = true;
					break;
				}
			}
			if(!isAvailable)
			{
			exisitingValues.add(rowValue);
			finalDedupFilterMap3.put(GID, exisitingValues);
			}
		}
		else{
			ArrayList<String[]> exisitingValues = new ArrayList<String[]>();
			exisitingValues.add(rowValue);
			finalDedupFilterMap3.put(GID, exisitingValues);
		}
	}
	private void validateDuplicateCOREIDsInAM_RESPONSE()
	{
		/*
		 * This Method is to validate duplicate COREIDs in GIDs different AM Response
		 *  
		 * |------------------------------------------------------------------------------|
		 * |  validate duplicate COREIDs in GIDs different AM Response                    |
		 * |------------------------------------------------------------------------------|
		 */
		
		this.proxyDedupMap =this.dedupMap;
		
		Iterator<Map.Entry<String, LinkedHashMap<String,ArrayList<String[]>>>> entries = dedupMap.entrySet().iterator();

		while (entries.hasNext()) 
		{
			Map.Entry<String, LinkedHashMap<String,ArrayList<String[]>>> entry = entries.next();
			String key = entry.getKey();
			LinkedHashMap<String,ArrayList<String[]>> values = entry.getValue();
			
			Iterator<Map.Entry<String, ArrayList<String[]>>> subentries = values.entrySet().iterator();
			while (subentries.hasNext()) 
			{
			
				Map.Entry<String, ArrayList<String[]>> subentry = subentries.next();
				String subkey = subentry.getKey();
				ArrayList<String[]> subvalues = subentry.getValue();
				
				// Check Multiple True in GID
				if(subkey.equalsIgnoreCase("TRUE"))
				{
					if(subvalues.size()>1)
					{
					  siteFixViewComponents.getQueryResult().append("Multiple True Found in GID "+key+"\n");
					}
					if(subvalues.size() == 0)
					{
						siteFixViewComponents.getQueryResult().append("No True Found in GID "+key+"\n");	
					}
				} 
			
				removeDuplicate_COREIDS(subvalues,key,subkey,proxyDedupMap);
					 
			
		}//end of inner while
	}// end of outer while
		
	}//end of method
	
	private void writeCSV(String resultFileLoc) throws IOException
	{
		String[] header = {"GID","REF_GID","AM Response","E_ICO1_CD","E_SITE_ID","E_ORANGE_SITE_NAME","E_CUSTOMER_SITE_NAME","E_SITE_STATUS_CD","E_SITE_COMMENT","E_PHONE","E_FAX","E_ADDRESS_ID","E_BUIDLING_NAME","E_LOCATION_NAME","E_ROOM","E_FLOOR","E_STREET_NAME","E_ZIP_CODE","E_CITY_NAME","E_STATE_CD","E_STATE_NAME","C_COUNTRY_ISO_3_CODE","E_IS_VALID_PRESALES_SALES","GOLD_SITECODE"};
		CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(resultFileLoc),true);
		writer.writeNext(header);
		
		/*
		 * 
		 * Iterate Map and write in CSV
		 */
		
		/*
		 * Sort HashMap.
		 * 
		 */
		Iterator<Map.Entry<String, ArrayList<String[]>>> entries = finalDedupMapProxy.entrySet().iterator();

		while (entries.hasNext()) 
		{
			Map.Entry<String,ArrayList<String[]>> entry = entries.next();
			String key = entry.getKey();
			ArrayList<String[]> values = entry.getValue();
			
			for(String[] value: values)
			{
				String []updated = new String[value.length+1];
				updated[0] = key;
				for(int X = 0; X< value.length ; X++)
				{
					updated[X+1] = value[X];
				}
				writer.writeNext(updated);
			}
		}
		writer.close();
		
	}
	
	
	
}
