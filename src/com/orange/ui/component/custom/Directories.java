package com.orange.ui.component.custom;

import java.io.File;

public class Directories {

	public static String UserName=(null == System.getProperty("user.name") || System.getProperty("user.name").length() ==0 ? "Lalit" : System.getProperty("user.name"));
	public static String HOME =(null == System.getenv("SystemDrive")||System.getenv("SystemDrive").length()==0 ? "C:":System.getenv("SystemDrive"));
	
	public static String BASEDIR=HOME+File.separator+UserName;
	
	public static String d9Validation=HOME+File.separator+UserName+File.separator+"D9Validations.csv";
	public static String defaultDBConfigLocation=HOME+File.separator+UserName+File.separator+"config.properties";
	public static String defaultFTPConfigLocation=HOME+File.separator+UserName+File.separator+"UtilftpUpdatedV01.properties";
	
	public static String fileChooserDefaultLoc = HOME+File.separator+UserName+File.separator+"Gold-Assignment"+File.separator+"CSM"+File.separator+"CSMTasks"+File.separator+"GOLD_Templates_Previous_Runs";
	public static String goldCSISyncDataFilesLoc = HOME+File.separator+UserName+File.separator+"CSM";
	public static String goldEarrachSyncDataFilesLoc = HOME+File.separator+UserName+File.separator+"EARRACH";
	public static String goldQueryExecutorFilesLoc = HOME+File.separator+UserName+File.separator+"QqueriesExeution";
	public static String goldSPLCHRSiteCodesFilesLoc = HOME+File.separator+UserName+File.separator+"CSM"+File.separator+"SplChars_SiteCodes";
	
	public static String hookahDirLoc =HOME+File.separator+UserName+File.separator+"ServiceBuild"+File.separator+"Hookah";
	public static String hrefOfferStatus=HOME+File.separator+UserName+File.separator+"HrefOfferStatus.csv";
	public static String iovOfferStatus=HOME+File.separator+UserName+File.separator+"IOVOfferStatus.csv";
	public static String l2DirLoc = HOME+File.separator+UserName+File.separator+"ServiceBuild"+File.separator+"L2";
	public static String mLanDirLoc =HOME+File.separator+UserName+File.separator+"ServiceBuild"+File.separator+"MLAN";
	public static String sbDirLoc =HOME+File.separator+UserName+File.separator+"ServiceBuild"+File.separator+"ServiceBuildInfo";
	public static String srf2DirLoc =HOME+File.separator+UserName+File.separator+"srf2check"+File.separator+"srf2info";
	public static String icoWithParent =HOME+File.separator+UserName+File.separator+"ICO_PAR"+File.separator+"srf2info";
	public static String DEFAULTDIR =HOME+File.separator+UserName+File.separator;
	
	public static String EQL2InstanceMappingAnalysisFilesLoc = HOME+File.separator+UserName+File.separator+"CSM"+File.separator+"EQL2InstanceMappingAnalysis";
	
	public static String customUserConfigFileLocationOLD=HOME+File.separator+UserName+File.separator+"CustomUserConfig.properties";
	public static String customUserConfigFileLocation=HOME+File.separator+UserName+File.separator+"CustomUserConfigV01.properties";
	public static String customUserConfigFileLocationV02=HOME+File.separator+UserName+File.separator+"CustomUserConfigV02.properties";
	
	public static String elkConfigFileLocation=HOME+File.separator+UserName+File.separator+"ELKConfig.properties";
	
	
	public static String goldxmlpath=HOME+File.separator+UserName+File.separator+"GOLDConnections.xml";
	public static String csixmlpath=HOME+File.separator+UserName+File.separator+"CSIConnections.xml";
	public static String archivalxmlpath=HOME+File.separator+UserName+File.separator+"ArchivalConnections.xml";
	
	public static String GOLDDBConfigFileName="_GOLD_DATABASE_CONFIG.properties";
	public static String CSIDBConfigFileName="_CSI_DATABASE_CONFIG.properties";
	public static String ArchiveDBConfigFileName="_Archive_DATABASE_CONFIG.properties";
	
	public static String GOLDDatabaseConfigLocation=HOME+File.separator+UserName+File.separator+GOLDDBConfigFileName;
	public static String CSIDatabaseConfigLocation=HOME+File.separator+UserName+File.separator+CSIDBConfigFileName;
	public static String ArchivalDatabaseConfigLocation=HOME+File.separator+UserName+File.separator+ArchiveDBConfigFileName;
	
	
	public static String defaultFTPConfigLocationOLD=HOME+File.separator+UserName+File.separator+"UtilftpUpdated.properties";
	
	public static String customUserLOGS=HOME+File.separator+UserName+File.separator+"log4j.properties";
	public static String customUserLOGSFile=HOME+File.separator+UserName+File.separator+"appLogs.txt";
	
	static{
		
		File base= new File(BASEDIR);
		if(!base.exists())
		{
			base.mkdirs();
		}
	}
}
