package com.orange.util.cibase.v02;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.orange.ui.component.Imadaqv02ViewComponent;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.ConnectionBeanArchived;
import com.orange.util.ConnectionBeanCSI;
import com.orange.util.MultiLinerPanel;
import com.orange.util.ProgressMonitorPane;

public class DetailedAnalysis 
{
      private Map<String, ArrayList<String>> oldUSIDEmptyMap,targetUSIDEmptyMap;
      private Map<String, ArrayList<String>> orderNUSIDMap;
      private Map<String, ArrayList<String>> orderNUSIDMapForMigratedService;
      private Map<String, ArrayList<String>> orderNTypeMap;
      private Map<String, ArrayList<String>> targetOrderNTypeMap;
      private int ROWNUM                	 =CommonUtils.getMaxRecords();
      private String PASS_TEXT   		="<font color='#FF8C00'>OVERALL_PASS</font>";
      private String FAILED_TEXT 		="<font color='#FF8C00'>OVERALL_FAILED</font>";
      private Map<String,ArrayList<String>> impactedOrderReportUpdateWithServiceErrorsMap;
      private Map<String,ArrayList<String>> impactedOrderReportUpdateWithSiteErrorsMap;
     
      private String CSS ="<head><style type='text/css'>table.altrowstable {      font-family: verdana,arial,sans-serif;    font-size:11px;  color:#33333;     border-width: 1px;      border-color: #a9c6c9;  border-collapse: collapse;}table.altrowstable th {     border-width: 1px;      padding: 8px;      border-style: solid;    border-color: #a9c6c9;}table.altrowstable td {  border-width: 1px;  padding: 8px;     border-style: solid;    border-color:#a9c6c9;}.oddrowcolor{      background-color:#ffffff;}.evenrowcolor{  background-color:#ffffff;}</style></head>";
      
      private int orderOnMSCount=0;
     
      
            
      private String GOLDSQL = "SELECT DISTINCT QUOTE.QUOTENUMBER,"
            +CommonUtils.ORDER_TYPE_DECODE
            +CommonUtils.EQ_TYPE_DECODE +
            " ORG1.ORGANIZATIONID AS END_USER_ICO,ORG2.ORGANIZATIONID AS CONTRACTING_PARTY_ICO,"+
            " SITE.SITECODE, "+
            " CASE WHEN SERVICE.DISP_NAME <> 'NULL' THEN SERVICE.DISP_NAME  ELSE QUOTE.SERVICENAME END AS QUOTE_EXISITING_SERVICE "+
          
