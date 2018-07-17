package com.orange.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.RowFilter;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import au.com.bytecode.opencsv.CSVReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.L3.MLAN.ServiceBuildFileFormater;
import com.orange.ui.component.MLANViewComponents;
import com.orange.ui.component.custom.CustomColorTheme;
import com.orange.ui.component.custom.CustomJCheckBox;
import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.CustomJPanel;
import com.orange.ui.component.custom.CustomRadioButton;
import com.orange.ui.component.custom.Directories;
import com.orange.ui.component.custom.Icons;
import com.orange.ui.component.custom.MyMenuBar;
import com.orange.util.csm.ConnectionForArchived;
import com.orange.util.csm.ConnectionForCSI;
import com.orange.util.csm.ConnectionForGOLD;
import com.orange.util.csm.CustomJTable;
import com.orange.util.others.MemoryUsages;


/*
 * QUERT TO OPTIMISE
 * 
 *  SELECT distinct 
        QUOTE.QUOTENUMBER , 
        DECODE (QUOTE.ARCHIVED, '1', 'Archived', '0', 'Non_archive','COULD_NOT_DECODE') ARCHIVED, 
        DECODE (QUOTE.EQ_ORDERTYPE, '1', 'New', '2', 'Change','3','Disconnect','COULD_NOT_DECODE') ORDER_TYPE,  
        CASE  
        WHEN QUOTE.EQ_ORDERTYPE = '3' 
        THEN DECODE (DIS.EQ_TYPE,'0','Router Soft Change','1','Price Update','2','Other','3','Service Change','4','HOT CUT MOVE (RELOCATION)','5','Migration - Hot Cut/Relocation','6','Migration - Premium Cut (Parallel)','7','Migration - Soft Cut (Parallel)','8','NA(8)','9','Commercial Migration','10','Number Deactivation','11','Fast Track','COULD_NOT_DECODE')  
        WHEN QUOTE.EQ_ORDERTYPE = '2' 
        THEN DECODE (CHG.EQ_TYPE,'0','Router Soft Change','1','Price Update','2','Other','3','Service Change','4','HOT CUT MOVE (RELOCATION)','5','Migration - Hot Cut/Relocation','6','Migration - Premium Cut (Parallel)','7','Migration - Soft Cut (Parallel)','8','NA(8)','9','Commercial Migration','10','Number Deactivation','11','Fast Track','COULD_NOT_DECODE')  
        ELSE '' END AS TYPE, 
        
        ORDSTATUS.EQ_STATUS, 
        QUOTE.SERVICENAME,
        SERVICE.DISP_NAME AS MIGRATIONSERVICENAME,  
        QUOTE.EQ_GOLDORIGNB ,
        QUOTE.ASSOCIATEDORDERNB,  
        SITE.CORE_SITE_ID,
        SITE.ADDRESS_ID,
        SITE.ORANGE_SITENAME,
        SITE.SITECODE,
        SITE.STATUS,  
        ADDR.COUNTRY,
        ADDR.COUNTRY_CODE,ADDR.COUNTRY AS COUNTRY_CAT, 
        ORG.ORGANIZATIONID AS LOCALSITE_USER_ICO,
        ORG.NAME AS LOCALSITE_USER_NAME, 
        ORG1.ORGANIZATIONID AS END_USER_ICO,
        ORG1.NAME AS END_USER_NAME, 
        ORG2.ORGANIZATIONID AS CONTRACTING_PARTY_ICO,
        ORG2.NAME AS CONTRACTING_PARTY_NAME, 
        QUOTE.EQ_CUSTOMERNOTICE AS SALES_NOTIFICATIONDATE,
        MILES.EQ_CREATIONDATE AS CREATION_DATE, 
        LINEITEM.DESCRIPTION,
        LINEITEM.VALUE,
        LINEITEM.EXIST_CONFIG,
        LINEITEM.NEW_CONFIG , 
        CASE  
      WHEN LINEITEM.VALUE <> 'NULL' OR  LINEITEM.VALUE <> null 
      THEN LINEITEM.VALUE 
      WHEN (QUOTE.EQ_ORDERTYPE = '1' OR QUOTE.EQ_ORDERTYPE ='3') AND (LINEITEM.VALUE IS NULL OR  LINEITEM.VALUE IS null) 
      THEN LINEITEM.EXIST_CONFIG 
      WHEN (QUOTE.EQ_ORDERTYPE = '3' AND DIS.EQ_TYPE ='1' ) OR (QUOTE.EQ_ORDERTYPE = '2' AND CHG.EQ_TYPE ='1') 
      THEN LINEITEM.NEW_CONFIG 
      WHEN (QUOTE.EQ_ORDERTYPE = '3' AND DIS.EQ_TYPE  <> '1' ) OR (QUOTE.EQ_ORDERTYPE = '2' AND CHG.EQ_TYPE <> '1') 
      THEN LINEITEM.EXIST_CONFIG 
      ELSE 'NO RULE FOUND' END  AS CONSIDERABLE_VALUE 
        FROM 
        EQMILESTONE MILES,
        SC_QUOTE QUOTE LEFT JOIN EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID),
        SC_HIERARCHY HR LEFT JOIN EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID) ,
        SC_QUOTE_LINE_ITEM LINEITEM, 
        EQ_ORDERSTATUS ORDSTATUS,
        SC_ADDRESS ADDR,
        EQ_SITE SITE,
        SC_ORGANIZATION ORG,
        SC_ORGANIZATION ORG1,
        SC_ORGANIZATION ORG2  
        WHERE  
        QUOTE.TRIL_GID = LINEITEM.QUOTE 
        AND ORDSTATUS.TRIL_GID = QUOTE.EQ_WFSTATUS 
        ---------------------------------------------------
        -- THIS BELOW PART OF QUERY MAKES QUERY 100 TIMES SLOW
        ---------------------------------------------------
        AND 
        ( 
        ((QUOTE.MIGRATIONSERVICE IS NULL  OR QUOTE.MIGRATIONSERVICE ='NONE') AND QUOTE.SERVICENAME IN ('ACACIA') )
        OR
        ((QUOTE.MIGRATIONSERVICE <> 'NULL' AND QUOTE.MIGRATIONSERVICE <> 'NONE') AND SERVICE.DISP_NAME IN ('ACACIA'))
        )  
        ------------------------------------------------------
        --- ABOVE PART OF QUERY MAKES QUERY 100 TIMES SLOWER 
        ------------------------------------------------------
        --AND QUOTE.SERVICENAME IN ('ACACIA')
        AND LINEITEM.DESCRIPTION IN ('ACACIA') 
        AND ((QUOTE.HOTCUTNEWSITE <> 'NULL' AND QUOTE.HOTCUTNEWSITE =  SITE.TRIL_GID) OR (QUOTE.HOTCUTNEWSITE IS NULL AND QUOTE.SITE = SITE.TRIL_GID ))    
        AND SITE.EQ_SITEOF = ORG.TRIL_GID AND QUOTE.EQ_DELIVERYPARTY = ORG1.TRIL_GID 
        AND QUOTE.EQ_REQUESTINGPARTY= ORG2.TRIL_GID 
        AND ADDR.TRIL_GID = SITE.SITEADDRESS 
        AND MILES.EQ_ORDERGID=QUOTE.TRIL_GID 
        AND MILES.EQ_TITLE='Order Raised Date' 
        AND QUOTE.CONFIGURATIONS = HR.TRIL_GID  
        AND QUOTE.ARCHIVED <> '1';
 * 
 * 
 */

/*
 * ------------------------------------------------------------------------------
 * BASE QUERY , 
 * MITIGATE ORDER CHANGE TYPE CONCERN FOR NEW , CHANGE AND DISCONNECT ORDERS
 * ------------------------------------------------------------------------------
 * 
 *  SELECT DISTINCT 
        QUOTE.QUOTENUMBER,
        DECODE (QUOTE.EQ_ORDERTYPE, '1', 'New', '2', 'Change','3','Disconnect','COULD_NOT_DECODE') ORDER_TYPE,
        CASE 
        WHEN QUOTE.EQ_ORDERTYPE = '3' THEN DECODE (DIS.EQ_TYPE,'0','Router Soft Change','1','Price Update','2','Other','3','Service Change','4','HOT CUT MOVE (RELOCATION)','5','Migration - Hot Cut/Relocation','6','Migration - Premium Cut (Parallel)','7','Migration - Soft Cut (Parallel)','8','NA(8)','9','Commercial Migration','10','Number Deactivation','11','Fast Track','COULD_NOT_DECODE') 
        WHEN QUOTE.EQ_ORDERTYPE = '2' THEN DECODE (CHG.EQ_TYPE,'0','Router Soft Change','1','Price Update','2','Other','3','Service Change','4','HOT CUT MOVE (RELOCATION)','5','Migration - Hot Cut/Relocation','6','Migration - Premium Cut (Parallel)','7','Migration - Soft Cut (Parallel)','8','NA(8)','9','Commercial Migration','10','Number Deactivation','11','Fast Track','COULD_NOT_DECODE') 
        ELSE '' END AS TYPE,
        ORDSTATUS.EQ_STATUS,
        ORG.ORGANIZATIONID AS EQ_DELIVERYPARTY,
        QUOTE.SERVICENAME,
        SERVICE.DISP_NAME AS MIGRATIONSERVICENAME,
        SITE.SITECODE,
        ITEM.NEW_CONFIG,ITEM.EXIST_CONFIG, ITEM.VALUE FROM 
        GOLDDBA.SC_QUOTE QUOTE LEFT JOIN GOLDDBA.EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID),
        GOLDDBA.SC_QUOTE_LINE_ITEM ITEM ,
        GOLDDBA.SC_ORGANIZATION ORG ,
        GOLDDBA.EQ_SITE SITE ,
        GOLDDBA.EQ_ORDERSTATUS ORDSTATUS ,
        GOLDDBA.SC_HIERARCHY HR LEFT JOIN GOLDDBA.EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID)
        LEFT JOIN GOLDDBA.EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID)
        WHERE QUOTE.TRIL_GID = ITEM.QUOTE
        AND 
        (ITEM.NEW_CONFIG = 'GO008Z61CE' OR ITEM.EXIST_CONFIG = 'GO008Z61CE' OR ITEM.VALUE = 'GO008Z61CE')
        AND ORG.TRIL_GID = QUOTE.EQ_DELIVERYPARTY
        and ORDSTATUS.TRIL_GID = QUOTE.EQ_WFSTATUS 
			  AND QUOTE.CONFIGURATIONS = HR.TRIL_GID 
        AND ((QUOTE.HOTCUTNEWSITE <> 'NULL' AND QUOTE.HOTCUTNEWSITE =  SITE.TRIL_GID) OR (QUOTE.HOTCUTNEWSITE IS NULL AND QUOTE.SITE = SITE.TRIL_GID ));
        
 * 
 * 
 * 
 */

