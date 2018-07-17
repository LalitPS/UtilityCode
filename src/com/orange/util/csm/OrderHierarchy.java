package com.orange.util.csm;

import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.ui.component.MLANViewComponents;
import com.orange.ui.component.custom.Directories;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.CustomViewComponents;
import com.orange.util.ProgressMonitorPane;

public class OrderHierarchy {

private Map<String[],ArrayList<String[]>> consolidatedresult ;
private String[] header = {"GID","<<GIVENORDER>>","GOLD_ORD","PREV_ORD","CREATION_DATE","ORDERTYPE","CHANGE_TYPE","ORDSTATUS","ICO","SERVICE","MIGRATION SERVICE","ARCHIVED","SITE","HOTCUTNEWSITE","SITECODE", "ORANGE_SITENAME","ADDRESS_ID","CORE_SITE_ID","STATUS"};
private ArrayList<String> quoteNumbers;

private Map<String[],ArrayList<String[]>> resultMap;
private MLANViewComponents mLANViewComponents;

public OrderHierarchy (MLANViewComponents mLANViewComponents,String ORDER) throws Exception
	{
		
		this.mLANViewComponents = mLANViewComponents;
		consolidatedresult = new LinkedHashMap<String[],ArrayList<String[]>> ();
		
		String path = this.mLANViewComponents.getFileToValidate().getText();
		this.mLANViewComponents.getQueryResult().append("\nCollecting list of quotes.\n");
		
		
		if(null == path || path.length() == 0 || path.isEmpty())
		{
		    if(null == ORDER || ORDER.length()==0){
		    	return;
		    }
			String parts[] = ORDER.split(",");
			path = Directories.DEFAULTDIR+parts[0]+".csv";
		    quoteNumbers = 	new ArrayList<String>();
		    for(String P : parts)
		    {
		    	quoteNumbers.add(CommonUtils.addZeroPrefixinOrder(P.trim()));
		    }
		}
		else
		{
				quoteNumbers = getListOfOrders(path);
		}
		
		
		this.mLANViewComponents.getQueryResult().append("Initiating fatching details of quotes.\n");
		int count = 0;
		for(String order : quoteNumbers)
		{
		 count++;
		 resultMap = getListOrder("GID_"+Integer.toString(count),order,order,mLANViewComponents,mLANViewComponents.getIncludeUpwards().isSelected());
		 ProgressMonitorPane.getInstance().setProgress(count,(double)quoteNumbers.size());	
		}
		
		
		this.mLANViewComponents.getQueryResult().append("\nInitiating filtering details of quotes.");
		Map<String[],ArrayList<String[]>> filterMap =getFilterValues(resultMap,mLANViewComponents.getIncludeSiteChange().isSelected());
		
		this.mLANViewComponents.getQueryResult().append("\nInitiating preparing details of quotes.this may take few moments.\nPlease wait.");
		ArrayList<String[]> queryResults = getQueryResults(filterMap);
		
		int index = path.lastIndexOf(".");
		String sub = path.substring(0,index) ;
		String csvPath=sub+"_Orders_Hierarchy_Details.csv";
		
		if(mLANViewComponents.getExcludeSiteChange().isSelected())
		{
			csvPath=sub+"_Orders_Hierarchy_Details_Exclude.csv";
		}
		
	
		this.mLANViewComponents.getQueryResult().append("\nFile export successfully."+csvPath);
		this.mLANViewComponents.getQueryResult().append("\nProcess completed successfully.");
		CommonUtils.showTable(header, queryResults, csvPath);
		queryResults = null;
		
		
	}

/*
 * 
 * OrderHierarchy OH = new  OrderHierarchy(,"1234455");
 * OH.getOrderHierarchy();
 */
ArrayList<String[]> orderHierarchyResults;
public OrderHierarchy (CustomViewComponents COMP,String ORDER,boolean isIncludeUpwards,boolean isSiteChnageIncluded) throws Exception
{
	consolidatedresult = new LinkedHashMap<String[],ArrayList<String[]>> ();
	quoteNumbers = 	new ArrayList<String>();
	quoteNumbers.add(CommonUtils.addZeroPrefixinOrder(ORDER));
	int count = 0;
	for(String order : quoteNumbers)
	{
	 count++;
	 resultMap = getListOrder("GID_"+Integer.toString(count),order,order,COMP,isIncludeUpwards);
    }
	Map<String[],ArrayList<String[]>> filterMap =getFilterValues(resultMap,isSiteChnageIncluded);
	orderHierarchyResults = getQueryResults(filterMap);
	/*
	 * queryResults IS THE RESULT
	 */
	}
	public ArrayList<String[]> getOrderHierarchy(){
	return orderHierarchyResults;
	
	}

