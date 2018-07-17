package com.orange.util;

public class FTPSettings {

	/*
	 * FTP CONNECTION SETTINGS
	 */
	private String ftpUser 					= "devuser";
	private String ftpPassword				= "devuser";
	private String ftpURL						="10.237.93.66";
	/*
	 * USER DATABSE ACCESS SETTINGS
	 */
	private String DBftpFileLocation		="/releasedata/DEV/CSM_SQLS/Lalit/User_DbConfigs/";
	private String GOLD						="_GOLD_DATABASE_CONFIG.properties";
	private String Archival					="_Archive_DATABASE_CONFIG.properties";
	private String CSI							="_CSI_DATABASE_CONFIG.properties";
	
	/*
	 * USER UTILITY VERSION UPDATE SETTINGS
	 */
	private String ftpFileLocation			="/releasedata/DEV/CSM_SQLS/Lalit/";
	private String ftpFileName				="Imadaq.jar";
	private String ftpMsgFileName		="ImaDaQ.txt";
	private String ftpLatestFileName	="ImadaqU.jar";
		
			
	public String getGOLD() {
		return GOLD;
	}
	public String getArchival() {
		return Archival;
	}
	public String getCSI() {
		return CSI;
	}
	public String getDBftpFileLocation() {
		return DBftpFileLocation;
	}
	public String getFtpFileName() {
		return ftpFileName;
	}
	public String getFtpMsgFileName() {
		return ftpMsgFileName;
	}
	public String getFtpFileLocation() {
		return ftpFileLocation;
	}
	public String getFtpLatestFileName() {
		return ftpLatestFileName;
	}
	public String getFtpUser() {
		return ftpUser;
	}
	public String getFtpPassword() {
		return ftpPassword;
	}
	public String getFtpURL() {
		return ftpURL;
	}
}
