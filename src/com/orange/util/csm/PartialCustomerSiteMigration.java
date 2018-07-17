package com.orange.util.csm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.ui.component.SiteFixedViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.ConnectionBeanCSI;
import com.orange.util.CustomCSVWriter;
import com.orange.util.ProgressMonitorPane;

public class PartialCustomerSiteMigration {
	
	private String addressIdCheck ="SELECT COUNT(*) FROM  "+ConnectionBean.getDbPrefix()+"EQ_SITE WHERE ADDRESS_ID=?";
	private String billingAccountNumberCheck= "SELECT COUNT(*) FROM  "+ConnectionBean.getDbPrefix()+"EQ_BILLINGPROFILE WHERE BILLINGACCOUNTNUMBER=?";
	private String checkQuoteExistence ="SELECT QUOTENUMBER FROM  "+ConnectionBean.getDbPrefix()+"SC_QUOTE WHERE QUOTENUMBER =?";
	private String columnName[] = {
			"GoldOrderRef",
			"TARGET_Billing_Account_Number",
			"TARGET_Contract_ID",
			"Target_Account_Manager",
			"TARGET_IC01_End_User",
			"TARGET_SITE_ID",
			"TARGET_CORE_Address_ID",
			"TARGET_CUSTOMER_NAME",
			"SITECODE",
			"TARGET_ORANGE_SITE_NAME"
			};
	
	private String contractIdChecks= "SELECT COUNT(*) FROM  "+ConnectionBean.getDbPrefix()+"EQ_CONTRACT WHERE CONTRACTID=?";

	private ArrayList<String> csisql ;
	private String csiSQLFilePath;
	String defaultContact ="SELECT COUNT(EQ_DEFAULTCONTACT1) FROM "+ConnectionBean.getDbPrefix()+"EQ_SITE WHERE ADDRESS_ID=?";
	
	
	Map<ArrayList<String>,ArrayList<String[]>> fileDataSuccessResult;
	
	Map<ArrayList<String>,ArrayList<String>> fileDataValidationResult;
	private String filePath;
	ArrayList<String> goldsql;
	private String goldSQLFilePath;
	Map<ArrayList<String>,Map<String,ArrayList<String[]>>> ordersOnService ;
	
	int rowscount = 0;
	private SiteFixedViewComponents siteFixViewComponents;
	//String accountManagerCheck="";
	String targetICOCheck ="SELECT COUNT(*) FROM  "+ConnectionBean.getDbPrefix()+"SC_ORGANIZATION WHERE ORGANIZATIONID=?";
	

	
	String targetSiteDetails ="SELECT EQ_DEFAULTCONTACT1, ADDRESS_ID, CORE_SITE_ID, SITECODE , STATUS , ORANGE_SITENAME from "+ConnectionBean.getDbPrefix()+"EQ_SITE WHERE ADDRESS_ID=?";
	
	String targetSiteICO = "SELECT ORGANIZATIONID FROM "+ConnectionBean.getDbPrefix()+"SC_ORGANIZATION WHERE TRIL_GID =(SELECT EQ_SITEOF FROM "+ConnectionBean.getDbPrefix()+"EQ_SITE WHERE SITECODE=?)"; 
	
	private String updatedCSVFilePath;
	
	
	public PartialCustomerSiteMigration(SiteFixedViewComponents siteFixViewComponents) throws Exception
	{
	
	this.siteFixViewComponents = siteFixViewComponents;
	this.filePath = this.siteFixViewComponents.getFileToValidate().getText();
	fileDataSuccessResult = new HashMap<ArrayList<String>,ArrayList<String[]>>();
	fileDataValidationResult =  new HashMap<ArrayList<String>,ArrayList<String>>();
	goldsql = new ArrayList<String>();
	csisql = new ArrayList<String>();
	ordersOnService = new HashMap<ArrayList<String>,Map<String,ArrayList<String[]>>>();
	
	
	int index = filePath.lastIndexOf(".");
	String sub = filePath.substring(0,index) ;
	this.updatedCSVFilePath = sub+"_Updated.csv";
	this.goldSQLFilePath = sub+"_GOLD_SQLSCRIPT.sql";
	this.csiSQLFilePath= sub+"_CSI_SQLSCRIPT.sql";
	
	
	// File Format Validation
	ArrayList<String> fileFormatValidationResult = getFileFormatValidation(filePath);
	if(fileFormatValidationResult.size() == 0)
	{
		
		// File Data Validation CHECK ICO , ADDRESS_ID, CONTRACT ID , BILLING ACCOUNT EXISTS OR NOT FOR A ROW
		Map<ArrayList<String>,ArrayList<String>> fileDataValidationResult = getFileDataValidation(filePath);
		
		// File Data Link Validation 
		Map<ArrayList<String>,ArrayList<String>> fileDataLinkValidationResult = getFileDataLinkValidation(filePath,fileDataValidationResult);
		
		boolean isMapHasCommonKeys = validateFailureandSuccessMap(fileDataLinkValidationResult,fileDataSuccessResult);
		
		// All Validation found and sync 
		if(!isMapHasCommonKeys)
		{
			siteFixViewComponents.getQueryResult().append("Map Validation Success.. . \n");
			// Update the CSV with Comments and Updated site details.
			updateCSVWithComments(filePath,fileDataLinkValidationResult,fileDataSuccessResult);
			siteFixViewComponents.getQueryResult().append("CSV updated with comments successfully. "+updatedCSVFilePath +"\n");
			writeSQLScript();
			siteFixViewComponents.getQueryResult().append("Process Completed successfully. ");
		    
		}
		else
		{
			// Map not valid some problems found .. could not process further	
			siteFixViewComponents.getQueryResult().append("Map Validation Failure.. Could not process further. \n");
			siteFixViewComponents.getQueryResult().append("Process Completed with Failures. \n");
		
		}
	
	}
	else
	{
	    // File Format Validation Failed.. could not process further.
		for(String validationComments : fileFormatValidationResult)
		{
		siteFixViewComponents.getQueryResult().append(validationComments +"\n");
		}
		siteFixViewComponents.getQueryResult().append("File Format Validation Failure >>> Process Completed with Failures. \n");
	}
	}
	
