package com.orange.util.others;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.ui.component.SiteFixedViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.CustomCSVWriter;
import com.orange.util.ProgressMonitorPane;
import com.orange.util.csm.ConnectionForGOLD;

public class ProductOrdersHierarchy {

	private String columns[]={"GID","QUOTENUMBER","EQ_GOLDORIGNB","ORDEREDSERVICE","MIGRATIONSERVICE","SITE_CONT","SITE","HOTCUTNEWSITE","ADDRESS_ID","CORE_SITE_ID","ORGANIZATIONID","SITECODE","ORANGE_SITENAME","ANALYSIS_RESULT"};
	private LinkedHashMap<String,ArrayList<String[]>> ordDetailsMap;
	private ArrayList<String> orders;
	private LinkedHashMap<String,ArrayList<String>> ordHierMap;
	
	
	private String QUERY_ORDERS ;
	private String QUERY_ORDERS_HIERARCHY;
	private SiteFixedViewComponents siteFixedViewComponents;
	
	private ArrayList<String[]> updatedRowWithAnalysis ;
	
	public ProductOrdersHierarchy(SiteFixedViewComponents siteFixedViewComponents) throws SQLException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException 
	{
		this.siteFixedViewComponents = siteFixedViewComponents;
		String readerFilePath = siteFixedViewComponents.getFileToValidate().getText();
		
		String prodName = prodName(readerFilePath);
		
		QUERY_ORDERS = "select quotenumber from "+ConnectionBean.getDbPrefix()+"sc_quote where ORDEREDSERVICE in (select tril_gid from "+ConnectionBean.getDbPrefix()+"eq_service where DISP_NAME in ("+prodName+"))" +
		" or MIGRATIONSERVICE in(select tril_gid from "+ConnectionBean.getDbPrefix()+"eq_service where DISP_NAME in ("+prodName+")) " +
		" order by quotenumber desc";
	
		QUERY_ORDERS_HIERARCHY = "select quotenumber,eq_goldorignb from "+ConnectionBean.getDbPrefix()+"sc_quote where quotenumber=?";
		
		
		siteFixedViewComponents.getQueryResult().append("Process 1/5 statred...\n");
		ArrayList<String> orderList = getListOfOrders(QUERY_ORDERS);
		/*
		orderList = new ArrayList<String>();
		orderList.add("921523");
		orderList.add("921708");
		orderList.add("923449");
		/*
		
		orderList.add("872589");
		orderList.add("871405");
		orderList.add("868632");
		orderList.add("849681");
		orderList.add("868631");
		orderList.add("871909");
		orderList.add("813381");
		orderList.add("123456");
		*/
		
		
		
		siteFixedViewComponents.getQueryResult().append("Process 2/5 statred...\n");
		LinkedHashMap<String,ArrayList<String>> orderHierarchyMap = getOrderHierarchy(orderList);
		
		siteFixedViewComponents.getQueryResult().append("Process 3/5 statred...\n");
		LinkedHashMap<String,ArrayList<String[]>> orderDetailsMap = getOrderDetails(orderHierarchyMap);
		
		siteFixedViewComponents.getQueryResult().append("Process 4/5 statred...\n");
		ArrayList<String[]> upwithAnalysis = analysisOrderDetails(orderDetailsMap);
		// write this method
		
		String path = readerFilePath;	
		int index = path.lastIndexOf(".");
		String sub = path.substring(0,index) ;
		String analysysiFilePath=sub+"_Analysis.csv";
		siteFixedViewComponents.getQueryResult().append("Process 5/5 statred...\n");
		
		writeCSV(upwithAnalysis,analysysiFilePath);
		
		siteFixedViewComponents.getQueryResult().append("All Process completed....."+analysysiFilePath+"\n");
		CommonUtils.createConsoleLogFile(siteFixedViewComponents);
	}
	