            " FROM "+
            ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE   "+
            "LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID) ,"+
            ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR  LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID) ,"+
            ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG1,"+
            ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG2,"+
            ConnectionBean.getDbPrefix()+"EQ_SITE SITE,"+
            ConnectionBean.getDbPrefix()+"SC_QUOTE_LINE_ITEM ATTR_LINEITEM " +
            " WHERE"+  
            " QUOTE.TRIL_GID = ATTR_LINEITEM.QUOTE  "+
                     
            " AND (ATTR_LINEITEM.NEW_CONFIG     = ? " +
            " OR  ATTR_LINEITEM.EXIST_CONFIG    = ? "+
            " OR  ATTR_LINEITEM.VALUE                 = ? )"+
            " AND QUOTE.EQ_DELIVERYPARTY          = ORG1.TRIL_GID"+
            " AND QUOTE.EQ_REQUESTINGPARTY     = ORG2.TRIL_GID"+
            " AND QUOTE.CONFIGURATIONS             = HR.TRIL_GID"+
            " AND ((QUOTE.HOTCUTNEWSITE <> 'NULL' AND QUOTE.HOTCUTNEWSITE =  SITE.TRIL_GID) " +
            " OR   (QUOTE.HOTCUTNEWSITE IS NULL AND QUOTE.SITE = SITE.TRIL_GID )) " +
            " AND ROWNUM between 1 and "+ROWNUM +
            " ORDER BY QUOTE.QUOTENUMBER";
      
      private String CSISQL=
    		" SELECT DISTINCT A.V_ORDHANDLE,A.V_ORDERTYPE ,'NA' AS TYPE,  A.V_ENDUSERHANDLE ,A.V_CUSTHANDLE , A.V_SITEHANDLE,A.V_SERVICEHANDLE "+
    		" FROM (SELECT V_ORDHANDLE,V_ORDERTYPE ,  V_ENDUSERHANDLE ,V_CUSTHANDLE , V_SITEHANDLE,V_SERVICEHANDLE "+
    	
  			" FROM "+ ConnectionBeanCSI.getDbPrefix()+"VIW_VERSION_ELEMENT_ATTRIBUTE V WHERE SE_USID = ? "+
  			" UNION "+
  			" SELECT V_ORDHANDLE,V_ORDERTYPE ,  V_ENDUSERHANDLE ,V_CUSTHANDLE , V_SITEHANDLE,V_SERVICEHANDLE "+
  			" FROM "+ ConnectionBeanCSI.getDbPrefix()+"VIW_VERSION_ELEMENT_ATTRIBUTE V WHERE T_VALUE=?) A ";
      /*
      private String CSISQL_PREV = "SELECT DISTINCT CVERSION.ORDHANDLE,"+
            " CVERSION.ORDERTYPE AS ORDER_TYPE,"+
            " 'NA' AS TYPE," +
            " CVERSION.ENDUSERHANDLE AS END_USER_ICO,CVERSION.CUSTHANDLE AS CONTRACTING_PARTY_ICO,"+
            " CVERSION.SITEHANDLE, "+
            " CVERSION.SERVICEHANDLE AS QUOTE_EXISITING_SERVICE"+
          
            " FROM "+
            ConnectionBeanCSI.getDbPrefix()+"CVERSION CVERSION ,"+
            ConnectionBeanCSI.getDbPrefix()+"CSERVICEELEMENT SERVICEELEMENT ,"+
            ConnectionBeanCSI.getDbPrefix()+"CVERSIONSERVICEELEMENT CVERSIONSERVICEELEMENT"+
            " WHERE"+  
            " SERVICEELEMENT.USID = ? " +
            " AND SERVICEELEMENT.SERVICEELEMENTID = CVERSIONSERVICEELEMENT.SERVICEELEMENTID " +
            " AND CVERSIONSERVICEELEMENT.VERSIONID = CVERSION.VERSIONID " +
            " AND ROWNUM between 1 and "+ROWNUM +
            " ORDER BY CVERSION.ORDHANDLE";
      */
      private String ARCHIVAL_SQL ="SELECT DISTINCT LINEITEM.ORDERNUMBER " +
                  " FROM " +
                  ConnectionBeanArchived.getDbPrefix()+"SC_QUOTE_LINE_ITEM_A LINEITEM " +
                  "WHERE " +
                  " (LINEITEM.NEW_CONFIG        = ? " +
                  " OR  LINEITEM.EXIST_CONFIG = ? "+
                  " OR  LINEITEM.VALUE          = ? )" +
                  " AND ROWNUM between 1 and "+ROWNUM+
                  " ORDER BY  LINEITEM.ORDERNUMBER";
      
      private String GETARCHIVEDORDERDETAILS = "SELECT DISTINCT QUOTE.QUOTENUMBER,"
            +CommonUtils.ORDER_TYPE_DECODE
            +CommonUtils.EQ_TYPE_DECODE +
            " ORG1.ORGANIZATIONID AS END_USER_ICO,ORG2.ORGANIZATIONID AS CONTRACTING_PARTY_ICO,"+
            " SITE.SITECODE, "+
            " CASE WHEN SERVICE.DISP_NAME <> 'NULL' THEN SERVICE.DISP_NAME  ELSE QUOTE.SERVICENAME END AS QUOTE_EXISITING_SERVICE "+
        
           // " CASE WHEN QUOTE.MIGRATIONSERVICENAME <> 'NULL' THEN QUOTE.MIGRATIONSERVICENAME  ELSE QUOTE.SERVICENAME END AS QUOTE_EXISITING_SERVICE "+
            " FROM "+
            ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE  "+
            "LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID) ,"+
            ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR  LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID) ,"+
            ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG1,"+
            ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG2,"+
            ConnectionBean.getDbPrefix()+"EQ_SITE SITE"+
            
            " WHERE"+  
            " QUOTE.EQ_DELIVERYPARTY = ORG1.TRIL_GID"+
            " AND QUOTE.EQ_REQUESTINGPARTY= ORG2.TRIL_GID"+
            " AND QUOTE.CONFIGURATIONS = HR.TRIL_GID"+
            " AND ((QUOTE.HOTCUTNEWSITE <> 'NULL' AND QUOTE.HOTCUTNEWSITE =  SITE.TRIL_GID) " +
            " OR   (QUOTE.HOTCUTNEWSITE IS NULL AND QUOTE.SITE = SITE.TRIL_GID )) " +
            " AND QUOTE.QUOTENUMBER = ? " +
            " AND ROWNUM between 1 and "+ROWNUM+
            " ORDER BY QUOTE.QUOTENUMBER";
      
      private int TOTAL_PASS		=0;
      private int TOTAL_FAILED	=0;
      
      public  DetailedAnalysis(Imadaqv02ViewComponent imadaqv02ViewComponent,ArrayList<File> files) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
      {
    	  	
            int totalrows = 0;
                       
            for(File F : files)
            {
            	   orderNUSIDMapForMigratedService =  new LinkedHashMap<String, ArrayList<String>>();

		            String      baseFileName = F.getAbsolutePath();
		            
		            MultiLinerPanel.getCheckBox(baseFileName).setForeground(Color.ORANGE);
		            
		            CSVReader csvReaderT = new CSVReader(new FileReader(baseFileName));
		            while ((csvReaderT.readNext()) != null) 
		            {
		                  totalrows++;
		            }
		            csvReaderT.close();
            
		           // ArrayList<String[]> allOrders = new ArrayList<String[]>();
                        /*
                        * 
                         *  READ THE FILE 
                         *  GET THE OK DATA,
                        *  GET THE EXISTING USID
                        */
            
                        String[] row = null;
            
                        ArrayList<String[]> writerRows = new ArrayList<String[]>();
                        int count = 0;
                        CSVReader br = new CSVReader(new FileReader(baseFileName));
                        
                        row = br.readNext();
                        
                        writerRows.add(addInRow(row, "DETAILED_ANALYSIS"));
                        
                        StringBuilder mainTableBuilder = new StringBuilder("<HTML>"+CSS+"\n<BODY>\n<TABLE class='altrowstable' id='alternatecolor' align='top'>\n");
                        mainTableBuilder.append("\n<TR><TD COLSPAN='2' ALIGN='LEFT'>Analysis Start Time : "+CommonUtils.getDateFormat()+"<font color='blue'><br>DATABASES USED : >> GOLD : "+ConnectionBean.getDbName()+" >> CSI :"+ConnectionBeanCSI.getDbName()+" >> ARCHIVAL :"+ConnectionBeanArchived.getDbName()+"</font><br>Please note , Orders highlighted with <font color='#2F4F4F'>Gray color </font>are impacted Order(s).</TH></TR>\n");
                        mainTableBuilder.append("\n<TR><TD COLSPAN='2'>\n<TABLE class='altrowstable' id='alternatecolor' align='top'>\n<TR><TD>TOTAL ANALYZED RECORDS</TD>\n<TD>TOTAL PASS RECORDS</TD>\n<TD>TOTAL FAILED RECORDS</TD>\n<TD>ORDERS_ON_MIGRATED_SERVICE</TD></TR>\n\n<TR><TD>TOTAL_RECORDS</TD>\n<TD>TOTAL_PASS_RECORDS</TD>\n<TD>TOTAL_FAILED_RECORDS</TD>\n<TD>ORDERS_ON_MIGRATED_SERVICE_LINK</TD></TR>\n</TABLE></TD></TR>\n");
                        
                        mainTableBuilder.append("\n<TR><TH>EXTRACT DATA </TH><TH>ANALYSIS</TH></TR>\n");
                        
                        impactedOrderReportUpdateWithServiceErrorsMap = new   LinkedHashMap<String,ArrayList<String>> ();
                        impactedOrderReportUpdateWithSiteErrorsMap    = new   LinkedHashMap<String,ArrayList<String>> ();
                        
                        while ((row = br.readNext()) != null) 
                        {   
                               getAnalysisProcess(row,mainTableBuilder,imadaqv02ViewComponent,writerRows,baseFileName);
                                count++;
                               ProgressMonitorPane.getInstance().setProgress(count,(double)totalrows-1);
                              
                        }
                        br.close();
                        
                        mainTableBuilder.append("<TR><TD COLSPAN='2'>Analysis Ends Time : "+CommonUtils.getDateFormat()+"</TD></TR></TABLE>\n</BODY>\n</HTML>\n");
                       
                        String path = baseFileName;   
                        int index = path.lastIndexOf(".");
                        String sub = path.substring(0,index) ;
                  
                        String HTMLFilePath=sub+"_Analysis.html";
                        String Order_migrated_service_HTMLFilePath=sub+"_Analysis_ORDER_SERVICE.html";
                        
                        File file = new File(HTMLFilePath);
                        File file1 = new File(Order_migrated_service_HTMLFilePath);
                        
                        
                        
                        String finalData = mainTableBuilder.toString().replace("TOTAL_RECORDS", ""+(totalrows-1));
                        finalData = finalData.replace("TOTAL_PASS_RECORDS", ""+TOTAL_PASS);
                        finalData = finalData.replace("TOTAL_FAILED_RECORDS", ""+TOTAL_FAILED);
                        
                        /*
                         * FIND ORDERS ON MIGRATED SERVICES : PPL LIST
                         * 
                         */
                       
                        ArrayList<String[]> ordersNServices = ordersOnMigratedService(imadaqv02ViewComponent);
                        if(null != ordersNServices && ordersNServices.size()>1)
                        {
                        finalData = finalData.replace("ORDERS_ON_MIGRATED_SERVICE_LINK", "<a href='./"+file1.getName()+"'>ORDERS_ON_MIGRATED_SERVICE_LINK</a>");
                        }
                        else
                        {
                        	finalData = finalData.replace("ORDERS_ON_MIGRATED_SERVICE_LINK","NO ORDER FOUND ON MIGRATED SERVICES");
                          
                        }
                        
                        BufferedWriter writer = null;
                        try {
                            writer = new BufferedWriter(new FileWriter(file));
                            writer.write(finalData);
                        } finally {
                            if (writer != null) writer.close();
                        }
                        
                        imadaqv02ViewComponent.getQueryResult().append("\nAnalysis file created successfully. "+HTMLFilePath);
                        
                        
                        finalData = null;
                        TOTAL_PASS = 0;
                        TOTAL_FAILED= 0;
                        totalrows = 0;
                        
                        
                        
                        CSVWriter bw = new CSVWriter(new FileWriter(sub+"_updated.csv"));
                        bw.writeAll(writerRows);
                        bw.close();
                        writerRows =  null;
                        imadaqv02ViewComponent.getQueryResult().append("\nfile updated successfully. "+sub+"_updated.csv");
                        
                        /*
                         * ORDERS ON MIGRATED SERVICES FILE SHOULD BE CREATED ONLY 
                         * IF ORDER/S FOUND ON MIGRATED SERVICE
                         */
                        if(null != ordersNServices && ordersNServices.size()>1)
                        {
                   
			                        StringBuilder orderAndServiceTableBuilder = new StringBuilder("<HTML>"+CSS+"\n<BODY>\n<TABLE class='altrowstable' id='alternatecolor' align='top'>\n");
			                        orderAndServiceTableBuilder.append("\n<TR><TD COLSPAN='2''><A HREF='./"+file.getName()+"' >BACK</A></TD><TD COLSPAN='10'>TOTAL ("+orderOnMSCount+") FOUND ON MIGRATED SERVICES.</TD></TR>\n");
			                        orderOnMSCount = 0;
			                        
			                        for(String[] arR : ordersNServices)
			                        {
			                           orderAndServiceTableBuilder.append("\n<TR>");
			                           for(String S : arR)
			                           {
			                        	   orderAndServiceTableBuilder.append("<TD>"+S+"</TD>\n");
			                           }
			                           orderAndServiceTableBuilder.append("\n</TR>");
			                        }
			                        orderAndServiceTableBuilder.append("<TABLE></BODY></HTML>");
			                        
			                         writer = null;
			                        try 
			                        {
			                            writer = new BufferedWriter(new FileWriter(file1));
			                            writer.write(orderAndServiceTableBuilder.toString());
			                        
			                        } finally {
			                            if (writer != null) writer.close();
			                        }
			                        imadaqv02ViewComponent.getQueryResult().append("\nAnalysis ORDERS AND SERVICE file created successfully. "+Order_migrated_service_HTMLFilePath);
			                        orderAndServiceTableBuilder = null;
			                        Order_migrated_service_HTMLFilePath = null;
                        }
                        ordersNServices =  null;
                        file  = null;
                        file1 = null;
                       
                        if(imadaqv02ViewComponent.getIsCiBaseFileCreate().isSelected())
                        {
                        	new CiBase_IOV_Update_Latest(imadaqv02ViewComponent,sub+"_updated.csv");
                        }
                        /*
                         * ADD SERVICE AND SITES CHECKS IN IMPACTED ORDER REPORT FILE.
                         * PLEASE ENSURE FILE MUST BE AVAILABE ON THE LOCATION.
                         * 
                         */
                        if(imadaqv02ViewComponent.getISUpdateImpactedOrderReport().isSelected())
                        {
                        	new UpdateImpactedOrderReport(imadaqv02ViewComponent,baseFileName,impactedOrderReportUpdateWithServiceErrorsMap,impactedOrderReportUpdateWithSiteErrorsMap);
                        }
                        
                        impactedOrderReportUpdateWithServiceErrorsMap = null;
                        impactedOrderReportUpdateWithSiteErrorsMap = null;
                        
                        MultiLinerPanel.getCheckBox(baseFileName).setSelected(false);  
                        MultiLinerPanel.getCheckBox(baseFileName).setForeground(Color.BLUE);  
            } // end of FOR loop     
           
      }
      
      private void getAnalysisProcess(String[] row, StringBuilder mainTableBuilder,Imadaqv02ViewComponent imadaqv02ViewComponent,ArrayList<String[]> writerRows,String baseFileName) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException{
    	  	
            boolean isMoreThenRownum = false;
           
            /*
            * GET THE OK DATA,
            * GET THE EXIST USID
            * 
             */
            String baseValue                    		= row[CiBaseConstants.existingValue_inFile];
            String targetValue                  		= row[CiBaseConstants.targetValue_inFile];
            String quoteValue 							= row[CiBaseConstants.quoteindexinfile];
            String serviceValue                		= row[CiBaseConstants.serviceNameIndex_inFile];
            String ensUserICOValue              	= row[CiBaseConstants.endUserICOIndex_inFile];
            String contractingPartyICOValue 	= row[CiBaseConstants.contractingPartyICOIndex_inFile];
            
         
            
            ArrayList<String[]> goldExisitingDetails  			= new ArrayList<String[]>();
            ArrayList<String[]> goldTargetDetails 				= new ArrayList<String[]>();
            ArrayList<String[]> csiExistingDetails 				= new ArrayList<String[]>();
            ArrayList<String[]> csiTargetDetails 				= new ArrayList<String[]>();
            ArrayList<String[]> archivalExistingDetails 	= new ArrayList<String[]>();
            ArrayList<String[]> archivalTargetDetails 		= new ArrayList<String[]>();
            
           
                      
		          
			           goldExisitingDetails  		=CommonUtils.getQueryResult(GOLDSQL,imadaqv02ViewComponent,baseValue,baseValue,baseValue);
			           goldTargetDetails      	    =CommonUtils.getQueryResult(GOLDSQL,imadaqv02ViewComponent,targetValue,targetValue,targetValue);
		           
			            csiExistingDetails 			= CommonUtils.getCSIQueryResult(CSISQL,imadaqv02ViewComponent,baseValue,baseValue);
			            csiTargetDetails   			= CommonUtils.getCSIQueryResult(CSISQL,imadaqv02ViewComponent,targetValue,targetValue);
		           
		            /*
		            * below query will give only orders
		            */
		           
			            archivalExistingDetails   = CommonUtils.getArchiveQueryResult(ARCHIVAL_SQL,imadaqv02ViewComponent,baseValue,baseValue,baseValue);
			            archivalTargetDetails     = CommonUtils.getArchiveQueryResult(ARCHIVAL_SQL,imadaqv02ViewComponent,targetValue,targetValue,targetValue);
		           
            
           
      
            
            /*
            * now these list of archival order details will be get from GOLD.
            */
            ArrayList<String[]> goldExistingDetailsForArchive = new ArrayList<String[]>();
            for(String[] archiveorder : archivalExistingDetails)
            {
                  
            ArrayList<String[]> goldDetails =CommonUtils.getQueryResult(GETARCHIVEDORDERDETAILS,imadaqv02ViewComponent,archiveorder[0]);
            goldExistingDetailsForArchive.addAll(goldDetails);
            }
            
            ArrayList<String[]> goldTargetDetailsForArchive = new ArrayList<String[]>();
            for(String[] archiveorder : archivalTargetDetails)
            {
                  
            ArrayList<String[]> goldDetails =CommonUtils.getQueryResult(GETARCHIVEDORDERDETAILS,imadaqv02ViewComponent,archiveorder[0]);
            goldTargetDetailsForArchive.addAll(goldDetails);
            }
            
            if(  goldExisitingDetails.size()>=ROWNUM || goldTargetDetails.size()>=ROWNUM
                    ||csiExistingDetails.size()>=ROWNUM || csiTargetDetails.size()>=ROWNUM 
                    ||archivalExistingDetails.size()>=ROWNUM || archivalTargetDetails.size()>=ROWNUM )
              {
                    isMoreThenRownum = true;
              }
            
            mainTableBuilder.append("\n<TR>");
            mainTableBuilder.append("\n<TD>");
            
            StringBuilder subDataTableBuilder = new StringBuilder("<TABLE class='altrowstable' id='alternatecolor'>");
            subDataTableBuilder.append("\n<TR><TD colspan='1000'><FONT COLOR='BLUE'>"+baseValue+"</FONT></TD></TR>");
            
            /*
            * Initialize the Map to check 
             * Order contains both the existing and Target USID.
            */
            
            oldUSIDEmptyMap				= new LinkedHashMap<String, ArrayList<String>>();
            targetUSIDEmptyMap			= new LinkedHashMap<String, ArrayList<String>>();
            orderNUSIDMap					= new LinkedHashMap<String, ArrayList<String>>();
            orderNTypeMap					= new LinkedHashMap<String, ArrayList<String>>();
            targetOrderNTypeMap 		= new LinkedHashMap<String, ArrayList<String>>();
            
        	
	            /*
	             * GOLD CHECKS
	             */
	            subDataTableBuilder = getTableData(quoteValue,"GOLD",baseValue,  goldExisitingDetails,subDataTableBuilder,true,false);
	         
	            subDataTableBuilder = getTableData(quoteValue,"GOLD",targetValue,goldTargetDetails,subDataTableBuilder,false,true);
	            /*
	             * CSI CHECKS
	             */
	            subDataTableBuilder = getTableData(quoteValue,"CSI",baseValue,csiExistingDetails,subDataTableBuilder,false,false);
	            subDataTableBuilder = getTableData(quoteValue,"CSI",targetValue,csiTargetDetails,subDataTableBuilder,false,true);
	            /*
	             * ARCHIVAL CHECKS
	             */
	            subDataTableBuilder = getTableData(quoteValue,"ARCHIVE",baseValue,goldExistingDetailsForArchive,subDataTableBuilder,false,false);
	            subDataTableBuilder = getTableData(quoteValue,"ARCHIVE",targetValue,goldTargetDetailsForArchive,subDataTableBuilder,false,true);
             
	          
            subDataTableBuilder.append("\n</TABLE>\n");
            mainTableBuilder.append(subDataTableBuilder.toString()+"\n</TD>");
            subDataTableBuilder = null;
            mainTableBuilder.append("\n<TD VALIGN='TOP'>");
            subDataTableBuilder = new StringBuilder("<TABLE class='altrowstable' id='alternatecolor'>");
            
            if(isMoreThenRownum)
            {
                  subDataTableBuilder.append("\n<TR><TD VALIGN='TOP' colspan='1000'>Analysis for <font color='#FF8C00'><b><u>first ("+ROWNUM+ ") found records,</u></b></font> '"+baseValue+"' and '"+targetValue+"'</TD></TR>");  
            }
            else
            {
                  subDataTableBuilder.append("\n<TR><TD VALIGN='TOP' colspan='1000'>Analysis for '"+baseValue+"' and '"+targetValue+"'</TD></TR>");      
            }
            
            subDataTableBuilder.append("\n<TR><TD>END USER ICO</TD><TD>CONTRACTING PARTY ICO</TD><TD>SITE</TD><TD>SERVICE</TD><TD>OVERALL</TD></TR>");
            
            LinkedHashSet<String> endUserICOData                  	= getConsolidateData(ensUserICOValue,3,goldExisitingDetails,goldTargetDetails,csiExistingDetails,csiTargetDetails,goldExistingDetailsForArchive,goldTargetDetailsForArchive);
            LinkedHashSet<String> contrattingPartyICOData   	= getConsolidateData(contractingPartyICOValue,4,goldExisitingDetails,goldTargetDetails,csiExistingDetails,csiTargetDetails,goldExistingDetailsForArchive,goldTargetDetailsForArchive);
            LinkedHashSet<String> siteData                             		= getConsolidateData("",5,goldExisitingDetails,goldTargetDetails,csiExistingDetails,csiTargetDetails,goldExistingDetailsForArchive,goldTargetDetailsForArchive);
            LinkedHashSet<String> analysisQuotesData       		= getConsolidateData("",0,goldExisitingDetails,goldTargetDetails,csiExistingDetails,csiTargetDetails,goldExistingDetailsForArchive,goldTargetDetailsForArchive);
            
            /*
             * SERVICE CHECK FOR CSI DATA : NOT FOR GOLD AND ARCHIVAL
             * 
             * THE BELOW COMMENT CHECKS SERCVICE FROM GOLD,ARCHIVAL AND CSI AS WELL SERVICE FROM INPUT FILE.
             * (AS OF NOW THIS METOD IS COMMENTED)
             * 
             * INSTEAD OF COMMENT METHOD , WE CHECK ONLY SERVICES FROM CSI DATA.
             */
            
            //LinkedHashSet<String> serviceData                    	= getConsolidateData(serviceValue,6,goldExisitingDetails,goldTargetDetails,csiExistingDetails,csiTargetDetails,goldExistingDetailsForArchive,goldTargetDetailsForArchive);
            LinkedHashSet<String> serviceData                    		= getConsolidateData("",6,null,null,csiExistingDetails,csiTargetDetails,null,null);
            
           
            serviceData = USIDValidations.syncService(serviceData);
            ArrayList<String> ar = new ArrayList<String>();
            ar.addAll(serviceData);
            
            /*
             * get the services details from CSI only for EU and TU
             * 
             */
            if(ar.size()>1)
            {
            	    // serviceData.add("<FONT COLOR='BLUE'>Target USID already exists in CSI with another product.</FONT>");
                	 addValueInMap(impactedOrderReportUpdateWithServiceErrorsMap,baseValue,ar);
            }
          
            
            
            
            siteData = USIDValidations.syncService(siteData);
            ArrayList<String> sitear = new ArrayList<String>();
            sitear.addAll(siteData);
            addValueInMap(impactedOrderReportUpdateWithSiteErrorsMap,baseValue,sitear);
           
            subDataTableBuilder.append("\n<TR>");
            boolean isAnyFailed = false;
            
            isAnyFailed 							= checkUniquness(isAnyFailed,subDataTableBuilder,endUserICOData);
            isAnyFailed 							= checkUniquness(isAnyFailed,subDataTableBuilder,contrattingPartyICOData);
            isAnyFailed 							= checkUniquness(isAnyFailed,subDataTableBuilder,siteData);
            isAnyFailed 							= checkUniquness(isAnyFailed,subDataTableBuilder,serviceData);
           
            if(!isAnyFailed)
            {
                  subDataTableBuilder.append("\n<TD>"+PASS_TEXT+"</TD>");
            }
            else
            {
            	
                  subDataTableBuilder.append("\n<TD>"+FAILED_TEXT+"</TD>");
            }
            
            
            subDataTableBuilder.append("\n</TR>");
            
            USIDValidations usidValidations = new USIDValidations();
            
							            /*
										  * quote not found in GOLD or ARCHIVE. 
										  */
							             usidValidations.isListedOrderAvailable(quoteValue,baseValue,targetValue,imadaqv02ViewComponent,subDataTableBuilder);
             
            							/*
            							 * Order contains both the USIDs
            							 * This check only with GOLD and Archival Data Base data
            							 */
             							 usidValidations.getUSIDValidation1(orderNUSIDMap, subDataTableBuilder);
             							 
             							 
             							 /*
             							  * Existing USID exists on different new order
             							  */
             							 //usidValidations.getUSIDValidation2(orderNTypeMap, subDataTableBuilder);
             							 
             							
             							 
             							 /*
             							  * Target USID is already present on different new order
             							  * If only one new order found. : Check the Order Chain.
             							  */
             							 usidValidations.getUSIDValidation3(targetOrderNTypeMap, subDataTableBuilder, analysisQuotesData, imadaqv02ViewComponent);
             							
             							 
             							 /*
             							  * Target USID already exists in CSI with another product.
             							  */
             							 usidValidations.getUSIDValidation4(serviceData, subDataTableBuilder);
             							 
             							  /*
             		                      * The Target USID ServiceElement belongs to different Service element categories
             		                      */
             		                     usidValidations.isSameServiceElementClass(baseValue,targetValue,imadaqv02ViewComponent,subDataTableBuilder);
                  
          
          /*
           * don't change the status of the boolean parameter if its status is being 'true' by previous validations.
           * Previous validation are : site ,service and ICO
           */
           if(!isAnyFailed) 
           {
        	   isAnyFailed = usidValidations.isAnyFailed();
           }
         
         
        
            /*
            * --------------
            * GEBERIC-COMMON CHECKS
            * --------------
            * Target USID OR old USID is empty. 
             * 
             */
            
            Iterator<Map.Entry<String, ArrayList<String>>> entries  = oldUSIDEmptyMap.entrySet().iterator();
            while (entries.hasNext()) 
            {
                  isAnyFailed = true;
                  Map.Entry<String, ArrayList<String>> entry = entries.next();
                  String key = entry.getKey();
                  subDataTableBuilder.append("\n<TR><TD COLSPAN='100'><font color='#FF8C00'>"+key+"</font></TD></TR>\n");
                        
            }
            entries = null;
            oldUSIDEmptyMap = null; 
            
            entries = targetUSIDEmptyMap.entrySet().iterator();
            while (entries.hasNext()) 
            {
                  isAnyFailed = true;
                  Map.Entry<String, ArrayList<String>> entry = entries.next();
                  String key = entry.getKey();
                  subDataTableBuilder.append("\n<TR><TD COLSPAN='100'><font color='#FF8C00'>"+key+"</font></TD></TR>\n");
                        
            }
            entries = null;
            targetUSIDEmptyMap = null;    
            
            String S = subDataTableBuilder.toString();
            
       
            if(isAnyFailed)
            {
            	 TOTAL_FAILED ++;
                  writerRows.add(addInRow(row,CiBaseConstants.GOLDFAILEDFilterMatch));
                  S = S.replace(PASS_TEXT, "<FONT COLOR='red'>"+CiBaseConstants.GOLDFAILEDFilterMatch+"</FONT>");
                  S = S.replace(FAILED_TEXT, "<FONT COLOR='red'>"+CiBaseConstants.GOLDFAILEDFilterMatch+"</FONT>");
            }
            else
            {
            	 TOTAL_PASS ++;
                  writerRows.add(addInRow(row,CiBaseConstants.GOLDPassFilterMatch));
                  S = S.replace(PASS_TEXT, "<FONT COLOR='#FF8C00'>"+CiBaseConstants.GOLDPassFilterMatch+"</FONT>");
                  S = S.replace(FAILED_TEXT, "<FONT COLOR='#FF8C00'>"+CiBaseConstants.GOLDPassFilterMatch+"</FONT>");
            }
            S = S+"\n</TABLE>\n";
         
            mainTableBuilder.append(S+"\n</TD>");
            subDataTableBuilder = null;
            S = null;
            isAnyFailed = false;
            mainTableBuilder.append("\n</TR>");
            
      }
      
      private void addValueInMap(Map<String,ArrayList<String>> fileReadMap,String key,ArrayList<String> value)
  	{
  		if(fileReadMap.containsKey(key))
  		{
  			ArrayList<String> values = fileReadMap.get(key);
  			values.addAll(value);
  			fileReadMap.put(key,values);
  		}
  		else
  		{
  				fileReadMap.put(key,value);
  		}
  	}
      private boolean checkUniquness(boolean isAnyFailed,StringBuilder subDataTableBuilder,LinkedHashSet<String> endUserICOData){
            if(endUserICOData.size()<=1)
            {
                  subDataTableBuilder.append("\n<TD>"+CiBaseConstants.GOLDPassFilterMatch+"</TD>");
            }
            else
            {
                  String toolTip="<ul>\n";
                  for (String o : endUserICOData) 
                  {
                        toolTip+="<li><font color='#2F4F4F'>"+o+"</font></li>\n";
                }
                  toolTip+="</ul>\n";
                  subDataTableBuilder.append("\n<TD VALIGN='TOP'><font color='red'>"+CiBaseConstants.GOLDFAILEDFilterMatch+"</font>"+toolTip+"</TD>");
                  isAnyFailed = true;
            }
            return isAnyFailed;
      }
      
      private StringBuilder getTableData(String quote,String source,String sourcevalue,ArrayList<String[]> data,StringBuilder builder,boolean isHeaderRequired, boolean isTargetUSIDCheck){
    	  
    	  	
            if(isHeaderRequired)
            {
                  // ADD TABLE HEADER 
                  String[] headers = {"SOURCE","BASE VALUE","QUOTENUMBER","ORDER_TYPE","TYPE","END_USER_ICO","CONTRACTING_PARTY_ICO","SITECODE","QUOTE_EXISITING_SERVICE"};
                  builder.append("\n<TR>");
                  for(String header : headers)
                  {
                        builder.append("\n<TH>"+header+"</TH>\n");
                  }
                  builder.append("\n</TR>");
                  
            }
            
            // ADD TABLE DATA
            
            if(isTargetUSIDCheck && (null == sourcevalue || sourcevalue.length()==0 || sourcevalue.isEmpty())){
                  targetUSIDEmptyMap.put("Target USID is not correct or is Empty.",null);
            }
            if(!isTargetUSIDCheck && (null == sourcevalue || sourcevalue.length()==0 || sourcevalue.isEmpty())){
                  oldUSIDEmptyMap.put("Old USID is not correct or is Empty.",null);
            }
            
           for(int row = 0; row<data.size();row++)
            {
                  builder.append("\n<TR><TD>"+source+"</TD><TD>"+sourcevalue+"</TD>");
                  int count = 0;
                  for(String datarow : data.get(row))
                  {
                        if(!isTargetUSIDCheck && count==0)
                        {
                              /*
                              * TO HEIGHLIGHT IMPACTED ORDERS GRAY AND BOLD.
                              */
                              builder.append("\n<TD><b><font color='#2F4F4F'>"+datarow+"</font></b></TD>\n");
                        }
                        else
                        {
                              builder.append("\n<TD>"+datarow+"</TD>\n");
                        }
                        
                        count++;
                  }
                  
                  addValueInOrderUSIDMap(source,data.get(row)[0],sourcevalue);
                  
                  if(null != data.get(row)[1] && data.get(row)[1].trim().equalsIgnoreCase("NEW"))
                  {
                        addValueInOrderTypeMap(data.get(row)[1],data.get(row)[0]);
                  }
                  
                  if(isTargetUSIDCheck && null != data.get(row)[1] && data.get(row)[1].trim().equalsIgnoreCase("NEW"))
                  {
                        addValueInTargetOrderTypeMap(data.get(row)[1],data.get(row)[0]);
                  }
                  
                  builder.append("\n</TR>");
                 
            }
        
           
      
            
            return builder;
      }
      
      private void addValueInOrderUSIDMap(String source,String key,String value){
          
		    	  if(orderNUSIDMapForMigratedService.containsKey(key))
		          {
		                ArrayList<String> existing = orderNUSIDMapForMigratedService.get(key);
		                existing.add(value);
		                orderNUSIDMapForMigratedService.put(key, existing);
		          }
		          else{
		                ArrayList<String> existing = new ArrayList<String>();
		                existing.add(value);
		                orderNUSIDMapForMigratedService.put(key, existing);
		          }
          
		    /*
		     * PUT VALUES IN THIS MAP ONLY FOR GOLD AND ARCHIVAL : NOT CHECK FOR CSI DATA	  
		     */
		    	  if(source!="CSI")
		    	  {
			            if(orderNUSIDMap.containsKey(key))
			            {
			                  ArrayList<String> existing = orderNUSIDMap.get(key);
			                  existing.add(value);
			                  orderNUSIDMap.put(key, existing);
			            }
			            else{
			                  ArrayList<String> existing = new ArrayList<String>();
			                  existing.add(value);
			                  orderNUSIDMap.put(key, existing);
			            }
		    	  }
      }
 
   
