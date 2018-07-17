package com.orange.util.csm;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.util.CustomCSVWriter;

public class LegacySitesFindings {

	public static void main(String args[]){
		
		String fp = "C:\\Lalit\\Gold-Assignment\\CSM\\CSMTasks\\Legacy_Site_Extract\\JavaValidation\\Sites.csv";
		String ssi = "C:\\Lalit\\Gold-Assignment\\CSM\\CSMTasks\\Legacy_Site_Extract\\JavaValidation\\SSI.csv";
		String hssi = "C:\\Lalit\\Gold-Assignment\\CSM\\CSMTasks\\Legacy_Site_Extract\\JavaValidation\\HighestSITE_SERVICE_ICO.csv";
		LegacySitesFindings obj = new LegacySitesFindings();
		    try{
			obj.findUniqueSiteCode(fp);
			obj.findSSI(ssi,hssi);
			obj.writeICOSiteServiceFile();
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
		
		
	}
	ArrayList<String> activeBasicAndHotcutSites;
	ArrayList<String> activeBasicSites;
	ArrayList<String> allLegacySites;
	ArrayList<String> onlyHotcutSites;
	
	
	Map<String,String[]> siteServiceICOMap;
	CustomCSVWriter writer ;
	
	public LegacySitesFindings(){
		allLegacySites = new ArrayList<String>();
		activeBasicSites = new ArrayList<String>();
		activeBasicAndHotcutSites = new  ArrayList<String>();
		onlyHotcutSites = new  ArrayList<String>();
		siteServiceICOMap = new HashMap<String,String[]>();
	}
	
	
	private void addMapData(String key , String[] value){
		
		if(!siteServiceICOMap.containsKey(key)){
			siteServiceICOMap.put(key, value);
		}
		else{
			String[] val = siteServiceICOMap.get(key);
			int existingOrder = Integer.parseInt(val[0]);
			int newOrder = Integer.parseInt(value[0]);
			if(newOrder >existingOrder )
			{
				siteServiceICOMap.put(key, value);
			}
		}
	}
	private void findSSI(String ssiPath,String resultPath) throws IOException{
		System.out.println("Wait Map is working");
		CSVReader csvReader = new CSVReader(new FileReader(ssiPath));
		String[] row = csvReader.readNext();
		writer = new CustomCSVWriter(new FileWriter(resultPath),true);
		writer.writeNext(row);
		
		while ((row = csvReader.readNext()) != null) {
			
			
			String ICO =  row[4];
			String Service= row[5];
			String Site = row[10];
			
			String key = ICO+Service+Site;
		
			addMapData(key , row);
			
		}
		csvReader.close();
		System.out.println("Keys in Map size is "+siteServiceICOMap.size());
	}
	
	private void findUniqueSiteCode(String filePath) throws IOException{
		
		CSVReader csvReader = new CSVReader(new FileReader(filePath));
		String[] row = csvReader.readNext();
		while ((row = csvReader.readNext()) != null) 
		{
			String allLegacy = row[0];
			String allBasic = row[1];
			String allBasicAndHC = row[2];
			String onlyHC = row[3];
			
			
			allLegacySites.add(allLegacy);
			activeBasicSites.add(allBasic);
			activeBasicAndHotcutSites.add(allBasicAndHC);
			onlyHotcutSites.add(onlyHC);
			
			
		}
		csvReader.close();
		System.out.println("All Legacy Sites size is "+allLegacySites.size());
		System.out.println("Active Basic Sites size is "+activeBasicSites.size());
		System.out.println("All Active Sites size is "+activeBasicAndHotcutSites.size());
		System.out.println("Only Active Sites size is "+onlyHotcutSites.size());
	
		
		Set<String> legacySet = new LinkedHashSet<String>(allLegacySites);
		Set<String> basicSet = new LinkedHashSet<String>(activeBasicSites);
		Set<String> basicAndHCSet = new LinkedHashSet<String>(activeBasicAndHotcutSites);
		Set<String> onlyHCSet = new LinkedHashSet<String>(onlyHotcutSites);
		
		
		allLegacySites.clear();
		activeBasicSites.clear();
		activeBasicAndHotcutSites.clear();
		onlyHotcutSites.clear();
		
		
		allLegacySites.addAll(legacySet);
		activeBasicSites.addAll(basicSet);
		activeBasicAndHotcutSites.addAll(basicAndHCSet);
		onlyHotcutSites.addAll(onlyHCSet);
	
		
		System.out.println("Array List of Active and all Legacy completed");
		System.out.println("All Legacy Sites size after removing duplicates is  "+allLegacySites.size());
		System.out.println("Active Basic Sites size after removing duplicates is "+activeBasicSites.size());
		System.out.println("AllBasic HC Sites size after removing duplicates is  "+activeBasicAndHotcutSites.size());
		System.out.println("ONLY HC Sites size after removing duplicates is  "+onlyHotcutSites.size());
		
	
		ArrayList<String> notFoundActive = new ArrayList<String>();
		int activeFoundInLagacy = 0;
		int activeNotFoundInLagacy = 0;
		for(String legacy : allLegacySites)
		{
			if(activeBasicAndHotcutSites.contains(legacy)){
				activeFoundInLagacy++;
			}
			else{
				activeNotFoundInLagacy++;
				notFoundActive.add(legacy);
			}
		}
		
		
		System.out.println("Total Active found in lagacy is "+activeFoundInLagacy);
		System.out.println("Total Active not found in lagacy is "+activeNotFoundInLagacy);
		System.out.println("Total count " +(activeFoundInLagacy+activeNotFoundInLagacy));
		System.out.println("SITE NOT AS LEGACY");
	
		
	}
	private void writeICOSiteServiceFile() throws IOException{
		Iterator<Map.Entry<String, String[]>> entries1 = siteServiceICOMap.entrySet().iterator();

		ArrayList<String> hssiSiteCode = new ArrayList<String>();
		while (entries1.hasNext()) {
			Map.Entry<String, String[]> entry = entries1.next();
			//String key = entry.getKey();
			String[] value = entry.getValue();
			hssiSiteCode.add(value[10]);
			writer.writeNext(value);
		}
		writer.close();
		System.out.println("Writer Complete");
		Set<String> hssiSet = new LinkedHashSet<String>(hssiSiteCode);
		hssiSiteCode.clear();
		hssiSiteCode.addAll(hssiSet);
		System.out.println("HSSI SiteCodes "+hssiSiteCode.size());
		
	}
}
