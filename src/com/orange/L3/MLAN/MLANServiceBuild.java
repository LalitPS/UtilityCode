package com.orange.L3.MLAN;

import java.awt.Frame;
import java.awt.Label;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JOptionPane;

import com.orange.ui.component.custom.CSMJTextArea;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.CustomCSVWriter;
import com.orange.util.csm.ConnectionForGOLD;

public class MLANServiceBuild {
	
	
	private String disconnectSQL = "SELECT SC_COL.COL_KEY , UPDOWNITEM.TRIL_GID FROM "+ConnectionBean.getDbPrefix()+"SC_COLLECTION SC_COL, "+ConnectionBean.getDbPrefix()+"EQ_UPDOWNITEM UPDOWNITEM WHERE SC_COL.OBJECTGID=UPDOWNITEM.TRIL_GID "+
	" AND SC_COL.COLLECTIONGID IN (SELECT EQ_DISCITEMS FROM "+ConnectionBean.getDbPrefix()+"EQ_DISCONNECT WHERE "+
	" TRIL_GID = (SELECT DATA FROM "+ConnectionBean.getDbPrefix()+"SC_HIERARCHY WHERE TRIL_GID="+
	"((SELECT CONFIGURATIONS FROM "+ConnectionBean.getDbPrefix()+"SC_QUOTE WHERE QUOTENUMBER= ?))))";
	
	private String l2SQL = "SELECT QUOTE.QUOTENUMBER,"+CommonUtils.ORDER_TYPE_DECODE+" LINEITEM.DESCRIPTION,LINEITEM.VALUE,LINEITEM.EXIST_CONFIG,LINEITEM.NEW_CONFIG,"+CommonUtils.getConsiderableValue()+" FROM "+ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE,"+ConnectionBean.getDbPrefix()+"SC_QUOTE_LINE_ITEM LINEITEM,"+ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID)  WHERE QUOTE.TRIL_GID = LINEITEM.QUOTE AND QUOTE.QUOTENUMBER=?  AND QUOTE.CONFIGURATIONS = HR.TRIL_GID " ;
	private String ordTypeSQL = "SELECT EQ_ORDERTYPE from "+ConnectionBean.getDbPrefix()+"SC_QUOTE WHERE QUOTENUMBER  = ?";
	
	private PreparedStatement pstmt;
	
	private CSMJTextArea queryResult;
	
	private String sql = "SELECT EQ_UPDOWNCOL.COL_KEY, EQ_UPDOWNCOL.OBJECTGID FROM "+ConnectionBean.getDbPrefix()+"EQ_UPDOWNCOL WHERE COLLECTIONGID IN (SELECT UPDOWNITEMS FROM "+ConnectionBean.getDbPrefix()+"EQ_CHANGE WHERE TRIL_GID =(SELECT DATA FROM "+ConnectionBean.getDbPrefix()+"SC_HIERARCHY WHERE TRIL_GID="
		         +"((SELECT CONFIGURATIONS FROM "+ConnectionBean.getDbPrefix()+"SC_QUOTE WHERE QUOTENUMBER=?))))";

