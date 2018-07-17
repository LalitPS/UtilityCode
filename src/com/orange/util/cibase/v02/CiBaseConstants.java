package com.orange.util.cibase.v02;

public class CiBaseConstants {

	public static final int existingValue_inFile 					= 0;
	public static final int targetValue_inFile 						= 1;
	public static final int quoteindexinfile 		 					= 2;
	public static final int serviceNameIndex_inFile 				= 3;
	public static final int endUserICOIndex_inFile 		 		= 4;
	public static final int contractingPartyICOIndex_inFile   = 5;
	public static final int GOLRESULT_inFile   						= 6;
	
	
	public static final int goldOKIndex = 24;
	
	//public static final int goldSiteIndex = 24;
	//public static final int goldServiceIndex = 24;
	//public static final int goldEndUserICOIndex = 24;
	
	
	/*
	 * 
	 * THESE INDEXES NEEDS TO BE RE-ALLIGN IF ANY NEW COLUMN IS ADDED IN BELOW QUERY.
	 * ANY NEW COLUMN IF NEEDED, SHOULD BE ADD IN LAST TO AVOID THESE INDEX CHANGES.
	 * 
	 */
	public static final int SERVICENAME_INDEX 	= 4;
	public static final int COMPONENTNAME_INDEX = 7;
	public static final int ELEMENTNAME_INDEX 	= 10;
	public static final int ATTRIBUTENAME_INDEX = 14;
	
	/*
	 * 
	 * THESE INDEXES NEEDS TO BE RE-ALLIGN IF ANY NEW COLUMN IS ADDED IN BELOW QUERY.
	 * ANY NEW COLUMN IF NEEDED, SHOULD BE ADD IN LAST TO AVOID THESE INDEX CHANGES.
	 */
	
	public static final int FORWARD_SERVICENAME_INDEX 	= 0;
	public static final int FORWARD_COMPONENTNAME_INDEX = 3;
	public static final int FORWARD_ELEMENTNAME_INDEX 	= 6;
	public static final int FORWARD_ATTRIBUTENAME_INDEX = 9;
	
	
	public static final String  dataForGOLDExecutionFileName 	="USIDMigrationData_Executable";
	public static final String  ciBaseExecutionReport			="USIDMigrationData_Final_Updated.csv";
	public static final String  usidMigrationDataFile				="USIDMigrationData_Final.csv";
	public static final String  ciBaseFilterMatch[] 				= new String[] {"OK","NA"};
	public static final String  GOLDPassFilterMatch 				= "PASS";
	public static final String  GOLDFAILEDFilterMatch 			= "FAILED";
	
}
