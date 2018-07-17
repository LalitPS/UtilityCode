package com.orange.L3.MLAN;

import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.ui.component.MLANViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.ProgressMonitorPane;

public class HookahBuildInfo {

	private String[] header = {"QUOTENUMBER","PARENT","PARTNUMBER","UPDOWNITEM.CSIOLDVALUE","UPDOWNITEM.EQ_OLDVALUE"};
	
	private MLANViewComponents mlanViewComponents;
	 
	
	private ArrayList<String> quoteNumbers;
	private String SQLQ =  
	 "SELECT " +
	 "QUOTE.QUOTENUMBER,  " +
	 "UPDOWNCOL.COL_KEY, "+

//	 " ATTR.DESCRIPTION , ATTR.NAME,ATTR.DISPLAYNAME," +
	 " ATTR.PARTNUMBER"+
	// " UPDOWNITEM.CSIOLDVALUE,UPDOWNITEM.EQ_OLDVALUE "+
	 " FROM  " +
	 ConnectionBean.getDbPrefix()+"EQ_L2ATTRIBUTE ATTR,"+
	 ConnectionBean.getDbPrefix()+"EQ_UPDOWNCOL UPDOWNCOL,"+
	 ConnectionBean.getDbPrefix()+"EQ_UPDOWNCOL UPDOWNCOL1,"+
	 ConnectionBean.getDbPrefix()+"EQ_UPDOWNITEM UPDOWNITEM,"+
	 ConnectionBean.getDbPrefix()+"EQ_CHANGE CHANGE,"+
	 ConnectionBean.getDbPrefix()+"SC_HIERARCHY HRC,"+
	 ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE " +
	 " WHERE "+
	 " QUOTE.QUOTENUMBER=?  " +
	 " AND QUOTE.CONFIGURATIONS = HRC.TRIL_GID  " +
	 " AND HRC.DATA = CHANGE.TRIL_GID  " +
	 " AND CHANGE.UPDOWNITEMS IN UPDOWNCOL.COLLECTIONGID   " +
	 " AND UPDOWNCOL.OBJECTGID IN UPDOWNITEM.TRIL_GID "+
	 " AND UPDOWNITEM.EQ_UDITEMS IN UPDOWNCOL1.COLLECTIONGID  " +
	 " AND UPDOWNCOL1.COL_KEY IN ATTR.TRIL_GID  "+
	 /*@ LALIT 07-JUNE 2018
		 * LIKE OPTION HAS NEEN DISABLED FROM THE UTILITY
		 * THIS ACTION HAS BEEN TAKEN TO OPTIMZE THE QUERY EXECUTUION.
		 * BY ENABALING TRUE , LIKE WILL BE ACTIVE AGAIN.
		 */
	  " AND ATTR.PARTNUMBER IN_OR_LIKE (ADD_LIKE_SECTION)";
	
	public HookahBuildInfo(String filePath,MLANViewComponents mlanViewComponents,String likeIncluded) throws IOException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		this.mlanViewComponents = mlanViewComponents;
		mlanViewComponents.getQueryResult().append("\nPlease wait..Process may take few moments.\n");
		mlanViewComponents.getQueryResult().append("\nIncluding Like in Query.\n");
		SQLQ = SQLQ.replace("ADD_LIKE_SECTION", likeIncluded);
		
		if(CommonUtils.isLikeDisabled)
		{
		SQLQ = SQLQ.replace("IN_OR_LIKE", "IN");
		}
		else
		{
		SQLQ = SQLQ.replace("IN_OR_LIKE", "LIKE");
		}
		
		
		quoteNumbers = getListOfOrders(filePath);
		ArrayList<String[]> queryResults = getQueryResults();
		
		String path = filePath;	
		int index = path.lastIndexOf(".");
		String sub = path.substring(0,index) ;
		String csvPath=sub+"_Hookah_Service_Details.csv";
		
		CommonUtils.showTable(header, queryResults, csvPath);
		mlanViewComponents.getQueryResult().append("\nFile export successfully."+csvPath);
		mlanViewComponents.getQueryResult().append("\nProcess completed successfully.");
	}
	
	private ArrayList<String> getListOfOrders(String filePath) throws IOException{
		ArrayList<String> quotes = new ArrayList<String>();
		CSVReader br = new CSVReader(new FileReader(filePath));
		String[] row = null;
		row = br.readNext();
		while ((row = br.readNext()) != null) 
		{
			quotes.add(CommonUtils.addZeroPrefixinOrder(row[0]));
		}
		br.close();
		return quotes;
	}
	
	private ArrayList<String[]> getParentInfo(String colKey) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		String filterColKey = colKey.substring(0,colKey.indexOf("__"));
		String LQL = "SELECT DESCRIPTION,NAME,PARENT,PARTNUMBER,SERVERSEQID from "+ ConnectionBean.getDbPrefix()+"EQ_L2ATTRIBUTE WHERE TRIL_GID = ?";
		ArrayList<String[]> queryResult = CommonUtils.getQueryResult(LQL, mlanViewComponents,filterColKey);
		return queryResult;
	}
	
	
	private ArrayList<String[]> getQueryResults() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		ArrayList<String[]> queryResults = new ArrayList<String[]>();
		int count = 0;
		for(String quote : quoteNumbers)
		{
			
			ArrayList<String[]> queryResult = CommonUtils.getQueryResult(SQLQ,  mlanViewComponents,quote);
			
			for(String[] row : queryResult)
			{
				String colKey = row[1];
				if(null != colKey && !colKey.equalsIgnoreCase("null") && colKey.length() !=0)
				{
					
					ArrayList<String[]> PARENTiNFO = getParentInfo(colKey);
					
						while(null != PARENTiNFO.get(0)[2])
						{
							PARENTiNFO = getParentInfo(PARENTiNFO.get(0)[2]);
						}
					row[1] = PARENTiNFO.get(0)[0] +" "+ (Integer.parseInt(PARENTiNFO.get(0)[4])+1);
				}
				
			}
			if(null == queryResult || queryResult.size() ==0)
			{
				String[] arr = {quote};
				queryResult.add(arr);
			
				
			}
			count++;
			ProgressMonitorPane.getInstance().setProgress(count,quoteNumbers.size());
			queryResults.addAll(queryResult);
			
		}
		return queryResults;
	}
}
