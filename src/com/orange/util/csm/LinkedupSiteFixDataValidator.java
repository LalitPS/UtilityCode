package com.orange.util.csm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
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

public class LinkedupSiteFixDataValidator {

	private ArrayList<String> csiExecutionScript;
	
	private ArrayList<String> errors;
	private ArrayList<String> goldExecutionScript;
	private LinkedupSiteFixFileValidator linkedupSiteFixFileValidator;
	private SiteFixedViewComponents siteFixViewComponents;
	
	private Map<ArrayList<String>, ArrayList<ArrayList<String>>> tableData;

	public LinkedupSiteFixDataValidator(LinkedupSiteFixFileValidator linkedupSiteFixFileValidator,SiteFixedViewComponents siteFixViewComponents) {
		this.linkedupSiteFixFileValidator = linkedupSiteFixFileValidator;
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
	private String createLegacy(String sitecode) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
		String query = "select orange_sitename from "+ConnectionBean.getDbPrefix()+"eq_site where sitecode=?";
		
		ArrayList<String[]> result = CommonUtils.getQueryResult(query,siteFixViewComponents,sitecode);
	
		if(result.size()>0)
		{
				String OSR = result.get(0)[0];
				
				result = null;
				
				/*
				 * Now check is this OSR exists for another sitecode or not
				 * if not exists for another sitecode then sitecode = osr
				 * if exists for another sitecode then change the sitecode and OSR.
				 * 
				 */
				
				
				query = "select orange_sitename from "+ConnectionBean.getDbPrefix()+"eq_site where orange_sitename=? and sitecode <> ?";
				
				result = CommonUtils.getQueryResult(query,siteFixViewComponents,OSR,sitecode);
			
				String newSiteCode = sitecode;	
				
				if(result.size()>0)
				{
				
				siteFixViewComponents.getQueryResult().append("SUGGESTED OSR FOUND IN DATABASE >> "+ OSR+"\n");
				 /*
				 * It mean :: OSR found somewhere in the database	
				 */
				newSiteCode = sitecode+"_"+OSR;	
				
					if(newSiteCode.length()>40)
					{
						newSiteCode = newSiteCode.substring(0,40);
					}
				newSiteCode = createUniqueOSR(newSiteCode);
				return newSiteCode;
			}
		}
		else
		{
			return null;
		}
		return sitecode;	
	}

	private String createUniqueOSR(String osr) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
		String query = "select orange_sitename,sitecode from "+ConnectionBean.getDbPrefix()+"eq_site where orange_sitename=?";
		
		ArrayList<String[]> result = CommonUtils.getQueryResult(query,siteFixViewComponents,osr);
	
