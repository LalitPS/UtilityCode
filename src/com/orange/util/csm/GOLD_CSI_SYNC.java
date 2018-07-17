package com.orange.util.csm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.orange.ui.component.SiteFixedViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.ConnectionBeanCSI;
import com.orange.util.CustomCSVWriter;
import com.orange.util.ProgressMonitorPane;

public class GOLD_CSI_SYNC {

	private CustomCSVWriter CSI_TO_GOLD_SITES;
	private ArrayList<ArrayList<String>> csidata ;
	private double csiDataCount = 0;
	private String csioutputFileName;
	private ArrayList<ArrayList<String>> golddata ;
	private double goldDataCount = 0;
	
	String GOLDEXtractQuery = "select " +
			"st.sitecode ,st.status, " +
			"quote.quotenumber,quote.eq_ordertype," +
			"status.eq_status,org.organizationid," +
			"quote.ORDEREDSERVICE,quote.hotcutnewsite,quote.Migrationservice, " +
			"addr.city , addr.country, addr.street1,addr.Street2,addr.Street3,addr.zipcode,addr.country_code,addr.State_code, " +
			"service.service_id,service.isservice,service.producttype,service.is_configurable " +
			"from "+ConnectionBean.getDbPrefix()+"eq_ORDERSTATUS status , "+ConnectionBean.getDbPrefix()+"sc_quote quote, "+ConnectionBean.getDbPrefix()+"eq_site st, "+ConnectionBean.getDbPrefix()+"eq_service service,"+ConnectionBean.getDbPrefix()+"sc_organization org ,"+ConnectionBean.getDbPrefix()+"sc_address addr " +
			"where status.tril_gid = quote.eq_wfstatus " +
			"and quote.site = st.tril_gid and st.eq_siteof = org.tril_gid " +
			"and st.siteaddress = addr.tril_gid and quote.orderedservice = service.service_id " +
			"and st.address_id <> 'null' and st.core_site_id <> 'null' " +
			"and (service.is_configurable != 0 or service.producttype != null) " +
			"and (EQ_STATUS in ('Manage','Acceptance','Billing','Awaiting Close','Closed') " +
			"or (eq_status = 'On-Hold' and eq_oldstatus in ('Manage','Acceptance','Billing','Awaiting Close','Closed')) " +
			"or (eq_status = 'Pending Cancellation' and eq_oldstatus in ('Manage','Acceptance','Billing','Awaiting Close','Closed')))";
	int goldfileIDCount = 1;
	
	String goldoutputFileName,goldoutputFileName_Part2;
	/*
	String GOLDEXtractQuery = "select distinct " +
			"st.sitecode ,st.status," +
			"quote.quotenumber,quote.eq_ordertype," +
			"status.eq_status,org.organizationid, " +
			"quote.ORDEREDSERVICE,quote.hotcutnewsite,quote.Migrationservice, " +
			
			"addr.city , addr.country, addr.street1,addr.Street2,addr.Street3,addr.zipcode,addr.country_code,addr.State_code, " +
			"service.service_id,service.isservice,service.producttype,service.is_configurable " +
			"from "+ConnectionBean.getDbPrefix()+"eq_ORDERSTATUS status , "+ConnectionBean.getDbPrefix()+"sc_quote quote, "+ConnectionBean.getDbPrefix()+"eq_site st, "+ConnectionBean.getDbPrefix()+"eq_service service,"+ConnectionBean.getDbPrefix()+"sc_organization org , "+ConnectionBean.getDbPrefix()+"sc_address addr " +
			"where status.eq_ordergid = quote.tril_gid " +
			"and quote.site = st.tril_gid " +
			"and quote.seqid=service.seqid " +
			"and st.eq_siteof = org.tril_gid " +
			"and st.siteaddress = addr.tril_gid " +
			"and st.address_id is null " +
			"and st.core_site_id is null " +
			"and service.ISSERVICE <> '0' "+ //--and (service.PRODUCTTYPE is not null  or service.is_configurable <> '0') " +
			"and EQ_STATUS in('Manage','Acceptance','Billing','Awaiting Close','Closed')"; 
	*/
	String localCSIQuery = "SELECT DISTINCT ADDRESSID , CORESITEID ,CUSTHANDLE,ORDHANDLE,SERVICEHANDLE FROM "+ConnectionBeanCSI.getDbPrefix()+"cversion WHERE SITEHANDLE =?";
	
	
	String outputFileLocation;