	private void addOrderDetailsInMap(String key,String[] value){
		if(ordDetailsMap.containsKey(key))
		{
			ArrayList<String[]> exisitingValues = ordDetailsMap.get(key);
			exisitingValues.add(value);
			ordDetailsMap.put(key, exisitingValues);
			
		}
		else
		{
			ArrayList<String[]> exisitingValues = new ArrayList<String[]>();
			exisitingValues.add(value);
			ordDetailsMap.put(key, exisitingValues);
			
			
		}
	}
	private boolean addOrderHirInMap(String key,String value){
    	boolean isUpdate = true;
		if(ordHierMap.containsKey(key))
		{
			ArrayList<String> exisitingValues = ordHierMap.get(key);
			for(String existingOrder : exisitingValues)
			{
				if(existingOrder.equals(value))
				{
					isUpdate =  false;
					break;
				}
			}
			if(isUpdate)
			{
			exisitingValues.add(value);
			ordHierMap.put(key, exisitingValues);
			}
			
		}
		else{
			ArrayList<String> exisitingValues = new ArrayList<String>();
			exisitingValues.add(value);
			ordHierMap.put(key, exisitingValues);
			isUpdate = true;
			
		}
		return isUpdate;
	}
	
	
	
	private ArrayList<String[]>  analysisOrderDetails(LinkedHashMap<String,ArrayList<String[]>> orderDetailsMap){
		
		updatedRowWithAnalysis = new ArrayList<String[]>();
		Iterator<Map.Entry<String, ArrayList<String[]>>> entries = orderDetailsMap.entrySet().iterator();

		double counts = 0.0;
		
		while (entries.hasNext()) 
		{
			
			counts++;	
			ProgressMonitorPane.getInstance().setProgress(counts,orderDetailsMap.size());	
			
			
			Map.Entry<String, ArrayList<String[]>> entry = entries.next();
			String key = entry.getKey();
			
			ArrayList<String[]> values = entry.getValue();
		
			ArrayList<Integer> newsitecount = new ArrayList<Integer>();
			int count = 0;
			
			for(String[] row : values)
			{
				
				if(null != row[4] && row[4].trim().equalsIgnoreCase("NEW"))
				{
					newsitecount.add(count);
				}
				count++;
			}
			// create sub maps if no new found
			if(newsitecount.size()== 0)
			{
				String[] masterrow = values.get(values.size()-1);
				for(int x =0 ; x < values.size() ; x++)
				{
					String[] row = values.get(x);
					
					String rsite = row[5];
					String mnewsite = masterrow[5];
				
					
					String[] updatedRow = new String[row.length+2];
					
					updatedRow[0] = key;
					
					
					for(int X = 0 ; X  <row.length ; X++)
					{
						updatedRow[X+1] = row[X];
					}
					
					updatedRowWithAnalysis.add(updatedRow(updatedRow,row,mnewsite,rsite,values));
			
				}
			}
			else
			{
				// Assume [0,3]
				// Now add till Last of the group
				newsitecount.add(values.size()-1);
				// [0,3,10]
				Collections.reverse(newsitecount);
				//[10,3,0]
				
				
				/* 
				 * check if last element is not zero , then add 0
				 * adding zero will facilitate to iterate loop till end to start 
				*/
				
				int lastValue = newsitecount.get(newsitecount.size()-1)  ;
				if(lastValue != 0 )
				{
					newsitecount.add(0);
				}
				//[10,3,0]
				
				/*
				 * for(int i =0 ; i < 2 ; i++)
				 * {
				 * for(int I = 10;I<=3;I--)
				   for(int I = 3;I<=0;I--)
				 * 
				 * String[] masterrow = values.get(10);
				 * 
				 *  String[] row = values.get(10:9:8:7:6:5:4:3);
				 * and
				 * String[] row = values.get(3:2:1:0);
				 * }
				 * 
				 */
				
				
				
			   for(int i=0;i<newsitecount.size()-1;i++)
			   {
				   String[] masterrow = values.get(newsitecount.get(i));
				   String manewsite = masterrow[5];
				 
				   for(int I = (newsitecount.get(i)== 0 ?  newsitecount.get(i) : (newsitecount.get(i)-1));I >=newsitecount.get(i+1);I--)
				   {
					   
					   String[] row = values.get(I);
					   String rsite = row[5];
					   
					   String[] updatedRow = new String[row.length+2];
					   updatedRow[0] = key;
						
						for(int X = 0 ; X  <row.length ; X++)
						{
							updatedRow[X+1] = row[X];
						}
						
						updatedRowWithAnalysis.add(updatedRow(updatedRow,row,manewsite,rsite,values));
				   }
			   }
			}
		
			
		}
	return updatedRowWithAnalysis;
	
	
	}
	
