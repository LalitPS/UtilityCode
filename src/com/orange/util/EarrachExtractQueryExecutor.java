package com.orange.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import com.orange.ui.component.EarrachExtractionView;
import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.Icons;
import com.orange.util.csm.CustomJTable;

public class EarrachExtractQueryExecutor {

	private String[] column1={"EQ_CIB_PREV_VERSION_ID","EQ_CIB_STABLE_ID","EQ_CIB_VERSION_ID","DESCRIPTION","OBJECTGID"};
	private String[] column2={"QUOTENUMBER" ,"MODIFICATIONDATE","EQ_GOLDORIGNB","TRIL_GID" ,"LINEITEMS","NEWLINEITEMS" ,"CONFIGURATIONS" ,	"NEWCONFIGURATIONS	" ,	"partialbillinginstcomplete" ,	"partialbillinginstinprogress" ,	"SERVICENAME" ,	"GRP_PRODVERSION" };
	private String[] column2_1={"CIB_TEMP_ID" ,	"LATEST_TEMP_ID" ,	"INSTALLED_OFFER_ID" ,	"INSTALLED_OFFER_VERSION_ID" ,	"IOV_STATUS" ,	"MODIFICATIONDATE" ,	"PREV_INST_OFFER_ID	" ,	"PREV_INST_OFFER_VERSION_ID",	"PREV_IOV_STATUS","INSTALLED_OFFER_ID","INSTALLED_OFFER_VERSION_ID","LATEST_TEMP_ID","CIB_TEMP_ID"};
	
	private String[] column3={"OCATID","FINANCIALPRODUCTCODE","DESCRIPTION","EXIST_CONFIG","NEW_CONFIG","EQ_CIB_VERSION_ID","EQ_CIB_PREV_VERSION_ID","EQ_CIB_STABLE_ID","CUSTOMERLABEL","EXIST_CUSTOMERLABEL","EQ_TEMP_ID","TRIL_GID","INSTANCENUM","MLI_CATEGORY","PARTIALBILLINGINSTCOMPLETE","PARTIALBILLINGINSTINPROGRESS","COMPONENT","PARTNUMBER","MODIFICATIONDATE"};
	private String[] column4={"DESCRIPTION","NAME","MODIFICATIONDATE","OCATID","ATTRIBUTES"};
	double count = 0.0;
	private EarrachExtractionView earrachExtractViewComponents;
	private String offer;
	
	private String QUERY4ATTRIBUTE = "SELECT " +
			"L2ATTR.DESCRIPTION," +
			"L2ATTR.NAME," +
			"L2ATTR.MODIFICATIONDATE," +
			"L2ATTR.OCATID," +
			"L2ATTR.ATTRIBUTES," +
			"UPDOWNCOL.OBJECTGID " +
		"FROM " +
		ConnectionBean.getDbPrefix()+"EQ_L2ATTRIBUTE L2ATTR, " +
		ConnectionBean.getDbPrefix()+"EQ_UPDOWNCOL UPDOWNCOL," +
		ConnectionBean.getDbPrefix()+"EQ_UPDOWNITEM DOWNITEM, " +
		ConnectionBean.getDbPrefix()+"EQ_UPDOWNCOL DOWNCOL," +
		ConnectionBean.getDbPrefix()+"EQ_L2ATTRIBUTE ATR ," +
		ConnectionBean.getDbPrefix()+"EQ_CHANGE CHGE," +
		ConnectionBean.getDbPrefix()+"SC_HIERARCHY  HR," +
		ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE " +
		"WHERE L2ATTR.TRIL_GID = UPDOWNCOL.COL_KEY " +
		"AND UPDOWNCOL.COLLECTIONGID = DOWNITEM.EQ_UDITEMS	" +
		"AND DOWNITEM.TRIL_GID = DOWNCOL.OBJECTGID	" +
		"AND ATR.TRIL_GID =SUBSTR(DOWNCOL.COL_KEY,1,32)	" +
		"AND DOWNCOL.COLLECTIONGID = CHGE.UPDOWNITEMS	" +
		"AND CHGE.TRIL_GID = HR.DATA	" +
		"AND HR.TRIL_GID = QUOTE.CONFIGURATIONS	" +
		"AND QUOTE.QUOTENUMBER = ?";
	
	private String QUERY4COMPONENTS = "SELECT " +
	"UPDOWNITEM.EQ_CIB_PREV_VERSION_ID," +
	"UPDOWNITEM.EQ_CIB_STABLE_ID," +
	"UPDOWNITEM.EQ_CIB_VERSION_ID," +
	"L2ATTR.DESCRIPTION,"+
	"UPDOWNCOL.OBJECTGID "+
	
