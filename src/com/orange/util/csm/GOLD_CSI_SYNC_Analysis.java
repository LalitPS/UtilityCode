package com.orange.util.csm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.ui.component.SiteFixedViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.ConnectionBeanCSI;
import com.orange.util.CustomCSVWriter;
import com.orange.util.ProgressMonitorPane;

public class GOLD_CSI_SYNC_Analysis {


	private String columns[] = {"CSI_SITEHANDLE","CSI_ADDRESSID","CSI_CORESITEID","CSI_CUSTHANDLE","CSI_ORDHANDLE","CSI_SERVICEHANDLE","CSI_USID","CSI_SERVICEELEMENTCLASS","GOLD_ADDRESS_ID","GOLD_CORE_SITE_ID","GOLD_ORGANIZATIONID","GOLD_SITECODE","GOLD_ORANGE_SITENAME"};
	
	private ArrayList<String> csiSQLs;
	private String csvReaderPath;
	private String csvReaderUpdatedPath;
	private SiteFixedViewComponents siteFixViewComponents;
	private String sqlWriterPath;
	private ArrayList<String[]> usidwritableData;
	private String usidWriterPath;
	
	private ArrayList<String[]> writableData;
	
	
	public GOLD_CSI_SYNC_Analysis(SiteFixedViewComponents siteFixViewComponents,String readerFilePath) throws Exception
	{
		int index = readerFilePath.lastIndexOf(".");
		String sub = readerFilePath.substring(0,index) ;
		
		sqlWriterPath=sub+"_CSI.sql";
		csvReaderUpdatedPath = sub+"_Updated.csv";
		usidWriterPath = sub+"_USID_DETAILS.csv";
	
		this.csvReaderPath = readerFilePath;
		this.siteFixViewComponents = siteFixViewComponents;
		
		writableData = new ArrayList<String[]>();
		usidwritableData = new ArrayList<String[]>();
		usidwritableData.add(columns);
		csiSQLs = new ArrayList<String>();
		
					
		siteFixViewComponents.getQueryResult().append("Process Start .. Please wait ..\n");
		siteFixViewComponents.getQueryResult().append("Information creation process started ....\n");
		initCSVReader(this.csvReaderPath);
		initCSVWriter(this.csvReaderUpdatedPath);
		siteFixViewComponents.getQueryResult().append("Information Updated ...."+csvReaderUpdatedPath+"\n");
		
		siteFixViewComponents.getQueryResult().append("SQLs process started  .. ..\n");
		createCSIScriptFile(this.sqlWriterPath);
		siteFixViewComponents.getQueryResult().append("SQLs created  .. .."+sqlWriterPath+"\n");
		
		siteFixViewComponents.getQueryResult().append("USID Information  process started  .. ..\n");
		initUSIDCSVWriter(usidWriterPath);
		siteFixViewComponents.getQueryResult().append("USID Information  created  .. .."+usidWriterPath+"\n");
		
		siteFixViewComponents.getQueryResult().append("All Process Completed ..  ..\n");
	}
	
	private void createCSIScriptFile(String filePath) throws Exception{
		File fout = new File(filePath);
		FileOutputStream fos = new FileOutputStream(fout);
		csiSQLs.add("commit;");
	
		csiSQLs = CommonUtils.setUmlaut(csiSQLs);
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		for(String script : csiSQLs){
			bw.write(script);
			bw.newLine();
		}
		bw.close();
		fos.close();
		csiSQLs = null;
		
	}
	
	
	private String getBiggestOrderNumber(String usid) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
		
		String localQueryFromSite = "select DISTINCT T.QUOTENUMBER from "+ConnectionBean.getDbPrefix()+"SC_QUOTE_LINE_ITEM A, "+ConnectionBean.getDbPrefix()+"SC_QUOTE T where (NEW_CONFIG = ? OR EXIST_CONFIG= ? OR Value = ?) AND T.TRIL_GID = A.QUOTE order by T.QUOTENUMBER ASC"; 
	
		PreparedStatement pstmt = ConnectionForGOLD.getPreparedStatement(localQueryFromSite);
		pstmt.setString(1, usid);
		pstmt.setString(2, usid);
		pstmt.setString(3, usid);
		String upquery = localQueryFromSite.replace("?", "'"+usid +"'");
		
		siteFixViewComponents.getQueryResult().append(upquery+"\n");
		
		ResultSet resultSet = pstmt.executeQuery();
		
		String quotenumber = "";
		while(resultSet.next())
		{
			quotenumber= resultSet.getString(1);
		}
		usid = null;
		resultSet.close();
		pstmt.close();
		