	private void addKeyData(Map<ArrayList<String>,ArrayList<String>> fileDataValidationResult,ArrayList<String> key,ArrayList<String> values){
	    if(values.size()>0)
	    {
			if(fileDataValidationResult.containsKey(key))
			{
				ArrayList<String> exisitingValues = fileDataValidationResult.get(key);
				for(String value: values)
				{
					exisitingValues.add(value);
				}
				fileDataValidationResult.put(key, exisitingValues);
			}
			else
			{
				fileDataValidationResult.put(key, values);
			}
	    }
		
	}
	
	private void addMapData(ArrayList<String> key,String[] value)
	{
		if(fileDataSuccessResult.containsKey(key))
		{
			ArrayList<String[]> existitng =fileDataSuccessResult.get(key);
			existitng.add(value);
			fileDataSuccessResult.put(key, existitng);
			
			
		}
		else
		{
			ArrayList<String[]> existitng =new ArrayList<String[]>();
			existitng.add(value);
			fileDataSuccessResult.put(key, existitng);
		}
		
		//System.out.println("fileDataSuccessResult size is " +fileDataSuccessResult.size());
	}
	
	private void addOrdersOnServiceMapData(ArrayList<String> mapKey,String mapIntkey, String[] value)
	{
		if(ordersOnService.containsKey(mapKey))
		{
			Map<String , ArrayList<String[]>> intMap = ordersOnService.get(mapKey);
			if(intMap.containsKey(mapIntkey))
			{
			
				ArrayList<String[]> exisiting = intMap.get(mapIntkey);
			
				// PUT ONLY AN ORDER WHICH HAS HOTCUTNEWSITE
				boolean isAnyHotCutExists = false;
				for(String[] row : exisiting)
				{
					String hotcutnewsite = row[2];
					if(null == hotcutnewsite || hotcutnewsite.isEmpty() || 
							   hotcutnewsite.length() ==0 || hotcutnewsite.equalsIgnoreCase("null"))
					{
						
					}
					else
					{
						isAnyHotCutExists = true;
						break;
					}
				}
				if(!isAnyHotCutExists)
				{
					exisiting.add(value);
				}
				else
				{
					siteFixViewComponents.getQueryResult().append("NOT ADDING ANY ORDER " + value[0] +" AS HOTCUT ALREADY EXISTS IN MAP FOR THIS SERVICE..");
				}
				intMap.put(mapIntkey, exisiting);
				ordersOnService.put(mapKey, intMap);
				
			}
			else
			{
				ArrayList<String[]> exisiting = new ArrayList<String[]>();
				exisiting.add(value);
				intMap.put(mapIntkey, exisiting);
				ordersOnService.put(mapKey, intMap);
			}
		}
		else
		{
			ArrayList<String[]> exisiting = new ArrayList<String[]>();
			exisiting.add(value);
			
			Map<String,ArrayList<String[]>> intMap = new HashMap<String,ArrayList<String[]>>();
			intMap.put(mapIntkey, exisiting);
			ordersOnService.put(mapKey, intMap);
		}
		
	}
	 
	
	private void createCSIScriptFile() throws Exception{
		File fout = new File(csiSQLFilePath);
		FileOutputStream fos = new FileOutputStream(fout);
		csisql.add("commit;");
		csisql = CommonUtils.setUmlaut(csisql);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		for(String script : csisql){
			bw.write(script);
			bw.newLine();
		}
		bw.close();
		fos.close();
		csisql = null;
		
	}
	
	private void createGOLDScriptFile() throws Exception{
		File fout = new File(goldSQLFilePath);
		FileOutputStream fos = new FileOutputStream(fout);
		goldsql.add("commit;");
		goldsql = CommonUtils.setUmlaut(goldsql);
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		for(String script : goldsql){
			bw.write(script);
			bw.newLine();
		}
		bw.close();
		fos.close();
		goldsql = null;
		
	}
	
	private int getCountOfZeroStatus(ArrayList<String[]> values){
		int countzero = 0; 
		for(String[] row : values)
		{
			/*
			String tril_gid 					= row[0];
			String address_id 				= row[1]; 
			String core_site_id				= row[2];
			String sitecode 					= row[3];
			String orange_sitename	 	= row[5];
			*/
			String status						= row[4];
			
			if(status.equals("0"))
			{
				countzero++;
			}
		}
		return countzero;
	}
	private int getCSVROWCount(String filePath) throws Exception 
	{
		int rowsCount = 0;
		CSVReader csvReader = new CSVReader(new FileReader(filePath));
		while ((csvReader.readNext()) != null) 
		{
			rowsCount++;
		}
		csvReader.close();
		return rowsCount;
	}
	
