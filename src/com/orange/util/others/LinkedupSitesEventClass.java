package com.orange.util.others;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.ui.component.SiteFixedViewComponents;
import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.Icons;
import com.orange.util.ConnectionBean;
import com.orange.util.CustomCSVWriter;
import com.orange.util.ProgressMonitorPane;
import com.orange.util.csm.ConnectionForGOLD;
import com.orange.util.csm.CustomJTable;

public class LinkedupSitesEventClass {
	static String updatedcolumnName[] = {"REPETITION", 
			"QUOTE_NUMBER", "MIGRATED_ORDER", "ADDRESS_ID",
			"PRIM_SITECODE","PRIM_SITECODE_STATUS","SEC_SITECODE","SEC_SITECODE_STATUS",
			"PREM_OSR","SEC_OSR","DELIVERY_PARTY","EQ_SITEOF","CORE_ICO","CORE_STATUS",
			"ORD_ICO","ORD_SITECODE","ORD_OSR"
			};
	private String columnName[] = { "ADDRESS_ID", "CORE_ICO", "CORE_STATUS"};
	
	private ArrayList<String> errors;
	private String filePath;
	
	
	Map<String[],ArrayList<String[]>> readerMap ;
	
	
	SiteFixedViewComponents siteFixViewComponents;
	
	Map<String[],ArrayList<String[]>> writerMap ;
	
	public LinkedupSitesEventClass(String filePath,SiteFixedViewComponents siteFixViewComponents) throws Exception{
		
		this.siteFixViewComponents = siteFixViewComponents;
		this.filePath = filePath;
		errors = new ArrayList<String>();
		readerMap = new HashMap<String[],ArrayList<String[]>>();
		writerMap = new HashMap<String[],ArrayList<String[]>>();
		
		ArrayList<String> errorsfound = getFileValidateResult(filePath);
		
		
		for(String err : errorsfound)
		{
			siteFixViewComponents.getQueryResult().append(err+"\n");
		}
		
	}

	
	private void addReaderMapKey(String[] key){
		if(!readerMap.containsKey(key)){
			readerMap.put(key, new ArrayList<String[]>());
		}
	}
	
	private void addWriterMapData(String[] key, ArrayList<String[]> mapdata){
		
		if(!writerMap.containsKey(key)){
			writerMap.put(key, mapdata);
		}
		else{
			ArrayList<String[]> exisitingdata = writerMap.get(key);
			for(String[] row : mapdata){
				exisitingdata.add(row);
			}
		}
	}
	
	public ArrayList<String> getFileValidateResult(String filePath) throws Exception{
		CSVReader csvReader = new CSVReader(new FileReader(filePath));
		String []row = csvReader.readNext();
		
		for (int C = 0; C < row.length; C++) 
		{
			if (!columnName[C].equalsIgnoreCase(row[C])) 
			{
				errors.add("COLUMN_MISMATCH ::"+columnName[C]+"::"+row[C]);
			}
		}
		
		int count =1 ;
		while ((row = csvReader.readNext()) != null) 
		{
			if(row[0].isEmpty() )
			{
				errors.add("ADDRESS_ID_EMPTY::"+count);
			}
			else if(row[1].isEmpty() )
			{
				errors.add("CORE_ICO_EMPTY::"+count);
			}
			else if(row[2].isEmpty())
			{
				errors.add("CORE_STATUS_EMPTY::"+count);
			}
			else
			{
			addReaderMapKey(row);
			}
		}
		csvReader.close();
		return errors;
	}
	
	private ArrayList<String[]> getQueryResult(String query, String param) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		String localParam = param;
		String localQuery = query;
		PreparedStatement pstmt = ConnectionForGOLD.getPreparedStatement(localQuery);
		pstmt.setString(1, localParam);
		String upquery = localQuery.replace("?", "'"+localParam +"'");
		siteFixViewComponents.getQueryResult().append(upquery+"\n");
		
