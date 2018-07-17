package com.orange.util.cibase.v02;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.orange.ui.component.Imadaqv02ViewComponent;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.ProgressMonitorPane;

public class CiBase_IOV_Update {
	
	private Imadaqv02ViewComponent parentComp;
	private String inputFileName,outputFileName;
	private String newHeaderRow[];
	
	private String IOV_DETAILS_REVERSE_4_ATTRIBUTE = "SELECT DISTINCT " +
								"CASE WHEN ATTR_LINEITEM.PARENT IS NULL THEN 'ELEMENT' ELSE 'ATTRIBUTE' END AS IS_ATTRIBUTE,"+
								CommonUtils.QUOTE_SERVICE_CONSIDERABLE_VALUE +
								" MIS.INSTALLED_OFFER_ID AS IO ,MIS.INSTALLED_OFFER_VERSION_ID AS IOV ,"+
								//"--SERVICE "+
								" SERVICE.DISP_NAME AS SERVICE_NAME, SERVICE.OCATID AS SERVICE_OCATID, SERVICE.GRP_VERSION AS SERVICE_GRPVERSION,"+
								//" --COMPONENT"+
								" L2ATTRIBUTE2.DESCRIPTION AS COMPONENT_NAME,L2ATTRIBUTE2.OCATID AS COMPONENT_OCATID,L2ATTRIBUTE2.GRP_VERSION AS COMPONENT_GRPVERSION,"+
								//" --ELEMENT"+
								" ELE_LINEITEM.DESCRIPTION AS ELEMENT_NAME,ELE_LINEITEM.OCATID AS ELEMENT_OCATID ,ELE_LINEITEM.EQ_CIB_VERSION_ID AS ELEMENT_EQ_CIB_VERSION_ID ,ELE_LINEITEM.EQ_CIB_STABLE_ID AS ELEMENT_EQ_CIB_STABLE_ID ,"+
								//" --ATTRIBUTE"+
								
								" ATTR_LINEITEM.DESCRIPTION AS ATTRIBUTE_NAME ,ATTR_LINEITEM.OCATID AS ATTRIBUTE_OCATID ,ATTR_LINEITEM.EQ_CIB_VERSION_ID AS ATTRIBUTE_EQ_CIB_VERSION_ID ,ATTR_LINEITEM.EQ_CIB_STABLE_ID AS ATTRIBUTE_EQ_CIB_STABLE_ID "+ 
								
								
								", 'OK' AS GOLD_STATUS , '' AS GOLD_STATUS_COMMENTS "+
								" FROM "+
								ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE  "+
								" LEFT JOIN "+ConnectionBean.getDbPrefix()+"SC_QUOTE_MISCELLANEOUS MIS ON (MIS.TRIL_GID = QUOTE.SC_QUOTE_MISCELLANEOUS) ,"+
								ConnectionBean.getDbPrefix()+"SC_QUOTE_LINE_ITEM ATTR_LINEITEM, "+
								ConnectionBean.getDbPrefix()+"SC_QUOTE_LINE_ITEM ELE_LINEITEM, "+
								ConnectionBean.getDbPrefix()+"EQ_L2ATTRIBUTECOL L2ATTRIBUTECOL, "+
								ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE, "+
								ConnectionBean.getDbPrefix()+"EQ_L2ATTRIBUTE L2ATTRIBUTE1, "+
								ConnectionBean.getDbPrefix()+"EQ_L2ATTRIBUTE L2ATTRIBUTE2 "+
								" WHERE "+
								" QUOTE.QUOTENUMBER = ?"+
								" AND QUOTE.TRIL_GID = ATTR_LINEITEM.QUOTE "+ 
								" AND ((ATTR_LINEITEM.NEW_CONFIG <> 'NULL' AND ATTR_LINEITEM.NEW_CONFIG = ?) OR (ATTR_LINEITEM.NEW_CONFIG IS NULL   AND ATTR_LINEITEM.EXIST_CONFIG = ?)) "+
								"AND( (ATTR_LINEITEM.PARENT <> 'NULL' AND ATTR_LINEITEM.PARENT = ELE_LINEITEM.TRIL_GID) "+
								"		OR (ATTR_LINEITEM.PARENT IS NULL AND ATTR_LINEITEM.TRIL_GID = ELE_LINEITEM.TRIL_GID))"+
								" AND L2ATTRIBUTE1.TRIL_GID = Regexp_Substr (ELE_LINEITEM.SERVPROD, '[^_]+', 1, '1')"+
								" AND L2ATTRIBUTE1.PARENT = L2ATTRIBUTE2.TRIL_GID"+
								" AND L2ATTRIBUTECOL.OBJECTGID = L2ATTRIBUTE2.TRIL_GID"+
								" AND SERVICE.ORDERABLECOMPONENTS = L2ATTRIBUTECOL.COLLECTIONGID ";
							
								

	
								
