package com.orange.L3.MLAN;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.orange.ui.component.MLANViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.CurrentDateTime;
import com.orange.util.ProgressMonitorPane;

public class DeliverSRF2Info {

	
	/*
	 * AS PER NEW FINDINGS 
	 * 
	 * EQ_ACTIONTAKEN : 'AUTO CLOSED' SHOULD BE REPLACED WITH 'SRF2 not required for this order'
	 * EQ_COMPLETEDBY : FROM APPLICATION IT TAKES LOGING USER NAME , FROM SCRIPTS THIS IS 'SYSTEM GENERATED'
	 * MODIFICATIONDATE = SYSDATE
	 * STATUS : CLOSED
	 * EQ_ACTUALDATE = SYSTEDATE : NOT USED
	 * EQ_CREATEDBY = system generated : NOT USED
	 */
	private MLANViewComponents mlanViewComponents;
	private String pathCSV,pathSQL,pathRBSQL;
	private ArrayList<String> goldExecutionScript;
	private ArrayList<String> goldExecutionrROLLBACKScript;
	private ArrayList<String[]> detailedQueryResults;
	
	
	String DETAILEDQUERY = "SELECT DISTINCT " +
			"MILES1.TRIL_GID," +
			"QUOTE.QUOTENUMBER, " +
			"ORDSTATUS.EQ_STATUS," +
			"MILES.EQ_TITLE, " +
			"MILES.EQ_ACTIONTAKEN, " +
			"MILES.EQ_STATUS ," +
			"MILES1.EQ_TITLE, " +
			"MILES1.EQ_ACTIONTAKEN, " +
			"MILES1.EQ_STATUS ," +
			"MILES1.MODIFICATIONDATE , " +
			"MILES1.EQ_COMPLETEDBY ,"+
			"MILES1.EQ_ACTUALDATE ,"+ 
			"MILES1.EQ_CREATEDBY "+
	" FROM "+ConnectionBean.getDbPrefix()+"EQMILESTONE MILES , "+ConnectionBean.getDbPrefix()+"EQMILESTONE MILES1, "+ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE ,"+ConnectionBean.getDbPrefix()+"EQ_ORDERSTATUS ORDSTATUS "+
	" WHERE "+
	" MILES.EQ_ORDERGID=QUOTE.TRIL_GID "+
	" AND MILES.EQ_ORDERGID = MILES1.EQ_ORDERGID "+
	" AND MILES.EQ_TITLE='Capture Technical Details' AND MILES.EQ_ACTIONTAKEN='Complete Task (SRF2 not required for this order)' "+
	" AND MILES1.EQ_TITLE='Deliver SRF2' AND MILES1.EQ_STATUS <> 'Closed' AND MILES1.EQ_ACTIONTAKEN IS null" +
	" AND QUOTE.EQ_WFSTATUS=  ORDSTATUS.TRIL_GID  AND ORDSTATUS.EQ_STATUS <> 'Cancelled' " +
	" ORDER BY QUOTE.QUOTENUMBER ";
	//" AND ROWNUM between 1 and 50";
	
	
	public DeliverSRF2Info(MLANViewComponents mlanViewComponents,String dirPath) throws Exception
	{
		this.mlanViewComponents  = mlanViewComponents;
		
		int randomNum = 999 + (int)(Math.random() * ((999999 - 999) + 1));
		pathCSV   =dirPath+File.separator+"_"+randomNum+".csv";
		pathSQL   =dirPath+File.separator+"_"+randomNum+".sql";
		pathRBSQL =dirPath+File.separator+"_"+randomNum+"_ROLLBACK.sql";
		
		goldExecutionScript          = new ArrayList<String>();
		goldExecutionrROLLBACKScript = new ArrayList<String>();
		
		mlanViewComponents.getQueryResult().append("Collecting Quote Details.\nThis Process may take few moments.Please Wait...\n");
		getQueryOrderDetails();
		mlanViewComponents.getQueryResult().append("Quotes details collection completed successfully.\n");
		
		if(null != detailedQueryResults && detailedQueryResults.size()>0)
		{
			mlanViewComponents.getQueryResult().append("Creating CSV file.\n");
			
			createCSV(pathCSV);
			
			mlanViewComponents.getQueryResult().append("CSV file created successfully."+pathCSV+"\n");
			
			mlanViewComponents.getQueryResult().append("*********************************************************\n");
			mlanViewComponents.getQueryResult().append("Creating SQL file.\n");
			
			createGOLDScriptFile(pathSQL);
			
			mlanViewComponents.getQueryResult().append("SQL file created successfully."+pathSQL+"\n");
			mlanViewComponents.getQueryResult().append("*********************************************************\n");
			
			mlanViewComponents.getQueryResult().append("*********************************************************\n");
			mlanViewComponents.getQueryResult().append("Creating SQL ROLLBACK SCRIPT file.\n");
			
			createGOLDRollBackScriptFile(pathRBSQL);
			
			mlanViewComponents.getQueryResult().append("SQL ROLLBACK file created successfully."+pathRBSQL+"\n");
			mlanViewComponents.getQueryResult().append("*********************************************************\n");

		
		}
		else
		{
		 JOptionPane.showMessageDialog(mlanViewComponents.getPanel(), "No Quote found to update for matching criteria.");	
		}
		mlanViewComponents.getQueryResult().append("Process completed successfully.\n");
		goldExecutionScript = null;
		goldExecutionrROLLBACKScript = null;
		detailedQueryResults = null;
		
	}
	
