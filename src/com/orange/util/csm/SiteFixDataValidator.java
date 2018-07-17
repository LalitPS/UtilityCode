package com.orange.util.csm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import com.orange.ui.component.SiteFixedViewComponents;
import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.Icons;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.ConnectionBeanCSI;
import com.orange.util.CurrentDateTime;
import com.orange.util.CustomCSVWriter;
import com.orange.util.ProgressMonitorPane;

public class SiteFixDataValidator {

	private ArrayList<String> csiExecutionScript;
	
	private ArrayList<String> errors;
	private ArrayList<String> goldExecutionScript;
	private SiteFixFileValidator siteFixFileValidator;
	private SiteFixedViewComponents siteFixViewComponents;


	private Map<ArrayList<String>, ArrayList<ArrayList<String>>> tableData;

	public SiteFixDataValidator(SiteFixFileValidator siteFixFileValidator,SiteFixedViewComponents siteFixViewComponents) {
		this.siteFixFileValidator = siteFixFileValidator;
		this.siteFixViewComponents = siteFixViewComponents;
		errors = new ArrayList<String>();
		goldExecutionScript = new ArrayList<String>();
		csiExecutionScript = new ArrayList<String>();
		
		tableData = new HashMap<ArrayList<String>, ArrayList<ArrayList<String>>>();
	}
	
	private void addMapData(ArrayList<String> key, ArrayList<String> data) {
		
		ArrayList<String> localkey = key;
		ArrayList<String> localData = data;
		
		if (!tableData.containsKey(localkey)) {
			ArrayList<ArrayList<String>> initData = new ArrayList<ArrayList<String>>();
			initData.add(localData);
			tableData.put(localkey, initData);
		
		} else 
		{
			ArrayList<ArrayList<String>> existingData = tableData.get(key);
			existingData.add(localData);
			tableData.put(localkey, existingData);
		}
		
		localkey = null;
		localData = null;
		key = null; 
		data = null;
	}

	public void createCSIScriptFile(String filePath) throws Exception{
		File fout = new File(filePath);
		FileOutputStream fos = new FileOutputStream(fout);
		
		csiExecutionScript = CommonUtils.setUmlaut(csiExecutionScript);
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		for(String script : csiExecutionScript){
			bw.write(script);
			bw.newLine();
		}
		bw.close();
		fos.close();
	}


	public void createGOLDScriptFile(String filePath) throws Exception{
		File fout = new File(filePath);
		FileOutputStream fos = new FileOutputStream(fout);
		goldExecutionScript = CommonUtils.setUmlaut(goldExecutionScript);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		for(String script : goldExecutionScript){
			bw.write(script);
			bw.newLine();
		}
		bw.close();
	}

	public ArrayList<String> getCsiExecutionScript() {
		return csiExecutionScript;
	}

	


	
	
	
	
	
	public ArrayList<String> getErrors() {
		return errors;
	}

	public ArrayList<String> getGoldExecutionScript() {
		return goldExecutionScript;
	}
	
