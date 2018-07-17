package com.orange.L3.MLAN;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JOptionPane;

import com.orange.ui.component.MLANViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ProgressMonitorPane;

public class DynamicCriteriaExecution {

	private MLANViewComponents mLANViewComponents;
	private Map<String,String[]> orderMap;
	
	private Map<String,String[]> orderMapProxy;
	private Map<String,String[]> orderMapWithDateRangeFilter;
	private ArrayList<String[]> serviceBuildItems;
	
	private int totallines =0;
	
	public DynamicCriteriaExecution(MLANViewComponents mLANViewComponents,String filePath) throws Exception{
		
		this.mLANViewComponents = mLANViewComponents;
		serviceBuildItems = new ArrayList<String[]>();
		initOrderDetailsMap(filePath);
		FileInputStream fstream = new FileInputStream(filePath);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String strLine;
		
		//Read File Line By Line
		while ((strLine = br.readLine()) != null)   
		{
		
		  String S = strLine;
		 
		  String ENV = S.substring(0,S.indexOf(":"));
		  String QUERY = "";
		  int count =0;
		  if(ENV.equals("GOLD"))
		  {
			  QUERY = S.substring(S.indexOf(":")+1,S.length());
			  serviceBuildItems.addAll(CommonUtils.getQueryResult(QUERY, mLANViewComponents));
		  }
		 
		  else if(ENV.equals("DYN_ARCHIVE") || ENV.equals("DYN_ARCHIVE_NOT_IN"))
		  {
			  if(null == orderMap || orderMap.size()==0)
			  {
				  mLANViewComponents.getQueryResult().append("\nERROR >> No Order Criteria found.");
			  }
			    QUERY = S.substring(S.indexOf(":")+1,S.length());
			  
				Iterator<Map.Entry<String, String[]>> entries1 = orderMap.entrySet().iterator();
				while (entries1.hasNext()) 
				{
					count++;
					ProgressMonitorPane.getInstance().setProgress(count,(double)totallines);	
					Map.Entry<String, String[]> entry = entries1.next();
					String key = entry.getKey();
					String[] mapData = entry.getValue();
					ArrayList<String[]> archiveOrdersLineItems = CommonUtils.getArchiveQueryResult(QUERY,mLANViewComponents,key);
					
					 for(String[] RR : archiveOrdersLineItems)
						{
						String[] ARR = new String[ServiceBuildInfo.columnArray.length];
						for(int x = 0 ; x< (ARR.length-6);x++)
						{
							ARR[x] = mapData[x];
						}
						ARR[ARR.length-6] = RR[1];
						ARR[ARR.length-5] = RR[2];
						ARR[ARR.length-4] = RR[3];
						ARR[ARR.length-3] = RR[4];
						ARR[ARR.length-2] = RR[5];
						ARR[ARR.length-1] = RR[6];
						serviceBuildItems.add(ARR);
						}
				}
		  }
		  else if(ENV.equals("ARCHIVED_FILE") || ENV.equals("ARCHIVE") || ENV.equals("DYN_ARCHIVE_ORDERS") || ENV.equals("DYN_ARCHIVE_ORDERS_DATE_RANGE"))
		  {
			  
		  }
		 
		  else
		  {
			  mLANViewComponents.getQueryResult().append("\nERROR >> Criteria file is not upto mark.");
			  JOptionPane.showMessageDialog(mLANViewComponents.getPanel(),"Criteria file is not upto mark.\n Can't execute.\n"+ENV );
		  }
		}
		fstream.close();
		br.close();
		
		
		String header[]=ServiceBuildInfo.columnArray;
		int randomNum = 999 + (int)(Math.random() * ((999999 - 999) + 1));
		String updatedPath = filePath.substring(0,filePath.lastIndexOf("."));
		String logpath = updatedPath.substring(0,updatedPath.lastIndexOf(File.separator));
		String path=updatedPath+"_DYNAMIC_"+randomNum+".csv";
		CommonUtils.showTable(header, ServiceBuildInfo.updateOnHoldNArchiveInfo(serviceBuildItems, mLANViewComponents), path);
		mLANViewComponents.getQueryResult().append("\nStep Completed Successfully..for dynamic criteria(s) >>"+filePath);
		mLANViewComponents.getQueryResult().append("\nFile export Successfully.."+path);
		System.out.println("\nFile export Successfully.."+path);
	    CommonUtils.createConsoleLogFile(mLANViewComponents,logpath);
	    CommonUtils.formatFile(mLANViewComponents,path);
	}
	private void initOrderDetailsMap(String filePath) throws IOException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		orderMap = new HashMap<String,String[]>();
		orderMapWithDateRangeFilter= new HashMap<String,String[]>();
		orderMapProxy = new HashMap<String,String[]>();
		