	private void getInternalOrders (String order,int GID) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
    {
    	ArrayList<String[]> results = 	CommonUtils.getQueryResult(QUERY_ORDERS_HIERARCHY, siteFixedViewComponents,order);
        
    	if(null != results && results.size()>0)
		{
			for(String[] row : results)
			{
				String leadOrd = row[0]; 
				String goldOrgNB = row[1];
				// 813381 and 751506
				boolean isUpdate = addOrderHirInMap(""+GID,leadOrd);
				if(isUpdate)
				{
					if(!CommonUtils.isNULL(goldOrgNB))
					{
						getInternalOrders(goldOrgNB,GID);
					}
				}
				else
				{
					System.out.println("SKIP for "+leadOrd +" As interlinking found.....");
					return;
				}
			}
		}
    }
	
	private ArrayList<String> getListOfOrders(String QUERY_ORDERS ) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		
		orders = new ArrayList<String>();

		ArrayList<String[]> results = 	CommonUtils.getQueryResult(QUERY_ORDERS, siteFixedViewComponents);
		double count = 0.0; 
		if(null != results && results.size()>0)
		{
			for(String[] row : results)
			{
				count++;	
				ProgressMonitorPane.getInstance().setProgress(count,results.size());	
				orders.add(row[0]);
			}
		}
		return orders;
	}


	private LinkedHashMap<String,ArrayList<String[]>> getOrderDetails(Map<String,ArrayList<String>> orderHierarchyMap) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
		 ordDetailsMap = new LinkedHashMap<String,ArrayList<String[]>>();
		 
		 Iterator<Map.Entry<String, ArrayList<String>>> entries = orderHierarchyMap.entrySet().iterator();

			double count = 0.0; 
			while (entries.hasNext()) 
			{
				
				count++;	
				ProgressMonitorPane.getInstance().setProgress(count,orderHierarchyMap.size());	
				
				Map.Entry<String, ArrayList<String>> entry = entries.next();
				String key = entry.getKey();
				ArrayList<String> value = entry.getValue();
				
				for(String order : value)
				{
					// get the Hotcutnewsite details of order
					String localQueryFromSite ="SELECT QUOTE.SITEDETAILS FROM "+ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE WHERE QUOTE.QUOTENUMBER='"+order+"'";
					Statement pstmt = ConnectionForGOLD.getStatement();
					String upquery = localQueryFromSite.replace("?", "'"+order +"'");
					siteFixedViewComponents.getQueryResult().append(upquery+"\n");
					ResultSet resultSet = pstmt.executeQuery(localQueryFromSite);

					while(resultSet.next())
					{
						String SITEDETAILS = resultSet.getString(1);
						
						if(null != SITEDETAILS && SITEDETAILS.trim().equalsIgnoreCase("NEW"))
						{
							String query =  "SELECT QUOTE.QUOTENUMBER,QUOTE.EQ_GOLDORIGNB,QUOTE.ORDEREDSERVICE,QUOTE.MIGRATIONSERVICE,QUOTE.SITEDETAILS,QUOTE.SITE,QUOTE.HOTCUTNEWSITE,SITE.ADDRESS_ID,SITE.CORE_SITE_ID,ORG.ORGANIZATIONID,SITE.SITECODE,SITE.ORANGE_SITENAME FROM "+ConnectionBean.getDbPrefix()+"EQ_SITE SITE , "+ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG , "+ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE WHERE QUOTE.QUOTENUMBER=? AND ORG.TRIL_GID = SITE.EQ_SITEOF AND SITE.TRIL_GID = QUOTE.HOTCUTNEWSITE";
						
							ArrayList<String[]> newsiteresults = 	CommonUtils.getQueryResult(query, siteFixedViewComponents,order);
							if(null != newsiteresults && newsiteresults.size()>0)
							{
								for(String[] row : newsiteresults)
								{
									addOrderDetailsInMap(key,row);
								}
							}
						}
						else
						{
							String query = "SELECT QUOTE.QUOTENUMBER,QUOTE.EQ_GOLDORIGNB,QUOTE.ORDEREDSERVICE,QUOTE.MIGRATIONSERVICE,QUOTE.SITEDETAILS,QUOTE.SITE,QUOTE.HOTCUTNEWSITE,SITE.ADDRESS_ID,SITE.CORE_SITE_ID,ORG.ORGANIZATIONID,SITE.SITECODE,SITE.ORANGE_SITENAME FROM "+ConnectionBean.getDbPrefix()+"EQ_SITE SITE , "+ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG , "+ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE WHERE QUOTE.QUOTENUMBER=? AND ORG.TRIL_GID = SITE.EQ_SITEOF AND SITE.TRIL_GID = QUOTE.SITE";
							
							ArrayList<String[]> newsiteresults = 	CommonUtils.getQueryResult(query, siteFixedViewComponents,order);
							if(null != newsiteresults && newsiteresults.size()>0)
							{
								for(String[] row : newsiteresults)
								{
									String[] updatedrow = row;
									updatedrow[4] ="EXISTING";
									addOrderDetailsInMap(key,updatedrow);
								}
							}
						}
					}
					resultSet.close();
					pstmt.close();
				}
			}
			return ordDetailsMap;
	}
	
    private LinkedHashMap<String,ArrayList<String>> getOrderHierarchy(ArrayList<String> orderList) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
		ordHierMap = new LinkedHashMap<String,ArrayList<String>>();
		double count = 0.0; 
		int GID =1;
		for(String order : orderList)
		{
			ArrayList<String[]> results = 	CommonUtils.getQueryResult(QUERY_ORDERS_HIERARCHY, siteFixedViewComponents,order);
			count++;	
			ProgressMonitorPane.getInstance().setProgress(count,orderList.size());	
			
			if(null != results && results.size()>0)
			{
				for(String[] row : results)
				{
					String leadOrd = row[0]; 
					String goldOrgNB = row[1]; 
					
					addOrderHirInMap(""+GID,leadOrd);
					
					if(!CommonUtils.isNULL(goldOrgNB))
					{
						getInternalOrders(goldOrgNB,GID);
					}
					GID++;
				}
			}
		}
		return ordHierMap;
	}
	
	private String prodName(String filePath) throws IOException{
		String prods="";
		CSVReader csvReader = new CSVReader(new FileReader(filePath));
		String[] columnHeader = csvReader.readNext();
		while ((columnHeader = csvReader.readNext()) != null) {
			prods+="'"+columnHeader[0].trim()+"',";
		}
		csvReader.close();
		prods = prods.substring(0,prods.length()-1);
		return prods;
	}
    
    private String[] updatedRow(String[] updateRow,String[] row,String manewsite,String rsite,ArrayList<String[]> values){
		
		if(CommonUtils.isNULL(manewsite))
		{
			updateRow[row.length+1]="Could not Match:New Site Null";
		}
		
		else if(CommonUtils.isNULL(rsite))
		{
			updateRow[row.length+1]="Could not Match: Site Null";
		}
		else if(manewsite.equals(rsite))
		{
			updateRow[row.length+1]="Match";
		}
		else
		{
			updateRow[row.length+1]="Not Match";
		}
		
		return updateRow;
	}
    
    private void writeCSV(ArrayList<String[]> updatedRowWithAnalysis,String analysisFileLoc) throws IOException
	{
		
		CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(analysisFileLoc),true);
		writer.writeNext(columns);
		double count = 0.0;
		for(String[] row : updatedRowWithAnalysis){
			
			count++;	
			ProgressMonitorPane.getInstance().setProgress(count,updatedRowWithAnalysis.size());	
			writer.writeNext(row);
		}
	 writer.close();
	}
    
}
