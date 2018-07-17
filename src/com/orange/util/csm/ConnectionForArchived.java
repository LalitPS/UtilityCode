package com.orange.util.csm;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.orange.ui.component.custom.CustomJCheckBox;
import com.orange.util.ConnectionBeanArchived;
import com.orange.util.ConnectionWindowForArchived;

public class ConnectionForArchived {
	public static Connection archivedconnection;

static ConnectionWindowForArchived frame = new ConnectionWindowForArchived();
private static int RESULTSET_CONCURRENCY=ResultSet.CONCUR_READ_ONLY;
private static int RESULTSET_TYPE = ResultSet.TYPE_SCROLL_INSENSITIVE;

public static void closeConnection() throws SQLException {
	if (archivedconnection != null) {
	
			archivedconnection.close();
			ConnectionBeanArchived.setUrl("");
		
	}
	archivedconnection = null;
}

public static Connection getArchivedConnection() throws InstantiationException,
IllegalAccessException, ClassNotFoundException, SQLException, IOException {

	if (archivedconnection == null) {
		frame.setSize(400,300);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		return null;
	} 
		return archivedconnection;
	
}

public static Connection getArchivedConnection(CustomJCheckBox validateFromDataBaseForArchived) throws InstantiationException,
		IllegalAccessException, ClassNotFoundException, SQLException, IOException 
		{
	if (archivedconnection == null) 
	{
		frame.setCheckBox(validateFromDataBaseForArchived);
		frame.setSize(400,300);
		frame.setLocationRelativeTo(null);
	    frame.setVisible(true);
	   return null;
	} 
		return archivedconnection;
	

}

public static PreparedStatement getPreparedStatement(String query) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
	
	/*
	 * If connection is null , take the pre-existing details and create connection. 
	 * 
	 */
	try
	{
	if(null == archivedconnection)
	{
		Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
		String url = "jdbc:oracle:thin:@" + ConnectionBeanArchived.getUrl()+ ":"+ConnectionBeanArchived.getPort()+":" + ConnectionBeanArchived.getSid();
		archivedconnection = DriverManager.getConnection(url, ConnectionBeanArchived.getUserName(),ConnectionBeanArchived.getPassword());
	
	}
	}catch(Exception e)	{}
	
	// if still null 
	if(null == archivedconnection)
	{
	
		getArchivedConnection();
		
	}
	return archivedconnection.prepareStatement(query,RESULTSET_TYPE,RESULTSET_CONCURRENCY);
}

public static Statement getStatement() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
	
	/*
	 * If connection is null , take the pre-existing details and create connection. 
	 * 
	 */
	try
	{
	if(null == archivedconnection)
	{
		Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
		String url = "jdbc:oracle:thin:@" + ConnectionBeanArchived.getUrl()+ ":"+ConnectionBeanArchived.getPort()+":" + ConnectionBeanArchived.getSid();
		archivedconnection = DriverManager.getConnection(url, ConnectionBeanArchived.getUserName(),ConnectionBeanArchived.getPassword());
	}
	}catch(Exception e)
	{
		
	}
	
	// if still null 
	if(null == archivedconnection)
	{
	
		getArchivedConnection();
		
	}
	
	return archivedconnection.createStatement(RESULTSET_TYPE,RESULTSET_CONCURRENCY);
}

public static boolean isConnected() throws SQLException {

	if (archivedconnection == null || archivedconnection.isClosed()) {
		return false;
	} else {
		return true;
	}

}

}

