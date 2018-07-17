package com.orange.util.cibase.v02;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.orange.ui.component.Imadaqv02ViewComponent;

public class UpdateImpactedOrderReport {

	private Imadaqv02ViewComponent parentComp;
	
	
	public UpdateImpactedOrderReport(Imadaqv02ViewComponent imadaqv02ViewComponent,String inputFileName,Map<String, ArrayList<String>> impactedOrderReportUpdateWithServiceErrorsMap,Map<String, ArrayList<String>> impactedOrderReportUpdateWithSiteErrorsMap) throws IOException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		this.parentComp = imadaqv02ViewComponent;
		
		
		int index =  inputFileName.lastIndexOf(File.separator);
		String location = inputFileName.substring(0,index) ;
		String fileNameToWork=location+File.separator+"USIDMigrationImpactedOrderReport.csv";
		
		location = null;
		
		if(!new File(fileNameToWork).exists())
		{
			parentComp.getQueryResult().append("\n************************************************************************************************************** ");
			parentComp.getQueryResult().append("\nfile not found. "+fileNameToWork);
			parentComp.getQueryResult().append("\n************************************************************************************************************** ");
		    return;	
		}
		
		CSVReader csvReader 	 = new CSVReader(new FileReader(fileNameToWork));
		ArrayList<String[]> fileUpdatedContent = new ArrayList<String[]>();
		
		/*
		 * Add Header
		 */
		fileUpdatedContent.add(csvReader.readNext());
		
		String row[];
		 while ((row = csvReader.readNext()) != null) 
			{
			   String baseUSID  = row[0];
			   
			   String status 		 = (null != row[3] && !row[3].isEmpty() && row[3].length()>0 ?row[3]:"");
			   String comments = (null != row[4] && !row[4].isEmpty() && row[4].length()>=0 ?row[4]:"");
			   
			   if((null == status || status.isEmpty() || status.length()==0) && (null == comments || comments.isEmpty() || comments.length()==0))
			   {
				  
				   /*
				    * CHECK FOR FOUND MULTIPLE SERVICES.
				    */
				     row[4] =addErrorForMultipleServices(baseUSID,impactedOrderReportUpdateWithServiceErrorsMap);
				     
				     /*
				      * CHECK FOR FOUND MULTIPLE SITES
				      * 
				      */
				     row[4] =row[4]+"  |  "+addErrorForMutilpleSites(baseUSID,impactedOrderReportUpdateWithSiteErrorsMap);
				     
			   }
			   fileUpdatedContent.add(row);
			}
		 csvReader.close();  
		 
		 CSVWriter csvWriter		 = new CSVWriter (new FileWriter (fileNameToWork));
		 csvWriter.writeAll(fileUpdatedContent);
		  csvWriter.close();  
		 fileUpdatedContent = null;
		 
		parentComp.getQueryResult().append("\nUpdated file created successfully. "+fileNameToWork);
		fileNameToWork= null;
	}
	
	private String addErrorForMultipleServices(String baseUSID,Map<String, ArrayList<String>>  impactedOrderReportUpdateWithServiceErrorsMap)
	{
		String error ="";
		 if(impactedOrderReportUpdateWithServiceErrorsMap.containsKey(baseUSID))
		   {
			   
		     ArrayList<String> values = impactedOrderReportUpdateWithServiceErrorsMap.get(baseUSID);
		     String S ="Target USID already exists in CSI with another product. >>";
		     for(String s : values)
		     {
		    	 S+=s+",";
		     }
		     return S;
		   }
		 return error;
	}
	
	private String addErrorForMutilpleSites(String baseUSID,Map<String, ArrayList<String>>  impactedOrderReportUpdateWithSiteErrorsMap)
	{
		String error ="";
		 if(impactedOrderReportUpdateWithSiteErrorsMap.containsKey(baseUSID))
		   {
			   
		     ArrayList<String> values = impactedOrderReportUpdateWithSiteErrorsMap.get(baseUSID);
		     String S ="USID is availabe on different sites >>";
		     for(String s : values)
		     {
		    	 S+=s+",";
		     }
		     return S;
		   }
		 return error;
	}

}