public class CommonUtils {
	
	static Logger logger = initLogger();
	
	public static String version="8.09";
	/*@ LALIT
	 * Force Update value can be META ,PATCH OR HOLD
	 * 
	 * META   		: if user is using previous version from this, then user will be forced to take update to continue.
	 * PATCH 		: if user is using previous version from this, then user can be used or continue with his existing version.
	 * HOLD_ALL  : UTILITY IS ON HOLD, NO ONE CAN USE FURTHER
	 * HOLD_FTID : UTILITY CAN BE HOLD FOR SPECIFIC USER(S) , BY PROVIDING THEIR FTID(S) WITH COMMA SEPERATED.
	 * This Token will be use below the version details of Imadaq.txt file -- 2nd line
	 * 
	 * Force  UPDATE    	: META_7.06 
	 * OPTIONAL UPDATE : PATCH_7.06 OR 7.06
	 * HOLD      				: HOLD_ALL
	 * HOLD_FTID 			: META_7.06,HOLD_NRP5678,HOLD_ADCD5423 OR PATCH_7.06,HOLD_NRP5678,HOLD_ADCD5423
	 */
	

	/*@ LALIT 07-JUNE 2018
	 * LIKE OPTION HAS NEEN DISABLED FROM THE UTILITY
	 * THIS ACTION HAS BEEN TAKEN TO OPTIMZE THE QUERY EXECUTUION.
	 * BY TRUE BELOW LINE , LIKE WILL BE ACTIVE AGAIN.
	 */
	/*
	 * do not change below identifier.
	 */
	public static boolean isLikeDisabled=true;
	
	
	private static Properties goldDBProperties,csiDBProperties,archivalDBProperties;
	public static String ARCHIVE_DECODE=" DECODE (QUOTE.ARCHIVED, '1', 'Archived', '0', 'Non_archive','COULD_NOT_DECODE') ARCHIVED,";
	public static String ORDER_TYPE_DECODE=" DECODE (QUOTE.EQ_ORDERTYPE, '1', 'New', '2', 'Change','3','Disconnect','COULD_NOT_DECODE') ORDER_TYPE,";
	public static String LINEITEM_UDSTATUS=" DECODE (LINEITEM.UDSTATUS, '0', 'EXISTING', '1', 'DELETED','2','NEW','COULD_NOT_DECODE') ELEMENT_SERVICEBUILD, ";
	public static String EQ_TYPE_DECODE=" " +
	" CASE " +
	" WHEN QUOTE.EQ_ORDERTYPE = '3' THEN DECODE (DIS.EQ_TYPE,'0','Router Soft Change','1','Price Update','2','Other','3','Service Change','4','HOT CUT MOVE (RELOCATION)','5','Migration - Hot Cut/Relocation','6','Migration - Premium Cut (Parallel)','7','Migration - Soft Cut (Parallel)','8','Migration/Relocation','9','Commercial Migration','10','Number Deactivation','11','Fast Track','12','BULK_UPDATE','COULD_NOT_DECODE') " +
	" WHEN QUOTE.EQ_ORDERTYPE = '2' THEN DECODE (CHG.EQ_TYPE,'0','Router Soft Change','1','Price Update','2','Other','3','Service Change','4','HOT CUT MOVE (RELOCATION)','5','Migration - Hot Cut/Relocation','6','Migration - Premium Cut (Parallel)','7','Migration - Soft Cut (Parallel)','8','Migration/Relocation','9','Commercial Migration','10','Number Deactivation','11','Fast Track','12','BULK_UPDATE','COULD_NOT_DECODE') " +
	" ELSE '' END AS TYPE,";
	
	
	public static String CONSIDERABLE_VALUE_PREV =" CASE  "+
     " WHEN LINEITEM.VALUE <> 'NULL' OR  LINEITEM.VALUE <> null " +
     " THEN LINEITEM.VALUE "+
     " WHEN (QUOTE.EQ_ORDERTYPE = '1' OR QUOTE.EQ_ORDERTYPE ='3') AND (LINEITEM.VALUE IS NULL OR  LINEITEM.VALUE IS null) " +
     " THEN LINEITEM.EXIST_CONFIG "+
     " WHEN (QUOTE.EQ_ORDERTYPE = '3' AND DIS.EQ_TYPE ='1' ) OR (QUOTE.EQ_ORDERTYPE = '2' AND CHG.EQ_TYPE ='1') " +
     " THEN LINEITEM.NEW_CONFIG "+
     " WHEN (QUOTE.EQ_ORDERTYPE = '3' AND DIS.EQ_TYPE  <> '1' ) OR (QUOTE.EQ_ORDERTYPE = '2' AND CHG.EQ_TYPE <> '1') " +
     " THEN LINEITEM.EXIST_CONFIG "+
     " ELSE 'NO RULE FOUND' END AS CONSIDERABLE_VALUE ";
	
	public static String CONSIDERABLE_VALUE_NEW =" CASE  "+
    " WHEN LINEITEM.VALUE <> 'NULL' OR  LINEITEM.VALUE <> null " +
    " THEN LINEITEM.VALUE "+
    " WHEN (QUOTE.EQ_ORDERTYPE = '1' OR QUOTE.EQ_ORDERTYPE ='3') AND (LINEITEM.VALUE IS NULL OR  LINEITEM.VALUE IS null) " +
    " THEN LINEITEM.EXIST_CONFIG "+
    " WHEN (QUOTE.EQ_ORDERTYPE = '2' AND CHG.EQ_TYPE = '3') " +
    " THEN LINEITEM.NEW_CONFIG "+
    " WHEN (QUOTE.EQ_ORDERTYPE = '2' AND CHG.EQ_TYPE <> '3') " +
    " THEN LINEITEM.EXIST_CONFIG "+
    " ELSE 'NO RULE FOUND' END AS CONSIDERABLE_VALUE ";

	public static String QUOTE_SERVICE_CONSIDERABLE_VALUE =" CASE  "+
    " WHEN QUOTE.MIGRATIONSERVICENAME <> 'NULL' THEN QUOTE.MIGRATIONSERVICENAME "+
    " ELSE QUOTE.SERVICENAME END AS QUOTE_EXISITING_SERVICE, ";
	
	
	private static String getChangeTypeSQL = "SELECT " +
	CommonUtils.EQ_TYPE_DECODE.substring(0,CommonUtils.EQ_TYPE_DECODE.lastIndexOf(","))+	
	" FROM " +
	ConnectionBean.getDbPrefix()+"SC_QUOTE  QUOTE, "+
	ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID) "+
	"WHERE " +
	"QUOTE.CONFIGURATIONS = HR.TRIL_GID " +
	"AND QUOTE.QUOTENUMBER =?";
	
	/*
	 * =====================================================================
	 * RULE TO EXTRACT VALUE,NEW_CONFIG,EXIST_CONFIG                    ===
	 * 
	 * IF VALUE EXISTS THIS WILL BE USED AND THEN BELOW REULS WILL BE 
	 * APPLICABLE.
	 * 
	 * IF VALUE IS NULL IN CASE OF NEW ORDER , 
	 * THEN NEW_CONFIG OR EXIST_CONFIG ANY CAN BE USE.
	 * ---------------------------------------------------------------------
	 * ORDER TYPE    	CHNAGE TYPE         		CONSIDERABLE         ===
	 * ---------------------------------------------------------------------
	 * 
	 * NEW              NA                			VALUE                ===
	 * DISCONNECT       ANY TYPE           			EXIST CONFIG         ===
	 * CHANGE           SERVICE CHANGE     			NEW CONFIG         ===
	 * CHANGE           OTHER THEN SERVICE CHANGE   EXISIT CONFIG           ===
	 * =====================================================================
	 */
	public static String[] considerableValuesForArchived(String[] arr,MLANViewComponents mLANViewComponents) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	
	{
		String ORD = arr[0];
		ArrayList<String[]> ORDER_CHANGE_TYPE = new ArrayList<String[]> ();
		 if(null != arr[26] && arr[26].length()>0)
			{
				arr[29] = arr[26];	
			}
			else if((arr[2].trim().equalsIgnoreCase("New")) && (null == arr[26] || arr[26].length()==0 ))
			{
					// EXIST_CONFIG OR NEW CONFIG ANY VALUE CAN BE PICK
					arr[29] = arr[27];
			}
			else if(arr[2].trim().equalsIgnoreCase("Disconnect"))
			{
				// EXISTING CONFIG
				arr[29] = arr[27];
			}
		 
			else if (arr[2].trim().equalsIgnoreCase("Change"))
			{
				ORDER_CHANGE_TYPE = CommonUtils.getQueryResult(getChangeTypeSQL, mLANViewComponents,ORD);
				
				if(null != ORDER_CHANGE_TYPE && ORDER_CHANGE_TYPE.size()> 0 && ORDER_CHANGE_TYPE.get(0)[0].trim().equalsIgnoreCase("Service Change"))
				{	//NEW CONFIG
				arr[29] = arr[28];
				}
				else
				{
					// EXISIT CONFIG
					arr[29] = arr[27];
				}
			}
			else 
			{
				// No RULE found
				arr[29] = "NO RULE FOUND.";
			}
		 arr[3] = (null == ORDER_CHANGE_TYPE || ORDER_CHANGE_TYPE.size() ==0 ? "Not available" :ORDER_CHANGE_TYPE.get(0)[0]);
	return arr;
	}
	
	
	public static String[] PPL_LIST = {"Business VPN Small Quick-Start","Business VPN Galerie","Mobile Threat Protection","Cisco Spark Subscription","Governance and Contract Execution","Supplementary Charges","Project Management Services","Consulting Services","Business VPN Small"};
	public static String[] PPL_LIST_OCAT_ID = {"10-GPD1607","10-GPD2362","10-GPD3625","10-GPD3686","10-GPD3695","10-GPD3716","10-GPD3718","10-GPD3721","10-GPD647"};
	
	public String CONSIDERABLE_VALUE_UNUSEDNOW ="CASE "+
	"WHEN LINEITEM.VALUE IS NOT NULL THEN LINEITEM.VALUE "+
	"WHEN LINEITEM.VALUE IS NULL AND QUOTE.EQ_ORDERTYPE = '2' AND CHG.EQ_TYPE = '1' THEN LINEITEM.EXIST_CONFIG "+
	"WHEN LINEITEM.VALUE IS NULL AND QUOTE.EQ_ORDERTYPE = '3' THEN LINEITEM.NEW_CONFIG "+
	"WHEN LINEITEM.VALUE IS NULL AND QUOTE.EQ_ORDERTYPE = '1' THEN LINEITEM.NEW_CONFIG "+
	"ELSE LINEITEM.NEW_CONFIG "+
	"END "+
	"AS CONSIDERABLE_VALUE";
	
	public static String getConsiderableValue(){
		
		Properties properties = CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV02);
		if(properties.getProperty("CONSIDERABLE_VALUE").equalsIgnoreCase("CONSIDERABLE_VALUE_NEW"))
		{
			return CONSIDERABLE_VALUE_NEW;
		}
		else
		{
			return CONSIDERABLE_VALUE_PREV;
		}
		
	}
	
	static class CollapseEvent implements ActionListener
	{
	    private GenericJTree GenericJTree;
	   
		public CollapseEvent(GenericJTree GenericJTree){
			this.GenericJTree = GenericJTree;
			
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			for (int i = 0; i < GenericJTree.getRowCount(); i++) {
				GenericJTree.collapseRow(i);
			}
			
			
		}
		
	}
	static class ExpendEvent implements ActionListener
	{
		 private GenericJTree GenericJTree;
		
	 public ExpendEvent(GenericJTree GenericJTree){
		 this.GenericJTree = GenericJTree;
		 
	 }
		@Override
		public void actionPerformed(ActionEvent arg0) {
			int	option = JOptionPane.showConfirmDialog(new Frame(), "This Process Make take bit longer time, \n Do you really want to Continue.?","Warning",JOptionPane.YES_NO_OPTION);
			if(option == JOptionPane.YES_OPTION)
			{
			for (int i = 0; i < GenericJTree.getRowCount(); i++) 
			{
				GenericJTree.expandRow(i);
				
			}
			
			
			}
			
		}
		
	}
	
	static class HideAll implements ActionListener{
	
		CustomJTable jtable;
		
		public HideAll(CustomJTable jtable){
			this.jtable = jtable;
			
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(null == jtable.getHideColumnsIndex() || jtable.getHideColumnsIndex().length ==0){
				return;
			}
			jtable.hideColumns(jtable.getHideColumnsIndex());
			
		}
		
	}
	

