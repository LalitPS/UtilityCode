package com.orange.util.others;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.ui.component.SiteFixedViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.CustomCSVWriter;
import com.orange.util.ProgressMonitorPane;
import com.orange.util.csm.ConnectionForGOLD;

public class LinkedupSitesNext {

	/*
	 * addressMap : Keys:-
	 * **************************
	 * AddressId
	 * primary site code
	 * primary site code status
	 * secondary site code
	 * secondary site code status
	 * primary osr
	 * secondary osr
	 * eq site of
	 * core ico
	 * 
	 * addressMap : Values:-
	 * **************************
	 * Org id as Key
	 * ArrayList of quotes as values of key
	 * 
	 */
	
	private Map<ArrayList<String> ,Map<String,ArrayList<String>>> addressMap;
	private Map<ArrayList<String> ,Map<String,ArrayList<String>>> addressMapFiltered;
	private ArrayList<String[]> analysisReportList;
	
	private String consolidatedDedupFilePath;
	
	private ArrayList<String> errors;
	private String filePath;
	
	/*
	 * orderFormDataMap : Keys:-
	 * **************************
	 * AddressId
	 * primary site code
	 * primary site code status
	 * secondary site code
	 * secondary site code status
	 * primary osr
	 * secondary osr
	 * eq site of
	 * core ico
	 * core ico status
	 * 
	 * orderFormDataMap : Values:-
	 * **************************
	 * 
	 * ArrayList of ord ico , ord site code , ord osr
	 * 
	 */
	
	Map<ArrayList<String>,ArrayList<ArrayList<String>>> orderFormDataMap;
	private String resultFileLoc;
	private SiteFixedViewComponents siteFixViewComponents; 
	
