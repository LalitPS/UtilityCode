package com.orange.ui.component.custom;

import java.util.Properties;

import com.orange.util.CommonUtils;

public class Icons {
	//http://www.iconarchive.com/search?q=software&page=5
	//http://www.iconarchive.com/show/glossy-social-icons-by-social-media-icons.3.html

		static Properties properties1 						= CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV03);
		public static String database 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/DataBase.png":"/resources_base/DataBase.png";
		public static String databaseBG 				=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/dbbk.jpg":"/resources_base/dbbk.jpg";
		public static String helpIcon 						=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/Help.png":"/resources_base/Help.png";
		public static String newHelpICON 				=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/HelpNew.png":"/resources_base/HelpNew.png";
		public static String helpFormat 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/HelpFormat.png":"/resources_base/HelpFormat.png";
		public static String helpText 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/HelpText.png":"/resources_base/HelpText.png";
		public static String iconPath1 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/Imadaq.png":"/resources_base/Imadaq.png";
		public static String iconPath10 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/DataByPass.png":"/resources_base/DataByPass.png";
		public static String iconPath11 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/MergeMigrated.png":"/resources_base/MergeMigrated.png";
		public static String ruleIcon 						=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/Rules.png":"/resources_base/Rules.png";
		public static String iconPath12 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/Package.png":"/resources_base/Package.png";
		public static String iconPath13 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/MergeErrors.png":"/resources_base/MergeErrors.png";
		public static String iconPath14 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/MergeErrors1.png":"/resources_base/MergeErrors1.png";
		public static String iconPath15 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/SiteFix.png":"/resources_base/SiteFix.png";
		public static String iconPath2 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/DedupRun.png":"/resources_base/DedupRun.png";
		public static String iconPath3 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/SiteMigration.png":"/resources_base/SiteMigration.png";
		public static String iconPath4 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/MLAN.png":"/resources_base/MLAN.png";
		public static String iconPath5 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/OpenWave.png":"/resources_base/OpenWave.png";
		public static String iconPath6 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/FormatCSV.png":"/resources_base/FormatCSV.png";
		public static String iconPath7 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/Refresh.png":"/resources_base/Refresh.png";
		public static String iconPath8 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/Validate.png":"/resources_base/Validate.png";
		public static String iconPath9 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/LeftICO.png":"/resources_base/LeftICO.png";
		public static String iconPathEarrach 			=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/Earrach.png":"/resources_base/Earrach.png";
		public static String linkedupSitesHelp 		=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/LinkedupSitesHelp.png":"/resources_base/LinkedupSitesHelp.png";
		public static String loadingIcon 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/Loader.png":"/resources_base/Loader.jpg";
		public static String queryExecutorICON 		=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/Query.png":"/resources_base/Query.png";
		public static String siteFixHelp 					=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/SiteFixHelp.png":"/resources_base/SiteFixHelp.png";
		public static String splCharsHelp 				=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/SplChars.png":"/resources_base/SplChars.png";
		public static String leafIcon 						=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/Leaf.png":"/resources_base/Leaf.png";
		public static String openLeafIcon 				=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/openLeaf.png":"/resources_base/openLeaf.png";
		public static String closeLeafIcon 				=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/closeLeaf.png":"/resources_base/closeLeaf.png";
		public static String treeExpandIcon 			=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/treeExpand.png":"/resources_base/treeExpand.png";
		public static String treeIcon 						=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/tree.png":"/resources_base/tree.png";
		public static String imadaqV02Icon 			=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/ImadaqV02.png":"/resources_base/ImadaqV02.png";
		public static String ciBasedIcon 				=  properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") ? "/resources/CiBase.png":"/resources_base/CiBase.png";
		
	
}
