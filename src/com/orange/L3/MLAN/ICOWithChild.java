package com.orange.L3.MLAN;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import com.orange.ui.component.MLANViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.ProgressMonitorPane;

public class ICOWithChild {

	private MLANViewComponents mlanViewComponents;
	private String pathCSV;
	
	public ICOWithChild(MLANViewComponents mlanViewComponents,String dirPath) throws SQLException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		
		this.mlanViewComponents  = mlanViewComponents;
		
		int randomNum = 999 + (int)(Math.random() * ((999999 - 999) + 1));
		pathCSV   =dirPath+File.separator+"_"+randomNum+".csv";
		
		mlanViewComponents.getQueryResult().append("Collecting ICO Details.\nThis Process may take few moments.Please Wait...\n");
		getICODetails();
		mlanViewComponents.getQueryResult().append("ICO details collection completed successfully.\n");
		
		
	}
	
	private void getICODetails() throws SQLException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		
		String QL = "SELECT PAR.ORGANIZATIONID AS PARENT_ICO , PAR.NAME AS PARENT_ORG_NAME, PAR.STATUS AS PRIMARY_ORG_STATUS,"+
		"CLD.ORGANIZATIONID AS CHILD_ICO , CLD.NAME AS CHILD_ORG_NAME, CLD.STATUS AS CHILD_ORG_STATUS FROM "+
		ConnectionBean.getDbPrefix()+"SC_ORGANIZATION PAR , "+
		ConnectionBean.getDbPrefix()+"SC_ORGANIZATION CLD "+
		"WHERE CLD.CUSTOMEROF = PAR.TRIL_GID "+
		"AND PAR.ORGANIZATIONID <> CLD.ORGANIZATIONID ORDER BY PAR.ORGANIZATIONID";
		
		ArrayList<String[]> result = CommonUtils.getQueryResultWithHeader(QL, mlanViewComponents);
		
		String header[]=result.get(0);
		result.remove(0);
		
		ProgressMonitorPane.getInstance().setProgress(result.size(),(double)result.size());
		CommonUtils.showTable(header, result, pathCSV);
		mlanViewComponents.getQueryResult().append("File downloaded successfully.. "+pathCSV+"\n");
	}
	
}
