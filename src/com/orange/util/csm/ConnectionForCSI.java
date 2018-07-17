package com.orange.util.csm;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.orange.ui.component.custom.CustomJCheckBox;
import com.orange.util.ConnectionBeanCSI;
import com.orange.util.ConnectionWindowForCSI;

public class ConnectionForCSI {
	
	public static Connection csiconnection;
	
	static ConnectionWindowForCSI frame = new ConnectionWindowForCSI();
	private static int RESULTSET_CONCURRENCY=ResultSet.CONCUR_READ_ONLY;
	private static int RESULTSET_TYPE = ResultSet.TYPE_SCROLL_INSENSITIVE;
	
	public static void closeConnection() throws SQLException {
		if (csiconnection != null) {
			
				csiconnection.close();
				ConnectionBeanCSI.setUrl("");
			
		}
		csiconnection = null;
	}

	public static Connection getCSIConnection() throws InstantiationException,
	IllegalAccessException, ClassNotFoundException, SQLException, IOException {

		if (csiconnection == null) {
			frame.setSize(400,300);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			return null;
		} 
			return csiconnection;
		
}
	
	public static Connection getCSIConnection(CustomJCheckBox validateFromDataBaseForCSI) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException, IOException 
			{
		if (csiconnection == null) 
		{
			frame.setCheckBox(validateFromDataBaseForCSI);
			frame.setSize(400,300);
			frame.setLocationRelativeTo(null);
		    frame.setVisible(true);
		   return null;
		} 
			return csiconnection;
		

	}
	
	public static PreparedStatement getPreparedStatement(String query) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		
		/*
		 * If connection is null , take the pre-existing details and create connection. 
		 * 
		 */
		try
		{
		if(null == csiconnection)
		{
			Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
			String url = "jdbc:oracle:thin:@" + ConnectionBeanCSI.getUrl()+ ":"+ConnectionBeanCSI.getPort()+":" + ConnectionBeanCSI.getSid();
			csiconnection = DriverManager.getConnection(url, ConnectionBeanCSI.getUserName(),ConnectionBeanCSI.getPassword());
		
		}
		}catch(Exception e)	{}
		
		// if still null 
		if(null == csiconnection)
		{
		
			getCSIConnection();
			
		}
		return csiconnection.prepareStatement(query,RESULTSET_TYPE,RESULTSET_CONCURRENCY);
	}

	public static Statement getStatement() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		
		/*
		 * If connection is null , take the pre-existing details and create connection. 
		 * 
		 */
		try
		{
		if(null == csiconnection)
		{
			Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
			String url = "jdbc:oracle:thin:@" + ConnectionBeanCSI.getUrl()+ ":"+ConnectionBeanCSI.getPort()+":" + ConnectionBeanCSI.getSid();
			csiconnection = DriverManager.getConnection(url, ConnectionBeanCSI.getUserName(),ConnectionBeanCSI.getPassword());
		}
		}catch(Exception e)
		{
			
		}
		
		// if still null 
		if(null == csiconnection)
		{
		
			getCSIConnection();
			
		}
		
		return csiconnection.createStatement(RESULTSET_TYPE,RESULTSET_CONCURRENCY);
	}

	public static boolean isConnected() throws SQLException {

		if (csiconnection == null || csiconnection.isClosed()) {
			return false;
		} else {
			return true;
		}

	}

}
