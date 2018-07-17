package com.orange.util;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.orange.ui.component.EarrachExtractionView;
import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.Icons;
import com.orange.util.csm.CustomJTable;

public class JSONToCSVParser {

	
	private String[] csvcolumnNames =  {"SEQUENCE_ID","QUOTE_NUMBER","PRODUCT_TYPE","PRODUCT_HREF","IO_ID","VERSIONID","IOV_OFFER_STATUS","OTHER HREF(OFFER)"};
	private LinkedHashMap<String[],ArrayList<String[]>> d9Map;
	private EarrachExtractionView earrachExtractViewComponents;
	private Map<D9ParserMapKey,ArrayList<String>> hrefValidationFileMap;
	
	
	
	private Dimension screenSize;
	
	
	
	
	
	
	
	public JSONToCSVParser(EarrachExtractionView earrachExtractViewComponents,File[] files,Dimension screenSize) throws FileNotFoundException, IOException, ParseException, SQLException{
		
		this.earrachExtractViewComponents = earrachExtractViewComponents;
		this.screenSize = screenSize;
		d9Map = new LinkedHashMap<String[],ArrayList<String[]>>();
		
		for(File F : files)
		{
		d9Map = getHrefList(F.getAbsolutePath());
		}
		
		String path = files[0].getAbsolutePath();	
		int index = path.lastIndexOf(".");
		String sub = path.substring(0,index) ;
		String csvPath=sub+"_JSON_DETAILS.csv";
	
		ArrayList<String[]> filterList = filterMap(d9Map);
		showTable(csvPath,filterList);
		
		// write csv and show on Table / export
		earrachExtractViewComponents.getQueryResult().append("\nFILE EXPORTED SUCCESSFULLY ....\n"+csvPath);
		earrachExtractViewComponents.getQueryResult().append("\nProcess Completed Successfully....");
		
	}
	
	
	
  /*
 * This method is used to filter junks from Map. 
 * 1. Only Master HREF will be displayed , others will be displayed in a separate column with comma separated. 
 * 2. Master HREF will be considered where productType=offer and versionId,Id are not null for corresponding sequenceId.
 * 
 */
private ArrayList<String[]> filterMap(Map<String[],ArrayList<String[]>> map) throws SQLException
{
	earrachExtractViewComponents.getQueryResult().append("\nOrder are being mapped. Please wait.\n");
	Iterator<Map.Entry<String[], ArrayList<String[]>>> entries = map.entrySet().iterator();
	ArrayList<String[]> sequenceIdRowData = new ArrayList<String[]>();
	int count = 0;
	int skipcount=0;
	String skipcountinfo="";
	while (entries.hasNext()) 
	{
		
		Map.Entry<String[], ArrayList<String[]>> entry = entries.next();
		String[] key = entry.getKey();
		
		ArrayList<String[]> value = entry.getValue();
		
		if(null != value && value.size()>0)
		{
		String newLine[] = new String[key.length+value.get(0).length];	
		 for(int keyx=0; keyx<key.length;keyx++)
		    {
			 newLine[keyx]=key[keyx];
		    }
		
		
		 ArrayList<String> othersHrefWithOffer= new ArrayList<String>();
		
		 
		boolean hasOfferRowAdded = false;
		for(String[] row: value)
		{
			// If product type = offer	and versionId is not null, take this Href as Core HREF.
			if(null != row[0] && row[0].trim().equalsIgnoreCase("OFFER") && null != row[3])
			{
				// This check only one add one Product Type = offer in a sequence id.
				// If second times product Type = offer found in a sequence id then its Href will be added in Other Href List.
				if(hasOfferRowAdded)
				{
					if(!othersHrefWithOffer.contains(row[1]))
					{ 
						othersHrefWithOffer.add(row[1]);
					}
				}
				else
				{
					// Product Type
					newLine[key.length+0] = row[0];
					// Href
					newLine[key.length+1] = row[1];
					// IO Id
					newLine[key.length+2] = row[2];
					// version Id
					newLine[key.length+3] = row[3];
					//iov status
					newLine[key.length+4] = row[4];
					hasOfferRowAdded = true;
				}
			}
			
			
			
			
		}
		
		String SWithOffer = "";
		for(String ohref: othersHrefWithOffer)
		{
			SWithOffer+=ohref+",";
		}
		newLine[key.length+5] = SWithOffer;
		othersHrefWithOffer = null;
		sequenceIdRowData.add(newLine);
		count++;
		ProgressMonitorPane.getInstance().setProgress(count,map.size());
	 }
		else
		{
			skipcount++;
			skipcountinfo+="Sequence Id >> "+(null == key[0] && key[0].length() ==0 ? "NA" : key[0]) +" And Order  "+(null == key[1] && key[1].length() ==0 ? "NA" : key[1])+"\n";
		}
		
	}
	if(skipcount>0)
	{
		JOptionPane.showMessageDialog(null,"Total " +skipcount+" Records Skipped.\n Deatails -----------\n "+skipcountinfo);
		count++;
		ProgressMonitorPane.getInstance().setProgress(count,map.size());
		earrachExtractViewComponents.getQueryResult().append("\nTotal " +skipcount+" Records Skipped.\n Deatails -----------\n "+skipcountinfo);
	}
		skipcount = 0;
		earrachExtractViewComponents.getQueryResult().append("\nOrder mapped successfully.");
		return sequenceIdRowData;
}


	
	