	public void prepareQuery() throws Exception {
		ArrayList<String[]> data = siteFixFileValidator.getFileData();
		/*
		ArrayList<String[]> data = new ArrayList<String[]>();
		String ARR[] = new String[7];
		ARR[0] = "8304 Basel	";
		ARR[1] = "0";
		ARR[2] = "Tsim Sha Tsui_035029";
		ARR[3] = "0";
		ARR[4] = "";
		ARR[5] = "";
		ARR[6] = "ALL";
		data.add(ARR);
	
		ARR = new String[7];
		ARR[0] = "VISAEAKW0002";
		ARR[1] = "3";
		ARR[2] = "089";
		ARR[3] = "0";
		ARR[4] = "";
		ARR[5] = "";
		ARR[6] = "ALL";
		data.add(ARR);
		
		ARR = new String[7];
		ARR[0] = "Kuwait City_181305";
		ARR[1] = "3";
		ARR[2] = "089";
		ARR[3] = "0";
		ARR[4] = "";
		ARR[5] = "";
		ARR[6] = "ALL";
		data.add(ARR);
		
		
		ARR = new String[7];
		ARR[0] = "09145";
		ARR[1] = "3";
		ARR[2] = "Singapore_056702";
		ARR[3] = "0";
		ARR[4] = "";
		ARR[5] = "";
		ARR[6] = "ALL";
		data.add(ARR);
		
		ARR = new String[7];
		ARR[0] = "-_078765";
		ARR[1] = "3";
		ARR[2] = "-_079539";
		ARR[3] = "0";
		ARR[4] = "";
		ARR[5] = "";
		ARR[6] = "ALL";
		data.add(ARR);
		
		*/
		double count =0.0;
		int totalCount = data.size();

		for (String[] row : data) {

			ProgressMonitorPane.getInstance().setProgress(count,(double)totalCount);
			String replaceSitecode = row[0];
			String replaceStatus = row[1];
			String replacementSitecode = row[2];
			String replacementStatus = row[3];
			String replaceOSR = row[4];
			String replacementOSR = row[5];
			String orders = row[6];
			
			
			ArrayList<String[]> quotes = new ArrayList<String[]>();
			ArrayList<String[]> hotcutquotes = new ArrayList<String[]>();
			String coresiteid = "";
			String addressid = "";
			goldExecutionScript.add("--------------------------------------------------------------------------------------------------------" );
			goldExecutionScript.add("-- INFO : GOLD Script Starts "+CurrentDateTime.getDateTimeText()+ " for Replace Sitecode:"+ replaceSitecode + " And Replacement Sitecode:"+ replacementSitecode+" Record Number ("+(int)(count+1)+" from "+totalCount+")");
			siteFixViewComponents.getQueryResult().append("-- INFO : GOLD Script for Replace Sitecode:"+ replaceSitecode + " And Replacement Sitecode:"+ replacementSitecode+"\n");
			
			/*
			 * These are Errors: No Tril GID More then One TRIL GID
			 */
			
			ArrayList<String[]> replace_SiteDetails = new ArrayList<String[]> ();
			ArrayList<String[]> replacement_SiteDetails = new ArrayList<String[]> ();

			if (!CommonUtils.isNULL(replaceSitecode)) 
			{
				String replaceTrilGID = "SELECT TRIL_GID,CORE_SITE_ID,ADDRESS_ID,STATUS,ORANGE_SITENAME FROM "+ConnectionBean.getDbPrefix()+ "EQ_SITE WHERE SITECODE=?";
				replace_SiteDetails = CommonUtils.getQueryResult(replaceTrilGID,siteFixViewComponents,replaceSitecode);
			}
			if (!CommonUtils.isNULL(replacementSitecode)) 
			{
				String replacementTrilGID = "SELECT TRIL_GID,CORE_SITE_ID,ADDRESS_ID,STATUS,ORANGE_SITENAME FROM "+ConnectionBean.getDbPrefix()+"EQ_SITE WHERE SITECODE=?";
				replacement_SiteDetails = CommonUtils.getQueryResult(replacementTrilGID,siteFixViewComponents,replacementSitecode);
			}

			if (replace_SiteDetails.size() == 0) 
			{
				errors.add("NO TRILGID FOR REPLACE SITECODE " + replaceSitecode);
				goldExecutionScript.add("-- ERROR : NO TRILGID FOR REPLACE SITECODE " + replaceSitecode );
			} else if (replace_SiteDetails.size() > 1) 
			{
				errors.add("MULTIPLE TRILGID FOR REPLACE SITECODE "+ replaceSitecode);
				goldExecutionScript.add("-- ERROR :  MULTIPLE TRILGID FOR REPLACE SITECODE "+ replaceSitecode );
			}
			if (replacement_SiteDetails.size() == 0) 
			{
				errors.add("NO TRILGID FOR REPLACEMENT SITECODE "+ replacementSitecode);
				goldExecutionScript.add("-- ERROR : NO TRILGID FOR REPLACEMENT SITECODE "+ replacementSitecode );
			} else if (replacement_SiteDetails.size() > 1) 
			{
				errors.add("MULTIPLE TRILGID FOR REPLACEMENT SITECODE "+ replacementSitecode);
				goldExecutionScript.add("-- ERROR : MULTIPLE TRILGID FOR REPLACEMENT SITECODE "+ replacementSitecode );
			}

			ArrayList<String> moveOrders = new ArrayList<String>();
			
			if (!CommonUtils.isNULL(orders) && !orders.equalsIgnoreCase("ALL") && !orders.equalsIgnoreCase("NONE") ) 
			{
				
				for(String retval: orders.trim().split(";")) 
				{
				String getOrderSQL = "select quotenumber from "+ConnectionBean.getDbPrefix()+ "sc_quote where quotenumber=?";
				ArrayList<String[]> order = CommonUtils.getQueryResult(getOrderSQL,siteFixViewComponents,CommonUtils.addZeroPrefixinOrder(retval));
				if(order.isEmpty())
				{
					errors.add("NO ORDER FOUND "+ retval);
					goldExecutionScript.add("-- ERROR : NO ORDER FOUND "+ retval );
				}
				else
				{
					moveOrders.add(retval);
				}
				}
			}
			
			if (replace_SiteDetails.size() == 1 && replacement_SiteDetails.size() == 1) 
			{
				
				String quotessql = "select quotenumber from "+ConnectionBean.getDbPrefix()+"sc_quote where site =?";//in (select tril_gid from "+ConnectionBean.getDbPrefix()+"EQ_SITE WHERE SITECODE=?)";
				quotes = CommonUtils.getQueryResult(quotessql, siteFixViewComponents,replace_SiteDetails.get(0)[0]);
				String hotcutquotessql = "select quotenumber,SITE,HOTCUTNEWSITE from "+ ConnectionBean.getDbPrefix()+ "sc_quote where HOTCUTNEWSITE =?";// (select tril_gid from "+ConnectionBean.getDbPrefix()+"EQ_SITE WHERE SITECODE=?)";
				hotcutquotes = CommonUtils.getQueryResult(hotcutquotessql,siteFixViewComponents,replace_SiteDetails.get(0)[0]);
				
				/*
				 * 
				 * Filter List of orders from Database 
				 * Remove orders to process other then MOVE_ORDERS columns 
				 * 
				 * 
				 */
				quotes = CommonUtils.moveQuotes(quotes,moveOrders);
				hotcutquotes = CommonUtils.moveQuotes(hotcutquotes,moveOrders);
				
				
				if (!CommonUtils.isNULL(orders) && orders.equalsIgnoreCase("NONE") ) 
				{
					 quotes = new ArrayList<String[]>();
					 hotcutquotes = new ArrayList<String[]>();
				}
			
				coresiteid = replacement_SiteDetails.get(0)[1];
				addressid = replacement_SiteDetails.get(0)[2];
				
			
				
				
				// for quotes
				for (String quote[] : quotes) 
				{
						String quotationNumber = quote[0];
						
						
						if(!replace_SiteDetails.get(0)[0].equals(replacement_SiteDetails.get(0)[0]))
						{
						String executiondata = "UPDATE "+ ConnectionBean.getDbPrefix()+ "SC_QUOTE SET MODIFICATIONDATE=sysdate, SITE='"+ replacement_SiteDetails.get(0)[0]+ "' WHERE QUOTENUMBER ='"+quotationNumber+"';";
						goldExecutionScript.add(executiondata);
						
						// ADD UPDATE OF EQL2InstanceMapping
						goldExecutionScript = CommonUtils.updateEQL2InstanceMapping(goldExecutionScript,siteFixViewComponents,replaceSitecode,replacementSitecode,quotationNumber);
						
						}
						else
						{
						goldExecutionScript.add("--WARNING : SITE FOUND UPDATED AS REPLACE SITE ("+replace_SiteDetails.get(0)[0]+") AND REPLACEMENT SITE ("+replacement_SiteDetails.get(0)[0]+"). HENCE NO UPDATE REQUIRED.");	
						}
						ArrayList<String> mapKey = new ArrayList<String>();
						mapKey.add(quotationNumber);
						mapKey.add(replaceSitecode);
						mapKey.add(replacementSitecode);
						
						addMapData(mapKey, new ArrayList<String>());

				}
				// for site migrated quotes
				for (String hotcutquote[] : hotcutquotes) 
				{
						String quotationNumber = hotcutquote[0];
						String site = hotcutquote[1];
						String hotcutsite = hotcutquote[2];

						ArrayList<String> mapKey = new ArrayList<String>();
						mapKey.add(quotationNumber);
						mapKey.add(replaceSitecode);
						mapKey.add(replacementSitecode);
						addMapData(mapKey, new ArrayList<String>());

						if ((!CommonUtils.isNULL(site))&& site.equalsIgnoreCase(replace_SiteDetails.get(0)[0])) 
						{
							if(!replace_SiteDetails.get(0)[0].equals(replacement_SiteDetails.get(0)[0]))
							{
								String executiondata = "UPDATE "+ConnectionBean.getDbPrefix()+"SC_QUOTE SET MODIFICATIONDATE=sysdate, SITE='"+replacement_SiteDetails.get(0)[0]+"' WHERE QUOTENUMBER ='"+quotationNumber+"';";
								goldExecutionScript.add(executiondata);
								
								// ADD UPDATE OF EQL2InstanceMapping
								goldExecutionScript = CommonUtils.updateEQL2InstanceMapping(goldExecutionScript,siteFixViewComponents,replaceSitecode,replacementSitecode,quotationNumber);
								
							
							}
							else
							{
								goldExecutionScript.add("--WARNING : SITE FOUND UPDATED AS REPLACE SITE ("+replace_SiteDetails.get(0)[0]+") AND REPLACEMENT SITE ("+replacement_SiteDetails.get(0)[0]+"). HENCE NO UPDATE REQUIRED.");	
							}
					
						} else if ((!CommonUtils.isNULL(hotcutsite))&& hotcutsite.equalsIgnoreCase(replace_SiteDetails.get(0)[0])) 
						{
							if(!replace_SiteDetails.get(0)[0].equals(replacement_SiteDetails.get(0)[0]))
							{
								String executiondata = "UPDATE "+ConnectionBean.getDbPrefix()+"SC_QUOTE SET MODIFICATIONDATE=sysdate, HOTCUTNEWSITE='"+replacement_SiteDetails.get(0)[0]+"' WHERE QUOTENUMBER ='"+quotationNumber+"';";
								goldExecutionScript.add(executiondata);
								
								// ADD UPDATE OF EQL2InstanceMapping
								goldExecutionScript = CommonUtils.updateEQL2InstanceMapping(goldExecutionScript,siteFixViewComponents,replaceSitecode,replacementSitecode,quotationNumber);
							
							
							}
							else
							{
								goldExecutionScript.add("--WARNING : HOTCUTNEWSITE FOUND UPDATED AS REPLACE SITE ("+replace_SiteDetails.get(0)[0]+") AND REPLACEMENT SITE ("+replacement_SiteDetails.get(0)[0]+"). HENCE NO UPDATE REQUIRED.");	
							}
						} else 
						{
							errors.add("NO SITE/HOTCUTNEWSITE MATCHED FOR REPLACE SITECODE "+ replaceSitecode+ " ORDER "+ quotationNumber);
							goldExecutionScript.add("-- ERROR : NO SITE/HOTCUTNEWSITE MATCHED FOR REPLACE SITECODE "+ replaceSitecode+ " ORDER "+ quotationNumber );
						}
					
				}
			
			// Change status of site codes.
			if (!CommonUtils.isNULL(replaceStatus)) 
			{
				
				if(!replace_SiteDetails.get(0)[3].equals(replaceStatus))
				{
				String executiondata = "UPDATE " +ConnectionBean.getDbPrefix()+"EQ_SITE SET STATUS=" + replaceStatus+ ", MODIFICATIONDATE=sysdate , EQ_COMMENT='PART OF SITE FIX ' ||sysdate where TRIL_GID='"+replace_SiteDetails.get(0)[0]+"';";
				goldExecutionScript.add(executiondata);
				}
				else
				{
				goldExecutionScript.add("--INFO : SITE STATUS "+replaceSitecode +" FOUND UPDATED ("+replace_SiteDetails.get(0)[3]+"). HENCE NO UPDATE REQUIRED.");
				}
			}
			if (!CommonUtils.isNULL(replacementStatus)) 
			{
				
				if(!replacement_SiteDetails.get(0)[3].equals(replacementStatus))
				{
					String executiondata = "UPDATE " +ConnectionBean.getDbPrefix()+"EQ_SITE SET STATUS=" + replacementStatus+ ", MODIFICATIONDATE=sysdate , EQ_COMMENT='PART OF SITE FIX ' ||sysdate where TRIL_GID='"+replacement_SiteDetails.get(0)[0]+"';";
					goldExecutionScript.add(executiondata);
				}
				else
				{
					goldExecutionScript.add("--INFO : SITE STATUS "+replacementSitecode +" FOUND UPDATED ("+replacement_SiteDetails.get(0)[3]+"). HENCE NO UPDATE REQUIRED.");
				}
				
			}

			// Change OSR of site codes.

			if (!CommonUtils.isNULL(replaceOSR)) 
			{
			
				if(!replace_SiteDetails.get(0)[4].equals(replaceOSR))
				{
					String executiondata = "UPDATE " + ConnectionBean.getDbPrefix()	+ "EQ_SITE SET ORANGE_SITENAME='" + CommonUtils.refineData(replaceOSR) + "', MODIFICATIONDATE=sysdate where TRIL_GID='"+ replace_SiteDetails.get(0)[0] + "';";
					goldExecutionScript.add(executiondata);
				}
				else
				{
					goldExecutionScript.add("--INFO : SITE ORANGE_SITENAME "+replaceOSR +" FOUND UPDATED for SITE ("+replaceSitecode+").HENCE NO UPDATE REQUIRED.");
				}
				
			}
			if (!CommonUtils.isNULL(replacementOSR)) 
			{
				
				if(!replacement_SiteDetails.get(0)[4].equals(replacementOSR))
				{
				
				String executiondata = "UPDATE " +ConnectionBean.getDbPrefix() + "EQ_SITE SET  ORANGE_SITENAME='" + CommonUtils.refineData(replacementOSR) + "', MODIFICATIONDATE=sysdate where TRIL_GID='"+ replacement_SiteDetails.get(0)[0] + "';";
				goldExecutionScript.add(executiondata);
				}
				else
				{
					goldExecutionScript.add("--INFO : SITE ORANGE_SITENAME "+replacementOSR +" FOUND UPDATED for SITE ("+replacementSitecode+").HENCE NO UPDATE REQUIRED.");
				}
			}

			// Here CSI Query for Row <n> data

			csiExecutionScript.add("-----------------------------------------------------------------------------");
			csiExecutionScript.add("-- INFO : CSI Script Starts "+CurrentDateTime.getDateTimeText() +" for Replace Sitecode:"+ replaceSitecode + " and Replacement Sitecode:"+ replacementSitecode+" Record Number ("+(int)(count+1)+" from "+totalCount+")");
			siteFixViewComponents.getQueryResult().append("-- INFO : CSI Script for Replace Sitecode:"+ replaceSitecode + " And Replacement Sitecode:"+ replacementSitecode+"\n");
		
			if(quotes.size()==0 && hotcutquotes.size() ==0)
			{
				csiExecutionScript.add("-- INFO : NO ORDER EXISTS.. ");
			}
			
			String usdiddatasql = "select Serviceelementid,USID from "+ConnectionBeanCSI.getDbPrefix()+"cserviceelement where serviceelementid in (select serviceelementid from "+ConnectionBeanCSI.getDbPrefix()+"CVERSIONSERVICEELEMENT  where versionid in (select versionid from "+ConnectionBeanCSI.getDbPrefix()+"cversion where ordhandle=?))";

			/*
			quotes = new ArrayList<String[]>();
			for(int X = 0 ; X< moveOrders.size();X++){
				String arr[] = new String[1];
				arr[0] = moveOrders.get(X);
				quotes.add(arr);
			}
			*/
			
			for (String quote[] : quotes) 
			{
				
					String quoteNumber = quote[0];
					ArrayList<String[]> usiddata = CommonUtils.getCSIQueryResult(usdiddatasql,siteFixViewComponents,quoteNumber);
					if(usiddata.size()==0)
					{
						csiExecutionScript.add("-- INFO : NO USID EXISTS FOR ORDER "+quoteNumber);
					}
					for (String arr[] : usiddata) 
					{
						String serviceElementID = arr[0];
						String usid = arr[1];
						
						boolean isAlreadyUpdatedUSID = CommonUtils.isAlreadyUpDateUSID(usid,replacementSitecode);
						String autoUpdatedUSID = CommonUtils.autoUpDateUSID(usid,replacementSitecode);
						
						String isAutoUpdated = "True";
						if (autoUpdatedUSID.equalsIgnoreCase(usid)) 
						{
							isAutoUpdated = "False";
						}
						if (isAlreadyUpdatedUSID) 
						{
							isAutoUpdated = "Existing Updated";
						}
						
						if(isAutoUpdated.equalsIgnoreCase("True"))
						{
							String executionData = "Update "+ConnectionBeanCSI.getDbPrefix()+"Cserviceelement Set Usid='"+ autoUpdatedUSID+ "',Lupddate=Sysdate Where Serviceelementid='"+ serviceElementID + "';";
							csiExecutionScript.add(executionData);
						}
						else{
							csiExecutionScript.add("--INFO :  USID "+autoUpdatedUSID +" FOUND "+isAutoUpdated +". HENCE NO UPDATE REQUIRED.");
						}

						ArrayList<String> mapKey = new ArrayList<String>();
						mapKey.add(quoteNumber);
						mapKey.add(replaceSitecode);
						mapKey.add(replacementSitecode);

						ArrayList<String> serviceElements = new ArrayList<String>();
						serviceElements.add(serviceElementID);
						serviceElements.add(usid);
						serviceElements.add(autoUpdatedUSID);
						serviceElements.add("" + isAutoUpdated);

						addMapData(mapKey, serviceElements);
					}
				
			}
			for (String quote[] : hotcutquotes) 
			{
					String quoteNumber = quote[0];
					ArrayList<String[]> usiddata = CommonUtils.getCSIQueryResult(usdiddatasql,siteFixViewComponents,quoteNumber);
					if(usiddata.size()==0)
					{
						csiExecutionScript.add("-- INFO : NO USID EXISTS FOR ORDER "+quoteNumber);
					}
					for (String arr[] : usiddata) {
						String serviceElementID = arr[0];
						String usid = arr[1];
						boolean isAlreadyUpdatedUSID = CommonUtils.isAlreadyUpDateUSID(usid,replacementSitecode);
						String autoUpdatedUSID = CommonUtils.autoUpDateUSID(usid,replacementSitecode);
						String isAutoUpdated = "True";
						if (autoUpdatedUSID.equalsIgnoreCase(usid)) {
							isAutoUpdated = "False";
						}
						if (isAlreadyUpdatedUSID) 
						{
							isAutoUpdated = "Existing Updated";
						}
						if(isAutoUpdated.equalsIgnoreCase("True")){
							String executionData = "Update "+ConnectionBeanCSI.getDbPrefix()+"Cserviceelement Set Usid='"+autoUpdatedUSID+"',Lupddate=Sysdate Where Serviceelementid='"+serviceElementID+"';";
							csiExecutionScript.add(executionData);
						}
						else{
							csiExecutionScript.add("-- INFO : USID "+autoUpdatedUSID +" FOUND "+isAutoUpdated +". HENCE NO UPDATE REQUIRED.");
						}
						
						ArrayList<String> mapKey = new ArrayList<String>();
						mapKey.add(quoteNumber);
						mapKey.add(replaceSitecode);
						mapKey.add(replacementSitecode);

						ArrayList<String> serviceElements = new ArrayList<String>();
						serviceElements.add(serviceElementID);
						serviceElements.add(usid);
						serviceElements.add(autoUpdatedUSID);
						serviceElements.add(""+isAutoUpdated);
						addMapData(mapKey, serviceElements);

					}
				
			}
			
			/*
			 * Check the 
			 * 1. Is Order Exists in CSI 
			 * 2. Core Ids linked with Order 
			 * 
			 */
			
			
			
			for (String quote[] : quotes) 
			{
				String quoteNumber = quote[0];
				String executionData = CommonUtils.addCSIScriptRow(quoteNumber, addressid, coresiteid, replacementSitecode, siteFixViewComponents);
				csiExecutionScript.add(executionData);
			
			}
			for (String quote[] : hotcutquotes) 
			{
				String quoteNumber = quote[0];
				String executionData = CommonUtils.addCSIScriptRow(quoteNumber, addressid, coresiteid, replacementSitecode, siteFixViewComponents);
				csiExecutionScript.add(executionData);
			}
			}
			count++;
			ProgressMonitorPane.getInstance().setProgress(count,(double)totalCount);
			
		}
		goldExecutionScript.add("commit;");
		csiExecutionScript.add("commit;");
		
	}
	
