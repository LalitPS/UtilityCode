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

public class MergeMigratedReport {

	private LinkedHashSet<String> countandPath = new LinkedHashSet<String>();

	public LinkedHashSet<String> getCountandPath() {
		return countandPath;
	}

	public void mergeMigratedReportData(String basePath) throws IOException {
		File file = new File(basePath);
		File[] fileList = file.listFiles();

		if (fileList != null) {

			for (int i = 0; i < fileList.length; i++) {
				if (fileList[i].isDirectory()) {
					File migrationFile1 = new File(basePath + Constants.seperator+ fileList[i].getName() + Constants.seperator+ Constants.migrationMigratedOrderReport);
					File migrationFile2 = new File(basePath + Constants.seperator+ fileList[i].getName() + Constants.seperator+ Constants.migrationMigratedOrderByPassReport);
				
					if (migrationFile1.exists() && migrationFile2.exists()) {

						LinkedHashSet<String[]> migrationOrderReportSet = readMigrationReportCSV(	migrationFile1.getAbsolutePath(),migrationFile2.getAbsolutePath());

						retrieveValuesFromList(basePath+Constants.seperator+ fileList[i].getName()+ Constants.seperator+ Constants.migrationMigratedOrderReport,migrationOrderReportSet);

						migrationFile1 = null;
						migrationFile2 = null;

					}

				}
				mergeMigratedReportData(String.valueOf(fileList[i]));
			}

		}
	}

	public LinkedHashSet<String[]> readMigrationReportCSV(
			String migrationReportFilePath, String migrationReportByPassFilePath) throws IOException {

		LinkedHashSet<String[]> mergeOrderReportSet = new LinkedHashSet<String[]>();
		LinkedHashSet<String[]> mergeOrderReportSetUpdated = new LinkedHashSet<String[]>();

		CSVReader csvReader = new CSVReader(new FileReader(migrationReportFilePath));
		String csvRow[];
		
			while ((csvRow = csvReader.readNext()) != null) {
				mergeOrderReportSet.add(csvRow);
			}

			csvReader.close();
			Iterator<String[]> itr = mergeOrderReportSet.iterator();

			while (itr.hasNext()) {
				csvReader = new CSVReader(new FileReader(migrationReportByPassFilePath));
				String migratedReportRow[] = itr.next();
				boolean flag = true;
				String reportByPasscsvRow[];
				while ((reportByPasscsvRow = csvReader.readNext()) != null) 
				{
					flag = true;
					if (reportByPasscsvRow.length >= 3) {
						for(String comment : Constants.dataByPassMatch)
						{
						if ((migratedReportRow[0].equals(reportByPasscsvRow[0])	
								&&
								migratedReportRow[1].equals(reportByPasscsvRow[1])	
								&&
								migratedReportRow[2].equals(reportByPasscsvRow[2])	
								&&
								migratedReportRow[3].equals("Failed")
								&&
								migratedReportRow[4].equals(comment)
								)&& reportByPasscsvRow[3].equals("Updated")) 
						{

							migratedReportRow[3] = "Updated";
							migratedReportRow[4] = "";
							mergeOrderReportSetUpdated.add(migratedReportRow);
							flag = false;
							break;
						}
						}
					}
				}
				csvReader.close();
				if (flag) {
					mergeOrderReportSetUpdated.add(migratedReportRow);
				}

			}

		return mergeOrderReportSetUpdated;
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
