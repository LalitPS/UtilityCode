package com.orange.util.others;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.ui.component.SiteFixedViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.ProgressMonitorPane;

public class CreateIsValidPreSalesFixForPrevDedup {

	private String fileName;
	private String getTrilGid="select tril_gid,ISVALIDPRESALES  from "+ConnectionBean.getDbPrefix()+"eq_site where sitecode =?";
	private ArrayList<String> goldsql;
	private String goldSQLFilePath ;
	private ArrayList<String[]> siteCodeList;
	
	private SiteFixedViewComponents siteFixViewComponents;
	
	
	
	public CreateIsValidPreSalesFixForPrevDedup(SiteFixedViewComponents siteFixViewComponents) throws Exception
	{
		
		this.fileName= siteFixViewComponents.getFileToValidate().getText();
		this.siteCodeList = new ArrayList<String[]>();
		this.siteFixViewComponents = siteFixViewComponents;
		goldsql = new ArrayList<String>();
		String path = this.fileName;	
		
		int index = path.lastIndexOf(".");
		String sub = path.substring(0,index) ;
		goldSQLFilePath=sub+"_IS_VALID_PRESALES_FIX.sql";
		
		readDedupCSV(this.fileName);
		createGOLDScriptFile(this.goldSQLFilePath);
		
		
	}
	private void createGOLDScriptFile(String goldSQLFilePath) throws Exception{
		double count = 0.0; 
		final int totalDataCount = siteCodeList.size();
		
		for(String[] arr : siteCodeList)
		{
			count++;	
			ProgressMonitorPane.getInstance().setProgress(count,totalDataCount);
			String sitecode = arr[0];
			String validPresales = arr[1];
			getTrilGIDofAddressId(sitecode,validPresales);
			
		}
		
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
		
		siteFixViewComponents.getQueryResult().append("\n --------------------------------------------------------------------------------");
		siteFixViewComponents.getQueryResult().append("\n ISVALIDPRESALES FIXES FILE CREATED "+goldSQLFilePath);
		siteFixViewComponents.getQueryResult().append("\n --------------------------------------------------------------------------------\n");
		
	}
	private void getTrilGIDofAddressId(String sitecode,String validPreSales) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		ArrayList<String[]> extract = CommonUtils.getQueryResult(getTrilGid, siteFixViewComponents,sitecode);
		goldsql.add("-- Script starts for Gold SITECODE "+sitecode);
		for(String[] row : extract)
		{
			String TRIL_GID = row[0];
			String validPereSaleValue = row[1];
			
			if (null != validPreSales) 
			{
        		if ("Y".equalsIgnoreCase(validPreSales)){
        			validPreSales = "1";}
        		else if ("N".equalsIgnoreCase(validPreSales)){
        			validPreSales = "0";}
        	}
			
			if(null == validPereSaleValue || (!validPereSaleValue.equalsIgnoreCase(validPreSales)))
			{
			String updateQuery = "update "+ConnectionBean.getDbPrefix()+"eq_site set ISVALIDPRESALES='"+validPreSales+"',MODIFICATIONDATE=sysdate where TRIL_GID='"+TRIL_GID+"';";
			goldsql.add(updateQuery);
			}
			else{
				goldsql.add("---PreSales Value already Updated.("+validPereSaleValue+")");
			}	
		}
		goldsql.add("-- Script ENDS for Gold SITECODE "+sitecode);
	}
	

	
	
	private void readDedupCSV(String filePath) throws IOException{
		
		CSVReader	csvReader = new CSVReader(new FileReader(filePath));
		String[] csvRow;
		while ((csvRow = csvReader.readNext()) != null) 
		{
			
			String AM_RESPONSE = csvRow[1];
			String isValidPreSales = csvRow[21];
			String SITECODE = csvRow[22];
		
			
			String arr[] = new String[2];
			if(AM_RESPONSE.equalsIgnoreCase("TRUE") && !CommonUtils.isNULL(isValidPreSales) && (isValidPreSales.trim().equalsIgnoreCase("1") || isValidPreSales.trim().equalsIgnoreCase("0") || isValidPreSales.trim().equalsIgnoreCase("Y") || isValidPreSales.trim().equalsIgnoreCase("N")))
			{
			arr[0]=SITECODE;
			arr[1]=isValidPreSales;
			siteCodeList.add(arr);
			
			}
		}
	csvReader.close();
	}
}