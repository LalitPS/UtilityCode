package com.orange.util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.orange.ui.component.Imadaqv02ViewComponent;

public class CopyOfIOVUpdate {
	
	private Imadaqv02ViewComponent parentComp;
	private String inputFileName,outputFileName;
	
	private final int quoteindexinfile = 2;
	private final int ocatkeyindexinfile = 0;
	private ArrayList<String[]> parentDetails = new ArrayList<String[]>();
	
	private String IOVDETAILS = "SELECT DISTINCT " +
								CommonUtils.QUOTE_SERVICE_CONSIDERABLE_VALUE +
								"SERVICE.PSID AS SERVICEPSID,SERVICE.GRP_VERSION AS SERVICE_GRP_VERSION,SERVICE.OCATID AS SERVICE_OCAT_ID,"+
			                    "MIS.INSTALLED_OFFER_ID,MIS.INSTALLED_OFFER_VERSION_ID,LINEITEM.DESCRIPTION, LINEITEM.OCATID,LINEITEM.EQ_CIB_STABLE_ID , LINEITEM.PARENT,'OK' as GOLD_ANALYSIS " +
			                   // ",'ELE_OCATID','ELE_PARENT','ELE_SERVPROD'"+
			                    //",'COMP_OCATID','COMP_GRP_VERSION','COMP_PARTNUMBER','COMP_PARENTNAME','COMP_PARENT'"+
								" FROM "
		 						+ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE " +
		 						" LEFT JOIN "+ConnectionBean.getDbPrefix()+"SC_QUOTE_MISCELLANEOUS MIS ON (MIS.TRIL_GID = QUOTE.SC_QUOTE_MISCELLANEOUS) "+
		 						" LEFT JOIN "+ConnectionBean.getDbPrefix()+"SC_QUOTE_LINE_ITEM LINEITEM ON ( QUOTE.TRIL_GID = LINEITEM.QUOTE AND ((LINEITEM.NEW_CONFIG <> 'NULL' AND LINEITEM.NEW_CONFIG = ?) OR (LINEITEM.NEW_CONFIG IS NULL   AND LINEITEM.EXIST_CONFIG = ?)) ) "+
		 						//" LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID) "+
		 						","+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE " +
		 						" WHERE QUOTE.QUOTENUMBER=? " +
		 						//" AND QUOTE.ORDEREDSERVICE= SERVICE.TRIL_GID";
		 						" AND (QUOTE.ORDEREDSERVICE= SERVICE.TRIL_GID or QUOTE.ORDEREDSERVICE= SERVICE.SERVICE_ID)";
								
	
	public CopyOfIOVUpdate(Imadaqv02ViewComponent imadaqv02ViewComponent) throws IOException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		parentComp = imadaqv02ViewComponent;
		
		inputFileName = imadaqv02ViewComponent.getFileToValidate().getText();	
		int index = inputFileName.lastIndexOf(".");
		String sub = inputFileName.substring(0,index) ;
		outputFileName=sub+"_Updated.csv";
		
