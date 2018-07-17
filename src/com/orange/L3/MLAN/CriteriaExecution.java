package com.orange.L3.MLAN;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.ui.component.MLANViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ProgressMonitorPane;

public class CriteriaExecution {

	
	private Map<String,String[]> orderMap;
	
	ArrayList<String[]> serviceBuildItems;
	
	private int totallines =0;
	
	public CriteriaExecution(MLANViewComponents mLANViewComponents,String filePath) throws Exception{
		serviceBuildItems = new ArrayList<String[]>();
		initOrderDetailsMap(filePath);
		FileInputStream fstream = new FileInputStream(filePath);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String strLine;
		 int count =0;
		//Read File Line By Line
		while ((strLine = br.readLine()) != null)   
		{
		
			count++;
			ProgressMonitorPane.getInstance().setProgress(count,(double)totallines);	
		  
			String S = strLine;
		 
		  String ENV = S.substring(0,S.indexOf(":"));
		  String QUERY = "";
		  
		  if(ENV.equals("GOLD"))
		  {
			  QUERY = S.substring(S.indexOf(":")+1,S.length());
			  serviceBuildItems.addAll(CommonUtils.getQueryResult(QUERY, mLANViewComponents));
		  }
		  
		  else if(ENV.equals("ARCHIVE"))
		  {
			  if(null == orderMap || orderMap.size()==0)
			  {
				  mLANViewComponents.getQueryResult().append("\nERROR >> No Order Criteria found.");
			  }
			 
			 
			  String ORD = S.substring(S.indexOf(":")+1,S.indexOf(">"));
			  QUERY = S.substring(S.indexOf(">")+1,S.length());
			  ArrayList<String[]> archiveOrdersLineItems = CommonUtils.getArchiveQueryResult(QUERY, mLANViewComponents);
			 
			  for(String[] RR : archiveOrdersLineItems)
				{
				    
				    String[] ARR = new String[ServiceBuildInfo.columnArray.length];
					if(orderMap.containsKey(ORD))
					{
						String mapData[] = orderMap.get(ORD);
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
						
					}
					else
					{
						mLANViewComponents.getQueryResult().append("\nWarning >> No Order found for criteria "+ORD);
						ARR[0] = ORD;
						for(int x = 1 ; x< (ARR.length-6);x++)
						{
							ARR[x] = "NO ORDER FOUND FOR CRITERIA.";
						}
						ARR[ARR.length-6] = RR[1];
						ARR[ARR.length-5] = RR[2];
						ARR[ARR.length-4] = RR[3];
						ARR[ARR.length-3] = RR[4];
						ARR[ARR.length-2] = RR[5];
						ARR[ARR.length-1] = RR[6];
					}
				serviceBuildItems.add(ARR);
				}
		  }
		  else if(ENV.equals("ARCHIVED_FILE"))
		  {
			  
		  }
		  else if ( ENV.equals("DYN_ARCHIVE") || ENV.equals("DYN_ARCHIVE_ORDERS") || ENV.equals("DYN_ARCHIVE_NOT_IN") || ENV.equals("DYN_ARCHIVE_ORDERS_DATE_RANGE"))
		  {
			  // these keys are only for DYn criteria execution
		  }
		  else
		  {
			  mLANViewComponents.getQueryResult().append("\nERROR >> Criteria file is not uoto mark."+ENV);
			  JOptionPane.showMessageDialog(mLANViewComponents.getPanel(),"Criteria file is not upto mark.\n Can't execute."+ENV );
		  }
		}
		fstream.close();
		br.close();
		
		
		String header[]=ServiceBuildInfo.columnArray;
		int randomNum = 999 + (int)(Math.random() * ((999999 - 999) + 1));
		String updatedPath = filePath.substring(0,filePath.lastIndexOf("."));
		String logpath = updatedPath.substring(0,updatedPath.lastIndexOf(File.separator));
		String path=updatedPath+"_"+randomNum+".csv";
		
		CommonUtils.showTable(header, ServiceBuildInfo.updateOnHoldNArchiveInfo(serviceBuildItems, mLANViewComponents), path);
		mLANViewComponents.getQueryResult().append("\nStep Completed Successfully..for criteria(s) >>"+filePath);
		mLANViewComponents.getQueryResult().append("\nFile export Successfully.."+path);
	    CommonUtils.createConsoleLogFile(mLANViewComponents,logpath);
	    CommonUtils.formatFile(mLANViewComponents,path);
	    
		
	}
	private void initOrderDetailsMap(String filePath) throws IOException
	{
		orderMap = new HashMap<String,String[]>();
		FileInputStream fstream = new FileInputStream(filePath);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;

		//Read File Line By Line
		while ((strLine = br.readLine()) != null)   
		{
		  String S = strLine;
		  totallines++;
		  String ENV = S.substring(0,S.indexOf(":"));
		  if(ENV.equals("ARCHIVED_FILE"))
		  {
			  String orderfileName = S.substring(S.indexOf(":")+1,S.length());
			  // INIT the Map with the file  
				  CSVReader reader = new CSVReader(new FileReader(orderfileName));
				  String []cols = reader.readNext();  
				  while ((cols = reader.readNext()) != null) 
					{
					  orderMap.put(cols[0], cols);
					}
				  reader.close();  
				  
			  }
		}
		 fstream.close();
		 br.close();
	}
	
	
	
}