static  class PanelFilterEventClass implements KeyListener{

	private CustomRadioButton contains;
	private CustomRadioButton equals;
	private JTextField field;
	private JScrollPane panel;
	private JMenuItem search;
	private CustomRadioButton startswith;
	
	
	public PanelFilterEventClass(JMenuItem search,JTextField field,JScrollPane panel,CustomRadioButton equals,CustomRadioButton startswith,CustomRadioButton contains){
		this.field = field;
		this.panel = panel;
		this.equals = equals;
		this.startswith = startswith;
		this.contains = contains;
		this.search= search;
		
	
	}
	@Override
	public void keyPressed(KeyEvent arg0) {}

	@Override
	public void keyReleased(KeyEvent arg0) {
		
		 int count = 0;
		 search.setText("FILTER                     >>"+count);
		 JViewport view1 = panel.getViewport();
	     Component[] components1 =view1.getComponents();
	       
	        for (Component component:components1) 
	        {
	        	
	        	if (component instanceof JPanel) 
	            {
	        		JPanel P = (JPanel)component;
	        		Component[] cbcomponents =P.getComponents();
	        		for (Component cbcomponent:cbcomponents) 
	    	        {
	        	
	        			if (cbcomponent instanceof CustomJCheckBox) 
			            {
	        				CustomJCheckBox cb = (CustomJCheckBox)cbcomponent;
	        				cb.setBackground(UIManager.getColor ( "Panel.background" ));
	        		    }
	    	        }
	            }
	        }
		
		if(null != field.getText() && field.getText().length()>0 && !field.getText().isEmpty())
		{
		 JViewport view = panel.getViewport();
	     Component[] components =view.getComponents();
	       
	        for (Component component:components) 
	        {
	        	
	        	if (component instanceof JPanel) 
	            {
	        		JPanel P = (JPanel)component;
	        		Component[] cbcomponents =P.getComponents();
	        		for (Component cbcomponent:cbcomponents) 
	    	        {
	        	
	        			
			            if (cbcomponent instanceof CustomJCheckBox) 
			            {
			            	CustomJCheckBox cb = (CustomJCheckBox)cbcomponent;
			            	if(equals.isSelected())
			            	{
			            		
				            	if(cb.getText().trim().equalsIgnoreCase(field.getText()))
				            	{
				            		 cb.setBackground(Color.GREEN);
				            		 cb.scrollRectToVisible(cb.getBounds());
				            		 panel.getVerticalScrollBar().setValue(cb.getY());
				            		 panel.getHorizontalScrollBar().setValue(cb.getX());
				            		 count++;
				            		 search.setText("FILTER                     >>"+count);
				            	}
			            	}
			            	
			            	else if(startswith.isSelected())
			            	{
			            	
			            		
			            		if(cb.getText().length() >=field.getText().length() )
			            		{
			            		
			                    	if(cb.getText().substring(0,field.getText().length()).trim().toUpperCase().contains(field.getText().toUpperCase()))
					            	{
			                    		
			                    		 cb.setBackground(Color.ORANGE);
					            		 cb.scrollRectToVisible(cb.getBounds());
					            		 panel.getVerticalScrollBar().setValue(cb.getY());
					            		 panel.getHorizontalScrollBar().setValue(cb.getX());
					            		 field.setBackground(Color.ORANGE);
					            		 count++;
					            		 search.setText("FILTER                     >>"+count);
					            		 //break;
					            	}
			                    	else
			                    	{
			                    		if(count== 0)
			                    		{
			                    		field.setBackground(Color.red);
			                    		}
			                    	}
			            		}
			            		
			            		
			            	}
			            	else if(contains.isSelected())
			            	{
			            		
			            	
			            		if(cb.getText().length() >=field.getText().length() )
			            		{
			                    	if(cb.getText().toUpperCase().contains(field.getText().toUpperCase()))
					            	{
			                    		
			                    		 cb.setBackground(Color.ORANGE);
					            		 cb.scrollRectToVisible(cb.getBounds());
					            		 panel.getVerticalScrollBar().setValue(cb.getY());
					            		 panel.getHorizontalScrollBar().setValue(cb.getX());
					            		 field.setBackground(Color.ORANGE);
					            		 count++;
					            		 search.setText("FILTER                     >>"+count);
					            		 //break;
					            	}
			                    	else
			                    	{
			                    		if(count == 0)
			                    		{
			                    		field.setBackground(Color.red);
			                    		}
			                    	}
			            		}
			            	}
			            }
	    	        }
	        }
	      }
		}
		else
		{
			 field.setBackground(Color.ORANGE);
		}
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {}
	}


static class SearchEvent implements KeyListener
{
	private GenericJTree GenericJTree;
	private JTextField searchFiled;
	public SearchEvent(JTextField searchFiled,GenericJTree GenericJTree){
		this.searchFiled = searchFiled;
		this.GenericJTree = GenericJTree;
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		TreePath path = find((DefaultMutableTreeNode )GenericJTree.getModel().getRoot(),searchFiled.getText());
		GenericJTree.setSelectionPath(path);
		GenericJTree.scrollPathToVisible(path);
	}
	public void keyTyped(KeyEvent arg0) {
	}
	
}

static class ShowAll implements ActionListener{

	CustomJTable jtable;
	
	public ShowAll(CustomJTable jtable){
		this.jtable = jtable;
		
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(null == jtable.getHideColumnsIndex() || jtable.getHideColumnsIndex().length ==0){
			return;
		}
		jtable.showColumns(jtable.getHideColumnsIndex());
		
	}
	
}
	
static  class TableFilterEventClass implements KeyListener{

	private JTextField field;
	private JMenuItem serach;
	private CustomJTable table;
	public TableFilterEventClass(JTextField field,CustomJTable table,JMenuItem search){
		this.field = field;
		this.table = table;
		this.serach = search;
		
	}
	@Override
	public void keyPressed(KeyEvent arg0) {}

	@Override
	public void keyReleased(KeyEvent arg0) {
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(((DefaultTableModel) table.getTable().getModel())); 
		sorter.setRowFilter(RowFilter.regexFilter(field.getText()));
	    table.getTable().setRowSorter(sorter);
	    serach.setText("FILTER  >>("+table.getTable().getRowCount()+")");
	    serach.setForeground(Color.RED);
	}

	@Override
	public void keyTyped(KeyEvent arg0) {}
	
  }
	
public static String _refineData(String data) {

		CharSequence cs1 = "&";
		String tempData = data;

		if (data.contains(cs1) ) {
			tempData = tempData.replace("&", "'||'&'||'");
		}
		return tempData;
		}
	
	public static String addCSIScriptRow(String quoteNumber,String addressId,String coreSiteId,String replacementSitecode,CustomViewComponents viewComponents) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		String executionData;
	
		String csiSQL = "select ordhandle,sitehandle,coresiteid,addressid from "+ConnectionBeanCSI.getDbPrefix()+"cversion where ordhandle =?";
		ArrayList<String[]> csiSQLData = CommonUtils.getCSIQueryResult(csiSQL,viewComponents,quoteNumber);
		