	private String sql2 ="SELECT CSIOLDVALUE,EQ_OLDVALUE,ELEMENTSTATUS,EQ_UDITEMS FROM "+ConnectionBean.getDbPrefix()+"EQ_UPDOWNITEM WHERE TRIL_GID = ?";
	private String sql3 ="SELECT COL_KEY,OBJECTGID FROM "+ConnectionBean.getDbPrefix()+"EQ_UPDOWNCOL WHERE COLLECTIONGID = ?";
	private String sql4 ="SELECT CSIOLDVALUE,EQ_OLDVALUE,ELEMENTSTATUS,EQ_UDITEMS FROM "+ConnectionBean.getDbPrefix()+"EQ_UPDOWNITEM WHERE TRIL_GID = ?";
	

	
	private CustomCSVWriter writer ;
	public MLANServiceBuild(String orderNumber,String resultFileLoc,CSMJTextArea queryResult,Label orderTypeLabel) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException{
		
		orderTypeLabel.setText("....");
		this.queryResult = queryResult;
		
		int ord = ordType(ordTypeSQL,orderNumber);
		if(ord == 1){
			orderTypeLabel.setText("New");
			JOptionPane.showMessageDialog(new Frame(),"Order Type is New, not supported by utility..("+orderNumber+")");
			return;
		}
		else if(ord == 4){
			orderTypeLabel.setText("Invalid");
			JOptionPane.showMessageDialog(new Frame(),"No Order found .. "+orderNumber);
			return;
		}
		
		else if(ord == 3){
			orderTypeLabel.setText("Disconnect");
			sql =disconnectSQL;
		}
		else if (ord == 2){
			orderTypeLabel.setText("Change");
		}
		
	
		queryResult.append("\n");
		String[] header = {"COL_KEY","EQ_UDITEMS","COL_KEY","CSIOLDValue","EQ_OLDVALUE","Element Status"};
		writer = new CustomCSVWriter(new FileWriter(resultFileLoc),true);
		writer.writeNext(header);
		int count = 1;
		queryResult.append("Reading " +count +" Level Information ..");
		queryResult.append("\n");
		
		ArrayList<Map<ArrayList<String>,ArrayList<String>>> firstLevelList = new ArrayList<Map<ArrayList<String>,ArrayList<String>>>();
		Map<ArrayList<String>,ArrayList<String>> result = getServiceBuildInfo(sql,orderNumber,2);
		firstLevelList.add(result);
		
		queryResult.append("Collect " +count +" Level Information ..");
		queryResult.append("\n");
		count++;
		queryResult.append("Reading " +count +" Level Information ..");
		queryResult.append("\n");
		ArrayList<Map<ArrayList<String>,ArrayList<String>>> secondLevelList = iterateOverForKeys(firstLevelList,sql2,4);
		queryResult.append("Collect " +count +" Level Information ..");
		queryResult.append("\n");
		count++;
		queryResult.append("Reading " +count +" Level Information ..");
		queryResult.append("\n");
		ArrayList<Map<ArrayList<String>,ArrayList<String>>> thirdLevelList =  iterateOverForKeys(secondLevelList,sql3,2);
		queryResult.append("Collect " +count +" Level Information ..");
		queryResult.append("\n");
		count++;
		queryResult.append("Reading " +count +" Level Information ..");
		queryResult.append("\n");
		ArrayList<Map<ArrayList<String>,ArrayList<String>>> fourthLevelList = iterateOverForKeys(thirdLevelList,sql4,1);
		queryResult.append("Collect " +count +" Level Information ..");
		count++;
		
		queryResult.append("Completed..");
		queryResult.append("\n");
		
		display(firstLevelList,secondLevelList,thirdLevelList,fourthLevelList);
		
		CommonUtils.showOnTable(resultFileLoc);
		
	}
	
public MLANServiceBuild(String orderNumber,String resultFileLoc,CSMJTextArea queryResult,Label orderTypeLabel,boolean isL2) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException{
		
		orderTypeLabel.setText("....");
		this.queryResult = queryResult;
		
		int ord = ordType(ordTypeSQL,orderNumber);
	
		if(ord == 1){
			orderTypeLabel.setText("New");
		}
		if(ord == 4){
			orderTypeLabel.setText("Invalid");
			JOptionPane.showMessageDialog(new Frame(),"No Order found .. "+orderNumber);
			return;
		}
		
		if(ord == 3){
			orderTypeLabel.setText("Disconnect");
		}
		if(ord == 2){
			orderTypeLabel.setText("Change");
		}
		
		
		queryResult.append("\n");
		
		
	
		queryResult.append("Reading service build Information ..");
		queryResult.append("\n");
	 	pstmt = ConnectionForGOLD.getPreparedStatement(l2SQL);
		pstmt.setString(1,orderNumber);
		
		
		ResultSet resultSet = pstmt.executeQuery();
		queryResult.append(l2SQL.replace("?","'"+orderNumber+"'"));
		queryResult.append("\n");
		
		String[] header = {"QUOTENUMBER","TYPE","DESCRIPTION","VALUE","EXIST_CONFIG","NEW_CONFIG","CONSIDERABLE_VALUE"};
		writer = new CustomCSVWriter(new FileWriter(resultFileLoc),true);
		writer.writeNext(header);
		
		while(resultSet.next()){
			String arr[] = new String[7];
			arr[0] = resultSet.getString(1);
			arr[1] = resultSet.getString(2);
			arr[2] = resultSet.getString(3);
			arr[3] = resultSet.getString(4);
			arr[4] = resultSet.getString(5);
			arr[5] = resultSet.getString(6);
			arr[6] = resultSet.getString(7);
			
			writer.writeNext(arr);
		}
		
		pstmt.close();
		resultSet.close();
		
		writer.close();
		
		queryResult.append("Complted");
		queryResult.append("\n");
		CommonUtils.showOnTable(resultFileLoc);
	}

	
		
