package com.orange.util.csm;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.orange.ui.component.SiteFixedViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBeanCSI;
import com.orange.util.CustomCSVWriter;
import com.orange.util.ProgressMonitorPane;

public class GOLD_CSI_SYNC_SEUSID_EX_Checks {

	private SiteFixedViewComponents siteFixViewComponents;
	
	
	public GOLD_CSI_SYNC_SEUSID_EX_Checks(String usidServiceExtractPath,SiteFixedViewComponents siteFixViewComponents) throws Exception
	{
		this.siteFixViewComponents = siteFixViewComponents;
		siteFixViewComponents.getQueryResult().append("USID Service Extract Validation Process Started.. \n");
		getUSIDServiceExtractValidate(usidServiceExtractPath);
		siteFixViewComponents.getQueryResult().append("USID Service Extract Validation Completed.. "+usidServiceExtractPath+"\n");
	}
	
	private ArrayList<String> getSiteHandleFromServiceElement(String usid) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
		String localQueryFromSite = "SELECT DISTINCT v.CUSTHANDLE , v.SITEHANDLE ,v.SERVICEHANDLE,v.ORDHANDLE FROM   "+ConnectionBeanCSI.getDbPrefix()+"CSERVICEELEMENT se , "+ConnectionBeanCSI.getDbPrefix()+"CVERSIONSERVICEELEMENT vse , "+ConnectionBeanCSI.getDbPrefix()+"CVERSION v WHERE  se.SERVICEELEMENTID = vse.SERVICEELEMENTID AND vse.VERSIONID = v.VERSIONID AND se.USID = ?";
		
		PreparedStatement pstmt = ConnectionForCSI.getPreparedStatement(localQueryFromSite);
		pstmt.setString(1,usid);
		siteFixViewComponents.getQueryResult().append(localQueryFromSite.replace("?", usid)+"\n");
		
		ResultSet resultset = pstmt.executeQuery();
	
