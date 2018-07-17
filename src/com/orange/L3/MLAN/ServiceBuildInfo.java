package com.orange.L3.MLAN;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.orange.ui.component.MLANViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.ConnectionBeanArchived;
import com.orange.util.CustomJFileChooser;
import com.orange.util.ProgressMonitorPane;

public class ServiceBuildInfo {
	
	
	public  static String columnArray[]			=  {"QUOTENUMBER","ARCHIVED","QUOTE_TYPE","EQ_TYPE","QUOTE_STATUS","QUOTE_SERVICE","QUOTE_MIGRATEDSERVICE","PREV_QUOTE","ASSOCIATED_QUOTE","CORE_SITE_ID","ADDRESS_ID","ORANGE_SITENAME","SITECODE","STATUS","COUNTRY","COUNTRY_CODE","COUNTRY_CAT","LOCALSITE_USER_ICO","LOCALSITE_USER_NAME","END_USER_ICO","END_USER_NAME","CONTRACTING_PARTY_ICO","CONTRACTING_PARTY_NAME","ELEMENT_SERVICEBUILD","SALES_NOTIFICATIONDATE","ORDER_CREATIONDATE","LINEITEM","VALUE","EXIST_CONFIG","NEW_CONFIG","CONSIDERABLE_VALUE"};
	private String orderFileHeader[]  			=  {"QUOTENUMBER","ARCHIVED","QUOTE_TYPE","EQ_TYPE","QUOTE_STATUS","QUOTE_SERVICE","QUOTE_MIGRATEDSERVICE","PREV_QUOTE","ASSOCIATED_QUOTE","CORE_SITE_ID","ADDRESS_ID","ORANGE_SITENAME","SITECODE","STATUS","COUNTRY","COUNTRY_CODE","COUNTRY_CAT","LOCALSITE_USER_ICO","LOCALSITE_USER_NAME","END_USER_ICO","END_USER_NAME","CONTRACTING_PARTY_ICO","CONTRACTING_PARTY_NAME","ELEMENT_SERVICEBUILD","SALES_NOTIFICATIONDATE","ORDER_CREATIONDATE","CONSIDERABLE_VALUE"};
	//private String ADD_DATE_RANGE_GOLD_SUFFIX	=" AND QUOTE.TRIL_GID = MILES.EQ_ORDERGID AND MILES.EQ_TITLE='Order Raised Date' AND MILES.EQ_CREATIONDATE BETWEEN <DATE_RNG_FROM> AND <DATE_RNG_TO>";
	private String ADD_DATE_RANGE_GOLD_SUFFIX	=" AND MILES.EQ_CREATIONDATE BETWEEN <DATE_RNG_FROM> AND <DATE_RNG_TO>";
	private String SITE_OR_HOTCUT				=" AND ((QUOTE.HOTCUTNEWSITE <> 'NULL' AND QUOTE.HOTCUTNEWSITE =  SITE.TRIL_GID) OR (QUOTE.HOTCUTNEWSITE IS NULL AND QUOTE.SITE = SITE.TRIL_GID ))";
	private String ADD_ICO_FILTER				=" AND QUOTE.<PARTY> = ORGANIZATION.TRIL_GID AND ORGANIZATION.ORGANIZATIONID IN (<IOCTEXT>)";
	
	
	private ArrayList<String[]> archiveOrders;
	private Map<String,String> checkOnly;
	
