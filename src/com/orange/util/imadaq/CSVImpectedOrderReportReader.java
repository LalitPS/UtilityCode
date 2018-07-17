package com.orange.util.imadaq;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.util.CustomCSVWriter;

public class CSVImpectedOrderReportReader {


	private LinkedHashSet<String> countandPath = new LinkedHashSet<String>();

	public void createLeftFilesData(String basePath) throws IOException {

		File file = new File(basePath);
		File[] fileList = file.listFiles();

		if (fileList != null) {

			for (int i = 0; i < fileList.length; i++) {
				if (fileList[i].isDirectory()) 
				{
					File reportFile = new File(basePath + Constants.seperator+ fileList[i].getName() + Constants.seperator+ Constants.impactOrderReport);
					File migrationFile = new File(basePath + Constants.seperator+ fileList[i].getName() + Constants.seperator+ Constants.usidMigrationData);
					if (reportFile.exists() && migrationFile.exists()) {
						LinkedHashSet<String> orderReportSet = readImpectedOrderReportCSV(reportFile.getAbsolutePath());
						LinkedHashSet<String[]> migrationDataSet = readMigrationDataCSV(migrationFile.getAbsolutePath());
						LinkedHashSet<String[]> filterDataSet = filterMigrationData(orderReportSet, migrationDataSet);

						if (filterDataSet != null && filterDataSet.size() > 0) 
						{
							retrieveValuesFromList(basePath + Constants.seperator + fileList[i].getName()+ Constants.seperator + Constants.leftICOFileName+ ".csv",filterDataSet);
						}
						reportFile = null;
						migrationFile = null;
						orderReportSet = null;
						migrationDataSet = null;

					}

				}
				else
				{
					File reportFile = new File(basePath + Constants.seperator+  Constants.impactOrderReport);
					File migrationFile = new File(basePath + Constants.seperator+ Constants.usidMigrationData);
					if (reportFile.exists() && migrationFile.exists()) {
						LinkedHashSet<String> orderReportSet = readImpectedOrderReportCSV(reportFile.getAbsolutePath());
						LinkedHashSet<String[]> migrationDataSet = readMigrationDataCSV(migrationFile.getAbsolutePath());
						LinkedHashSet<String[]> filterDataSet = filterMigrationData(orderReportSet, migrationDataSet);

						if (filterDataSet != null && filterDataSet.size() > 0) 
						{
							retrieveValuesFromList(basePath + Constants.seperator + Constants.leftICOFileName+ ".csv",filterDataSet);
						}
						reportFile = null;
						migrationFile = null;
						orderReportSet = null;
						migrationDataSet = null;

					}

				
				}
				createLeftFilesData(String.valueOf(fileList[i]));
			}

		}
	}

	public LinkedHashSet<String[]> filterMigrationData(
			LinkedHashSet<String> orderReportSet,
			LinkedHashSet<String[]> migrationDataSet) {
		boolean putHeader = true;
	
		LinkedHashSet<String[]> filterDataSet = new LinkedHashSet<String[]>();
		
		Iterator<String> orderSetIterator = orderReportSet.iterator();
		while (orderSetIterator.hasNext()) {
			String oderRow = orderSetIterator.next();
			Iterator<String[]> migrationSetIterator = migrationDataSet.iterator();
			while (migrationSetIterator.hasNext()) {
				String migrationRow[] = migrationSetIterator.next();
				/*
				 * Read First Row as header...
				 */
				if (putHeader) {
					filterDataSet.add(migrationRow);
					putHeader = false;
				}

				if (oderRow.equalsIgnoreCase(migrationRow[0])) {
					filterDataSet.add(migrationRow);
					break;
				}
			}
		}
	
		return filterDataSet;
	}

	public LinkedHashSet<String> getCountandPath() {
		return countandPath;
	}

	public LinkedHashSet<String> readImpectedOrderReportCSV(String csvFilePath) throws IOException {
		LinkedHashSet<String> impectedOrderReportSet = new LinkedHashSet<String>();
			CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));
			String csvData[];
			while ((csvData = csvReader.readNext()) != null) {
					if (csvData.length > 4) {
					for (String s : Constants.leftICOMatch) {
						if (s.equalsIgnoreCase(csvData[4])) {
							impectedOrderReportSet.add(csvData[0]);
						}
					}
				}
			}
			csvReader.close();
		return impectedOrderReportSet;
	}

	public LinkedHashSet<String[]> readMigrationDataCSV(String csvFilePath) throws IOException {
		LinkedHashSet<String[]> migrationDataSet = new LinkedHashSet<String[]>();
			CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));
			String csvData[];
			while ((csvData = csvReader.readNext()) != null) {
				migrationDataSet.add(csvData);
			}
			csvReader.close();
		return migrationDataSet;
	}

	public void retrieveValuesFromList(String path, Set<String[]> set) throws IOException {
			writeCSVFile(path, set);
			countandPath.add(path);
	}


	private void writeCSVFile(String path, Set<String[]> set) throws IOException {
		CustomCSVWriter CustomCSVWriter = new CustomCSVWriter(new FileWriter(path));
		Iterator<String[]> itr = set.iterator();
		while (itr.hasNext()) {
			CustomCSVWriter.writeNext(itr.next());
		}
		CustomCSVWriter.close();
	}
}
