package com.orange.util;

import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.orange.ui.component.EarrachExtractionView;
import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.Icons;
import com.orange.util.csm.CustomJTable;
import com.orange.util.earrach.OfferDetailsActionRenderer;
import com.orange.util.earrach.ProductDetailActionRenderer;

public class EarrachJSONParser {

	String[] csvcolumnNames =  {"QUOTE_NUMBER","PREV_GOLD_ORDER","ORDER_TYPE","TYPE","ORDEREDSERVICE","MIGRATIONSERVICENAME","PARTIALBILLINGINSTCOMPLETE","PARTIALBILLINGINSTINPROGRESS","MODIFICATIONDATE","STATUS","GRP_PRODVERSION", "END_USER_ICO","END_USER_NAME","CONTRACTING_PARTY_ICO","CONTRACTING_PARTY_NAME","PRODUCT_TYPE","HREF","ID","VERSION_ID"};
	private EarrachExtractionView earrachExtractViewComponents;
	private String fileName;
	private String[] modelcolumnNames ={"QUOTE_NUMBER","PREV_GOLD_ORDER","ORDER_TYPE","TYPE","ORDEREDSERVICE","MIGRATIONSERVICENAME","PARTIALBILLINGINSTCOMPLETE","PARTIALBILLINGINSTINPROGRESS","MODIFICATIONDATE","STATUS","GRP_PRODVERSION", "END_USER_ICO","END_USER_NAME","CONTRACTING_PARTY_ICO","CONTRACTING_PARTY_NAME","DETAILED_INFO","PRODUCT_DETAILS"};
	private HashMap<String,ArrayList<String[]>> productHrefMap;
	private Dimension screenSize;
	
