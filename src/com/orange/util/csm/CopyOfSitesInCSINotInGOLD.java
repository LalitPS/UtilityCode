package com.orange.util.csm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import au.com.bytecode.opencsv.CSVWriter;

import com.orange.ui.component.SiteFixedViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.ConnectionBeanCSI;

public class CopyOfSitesInCSINotInGOLD {

	
	private HashMap<String,ArrayList<String>> csiDataRows ;
	private CSVWriter cSIGOLDSync ;
	private ArrayList<String[]> duplicates;
	private CSVWriter duplicatesWriter;
	private HashMap<String,ArrayList<String>> goldDataRows ;
	private CSVWriter sitesNotInCSI ;
	private CSVWriter sitesNotInGOLD ;
	
	public CopyOfSitesInCSINotInGOLD(SiteFixedViewComponents siteFixViewComponents,String outputFileName) throws Exception{
		new File(outputFileName).mkdirs();
		goldDataRows = new HashMap<String,ArrayList<String>>();
		csiDataRows = new HashMap<String,ArrayList<String>>();
		duplicates = new ArrayList<String[]>();
		
		siteFixViewComponents.getQueryResult().append("Data Extraction start from CSI.\n");
		getCSISiteCodes(csiDataRows);
		siteFixViewComponents.getQueryResult().append("Data Extraction ends from CSI.\n");
		siteFixViewComponents.getQueryResult().append("Data Extraction start from GOLD.\n");
		getGOLDSiteCodes(goldDataRows);
		siteFixViewComponents.getQueryResult().append("Data Extraction ends from GOLD.\n");
		initCSVWrioters(outputFileName);
		siteFixViewComponents.getQueryResult().append("Initilize CSV writer(s) successfully.\n");
		meashupMaps();
		siteFixViewComponents.getQueryResult().append("File Creation successfully.\n");
		siteFixViewComponents.getQueryResult().append("Completed successfully.\n");
	}
	
	private void addINMap(String key , ArrayList<String> value,HashMap<String,ArrayList<String>> dataRows,String env){
		if(dataRows.containsKey(key))
		{
			
			//String s = "Duplicate sitecode found in "+env +" :: "+key;
		
			String arr[] = new String[2];
			arr[0] = env;
			arr[1] = key;
			duplicates.add(arr);
		}
		else
		{
			dataRows.put(key, value);
		}
	}
	
	private void getCSISiteCodes(HashMap<String,ArrayList<String>> csiDataRows) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
		
		
		String localQuery = "select sitehandle, addressid , coresiteid , servicehandle, ordhandle, custhandle from "+ConnectionBeanCSI.getDbPrefix()+"cversion";
		
		Statement pstmt = ConnectionForCSI.getStatement();
		ResultSet resultSet = pstmt.executeQuery(localQuery);
		int coulmncount = resultSet.getMetaData().getColumnCount();
		