	private LinkedHashMap<String[],ArrayList<String[]>> getHrefList(String fileName) throws FileNotFoundException, IOException, ParseException{
		earrachExtractViewComponents.getQueryResult().append("\nStart file reading for HrefList.."+fileName);
		earrachExtractViewComponents.getQueryResult().append("\nProcess may take few minutes , please wait ....");
	  
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader(fileName));
		
		
		JSONObject jsonObject = (JSONObject) obj;
		
		JSONArray IOVsList = (JSONArray) jsonObject.get("IOVs");
        
        
        @SuppressWarnings("unchecked")
		Iterator<JSONObject> IOVIterator = IOVsList.iterator();
        
       
        while (IOVIterator.hasNext()) 
        {
        	 String[] d9MapKey = new String[2];
        	 JSONObject IOV = (JSONObject)IOVIterator.next();
        	
        	 Long sequenceId = (Long)IOV.get("sequenceId");
        	 
        	 String d9MapKeyPart1 = sequenceId.toString();
        	 d9MapKey[0]=d9MapKeyPart1;
        	 
        	 
        	 JSONObject products = (JSONObject)IOV.get("products");
        	 JSONArray productList = (JSONArray)products.get("product"); 
        	 
        	 @SuppressWarnings("unchecked")
			 Iterator<JSONObject> productIterator = productList.iterator();
        	 ArrayList<String[]> keyVal = new ArrayList<String[]>();
        	 while (productIterator.hasNext()) 
 	        {
        	   
 	           JSONObject product = (JSONObject)productIterator.next();
 	           String productType = (String)product.get("productType");
 	           String productHref = (String)product.get("href");
 	           String iovId 	  = (String)product.get("id");
 	           String versionId   = (String)product.get("versionId");
 	           String iovsstatus  = (String)product.get("status");
 	          
 	           
 	           String[] valARR = new String[csvcolumnNames.length];
 	           valARR[0] = productType;
 	           valARR[1] = productHref;
 	           valARR[2] = iovId;
 	           valARR[3] = versionId;
 	           valARR[4] = iovsstatus;
 	           valARR[5] = ""; 
 	           valARR[6] = ""; 
 	           valARR[7] = ""; 
 	         
 	       
 	          
 	           if(null !=productHref && productHref.length()>0)
 	           {
 	        	   keyVal.add(valARR);
 	           }
 	        }
        	 
        	 JSONObject productOrder = (JSONObject)productList.get(1); 
        	
        	 JSONArray productOrderList = (JSONArray)productOrder.get("productOrder"); 
        		 
        		 @SuppressWarnings("unchecked")
				  Iterator<JSONObject> offerIterator = productOrderList.iterator();
        		
        		    String d9MapKeyPart2="";
        	        while (offerIterator.hasNext()) 
        	        {
        	        	JSONObject OFFER = (JSONObject)offerIterator.next();
        	            String OFF = (String)OFFER.get("id");
        	            OFF = OFF.substring(OFF.indexOf("-")+1);
        	            d9MapKeyPart2 = OFF;
        	        }
        	        
        	        d9MapKey[1]=d9MapKeyPart2;
        	        
        	        if(!d9Map.containsKey(d9MapKey))
        	        {
        	        	d9Map.put(d9MapKey, keyVal);  
        	        }
        	        else
        	        {
        	        	ArrayList<String[]> exisiting = d9Map.get(d9MapKey);
        	        	for(String[] newV : keyVal)
        	        	{
        	        		exisiting.add(newV);
        	        	}
        	        }
        	     
        
        }
        IOVIterator = null;
        IOVsList = null;
        jsonObject = null;
        parser = null;
        obj = null;
        earrachExtractViewComponents.getQueryResult().append("\nFile read successfully.."+fileName+"\n");
	
		return d9Map;
		
	}
	@SuppressWarnings("unused")
	private void showMapDetails(){
		Iterator<Map.Entry<D9ParserMapKey, ArrayList<String>>> entries1 = hrefValidationFileMap.entrySet().iterator();
		while (entries1.hasNext()) {
			Map.Entry<D9ParserMapKey, ArrayList<String>> entry = entries1.next();
			D9ParserMapKey key = entry.getKey();
			ArrayList<String> value = entry.getValue();
			System.out.println("=====================");
			System.out.println("Key is "+key.getCoreProduct() +" and " +key.getHref());
			for(String s: value){
			System.out.print(s+",");
			}
			System.out.println("=====================");
		}
	}
	public void showTable(String resultFileLoc,ArrayList<String[]> dataRows) throws IOException{
		CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(resultFileLoc),true);
		writer.writeNext(csvcolumnNames);
		DefaultTableModel model = new DefaultTableModel(0, 0);
		model.setColumnIdentifiers(csvcolumnNames);
		
		CustomJTable table = new CustomJTable();
		table.getTable().setModel(model);
		table.getTable().setAutoCreateRowSorter(true);
		
		
		for(String[] row : dataRows)
		{
			writer.writeNext(row);
			model.addRow(row); 
		}
		writer.close();
		
		JScrollPane scroll = new JScrollPane(table.getTable(),JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		CustomJFrame f = new CustomJFrame("Total Orders ("+table.getTable().getRowCount()+")",Icons.iconPathEarrach);
		f.setBounds(10,15,screenSize.width * 80 / 100,screenSize.height * 80 / 100);
		f.add(scroll);
		f.setVisible(true);
	}
	
	
	
	
	
	
	
	
	
	
	
	
}

