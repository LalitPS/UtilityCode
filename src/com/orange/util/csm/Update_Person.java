package com.orange.util.csm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.ui.component.SiteFixedViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.CurrentDateTime;
import com.orange.util.CustomCSVWriter;
import com.orange.util.ProgressMonitorPane;

public class Update_Person {

	private ArrayList<String[]> analysisFiledata;
	private String columnName[] = {
			"E_ICO1_CD", 
			"E_SITE_ID", 
			"E_ADDRESS_ID", 
		//	"DEFAULTCONTACT1_TRIL_GID",
			"DEFAULTCONTACT1_EMAIL",
			"DEFAULTCONTACT1_FIRSTNAME",
			"DEFAULTCONTACT1_LASTNAME",
			"DEFAULTCONTACT1_TEL",
		//	"DEFAULTCONTACT2_TRIL_GID",
			"DEFAULTCONTACT2_EMAIL",
			"DEFAULTCONTACT2_FIRSTNAME",
			"DEFAULTCONTACT2_LASTNAME",
			"DEFAULTCONTACT2_TEL"
			};
	private String csvAnalysisPath;
	private Map<String,ArrayList<String[]>> emailTrilgidMap;
	private ArrayList<String> errors;
	
	private String goldSQLPath;
	
	private ArrayList<String> goldSQLs;
	private Map<String,ArrayList<String[]>> ORGMap;
	private SiteFixedViewComponents siteFixViewComponents;
	
	public Update_Person(SiteFixedViewComponents siteFixViewComponents) throws Exception{
		
		this.siteFixViewComponents = siteFixViewComponents;
		emailTrilgidMap = new HashMap<String,ArrayList<String[]>>();
		ORGMap = new HashMap<String,ArrayList<String[]>>();
		String path = siteFixViewComponents.getFileToValidate().getText();	
		int index = path.lastIndexOf(".");
		String sub = path.substring(0,index) ;
		goldSQLPath=sub+"_GOLD.sql";
		csvAnalysisPath=sub+"_Analysis.csv";
		
		//String logPath=sub+"_CONSOLE_LOGS.xml";
		
		
		
		
		
		errors = new ArrayList<String>();
		goldSQLs = new ArrayList<String>();
		analysisFiledata =  new ArrayList<String[]>();
		ArrayList<String> validateResult = getFileValidateResult(path);
		if(validateResult.size() == 0)
		{
			siteFixViewComponents.getQueryResult().append("File Format Validation Success..\n");
			siteFixViewComponents.getQueryResult().append("Start Analysis ..\n");
			getFileAnalysisResult(path);
			siteFixViewComponents.getQueryResult().append("Analysis Completed Successfully..\n");
			siteFixViewComponents.getQueryResult().append("Creating Analysis results..\n");
			writeAnalysisFile(csvAnalysisPath);
			siteFixViewComponents.getQueryResult().append("Analysis results saved successfully .."+csvAnalysisPath+"\n");
			siteFixViewComponents.getQueryResult().append("Creating SQLs results..\n");
			writeGOLDScriptFile(goldSQLPath);
			siteFixViewComponents.getQueryResult().append("SQLs results saved successfully .."+goldSQLPath+"\n");
			siteFixViewComponents.getQueryResult().append("Process completed successfully ..\n");
			CommonUtils.createConsoleLogFile(siteFixViewComponents);
		}
		else{
			siteFixViewComponents.getQueryResult().append("File Format Validation Failed..\n");
			siteFixViewComponents.getQueryResult().append("Process failed  ..\n");
		}
	}
	
