package com.orange.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import com.orange.ui.component.SiteFixedViewComponents;

public class EQL2InstanceMappingAnalysis {

	private SiteFixedViewComponents siteFixedViewComponents;
	private String header[] = {"QUOTE","SITE_IN_EQL2MAPPING","QUOTESITECODE","STATUS"};
	private ArrayList<String> goldExecutionScript;
	
	public EQL2InstanceMappingAnalysis(String dirLoc, SiteFixedViewComponents siteFixViewComponents) throws Exception{
		
		this.siteFixedViewComponents  =siteFixViewComponents;
		goldExecutionScript = new ArrayList<String>();
		
		siteFixedViewComponents.getQueryResult().append("Analysis Process Starts , Please wait....\n");
		ArrayList<String[]> orderLists = getListOfEQL2InstanceMappingOrders();
		
		ArrayList<String[]> analysisData = new ArrayList<String[]>();
		
		int count = 0;
		double CC = 0.0;
		
		for(String[] orderList : orderLists)
		{
		
			ArrayList<String[]> orderSite=  getSiteCodeOfOrder(orderList[0]);
			goldExecutionScript.add("--------------------------------------------------------------------------------------------------------" );
			
			
			
			if(null!= orderSite && orderSite.size()>0)
			{
				
				goldExecutionScript.add("-- INFO : GOLD Script Starts "+CurrentDateTime.getDateTimeText()+ " for Replace Sitecode:"+ orderList[1] + " And Replacement Sitecode:"+ orderSite.get(0)[0]+" Record Number ("+(int)(count+1)+" from "+orderLists.size()+")");
			
				
				if((orderSite.get(0)[0]).equals(orderList[1]))
				{
					String ARR[] = {orderList[0],orderList[1],orderSite.get(0)[0],"PASS"};
					analysisData.add(ARR);
					goldExecutionScript.add("-- INFO : BOTH SITECODES ARE EQUAL , NOTHING TO DO. ");
				}
				else
				{
					String ARR[] = {orderList[0],orderList[1],orderSite.get(0)[0],"FAILED_FIX_GENERATED"};
					analysisData.add(ARR);	
					
					goldExecutionScript = 	      CommonUtils.updateEQL2InstanceMapping(
						                          goldExecutionScript,
						                          siteFixedViewComponents,
						                          orderList[1],
						                          orderSite.get(0)[0],
						                          orderList[0]);
				}
				goldExecutionScript.add("-- INFO : GOLD Script ends for Replace Sitecode:"+ orderList[1] + " And Replacement Sitecode:"+ orderSite.get(0)[0]);
			}
		else
		{
			goldExecutionScript.add("-- INFO : GOLD Script Starts "+CurrentDateTime.getDateTimeText()+ " for Replace Sitecode:"+ orderList[1] + " And Replacement Sitecode: found NULL  Record Number ("+(int)(count+1)+" from "+orderLists.size()+")");
			goldExecutionScript.add("-- INFO : GOLD Script ends for Replace Sitecode:"+ orderList[1] + " And Replacement Sitecode: NULL");
		}
			
			count++;
			ProgressMonitorPane.getInstance().setProgress(CC,(double)orderLists.size());
			CC++;
		}
		
		String resultFileLoc = "";
		String sqlresultFileLoc = "";
		
		new File(dirLoc+File.separator+ConnectionBean.getUrl()).mkdirs();
		
		resultFileLoc = dirLoc+File.separator+ConnectionBean.getUrl()+File.separator+"EQL2InstanceMappingAnalysis.csv";
		sqlresultFileLoc = dirLoc+File.separator+ConnectionBean.getUrl()+File.separator+"EQL2InstanceMapping.sql";
		
		CommonUtils.showTable(header,analysisData,resultFileLoc);
		createGOLDScriptFile(sqlresultFileLoc);
		siteFixedViewComponents.getQueryResult().append("Analysis File "+resultFileLoc+" Create Successfully.\n");
		siteFixedViewComponents.getQueryResult().append("SQL script File "+sqlresultFileLoc+" Create Successfully.\n");
		siteFixedViewComponents.getQueryResult().append("Analysis Process Ends Successfully.\n");
		
	}
	
	private void createGOLDScriptFile(String filePath) throws Exception{
		File fout = new File(filePath);
		FileOutputStream fos = new FileOutputStream(fout);
		goldExecutionScript = CommonUtils.setUmlaut(goldExecutionScript);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		for(String script : goldExecutionScript){
			bw.write(script);
			bw.newLine();
		}
		bw.close();
	}
	
	private ArrayList<String[]> getListOfEQL2InstanceMappingOrders() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
	    String QUERY ="SELECT DISTINCT ORDERID, Regexp_Substr (INSTANCEID, '[^::]+', 1, '3') as SITECODE FROM "+ConnectionBean.getDbPrefix()+"EQL2InstanceMapping WHERE rownum between  1 and 10000";
		return CommonUtils.getQueryResult(QUERY, siteFixedViewComponents);
	}
	
	private ArrayList<String[]> getSiteCodeOfOrder(String quoteNumber) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
	    String QUERY ="SELECT SITE.SITECODE FROM "+
	    	  ConnectionBean.getDbPrefix()+"EQ_SITE SITE ,"+
	    	  ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE "+
	    	  "WHERE QUOTE.QUOTENUMBER = ? "+
	    	  " AND ((QUOTE.HOTCUTNEWSITE <> 'NULL' AND QUOTE.HOTCUTNEWSITE =  SITE.TRIL_GID) OR (QUOTE.HOTCUTNEWSITE IS NULL AND QUOTE.SITE = SITE.TRIL_GID ))";
	    return CommonUtils.getQueryResult(QUERY, siteFixedViewComponents,quoteNumber);
	}
	
	
}