			if(csiSQLData.size() >0)
			{
		
				if (CommonUtils.isNULL(coreSiteId)) 
				{
				   if(!csiSQLData.get(0)[1].equals(replacementSitecode))
				   {
					executionData = "Update "+ConnectionBeanCSI.getDbPrefix()+"cversion set Lupddate=sysdate,sitehandle='"+ replacementSitecode
							+ "' where ordhandle='"+ quoteNumber + "';";
					
				   }
				   else
				   {
					   executionData = "-- SITEHANDLE ALREADY UPDATED FOR ORDER >>"+quoteNumber +" AS " +replacementSitecode;
				   }
				
				} 
				else 
				{
					if(!CommonUtils.isNULL(csiSQLData.get(0)[2]))
					{
						if(!csiSQLData.get(0)[2].equals(coreSiteId) || !csiSQLData.get(0)[3].equals(addressId))
						{
						executionData = "Update "+ConnectionBeanCSI.getDbPrefix()+"cversion set Lupddate=sysdate,sitehandle='"	+ replacementSitecode+ "',coresiteid='"+ coreSiteId+ "',addressid='"+ addressId+ "' where ordhandle='" + quoteNumber + "';";
						
						}
					    else
					    {
					    	executionData = "-- COREIDS ALREADY UPDATED FOR ORDER >>"+quoteNumber +" AS CORESITEID " +coreSiteId +" AND ADDRESSID "+addressId;
						}
					}
					else
					{
						executionData = "Update "+ConnectionBeanCSI.getDbPrefix()+"cversion set Lupddate=sysdate,sitehandle='"	+ replacementSitecode+ "',coresiteid='"+ coreSiteId+ "',addressid='"+ addressId+ "' where ordhandle='" + quoteNumber + "';";
					}
				}
				
			
			}
			else
			{
				executionData = "-- QUOTE NOT FOUND IN CSI >>"+quoteNumber;
			}
			return executionData;
	}
	
	public static String addZeroPrefixinOrder(String quote)
	{
		String prefix="";
		if(null != quote && quote.length()>0)
		{
			
				if(quote.length()<6)
				{
					
					int len = quote.length();
					
					for(int x=len; x<6;x++)
					{
						prefix+="0";
					}
					//moveingQuotes.add(prefix+quote);
				}
				
				
		}
		return prefix+quote;
		}
		
		
	
	private static ArrayList<String> addZeroPrefixinOrders(ArrayList<String> proxymoveingQuotes)
	{
		ArrayList<String> moveingQuotes = new ArrayList<String>();
		
		if(proxymoveingQuotes.size()>0)
		{
			for(String order: proxymoveingQuotes)
			{
				if(order.length()<6)
				{
					
					int len = order.length();
					String prefix="";
					for(int x=len; x< 6;x++)
					{
						prefix+="0";
					}
					moveingQuotes.add(prefix+order);
				}
				else
				{
					moveingQuotes.add(order);
				}
				
			}
		}
		return moveingQuotes;
		
	}
	
		
	public static String autoUpDateUSID(String usid,String newSitecode){
	
		if(null != usid)
		{
		/* This for for some special cases where USID format is 
		 BNAZZZO5UGKEZ0UK5UZOJ23MJLWDIWYZ::35015::::B1UZZZUUUGKEZDWDT4ZZI21GWEKW1UZZ::0
		 */
		usid = usid.replace("::::", ":: ::");
			
		String newUSID=usid;
		int lastIndex = 0;
		int count = 0;
		String findStr="::";
		while(lastIndex != -1)
		{
		    lastIndex = usid.indexOf(findStr,lastIndex);
		    if(lastIndex != -1)
		    {
		        count ++;
		        lastIndex += findStr.length();
		    }
		}
	
		if(count >= 3)
		{
		int index = usid.indexOf("::", usid.indexOf("::") + 1);
		String sub = usid.substring(index+2);
		int nextIndexOf = sub.indexOf("::");
		String sitecods  = usid.substring(index+2,index+2+nextIndexOf);
		newUSID= usid.replace(sitecods, newSitecode);
		}
		return newUSID;
		}
		return null;
	}

	public static String autoUpDateUSIDandICO(String usid,String newSitecode,String newICO)
	{
		if(null != usid)
		{
		/* This for for some special cases where USID format is 
		 BNAZZZO5UGKEZ0UK5UZOJ23MJLWDIWYZ::35015::::B1UZZZUUUGKEZDWDT4ZZI21GWEKW1UZZ::0
		 */
		usid = usid.replace("::::", ":: ::");
			
		String newUSID=usid;
		int lastIndex = 0;
		int count = 0;
		String findStr="::";
		while(lastIndex != -1)
		{
		    lastIndex = usid.indexOf(findStr,lastIndex);
		    if(lastIndex != -1)
		    {
		        count ++;
		        lastIndex += findStr.length();
		    }
		}
	
		if(count >= 3)
		{
		int index = usid.indexOf("::", usid.indexOf("::") + 1);
		String sub = usid.substring(index+2);
		int nextIndexOf = sub.indexOf("::");
		String sitecods  = usid.substring(index+2,index+2+nextIndexOf);
		newUSID= usid.replace(sitecods, newSitecode);
		//System.out.println()
		
		int indexofICO = newUSID.indexOf("::", newUSID.indexOf("::"));
		String ico = newUSID.substring(indexofICO+2);
		int nextIndexOfICO1 = ico.indexOf("::");
		String oldICO  = newUSID.substring(indexofICO+2,indexofICO+2+nextIndexOfICO1);
		newUSID= newUSID.replace(oldICO, newICO);
		}
		return newUSID;
		}
		return null;
	}
	
	public static void checkHeap(JLabel heaplabel,Label statistics)
	{
		int mb = 1024*1024;
		Runtime runtime = Runtime.getRuntime();
		double upperRange = ((runtime.maxMemory() / mb));
		double UM = ((runtime.totalMemory() - runtime.freeMemory()) / mb);
		double FM = (runtime.freeMemory() / mb);
		String statsdetails = "Used Memory>>>: "+ UM +"(Mb) Free Memory: " + FM +"(Mb)";
		String fixDetails ="Total Memory:" + (runtime.totalMemory() / mb)+"(Mb) Max Memory:" + (runtime.maxMemory() / mb)+"(Mb)";
		if(UM > FM){
			statistics.setForeground(Color.red);	
		}
		else{
			statistics.setForeground(Color.blue);	
		}
		heaplabel.setText(fixDetails);
		statistics.setText(statsdetails);
		fixDetails = null;
		statsdetails = null;
		MemoryUsages.getInstance(upperRange).setMemVal(UM,FM);
		
		
		
	}
	
	private static void continiousThread(final JLabel heaplabel,final Label statistics ) {
		Executors.newSingleThreadExecutor().execute(new Runnable() {
			@Override
			public void run() {
	
				while (true) {
					try {
						Thread.sleep(1000);
						checkHeap(heaplabel,statistics);
					} catch (InterruptedException e) {}
				
				}
			}
		});
	
	}
	
	public static void createConsoleLogFile(CustomViewComponents viewComponent) throws FileNotFoundException {
		
		String path = viewComponent.getFileToValidate().getText();	
		int index = path.lastIndexOf(".");
		String sub = path.substring(0,index) ;
		String logPath=sub+"_LOGS_"+getFileName()+".xml";
		
			XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(logPath)));
			e.writeObject(viewComponent.getQueryResult());
			e.close();
			viewComponent.getQueryResult().append("\n Log File Created.."+logPath+"\n");
	
		
	}
	
	
	public static void createConsoleLogFile(CustomViewComponents viewComponent,String path) {
		String logPath=path+File.separator+"_LOGS_"+getFileName()+".xml";
		try {
			XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(logPath)));
			e.writeObject(viewComponent.getQueryResult());
			e.close();
			viewComponent.getQueryResult().append("\n Log File Created.."+logPath+"\n");
	
		} catch (FileNotFoundException e1) {
			JOptionPane.showMessageDialog(null,e1);
		}
	}
	
	public static String filterUSID(String USID){
		
		if(null != USID && !USID.isEmpty() && ( USID.contains(":") || USID.contains("::")))
		{
			
			if(USID.contains(":"))
			{
				int lastIndexOf = USID.lastIndexOf(":");
				USID = USID.substring(lastIndexOf+1);
			}
			else if(USID.contains("::"))
			{
				int lastIndexOf = USID.lastIndexOf("::");
				USID = USID.substring(lastIndexOf+2);
			}
			
		}
		
		
		
		return USID;
	}
	private static TreePath find(DefaultMutableTreeNode root, String s) {
	    @SuppressWarnings("unchecked")
	    Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
	    while (e.hasMoreElements()) {
	        DefaultMutableTreeNode node = e.nextElement();
	        if (node.toString().contains(s)) {
	            return new TreePath(node.getPath());
	        }
	    }
	    return null;
	}
	
	public static void formatFile(MLANViewComponents mLANViewComponents,String filePath) throws Exception{
		int option = JOptionPane.showConfirmDialog(new Frame(), "Do you want to Format this file.?");
		if(option == JOptionPane.YES_OPTION)
		{
			mLANViewComponents.getFileToValidate().setText(filePath);
			new ServiceBuildFileFormater(mLANViewComponents);
			mLANViewComponents.getFileToValidate().setText("");
		}
		
	}
	
	
	public static ArrayList<String[]> getArchiveQueryResult(String query, CustomViewComponents viewComponents) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		logger.info("["+Directories.UserName+"]"+query);
		String localQuery = query;
		Statement stmt = ConnectionForArchived.getStatement();
		if(viewComponents.getConnetionPane().getShowSQL().isSelected())
		{
		viewComponents.getQueryResult().append("\n"+localQuery+"\n");
		}
		ResultSet resultSet = stmt.executeQuery(query);
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
		stmt.close();
		localQuery = "";
		query="";
		logger.info("["+Directories.UserName+"] query ends.");
		return results;
	}
	


	public static ArrayList<String[]> getArchiveQueryResult(String query, CustomViewComponents viewComponents,String... params) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		logger.info("["+Directories.UserName+"]"+query);
		PreparedStatement pstmt = ConnectionForArchived.getPreparedStatement(query);
		String Params="";
		
		for(int X = 0 ; X< params.length;X++)
		{
		pstmt.setString(X+1, params[X]);
		Params+=params[X]+",";
		}
			if(viewComponents.getConnetionPane().getShowSQL().isSelected())
			{
				viewComponents.getQueryResult().append(query+"\n");
				viewComponents.getQueryResult().append("Params Change to >>"+Params+"\n");
			}
		
		ResultSet resultSet = pstmt.executeQuery();
		
		int columnCount = resultSet.getMetaData().getColumnCount();
		ArrayList<String[]> results = new ArrayList<String[]>();
		
		while(resultSet.next()) 
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
	
		
		query="";
		
		logger.info("["+Directories.UserName+"] query ends.");
		return results;

	}
	
	public static ArrayList<String[]> getArchiveQueryResultWithHeader(String query, CustomViewComponents viewComponents) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		logger.info("["+Directories.UserName+"]"+query);
		String localQuery = query;
		Statement stmt = ConnectionForArchived.getStatement();
		if(viewComponents.getConnetionPane().getShowSQL().isSelected()){
		viewComponents.getQueryResult().append("\n"+localQuery+"\n");
		}
		ResultSet resultSet = stmt.executeQuery(query);
		int columnCount = resultSet.getMetaData().getColumnCount();
		ArrayList<String[]> results = new ArrayList<String[]>();

		String columns[] = new String[columnCount];
		for(int x = 0 ; x<columnCount;x++ ){
			columns[x] = resultSet.getMetaData().getColumnName(x+1);
		}
		results.add(columns);
		
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
		stmt.close();
		localQuery = "";
		query="";
		logger.info("["+Directories.UserName+"] query ends.");
		return results;
	}
	
	private static String getCoreIDSQuery(String quotenumber,CustomViewComponents viewComponents) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException 
	{
		String query = "";
		String localQueryFromSite ="SELECT QUOTE.SITEDETAILS FROM "+ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE WHERE QUOTE.QUOTENUMBER='"+quotenumber+"'";
		
		Statement pstmt = ConnectionForGOLD.getStatement();
		String upquery = localQueryFromSite.replace("?", "'"+quotenumber +"'");
		viewComponents.getQueryResult().append(upquery+"\n");
		ResultSet resultSet = pstmt.executeQuery(localQueryFromSite);
		logger.info("["+Directories.UserName+"]"+localQueryFromSite);

		while(resultSet.next())
		{
			String SITEDETAILS = resultSet.getString(1);
			if(null != SITEDETAILS && SITEDETAILS.trim().equalsIgnoreCase("New"))
			{
				query =  "SELECT SITE.ADDRESS_ID,SITE.CORE_SITE_ID,ORG.ORGANIZATIONID,SITE.SITECODE,SITE.ORANGE_SITENAME FROM "+ConnectionBean.getDbPrefix()+"EQ_SITE SITE , "+ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG , "+ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE WHERE QUOTE.QUOTENUMBER=? AND ORG.TRIL_GID = SITE.EQ_SITEOF AND SITE.TRIL_GID = QUOTE.HOTCUTNEWSITE";
			}
			else
			{
				query =  "SELECT SITE.ADDRESS_ID,SITE.CORE_SITE_ID,ORG.ORGANIZATIONID,SITE.SITECODE,SITE.ORANGE_SITENAME FROM "+ConnectionBean.getDbPrefix()+"EQ_SITE SITE , "+ConnectionBean.getDbPrefix()+"SC_ORGANIZATION ORG , "+ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE WHERE QUOTE.QUOTENUMBER=? AND ORG.TRIL_GID = SITE.EQ_SITEOF AND SITE.TRIL_GID = QUOTE.SITE";
			}
		}
		resultSet.close();
		pstmt.close();
		localQueryFromSite = null;
		
		return query;
	}
	
	public static ArrayList<String[]> getCoreSiteDetailsFromOrder(String order,CustomViewComponents viewComponents) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		String localQueryFromSite = CommonUtils.getCoreIDSQuery(order, viewComponents);
		PreparedStatement pstmt = ConnectionForGOLD.getPreparedStatement(localQueryFromSite);
		pstmt.setString(1, order);
		String upquery = localQueryFromSite.replace("?", "'"+order +"'");
		
		viewComponents.getQueryResult().append(upquery+"\n");
		
		ResultSet resultSet = pstmt.executeQuery();
		
		ArrayList<String[]> coreIds = new ArrayList<String[]>();
		
		while(resultSet.next())
		{
			String arr[] = new String[5];
			arr[0] = resultSet.getString(1);
			arr[1] = resultSet.getString(2);
			arr[2] = resultSet.getString(3);
			arr[3] = resultSet.getString(4);
			arr[4] = resultSet.getString(5);
			coreIds.add(arr);
		}
		resultSet.close();
		pstmt.close();
		upquery = null;
		return coreIds;
	}
	
	public static ArrayList<String[]> getCSIQueryResult(String query, CustomViewComponents viewComponents) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		logger.info("["+Directories.UserName+"]"+query);
		String localQuery = query;
		Statement stmt = ConnectionForCSI.getStatement();
		if(viewComponents.getConnetionPane().getShowSQL().isSelected()){
		viewComponents.getQueryResult().append("\n"+localQuery+"\n");
		}
		ResultSet resultSet = stmt.executeQuery(query);
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
		stmt.close();
		localQuery = "";
		query="";
		logger.info("["+Directories.UserName+"] query ends.");
		return results;
	}
	
	public static ArrayList<String[]> getCSIQueryResult(String query, CustomViewComponents viewComponents,String... params) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {

		logger.info("["+Directories.UserName+"]"+query);
		String localQuery = query;
		PreparedStatement pstmt = ConnectionForCSI.getPreparedStatement(localQuery);
		
		String Params="";
		
		for(int X = 0 ; X< params.length;X++)
		{
		pstmt.setString(X+1, params[X]);
		Params+=params[X]+",";
		}
			if(viewComponents.getConnetionPane().getShowSQL().isSelected())
			{
				viewComponents.getQueryResult().append(query+"\n");
				viewComponents.getQueryResult().append("Params Change to >>"+Params+"\n");
			}
			
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
		query="";
		logger.info("["+Directories.UserName+"] query ends.");
		return results;

	}
	
		
	public static ArrayList<String[]> getCSIQueryResultWithHeader(String query, CustomViewComponents viewComponents) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		logger.info("["+Directories.UserName+"]"+query);
		String localQuery = query;
		Statement stmt = ConnectionForCSI.getStatement();
		if(viewComponents.getConnetionPane().getShowSQL().isSelected()){
		viewComponents.getQueryResult().append("\n"+localQuery+"\n");
		}
		ResultSet resultSet = stmt.executeQuery(query);
		int columnCount = resultSet.getMetaData().getColumnCount();
		ArrayList<String[]> results = new ArrayList<String[]>();

		String columns[] = new String[columnCount];
		for(int x = 0 ; x<columnCount;x++ ){
			columns[x] = resultSet.getMetaData().getColumnName(x+1);
		}
		results.add(columns);
		
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
		stmt.close();
		localQuery = "";
		query="";
		logger.info("["+Directories.UserName+"] query ends.");
		return results;
	}
	
	public static  ArrayList<String[]> getCSIServiceElementIds(String param,CustomViewComponents viewComponents) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
	
		String custInfo = "select Distinct CUSTHANDLE,SITEHANDLE from "+ConnectionBeanCSI.getDbPrefix()+"cversion where ordhandle=?";
		ArrayList<String[]> custINFO =  getCSIQueryResult(custInfo,viewComponents,param);
		
		String usdiddatasql = "Select Distinct t3.SERVICEELEMENTID, t3.USID " +
		"From "+ConnectionBeanCSI.getDbPrefix()+"Cversion T1 , "+ConnectionBeanCSI.getDbPrefix()+"Cversionserviceelement T2,"+ConnectionBeanCSI.getDbPrefix()+"CSERVICEELEMENT t3 " +
		"Where   T1.Custhandle=? " +
		"AND T1.Sitehandle=? " +
		"AND T2.Versionid=T1.Versionid AND t3.Serviceelementid=t2.Serviceelementid ";
		
		
		ArrayList<String[]> serviceElementsIDS =  new ArrayList<String[]>();
		if(null != custINFO && custINFO.size()>0)
		{
			String csthandle = custINFO.get(0)[0];
			String sitehandle = custINFO.get(0)[1];
			serviceElementsIDS = getCSIQueryResult(usdiddatasql,viewComponents,csthandle,sitehandle);
		}
		return serviceElementsIDS;

	}
	
	