	SiteFixedViewComponents siteFixViewComponents;
	
	public GOLD_CSI_SYNC(SiteFixedViewComponents siteFixViewComponents,String outputFileLocation) throws Exception{
		
		this.outputFileLocation = outputFileLocation;
		this.csioutputFileName =  this.outputFileLocation+"\\"+CommonUtils.getTimeStampFileName()+"_CSI_SYNC_SITES_DETAILS.csv";
		this.goldoutputFileName = this.outputFileLocation+"\\"+CommonUtils.getTimeStampFileName()+"_GOLD_SYNC_SITES_DETAILS.csv";
		this.goldoutputFileName_Part2 = this.outputFileLocation+"\\"+CommonUtils.getTimeStampFileName()+"_GOLD_SYNC_SITES_DETAILS_PART2.csv";
		this.siteFixViewComponents = siteFixViewComponents;
		new File(outputFileLocation).mkdirs();
	}
	public void getCSICountData() throws Exception{
		siteFixViewComponents.getQueryResult().append("Data Extraction Volume assesment.\n");
		String localQuery = "SELECT COUNT(*) FROM (SELECT DISTINCT SITEHANDLE, ADDRESSID , CORESITEID ,CUSTHANDLE,ORDHANDLE,SERVICEHANDLE FROM "+ConnectionBeanCSI.getDbPrefix()+"cversion)";
		Statement csistamt =ConnectionForCSI.getStatement();
		ResultSet csiResultSet = csistamt.executeQuery(localQuery);
		localQuery = null;
		
		while(csiResultSet.next())
		{
			csiDataCount = csiResultSet.getDouble(1);
		}
		csiResultSet.close();
		csistamt.close();
		
		siteFixViewComponents.getQueryResult().append("For CSI "+csiDataCount +" sites will analyze.\n");
		siteFixViewComponents.getQueryResult().append("Data Extraction Volume assesment completed.\n");
		siteFixViewComponents.getQueryResult().append("Data Extraction start for CSI sites.\n");
		getCSISiteCodes();
		
		siteFixViewComponents.getQueryResult().append("Data Extraction ends from CSI sites."+csioutputFileName+"\n");
	}
	
	public String getCSIOutPutFileName(){
		return csioutputFileName;
	}
	
	private void getCSISiteCodes() throws Exception
	{
		
		csidata = new ArrayList<ArrayList<String>> ();
		
		String localQuery = "SELECT DISTINCT  SITEHANDLE, ADDRESSID , CORESITEID ,CUSTHANDLE,ORDHANDLE,SERVICEHANDLE FROM "+ConnectionBeanCSI.getDbPrefix()+"cversion";// where ROWNUM between 1 and 20000";
	
		Statement csistamt = ConnectionForCSI.getStatement();
		ResultSet csiResultSet = csistamt.executeQuery(localQuery);
		localQuery = null;
		
	
		double count = 0.0; 
		while(csiResultSet.next())
		{
		count++;	
		ProgressMonitorPane.getInstance().setProgress(count,csiDataCount);	
		
		ArrayList<String> csirow = new ArrayList<String>();
		String V_SITEHANDLE = csiResultSet.getString(1);
		String V_ADDRESSID = csiResultSet.getString(2);
		String V_CORESITEID = csiResultSet.getString(3);
		String V_CUSTHANDLE = csiResultSet.getString(4);
		String V_ORDHANDLE = csiResultSet.getString(5);
		String  V_SERVICEHANDLE =  csiResultSet.getString(6);
		
	
			csirow.add(V_SITEHANDLE);
			csirow.add(V_ADDRESSID);
			csirow.add(V_CORESITEID);
			csirow.add(V_CUSTHANDLE);
			csirow.add(V_ORDHANDLE);
			csirow.add(V_SERVICEHANDLE);
			csirow.add("NOT REQUIRED");
			csirow.add("NOT REQUIRED");
		
			
			ArrayList<String> goldrow =getGOLDAppendRowData(V_SITEHANDLE);
			
			for(String col : goldrow)
			{
				csirow.add(col);
			}
			csidata.add(csirow);
			goldrow = null;
			csirow = null;
		}
		
		csiResultSet.close();
		csistamt.close();
		
		initCSVWritersForCSI(csioutputFileName);
		
	}
	