	private Map<ArrayList<String>,ArrayList<String>> getFileDataLinkValidation(String filePath,Map<ArrayList<String>,ArrayList<String>> fileDataValidationResult) throws Exception
	{
		rowscount = getCSVROWCount(filePath);
		
		CSVReader csvReader = new CSVReader(new FileReader(filePath));
		String []row = csvReader.readNext();
		double progresscount = 0.0;  
		while ((row = csvReader.readNext()) != null) 
		{
			progresscount++;	
			ProgressMonitorPane.getInstance().setProgress(progresscount,rowscount);	
			
			String GoldOrderRef 				= CommonUtils.isNULL(row[0]) ? row[0] : row[0].trim();
		//  String Billing_Account_Number		= CommonUtils.isNULL(row[1]) ? row[1] : row[1].trim();
		//  String Contract_IDs					= CommonUtils.isNULL(row[2]) ? row[2] : row[2].trim();
		//  String Account_Manager				= CommonUtils.isNULL(row[3]) ? row[3] : row[3].trim();
			String TARGET_IC01					= CommonUtils.isNULL(row[4]) ? row[4] : row[4].trim();
		//  String NEW_TARGET_SITE_ID			= CommonUtils.isNULL(row[5]) ? row[5] : row[5].trim();
			String NEW_TARGET_CORE_ADDRESS_ID	= CommonUtils.isNULL(row[6]) ? row[6] : row[6].trim();
		//  String TARGET_CUSTOMER_NAME			= CommonUtils.isNULL(row[7]) ? row[7] : row[7].trim();
			String SITECODE						= CommonUtils.isNULL(row[8]) ? row[8] : row[8].trim();
		//  String TARGET_ORANGE_SITENAME		= CommonUtils.isNULL(row[9]) ? row[9] : row[9].trim();
			
			ArrayList<String> key  = new ArrayList<String>(Arrays.asList(row));
			
			ArrayList<String[]> targetSiteDetailsData = new ArrayList<String[]>();
			ArrayList<String> dataValidation = new ArrayList<String>();
			
			// CHECK EXISTENCE OF QUOTE
			ArrayList<String[]> results =CommonUtils.getQueryResult(checkQuoteExistence,  siteFixViewComponents,GoldOrderRef);
			if(results.size() == 0)
			{
				dataValidation.add(">>>>GIVEN QUOTE NOT FOUND IN SYSTEM."+GoldOrderRef);
			}
			// If order exists then check further links validation(s).
			else
			{
			    // GET TARGET SITE DETAILS AS EQ_DEFAULTCONTACT1, ADDRESS_ID, CORE_SITE_ID, SITECODE , STATUS , ORANGE_SITENAME
				results = 	CommonUtils.getQueryResult(targetSiteDetails,  siteFixViewComponents,NEW_TARGET_CORE_ADDRESS_ID);
				
			
				// GET SYSTEM SUGGESTED ORDER FOR EACH SERVICE ON SITE. 				
				Map<ArrayList<String>,Map<String,ArrayList<String[]>>> orderList = listOfOrdersForSite(key,GoldOrderRef);
				
				
				
				if(results.size() ==0)
				{
					dataValidation.add(">>No Target site exists."+NEW_TARGET_CORE_ADDRESS_ID);
				}
				else if(results.size() == 1)
				{
					// Get the status
					
					String defaultContact1 = results.get(0)[0];
					String address_id = results.get(0)[1]; 
					String core_site_id= results.get(0)[2];
					String sitecode = results.get(0)[3];
					String status= results.get(0)[4];
					String orange_sitename = results.get(0)[5];
					
					// If status is non zero : 
					if(!status.equals("0"))
					{
						dataValidation.add(">>No active Target site exists."+NEW_TARGET_CORE_ADDRESS_ID);
					}
					
					// If status is zero : very clean site to use
					else if(status.equals("0"))
					{
					
					
						if(!SITECODE.equals(sitecode))
						{
							dataValidation.add(">>GIVEN SITECODE "+SITECODE +" NOT MATCH ."+sitecode);
							
						}
						else
						{
							// Iterate All the sites / orders for key
							
							Map<String,ArrayList<String[]>> orders = ordersOnService.get(key);
							Iterator<Map.Entry<String, ArrayList<String[]>>> entries = orders.entrySet().iterator();
							int count =0 ;
							while (entries.hasNext()) 
							{
								Map.Entry<String, ArrayList<String[]>> entry = entries.next();
								String intkey = entry.getKey();
								ArrayList<String[]> intvalue = entry.getValue();
								
								for(String[] order : intvalue)
								{
								String[] orddata = new String[8];
								orddata[0] = order[0];
								orddata[1] = defaultContact1;
								orddata[2] = address_id;
								orddata[3] = core_site_id;
								orddata[4] = sitecode;
								orddata[5] = status;
								orddata[6] = orange_sitename;
								orddata[7] = order[2];// Hot Cut New Site
							
								System.out.println(count++ +"adding row data for "+orddata[0]  +" fro service "+intkey);
								targetSiteDetailsData.add(orddata);
								}
							}
						
					}
				 }
				}
				// If multiple target site found
				else if(results.size() > 1)
				{
					
					// Check zero status
					int zeroStatusCount = getCountOfZeroStatus(results);
					
					// If zero status is one ; USE active site only
					if(zeroStatusCount == 1)
					{
						
						for(int x = 0 ; x<results.size(); x++ )
						{
							String status = results.get(x)[4];
							if(status.equals("0"))
							{
							
								String defaultContact1 = results.get(x)[0];
								String address_id = results.get(x)[1]; 
								String core_site_id= results.get(x)[2];
								String sitecode = results.get(x)[3];
								String stat= results.get(x)[4];
								String orange_sitename = results.get(x)[5];
								
								if(!SITECODE.equals(sitecode))
								{
									dataValidation.add(">>GIVEN SITECODE "+SITECODE +" NOT MATCH ."+sitecode);
									
								}
								else
								{
									
								
								// Iterate All the sites / orders for key
								
									Map<String,ArrayList<String[]>> orders = ordersOnService.get(key);
									Iterator<Map.Entry<String, ArrayList<String[]>>> entries = orders.entrySet().iterator();
								
										while (entries.hasNext()) 
										{
											Map.Entry<String, ArrayList<String[]>> entry = entries.next();
											String intkey = entry.getKey();
											ArrayList<String[]> intvalue = entry.getValue();
											
											for(String[] order : intvalue)
											{
											String[] orddata = new String[8];
											orddata[0] = order[0];
											orddata[1] = defaultContact1;
											orddata[2] = address_id;
											orddata[3] = core_site_id;
											orddata[4] = sitecode;
											orddata[5] = status;
											orddata[6] = orange_sitename;
											orddata[7] = order[2];// Hot Cut New Site
											targetSiteDetailsData.add(orddata);
										}
									}
								}
								
							}
						}
					}
					// if zero more then one
					else if(zeroStatusCount > 1)
					{
						dataValidation.add(">>Multiple active Target site exists."+NEW_TARGET_CORE_ADDRESS_ID);
					}
					// if no zero
					else if(zeroStatusCount == 0)
					{
						dataValidation.add(">>No active Target site exists."+NEW_TARGET_CORE_ADDRESS_ID);
					}
					
				}
			
			}
			
		
			if(targetSiteDetailsData.size() != 0)
			{
				
				// Target site must be for target ico in system 
				results = 	CommonUtils.getQueryResult(targetSiteICO,  siteFixViewComponents,targetSiteDetailsData.get(0)[4]);
				if(results.size() ==0)
				{
					dataValidation.add(">>No ico found for Target site."+targetSiteDetailsData.get(0)[4]);
				}
				if(results.size() > 1)
				{
					dataValidation.add(">>Multiple ico found for Target site."+targetSiteDetailsData.get(0)[4]);
				}
				if(results.size() == 1)
				{
					if(!results.get(0)[0].equals(TARGET_IC01))
					{
						dataValidation.add(">>Target site not for target ico in system."+targetSiteDetailsData.get(0)[4]);
					}
				
				}
			}
				if(dataValidation.size()>0)
				{
					// Add validation and remove all target site details as not required now
					addKeyData(fileDataValidationResult,key,dataValidation);
					targetSiteDetailsData = null;
				}
				else
				{
					if(!fileDataValidationResult.containsKey(key))
					{
						//System.out.println("File Data Validation result targetSiteDetailsData size is "+targetSiteDetailsData.size());
						for(String [] ordrow:targetSiteDetailsData)
						{
						//System.out.println("adding order row length "+ordrow.length +" for order "+ordrow[0]);	
						addMapData(key,ordrow);
						}
						//System.out.println(" add map data dompleted for ");
					}
					
				}
		}
		csvReader.close();
		return fileDataValidationResult;
	}
	