	private String IOV_DETAILS_FORWARD_4_ELEMENT = "SELECT "+
					" SEARCH.SERVICE AS FORWARD_SERVICE ,SEARCH.SERVICE_OCATID AS FORWARD_SERVICE_OCATID, SEARCH.SERVICE_GRP_VERSION AS FORWARD_SERVICE_GRP_VERSION,"+
					" SEARCH.COMPONENT AS FORWARD_COMPONENT,SEARCH.COMP_OCATID AS FORWARD_COMPONENT_OCATID, SEARCH.COMP_GRP_VERSION AS FORWARD_COMPONENT_GRP_VERSION,"+
					" SEARCH.ELEMENT AS FORWARD_ELEMENT,SEARCH.ELE_OCATID AS FORWARD_ELEMENT_OCATID, SEARCH.ELE_GRP_VERSION AS FORWARD_ELEMENT_GRP_VERSION,"+
					" L2ATTRIBUTE3.DESCRIPTION AS FORWARD_ATTRIBUTE ,L2ATTRIBUTE3.OCATID AS FORWARD_ATTRIBUTE_OCATID, L2ATTRIBUTE3.GRP_VERSION AS FORWARD_ATTRIBUTE_GRP_VERSION "+
					" FROM"+
					" ("+
					" SELECT "+
					" SERVICE.DISP_NAME AS SERVICE,SERVICE.OCATID AS SERVICE_OCATID, SERVICE.GRP_VERSION AS SERVICE_GRP_VERSION,"+
					" L2ATTRIBUTE1.DESCRIPTION AS COMPONENT , L2ATTRIBUTE1.OCATID AS COMP_OCATID, L2ATTRIBUTE1.GRP_VERSION AS COMP_GRP_VERSION,"+
					" ELE_LINEITEM.DESCRIPTION AS ELEMENT  ,  ELE_LINEITEM.OCATID AS ELE_OCATID , ELE_LINEITEM.GRP_VERSION AS ELE_GRP_VERSION,"+
					" ELE_LINEITEM.TRIL_GID"+
					" FROM "	+
					ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE , "+
					ConnectionBean.getDbPrefix()+"EQ_L2ATTRIBUTECOL L2ATTRIBUTECOL ,"+ 
					ConnectionBean.getDbPrefix()+"EQ_L2ATTRIBUTE L2ATTRIBUTE1 ,"+
					ConnectionBean.getDbPrefix()+"EQ_L2ATTRIBUTE ELE_LINEITEM"+
					" WHERE"+ 
					// SERVICE NAME
					" SERVICE.DISP_NAME IN (?)"+ 
					// COMPONENT NAME
					 "AND L2ATTRIBUTE1.DESCRIPTION IN (?)"+ 
					// ELEMENT NAME
					" AND ELE_LINEITEM.DESCRIPTION IN (?)"+  
				    " AND SERVICE.ORDERABLECOMPONENTS = L2ATTRIBUTECOL.COLLECTIONGID"+
					" AND L2ATTRIBUTECOL.OBJECTGID = L2ATTRIBUTE1.TRIL_GID"+
					" AND ELE_LINEITEM.PARENT = L2ATTRIBUTE1.TRIL_GID) SEARCH ,"+
					ConnectionBean.getDbPrefix()+"EQ_L2ATTRIBUTE L2ATTRIBUTE3 WHERE L2ATTRIBUTE3.PARENT(+) = SEARCH.TRIL_GID";
	
