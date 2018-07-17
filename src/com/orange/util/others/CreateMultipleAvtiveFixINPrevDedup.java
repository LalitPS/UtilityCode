package com.orange.util.others;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.ui.component.SiteFixedViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.CustomCSVWriter;
import com.orange.util.ProgressMonitorPane;

public class CreateMultipleAvtiveFixINPrevDedup {

	private ArrayList<String> addressIdsList;
	private String fileName;
	private Map<String,ArrayList<String[]>> fileReaderMap;
	private String getDuolicateActiveIDs="select distinct address_id  from "+ConnectionBean.getDbPrefix()+"eq_site where ADDRESS_ID in (SELECT ADDRESS_ID  FROM "+ConnectionBean.getDbPrefix()+"EQ_SITE group by ADDRESS_ID,status HAVING COUNT(address_id) > 1 and status='0' )and status='0'";
	private String resultFileName ;
	
	private SiteFixedViewComponents siteFixViewComponents;
	
	private String TrueSQL ="select distinct organ.OrganizationID ,site.Core_site_id ,site.orange_sitename ,site.customer_sitename ," +
	"site.status ,site.eq_comment ,address.phone ,address.fax ,site.address_id ,address.street2 ,address.street3 ," +
	"site.Room ,site.Floor ,address.Street1  ,address.zipcode,address.city,address.state_code,address.state," +
	"address.country_code,site.ISVALIDPRESALES,site.sitecode from "+ConnectionBean.getDbPrefix()+"sc_organization organ " +
			"join "+ConnectionBean.getDbPrefix()+"eq_site site on  site.eq_siteof=organ.tril_gid " +
			"join "+ConnectionBean.getDbPrefix()+"sc_address address on site.siteaddress=address.tril_gid	" +
			"where site.address_id in(?) and site.status = '0'";
	
	public CreateMultipleAvtiveFixINPrevDedup(SiteFixedViewComponents siteFixViewComponents) throws IOException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		
		this.fileName= siteFixViewComponents.getFileToValidate().getText();
		fileReaderMap = new LinkedHashMap<String,ArrayList<String[]>>();
		this.siteFixViewComponents = siteFixViewComponents;
		addressIdsList = new ArrayList<String>();
		String path = this.fileName;	
		
		int index = path.lastIndexOf(".");
		String sub = path.substring(0,index) ;
		resultFileName=sub+"_MULTIPL_ACTIVE_SITEFIX.csv";
		getListOfAddressIds();
		readDedupCSV(this.fileName);
		writeSiteFixCSV(this.resultFileName);
		
	}
	private void addMapData(String key,String[] value)
	{
		if(fileReaderMap.containsKey(key))
		{
			ArrayList<String[]> existitng =fileReaderMap.get(key);
			existitng.add(value);
			fileReaderMap.put(key, existitng);
		}
		else
		{
			ArrayList<String[]> existitng =new ArrayList<String[]>();
			existitng.add(value);
			fileReaderMap.put(key, existitng);
		}
	}
	private void getListOfAddressIds() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		ArrayList<String[]> extract = CommonUtils.getQueryResult(getDuolicateActiveIDs, siteFixViewComponents);
		for(String[] row : extract)
		{
			addressIdsList.add(row[0]);
		}
	}
	
	private void readDedupCSV(String filePath) throws IOException{
		
		CSVReader	csvReader = new CSVReader(new FileReader(filePath));
		String[] csvRow;
		while ((csvRow = csvReader.readNext()) != null) 
		{
			String GID = csvRow[0];
			
			String AM_RESPONSE = csvRow[1];
			String ICO = csvRow[2];
			String ADDRESS_ID = csvRow[10];
			String arr[] = new String[4];
			String SITECODE = csvRow[22];
			
			
			if(AM_RESPONSE.equalsIgnoreCase("TRUE"))
			{
			arr[0]=AM_RESPONSE;
			arr[1]=ICO;
			arr[2]=SITECODE;
			arr[3]=ADDRESS_ID;
			if(addressIdsList.contains(ADDRESS_ID))
			{
				addMapData(GID,arr);
			}
			}
		}
	csvReader.close();
	}
	
	private void writeSiteFixCSV(String resultFileName) throws IOException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		
		CustomCSVWriter siteFixWriter = new CustomCSVWriter(new FileWriter(resultFileName),true);
		
		ArrayList<String[]> writerData = new ArrayList<String[]>();

		String header[] = 	new String[]{"REPLACE_SITECODE","REPLACE_STATUS","REPLACEMENT_SITECODE","REPLACEMENT_STATUS","REPLACE_OSR","REPLACEMENT_OSR","MOVE_ORDERS"};
		siteFixWriter.writeNext(header);
		
		Iterator<Map.Entry<String, ArrayList<String[]>>> entries = fileReaderMap.entrySet().iterator();
	
		double count = 0.0; 
		final int  totalDataCount = fileReaderMap.size();
		while (entries.hasNext()) 
		{
			Map.Entry<String, ArrayList<String[]>> entry = entries.next();
			ArrayList<String[]> value = entry.getValue();
			
			String TRUE_SITECODE="";
			
			count++;	
			ProgressMonitorPane.getInstance().setProgress(count,totalDataCount);
			
			
			for (int x = 0; x < value.size(); x++) 
			{
				String[] ARR = value.get(x);
				String amresponse=ARR[0].trim();
				//String ico=ARR[1].trim();
				String sitecode=ARR[2].trim();
				String address_id=ARR[3].trim();
				
				if(amresponse.equalsIgnoreCase("TRUE"))
				{
					
					TRUE_SITECODE = sitecode;
					
					ArrayList<String[]> queryResults = CommonUtils.getQueryResult(TrueSQL, siteFixViewComponents,address_id);
					
					if(queryResults.size() >1)
					{
						for(String[] queryResult : queryResults)
						{
							String resultsitecode = queryResult[20];
							
							if(!resultsitecode.equalsIgnoreCase(TRUE_SITECODE))
							{
								 String[] arr = new String[header.length];
								 arr[0]=resultsitecode;
								 arr[1]="3";
								 
								 arr[2]=TRUE_SITECODE;
								 arr[3]="0";
								 
								 arr[4]="";
								 arr[5]="";
								 arr[6]="ALL";
								 writerData.add(arr);
							}
						}
					}
				}
				
				
			}
		}
		siteFixWriter.writeAll(writerData);
		siteFixWriter.close();
		writerData = null;
		fileReaderMap = null;
		siteFixViewComponents.getQueryResult().append("\n --------------------------------------------------------------------------------");
		siteFixViewComponents.getQueryResult().append("\n MUltiple Active SITEFIX FILE CREATED "+resultFileName);
		siteFixViewComponents.getQueryResult().append("\n --------------------------------------------------------------------------------\n");
	}
}