	private Map<ArrayList<String>,ArrayList<String>> getFileDataValidation(String filePath) throws Exception
	{
		CSVReader csvReader = new CSVReader(new FileReader(filePath));
		String []row = csvReader.readNext();
		while ((row = csvReader.readNext()) != null) 
		{
			String GoldOrderRef 									= CommonUtils.isNULL(row[0]) ? row[0] : row[0].trim();
			String Billing_Account_Number					= CommonUtils.isNULL(row[1]) ? row[1] : row[1].trim();
			String Contract_IDs									= CommonUtils.isNULL(row[2]) ? row[2] : row[2].trim();
			String Account_Manager								= CommonUtils.isNULL(row[3]) ? row[3] : row[3].trim();
			String TARGET_IC01									= CommonUtils.isNULL(row[4]) ? row[4] : row[4].trim();
			String NEW_TARGET_SITE_ID						= CommonUtils.isNULL(row[5]) ? row[5] : row[5].trim();
			String NEW_TARGET_CORE_ADDRESS_ID		= CommonUtils.isNULL(row[6]) ? row[6] : row[6].trim();
			String TARGET_CUSTOMER_NAME				= CommonUtils.isNULL(row[7]) ? row[7] : row[7].trim();
			String SITECODE										= CommonUtils.isNULL(row[8]) ? row[8] : row[8].trim();
			String TARGET_ORANGE_SITENAME			= CommonUtils.isNULL(row[9]) ? row[9] : row[9].trim();
			
			ArrayList<String> key  = new ArrayList<String>(Arrays.asList(row));
			ArrayList<String[]> count = 	CommonUtils.getQueryResult(billingAccountNumberCheck,  siteFixViewComponents,Billing_Account_Number);
			ArrayList<String> dataValidation = new ArrayList<String>();
			
			if(Integer.parseInt(count.get(0)[0]) <1)
			{
				dataValidation.add(">>BILLING_ACCOUNTNUMBER NOT EXISTS."+Billing_Account_Number);
			
			}
			
			count = CommonUtils.getQueryResult(contractIdChecks,  siteFixViewComponents,Contract_IDs);
			if(Integer.parseInt(count.get(0)[0]) <1)
			{
				dataValidation.add(">>CONTRACT ID NOT EXISTS."+Contract_IDs);
			
			}
			/*
			count = CommonUtils.getQueryResult(accountManagerCheck, Billing_Account_Number, siteFixViewComponents);
			if(Integer.parseInt(count.get(0)[0]) <1)
			{
				dataValidation.add(">>ACCOUNT MANAGER NOT EXISTS.");
			}
			*/
			count = CommonUtils.getQueryResult(targetICOCheck,  siteFixViewComponents,TARGET_IC01);
			if(Integer.parseInt(count.get(0)[0]) <1)
			{
				dataValidation.add(">>TARGET ICO NOT EXISTS."+TARGET_IC01);
			
			}
			count = 	CommonUtils.getQueryResult(addressIdCheck,  siteFixViewComponents,NEW_TARGET_CORE_ADDRESS_ID);
			if(Integer.parseInt(count.get(0)[0]) <1)
			{
				dataValidation.add(">>ADDRESS_ID NOT EXISTS." +NEW_TARGET_CORE_ADDRESS_ID);
			
			}
			count = 	CommonUtils.getQueryResult(defaultContact,  siteFixViewComponents,NEW_TARGET_CORE_ADDRESS_ID);
			if(Integer.parseInt(count.get(0)[0]) <1)
			{
				
				 //dataValidation.add(">>No default contact exists for address."+NEW_TARGET_CORE_ADDRESS_ID);
				// If no default contact for Target site .. Use existing order default contact
				
			}
			// ADD DATA VALIDATION FAILURE RESULTS IN MAP . CSV FILE ROW AS KEY
			addKeyData(fileDataValidationResult,key,dataValidation);
			
		}
		csvReader.close();
		
		return fileDataValidationResult;
	
	}
	
	private ArrayList<String> getFileFormatValidation(String filePath) throws Exception
	{
		ArrayList<String> fileFormatValidationResult = new ArrayList<String>();
		/*
		 * Column Number
		 * Column Name
		 * Column Sequences
		 * IsEmpty
		 */
		
		CSVReader csvReader = new CSVReader(new FileReader(filePath));
		String []row = csvReader.readNext();
		for (int C = 0; C < row.length; C++) 
		{
			if (!columnName[C].equalsIgnoreCase(row[C])) 
			{
				fileFormatValidationResult.add("COLUMN_MISMATCH ::"+columnName[C]+">>"+row[C]);
			
			}
		}
		int count =1 ;
		while ((row = csvReader.readNext()) != null) 
		{
			for(int x = 0; x<columnName.length;x++ )
			{
				if(row[x].isEmpty() )
				{
					fileFormatValidationResult.add(columnName[x]+"_EMPTY>>"+count);
			
				}
			}
			count++;
		}
		csvReader.close();
		return fileFormatValidationResult;
	}
	
