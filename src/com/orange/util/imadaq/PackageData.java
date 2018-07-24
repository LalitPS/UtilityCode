package com.orange.util.imadaq;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.util.CustomCSVWriter;

public class PackageData {

	String migratedReportExecutionFolder;
	String impactedReportExecutionFolder;
	String parentPath;

	public PackageData(String parentPath) {
		this.parentPath = parentPath;
		migratedReportExecutionFolder = parentPath.substring(parentPath.lastIndexOf(File.separator ) + 1, parentPath.length())	+ "-MigratedReportsExecution";
		File packageDir = new File(parentPath +File.separator  + migratedReportExecutionFolder);
		if (!packageDir.exists()) 
		{
			packageDir.mkdir();
		}
		
		impactedReportExecutionFolder = parentPath.substring(parentPath.lastIndexOf(File.separator ) + 1, parentPath.length())	+ "-ImpactedReportsExecution";
		packageDir = new File(parentPath +File.separator  + impactedReportExecutionFolder);
		if (!packageDir.exists()) 
		{
			packageDir.mkdir();
		}
		

	}
	
	public void packageFileForImpactedReportsData(String basePath) throws IOException 
	{

		File file = new File(basePath);
		File[] fileList = file.listFiles();

		if (fileList != null)
		{

			for (int i = 0; i < fileList.length; i++) 
			{
				if (fileList[i].isDirectory()) 
				{
					File packageFile = new File(basePath + File.separator 	+ fileList[i].getName() + File.separator + Constants.packageImpactedFilesPrefix + ".csv");
					if (packageFile.exists()) 
					{
						CSVReader csvReader = new CSVReader(new FileReader(	packageFile));
						String removeICO1 = fileList[i].getName().replace(Constants.directoryPrefix, "");
						CustomCSVWriter CustomCSVWriter = new CustomCSVWriter(new FileWriter(	parentPath + File.separator + impactedReportExecutionFolder +File.separator+ Constants.packageImpactedFilesPrefix + "_"
								+ removeICO1 + ".csv"));
						String[] csvRow;
						while ((csvRow = csvReader.readNext()) != null) 
						{
							CustomCSVWriter.writeNext(csvRow);
						}
						csvReader.close();
						CustomCSVWriter.close();
					}
				}
				packageFileForImpactedReportsData(String.valueOf(fileList[i]));
			}

		}
	}


	public void packageFileForMigratedReportsData(String basePath) throws IOException 
	{

		File file = new File(basePath);
		File[] fileList = file.listFiles();

		if (fileList != null)
		{

			for (int i = 0; i < fileList.length; i++) 
			{
				if (fileList[i].isDirectory()) 
				{
					File packageFile = new File(basePath + File.separator 	+ fileList[i].getName() + File.separator + Constants.packageMigratedFilesPrefix + ".csv");
					if (packageFile.exists()) 
					{
						CSVReader csvReader = new CSVReader(new FileReader(	packageFile));
						String removeICO1 = fileList[i].getName().replace(Constants.directoryPrefix, "");
						CustomCSVWriter CustomCSVWriter = new CustomCSVWriter(new FileWriter(	parentPath + File.separator + migratedReportExecutionFolder +File.separator+ Constants.packageMigratedFilesPrefix + "_"
								+ removeICO1 + ".csv"));
						String[] csvRow;
						while ((csvRow = csvReader.readNext()) != null) 
						{
							CustomCSVWriter.writeNext(csvRow);
						}
						csvReader.close();
						CustomCSVWriter.close();
					}
				}
				packageFileForMigratedReportsData(String.valueOf(fileList[i]));
			}

		}
	}
}
