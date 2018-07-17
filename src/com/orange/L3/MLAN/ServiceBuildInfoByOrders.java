package com.orange.L3.MLAN;

import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.ui.component.MLANViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.ConnectionBeanArchived;
import com.orange.util.ProgressMonitorPane;

public class ServiceBuildInfoByOrders {
	
	private String SITE_OR_HOTCUT=" AND ((QUOTE.HOTCUTNEWSITE <> 'NULL' AND QUOTE.HOTCUTNEWSITE =  SITE.TRIL_GID) OR (QUOTE.HOTCUTNEWSITE IS NULL AND QUOTE.SITE = SITE.TRIL_GID ))";
	
	private  static String HEADER[]={"QUOTENUMBER","ARCHIVED","QUOTE_TYPE","EQ_TYPE","QUOTE_STATUS","QUOTE_SERVICE","QUOTE_MIGRATEDSERVICE","PREV_QUOTE","ASSOCIATED_QUOTE","CORE_SITE_ID","ADDRESS_ID","ORANGE_SITENAME","SITECODE","STATUS","COUNTRY","COUNTRY_CODE","COUNTRY_CAT","LOCALSITE_USER_ICO","LOCALSITE_USER_NAME","END_USER_ICO","END_USER_NAME","CONTRACTING_PARTY_ICO","CONTRACTING_PARTY_NAME","ELEMENT_SERVICEBUILD","SALES_NOTIFICATIONDATE","ORDER_CREATIONDATE","LINEITEM","VALUE","EXIST_CONFIG","NEW_CONFIG","CONSIDERABLE_VALUE"};
	
	private ArrayList<String> archive ;

	
	private String findArchiveOrNo = "SELECT QUOTE.ARCHIVED,QUOTE.QUOTENUMBER FROM "
		                              +ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE " +
		                              "WHERE QUOTE.QUOTENUMBER IN (##HERE_QUOTES##)";
	
	private String getArchivedOrdersServiceBuildInfo = 
		" SELECT " +
		" LINEITEM.ORDERNUMBER,"+
		 CommonUtils.LINEITEM_UDSTATUS +
		" MILES.EQ_ACTUALDATE AS CREATION_DATE," +
		" LINEITEM.DESCRIPTION,LINEITEM.VALUE,LINEITEM.EXIST_CONFIG,LINEITEM.NEW_CONFIG," +
		"'to be updated' as CONSIDERABLE_VALUE " +
		" FROM "+
		  ConnectionBeanArchived.getDbPrefix()+"SC_QUOTE_LINE_ITEM_A LINEITEM, " +
		  ConnectionBeanArchived.getDbPrefix()+"EQMILESTONE_A MILES "+
		" WHERE " +
		" LINEITEM.ORDERNUMBER IN (##HERE_QUOTES##) "+
		" ##INCLUDED## "+
		" AND MILES.EQ_TITLE = 'Order Raised Date'"+
		" AND MILES.ORDERNUMBER = LINEITEM.ORDERNUMBER";
	
	
	
