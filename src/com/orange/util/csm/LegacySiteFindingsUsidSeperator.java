package com.orange.util.csm;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JOptionPane;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.util.CommonUtils;
import com.orange.util.CustomCSVWriter;

public class LegacySiteFindingsUsidSeperator {

	public static void main(String ad[]){
		
		String readerFilePath = "C:\\Lalit\\Gold-Assignment\\CSM\\CSMTasks\\Legacy_Site_Extract\\USIDBasedExtract\\OrderWithUSID.csv";
		String readerFilePath1 = "C:\\Lalit\\Gold-Assignment\\CSM\\CSMTasks\\Legacy_Site_Extract\\USIDBasedExtract\\OrderWithUSID1.csv";
		String readerFilePath2 = "C:\\Lalit\\Gold-Assignment\\CSM\\CSMTasks\\Legacy_Site_Extract\\USIDBasedExtract\\OrderWithUSID2.csv";
		
		String wrtiterFilePath ="C:\\Lalit\\Gold-Assignment\\CSM\\CSMTasks\\Legacy_Site_Extract\\USIDBasedExtract\\OrderWithUSIDUpdated.csv";
		try {
			String finalreaderFilePath = "C:\\Lalit\\Gold-Assignment\\CSM\\CSMTasks\\Legacy_Site_Extract\\USIDBasedExtract\\LegacySiteExtract_Orders.csv";
			
			String finalwrtiterFilePath ="C:\\Lalit\\Gold-Assignment\\CSM\\CSMTasks\\Legacy_Site_Extract\\USIDBasedExtract\\LegacySiteExtract_OrdersUpdated.csv";
			System.out.println("Updating exisitinng file..");
			LegacySiteFindingsUsidSeperator ls = new LegacySiteFindingsUsidSeperator(readerFilePath,readerFilePath1,readerFilePath2,wrtiterFilePath);
			ls.updateOrderFile(finalreaderFilePath,finalwrtiterFilePath);
			System.out.println("Updating exisitinng file completed successfully.");
			//String finalwrtiterFilePath1 ="C:\\Lalit\\Gold-Assignment\\CSM\\CSMTasks\\Legacy_Site_Extract\\USIDBasedExtract\\LegacySiteExtract_OrdersUpdated_Final.csv";
			//System.out.println("Fine Tune Start");
			//ls.fineTune(finalwrtiterFilePath,finalwrtiterFilePath1);
			//System.out.println("Fine Tune Completed");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null,e);
		}
	}
	
	Map<String,ArrayList<String[]>> readerMap ;
	Map<String,ArrayList<String[]>> readerProxyMap;
	
	
	CustomCSVWriter writer;
	public LegacySiteFindingsUsidSeperator(String readerFilePath,String readerFilePath1,String readerFilePath2,String wrtiterFilePath) throws Exception{
		
		CSVReader csvReader = new CSVReader(new FileReader(readerFilePath));
		String[] row = csvReader.readNext();
		writer = new CustomCSVWriter(new FileWriter(wrtiterFilePath),true);
		writer.writeNext(row);
		readerMap = new HashMap<String,ArrayList<String[]>>();
		
		while ((row = csvReader.readNext()) != null) 
		{
			addMapData(row[0],row);
		}
		csvReader.close();
		System.out.println(" First Reader Completed");
		CSVReader csvReader1 = new CSVReader(new FileReader(readerFilePath1));
		String[] row1 = csvReader1.readNext();
		while ((row1 = csvReader1.readNext()) != null) 
		{
			addMapData(row1[0],row1);
		}
		csvReader1.close();
		System.out.println(" Second Reader Completed");
		CSVReader csvReader2 = new CSVReader(new FileReader(readerFilePath2));
		String[] row2 = csvReader2.readNext();
		while ((row2 = csvReader2.readNext()) != null) 
		{
			addMapData(row2[0],row2);
		}
		csvReader2.close();
		System.out.println(" Reader Completed");
		System.out.println(" Filtering Reader Map");
		filterMapData();
		System.out.println(" Filtering Reader Map Completed");
		System.out.println(" CSV writer Started");
		writeUSIDCSV();
		System.out.println(" USID CSV writer completed");
		
	}
	private void addMapData(String key , String[] value){
		if(!readerMap.containsKey(key))
		{
				ArrayList<String[]> values =  new ArrayList<String[]>();
				values.add(value);
				readerMap.put(key, values);
		}
		else{
			String DESCRIPTION = value[5];
			if(DESCRIPTION.equals("RouterUSID") 
					|| DESCRIPTION.equals("Router USID") 
					|| DESCRIPTION.equals("Access USID") 
					|| (DESCRIPTION.contains("Service USID")) )
			{
			ArrayList<String[]> existingValues = readerMap.get(key);
			existingValues.add(value);
			readerMap.put(key, existingValues);
			}
		}
	}
	