	private ArrayList<String> getGOLDAppendRowData(String V_SITEHANDLE) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		
		ArrayList<String> goldrow = new ArrayList<String>();
		String goldInnerQuery = "select site.sitecode, site.address_id , site.core_site_id , site.status,site.orange_sitename,org.organizationid  from "+ConnectionBean.getDbPrefix()+"eq_site site,"+ConnectionBean.getDbPrefix()+"sc_organization org where org.tril_gid = site.eq_siteof and  site.sitecode = ?";
	
		
		PreparedStatement goldstamt = ConnectionForGOLD.getPreparedStatement(goldInnerQuery);
		goldstamt.setString(1, V_SITEHANDLE);
	
		ResultSet goldResultSet = goldstamt.executeQuery();
		
		boolean countgoldresult = false;
				while(goldResultSet.next())
				{
					String sitecode = goldResultSet.getString(1);
					String address_id = goldResultSet.getString(2);
					String core_site_id = goldResultSet.getString(3);
					String status = goldResultSet.getString(4);
					String osr = goldResultSet.getString(5);
					String organizationid = goldResultSet.getString(6);
					
					goldrow.add(sitecode);
					goldrow.add(address_id);
					goldrow.add(core_site_id);
					goldrow.add(status);
					goldrow.add(osr);
					goldrow.add(CommonUtils.isNULL(organizationid) ? "NO_ICO_FOUND_IN_GOLD" : organizationid);
					goldrow.add("CSI_SITEHANDLE_FOUND_IN_GOLD");
					countgoldresult = true;
				}
				