	public LinkedupSitesNext(SiteFixedViewComponents siteFixViewComponents) throws Exception{
		this.siteFixViewComponents =siteFixViewComponents;
		this.filePath = this.siteFixViewComponents.getFileToValidate().getText();
		this.consolidatedDedupFilePath = this.siteFixViewComponents.getDedupToValidate().getText();
		
		int index = filePath.lastIndexOf(".");
		String sub = filePath.substring(0,index) ;
		this.resultFileLoc=sub+"_Analysis.csv";
		
		
		this.addressMap = new HashMap<ArrayList<String> ,Map<String,ArrayList<String>>>();
		this.orderFormDataMap = new HashMap<ArrayList<String>,ArrayList<ArrayList<String>>> (); 
		this.errors = new ArrayList<String>();
		this.analysisReportList = new ArrayList<String[]>();
	
	
		ArrayList<String> errorfound = invokeFileValidator();
		if(errorfound.size()==0)
		{
			siteFixViewComponents.getQueryResult().append("File Validated Successfuully ..\n");
			readFileData();
		}
		else
		{
			for(String err : errorfound)
			{
				siteFixViewComponents.getQueryResult().append(err +" ..\n");
			}
		}
	}
	
	
	private void analysisSiteFixData() throws Exception{
		/*
		Case 1:
		1) If CORE_ICO = ORD_ICO of PRIM_SITECODE 
		Then make SEC_SITECODE , a legacy site and change status of SEC_SITECODE to 0 & move orders of ICO found for SEC_SITECODE to SEC_SITECODE.
		
		Case 2:
		2)If CORE_ICO != ORD_ICO of PRIM_SITECODE 
		Then make PRIM_SITECODE , a legacy site and change status of SEC_SITECODE to 0 & move orders of ICO found for SEC_SITECODE to SEC_SITECODE.
		*/
		
	
		Iterator<Map.Entry<ArrayList<String>,Map<String,ArrayList<String>>>> addressMapFilteredReplica = addressMapFiltered.entrySet().iterator();
		
		int total = addressMapFiltered.size();
		int cc = 0;
		
		while (addressMapFilteredReplica.hasNext()) 
		{
			cc++;
			ProgressMonitorPane.getInstance().setProgress(cc,total);
			Map.Entry<ArrayList<String>,Map<String,ArrayList<String>>> innerEntry = addressMapFilteredReplica.next();
			
			ArrayList<String> key = innerEntry.getKey();
		
			
			
			String ADDRESS_ID 			=key.get(0);
			String PRIM_SITECODE 		=key.get(1);
			//String PRIM_SITECODE_STATUS =key.get(2);
			String SEC_SITECODE 		=key.get(3);
			//String SEC_SITECODE_STATUS 	=key.get(4);
			//String PREM_OSR 			=key.get(5);
			//String SEC_OSR 				=key.get(6);
			//String EQ_SITEOF 			=key.get(7);
			String CORE_ICO 			=key.get(8);
			//String CORE_STATUS 			=key.get(9);
			//String REPETITION 			=key.get(10);
			
		
			ArrayList<ArrayList<String>> values = orderFormDataMap.get(key);
			String ord_ico_as_core_ico="";
			String ord_site_code_as_prim_site_code="";
			boolean isOrdICOasCore_OrdSitecodeasPrim=false;
			boolean isRepeatedSiteCodeForAnAddressId = false;
			boolean isOrdICONOTasCore_OrdSitecodeasPrim = false;
			
			ArrayList<String> checkRepeatedSiteCodes = new ArrayList<String>();
			
			for(ArrayList<String> value : values)
			{
				
				String ord_ico = value.get(0);
				String ord_site_code = value.get(1);
				/*
				 * To check If sitecode repeated for and address id. 
				 * Such case should be failed and required manual investigation.
				 * 
				 */
				
				if(checkRepeatedSiteCodes.contains(ord_site_code))
				{
					isRepeatedSiteCodeForAnAddressId = true;
				}
				else
				{
					checkRepeatedSiteCodes.add(ord_site_code);
				}
				
				if(ord_site_code.equals(PRIM_SITECODE) && ord_ico.equals(CORE_ICO))
				{
					ord_ico_as_core_ico=ord_ico;
					ord_site_code_as_prim_site_code = ord_site_code;
					isOrdICOasCore_OrdSitecodeasPrim = true;
				}
				
				if(ord_site_code.equals(PRIM_SITECODE) && !ord_ico.equals(CORE_ICO))
				{
					ord_ico_as_core_ico=CORE_ICO;
					ord_site_code_as_prim_site_code = PRIM_SITECODE;
					isOrdICONOTasCore_OrdSitecodeasPrim = true;
				}
				
	
			}
			
			/*
			 * If sitecode repeated for and address id. 
			 * Such case should be failed and required manual investigation.
			 * 
			 */
			
			
			if(isRepeatedSiteCodeForAnAddressId)
			{
				String[] analysisRow = new String[14];
				analysisRow[8] = key.get(0);
				analysisRow[9] = "FAILED";
				analysisRow[10] = "REPEATED_SITECODE_FOR_ADDRESSID "+ADDRESS_ID;
				analysisReportList.add(analysisRow);
			}
			
			
			
			/*
			 * CORE_ICO = ORD_ICO of PRIM_SITECODE 
			 * Then make SEC_SITECODE , a legacy site and change status of SEC_SITECODE to 0 & 
			 * move orders of ICO found for SEC_SITECODE to SEC_SITECODE.
			 * 
			 * if other sitecode(s) of order from matches with sec_sitecode , then move their order to sec_sitecode.
			 * else if not matches to any available sec_sitecode(s) , check this ord_form sitecode in DB , 
			 * if status is 0 , then use ord_form sitecode. 
			 * if status is not 0 , then use sec_sitecode sitecode. 
			 * if multiple sec_sitecode found in sheet , FAILD this case .. need manual invetigation.
			 * 
			 * 
			 */
			if(!isRepeatedSiteCodeForAnAddressId)
			{
			    //CORE_ICO = ORD_ICO of PRIM_SITECODE 
				if(isOrdICOasCore_OrdSitecodeasPrim)
				{
					for(ArrayList<String> value : values)
					{
						String ord_ico = value.get(0);
						String ord_site_code = value.get(1);
						
						
						if(ord_site_code.equals(ord_site_code_as_prim_site_code) && ord_ico.equals(ord_ico_as_core_ico))
						{
							// Nothing to do in this case , as this already processed. 
							// We need to process only those sitecodes , which doesn't satisfy this condition.
							
							
							//IF ONLY ONE ORG ID is LINKED then nothing to DO
							// But for USER INFO mark Status NO_ANALYSIS_REQUIRED_CLEAN_CASE
							Map<String,ArrayList<String>> VAL = innerEntry.getValue();
							
							if(VAL.size() == 1 )
							{
							String[] analysisRow = new String[14];
							analysisRow[8] = key.get(0);
							analysisRow[9] = "AUTO_PASS";
							analysisRow[10] = "NO_ANALYSIS_REQUIRED_CLEAN_CASE.(CORE_ICO = ORD_ICO of PRIM_SITECODE and ONLY ONE ORGID) ";
							analysisReportList.add(analysisRow);
							
							}
							
						}
						else
						{
							String [] sec_site_codes = SEC_SITECODE.split(";");
							boolean isMatchwithSecSiteCodeList= false;
							for(String sec_site_code : sec_site_codes)
							{
								if(sec_site_code.equals(ord_site_code))
								{
									isMatchwithSecSiteCodeList = true;
								}
							}
							/*
							 * If ord form sitecode match with the list of sec sitecode(s).
							 * Use this as sec sitecode
							 * 
							 */
							if(isMatchwithSecSiteCodeList)
							{
								/*
								 * 1. Make ord_site_code as legacy
								 * 2. Move orders of linked ico , to sec_sitecode from prim sitecode
								 */
								
								fillAnalysisRow(key,ord_ico,PRIM_SITECODE,ord_site_code,ord_site_code);
							}
							/*
							 * If ord form sitecode does match with the list of sec sitecode(s).
							 * get the status from database.
							 * 
							 */
							else
							{
								String status = getStatusOfSiteCode(ord_site_code);
								if(status.equals("0"))
								{
									/*
									 * 1. Make ord_site_code as legacy
									 * 2. Move orders of linked ico , to sec_sitecode from prim sitecode
									 */
									fillAnalysisRow(key,ord_ico,PRIM_SITECODE,ord_site_code,ord_site_code);
									
								}
								/*
								 * 
								 * sitecode not found in data base
								 */
								else if(status.equals("-1"))
								{
									String[] analysisRow = new String[14];
									analysisRow[8] = key.get(0);
									analysisRow[9] = "FAILED";
									analysisRow[10] = "SITECODE_NOT_FOUND_IN_DATABASE_"+ord_site_code;
									analysisReportList.add(analysisRow);
								}
								/*
								 * If sitecode status is not zero in database
								 * 
								 */
								else
								{
									 /*
									 * If single sec_side_code in sheet , use this
									 * else Failed..
									 */
									sec_site_codes = SEC_SITECODE.split(";");
									if(sec_site_codes.length ==1)
									{
										/*
										 * 1. Make ord_site_code as legacy
										 * 2. Move orders of linked ico , to sec_sitecode from prim sitecode
										 */
										
										// IF NO sec Sitecode in sheet ... SEC_SITECODE = ord_site_code
										
										if(null == SEC_SITECODE || SEC_SITECODE.isEmpty())
										{
											SEC_SITECODE = ord_site_code;
										
										}
										
										fillAnalysisRow(key,ord_ico,PRIM_SITECODE,SEC_SITECODE,SEC_SITECODE);
										
									}
									else
									{
										String[] analysisRow = new String[14];
										analysisRow[8] = key.get(0);
										analysisRow[9] = "FAILED";
										analysisRow[10] = "MULTIPLE_SEC_SITECODE_FOUND_"+SEC_SITECODE;
										analysisReportList.add(analysisRow);
									}
								}
							}
						}
					}//
				}
				/*
				 * If CORE_ICO != ORD_ICO of PRIM_SITECODE 
				 * Then make PRIM_SITECODE , a legacy site and change status of SEC_SITECODE to 0 & move orders of ICO found for SEC_SITECODE to SEC_SITECODE.
				 * 
				 */
				
				//CORE_ICO != ORD_ICO of PRIM_SITECODE 
				
				else if(isOrdICONOTasCore_OrdSitecodeasPrim)
				{
					for(ArrayList<String> value : values)
					{
						String ord_ico = value.get(0);
						String ord_site_code = value.get(1);
						if(ord_site_code.equals(ord_site_code_as_prim_site_code) && ord_ico.equals(ord_ico_as_core_ico))
						{
							// Nothing to do in this case , as this already processed. 
							// We need to process only those site codes , which doesn't satisfy this condition.
						}
						else
						{
							String [] sec_site_codes = SEC_SITECODE.split(";");
							boolean isMatchwithSecSiteCodeList= false;
							for(String sec_site_code : sec_site_codes)
							{
								if(sec_site_code.equals(ord_site_code))
								{
									isMatchwithSecSiteCodeList = true;
								}
							}
							/*
							 * If ord form sitecode match with the list of sec sitecode(s).
							 * Use this as sec sitecode
							 * 
							 */
							
						
							if(isMatchwithSecSiteCodeList)
							{
								/*
								 * 1. Make ord_site_code as legacy
								 * 2. Move orders of linked ico , to sec_sitecode from prim sitecode
								 */
								fillAnalysisRow(key,ord_ico,PRIM_SITECODE,ord_site_code,PRIM_SITECODE);
							}
							/*
							 * If ord form sitecode does match with the list of sec sitecode(s).
							 * get the status from database.
							 * 
							 */
							else
							{
								
								
								String status = getStatusOfSiteCode(ord_site_code);
								
							
								if(status.equals("0"))
								{
									/*
									 * 1. Make ord_site_code as legacy
									 * 2. Move orders of linked ico , to sec_sitecode from prim sitecode
									 */
									fillAnalysisRow(key,ord_ico,PRIM_SITECODE,ord_site_code,PRIM_SITECODE);
								}
								/*
								 * 
								 * sitecode not found in data base
								 */
								else if(status.equals("-1"))
								{
									String[] analysisRow = new String[14];
									analysisRow[8] = key.get(0);
									analysisRow[9] = "FAILED";
									analysisRow[10] = "SITECODE_NOT_FOUND_IN_DATABASE_"+ord_site_code;
									analysisReportList.add(analysisRow);
								}
								/*
								 * If sitecode status is not zero in databse
								 * 
								 */
								else
								{
									 /*
									 * If single sec_side_code in sheet , use this
									 * else Failed..
									 */
									sec_site_codes = SEC_SITECODE.split(";");
									
								
									if(sec_site_codes.length ==1)
									{
										/*
										 * 1. Make ord_site_code as legacy
										 * 2. Move orders of linked ico , to sec_sitecode from prim sitecode
										 */
										
										
										// IF NO Sitecode in sheet ... SEC_SITECODE = ord_site_code
										
										if(null == SEC_SITECODE || SEC_SITECODE.isEmpty())
										{
											SEC_SITECODE = ord_site_code;
											
										}
										fillAnalysisRow(key,ord_ico,PRIM_SITECODE,SEC_SITECODE,PRIM_SITECODE);
									}
									else
									{
										String[] analysisRow = new String[14];
										analysisRow[8] = key.get(0);
										analysisRow[9] = "FAILED";
										analysisRow[10] = "MULTIPLE_SEC_SITECODE_FOUND_"+SEC_SITECODE;
										analysisReportList.add(analysisRow);
									}
								}
							}
						}
					}
					
				}
				
				/* If No Primary site code found form ORD form 
				 * 
				 * FAILED these cases PRIM_SITECODE_NOT_MATCH_WITH_ORD_SITECODES
				 */
				else
				{
					
					String[] analysisRow = new String[14];
					analysisRow[8] = key.get(0);
					analysisRow[9] = "FAILED";
					analysisRow[10] = "PRIM_SITECODE_NOT_MATCH_WITH_ORD_SITECODES_(Address ID : "+key.get(0)+")";
					analysisReportList.add(analysisRow);
				}
			}
		}
	}
	