    private Map<String[],ArrayList<String[]>> getFilterValues(Map<String[],ArrayList<String[]>> result,boolean isIncludeSiteChange)
	{
    	
		Map<String[],ArrayList<String[]>> proxyMap = new LinkedHashMap<String[],ArrayList<String[]>> (result);
		
		String[] base = null;
		
		Iterator<Map.Entry<String[],ArrayList<String[]>>> entries = result.entrySet().iterator();
		while (entries.hasNext()) 
		{
			Map.Entry<String[], ArrayList<String[]>> entry = entries.next();
			String[] key = entry.getKey();
			ArrayList<String[]> value = entry.getValue();
			for(String[] ARR : value)
			{
				if(ARR[0].equals(key[1]))
				{
					base = ARR;
				}
			}
			String BASESITE="";
			if(null != base)
			{
			BASESITE = (null != base[11] && !base[11].isEmpty() && base[11].length()>0 ? base[11] : base[10]);
			}
			for(String[] ARR : value)
			{
				String ORDERSITE = (null != ARR[11] && !ARR[11].isEmpty() && ARR[11].length()>0 ? ARR[11] : ARR[10]);
				
				if(!BASESITE.equals(ORDERSITE))
				{
					ArrayList<String[]> existingList = proxyMap.get(key);
					ArrayList<String[]> proxyList = new ArrayList<String[]> (existingList);
					proxyList.remove(ARR);
					proxyMap.put(key, proxyList);
					proxyList = null;
					existingList = null;
				}
			}
			
		 	
		}
		if(isIncludeSiteChange)
		{
			return result;
		}
		else if(!isIncludeSiteChange)
		{
			return proxyMap;
		}
		return null;
		
		
	}
	private ArrayList<String> getListOfOrders(String filePath) throws IOException
	{
		ArrayList<String> quotes = new ArrayList<String>();
		CSVReader br = new CSVReader(new FileReader(filePath));
		String[] row = null;
		row = br.readNext();
			while ((row = br.readNext()) != null) 
			{
				quotes.add(CommonUtils.addZeroPrefixinOrder(row[0]));
			}
		br.close();
		return quotes;
	}
	
	private Map<String[],ArrayList<String[]>> getListOrder(String count,String keyOrder,String order,CustomViewComponents comp,boolean isIncludeUpwards) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
		