	"FROM " +
	ConnectionBean.getDbPrefix()+"EQ_UPDOWNITEM UPDOWNITEM, 	" +
	ConnectionBean.getDbPrefix()+"EQ_UPDOWNCOL UPDOWNCOL, " +
	ConnectionBean.getDbPrefix()+"EQ_L2ATTRIBUTE L2ATTR, " +
	ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR, " +
	ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE, " +
	ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG " +
	"WHERE 	" +
	"QUOTE.QUOTENUMBER = ? " +
	"AND QUOTE.CONFIGURATIONS = HR.TRIL_GID " +
	"AND HR.DATA = CHG.TRIL_GID  " +
	"AND UPDOWNCOL.COLLECTIONGID = CHG.UPDOWNITEMS 	" +
	"AND L2ATTR.TRIL_GID =SUBSTR(COL_KEY,1,32) " +
	"AND UPDOWNITEM.TRIL_GID = UPDOWNCOL.OBJECTGID"; 
	
	private String QUERY4MLI_ELEMENT_COMPANION ="SELECT " +
			"SC_QUOTE_LINE_ITEM.OCATID," +
			"SC_QUOTE_LINE_ITEM.FINANCIALPRODUCTCODE," +
			"SC_QUOTE_LINE_ITEM.DESCRIPTION," +
			"SC_QUOTE_LINE_ITEM.EXIST_CONFIG," +
			"SC_QUOTE_LINE_ITEM.NEW_CONFIG," +
			"SC_QUOTE_LINE_ITEM.EQ_CIB_VERSION_ID," +
			"SC_QUOTE_LINE_ITEM.EQ_CIB_PREV_VERSION_ID," +
			"SC_QUOTE_LINE_ITEM.EQ_CIB_STABLE_ID, " +
			"SC_QUOTE_LINE_ITEM.CUSTOMERLABEL," +
			"SC_QUOTE_LINE_ITEM.EXIST_CUSTOMERLABEL," +
			"SC_QUOTE_LINE_ITEM.EQ_TEMP_ID," +
			"SC_QUOTE_LINE_ITEM.TRIL_GID," +
			"SC_QUOTE_LINE_ITEM.INSTANCENUM," +
			"SC_QUOTE_LINE_ITEM.MLI_CATEGORY," +
			"SC_QUOTE.PARTIALBILLINGINSTCOMPLETE," +
			"SC_QUOTE.PARTIALBILLINGINSTINPROGRESS, " +
			"SC_QUOTE_LINE_ITEM.COMPONENT," +
			"SC_QUOTE_LINE_ITEM.PARTNUMBER," +
			"SC_QUOTE_LINE_ITEM.MODIFICATIONDATE "+
			" FROM " +
			ConnectionBean.getDbPrefix()+"SC_QUOTE_LINE_ITEM INNER JOIN " +
			ConnectionBean.getDbPrefix()+"SC_QUOTE ON SC_QUOTE.TRIL_GID = SC_QUOTE_LINE_ITEM.QUOTE " +
			" WHERE SC_QUOTE.QUOTENUMBER = ?"; 

	private String QUERY4OFFER = "SELECT " +
	"QUOTE.QUOTENUMBER,	" +
	"QUOTE.MODIFICATIONDATE,	" +
	"QUOTE.EQ_GOLDORIGNB," +
	"QUOTE.TRIL_GID,	" +
	"QUOTE.LINEITEMS,	" +
	"QUOTE.NEWLINEITEMS,	" +
	"QUOTE.CONFIGURATIONS ," +
	"QUOTE.NEWCONFIGURATIONS,	" +
	"QUOTE.PARTIALBILLINGINSTCOMPLETE,	" +
	"QUOTE.PARTIALBILLINGINSTINPROGRESS,	" +
	"QUOTE.SERVICENAME,	" +
	"QUOTE.GRP_PRODVERSION " +
	
	
	
	"FROM "+
	ConnectionBean.getDbPrefix()+"SC_QUOTE_MISCELLANEOUS MIS ,"+
	ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE "+
	"WHERE MIS.TRIL_GID = QUOTE.SC_QUOTE_MISCELLANEOUS "+
	"AND QUOTE.QUOTENUMBER=? ";


	private String QUERY4OFFERMIS = "SELECT " +
		