	private void display(ArrayList<Map<ArrayList<String>,ArrayList<String>>> dataList,ArrayList<Map<ArrayList<String>,ArrayList<String>>> dataList2,ArrayList<Map<ArrayList<String>,ArrayList<String>>> dataList3,ArrayList<Map<ArrayList<String>,ArrayList<String>>> dataList4) throws IOException
	{
		ArrayList<String[]> results = new ArrayList<String[]>();
		
		for(Map<ArrayList<String>,ArrayList<String>> map : dataList)
		{
		
			
		Iterator<Map.Entry<ArrayList<String>,ArrayList<String>>> keys = map.entrySet().iterator();
		
		while(keys.hasNext())
		{
			Map.Entry<ArrayList<String>,ArrayList<String>> entry = keys.next();
			ArrayList<String> keyPair = entry.getKey();
			String key = keyPair.get(0);
			//String parentKey = keyPair.get(1);
			
			ArrayList<String> values = entry.getValue();
			
			String value1 = values.get(0);
			//String value2 = values.get(1);
		
			
			//==========Next Map=====================
			
			for(Map<ArrayList<String>,ArrayList<String>> map2 : dataList2)
			{
			
			Iterator<Map.Entry<ArrayList<String>,ArrayList<String>>> keys2 = map2.entrySet().iterator();
			
			while(keys2.hasNext())
			{
				Map.Entry<ArrayList<String>,ArrayList<String>> entry2 = keys2.next();
				ArrayList<String> keyPair2 = entry2.getKey();
				String key2 = keyPair2.get(0);
				String parentKey2 = keyPair2.get(1);
				
				if(key.equalsIgnoreCase(parentKey2))
				{	
				ArrayList<String> values2 = entry2.getValue();
				/*
				String value12 = values2.get(0);
				String value22 = values2.get(1);
				String value32 = values2.get(2);
				*/
				String value42 = values2.get(3);
				
				//==========Next Map Space=====================
				for(Map<ArrayList<String>,ArrayList<String>> map3 : dataList3)
				{
				
				Iterator<Map.Entry<ArrayList<String>,ArrayList<String>>> keys3 = map3.entrySet().iterator();
				
				while(keys3.hasNext())
				{
					Map.Entry<ArrayList<String>,ArrayList<String>> entry3 = keys3.next();
					ArrayList<String> keyPair3 = entry3.getKey();
					String key3 = keyPair3.get(0);
					String parentKey3 = keyPair3.get(1);
					
					if(key2.equalsIgnoreCase(parentKey3))
					{
					
					ArrayList<String> values3 = entry3.getValue();
					
					String value13 = values3.get(0);
					//String value23 = values3.get(1);
					
					
					//==========Next Map Space=====================
					
					for(Map<ArrayList<String>,ArrayList<String>> map4 : dataList4)
					{
					
					Iterator<Map.Entry<ArrayList<String>,ArrayList<String>>> keys4 = map4.entrySet().iterator();
					
					while(keys4.hasNext())
					{
						Map.Entry<ArrayList<String>,ArrayList<String>> entry4 = keys4.next();
						ArrayList<String> keyPair4 = entry4.getKey();
						//String key4 = keyPair4.get(0);
						String parentKey4 = keyPair4.get(1);
						
						if(key3.equalsIgnoreCase(parentKey4))
						{
						
						ArrayList<String> values4 = entry4.getValue();
						
						String value14 = values4.get(0);
						String value24 = values4.get(1);
						String value34 = values4.get(2);
						//String value44 = values4.get(3);
						
						String[] result = new String[6];
						result[0]=value1;
						result[1]=value42;
						result[2]=value13;
						result[3]=value14;
						result[4]=value24;
						result[5]=value34;
						results.add(result);	
					
					
						}
						 //==========Next Map Space=====================
						
						
						
						
						//=================Next Map Space Ends======================
						
							
					}
					}
					}
					
					//=================Next Map Space Ends======================
					
						
				}
				}
				}
				
				
				//=================Next Map Space Ends======================
				
					
			}
			}
			
			//=================Next Map Space Ends======================
			
				
		}
		
	
		}
		
		
		
		for(String[] rows: results)
		{
			writer.writeNext(rows);
		}
		
			writer.close();
		
		queryResult.append("File Created Successfully....");
		queryResult.append("\n");
		
	}
	
	
	private Map<ArrayList<String>,ArrayList<String>> getServiceBuildInfo(String sql , String input,int keyIndex) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
	