		insertIOVInfo(inputFileName,outputFileName);
		parentComp.getQueryResult().append("\nUpdated file created successfully. "+outputFileName);
	}
	
	private void insertIOVInfo(String readCsvFilePath,String writeCsvFilePath) throws IOException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		int totalrows = 0;
		CSVReader csvReaderT = new CSVReader(new FileReader(readCsvFilePath));
		while ((csvReaderT.readNext()) != null) 
		{
			totalrows++;
		}
		csvReaderT.close();
		
		CSVReader csvReader = new CSVReader(new FileReader(readCsvFilePath));
		CSVWriter csvWriter = new CSVWriter(new FileWriter(writeCsvFilePath));
		
		ArrayList<String[]> ciBaseHeader = CommonUtils.getQueryHeader(IOVDETAILS, parentComp,"","","");
		
		String newHeaderRow[] = addInRow(csvReader.readNext(),ciBaseHeader.get(0));
		 
		parentComp.getQueryResult().append("------------------------------------------------------------\n");
		parentComp.getQueryResult().append("GETTING QUOTE NUMBER FROM "+newHeaderRow[quoteindexinfile]+"\n");
		parentComp.getQueryResult().append("GETTING OCATID KEY   FROM "+newHeaderRow[ocatkeyindexinfile]+"\n");
		parentComp.getQueryResult().append("------------------------------------------------------------\n");
		
		csvWriter.writeNext(newHeaderRow);
		
		String[] csvRow;
		
		
		    int count = 0;
			while ((csvRow = csvReader.readNext()) != null) 
			{
				    count++;
					ProgressMonitorPane.getInstance().setProgress(count,(double)totalrows-1);
					String newWriterRow[] = new String[newHeaderRow.length];
				/*
				 * GET QUOTE NUMBER.
				 * IO AND IOV DETAILS WILL BE FATCHED AND ADDED IN LAST OF THE FILE
				 */
		       String quoteNumber       = 	CommonUtils.addZeroPrefixinOrder(csvRow[quoteindexinfile]);
		       String PRIMARYROUTERUSID = 	csvRow[ocatkeyindexinfile];
		       
		       
		       ArrayList<String[]> iovdetails = CommonUtils.getQueryResult(IOVDETAILS, parentComp,PRIMARYROUTERUSID,PRIMARYROUTERUSID,quoteNumber);
			   
		       if(null == iovdetails || iovdetails.size()==0)
		       {
		    	 String arr[]= {"QUOTE NOT FOUND"};
		    	 newWriterRow = addInRow(csvRow,arr);
		    	 csvWriter.writeNext(newWriterRow);
		       }
		      else if(iovdetails.size() >1)
		    	  
		      {
		    	   String S ="";
		    	     for(String[] ARR : iovdetails)
		    	     {
		    	    	 S+=ARR[6]+"|";
		    	     }
		    	     for(String[] ARR : iovdetails)
		    	     {
			    		 ARR[10] = "MULTIPLE FOUND"+S;
			    		 newWriterRow = addInRow(csvRow,ARR);
			    		 
			    		 if(null != iovdetails.get(0)[9])
			    		 {
				    		 ArrayList<String[]> PARENTiNFO = getElementHierarchy(iovdetails.get(0)[9]);
				    		 String[] ARRAY = getArrayFromList(PARENTiNFO);
				    		 parentDetails =  new ArrayList<String[]>();
				    		 newWriterRow = addInRow(newWriterRow,ARRAY);
				    	 }
				    	csvWriter.writeNext(newWriterRow);
				     }
			  }
		     
		       else
		       {
		       newWriterRow = addInRow(csvRow,iovdetails.get(0));
		       if(null != iovdetails.get(0)[9])
	    		 {
			       ArrayList<String[]> PARENTiNFO = getElementHierarchy(iovdetails.get(0)[9]);
			       String[] ARRAY = getArrayFromList(PARENTiNFO);
			       parentDetails =  new ArrayList<String[]>();
			     
		    	   newWriterRow = addInRow(newWriterRow,ARRAY);
		    	 }
		       csvWriter.writeNext(newWriterRow);
			   }
		    }
			
			csvWriter.close();
			csvReader.close();
			
			/*
			 * Removing UnNecessary or create new subfile and show.
			 * 
			 */
			//fileterCSVFile(writeCsvFilePath,writeCsvFilePath);
			CommonUtils.showOnTable(writeCsvFilePath);
	
	}
	
	private void fileterCSVFile(String fromFile , String toFile) throws IOException{
		
		CSVReader csvReader = new CSVReader(new FileReader(fromFile));
		CSVWriter csvWriter = new CSVWriter(new FileWriter(toFile));
		
	}
	private String[] addInRow(String[] prev,String[] addon)
	{
		String newHeaderRow[] = new String[prev.length+addon.length];
		
			for(int x = 0 ; x<prev.length;x++ )
			{
				newHeaderRow[x] = prev[x];
			}
	       for(int y = 0 ; y<addon.length;y++)
	       {
	    	   newHeaderRow[prev.length+y] = addon[y];
	       }
	       prev = null;
	       addon = null;
	       return newHeaderRow;
	}
	
	
	
	private ArrayList<String[]> getElementHierarchy(String parentTrilGid) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException{
		String QL = "SELECT OCATID,PARENT,Regexp_Substr (SERVPROD, '[^_]+', 1, '1') FROM "+ConnectionBean.getDbPrefix()+"SC_QUOTE_LINE_ITEM LINEITEM WHERE TRIL_GID=?"; 
		
		ArrayList<String[]> parentDetail = CommonUtils.getQueryResult(QL, parentComp,parentTrilGid);
		
		if(null == parentDetail || parentDetail.size() ==0 )
		{
			System.out.println("NO TRIL_GID FOUND...");
		}
		else if(parentDetail.size() >1 )
		{
			 System.out.println("MULTIPLE RECORDS FOUNDS...");
		}
		else
		{
			
			parentDetails.add(parentDetail.get(0));
		   if(null != parentDetail.get(0)[1] && !parentDetail.get(0)[1].isEmpty() && parentDetail.get(0)[1].length()>0)
		    {
		    	getElementHierarchy(parentDetail.get(0)[1]);
		    }
		    else{
		    	getComponentHierarchy(parentDetail.get(0)[2]);
		    }
		    
		}
		
		return parentDetails;
	}
	
	private ArrayList<String[]> getComponentHierarchy(String compTrilGid) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException{
		
		String QL = "SELECT OCATID,GRP_VERSION,PARTNUMBER,PARENTNAME,PARENT FROM "+ConnectionBean.getDbPrefix()+"EQ_L2ATTRIBUTE WHERE TRIL_GID=?"; 
		
		ArrayList<String[]> parentDetail = CommonUtils.getQueryResult(QL, parentComp,compTrilGid);
		
		if(null == parentDetail || parentDetail.size() ==0 )
		{
			
				System.out.println("NO TRIL_GID FOUND>>...");
		}
		else if(parentDetail.size() >1 )
		{
			 	System.out.println("MULTIPLE RECORDS FOUNDS>>...");
		}
		else
		{
			
			parentDetails.add(parentDetail.get(0));
		    if(null != parentDetail.get(0)[4] &&  !parentDetail.get(0)[4].isEmpty() && parentDetail.get(0)[4].length()>0)
		    {
		    	getComponentHierarchy(parentDetail.get(0)[4]);
		    }
		    
		}
		
		return parentDetails;
	}

  private String[] getArrayFromList(ArrayList<String[]> list)
  {
	  System.out.println(">>> "+list.size());
	  ArrayList<String> strList = new ArrayList<String>();
	  for(String[] ARR : list)
	  {
		  for(int X = 0 ; X< ARR.length;X++)
		  {
			  strList.add(ARR[X]);
		  }
	  }
	  return strList.toArray(new String[strList.size()]);
	 
  }
}
