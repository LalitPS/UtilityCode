package com.orange.util.csm;

import java.io.FileReader;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.util.CommonUtils;

public class LinkedupSiteFixFileValidator {
	String columnName[] = {
			"REPLACE_SITECODE", 
			"REPLACE_STATUS", 
			"REPLACEMENT_SITECODE", 
			"REPLACEMENT_STATUS",
			"REPLACE_OSR",
			"REPLACEMENT_OSR",
			"MOVE_ORDERS",
			"LEGACY_SITECODE",
			"ADDRESS_ID",
			"ANALYSIS_STATUS",
			"COMMENTS",
			"LEGACY_SITECODE_ICO",
			"LEG_SC_CORE_ADDRESSID",
			"LEG_SC_CORE_COREID"};
	private ArrayList<String> errors;
	private ArrayList<String[]> filedata;
	
	String filePath ;
	public LinkedupSiteFixFileValidator(String filePath){
		
		this.filePath = filePath;
		errors = new ArrayList<String>();
		filedata = new ArrayList<String[]>();
		
	}
	
	public ArrayList<String[]> getFileData() throws Exception{
		CSVReader br = new CSVReader(new FileReader(filePath));
		String []row = br.readNext();
		while ((row = br.readNext()) != null) 
		{
		/*
		 * Only Passed status will be process for SQL 
		 * 
		 */
			if(row[9].trim().equalsIgnoreCase("PASSED"))
			{
				filedata.add(row);
			}
		}
		br.close();
		return filedata;
	}
	
	
	
	public ArrayList<String> getFileValidateResult() throws Exception{
		CSVReader br = new CSVReader(new FileReader(filePath));
		String []row = br.readNext();
		for (int C = 0; C < row.length; C++) 
		{
			if (!columnName[C].equalsIgnoreCase(row[C])) 
			{
				errors.add("COLUMN_MISMATCH ::"+columnName[C]+"::"+row[C]);
			}
		}
		int count =1 ;
		while ((row = br.readNext()) != null) 
		{
			/*
			 * Check if ANALYSIS_STATUS = Passed then only check the 
			 * Others Column Values
			 * 
			 */
			if(row[9].equals("PASSED"))
			{
			
				if(row[0].isEmpty() )
				{
					errors.add("REPLACE_SITECODE_EMPTY::"+count);
				}
				else if(CommonUtils.isSpecialChars(row[0]))
				{
					errors.add("SPL_CHAR_REPLACE_SITECODE::"+count);
				}
				else if(row[0].length()>40)
				{
					errors.add("REPLACE_SITECODE_LENGTH::"+count);
				}
				
				if(row[1].isEmpty() )
				{
					errors.add("REPLACE_STATUS_EMPTY::"+count);
				}
				if(row[2].isEmpty() )
				{
					errors.add("REPLACEMENT_SITECODE_EMPTY::"+count);
				}
				else if(CommonUtils.isSpecialChars(row[2]))
				{
					errors.add("SPL_CHAR_REPLACEMENT_SITECODE::"+count);
				}
				else if(row[2].length()>40)
				{
					errors.add("REPLACEMENT_SITECODE_LENGTH::"+count);
				}
				
				if(row[3].isEmpty() )
				{
					errors.add("REPLACEMENT_STATUS_EMPTY::"+count);
				}
				if(row[8].isEmpty()){
					errors.add("ADDRESS_ID_EMPTY::"+count+"::"+count);
				}
				if(row[9].isEmpty()){
					errors.add("ANALYSIS_STATUS_EMPTY::"+count+"::"+count);
				}
				
				if(!row[1].isEmpty())
				{
					try
					{
					int value = Integer.parseInt(row[1]);
					if(value>3){
						errors.add("REPLACE_STATUS_NOT_VALID_VALUE::"+count+"::"+value);
					}
					}catch(Exception e){
						errors.add("REPLACE_STATUS_NOT_NUMBER::"+count);
					}
				}
				else
				{
					errors.add("REPLACE_STATUS_EMPTY::"+count);
				}
				if(!row[3].isEmpty()){
					try
					{
					int value = Integer.parseInt(row[1]);
					if(value>3){
						errors.add("REPLACEMENT_STATUS_NOT_VALID_VALUE::"+count+"::"+value);
					}
					}catch(Exception e){
						errors.add("REPLACEMENT_STATUS_NOT_NUMBER::"+count);
					}
				}
				else
				{
					errors.add("REPLACEMENT_STATUS_EMPTY::"+count);
				}
				
				if(!row[6].isEmpty() && (!row[6].trim().equalsIgnoreCase("All") && !row[6].trim().equalsIgnoreCase("NONE")))
				{
					splitString(row[6],count);
				}
				
			
			}
			count++;	
		}
		br.close();
		return errors;
	}
	
	private void splitString(String orders,int count){
		
		for (String retval: orders.trim().split(";")) {
			try{
			  Integer.parseInt(retval);
			}catch(Exception e){
				errors.add("ORDER_NOT_NUMBER::"+retval +"on Row" +count);
			}
	      }
	}
}