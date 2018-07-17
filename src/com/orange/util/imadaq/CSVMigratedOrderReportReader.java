package com.orange.util.imadaq;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.util.CustomCSVWriter;

public class CSVMigratedOrderReportReader {

	private final static Logger LOGGER = Logger
			.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private LinkedHashSet<String> countandPath = new LinkedHashSet<String>();

	public void createDataBypassFilesData(String basePath) throws IOException {

		File file = new File(basePath);
		File[] fileList = file.listFiles();

		if (fileList != null) {

			for (int i = 0; i < fileList.length; i++) {
				if (fileList[i].isDirectory()) {
					File reportFile = new File(basePath + Constants.seperator
							+ fileList[i].getName() + Constants.seperator
							+ Constants.migrationMigratedOrderReport);
					File migrationFile = new File(basePath + Constants.seperator
							+ fileList[i].getName() + Constants.seperator
							+ Constants.usidMigrationData);

					if (reportFile.exists() && migrationFile.exists()) {

						LinkedHashSet<String> migrationOrderReportSet = readMigrationReportCSV(reportFile
								.getAbsolutePath());
						LinkedHashSet<String[]> migrationDataSet = readMigrationDataCSV(migrationFile
								.getAbsolutePath());
						LinkedHashSet<String[]> filterDataSet = filterMigrationData(
								migrationOrderReportSet, migrationDataSet);

						if (filterDataSet != null && filterDataSet.size() > 0) {

							retrieveValuesFromList(basePath + Constants.seperator
									+ fileList[i].getName() + Constants.seperator
									+ Constants.migrationDataBypass,
									filterDataSet);
							
							
							String arr = "Before executing the databypass script , in case of The Target USID ServiceElement belongs to different Service element categories.\n";
							arr+="Be ensure the Target USID service element belongs to the different service element catagories,\n";
							arr+="Different service element should be IP/VPN to BVPN. \n";
							arr+="And the order handle will be in availabe of the sequence of the orders which are available in USIDMigrationMigratedOrderReport.csv for that USID.\n";
							arr+="As below example :: GO008DV58E : FI00QLKUCB	:475670	 : Failed	: The Target USID ServiceElement belongs to different Service element categories.\n";
							arr+="SELECT V_ORDHANDLE,SE_SERVICEELEMENTCLASS FROM viw_version_element_attribute T WHERE T.SE_USID = 'FI00QLKUCB';\n";
							arr+="SELECT V_ORDHANDLE,SE_SERVICEELEMENTCLASS FROM viw_version_element_attribute T WHERE T.T_VALUE = 'FI00QLKUCB';\n";
							arr+="Results are 770323	CPE and 770323	AccessConnection resp of above query.\n";
							arr+="Open that order and check the Previuous GOLD Order , this should be in available in USIDMigrationMigratedOrderReport.csv for that USID.\n";
							
					
							BufferedWriter bw = new BufferedWriter(new FileWriter(new File(basePath + Constants.seperator + fileList[i].getName() + Constants.seperator + Constants.migrationDataBypassHelp)));
							bw.write(arr);	
							bw.close();
							
						
							
							
							
						}

						LOGGER.info(" Creating ICO1 file " + basePath + Constants.seperator
								+ fileList[i].getName() + Constants.seperator
								+ Constants.migrationDataBypass);

						LinkedHashSet<String> migrationOrderReportSetForLockedOrder = readMigrationReportCSVForLockedOrder(reportFile
								.getAbsolutePath());
						LinkedHashSet<String[]> migrationDataSetForLocked = readMigrationDataCSV(migrationFile
								.getAbsolutePath());
						LinkedHashSet<String[]> filterDataSetForLocked = filterMigrationData(
								migrationOrderReportSetForLockedOrder,
								migrationDataSetForLocked);

						if (filterDataSetForLocked != null
								&& filterDataSetForLocked.size() > 0) {
							retrieveValuesFromList(basePath + Constants.seperator
									+ fileList[i].getName() + Constants.seperator
									+ Constants.lockedOrderFileName,
									filterDataSetForLocked);
						}

						LOGGER.info(" Creating ICO1 file " + basePath + Constants.seperator
								+ fileList[i].getName() + Constants.seperator
								+ Constants.lockedOrderFileName);

						reportFile = null;
						migrationFile = null;
						migrationOrderReportSet = null;
						migrationDataSet = null;

					}

				}
				createDataBypassFilesData(String.valueOf(fileList[i]));
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
				String[] migrationRow = migrationSetIterator.next();
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

	public LinkedHashSet<String[]> readMigrationDataCSV(String csvFilePath) throws IOException {
		LinkedHashSet<String[]> migrationDataSet = new LinkedHashSet<String[]>();
		CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));
		String[] csvRow;
			while ((csvRow = csvReader.readNext()) != null) {
				
				migrationDataSet.add(csvRow);
			}
			csvReader.close();
		return migrationDataSet;
	}

	public LinkedHashSet<String> readMigrationReportCSV(String csvFilePath) throws IOException {
		LinkedHashSet<String> orderReportSet = new LinkedHashSet<String>();

		LOGGER.info("Reading csv data from MigrationReportCSV " + csvFilePath);
		
			CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));
			String csvData[];
			while ((csvData = csvReader.readNext()) != null) {
				if (csvData.length > 4) {
					for (String s : Constants.dataByPassMatch) {
						if (s.equalsIgnoreCase(csvData[4])
								&& Constants.matchStatus
										.equalsIgnoreCase(csvData[3])) {
					
							orderReportSet.add(csvData[0]);
						}
					}
				}
			}
			csvReader.close();
			LOGGER.info(" Reading csv data from " + csvFilePath + " completed.");
		
			
		
		return orderReportSet;
	}

	public LinkedHashSet<String> readMigrationReportCSVForLockedOrder(
			String csvFilePath) throws IOException {
		LinkedHashSet<String> orderReportSet = new LinkedHashSet<String>();

		String csvFile = csvFilePath;
		LOGGER.info("Reading csv data from MigrationReportCSV " + csvFilePath);
		
			CSVReader csvReader = new CSVReader(new FileReader(csvFile));
			String csvData[];
		
			while ((csvData = csvReader.readNext()) != null) {
				if (csvData.length > 4) {
					for (String s : Constants.lockOrderComment) {
						if (csvData[4].contains(s)
								&& Constants.matchStatus
										.equalsIgnoreCase(csvData[3])) {
						
							orderReportSet.add(csvData[0]);
						}
					}
				}
			}
			csvReader.close();
			LOGGER.info(" Reading csv data from " + csvFilePath + " completed.");
		
		return orderReportSet;
	}

	public void retrieveValuesFromList(String path, Set<String[]> set) throws IOException {
			writeCSVFile(path, set);
			countandPath.add(path);
			
			
		
	}

	private void writeCSVFile(String path, Set<String[]> set) throws IOException {
		CustomCSVWriter CustomCSVWriter = new CustomCSVWriter(new FileWriter(path));
		
		Iterator<String[]> itr = set.iterator();
		while (itr.hasNext()) {
			String ARR[] = itr.next();
		
			CustomCSVWriter.writeNext(ARR);
		}
		CustomCSVWriter.close();
	}
}
