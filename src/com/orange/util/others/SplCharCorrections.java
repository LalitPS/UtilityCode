package com.orange.util.others;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.util.CommonUtils;

public class SplCharCorrections {

	public static void main(String ad[]){
		String fileName = "C://Lalit//Gold-Assignment//CSM//CSMTasks//SiteCodeswith&and'//oakgolddbvip.dc.iad.equant.com//2016-12-14_T612045867//USIDCorrection//Book1.csv";
		String updatedSQLFilePath = "C://Lalit//Gold-Assignment//CSM//CSMTasks//SiteCodeswith&and'//oakgolddbvip.dc.iad.equant.com//2016-12-14_T612045867//USIDCorrection//Book1_UPDATED.sql";
		try {
			new  SplCharCorrections(fileName,updatedSQLFilePath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null,e);
		}
	}
	ArrayList<String> csiExecutionScript;
	
	CSVReader csvReader;
	
	
	public SplCharCorrections(String readCsvFilePath,String updatedSQLFilePath) throws Exception
	{
		
		setNewUSID(readCsvFilePath,updatedSQLFilePath);
	}
	
	public void createCSIScriptFile(String filePath) throws Exception{
		File fout = new File(filePath);
		FileOutputStream fos = new FileOutputStream(fout);
	 
		csiExecutionScript = CommonUtils.setUmlaut(csiExecutionScript);
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		for(String script : csiExecutionScript){
			bw.write(script);
			bw.newLine();
		}
		bw.close();
	}
	
	private void setNewUSID(String readCsvFilePath,String updatedSQLFilePath) throws Exception{
		csiExecutionScript = new ArrayList<String>();
		
		csvReader = new CSVReader(new FileReader(readCsvFilePath));
		// skip header
		csvReader.readNext();
		
		String[] csvRow;
		int totalData=1;
		while ((csvRow = csvReader.readNext()) != null) 
		{
			
			//String QUOTE_NUMBER  = csvRow[0];
			//String Replace_SiteCode  = csvRow[1];
			//String Replacement_SiteCode  = csvRow[2];
			//String ADDRESS_ID  = csvRow[3];
			//String CORE_SITE_ID  = csvRow[4];
			String SERVICE_ELEMENT_ID  = csvRow[5];
			String PREV_USID  = csvRow[6];
			String UPDATED_USID  = csvRow[7];
			
			String arr[] = PREV_USID.split("::");
			if(null != arr && arr.length>4)
			{
				

				String rep="";
				for(int x = 3;x<arr.length;x++)
				{
					rep+=arr[x]+"::";
				}
				
				String arr1[] = UPDATED_USID.split("::");
				String ex="";
				for(int x = 0 ; x< 3 ; x++)
				{
				ex+=arr1[x]+"::";
				}
				ex=ex+rep;
				ex = ex.substring(0,ex.length()-2);
				
				String csiQ ="Update csi.Cserviceelement Set Usid='"+ex+"',Lupddate=Sysdate Where Serviceelementid='"+SERVICE_ELEMENT_ID+"';";
				csiExecutionScript.add("-- Update Incorrect USID "+PREV_USID+" from "+ex);
				csiExecutionScript.add(csiQ);
				totalData++;
			}
			
		
		}
		
		csvReader.close();
		createCSIScriptFile(updatedSQLFilePath);
		System.out.println("Process Complted Successfully for "+totalData);
	}
	
	
	
}
