package com.orange.util.imadaq;

import java.io.File;

public class Constants {

	public static String dataByPassMatch[] = new String[] { "Target USID already exists in CSI with another product or customer.",
		"The Target USID ServiceElement belongs to different Service element categories."};
	public static String directoryPrefix = "ICO_";
	public static String exemptLeftFileFolder = "_Left_";
	
	
	
	public static String lockedOrderFileName = "USIDMigrationData_Locked.csv";

	public static String lockOrderComment[] = new String[] {
			"Failed to get lock on order",
			"Object Version Conflict. your version" };
	public static String matchStatus = "Failed";
	public static String migrationDataBypass = "USIDMigrationDataByPass.csv";
	public static String migrationDataBypassHelp = "USIDMigrationDataByPassHelp.txt";
	public static String migrationMigratedOrderByPassReport = "USIDMigrationMigratedOrderReportByPass.csv";
	public static String migrationMigratedOrderReport = "USIDMigrationMigratedOrderReport.csv";
	public static String packageFilesPrefix = "USIDMigrationMigratedOrderReport";
	public static String seperator = File.separator;
	public static String tableStyle =" <script type='text/javascript'>function altRows(id){	if(document.getElementsByTagName){  var table = document.getElementById(id);  	var rows = table.getElementsByTagName('tr');	for(i = 0; i < rows.length; i++){       	if(i % 2 == 0){		rows[i].className = 'evenrowcolor';	}else{		rows[i].className = 'oddrowcolor';	}      	}}}window.onload=function(){altRows('alternatecolor');}</script><style type='text/css'>table.altrowstable {	font-family: verdana,arial,sans-serif;	font-size:11px;	color:#333333;	border-width: 1px;border-color: #a9c6c9;	border-collapse: collapse;}table.altrowstable th {	border-width: 1px;	padding: 8px;	border-style: solid;border-color: #a9c6c9;}table.altrowstable td {	border-width: 1px;	padding: 8px;	border-style: solid;	border-color: #a9c6c9;}.oddrowcolor{	background-color:#d4e3e5;}.evenrowcolor{	background-color:#c3dde0;}</style>";
	
	public static String leftICOFileName = "USIDMigrationData_Left_";
	public static String impactOrderReport = "USIDMigrationImpactedOrderReport.csv";
	public static String usidMigrationData = "USIDMigrationData.csv";
	public static String leftICOMatch[] = new String[] {"Both the USIDs are present on different site."};
	
	
	

}
