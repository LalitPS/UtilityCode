package com.orange.util.others;

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
import java.sql.Statement;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.orange.ui.component.SiteFixedViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.ConnectionBeanCSI;
import com.orange.util.CurrentDateTime;
import com.orange.util.CustomCSVWriter;
import com.orange.util.ProgressMonitorPane;
import com.orange.util.csm.ConnectionForGOLD;

public class SiteCodeswithSpecialChars {

	private String columnName[] = { "ORANGE_SITENAME", "ADDRESS_ID", "CORE_SITE_ID", "SITECODE","TRIL_GID"};
	private ArrayList<String> csiSQLs ;
	
	private ArrayList<String[]> csvUpdatedData;
	private ArrayList<String> goldSQLs ;
	
	private String hotcutquotessql = "select quotenumber from "+ConnectionBean.getDbPrefix()+"sc_quote where HOTCUTNEWSITE in (select tril_gid from "+ConnectionBean.getDbPrefix()+"eq_site where SITECODE=?)";
	private String quotessql       = "select quotenumber from "+ConnectionBean.getDbPrefix()+"sc_quote where site in (select tril_gid from "+ConnectionBean.getDbPrefix()+"eq_site where SITECODE=?)";
	
	private String readerfileLocation;
	private SiteFixedViewComponents siteFixViewComponents;
	
	private String SQL = "select ORANGE_SITENAME, ADDRESS_ID, CORE_SITE_ID, SITECODE,TRIL_GID from "+ConnectionBean.getDbPrefix()+"eq_site where sitecode like '%''%' " +
			"Union All select ORANGE_SITENAME, ADDRESS_ID, CORE_SITE_ID, SITECODE,TRIL_GID from "+ConnectionBean.getDbPrefix()+"eq_site where sitecode like '%&%' " +
			"Union All select ORANGE_SITENAME, ADDRESS_ID, CORE_SITE_ID, SITECODE,TRIL_GID from "+ConnectionBean.getDbPrefix()+"eq_site where sitecode like '%;%'";
	private ArrayList<String[]> tableData;
	private String updatedColumnName[] = { "ORANGE_SITENAME", "ADDRESS_ID", "CORE_SITE_ID", "SITECODE" ,"UPDATED_SITECODE","UPDATED_ORANGE_SITENAME"};
	private String usdiddatasql = "select Serviceelementid,USID from "+ConnectionBeanCSI.getDbPrefix()+"cserviceelement where serviceelementid in (select serviceelementid from "+ConnectionBeanCSI.getDbPrefix()+"CVERSIONSERVICEELEMENT  where versionid in (select versionid from "+ConnectionBeanCSI.getDbPrefix()+"cversion where ordhandle=?))";
	
	public SiteCodeswithSpecialChars(String readerfileLocation,SiteFixedViewComponents siteFixViewComponents) throws Exception{
		this.siteFixViewComponents = siteFixViewComponents;
		this.readerfileLocation = readerfileLocation;
		goldSQLs = new ArrayList<String> ();
		csiSQLs = new ArrayList<String> ();
		tableData = new ArrayList<String[]>();
		csvUpdatedData = new ArrayList<String[]>();
		new File(readerfileLocation+File.separator+ConnectionBean.getUrl()).mkdirs();
	}
	
	private String  createReaderFile() throws Exception{
		
		String readerFilePath = readerfileLocation+"\\"+ConnectionBean.getUrl()+"\\SiteCodesSplChars.csv";
		siteFixViewComponents.getFileToValidate().setText(readerFilePath);
		
		CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(readerFilePath),true);
		writer.writeNext(columnName);
	
		siteFixViewComponents.getQueryResult().append(SQL+"\n");
		Statement stm = ConnectionForGOLD.getStatement();
		ResultSet resultset = stm.executeQuery(SQL);
		while(resultset.next()){
			String row[]  = new String[5];
			row[0] = resultset.getString(1);
			row[1] = resultset.getString(2);
			row[2] = resultset.getString(3);
			row[3] = resultset.getString(4);
			row[4] = resultset.getString(5);
			writer.writeNext(row);
		}
		resultset.close();
		stm.close();
		writer.close();
		