		while(resultSet.next())
		{ 
		ArrayList<String> row = new ArrayList<String>();
		for (int x = 1 ; x<= coulmncount; x++) 
		{
			String sitecode = resultSet.getString(1);
			row.add(resultSet.getString(x));
			addINMap(sitecode,row,csiDataRows,"CSI");
		}
		}
		resultSet.close();
		pstmt.close();
	}
	
	
	private void getGOLDSiteCodes(HashMap<String,ArrayList<String>> goldDataRows) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		
		String localQuery = "select sitecode, address_id , core_site_id , status from "+ConnectionBean.getDbPrefix()+"eq_site";
		
		Statement pstmt = ConnectionForGOLD.getStatement();
		ResultSet resultSet = pstmt.executeQuery(localQuery);
		int coulmncount = resultSet.getMetaData().getColumnCount();
		
		while(resultSet.next()){
		ArrayList<String> row = new ArrayList<String>();
		for (int x = 1 ; x<= coulmncount; x++) 
		{
			String sitecode = resultSet.getString(1);
			row.add(resultSet.getString(x));
			addINMap(sitecode,row,goldDataRows,"GOLD");
		}
		}
		resultSet.close();
		pstmt.close();
	}
	
	
	/*
	 * 1. sitecode not available in CSI
	 * # Key in GOLD map , nut not in CSI Map : write in a CSV
	 * 2. sitecode not available in GOLD
	 * # Key in CSI map , but not in GOLD Map : write in a CSV
	 * 
	 * : write in a Main CSV GOLDCSISync.csv
	 * 3. inactive gold sitecode available in CSI
	 * # Key with status 3 , but still available in CSI Map as key
	 * 4. CSI and GOLD sitecodes matching
	 * # If key exists in both , display both keys row as addition
	 * 
	 */
	
	private void initCSVWrioters(String dirLoc) throws IOException{
		
		sitesNotInGOLD = new CSVWriter(new FileWriter(dirLoc+"\\"+CommonUtils.getFileName()+"_CSI_SITES_DETAILS.csv"));
		sitesNotInCSI = new CSVWriter(new FileWriter(dirLoc+"\\"+CommonUtils.getFileName()+"_SitesNotInCSI.csv"));
		cSIGOLDSync = new CSVWriter(new FileWriter(dirLoc+"\\"+CommonUtils.getFileName()+"_CSIGOLDSYnc.csv"));
		duplicatesWriter = new CSVWriter(new FileWriter(dirLoc+"\\"+CommonUtils.getFileName()+"_Duplicates_Sitecode.csv"));
		
		String columns[] ={"GOLD_Sites_Not_IN_CSI","GOLD_ADDRESS_ID","GOLD_CORE_SITE_ID","GOLD_STATUS"};
		String columns1[] ={"CSI_Sites_Not_IN_GOLD","CSI_ADDRESS_ID","CSI_CORE_SITE_ID","SERVICEHANDLE"};
		String columns2[] ={"GOLD_SITECODE","GOLD_ADDRESS_ID","GOLD_CORE_SITE_ID","GOLD_STATUS","CSI_SITECODE","CSI_ADDRESS_ID","CSI_CORE_SITE_ID","CSI_SERVICE_HANDLE","CSI_ORDER_HANDLE","CSI_CUST_HANDLE"};
		String duplicate[] ={"Enviorement","DUPLICATE_SITECODE"};
		
		sitesNotInCSI.writeNext(columns);
		sitesNotInGOLD.writeNext(columns1);;
		cSIGOLDSync.writeNext(columns2);
		duplicatesWriter.writeNext(duplicate);
		
	}
	
	private void meashupMaps() throws IOException{
		
		/*
		 * GOLD data available in CSI
		 * 
		 */
		Iterator<Map.Entry<String,ArrayList<String>>> keys = goldDataRows.entrySet().iterator();
		while(keys.hasNext())
		{
			Map.Entry<String,ArrayList<String>> entry = keys.next();
			String key = entry.getKey();
			ArrayList<String> goldRows = entry.getValue();
			int goldrowsize = goldRows.size();
			
			if(csiDataRows.containsKey(key))
			{
				ArrayList<String> csiRows = csiDataRows.get(key);
				int csirowsize = csiRows.size();
				String[] stockArr = new String[goldrowsize+csirowsize];
				
				for(int x = 0; x< goldrowsize; x++){
					stockArr[x] = goldRows.get(x);
				}
				for(int x = 0 ; x< csirowsize;x++){
					stockArr[x+goldrowsize] = csiRows.get(x);
				}

				cSIGOLDSync.writeNext(stockArr);
				//csiDataRows.remove(key);
			}
			else{
				String[] stockArr = new String[goldRows.size()];
				stockArr = goldRows.toArray(stockArr);
				sitesNotInCSI.writeNext(stockArr);
			}
		}
		
		// Data in CSI not in GOLD
		Iterator<Map.Entry<String,ArrayList<String>>> csikeys = csiDataRows.entrySet().iterator();
		while(csikeys.hasNext())
		{
			Map.Entry<String,ArrayList<String>> entry = csikeys.next();
			String key = entry.getKey();
			ArrayList<String> csiRows = entry.getValue();
			
			if(goldDataRows.containsKey(key))
			{
				
				
				
				
			}
			else
			{
			String[] stockArr = new String[csiRows.size()];
			stockArr = csiRows.toArray(stockArr);
			sitesNotInGOLD.writeNext(stockArr);
			}
		}
		
		for(String dupl[] : duplicates)
		{
			duplicatesWriter.writeNext(dupl);
		}
		
		sitesNotInGOLD.close();
		sitesNotInCSI.close();
		cSIGOLDSync.close();
		duplicatesWriter.close();
		goldDataRows.clear();
		csiDataRows.clear();
			
	}
	
	
	

}