	private void getFileAnalysisResult(String filePath) throws Exception
	{
		
		
		CSVReader csvReader = new CSVReader(new FileReader(filePath));
		double goldDataCount = 0; 
		
		while ((csvReader.readNext()) != null) 
		{
			goldDataCount++;
		}
		csvReader.close();
				
		
		String checkCoreIds = "SELECT EQ_SITEOF,TRIL_GID,EQ_DEFAULTCONTACT1, EQ_DEFAULTCONTACT2 FROM "+ConnectionBean.getDbPrefix()+"EQ_SITE WHERE CORE_SITE_ID=? AND ADDRESS_ID=? AND STATUS ='0'";
		String checkINACTIVECoreIds = "SELECT EQ_SITEOF,TRIL_GID,EQ_DEFAULTCONTACT1, EQ_DEFAULTCONTACT2 FROM "+ConnectionBean.getDbPrefix()+"EQ_SITE WHERE CORE_SITE_ID=? AND ADDRESS_ID=? AND STATUS <> '0'";
		String checkPERSON ="SELECT PER.TRIL_GID FROM "+ConnectionBean.getDbPrefix()+"SC_PERSON PER WHERE PER.EMAIL = ? ORDER BY PER.MODIFICATIONDATE DESC";
		String checkICO = "SELECT ORGANIZATIONID FROM "+ConnectionBean.getDbPrefix()+"SC_ORGANIZATION WHERE TRIL_GID= ?";
		
		
		
		CSVReader br = new CSVReader(new FileReader(filePath));
		String []row = br.readNext();
		String []analysisRowHeader = new String[row.length+6];
		
		 for(int x = 0 ; x< row.length;x++)
		    {
			 analysisRowHeader[x] = row[x];
		    }
		 analysisRowHeader[row.length+0] = "CORE_IDS_ANALYSIS";
		 analysisRowHeader[row.length+1] = "ICO_ANALYSIS";
		 analysisRowHeader[row.length+2] = "PERSON_ANALYSIS";
		 analysisRowHeader[row.length+3] = "STATUS";
		 analysisRowHeader[row.length+4] = "SECOND_PERSON_ANALYSIS";
		 analysisRowHeader[row.length+5] = "STATUS";
		 analysisFiledata.add(analysisRowHeader);
		
		double count = 0.0;  
	
		
		while ((row = br.readNext()) != null) 
		{
			count++;	
			ProgressMonitorPane.getInstance().setProgress(count,goldDataCount);	
			String [] analysisRow = new String[analysisRowHeader.length];
			boolean isPass = true;
		    for(int x = 0 ; x< row.length;x++)
		    {
		    	analysisRow[x] = row[x];
		    }
			ArrayList<String[]> coreIdsresults = CommonUtils.getQueryResult(checkCoreIds, siteFixViewComponents,row[1].trim(), row[2].trim());
			if(coreIdsresults.size() ==0)
			{
		        // check for is inactive available?	
				ArrayList<String[]> INACTIVEcoreIdsresults = CommonUtils.getQueryResult(checkINACTIVECoreIds,  siteFixViewComponents,row[1].trim(), row[2].trim());
				if(INACTIVEcoreIdsresults.size() > 0)
				{
					analysisRow[row.length+0] = "ONLY INACTIVE OR MFD SITE FOUND.";
				}
				else
				{
					// NO ATVIVE OR NO INACTIVE FOUND
					analysisRow[row.length+0] = "NOT FOUND";
				}
				
				isPass = false;
			}
			else if(coreIdsresults.size() >1)
			{
				analysisRow[row.length+0] = "FOUND MORETHEN ONE";
				isPass = false;
			}
			else if(coreIdsresults.size() ==1)
			{
				analysisRow[row.length+0] = "FOUND";
				
				ArrayList<String[]> icoresults =null;
				if(ORGMap.containsKey(coreIdsresults.get(0)[0]))
				{
					icoresults = ORGMap.get(coreIdsresults.get(0)[0]);
					siteFixViewComponents.getQueryResult().append("Getting ICO "+coreIdsresults.get(0)[0] +" details from cache..\n");
				}
				else
				{
					icoresults = CommonUtils.getQueryResult(checkICO, siteFixViewComponents,coreIdsresults.get(0)[0]);
					ORGMap.put(coreIdsresults.get(0)[0], icoresults);
					siteFixViewComponents.getQueryResult().append("Adding ICO "+coreIdsresults.get(0)[0] +" details in cache..\n");
				}
				if(null == icoresults || icoresults.size()>1 || icoresults.size()==0)
				{
					analysisRow[row.length+1] = "NOT FOUND|FOUND MORETHEN ONE";
					isPass = false;
				}
				else
				{
					String rICO = icoresults.get(0)[0];
					if(!rICO.equals(row[0]))
					{
						analysisRow[row.length+1] = "MISMATCH";
						isPass = false;
					}
					else
					{
						analysisRow[row.length+1] = "MATCH";	
					
					}
				}
				
			}
			
			ArrayList<String[]> PERSONResults =null;
			if(emailTrilgidMap.containsKey(row[3].trim()))
			{
				PERSONResults = emailTrilgidMap.get(row[3].trim());
				siteFixViewComponents.getQueryResult().append("Getting person "+row[3].trim() +" details from cache..\n");
			}
			else
			{
				PERSONResults = CommonUtils.getQueryResult(checkPERSON,  siteFixViewComponents,row[3].trim());
				emailTrilgidMap.put(row[3].trim(), PERSONResults);
				siteFixViewComponents.getQueryResult().append("Adding person "+row[3].trim() +" details in cache..\n");
			}
			// Check default contact1
			
			String DEFUALTCONTACT1_TRILGID= "";
			if(null == PERSONResults || PERSONResults.size()==0 )//|| PERSONResults.size()>1)
			{
				analysisRow[row.length+2] = "NOT FOUND";
				isPass = false;
			}
			
			else if(PERSONResults.size()>1)
			{
				analysisRow[row.length+2] = "FOUND MORETHEN ONE : TAKING FIRST ONE";
				DEFUALTCONTACT1_TRILGID = PERSONResults.get(0)[0];
				//isPass = false;
			}
			
			else
			{
				analysisRow[row.length+2] = "FOUND";
				DEFUALTCONTACT1_TRILGID = PERSONResults.get(0)[0];
			}
			
			String DEFUALTCONTACT2_TRILGID= "";
			
			if(null != row[7] && row[7].length()>1)
			{
			   
				ArrayList<String[]> PERSONResults2 =null;
				if(emailTrilgidMap.containsKey(row[7].trim()))
				{
					PERSONResults2 = emailTrilgidMap.get(row[7].trim());
					siteFixViewComponents.getQueryResult().append("\nGetting "+row[7].trim() +" details from cache..");
				}
				else
				{
					PERSONResults2 = CommonUtils.getQueryResult(checkPERSON,  siteFixViewComponents,row[7].trim());
					emailTrilgidMap.put(row[7].trim(), PERSONResults2);
					siteFixViewComponents.getQueryResult().append("\nAdding "+row[7].trim() +" details in cache..");
				}
				
				// Check default contact2
				
				
				if(null == PERSONResults2 || PERSONResults2.size()==0 )
				{
					analysisRow[row.length+4] = "NOT FOUND";
					isPass = false;
				}
				else if(PERSONResults.size()>1)
				{
					analysisRow[row.length+2] = "FOUND MORETHEN ONE : TAKING FIRST ONE ";
					DEFUALTCONTACT2_TRILGID = PERSONResults2.get(0)[0];
					//isPass = false;
				}
				else
				{
					analysisRow[row.length+4] = "FOUND";
					DEFUALTCONTACT2_TRILGID = PERSONResults2.get(0)[0];
				}
			
			}
			else
			{
				analysisRow[row.length+4] = "DEFAULTCONTACT_2 NOT BEING UPDATED.";
			}
			if(isPass)
			{
				analysisRow[row.length+3] = "SUCCESS";
				analysisRow[row.length+5] = "SUCCESS";
				
				/*
				 * GOLD SQL FILE 
				 * 
				 */
				goldSQLs.add("\n--GOLD SCRIPT STARTS "+CurrentDateTime.getDateTimeText()+" FOR RECORD ("+(int) count +" FROM " + (int) goldDataCount +")");
				
				
				String SQL ="";
				
				if(null != row[7] && row[7].trim().length()>1)
				{
					goldSQLs.add("--GOLD SCRIPT STARTS AT "+CurrentDateTime.getDateTimeText()+" FOR CORE_SITE_ID '"+row[1]+"' ADDRESS_ID '"+row[2] +" CHANGE EQ_DEFAULTCONTACT1 FROM '"+coreIdsresults.get(0)[2]+"' TO '"+DEFUALTCONTACT1_TRILGID+"' and EQ_DEFAULTCONTACT2 FROM '"+coreIdsresults.get(0)[3]+" ' TO '"+DEFUALTCONTACT2_TRILGID);
					if(DEFUALTCONTACT1_TRILGID.equals(coreIdsresults.get(0)[2]) && DEFUALTCONTACT2_TRILGID.equals(coreIdsresults.get(0)[3]))
					{
						goldSQLs.add("--GOLD SCRIPT : EQ_DEFAULTCONTACT1 AND EQ_DEFAULTCONTACT2 ALREADY UPDATED. HENCE NO UPDATION REQUIRED..");	
					    SQL="-- NO SQL REQUIRED";
					}
					else if(!DEFUALTCONTACT1_TRILGID.equals(coreIdsresults.get(0)[2]) && DEFUALTCONTACT2_TRILGID.equals(coreIdsresults.get(0)[3]))
					{
						goldSQLs.add("--GOLD SCRIPT : EQ_DEFAULTCONTACT2 ALREADY UPDATED.HENCE NO UPDATION REQUIRED.");	
						SQL  = "UPDATE "+ConnectionBean.getDbPrefix()+"EQ_SITE SET EQ_DEFAULTCONTACT1 = '"+DEFUALTCONTACT1_TRILGID+"', MODIFICATIONDATE=sysdate WHERE TRIL_GID ='"+coreIdsresults.get(0)[1]+"';";
					}
					else if(DEFUALTCONTACT1_TRILGID.equals(coreIdsresults.get(0)[2]) && !DEFUALTCONTACT2_TRILGID.equals(coreIdsresults.get(0)[3]))
					{
						goldSQLs.add("--GOLD SCRIPT : EQ_DEFAULTCONTACT1 ALREADY UPDATED.HENCE NO UPDATION REQUIRED.");	
						SQL  = "UPDATE "+ConnectionBean.getDbPrefix()+"EQ_SITE SET EQ_DEFAULTCONTACT2 = '"+DEFUALTCONTACT2_TRILGID+"', MODIFICATIONDATE=sysdate WHERE TRIL_GID ='"+coreIdsresults.get(0)[1]+"';";
					}
					else
					{
						
						SQL  = "UPDATE "+ConnectionBean.getDbPrefix()+"EQ_SITE SET EQ_DEFAULTCONTACT1 = '"+DEFUALTCONTACT1_TRILGID+"', EQ_DEFAULTCONTACT2 = '"+DEFUALTCONTACT2_TRILGID+"', MODIFICATIONDATE=sysdate WHERE TRIL_GID ='"+coreIdsresults.get(0)[1]+"';";
					}
				}
				else
				{
					goldSQLs.add("--GOLD SCRIPT STARTS at "+CurrentDateTime.getDateTimeText()+" for core_site_id '"+row[1]+"' ADDRESS_ID '"+row[2] +" change EQ_DEFAULTCONTACT1 from '"+coreIdsresults.get(0)[2]+"' to '"+DEFUALTCONTACT1_TRILGID+"' and EQ_DEFAULTCONTACT2 NOT BEING UPDATE.");
				
					if(DEFUALTCONTACT1_TRILGID.equals(coreIdsresults.get(0)[2]))
					{
						goldSQLs.add("--GOLD SCRIPT : EQ_DEFAULTCONTACT1 ALREADY UPDATED.HENCE NO UPDATION REQUIRED.");	
						SQL="-- NO SQL REQUIRED";
					}
					else
					{
						SQL  = "UPDATE "+ConnectionBean.getDbPrefix()+"EQ_SITE SET EQ_DEFAULTCONTACT1 = '"+DEFUALTCONTACT1_TRILGID+"', MODIFICATIONDATE=sysdate WHERE TRIL_GID ='"+coreIdsresults.get(0)[1]+"';";
					}
				}
				goldSQLs.add(SQL);
				goldSQLs.add("--GOLD SCRIPT ENDS FOR CORE_SITE_ID '"+row[1]+"' ADDRESS_ID '"+row[2] +"' PERSON '"+row[3]+"'");
			}
			else
			{
				analysisRow[row.length+3] = "FAILED";
			}
			analysisFiledata.add(analysisRow);
		}
		br.close();
	}
	
	public ArrayList<String> getFileValidateResult(String filePath) throws Exception
	{
		CSVReader br = new CSVReader(new FileReader(filePath));
		String []row = br.readNext();
		for (int C = 0; C < row.length; C++) 
		{
			if (!columnName[C].equalsIgnoreCase(row[C])) 
			{
				errors.add("COLUMN_MISMATCH ::"+columnName[C]+"::"+row[C]);
			}
		}
		br.close();
		return errors;
	}
	
	
	private void writeAnalysisFile(String path) throws IOException{
		CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(path),true);
		for(String[] row : analysisFiledata){
			writer.writeNext(row);
		}
		writer.close();
	}
	private void writeGOLDScriptFile(String filePath) throws Exception{
		File fout = new File(filePath);
		FileOutputStream fos = new FileOutputStream(fout);
		goldSQLs = CommonUtils.setUmlaut(goldSQLs);
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		for(String script : goldSQLs){
			bw.write(script);
			bw.newLine();
		}
		bw.close();
		fos.close();
		goldSQLs = null;
	}
}