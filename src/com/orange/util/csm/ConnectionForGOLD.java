package com.orange.util.csm;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.orange.ui.component.custom.CustomJCheckBox;
import com.orange.util.ConnectionBean;
import com.orange.util.ConnectionWindowForGOLD;

public class ConnectionForGOLD {
	
	public static Connection connection;
	
	static ConnectionWindowForGOLD frame = new ConnectionWindowForGOLD();
	private static int RESULTSET_CONCURRENCY=ResultSet.CONCUR_READ_ONLY;
	
	private static int RESULTSET_TYPE = ResultSet.TYPE_SCROLL_INSENSITIVE;
	
	
	
	public static void closeConnection() throws SQLException {
		if (connection != null) {
		
				connection.close();
				ConnectionBean.setUrl("");
			
			
		}
		connection = null;
	}

	
	public static Connection getGOLDConnection() throws InstantiationException,	IllegalAccessException, ClassNotFoundException, SQLException, IOException 
	{
		if (connection == null) 
		{
			frame.setSize(400,300);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			return null;
		} 
		return connection;
	}
	
	
	public static Connection getGOLDConnection(CustomJCheckBox validateFromDataBase) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException, IOException 
			{
		
		if (connection == null) 
		{
			frame.setCheckBox(validateFromDataBase);
			frame.setSize(400,300);
			frame.setLocationRelativeTo(null);
		    frame.setVisible(true);
		    return null;
		}
		return connection;

	}
	
	
	public static PreparedStatement getPreparedStatement(String query) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
		/*
		 * If connection is null , take the pre-existing details and create connection. 
		 * 
		 */
		try
		{
		if(null == connection)
		{
			Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
			String url = "jdbc:oracle:thin:@" + ConnectionBean.getUrl()+ ":"+ConnectionBean.getPort()+":" + ConnectionBean.getSid();
			connection = DriverManager.getConnection(url, ConnectionBean.getUserName(),ConnectionBean.getPassword());
		}
		}catch(Exception e)	{}
		
		// if still null 
		if(null == connection)
		{
			
				getGOLDConnection();
					
		}
		
		return connection.prepareStatement(query,RESULTSET_TYPE,RESULTSET_CONCURRENCY);
		
	}
	
	public static Statement getStatement() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException
	{
		/*
		 * If connection is null , take the pre-existing details and create connection. 
		 * 
		 */
		try
		{
		if(null == connection)
		{
			Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
			String url = "jdbc:oracle:thin:@" + ConnectionBean.getUrl()+ ":"+ConnectionBean.getPort()+":" + ConnectionBean.getSid();
			connection = DriverManager.getConnection(url, ConnectionBean.getUserName(),ConnectionBean.getPassword());
		}
		}catch(Exception e)	{}
		// if still null 
		
		if(null == connection)
		{
				getGOLDConnection();
				
		}
		
			return connection.createStatement(RESULTSET_TYPE,RESULTSET_CONCURRENCY);
	}

	public static boolean isConnected() throws SQLException {

		if (connection == null || connection.isClosed() ) {
			return false;
		} else 
		{
			return true;
		}

	}

}