public static String getFileName(){
		    Date today = Calendar.getInstance().getTime();
		    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-ss");
		    String fileName = formatter.format(today);
		    return fileName;
		}

public static ImageIcon getIcon(String iconPath){
	URL url = MyMenuBar.class.getResource(iconPath);
	ImageIcon img = new ImageIcon(url);
	return new ImageIcon(((img.getImage()).getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)));
	
}

public static  StringBuilder getIterate(Iterator<String> iterator,String commonQLData, String commonQLDataTail) {
		StringBuilder stringBuilder = new StringBuilder(commonQLData);
		int count = 0;
		while (iterator.hasNext()) 
		{
			if (count <= 999) {
				String data = iterator.next();
				stringBuilder.append("'" + data + "',");
			} else 
			{
				break;
			}
			count++;
		}
		// Remove Last Comma value from the String..
		stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		stringBuilder.append(commonQLDataTail);
		return stringBuilder;
	}
public static  StringBuilder getIterateAndZeroPrefix(Iterator<String> iterator,
		String commonQLData, String commonQLDataTail) {
	StringBuilder stringBuilder = new StringBuilder(commonQLData);
	int count = 0;
	while (iterator.hasNext()) {
		if (count <= 999) {
			String data = iterator.next();
			data = addZeroPrefixinOrder(data);
			stringBuilder.append("'" + data + "',");
		} else {
			break;
		}
		count++;
	}
	// Remove Last Comma value from the String..
	stringBuilder.deleteCharAt(stringBuilder.length() - 1);
	stringBuilder.append(commonQLDataTail);
	return stringBuilder;
}
/*
 * 
 * START >>> CHECK HEAP MEMORY FEATURES
 * 
 */
public static JPanel getMemoryMonitorPane(int lwidth){	
	
	JLabel heaplabel = new JLabel("##### Heap utilization statistics [MB] #####");
	Label statistics = new Label();
	
	heaplabel.setBounds(10,15,lwidth-12,20);
	statistics.setBounds(10,35,lwidth-15,25);
		
	GenericTitledBorder titled = new GenericTitledBorder("Memory Monitor");
	JPanel panel = new CustomJPanel();
	panel.setLayout(null);
	panel.setBorder(titled);
	
	panel.add(heaplabel);
	panel.add(statistics);
	continiousThread(heaplabel,statistics);
	Properties properties = CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV02);
	if(properties.getProperty("MemoryMonitor").equalsIgnoreCase("YES"))
	{
		panel.setVisible(true);
	}
	else{
		panel.setVisible(false);
	}
	return panel;
}

public static JPopupMenu getPopup(GenericJTree GenericJTree){
	   
	   final JPopupMenu popup = new JPopupMenu();
	   JMenuItem expend = new JMenuItem("Expend All");
	   JMenuItem collapse = new JMenuItem("Collapse All");
	   JMenuItem search = new JMenuItem("Search >>");
		JTextField searchFiled = new JTextField();
		popup.add(expend);
		popup.add(collapse);
		popup.add(search);
		popup.add(searchFiled);
		expend.addActionListener(new ExpendEvent(GenericJTree));
		collapse.addActionListener(new CollapseEvent(GenericJTree));
		searchFiled.addKeyListener(new SearchEvent(searchFiled,GenericJTree));
		return popup;
}

