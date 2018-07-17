package com.orange.util;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.ui.component.EarrachExtractionView;
import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.Directories;
import com.orange.ui.component.custom.Icons;
import com.orange.util.csm.CustomJTable;

public class EarrachJSON_D9Parser {

	
	private String[] csvcolumnNames =  {"SEQUENCE_ID","QUOTE_NUMBER","PRODUCT_TYPE","PRODUCT_HREF","IO","VERSIONID","IOV_OFFER_STATUS","HREF(OFFER)","HREF_MLI","HREF_ELEMENT","HREF_COMPANION","HREF_COMPONENT","HREF_OTHERS","OFFERSTATUS","ORDER_TYPE","ORDER_CHANGE_TYPE","ANALYSIS_HERF","ALALYSIS_HERF_COMMENTS","ANALYSIS_DB","ALALYSIS_DB_COMMENTS","IOV_OFFER_STATUS_CHECK","IOV_OFFER_STATUS_ANALYSIS","HREF_OFFER_STATUS_CHECK","HREF_OFFER_STATUS_ANALYSIS","MAIN_HREF_CHECK","MAIN_HREF_ANALYSIS","MILESTONE_STATUS","MILESTONE_ANALYSIS","CONSOLIDATED_RESULT"};
	private LinkedHashMap<String[],ArrayList<String[]>> d9Map;
	private EarrachExtractionView earrachExtractViewComponents;
	private Map<D9ParserMapKey,String> hrefCATMap;
	private Map<String,ArrayList<String>> hrefOfferStatus;
	private Map<D9ParserMapKey,ArrayList<String>> hrefValidationFileMap;
	private Map<String,ArrayList<String[]>> iovOfferStatus;
	
	// as discussed with shivani on 23 rd Jan this check should skip for closed order status.
	private String mileStoneSQL = "SELECT EQ_TITLE FROM "+
			ConnectionBean.getDbPrefix()+"EQMILESTONE MILE, "+
			ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE ,"+
			ConnectionBean.getDbPrefix()+"EQ_ORDERSTATUS ORDSTATUS "+
			"WHERE MILE.EQ_ORDERGID = QUOTE.TRIL_GID "+
			"AND MILE.EQ_STATUS='Open' "+
			"AND (MILE.EQ_TITLE = 'Setup Stop Billing' OR MILE.EQ_TITLE = 'Setup Billing')"+
			"AND QUOTE.QUOTENUMBER=? " +
			"AND ORDSTATUS.TRIL_GID = QUOTE.EQ_WFSTATUS " +
			"AND ORDSTATUS.EQ_STATUS <> 'Closed' ";
	
	private String mileStoneSQLForOne = "SELECT EQ_TITLE FROM "+
	ConnectionBean.getDbPrefix()+"EQMILESTONE MILE, "+
	ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE "+
	"WHERE MILE.EQ_ORDERGID = QUOTE.TRIL_GID "+
	"AND MILE.EQ_STATUS='Open' "+
	"AND MILE.EQ_TITLE = 'Setup Billing'"+
	"AND QUOTE.QUOTENUMBER=? ";
	
	private String mileStoneSQLForTwo = "SELECT EQ_TITLE FROM "+
	ConnectionBean.getDbPrefix()+"EQMILESTONE MILE, "+
	ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE "+
	"WHERE MILE.EQ_ORDERGID = QUOTE.TRIL_GID "+
	"AND MILE.EQ_STATUS='Open' "+
	"AND MILE.EQ_TITLE = 'Setup Stop Billing'"+
	"AND QUOTE.QUOTENUMBER=? ";
	
	private Map<String,ArrayList<String>> onlyValidForMap ;
	
	private Dimension screenSize;
	
	
	
	private String SQL ="SELECT DISTINCT " +
			CommonUtils.ORDER_TYPE_DECODE+
			CommonUtils.EQ_TYPE_DECODE+
			" ORDSTATUS.EQ_STATUS " +
			" FROM " +
			ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID),"+
			ConnectionBean.getDbPrefix()+"EQ_ORDERSTATUS ORDSTATUS, " +
			ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID) "+
			"WHERE " +
			"ORDSTATUS.TRIL_GID = QUOTE.EQ_WFSTATUS " +
			"AND QUOTE.CONFIGURATIONS = HR.TRIL_GID " +
			"AND QUOTE.QUOTENUMBER = ?";
	
	private String SQLPREVSTATUS ="SELECT DISTINCT " +
	" ORDSTATUS.EQ_OLDSTATUS " +
	" FROM " +
	ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID),"+
	ConnectionBean.getDbPrefix()+"EQ_ORDERSTATUS ORDSTATUS, " +
	ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID) "+
	
	"WHERE " +
	"ORDSTATUS.TRIL_GID = QUOTE.EQ_WFSTATUS " +
	"AND QUOTE.CONFIGURATIONS = HR.TRIL_GID " +
	"AND QUOTE.QUOTENUMBER = ?";
	
	
	public EarrachJSON_D9Parser(EarrachExtractionView earrachExtractViewComponents,File[] files,Dimension screenSize) throws FileNotFoundException, IOException, ParseException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		
		this.earrachExtractViewComponents = earrachExtractViewComponents;
		this.screenSize = screenSize;
		
		initHrefPairs();
		
		d9Map = new LinkedHashMap<String[],ArrayList<String[]>>();
		
		for(File F : files)
		{
		d9Map = getHrefList(F.getAbsolutePath());
		}
		
		
		
		
		String path = files[0].getAbsolutePath();	
		int index = path.lastIndexOf(".");
		String sub = path.substring(0,index) ;
		String csvPath=sub+"_D9_VALIDATION_DETAILS.csv";
	
		
		
		ArrayList<String[]> filterList = filterMap(d9Map);
		ArrayList<String[]> filterListWithHrefPairsComments = offerHrefMapingValidation(filterList);
		
		filterListWithHrefPairsComments = offerHrefMapingValidation_ON_MLI_ELEMENT_COMPANION(filterListWithHrefPairsComments,"MLI",8);
		filterListWithHrefPairsComments = offerHrefMapingValidation_ON_MLI_ELEMENT_COMPANION(filterListWithHrefPairsComments,"ELEMENT",9);
		filterListWithHrefPairsComments = offerHrefMapingValidation_ON_MLI_ELEMENT_COMPANION(filterListWithHrefPairsComments,"COMPANION",10);
		filterListWithHrefPairsComments = offerHrefMapingValidation_ON_COMPONENT(filterListWithHrefPairsComments,"COMPONENT",11);
		
	     
		
		ArrayList<String[]> filterListWithValidation = filterMapForValidation(filterListWithHrefPairsComments);
		filterListWithValidation = validateIOVOfferStatus(filterListWithValidation,iovOfferStatus);
		filterListWithValidation = validateHREFOfferStatus(filterListWithValidation,hrefOfferStatus);
		filterListWithValidation = validateMainHREFStatus(filterListWithValidation);
		filterListWithValidation = validateMileStoneStatus(filterListWithValidation);
		filterListWithValidation =  validateMileStoneStatusForOne(filterListWithValidation);
		filterListWithValidation =  validateMileStoneStatusForTwo(filterListWithValidation);
		
		ArrayList<String[]> filterListWithConsolidatedAnalysis = tuneWithConsolidatedAnalysis(filterListWithValidation);
		
		
		showTable(csvPath,filterListWithConsolidatedAnalysis);
		
		// write csv and show on Table / export
		earrachExtractViewComponents.getQueryResult().append("\nFILE EXPORTED SUCCESSFULLY ....\n"+csvPath);
		earrachExtractViewComponents.getQueryResult().append("\nProcess Completed Successfully....");
		
	}
	
	private String addInRow(String data){
		String updated ="";
		data = data.replace("null", "");
		String arr[] = data.split(",");
		ArrayList<String> STRARR = new ArrayList<String>();
		for(String S : arr)
		{
			if(null != S && S.length()>0 && !S.equals(","))
			{
			if(!STRARR.contains(S)){
				STRARR.add(S);
			}
			}
		}
		for(String S : STRARR)
		{
			updated+=S+",";
		}
		return updated;
	}
	
  /*
 * This method is used to filter junks from Map. 
 * 1. Only Master HREF will be displayed , others will be displayed in a separate column with comma separated. 
 * 2. Master HREF will be considered where productType=offer and versionId,Id are not null for corresponding sequenceId.
 * 
 */