	private Map<ArrayList<String>,Map<String,ArrayList<String[]>>> listOfOrdersForSite(ArrayList<String> csvrow,String GoldOrderRef) throws Exception
	{
		
		String QUERY = "SELECT QUOTENUMBER, SITE, HOTCUTNEWSITE, SERVICENAME, ORDEREDSERVICE, MIGRATIONSERVICE FROM " +
				ConnectionBean.getDbPrefix()+"SC_QUOTE QT, "+ConnectionBean.getDbPrefix()+"EQ_SITE ST, "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERV " +
				"WHERE QT.SITE = ST.TRIL_GID AND QT.ORDEREDSERVICE = SERV.SERVICE_ID " +
				"AND QT.SITE = (SELECT SITE FROM "+ConnectionBean.getDbPrefix()+"SC_QUOTE WHERE QUOTENUMBER = ?) " +
				"AND SERV.SERVICE_ID IN " +
				"(SELECT DISTINCT ORDEREDSERVICE FROM "+ConnectionBean.getDbPrefix()+"SC_QUOTE WHERE SITE = (SELECT SITE FROM "+ConnectionBean.getDbPrefix()+"SC_QUOTE WHERE QUOTENUMBER = ?)) " +
				"ORDER BY QUOTENUMBER DESC";
	
		ArrayList<String[]> results = 	CommonUtils.getQueryResult(QUERY,  siteFixViewComponents,GoldOrderRef,GoldOrderRef);
		if(null != results && results.size()>0)
		{
			for(String[] row : results)
			{
				String mapkey = row[4]; // ORDEREDSERVICE
				// ADD ROW ON ORDERED SERVICE
				addOrdersOnServiceMapData(csvrow,mapkey,row);
			}
		}
		return ordersOnService;
	}

	private void updateCSVWithComments(String filePath,Map<ArrayList<String>,ArrayList<String>> fileDataLinkValidationResult,Map<ArrayList<String>,ArrayList<String[]>> fileDataSuccessResult) throws Exception
	{
		
		CSVReader csvReader = new CSVReader(new FileReader(filePath));
		
		ArrayList<ArrayList<String>> writerData  = new ArrayList<ArrayList<String>>();
		String []row1 = csvReader.readNext();
		ArrayList<String> header  = new ArrayList<String>(Arrays.asList(row1));
		
		header.add("SYSTEM_STATUS");
		header.add("SYSTEM_COMMENTS");
		
		header.add("MOVING_ORDER");
		header.add("SYSTEM_PRIMARY_CONTACT");
		header.add("SYSTEM_ADDRESS_ID");
		header.add("SYSTEM_CORE_SITE_ID");
		header.add("SYSTEM_SITECODE");
		header.add("SYSTEM_STSTUS");
		header.add("SYSTEM_ORANGE_SITE_NAME");
		header.add("HOTCUTNEW_SITE");
		
		
		writerData.add(header);
		String []row = null;
		while ((row = csvReader.readNext()) != null) 
		{
			String GoldOrderRef 									= CommonUtils.isNULL(row[0]) ? row[0] : row[0].trim();
			String Billing_Account_Number					= CommonUtils.isNULL(row[1]) ? row[1] : row[1].trim();
			String Contract_IDs									= CommonUtils.isNULL(row[2]) ? row[2] : row[2].trim();
			String Account_Manager								= CommonUtils.isNULL(row[3]) ? row[3] : row[3].trim();
			String TARGET_IC01									= CommonUtils.isNULL(row[4]) ? row[4] : row[4].trim();
			String NEW_TARGET_SITE_ID						= CommonUtils.isNULL(row[5]) ? row[5] : row[5].trim();
			String NEW_TARGET_CORE_ADDRESS_ID		= CommonUtils.isNULL(row[6]) ? row[6] : row[6].trim();
			String TARGET_CUSTOMER_NAME				= CommonUtils.isNULL(row[7]) ? row[7] : row[7].trim();
			String SITECODE										= CommonUtils.isNULL(row[8]) ? row[8] : row[8].trim();
			String TARGET_ORANGE_SITENAME			= CommonUtils.isNULL(row[9]) ? row[9] : row[9].trim();
			
			ArrayList<String> key  = new ArrayList<String>(Arrays.asList(row));
			if(fileDataLinkValidationResult.containsKey(key))
			{
				ArrayList<String> comments = fileDataLinkValidationResult.get(key);
				String COMMENTS="";
				for(String comment : comments)
				{
					COMMENTS+=comment;
				}
				key.add("VALIDATION_FAILED");
				key.add(COMMENTS);
				writerData.add(key);
			}
			else if(fileDataSuccessResult.containsKey(key))
			{
				ArrayList<String[]> suggestedSiteData = fileDataSuccessResult.get(key);
				
			
				for(String[] suggestedData:suggestedSiteData)
				{
					key  = new ArrayList<String>(Arrays.asList(row));
					key.add("VALIDATION_SUCCESS");
					key.add("NA");
					System.out.println("suggestedData length is "+suggestedData.length);
					for(int x = 0 ; x <suggestedData.length ; x++ )
					{
						key.add(suggestedData[x]);
					}
					writerData.add(key);
					
				}
			}
			else
			{
				key.add("VALIDATION_FAILED_SEVERE_ERROR");
				key.add("ERROR");
				siteFixViewComponents.getQueryResult().append("A SEVERE ERROR >>>>>>, key not found in any of the map , neither in Pass and Faild Map for Order "+key.get(0)+"\n");
				writerData.add(key);
			}
			
		}
		csvReader.close();
		
		CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(updatedCSVFilePath));
		for(ArrayList<String> rowdata : writerData)
	    {
			String[] stockArr = new String[rowdata.size()];
			stockArr = rowdata.toArray(stockArr);
			writer.writeNext(stockArr);
	    }
	    writer.close();
	    