		FileInputStream fstream = new FileInputStream(filePath);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;

	
		
		//Read File Line By Line
		while ((strLine = br.readLine()) != null)   
		{
		  
		  String S = strLine;
		  String ENV = S.substring(0,S.indexOf(":"));
		  if(ENV.equals("DYN_ARCHIVE_ORDERS"))
		  {
			  String QUERY = S.substring(S.indexOf(":")+1,S.length());
			  ArrayList<String[]> listofOrders = CommonUtils.getQueryResult(QUERY, mLANViewComponents);
			  
			  for(String[] order : listofOrders)
			  {
				  orderMapProxy.put(CommonUtils.addZeroPrefixinOrder(order[0]),order);
				  orderMap.put(CommonUtils.addZeroPrefixinOrder(order[0]),order);
			  }
			  // get the list of archive orders
		  }
		}
		 fstream.close();
		 br.close();
		 
		    boolean isUserSelectedDateRange = false;
		    FileInputStream fstreambyDate = new FileInputStream(filePath);
			BufferedReader brbyDate = new BufferedReader(new InputStreamReader(fstreambyDate));
			String strLinebyDate;

			//Read File Line By Line
			while ((strLinebyDate = brbyDate.readLine()) != null)   
			{
				  String S = strLinebyDate;
				  String ENV = S.substring(0,S.indexOf(":"));
				  if(ENV.equals("DYN_ARCHIVE_ORDERS_DATE_RANGE"))
				  {
					  isUserSelectedDateRange = true;
					  String QUERY = S.substring(S.indexOf(":")+1,S.length());
					  
					  int count = 0;
					  Iterator<Map.Entry<String, String[]>> entries1 = orderMap.entrySet().iterator();
						while (entries1.hasNext()) 
						{
							count++;
							ProgressMonitorPane.getInstance().setProgress(count,(double)orderMap.size());	
							Map.Entry<String, String[]> entry = entries1.next();
							String key = CommonUtils.addZeroPrefixinOrder(entry.getKey());
					  
							ArrayList<String[]> listofOrdersbyDateRange = CommonUtils.getArchiveQueryResult(QUERY, mLANViewComponents,key);
							if(null != listofOrdersbyDateRange && listofOrdersbyDateRange.size() !=0)
							{
								orderMapWithDateRangeFilter.put(key, orderMapProxy.get(key));
							}
						}
					 
					  
					  
				  }
			}
			fstreambyDate.close();
			brbyDate.close();
		 
		    if(isUserSelectedDateRange)
		    {
		    	/*
		    	 * 
		    	 * if User selects Data Range then orderMapWithDateRangeFilter should use. 
		    	 * 
		    	 */
		    	orderMap = new HashMap<String,String[]>(orderMapWithDateRangeFilter);
		    }
		    else
		    {
		    	/*
		    	 * 
		    	 * if User didn't selects Data Range then previously initialized orderMap should use. 
		    	 * 
		    	 */
		    }
		    /*
		     * both below map should be null, as not required more.
		     */
		    orderMapProxy = null;
		    orderMapWithDateRangeFilter = null;
		    isUserSelectedDateRange = false;
			totallines = orderMap.size();
	}
	
	
	
}