		return quotenumber;
	}

	
	
	
	
	private int getCaseCategory(String []row){
		int caseCategory = -1;
		String CSI_ADDRESS_ID					= row[1];	
		String CSI_CORE_SITE_ID					= row[2];
		String CSI_ORDER_HANDLE					= row[4];
		String GOLD_ADDRESS_ID					= row[9];
		String GOLD_CORE_SITE_ID				= row[10];
		String GOLD_STATUS						= row[11];
		String IS_AVAILABLE_AFTER_ADD_WHITESPACE = row[14];
	
		// Legacy in CSI
		if(CommonUtils.isNULL(CSI_ADDRESS_ID) && CommonUtils.isNULL(CSI_CORE_SITE_ID))
		{
			// Found in GOLD
			if(IS_AVAILABLE_AFTER_ADD_WHITESPACE.equalsIgnoreCase("CSI_SITEHANDLE_FOUND_IN_GOLD"))
			{
				
				// Found Core IDS in GOLD
				if(!CommonUtils.isNULL(GOLD_ADDRESS_ID) && !CommonUtils.isNULL(GOLD_CORE_SITE_ID))
				{
					
					// If Status is 0 OR 1
					if(GOLD_STATUS.equalsIgnoreCase("0") || GOLD_STATUS.equalsIgnoreCase("1"))
					{
						return 1;
					}
					
					// if status is 2 or 3
					if(GOLD_STATUS.equalsIgnoreCase("2") || GOLD_STATUS.equalsIgnoreCase("3"))
					{
						if(!CommonUtils.isNULL(CSI_ORDER_HANDLE))
						{
							return 2;
						}
						if(CommonUtils.isNULL(CSI_ORDER_HANDLE))
						{
							return 3;
						}
						
					}
				}
				
				
			}
			// Found After Trim
			if(IS_AVAILABLE_AFTER_ADD_WHITESPACE.equalsIgnoreCase("CSI_SITEHANDLE_FOUND_IN_GOLD_AFTER_TRIM"))
			{
				// If Status is 0 OR 1
				if(GOLD_STATUS.equalsIgnoreCase("0") || GOLD_STATUS.equalsIgnoreCase("1"))
				{
					return 4;
				}
				
				// if status is 2 or 3
				if(GOLD_STATUS.equalsIgnoreCase("2") || GOLD_STATUS.equalsIgnoreCase("3"))
				{
					if(!CommonUtils.isNULL(CSI_ORDER_HANDLE))
					{
						return 5;
					}
					if(CommonUtils.isNULL(CSI_ORDER_HANDLE))
					{
						return 6;
					}
					
				}
			}
		
			// Legacy not found in GOLD
			if(IS_AVAILABLE_AFTER_ADD_WHITESPACE.equalsIgnoreCase("CSI_SITEHANDLE_NOT_FOUND_IN_GOLD"))
			{
				
				// Has ORD Handle
				if(!CommonUtils.isNULL(CSI_ORDER_HANDLE))
				{
					return 7;
				}
				// No Ord Handle
				if(CommonUtils.isNULL(CSI_ORDER_HANDLE))
				{
					return 8;
				}
			}
			
		}
		// Active Site in CSI
		if(!CommonUtils.isNULL(CSI_ADDRESS_ID) && !CommonUtils.isNULL(CSI_CORE_SITE_ID))
		{
			// Active Sites not found in GOLD
			if(IS_AVAILABLE_AFTER_ADD_WHITESPACE.equalsIgnoreCase("CSI_SITEHANDLE_NOT_FOUND_IN_GOLD"))
			{
				// Has ORD Handle
				if(!CommonUtils.isNULL(CSI_ORDER_HANDLE))
				{
					return 9;
				}
				// No Ord Handle
				if(CommonUtils.isNULL(CSI_ORDER_HANDLE))
				{
					return 10;
				}
			}
		}
	
	return caseCategory;
	}
	
	private ArrayList<String[]> getCoreSiteDetailsFromSiteCode(String sitecode) throws Exception{
		
	    String localQuery = "SELECT SITE.ADDRESS_ID,SITE.CORE_SITE_ID,ORG.ORGANIZATIONID,SITE.SITECODE,SITE.ORANGE_SITENAME FROM "+ConnectionBean.getDbPrefix()+"EQ_SITE SITE , "+ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG WHERE SITECODE=? AND ORG.TRIL_GID = SITE.EQ_SITEOF";
		
		PreparedStatement pstmt = ConnectionForGOLD.getPreparedStatement(localQuery);
		pstmt.setString(1, sitecode);
		String upquery = localQuery.replace("?", "'"+sitecode +"'");
		
		siteFixViewComponents.getQueryResult().append(upquery+"\n");
		
		ResultSet resultSet = pstmt.executeQuery();
		
		ArrayList<String[]> coreIds = new ArrayList<String[]>();
		
		while(resultSet.next())
		{
			String arr[] = new String[5];
			arr[0] = resultSet.getString(1);
			arr[1] = resultSet.getString(2);
			arr[2] = resultSet.getString(3);
			arr[3] = resultSet.getString(4);
			arr[4] = resultSet.getString(5);
			
			coreIds.add(arr);
			
		}
		resultSet.close();
		pstmt.close();
		upquery = null;
		return coreIds;
	}
	
