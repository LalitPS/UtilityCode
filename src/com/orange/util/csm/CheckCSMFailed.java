package com.orange.util.csm;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.util.CustomCSVWriter;

public class CheckCSMFailed {

	static String resultFileLoc;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		resultFileLoc = "C:\\Lalit\\Gold-Assignment\\CSM\\Run8\\Result.csv";
		CheckCSMFailed cd = new CheckCSMFailed();
		String parentFile = "C:\\Lalit\\Gold-Assignment\\CSM\\Run8\\CustomerSiteMigrate.csv";
		String childFile = "C:\\Lalit\\Gold-Assignment\\CSM\\Run8\\CustomerMigrateLog02_May_16_46_44.csv";
		cd.checkDuplicateFromFile(parentFile, childFile);

	}
	LinkedHashMap<String, String[]> failedSet = new LinkedHashMap<String, String[]>();

	LinkedHashMap<String, String[]> parentHashMap = new LinkedHashMap<String, String[]>();

	private void checkDuplicateFromFile(String parentFile, String ChildFile) {

		String[] parentRow = null;
		String[] childRow = null;
		try {
			CSVReader parent = new CSVReader(new FileReader(parentFile));
			parent.readNext();

			while ((parentRow = parent.readNext()) != null) {
				parentHashMap.put(parentRow[0] + parentRow[4] + parentRow[2],
						parentRow);

			}
			parent.close();
			CSVReader child = new CSVReader(new FileReader(ChildFile));

			while ((childRow = child.readNext()) != null) {
				if (childRow[4].equalsIgnoreCase("Fail")) {
					failedSet.put(childRow[0] + childRow[1].replace("''", "'")
							+ childRow[2], childRow);

				}
			}
			child.close();
			findFailedRow(parentHashMap, failedSet);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void findFailedRow(LinkedHashMap<String, String[]> parentHashSet,
			LinkedHashMap<String, String[]> failedSet) throws IOException {
			CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(resultFileLoc),true);

			for (String osr : failedSet.keySet()) {

				if (parentHashMap.containsKey(osr)) {
					String row[] = parentHashMap.get(osr);
					writer.writeNext(row);
				}

			}
			writer.close();
	
	}

}