	private ArrayList<String> existingORDSiteCodes(ArrayList<ArrayList<String>> ordFormRows){
		ArrayList<String> ordsitecodes = new ArrayList<String>();
		for(ArrayList<String> ordFormRow : ordFormRows){
			ordsitecodes.add(ordFormRow.get(1));
		}
		
		return ordsitecodes;
		
	}
	private void fillAddressMap(ArrayList<String> key , String orgIdInnerKey, String quote){
		if(addressMap.containsKey(key))
		{
			Map<String,ArrayList<String>> innerMap = addressMap.get(key);
			if(innerMap.containsKey(orgIdInnerKey))
			{
				ArrayList<String> quotes = innerMap.get(orgIdInnerKey);
				quotes.add(quote);
				innerMap.put(orgIdInnerKey, quotes);
				addressMap.put(key, innerMap);
				quotes = null;
			}
			else{
				
				ArrayList<String> quotes = new ArrayList<String>();
				quotes.add(quote);
				innerMap.put(orgIdInnerKey, quotes);
				addressMap.put(key, innerMap);
				quotes = null;
				
			}
		}
		else
		{
		Map<String,ArrayList<String>> innerMap = new HashMap<String,ArrayList<String>>();
		ArrayList<String> quotes = new ArrayList<String>();
		quotes.add(quote);
		innerMap.put(orgIdInnerKey, quotes);
		addressMap.put(key, innerMap);
		}
	}
	
	private void fillAnalysisRow(ArrayList<String> key, String ord_ico,String repSitecode, String replacementSiteCode, String legacySite) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		
		/*
		 * Get the occurane of ICO in sc_organization table.
		 * 
		 */
		
		String QL = "select tril_gid,organizationid, status from " +
				ConnectionBean.getDbPrefix()+"sc_organization where organizationid in( " +
				"select organizationid from "+ConnectionBean.getDbPrefix()+"sc_organization where tril_gid = ( " +
				"select eq_siteof from "+ConnectionBean.getDbPrefix()+"eq_site where sitecode=?))";
		
		ArrayList<String[]> result = CommonUtils.getQueryResult(QL,siteFixViewComponents,replacementSiteCode);
		
		/*
		 *get the list of orders 
		*/
		
	
		Map<String,ArrayList<String>> orgIds = addressMap.get(key);
		ArrayList<String> orders = orgIds.get(ord_ico);
		
