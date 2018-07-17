package com.orange.util.imadaq;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javax.swing.JOptionPane;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.ui.component.custom.CustomJFrame;
import com.orange.util.CustomCSVWriter;

public class MergeErrorsDataFromImpectedOrderFile {

	String check = "No order(s) find in GOLD/Archival";
	LinkedHashMap<String, String> consolidatedErrors = new LinkedHashMap<String, String>();
	CustomCSVWriter consolidatedWriter;
	int counter = 1;
	CustomCSVWriter detailedWriter;
	String executionFolder;
	String fileNameOFUSID = "USIDMigrationData.csv";
	String fileNameToRead = "USIDMigrationImpactedOrderReport.csv";
	ArrayList<String> readFileInfo = new ArrayList<String>();
	int usidFileCounter = 1;
	CustomCSVWriter writerUsid;
	
	public MergeErrorsDataFromImpectedOrderFile(String parentPath) {
		executionFolder = parentPath.substring(	parentPath.lastIndexOf("\\") + 1, parentPath.length())+ "-MergeErrors";
		File mergeErrorDir = new File(parentPath + "\\" + executionFolder);
		if (!mergeErrorDir.exists()) {
			mergeErrorDir.mkdir();
		}

	}

	public ArrayList<String> getReadFilesList() {
		return readFileInfo;
	}

	private void initCustomCSVWriter(String detailedPath,String usidFilePath,String consolidatedPath) throws IOException {
		detailedWriter = new CustomCSVWriter(new FileWriter(detailedPath));
		writerUsid = new CustomCSVWriter(new FileWriter(usidFilePath));
		consolidatedWriter = new CustomCSVWriter(new FileWriter(consolidatedPath));
	}

	public void mergeErrors(String basePath) throws IOException {
		initCustomCSVWriter(basePath + "\\" + executionFolder
				+ "\\DetailedMergeErrorsFromImpactedFiles("+executionFolder+").csv",
				basePath + "\\" + executionFolder
				+ "\\USIDSFromImpactedFiles("+executionFolder+").csv",
				basePath + "\\" + executionFolder
				+ "\\ConsolidatedMergeErrorsFromImpactedFiles("+executionFolder+").csv");
		packageFileData(basePath);
		detailedWriter.close();
		consolidatedWriter.close();
		writerUsid.close();

	}

	private void packageFileData(String basePath) throws IOException {
		LinkedHashSet<String> usids = new LinkedHashSet<String>();
		File file = new File(basePath);
		File[] fileList = file.listFiles();

		if (fileList != null) {
			for (int i = 0; i < fileList.length; i++) {
				File whichfile = fileList[i];
				if (!whichfile.isDirectory()) {
					if (whichfile.getName().equalsIgnoreCase(fileNameToRead)) 
					{
						readFileInfo.add(whichfile.getAbsolutePath());
						CSVReader br = new CSVReader(new FileReader(whichfile));
						String[] row ;
						// This is to add Header once in a file
						if(counter == 1){
							row = br.readNext();
							detailedWriter.writeNext(row);
							consolidatedWriter.writeNext(row);
						}
						if(counter !=1){
							row = br.readNext();
						}
						counter++;
						while ((row = br.readNext()) != null) {
							String status = row[4];
							if (status != null && !status.isEmpty()
									&& !status.equalsIgnoreCase(check)) 
							{
								if(!usids.contains(row[0])){
									usids.add(row[0]);
									}
								detailedWriter.writeNext(row);
								String USID = row[0];
								String comments = row[4];
								// If Same USID and Comments exists ..
								// Not to add.. 
								if(!consolidatedErrors.containsKey(USID+comments)){
									consolidatedErrors.put(USID+comments, whichfile.getName());
									consolidatedWriter.writeNext(row);
								}
							}
						}
						
						File F = new File(basePath+"\\"+fileNameOFUSID);
						if(!F.exists()){
							JOptionPane.showMessageDialog(new CustomJFrame(), "USID Collection Failure .. No Usid Key File Found" +F.getAbsolutePath());
						}
						if(F.exists())
						{
							//here write writerUSID
						br = new CSVReader(new FileReader(F));
						
						// This is to add Header once in a file
						if(usidFileCounter ==1){
							writerUsid.writeNext(br.readNext());
						}
						if(usidFileCounter !=1){
							row = br.readNext();
						}
						usidFileCounter++;
						
						while ((row = br.readNext()) != null) 
						{
							String USID = row[0];
							if (usids.contains(USID)) 
							{
								writerUsid.writeNext(row);
							}
						}
					}
						br.close();
					}
				}
				packageFileData(String.valueOf(fileList[i]));
			}
		}
	}

}
