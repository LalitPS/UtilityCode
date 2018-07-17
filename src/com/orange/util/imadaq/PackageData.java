package com.orange.util.imadaq;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.util.CustomCSVWriter;

public class PackageData {

	String executionFolder;
	String parentPath;

	public PackageData(String parentPath) {
		this.parentPath = parentPath;
		executionFolder = parentPath.substring(parentPath.lastIndexOf(File.separator ) + 1, parentPath.length())	+ "-Execution";
		File packageDir = new File(parentPath +File.separator  + executionFolder);
		if (!packageDir.exists()) 
		{
			packageDir.mkdir();
		}

	}

	public void packageFileData(String basePath) throws IOException 
	{

		File file = new File(basePath);
		File[] fileList = file.listFiles();

		if (fileList != null)
		{

			for (int i = 0; i < fileList.length; i++) 
			{
				if (fileList[i].isDirectory()) 
				{
					File packageFile = new File(basePath + File.separator 	+ fileList[i].getName() + File.separator + Constants.packageFilesPrefix + ".csv");
					if (packageFile.exists()) 
					{
						CSVReader csvReader = new CSVReader(new FileReader(	packageFile));
						String removeICO1 = fileList[i].getName().replace(Constants.directoryPrefix, "");
						CustomCSVWriter CustomCSVWriter = new CustomCSVWriter(new FileWriter(	parentPath + File.separator + executionFolder +File.separator+ Constants.packageFilesPrefix + "_"
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
				packageFileData(String.valueOf(fileList[i]));
			}

		}
	}
}