		ArrayList<String> data = new ArrayList<String>();
		while(resultset.next())
		{
			data.add(resultset.getString(1));
			data.add(resultset.getString(2));
			data.add(resultset.getString(3));
			data.add(resultset.getString(4));
			
		}
		resultset.close();
		pstmt.close();
		return data;
	}
	
	private ArrayList<ArrayList<String>> getUSIDExtract() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		String localQueryFromSite = "SELECT COUNT(*) FROM (SELECT DISTINCT " +
		        "ele.SERVICEELEMENTID,"+
				"V.SITEHANDLE, " +
				"V.ADDRESSID , " +
				"V.CORESITEID ," +
				"V.CUSTHANDLE," +
				"V.ORDHANDLE," +
				"V.SERVICEHANDLE," +
				"ele.USID ," +
				"ele.SERVICEELEMENTCLASS " +
				"FROM "+ConnectionBeanCSI.getDbPrefix()+"CSERVICEELEMENT ele, "+ConnectionBeanCSI.getDbPrefix()+"CVERSIONSERVICEELEMENT vele, "+ConnectionBeanCSI.getDbPrefix()+"CVERSION v " +
						"WHERE vele.VERSIONID = v.VERSIONID " +
						"and ele.SERVICEELEMENTID = vele.SERVICEELEMENTID " +
						"and ele.serviceelementclass not in('CPE', 'NASBackup', 'AccessConnection','BackupOptions') " +
						"and ele.usid like '%::%::%::%')" ;
						//"where rownum between  1 and 1000";
		
		
		Statement pstmt = ConnectionForCSI.getStatement();
		siteFixViewComponents.getQueryResult().append(localQueryFromSite+"\n");
		
		ResultSet resultSet = pstmt.executeQuery(localQueryFromSite);
		double usidExtractDataCount = 0.0;
		
		while(resultSet.next())
		{
			usidExtractDataCount = resultSet.getDouble(1);	
		}
		resultSet.close();
		
		localQueryFromSite = "SELECT DISTINCT " +
        "ele.SERVICEELEMENTID,"+
		"V.SITEHANDLE, " +
		"V.ADDRESSID , " +
		"V.CORESITEID ," +
		"V.CUSTHANDLE," +
		"V.ORDHANDLE," +
		"V.SERVICEHANDLE," +
		"ele.USID ," +
		"ele.SERVICEELEMENTCLASS " +
		"FROM "+ConnectionBeanCSI.getDbPrefix()+"CSERVICEELEMENT ele, "+ConnectionBeanCSI.getDbPrefix()+"CVERSIONSERVICEELEMENT vele, "+ConnectionBeanCSI.getDbPrefix()+"CVERSION v " +
				"WHERE vele.VERSIONID = v.VERSIONID " +
				"and ele.SERVICEELEMENTID = vele.SERVICEELEMENTID " +
				"and ele.serviceelementclass not in('CPE', 'NASBackup', 'AccessConnection','BackupOptions') " +
				"and  ele.usid like '%::%::%::%' " ;
				//"and rownum between  1 and 1000";
		
		resultSet = pstmt.executeQuery(localQueryFromSite);
		
		ArrayList<ArrayList<String>> usids = new ArrayList<ArrayList<String>>();
		double count = 0.0;
		while(resultSet.next())
		{
			
			count++;	
			ProgressMonitorPane.getInstance().setProgress(count,usidExtractDataCount);	
			
			String SERVICEELEMENTID = resultSet.getString(1);
			String SITEHANDLE = resultSet.getString(2);
			String ADDRESSID = resultSet.getString(3);
			String CORESITEID = resultSet.getString(4);
			String CUSTHANDLE= resultSet.getString(5);
			String ORDHANDLE= resultSet.getString(6);
			String SERVICEHANDLE= resultSet.getString(7);
			String USID = resultSet.getString(8);
			String SERVICEELEMENTCLASS = resultSet.getString(9);
			
						
			ArrayList<String> usidElements = CommonUtils.getUSIDElements(USID);
				if(usidElements.size()>3)
				{
					String ico = usidElements.get(1);
					String sitecode = usidElements.get(2);
					String service = usidElements.get(3);
					
					
					
					
					/*
					 * Now this service element to be pass in a query to get the sitehandle linked with.
					 * 
					 * if sitecode element of USID and sitehandle linked with service, 
					 * is equal >> then pass
					 * if not equal  >> then it mean Failed.
					 * 
					 */
					
					ArrayList<String> sitehandleLinked = getSiteHandleFromServiceElement(USID);
					String failedComments ="";
					if(null == sitehandleLinked || sitehandleLinked.size()<1)
					{
						failedComments =">> No details found for USID "+USID;
					}
					else
					{
						if(!ico.equals(sitehandleLinked.get(0)))
						{
							failedComments = ">>CUSTOMER MISMATCH:USID "+USID+" belongs to customer "+sitehandleLinked.get(0)+", but given "+ico+" customer.";
						}
						if(!sitecode.equals(sitehandleLinked.get(1)))
						{
							failedComments+= ">>SITECODE MISMATCH:USID "+USID+" belongs to site "+sitehandleLinked.get(1)+", but given "+sitecode+" site.";
						}
						if(!service.equals(sitehandleLinked.get(2)))
						{
							failedComments+= ">>SERVICE MISMATCH:USID "+USID+" belongs to service "+sitehandleLinked.get(2)+", but given "+service+" service.";
						}
						if(usidElements.size()>4)
						{
							String order = usidElements.get(4);
							if(!order.equals(sitehandleLinked.get(3)))
							{
								failedComments+= ">>ORDER MISMATCH:USID "+USID+" belongs to Order "+sitehandleLinked.get(3)+", but given "+order+" order.";
							}
						}
					}
						ArrayList<String> usid = new ArrayList<String>(); 
						usid.add(SERVICEELEMENTID);
						usid.add(SITEHANDLE);
						usid.add(ADDRESSID);
						usid.add(CORESITEID);
						usid.add(CUSTHANDLE);
						usid.add(ORDHANDLE);
						usid.add(SERVICEHANDLE);
						usid.add(USID);
						usid.add(SERVICEELEMENTCLASS);
						usid.add(failedComments);
						usids.add(usid);
						usid = null;
			}
		}
		resultSet.close();
		pstmt.close();
		return usids;
	}

	private void getUSIDServiceExtractValidate(String csvFileName) throws Exception
	{
		
		String columns[] ={"SERVICEELEMENTID","SITEHANDLE","ADDRESSID","CORESITEID","CUSTHANDLE","ORDHANDLE",
				"SERVICEHANDLE","USID","SERVICEELEMENTCLASS","COMMENTS"};
		
		CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(csvFileName),true);
		writer.writeNext(columns);
		
		ArrayList<ArrayList<String>> usidServiceCheckRows = getUSIDExtract();
		
	    for(ArrayList<String> row : usidServiceCheckRows)
	    {
	    	
	    	String arr[] = new String[row.size()];
	    	int X=0;
	    	for(String S : row)
	    	{
	    		arr[X] = S;
	    		X++;
	    	}
	    	X= 0;
	    	writer.writeNext(arr);
	    }
	    usidServiceCheckRows = null;
	    writer.close();
		
	}
}