public static Window getProgressBar(int MAXVALUE,final String message,final boolean isContinue)
{
	final int MAX = MAXVALUE;
    final Window window = new Window(null);
  
    // creates progress bar
    final JProgressBar pb = new JProgressBar();
    pb.setMinimum(0);
    pb.setMaximum(MAX);
  
    pb.setString(message);
    pb.setStringPainted(true);
    pb.setVisible(true);
    pb.setBackground(Color.BLACK);
    pb.setForeground(CustomColorTheme.toolBarColor);
    // add progress bar
    window.setLayout(new BorderLayout());
    window.add(pb);
    window.setSize(200, 70);
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    window.setLocation(dim.width/2-window.getSize().width/2, dim.height/2-window.getSize().height/2);
    
  
    window.setAlwaysOnTop(true);
    window.setVisible(true);
    
    new Thread(new Runnable()
	        {
	          public void run()
	          {
	            try
	            {  
	            	while(true){
	            	Thread.sleep(500);
	            	int randomNum = ThreadLocalRandom.current().nextInt(0, 150 + 1);
	            	pb.setValue(randomNum);
	            	}
	            }
	            catch ( Throwable th )
	            {
	            }
	          }
	        }).start();
     return window;
	}
public static ArrayList<String[]> getQueryResult(String query, CustomViewComponents viewComponents) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
	logger.info("["+Directories.UserName+"]"+query);
	String localQuery = query;
	Statement stmt = ConnectionForGOLD.getStatement();
	if(viewComponents.getConnetionPane().getShowSQL().isSelected()){
	viewComponents.getQueryResult().append("\n"+localQuery+"\n");
	}
	ResultSet resultSet = stmt.executeQuery(query);
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
	stmt.close();
	localQuery = "";
	query="";
	logger.info("["+Directories.UserName+"] query ends.");
	return results;
}


public static ArrayList<String[]> getQueryResult(String query, CustomViewComponents viewComponents,String... params) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {

	logger.info("["+Directories.UserName+"]"+query);
	PreparedStatement pstmt = ConnectionForGOLD.getPreparedStatement(query);
	
	String Params="";
	
	for(int X = 0 ; X< params.length;X++)
	{
	pstmt.setString(X+1, params[X]);
	Params+=params[X]+",";
	}
		if(viewComponents.getConnetionPane().getShowSQL().isSelected())
		{
			viewComponents.getQueryResult().append(query+"\n");
			viewComponents.getQueryResult().append("Params Change to >>"+Params+"\n");
		}
	
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
	query="";
	logger.info("["+Directories.UserName+"] query ends.");
	return results;

}
public static ArrayList<String[]> getQueryHeader(String query, CustomViewComponents viewComponents,String... params) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {

	logger.info("["+Directories.UserName+"]"+query);
	PreparedStatement pstmt = ConnectionForGOLD.getPreparedStatement(query);
	String Params="";
	for(int X = 0 ; X< params.length;X++)
	{
		pstmt.setString(X+1, params[X]);
		Params+=params[X]+",";
	}
	if(viewComponents.getConnetionPane().getShowSQL().isSelected())
		{
			viewComponents.getQueryResult().append("\n"+query+"\n");
			viewComponents.getQueryResult().append("Params Change to >>"+Params+"\n");
		}
	
	ResultSet resultSet = pstmt.executeQuery();
	int columnCount = resultSet.getMetaData().getColumnCount();
	ArrayList<String[]> results = new ArrayList<String[]>();
	
	String columns[] = new String[columnCount];
	for(int x = 0 ; x<columnCount;x++ ){
		columns[x] = resultSet.getMetaData().getColumnName(x+1);
	}
	results.add(columns);
	logger.info("["+Directories.UserName+"] query ends.");
	return results;
}

public static ArrayList<String[]> getQueryHeader(String query, CustomViewComponents viewComponents) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
	logger.info("["+Directories.UserName+"]"+query);
	String localQuery = query;
	Statement stmt = ConnectionForGOLD.getStatement();
	if(viewComponents.getConnetionPane().getShowSQL().isSelected()){
	viewComponents.getQueryResult().append("\n"+localQuery+"\n");
	}
	
	ResultSet resultSet = stmt.executeQuery(query);
	int columnCount = resultSet.getMetaData().getColumnCount();
	ArrayList<String[]> results = new ArrayList<String[]>();
	
	String columns[] = new String[columnCount];
	for(int x = 0 ; x<columnCount;x++ ){
		columns[x] = resultSet.getMetaData().getColumnName(x+1);
	}
	results.add(columns);
	logger.info("["+Directories.UserName+"] query ends.");
	return results;
}
public static ArrayList<String[]> getQueryResultWithHeader(String query, CustomViewComponents viewComponents,String... params) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
	logger.info("["+Directories.UserName+"]"+query);
	String localQuery = query;
	
	PreparedStatement pstmt = ConnectionForGOLD.getPreparedStatement(localQuery);
	
	String Params="";
	
	for(int X = 0 ; X< params.length;X++)
	{
	pstmt.setString(X+1, params[X]);
	Params+=params[X]+",";
	}
		if(viewComponents.getConnetionPane().getShowSQL().isSelected())
		{
			viewComponents.getQueryResult().append(query+"\n");
			viewComponents.getQueryResult().append("Params Change to >>"+Params+"\n");
		}
		
	ResultSet resultSet = pstmt.executeQuery();
	int columnCount = resultSet.getMetaData().getColumnCount();
	ArrayList<String[]> results = new ArrayList<String[]>();
	
	String columns[] = new String[columnCount];
	for(int x = 0 ; x<columnCount;x++ )
	{
		columns[x] = resultSet.getMetaData().getColumnName(x+1);
	}
	results.add(columns);
	
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
	localQuery = "";
	query="";
	logger.info("["+Directories.UserName+"] query ends.");
	return results;
}

public static ArrayList<String[]> getCSIQueryResultWithHeader(String query, CustomViewComponents viewComponents,String... params) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
	logger.info("["+Directories.UserName+"]"+query);
	String localQuery = query;
	
	PreparedStatement pstmt = ConnectionForCSI.getPreparedStatement(localQuery);
	
	String Params="";
	
	for(int X = 0 ; X< params.length;X++)
	{
	pstmt.setString(X+1, params[X]);
	Params+=params[X]+",";
	}
		if(viewComponents.getConnetionPane().getShowSQL().isSelected())
		{
			viewComponents.getQueryResult().append(query+"\n");
			viewComponents.getQueryResult().append("Params Change to >>"+Params+"\n");
		}
		
	ResultSet resultSet = pstmt.executeQuery();
	int columnCount = resultSet.getMetaData().getColumnCount();
	ArrayList<String[]> results = new ArrayList<String[]>();
	
	String columns[] = new String[columnCount];
	for(int x = 0 ; x<columnCount;x++ )
	{
		columns[x] = resultSet.getMetaData().getColumnName(x+1);
	}
	results.add(columns);
	
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
	localQuery = "";
	query="";
	logger.info("["+Directories.UserName+"] query ends.");
	return results;
}

public static ArrayList<String[]> getQueryResultWithHeader(String query, CustomViewComponents viewComponents) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
	logger.info("["+Directories.UserName+"]"+query);
	String localQuery = query;
	Statement stmt = ConnectionForGOLD.getStatement();
	if(viewComponents.getConnetionPane().getShowSQL().isSelected()){
	viewComponents.getQueryResult().append("\n"+localQuery+"\n");
	}
	
	ResultSet resultSet = stmt.executeQuery(query);
	int columnCount = resultSet.getMetaData().getColumnCount();
	ArrayList<String[]> results = new ArrayList<String[]>();
	
	String columns[] = new String[columnCount];
	for(int x = 0 ; x<columnCount;x++ ){
		columns[x] = resultSet.getMetaData().getColumnName(x+1);
	}
	results.add(columns);
	
	
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
	stmt.close();
	localQuery = "";
	query="";
	logger.info("["+Directories.UserName+"] query ends.");
	return results;
}

public static  String getTimeStampFileName(){
    Date today = Calendar.getInstance().getTime();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_MM_SS");
    String fileName = formatter.format(today);
    return fileName;
}

public static  String getDateFormat(){
    Date today = Calendar.getInstance().getTime();
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
    String fileName = formatter.format(today);
    return fileName;
}


public static ArrayList<String> getUSIDElements(String USID)
{
	
	ArrayList<String> elements = null;
		if(null != USID)
		{
		/* This for for some special cases where USID format is 
		 BNAZZZO5UGKEZ0UK5UZOJ23MJLWDIWYZ::35015::::B1UZZZUUUGKEZDWDT4ZZI21GWEKW1UZZ::0
		 */
		USID = USID.replace("::::", ":: ::");
			
		String element [] = USID.split("::");
		elements = new ArrayList<String>(Arrays.asList(element));  
		return elements; 
		}
	elements = new ArrayList<String>();
	elements.add(USID);
	return elements;
}
public static  boolean isAlreadyUpDateUSID(String usid, String newSitecode) {
		
		if (null != usid) 
		{
			usid = usid.replace("::::", ":: ::");
			String newUSID = usid;
			int lastIndex = 0;
			int count = 0;
			String findStr = "::";
			while (lastIndex != -1) {
				lastIndex = newUSID.indexOf(findStr, lastIndex);
				if (lastIndex != -1) {
					count++;
					lastIndex += findStr.length();
				}
			}

			if (count >= 3) {
				int index = usid.indexOf("::", usid.indexOf("::") + 1);
				String sub = usid.substring(index+2);
				int nextIndexOf = sub.indexOf("::");
				String sitecods  = usid.substring(index+2,index+2+nextIndexOf);
				
				if(sitecods.equalsIgnoreCase(newSitecode))
				{
					return true;	
				}
				
			}
			
		}
		return false;
	}

public static boolean isFileExists(String filePath){
	 File file = new File(filePath);
	    if(!file.exists()){
	    	JOptionPane.showMessageDialog(null,"File / Path is not a valid path. Not exists in the System.");
	    	return false;
	    }
	    return true;
	   
}

public static boolean isNULL(String string) {

	boolean isNull = true;
	if (null != string && !string.isEmpty() && string.length() > 0) {
		isNull = false;
	}
	return isNull;
}

public static boolean isSpecialChars(String data)
{
	if(data.contains("'") || data.contains("&") || data.contains(";") )
	{
		return true;
	}
	return false;
}