	private String getserviceBuildInfoForArchive = 
        " SELECT DISTINCT" +
		" QUOTE.QUOTENUMBER," +
		  CommonUtils.ARCHIVE_DECODE+
		  CommonUtils.ORDER_TYPE_DECODE+
	      CommonUtils.EQ_TYPE_DECODE+
		" ORDSTATUS.EQ_STATUS,"+
		" QUOTE.SERVICENAME, " +
		//" QUOTE.MIGRATIONSERVICENAME AS MIGRATIONSERVICENAME, "+
		" SERVICE.DISP_NAME AS MIGRATIONSERVICENAME, "+
		" QUOTE.EQ_GOLDORIGNB ,QUOTE.ASSOCIATEDORDERNB, "+
		" SITE.CORE_SITE_ID,SITE.ADDRESS_ID,SITE.ORANGE_SITENAME,SITE.SITECODE,SITE.STATUS, "+
		" ADDR.COUNTRY,ADDR.COUNTRY_CODE,MIS.COUNTRYCATEGORY COUNTRY_CAT,"+
		" ORG.ORGANIZATIONID AS LOCALSITE_USER_ICO,ORG.NAME AS LOCALSITE_USER_NAME,"+
		" ORG1.ORGANIZATIONID AS END_USER_ICO,ORG1.NAME AS END_USER_NAME,"+
		" ORG2.ORGANIZATIONID AS CONTRACTING_PARTY_ICO,ORG2.NAME AS CONTRACTING_PARTY_NAME,"+
		" QUOTE.EQ_CUSTOMERNOTICE AS SALES_NOTIFICATIONDATE "+
		" FROM "+
	        ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORGANIZATION,"+  
	        ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE " +
	        "LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID) "+
	        "LEFT JOIN "+ConnectionBean.getDbPrefix()+"SC_QUOTE_MISCELLANEOUS MIS ON (MIS.TRIL_GID = QUOTE.SC_QUOTE_MISCELLANEOUS),"+
	        ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID) ,"+
	        ConnectionBean.getDbPrefix()+"EQ_ORDERSTATUS ORDSTATUS," +
		    ConnectionBean.getDbPrefix()+"SC_ADDRESS ADDR," +
		    ConnectionBean.getDbPrefix()+"EQ_SITE SITE,"+
		    ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG," +
		    ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG1," +
			ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG2" + 
		
		" WHERE " +
		" ORDSTATUS.TRIL_GID = QUOTE.EQ_WFSTATUS"+
		SITE_OR_HOTCUT+
		" AND SITE.EQ_SITEOF = ORG.TRIL_GID"+
		" AND QUOTE.EQ_DELIVERYPARTY = ORG1.TRIL_GID"+
		" AND QUOTE.EQ_REQUESTINGPARTY= ORG2.TRIL_GID"+ 
		" AND QUOTE.CONFIGURATIONS = HR.TRIL_GID " +
		" AND ADDR.TRIL_GID = SITE.SITEADDRESS"+
		" AND QUOTE.ARCHIVED = '1' AND QUOTE.QUOTENUMBER IN (##HERE_QUOTES##)";
	
	
	private String getserviceBuildInfoForNonArchive = 
        " SELECT DISTINCT" +
        " QUOTE.QUOTENUMBER ," +
        CommonUtils.ARCHIVE_DECODE+
        CommonUtils.ORDER_TYPE_DECODE+
        CommonUtils.EQ_TYPE_DECODE+
        " ORDSTATUS.EQ_STATUS,"+
        " QUOTE.SERVICENAME, " +
        " SERVICE.DISP_NAME AS MIGRATIONSERVICENAME, "+
        " QUOTE.EQ_GOLDORIGNB ,QUOTE.ASSOCIATEDORDERNB, "+
    	" SITE.CORE_SITE_ID,SITE.ADDRESS_ID,SITE.ORANGE_SITENAME,SITE.SITECODE,SITE.STATUS, "+
        " ADDR.COUNTRY,ADDR.COUNTRY_CODE,MIS.COUNTRYCATEGORY AS COUNTRY_CAT,"+
        " ORG.ORGANIZATIONID AS LOCALSITE_USER_ICO,ORG.NAME AS LOCALSITE_USER_NAME,"+
		" ORG1.ORGANIZATIONID AS END_USER_ICO,ORG1.NAME AS END_USER_NAME,"+
		" ORG2.ORGANIZATIONID AS CONTRACTING_PARTY_ICO,ORG2.NAME AS CONTRACTING_PARTY_NAME,"+
		 CommonUtils.LINEITEM_UDSTATUS +
        " QUOTE.EQ_CUSTOMERNOTICE AS SALES_NOTIFICATIONDATE,MILES.EQ_CREATIONDATE AS CREATION_DATE,"+
        " LINEITEM.DESCRIPTION,LINEITEM.VALUE,LINEITEM.EXIST_CONFIG,LINEITEM.NEW_CONFIG ," +
        CommonUtils.getConsiderableValue() +
        " FROM "+
        ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORGANIZATION,"+  
        ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE " +
        "LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID) "+
        "LEFT JOIN "+ConnectionBean.getDbPrefix()+"SC_QUOTE_MISCELLANEOUS MIS ON (MIS.TRIL_GID = QUOTE.SC_QUOTE_MISCELLANEOUS) "+
    	"LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQMILESTONE MILES ON (MILES.EQ_ORDERGID=QUOTE.TRIL_GID AND MILES.EQ_TITLE='Order Raised Date'),"+
        ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID), "+
        ConnectionBean.getDbPrefix()+"SC_QUOTE_LINE_ITEM LINEITEM, "+
        ConnectionBean.getDbPrefix()+"EQ_ORDERSTATUS ORDSTATUS," +
        ConnectionBean.getDbPrefix()+"SC_ADDRESS ADDR," +
        ConnectionBean.getDbPrefix()+"EQ_SITE SITE,"+
        ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG," +
        ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG1," +
    	ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG2 " +

        " WHERE " +
        " QUOTE.TRIL_GID = LINEITEM.QUOTE" +
        " AND ORDSTATUS.TRIL_GID = QUOTE.EQ_WFSTATUS"+
         SITE_OR_HOTCUT+
        " AND SITE.EQ_SITEOF = ORG.TRIL_GID"+
        " AND QUOTE.EQ_DELIVERYPARTY = ORG1.TRIL_GID"+
        " AND QUOTE.EQ_REQUESTINGPARTY= ORG2.TRIL_GID"+ 
        " AND ADDR.TRIL_GID = SITE.SITEADDRESS"+
       // " AND MILES.EQ_ORDERGID=QUOTE.TRIL_GID"+
        " AND QUOTE.CONFIGURATIONS = HR.TRIL_GID " +
	   // " AND MILES.EQ_TITLE='Order Raised Date'"+
        "  ##INCLUDED## "+
   		"AND QUOTE.QUOTENUMBER IN (##HERE_QUOTES##)";
	
	
	
	ArrayList<String> nonArchive ;
	private ArrayList<String> quoteNumbers;
	
	private static ArrayList<String[]> getQuotePrevStateInfo(String quoteNumber,MLANViewComponents mLANViewComponents) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
		String SQLPREVSTATUS ="SELECT DISTINCT " +
		"ORDSTATUS.EQ_OLDSTATUS " +
		"FROM " +
		ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE, " +
		ConnectionBean.getDbPrefix()+"EQ_ORDERSTATUS ORDSTATUS " +
		"WHERE " +
		"ORDSTATUS.TRIL_GID = QUOTE.EQ_WFSTATUS " +
		"AND QUOTE.QUOTENUMBER = ?";
			
	ArrayList<String[]> quote = CommonUtils.getQueryResult(SQLPREVSTATUS, mLANViewComponents,quoteNumber);
	return quote;
	}
	
	public ServiceBuildInfoByOrders(String filePath,MLANViewComponents mlanViewComponents,String likeIncluded) throws IOException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		mlanViewComponents.getQueryResult().append("\nPlease wait..Process may take few moments.\n");
		mlanViewComponents.getQueryResult().append("\nIncluding Like in Query is disabled.\n");
		
		getserviceBuildInfoForNonArchive = getserviceBuildInfoForNonArchive.replace("##INCLUDED##", likeIncluded);
		getArchivedOrdersServiceBuildInfo = getArchivedOrdersServiceBuildInfo.replace("##INCLUDED##", likeIncluded);
		
		archive = new ArrayList<String>();
		nonArchive = new ArrayList<String>();
		
		quoteNumbers = getListOfOrders(filePath,mlanViewComponents);
		mlanViewComponents.getQueryResult().append("\nEXTRACTING ARCHIVE AND NON ARCHIVE ORDERS..\n");
	
		findArchiveNonArchiveQueryResults(quoteNumbers,mlanViewComponents);
		
			
		mlanViewComponents.getQueryResult().append("\nEXTRACTING NON ARCHIVE ORDERS DETAILS..\n");
		ArrayList<String[]> nonArchiveInfo = getNonArchiveInfo(nonArchive,mlanViewComponents);
		
			
		mlanViewComponents.getQueryResult().append("\nEXTRACTING ARCHIVE ORDERS DETAILS..\n");
		ArrayList<String[]> archiveInfo =    getArchiveInfo(archive,mlanViewComponents);
		
		
		
		mlanViewComponents.getQueryResult().append("\nMERGING ARCHIVE AND NON ARCHIVE ORDERS DETAILS..\n");
		
		ArrayList<String[]> combindInfo = new ArrayList<String[]>(nonArchiveInfo);
		combindInfo.addAll(archiveInfo);
		
		
		
		mlanViewComponents.getQueryResult().append("\nUPDATING PREVIOUS STATUS IF ONHOLD/UPDATING ARCHIVE ORDER INFORMATIONS..");
		combindInfo = updateOnHoldNArchiveInfo(combindInfo,mlanViewComponents);
		
		String path = filePath;	
		int index = path.lastIndexOf(".");
		String sub = path.substring(0,index) ;
		String csvPath=sub+"_Service_Details.csv";
		
		CommonUtils.showTable(HEADER, combindInfo, csvPath);
		
		mlanViewComponents.getQueryResult().append("\nFile export successfully."+csvPath);
		mlanViewComponents.getQueryResult().append("\nProcess completed successfully.");
	}
	
	private Map<String,ArrayList<String[]>> addInMap(Map<String,ArrayList<String[]>> map,String key,String[] value)
	{
		if(map.containsKey(key))
		{
			ArrayList<String[]> existing = map.get(key);
			existing.add(value);
			map.put(key, existing);
		
		
		}
		else
		{
			ArrayList<String[]> newvalue = new ArrayList<String[]>();
			newvalue.add(value);
			map.put(key, newvalue);
		}
		
		return map;
	}
	
	
	private void findArchiveNonArchiveQueryResults(ArrayList<String> quoteNumbers,MLANViewComponents mLANViewComponents) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{

		for(String inorders: putIn999(quoteNumbers))
		{
		String newfindArchiveOrNo = findArchiveOrNo.replace("##HERE_QUOTES##",inorders);
		ArrayList<String[]> ARCHIVE_DETAILS = CommonUtils.getQueryResult(newfindArchiveOrNo, mLANViewComponents);
		
		mLANViewComponents.getQueryResult().append("\nTotal Orders Store in Cache "+ARCHIVE_DETAILS.size()+"\n");
	
		 
			for(String[] arr : ARCHIVE_DETAILS)
			{
			
			  
			  if (null == arr || arr.length == 0 || arr[0] == null || arr[0].equals(null) || arr[0].equals("null"))
			  {
				  nonArchive.add(arr[1]);
			  }
			  else  if(null != arr && arr.length > 0 && arr[0].equalsIgnoreCase("0"))
			  {
				  nonArchive.add(arr[1]);
			  }
			 
			  
			  else
			  {
				  archive.add(arr[1]);
			  }
			}
			mLANViewComponents.getQueryResult().append("\nTotal  archive orders in Cache "+archive.size()+"\n");
			mLANViewComponents.getQueryResult().append("\nTotal  non archive orders in Cache "+nonArchive.size()+"\n");
		}
	}
	
	private ArrayList<String[]> getArchiveInfo(ArrayList<String> archive,MLANViewComponents mLANViewComponents) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
		ArrayList<String[]> ARCHIVE_ORDERS_DETAILS_LIST = new ArrayList<String[]>();
		
		Map<String,ArrayList<String[]>> goldInfo = new LinkedHashMap<String,ArrayList<String[]>>();
		Map<String,ArrayList<String[]>> archiveInfo = new LinkedHashMap<String,ArrayList<String[]>>();
		
		for(String inorders : putIn999(archive))
		{
			String newgetArchivedOrdersServiceBuildInfo = getArchivedOrdersServiceBuildInfo.replace("##HERE_QUOTES##",inorders);
			String newgetserviceBuildInfoForArchive = getserviceBuildInfoForArchive.replace("##HERE_QUOTES##",inorders);
			ArrayList<String[]> ARCHIVE_ORDERS_DETAILS 			 = CommonUtils.getArchiveQueryResult(newgetArchivedOrdersServiceBuildInfo, mLANViewComponents);
			
				for(String[] value : ARCHIVE_ORDERS_DETAILS)
				{
					addInMap(archiveInfo,value[0],value);
				
				}
			ArrayList<String[]> ARCHIVE_ORDERS_DETAILS_FROM_GOLD = CommonUtils.getQueryResult(newgetserviceBuildInfoForArchive, mLANViewComponents);
			
				for(String[] value : ARCHIVE_ORDERS_DETAILS_FROM_GOLD)
				{
					addInMap(goldInfo,value[0],value);
				}
		}
		
		
		
		Iterator<Map.Entry<String, ArrayList<String[]>>> entries = goldInfo.entrySet().iterator();
		while (entries.hasNext()) 
		{
			Map.Entry<String, ArrayList<String[]>> entry = entries.next();
			String key = entry.getKey();
			ArrayList<String[]> value = entry.getValue();
			
			
			String []ARRs = value.get(0);
			
			if(archiveInfo.containsKey(key))
			{
				
				ArrayList<String[]> VALs = archiveInfo.get(key);
			
				
				for(String[] VAL : VALs)
				{
					String ARR[] = new String[HEADER.length];
	                 for(int x = 0 ; x< ARRs.length;x++)
					   {
						   ARR[x] =  ARRs[x];
					   }
					
						   ARR[ARRs.length] = VAL[1];
						   ARR[ARRs.length+(1)] =  VAL[2];
						   ARR[ARRs.length+(2)] =  VAL[3];
						   
						
						   ARR[ARRs.length+(3)] =  VAL[4];
						   ARR[ARRs.length+(4)] =  VAL[5];
						   ARR[ARRs.length+(5)] =  VAL[6];
						  
				
					 
					 ARCHIVE_ORDERS_DETAILS_LIST.add(ARR);
				}
			}
			else
			{
				String ARR[] = new String[HEADER.length];
				 for(int x = 0 ; x< ARRs.length;x++)
				   {
					   ARR[x] =  ARRs[x];
				   }
				 ARCHIVE_ORDERS_DETAILS_LIST.add(ARR);
			}
			
		}
		
		return ARCHIVE_ORDERS_DETAILS_LIST;
		
	}
	
	private ArrayList<String> getListOfOrders(String filePath,MLANViewComponents mLANViewComponents) throws IOException{
		ArrayList<String> quotes = new ArrayList<String>();
		CSVReader br = new CSVReader(new FileReader(filePath));
		String[] row = null;
		row = br.readNext();
		int line = 0;
		while ((row = br.readNext()) != null) 
		{
			quotes.add(CommonUtils.addZeroPrefixinOrder(row[0]));
			line++;
			
		}
		mLANViewComponents.getQueryResult().append("\nTotal Lines read >>"+ line+"\n");
		
		br.close();
		return quotes;
	}
	
	private ArrayList<String[]> getNonArchiveInfo(ArrayList<String> nonArchive,MLANViewComponents mLANViewComponents) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
		ArrayList<String[]> NON_ARCHIVE_ORDERS_DETAILS_LIST = new ArrayList<String[]>();
		for(String inorders :  putIn999(nonArchive))
		{
		String newgetserviceBuildInfoForNonArchive = getserviceBuildInfoForNonArchive.replace("##HERE_QUOTES##",inorders);
		ArrayList<String[]> NON_ARCHIVE_ORDERS_DETAILS = CommonUtils.getQueryResult(newgetserviceBuildInfoForNonArchive, mLANViewComponents);
		NON_ARCHIVE_ORDERS_DETAILS_LIST.addAll(NON_ARCHIVE_ORDERS_DETAILS);
		
		}
		return NON_ARCHIVE_ORDERS_DETAILS_LIST;
		
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
	
	public ArrayList<String[]> updateOnHoldNArchiveInfo(ArrayList<String[]> serviceBuildItems ,MLANViewComponents mLANViewComponents) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
      
       ArrayList<String[]> updated = new ArrayList<String[]>();
       int count = 0;
       for(String[] arr : serviceBuildItems)
       {
    	   count++;
    	   ProgressMonitorPane.getInstance().setProgress(count,(double)serviceBuildItems.size());
    	   String ORD = CommonUtils.addZeroPrefixinOrder(arr[0]);
    	   
    	      	 
    	   if(null != arr[4] && arr[4].trim().equalsIgnoreCase("On-Hold"))
    	   {
    		   
    		   ArrayList<String[]> prevState =   getQuotePrevStateInfo(ORD,mLANViewComponents);
    		   arr[4] = "ON_HOLD( Prev was >> "+prevState.get(0)[0]+")";
    	   }
    	  
    	   /*
    	    * THIS COMMENT WILL BE FATCHED FROM QUERY ITSELF for archived  orders. 
    	    * FOR NON ARCHIVED WE NEED TO USE CommonUtils.CONSIDERABLE_VALUE .
    	    * 
    	    */
    	   if(arr[1].equalsIgnoreCase("Archived"))
    	   {
    	   arr = CommonUtils.considerableValuesForArchived(arr,mLANViewComponents);
    	   }
    	   updated.add(arr);
       }
       
		return updated;
	}
	
}
