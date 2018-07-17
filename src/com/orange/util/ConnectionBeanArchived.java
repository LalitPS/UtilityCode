package com.orange.util;

public class ConnectionBeanArchived {

	private static String dbName; 
	private static String dbPrefix; 
	private static String password;
	private static String port;
	private static String sid;
	private static String url ; 
	private static String userName;
	
	
	public static String getDbPrefix() {
		return dbPrefix;
	}
	public static String getPassword() {
		return password;
	}
	public static String getPort() {
		return port;
	}
	public static String getSid() {
		return sid;
	}
	public static String getUrl() {
		return url;
	}
	public static String getUserName() {
		return userName;
	}
	public static void setDbPrefix(String dbPrefix) {
		ConnectionBeanArchived.dbPrefix = dbPrefix;
	}
	public static void setPassword(String password) {
		ConnectionBeanArchived.password = password;
	}
	public static void setPort(String port) {
		ConnectionBeanArchived.port = port;
	}
	public static void setSid(String sid) {
		ConnectionBeanArchived.sid = sid;
	}
	public static void setUrl(String url) {
		ConnectionBeanArchived.url = url;
	}
	public static void setUserName(String userName) {
		ConnectionBeanArchived.userName = userName;
	}
	public static String getDbName() {
		return dbName;
	}
	public static void setDbName(String dbName) {
		ConnectionBeanArchived.dbName = dbName;
	}
	
}