		pstmt = ConnectionForGOLD.getPreparedStatement(sql);
		pstmt.setString(1,input);
		
		
		ResultSet resultSet = pstmt.executeQuery();
		ResultSetMetaData rmd = resultSet.getMetaData();
		
		queryResult.append(sql.replace("?","'"+input+"'"));
		queryResult.append("\n");
		
		int columnCount = rmd.getColumnCount();
		
		Map<ArrayList<String>,ArrayList<String>> tabledata = new HashMap<ArrayList<String>,ArrayList<String>>();
		
		while(resultSet.next())
		{
		ArrayList<String> rowdata = new ArrayList<String>();
		String key ="";
		for(int columnIndex = 1 ; columnIndex<= columnCount ; columnIndex++)
		{
			String colData = resultSet.getString(columnIndex);
			if(columnIndex == keyIndex)
			{
					key = colData;
			}
			
			rowdata.add(colData);
		}
		
		if(!hasKeyExists(tabledata,key)){
			
			ArrayList<String> keyPair = new ArrayList<String>();
			keyPair.add(key);
			keyPair.add(input);
			tabledata.put(keyPair,rowdata);
		}
		
		else{
			ArrayList<String> keyPair = new ArrayList<String>();
			keyPair.add(key);
			keyPair.add(input);
			
			ArrayList<String> prevData = tabledata.get(keyPair);
			for(String col : rowdata){
				prevData.add(col);
			}
			
			tabledata.put(keyPair,rowdata);
			
		}
		
		}
		
		pstmt.close();
		resultSet.close();
		return tabledata;
	}
	
	private boolean hasKeyExists(Map<ArrayList<String>,ArrayList<String>> map,String key){
		boolean hasExists = false;
		Iterator<Map.Entry<ArrayList<String>,ArrayList<String>>> keys = map.entrySet().iterator();
		while(keys.hasNext()){
			Map.Entry<ArrayList<String>,ArrayList<String>> entry = keys.next();
			ArrayList<String> keyPair = entry.getKey();
			String orgKey = keyPair.get(0);
			if(orgKey.equalsIgnoreCase(key)){
				hasExists = true;
				break;
			}
		}
		return hasExists;
	}
	
	private ArrayList<Map<ArrayList<String>,ArrayList<String>>> iterateOverForKeys(ArrayList<Map<ArrayList<String>,ArrayList<String>>> listOfMap,String sql,int keyIndex) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		ArrayList<Map<ArrayList<String>,ArrayList<String>>> mapList = new ArrayList<Map<ArrayList<String>,ArrayList<String>>>();
		
		for(Map<ArrayList<String>,ArrayList<String>> map : listOfMap)
		{
		
		Iterator<Map.Entry<ArrayList<String>,ArrayList<String>>> keys = map.entrySet().iterator();
		
		while(keys.hasNext()){
			Map.Entry<ArrayList<String>,ArrayList<String>> entry = keys.next();
			ArrayList<String> keyPair = entry.getKey();
			String orgKey = keyPair.get(0);
			//String prevValue = keyPair.get(1);
			Map<ArrayList<String>,ArrayList<String>> innerResult =getServiceBuildInfo(sql,orgKey,keyIndex);
			mapList.add(innerResult);
		}
		
		}
		
		
		return mapList;
	}
	
	private int ordType(String sql , String input) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		pstmt = ConnectionForGOLD.getPreparedStatement(sql);
		pstmt.setString(1,input);
		
		String orderType = "4";
		ResultSet resultSet = pstmt.executeQuery();
		while (resultSet.next()){
		orderType = resultSet.getString(1);
		if(null == orderType || orderType.isEmpty()){
			return  4;
		}
		
		}
		pstmt.close();
		resultSet.close();
		return Integer.parseInt(orderType);
	}
	
	

}