	private void filterMapData(){
		
		readerProxyMap = new HashMap<String,ArrayList<String[]>>();
	
		Iterator<Map.Entry<String, ArrayList<String[]>>> entries1 = readerMap.entrySet().iterator();

	
		while (entries1.hasNext()) 
		{
			Map.Entry<String, ArrayList<String[]>> entry = entries1.next();
			String key = entry.getKey();
			ArrayList<String[]> values = entry.getValue();
			
			for(String[] row : values)
			{
				String DESCRIPTION = row[5];
				String NEW_CONFIG  = row[6];
				String EXT_CONFIG  = row[7];
				String VALUE 	   = row[8];
				
			
				if(DESCRIPTION.equals("RouterUSID") || DESCRIPTION.equals("Router USID") || DESCRIPTION.equals("Access USID") || (DESCRIPTION.contains("Service USID") ))
				{
					if(!isNull(NEW_CONFIG) || !isNull(EXT_CONFIG)|| !isNull(VALUE))
					{
						if(readerProxyMap.containsKey(key))
						{
							ArrayList<String[]> newRow = readerProxyMap.get(key);
							newRow.add(row);
							readerProxyMap.put(key,newRow);
						}
						else
						{
							ArrayList<String[]> newRow = new ArrayList<String[]>();
							newRow.add(row);
							readerProxyMap.put(key,newRow);
						}
					}
				}
			}
			
			if(!readerProxyMap.containsKey(key))
			{
				ArrayList<String[]> newRow = new ArrayList<String[]>();
				newRow.add(values.get(0));
				readerProxyMap.put(key,newRow);
			}
			
	}
	}
	
	@SuppressWarnings("unused")
	private void fineTune(String readerFilePath ,String wrtiterFilePath) throws Exception{
		String query = "select service.is_CONFIGURABLE,service.isservice,service.ISCATALOGIDDEDUCTION from golddba.sc_quote quote,golddba.eq_service service where  quote.orderedservice = service.service_id and quote.quotenumber = ?"; 
		Connection connection = getConnection();
		System.out.println("Connection Created.....");
		
		CSVReader csvReader = new CSVReader(new FileReader(readerFilePath));
		String[] row = csvReader.readNext();
		CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(wrtiterFilePath),true);
		writer.writeNext(row);
		
		while ((row = csvReader.readNext()) != null) 
		{
			String order = row[0];
			String IS_CONFIGURABLE=row[1];
			if(null == IS_CONFIGURABLE || IS_CONFIGURABLE.isEmpty() || IS_CONFIGURABLE.length()==0)
			{
				PreparedStatement stm = connection.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				stm.setString(1, CommonUtils.addZeroPrefixinOrder(order));
				ResultSet rs = stm.executeQuery();
				while(rs.next())
				{
				row[1] = rs.getString(1);
				row[2] = rs.getString(2);
				row[3] = rs.getString(3);
				row[4] ="SYSTEM_NEXT";
				}
				
				writer.writeNext(row);
				stm.close();
				rs.close();
			}
			else{
				writer.writeNext(row);
			}
			
		}
		csvReader.close();
		writer.close();
	}
	private Connection getConnection() throws Exception {
		Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
		String url = "jdbc:oracle:thin:@oakgolddbvip.dc.iad.equant.com:1521:goldprd1";
		return DriverManager.getConnection(url, "gold_read", "GOLD_READ");
	}
	
	
	private boolean isNull(String s){
		if(null != s && !s.isEmpty() && s.length()>0){
			return false;
		}
		return true;
	}
	
		private void updateOrderFile(String readerFilePath,String wrtiterFilePath) throws Exception{
			
			String query = "select service.is_CONFIGURABLE,service.isservice,service.ISCATALOGIDDEDUCTION from golddba.sc_quote quote,golddba.eq_service service where  quote.orderedservice = service.service_idand quote.quotenumber = ?"; 
			
			Connection connection = getConnection();
			
			CSVReader csvReader = new CSVReader(new FileReader(readerFilePath));
			String[] row = csvReader.readNext();
			CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(wrtiterFilePath),true);
			writer.writeNext(row);
			
			while ((row = csvReader.readNext()) != null) 
			{
				String order = row[0];
				if(readerProxyMap.containsKey(order))
				{
					ArrayList<String[]> rows = readerProxyMap.get(order);
					for(String[] R : rows)
					{
						writer.writeNext(R);
					}
				}
				
				else{
					PreparedStatement stm = connection.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
					stm.setString(1, CommonUtils.addZeroPrefixinOrder(order));
					ResultSet rs = stm.executeQuery();
					while(rs.next())
					{
					row[1] = rs.getString(1);
					row[2] = rs.getString(2);
					row[3] = rs.getString(3);
					row[4] ="SYSTEM";
					}
					
					writer.writeNext(row);
					stm.close();
					rs.close();
					
				}
			}
			csvReader.close();
			writer.close();
			
		}
	private void writeUSIDCSV() throws IOException
	{
		Iterator<Map.Entry<String, ArrayList<String[]>>> entries1 = readerProxyMap.entrySet().iterator();
		while (entries1.hasNext()) 
		{
			Map.Entry<String, ArrayList<String[]>> entry = entries1.next();
			ArrayList<String[]> values = entry.getValue();
			for(String[] row: values){
			writer.writeNext(row);
			}
			
		}
		writer.close();
	}
}