public static ArrayList<String[]> moveQuotes(ArrayList<String[]> allQuotes,ArrayList<String> moveingQuotes){
	ArrayList<String[]> moveQuotes = new ArrayList<String[]>();
	moveingQuotes = addZeroPrefixinOrders(moveingQuotes);
	if(moveingQuotes.size()>0)
	{
		for(String[] orders : allQuotes)
		{
				String order = orders[0];
				
				for(String moveingQuote :moveingQuotes)
				{
					if(moveingQuote.equals(order))
					{
						moveQuotes.add(orders);
					}
				}
		}
	}
	else
	{
		moveQuotes = allQuotes;
	}
	
	
	return moveQuotes;
}

public static String printExceptionStack(Exception E,CustomViewComponents customViewComponents){
	 StringWriter writer = new StringWriter();
	 E.printStackTrace( new PrintWriter(writer,true ));
	 customViewComponents.getQueryResult().append("Exeption stack is :\n"+writer.toString());
	 return writer.toString();
}

public static void showExceptionStack(Exception E){
	 StringWriter writer = new StringWriter();
	 E.printStackTrace( new PrintWriter(writer,true ));
	 
	 JOptionPane opt = new JOptionPane("<html><body><font color='red' size='3'>"+ writer.toString().replaceAll("\n", "<br><font color='blue' size='2'>")+"</font><BR><BR><font color='red' size='2'>Will close automatically in 5 seconds.</font></body></html>", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}); // no buttons
     opt.setIcon(CommonUtils.getIcon(Icons.iconPath8));
     final JDialog dlg = opt.createDialog("Information");
     new Thread(new Runnable()
     {
       public void run()
       {
         try
         {
           Thread.sleep(5000);
           dlg.dispose();
         }
         catch ( Throwable th )
         {
         }
       }
     }).start();
     dlg.setAlwaysOnTop(true);
	dlg.setVisible(true);
	
	 
}


	public static String refineData(String data) 
	{
	
		CharSequence cs1 = "''";
		CharSequence cs2 = "'||'&'||'";
		String tempData = data;
	
		if (data.contains(cs1) && !data.contains(cs2)) 
		{
			tempData = tempData.replace("&", "'||'&'||'");
		}
		if (!data.contains(cs1) && !data.contains(cs2)) 
		{
			tempData = tempData.replace("'", "''");
			tempData = tempData.replace("&", "'||'&'||'");
			
		}
	
	return tempData;
	}


public static String removeSpecialChars(String data)
{
	String temp = data;
	if(temp.contains("'"))
	{
		temp = temp.replaceAll("'", "");
	}
	if(temp.contains("&"))
	{
		temp = temp.replaceAll("&", "");
	}
	if(temp.contains(";"))
	{
		temp = temp.replaceAll(";", "-");
	}
	int initChar = temp.charAt(0);
	if(initChar == '-'){
		temp = temp.replaceFirst("-", "");
	}
	if(temp.length()>37){
		temp = temp.substring(0,37);
	}
		return temp+"_SC";
}
public static JPopupMenu setPanelPopup(JScrollPane panel)
{
    final JPopupMenu popup = new JPopupMenu();
    JMenuItem info = new JMenuItem("Right click again to clean the filter.");
    JMenuItem search = new JMenuItem("FILTER                     >>");
   
    CustomRadioButton equals = new CustomRadioButton("EQUAL");
    CustomRadioButton startswith = new CustomRadioButton("STARTS WITH",true);
    CustomRadioButton contains = new CustomRadioButton("CONTAINS",false);
    ButtonGroup bg = new ButtonGroup();
    bg.add(equals);
    bg.add(startswith);
    bg.add(contains);
    
	
	
	final JTextField searchFiled = new JTextField("SEARCH");
	
	
	popup.add(info);
	popup.add(search);
	popup.add(equals);
	popup.add(startswith);
	popup.add(contains);
	
	popup.add(searchFiled);
	searchFiled.setBackground(Color.ORANGE);
	searchFiled.addKeyListener(new PanelFilterEventClass(search,searchFiled,panel,equals,startswith,contains));
	searchFiled.setText("");
 	return popup;
}
public static Image setSizeImage(String path,int wid, int height)
{
	   ImageIcon icon =  new ImageIcon(CommonUtils.class.getResource(path));
	   Image img = icon.getImage() ;  
	   Image newimg = img.getScaledInstance( wid, height,  java.awt.Image.SCALE_SMOOTH ) ; 
	   return newimg;
}

public static JPopupMenu setTablePopup(CustomJTable jTable)

{
	    final JPopupMenu popup = new JPopupMenu();
	    JMenuItem info = new JMenuItem("Right click again to clean the filter.");
	    
	    JMenuItem showall = new JMenuItem("SHOW ALL COLUMNS (Click Twice)");
	    JMenuItem hide = new JMenuItem("HIDE COLUMNS AGAIN");
	  
	    JMenuItem search = new JMenuItem("FILTER                     >>");
		final JTextField searchFiled = new JTextField("SEARCH");
	
		
		
		popup.add(info);
		popup.add(showall);
		popup.add(hide);
		popup.add(search);
		popup.add(searchFiled);
		searchFiled.setBackground(Color.ORANGE);
		searchFiled.addKeyListener(new TableFilterEventClass(searchFiled,jTable,search));
		showall.addActionListener(new ShowAll((CustomJTable)jTable));
		hide.addActionListener(new HideAll((CustomJTable)jTable));
		searchFiled.setText("");
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(((DefaultTableModel) jTable.getTable().getModel())); 
		sorter.setRowFilter(RowFilter.regexFilter(searchFiled.getText()));
		jTable.getTable().setRowSorter(sorter);
	 	return popup;
}

public static ArrayList<String> setUmlaut(ArrayList<String> sqls)
{
	//Charset utf8charset = Charset.forName("UTF-8");
    //Charset iso88591charset = Charset.forName("ISO-8859-1");
    
	
	ArrayList<String> allSQLSWithUmlaut = new ArrayList<String>();
	ArrayList<String> careUmlaut = new ArrayList<String>();
	ArrayList<String> filterUmlaut = new ArrayList<String>(sqls);
	
	String[] chars ={"Â","À","Å","Ã","â","à","å","ã","Ä","ä","Ç","ç","É","Ê","È","Ë","é","ê","è","ë","Ó","Ô","Ò","Õ","Ø","ó","ô","ò","õ","Ö","ö","Š","š","ß","Ú","Û","Ù",
			  "ú","û","ù","Ü","ü","Ý","Ÿ","ý","ÿ","Ž","ž"};
	
	
	allSQLSWithUmlaut.add("--***************************************************************************");
	allSQLSWithUmlaut.add("--Please Check the SQL scripts as this may have some unexpected chars.");
	allSQLSWithUmlaut.add("--Which may not have been auto corrected.");
	allSQLSWithUmlaut.add("--Like '  & ; etc ...");
	allSQLSWithUmlaut.add("--***************************************************************************");
	
	String spoolStart = "spool LOG_"+getTimeStampFileName()+".txt;";
	allSQLSWithUmlaut.add(spoolStart);
	
	
	for(String sql : sqls)
	{
		String updatedSQL=sql;
		boolean isUmlaut = false;
		for(int x = 0 ; x<chars.length; x++ )
		{
			if(sql.contains(chars[x]))
			{
				//updatedSQL = updatedSQL.replaceAll(chars[x],"'||'"+chars[x]+"'||'");
				//System.out.println("Yes germen char found "+chars[x]);
				isUmlaut = true;
				break;
			}
		}
		if(isUmlaut)
		{
			
			careUmlaut.add(updatedSQL);
			//careUmlaut.add(sql);
			filterUmlaut.remove(sql);
		}
		
	}

	for(String withoutUmlaut : filterUmlaut)
	{
		if(!withoutUmlaut.contentEquals("commit;") && !withoutUmlaut.contentEquals("Commit;"))
		{
			allSQLSWithUmlaut.add(withoutUmlaut);
		}
		
	}
	
	if(careUmlaut.size()>0)
	{
	  allSQLSWithUmlaut.add("--***************************************************************");	
	  allSQLSWithUmlaut.add("--CHANGE SESSION LANGUAGE = German TO HANDLE GERMEN CHARS");
	  allSQLSWithUmlaut.add("--ALTER SESSION SET NLS_LANGUAGE = German;");
	  allSQLSWithUmlaut.add("--***************************************************************");	
	  for(String umlautSQL : careUmlaut)
	  {
		  allSQLSWithUmlaut.add(umlautSQL);
	  }
	  allSQLSWithUmlaut.add("--ENDS CHANGE SESSION LANGUAGE = German TO HANDLE GERMEN CHARS");
	}
	filterUmlaut = null;
	careUmlaut = null;
	
	allSQLSWithUmlaut.add("commit;");
	String spoolEnd = "spool off;";
	allSQLSWithUmlaut.add(spoolEnd);
	return allSQLSWithUmlaut;
}


public static void showOnTable(String resultFileLoc) throws IOException{
	Object[] columnnames;
	CSVReader reader = new CSVReader(new FileReader(resultFileLoc));
	List<String[]> myEntries = reader.readAll();
	columnnames = (String[]) myEntries.get(0);
	DefaultTableModel tableModel = new DefaultTableModel(columnnames,myEntries.size() - 1);
	int rowcount = tableModel.getRowCount();
	for (int x = 0; x < rowcount + 1; x++) 
	{
		int columnnumber = 0;
	
		if (x > 0) 
		{
			for (String thiscellvalue : (String[]) myEntries.get(x)) 
			{
				tableModel.setValueAt(thiscellvalue, x - 1, columnnumber);
				columnnumber++;
			}
		}

	}
	reader.close();
	CustomJTable MyJTable = new CustomJTable();
	MyJTable.getTable().setModel(tableModel);
	MyJTable.getTable().setAutoCreateRowSorter(true);
	JScrollPane scroll = new JScrollPane(MyJTable.getTable());
	CustomJFrame f = new CustomJFrame("Sample Data..("+rowcount+")",Icons.helpIcon);
	f.setBounds(200, 150,200,400);
	f.add(scroll);
	f.setVisible(true);
	f.pack();
}

public static void showTable(String[] header,ArrayList<String[]> data,String resultFileLoc) throws IOException{
	
	
	CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(resultFileLoc),true);
	DefaultTableModel model = new DefaultTableModel(0, 0);
	
	writer.writeNext(header);
	model.setColumnIdentifiers(header);
	
	for (String [] row : data)
	{
			model.addRow(row);
			writer.writeNext(row);
	}
	writer.close();
	
	CustomJTable table = new CustomJTable();
	table.getTable().setModel(model);
	table.getTable().setAutoCreateRowSorter(true);
	JScrollPane scroll = new JScrollPane(table.getTable());
	CustomJFrame f = new CustomJFrame("Total Rows : "+data.size(),Icons.helpIcon);
	f.setBounds(200,150,500,400);
	f.add(scroll);
	f.setVisible(true);
	f.pack();
	
}