	public void showTable(String resultFileLoc) throws IOException{
		CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(resultFileLoc),true);
		String[] columnNames ={"QUOTE_NUMBER","Replace SiteCode","Replacement SiteCode","SERVICE ELEMENT ID","PREV USID","UPDATED USID","IS AUTO UPDATED"};
		writer.writeNext(columnNames);
		Iterator<Map.Entry<ArrayList<String>, ArrayList<ArrayList<String>>>> entries1 = tableData.entrySet().iterator();
		DefaultTableModel model = new DefaultTableModel(0, 0);
		model.setColumnIdentifiers(columnNames);
		while (entries1.hasNext())
		{
			Map.Entry<ArrayList<String>, ArrayList<ArrayList<String>>> entry = entries1.next();
			ArrayList<String> key = entry.getKey();
			ArrayList<ArrayList<String>> keyRows = entry.getValue();
			for(ArrayList<String> row : keyRows)
			{
				String arr[] = new String[(key.size()+row.size())];
				for(int x = 0 ;x< key.size() ; x++)
				{
					arr[x] = key.get(x);
				}
				for(int x = 0;x<row.size(); x++)
				{
					arr[x+3] = row.get(x);
				}
				writer.writeNext(arr);
				model.addRow(arr);
				
			}
		}
		writer.close();
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
	
	
}