	private String SQL ="SELECT DISTINCT " +
			"QUOTE.QUOTENUMBER," +
			"QUOTE.EQ_GOLDORIGNB AS PREV_GOLD_ORDER , " +
			CommonUtils.ORDER_TYPE_DECODE+
			CommonUtils.EQ_TYPE_DECODE+
			"QUOTE.SERVICENAME, " +
			"SERVICE.DISP_NAME AS MIGRATIONSERVICENAME, " +
			"QUOTE.PARTIALBILLINGINSTCOMPLETE, " +
			"QUOTE.PARTIALBILLINGINSTINPROGRESS, " +
			"QUOTE.MODIFICATIONDATE, " +
			"ORDSTATUS.EQ_STATUS, " +
			"QUOTE.GRP_PRODVERSION, "+
			" ORG.ORGANIZATIONID AS END_USER_ICO,ORG.NAME AS END_USER_NAME,"+
			" ORG1.ORGANIZATIONID AS CONTRACTING_PARTY_ICO,ORG1.NAME AS CONTRACTING_PARTY_NAME "+
			" FROM " +
			ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE LEFT JOIN " +ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID), "+
			ConnectionBean.getDbPrefix()+"EQ_ORDERSTATUS ORDSTATUS, " +
			ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID), "+
			ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG," +
			ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG1" +
			" WHERE " +
			" ORDSTATUS.TRIL_GID = QUOTE.EQ_WFSTATUS " +
			" AND QUOTE.CONFIGURATIONS = HR.TRIL_GID " +
			" AND QUOTE.EQ_DELIVERYPARTY = ORG.TRIL_GID "+
			" AND QUOTE.EQ_REQUESTINGPARTY= ORG1.TRIL_GID "+ 
			" AND QUOTE.QUOTENUMBER = ?";
	
	public EarrachJSONParser(EarrachExtractionView earrachExtractViewComponents,String fileName,Dimension screenSize,boolean isOnlyForClosed) throws FileNotFoundException, IOException, ParseException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		this.fileName = fileName;
		this.earrachExtractViewComponents = earrachExtractViewComponents;
		this.screenSize = screenSize;
		productHrefMap = new HashMap<String,ArrayList<String[]>>();
		
		ArrayList<String> offers = getOfferList();
		int totOffers = offers.size();
		
		ArrayList<String[]> ARR = new ArrayList<String[]>();
		double count = 0.0;
		for(String offer: offers)
		{
			ArrayList<String[]> info = getQuoteInfo(offer);
			for(String arr[] : info)
			{
			ARR.add(arr);
	        }
			count++;
			ProgressMonitorPane.getInstance().setProgress(count,totOffers);
			
		}
		
		String path = fileName;	
		int index = path.lastIndexOf(".");
		String sub = path.substring(0,index) ;
		String csvPath=sub+"_OFFER_DETAILS.csv";
		productHrefMap = getHrefList(isOnlyForClosed);
		showTable(csvPath,ARR,productHrefMap);
		
		// write csv and show on Table / export
		earrachExtractViewComponents.getQueryResult().append("\n FILE EXPORTED SUCCESSFULLY ....\n"+csvPath);
		earrachExtractViewComponents.getQueryResult().append("\n Process Completed Successfully....");
		
	}
	
	private HashMap<String,ArrayList<String[]>> getHrefList(boolean isOnlyForClosed) throws FileNotFoundException, IOException, ParseException{
		earrachExtractViewComponents.getQueryResult().append("\n Start file reading for HrefList.."+fileName);
		earrachExtractViewComponents.getQueryResult().append("\n Process may take few minutes , please wait ....");
		
	   
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader(fileName));
		
		
		JSONObject jsonObject = (JSONObject) obj;
		
		JSONArray IOVsList = (JSONArray) jsonObject.get("IOVs");
        
        
        @SuppressWarnings("unchecked")
		Iterator<JSONObject> IOVIterator = IOVsList.iterator();
        while (IOVIterator.hasNext()) 
        {
        	 JSONObject IOV = (JSONObject)IOVIterator.next();
        	 //Long sequenceId = (Long)IOV.get("sequenceId");
        	 
        	 
        	 JSONObject products = (JSONObject)IOV.get("products");
        	 JSONArray productList = (JSONArray)products.get("product"); 
        	 
        	 @SuppressWarnings("unchecked")
			 Iterator<JSONObject> productIterator = productList.iterator();
        	 ArrayList<String[]> keyVal = new ArrayList<String[]>();
        	 while (productIterator.hasNext()) 
 	        {
        	   
 	           JSONObject product = (JSONObject)productIterator.next();
 	           String productType = (String)product.get("productType");
 	           
 	           if(isOnlyForClosed && productType.equalsIgnoreCase("offer"))
 	           {
 	           String productHref = (String)product.get("href");
 	           String id = (String)product.get("id");
 	           String versionId = (String)product.get("versionId");
 	            String[] valARR = new String[4];
 	            valARR[0] = productType;
 	            valARR[1] = productHref;
 	            valARR[2] = id;
 	            valARR[3] = versionId;
 	            keyVal.add(valARR);
 	           }
 	           else if(!isOnlyForClosed){
 	        	  String productHref = (String)product.get("href");
 	 	           String id = (String)product.get("id");
 	 	           String versionId = (String)product.get("versionId");
 	 	            String[] valARR = new String[4];
 	 	            valARR[0] = productType;
 	 	            valARR[1] = productHref;
 	 	            valARR[2] = id;
 	 	            valARR[3] = versionId;
 	 	            keyVal.add(valARR);
 	           }
 	           
 	        }
        	 
        	 JSONObject productOrder = (JSONObject)productList.get(1); 
        	
        	 JSONArray productOrderList = (JSONArray)productOrder.get("productOrder"); 
        		 
        		 @SuppressWarnings("unchecked")
				  Iterator<JSONObject> offerIterator = productOrderList.iterator();
        		    String  keyArr = "";
        	        while (offerIterator.hasNext()) 
        	        {
        	        	JSONObject OFFER = (JSONObject)offerIterator.next();
        	            String OFF = (String)OFFER.get("id");
        	            OFF = OFF.substring(OFF.indexOf("-")+1);
        	            keyArr = OFF;
        	        }
        
        	        if(!productHrefMap.containsKey(keyArr))
        	        {
        	 
        	        	// add new key data
        	        productHrefMap.put(keyArr, keyVal);  
        	        }
        	        else
        	        {
        	     
        	        	ArrayList<String[]> exisiting = productHrefMap.get(keyArr);
        	        	for(String[] newV : keyVal){
        	        		exisiting.add(newV);
        	        	}
        	        }
        
        }
        IOVIterator = null;
        IOVsList = null;
        jsonObject = null;
        parser = null;
        obj = null;
        earrachExtractViewComponents.getQueryResult().append("\nFile read successfully.."+fileName);
		return productHrefMap;
		
	}
	
	
	
	
	private ArrayList<String> getOfferList() throws FileNotFoundException, IOException, ParseException{
		earrachExtractViewComponents.getQueryResult().append("\n Start file reading for OfferList.."+fileName);
		earrachExtractViewComponents.getQueryResult().append("\n Process may take few minutes , please wait ....");
		
		ArrayList<String> offers = new ArrayList<String>();
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader(fileName));
		
		
		JSONObject jsonObject = (JSONObject) obj;
		
		JSONArray IOVsList = (JSONArray) jsonObject.get("IOVs");
        
        
        @SuppressWarnings("unchecked")
		Iterator<JSONObject> IOVIterator = IOVsList.iterator();
        while (IOVIterator.hasNext()) 
        {
        	 JSONObject IOV = (JSONObject)IOVIterator.next();
        	 JSONObject products = (JSONObject)IOV.get("products");
        	 JSONArray productList = (JSONArray)products.get("product"); 
        	 JSONObject productOrder = (JSONObject)productList.get(1); 
        	
        	 JSONArray productOrderList = (JSONArray)productOrder.get("productOrder"); 
        		 
        		 @SuppressWarnings("unchecked")
				Iterator<JSONObject> offerIterator = productOrderList.iterator();
        	        while (offerIterator.hasNext()) 
        	        {
        	        	JSONObject OFFER = (JSONObject)offerIterator.next();
        	            String OFF = (String)OFFER.get("id");
        	            OFF = OFF.substring(OFF.indexOf("-")+1);
        	            offers.add(OFF);
        	        }
        }
        IOVIterator = null;
        IOVsList = null;
        jsonObject = null;
        parser = null;
        obj = null;
        earrachExtractViewComponents.getQueryResult().append("\nFile read successfully.."+fileName);
		return offers;
		
	}
	
	private ArrayList<String[]> getQuoteInfo(String quoteNumber) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
	ArrayList<String[]> quote = CommonUtils.getQueryResult(SQL, earrachExtractViewComponents,quoteNumber);
	
	return quote;
	}
	/*
	public static ArrayList<String[]> addEQTYPE1(ArrayList<String[]> serviceBuildItems ,EarrachExtractionView earrachExtractViewComponents) throws SQLException{
     
		String getEQ_TYPE=
			CommonUtils.ORDER_CHANGE_TYPE_DECODE +
			" FROM " +
			ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE," +
			ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG, " +
			ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR " +
			" WHERE QUOTE.QUOTENUMBER =? AND QUOTE.CONFIGURATIONS = HR.TRIL_GID AND HR.DATA = CHG.TRIL_GID";
		
           ArrayList<String[]> updated = new ArrayList<String[]>();
      
           String[] arr = serviceBuildItems.get(0);
           String ORD = CommonUtils.addZeroPrefixinOrder(arr[0]);
    	   ArrayList<String[]> eq_type = CommonUtils.getQueryResult(getEQ_TYPE, ORD,earrachExtractViewComponents,earrachExtractViewComponents.hasshowSQL());
    	   arr[3] = (null == eq_type || eq_type.size() ==0 ? "Not available" :eq_type.get(0)[0]);
    	   updated.add(arr);
      
       
		return updated;
	}
	*/
	
	
	public void showTable(String resultFileLoc,ArrayList<String[]> ARR,HashMap<String,ArrayList<String[]>> hrefMap) throws IOException, SQLException{
		CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(resultFileLoc),true);
		writer.writeNext(csvcolumnNames);
		
		DefaultTableModel model = new DefaultTableModel(0, 0);
		model.setColumnIdentifiers(modelcolumnNames);
		CustomJTable table = new CustomJTable();
		table.getTable().setModel(model);
		table.getTable().setAutoCreateRowSorter(true);
		
		
		
		for (String[] arr : ARR)
		{
			String KEY = arr[0];
			int initLen = arr.length;
			   if(hrefMap.containsKey(KEY))
			   {
				
			    ArrayList<String[]> herfValues = hrefMap.get(KEY);
			    String addButton[] = new String[initLen+1];
			   
			    
			    for(int x = 0 ; x< initLen;x++)
		    	 {
			    	addButton[x] = arr[x];
				 }
			    
			    for(String[] herfValue : herfValues)
			    {
			    	 String extendedArr[] = new String[initLen+4];
			    	 for(int x = 0 ; x< initLen;x++)
			    	 {
					    	extendedArr[x] = arr[x];
					 }
			    	 for(int x1=0;x1<herfValue.length;x1++)
			    	 {
			    		 
			    		 extendedArr[initLen+x1] = herfValue[x1];
			    	 }
			    	 writer.writeNext(extendedArr);
					 //model.addRow(extendedArr);
			    }
			    }  
			   else{
				   writer.writeNext(arr);
				   //model.addRow(arr);  
			   }
			   model.addRow(arr); 
			   
			   OfferDetailsActionRenderer or = new OfferDetailsActionRenderer(table.getTable(),earrachExtractViewComponents,screenSize);
			   TableColumn column1 = table.getTable().getColumnModel().getColumn(modelcolumnNames.length-2);
			   column1.setCellRenderer(or);
			   column1.setCellEditor(or);
			   
			   ProductDetailActionRenderer ar = new ProductDetailActionRenderer(table.getTable(),hrefMap,screenSize);
			   TableColumn column2 = table.getTable().getColumnModel().getColumn(modelcolumnNames.length-1);
			   column2.setCellRenderer(ar);
			   column2.setCellEditor(ar);
			   
			  
			
		}
		writer.close();
		
		JScrollPane scroll = new JScrollPane(table.getTable());
		CustomJFrame f = new CustomJFrame("Total Orders ("+table.getTable().getRowCount()+")",Icons.iconPathEarrach);
		f.setBounds(10,15,screenSize.width * 80 / 100,screenSize.height * 80 / 100);
		f.add(scroll);
		f.setVisible(true);
		
		
	}
	
}