private void addValueInOrderTypeMap(String key,String value){
            
            if(orderNTypeMap.containsKey(key))
            {
                  ArrayList<String> existing = orderNTypeMap.get(key);
                  existing.add(value);
                  orderNTypeMap.put(key, existing);
            }
            else{
                  ArrayList<String> existing = new ArrayList<String>();
                  existing.add(value);
                  orderNTypeMap.put(key, existing);
            }
      }
      
private void addValueInTargetOrderTypeMap(String key,String value){
      
      if(targetOrderNTypeMap.containsKey(key))
      {
            ArrayList<String> existing = targetOrderNTypeMap.get(key);
            existing.add(value);
            targetOrderNTypeMap.put(key, existing);
            
      }
      else{
            ArrayList<String> existing = new ArrayList<String>();
            existing.add(value);
            targetOrderNTypeMap.put(key, existing);
            
      }
}

     private LinkedHashSet<String> getConsolidateData(String fileValue,int index,ArrayList<String[]>...arrayLists)
      {
      
      ArrayList<String> data = new ArrayList<String>();
     
	      for(ArrayList<String[]> lists : arrayLists)
	      { 
	    	  if(null !=lists)
	          {
		             for(String[] list : lists)
		            {
		                        if(null != list && list.length >= index  && null != list[index])
		                        {
		                        	 String ord = list[index];
		                        	 data.add(ord);
		                              
		                         }
		            }
	            }
	      }
      
      if(null != fileValue && fileValue.length()>0)
      {
    	  data.add(fileValue);
      }
      LinkedHashSet<String> set = new LinkedHashSet<String>(data);
      data = null;
      return set;
      }
     
	
    
      
      private String[] addInRow(String[] row, String newData)
      {
            String []writeRow = new String[row.length+1];
            for(int X = 0 ; X <row.length;X++)
            {
                  writeRow[X]= row[X];
            }
            writeRow[row.length]= newData;
            return writeRow;
      }
      private ArrayList<String> putIn999(ArrayList<String> quoteNumbers)
  	{
  		
  		ArrayList<String> inMax999 = new ArrayList<String>();
  		
  		if(null != quoteNumbers  && quoteNumbers.size()>999)
  		{
  			int Y = 0;
  			String data = "";
  			for(int x = Y ; x<quoteNumbers.size(); x++)
  			{
  				Y++;
  				data+="'"+quoteNumbers.get(x)+"',";
  				if(Y>=999)
  				{
  					// remove last ,
  					data = data.substring(0,data.length()-1);
  					inMax999.add(data);
  					data = "";
  					Y = 0;
  				}
  				
  			}
  			
  			
  		}
  		else
  		{
  			String data = "";
  			for(int x = 0 ; x<quoteNumbers.size(); x++)
  			{
  				data+="'"+quoteNumbers.get(x)+"',";
  			}
  			// remove last ,
  			if(null != data && data.length()>1)
  			{
  			data = data.substring(0,data.length()-1);
  			inMax999.add(data);
  			}
  			data = "";
  		}
  		return inMax999;
  		
  	}
      private ArrayList<String[]> ordersOnMigratedService(Imadaqv02ViewComponent imadaqv02ViewComponent) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException
      {
    	  ArrayList<String[]> results = new  ArrayList<String[]> ();
    	  String[] header = {"QUOTENUMBER","QUOTE_IO","QUOTE_IOV","SERVICENAME","MIGRATION_SERVICENAME","IS_FOUND_ON_MIGRATED_SERVICE_LIST","USID",};
    	  results.add(header);
    	  
    	  ArrayList<String> pplList = new ArrayList<String>(Arrays.asList(CommonUtils.PPL_LIST));
    	  
    	  /*
    	   * Get All Orders from OrderandUsidMap.
    	   * 
    	   */
    	  ArrayList<String> allOrders = new ArrayList<String>();
    	  
    	  Iterator<Map.Entry<String, ArrayList<String>>> entries = orderNUSIDMapForMigratedService.entrySet().iterator();
          while (entries.hasNext()) 
          {
                Map.Entry<String, ArrayList<String>> entry = entries.next();
                String key = entry.getKey();
                allOrders.add(key);
          }
          
    	  for(String INorders : putIn999(allOrders))
    	  {
    	  String lql = "SELECT QUOTE.QUOTENUMBER,MIS.INSTALLED_OFFER_ID AS QUOTE_IO ,MIS.INSTALLED_OFFER_VERSION_ID AS QUOTE_IOV,QUOTE.SERVICENAME ,SERVICE.DISP_NAME AS MIGRATIONSERVICENAME,  'not_on_migrated_service' as ON_MIGARATED_SERVICE ,'' as USID "+
    	  		 " FROM        "+ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE "+
    	  		 " LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID) "+
    	  		 " LEFT JOIN "+ConnectionBean.getDbPrefix()+"SC_QUOTE_MISCELLANEOUS MIS ON (MIS.TRIL_GID = QUOTE.SC_QUOTE_MISCELLANEOUS) "+
    	  		"  WHERE QUOTENUMBER IN ("+ INorders+") "+
    	  	    "  AND (MIS.INSTALLED_OFFER_ID <> 'NULL' OR MIS.INSTALLED_OFFER_VERSION_ID <> 'NULL') "; 
    	  
          ArrayList<String[]> result = CommonUtils.getQueryResult(lql, imadaqv02ViewComponent) ;
         
          
          for(String[] ARR : result)
          {
    	  		if(null != ARR && ARR.length>0 && (pplList.contains(ARR[3]) || pplList.contains(ARR[4])))
    			  {
    		  		ARR[5] = "<FONT COLOR='RED'>ON_MIGRATED_SERVICE</FONT>";
    		  		orderOnMSCount++;
    		  	   
    	  		
    		  			ArrayList<String> value = orderNUSIDMapForMigratedService.get(ARR[0]);
				  		Set<String> usids = new HashSet<String>();
			            usids.addAll(value);
			            value.clear();
			            value.addAll(usids);
			            String TXT = "";
				            for(String USID: value)
				            {
				            	TXT+=USID+",";
				            }
				            ARR[6] =  TXT;
	          		
          
				            results.add(ARR);
    		
		    		        TXT = null;
		    		  		value.clear();
		    		  		value = null;
		    		  		usids.clear();
		    		  		usids = null;
          }
    	  }
    	  result = null;
    	  }
    	  allOrders.clear();
    	  allOrders = null;
    	  pplList = null;
    	  header = null;
    	  orderNUSIDMapForMigratedService = null;
    	
    	  return results;
      }
      
     
}