	private void getQueryOrderDetails() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		
		detailedQueryResults = CommonUtils.getQueryResult(DETAILEDQUERY, mlanViewComponents);
	    
		int count =0;
		int totalCount = detailedQueryResults.size();
		for(String[] orderdetails : detailedQueryResults)
		{
			count++;
			ProgressMonitorPane.getInstance().setProgress(count,(double)totalCount);
			
			String QuoteNumber = orderdetails[1];
			
			/*
			0=MILES1.TRIL_GID,
			1=QUOTE.QUOTENUMBER, 
			2=ORDSTATUS.EQ_STATUS, 
			3=MILES.EQ_TITLE, 
			4=MILES.EQ_ACTIONTAKEN, 
			5=MILES.EQ_STATUS ,
			6=MILES1.EQ_TITLE, 
			7=MILES1.EQ_ACTIONTAKEN, 
			8=MILES1.EQ_STATUS ,
			9=MILES1.MODIFICATIONDATE , 
			10=MILES1.EQ_COMPLETEDBY
			*/
			
			goldExecutionScript.add("--------------------------------------------------------------------------------------------------------" );
			goldExecutionScript.add("-- INFO : GOLD Script Starts "+CurrentDateTime.getDateTimeText()+ " for QUOTENUMBER:"+ QuoteNumber +" Record Number ("+(int)(count)+" from "+totalCount+")");
			goldExecutionScript.add("-- INFO : VALUES WILL BE CHANGE FROM  ");
			goldExecutionScript.add("-- 1. MODIFICATIONDATE >> '"+orderdetails[9]+"' TO SYSDATE ");
			goldExecutionScript.add("-- 2. EQ_STATUS        >> '"+orderdetails[8]+"' TO 'Closed' ");
			goldExecutionScript.add("-- 3. EQ_COMPLETEDBY   >> '"+orderdetails[10]+"' TO 'system generated' ");
			goldExecutionScript.add("-- 4. EQ_ACTIONTAKEN   >> '"+orderdetails[7]+"' TO 'SRF2 not required for this order' ");
			goldExecutionScript.add("-- 5. EQ_ACTUALDATE    >> '"+orderdetails[11]+"' TO 'SYSDATE' ");
			
			String updateSQL ="UPDATE "+ConnectionBean.getDbPrefix()+"EQMILESTONE MILES SET MODIFICATIONDATE = SYSDATE , EQ_STATUS='Closed' , MILES.EQ_COMPLETEDBY='system generated',MILES.EQ_ACTIONTAKEN='SRF2 not required for this order' , MILES.EQ_ACTUALDATE = SYSDATE WHERE MILES.TRIL_GID='"+orderdetails[0]+"' ;";
			goldExecutionScript.add(updateSQL);
			goldExecutionScript.add("-- INFO : GOLD Script ends for QUOTENUMBER:"+ QuoteNumber );
			
			/*
			 * THIS SECTION WILL CREATE ROLLBACK SCRIPTS
			 * 
			 */
			
			goldExecutionrROLLBACKScript.add("--------------------------------------------------------------------------------------------------------" );
			goldExecutionrROLLBACKScript.add("-- INFO : GOLD ROLLBACK Script Starts "+CurrentDateTime.getDateTimeText()+ " for QUOTENUMBER:"+ QuoteNumber +" Record Number ("+(int)(count)+" from "+totalCount+")");
			goldExecutionrROLLBACKScript.add("-- INFO : VALUES WILL BE CHANGE FROM  ");
			goldExecutionrROLLBACKScript.add("-- 1. MODIFICATIONDATE >> THIS WILL NOT BE ROLLBACK");
			goldExecutionrROLLBACKScript.add("-- 2. EQ_STATUS        >> 'Closed' TO '"+orderdetails[8]+"'");
			goldExecutionrROLLBACKScript.add("-- 3. EQ_COMPLETEDBY   >> 'system generated' TO '"+orderdetails[10]+"'");
			goldExecutionrROLLBACKScript.add("-- 4. EQ_ACTIONTAKEN   >> 'SRF2 not required for this order' TO '"+orderdetails[7]+"'");
			goldExecutionrROLLBACKScript.add("-- 5. EQ_ACTUALDATE    >> THIS WILL NOT BE ROLLBACK");
			
			String updateRBSQL ="UPDATE "+ConnectionBean.getDbPrefix()+"EQMILESTONE MILES SET MODIFICATIONDATE = SYSDATE , EQ_STATUS='"+(null == orderdetails[8]? "":orderdetails[8]) +"' , MILES.EQ_COMPLETEDBY='"+(null == orderdetails[10]?"":orderdetails[10])+"',MILES.EQ_ACTIONTAKEN='"+(null == orderdetails[7]?"":orderdetails[7])+"' WHERE MILES.TRIL_GID='"+orderdetails[0]+"' ;";
			goldExecutionrROLLBACKScript.add(updateRBSQL);
			goldExecutionrROLLBACKScript.add("-- INFO : GOLD ROLLBACK Script ends for QUOTENUMBER:"+ QuoteNumber );
			
			
			
		}
	    
	}
	
	private void createCSV(String pathCSV) throws IOException
	{
		String[] header = {"Deliver_SRF2.TRIL_GID","QUOTE.QUOTENUMBER", "ORDSTATUS.EQ_STATUS","MILES.EQ_TITLE", "MILES.EQ_ACTIONTAKEN", "MILES.EQ_STATUS" ,"Deliver_SRF2.EQ_TITLE", "Deliver_SRF2.EQ_ACTIONTAKEN", "Deliver_SRF2.EQ_STATUS" ,"Deliver_SRF2.MODIFICATIONDATE" , "Deliver_SRF2.EQ_COMPLETEDBY","Deliver_SRF2.EQ_ACTUALDATE","Deliver_SRF2.EQ_CREATEDBY"};
		CommonUtils.showTable(header, detailedQueryResults, pathCSV);
	}
	
	public void createGOLDScriptFile(String filePath) throws Exception{
		File fout = new File(filePath);
		FileOutputStream fos = new FileOutputStream(fout);
		
		goldExecutionScript = CommonUtils.setUmlaut(goldExecutionScript);
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		for(String script : goldExecutionScript){
			bw.write(script);
			bw.newLine();
		}
		bw.close();
		fos.close();
	
	}
	public void createGOLDRollBackScriptFile(String filePath) throws Exception{
		File fout = new File(filePath);
		FileOutputStream fos = new FileOutputStream(fout);
		
		goldExecutionrROLLBACKScript = CommonUtils.setUmlaut(goldExecutionrROLLBACKScript);
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		for(String script : goldExecutionrROLLBACKScript){
			bw.write(script);
			bw.newLine();
		}
		bw.close();
		fos.close();
	
	}
	
}