	    rowscount = getCSVROWCount(updatedCSVFilePath);
	}
	
	
	
	private boolean validateFailureandSuccessMap(Map<ArrayList<String>,ArrayList<String>> fileDataLinkValidationResult,Map<ArrayList<String>,ArrayList<String[]>> fileDataSuccessResult)
	{
		boolean isAnyKeyMatch = false;
		// No key should match in both maps
		
		Iterator<Map.Entry<ArrayList<String>, ArrayList<String>>> entries1 = fileDataLinkValidationResult.entrySet().iterator();

		
		while (entries1.hasNext()) 
		{
			Map.Entry<ArrayList<String>, ArrayList<String>> entry = entries1.next();
			ArrayList<String> key = entry.getKey();
			ArrayList<String> value = entry.getValue();
			
			if(fileDataSuccessResult.containsKey(key))
			{
				isAnyKeyMatch = true;
				siteFixViewComponents.getQueryResult().append("MAP VALIDATION FAILED, KEY FOUND IN FAILED AND PASS MAP FOR ORDER "+value.get(0)+"\n");
			}
		}
		
	 return isAnyKeyMatch;
	}
	
	private void writeSQLScript()throws Exception
	{
		CSVReader csvReader = new CSVReader(new FileReader(updatedCSVFilePath));
		String []row = csvReader.readNext();
		double progresscount = 0.0;
		while ((row = csvReader.readNext()) != null) 
		{
			progresscount++;	
			ProgressMonitorPane.getInstance().setProgress(progresscount,rowscount);	
			
			String SYSTEM_STATUS				= CommonUtils.isNULL(row[10]) ? row[10] : row[10].trim();
			if(SYSTEM_STATUS.equalsIgnoreCase("VALIDATION_SUCCESS"))
			{
			String GoldOrderRef 									= CommonUtils.isNULL(row[0]) ? row[0] : row[0].trim();
			String Billing_Account_Number					= CommonUtils.isNULL(row[1]) ? row[1] : row[1].trim();
			String Contract_IDs									= CommonUtils.isNULL(row[2]) ? row[2] : row[2].trim();
			String Account_Manager								= CommonUtils.isNULL(row[3]) ? row[3] : row[3].trim();
			String TARGET_IC01									= CommonUtils.isNULL(row[4]) ? row[4] : row[4].trim();
			String NEW_TARGET_SITE_ID						= CommonUtils.isNULL(row[5]) ? row[5] : row[5].trim();
			String NEW_TARGET_CORE_ADDRESS_ID		= CommonUtils.isNULL(row[6]) ? row[6] : row[6].trim();
			String TARGET_CUSTOMER_NAME				= CommonUtils.isNULL(row[7]) ? row[7] : row[7].trim();
			String SITECODE										= CommonUtils.isNULL(row[8]) ? row[8] : row[8].trim();
			String TARGET_ORANGE_SITENAME			= CommonUtils.isNULL(row[9]) ? row[9] : row[9].trim();
		
			
			String SYSTEM_COMMENTS							= CommonUtils.isNULL(row[11]) ? row[11] : row[11].trim();
			String SYSTEM_ORDER								= CommonUtils.isNULL(row[12]) ? row[12] : row[12].trim();
			String SYSTEM_PRIMARY_CONTACT			= CommonUtils.isNULL(row[13]) ? row[13] : row[13].trim();
			String SYSTEM_ADDRESS_ID						= CommonUtils.isNULL(row[14]) ? row[14] : row[14].trim();
			String SYSTEM_CORE_SITE_ID					= CommonUtils.isNULL(row[15]) ? row[15] : row[15].trim();
			String SYSTEM_SITECODE							= CommonUtils.isNULL(row[16]) ? row[16] : row[16].trim();
			String SYSTEM_STASTUS							= CommonUtils.isNULL(row[17]) ? row[17] : row[17].trim();
			String SYSTEM_ORANGE_SITE_NAME			= CommonUtils.isNULL(row[18]) ? row[18] : row[18].trim();
			String HOTCUTNEW_SITE							= CommonUtils.isNULL(row[19]) ? row[19] : row[19].trim();
			
			String[] accountmanagerFirstLastName= Account_Manager.split("\\s+");
			
			String NEW_AccountManager 						= CommonUtils.getQueryResult("SELECT TRIL_GID FROM "+ConnectionBean.getDbPrefix()+"SC_USER WHERE PERSON=(SELECT TRIL_GID FROM "+ConnectionBean.getDbPrefix()+"SC_PERSON WHERE TRIL_GID =(SELECT TRIL_GID FROM "+ConnectionBean.getDbPrefix()+"SC_PERSON WHERE FIRSTNAME=? AND LASTNAME=?))",siteFixViewComponents,accountmanagerFirstLastName[0],accountmanagerFirstLastName[1]).get(0)[0];
			String NEW_EQ_BillingProfile						= CommonUtils.getQueryResult("SELECT TRIL_GID FROM "+ConnectionBean.getDbPrefix()+"EQ_BILLINGPROFILE WHERE BILLINGACCOUNTNUMBER=?",  siteFixViewComponents,Billing_Account_Number).get(0)[0]; 
			String NEW_EQ_CONTRACT			   			= CommonUtils.getQueryResult("SELECT TRIL_GID FROM "+ConnectionBean.getDbPrefix()+"EQ_CONTRACT WHERE CONTRACTID=?", siteFixViewComponents,Contract_IDs).get(0)[0]; 
			String NEW_EQ_DELIVERYPARTY 				= CommonUtils.getQueryResult("SELECT TRIL_GID FROM "+ConnectionBean.getDbPrefix()+"SC_ORGANIZATION WHERE ORGANIZATIONID=? ",  siteFixViewComponents,TARGET_IC01).get(0)[0]; 
			String NEW_EQ_REQUESTINGPARTY 			= CommonUtils.getQueryResult("SELECT TRIL_GID FROM "+ConnectionBean.getDbPrefix()+"SC_ORGANIZATION WHERE ORGANIZATIONID=? ",  siteFixViewComponents,TARGET_IC01).get(0)[0]; 
			String NEW_SITE 										= CommonUtils.getQueryResult("SELECT TRIL_GID FROM "+ConnectionBean.getDbPrefix()+"EQ_SITE WHERE ADDRESS_ID =? AND STATUS='0'",  siteFixViewComponents,SYSTEM_ADDRESS_ID).get(0)[0]; 
			String NEW_ORDER_CURRENCY 					= CommonUtils.getQueryResult("SELECT CURRENCYCODE FROM "+ConnectionBean.getDbPrefix()+"EQ_BILLINGPROFILE WHERE BILLINGACCOUNTNUMBER=?",  siteFixViewComponents,Billing_Account_Number).get(0)[0];
			
			ArrayList<String[]> prevValue 					= CommonUtils.getQueryResult("SELECT ACCOUNTMANAGER,EQ_BillingProfile,EQ_CONTRACT,EQ_DELIVERYPARTY,EQ_REQUESTINGPARTY,SITE,ORDER_CURRENCY,EQADDCONTRACTCOND FROM "+ConnectionBean.getDbPrefix()+"SC_QUOTE WHERE QUOTENUMBER=?",siteFixViewComponents,SYSTEM_ORDER);
			
			String Prev_ACCOUNTMANAGER 					=(null == prevValue ? "" :prevValue.get(0)[0]);
			String Prev_EQ_BillingProfile						=(null == prevValue ? "" :prevValue.get(0)[1]);
			String Prev_EQ_CONTRACT						=(null == prevValue ? "" :prevValue.get(0)[2]);
			String Prev_EQ_DELIVERYPARTY					=(null == prevValue ? "" :prevValue.get(0)[3]);
			String Prev_EQ_REQUESTINGPARTY			=(null == prevValue ? "" :prevValue.get(0)[4]);
			String Prev_SITE										=(null == prevValue ? "" :prevValue.get(0)[5]);
			String Prev_ORDER_CURRENCY					=(null == prevValue ? "" :prevValue.get(0)[6]);
			String Prev_EQADDCONTRACTCOND			=(null == prevValue ? "" :prevValue.get(0)[7]);
			
			ArrayList<String[]> prevAccountManagerName 			= CommonUtils.getQueryResult("SELECT FIRSTNAME,LASTNAME FROM  "+ConnectionBean.getDbPrefix()+"SC_PERSON WHERE TRIL_GID=(SELECT PERSON FROM "+ConnectionBean.getDbPrefix()+"SC_USER WHERE TRIL_GID=(?))",siteFixViewComponents,Prev_ACCOUNTMANAGER);
			ArrayList<String[]> prevBillingAccountNumber 			= CommonUtils.getQueryResult("SELECT BILLINGACCOUNTNUMBER FROM "+ConnectionBean.getDbPrefix()+"EQ_BILLINGPROFILE WHERE TRIL_GID=?",siteFixViewComponents,Prev_EQ_BillingProfile);
			ArrayList<String[]> prevContractNumber 					= CommonUtils.getQueryResult("SELECT CONTRACTID FROM  "+ConnectionBean.getDbPrefix()+"EQ_CONTRACT WHERE TRIL_GID=?",siteFixViewComponents,Prev_EQ_CONTRACT);
			ArrayList<String[]> prevDeliveryParty 							= CommonUtils.getQueryResult("SELECT ORGANIZATIONID FROM  "+ConnectionBean.getDbPrefix()+"SC_ORGANIZATION WHERE TRIL_GID=?",siteFixViewComponents,Prev_EQ_DELIVERYPARTY);
			ArrayList<String[]> prevRequestingParty 					= CommonUtils.getQueryResult("SELECT ORGANIZATIONID FROM  "+ConnectionBean.getDbPrefix()+"SC_ORGANIZATION WHERE TRIL_GID=?",siteFixViewComponents,Prev_EQ_REQUESTINGPARTY);
			ArrayList<String[]> prevSite										= CommonUtils.getQueryResult("SELECT ADDRESS_ID,CORE_SITE_ID,ORANGE_SITENAME FROM "+ConnectionBean.getDbPrefix()+"EQ_SITE WHERE TRIL_GID=?",siteFixViewComponents,Prev_SITE);
			
			String newComments ="--Order ICO and Site Migration--- " +
					"(1)Account Manager ["+((null == prevAccountManagerName || prevAccountManagerName.size() ==0 )? "" : prevAccountManagerName.get(0)[0])+"    "+(null == prevAccountManagerName || prevAccountManagerName.size() ==0? "" : prevAccountManagerName.get(0)[1])+" ] to ["+accountmanagerFirstLastName[0]+"   "+accountmanagerFirstLastName[1]+"]"+
					"(2)BAN ["+((null == prevBillingAccountNumber || prevBillingAccountNumber.size() ==0) ? "" : prevBillingAccountNumber.get(0)[0])+"] to ["+Billing_Account_Number+"]"+
					"(3)Contract ["+((null == prevContractNumber || prevContractNumber.size() ==0) ? "" :prevContractNumber.get(0)[0])+"] to ["+Contract_IDs+"]"+
					"(4)Delivery Party ["+((null == prevDeliveryParty || prevDeliveryParty.size() ==0) ? "" :prevDeliveryParty.get(0)[0])+"] to ["+TARGET_IC01+"]"+
					"(5)Requesting Party ["+((null == prevRequestingParty || prevRequestingParty.size() ==0) ? "" :prevRequestingParty.get(0)[0])+"] to ["+TARGET_IC01+"]"+
					"(6)Site ["+((null == prevSite || prevSite.size() ==0) ? "" :prevSite.get(0)[0])+"] to ["+SYSTEM_ADDRESS_ID+"]"+
					"(7)Core_site_id ["+((null == prevSite || prevSite.size() ==0) ? "" :prevSite.get(0)[1])+"] to ["+NEW_TARGET_SITE_ID+"]"+
					"(6)orange_sitename ["+((null == prevSite || prevSite.size() ==0) ? "" :prevSite.get(0)[2])+"] to ["+TARGET_ORANGE_SITENAME+"]"+
					"(7)Currency ["+Prev_ORDER_CURRENCY+"] to ["+NEW_ORDER_CURRENCY+"]";
			
			//String newCommentsUpdated =Prev_EQADDCONTRACTCOND+newComments;
			String newCommentsUpdated =newComments;
			if(newCommentsUpdated.length()>1024)
			{
				newCommentsUpdated = newCommentsUpdated.substring(0,1024);
			}
			goldsql.add("-- SCRIPT STATRTS FOR GOLDORDREF "+GoldOrderRef +" AND SYSTEM ORDER "+SYSTEM_ORDER);
			
			if(null == SYSTEM_PRIMARY_CONTACT || SYSTEM_PRIMARY_CONTACT.isEmpty() || SYSTEM_PRIMARY_CONTACT.equalsIgnoreCase("null")){
				goldsql.add("-- DEFAULT CONTACT WAS NULL FOR TARGET SITE : USE ORDER SITE CONTACT AS DEFAULT CONTACT");
				String QL = "SELECT EQ_DEFAULTCONTACT1 from "+ConnectionBean.getDbPrefix()+"EQ_SITE WHERE TRIL_GID=(SELECT SITE FROM "+ConnectionBean.getDbPrefix()+"sc_quote where quotenumber=?)";
				ArrayList<String[]> res = CommonUtils.getQueryResult(QL,  siteFixViewComponents,SYSTEM_ORDER);
				String updateDC1 = "UPDATE "+ConnectionBean.getDbPrefix()+"EQ_SITE SET EQ_DEFAULTCONTACT1='"+res.get(0)[0]+"',MODIFICATIONDATE=sysdate where sitecode='"+SYSTEM_SITECODE+"';";
				goldsql.add(updateDC1);
			}
			
			
			goldsql.add("-- EQ NOTES COMMENTS for System order "+SYSTEM_ORDER +" "+newCommentsUpdated);
			
			String GOLDUpdateQuery ="UPDATE "+ConnectionBean.getDbPrefix()+"SC_QUOTE SET ACCOUNTMANAGER='"+NEW_AccountManager+"', EQ_BillingProfile='"+NEW_EQ_BillingProfile+"', EQ_CONTRACT='"+NEW_EQ_CONTRACT+"', EQ_DELIVERYPARTY='"+NEW_EQ_DELIVERYPARTY+"', EQ_REQUESTINGPARTY='"+NEW_EQ_REQUESTINGPARTY+"', SITE='"+NEW_SITE+"', ORDER_CURRENCY='"+NEW_ORDER_CURRENCY+"',MODIFICATIONDATE=sysdate where quotenumber='"+SYSTEM_ORDER+"';";
			
			if(null != HOTCUTNEW_SITE && !HOTCUTNEW_SITE.isEmpty() && HOTCUTNEW_SITE.length() >1)
			{
				goldsql.add("-- SCRIPTS CHANGES HOTCUTNEWSITE FOR ORDER "+SYSTEM_ORDER);
				GOLDUpdateQuery ="UPDATE "+ConnectionBean.getDbPrefix()+"SC_QUOTE SET ACCOUNTMANAGER='"+NEW_AccountManager+"', EQ_BillingProfile='"+NEW_EQ_BillingProfile+"', EQ_CONTRACT='"+NEW_EQ_CONTRACT+"', EQ_DELIVERYPARTY='"+NEW_EQ_DELIVERYPARTY+"', EQ_REQUESTINGPARTY='"+NEW_EQ_REQUESTINGPARTY+"', HOTCUTNEWSITE='"+NEW_SITE+"', ORDER_CURRENCY='"+NEW_ORDER_CURRENCY+"', MODIFICATIONDATE=sysdate where quotenumber='"+SYSTEM_ORDER+"';";
			}
			goldsql.add(GOLDUpdateQuery);
			
			String goldUpdateSiteOSRandCoreId ="UPDATE "+ConnectionBean.getDbPrefix()+"EQ_SITE SET CORE_SITE_ID='"+NEW_TARGET_SITE_ID+"', ORANGE_SITENAME='"+TARGET_ORANGE_SITENAME+"',MODIFICATIONDATE=sysdate WHERE ADDRESS_ID='"+SYSTEM_ADDRESS_ID+"' AND STATUS ='0';";
			goldsql.add(goldUpdateSiteOSRandCoreId);
			goldsql.add("-- SCRIPT ENDS FOR ORDER "+GoldOrderRef);
			
			
			csisql.add("-- SCRIPT STARTS FOR ORDER "+GoldOrderRef +" BIGGEST SYSTEM ORDER "+SYSTEM_ORDER);
			// Update the details in cversion 
			// Lalit : Update script to change core_site_id (now it is as given in File instead of System) 
			String CSISQLUpdate = "UPDATE "+ConnectionBeanCSI.getDbPrefix()+"CVERSION SET SITEHANDLE='"+SYSTEM_SITECODE+"', CUSTHANDLE='"+TARGET_IC01+"', ENDUSERHANDLE='"+TARGET_IC01+"', CORESITEID='"+NEW_TARGET_SITE_ID+"', ADDRESSID='"+SYSTEM_ADDRESS_ID+"',Lupddate=Sysdate WHERE ORDHANDLE='"+SYSTEM_ORDER+"';";
			csisql.add(CSISQLUpdate);
			
			// Get the List of service element
			//String usdiddatasql = "select Serviceelementid,USID from "+ConnectionBeanCSI.getDbPrefix()+"cserviceelement where serviceelementid in (select serviceelementid from "+ConnectionBeanCSI.getDbPrefix()+"CVERSIONSERVICEELEMENT  where versionid in (select versionid from "+ConnectionBeanCSI.getDbPrefix()+"cversion where ordhandle=?))";
			
			
			ArrayList<String[]> usids  = CommonUtils.getCSIServiceElementIds(SYSTEM_ORDER,siteFixViewComponents);
			for(String arr[]:usids)
			{
				String serviceElementID = arr[0];
				String usid = arr[1];
				String autoUpdatedUSID = CommonUtils.autoUpDateUSIDandICO(usid,SYSTEM_SITECODE,TARGET_IC01);
			
				String isAutoUpdated = "True";
				if (autoUpdatedUSID.equalsIgnoreCase(usid)) 
				{
					isAutoUpdated = "False";
				}
				if(isAutoUpdated.equals("True"))
				{
					String csiexecutionData = "UPDATE "+ConnectionBeanCSI.getDbPrefix()+"CSERVICEELEMENT SET USID='"+ autoUpdatedUSID+ "',Lupddate=Sysdate WHERE SERVICEELEMENTID='"+serviceElementID+"';";
					csisql.add(csiexecutionData);
				}
			}
			csisql.add("-- SCRIPT ENDS FOR ORDER "+GoldOrderRef);
		}
		}
		csvReader.close();
		createGOLDScriptFile();
		createCSIScriptFile();
		CommonUtils.createConsoleLogFile(siteFixViewComponents);
		
	}
	
	
}
