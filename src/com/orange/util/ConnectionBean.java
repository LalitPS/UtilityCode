package com.orange.util;

public class ConnectionBean {

	private static String dbName; 
	private static String dbPrefix; 
	private static String password;
	private static String port;
	private static String sid;
	private static String url ; 
	private static String userName;
	
	
	public static String getDbPrefix() {
		return ConnectionBean.dbPrefix;
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
		ConnectionBean.dbPrefix = dbPrefix;
	}
	public static void setPassword(String password) {
		ConnectionBean.password = password;
	}
	public static void setPort(String port) {
		ConnectionBean.port = port;
	}
	public static void setSid(String sid) {
		ConnectionBean.sid = sid;
	}
	public static void setUrl(String url) {
		ConnectionBean.url = url;
	}
	public static void setUserName(String userName) {
		ConnectionBean.userName = userName;
	}
	public static String getDbName() {
		return dbName;
	}
	public static void setDbName(String dbName) {
		ConnectionBean.dbName = dbName;
	}
	
	
	
}