	private String IOV_DETAILS_FORWARD_4_ATTRIBUTE = 
		            IOV_DETAILS_FORWARD_4_ELEMENT +
		            " AND  L2ATTRIBUTE3.DESCRIPTION IN (?)";
	
	public CiBase_IOV_Update(Imadaqv02ViewComponent imadaqv02ViewComponent) throws IOException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
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
		
		ArrayList<String[]> ciBaseHeader = CommonUtils.getQueryHeader(IOV_DETAILS_REVERSE_4_ATTRIBUTE, parentComp,"","","");
		ArrayList<String[]> ciBaseHeader1 = CommonUtils.getQueryHeader(IOV_DETAILS_FORWARD_4_ATTRIBUTE, parentComp,"","","","");
		
		newHeaderRow = addInRow(csvReader.readNext(),ciBaseHeader.get(0));
		newHeaderRow = addInRow(newHeaderRow,ciBaseHeader1.get(0));
		 
		parentComp.getQueryResult().append("\n------------------------------------------------------------\n");
		parentComp.getQueryResult().append("GETTING QUOTE NUMBER FROM "+newHeaderRow[CiBaseConstants.quoteindexinfile]+"\n");
		parentComp.getQueryResult().append("GETTING OCATID KEY   FROM "+newHeaderRow[CiBaseConstants.existingValue_inFile]+"\n");
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
		       String quoteNumber      			= 	CommonUtils.addZeroPrefixinOrder(csvRow[CiBaseConstants.quoteindexinfile]);
		       String PRIMARYROUTERUSID 	= 	csvRow[CiBaseConstants.existingValue_inFile];
		       
		       
		    
		       
		       ArrayList<String[]> iovdetails = CommonUtils.getQueryResult(IOV_DETAILS_REVERSE_4_ATTRIBUTE, parentComp,quoteNumber,PRIMARYROUTERUSID,PRIMARYROUTERUSID);
			   
		       if(null == iovdetails || iovdetails.size()==0)
		       {
		    	 String arr[]= {"QUOTE NOT FOUND"};
		    	 newWriterRow = addInRow(csvRow,arr);
		    	 csvWriter.writeNext(newWriterRow);
		       }
		       
		       else if(iovdetails.size() == 1)
		       {
		    	   String[] QUOTE_BASEDARR = iovdetails.get(0);
		    	   
		    	   ArrayList<String[]> iovdetailsFORWARD = getForwardServiceDetails(CiBaseConstants.SERVICENAME_INDEX,CiBaseConstants.COMPONENTNAME_INDEX,CiBaseConstants.ELEMENTNAME_INDEX,CiBaseConstants.ATTRIBUTENAME_INDEX,QUOTE_BASEDARR);   
			       /*
			        * ALMOST IMPOSSIBLE CONDITION
			        */
		    	   if(iovdetailsFORWARD.size() > 1)
			       {
			    	   JOptionPane.showMessageDialog(null, "MULTIPLE FORWARD SERVICE FOUND FOR SERVICE >>."+QUOTE_BASEDARR[CiBaseConstants.SERVICENAME_INDEX]+" COMPONENT >>"+QUOTE_BASEDARR[CiBaseConstants.COMPONENTNAME_INDEX]+" ELEMENT >>"+QUOTE_BASEDARR[CiBaseConstants.ELEMENTNAME_INDEX]+" ATTRIBUTE >>"+QUOTE_BASEDARR[CiBaseConstants.ATTRIBUTENAME_INDEX]);
			       }
		       
			      
		    	   else if(iovdetailsFORWARD.size() ==1)
			       {
			    	   String[] SERVICE_BASEDARR	= iovdetailsFORWARD.get(0) ;
			    	   checkReverseNForwardServiceMapping(QUOTE_BASEDARR,SERVICE_BASEDARR);
			    	   
			    	   newWriterRow = addInRow(csvRow,QUOTE_BASEDARR);
			    	   newWriterRow = addInRow(newWriterRow,SERVICE_BASEDARR);
			       }
		    	   else
		    	   {
		    		   QUOTE_BASEDARR[QUOTE_BASEDARR.length-1] = "FORWARD HIERARCHY NOT FOUND.";
			    	   QUOTE_BASEDARR[QUOTE_BASEDARR.length-2] = "FAILED";
		    		   newWriterRow = addInRow(csvRow,QUOTE_BASEDARR);
		    	   }
			       csvWriter.writeNext(newWriterRow);
			   }
		      