		siteFixViewComponents.getQueryResult().append("Data Extraction of sitecodes completed.\n");
		return readerFilePath;
	}
	
	public void createScriptFile(String filePath,ArrayList<String> goldExecutionScript,String Var,String ext) throws Exception{
		
		createScriptFilePath(filePath,Var,ext);
		
		File fout = new File(createScriptFilePath(filePath,Var,ext));
		FileOutputStream fos = new FileOutputStream(fout);
	 
		goldExecutionScript = CommonUtils.setUmlaut(goldExecutionScript);
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		for(String script : goldExecutionScript){
			bw.write(script);
			bw.newLine();
		}
		bw.close();
	}
	

	public String createScriptFilePath(String filePath,String Var,String ext) throws Exception{
		
		String path = filePath;	
		int index = path.lastIndexOf(".");
		String sub = path.substring(0,index) ;
		String goldPath=sub+"_"+Var+"_SiteCodes."+ext;
		
		return goldPath;
	}
	
	private void csvWriter(String readerfilePath) throws IOException{
		CSVWriter csvWriter = new CSVWriter(new FileWriter(readerfilePath));
		csvWriter.writeNext(updatedColumnName);
		for(String[] row : csvUpdatedData){
			csvWriter.writeNext(row);
		}
		csvWriter.close();
	}
	
	private ArrayList<String> getQueryResult(String query, String param) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {

		String localParam = param;
		String localQuery = query;
		PreparedStatement pstmt = ConnectionForGOLD.getPreparedStatement(localQuery);
		pstmt.setString(1, localParam);
		String upquery = localQuery.replace("?", "'"+localParam +"'");
		
		siteFixViewComponents.getQueryResult().append(upquery+"\n");
		
		ResultSet resultSet = pstmt.executeQuery();
		ArrayList<String> results = new ArrayList<String>();
		while (resultSet.next()) 
		{
			results.add(resultSet.getString(1));
		}
		resultSet.close();
		pstmt.close();
		localParam = "";
		localQuery = "";
		query="";
		param="";
		return results;

	}
	private boolean isSiteCodeExists(String proposedSitecode) throws Exception{
		String query = "select sitecode from "+ConnectionBean.getDbPrefix()+"eq_site where sitecode = ?";
		PreparedStatement pstmt = ConnectionForGOLD.getPreparedStatement(query);
		pstmt.setString(1, proposedSitecode);
		ResultSet resultSet = pstmt.executeQuery();
		int count =0;
		while (resultSet.next()) {
			count++;
		}
		resultSet.close();
		pstmt.close();
	    siteFixViewComponents.getQueryResult().append(query.replace("?", proposedSitecode)+"\n");
		return (count > 0 ? true : false);
	}
	
	public void showTable(String resultFileLoc) throws IOException
	{
		CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(resultFileLoc),true);
		
		String[] columnNames ={"QUOTE_NUMBER","REPLACE_SITECODE","REPLACEMENT_SITECODE","ADDRESS ID","CORE_SITE_ID","SERVICE ELEMENT ID","PREV USID","UPDATED USID","IS AUTO UPDATED"};
		writer.writeNext(columnNames);
		for (String row[]:tableData)
		{
				writer.writeNext(row);
		}
		writer.close();
		CommonUtils.showOnTable(resultFileLoc);
	}
	
	private String[] siteCodesScript(String TRIL_GID,String ORANGE_SITENAME,String SITECODE,String ADDRESS_ID,String CORE_SITE_ID,String[] updateCSV,boolean isLegacy) throws Exception{
	
		String updatedSiteCode = CommonUtils.removeSpecialChars(SITECODE);
		String refineSiteCode = CommonUtils.refineData(SITECODE);
		String refineOSR = CommonUtils.refineData(ORANGE_SITENAME);
	
		if(updatedSiteCode.equalsIgnoreCase(SITECODE)){
			String SQL = "-- Sitecode " + updatedSiteCode +" doesn't contain any special characters.";
			siteFixViewComponents.getQueryResult().append(SQL+"\n");
			goldSQLs.add(SQL);
			csiSQLs.add(SQL);
			updateCSV[4] =SITECODE;
			updateCSV[5] = ORANGE_SITENAME;
		}
		
		else
		{
			if(!isSiteCodeExists(updatedSiteCode))
			{
				
				
				// ADD UPDATE OF EQL2InstanceMapping
				goldSQLs = CommonUtils.updateEQL2InstanceMapping(goldSQLs,siteFixViewComponents,refineSiteCode,updatedSiteCode,null);
				
				
				// For Legacy 
				if(isLegacy)
				{
						
						String EQ_COMMENT = "Change sitecode "+refineSiteCode+" from " +updatedSiteCode +" and Orange_sitename from "+refineOSR+" to "+updatedSiteCode;
						String SQL = "update "+ConnectionBean.getDbPrefix()+"eq_site set sitecode='"+updatedSiteCode+"', orange_sitename='"+updatedSiteCode+"',MODIFICATIONDATE=sysdate,EQ_COMMENT='"+EQ_COMMENT+"' where TRIL_GID='"+TRIL_GID+"';";
						goldSQLs.add(SQL);
						
						siteFixViewComponents.getQueryResult().append(SQL+"\n");
						
						ArrayList<String> quotes = getQueryResult(quotessql,SITECODE);
						ArrayList<String> hotcutquotes = getQueryResult(hotcutquotessql,SITECODE);
						
						updateCversion(quotes,updatedSiteCode);
						updateCversion(hotcutquotes,updatedSiteCode);
						
						updateCServiceElements(quotes,SITECODE,updatedSiteCode,ADDRESS_ID,CORE_SITE_ID);
						updateCServiceElements(hotcutquotes,SITECODE,updatedSiteCode,ADDRESS_ID,CORE_SITE_ID);
						
					
						updateCSV[4] =updatedSiteCode;
						updateCSV[5] = updatedSiteCode;
						
						
				}
				// For Non Legacy Sites
				else
				{
					
					String EQ_COMMENT = "Change sitecode "+refineSiteCode+" from " +updatedSiteCode;
					String SQL = "update "+ConnectionBean.getDbPrefix()+"eq_site set sitecode='"+updatedSiteCode+"',MODIFICATIONDATE=sysdate,EQ_COMMENT='"+EQ_COMMENT+"' where TRIL_GID='"+TRIL_GID+"';";
					goldSQLs.add(SQL);
					
					siteFixViewComponents.getQueryResult().append(SQL+"\n");
					
					ArrayList<String> quotes = getQueryResult(quotessql,SITECODE);
					ArrayList<String> hotcutquotes = getQueryResult(hotcutquotessql,SITECODE);
					
					updateCversion(quotes,updatedSiteCode);
					updateCversion(hotcutquotes,updatedSiteCode);
					
					updateCServiceElements(quotes,SITECODE,updatedSiteCode,ADDRESS_ID,CORE_SITE_ID);
					updateCServiceElements(hotcutquotes,SITECODE,updatedSiteCode,ADDRESS_ID,CORE_SITE_ID);
					
				
					
					updateCSV[4] =updatedSiteCode;
					updateCSV[5] = ORANGE_SITENAME;
					
					
				}
			}
			else
			{
					String SQL = "-- Sitecode " + updatedSiteCode +" already exists in database.";
					goldSQLs.add(SQL);
					csiSQLs.add(SQL);
					siteFixViewComponents.getQueryResult().append("Sitecode " + updatedSiteCode +" already exists in database.\n");
					updateCSV[4] =SITECODE;
					updateCSV[5] = ORANGE_SITENAME;
			}
		}
		
		return updateCSV;
	}
	
	public void startSQLs()throws Exception 
	{
		String readerfilePath = createReaderFile();
		CSVReader csvFileReader = new CSVReader(new FileReader(readerfilePath));
		ArrayList<String[]> fileRows = new ArrayList<String[]>();
		
		String[] fileData = csvFileReader.readNext();
	
		while ((fileData = csvFileReader.readNext()) != null) 
		{
			fileRows.add(fileData);
		}
		csvFileReader.close();
	
		CSVReader csvReader = new CSVReader(new FileReader(readerfilePath));
		String[] columnHeader = csvReader.readNext();
		
		for (int C = 0; C < columnHeader.length; C++) 
		{
			if (!columnName[C].equalsIgnoreCase(columnHeader[C])) 
			{
				siteFixViewComponents.getQueryResult().append("COLUMN_MISMATCH ::"+columnName[C]+"::"+columnHeader[C]+"\n");
			}
		}
		goldSQLs.add("--GOLD scripts starts "+CurrentDateTime.getDateTimeText());
		csiSQLs.add("--CSI scripts starts "+CurrentDateTime.getDateTimeText());
		double count = 0.0;
		
		while ((columnHeader = csvReader.readNext()) != null) 
		{
			String ORANGE_SITENAME= columnHeader[0];
			String ADDRESS_ID = columnHeader[1];
			String CORE_SITE_ID = columnHeader[2];
			String SITECODE = columnHeader[3];
			String TRIL_GID = columnHeader[4];
			
			String[] updateCSV = new String[6];
			updateCSV[0] = ORANGE_SITENAME;
			updateCSV[1] = ADDRESS_ID;
			updateCSV[2] = CORE_SITE_ID;
			updateCSV[3] = SITECODE;
			
			
			if(CommonUtils.isNULL(ADDRESS_ID) || CommonUtils.isNULL(CORE_SITE_ID)){
				String headerInfo = "--Update ORANGE_SITENAME "+ORANGE_SITENAME +" and SITECODE " +SITECODE + " For Legacy sites.";
				goldSQLs.add(headerInfo);
				csiSQLs.add(headerInfo);
				String[] arr = siteCodesScript(TRIL_GID,ORANGE_SITENAME,SITECODE,ADDRESS_ID,CORE_SITE_ID,updateCSV,true);
				csvUpdatedData.add(arr);
				
			}
			else{
				String headerInfo = "--Update SITECODE " +SITECODE + " For Non Legacy sites.";
				goldSQLs.add(headerInfo);
				csiSQLs.add(headerInfo);
				String arr[] =siteCodesScript(TRIL_GID,ORANGE_SITENAME,SITECODE,ADDRESS_ID,CORE_SITE_ID,updateCSV,false);
				csvUpdatedData.add(arr);
				
			}
			count++;
			ProgressMonitorPane.getInstance().setProgress(count,(double)fileRows.size());
			
		}
		csvReader.close();
		goldSQLs.add("Commit;");
		csiSQLs.add("Commit;");
		createScriptFile(readerfilePath,goldSQLs,"GOLD","sql");
		createScriptFile(readerfilePath,csiSQLs,"CSI","sql");
		
		csvWriter(readerfilePath);
		showTable(createScriptFilePath(readerfilePath,"View",".csv"));
		siteFixViewComponents.getQueryResult().append("Completed....\n");
		CommonUtils.createConsoleLogFile(siteFixViewComponents);
	}
	
	private void updateCServiceElements(ArrayList<String> quotes,String SITECODE,String updatedSiteCode,String ADDRESS_ID,String CORE_SITE_ID) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		
		for(String quote : quotes)
		{
			ArrayList<String[]> usids  = CommonUtils.getCSIQueryResult(usdiddatasql,siteFixViewComponents,quote);
			for(String arr[]:usids)
			{
				
				String serviceElementID = arr[0];
				String usid = arr[1];
				
				boolean isAlreadyUpdatedUSID = CommonUtils.isAlreadyUpDateUSID(usid,updatedSiteCode);
				String autoUpdatedUSID = CommonUtils.autoUpDateUSID(usid,updatedSiteCode);
				
				String csiexecutionData = "Update "+ ConnectionBeanCSI.getDbPrefix()+ "Cserviceelement Set Usid='"+ autoUpdatedUSID+ "',Lupddate=Sysdate Where Serviceelementid='"+serviceElementID+"';";
				
				csiSQLs.add(csiexecutionData);
				
				String isAutoUpdated = "True";
				
				if (autoUpdatedUSID.equalsIgnoreCase(usid)) 
				{
					isAutoUpdated = "False";
				}
				if (isAlreadyUpdatedUSID) 
				{
					isAutoUpdated = "Existing Updated";
				}
				
				String[] row = new String[9];
				row[0]=quote;
				row[1]=SITECODE;
				row[2]=updatedSiteCode;
				row[3]=ADDRESS_ID;
				row[4]=CORE_SITE_ID;
				row[5]=serviceElementID;
				row[6]=usid;
				row[7]=autoUpdatedUSID;
				row[8]=isAutoUpdated;
				
				tableData.add(row);
			}
		}
	}
	
	/*
	 * Get List of Orders for change sitecode
	 * Move into CSI with that list....
	 * 
	 * 
	 */
	private void updateCversion(ArrayList<String> quotes,String replacementSitecode){
		for(String quoteNumber:quotes)
		{
		String csiexecutionData = "Update "+ConnectionBeanCSI.getDbPrefix()+"cversion set Lupddate=sysdate,sitehandle='"+replacementSitecode+"' where ordhandle='"+ quoteNumber + "';";
		csiSQLs.add(csiexecutionData);
		}
		
	}
	
}