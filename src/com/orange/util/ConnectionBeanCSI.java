package com.orange.util;

public class ConnectionBeanCSI {

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
		ConnectionBeanCSI.dbPrefix = dbPrefix;
	}
	public static void setPassword(String password) {
		ConnectionBeanCSI.password = password;
	}
	public static void setPort(String port) {
		ConnectionBeanCSI.port = port;
	}
	public static void setSid(String sid) {
		ConnectionBeanCSI.sid = sid;
	}
	public static void setUrl(String url) {
		ConnectionBeanCSI.url = url;
	}
	public static void setUserName(String userName) {
		ConnectionBeanCSI.userName = userName;
	}
	public static String getDbName() {
		return dbName;
	}
	public static void setDbName(String dbName) {
		ConnectionBeanCSI.dbName = dbName;
	}
	
}