	private String getArchivedOrderByDateRange = "SELECT MILES.ORDERNUMBER FROM " +
												 ConnectionBeanArchived.getDbPrefix()+"EQMILESTONE_A MILES " +
												" WHERE " +
												" MILES.EQ_TITLE = 'Order Raised Date' " +
												" AND MILES.EQ_ACTUALDATE BETWEEN <DATE_RNG_FROM> AND <DATE_RNG_TO>"+
												" AND MILES.ORDERNUMBER = ?";
	
	
	
	
	private String getArchivedOrdersServiceBuildInfo = 
												" SELECT " +
												" LINEITEM.ORDERNUMBER,"+
												 CommonUtils.LINEITEM_UDSTATUS +
												" MILES.EQ_ACTUALDATE AS CREATION_DATE," +
												" LINEITEM.DESCRIPTION,LINEITEM.VALUE,LINEITEM.EXIST_CONFIG,LINEITEM.NEW_CONFIG," +
												"'to be updated' as CONSIDERABLE_VALUE " +
												" FROM "+
												  ConnectionBeanArchived.getDbPrefix()+"SC_QUOTE_LINE_ITEM_A LINEITEM, " +
												  ConnectionBeanArchived.getDbPrefix()+"EQMILESTONE_A MILES "+
												" WHERE " +
												" LINEITEM.ORDERNUMBER IN (?) "+
												" AND LINEITEM.DESCRIPTION DES_INORLIKE (<PARAM2>)"+
												" AND MILES.EQ_TITLE = 'Order Raised Date'"+
												" AND MILES.ORDERNUMBER = LINEITEM.ORDERNUMBER";
	
	
	
	
	private String getArchivedOrdersServiceBuildInfo_INCLUDE_NOTIN =
		                                                        " SELECT " +
		                                                    	" LINEITEM.ORDERNUMBER," +
		                                                    	  CommonUtils.LINEITEM_UDSTATUS +
		                                                        " MILES.EQ_ACTUALDATE AS CREATION_DATE," +
		                                                        " CASE WHEN LINEITEM.DESCRIPTION <> 'NULL' THEN '' ELSE '' END AS DESCRIPTION, " +
															    " CASE WHEN LINEITEM.VALUE <> 'NULL' THEN '' ELSE '' END AS VALUE, " +
															    " CASE WHEN LINEITEM.EXIST_CONFIG <> 'NULL' THEN '' ELSE '' END AS EXIST_CONFIG, " +
															    " CASE WHEN LINEITEM.NEW_CONFIG <> 'NULL' THEN '' ELSE '' END AS NEW_CONFIG, " +
															     CommonUtils.getConsiderableValue() +
															    " FROM "+
																  ConnectionBeanArchived.getDbPrefix()+"SC_QUOTE_LINE_ITEM_A LINEITEM, " +
																  ConnectionBeanArchived.getDbPrefix()+"EQMILESTONE_A MILES "+
																" WHERE " +
																" LINEITEM.ORDERNUMBER IN (?) "+
																" AND LINEITEM.DESCRIPTION DES_INORLIKE (<PARAM2>)"+
																" AND MILES.EQ_TITLE = 'Order Raised Date'"+
																" AND MILES.ORDERNUMBER = LINEITEM.ORDERNUMBER";
								
	
	private String getGOLDLineItemsInfo = 
		                        " SELECT DISTINCT LINEITEM.DESCRIPTION, " +
		                        " COUNT(DISTINCT QUOTE.QUOTENUMBER) AS QUOTE_COUNT," +
		                        " CASE  WHEN COUNT(QUOTE.QUOTENUMBER) > 500  THEN 'Morethen 500 Orders.'  " +
		                        " ELSE  XMLAGG(XMLELEMENT(E,QUOTE.QUOTENUMBER,',')).EXTRACT('//text()').getStringVal() END AS QUOTES "+
		                        " FROM "+
		                        ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORGANIZATION,"+  
		                        ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE " +
		                        " LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID)"+
		                        " LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQMILESTONE MILES ON (MILES.EQ_ORDERGID=QUOTE.TRIL_GID AND MILES.EQ_TITLE='Order Raised Date'),"+
		                        ConnectionBean.getDbPrefix()+"SC_QUOTE_LINE_ITEM LINEITEM "+
								" WHERE " +
								" QUOTE.TRIL_GID = LINEITEM.QUOTE " +
								" ADD_ICO_FILTER "+
								" AND ((QUOTE.MIGRATIONSERVICE <> 'NULL' AND QUOTE.MIGRATIONSERVICE <> 'NONE' AND SERVICE.DISP_NAME SER_INORLIKE (<PARAM1>) ) OR  ( (QUOTE.MIGRATIONSERVICE IS NULL  OR QUOTE.MIGRATIONSERVICE ='NONE') AND QUOTE.SERVICENAME SER_INORLIKE (<PARAM1>) ))  "+ 
								" AND LINEITEM.DESCRIPTION <> 'NULL' " +
								" AND QUOTE.ARCHIVED <> '1' ";
	
	
	private String getGOLDLineItemsInfoForSites = 
					        " SELECT DISTINCT LINEITEM.DESCRIPTION, " +
					        " COUNT(DISTINCT QUOTE.QUOTENUMBER) AS QUOTE_COUNT," +
					        " CASE  WHEN COUNT(QUOTE.QUOTENUMBER) > 500  THEN 'Morethen 500 Orders.'  " +
					        " ELSE  XMLAGG(XMLELEMENT(E,QUOTE.QUOTENUMBER,',')).EXTRACT('//text()').getStringVal() END AS QUOTES "+
					        " FROM "+
					        ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORGANIZATION,"+  
					        ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE " +
					        " LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID)"+
					        " LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQMILESTONE MILES ON (MILES.EQ_ORDERGID=QUOTE.TRIL_GID AND MILES.EQ_TITLE='Order Raised Date'),"+
							ConnectionBean.getDbPrefix()+"SC_QUOTE_LINE_ITEM LINEITEM ,"+
							/*
							
	                         THIS BELOW MILES LINE WILL REPLACE WITH NULL <EMPTY IF NOT DATE RANGE SELECTED.>
	                         
	                        
	                        ConnectionBean.getDbPrefix()+"EQMILESTONE MILES,"+
	                        
	                        
	                         END OF CONMMENT
	                         
	                        */
							ConnectionBean.getDbPrefix()+"EQ_SITE SITE "+
							" WHERE " +
							" QUOTE.TRIL_GID = LINEITEM.QUOTE " +
							" ADD_ICO_FILTER "+
							" AND ((QUOTE.MIGRATIONSERVICE <> 'NULL' AND QUOTE.MIGRATIONSERVICE <> 'NONE' AND SERVICE.DISP_NAME SER_INORLIKE (<PARAM1>) ) OR  ( (QUOTE.MIGRATIONSERVICE IS NULL  OR QUOTE.MIGRATIONSERVICE ='NONE') AND QUOTE.SERVICENAME SER_INORLIKE (<PARAM1>) ))  "+ 
							" AND ((QUOTE.HOTCUTNEWSITE <> 'NULL' AND QUOTE.HOTCUTNEWSITE =  SITE.TRIL_GID) OR (QUOTE.HOTCUTNEWSITE IS NULL AND QUOTE.SITE = SITE.TRIL_GID )) "+
							" AND LINEITEM.DESCRIPTION <> 'NULL' " +
							" AND <SEARCHWITH> IN( <PARAMS> ) "+
							" AND QUOTE.ARCHIVED <> '1' ";
	
	
	private String getListOfArchivedLineItemsInfo = 
											" SELECT LINEITEM.DESCRIPTION FROM "+
											  ConnectionBeanArchived.getDbPrefix()+"SC_QUOTE_LINE_ITEM_A LINEITEM " +
											" WHERE  LINEITEM.ORDERNUMBER IN (?) " +
											" AND LINEITEM.DESCRIPTION <> 'NULL' ";
	
	
	// GET ARCHIVE DATE RANGE ORDER LIST FROM ARCHIVE DATABASE 
	private String getListOfArchivedOrders = 
		                                    " SELECT DISTINCT" +
											" QUOTE.QUOTENUMBER," +
											  CommonUtils.ARCHIVE_DECODE+
											  CommonUtils.ORDER_TYPE_DECODE+
										      CommonUtils.EQ_TYPE_DECODE+
											" ORDSTATUS.EQ_STATUS,"+
											" QUOTE.SERVICENAME," +
											" SERVICE.DISP_NAME AS MIGRATIONSERVICENAME, "+
											" QUOTE.EQ_GOLDORIGNB ,QUOTE.ASSOCIATEDORDERNB, "+
											" SITE.CORE_SITE_ID,SITE.ADDRESS_ID,SITE.ORANGE_SITENAME,SITE.SITECODE,SITE.STATUS, "+
											" ADDR.COUNTRY,ADDR.COUNTRY_CODE,MIS.COUNTRYCATEGORY AS COUNTRY_CAT,"+
											" ORG.ORGANIZATIONID AS LOCALSITE_USER_ICO,ORG.NAME AS LOCALSITE_USER_NAME,"+
											" ORG1.ORGANIZATIONID AS END_USER_ICO,ORG1.NAME AS END_USER_NAME,"+
											" ORG2.ORGANIZATIONID AS CONTRACTING_PARTY_ICO,ORG2.NAME AS CONTRACTING_PARTY_NAME,"+
											" QUOTE.EQ_CUSTOMERNOTICE AS SALES_NOTIFICATIONDATE "+
											" FROM "+
										        ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORGANIZATION,"+  
										        ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE " +
										        "LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID) "+
										        "LEFT JOIN "+ConnectionBean.getDbPrefix()+"SC_QUOTE_MISCELLANEOUS MIS ON (MIS.TRIL_GID = QUOTE.SC_QUOTE_MISCELLANEOUS),"+
										        ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID) ,"+
										        ConnectionBean.getDbPrefix()+"EQ_ORDERSTATUS ORDSTATUS," +
											    ConnectionBean.getDbPrefix()+"SC_ADDRESS ADDR," +
											    ConnectionBean.getDbPrefix()+"EQ_SITE SITE,"+
											    ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG," +
											    ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG1," +
												ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG2" + 
										
											" WHERE " +
											" ((QUOTE.MIGRATIONSERVICE <> 'NULL' AND QUOTE.MIGRATIONSERVICE <> 'NONE' AND SERVICE.DISP_NAME SER_INORLIKE (<PARAM1>) ) OR  ( (QUOTE.MIGRATIONSERVICE IS NULL  OR QUOTE.MIGRATIONSERVICE ='NONE') AND QUOTE.SERVICENAME SER_INORLIKE (<PARAM1>) ))  "+
											" AND QUOTE.SERVICENAME <> 'NULL' " +
											" AND ORDSTATUS.TRIL_GID = QUOTE.EQ_WFSTATUS"+
									          SITE_OR_HOTCUT+
											"  ADD_ICO_FILTER "+
											" AND SITE.EQ_SITEOF = ORG.TRIL_GID"+
											" AND QUOTE.EQ_DELIVERYPARTY = ORG1.TRIL_GID"+
											" AND QUOTE.EQ_REQUESTINGPARTY= ORG2.TRIL_GID"+ 
											" AND ADDR.TRIL_GID = SITE.SITEADDRESS"+
											" AND QUOTE.CONFIGURATIONS = HR.TRIL_GID " +
											" AND QUOTE.ARCHIVED = '1'" ;
											
	
	private String getListOfArchivedOrdersForSites = 
					        " SELECT DISTINCT" +
							" QUOTE.QUOTENUMBER," +
							  CommonUtils.ARCHIVE_DECODE+
							  CommonUtils.ORDER_TYPE_DECODE+
						      CommonUtils.EQ_TYPE_DECODE+
							" ORDSTATUS.EQ_STATUS,"+
							" QUOTE.SERVICENAME," +
							" SERVICE.DISP_NAME AS MIGRATIONSERVICENAME, "+
							" QUOTE.EQ_GOLDORIGNB ,QUOTE.ASSOCIATEDORDERNB, "+
							" SITE.CORE_SITE_ID,SITE.ADDRESS_ID,SITE.ORANGE_SITENAME,SITE.SITECODE,SITE.STATUS, "+
							" ADDR.COUNTRY,ADDR.COUNTRY_CODE,MIS.COUNTRYCATEGORY AS COUNTRY_CAT,"+
							" ORG.ORGANIZATIONID AS LOCALSITE_USER_ICO,ORG.NAME AS LOCALSITE_USER_NAME,"+
							" ORG1.ORGANIZATIONID AS END_USER_ICO,ORG1.NAME AS END_USER_NAME,"+
							" ORG2.ORGANIZATIONID AS CONTRACTING_PARTY_ICO,ORG2.NAME AS CONTRACTING_PARTY_NAME,"+
							" QUOTE.EQ_CUSTOMERNOTICE AS SALES_NOTIFICATIONDATE "+
							" FROM "+
						        ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORGANIZATION,"+  
						        ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE " +
						        "LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID) "+
						        "LEFT JOIN "+ConnectionBean.getDbPrefix()+"SC_QUOTE_MISCELLANEOUS MIS ON (MIS.TRIL_GID = QUOTE.SC_QUOTE_MISCELLANEOUS),"+
						        ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID) ,"+
						        ConnectionBean.getDbPrefix()+"EQ_ORDERSTATUS ORDSTATUS," +
							    ConnectionBean.getDbPrefix()+"SC_ADDRESS ADDR," +
							    ConnectionBean.getDbPrefix()+"EQ_SITE SITE,"+
							    ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG," +
							    ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG1," +
								ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG2" + 
						
							" WHERE " +
							" ((QUOTE.MIGRATIONSERVICE <> 'NULL' AND QUOTE.MIGRATIONSERVICE <> 'NONE' AND SERVICE.DISP_NAME SER_INORLIKE (<PARAM1>) ) OR  ( (QUOTE.MIGRATIONSERVICE IS NULL  OR QUOTE.MIGRATIONSERVICE ='NONE') AND QUOTE.SERVICENAME SER_INORLIKE (<PARAM1>) ))  "+
							" AND QUOTE.SERVICENAME <> 'NULL' " +
							" AND ORDSTATUS.TRIL_GID = QUOTE.EQ_WFSTATUS"+
							   SITE_OR_HOTCUT+
							"  ADD_ICO_FILTER "+
							" AND SITE.EQ_SITEOF = ORG.TRIL_GID"+
							" AND QUOTE.EQ_DELIVERYPARTY = ORG1.TRIL_GID"+
							" AND QUOTE.EQ_REQUESTINGPARTY= ORG2.TRIL_GID"+ 
							" AND ADDR.TRIL_GID = SITE.SITEADDRESS"+
							" AND QUOTE.CONFIGURATIONS = HR.TRIL_GID " +
							" AND <SEARCHWITH> IN( <PARAMS> ) "+
							" AND QUOTE.ARCHIVED = '1'" ;
	
	// below are CORE query
	// ADD GOLD DATE RANGE
	
	private String getserviceBuildInfo = 
							        " SELECT distinct" +
							        " QUOTE.QUOTENUMBER ," +
							        CommonUtils.ARCHIVE_DECODE+
							        CommonUtils.ORDER_TYPE_DECODE+
							        CommonUtils.EQ_TYPE_DECODE+
							        " ORDSTATUS.EQ_STATUS,"+
							        " QUOTE.SERVICENAME," +
							       	"SERVICE.DISP_NAME AS MIGRATIONSERVICENAME, "+
							        " QUOTE.EQ_GOLDORIGNB ,QUOTE.ASSOCIATEDORDERNB, "+
							    	" SITE.CORE_SITE_ID,SITE.ADDRESS_ID,SITE.ORANGE_SITENAME,SITE.SITECODE,SITE.STATUS, "+
							        " ADDR.COUNTRY,ADDR.COUNTRY_CODE,MIS.COUNTRYCATEGORY AS COUNTRY_CAT,"+
							        " ORG.ORGANIZATIONID AS LOCALSITE_USER_ICO,ORG.NAME AS LOCALSITE_USER_NAME,"+
									" ORG1.ORGANIZATIONID AS END_USER_ICO,ORG1.NAME AS END_USER_NAME,"+
									" ORG2.ORGANIZATIONID AS CONTRACTING_PARTY_ICO,ORG2.NAME AS CONTRACTING_PARTY_NAME,"+
									  CommonUtils.LINEITEM_UDSTATUS +
							        " QUOTE.EQ_CUSTOMERNOTICE AS SALES_NOTIFICATIONDATE,MILES.EQ_CREATIONDATE AS CREATION_DATE,"+
							        " LINEITEM.DESCRIPTION,LINEITEM.VALUE,LINEITEM.EXIST_CONFIG,LINEITEM.NEW_CONFIG ," +
							        CommonUtils.getConsiderableValue() +
							      
							        " FROM "+
							        ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORGANIZATION,"+  
							        ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE " +
							        "LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID) "+
							        "LEFT JOIN "+ConnectionBean.getDbPrefix()+"SC_QUOTE_MISCELLANEOUS MIS ON (MIS.TRIL_GID = QUOTE.SC_QUOTE_MISCELLANEOUS) "+
							    	"LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQMILESTONE MILES ON (MILES.EQ_ORDERGID=QUOTE.TRIL_GID AND MILES.EQ_TITLE='Order Raised Date'),"+
							        
							    	ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID) ,"+
							        ConnectionBean.getDbPrefix()+"SC_QUOTE_LINE_ITEM LINEITEM, "+
							        ConnectionBean.getDbPrefix()+"EQ_ORDERSTATUS ORDSTATUS," +
							        ConnectionBean.getDbPrefix()+"SC_ADDRESS ADDR," +
							        ConnectionBean.getDbPrefix()+"EQ_SITE SITE,"+
							        ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG," +
							        ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG1," +
							    	ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG2 " +
							        " WHERE " +
							        " QUOTE.TRIL_GID = LINEITEM.QUOTE" +
							        " AND ORDSTATUS.TRIL_GID = QUOTE.EQ_WFSTATUS"+
							        " AND ((QUOTE.MIGRATIONSERVICE <> 'NULL' AND QUOTE.MIGRATIONSERVICE <> 'NONE' AND SERVICE.DISP_NAME SER_INORLIKE (<PARAM1>) ) OR  ( (QUOTE.MIGRATIONSERVICE IS NULL  OR QUOTE.MIGRATIONSERVICE ='NONE') AND QUOTE.SERVICENAME SER_INORLIKE (<PARAM1>) ))  "+
							        " AND LINEITEM.DESCRIPTION LINE_INORLIKE (<PARAM2>)" +
							         SITE_OR_HOTCUT+
							        "  ADD_ICO_FILTER "+
							        " AND SITE.EQ_SITEOF = ORG.TRIL_GID"+
							        " AND QUOTE.EQ_DELIVERYPARTY = ORG1.TRIL_GID"+
							        " AND QUOTE.EQ_REQUESTINGPARTY= ORG2.TRIL_GID"+ 
							        " AND ADDR.TRIL_GID = SITE.SITEADDRESS"+
							       // " AND MILES.EQ_ORDERGID=QUOTE.TRIL_GID"+
							       // " AND MILES.EQ_TITLE='Order Raised Date'"+
							    	" AND QUOTE.CONFIGURATIONS = HR.TRIL_GID " +
							        " AND QUOTE.ARCHIVED <> '1'";
	
	private String getserviceBuildInfoForSites = 
		                                    " SELECT distinct" +
		                                    " QUOTE.QUOTENUMBER ," +
		                                    CommonUtils.ARCHIVE_DECODE+
		                                    CommonUtils.ORDER_TYPE_DECODE+
		                                    CommonUtils.EQ_TYPE_DECODE+
		                                    " ORDSTATUS.EQ_STATUS,"+
		                                    " QUOTE.SERVICENAME," +
		                                   	"SERVICE.DISP_NAME AS MIGRATIONSERVICENAME, "+
		                                    " QUOTE.EQ_GOLDORIGNB ,QUOTE.ASSOCIATEDORDERNB, "+
		                                	" SITE.CORE_SITE_ID,SITE.ADDRESS_ID,SITE.ORANGE_SITENAME,SITE.SITECODE,SITE.STATUS, "+
		                                    " ADDR.COUNTRY,ADDR.COUNTRY_CODE,MIS.COUNTRYCATEGORY AS COUNTRY_CAT,"+
		                                    " ORG.ORGANIZATIONID AS LOCALSITE_USER_ICO,ORG.NAME AS LOCALSITE_USER_NAME,"+
											" ORG1.ORGANIZATIONID AS END_USER_ICO,ORG1.NAME AS END_USER_NAME,"+
											" ORG2.ORGANIZATIONID AS CONTRACTING_PARTY_ICO,ORG2.NAME AS CONTRACTING_PARTY_NAME,"+
										    CommonUtils.LINEITEM_UDSTATUS +
		                                    " QUOTE.EQ_CUSTOMERNOTICE AS SALES_NOTIFICATIONDATE,MILES.EQ_CREATIONDATE AS CREATION_DATE,"+
		                                    " LINEITEM.DESCRIPTION,LINEITEM.VALUE,LINEITEM.EXIST_CONFIG,LINEITEM.NEW_CONFIG ," +
		                                    CommonUtils.getConsiderableValue() +
		                                
		                                    " FROM "+
		                                    ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORGANIZATION,"+  
		                                    ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE " +
		                                    "LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID) "+
		                                    "LEFT JOIN "+ConnectionBean.getDbPrefix()+"SC_QUOTE_MISCELLANEOUS MIS ON (MIS.TRIL_GID = QUOTE.SC_QUOTE_MISCELLANEOUS) "+
		                                	"LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQMILESTONE MILES ON (MILES.EQ_ORDERGID=QUOTE.TRIL_GID AND MILES.EQ_TITLE='Order Raised Date'),"+
		                                    ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID) ,"+
									        ConnectionBean.getDbPrefix()+"SC_QUOTE_LINE_ITEM LINEITEM, "+
		                                    ConnectionBean.getDbPrefix()+"EQ_ORDERSTATUS ORDSTATUS," +
		                                    ConnectionBean.getDbPrefix()+"SC_ADDRESS ADDR," +
		                                    ConnectionBean.getDbPrefix()+"EQ_SITE SITE,"+
		                                    ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG," +
		                                    ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG1," +
		                                	ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG2 " +
		                                    " WHERE " +
								            " QUOTE.TRIL_GID = LINEITEM.QUOTE" +
		                                    " AND ORDSTATUS.TRIL_GID = QUOTE.EQ_WFSTATUS"+
		                                    " AND ((QUOTE.MIGRATIONSERVICE <> 'NULL' AND QUOTE.MIGRATIONSERVICE <> 'NONE' AND SERVICE.DISP_NAME SER_INORLIKE (<PARAM1>) ) OR  ( (QUOTE.MIGRATIONSERVICE IS NULL  OR QUOTE.MIGRATIONSERVICE ='NONE') AND QUOTE.SERVICENAME SER_INORLIKE (<PARAM1>) ))  "+
		                                    " AND LINEITEM.DESCRIPTION LINE_INORLIKE (<PARAM2>)" +
		                                     SITE_OR_HOTCUT+
		                                    "  ADD_ICO_FILTER "+
		                                    " AND SITE.EQ_SITEOF = ORG.TRIL_GID"+
		                                    " AND QUOTE.EQ_DELIVERYPARTY = ORG1.TRIL_GID"+
		                                    " AND QUOTE.EQ_REQUESTINGPARTY= ORG2.TRIL_GID"+ 
		                                    " AND ADDR.TRIL_GID = SITE.SITEADDRESS"+
		                                    " AND QUOTE.CONFIGURATIONS = HR.TRIL_GID " +
		                                	" AND <SEARCHWITH> IN( <PARAMS> ) "+
		                                    " AND QUOTE.ARCHIVED <> '1'";
	
	// ADD GOLD DATE RANGE
	
	private String getserviceBuildInfo_INCLUDE_NOTIN =
													" SELECT DISTINCT " +
													" QUOTE.QUOTENUMBER ," +
													CommonUtils.ARCHIVE_DECODE+
													CommonUtils.ORDER_TYPE_DECODE+
													CommonUtils.EQ_TYPE_DECODE+
													" ORDSTATUS.EQ_STATUS,"+
													" QUOTE.SERVICENAME, " +
													//" QUOTE.MIGRATIONSERVICENAME AS MIGRATIONSERVICENAME, "+
													" SERVICE.DISP_NAME AS MIGRATIONSERVICENAME, "+
													" QUOTE.EQ_GOLDORIGNB ,QUOTE.ASSOCIATEDORDERNB, "+
													" SITE.CORE_SITE_ID,SITE.ADDRESS_ID,SITE.ORANGE_SITENAME,SITE.SITECODE,SITE.STATUS, "+
													" ADDR.COUNTRY,ADDR.COUNTRY_CODE,MIS.COUNTRYCATEGORY AS COUNTRY_CAT,"+
													" ORG.ORGANIZATIONID AS LOCALSITE_USER_ICO,ORG.NAME AS LOCALSITE_USER_NAME,"+
													" ORG1.ORGANIZATIONID AS END_USER_ICO,ORG1.NAME AS END_USER_NAME,"+
													" ORG2.ORGANIZATIONID AS CONTRACTING_PARTY_ICO,ORG2.NAME AS CONTRACTING_PARTY_NAME,"+
													  CommonUtils.LINEITEM_UDSTATUS +
													" QUOTE.EQ_CUSTOMERNOTICE AS SALES_NOTIFICATIONDATE,MILES.EQ_CREATIONDATE AS CREATION_DATE,"+
													" CASE WHEN LINEITEM.DESCRIPTION <> 'NULL' THEN '' ELSE '' END AS DESCRIPTION,"+
													" CASE WHEN LINEITEM.VALUE <> 'NULL' THEN '' ELSE '' END AS VALUE, " +
													" CASE WHEN LINEITEM.EXIST_CONFIG <> 'NULL' THEN '' ELSE '' END AS EXIST_CONFIG, " +
													" CASE WHEN LINEITEM.NEW_CONFIG <> 'NULL' THEN '' ELSE '' END AS NEW_CONFIG, " +
													CommonUtils.getConsiderableValue() +
													
													" FROM "+
													ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORGANIZATION,"+  
													ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE " +
													"LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID) "+
													"LEFT JOIN "+ConnectionBean.getDbPrefix()+"SC_QUOTE_MISCELLANEOUS MIS ON (MIS.TRIL_GID = QUOTE.SC_QUOTE_MISCELLANEOUS) "+
													"LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQMILESTONE MILES ON (MILES.EQ_ORDERGID=QUOTE.TRIL_GID AND MILES.EQ_TITLE='Order Raised Date'),"+
													ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID), "+
											        ConnectionBean.getDbPrefix()+"SC_QUOTE_LINE_ITEM LINEITEM, "+
													ConnectionBean.getDbPrefix()+"EQ_ORDERSTATUS ORDSTATUS, " +
													ConnectionBean.getDbPrefix()+"SC_ADDRESS ADDR," +
													ConnectionBean.getDbPrefix()+"EQ_SITE SITE,"+
													ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG," +
													ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG1," +
													ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG2 " +
													" WHERE " +
													" QUOTE.TRIL_GID = LINEITEM.QUOTE" +
													" AND ORDSTATUS.TRIL_GID = QUOTE.EQ_WFSTATUS"+
													" AND ((QUOTE.MIGRATIONSERVICE <> 'NULL' AND QUOTE.MIGRATIONSERVICE <> 'NONE' AND SERVICE.DISP_NAME SER_INORLIKE (<PARAM1>) ) OR  ( (QUOTE.MIGRATIONSERVICE IS NULL  OR QUOTE.MIGRATIONSERVICE ='NONE') AND QUOTE.SERVICENAME SER_INORLIKE (<PARAM1>) ))  "+
													//" AND ((QUOTE.MIGRATIONSERVICENAME <> 'NULL' AND QUOTE.MIGRATIONSERVICENAME SER_INORLIKE (<PARAM1>) )  OR  ( QUOTE.MIGRATIONSERVICENAME IS NULL  AND QUOTE.SERVICENAME SER_INORLIKE (<PARAM1>) )) "+
													" AND LINEITEM.DESCRIPTION LINE_INORLIKE (<PARAM2>)"+
													SITE_OR_HOTCUT+
													"  ADD_ICO_FILTER "+
													" AND SITE.EQ_SITEOF = ORG.TRIL_GID"+
													" AND QUOTE.EQ_DELIVERYPARTY = ORG1.TRIL_GID"+
													" AND QUOTE.EQ_REQUESTINGPARTY= ORG2.TRIL_GID"+ 
													" AND ADDR.TRIL_GID = SITE.SITEADDRESS"+
													//" AND MILES.EQ_ORDERGID=QUOTE.TRIL_GID"+
													//" AND MILES.EQ_TITLE='Order Raised Date'"+
													" AND QUOTE.CONFIGURATIONS = HR.TRIL_GID "+
													" AND QUOTE.ARCHIVED <> '1'";
	
	private String getserviceBuildInfo_INCLUDE_NOTIN_ForSites =
												" SELECT DISTINCT " +
												" QUOTE.QUOTENUMBER ," +
												CommonUtils.ARCHIVE_DECODE+
												CommonUtils.ORDER_TYPE_DECODE+
												CommonUtils.EQ_TYPE_DECODE+
												" ORDSTATUS.EQ_STATUS,"+
												" QUOTE.SERVICENAME, " +
												//" QUOTE.MIGRATIONSERVICENAME AS MIGRATIONSERVICENAME, "+
												" SERVICE.DISP_NAME AS MIGRATIONSERVICENAME, "+
												" QUOTE.EQ_GOLDORIGNB ,QUOTE.ASSOCIATEDORDERNB, "+
												" SITE.CORE_SITE_ID,SITE.ADDRESS_ID,SITE.ORANGE_SITENAME,SITE.SITECODE,SITE.STATUS, "+
												" ADDR.COUNTRY,ADDR.COUNTRY_CODE,MIS.COUNTRYCATEGORY AS COUNTRY_CAT,"+
												" ORG.ORGANIZATIONID AS LOCALSITE_USER_ICO,ORG.NAME AS LOCALSITE_USER_NAME,"+
												" ORG1.ORGANIZATIONID AS END_USER_ICO,ORG1.NAME AS END_USER_NAME,"+
												" ORG2.ORGANIZATIONID AS CONTRACTING_PARTY_ICO,ORG2.NAME AS CONTRACTING_PARTY_NAME,"+
												  CommonUtils.LINEITEM_UDSTATUS +
												" QUOTE.EQ_CUSTOMERNOTICE AS SALES_NOTIFICATIONDATE,MILES.EQ_CREATIONDATE AS CREATION_DATE,"+
												" CASE WHEN LINEITEM.DESCRIPTION <> 'NULL' THEN '' ELSE '' END AS DESCRIPTION,"+
												" CASE WHEN LINEITEM.VALUE <> 'NULL' THEN '' ELSE '' END AS VALUE, " +
												" CASE WHEN LINEITEM.EXIST_CONFIG <> 'NULL' THEN '' ELSE '' END AS EXIST_CONFIG, " +
												" CASE WHEN LINEITEM.NEW_CONFIG <> 'NULL' THEN '' ELSE '' END AS NEW_CONFIG, " +
												CommonUtils.getConsiderableValue() +
											
												" FROM "+
												ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORGANIZATION,"+  
												ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE " +
												"LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID) "+
												"LEFT JOIN "+ConnectionBean.getDbPrefix()+"SC_QUOTE_MISCELLANEOUS MIS ON (MIS.TRIL_GID = QUOTE.SC_QUOTE_MISCELLANEOUS) "+
												"LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQMILESTONE MILES ON (MILES.EQ_ORDERGID=QUOTE.TRIL_GID AND MILES.EQ_TITLE='Order Raised Date'),"+
												ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID), "+
										        ConnectionBean.getDbPrefix()+"SC_QUOTE_LINE_ITEM LINEITEM, "+
												ConnectionBean.getDbPrefix()+"EQ_ORDERSTATUS ORDSTATUS, " +
												ConnectionBean.getDbPrefix()+"SC_ADDRESS ADDR," +
												ConnectionBean.getDbPrefix()+"EQ_SITE SITE,"+
												ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG," +
												ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG1," +
												ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG2 " +
												" WHERE " +
												" QUOTE.TRIL_GID = LINEITEM.QUOTE" +
												" AND ORDSTATUS.TRIL_GID = QUOTE.EQ_WFSTATUS"+
												" AND ((QUOTE.MIGRATIONSERVICE <> 'NULL' AND QUOTE.MIGRATIONSERVICE <> 'NONE' AND SERVICE.DISP_NAME SER_INORLIKE (<PARAM1>) ) OR  ( (QUOTE.MIGRATIONSERVICE IS NULL  OR QUOTE.MIGRATIONSERVICE ='NONE') AND QUOTE.SERVICENAME SER_INORLIKE (<PARAM1>) ))  "+
												//" AND ((QUOTE.MIGRATIONSERVICENAME <> 'NULL' AND QUOTE.MIGRATIONSERVICENAME SER_INORLIKE (<PARAM1>) )  OR  ( QUOTE.MIGRATIONSERVICENAME IS NULL  AND QUOTE.SERVICENAME SER_INORLIKE (<PARAM1>) )) "+
												" AND LINEITEM.DESCRIPTION LINE_INORLIKE (<PARAM2>)"+
												 SITE_OR_HOTCUT+
												"  ADD_ICO_FILTER "+
												" AND SITE.EQ_SITEOF = ORG.TRIL_GID"+
												" AND QUOTE.EQ_DELIVERYPARTY = ORG1.TRIL_GID"+
												" AND QUOTE.EQ_REQUESTINGPARTY= ORG2.TRIL_GID"+ 
												" AND ADDR.TRIL_GID = SITE.SITEADDRESS"+
												" AND QUOTE.CONFIGURATIONS = HR.TRIL_GID "+
												" AND <SEARCHWITH> IN( <PARAMS> ) "+
												" AND QUOTE.ARCHIVED <> '1'";
	
	private String   getServiceInfo = "SELECT SERVICE.DISP_NAME,SERVICE.SERVICE_ID,SERVICE.IS_CONFIGURABLE,SERVICE.ISSERVICE,SERVICE.IS_STANDALONE,SERVICE.ISACTIVE,SERVICE.ISASSOCIATEDSERVICE,SERVICE.PRACTICEDESCRIPTION,SERVICE.PRODUCTTYPE,SERVICE.PSID,SERVICE.OCATID FROM "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE WHERE SERVICE.DISP_NAME IN (SELECT  DISTINCT SERVICE_INT.DISP_NAME FROM "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE_INT WHERE SERVICE_INT.DISP_NAME <> 'NULL' ) ORDER BY SERVICE.DISP_NAME";
	
	private String   getServiceInfoForSites = "SELECT DISTINCT SERVICE.DISP_NAME,SERVICE.SERVICE_ID,SERVICE.IS_CONFIGURABLE,SERVICE.ISSERVICE,SERVICE.IS_STANDALONE,SERVICE.ISACTIVE,SERVICE.ISASSOCIATEDSERVICE,SERVICE.PRACTICEDESCRIPTION,SERVICE.PRODUCTTYPE,SERVICE.PSID,SERVICE.OCATID FROM "+ConnectionBean.getDbPrefix()+"EQ_SERVICE SERVICE,"+ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE ,"+ConnectionBean.getDbPrefix()+"EQ_SITE SITE WHERE ((QUOTE.HOTCUTNEWSITE <> 'NULL' AND QUOTE.HOTCUTNEWSITE =  SITE.TRIL_GID) OR (QUOTE.HOTCUTNEWSITE IS NULL AND QUOTE.SITE = SITE.TRIL_GID )) AND <SEARCHWITH> IN( <PARAMS> ) AND ((QUOTE.MIGRATIONSERVICE <> 'NULL' AND QUOTE.MIGRATIONSERVICE <> 'NONE' AND QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID ) OR  ( (QUOTE.MIGRATIONSERVICE IS NULL  OR QUOTE.MIGRATIONSERVICE ='NONE') AND QUOTE.ORDEREDSERVICE = SERVICE.SERVICE_ID )) ORDER BY SERVICE.DISP_NAME";
	
	private boolean hasArchived = false;
	
	private String[] LINEITEMSELECTS = {"DESCRIPTION","QUOTES COUNT","QUOTE NUMBERS"};
	private Map<String,ArrayList<String>> orderDetailsMap;
	
	private ArrayList<String> qlcriteria;
	private String[] SERVICESELECTS = {"SERVICE DISP NAME","SERVICE ID","IS CONFIGURABLE?","IS SERVICE?","IS STANDALONE?","IS ACTIVE?","IS ASSOCIATED SERVICE?","PRACTICE DESCRIPTION","PRODUCT TYPE","PSID","OCATID"};
	
	/*
	 * THESE PARAMS FOR INSERTING DATE RANGE IN QUERIES.
	 * 
	 * 1. ADD DATE RANGES IN GOLD 
	 * 
	 * 	AND MILES.EQ_TITLE='Order Raised Date' AND MILES.EQ_ORDERGID=QUOTE.TRIL_GID
	 * 
	 */
	
	
	
	
	private class LineItemOKEvent implements ActionListener
	{

		private ServiceBuildSelectorFrame lineitemForm;
		private MLANViewComponents mLANViewComponents;
		private ServiceBuildSelectorFrame serviceForm;
		private String servInfo;
		private String updatedPath;
		public LineItemOKEvent(ServiceBuildSelectorFrame serviceForm,ServiceBuildSelectorFrame lineitemForm,MLANViewComponents mLANViewComponents,String servInfo,String updatedPath){
			this.serviceForm =serviceForm;
			this.lineitemForm = lineitemForm;
			this.mLANViewComponents = mLANViewComponents;
			this.servInfo=servInfo;
			this.updatedPath = updatedPath;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			try
			{
			mLANViewComponents.getQueryResult().append("\nPlease wait..Process may take few moments.");
			/*
			 * 
			 * IF USER PUTS USER INPUT BY SELECTING  LIKE OPTION IN FIRST FORM , BUT IN SECOND FORM HE CHOOSE FROM CHECKBOX
			 */
			if(null != serviceForm.getUserServiceInfo() && serviceForm.getWithLIKE().isSelected())
			{
				getserviceBuildInfo = getserviceBuildInfo.replace("SER_INORLIKE", "LIKE");
				getserviceBuildInfo_INCLUDE_NOTIN = getserviceBuildInfo_INCLUDE_NOTIN.replace("SER_INORLIKE", "LIKE");
			}
			else
			{
				getserviceBuildInfo = getserviceBuildInfo.replace("SER_INORLIKE", "IN");
				getserviceBuildInfo_INCLUDE_NOTIN = getserviceBuildInfo_INCLUDE_NOTIN.replace("SER_INORLIKE", "IN");
			}
			
			
			if(null != lineitemForm.getUserServiceInfo() && lineitemForm.getWithLIKE().isSelected())
			{
			
				getserviceBuildInfo = getserviceBuildInfo.replace("LINE_INORLIKE", "LIKE");
				getserviceBuildInfo_INCLUDE_NOTIN = getserviceBuildInfo_INCLUDE_NOTIN.replace("LINE_INORLIKE", "NOT LIKE");
				
				getArchivedOrdersServiceBuildInfo = getArchivedOrdersServiceBuildInfo.replace("DES_INORLIKE", "LIKE");
				getArchivedOrdersServiceBuildInfo_INCLUDE_NOTIN = getArchivedOrdersServiceBuildInfo_INCLUDE_NOTIN.replace("DES_INORLIKE", "NOT LIKE");
			}
			else
			{
				getserviceBuildInfo = getserviceBuildInfo.replace("LINE_INORLIKE", "IN");
				getserviceBuildInfo_INCLUDE_NOTIN = getserviceBuildInfo_INCLUDE_NOTIN.replace("LINE_INORLIKE", "NOT IN");
				
				getArchivedOrdersServiceBuildInfo = getArchivedOrdersServiceBuildInfo.replace("DES_INORLIKE", "IN");
				getArchivedOrdersServiceBuildInfo_INCLUDE_NOTIN = getArchivedOrdersServiceBuildInfo_INCLUDE_NOTIN.replace("DES_INORLIKE", "NOT IN");
			}
			
			
			if(lineitemForm.getSelectedCount()>999 && null == lineitemForm.getUserServiceInfo())
			{
				JOptionPane.showMessageDialog(mLANViewComponents.getPanel(), "Selected Item ("+lineitemForm.getSelectedCount()+") \n More then 999 selection of lineitems are not allowed.\n Please reselect again.");
			}
			else if(lineitemForm.getSelectedCount() ==0 && null == lineitemForm.getUserServiceInfo())
			{
				JOptionPane.showMessageDialog(mLANViewComponents.getPanel(), "Please select at least one lineitem.");
			}
			else
			{
				
					final String lineItemInfo = ((null != lineitemForm.getUserServiceInfo() && lineitemForm.getUserServiceInfo().length()> 0)?lineitemForm.getUserServiceInfo() :lineitemForm.getSelectedServiceInfo());
					
					lineitemForm.getFrame().setVisible(false);
					
					getserviceBuildInfo = getserviceBuildInfo.replace("<PARAM1>", servInfo);
					getserviceBuildInfo = getserviceBuildInfo.replace("<PARAM2>", lineItemInfo);
					
					getserviceBuildInfo_INCLUDE_NOTIN = getserviceBuildInfo_INCLUDE_NOTIN.replace("<PARAM1>", servInfo);
					getserviceBuildInfo_INCLUDE_NOTIN = getserviceBuildInfo_INCLUDE_NOTIN.replace("<PARAM2>", lineItemInfo);
					
					
					final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
					new Thread(new Runnable(){public void run(){try{
					
					ArrayList<String[]> serviceBuildItems = CommonUtils.getQueryResult(getserviceBuildInfo, mLANViewComponents);
					qlcriteria.add("GOLD:"+getserviceBuildInfo);
					
					getArchivedOrdersServiceBuildInfo =getArchivedOrdersServiceBuildInfo.replace("<PARAM2>", lineItemInfo);
					getArchivedOrdersServiceBuildInfo_INCLUDE_NOTIN =getArchivedOrdersServiceBuildInfo_INCLUDE_NOTIN.replace("<PARAM2>", lineItemInfo);
					
					
					
					
						if(lineitemForm.getIncludeNotInOrders().isSelected())
						{
							ArrayList<String[]> serviceBuildItemsIncludeNotIn;
							
								serviceBuildItemsIncludeNotIn = CommonUtils.getQueryResult(getserviceBuildInfo_INCLUDE_NOTIN, mLANViewComponents);
								qlcriteria.add("GOLD:"+getserviceBuildInfo_INCLUDE_NOTIN);
								serviceBuildItems.addAll(serviceBuildItemsIncludeNotIn);	
							
							
							
						}
						
						
						if(mLANViewComponents.hasArchivedValidation())
						{
							ArrayList<String[]> archiveListOfLineItems = new ArrayList<String[]> ();
							// get list of all archived orders line items
							int count = 0;
							for(String[] order: archiveOrders)
							{
								count++;
								ProgressMonitorPane.getInstance().setProgress(count,(double)archiveOrders.size());
								String ORD = CommonUtils.addZeroPrefixinOrder(order[0]);
								ArrayList<String[]> archiveOrdersLineItems = CommonUtils.getArchiveQueryResult(getArchivedOrdersServiceBuildInfo, mLANViewComponents,ORD);
								
								if(!checkOnly.containsKey("DYN_ARCHIVE"))
								{
								// only one query is required.
									qlcriteria.add("DYN_ARCHIVE:"+getArchivedOrdersServiceBuildInfo);
									checkOnly.put("DYN_ARCHIVE", "");
								}
								
								String upquery = getArchivedOrdersServiceBuildInfo.replace("?", "'"+ORD+"'");
								qlcriteria.add("ARCHIVE:"+ORD+">"+upquery);
								
								
								for(String[] RR : archiveOrdersLineItems)
								{
									String[] ARR = new String[columnArray.length];
									ArrayList<String> mapData = orderDetailsMap.get(ORD) ;
										for(int x = 0 ; x< (ARR.length-7);x++)
										{
											
											ARR[x] = mapData.get(x);
										}
									ARR[ARR.length-6] = RR[1];
									ARR[ARR.length-5] = RR[2];
									ARR[ARR.length-4] = RR[3];
									ARR[ARR.length-3] = RR[4];
									ARR[ARR.length-2] = RR[5];
									ARR[ARR.length-1] = RR[6];
									archiveListOfLineItems.add(ARR);
								}
							}
							// add archive line item list to non archive line items
							serviceBuildItems.addAll(archiveListOfLineItems);
							/*
							 * END ARCHIVED CHECK BOX CHECKED
							 * 
							 * */	
						}
						
						if(mLANViewComponents.hasArchivedValidation() && lineitemForm.getIncludeNotInOrders().isSelected())
						{
							ArrayList<String[]> archiveListOfLineItems = new ArrayList<String[]> ();
							int count = 0;
							for(String[] order: archiveOrders)
							{
								count++;
								ProgressMonitorPane.getInstance().setProgress(count,(double)archiveOrders.size());
								String ORD = CommonUtils.addZeroPrefixinOrder(order[0]);
								ArrayList<String[]> archiveOrdersLineItems = CommonUtils.getArchiveQueryResult(getArchivedOrdersServiceBuildInfo_INCLUDE_NOTIN, mLANViewComponents,ORD);
								
								if(!checkOnly.containsKey("DYN_ARCHIVE_NOT_IN"))
								{
									// only one query is required.
									qlcriteria.add("DYN_ARCHIVE_NOT_IN:"+getArchivedOrdersServiceBuildInfo_INCLUDE_NOTIN);
									checkOnly.put("DYN_ARCHIVE_NOT_IN", "");
								}
								
								
								String upquery = getArchivedOrdersServiceBuildInfo_INCLUDE_NOTIN.replace("?", "'"+ORD+"'");
								qlcriteria.add("ARCHIVE:"+ORD+">"+upquery);
								
								for(String[] RR : archiveOrdersLineItems)
								{
									String[] ARR = new String[columnArray.length];
									ArrayList<String> mapData =orderDetailsMap.get(ORD) ;
										for(int x = 0 ; x< (ARR.length-6);x++)
										{
											ARR[x] = mapData.get(x);
										}
										ARR[ARR.length-6] = RR[1];
										ARR[ARR.length-5] = RR[2];
										ARR[ARR.length-4] = RR[3];
										ARR[ARR.length-3] = RR[4];
										ARR[ARR.length-2] = RR[5];
										ARR[ARR.length-1] = RR[6];
										//ARR[ARR.length-1] = RR[6];
										archiveListOfLineItems.add(ARR);
								}
							}
							// add archive line item list to non archive line items
							serviceBuildItems.addAll(archiveListOfLineItems);
							/*
							 * END ARCHIVED CHECK BOX CHECKED
							 * 
							 * */	
					 		
					}
					
					
					
					int randomNum = 999 + (int)(Math.random() * ((999999 - 999) + 1));
					String path=updatedPath+File.separator+"_"+randomNum+".csv";
					
					pwin.dispose();
					
					mLANViewComponents.getQueryResult().append("\nUPDATING PREVIOUS STATUS IF ONHOLD/UPDATING ARCHIVE ORDER INFORMATIONS..");
					CommonUtils.showTable(columnArray, updateOnHoldNArchiveInfo(serviceBuildItems,mLANViewComponents), path);
					
					mLANViewComponents.getQueryResult().append("\nStep Completed Successfully..for lineitem(s) >>"+lineItemInfo);
					mLANViewComponents.getQueryResult().append("\nFile export Successfully.."+path);
					
					saveSQLCriteria(mLANViewComponents,hasArchived);
				    CommonUtils.createConsoleLogFile(mLANViewComponents,updatedPath);
				    CommonUtils.formatFile(mLANViewComponents,path);
				   
					} catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,mLANViewComponents);}}}).start();
			 }
		}catch(Exception E){CommonUtils.showExceptionStack(E);}	
		}
		
	}
	private class ServiceOKEvent implements ActionListener{

		private MLANViewComponents mLANViewComponents;
		private Dimension screenSize;
		private ServiceBuildSelectorFrame serviceForm;
		private String updatedPath;
		public ServiceOKEvent(ServiceBuildSelectorFrame serviceForm,MLANViewComponents mLANViewComponents,Dimension screenSize,String updatedPath){
			this.serviceForm = serviceForm;
			this.mLANViewComponents = mLANViewComponents;
			this.screenSize = screenSize;
			this.updatedPath = updatedPath;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			
			qlcriteria = new ArrayList<String>();
			hasArchived = false;
			try
			{
				mLANViewComponents.getQueryResult().append("\nPlease wait..Process may take few moments.");
				
				if(null != serviceForm.getUserServiceInfo() && serviceForm.getWithLIKE().isSelected())
				{
					getGOLDLineItemsInfo = getGOLDLineItemsInfo.replace("SER_INORLIKE", "LIKE");
					getListOfArchivedOrders = getListOfArchivedOrders.replace("SER_INORLIKE", "LIKE");
				}
				else
				{
					getGOLDLineItemsInfo = getGOLDLineItemsInfo.replace("SER_INORLIKE", "IN");
					getListOfArchivedOrders = getListOfArchivedOrders.replace("SER_INORLIKE", "IN");
				}
				
				
				if(serviceForm.getSelectedCount()>999 && null == serviceForm.getUserServiceInfo())
				{
					JOptionPane.showMessageDialog(serviceForm.getFrame(), "Selected Item ("+serviceForm.getSelectedCount()+") \n More then 999 selection of services are not allowed.\n Please reselect again.");
				}
				else if(serviceForm.getSelectedCount() ==0 && null == serviceForm.getUserServiceInfo())
				{
					JOptionPane.showMessageDialog(serviceForm.getFrame(), "Please select at least one service.");
				}
				else
						{
						
						final String servInfo = ((null != serviceForm.getUserServiceInfo() && serviceForm.getUserServiceInfo().length()> 0)?serviceForm.getUserServiceInfo() :serviceForm.getSelectedServiceInfo());
						serviceForm.getFrame().setVisible(false);
						getGOLDLineItemsInfo = getGOLDLineItemsInfo.replace("<PARAM1>", servInfo);
						
						/*
						 * IF DATE RANGE IS SELECTED
						 * 
						 */
					   if(mLANViewComponents.hasSetDate())
					   {
						   if(null == mLANViewComponents.getExternalFromdatePicker().toString() 
								   || mLANViewComponents.getExternalFromdatePicker().toString().length()==0 ||
							  null == mLANViewComponents.getExternalTodatePicker().toString()
								   || mLANViewComponents.getExternalTodatePicker().toString().length() ==0)
						   {
							   mLANViewComponents.getSetDateCheckBox().setBackground(Color.ORANGE);
							   JOptionPane.showMessageDialog(mLANViewComponents.getPanel(),"Please Select the Date \n OR \n unchecked 'Select Date Range' Option.");
							   //mLANViewComponents.getSetDateCheckBox().setBackground(UIManager.getColor ( "Panel.background" ));
							   mLANViewComponents.getSetDateCheckBox().setBackground(UIManager.getColor ( "Panel.background" ));
							   return;
						   }
						  
						   ADD_DATE_RANGE_GOLD_SUFFIX = ADD_DATE_RANGE_GOLD_SUFFIX.replace("<DATE_RNG_FROM>", "'"+mLANViewComponents.getExternalFromdatePicker().toString().toUpperCase()+"'");
						   ADD_DATE_RANGE_GOLD_SUFFIX = ADD_DATE_RANGE_GOLD_SUFFIX.replace("<DATE_RNG_TO>", "'"+mLANViewComponents.getExternalTodatePicker().toString().toUpperCase()+"'");
						  
						   
						   getGOLDLineItemsInfo = getGOLDLineItemsInfo+ADD_DATE_RANGE_GOLD_SUFFIX;
						   
						
						   getserviceBuildInfo = getserviceBuildInfo+ADD_DATE_RANGE_GOLD_SUFFIX;
						   getserviceBuildInfo_INCLUDE_NOTIN = getserviceBuildInfo_INCLUDE_NOTIN+ADD_DATE_RANGE_GOLD_SUFFIX;
							
						
					   }
					   else
					   {
						   getGOLDLineItemsInfo = getGOLDLineItemsInfo.replace(ConnectionBean.getDbPrefix()+"EQMILESTONE MILES,", "");
						   //getGOLDLineItemsInfo = getGOLDLineItemsInfo.replace("ADD_DATE_RANGE_CONDITION", "");
					   }
					   
					   getGOLDLineItemsInfo+=" GROUP BY LINEITEM.DESCRIPTION ORDER BY LINEITEM.DESCRIPTION";
					   
					final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
					new Thread(new Runnable(){public void run(){try{
					ArrayList<String[]> lineItems = CommonUtils.getQueryResult(getGOLDLineItemsInfo, mLANViewComponents);
					
						
						
						/*
					    * IF ARCHIVED CHECK BOX IS CLICKED
					    * 
					    */
						
						if(mLANViewComponents.hasArchivedValidation())
						{
							hasArchived = true;
							getListOfArchivedOrders = getListOfArchivedOrders.replace("<PARAM1>", servInfo);
							// GET LIST OF ARCHIVED ORDERS
							ArrayList<String[]> unfilteredarchiveOrders = CommonUtils.getQueryResult(getListOfArchivedOrders, mLANViewComponents);
							
					
							if(mLANViewComponents.hasSetDate())
							{
								
							
								getArchivedOrderByDateRange =  getArchivedOrderByDateRange.replace("<DATE_RNG_FROM>", "'"+mLANViewComponents.getExternalFromdatePicker().toString().toUpperCase()+"'");
								getArchivedOrderByDateRange =  getArchivedOrderByDateRange.replace("<DATE_RNG_TO>", "'"+mLANViewComponents.getExternalTodatePicker().toString().toUpperCase()+"'");
						   
								mLANViewComponents.getQueryResult().append("\nImporting the Archive order(s) information.Please wait.\n");
								int count =0;
								for(String[] order : unfilteredarchiveOrders)
								{
									count++;
									ProgressMonitorPane.getInstance().setProgress(count,(double)unfilteredarchiveOrders.size());
									
								    ArrayList<String[]> archiveOrderDetails = CommonUtils.getArchiveQueryResult(getArchivedOrderByDateRange, mLANViewComponents,order[0]);
									if(null != archiveOrderDetails && archiveOrderDetails.size() !=0)
									{
										archiveOrders.add(order);
									}
								
								}
								unfilteredarchiveOrders = null;
								
									
									
							}
							// IF NO DATE RANGE SELECTED BY USER , SHOW ALL ORDERS
							else
							{
								getArchivedOrderByDateRange =  getArchivedOrderByDateRange.replace("AND MILES.EQ_ACTUALDATE BETWEEN <DATE_RNG_FROM> AND <DATE_RNG_TO>", "");
								archiveOrders = unfilteredarchiveOrders;
							}
							
							unfilteredarchiveOrders = null;
							
							if(!checkOnly.containsKey("DYN_ARCHIVE_ORDERS"))
							{
							// only one query is required.
							qlcriteria.add("DYN_ARCHIVE_ORDERS:"+getListOfArchivedOrders);
							checkOnly.put("DYN_ARCHIVE_ORDERS", "");
							}
		
						    if(!checkOnly.containsKey("DYN_ARCHIVE_ORDERS_DATE_RANGE"))
							{
							// only one time query is required.
							qlcriteria.add("DYN_ARCHIVE_ORDERS_DATE_RANGE:"+getArchivedOrderByDateRange);
							checkOnly.put("DYN_ARCHIVE_ORDERS_DATE_RANGE", "");
							
							}
						    
						
							// Add Order details in Map	
							for(String[] order: archiveOrders)
								{
									ArrayList<String> data = new ArrayList<String>();
									 for(String str: order)
									 {
										 data.add(str);
									 }
							orderDetailsMap.put(CommonUtils.addZeroPrefixinOrder(data.get(0)),data);
								}
							ArrayList<String[]> archiveListOfLineItems = new ArrayList<String[]> ();
							// get list of all archived orders line items
							mLANViewComponents.getQueryResult().append("\nCache the Archive order(s) information.Please wait.\n");
							int count = 0;
							for(String[] order: archiveOrders)
							{
								count++;
								ProgressMonitorPane.getInstance().setProgress(count,(double)archiveOrders.size());
								String ORD = CommonUtils.addZeroPrefixinOrder(order[0]);
								ArrayList<String[]> archiveLineItems = CommonUtils.getArchiveQueryResult(getListOfArchivedLineItemsInfo,mLANViewComponents,ORD);
								
								/*
								 * Insert only Non existing description as LineIrem Info
								 * 
								 * 
								 */
								ArrayList<String[]> updatedArray = new ArrayList<String[]>();
								for(String [] arr : archiveLineItems)
								{
									boolean isAvailable =false;
									String toCheck= arr[0];
									for(String [] ARR :archiveListOfLineItems)
									{
										if(toCheck.equalsIgnoreCase(ARR[0]))
										{
											isAvailable = true;
											break;
										}
									}
									if(!isAvailable)
									{
										String ar[] = new String[]{toCheck," Archived hence no Order(s) count available.",""};
										updatedArray.add(ar);
									}
								}
								archiveListOfLineItems.addAll(updatedArray);
							}
							
						
							
							// add archive line item list to non archive line items
							lineItems.addAll(archiveListOfLineItems);
							
							/*
							 * END ARCHIVED CHECK BOX CHECKED
							 * 
							 * */	
						}
						
						
						if(null != lineItems && lineItems.size()>0)
						{
							pwin.dispose();
							ServiceBuildSelectorFrame lineitemForm = new ServiceBuildSelectorFrame("LineItems ("+lineItems.size()+")"+servInfo,lineItems,LINEITEMSELECTS,screenSize,true);
							lineitemForm.getOkButton().addActionListener(new LineItemOKEvent(serviceForm,lineitemForm,mLANViewComponents,servInfo,updatedPath));
						}
						else
						{
							pwin.dispose();
							mLANViewComponents.getQueryResult().append("\nEnter Here 1234>>");
							JOptionPane.showMessageDialog(mLANViewComponents.getPanel(),"No LineItem(s) found..\nOR\nNo Order is linked with this service.\nPlease select other service(s).");
						}
						mLANViewComponents.getQueryResult().append("\nStep Completed Successfully..for service(s) >>"+servInfo);
						
				    } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,mLANViewComponents);}}}).start();	
				}
			}catch(Exception E){
				CommonUtils.printExceptionStack(E,mLANViewComponents);
			}
		}
		
	}
	
	
	
	private static ArrayList<String[]> getQuotePrevStateInfo(String quoteNumber,MLANViewComponents mLANViewComponents) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
		String SQLPREVSTATUS ="SELECT DISTINCT " +
		"ORDSTATUS.EQ_OLDSTATUS " +
		"FROM " +
		ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE, " +
		ConnectionBean.getDbPrefix()+"EQ_ORDERSTATUS ORDSTATUS " +
		"WHERE " +
		"ORDSTATUS.TRIL_GID = QUOTE.EQ_WFSTATUS " +
		"AND QUOTE.QUOTENUMBER = ?";
			
	ArrayList<String[]> quote = CommonUtils.getQueryResult(SQLPREVSTATUS, mLANViewComponents,quoteNumber);
	return quote;
	}
	
	
	
	public static ArrayList<String[]> updateOnHoldNArchiveInfo(ArrayList<String[]> serviceBuildItems ,MLANViewComponents mLANViewComponents) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
       ArrayList<String[]> updated = new ArrayList<String[]>();
       int count = 0;
       for(String[] arr : serviceBuildItems)
       {
    	   count++;
    	   ProgressMonitorPane.getInstance().setProgress(count,(double)serviceBuildItems.size());
    	   String ORD = CommonUtils.addZeroPrefixinOrder(arr[0]);
    	   
    	 
    	   if(null != arr[4] && arr[4].trim().equalsIgnoreCase("On-Hold"))
    	   {
    		   
    		   ArrayList<String[]> prevState =   getQuotePrevStateInfo(ORD,mLANViewComponents);
    		   arr[4] = "ON_HOLD( Prev was >> "+prevState.get(0)[0]+")";
    	   }
    	  
    	   /*
    	    * THIS COMMENT AS WILL BE FATCHED FROM QUERY ITSELF for non archived orders. 
    	    * FOR ARCHIVE WE NEED TO USE THIS AS  CommonUtils.CONSIDERABLE_VALUE  WILL NOT WORK WITH ARCHIVE QUERY.
    	    * 
    	    */
    	   if(arr[1].equalsIgnoreCase("Archived"))
    	   {
    	   arr = CommonUtils.considerableValuesForArchived(arr,mLANViewComponents);
    	   }
    	   updated.add(arr);
       }
       
		return updated;
	}
	
	
	
	public ServiceBuildInfo(MLANViewComponents mLANViewComponents,Dimension screenSize,String updatedPath,boolean isUSIDInfo,String params,String selectedItem) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		
		archiveOrders = new ArrayList<String[]>();
		checkOnly = new HashMap<String,String>();
		orderDetailsMap = new HashMap<String,ArrayList<String>>();
		qlcriteria = new ArrayList<String>();
		hasArchived = false;
		if(isUSIDInfo)
		{
			if(null == params || params.length() ==0)
			{
			return;	
			}
			
			String parts[] = params.trim().split(",");
			String S ="";
			for(String P:parts)
			{
			  P = CommonUtils.addZeroPrefixinOrder(P.trim());
			
			  S+="'"+P+"',";
			}
			
			S= S.substring(0,S.lastIndexOf(","));
			
			getServiceInfoForSites = getServiceInfoForSites.replace("<PARAMS>", S);
			getServiceInfoForSites = getServiceInfoForSites.replace("<SEARCHWITH>",selectedItem);
			
			
			getGOLDLineItemsInfoForSites = getGOLDLineItemsInfoForSites.replace("<PARAMS>", S);
			getGOLDLineItemsInfoForSites = getGOLDLineItemsInfoForSites.replace("<SEARCHWITH>",selectedItem);
			
			
			getListOfArchivedOrdersForSites = getListOfArchivedOrdersForSites.replace("<PARAMS>", S);
			getListOfArchivedOrdersForSites = getListOfArchivedOrdersForSites.replace("<SEARCHWITH>",selectedItem);
			
			getserviceBuildInfoForSites = getserviceBuildInfoForSites.replace("<PARAMS>", S);
			getserviceBuildInfoForSites = getserviceBuildInfoForSites.replace("<SEARCHWITH>",selectedItem);
			
			getserviceBuildInfo_INCLUDE_NOTIN_ForSites = getserviceBuildInfo_INCLUDE_NOTIN_ForSites.replace("<PARAMS>", S);
			getserviceBuildInfo_INCLUDE_NOTIN_ForSites = getserviceBuildInfo_INCLUDE_NOTIN_ForSites.replace("<SEARCHWITH>",selectedItem);
			
			
			getServiceInfo = getServiceInfoForSites;
			getGOLDLineItemsInfo = getGOLDLineItemsInfoForSites;
			getListOfArchivedOrders =getListOfArchivedOrdersForSites;
			getserviceBuildInfo = getserviceBuildInfoForSites;
			getserviceBuildInfo_INCLUDE_NOTIN = getserviceBuildInfo_INCLUDE_NOTIN_ForSites;
			
			
		}
		
		if(mLANViewComponents.getICOCheckBox().isSelected())
		{
			if(mLANViewComponents.getICOTextBox().getText().length()==0)
			{
				   mLANViewComponents.getICOCheckBox().setBackground(Color.ORANGE);
				   JOptionPane.showMessageDialog(mLANViewComponents.getPanel(),"Please insert the ICO(s).\n OR \nUnchecked 'ICO' Option.");
				   mLANViewComponents.getICOCheckBox().setBackground(UIManager.getColor ( "Panel.background" ));
				   return;
			}
			else
			{
				
				String parts[] = mLANViewComponents.getICOTextBox().getText().trim().split(",");
				String S ="";
				for(String P:parts)
				{
				  S+="'"+P+"',";
				}
				S= S.substring(0,S.lastIndexOf(","));
				
				
				ADD_ICO_FILTER = ADD_ICO_FILTER.replace("<IOCTEXT>",S);
				
				if(mLANViewComponents.getIsEndUser().isSelected())
				{
					ADD_ICO_FILTER = ADD_ICO_FILTER.replace("<PARTY>","EQ_DELIVERYPARTY");
				}
				else if (mLANViewComponents.getIsContractingParty().isSelected())
				{
					ADD_ICO_FILTER = ADD_ICO_FILTER.replace("<PARTY>","EQ_REQUESTINGPARTY");
				}
				
				
				getGOLDLineItemsInfo = getGOLDLineItemsInfo.replace("ADD_ICO_FILTER",ADD_ICO_FILTER);
				getListOfArchivedOrders =getListOfArchivedOrders.replace("ADD_ICO_FILTER",ADD_ICO_FILTER);
				getserviceBuildInfo =getserviceBuildInfo.replace("ADD_ICO_FILTER",ADD_ICO_FILTER);
				getserviceBuildInfo_INCLUDE_NOTIN=getserviceBuildInfo_INCLUDE_NOTIN.replace("ADD_ICO_FILTER",ADD_ICO_FILTER);
			
			}
		}
		else
		{
			ADD_ICO_FILTER="";	
			getGOLDLineItemsInfo = getGOLDLineItemsInfo.replace(ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORGANIZATION,","");
			getListOfArchivedOrders =getListOfArchivedOrders.replace(ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORGANIZATION,","");
			getserviceBuildInfo =getserviceBuildInfo.replace(ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORGANIZATION,","");
			getserviceBuildInfo_INCLUDE_NOTIN=getserviceBuildInfo_INCLUDE_NOTIN.replace(ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORGANIZATION,","");
		
			getGOLDLineItemsInfo = getGOLDLineItemsInfo.replace("ADD_ICO_FILTER",ADD_ICO_FILTER);
			getListOfArchivedOrders =getListOfArchivedOrders.replace("ADD_ICO_FILTER",ADD_ICO_FILTER);
			getserviceBuildInfo =getserviceBuildInfo.replace("ADD_ICO_FILTER",ADD_ICO_FILTER);
			getserviceBuildInfo_INCLUDE_NOTIN=getserviceBuildInfo_INCLUDE_NOTIN.replace("ADD_ICO_FILTER",ADD_ICO_FILTER);
			
		}
		
		mLANViewComponents.getQueryResult().append("\nPlease wait..Process may take few moments.");
		
		ArrayList<String[]> services = CommonUtils.getQueryResult(getServiceInfo, mLANViewComponents);
		
		if(null != services && services.size()>0)
		{
			ServiceBuildSelectorFrame serviceForm = new ServiceBuildSelectorFrame("Services ("+services.size()+")",services,SERVICESELECTS,screenSize,false);
			serviceForm.getOkButton().addActionListener(new ServiceOKEvent(serviceForm,mLANViewComponents,screenSize,updatedPath));
		}
		else
		{
			JOptionPane.showMessageDialog(mLANViewComponents.getPanel(),"No Service found... Please try with another criteris.");
		}
		
		mLANViewComponents.getQueryResult().append("\nStep Completed Successfully..");
	}
	
	private void saveCrtFile(JFileChooser fileChooser,MLANViewComponents mLANViewComponents,boolean hasArchived) throws IOException{
		String FN_PREFIX= ((null != fileChooser && null != fileChooser.getSelectedFile()) ? ""+fileChooser.getSelectedFile() : "TEMP"+Math.random());
		String FN = FN_PREFIX+".criteria";
    	FileWriter fw = new FileWriter(FN) ;
    	for(String line : qlcriteria)
    	{
    	fw.write(line);
    	fw.write("\n");
    	}
    	
    	if(hasArchived)
    	{
    	// save the Order details
    	fw.write("ARCHIVED_FILE:"+FN_PREFIX+".orders");	
    	CommonUtils.writeCSVFile(orderFileHeader,FN_PREFIX+".orders",archiveOrders);
    	}
    	fw.close();
    	mLANViewComponents.getQueryResult().append("\nSQL Creteria saved successfully..\n"+FN+"\n");
	}
	
	
	private void saveSQLCriteria(MLANViewComponents mLANViewComponents,boolean hasArchived) throws IOException{
		 int option = JOptionPane.showConfirmDialog(null, "Would you like to save queries criteria?");
		 JFileChooser fileChooser = null;
		 if(option == JOptionPane.YES_OPTION)
		    {
		    	fileChooser = new CustomJFileChooser();
		    	int returnValue = fileChooser.showSaveDialog(null);
				if (returnValue == CustomJFileChooser.APPROVE_OPTION) 
				{
					
					saveCrtFile(fileChooser,mLANViewComponents,hasArchived);
			    	
				}
			}
		 else{
			 saveCrtFile(fileChooser,mLANViewComponents,hasArchived); 
		 }
		 checkOnly = null;
	}
}