		       else if(iovdetails.size() >1)
		       {
		    	  String S="";
		    	  for(String[] ARR : iovdetails)
		    	     {
		    		  S+=ARR[CiBaseConstants.SERVICENAME_INDEX]+"|";
		    	     }
		    	     
		    	     for(String[] QUOTE_BASEDARR : iovdetails)
		    	     {
		    	    	
			    		 QUOTE_BASEDARR[QUOTE_BASEDARR.length-1] = "MULTIPLE SERVICE FOUND >> "+S;
			    		 QUOTE_BASEDARR[QUOTE_BASEDARR.length-2] = "FAILED";
			    		 
			    		 ArrayList<String[]> iovdetailsFORWARD = getForwardServiceDetails(CiBaseConstants.SERVICENAME_INDEX,CiBaseConstants.COMPONENTNAME_INDEX,CiBaseConstants.ELEMENTNAME_INDEX,CiBaseConstants.ATTRIBUTENAME_INDEX,QUOTE_BASEDARR);
			    		
			    		   /*
					        * ALMOST IMPOSSIBLE CONDITION
					        */
			    		 if(iovdetailsFORWARD.size() > 1)
			    		 {
			    			JOptionPane.showMessageDialog(null, "MULTIPLE FORWARD SERVICE FOUND FOR SERVICE >>."+QUOTE_BASEDARR[CiBaseConstants.SERVICENAME_INDEX]+" COMPONENT >>"+QUOTE_BASEDARR[CiBaseConstants.COMPONENTNAME_INDEX]+" ELEMENT >>"+QUOTE_BASEDARR[CiBaseConstants.ELEMENTNAME_INDEX]+" ATTRIBUTE >>"+QUOTE_BASEDARR[CiBaseConstants.ATTRIBUTENAME_INDEX]);
			  		     }
			    		 else if(iovdetailsFORWARD.size() ==1)
			    		 {
			    			 String[] SERVICE_BASEDARR	= iovdetailsFORWARD.get(0) ;
			    			 
			    			 checkReverseNForwardServiceMapping(QUOTE_BASEDARR,SERVICE_BASEDARR);
			    			 
			    			 newWriterRow = addInRow(csvRow,QUOTE_BASEDARR);
			    			 newWriterRow = addInRow(newWriterRow,SERVICE_BASEDARR);
			  		     }
			    		 else
			    		 {
			    			 QUOTE_BASEDARR[QUOTE_BASEDARR.length-1] = "FORWARD HIERARCHY NOT FOUND.";
					    	 QUOTE_BASEDARR[QUOTE_BASEDARR.length-2] = "FAILED";
			    			 newWriterRow = addInRow(csvRow,QUOTE_BASEDARR);
			    		 }
			    		 csvWriter.writeNext(newWriterRow);
				     }
			  }
		     
		      
		    }
			
			csvWriter.close();
			csvReader.close();
			CommonUtils.showOnTable(writeCsvFilePath);
			
		
	
	}
	/*
	 * THIS METHOD TO FATCH THE FORWARD SERVICE DETAILS 
	 * 1. IF ATTRIBUTE
	 * 2. IF ELEMENT
	 */
	private ArrayList<String[]> getForwardServiceDetails(int SERVICENAME_INDEX,int COMPONENTNAME_INDEX,int ELEMENTNAME_INDEX ,int ATTRIBUTENAME_INDEX,String[] QUOTE_BASEDARR) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException
	{
		if(isAttribute(QUOTE_BASEDARR))
		{
			// SERVICE, COMPONENT,ELELEMT ,ATTRIBUTE SERACH
			 return CommonUtils.getQueryResult(IOV_DETAILS_FORWARD_4_ATTRIBUTE, parentComp,QUOTE_BASEDARR[SERVICENAME_INDEX],QUOTE_BASEDARR[COMPONENTNAME_INDEX],QUOTE_BASEDARR[ELEMENTNAME_INDEX],QUOTE_BASEDARR[ATTRIBUTENAME_INDEX]);
	     
		}
		else
		{
			// SERVICE, COMPONENT,ELELEMT, ATTRIBUTE SERACH
			
			 return CommonUtils.getQueryResult(IOV_DETAILS_FORWARD_4_ELEMENT, parentComp,QUOTE_BASEDARR[SERVICENAME_INDEX],QUOTE_BASEDARR[COMPONENTNAME_INDEX],QUOTE_BASEDARR[ELEMENTNAME_INDEX]);
		}
	}
	
	private boolean isAttribute(String[] QUOTE_BASEDARR)
	{
		if("ELEMENT".equalsIgnoreCase(QUOTE_BASEDARR[0]))
		{
			return false;
		}
		return true;
	}
	
	
	
	private void checkReverseNForwardServiceMapping(String[] QUOTE_BASEDARR, String[] SERVICE_BASEDARR)
	{
		
		int reverseServiceIndex 		= CiBaseConstants.SERVICENAME_INDEX;
		int forwardServiceIndex 		= CiBaseConstants.FORWARD_SERVICENAME_INDEX;
		int reverseServiceOcatIndex 	= CiBaseConstants.SERVICENAME_INDEX+1;
		int forwardServiceOcatIndex 	= CiBaseConstants.FORWARD_SERVICENAME_INDEX+1;
		int reverseServiceGRPIndex 		= CiBaseConstants.SERVICENAME_INDEX+2;
		int forwardServiceGRPIndex 		= CiBaseConstants.FORWARD_SERVICENAME_INDEX+2;
		
		
		int reverseComponentIndex 		= CiBaseConstants.COMPONENTNAME_INDEX;
		int forwardComponentIndex 		= CiBaseConstants.FORWARD_COMPONENTNAME_INDEX;
		int reverseComponentOcatIndex 	= CiBaseConstants.COMPONENTNAME_INDEX+1;
		int forwardComponentOcatIndex 	= CiBaseConstants.FORWARD_COMPONENTNAME_INDEX+1;
		int reverseComponentGRPIndex 	= CiBaseConstants.COMPONENTNAME_INDEX+2;
		int forwardComponentGRPIndex 	= CiBaseConstants.FORWARD_COMPONENTNAME_INDEX+2;
		
		int reverseElementIndex 		= CiBaseConstants.ELEMENTNAME_INDEX;
		int forwardElementIndex 		= CiBaseConstants.FORWARD_ELEMENTNAME_INDEX;
		int reverseElementOcatIndex 	= CiBaseConstants.ELEMENTNAME_INDEX+1;
		int forwardElementOcatIndex 	= CiBaseConstants.FORWARD_ELEMENTNAME_INDEX+1;
		
		
		int reverseAttributeIndex 		= CiBaseConstants.ATTRIBUTENAME_INDEX;
		int forwardAttributeIndex 		= CiBaseConstants.FORWARD_ATTRIBUTENAME_INDEX;
		int reverseAttributeOcatIndex 	= CiBaseConstants.ATTRIBUTENAME_INDEX+1;
		int forwardAttributeOcatIndex 	= CiBaseConstants.FORWARD_ATTRIBUTENAME_INDEX+1;
		
		
		StringBuilder sb = new StringBuilder();
		
		QUOTE_BASEDARR = fillForNull(QUOTE_BASEDARR);
		SERVICE_BASEDARR = fillForNull(SERVICE_BASEDARR);
		
		
		/*
		 * SERVICE
		 */
		if(!QUOTE_BASEDARR[reverseServiceIndex].equals(SERVICE_BASEDARR[forwardServiceIndex]))
		{ 
			sb.append("<< SERVICE NAME NOT MATCH >>");
		}
		if(!QUOTE_BASEDARR[reverseServiceOcatIndex].equals(SERVICE_BASEDARR[forwardServiceOcatIndex]))
		{
			sb.append("<< SERVICE OCATID NOT MATCH >>");
		}
		if(!QUOTE_BASEDARR[reverseServiceGRPIndex].equals(SERVICE_BASEDARR[forwardServiceGRPIndex]))
		{
			sb.append("<< SERVICE GRP VERSION NOT MATCH >>");
		}
		
		/*
		 * COMPONENT
		 */
		if(!QUOTE_BASEDARR[reverseComponentIndex].equals(SERVICE_BASEDARR[forwardComponentIndex]))
		{
			sb.append("<< COMPONENT NAME NOT MATCH >>");
		}
		if(!QUOTE_BASEDARR[reverseComponentOcatIndex].equals(SERVICE_BASEDARR[forwardComponentOcatIndex]))
		{
			sb.append("<< COMPONENT OCATID NOT MATCH >>");
		}
		if(!QUOTE_BASEDARR[reverseComponentGRPIndex].equals(SERVICE_BASEDARR[forwardComponentGRPIndex]))
		{
			sb.append("<< COMPONENT GRP VERSION NOT MATCH >>");
		}
		/*
		 * ELEMENT
		 * 
		 */
		
		
		if(!QUOTE_BASEDARR[reverseElementIndex].equals(SERVICE_BASEDARR[forwardElementIndex]))
		{
			sb.append("<< ELEMENT NAME NOT MATCH >>");
		}
		if(!QUOTE_BASEDARR[reverseElementOcatIndex].equals(SERVICE_BASEDARR[forwardElementOcatIndex]))
		{
			sb.append("<< ELEMENT OCATID NOT MATCH >>");
		}
		
		/*
		 * ATTRIBUTE
		 */
		if(isAttribute(QUOTE_BASEDARR))
		{
		
			if(!QUOTE_BASEDARR[reverseAttributeIndex].equals(SERVICE_BASEDARR[forwardAttributeIndex]))
			{
				sb.append("<< ATTRIBUTE NAME NOT MATCH >>");
			}
			if(!QUOTE_BASEDARR[reverseAttributeOcatIndex].equals(SERVICE_BASEDARR[forwardAttributeOcatIndex]))
			{
				sb.append("<< ATTRIBUTE OCATID NOT MATCH >>");
			}
		}
		 
		if(sb.length()>0)
		 {
		 QUOTE_BASEDARR[QUOTE_BASEDARR.length-1] = QUOTE_BASEDARR[QUOTE_BASEDARR.length-1]+sb.toString();
		 QUOTE_BASEDARR[QUOTE_BASEDARR.length-2] = "FAILED";
		 }
		
	}
	
	private String[] fillForNull(String[] ARR)
	{
		for(int X = 0 ; X <ARR.length ; X++)
		{
		  ARR[X] =(null == ARR[X] ? "" : ARR[X]);	
		}
		return ARR;
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
}