private ArrayList<String[]> filterMap(Map<String[],ArrayList<String[]>> map) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
{
	earrachExtractViewComponents.getQueryResult().append("\nOrder Status being mapped. Please wait.\n");
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
		 ArrayList<String> othersHrefWithMLI= new ArrayList<String>();
		 ArrayList<String> othersHrefWithElement= new ArrayList<String>();
		 ArrayList<String> othersHrefWithCompanion= new ArrayList<String>();
		 ArrayList<String> othersHrefWithComponent= new ArrayList<String>();
		 ArrayList<String> othersHrefWithOthers= new ArrayList<String>();
		 
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
					ArrayList<String[]> info = getQuoteInfo(key[1]);
					
					if(null != info && info.size()>0 )
					{
					// ORDER STATUS
					newLine[key.length+11] = info.get(0)[2];
					// ORDER TYPE
					newLine[key.length+12] = info.get(0)[0];
					// ORDER CHANGETYPE
					newLine[key.length+13] = info.get(0)[1];
					}
					else
					{
						newLine[key.length+11] = "NA";
						// ORDER TYPE
						newLine[key.length+12] = "NA";
						// ORDER CHANGETYPE
						newLine[key.length+13] = "NA";
					}
					//ANALYSIS
					newLine[key.length+14] = "";
					//ANALYSIS COMMENTS
					newLine[key.length+15] = "";
					hasOfferRowAdded = true;
				}
			}
			
			// If product type = MLI take this Href as MLI HREF.
			else if(null != row[0] && row[0].trim().equalsIgnoreCase("MLI") && null != row[1])
			{
					if(!othersHrefWithMLI.contains(row[1]))
					{ 
						othersHrefWithMLI.add(row[1]);
					}
			}
			// If product type = Element take this Href as Element HREF.
			else if(null != row[0] && row[0].trim().equalsIgnoreCase("ELEMENT") && null != row[1])
			{
					if(!othersHrefWithElement.contains(row[1]))
					{ 
						othersHrefWithElement.add(row[1]);
					}
			}
			// If product type = Attribute take this Href as Attribute HREF.
			else if(null != row[0] && row[0].trim().equalsIgnoreCase("COMPANION") && null != row[1])
			{
					if(!othersHrefWithCompanion.contains(row[1]))
					{ 
						othersHrefWithCompanion.add(row[1]);
					}
			}
			// If product type = Component take this Href as Component HREF.
			else if(null != row[0] && row[0].trim().equalsIgnoreCase("COMPONENT") && null != row[1])
			{
					if(!othersHrefWithComponent.contains(row[1]))
					{ 
						othersHrefWithComponent.add(row[1]);
					}
			}
			// The below else will be used only if we requited HREF other then OFFER/MLI/ATTRIBUTE/ELEMENT/COMPONENT productType
			else
			{
				
				if(!othersHrefWithOthers.contains(row[1]))
				{
					othersHrefWithOthers.add(row[1]);
				}
			}
			
		}
		
		String SWithOffer = "";
		String SWithMLI = "";
		String SWithELEMENT = "";
		String SWithCOMPANION = "";
		String SWithCOMPONENT = "";
		String SWithOTHERS = "";
		
		
		for(String ohref: othersHrefWithOffer)
		{
			SWithOffer+=ohref+",";
		}
		for(String ohref: othersHrefWithMLI)
		{
			SWithMLI+=ohref+",";
		}
		for(String ohref: othersHrefWithElement)
		{
			SWithELEMENT+=ohref+",";
		}
		for(String ohref: othersHrefWithCompanion)
		{
			SWithCOMPANION+=ohref+",";
		}
		for(String ohref: othersHrefWithComponent)
		{
			SWithCOMPONENT+=ohref+",";
		}
		for(String ohref: othersHrefWithOthers)
		{
			SWithOTHERS+=ohref+",";
		}
		
		newLine[key.length+5] = SWithOffer;
		newLine[key.length+6] = SWithMLI;
		newLine[key.length+7] = SWithELEMENT;
		newLine[key.length+8] = SWithCOMPANION;
		newLine[key.length+9] = SWithCOMPONENT;
		newLine[key.length+10] = SWithOTHERS;
		
		othersHrefWithOffer = null;
		othersHrefWithMLI = null;
		othersHrefWithElement = null;
		othersHrefWithCompanion = null;
		othersHrefWithComponent = null;
		othersHrefWithOthers = null;
		
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
		earrachExtractViewComponents.getQueryResult().append("\nOrder Status mapped successfully.");
		return sequenceIdRowData;
}


	private ArrayList<String[]> filterMapForValidation(ArrayList<String[]> filterListWithHrefPairsComments) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		earrachExtractViewComponents.getQueryResult().append("\nOffer , Order status validation being processed. Please wait.\n");
		
		//Here we will add Offer | D9 | Closed
		
		for(String[] row : filterListWithHrefPairsComments)
		{
		  if(null != row[18] && row[18].length()> 0)
		  {
			  String CATS = row[18];
			  String arr[] = CATS.split(",");
			  for(String CAT : arr)
			  {
			  if(CAT.equalsIgnoreCase("CAT1"))
			  {
				  if(null != row[13] && row[13].length()> 0 && row[13].trim().equalsIgnoreCase("Closed"))
				  {
					  
					  row[18] ="PASS";
				  }
				  else
				  {
					  row[18] ="FAILED";
					  row[19] =row[19]+"##Order Status is Not Closed."; 
					  row[19] =addInRow(row[19]);
				  }
			  }
			  else if(CAT.equalsIgnoreCase("CAT2"))
			  {
				  if(null != row[13] && row[13].length()> 0 && (row[13].trim().equalsIgnoreCase("Creation") || row[13].trim().equalsIgnoreCase("Pricing")))
				  {
					  row[18] ="PASS";
				  }
				  else if(null != row[13] && row[13].length()> 0 && row[13].trim().equalsIgnoreCase("On-Hold"))
				  {
					  
					  row[0] = row[0]+"[ON-HOLD]";
					  row[0] =addInRow(row[0]);
					  
					  ArrayList<String[]> info = getQuotePrevStateInfo(row[1]);
					
					 
					  if(null == info.get(0)[0] || info.get(0)[0].length()==0)
					  {
						  row[13] ="COULD NOT RETRIVE";
						  row[18] ="FAILED";
						  row[19] =row[19]+"##Previous Order Status could not reterived.";   
						  row[19] =addInRow(row[19]);
					  }
					  else if(info.get(0)[0].equalsIgnoreCase("Creation") || info.get(0)[0].equalsIgnoreCase("Pricing"))
					  {
						  row[13] =info.get(0)[0];
						  row[18] ="PASS";
						  row[19] =addInRow("*PREV STATE WAS "+info.get(0)[0]);
					  }
					  else
					  {
						  row[13] =info.get(0)[0];
						  row[18] ="FAILED";
						  row[19] =row[19]+"##Previous Order Status is not on Creation Or Pricing.";   
						  row[19] =addInRow(row[19]);
					  }
				  }
				  else
				  {
					  row[18] ="FAILED";
					  row[19] =row[19]+"##Order Status is not on Creation Or Pricing.";  
					  row[19] =addInRow(row[19]);
				  }
				  
			  }
			  
			  else if(CAT.equalsIgnoreCase("CAT3"))
			  {
				  String[] checkState = {"Approval","Release","Manage","Acceptance","Billing"};
				  
				  if(null != row[13] && row[13].length()> 0 && !row[13].trim().equalsIgnoreCase("On-Hold"))
				  {
					  boolean isPass = false;
					  for(String state : checkState)
					  {
						if(row[13].trim().equalsIgnoreCase(state) )
						{
							isPass = true;
						}
					  }
					if(isPass)
					{
					  row[18] ="PASS";
					}
					else
					{
						
						row[18] ="FAILED";
						row[19] =row[19]+"##Order Status is not on Approval,Release,Manage,Acceptance,Billing.";  
						row[19] =addInRow(row[19]);
					}
				  }
				  else if(null != row[13] && row[13].length()> 0 && row[13].trim().equalsIgnoreCase("On-Hold"))
				  {
					  
					  row[0] = row[0]+"[ON-HOLD]";
					  row[0] =addInRow(row[0]);
					  
					  ArrayList<String[]> info = getQuotePrevStateInfo(row[1]);
					  if(null == info.get(0)[0] || info.get(0)[0].length()==0)
					  {
						  row[13] ="COULD NOT RETRIVE";
						  row[18] ="FAILED";
						  row[19] =row[19]+"##Previous Order Status could not reterived.";   
					  }
					  else 
					  {
						  boolean isPass = false;
						  for(String state : checkState)
						  {
							if(info.get(0)[0].equalsIgnoreCase(state) )
							{
								isPass = true;
							}
						  }
						if(isPass)
						{
						  row[13] =info.get(0)[0];
						  row[18] ="PASS";
						  row[19] =addInRow("**PREV STATE WAS "+info.get(0)[0]);
						}
						else
						{
							row[13] =info.get(0)[0];
							row[18] ="FAILED";
							row[19] =row[19]+"##Previous Order Status is not on Approval,Release,Manage,Acceptance,Billing.";  
							row[19] =addInRow(row[19]);
						}
						  
					  }
				  }
				}
			  
			  else if(CAT.equalsIgnoreCase("CAT4"))
			  {
				  if(null != row[13] && row[13].length()> 0 && !(row[13].trim().equalsIgnoreCase("Closed")))
				  {
					  row[18] ="PASS";
				  }
				  else if(null != row[13] && row[13].length()> 0 && row[13].trim().equalsIgnoreCase("On-Hold"))
				  {
					  
					  row[0] = row[0]+"[ON-HOLD]";
					  row[0] =addInRow(row[0]);
					  
					  ArrayList<String[]> info = getQuotePrevStateInfo(row[1]);
					
					 
					  if(null == info.get(0)[0] || info.get(0)[0].length()==0)
					  {
						  row[13] ="COULD NOT RETRIVE";
						  row[18] ="FAILED";
						  row[19] =row[19]+"##Previous Order Status could not reterived.";   
						  row[19] =addInRow(row[19]);
					  }
					  else if(!info.get(0)[0].equalsIgnoreCase("Closed") )
					  {
						  row[13] =info.get(0)[0];
						  row[18] ="PASS";
					  }
					  else
					  {
						  row[13] =info.get(0)[0];
						  row[18] ="FAILED";
						  row[19] =row[19]+"##Previous Order Status is on Closed state.";   
						  row[19] =addInRow(row[19]);
					  }
				  }
				  else
				  {
					  row[18] ="FAILED";
					  row[19] =row[19]+"##Order Status is on Closed state.";  
					  row[19] =addInRow(row[19]);
				  }
				  
			  }
			  
		  		}
			  }
		}
		ArrayList<String[]> filterListWithValidation = new ArrayList<String[]> (filterListWithHrefPairsComments);
		
		earrachExtractViewComponents.getQueryResult().append("\nOffer , Order status validation completed.");
		return filterListWithValidation;
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
 	          
 	           
 	           String[] valARR = new String[csvcolumnNames.length-2];
 	           valARR[0] = productType;
 	           valARR[1] = productHref;
 	           valARR[2] = iovId;
 	           valARR[3] = versionId;
 	           valARR[4] = iovsstatus;
 	           
 	           
 	           valARR[5] = ""; 
 	           valARR[6] = ""; 
 	           valARR[7] = ""; 
 	           valARR[8] = ""; 
 	           valARR[9] = ""; 
 	           valARR[10] = ""; 
 	           
 	           // ORDER STATUS
 	           valARR[11] = "";
 	           //ORDER TYPE
 	           valARR[12] = "";
 	           //ORDER CHANGE TYPE
 	           valARR[13] = ""; 
 	           
 	           valARR[14] = "";
 	           valARR[15] = "";
 	       
 	          
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
	private ArrayList<String[]> getQuoteInfo(String quoteNumber) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
	ArrayList<String[]> quote = CommonUtils.getQueryResult(SQL,earrachExtractViewComponents,quoteNumber);
	return quote;
	}
	private ArrayList<String[]> getQuotePrevStateInfo(String quoteNumber) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
	ArrayList<String[]> quote = CommonUtils.getQueryResult(SQLPREVSTATUS, earrachExtractViewComponents,quoteNumber);
	return quote;
	}
	
	private Map<String,ArrayList<String>> initHREFOfferStatusCkeck(String href,String orderType){
			
			
			if(!hrefOfferStatus.containsKey(href))
			{
				ArrayList<String> list = new ArrayList<String>();
				list.add(orderType);
				hrefOfferStatus.put(href, list);
			}
			else
			{
				ArrayList<String> list  = hrefOfferStatus.get(href);
				list.add(orderType);
				hrefOfferStatus.put(href, list);
			}
			
			
			return hrefOfferStatus;
		}
	
	private void initHrefPairs() throws IOException{
		hrefValidationFileMap = new LinkedHashMap<D9ParserMapKey,ArrayList<String>>();
		iovOfferStatus = new LinkedHashMap<String,ArrayList<String[]>>();
		hrefOfferStatus = new LinkedHashMap<String,ArrayList<String>>();
		hrefCATMap = new LinkedHashMap<D9ParserMapKey,String>();
		onlyValidForMap = new LinkedHashMap<String,ArrayList<String>>();
		
		String[] Info  =  new String[]{"PLEASE MAKE SURE NO WHITE SPACES AND NO MULTIPLE CHARS ENTRY IN ANY CELL EXCEPT CATEGORY COLUMNS."};
		String[] header  =  new String[]{"CATEGORY","HREFPRODUCT","HREFKEY","HERF","HREF"};
		String[] iovOfferStatusHeader  =  new String[]{"IOV_OFFER_STATUS","ORDER_STATUS","ORDER_TYPE"};
		String[] hrefOfferStatusHeader =  new String[]{"HREF","VALID_ORDER_TYPE"};
		
		
		String[] A  	 =  new String[]{"CAT4","OFFER","1","3","4","5","8","C","E","!2","!7","!9","!A","!D"};
		String[] B   	 =  new String[]{"CAT4","OFFER","2","4","5","C","E","!1","!3","!7","!8","!9","!A","!D"};
		String[] C   	 =  new String[]{"CAT4","OFFER","3","1","4","5","8","A","C","E","!2","!7","!9","!D"};
		String[] D   	 =  new String[]{"CAT4","OFFER","4","1","2","3","5","7","8","A","C","E","!9","!D"};
		
		String[] E   	 =  new String[]{"CAT3","OFFER","5","1","2","3","4","8","C","E","!7","!9","!A","!D"};
		
		String[] F   	 =  new String[]{"CAT4","OFFER","8","1","3","4","5","A","C","E","!2","!7","!9","!D"};
		String[] G   	 =  new String[]{"CAT1","OFFER","9","D"}; 
		String[] H   	 =  new String[]{"CAT2","OFFER","A","1","2","3","4","8","C","E","!5","!7","!9","!D"};
		String[] I   	 =  new String[]{"CAT4","OFFER","C","1","2","3","4","5","8","A","E","!7","!9","!D"};
		String[] J   	 =  new String[]{"CAT1","OFFER","D","9"};
		String[] K   	 =  new String[]{"CAT4","OFFER","E","1","2","3","4","5","7","8","A","C","!9","!D"};
		
		/*
		1.	Href = 4 is valid with href = 7,E and at element/MLI/Companion level only.
		2.	Href = 7 is valid with href = 4,E and at element/MLI/Companion level only.
		3.	Href = E is valid with href = 4,7 and at element/MLI/Companion level only.
		4.	Href = 7 is only allowed at MLI level.
        */
		
		
		String[] L1   	 =  new String[]{"CAT4","ELEMENT","4","1","2","3","5","7","8","A","C","E","!9","!D"};
		String[] L2   	 =  new String[]{"CAT4","MLI","4","1","2","3","5","7","8","A","C","E","!9","!D"};
		String[] L3   	 =  new String[]{"CAT4","COMPANION","4","1","2","3","5","7","8","A","C","E","!9","!D"};
		
		String[] M1   	 =  new String[]{"CAT4","ELEMENT","E","1","2","3","4","5","7","8","A","C","!9","!D"};
		String[] M2   	 =  new String[]{"CAT4","MLI","E","1","2","3","4","5","7","8","A","C","!9","!D"};
		String[] M3   	 =  new String[]{"CAT4","COMPANION","E","1","2","3","4","5","7","8","A","C","!9","!D"};
		
		String[] N1   	 =  new String[]{"CAT4","MLI","7","4","E","!1","!2","!3","!5","!8","!9","!A","!C","!D"};
		
		// CREATE hrefOfferStatus FILE
		File FILE2 = new File(Directories.hrefOfferStatus);
		if(!FILE2.exists())
		{
			initHREFOfferStatusCkeck("1","New");
			initHREFOfferStatusCkeck("1","Change");
			initHREFOfferStatusCkeck("2","Disconnect");
			initHREFOfferStatusCkeck("3","New");
			initHREFOfferStatusCkeck("3","Change");
			initHREFOfferStatusCkeck("4","New");
			initHREFOfferStatusCkeck("4","Change");
			initHREFOfferStatusCkeck("4","Disconnect");
			initHREFOfferStatusCkeck("5","New");
			initHREFOfferStatusCkeck("5","Change");
			initHREFOfferStatusCkeck("5","Disconnect");
			initHREFOfferStatusCkeck("7","New");
			initHREFOfferStatusCkeck("7","Change");
			initHREFOfferStatusCkeck("7","Disconnect");
			initHREFOfferStatusCkeck("8","New");
			initHREFOfferStatusCkeck("8","Change");
			initHREFOfferStatusCkeck("9","New");
			initHREFOfferStatusCkeck("9","Change");
			initHREFOfferStatusCkeck("A","New");
			initHREFOfferStatusCkeck("A","Change");
			initHREFOfferStatusCkeck("A","Disconnect");
			initHREFOfferStatusCkeck("C","New");
			initHREFOfferStatusCkeck("C","Change");
			initHREFOfferStatusCkeck("C","Disconnect");
			initHREFOfferStatusCkeck("D","New");
			initHREFOfferStatusCkeck("D","Change");
			initHREFOfferStatusCkeck("E","New");
			initHREFOfferStatusCkeck("E","Change");
			hrefOfferStatus = initHREFOfferStatusCkeck("E","Disconnect");
			
			earrachExtractViewComponents.getD9().setEnabled(false);
			
			CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(Directories.hrefOfferStatus),true);
			writer.writeNext(hrefOfferStatusHeader);
			
			Iterator<Map.Entry<String, ArrayList<String>>> entries = hrefOfferStatus.entrySet().iterator();
			while (entries.hasNext()) 
			{
				Map.Entry<String, ArrayList<String>> entry = entries.next();
				String KEY = entry.getKey();
				ArrayList<String> value = entry.getValue();
				for(String arr : value)
				{
				String arrArray[] = {KEY,arr};	
			    writer.writeNext(arrArray);
				}
			}
			writer.close();
			hrefOfferStatus = null;
			earrachExtractViewComponents.getQueryResult().append("\nNecessary File "+ Directories.hrefOfferStatus +" was missing.\nTemporary file has been created for the required format.\nPlease update the file as per your requirements then restart application.");
			JOptionPane.showMessageDialog(null,"Necessary File "+ Directories.hrefOfferStatus +" was missing.\nTemporary file has been created for the required format.\nPlease update the file as per your requirements then restart application.");
			earrachExtractViewComponents.getQueryResult().append("\nPlease wait Cache refreshing...");
			initHrefPairs();
			try{
				Thread.sleep(5000);
				}catch(Exception e){}
			earrachExtractViewComponents.getQueryResult().append("\nSystem Refresh Successfully");
		
		}
		
		
		//CREATE iovOfferStatus FILE
		File FILE1 = new File(Directories.iovOfferStatus);
		if(!FILE1.exists())
		{
			initIOVOfferStatusCkeck("Created","Approval","New");
			initIOVOfferStatusCkeck("Pending_active","Release","New");
			initIOVOfferStatusCkeck("Pending_active","Manage","New");
			initIOVOfferStatusCkeck("Active","Acceptance","New");
			initIOVOfferStatusCkeck("Active","Billing","New");
			initIOVOfferStatusCkeck("Active","Closed","New");
			  	
			initIOVOfferStatusCkeck("Created","Approval","Price Update");
			initIOVOfferStatusCkeck("Active","Billing","Price Update");
			initIOVOfferStatusCkeck("Active","Closed","Price Update");
			initIOVOfferStatusCkeck("Active","Creation","Price Update");
			initIOVOfferStatusCkeck("Active","Pricing","Price Update");
			
			
			initIOVOfferStatusCkeck("Created","Approval","Commercial Migration");
			initIOVOfferStatusCkeck("Active","Billing","Commercial Migration");
			initIOVOfferStatusCkeck("Active","Closed","Commercial Migration");
			initIOVOfferStatusCkeck("Active","Creation","Commercial Migration");
			initIOVOfferStatusCkeck("Active","Pricing","Commercial Migration");
	
	
			initIOVOfferStatusCkeck("Created","Approval","Service Change");
			initIOVOfferStatusCkeck("Pending_active","Release","Service Change");
			initIOVOfferStatusCkeck("Pending_active","Manage","Service Change");
			initIOVOfferStatusCkeck("Active","Creation","Service Change");
			initIOVOfferStatusCkeck("Active","Pricing","Service Change");
			initIOVOfferStatusCkeck("Active","Acceptance","Service Change");
			initIOVOfferStatusCkeck("Active","Billing","Service Change");
			initIOVOfferStatusCkeck("Active","Closed","Service Change");
			
			
			initIOVOfferStatusCkeck("Created","Approval","Migration - Hot Cut/Relocation");
			initIOVOfferStatusCkeck("Pending_active","Release","Migration - Hot Cut/Relocation");
			initIOVOfferStatusCkeck("Pending_active","Manage","Migration - Hot Cut/Relocation");
			initIOVOfferStatusCkeck("Active","Acceptance","Migration - Hot Cut/Relocation");
			initIOVOfferStatusCkeck("Active","Billing","Migration - Hot Cut/Relocation");
			initIOVOfferStatusCkeck("Active","Closed","Migration - Hot Cut/Relocation");
			initIOVOfferStatusCkeck("Active","Creation","Migration - Hot Cut/Relocation");
			initIOVOfferStatusCkeck("Active","Pricing","Migration - Hot Cut/Relocation");
			
			
			// ADD AS PER SIVANI REQUEST ON 23-JAN-2018
			
			initIOVOfferStatusCkeck("Created","Approval","Migration - Soft Cut (Parallel)");
			initIOVOfferStatusCkeck("Pending_active","Manage","Migration - Soft Cut (Parallel)");
			initIOVOfferStatusCkeck("Pending_active","Release","Migration - Soft Cut (Parallel)");
			initIOVOfferStatusCkeck("Active","Creation","Migration - Soft Cut (Parallel)");
			initIOVOfferStatusCkeck("Active","Pricing","Migration - Soft Cut (Parallel)");
			initIOVOfferStatusCkeck("Active","Acceptance","Migration - Soft Cut (Parallel)");
			initIOVOfferStatusCkeck("Active","Billing","Migration - Soft Cut (Parallel)");
			initIOVOfferStatusCkeck("Active","Closed","Migration - Soft Cut (Parallel)");
			
			initIOVOfferStatusCkeck("Pending_active","Manage","Migration - Premium Cut (Parallel)");
			initIOVOfferStatusCkeck("Pending_active","Release","Migration - Premium Cut (Parallel)");
			initIOVOfferStatusCkeck("Created","Approval","Migration - Premium Cut (Parallel)");
			initIOVOfferStatusCkeck("Active","Creation","Migration - Premium Cut (Parallel)");
			initIOVOfferStatusCkeck("Active","Pricing","Migration - Premium Cut (Parallel)");
			initIOVOfferStatusCkeck("Active","Acceptance","Migration - Premium Cut (Parallel)");
			initIOVOfferStatusCkeck("Active","Billing","Migration - Premium Cut (Parallel)");
			initIOVOfferStatusCkeck("Active","Closed","Migration - Premium Cut (Parallel)");
			
			
			initIOVOfferStatusCkeck("Pending_terminate","Approval","Disconnect");
			initIOVOfferStatusCkeck("Pending_terminate","Release","Disconnect");
			initIOVOfferStatusCkeck("Pending_terminate","Manage","Disconnect");
			initIOVOfferStatusCkeck("Active","Creation","Disconnect");
			initIOVOfferStatusCkeck("Active","Pricing","Disconnect");
			initIOVOfferStatusCkeck("Terminated","Awaiting Close","Disconnect");
			iovOfferStatus = initIOVOfferStatusCkeck("Terminated","Closed","Disconnect");
			
			earrachExtractViewComponents.getD9().setEnabled(false);
			CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(Directories.iovOfferStatus),true);
			writer.writeNext(iovOfferStatusHeader);
			
			Iterator<Map.Entry<String, ArrayList<String[]>>> entries = iovOfferStatus.entrySet().iterator();
			while (entries.hasNext()) 
			{
				Map.Entry<String, ArrayList<String[]>> entry = entries.next();
				
				ArrayList<String[]> value = entry.getValue();
				for(String[] arr : value)
				{
			    writer.writeNext(arr);
				}
			}
			writer.close();
			iovOfferStatus = null;
			earrachExtractViewComponents.getQueryResult().append("\nNecessary File "+ Directories.iovOfferStatus +" was missing.\nTemporary file has been created for the required format.\nPlease update the file as per your requirements then restart application.");
			JOptionPane.showMessageDialog(null,"Necessary File "+ Directories.iovOfferStatus +" was missing.\nTemporary file has been created for the required format.\nPlease update the file as per your requirements then restart application.");
			earrachExtractViewComponents.getQueryResult().append("\nPlease wait Cache refreshing...");
			initHrefPairs();
			try{
				Thread.sleep(5000);
				}catch(Exception e){}
			earrachExtractViewComponents.getQueryResult().append("\nSystem Refresh Successfully");

		}
		//CREATE d9Validation FILE
		File FILE = new File(Directories.d9Validation);
		if(!FILE.exists())
		{
			earrachExtractViewComponents.getD9().setEnabled(false);
			
			CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(Directories.d9Validation),true);
			writer.writeNext(Info);
			writer.writeNext(header);
			
			writer.writeNext(A);
			writer.writeNext(B);
			writer.writeNext(C);
			writer.writeNext(D);
			writer.writeNext(E);
			writer.writeNext(F);
			writer.writeNext(G);
			writer.writeNext(H);
			writer.writeNext(I);
			writer.writeNext(J);
			writer.writeNext(K);
			writer.writeNext(L1);
			writer.writeNext(L2);
			writer.writeNext(L3);
			writer.writeNext(M1);
			writer.writeNext(M2);
			writer.writeNext(M3);
			writer.writeNext(N1);
			
			
			writer.close();
			earrachExtractViewComponents.getQueryResult().append("\nNecessary File "+ Directories.d9Validation +" was missing.\nTemporary file has been created for the required format.\nPlease update the file as per your requirements then restart application.");
			JOptionPane.showMessageDialog(null,"Necessary File "+ Directories.d9Validation +" was missing.\nTemporary file has been created for the required format.\nPlease update the file as per your requirements then restart application.");
			earrachExtractViewComponents.getQueryResult().append("\nPlease wait Cache refreshing...");
			initHrefPairs();
			try{
			Thread.sleep(5000);
			}catch(Exception e){}
			earrachExtractViewComponents.getQueryResult().append("\nSystem Refresh Successfully");
		}
		
		// LOAD hrefOfferStatus FILE
		CSVReader br2 = new CSVReader(new FileReader(Directories.hrefOfferStatus));
		String []cols2 = new String[]{};
		br2.readNext();
		while ((cols2 = br2.readNext()) != null) 
		{
			initHREFOfferStatusCkeck(cols2[0],cols2[1]);
		}
		br2.close();
		
		//LOAD iovOfferStatus FILE
		CSVReader br1 = new CSVReader(new FileReader(Directories.iovOfferStatus));
		String []cols1 = new String[]{};
		br1.readNext();
		while ((cols1 = br1.readNext()) != null) 
		{
			initIOVOfferStatusCkeck(cols1[0],cols1[1],cols1[2]);
		}
		br1.close();
		
		//LOAD d9Validation FILE
		CSVReader br = new CSVReader(new FileReader(Directories.d9Validation));
		String []cols = new String[]{};
		StringBuilder sb = new StringBuilder();
		int row = 0;
		//skip info
		br.readNext();
		// skip header
		br.readNext();
		while ((cols = br.readNext()) != null) 
		{
			row++;
			String hrefCATKey = new String();
			String hrefProdKey = new String();
			String hrefKey = new String();
			
			
			D9ParserMapKey hrefMapKey = null;
			
			ArrayList<String> values = new ArrayList<String>();
			for(int index = 3 ; index<cols.length;index++)
				{
				
				if(null == cols[0] && cols[0].trim().length()==0)
				{
				 	sb.append("\n File Validation Error found on "+row +"Category Missing..");
				}
				if(null == cols[1] && cols[1].trim().length()==0)
				{
				 	sb.append("\n File Validation Error found on "+row +"Product Missing..");
				}
				
				if(null == cols[2] && cols[2].trim().length()==0)
				{
				 	sb.append("\n File Validation Error found on "+row +"HREF Missing..");
				}
			
				    hrefCATKey = cols[0];
					hrefProdKey = cols[1];
					hrefKey = cols[2];
			
					hrefMapKey = new D9ParserMapKey(hrefProdKey,hrefKey);
						
						
						if((cols[index].trim().length()>1 && cols[index].trim().indexOf("!")!=0 )|| (cols[index].trim().length()>2 && cols[index].trim().indexOf("!")==0))
						{
							sb.append("\n File Validation Error found on row "+row +" >> Having morethen one char.");
						}
						else
						{
							if(!hrefProdKey.contains("ONLY_VALID_FOR"))
							{
								if(null != cols[index].trim() && cols[index].trim().length()>0)
								{
									values.add(cols[index].trim());
								}
							}
							else if(hrefProdKey.contains("ONLY_VALID_FOR"))
							{
								
								if(onlyValidForMap.containsKey(hrefProdKey))
								{
									ArrayList<String> onlyvalidforvalues = new ArrayList<String>();
									for(int in = 2 ; in<cols.length;in++)
									{
										if(null != cols[in].trim() && cols[in].trim().length()>0)
										{
											onlyvalidforvalues.add(cols[in].trim());
										}
									}
									ArrayList<String> existingValues = onlyValidForMap.get(hrefProdKey);
									values.add(hrefKey);
									existingValues.addAll(onlyvalidforvalues);
									onlyValidForMap.put(hrefProdKey, existingValues);	
							
								}
								else
								{
									ArrayList<String> onlyvalidforvalues = new ArrayList<String>();
									for(int in = 2 ; in<cols.length;in++)
									{
										if(null != cols[in].trim() && cols[in].trim().length()>0)
										{
											onlyvalidforvalues.add(cols[in].trim());
										}
									}
									onlyValidForMap.put(hrefProdKey, onlyvalidforvalues);
								}
							}
						}
				}
				if(hrefValidationFileMap.containsKey(hrefMapKey))
						{
						 	ArrayList<String> existingValues = hrefValidationFileMap.get(hrefMapKey);
							existingValues.addAll(values);
							hrefValidationFileMap.put(hrefMapKey, existingValues);
							String CAT = hrefCATMap.get(hrefMapKey);
							if(!CAT.equalsIgnoreCase(hrefCATKey))
							{
								sb.append("\n File Validation Error found on ("+row +"), Same HREF Key having different CAT Id.("+CAT+" and "+hrefMapKey.getHref()+")");
							}
						}
						else
						{
							hrefValidationFileMap.put(hrefMapKey, values);
							hrefCATMap.put(hrefMapKey, hrefCATKey);
						}
						values =  null;
				
			
		}
		
		if(sb.length()>0)
		{
		JOptionPane.showMessageDialog(null,"File " +Directories.d9Validation+ "Primary validation failed.\n"+sb.toString())	;
		earrachExtractViewComponents.getQueryResult().append("\nFile " +Directories.d9Validation+ "Primary validation failed.\n"+sb.toString());
		//hrefValidationFileMap = null;
		br.close();
		return;
		}
		br.close();
	
		earrachExtractViewComponents.getQueryResult().append("\n-----------------------------------------------------------------------------------------------------");
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		earrachExtractViewComponents.getQueryResult().append("\nFile "+ Directories.hrefOfferStatus +" modified on ("+sdf.format(FILE.lastModified())+"), loads successfully in cache.");
		earrachExtractViewComponents.getQueryResult().append("\nFile "+ Directories.iovOfferStatus +" modified on ("+sdf.format(FILE.lastModified())+"), loads successfully in cache.");
		earrachExtractViewComponents.getQueryResult().append("\nFile "+ Directories.d9Validation +" modified on ("+sdf.format(FILE.lastModified())+"), loads successfully in cache.");
		earrachExtractViewComponents.getQueryResult().append("\nIf required , update the file accordingly then restart the application.");
		earrachExtractViewComponents.getQueryResult().append("\n-----------------------------------------------------------------------------------------------------");
	
	   
	}
	
	private Map<String,ArrayList<String[]>> initIOVOfferStatusCkeck(String iovorderType,String orderType,String orderStatus){
		
		String arr[] = {iovorderType,orderType,orderStatus};
		if(!iovOfferStatus.containsKey(iovorderType))
		{
			ArrayList<String[]> list = new ArrayList<String[]>();
			list.add(arr);
			iovOfferStatus.put(iovorderType, list);
		}
		else
		{
			ArrayList<String[]> list  = iovOfferStatus.get(iovorderType);
			list.add(arr);
			iovOfferStatus.put(iovorderType, list);
		}
		
		
		return iovOfferStatus;
	}
	private ArrayList<String[]> offerHrefMapingValidation(ArrayList<String[]> filterList){
		
		earrachExtractViewComponents.getQueryResult().append("\nHREF VALIDATION BEING PROCESS AT  OFFER LEVEL.PLEASE WAIT.");
	
		for(String[] row: filterList)
		{
			String coreProduct = row[2];
			String corehref = row[3];
			
			if(null != corehref && corehref.length()>0)
				{
				char[] corehrefcharArray = corehref.toCharArray();
				ArrayList<String> selfCombinationCheck = new ArrayList<String>();
			
			
				for(char C : corehrefcharArray)
					{
					
					for(int X = 0 ; X <corehrefcharArray.length; X++)
					  {
						if(corehrefcharArray[X]!=C)
						  {
							  String addselfcombichars = corehrefcharArray[X]+",";
							  if(null != addselfcombichars && addselfcombichars.length()>0)
							  {
								  if(!selfCombinationCheck.contains(addselfcombichars))
								  {
								  selfCombinationCheck.add(addselfcombichars);
								  }
							  }
						  }
					  }
					
					  String S = String.valueOf(C).trim();
					  
					  D9ParserMapKey hrefMapKey = new D9ParserMapKey(coreProduct,S);
					  if(hrefValidationFileMap.containsKey(hrefMapKey))
					  {
						// VALIDATE CORE HREF MAPPING  
					    ArrayList<String> values = hrefValidationFileMap.get(hrefMapKey);
					    
					    String otherhref = row[7];
					    
					    // add self combination to validate LIKE A5.
					    if (null != selfCombinationCheck)
					    {
						    for(String Self : selfCombinationCheck)
						    {
						    	otherhref+=Self;
						    }
					    }
					    boolean isHrefFailed= false;	
					   
					    if(null!= otherhref && otherhref.length()>0)
						{
					    	for(String retVAL: otherhref.trim().split(",")) 
							{
								char[] charArrayA = retVAL.toCharArray();
								char[] charArrayB = corehrefcharArray;
								
								char[] charArray = new char[charArrayA.length+charArrayB.length];
								
								 for(int x = 0; x< charArrayA.length;x++)
								 {
									 charArray[x] = charArrayA[x];
								 }
								 for(int x = 0; x< charArrayB.length;x++)
								 {
									 charArray[charArrayA.length+x] = charArrayB[x];
								 }
								 for(char CR : charArray)
								 {
									 String retval = String.valueOf(CR);
									 if(!retval.equals(S))
									 {
										 if(values.contains("!"+retval))
										 {
											 String msg = "#"+retval+" IS NOT ALLOWED WITH "+S+" AT OFFER LEVEL.";
											
											 row[17]= row[17]+msg;
									    	 row[17]=addInRow(row[17]);
											
											 isHrefFailed = true;
										 }
										 else if(!values.contains("*"))
										 {
											 if(!values.contains(retval))
											 {
												 String msg = "#"+retval+" IS NOT ALLOWED WITH "+S+" AT OFFER LEVEL.";
												 row[17]= row[17]+msg;
										    	 row[17]=addInRow(row[17]);
												
												 isHrefFailed = true;
											 }
										 }
									 }
									 String failureNote = onlyValidForCheck("ONLY_VALID_FOR_OFFER",retval);	  
									 if(null != failureNote)
									 {
										 row[17]= row[17]+failureNote;
								    	 row[17]=addInRow(row[17]);
										
										 isHrefFailed = true;
									 }
								 }
						}
					  }
				
				   if(isHrefFailed)
					    {
					    // HREF Validation Failed	
					    	 row[16] = "FAILED";
					   }
					   else
					    {
						   row[16] = "PASS";
						   if(null != row[17] && row[17].length()>0)
						   {
							   row[16] = "FAILED";
						   }
					    }
				         
				         row[18] = row[18]+"," +hrefCATMap.get(hrefMapKey);
				         row[18] = addInRow(row[18]);
					
				    } 
					  else
					  {
						  row[16] = "UNDEFINED_HREF";
						  row[18] = row[18]+"," +"UNDEFINED_DB_VALIDATION";
						  row[18] = addInRow(row[18]);
					  }
				//END VALIDATIONS OF CORE HREF MAPPING	AT OFFER LEVEL  
			}
		}
	  }
		
		ArrayList<String[]> filterListWithHrefPairsComments = new ArrayList<String[]>(filterList);
		return filterListWithHrefPairsComments;
		
	}