public static void writeCSVFile(String header[],String path,ArrayList<String[]> serviceBuildItems) throws IOException{
	CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(path),true);
	writer.writeNext(header);
	for (int row = 0; row < serviceBuildItems.size(); row++) 
	{
		writer.writeNext(serviceBuildItems.get(row));
	}
	writer.close();
}



/*
 * 
 * ENDS >>> CHECK HEAP MEMORY FEATURES
 * 
 */



public static boolean isJSONValid(String jsonfileName,CustomViewComponents customViewComponents ) throws IOException 
{
	FileInputStream inputStream = null;
	Scanner sc = null;
	StringBuilder sb;
	String SS;
	ObjectMapper mapper;
	
	try {
    	
    	inputStream = new FileInputStream(jsonfileName);
		sc = new Scanner(inputStream, "UTF-8");
		
		sb = new StringBuilder();
		while (sc.hasNextLine()) 
		    {
			sb.append(sc.nextLine());
		    }
		
	   SS = sb.toString();	
       mapper = new ObjectMapper();
       mapper.readTree(SS);
       
       inputStream.close();
       SS = null;
       mapper = null;
       sc.close();
       
       return true;
    } catch (IOException e) {
    	
    	   if(null != inputStream)
    	   {
    		   inputStream.close();
    	   }
    	   if(null != sc)
    	   {
    		   sc.close(); 
    	   }
	       SS = null;
	      mapper = null;
	      String stack = CommonUtils.printExceptionStack(e, customViewComponents);
	      String text = "<html><font color = 'red' size='3'>"+jsonfileName +" validation failed..<br>"+stack;
	      JOptionPane.showMessageDialog(null, text);
	      return false;
    }
}

/*
 * 
 * UPDATE EQL2InstanceMapping TABLE.
 * NEWLY ADDED ON 21-FEB-2018
 * 
 */
public static ArrayList<String>  updateEQL2InstanceMapping(ArrayList<String> goldExecutionScript,CustomViewComponents viewComponents,String replacesitecode,String replacementsitecode,String orderId) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
{
	if(null== replacesitecode || replacesitecode.length()==0 || null == replacementsitecode || replacementsitecode.length() ==0)
	{
		goldExecutionScript.add("--update EQL2InstanceMapping for sitecode "+replacesitecode +" to "+replacementsitecode);
		goldExecutionScript.add("--update EQL2InstanceMapping for sitecode failed for empty sitdecode");
		goldExecutionScript.add("--End update EQL2InstanceMapping for sitecode "+replacesitecode +" to "+replacementsitecode);	
		return goldExecutionScript;
	}
	
	// FOR FIND ORDERS 
	goldExecutionScript.add("--update EQL2InstanceMapping from sitecode "+replacesitecode +" to "+replacementsitecode +" FOR ORDER "+orderId);	
	
	/*
	 * String TRIL_GIDsSQL = "SELECT TRIL_GID FROM "+ConnectionBean.getDbPrefix()+"EQL2INSTANCEMAPPING WHERE Regexp_Substr (INSTANCEID, '[^::]+', 1, '3')='"+ replacesitecode + "' AND ORDERID ='"+orderId+"'";
	 * 
	*/
		
	String TRIL_GIDsSQL = "SELECT TRIL_GID FROM "+ConnectionBean.getDbPrefix()+"EQL2INSTANCEMAPPING WHERE ORDERID ='"+orderId+"'";
	
	/*
	 * IF ORDERID IS NULL , IT MEANS UTILITY WILL REPLACE ALL FOUND INSTANCES FROM THE TABLE. 
	 * AND WILL NOT BE ORPDER SPACIFIC
	 * IN THIS UTILITY THIS IS BEING USED IN SiteCodeswithSpecialChars CLASS.
	 * 
	 */
	
	if(null ==orderId)
	{
		TRIL_GIDsSQL = "SELECT TRIL_GID FROM "+ConnectionBean.getDbPrefix()+"EQL2INSTANCEMAPPING WHERE Regexp_Substr (INSTANCEID, '[^::]+', 1, '3')='"+ replacesitecode + "'";
	}
	
	ArrayList<String[]> TRIL_GIDs = CommonUtils.getQueryResult(TRIL_GIDsSQL, viewComponents);
	
	if(null !=TRIL_GIDs && TRIL_GIDs.size()>0)
	{
		for(String[] TRILGID:TRIL_GIDs)
		{
		goldExecutionScript.add("--update EQL2InstanceMapping  InstanceId from sitecode "+replacesitecode +" to "+replacementsitecode +" for order "+orderId);
		String sqlQuery = "UPDATE "+ConnectionBean.getDbPrefix()+"EQL2INSTANCEMAPPING SET ModificationDate=sysdate,INSTANCEID=REPLACE(INSTANCEID,'"
				+ replacesitecode
				+ "','"
				+ replacementsitecode
				+ "') WHERE TRIL_GID ='"+TRILGID[0]+"';";
				goldExecutionScript.add(sqlQuery);	
		}
	}
	else
	{
		goldExecutionScript.add("--No InstanceId found in EQL2InstanceMapping for ORDER "+orderId);	
	}
	goldExecutionScript.add("--End update EQL2InstanceMapping from sitecode "+replacesitecode +" to "+replacementsitecode +" for order "+orderId);	
    return  goldExecutionScript;
}

public static Properties loadFTPConfigProp(String propFilePath)
{
	Properties prop = new Properties();
	try{
		
		InputStream input = null;
		input = new FileInputStream(propFilePath);
		prop.load(input);
		input.close();
		
	}catch(Exception e){}
	return prop;
}



public static void writeCustomConfigProp(boolean deleteIfExists) throws IOException
{
	Properties properties = new Properties();
	
	properties.setProperty("lookNFeel", "com.jtattoo.plaf.mint.MintLookAndFeel");
	properties.setProperty("TableScroll", "YES");
	properties.setProperty("DefaultBrowseLocation", Directories.BASEDIR);
	properties.setProperty("MemoryMonitor", "YES");
	properties.setProperty("ProgressMonitor", "YES");
	properties.setProperty("MemoryPulse", "YES");
	properties.setProperty("VersionUpdateAlert", "YES");
	properties.setProperty("CONSIDERABLE_VALUE", "CONSIDERABLE_VALUE_NEW");
	properties.setProperty("MAX_ROW_RECORDS", "50");
	properties.setProperty("Theme", "STANDARD");
	
	
	File file1 = new File(Directories.customUserConfigFileLocationOLD);
	if(file1.exists())
	{
		file1.delete();
		file1 = null;
	}
	file1 = new File(Directories.customUserConfigFileLocation);
	if(file1.exists())
	{
		file1.delete();
		file1 = null;
	}
	
	File file = new File(Directories.customUserConfigFileLocationV02);
	if(!file.exists() || deleteIfExists)
	{
	FileOutputStream fileOut = new FileOutputStream(file);
	properties.store(fileOut, "FTP SERVER SETTINGS");
	fileOut.close();	

	System.out.println("====================================================================================================");
	System.out.println("Default Custom User Configuration file "+file.getAbsolutePath() +" has been created/modified for your reference.\nPlease update as per your requirement.");
	System.out.println("====================================================================================================");
	}
	else
	{
		System.out.println("====================================================================================================");
		System.out.println("Default Custom User Configuration file "+file.getAbsolutePath() +" exists.\nDelete file and restart the application to get default file.");
		System.out.println("====================================================================================================");
	}
}


public static void writeELKConfigProp(boolean deleteIfExists) throws IOException
{
	
	
	Properties properties = new Properties();
	properties.setProperty("KIBANA_HOME", "");
	properties.setProperty("ELASTICSEARCH_HOME", "");
	properties.setProperty("LOGSTASH_HOME", "");
	properties.setProperty("LOGSTASH_CONFIG", "");
	properties.setProperty("PREFIX_COMMAND", "");

	File file = new File(Directories.elkConfigFileLocation);
	if(!file.exists() || deleteIfExists)
	{
	FileOutputStream fileOut = new FileOutputStream(file);
	properties.store(fileOut, "ELK SERVER SETTINGS");
	fileOut.close();	

	System.out.println("====================================================================================================");
	System.out.println("Default ELK Configuration file "+file.getAbsolutePath() +" has been created/modified for your reference.\nPlease update as per your requirement.");
	System.out.println("====================================================================================================");
	}
	else
	{
		System.out.println("====================================================================================================");
		System.out.println("Default ELK Configuration file "+file.getAbsolutePath() +" exists.\nDelete file and restart the application to get default file.");
		System.out.println("====================================================================================================");
	}
}

public static boolean checkLeadingTrailingWhiteSpace(String input){
		return input.trim().equals(input) ? true: false;
	
}


public static Properties getGoldDBProperties() {
	return goldDBProperties;
}

public static void setGoldDBProperties(Properties goldDBProperties) {
	CommonUtils.goldDBProperties = goldDBProperties;
}

public static Properties getCsiDBProperties() {
	return csiDBProperties;
}

public static void setCsiDBProperties(Properties csiDBProperties) {
	CommonUtils.csiDBProperties = csiDBProperties;
}

public static Properties getArchivalDBProperties() {
	return archivalDBProperties;
}

public static void setArchivalDBProperties(Properties archivalDBProperties) {
	CommonUtils.archivalDBProperties = archivalDBProperties;
}


public static int getMaxRecords()
{
	Properties properties = CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV02);
	return Integer.parseInt(properties.getProperty("MAX_ROW_RECORDS"));
	
	
}

public static JScrollPane getEditorPane(String text){
	JEditorPane jEditorPane = new JEditorPane();
	jEditorPane.setEditable(false);
	HTMLEditorKit kit = new HTMLEditorKit();
	jEditorPane.setEditorKit(kit);

	jEditorPane.setText(text);
	
	
	JScrollPane scroll = new JScrollPane(jEditorPane);
	jEditorPane.setCaretPosition(0);
	return scroll;
	
}

private static Logger  initLogger(){
	 Logger logger = Logger.getLogger(CommonUtils.class.getName());
	
	 String log4jConfigFile =Directories.customUserLOGS;
	 PropertyConfigurator.configure(log4jConfigFile);
	 return logger;
}
}