				if(!countgoldresult)
				{
					
					PreparedStatement goldstamt1 = ConnectionForGOLD.getPreparedStatement(goldInnerQuery);
					goldstamt1.setString(1, (CommonUtils.isNULL(V_SITEHANDLE) ? V_SITEHANDLE : V_SITEHANDLE.trim()));
				
					ResultSet goldResultSet1 = goldstamt1.executeQuery();
					
					
							while(goldResultSet1.next())
							{
								String sitecode = goldResultSet1.getString(1);
								String address_id = goldResultSet1.getString(2);
								String core_site_id = goldResultSet1.getString(3);
								String status = goldResultSet1.getString(4);
								String osr = goldResultSet1.getString(5);
								String organizationid = goldResultSet1.getString(6);
								
								goldrow.add(sitecode);
								goldrow.add(address_id);
								goldrow.add(core_site_id);
								goldrow.add(status);
								goldrow.add(osr);
								goldrow.add(CommonUtils.isNULL(organizationid) ? "NO_ICO_FOUND_IN_GOLD" : organizationid);
								goldrow.add("CSI_SITEHANDLE_FOUND_IN_GOLD_AFTER_TRIM");
								countgoldresult = true;
							}
							goldstamt1.close();
							goldResultSet1.close();
							
							
				}
				if(!countgoldresult)
				{
					goldrow.add("");
					goldrow.add("");
					goldrow.add("");
					goldrow.add("");
					goldrow.add("");
					goldrow.add("");
					goldrow.add("CSI_SITEHANDLE_NOT_FOUND_IN_GOLD");
				}
				goldResultSet.close();
				goldstamt.close();
				countgoldresult = false;
				return goldrow;
	}
	
	public void getGOLDCoundData() throws Exception
	{
		siteFixViewComponents.getQueryResult().append("Data Extraction Volume assesment.\n");
		String goldlocalQuery = "select count(sitecode) from ("+GOLDEXtractQuery+")";
		Statement goldstamt = ConnectionForGOLD.getStatement();
		ResultSet goldResultSet = goldstamt.executeQuery(goldlocalQuery);
		goldlocalQuery = null;
		
		while(goldResultSet.next())
		{
			goldDataCount = goldResultSet.getDouble(1);
		}
		
		siteFixViewComponents.getQueryResult().append("For GOLD "+goldDataCount +" sites  will  analyze.\n");
		siteFixViewComponents.getQueryResult().append("Data Extraction Volume assesment completed.\n");
		goldResultSet.close();
		goldstamt.close();	
		siteFixViewComponents.getQueryResult().append("Data Extraction start from GOLD sites.\n");
		getGOLDSiteCodes();
		siteFixViewComponents.getQueryResult().append("Data Extraction ends from GOLD sites."+goldoutputFileName+"\n");
		siteFixViewComponents.getQueryResult().append("File Creation successfully.\n");
	}
	
	private void getGOLDSiteCodes() throws Exception
	{
		golddata = new ArrayList<ArrayList<String>> ();
		
		String localQuery  = GOLDEXtractQuery;
		Statement goldstamt = ConnectionForGOLD.getStatement();
		ResultSet goldResultSet = goldstamt.executeQuery(localQuery);
		int columnCount = goldResultSet.getMetaData().getColumnCount();
		localQuery = null;
		double count = 0.0;
		while(goldResultSet.next())
		{
			count++;
			ProgressMonitorPane.getInstance().setProgress(count,goldDataCount);	
			ArrayList<String> goldrow = new ArrayList<String>();
			for(int x = 1; x<=columnCount;x++)
			{
				goldrow.add(goldResultSet.getString(x));
			}
		
	
		PreparedStatement csipstmt = ConnectionForCSI.getPreparedStatement(localCSIQuery);
		csipstmt.setString(1,goldrow.get(0));
	
		ResultSet csiResultSet = csipstmt.executeQuery();
		int csicolumnCount = csiResultSet.getMetaData().getColumnCount();
		int totalDatafound = 0;
		while(csiResultSet.next())
		{
			ArrayList<String> csirow = new ArrayList<String>(goldrow);
			
			for(int x = 1; x<=csicolumnCount;x++)
			{
				csirow.add(csiResultSet.getString(x));
			}
			csirow.add("GOLD_SITECODE_FOUND_IN_CSI");
			golddata.add(csirow);
			csirow = null;
			totalDatafound++;
		}
		
		csiResultSet.close();
		csipstmt.close();
		
		if(totalDatafound==0)
		{
			csipstmt = ConnectionForCSI.getPreparedStatement(localCSIQuery);
			csipstmt.setString(1,goldrow.get(0)+" ");
			csiResultSet = csipstmt.executeQuery();
			csicolumnCount = csiResultSet.getMetaData().getColumnCount();
			totalDatafound = 0;
			
			while(csiResultSet.next())
			{
				ArrayList<String> csirow = new ArrayList<String>(goldrow);
				
				for(int x = 1; x<=csicolumnCount;x++)
				{
					csirow.add(csiResultSet.getString(x));
				}
				csirow.add("GOLD_SITECODE_FOUND_IN_CSI_AFTER_ADDING_WHITE_SPACE");
				golddata.add(csirow);
				csirow = null;
				totalDatafound++;
			}
			csiResultSet.close();
			csipstmt.close();
			
			if(totalDatafound == 0)
			{
				ArrayList<String> csirow = new ArrayList<String>(goldrow);
				String V_ADDRESSID = "";
				String V_CORESITEID = "";
				String V_CUSTHANDLE = "";
				String V_ORDHANDLE = "";
				String V_SERVICEHANDLE = "";
				csirow.add(V_ADDRESSID);
				csirow.add(V_CORESITEID);
				csirow.add(V_CUSTHANDLE);
				csirow.add(V_ORDHANDLE);
				csirow.add(V_SERVICEHANDLE);
				csirow.add("GOLD_SITECODE_NOT_FOUND_IN_CSI");
				golddata.add(csirow);
				csirow = null;
			}
			totalDatafound =0;
		}
		totalDatafound = 0;
		goldrow = null;
		}
		goldResultSet.close();
		goldstamt.close();
		initCSVWritersForGOLD(goldoutputFileName);
		golddata = null;
	}
	
	private void initCSVWritersForCSI(String csioutputFileName) throws Exception{
		
		siteFixViewComponents.getQueryResult().append("Initilize CSV writer(s) successfully.\n");
		siteFixViewComponents.getQueryResult().append("extracting csi data from cache .("+csidata.size()+")\n");
		CSI_TO_GOLD_SITES = new CustomCSVWriter(new FileWriter(csioutputFileName),true);
		
		String csicolumns[] ={  "CSI_SITE_HANDLE",
								"CSI_ADDRESS_ID", 
								"CSI_CORE_SITE_ID", 
								"CSI_CUST_HANDLE",
								"CSI_ORDER_HANDLE",
								"CSI_SERVICEHANDLE",
								"CSI_USID",
								"CSI_SERVICEELEMENTCLASS",
								"GOLD_SITE_CODE",
								"GOLD_ADDRESS_ID",
								"GOLD_CORE_SITE_ID",
								"GOLD_STATUS",
								"GOLD_ORANGE_SITENAME",
								"GOLD_ICO",
								"IS_AVAILABLE_AFTER_TRIM"};

		CSI_TO_GOLD_SITES.writeNext(csicolumns);
		for(ArrayList<String> csirow : csidata)
		{
			String array[] = new String[csirow.size()]; 
			for(int x = 0 ; x< csirow.size();x++)
			{
				array[x] = csirow.get(x);
			}
			CSI_TO_GOLD_SITES.writeNext(array);
		}
		CSI_TO_GOLD_SITES.close();
		siteFixViewComponents.getQueryResult().append("File Created Successfully on Location "+csioutputFileName+" \n");
		siteFixViewComponents.getFileToValidate().setText(csioutputFileName);
		csidata = null;
	}

	private void initCSVWritersForGOLD(String goldoutputFileName) throws IOException{
		siteFixViewComponents.getQueryResult().append("Initilize CSV writer(s) successfully.\n");
		
		String goldcolumns[] ={"GOLD_SITE_CODE","SITE_STATUS","QUOTENUMBER",
				"QUOTE_TYPE","QUOTE_STATUS","ORG_ID","ORDEREDSERVICE",
				"HOTCUTNEWSITE","MIGRATIONSERVICE","CITY",
				"COUNTRY","STREET1","STREET2","STREET3","ZIPCODE",
				"COUNTRY_CODE","STATE_CODE","SERVICE_ID","SERVICE_BUILD",
				"PRODUCT_TYPE","IS_CONFIGURABLE",
				"CSI_ADDRESS_ID","CSI_CORE_SITE_ID","CSI_CUST_HANDLE",
				"CSI_ORDER_HANDLE","CSI_SERVICEHANDLE","AVAILABLE_IN_CSI"};
		
		
		int rowinafile = 500000;
		int totalFile = golddata.size()/rowinafile;
		
		if(golddata.size()%rowinafile !=0){
			totalFile = totalFile+1;
		}
		
		
		if(golddata.size()<=rowinafile){
			totalFile = 1;
			rowinafile = golddata.size();
		}
		
		int from =0;
		int to= rowinafile;
		
		for(int F = 0 ; F <totalFile ; F++)
		{
		
			CustomCSVWriter GOLD_TO_CSI_SITES = new CustomCSVWriter(new FileWriter(goldoutputFileName+"_PART_"+(F+1)+".csv"),true);
			GOLD_TO_CSI_SITES.writeNext(goldcolumns);
			    
				for(int x = from ; x<to; x++ )
				{
					ArrayList<String> goldrow = golddata.get(x);
					String array[] = new String[goldrow.size()]; 
					for(int y = 0 ; y< goldrow.size();y++)
					{
						array[y] = goldrow.get(y);
					}
					GOLD_TO_CSI_SITES.writeNext(array);
					from = x+1;
					
				}
				GOLD_TO_CSI_SITES.close();
				to = (to+rowinafile <=golddata.size() ? to+rowinafile :  to+(golddata.size()-to));
				siteFixViewComponents.getQueryResult().append("File Created Successfully on Location "+goldoutputFileName+"_PART_"+(F+1)+".csv out of "+totalFile+" \n");
		}
		
		
	}


	

}