		if(null == orders)
		{
		/*
		 * This is the case where order form ICO , not exists in  Organization Ids
		 * 	
		 */
			String[] analysisRow = new String[14];
			analysisRow[8] = key.get(0);
			analysisRow[9] = "FAILED";
			analysisRow[10] = "ORD_ICO_NOT_FOUND_IN_ORG_IDS_"+ord_ico;
			analysisReportList.add(analysisRow);
		}
		else if(null != result && result.size()>1)
		{
			/*
			 * This is the case where replacement sitecode's ICO found multiple
			 * 	
			 */
				String[] analysisRow = new String[14];
				analysisRow[8] = key.get(0);
				analysisRow[9] = "FAILED";
				analysisRow[10] = "ICO_FOUND_MULTIPLE_IN_ORGANIZATION_"+result.get(0)[1];
				analysisReportList.add(analysisRow);
		
		}
		else
		{
		String ords="";
		for(String ord:orders)
		{
			ords+=ord+";";
		}
		ords = filterString(ords);
		
		String replacesitecode = repSitecode;
		String replacesitecodestatus = "0";
		String replacementsitecode = replacementSiteCode;
		String replacementsitecodestatus = "0";
		String replaceOSR ="";
		String replacementOSR ="";
		String moveorders = ords;
		String legSite = legacySite;
		
		String[] analysisRow = new String[14];
		
		analysisRow[0] = replacesitecode;
		analysisRow[1] = replacesitecodestatus;
		
		analysisRow[2] = replacementsitecode;
		analysisRow[3] = replacementsitecodestatus;
		
		analysisRow[4] = replaceOSR;
		analysisRow[5] = replacementOSR;
		
		analysisRow[6] = moveorders;
		analysisRow[7] = legSite;
		analysisRow[8] = key.get(0);
		
		analysisRow[9] = "Passed";
		analysisRow[10] = "";
		
		/*
		 * Don't show if row has replacement = replace site code
		 */
		if(!replacesitecode.equals(replacementsitecode)){
			analysisReportList.add(analysisRow);	
		}
		else{
			
			/*
			These are the case where ORD form PRIMSITECODES ORD ICO ! CORE ICO 
			and Only ONE ORG ID linked ..
			*/
			// If only one organization Id exists 
			/*
			 * 
			 * then only make prim sitecode as legacy , nothing extra
			 */
		
			if(orgIds.size()==1)
			{
				analysisRow[0] = replacesitecode;
				analysisRow[1] = replacesitecodestatus;
				
				analysisRow[2] = replacementsitecode;
				analysisRow[3] = replacementsitecodestatus;
				
				analysisRow[4] = replaceOSR;
				analysisRow[5] = replacementOSR;
				
				analysisRow[6] = moveorders;
				analysisRow[7] = legSite;
				analysisRow[8] = key.get(0);
				
				analysisRow[9] = "Passed";
				analysisRow[10] = "";
				
				analysisReportList.add(analysisRow);	
			}
			 
			
			}
		
		}
	}
	
	
	private void fillOrderFormDataMap(ArrayList<String> key,String ordIco,String ordSitecode,String ordOSR){
		
		if(!CommonUtils.isNULL(ordSitecode) || !CommonUtils.isNULL(ordOSR))
		{
			if(orderFormDataMap.containsKey(key))
			{
				ArrayList<ArrayList<String>> existing = orderFormDataMap.get(key);
				ArrayList<String> newformfindings = new ArrayList<String>();
				newformfindings.add(ordIco);
				newformfindings.add(ordSitecode);
				newformfindings.add(ordOSR);
				existing.add(newformfindings);
							
				orderFormDataMap.put(key, existing);
			}
			else
			{
				ArrayList<ArrayList<String>> existing = new ArrayList<ArrayList<String>>();
				ArrayList<String> newformfindings = new ArrayList<String>();
				newformfindings.add(ordIco);
				newformfindings.add(ordSitecode);
				newformfindings.add(ordOSR);
				existing.add(newformfindings);
				orderFormDataMap.put(key, existing);
			}
		}
	}

	private int filterBiggest(String GID, int biggestGID)
	{
		try
		{
		int X = Integer.parseInt(GID.trim());
		int Y = biggestGID;
		return ( X > Y ? X : Y);
		}catch(Exception e){
			return 0;
		}
	}
	private String filterString(String input){
		if(null != input && input.contains(";"))
		{
			return input.substring(0,input.lastIndexOf(";"));
			}
			else{
				return input;
			}
		
	}
	private Map<String,ArrayList<String[]>> getConsolidatedDedupMap() throws Exception{
		Map<String,ArrayList<String[]>> dedupMap = new HashMap<String,ArrayList<String[]>>();
		
		CSVReader csvReader = new CSVReader(new FileReader(consolidatedDedupFilePath));
		String []row = csvReader.readNext();
		
		while ((row = csvReader.readNext()) != null) 
		{
			String GID = row[0];
			String amResponse =row[1];
			String sitecode = row[22];
			
			
			if(!dedupMap.containsKey(sitecode))
			{
				String[] value = new String[2];
				value[0] = GID;
				value[1] = amResponse;
				ArrayList<String[]>  values = new ArrayList<String[]>();
				values.add(value);
				dedupMap.put(sitecode,values);
				values = null;
				value = null;
				
				
			}
			else if(dedupMap.containsKey(sitecode))
			{
				ArrayList<String[]>  values = dedupMap.get(sitecode);
				String[] value = new String[2];
				value[0] = GID;
				value[1] = amResponse;
				values.add(value);
				dedupMap.put(sitecode,values);
				values = null;
				value = null;
			}
		}
		csvReader.close();
		return dedupMap;
	}
	private void getDedupReplacementSiteCode(String sitecode,String comments,String[] row,Map<String,ArrayList<String[]>> dedupMap,CustomCSVWriter writer) throws Exception
	{
		comments+=">>"+sitecode;
		siteFixViewComponents.getQueryResult().append("Search for the sitecode >>"+sitecode +" in previous dedup file \n");
		/*
		 * Remove the duplicate for same sitecode
		 * some time found same sitecode is available in dedup sheet for same GID 
		 * :i.e. 
		 * 123   sitecode1    false
		 * 123   sitecode1    false
		 * 
		 * by this 'removeDuplicate' method one row will be removed.
		 */
		
		/*
		 * Now we have to remove the row if same sitecode is available 
		 * for same GID in true and false status both. 
		 * 
		 * :i.e. 
		 * 123   sitecode1    true
		 * 123   sitecode1    false
		 * 
		 * if this is the case , in this case false will be removed. 
		 * And remaining true will participate in process. 
		 * 
		 * 
		 */
		
		ArrayList<String[]> values = removeDuplicate(dedupMap.get(sitecode));
		
		
		boolean isAllTruefound = true;
		int biggestGID = 0;
		for(String[] value : values)
		{
			String GID = value[0];
			String amResponse = value[1];
			if(amResponse.trim().equalsIgnoreCase("False"))
			{
				isAllTruefound = false;
				biggestGID = filterBiggest(GID,biggestGID);
			}
			
		}
		if(isAllTruefound)
		{
			/*
			 * If Suggested Legacy is equals to replacement sitecode. 
			 * And if replacement sitecode changes due to previous DEDUP data, 
			 * then legacy sitecode must be chnage as new dedup replacement sitecode.
			 * 
			 * 
			 */
			
			String repSitecode = row[2] ;
			String legSitecode = row[7] ;
			
			if(repSitecode.equals(legSitecode))
			{
				legSitecode = sitecode;
				row[7] = legSitecode;
			}
			
			row[2] = sitecode;
			row[9] = "DEDUP_PASS";
			row[10] = comments+"(Found in previous DEDUP)";
			row[11] = getICOfromSiteCode(CommonUtils.isNULL(legSitecode)? "" : legSitecode);
			
			writer.writeNext(row);
		}
		else if(!isAllTruefound)
		{
		// Here Get the Biggest GID and then found that GIDs True sitecode.
		if(biggestGID == 0)
		{
			row[9] = "DEDUP_FAILED";
			row[10]=comments+"(NO TRUE GID FOUND FOR "+sitecode+")";
			row[11] = getICOfromSiteCode(CommonUtils.isNULL(row[7])? "" : row[7]);
			writer.writeNext(row);
		}
		else
		{
			String finalSiteCode = getTrueSiteCodeForGID(""+biggestGID);
			if(null == finalSiteCode || finalSiteCode.isEmpty() || finalSiteCode.length()<1)
			{
				row[9] = "DEDUP_FAILED";
				row[10]=comments+"(NO TRUE SITECODE FOR "+sitecode+" GID "+biggestGID+")";
				row[11] = getICOfromSiteCode(CommonUtils.isNULL(row[7])? "" : row[7]);
				writer.writeNext(row);
			}
			else
			{
				siteFixViewComponents.getQueryResult().append("Link found for sitecode in previous dedup file >>"+finalSiteCode +"\n");
				comments+=">>"+finalSiteCode;
				getDedupReplacementSiteCode(finalSiteCode,comments,row,dedupMap,writer);
				
			}
		}
		
	} 
		
}
	private String getICOfromSiteCode(String legacySitecode) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
		String localQuery = "select org.organizationid from "+ConnectionBean.getDbPrefix()+"sc_organization org,"+ConnectionBean.getDbPrefix()+"eq_site site where site.eq_siteof = org.tril_gid and site.sitecode=?";
		
		PreparedStatement pstmt = ConnectionForGOLD.getPreparedStatement(localQuery);
		pstmt.setString(1, legacySitecode);
		String upquery = localQuery.replace("?", "'"+legacySitecode +"'");
		siteFixViewComponents.getQueryResult().append(upquery+"\n");
		
		ResultSet resultSet = pstmt.executeQuery();
		String organizationid="Not found";
		while (resultSet.next()) 
		{
			organizationid = resultSet.getString(1);
		}
		pstmt.close();
		resultSet.close();
		
		return organizationid;
	}
	public String getResultFileLoc() {
		return resultFileLoc;
	}
	
	private ArrayList<ArrayList<String>> getSiteCodeForOSR(String osr) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		
		ArrayList<ArrayList<String>> datatablerows = new ArrayList<ArrayList<String>>();
		String query = "select sitecode,status from "+ConnectionBean.getDbPrefix()+"eq_site where orange_sitename=?";
		PreparedStatement pstmt = ConnectionForGOLD.getPreparedStatement(query);
		pstmt.setString(1, osr);
		ResultSet resultSet = pstmt.executeQuery();
		
		while (resultSet.next()) {
			ArrayList<String> datatablerow = new ArrayList<String>();
			String sitecode = resultSet.getString(1);
			String status = resultSet.getString(2);
			datatablerow.add(sitecode);
			datatablerow.add(status);
			datatablerows.add(datatablerow);
		}
		resultSet.close();
		pstmt.close();
		return datatablerows;
	}
	
	
	
	
	private String getStatusOfSiteCode(String sitecode) throws Exception{
		String dbstatus = "-1";
		String query = "select sitecode,status from "+ConnectionBean.getDbPrefix()+"eq_site where sitecode=?";
		PreparedStatement pstmt = ConnectionForGOLD.getPreparedStatement(query);
		pstmt.setString(1, sitecode);
		ResultSet resultSet = pstmt.executeQuery();
		
		while (resultSet.next()) 
		{
			//String dbsitecode = resultSet.getString(1);
			dbstatus = resultSet.getString(2);
		}	
		pstmt.close();
		resultSet.close();
		return dbstatus;
	}
	
	private String getTrueSiteCodeForGID(String GRPID) throws Exception{
		
		CSVReader csvReader = new CSVReader(new FileReader(consolidatedDedupFilePath));
		String []row = csvReader.readNext();
		String finalSitecode ="";
		while ((row = csvReader.readNext()) != null) 
		{
			String GID = row[0];
			String amResponse =row[1];
			if(GID.trim().equalsIgnoreCase(GRPID) && amResponse.trim().equalsIgnoreCase("True"))
			{
				finalSitecode = row[22];
			}
		}
		csvReader.close();
		
		return finalSitecode;
		
	}