private ArrayList<String[]>  offerHrefMapingValidation_ON_COMPONENT(ArrayList<String[]> filterList,String productType,int checkColumnIndex){
	earrachExtractViewComponents.getQueryResult().append("\nHREF VALIDATION BEING PROCESS AT  "+productType+ " LEVEL.PLEASE WAIT.");
	for(String[] row: filterList)
	{
	  String otherMLIhref = row[checkColumnIndex];
	 if(null!= otherMLIhref && otherMLIhref.length()>0)
	 {
		 row[16] = "FAILED_HREF";
    	 String MSG ="# HREF NOT ALLOWED AT COMPONENT LEVEL.";
    	 row[17]= row[17]+MSG;
    	 row[17]=addInRow(row[17]);
	 }
	}

	ArrayList<String[]> filterListWithHrefPairsComments = new ArrayList<String[]>(filterList);
	return filterListWithHrefPairsComments;
}	

	private ArrayList<String[]> offerHrefMapingValidation_ON_MLI_ELEMENT_COMPANION(ArrayList<String[]> filterList,String productType,int checkColumnIndex){
		earrachExtractViewComponents.getQueryResult().append("\nHREF VALIDATION BEING PROCESS AT  "+productType+ " LEVEL.PLEASE WAIT.");
		for(String[] row: filterList)
		{
			ArrayList<String> failureNotes = new ArrayList<String>();	
			String otherMLIhref = row[checkColumnIndex];
			boolean isHrefFailed= false;	
			   if(null!= otherMLIhref && otherMLIhref.length()>0)
						{
					    	for(String retVAL: otherMLIhref.trim().split(",")) 
							{
								char[] charArray = retVAL.toCharArray();
								 for(char CR : charArray)
								 {
									  String retval = String.valueOf(CR);
									  D9ParserMapKey hrefMapKey = new D9ParserMapKey(productType.toUpperCase(),retval);
								
									  if(hrefValidationFileMap.containsKey(hrefMapKey))
									  {
										  ArrayList<String> values = hrefValidationFileMap.get(hrefMapKey);
										  if(values.contains("!"+retval))
										  {
											 String msg = "#"+retval+" IS NOT ALLOWED WITH "+CR+" AT "+productType+ " LEVEL.";
											 failureNotes.add(msg);
											 isHrefFailed = true;
										 }
										 else if(values.contains("*"))
											 {
											  // ALL PASS
											 }
										  else if(!values.contains("*"))
										 {
											  /*
											   * 
											   *  ADD SELF TO PASS SELF CHECKING CASE
											   */
											  
											 values.add(retval);
											  
											  
											 if(!values.contains(retval))
											 {
												 String msg = "#"+retval+" IS NOT ALLOWED WITH "+CR+" AT "+productType+ " LEVEL.";
												 failureNotes.add(msg);
												 isHrefFailed = true;
											 }
											 for(char others : charArray)
											 {
											   String otherchars = String.valueOf(others); 
										
											   if(!otherchars.equalsIgnoreCase(retval))
											   {
												   
												   if(values.contains("!"+otherchars))
													  {
													    String msg = "#"+otherchars+" IS NOT ALLOWED WITH "+retval+" AT "+productType+ " LEVEL.";
														failureNotes.add(msg);
														isHrefFailed = true; 
													  }
												   else if(!values.contains(otherchars))
													 {
														 String msg = "#"+otherchars+" IS NOT ALLOWED WITH "+retval+" AT "+productType+ " LEVEL.";
														 failureNotes.add(msg);
														 isHrefFailed = true;
													 }
											   }
											 }
										 }
									  }//end of map key exists 
									  // If no key found
									  else
									  {
										  String msg = "#"+retval+" IS NOT DEFINED WITH "+productType+ " LEVEL.";
										  failureNotes.add(msg);
										  isHrefFailed = true;
									  }
									      String key ="ONLY_VALID_FOR_"+productType.toUpperCase();	
									      
								
									      String failureNote = onlyValidForCheck(key,retval);	  
									      
										 if(null != failureNote)
										 {
											 failureNotes.add(failureNote); 
											 isHrefFailed = true;
										 }		  
								 }// end of char
						} // end of each comma separated value
					  } // end of , if href not null
				   if(isHrefFailed)
					    {
					    	    row[16] = "FAILED";
					    	
					    	    Set<String> hs = new HashSet<String>();
						    	hs.addAll(failureNotes);
						    	failureNotes.clear();
						    	failureNotes.addAll(hs);
						    	
					    	 for(String msg: failureNotes)
					    	 {
					    		 row[17]= row[17]+msg;
					    		 row[17]=addInRow(row[17]);
					    	 }
					    	
					   }
					   else
					    {
						   // NOT CAT ON THESE LEVEL
					    	row[16] = row[16];
					    }
				} // end of row
		//END VALIDATIONS OF CORE HREF MAPPING	AT MLI LEVEL  
	
		ArrayList<String[]> filterListWithHrefPairsComments = new ArrayList<String[]>(filterList);
		return filterListWithHrefPairsComments;
	}
	private String onlyValidForCheck(String check,String value)
	{
		Iterator<Map.Entry<String, ArrayList<String>>> entries1 = onlyValidForMap.entrySet().iterator();
		while (entries1.hasNext()) 
		{
			Map.Entry<String, ArrayList<String>> entry = entries1.next();
			String key = entry.getKey();
			
			if(!key.equalsIgnoreCase(check))
			{
				ArrayList<String> values = entry.getValue();
				if(values.contains(value))
				{
				return "#"+value +" IS "+ key +" , IT IS NOT ALLOWED AS "+check.substring(check.lastIndexOf("_"));	
				}
			}
		}
	return null;
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
		int arr[] ={2,7,8,9,10,11};
		table.hideColumns(arr);
		
		
	}
	
	private ArrayList<String[]> tuneWithConsolidatedAnalysis(ArrayList<String[]> filterListWithValidation){
		for(String[] row : filterListWithValidation)
		{

			String hrefAnalysis=row[16];
			String dbAnalysis=row[18];
			String iovOfferStatusAnalysis=row[20];
			String hrefOfferStatusAnalysis=row[22];
			String mainHrefStatusAnalysis=row[24];
			String mileStoneAnalysis=row[26];
			
			
			if(hrefAnalysis.equalsIgnoreCase("PASS") 
					&& dbAnalysis.equalsIgnoreCase("PASS") 
					&& (iovOfferStatusAnalysis.equalsIgnoreCase("PASS") || iovOfferStatusAnalysis.equalsIgnoreCase("NA")) 
					&& hrefOfferStatusAnalysis.equalsIgnoreCase("PASS") 
					&& mainHrefStatusAnalysis.equalsIgnoreCase("PASS") 
					&& (mileStoneAnalysis.equalsIgnoreCase("PASS") || mileStoneAnalysis.equalsIgnoreCase("NA")))
			{
				row[28] = "PASS";
			}
			else 
			{
				row[28] = "FAILED";
			}
			
			
		 }
		
	return filterListWithValidation;
	}
	
	
	private ArrayList<String[]> validateHREFOfferStatus(ArrayList<String[]> filterListWithValidation,Map<String,ArrayList<String>> hrefOfferStatusList)
	{
		earrachExtractViewComponents.getQueryResult().append("\nHREF and Offer Status Validation in Progress...");
		/**
		 GET THE CORE HREF ROW[3]
		 CHANGE INTO CHARS ARRAY
		 CHECK EVERY CHAR KEY IN MAP IF EXISTS
		 ADD MAP VALUES IN A LIST
		
		 CHECK EACH HREF WITH THIS LIST
		 COLUMN VALUE MUST BE IN LIST
		
		**/
		for(String[] row : filterListWithValidation)
		{
			ArrayList<String> validOrderStatus = null;
			char charARR[] = row[3].toCharArray();
			for(char C : charARR)
			{
				String S = String.valueOf(C);
				if(hrefOfferStatusList.containsKey(S))
				{
					ArrayList<String> values =hrefOfferStatusList.get(S);
						if(null == validOrderStatus)
						{
							validOrderStatus = new ArrayList<String>(values);
						}
					validOrderStatus.retainAll(values);
				}
				
			}
			
			String orderStatus = row[14];
			if(validOrderStatus.contains(orderStatus))
			{
				// FOR HREF 8 or 9 , IF STATUS IS CHANGE , THEN IT MUST BE SERVICE CHANGE OR PRICE UPDATE
				if((row[3].contains("8") || row[3].contains("9")) && orderStatus.equalsIgnoreCase("CHANGE"))
				{
					
					if(null != row[15] && row[15].length()>0 && (row[15].equalsIgnoreCase("Service Change") || row[15].equalsIgnoreCase("Price Update")))
					{
						row[22] ="PASS";
					}
					else
					{
						row[22] ="FAILED";
						row[23]= row[23]+"#For HREF 8 OR 9 CHANGE TYPE MUST BE 'PRICE UPDATE' OR 'SERVICE CHANGE'.";
			    		row[23]=addInRow(row[23]);
					}
				}
				else
				{
					row[22] ="PASS";
				}
				
			}
			else
			{
				row[22] ="FAILED";
				row[23]= row[23]+"#For HREF '"+row[3]+"' OFFER TYPE '"+orderStatus+"' IS NOT VALID.";
	    		row[23]=addInRow(row[23]);
			}
		
		}
		earrachExtractViewComponents.getQueryResult().append("\nHREF and Offer Status Validation Completed...");
		return filterListWithValidation;
	}
	
	private ArrayList<String[]> validateIOVOfferStatus(ArrayList<String[]> filterListWithValidation,Map<String,ArrayList<String[]>> iovOfferStatusList)
	{
		earrachExtractViewComponents.getQueryResult().append("\nIOV and Offer Status Validation in Progress...");
		for(String[] row : filterListWithValidation)
		{
			
			String iovOfferStatus = row[6];
			String offerStatus = row[13];
			String offerType = (row[14].equalsIgnoreCase("Change") ? row[15] : row[14]);
			String CONC = iovOfferStatus+"||"+offerStatus+"||"+offerType;
		
			if(iovOfferStatusList.containsKey(iovOfferStatus))
			{
				ArrayList<String[]> arr = iovOfferStatusList.get(iovOfferStatus);
				boolean isMatch = false;
				for(String[] ARR : arr)
				{
					String IOS = ARR[0]+"||"+ARR[1]+"||"+ARR[2];
				
					if((CONC==IOS) || (CONC.equals(IOS)))
					{
						isMatch = true;
						break;
					}
					
				}
				if(isMatch)
				{
					row[20] ="PASS";
				}
				else{
					row[20] ="FAILED";
					row[21]= row[21]+"#For IOV's OFFER STATUS '"+iovOfferStatus +"' ORDER_STATUS AND ORDER_TYPE DOES NOT MATCH.";
		    		row[21]=addInRow(row[21]);
				
				}
				
			}
			else
			{
				row[20] ="NA";
			}
		}
		earrachExtractViewComponents.getQueryResult().append("\nIOV and Offer Status Validation Completed...");
		return filterListWithValidation;
	}
	
	private ArrayList<String[]> validateMainHREFStatus(ArrayList<String[]> filterListWithValidation)
	{
		earrachExtractViewComponents.getQueryResult().append("\nMain HREF Validation in Progress...");
		/**
		 GET THE CORE HREF ROW[3] , MUST HAVE EITHER A,5 OR/AND D
		 CHANGE INTO CHARS ARRAY
		 CHECK EVERY CHAR IN VALIDCHECK STRING
		 AT LEAST ONE CHAR MUST BE IN VALIDCHECK STRING
		
		**/
		String validCheck ="A5D";
		for(String[] row : filterListWithValidation)
		{
			char charARR[] = row[3].toCharArray();
			boolean isContains = false;
			for(char C : charARR)
			{
				String S = String.valueOf(C);
				if(validCheck.contains(S))
				{
					isContains = true;
					break;
				}
				
			}
			if(isContains)
			{
				row[24] ="PASS";
				
			}
			else
			{
				row[24] ="FAILED";
				row[25]= row[25]+"#IN HREF '"+row[3]+"' MAIN HREF MISSING.";
	    		row[25]=addInRow(row[25]);
			}
		
		}
		earrachExtractViewComponents.getQueryResult().append("\nMain HREF Validation Completed...");
		return filterListWithValidation;
	}
	
	private ArrayList<String[]> validateMileStoneStatus(ArrayList<String[]> filterListWithValidation) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
		earrachExtractViewComponents.getQueryResult().append("\nMileStone Validation for Non 1 and 2 HREF in Progress...\n");
		/**
		 GET THE CORE HREF ROW[3]
		 CHANGE INTO CHARS ARRAY
		 CHECK EVERY CHAR IN VALIDCHECK STRING
		 IF ANY CHAR EXISTS , THEN CHECK MILESTONE
		 NO 'Setup Stop Billing' AND 'Setup Billing' MILSTONE IN OPEN STATE.
		
		**/
		int count = 0;
		String validCheck ="12";
		
		for(String[] row : filterListWithValidation)
		{
			count++;
			ProgressMonitorPane.getInstance().setProgress(count,filterListWithValidation.size());
			char charARR[] = row[3].toCharArray();
			boolean isContains = false;
			
			for(char C : charARR)
			{
				String S = String.valueOf(C);
				if(validCheck.contains(S))
				{
					isContains = true;
					break;
				}
				
			}
			if(!isContains)
			{
				// CHECK MILESTONE
				
				ArrayList<String[]> mileStoneInfo = CommonUtils.getQueryResult(mileStoneSQL,  earrachExtractViewComponents, CommonUtils.addZeroPrefixinOrder(row[1]));
				if(null == mileStoneInfo || mileStoneInfo.size() ==0)
				{
				row[26] ="PASS";
				}
				else
				{
					row[26] ="FAILED";
					String MS = "";
					for(String[] ms : mileStoneInfo)
					{
						MS+="'"+ms[0]+"',";
					}
					row[27]= row[27]+"#IN HREF '"+row[3]+"' MILESTONE "+MS+" OPEN.";
		    		row[27]=addInRow(row[27]);
				}
			}
			
			
			else
			{
				row[26] ="NA";
			}
			
		
		}
		earrachExtractViewComponents.getQueryResult().append("\nMileStone Validation Completed...");
		return filterListWithValidation;
	}
	
	private ArrayList<String[]> validateMileStoneStatusForOne(ArrayList<String[]> filterListWithValidation) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
		earrachExtractViewComponents.getQueryResult().append("\nMileStone Validation for 1 in Progress...\n");
		/**
		 GET THE CORE HREF ROW[3]
		 CHANGE INTO CHARS ARRAY
		 CHECK EVERY CHAR IN VALIDCHECK STRING
		 IF ANY CHAR EXISTS , THEN CHECK MILESTONE must be 'Setup Billing' MILSTONE IN OPEN STATE.
		
		**/
		int count = 0;
		String validCheck ="1";
	
		for(String[] row : filterListWithValidation)
		{
			count++;
			ProgressMonitorPane.getInstance().setProgress(count,filterListWithValidation.size());
			char charARR[] = row[3].toCharArray();
			boolean isContains1 = false;
		
			
			for(char C : charARR)
			{
				String S = String.valueOf(C);
				if(validCheck.contains(S))
				{
					isContains1 = true;
					break;
				}
				
			}
			
			// IF CONTAINS 1 THEN MILESTONE MUST 'Setup Billing'  IN OPEN STATE
			if(isContains1)
			{
				// CHECK MILESTONE
				
				ArrayList<String[]> mileStoneInfo = CommonUtils.getQueryResult(mileStoneSQLForOne,  earrachExtractViewComponents, CommonUtils.addZeroPrefixinOrder(row[1]));
				if(null != mileStoneInfo && mileStoneInfo.size() >0)
				{
				row[26] ="PASS";
				}
				else
				{
					row[26] ="FAILED";
					row[27]= row[27]+"#IN HREF '"+row[3]+"' FOR HREF '1' MILESTONE 'SETUP BILLING' NOT OPEN.";
		    		row[27]=addInRow(row[27]);
				}
			}
		}
		earrachExtractViewComponents.getQueryResult().append("\nMileStone Validation Completed...");
		return filterListWithValidation;
	}
	private ArrayList<String[]> validateMileStoneStatusForTwo(ArrayList<String[]> filterListWithValidation) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
		earrachExtractViewComponents.getQueryResult().append("\nMileStone Validation for 2 in Progress...");
		/**
		 GET THE CORE HREF ROW[3]
		 CHANGE INTO CHARS ARRAY
		 CHECK EVERY CHAR IN VALIDCHECK STRING
		 IF ANY CHAR EXISTS , THEN CHECK MILESTONE must be 'Setup Stop Billing' MILSTONE IN OPEN STATE.
		
		**/
		int count = 0;
		String validCheck ="2";
	
		for(String[] row : filterListWithValidation)
		{
			count++;
			ProgressMonitorPane.getInstance().setProgress(count,filterListWithValidation.size());
			char charARR[] = row[3].toCharArray();
			boolean isContains1 = false;
		
			
			for(char C : charARR)
			{
				String S = String.valueOf(C);
				if(validCheck.contains(S))
				{
					isContains1 = true;
					break;
				}
				
			}
			
			// IF CONTAINS 2 THEN MILESTONE MUST 'Setup Stop Billing'  IN OPEN STATE
			if(isContains1)
			{
				// CHECK MILESTONE
				
				ArrayList<String[]> mileStoneInfo = CommonUtils.getQueryResult(mileStoneSQLForTwo,  earrachExtractViewComponents, CommonUtils.addZeroPrefixinOrder(row[1]));
				if(null != mileStoneInfo && mileStoneInfo.size() >0)
				{
				row[26] ="PASS";
				}
				else
				{
					row[26] ="FAILED";
					row[27]= row[27]+"#IN HREF '"+row[3]+"' FOR HREF '2' MILESTONE 'SETUP STOP BILLING' NOT OPEN.";
		    		row[27]=addInRow(row[27]);
				}
			}
		}
		earrachExtractViewComponents.getQueryResult().append("\nMileStone Validation Completed...");
		return filterListWithValidation;
	}
}
