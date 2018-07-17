package com.orange.util.imadaq;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;

public class ValidateReport {

	LinkedHashSet<LinkedHashSet<String>> uncommonParrent = new LinkedHashSet<LinkedHashSet<String>>();

	private LinkedHashSet<String> findUnCommon(Set<String> orderReportSet,
			Set<String> migrationFileSet, Set<String> migrationReportFileSet,
			String path) {

		LinkedHashSet<String> uncommon = new LinkedHashSet<String>();
		for (String elemnent : orderReportSet) {
			
			if (!migrationFileSet.contains(elemnent)) {
				uncommon.add(path + " USIDMigrationDataFile does not contain("+ elemnent + ") but found in ImpectedOrderReport.");

			}
			if (!migrationReportFileSet.contains(elemnent)) {
				uncommon.add(path + " MigrationMigratedOrderReport does not contain("+ elemnent + ") but found in ImpectedOrderReport.");

			}
		}

		for (String elemnent : migrationFileSet) {
			if (!orderReportSet.contains(elemnent)) {
				uncommon.add(path + "ImpectedOrderReport does not contain("+elemnent+") but found in USIDMigrationDataFile.");

			}
			if (!migrationReportFileSet.contains(elemnent)) {
				uncommon.add(path
						+ " MigrationMigratedOrderReport does not contain("+elemnent+") but found in USIDMigrationDataFile.");

			}
		}

		for (String elemnent : migrationReportFileSet) {
			if (!orderReportSet.contains(elemnent)) {
				uncommon.add(path + " ImpectedOrderReport does not contain("+elemnent+") but found in MigrationMigratedOrderReport.");

			}
			if (!migrationFileSet.contains(elemnent)) {
				uncommon.add(path + " USIDMigrationDataFile does not contain("+elemnent+") but found in MigrationMigratedOrderReport.");

			}
		}

		return uncommon;
	}

	public LinkedHashSet<LinkedHashSet<String>> getUncommonParrent() {
		return uncommonParrent;
	}

	public LinkedHashSet<String> readOrderReportCSV(String csvFilePath) throws IOException {
		LinkedHashSet<String> orderReportSet = new LinkedHashSet<String>();
		CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));
			/*
			 * Pass First Row {Header Info}
			 */
			String csvData[] =csvReader.readNext();
			while ((csvData = csvReader.readNext()) != null) {
				if (csvData.length > 0) {
					orderReportSet.add(csvData[0].trim());
				}
			}
			csvReader.close();	
		return orderReportSet;
	}

	public void validateReports(String basePath) throws IOException {

		File file = new File(basePath);
		File[] fileList = file.listFiles();

		if (fileList != null) {

			for (int i = 0; i < fileList.length; i++) {
				if (fileList[i].isDirectory()) {
					File impectedOrderReportFile = new File(basePath + Constants.seperator+ fileList[i].getName()+ "\\USIDMigrationImpactedOrderReport.csv");
					File usidMigrationFile = new File(basePath + Constants.seperator+ fileList[i].getName() + "\\USIDMigrationData.csv");
					File migrationReportFile = new File(basePath + Constants.seperator+ fileList[i].getName()+ "\\USIDMigrationMigratedOrderReport.csv");

					if (impectedOrderReportFile.exists() && usidMigrationFile.exists()&& migrationReportFile.exists()) {
						LinkedHashSet<String> migrationFileSet = readOrderReportCSV(usidMigrationFile.getAbsolutePath());
						LinkedHashSet<String> impectedOrderReportSet = readOrderReportCSV(impectedOrderReportFile.getAbsolutePath());
						LinkedHashSet<String> migrationReportFileSet = readOrderReportCSV(migrationReportFile.getAbsolutePath());

						LinkedHashSet<String> uncommon = findUnCommon(impectedOrderReportSet, migrationFileSet,migrationReportFileSet, basePath + Constants.seperator+ fileList[i].getName());
						
						if (uncommon.size() > 0) {
							uncommonParrent.add(uncommon);
						}

						impectedOrderReportFile = null;
						usidMigrationFile = null;
						migrationReportFile = null;
						impectedOrderReportSet = null;
						migrationFileSet = null;
						migrationReportFileSet = null;

					}

				}
				validateReports(String.valueOf(fileList[i]));
			}

		}
	}

}