private ArrayList<String[]> getSEIdFromOrdHandle(String ORDHANDLE) throws Exception{
		
		String localQuery = "SELECT SERVICEELEMENTID,USID FROM "+ConnectionBeanCSI.getDbPrefix()+"CSERVICEELEMENT WHERE SERVICEELEMENTID IN (SELECT SERVICEELEMENTID FROM "+ConnectionBeanCSI.getDbPrefix()+"CVERSIONSERVICEELEMENT  WHERE VERSIONID IN (SELECT VERSIONID FROM "+ConnectionBeanCSI.getDbPrefix()+"CVERSION WHERE" +
		" ORDHANDLE=?))";
		PreparedStatement pstmt = ConnectionForCSI.getPreparedStatement(localQuery);
		pstmt.setString(1, ORDHANDLE);
		String upquery = localQuery.replace("?", "'"+ORDHANDLE +"'");
		
		siteFixViewComponents.getQueryResult().append(upquery+"\n");
		
		ResultSet resultSet = pstmt.executeQuery();
		
		ArrayList<String[]> serviceElements = new ArrayList<String[]>();
		
		while(resultSet.next())
		{
			String arr[] = new String[2];
			arr[0] = resultSet.getString(1);
			arr[1] = resultSet.getString(2);
			serviceElements.add(arr); 
			arr = null;
		
		}
		resultSet.close();
		pstmt.close();
		upquery = null;
		localQuery = null;
		
		return serviceElements;
	}

	private ArrayList<String[]> getSEIdFromSiteCode(String sitecode) throws Exception{
		
		String localQuery = "SELECT SERVICEELEMENTID,USID FROM "+ConnectionBeanCSI.getDbPrefix()+"CSERVICEELEMENT WHERE SERVICEELEMENTID IN (SELECT SERVICEELEMENTID FROM "+ConnectionBeanCSI.getDbPrefix()+"CVERSIONSERVICEELEMENT  WHERE VERSIONID IN (SELECT VERSIONID FROM "+ConnectionBeanCSI.getDbPrefix()+"CVERSION WHERE" +
		" SITEHANDLE=?))";
		PreparedStatement pstmt = ConnectionForCSI.getPreparedStatement(localQuery);
		pstmt.setString(1, sitecode);
		String upquery = localQuery.replace("?", "'"+sitecode +"'");
		
		siteFixViewComponents.getQueryResult().append(upquery+"\n");
		
		ResultSet resultSet = pstmt.executeQuery();
		
		ArrayList<String[]> serviceElements = new ArrayList<String[]>();
		
		while(resultSet.next())
		{
			String arr[] = new String[2];
			arr[0] = resultSet.getString(1);
			arr[1] = resultSet.getString(2);
			serviceElements.add(arr); 
			arr = null;
		
		}
		resultSet.close();
		pstmt.close();
		upquery = null;
		localQuery = null;
	
		return serviceElements;
	}
	
	
	
	
		


	private void getUSIDData(String[] row) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
		ArrayList<ArrayList<String>> usidsInfo= getUSIDDataForSiteHandle(row);
		
		for(ArrayList<String> usid : usidsInfo)
		{
			String arr[] = new String[13];
			
			String SITEHANDLE =usid.get(0);
			String ADDRESSID =usid.get(1);
			String CORESITEID =usid.get(2);
			String CUSTHANDLE=usid.get(3);
			String ORDHANDLE=usid.get(4);
			String SERVICEHANDLE=usid.get(5);
			String USID =usid.get(6);
			String SERVICEELEMENTCLASS =usid.get(7);
			
			USID = CommonUtils.filterUSID(USID);
			
			
			String quotenumber = getBiggestOrderNumber(USID);
			
			if(null ==quotenumber || quotenumber.isEmpty())
			{
				USID = USID.trim();
				quotenumber = getBiggestOrderNumber(USID);
			}
		
			String ADDRESS_ID="";
			String CORE_SITE_ID=""; 
			String ORGANIZATIONID="";
			String SITECODE="";
			String ORANGE_SITENAME="";
			
			if(null !=quotenumber && !quotenumber.isEmpty())
			{
				ArrayList<String[]> goldcoredetails = CommonUtils.getCoreSiteDetailsFromOrder(quotenumber,siteFixViewComponents);
		
				if(!goldcoredetails.isEmpty())
				{
				ADDRESS_ID  = goldcoredetails.get(0)[0];
				CORE_SITE_ID = goldcoredetails.get(0)[1];
				ORGANIZATIONID= goldcoredetails.get(0)[2];
				SITECODE= goldcoredetails.get(0)[3];
				ORANGE_SITENAME= goldcoredetails.get(0)[4];
				}
			}
			
			arr[0] = SITEHANDLE;
			arr[1] = ADDRESSID;
			arr[2] = CORESITEID;
			arr[3] = CUSTHANDLE;
			arr[4] = ORDHANDLE;
			arr[5] = SERVICEHANDLE;
			arr[6] = USID;
			arr[7] = SERVICEELEMENTCLASS;
			
			
			arr[8] = ADDRESS_ID;
			arr[9] = CORE_SITE_ID;
			arr[10] = ORGANIZATIONID;
			arr[11] = SITECODE;
			arr[12] = ORANGE_SITENAME;
	
			usidwritableData.add(arr);
			arr = null;
			usid = null;
			SITEHANDLE = null;
			ADDRESSID= null;
			CORESITEID= null;
			CUSTHANDLE= null;
			ORDHANDLE= null;
			SERVICEHANDLE= null;
			USID= null;
			SERVICEELEMENTCLASS= null;
			ADDRESS_ID= null;
			CORE_SITE_ID= null;
			ORGANIZATIONID= null;
			SITECODE= null;
			ORANGE_SITENAME = null;
		}
		usidsInfo = null;
	}
	private ArrayList<ArrayList<String>> getUSIDDataForSiteHandle(String[] row) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		
		String localQueryFromSite = "SELECT DISTINCT " +
				"V.SITEHANDLE, " +
				"V.ADDRESSID , " +
				"V.CORESITEID ," +
				"V.CUSTHANDLE," +
				"V.ORDHANDLE," +
				"V.SERVICEHANDLE," +
				"ele.USID ," +
				"ele.SERVICEELEMENTCLASS " +
				"FROM "+ConnectionBeanCSI.getDbPrefix()+"CSERVICEELEMENT ele, "+ConnectionBeanCSI.getDbPrefix()+"CVERSIONSERVICEELEMENT vele, "+ConnectionBeanCSI.getDbPrefix()+"CVERSION v WHERE vele.VERSIONID = v.VERSIONID and ele.SERVICEELEMENTID = vele.SERVICEELEMENTID and v.sitehandle=? and ele.serviceelementclass in('CPE', 'NASBackup', 'AccessConnection','BackupOptions')";
		
		
		PreparedStatement pstmt = ConnectionForCSI.getPreparedStatement(localQueryFromSite);
		pstmt.setString(1, row[0]);
		String upquery = localQueryFromSite.replace("?", "'"+row[0] +"'");
		
		siteFixViewComponents.getQueryResult().append(upquery+"\n");
		
		ResultSet resultSet = pstmt.executeQuery();
		
		ArrayList<ArrayList<String>> usids = new ArrayList<ArrayList<String>>();
	
		while(resultSet.next())
		{
			ArrayList<String> usid = new ArrayList<String>(); 
			usid.add(resultSet.getString(1));
			usid.add(resultSet.getString(2));
			usid.add(resultSet.getString(3));
			usid.add(resultSet.getString(4));
			usid.add(resultSet.getString(5));
			usid.add(resultSet.getString(6));
			usid.add(resultSet.getString(7));
			usid.add(resultSet.getString(8));
			
			usids.add(usid);
			usid = null;
		}
		
		resultSet.close();
		pstmt.close();
		upquery = null;
		return usids;
		
	}
	
	
	

	

	private void initCSVReader(String filePath) throws Exception
	{
		CSVReader csvReader = new CSVReader(new FileReader(filePath));
		double csiDataCount = 0; 
		
		String []row = csvReader.readNext();
		while ((row = csvReader.readNext()) != null) 
		{
			csiDataCount++;
		}
		csvReader.close();
		row = null; 
		
		csvReader = new CSVReader(new FileReader(filePath));
		row = csvReader.readNext();
	
		updateCSVRowStatus(row,"CASE","Status");
		
		double count = 0.0; 
		
		while ((row = csvReader.readNext()) != null) 
		{
			count++;	
			ProgressMonitorPane.getInstance().setProgress(count,csiDataCount);	
			
			int caseCategory = getCaseCategory(row);
			
			switch(caseCategory)
			{
			
			case 1:
				/* CASE : Legacy Site > Found in GOLD > Found Core IDS in GOLD >If Status is 0 OR 1 **/
				/* Action : Update CSI Core Ids from GOLD core Ids */
				/*
				
				1. GET CORE IDS FROM SHEET
				2. UPDATE COREIDS IN CVERSION TABLE FOR SITEHANDLE.
				3. Update USIDs 
				#Update cversion set Lupddate=sysdate,coresiteid='',addressid='' where sitehandle='HOD';
				
				*/
				csiSQLs.add("--CASE : "+ caseCategory +" [Legacy Site > Found in GOLD > Found Core IDS in GOLD >If Status is 0 OR 1] { For Sitecode " + row[8] +"}");
				updateCoreSitesBySiteHandle(row,""+caseCategory);
				csiSQLs.add("--CASE : Ends "+caseCategory );
				
				break;
			case 2:
				/* CASE : Legacy Site > Found in GOLD >	Found Core IDS in GOLD > If Status is 2 OR 3 >CSI_ORDER_HANDLE  not null **/
				/* ACTION : Get the Site details from GOLD order and Update in CSI*/
				
				/*
				 *  GET SITEDETAILS FOR ORDER IN GOLD [DISCUSS WITH MAYANK HOW TO FOUND CHANGE ORDER SITE DETAILS]
				 *  
				 *  #select sitecode,address_id , site.CORE_SITE_ID from eq_site site, sc_quote quote
					where quote.quotenumber = '032730' And site.tril_gid = quote.SITE or QUOTE.HOTCUTNEWSITE;
				 * 
				 * UPDATE SITECODE AND COREIDS
				 * #Update cversion set Lupddate=sysdate,coresiteid='<GROM GOLD>',addressid='<FROM GOLD>', SITEHANDLE='<GOLD SITECODE>' where sitehandle='HOD';
				 * 
				 * GET THE IDS OF ORDER
				 * #select Serviceelementid,USID from cserviceelement where serviceelementid in 
				 * (select serviceelementid from CVERSIONSERVICEELEMENT  where versionid in 
				 * (select versionid from cversion where ordhandle='035048'));
				 * 
				 * UPDATE ALL USID FOR LIST OF ALL SERVICE ELEMENT IDS LINKED WITH ORDER  
				 * #Update Cserviceelement Set Usid='"+ autoUpdatedUSID+ "',Lupddate=Sysdate Where Serviceelementid='"+ serviceElementID + "';";
				 */
				
				csiSQLs.add("--CASE : "+ caseCategory +" [Legacy Site > Found in GOLD >	Found Core IDS in GOLD > If Status is 2 OR 3 >CSI_ORDER_HANDLE  not null]{ For ORDER " + row[4] +"}");
				updateCoreSitesByORDHandle(row,""+caseCategory);
				csiSQLs.add("--CASE : Ends "+caseCategory );
				
				break;
			case 3:
				/* CASE : Legacy Site >	Found in GOLD >	Found Core IDS in GOLD > If Status is 2 OR 3 > CSI_ORDER_HANDLE  is  null **/
				
				/* ACTION : Get The USID details (CPE, NASBackup, AccessConnection,BackupOptions ), Order number , core ids , sitecode , OSR , status from GOLD and 
				Send to CORE Team*/
				
               
				/* 
				  SELECT DISTINCT V.SITEHANDLE, V.ADDRESSID , V.CORESITEID ,V.CUSTHANDLE,V.ORDHANDLE,V.SERVICEHANDLE,ele.USID ,ele.SERVICEELEMENTCLASS FROM CSERVICEELEMENT ele, CVERSIONSERVICEELEMENT vele,
         		  CVERSION v WHERE vele.VERSIONID = v.VERSIONID and ele.SERVICEELEMENTID = vele.SERVICEELEMENTID and 
				  v.sitehandle='CIS0032365' and ele.serviceelementclass in('CPE', 'NASBackup', 'AccessConnection','BackupOptions');
				*/
				
				// get the biggest quote number
				
				/*
				 * 	select DISTINCT T.QUOTENUMBER from SC_QUOTE_LINE_ITEM A, SC_QUOTE T
					where (NEW_CONFIG = 'GO01WWTH88' OR EXIST_CONFIG= 'GO01WWTH88' OR Value = 'GO01WWTH88')
					AND T.TRIL_GID = A.QUOTE order by T.QUOTENUMBER ASC;
				 * 
				 */
				getUSIDData(row);
				updateCSVRowStatus(row,""+caseCategory,"USID_INFO");
				
				break;
			case 4:
				/* CASE : Legacy Site >	Found in GOLD after TRIM > If Status is 0 OR 1 **/
				/* ACTION : Update SITEHANDLE AND Core Ids from GOLD core Ids*/
				
				/*
				GET CORE IDS FROM SHEET
				UPDATE COREIDS AND SITEHANDLE IN CVERSION TABLE FOR SITEHANDLE.
				#Update cversion set Lupddate=sysdate,coresiteid='',addressid='' , SITEHANDLE ='<WITHOUTTRIM>' where sitehandle='HOD ';
				
				
				#select Serviceelementid,USID from cserviceelement where serviceelementid in 
				(select serviceelementid from CVERSIONSERVICEELEMENT  where versionid in 
				(select versionid from cversion where SITEHANDLE='HOD '));
				  
				UPDATE ALL USID FOR LIST OF ALL SERVICE ELEMENT IDS LINKED WITH SITEHANDLE  
			    #Update Cserviceelement Set Usid='"+ autoUpdatedUSID+ "',Lupddate=Sysdate Where Serviceelementid='"+ serviceElementID + "';";
				  
				 */
				
				csiSQLs.add("--CASE : "+ caseCategory +" [Legacy Site >	Found in GOLD after TRIM > If Status is 0 OR 1]{ For Sitecode " + row[8] +"}");
				updateCoreSitesBySiteHandle(row,""+caseCategory);
				csiSQLs.add("--CASE : Ends "+caseCategory );
				break;
			case 5:
				/* CASE : Legacy Site >	Found in GOLD after TRIM > If Status is 2 OR 3 > CSI_ORDER_HANDLE  not null **/
				/* ACTION : Get the Site details from GOLD order and Update in CSI*/
				
				
				/*
				 *  GET SITEDETAILS FOR ORDER IN GOLD [DISCUSS WITH MAYANK HOW TO FOUND CHANGE ORDER SITE DETAILS]
				 *  
				 *  #select sitecode,address_id , site.CORE_SITE_ID from eq_site site, sc_quote quote
					where quote.quotenumber = '032730' And site.tril_gid = quote.SITE or QUOTE.HOTCUTNEWSITE;
				 * 
				 * UPDATE SITECODE AND COREIDS
				 * #Update cversion set Lupddate=sysdate,coresiteid='<GROM GOLD>',addressid='<FROM GOLD>', SITEHANDLE='<GOLD SITECODE>' where sitehandle='HOD ';
				 * 
				 * GET THE IDS OF ORDER
				 * #select Serviceelementid,USID from cserviceelement where serviceelementid in 
				 * (select serviceelementid from CVERSIONSERVICEELEMENT  where versionid in 
				 * (select versionid from cversion where SITEHANDLE='HOD  '));
				 * 
				 * UPDATE ALL USID FOR LIST OF ALL SERVICE ELEMENT IDS LINKED WITH ORDER  
				 * #Update Cserviceelement Set Usid='"+ autoUpdatedUSID+ "',Lupddate=Sysdate Where Serviceelementid='"+ serviceElementID + "';";
				 */
				
				csiSQLs.add("--CASE : "+ caseCategory +" [Legacy Site >	Found in GOLD after TRIM > If Status is 2 OR 3 > CSI_ORDER_HANDLE  not null]{ For ORDER " + row[4] +"}");
				updateCoreSitesByORDHandle(row,""+caseCategory);
				csiSQLs.add("--CASE : Ends "+caseCategory );
				
				break;
			case 6:
				/* CASE : Legacy Site >	Found in GOLD after TRIM >	If Status is 2 OR 3 > CSI_ORDER_HANDLE  is null **/
				/* ACTION : Get The USID details (CPE, NAs Backup , Access Connection , Backup Options ), Order number , core ids , sitecode , OSR , status from GOLD and 
				Send to CORE Team*/
				
				/* 
				 #SELECT DISTINCT ele.USID ,ele.SERVICEELEMENTCLASS FROM CSERVICEELEMENT ele, CVERSIONSERVICEELEMENT vele,CVERSION v 
				 WHERE vele.VERSIONID = v.VERSIONID and 
				 ele.SERVICEELEMENTID = vele.SERVICEELEMENTID and 
				 v.sitehandle='CIS0032365';
				*/
				
				getUSIDData(row);
				updateCSVRowStatus(row,""+caseCategory,"USID_INFO");
				
				break;
			case 7:
				/* CASE : Legacy Site > Legacy not found in GOLD > ORD Handle not Null **/
				/* ACTION : Get the Site details from GOLD order and Update in CSI*/
				
				
				/*
				 *  GET SITEDETAILS FOR ORDER IN GOLD [DISCUSS WITH MAYANK HOW TO FOUND CHANGE ORDER SITE DETAILS]
				 *  
				 *  #select sitecode,address_id , site.CORE_SITE_ID from eq_site site, sc_quote quote
					where quote.quotenumber = '032730' And site.tril_gid = quote.SITE or QUOTE.HOTCUTNEWSITE;
				 * 
				 * UPDATE SITECODE AND COREIDS
				 * #Update cversion set Lupddate=sysdate,coresiteid='<GROM GOLD>',addressid='<FROM GOLD>', SITEHANDLE='<GOLD SITECODE>' where sitehandle='HOD ';
				 * 
				 * GET THE IDS OF ORDER
				 * #select Serviceelementid,USID from cserviceelement where serviceelementid in 
				 * (select serviceelementid from CVERSIONSERVICEELEMENT  where versionid in 
				 * (select versionid from cversion where SITEHANDLE='HOD  '));
				 * 
				 * UPDATE ALL USID FOR LIST OF ALL SERVICE ELEMENT IDS LINKED WITH ORDER  
				 * #Update Cserviceelement Set Usid='"+ autoUpdatedUSID+ "',Lupddate=Sysdate Where Serviceelementid='"+ serviceElementID + "';";
				 */
				
				csiSQLs.add("--CASE : "+ caseCategory +" [Legacy Site > Legacy not found in GOLD > ORD Handle not Null]{ For ORDER " + row[4] +"}");
				updateCoreSitesByORDHandle(row,""+caseCategory);
				csiSQLs.add("--CASE : Ends "+caseCategory );
				break;
			case 8:
				/*
				 * CASE : Legacy Site >	Legacy not found in GOLD >	ORD Handle is Null * */
				/* ACTION : Get The USID details (CPE, NAs Backup , Access Connection , Backup Options ), Order number , core ids , sitecode , OSR , status from GOLD and 
				Send to CORE Team*/

				/* 
				 #SELECT DISTINCT ele.USID ,ele.SERVICEELEMENTCLASS FROM CSERVICEELEMENT ele, CVERSIONSERVICEELEMENT vele,CVERSION v 
				 WHERE vele.VERSIONID = v.VERSIONID and 
				 ele.SERVICEELEMENTID = vele.SERVICEELEMENTID and 
				 v.sitehandle='CIS0032365';
				*/
				getUSIDData(row);
				updateCSVRowStatus(row,""+caseCategory,"USID_INFO");
				
				break;
			case 9:
				/* CASE : Active Site in CSI >	Active Sites not found in GOLD > ORD Handle not Null * */
				/* ACTION : Get the Site details from GOLD order and Update in CSI*/
				
				/*
				 *  GET SITEDETAILS FOR ORDER IN GOLD [DISCUSS WITH MAYANK HOW TO FOUND CHANGE ORDER SITE DETAILS]
				 *  
				 *  #select sitecode,address_id , site.CORE_SITE_ID from eq_site site, sc_quote quote
					where quote.quotenumber = '032730' And site.tril_gid = quote.SITE or QUOTE.HOTCUTNEWSITE;
				 * 
				 * UPDATE SITECODE AND COREIDS
				 * #Update cversion set Lupddate=sysdate,coresiteid='<GROM GOLD>',addressid='<FROM GOLD>', SITEHANDLE='<GOLD SITECODE>' where sitehandle='HOD ';
				 * 
				 * GET THE IDS OF ORDER
				 * #select Serviceelementid,USID from cserviceelement where serviceelementid in 
				 * (select serviceelementid from CVERSIONSERVICEELEMENT  where versionid in 
				 * (select versionid from cversion where SITEHANDLE='HOD  '));
				 * 
				 * UPDATE ALL USID FOR LIST OF ALL SERVICE ELEMENT IDS LINKED WITH ORDER  
				 * #Update Cserviceelement Set Usid='"+ autoUpdatedUSID+ "',Lupddate=Sysdate Where Serviceelementid='"+ serviceElementID + "';";
				 */
				
				csiSQLs.add("--CASE : "+ caseCategory +" [Active Site in CSI >	Active Sites not found in GOLD > ORD Handle not Null]{ For ORDER " + row[4] +"}");
				updateCoreSitesByORDHandle(row,""+caseCategory);
				csiSQLs.add("--CASE : Ends "+caseCategory );
				
				break;
			case 10:
				/* CASE : Active Site in CSI > Active Sites not found in GOLD >	ORD Handle is Null  **/
				/* ACTION : Only One Case found (Skip) */
				
				/*
				 * CASE : Legacy Site >	Legacy not found in GOLD >	ORD Handle is Null * */
				/* ACTION : Get The USID details (CPE, NAs Backup , Access Connection , Backup Options ), Order number , core ids , sitecode , OSR , status from GOLD and 
				Send to CORE Team*/

				/* 
				 #SELECT DISTINCT ele.USID ,ele.SERVICEELEMENTCLASS FROM CSERVICEELEMENT ele, CVERSIONSERVICEELEMENT vele,CVERSION v 
				 WHERE vele.VERSIONID = v.VERSIONID and 
				 ele.SERVICEELEMENTID = vele.SERVICEELEMENTID and 
				 v.sitehandle='CIS0032365';
				*/
				getUSIDData(row);
				updateCSVRowStatus(row,""+caseCategory,"USID_INFO");
				break;
			default:	
			updateCSVRowStatus(row,"UN_DEFINED","SKIP_ANALYSIS");
			}
			
		}
		csvReader.close();
		row = null; 
	}
	private void initCSVWriter(String csvReaderUpdatedPath) throws Exception{
		CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(csvReaderUpdatedPath),true);
	    for(String[] row : writableData)
	    {
	    	writer.writeNext(row);
	    }
	    writableData = null;
	    writer.close();
	}
	
	private void initUSIDCSVWriter(String usidWriterPath) throws Exception{
		CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(usidWriterPath),true);
	   for(String[] row : usidwritableData)
	    {
	    	writer.writeNext(row);
	    }
	    writableData = null;
	    writer.close();
	}
	
	private void updateCoreSitesByORDHandle(String[] row,String cat) {
		String CSI_CUST_HANDLE				= row[3];
		String CSI_ORDER_HANDLE				= row[4];
		
		
		try
		{
		// Get Core Sites Details
		ArrayList<String[]> coreIds = CommonUtils.getCoreSiteDetailsFromOrder(CSI_ORDER_HANDLE,siteFixViewComponents);
		
		
		String GOLD_ADD_ID = coreIds.get(0)[0];
		String GOLD_CORE_SITEID=coreIds.get(0)[1];
		String GOLD_ICO =coreIds.get(0)[2]; 
		String GOLD_SITECODE=coreIds.get(0)[3];
		
		
		if(coreIds.size() != 1)
		{
			updateCSVRowStatus(row,cat,"NO_OR_MULTIPLE_SITECODE_FOUND_IN_GOLD");
			csiSQLs.add("--NO_OR_MULTIPLE_SITECODE_FOUND_IN_GOLD");
		}
		else if(!CSI_CUST_HANDLE.equals(GOLD_ICO))
		{
			updateCSVRowStatus(row,cat,"ICO_MISMATCH");
			csiSQLs.add("--ICO_MISMATCH");
		}
		else if(coreIds.size() == 1)
		{
			String csiSql = "Update "+ConnectionBeanCSI.getDbPrefix()+"cversion set Lupddate=sysdate,addressid='"+GOLD_ADD_ID+"',coresiteid='"+GOLD_CORE_SITEID+"',sitehandle='"+CommonUtils.refineData(GOLD_SITECODE)+"' where ORDHANDLE='"+CSI_ORDER_HANDLE+"';";
			csiSQLs.add(csiSql);
			
			ArrayList<String[]> serviceElements = getSEIdFromOrdHandle(CSI_ORDER_HANDLE);
			for(String serviceElement[] : serviceElements)
			{
				String slement = serviceElement[0];
				String usid = serviceElement[1];
				
				
				String updatedUSID = CommonUtils.autoUpDateUSID(usid,GOLD_SITECODE);
				if(!usid.equals(updatedUSID))
				{
				csiSql = "Update "+ConnectionBeanCSI.getDbPrefix()+"Cserviceelement Set Usid='"+CommonUtils.refineData(updatedUSID)+"',Lupddate=Sysdate Where Serviceelementid='"+slement+"';";
				csiSQLs.add(csiSql);
				}
				slement = null;
				usid = null;
				updatedUSID = null;
				
			}
			csiSql = null;
			updateCSVRowStatus(row,cat,"PASS");
		}
		else
		{
			updateCSVRowStatus(row,cat,"UNEXPECTED_DATA_FOUND");
			csiSQLs.add("--UNEXPECTED_DATA_FOUND");
		}
		coreIds = null;
		}catch(Exception e){
			updateCSVRowStatus(row,cat,"EXCEPTION : >>"+e.getMessage());
			csiSQLs.add("--"+e.getMessage());
		}
	}
	
	
	private void updateCoreSitesBySiteHandle(String[] row,String cat) {
		
		String CSI_SITE_HANDLE	 			= row[0];
		String CSI_CUST_HANDLE				= row[3];
		String GOLD_SITE_CODE				= row[8];
		
		try
		{
		// Get Core Sites Details
		ArrayList<String[]> coreIds = getCoreSiteDetailsFromSiteCode(GOLD_SITE_CODE);
		
		String GOLD_ADD_ID = coreIds.get(0)[0];
		String GOLD_CORE_SITEID=coreIds.get(0)[1];
		String GOLD_ICO =coreIds.get(0)[2]; 
		String GOLD_SITECODE=coreIds.get(0)[3];
		
		if(coreIds.size() != 1)
		{
			updateCSVRowStatus(row,cat,"NO_OR_MULTIPLE_SITECODE_FOUND_IN_GOLD");
			csiSQLs.add("--NO_OR_MULTIPLE_SITECODE_FOUND_IN_GOLD");
		}
		else if(!CSI_CUST_HANDLE.equals(GOLD_ICO))
		{
			updateCSVRowStatus(row,cat,"ICO_MISMATCH");
			csiSQLs.add("--ICO_MISMATCH");
		}
		else if(coreIds.size() == 1)
		{
			String csiSql = "Update "+ConnectionBeanCSI.getDbPrefix()+"cversion set Lupddate=sysdate,addressid='"+GOLD_ADD_ID+"',coresiteid='"+GOLD_CORE_SITEID+"',sitehandle='"+CommonUtils.refineData(GOLD_SITECODE)+"' where sitehandle='"+CommonUtils.refineData(CSI_SITE_HANDLE)+"';";
			csiSQLs.add(csiSql);
			
			ArrayList<String[]> serviceElements = getSEIdFromSiteCode(CSI_SITE_HANDLE);
			for(String serviceElement[] : serviceElements)
			{
				String slement = serviceElement[0];
				String usid = serviceElement[1];
				String updatedUSID = CommonUtils.autoUpDateUSID(usid,GOLD_SITECODE);
				if(!usid.equals(updatedUSID))
				{
				csiSql = "Update "+ConnectionBeanCSI.getDbPrefix()+"Cserviceelement Set Usid='"+CommonUtils.refineData(updatedUSID)+"',Lupddate=Sysdate Where Serviceelementid='"+slement+"';";
				csiSQLs.add(csiSql);
				}
				slement = null;
				usid = null;
				updatedUSID = null;
			}
			csiSql = null;
			updateCSVRowStatus(row,cat,"PASS");
		}
		else
		{
			updateCSVRowStatus(row,cat,"UNEXPECTED_DATA_FOUND");
			csiSQLs.add("--UNEXPECTED_DATA_FOUND");
		}
		coreIds = null;
		}catch(Exception e){
			updateCSVRowStatus(row,cat,e.getMessage());
			csiSQLs.add("--"+e.getMessage());
		}
	}
	
	private void updateCSVRowStatus(String[] row,String cat , String status){
		
		String[] updatedRow = new String[row.length+2];
		for(int x = 0; x< row.length; x++){
			updatedRow[x] = row[x];
		}
		updatedRow[row.length-1] = cat;
		updatedRow[row.length] = status;
		writableData.add(updatedRow);
		updatedRow = null;
	}

	
	
	
}