		if(result.size()>0)
		{
			String OSR = result.get(0)[0];
			String sitecode = result.get(0)[1];
			osr = sitecode+"_"+OSR;
			
			if(osr.length()>40)
			{
				osr = osr.substring(0,40);
			}
			
			createUniqueOSR(osr);
		}
		return osr;
	}

	public ArrayList<String> getErrors() {
		return errors;
	}
	
	public void prepareQuery() throws Exception {
		ArrayList<String[]> data = linkedupSiteFixFileValidator.getFileData();

		
		double count =0.0;
		int totalCount = data.size();
		for (String[] row : data) 
		{

			ProgressMonitorPane.getInstance().setProgress(count,(double)totalCount);
			String replaceSitecode = row[0];
			String replaceStatus = row[1];
			String replacementSitecode = row[2];
			String replacementStatus = row[3];
			String replaceOSR = row[4];
			String replacementOSR = row[5];
			
			String orders = row[6];
			
			String legacySiteCode = row[7];
			String ADDRESS_ID = row[8];
			String ANALYSIS_STATUS = row[9];
			//String COMMENTS = row[10];
			
			if(!ANALYSIS_STATUS.trim().equalsIgnoreCase("PASSED"))
			{
				return;
			}
			
			ArrayList<String[]> quotes = new ArrayList<String[]>();
			ArrayList<String[]> hotcutquotes = new ArrayList<String[]>();
			String coresiteid = "";
			String addressid = "";
			
			
			
			goldExecutionScript.add("-- INFO : GOLD Script Starts for "+ADDRESS_ID +" AT "+CurrentDateTime.getDateTimeText() +" for Replace Sitecode:"+ replaceSitecode + " And Replacement Sitecode:"+ replacementSitecode+" Record Number ("+(int)(count+1)+" from "+totalCount+")");
			siteFixViewComponents.getQueryResult().append("-- INFO : GOLD Script for Replace Sitecode:"+ replaceSitecode + " And Replacement Sitecode:"+ replacementSitecode+"\n");
			
			/*
			 * These are Errors: No Tril GID More then One TRIL GID
			 */
			
			ArrayList<String[]> replace_SiteDetails = new ArrayList<String[]> ();
			ArrayList<String[]> replacement_SiteDetails = new ArrayList<String[]> ();

			if (!CommonUtils.isNULL(replaceSitecode)) {
				String replaceTrilGID = "SELECT TRIL_GID,CORE_SITE_ID,ADDRESS_ID,STATUS,ORANGE_SITENAME FROM "+ConnectionBean.getDbPrefix()+ "eq_site where SITECODE=?";
				replace_SiteDetails = CommonUtils.getQueryResult(replaceTrilGID,siteFixViewComponents,replaceSitecode);
			}
			if (!CommonUtils.isNULL(replacementSitecode)) {
				String replacementTrilGID = "SELECT TRIL_GID,CORE_SITE_ID,ADDRESS_ID,STATUS,ORANGE_SITENAME FROM "+ConnectionBean.getDbPrefix()+"eq_site where SITECODE=?";
				replacement_SiteDetails = CommonUtils.getQueryResult(replacementTrilGID,siteFixViewComponents,replacementSitecode);
			}

			if (replace_SiteDetails.size() == 0) 
			{
				errors.add("NO TRILGID FOR REPLACE SITECODE " + replaceSitecode);
				goldExecutionScript.add("-- ERROR : NO TRILGID FOR REPLACE SITECODE " + replaceSitecode );
			} else if (replace_SiteDetails.size() > 1) 
			{
				errors.add("MULTIPLE TRILGID FOR REPLACE SITECODE "+ replaceSitecode);
				goldExecutionScript.add("-- ERROR : MULTIPLE TRILGID FOR REPLACE SITECODE "+ replaceSitecode );
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
			if (!CommonUtils.isNULL(orders) && !orders.equalsIgnoreCase("ALL") && !orders.equalsIgnoreCase("NONE")) 
			{
				
				for(String retval: orders.trim().split(";")) 
				{
				String getOrderSQL = "select quotenumber from "+ConnectionBean.getDbPrefix()+ "sc_quote where quotenumber=?";
				ArrayList<String[]> order = CommonUtils.getQueryResult(getOrderSQL,siteFixViewComponents,CommonUtils.addZeroPrefixinOrder(retval));
				if(order.isEmpty())
				{
					errors.add("ORDER NOT FOUND TO MOVE "+ retval);
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
				
				String quotessql = "SELECT QUOTENUMBER FROM "+ConnectionBean.getDbPrefix()+"SC_QUOTE WHERE SITE =?";//in (select tril_gid from "+ConnectionBean.getDbPrefix()+"eq_site where SITECODE=?)";
				quotes = CommonUtils.getQueryResult(quotessql, siteFixViewComponents,replace_SiteDetails.get(0)[0]);
				String hotcutquotessql = "SELECT QUOTENUMBER ,SITE,HOTCUTNEWSITE FROM "+ ConnectionBean.getDbPrefix()+ "SC_QUOTE WHERE HOTCUTNEWSITE =?";// (select tril_gid from "+ConnectionBean.getDbPrefix()+"eq_site where SITECODE=?)";
				hotcutquotes = CommonUtils.getQueryResult(hotcutquotessql,siteFixViewComponents,replace_SiteDetails.get(0)[0]);
				
				/*
				 * 
				 * Filter List of orders from Database 
				 * Remove orders from process, other then MOVE_ORDERS columns 
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
				addressid  = replacement_SiteDetails.get(0)[2];
				
				// for quotes
				for (String quote[] : quotes) 
				{
						String quotationNumber = quote[0];
						if(!replace_SiteDetails.get(0)[0].equals(replacement_SiteDetails.get(0)[0]))
						{
						String executiondata = "UPDATE "+ConnectionBean.getDbPrefix()+"SC_QUOTE  SET MODIFICATIONDATE=Sysdate, SITE='"+ replacement_SiteDetails.get(0)[0]+ "' WHERE QUOTENUMBER ='"+quotationNumber+"';";
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
								String executiondata = "UPDATE "+ConnectionBean.getDbPrefix()+"SC_QUOTE SET MODIFICATIONDATE=Sysdate, SITE='"+replacement_SiteDetails.get(0)[0]+"' WHERE QUOTENUMBER ='"+quotationNumber+"';";
								goldExecutionScript.add(executiondata);
								
								// ADD UPDATE OF EQL2InstanceMapping
								goldExecutionScript = CommonUtils.updateEQL2InstanceMapping(goldExecutionScript,siteFixViewComponents,replaceSitecode,replacementSitecode,quotationNumber);
							}
							else
							{
								goldExecutionScript.add("--WARNING : SITE FOUND UPDATED AS REPLACE SITE ("+replace_SiteDetails.get(0)[0]+") AND REPLACEMENT SITE ("+replacement_SiteDetails.get(0)[0]+"). HENCE NO UPDATE REQUIRED.");	
							}
							
						} 
						else if ((!CommonUtils.isNULL(hotcutsite))&& hotcutsite.equalsIgnoreCase(replace_SiteDetails.get(0)[0])) 
						{
							if(!replace_SiteDetails.get(0)[0].equals(replacement_SiteDetails.get(0)[0]))
							{
								String executiondata = "UPDATE "+ConnectionBean.getDbPrefix()+"SC_QUOTE  SET MODIFICATIONDATE=Sysdate, HOTCUTNEWSITE='"+replacement_SiteDetails.get(0)[0]+"' WHERE QUOTENUMBER ='"+quotationNumber+"';";
								goldExecutionScript.add(executiondata);
								
								// ADD UPDATE OF EQL2InstanceMapping
								goldExecutionScript = CommonUtils.updateEQL2InstanceMapping(goldExecutionScript,siteFixViewComponents,replaceSitecode,replacementSitecode,quotationNumber);
							}
							else
							{
								goldExecutionScript.add("--WARNING : HOTCUTNEWSITE FOUND UPDATED AS REPLACE SITE ("+replace_SiteDetails.get(0)[0]+") AND REPLACEMENT SITE ("+replacement_SiteDetails.get(0)[0]+"). HENCE NO UPDATE REQUIRED.");	
							}
						} 
						else 
						{
							errors.add("NO SITE/HOTCUTNEWSITE MATCHED FOR REPLACE SITECODE "+ replaceSitecode+ " ORDER "+ quotationNumber);
							goldExecutionScript.add("-- ERROR :  NO SITE/HOTCUTNEWSITE MATCHED FOR REPLACE SITECODE "+ replaceSitecode+ " ORDER "+ quotationNumber );
						}
					
				}
			

			// Change status of site codes.
			if (!CommonUtils.isNULL(replaceStatus)) {
				if(!replace_SiteDetails.get(0)[3].equals(replaceStatus))
				{
					String executiondata = "UPDATE " +ConnectionBean.getDbPrefix()+"EQ_SITE SET STATUS=" + replaceStatus+ ", MODIFICATIONDATE=sysdate , EQ_COMMENT='PART OF MULTIPLE ACTIVE SITE FIX ' ||sysdate where TRIL_GID='"+replace_SiteDetails.get(0)[0]+"';";
					goldExecutionScript.add(executiondata);
				}
				else
				{
					goldExecutionScript.add("--INFO :SITE STATUS "+replaceSitecode +" FOUND UPDATED ("+replaceStatus+"). HENCE NO UPDATE REQUIRED.");
				}
			}
			if (!CommonUtils.isNULL(replacementStatus)) 
			{
				if(!replacement_SiteDetails.get(0)[3].equals(replacementStatus))
				{
					String executiondata = "UPDATE " +ConnectionBean.getDbPrefix()+"EQ_SITE SET STATUS=" + replacementStatus+ ", MODIFICATIONDATE=sysdate , EQ_COMMENT='PART OF MULTIPLE ACTIVE SITE FIX ' ||sysdate where TRIL_GID='"+replacement_SiteDetails.get(0)[0]+"';";
					goldExecutionScript.add(executiondata);
				}
				else
				{
					goldExecutionScript.add("--INFO :SITE STATUS "+replacementSitecode +" FOUND UPDATED ("+replacementStatus+"). HENCE NO UPDATE REQUIRED.");
				}
				
			}

			// Change OSR of site codes.

			if (!CommonUtils.isNULL(replaceOSR)) 
			{
				
				if(!replace_SiteDetails.get(0)[4].equals(replaceOSR))
				{
					String executiondata = "UPDATE " + ConnectionBean.getDbPrefix()	+ "EQ_SITE SET ORANGE_SITENAME='" + replaceOSR + "', MODIFICATIONDATE=sysdate where TRIL_GID='"+ replace_SiteDetails.get(0)[0] + "';";
					goldExecutionScript.add(executiondata);
				}
				else
				{
					goldExecutionScript.add("--INFO :SITE ORANGE_SITENAME "+replaceOSR +" FOUND UPDATED for SITE ("+replaceSitecode+"). HENCE NO UPDATE REQUIRED.");
				}
				
			}
			if (!CommonUtils.isNULL(replacementOSR)) 
			{
				
				if(!replacement_SiteDetails.get(0)[4].equals(replacementOSR))
				{
					String executiondata = "UPDATE " +ConnectionBean.getDbPrefix() + "EQ_SITE SET ORANGE_SITENAME='" + replacementOSR + "', MODIFICATIONDATE=sysdate where TRIL_GID='"+ replacement_SiteDetails.get(0)[0] + "';";
					goldExecutionScript.add(executiondata);
				}
				else
				{
					goldExecutionScript.add("--INFO :SITE ORANGE_SITENAME "+replacementOSR +" FOUND UPDATED for SITE ("+replacementSitecode+"). HENCE NO UPDATE REQUIRED.");
				}
			}
			
			
				 /*
				 * 
				 * Make Site legacy
				 * 
				 */
				if(!legacySiteCode.isEmpty() && siteFixViewComponents.isLegacyRequired().isSelected())
				{
				goldExecutionScript.add("-- INFO : MAKE LEGACY SITE "+legacySiteCode);
				
				String newSiteCode = createLegacy(legacySiteCode);
				if(!CommonUtils.isNULL(newSiteCode))
				{
					
					if(!newSiteCode.equals(legacySiteCode))
					{
						goldExecutionScript.add("-- INFO : Legacy sitecode >> "+legacySiteCode+" changed with auto generated new sitecode "+newSiteCode);
						String executiondata = "UPDATE " +ConnectionBean.getDbPrefix()+"EQ_SITE SET ORANGE_SITENAME='"+newSiteCode+"',CORE_SITE_ID ='',ADDRESS_ID='' , SITECODE ='"+newSiteCode+"', MODIFICATIONDATE=Sysdate WHERE SITECODE='"+legacySiteCode+"';";
						goldExecutionScript.add(executiondata);
					}
					else{
						goldExecutionScript.add("-- INFO : LEGACY SITECODE "+legacySiteCode+" AND NEW SITECODE "+newSiteCode+" ARE SAME.HENCE NOT UPDATED.");
					}
				}
				if(CommonUtils.isNULL(newSiteCode))
				{
					goldExecutionScript.add("-- ERROR :  LEGACY SITECODE "+legacySiteCode+" CHANGED WITH AUTO-GENETRATED NULL, FURTHER ACTION(S) SUSPENDED.");
				}
				}
			
			// Here CSI Connection
			// Here CSI Query for Row <n> data
			csiExecutionScript.add("-----------------------------------------------------------------------------");
			csiExecutionScript.add("-- INFO :CSI Script Starts for "+ADDRESS_ID +" AT " +CurrentDateTime.getDateTimeText()+" for Replace Sitecode:"+ replaceSitecode + " and Replacement Sitecode:"+ replacementSitecode+" Record Number ("+(int)(count+1)+" from "+totalCount+")");
			siteFixViewComponents.getQueryResult().append("-- INFO :CSI Script for Replace Sitecode:"+ replaceSitecode + " And Replacement Sitecode:"+ replacementSitecode+"\n");
			
			if(quotes.size()==0 && hotcutquotes.size() ==0)
			{
				csiExecutionScript.add("-- INFO :NO ORDER EXISTS.. ");
			}
			
			String usdiddatasql = "select Serviceelementid,USID from "+ConnectionBeanCSI.getDbPrefix()+"cserviceelement where serviceelementid in (select serviceelementid from "+ConnectionBeanCSI.getDbPrefix()+"CVERSIONSERVICEELEMENT  where versionid in (select versionid from "+ConnectionBeanCSI.getDbPrefix()+"cversion where ordhandle=?))";

			
			/*
			 * Here we need to change replacement sitecode and sitecode for legacy 
			 * if that site code been changed for GOLD to make legacy rules
			 * 
			 * 
			 */
			
				String proxyReplacementSitecode = replacementSitecode;
				String proxylegacySiteCode = legacySiteCode;
				
				if(!legacySiteCode.isEmpty() && siteFixViewComponents.isLegacyRequired().isSelected())
				{
					String newSiteCode = createLegacy(legacySiteCode);		
				
					if(!CommonUtils.isNULL(newSiteCode) && legacySiteCode.equals(replacementSitecode))
					{
						legacySiteCode = newSiteCode;
						replacementSitecode = legacySiteCode;
						csiExecutionScript.add("-- INFO :LEGACY SITECODE "+proxylegacySiteCode+"  AND REPLACEMENT SITECODE "+proxyReplacementSitecode +" CHANGED WITH AUTO-GENERATED SITECODE "+ legacySiteCode);
					}
					else if(!CommonUtils.isNULL(newSiteCode) && !legacySiteCode.equals(replacementSitecode))
					{
						legacySiteCode = newSiteCode;
						csiExecutionScript.add("-- INFO :LEGACY SITECODE "+proxylegacySiteCode+"  CHANGED WITH "+ legacySiteCode);
					}
					else {
						csiExecutionScript.add("-- ERROR :  LEGACY SITECODE IS NULL , FURTHER ACTIONS(S) SUSPENDED.");
					}
				}
			
			for (String quote[] : quotes) 
			{
				
					String quoteNumber = quote[0];
					ArrayList<String[]> usiddata = CommonUtils.getCSIQueryResult(usdiddatasql,	siteFixViewComponents,quoteNumber);
					
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
						if(isAutoUpdated.equals("True"))
						{
						String executionData = "Update "+ConnectionBeanCSI.getDbPrefix()+"Cserviceelement Set Usid='"+ autoUpdatedUSID+ "',Lupddate=Sysdate Where Serviceelementid='"+ serviceElementID + "';";
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
						
						if(isAutoUpdated.equals("True"))
						{
						String executionData = "Update "+ConnectionBeanCSI.getDbPrefix()+"Cserviceelement Set Usid='"+ autoUpdatedUSID+ "',Lupddate=Sysdate Where Serviceelementid='"+ serviceElementID + "';";
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
			/*
			 * 
			 * Make Site legacy
			 * 
			 */
			if(!legacySiteCode.isEmpty() && siteFixViewComponents.isLegacyRequired().isSelected())
			{
				csiExecutionScript.add("-- Make Legacy site "+legacySiteCode);
				String legacyExecutionData = "Update "+ConnectionBeanCSI.getDbPrefix()+"cversion set Lupddate=Sysdate,coresiteid='',addressid='', sitehandle='"+legacySiteCode+"' where sitehandle='" + proxylegacySiteCode + "';";
				csiExecutionScript.add(legacyExecutionData);
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
		String[] columnNames ={"QUOTE_NUMBER","REPLACE_SITECODE","REPLACEMENT_SITECODE","SERVICE ELEMENT ID","PREV USID","UPDATED USID","IS AUTO UPDATED"};
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
		table.setModel(model);
		table.getTable().setAutoCreateRowSorter(true);
		JScrollPane scroll = new JScrollPane(table.getTable());
		CustomJFrame f = new CustomJFrame("Total Rows("+table.getTable().getRowCount()+")",Icons.iconPath15);
		f.setBounds(200,150,500,400);
		f.add(scroll);
		f.setVisible(true);
		f.pack();
		
	}
}
	
	