		ResultSet resultSet = pstmt.executeQuery();
		int columnCount = resultSet.getMetaData().getColumnCount();
		ArrayList<String[]> results = new ArrayList<String[]>();

		while (resultSet.next()) 
		{
			String dataarr[] = new String[columnCount];
			for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) 
			{
				String tgid = resultSet.getString(columnIndex);
				dataarr[columnIndex - 1] = tgid;
			}
			results.add(dataarr);
		}
		resultSet.close();
		pstmt.close();
		localParam = "";
		localQuery = "";
		query="";
		param="";
		
		return results;

	}
	
	public void showTable(String resultFileLoc) throws IOException
	{
		CSVReader csvReader = new CSVReader(new FileReader(resultFileLoc));
		
		String []row = csvReader.readNext();
		
		DefaultTableModel model = new DefaultTableModel(0, 0);
		model.setColumnIdentifiers(row);
		
		while ((row = csvReader.readNext()) != null) 
		{
			model.addRow(row);
		}
		
		csvReader.close();
		CustomJTable table = new CustomJTable();
		table.getTable().setModel(model);
		table.getTable().setAutoCreateRowSorter(true);
		JScrollPane scroll = new JScrollPane(table.getTable());
		CustomJFrame f = new CustomJFrame("Total Rows("+table.getTable().getRowCount()+")",Icons.iconPath15);
		f.setBounds(200,150,500,400);
		f.add(scroll);
		f.setVisible(true);
		f.pack();
	}
	
	public void startSQLs()throws Exception 
	{
		
		String getSiteCodesQL = "select site.ORANGE_SITENAME,site.SITECODE,site.STATUS,org.ORGANIZATIONID from "+ConnectionBean.getDbPrefix()+"eq_site site,"+ConnectionBean.getDbPrefix()+"sc_organization org " +
				"where ADDRESS_ID =? and org.tril_gid = site.eq_siteof";

		String getAllDataQL = "select quote.QUOTENUMBER ,quote.HOTCUTNEWSITE as MIGRATED_ORDERS,site.address_id,site.sitecode,site.status," +
				"site.orange_sitename ,org.organizationid from "+ConnectionBean.getDbPrefix()+"sc_quote quote, "+ConnectionBean.getDbPrefix()+"eq_site site, "+ConnectionBean.getDbPrefix()+"sc_organization org "+
		"where (site.address_id=? and quote.site=site.tril_gid ) and org.organizationid in (select organizationid from "+ConnectionBean.getDbPrefix()+"sc_organization where quote.eq_deliveryparty=org.tril_gid) order by org.ORGANIZATIONID, quote.QUOTENUMBER ASC"; 

		String hotCutSiteICOQL ="select org.ORGANIZATIONID from "+ConnectionBean.getDbPrefix()+"eq_site site,"+ConnectionBean.getDbPrefix()+"sc_organization org where site.tril_gid=? and org.tril_gid = site.eq_siteof";
		
		
		Iterator<Map.Entry<String[], ArrayList<String[]>>> entries = readerMap.entrySet().iterator();
		
		double count =0.0;
		ProgressMonitorPane.getInstance().setProgress(count,readerMap.size());
		
		while (entries.hasNext()) 
		{
			Map.Entry<String[], ArrayList<String[]>> entry = entries.next();
			String[] key = entry.getKey();
			String addressId = key[0];
			String coreico = key[1];
			String corestatus = key[2];
			
			
			ArrayList<String[]>  result1 = getQueryResult(getSiteCodesQL, addressId);
			ArrayList<String[]>  result2 = getQueryResult(getAllDataQL, addressId);
			
			ArrayList<String[]>  rowlist = new ArrayList<String[]>();
			
			
			
			String sec_osr="";
			String sec_sitecode="";
			String sec_sitecode_status="";
			String sec_eqsiteof="";
			
			int repeat =0;
			for(String[] r:result1)
			{
				sec_osr+= r[0]+";";
				sec_sitecode+= r[1]+";";
				sec_sitecode_status+= r[2]+";";
				sec_eqsiteof =r[3];
				repeat++;
			}
			
			
			for(String[] r:result2)
			{
				String[] row = new String[updatedcolumnName.length];
				String qno = r[0];
				String migratedorder = r[1];
				String addressid = r[2];
				String primsitecode = r[3];
				String primstitecodestatus = r[4];
				String primosr = r[5];
				String orgno = r[6];
				
				if(null !=migratedorder && migratedorder.length()>1)
				{
					ArrayList<String[]>  result3 = getQueryResult(hotCutSiteICOQL, migratedorder);
					migratedorder = result3.get(0)[0];
				}
				
				row[1] = qno;
				row[2] = migratedorder;
				row[3] = addressid;
				row[4] = primsitecode;
				row[5] = primstitecodestatus;
				
				row[8] = primosr;
			
				row[10] = orgno;
				row[12] = coreico;
				row[13] = corestatus;
				row[14] = "";
				row[15] = "";
				row[16] = "";
				
				if(repeat ==1){
					row[0] = "SINGLE";
				}
				if(repeat ==2){
					row[0] = "DOUBLE";
				}
				if(repeat >2){
					row[0] = "MULTIPLE";
				}
				
				row[6] = updatedData(sec_sitecode,primsitecode,repeat);
				row[7] = updatedData(sec_sitecode_status,primstitecodestatus,repeat);
				row[9] = updatedData(sec_osr,primosr,repeat);
				row[11] = sec_eqsiteof;
				
				
				rowlist.add(row);
				
			}
			addWriterMapData(key,rowlist);
			count ++;
			ProgressMonitorPane.getInstance().setProgress(count,readerMap.size());	
			
		}
		writeUpdatedFile();
	}
	
	
	
	private String updatedData(String input, String filterto,int repeat){
		
		if(repeat == 1){
			return "";
		}
		
		ArrayList<String> avoidDuplicate = new ArrayList<String>();
		String input2="";
		for (String retval: input.split(";")) {
			if(retval.equalsIgnoreCase(filterto))
			{
				// just to remove only exact string (not matching text) from ; separated text
			}
			else
			{
				if(avoidDuplicate.contains(retval))
				{
					// Just not to add duplicate in string.
				}
				else
				{
				avoidDuplicate.add(retval);
				input2+=retval+";";
				}
			}
		}
		
		avoidDuplicate = null;
		String updated = input2;
		
		if(!updated.isEmpty() && null != updated && updated.length()>1)
		{
			if(updated.contains(";;")){
				updated = updated.replace(";;",";");
			}
			if(updated.lastIndexOf(";") == updated.length()-1){
				updated = updated.substring(0,updated.length()-1);
			}
			if(updated.indexOf(";") == 0){
				updated = updated.substring(1,updated.length());
			}
		}
		else{
			/*
			 * If input wasn't empty .. but after filter it become empty,
			 * it mean filter removes all data , so just initilize it with prev data.
			 */
			updated = filterto;
		}
		return updated;
		
		
	}
	
	private void writeUpdatedFile() throws IOException{
		
		String path = filePath;	
		int index = path.lastIndexOf(".");
		String sub = path.substring(0,index) ;
		String updatedPath=sub+"_Updated.csv";
		
		CustomCSVWriter csvWriter = new CustomCSVWriter(new FileWriter(updatedPath),true);
		
		csvWriter.writeNext(updatedcolumnName);
		Iterator<Map.Entry<String[], ArrayList<String[]>>> entries = writerMap.entrySet().iterator();
		
			
		while (entries.hasNext()) 
		{
			Map.Entry<String[], ArrayList<String[]>> entry = entries.next();
			ArrayList<String[]> value = entry.getValue();
			csvWriter.writeAll(value);
		}
		
		csvWriter.close();
		showTable(updatedPath);
	}
	
	

}