	"MIS.CIB_TEMP_ID,	" +
	"MIS.LATEST_TEMP_ID,	" +
	"MIS.INSTALLED_OFFER_ID,	" +
	"MIS.INSTALLED_OFFER_VERSION_ID,	" +
	"QUOTE.MODIFICATIONDATE," +
	"MIS.IOV_STATUS,	" +
	"MIS.PREV_INST_OFFER_ID,	" +
	"MIS.PREV_INST_OFFER_VERSION_ID,	" +
	"MIS.PREV_IOV_STATUS,"+
	"MIS.INSTALLED_OFFER_ID," +
	"MIS.INSTALLED_OFFER_VERSION_ID," +
	"MIS.LATEST_TEMP_ID," +
	"MIS.CIB_TEMP_ID "+  
	
	"FROM "+
	ConnectionBean.getDbPrefix()+"SC_QUOTE_MISCELLANEOUS MIS ,"+
	ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE "+
	"WHERE MIS.TRIL_GID = QUOTE.SC_QUOTE_MISCELLANEOUS "+
	"AND QUOTE.QUOTENUMBER=? ";




private String resultFileLoc;


private final CustomJFrame tabbedFrame;

	private JTabbedPaneCloseButton tabbedPane;
	int totaldata = 0;
	public EarrachExtractQueryExecutor(String offer,EarrachExtractionView earrachExtractViewComponents,String resultFileLoc,Dimension screenSize)
	{
		this.earrachExtractViewComponents = earrachExtractViewComponents;
		this.resultFileLoc = resultFileLoc;
		this.offer = offer.trim();
		tabbedPane = new JTabbedPaneCloseButton();
		tabbedFrame = new CustomJFrame(offer,Icons.iconPathEarrach);
		tabbedFrame.setBounds(50,50,screenSize.width * 60 / 100,screenSize.height * 60 / 100);
		
		tabbedFrame.add(tabbedPane, BorderLayout.CENTER);
		tabbedFrame.setAlwaysOnTop( true );
		
		
		
	}

	
	
private void addTable(String[] header,String tabName,ArrayList<String[]> rows,String resultFileLoc) throws IOException
{
	String filePath=resultFileLoc+tabName+".csv";
	CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(filePath),true);
	DefaultTableModel model = new DefaultTableModel(0, 0);
	model.setColumnIdentifiers(header);
	writer.writeNext(header);
	final CustomJTable table = new CustomJTable();
	table.setModel(model);
	
	JScrollPane tableContainer = new JScrollPane(table.getTable());
	
	for (int row = 0; row < rows.size(); row++) 
	{
		model.addRow(rows.get(row));
		writer.writeNext(rows.get(row));
		count++;	
		ProgressMonitorPane.getInstance().setProgress(count,totaldata);	
	}
	writer.close();
	ImageIcon imageIcon = new ImageIcon("");
	tabbedPane.addTab(tabName+".csv"+" ("+table.getTable().getRowCount()+")", imageIcon, tableContainer, filePath);
	table.decorateRows();
}

public void getBaseQueryExecution() throws SQLException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		ArrayList<String[]> QUERY4COMPONENTSResults = CommonUtils.getQueryResult(QUERY4COMPONENTS, earrachExtractViewComponents,offer);
		ArrayList<String[]> QUERY4OFFERResults = CommonUtils.getQueryResult(QUERY4OFFER, earrachExtractViewComponents,offer);
		ArrayList<String[]> QUERY4OFFERResults_MIS= CommonUtils.getQueryResult(QUERY4OFFERMIS, earrachExtractViewComponents,offer);
		
		ArrayList<String[]> QUERY4MLI_ELEMENT_COMPANIONResults = CommonUtils.getQueryResult(QUERY4MLI_ELEMENT_COMPANION, earrachExtractViewComponents,offer);
		ArrayList<String[]> QUERY4ATTRIBUTEResults = CommonUtils.getQueryResult(QUERY4ATTRIBUTE, earrachExtractViewComponents,offer);
		totaldata = QUERY4COMPONENTSResults.size()+QUERY4OFFERResults.size()+QUERY4MLI_ELEMENT_COMPANIONResults.size()+QUERY4ATTRIBUTEResults.size()+QUERY4OFFERResults_MIS.size();
		count = 0.0; 
		addTable(column1,"COMPONENTS",QUERY4COMPONENTSResults,resultFileLoc);
		addTable(column2,"OFFER",QUERY4OFFERResults,resultFileLoc);
		addTable(column2_1,"OFFER_MIS",QUERY4OFFERResults_MIS,resultFileLoc);
		addTable(column3,"MLI_ELEMENT_COMPANION",QUERY4MLI_ELEMENT_COMPANIONResults,resultFileLoc);
		addTable(column4,"ATTRIBUTE",QUERY4ATTRIBUTEResults,resultFileLoc);
		
		tabbedFrame.setVisible(true);
		
}
}