/*
 * Before finalize replacement sitecode ,
 * we should check in previous dedup 
 * If Not Found in Previous DEDUP 			 : >> Replacement sitecode remain unchanged
 * If Found in Previous DEDUP as Latest TRUE : >> Replacement sitecode remain unchanged
 * If Found in Previous DEDUP as Latest FALSE: >> Corresponding true sitecode of dedup will replace the replacement sitecode. And will act as replacement sitecode.
 * 
 */
	
	
	public ArrayList<String> invokeFileValidator() throws Exception{
		CSVReader csvReader = new CSVReader(new FileReader(filePath));
		String []row = csvReader.readNext();
		
		for (int C = 0; C < row.length; C++) 
		{
			if (!LinkedupSitesEventClass.updatedcolumnName[C].equalsIgnoreCase(row[C])) 
			{
				errors.add("COLUMN_MISMATCH ::"+LinkedupSitesEventClass.updatedcolumnName[C]+"::"+row[C]);
			}
		}
		
		int count =1 ;
		while ((row = csvReader.readNext()) != null) 
		{
			if(row[0].isEmpty() )
			{
				errors.add("REPETITION_EMPTY:"+count);
			}
			
			if(row[1].isEmpty() )
			{
				errors.add("QUOTE_NUMBER_EMPTY:"+count);
			}
			if(row[3].isEmpty() )
			{
				errors.add("ADDRESS_ID_EMPTY:"+count);
			}
			if(row[4].isEmpty() )
			{
				errors.add("PRIM_SITECODE_EMPTY:"+count);
			}
			if(row[5].isEmpty())
			{
				errors.add("PRIM_SITECODE_STATUS_EMPTY:"+count);
			}
			if(!row[0].equals("SINGLE"))
			{
				if(row[6].isEmpty())
				{
					errors.add("SEC_SITECODE_EMPTY:"+count);
				}
				else if(row[7].isEmpty())
				{
					errors.add("SEC_SITECODE_STATUS_EMPTY:"+count);
				}
				else if(row[9].isEmpty())
				{
					errors.add("SEC_OSR_EMPTY::"+count);
				}
			}
			if(row[8].isEmpty())
			{
				errors.add("PRIM_OSR_EMPTY::"+count);
			}
			if(row[10].isEmpty())
			{
				errors.add("ORGANIZATION_ID_EMPTY::"+count);
			}
			if(row[11].isEmpty())
			{
				errors.add("EQ_SITEOF_EMPTY::"+count);
			}
			if(row[12].isEmpty())
			{
				errors.add("CORE_ICO_EMPTY::"+count);
			}
			if(row[13].isEmpty())
			{
				errors.add("CORE_ICO_STATUS_EMPTY::"+count);
			}
		    
		
			count++;
		}
		csvReader.close();
		return errors;
	}

	public void readFileData() throws Exception{
		
		CSVReader csvReader = new CSVReader(new FileReader(filePath));
		String []row = csvReader.readNext();
		
		while ((row = csvReader.readNext()) != null) 
		{
				String REPETITION     		=(removeNewLine(CommonUtils.isNULL(row[0]) ? row[0] : row[0].trim()));
				String QUOTE_NUMBER			=(removeNewLine(CommonUtils.isNULL(row[1]) ? row[1] : row[1].trim()));
			//	String MIGRATED_ORDER		=(removeNewLine(CommonUtils.isNULL(row[2]) ? row[2] : row[2].trim()));
				String ADDRESS_ID			=(removeNewLine(CommonUtils.isNULL(row[3]) ? row[3] : row[3].trim()));
				String PRIM_SITECODE		=(removeNewLine(CommonUtils.isNULL(row[4]) ? row[4] : row[4].trim()));
				String PRIM_SITECODE_STATUS	=(removeNewLine(CommonUtils.isNULL(row[5]) ? row[5] : row[5].trim()));
				String SEC_SITECODE			=(removeNewLine(CommonUtils.isNULL(row[6]) ? row[6] : row[6].trim()));
				String SEC_SITECODE_STATUS	=(removeNewLine(CommonUtils.isNULL(row[7]) ? row[7] : row[7].trim()));
				String PREM_OSR				=(removeNewLine(CommonUtils.isNULL(row[8]) ? row[8] : row[8].trim()));
				String SEC_OSR				=(removeNewLine(CommonUtils.isNULL(row[9]) ? row[9] : row[9].trim()));
				String ORGANIZATION_ID		=(removeNewLine(CommonUtils.isNULL(row[10]) ? row[10] : row[10].trim()));
				String EQ_SITEOF			=(removeNewLine(CommonUtils.isNULL(row[11]) ? row[11] : row[11].trim()));
				String CORE_ICO				=(removeNewLine(CommonUtils.isNULL(row[12]) ? row[12] : row[12].trim()));
				String CORE_STATUS			=(removeNewLine(CommonUtils.isNULL(row[13]) ? row[13] : row[13].trim()));
				String ORD_ICO				=(removeNewLine(CommonUtils.isNULL(row[14]) ? row[14] : row[14].trim()));
				String ORD_SITECODE			=(removeNewLine(CommonUtils.isNULL(row[15]) ? row[15] : row[15].trim()));
				String ORD_OSR				=(removeNewLine(CommonUtils.isNULL(row[16]) ? row[16] : row[16].trim()));
			
		
				
				
				ArrayList<String> key = new ArrayList<String>();
					key.add(ADDRESS_ID);
					key.add(PRIM_SITECODE);
					key.add(PRIM_SITECODE_STATUS);
					key.add(SEC_SITECODE);
					key.add(SEC_SITECODE_STATUS);
					key.add(PREM_OSR);
					key.add(SEC_OSR);
					key.add(EQ_SITEOF);
					key.add(CORE_ICO);
					key.add(CORE_STATUS);
					key.add(REPETITION);
					
					fillAddressMap(key,ORGANIZATION_ID,QUOTE_NUMBER);
					fillOrderFormDataMap(key,ORD_ICO,ORD_SITECODE,ORD_OSR);
				
				
			
		}
		csvReader.close();
		
		siteFixViewComponents.getQueryResult().append("Map reader completed.. \n");
		startAnalysis();
		
		/*
		 * 
		 * If analysis failed .. prompt to user
		 */
		ArrayList<String> errors = validateAnalysis();
		if(errors.size()>=1)
		{
			
			for(String err : errors)
			{
				siteFixViewComponents.getQueryResult().append(err+"\n");
			}
		}
		/*
		 * If Analysis Pass.. move further..
		 */
		else
		{
			analysisSiteFixData();
			writeSiteFixdataFile();
			siteFixViewComponents.getQueryResult().append("Process completed successfully..\n");
			siteFixViewComponents.getQueryResult().append("Analysis report can be found on ."+resultFileLoc+"\n");
			CommonUtils.createConsoleLogFile(siteFixViewComponents);
		}
		
		
	}
	
	private ArrayList<String[]> removeDuplicate(ArrayList<String[]> list){
		/*
		 * Remove the duplicate for same sitecode
		 * some time found same sitecode is available in dedup sheet 
		 * for same GID :i.e. 
		 * 123   sitecode1    false
		 * 123   sitecode1    false
		 * 
		 *   by this method one row will be removed.
		 *  
		 * 
		 */
		
		ArrayList<String> concatList = new ArrayList<String>();
		for(String[] A : list)
		{
		  String gid = A[0];
		  String status = A[1];
		  String cont = gid+":"+status;
		  concatList.add(cont);
		}
		
		ArrayList<String[]> UPDATEDLIST = new ArrayList<String[]>();
		/*
		 * Remove Duplicate
		 */
		
	
		ArrayList<String> updatedlist = new ArrayList<String>(new LinkedHashSet<String>(concatList));
		ArrayList<String> clioneupdatedlist = new ArrayList<String>(new LinkedHashSet<String>(concatList));
		/* 
		 * Remove the row if same sitecode is available for same GID in true and false status both. 
		 * 
		 * :i.e. 
		 * 123   sitecode1    true
		 * 123   sitecode1    false
		 * 
		 * if this is the case , in this case false will be removed. 
		 * And remaining true will participate in further process. 
		 * 
		 * 
		 * */
		
		for(String UP : updatedlist)
		{
			String mn = UP;
			String GID = mn.substring(0, mn.indexOf(":"));
			String STATUS = mn.substring(mn.indexOf(":")+1);
			
			if(STATUS.trim().equalsIgnoreCase("True"))
			{
			
			String toCheck = GID+":False";
			if(clioneupdatedlist.contains(toCheck))
			{
				clioneupdatedlist.remove(toCheck);
				siteFixViewComponents.getQueryResult().append("Remove False row for the GID and sitecode .. (same sitecode found for both AM Response for same GID)"+toCheck +"\n");
				
			}
			
			toCheck = GID+":FALSE";
			if(clioneupdatedlist.contains(toCheck))
			{
				clioneupdatedlist.remove(toCheck);
				siteFixViewComponents.getQueryResult().append("Remove False row for the GID and sitecode .. (same sitecode found for both AM Response for same GID)"+toCheck +"\n");
			}
			}
			
		}
		
		for(String UP : clioneupdatedlist)
		{
			String mn = UP;
			String GID = mn.substring(0, mn.indexOf(":"));
			String STATUS = mn.substring(mn.indexOf(":")+1);
			
			String ARR[] = new String [2];
			ARR[0] = GID;
			ARR[1] = STATUS;
			UPDATEDLIST.add(ARR);
		}
		
	
		 updatedlist = null;
         return UPDATEDLIST;
	}
	private String removeNewLine(String S){
		
		if(null != S && S.length()>0)
		{
			S = S.replaceAll("[\n\r]", "");
		}
		return S;
	}
	private void startAnalysis() throws Exception{
		
		// Get the Keys from  orderFormDataMap  
		// Check the arraylist size 
		// If 0 .. nothing to do .. no sitecode .. no osr
		// if 2 .. then start 
		// if 1 and 3 .. not yet implemented .. 
	
		addressMapFiltered = new HashMap<ArrayList<String> ,Map<String,ArrayList<String>>>(addressMap);
		
		Iterator<Map.Entry<ArrayList<String>,Map<String,ArrayList<String>>>> addressMapEntries = addressMap.entrySet().iterator();
		while (addressMapEntries.hasNext()) 
		{
			Map.Entry<ArrayList<String>,Map<String,ArrayList<String>>> innerEntry = addressMapEntries.next();
			
			ArrayList<String> key = innerEntry.getKey();
			
			/*
			 *All those cases where more than 2 ORGANIZATION_ID are found should be parked.
			  COMMENTS: address id linked with more than 2 icos.
			 * 
			 * 
			 */
			
			Map<String,ArrayList<String>> innerMap =innerEntry.getValue();
			
		
		
			if(innerMap.size()>2)
			{
					String[] analysisRow = new String[14];
					analysisRow[8] = key.get(0);
					analysisRow[9] =  "SKIP_Processed";
					analysisRow[10] = "ADDRESS_ID_LINKED_WITH_MORETHAN_2_ICOS";
					analysisReportList.add(analysisRow);
					addressMapFiltered.remove(key); 
			}
			
			/*
			 * Check weather key is available in orderFormDataMap
			 * IF NO : means no ORD Form Sitecode / OSR available
			 * Status : NOT_Processed
			 * Comments : NO_ORD_SITECODE_OR_OSR_FOUND.
			 * 
			 * IF YES : Means details available of Ord Form.
			 * Consider for process
			 * 
			 * Remove unavailable keys from addressMap. 
			 * After success full completion : both map addressMap & orderFormDataMap should equal in size.
			 */
			else
			{
				
			if(!orderFormDataMap.containsKey(key))
			{
				
				String[] analysisRow = new String[14];
				analysisRow[8] = key.get(0);
				analysisRow[9] = "SKIP_Processed";
				analysisRow[10] = "NO_ORD_SITECODE_OR_OSR_FOUND";
				analysisReportList.add(analysisRow);
				addressMapFiltered.remove(key); 
				
				
			}
			else
			{
				
				
				ArrayList<ArrayList<String>> ordFormRows = orderFormDataMap.get(key);
				ArrayList<String> existingORDSiteCodes = existingORDSiteCodes(ordFormRows);
				
				for(ArrayList<String> ordFormRow : ordFormRows)
				{
					//String ord_ico = ordFormRow.get(0);
					String ord_sitecode = ordFormRow.get(1);
					String ord_osr = ordFormRow.get(2);
					
					/*
					 * GET site code for given OSR
					 * 
					 */
					if(CommonUtils.isNULL(ord_sitecode) && !CommonUtils.isNULL(ord_osr))
					{
					
						ArrayList<ArrayList<String>> siteCodesForOSR = getSiteCodeForOSR(ord_osr);
						
						if(siteCodesForOSR.size() == 0)
						{
							/*
							 * If no row found against OSR.
							 * OSR is wrong in sheet
							 * 1. Alert to User
							 * 2. Remove Key as useless.
							 */
							String[] analysisRow = new String[14];
							analysisRow[8] = key.get(0);
							analysisRow[9] = "FAILED";
							analysisRow[10] = "NO_DATA_FOUND_FOR_OSR("+ord_osr+")";
							
							analysisReportList.add(analysisRow);
							addressMapFiltered.remove(key);
						}
						/*
						 * If data found against OSR
						 */
						else
						{
							
						
							siteFixViewComponents.getQueryResult().append("Get site codes for "+ord_osr +"\n");
							ArrayList<ArrayList<String>> filterSiteCodesForOSR = new ArrayList<ArrayList<String>>(siteCodesForOSR);
							
							/*
							 *  Remove sitecode which are already taken as ORD sitecode and its match to primary sitecode.
							*/
							
							for(ArrayList<String> siteCodeForOSR : siteCodesForOSR)
							{
								String sitecode = siteCodeForOSR.get(0);
								String sitecodestatus = siteCodeForOSR.get(1);
								
								
							
								if(existingORDSiteCodes.contains(sitecode))
								{
									siteFixViewComponents.getQueryResult().append("Found sitecode taken as ord sitecode "+sitecode +" and prim sitecode is "+key.get(1) +"\n");
									if(sitecode.equals(key.get(1)))
									{
										ArrayList<String> removeRow = new ArrayList<String>();
										removeRow.add(sitecode);
										removeRow.add(sitecodestatus);
										filterSiteCodesForOSR.remove(removeRow);
										/*
										 * 
										 * After remove check the length of filterSiteCodesForOSR if it is 0 .. 
										 * Then check the sec site code from the sheet and 
										 * if it is one .. use that site code 
										 * if it is more then one .. Failure
										 */
										
										if(filterSiteCodesForOSR.size()==0)
										{
											String secSitecode = key.get(3);
											String[] sitecodes = secSitecode.split(";");
											
											if(sitecodes.length == 1 && secSitecode.trim().length()>0)
											{
												ordFormRow.add(1,secSitecode);
												siteFixViewComponents.getQueryResult().append("SEC site code set as sitecode..."+secSitecode+"\n");
											}
											else if(sitecodes.length > 1)
											{
												String[] analysisRow = new String[14];
												analysisRow[8] = key.get(0);
												analysisRow[9] = "FAILED";
												analysisRow[10] = "MULTIPLE_SEC_SITECODE_FOR_OSR("+ord_osr+")";
												analysisReportList.add(analysisRow);
												addressMapFiltered.remove(key); 
												siteFixViewComponents.getQueryResult().append("Failed multiple SEC site code found .."+secSitecode+"\n");
											}
											else if(secSitecode.trim().length() ==0)
											{
												String[] analysisRow = new String[14];
												analysisRow[8] = key.get(0);
												analysisRow[9] = "FAILED";
												analysisRow[10] = "OSR_SITECODE_AS_PRIM_FOR_ADDRESSID_("+key.get(0)+")AND_NO_SEC_SITECODE_FOR_OSR("+ord_osr+")";
												analysisReportList.add(analysisRow);
												addressMapFiltered.remove(key); 
												siteFixViewComponents.getQueryResult().append("Failed No SEC site code found ..for OSR "+ord_osr+"\n");
											}
										}
										siteFixViewComponents.getQueryResult().append("Remove sitecode as match with primary sitecode (ord sitecode = primsitecode).."+sitecode+"\n");
									}
								}
								
							}
								
								 /* 
								  * no ord sitecode mention in the File 
								  * or 
								 * if mention in the file but not As primary sitecode.
								 * 
								 * In this case match if sitecodes has status 0  give them priority.
								 * 
								 */
								
								int howmanyzerostatus = 0;
								
								String sitecode="";
								if(filterSiteCodesForOSR.size()>0)
								{
									
									for(ArrayList<String> siteCodeForOSR : filterSiteCodesForOSR)
									{
										sitecode = siteCodeForOSR.get(0);
										String sitecodestatus = siteCodeForOSR.get(1);
										
										
										
										/*
										 * check : if zero status found more then 1 .. 
										 * Park this case as this required manual observation
										 * 
										 */
										if(sitecodestatus.equals("0"))
										{
											howmanyzerostatus++;
										}
										
									}
								
									/*
									 * Only one active status found.
									 * Give them high priority
									 */
									
									if(howmanyzerostatus == 1)
									{
										ordFormRow.add(1, sitecode);
										siteFixViewComponents.getQueryResult().append("Fill Single active status sitecode.."+sitecode +"\n");
										
									}
									/*
									 * More then one active status found.
									 * Failed this case , Manual Investigation required
									 */
									else if(howmanyzerostatus>1)
									{
										String[] analysisRow = new String[14];
										analysisRow[8] = key.get(0);
										analysisRow[9] = "FAILED";
										analysisRow[10] = "MORE_ACTIVE_STATUS_FOR_OSR("+ord_osr+")";
										analysisReportList.add(analysisRow);
										addressMapFiltered.remove(key);
									}
									/*
									 * No active status found. 
									 * Match with Secondary sitecode
									 */
									else if(howmanyzerostatus == 0)
									{
									
										String sec_sitecodes = key.get(3);
										String[] sitecodes = sec_sitecodes.split(";");
										int howmanymatchswithsecondarysitecode=0;
										String matchwithsecondrysitecode ="";
										for(ArrayList<String> siteCodeForOSR : filterSiteCodesForOSR)
										{
											sitecode = siteCodeForOSR.get(0);
											//String sitecodestatus = siteCodeForOSR.get(1);
											for(String scode : sitecodes)
											{
												if(scode.equals(sitecode))
												{
													howmanymatchswithsecondarysitecode++;
													matchwithsecondrysitecode = sitecode;
												}
											}
										}
										/*
										 * If only one sitecode matches with secondary site codes .. 
										 * take priority
										 * 
										 * if more then one matches 
										 * Failed this case , Manual Investigation required
										 * 
										 * if no matches 
										 * Failed this case , Manual Investigation required
										 */
										if(howmanymatchswithsecondarysitecode ==1)
										{
											ordFormRow.add(1, matchwithsecondrysitecode);
											siteFixViewComponents.getQueryResult().append("Fill Single secondry sitecode .."+matchwithsecondrysitecode+"\n");
										}
										
										else if(howmanymatchswithsecondarysitecode>1)
										{
											String[] analysisRow = new String[14];
											analysisRow[8] = key.get(0);
											analysisRow[9] = "FAILED";
											analysisRow[10] = "MORE_SEC_SITECODE_FOR_OSR("+ord_osr+")";
											analysisReportList.add(analysisRow);
											addressMapFiltered.remove(key);
										}
										
										else
										{
											String[] analysisRow = new String[14];
											analysisRow[8] = key.get(0);
											analysisRow[9] = "FAILED";
											analysisRow[10] = "NO_SEC_SITECODE_MATCHES_FOR_OSR("+ord_osr+")";
											analysisReportList.add(analysisRow);
											addressMapFiltered.remove(key); 
										}
								}
						}
						}
					}
				}
			}
		}
		}
	}
	
	private ArrayList<String> validateAnalysis(){
		
		 /* Now all OSRs has been identified to sitecode and all unmatched has been removed. 
		 *  Now orderFormDataMap has all the values of sitecodes against provided OSR... 
		 *  To check nothing will be empty.. 
		 * 
		 *  all keys addressMapFiltered  will be available in 
		 *  orderFormDataMap and all Values must have sitecode against provided OSR
		 * 
		 */
		Iterator<Map.Entry<ArrayList<String>,Map<String,ArrayList<String>>>> addressMapFilteredReplica = addressMapFiltered.entrySet().iterator();
		boolean isAnalysisPass = true;
		while (addressMapFilteredReplica.hasNext()) 
		{
			Map.Entry<ArrayList<String>,Map<String,ArrayList<String>>> innerEntry = addressMapFilteredReplica.next();
			ArrayList<String> key = innerEntry.getKey();
			ArrayList<ArrayList<String>> values = orderFormDataMap.get(key);
			for(ArrayList<String> value : values)
			{
				String sitecode = value.get(1);
				if(CommonUtils.isNULL(sitecode))
				{
					isAnalysisPass = false;
					errors.add("Analysis Failed ..REPETITION TYPE ("+key.get(10)+") for addressid "+key.get(0) +" OSR " +value.get(3));
				}
			}
		}
		if(isAnalysisPass){
			siteFixViewComponents.getQueryResult().append("Analysis completed successfully..\n");
		}
		else{
			errors.add("Analysis Failed ..");
		}
		return errors;
	}

	



	private void writeSiteFixdataFile() throws Exception{
		String columnName[] = { "REPLACE_SITECODE", "REPLACE_STATUS", "REPLACEMENT_SITECODE", "REPLACEMENT_STATUS","REPLACE_OSR","REPLACEMENT_OSR","MOVE_ORDERS","LEGACY_SITECODE","ADDRESS_ID","ANALYSIS_STATUS","COMMENTS","LEGACY_SITECODE_ICO","LEG_SC_CORE_ADDRESSID","LEG_SC_CORE_COREID"};
		CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(resultFileLoc),true);
		writer.writeNext(columnName);
		if(!siteFixViewComponents.getCheckDEDUP().isSelected())
		{
			for(String[] row : analysisReportList)
			{
				//String legacySitecode = row[7];
			    row[11] = getICOfromSiteCode(CommonUtils.isNULL(row[7])? "" : row[7]);
				writer.writeNext(row);
			}
		}
		
		else if(siteFixViewComponents.getCheckDEDUP().isSelected())
			{
				/*
				 * Before finalize replacement sitecode ,
				 * we should check in previous dedup 
				 * If Not Found in Previous DEDUP 			 : >> Replacement sitecode remain unchanged
				 * If Found in Previous DEDUP as Latest TRUE : >> Replacement sitecode remain unchanged
				 * If Found in Previous DEDUP as Latest FALSE: >> Corresponding true sitecode of dedup will replace the replacement sitecode. And will act as replacement sitecode.
				 * 
				 * 
				 */
			Map<String,ArrayList<String[]>> dedupMap = getConsolidatedDedupMap();
			siteFixViewComponents.getQueryResult().append("Previous DEDUP consilidation completed successfully  ..total sitecodes found >>"+dedupMap.size() +"\n");	
			for(String[] row : analysisReportList)
			{
				String suggestedReplacementSitecode = row[2];
				//String legacySitecode = row[7];
				String comments = row[10];
				
				if(null != suggestedReplacementSitecode && !suggestedReplacementSitecode.isEmpty())
				{	
					// Sitecode not found in previous dedup
						if(!dedupMap.containsKey(suggestedReplacementSitecode))
						{
							row[10] = comments+"(No Apperence found in previous DEDUPs)";
							row[11] = getICOfromSiteCode(CommonUtils.isNULL(row[7])? "" : row[7]);
							writer.writeNext(row);
						}
						
						
						else if(dedupMap.containsKey(suggestedReplacementSitecode))
						{
							
							getDedupReplacementSiteCode(suggestedReplacementSitecode,comments,row,dedupMap,writer);
							
						}
				}	
				else
				{
					row[11] = getICOfromSiteCode(CommonUtils.isNULL(row[7])? "" : row[7]);
					writer.writeNext(row);	
				}
			}
			
			}
			
		
		writer.close();
	}
	
}