		String QL = "SELECT " +
					"QUOTE.QUOTENUMBER," +
					"QUOTE.EQ_GOLDORIGNB," +
					"MILES.EQ_CREATIONDATE AS CREATION_DATE,"+
					"DECODE (QUOTE.EQ_ORDERTYPE, '1', 'New', '2', 'Change','3','Disconnect','COULD_NOT_DECODE') ORDER_TYPE , " +
					 CommonUtils.EQ_TYPE_DECODE+
					"ORDSTATUS.EQ_STATUS,"+
					"ORG.ORGANIZATIONID AS DELIVERYPARTY,"+
					"QUOTE.SERVICENAME AS SERVICE,"+
					"SERVICE.DISP_NAME AS MIGRATIONSERVICENAME, "+
				    "DECODE (QUOTE.ARCHIVED, '1', 'Archived', NULL ,'(NULL)CONSIDERED_NON_ARCHIVED','0', 'Non_archive','COULD_NOT_DECODE') ARCHIVED,"+
				    "QUOTE.SITE," +
					"QUOTE.HOTCUTNEWSITE, " +
	    		    "SITE.SITECODE, " +
	    		    "SITE.ORANGE_SITENAME," +
	    		    "SITE.ADDRESS_ID," +
	    		    "SITE.CORE_SITE_ID," +
	    		    "SITE.STATUS " +
	    			"FROM " +
	    			ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE " +
	    			"LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID) "+
	    			"LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQMILESTONE MILES ON (MILES.EQ_ORDERGID=QUOTE.TRIL_GID AND MILES.EQ_TITLE='Order Raised Date'),"+
	    			ConnectionBean.getDbPrefix()+"EQ_ORDERSTATUS ORDSTATUS," +
	    			ConnectionBean.getDbPrefix()+"EQ_SITE SITE, " +
	    			ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG," +
	    			ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID) "+
	    			"WHERE QUOTE.QUOTENUMBER = ? AND ORDSTATUS.TRIL_GID = QUOTE.EQ_WFSTATUS " +
	    			"AND ((QUOTE.HOTCUTNEWSITE <> 'NULL' AND QUOTE.HOTCUTNEWSITE =  SITE.TRIL_GID) OR (QUOTE.HOTCUTNEWSITE IS NULL AND QUOTE.SITE = SITE.TRIL_GID )) " +
	    			"AND QUOTE.CONFIGURATIONS = HR.TRIL_GID " +
	    			"AND QUOTE.EQ_DELIVERYPARTY = ORG.TRIL_GID ";
		        
	
		ArrayList<String[]> data = CommonUtils.getQueryResult(QL,comp,order);
		for(String[] arr : data)
		{
			String[] KEYARR = {count,keyOrder};
			if(!consolidatedresult.containsKey(KEYARR))
			{
				ArrayList<String[]> ARR = new ArrayList<String[]>();
				ARR.add(arr);
				consolidatedresult.put(KEYARR, ARR);
				if(null != arr[1] && !arr[1].isEmpty() && arr[1].length()>0)
				{
					getListOrder(count,keyOrder+">>"+arr[1],arr[1],comp,isIncludeUpwards);
				}
				
			}
			
			else
			{
				ArrayList<String[]> ARR = consolidatedresult.get(keyOrder);
				boolean isPriorExists = false;
				for(String[] ar : ARR)
				{
					 if(ar[0].equals(arr[0]))
					 {
						 isPriorExists = true;
						 break;
					 }
				}
				if(!isPriorExists)
				{
					ARR.add(arr);
					consolidatedresult.put(KEYARR, ARR);
					if(null != arr[1] && !arr[1].isEmpty() && arr[1].length()>0)
					{
						getListOrder(count,keyOrder+">>"+arr[1],arr[1],comp,isIncludeUpwards);
					}
					
				}
			}
			
		}
		
		 /*
		 * if check-box is selected , it means the backward and forward order Hierarchy will be identified for given order(s).
		 * 
		 */
	    //if(mLANViewComponents.getIncludeUpwards().isSelected())
		if(isIncludeUpwards)
		{
	    	consolidatedresult = getParentOrder(count,keyOrder+"<<"+keyOrder,keyOrder,comp);
	    }
	
		
		return consolidatedresult;
	}
	
	
	private Map<String[],ArrayList<String[]>> getParentOrder(String count,String keyOrderIn,String order,CustomViewComponents comp) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
	String QL ="SELECT " +
				"QUOTE.QUOTENUMBER," +
				"QUOTE.EQ_GOLDORIGNB," +
				"MILES.EQ_CREATIONDATE AS CREATION_DATE,"+
				"DECODE (QUOTE.EQ_ORDERTYPE, '1', 'New', '2', 'Change','3','Disconnect','COULD_NOT_DECODE') ORDER_TYPE , " +
				CommonUtils.EQ_TYPE_DECODE+
				"ORDSTATUS.EQ_STATUS,"+
				"ORG.ORGANIZATIONID AS DELIVERYPARTY,"+
				"QUOTE.SERVICENAME AS SERVICE,"+
				"SERVICE.DISP_NAME AS MIGRATIONSERVICENAME, "+
				"DECODE (QUOTE.ARCHIVED, '1', 'Archived', '0', 'Non_archive','COULD_NOT_DECODE') ARCHIVED,"+
    			"QUOTE.SITE," +
				"QUOTE.HOTCUTNEWSITE, " +
    			"SITE.SITECODE, " +
    			"SITE.ORANGE_SITENAME," +
    			"SITE.ADDRESS_ID," +
    			"SITE.CORE_SITE_ID," +
    			"SITE.STATUS " +
    			"FROM " +
    			ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE " +
    			"LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID) "+
    			"LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQMILESTONE MILES ON (MILES.EQ_ORDERGID=QUOTE.TRIL_GID AND MILES.EQ_TITLE='Order Raised Date'),"+
    			ConnectionBean.getDbPrefix()+"EQ_ORDERSTATUS ORDSTATUS," +
    			ConnectionBean.getDbPrefix()+"EQ_SITE SITE, " +
    			ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG," +
    			ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID) "+
    			"WHERE QUOTE.EQ_GOLDORIGNB = ? AND ORDSTATUS.TRIL_GID = QUOTE.EQ_WFSTATUS " +
    			"AND ((QUOTE.HOTCUTNEWSITE <> 'NULL' AND QUOTE.HOTCUTNEWSITE =  SITE.TRIL_GID) OR (QUOTE.HOTCUTNEWSITE IS NULL AND QUOTE.SITE = SITE.TRIL_GID )) " +
    			"AND QUOTE.CONFIGURATIONS = HR.TRIL_GID " +
    			//"AND MILES.EQ_ORDERGID=QUOTE.TRIL_GID "+
    			"AND QUOTE.EQ_DELIVERYPARTY = ORG.TRIL_GID ";
		        //"AND MILES.EQ_TITLE='Order Raised Date'" ;
    			
	    ArrayList<String[]> result = CommonUtils.getQueryResult(QL, comp,order);
		if(null != result && result.size() >0)
			{
				for(String[] arr : result)
				{
					String keyOrder = keyOrderIn+"<<"+arr[0];
					String KEYARR[] = {count,keyOrder};
					if(!consolidatedresult.containsKey(KEYARR))
					{
						ArrayList<String[]> ARR = new ArrayList<String[]>();
						ARR.add(arr);
						consolidatedresult.put(KEYARR, ARR);
						getParentOrder(count,keyOrder,arr[0],comp);
					}
					else
					{
						ArrayList<String[]> ARR = consolidatedresult.get(keyOrder);
						boolean isPriorExists = false;
						for(String[] ar : ARR)
						{
							 if(ar[0].equals(arr[0]))
							 {
								 isPriorExists = true;
								 break;
							 }
						}
						if(!isPriorExists)
						{
							ARR.add(arr);
							consolidatedresult.put(KEYARR, ARR);
							getParentOrder(count,keyOrder,arr[0],comp);
						}
					}
				}
			}
		
	
	return consolidatedresult;
	}

	
	private ArrayList<String[]>  getQueryResults(Map<String[],ArrayList<String[]>> data)
    {
    	ArrayList<String[]> consolidatedData = new ArrayList<String[]> ();
    	Iterator<Map.Entry<String[],ArrayList<String[]>>> entries = data.entrySet().iterator();
		while (entries.hasNext()) 
		{
			Map.Entry<String[], ArrayList<String[]>> entry = entries.next();
			String[] key = entry.getKey();
			ArrayList<String[]> value = entry.getValue();
			for(String[] arr : value)
			{
			String[] newarray = new String[arr.length+2];
			newarray[0] = key[0];
			newarray[1] = key[1];
			for(int x = 0 ; x<arr.length ; x++ )
			{
				newarray[x+2] = arr[x];
				
			}
			consolidatedData.add(newarray);
			}
		}
		
		return consolidatedData;
    }
	
}
