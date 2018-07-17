package com.orange.util.others;

import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.util.CustomCSVWriter;
import com.orange.util.csm.ConnectionForGOLD;

public class SiteCodeForAddressIds {

	public static void main(String ad[]) {
		String readFilePath = "C:\\Lalit\\Gold-Assignment\\CSM\\GOLD templates_previous Runs\\Dedup_GOLD_Consolidated-1.csv";
		String writeFilePath = "C:\\Lalit\\Gold-Assignment\\CSM\\GOLD templates_previous Runs\\Dedup_GOLD_Consolidated_Updated-1.csv";
		try {
			new SiteCodeForAddressIds(readFilePath, writeFilePath);
			
		} catch (Exception e) {

			JOptionPane.showMessageDialog(null,e);
		}

	}
	Connection con;
	CustomCSVWriter csvwriter;

	PreparedStatement pstmt;

	public SiteCodeForAddressIds()throws Exception{
		con = getConnection();

		String query = "select sitecode,status from eq_site where address_id = ?";
		pstmt = ConnectionForGOLD.getPreparedStatement(query);
		pstmt.setString(1, "MAG4205882");
		ResultSet resultSet = pstmt.executeQuery();

		while (resultSet.next()) {
			String SC = resultSet.getString(1);
			String Status = resultSet.getString(2);
			System.out.println(SC);
			System.out.println(Status);
		}
		resultSet.close();
		pstmt.close();
		
	}
	public SiteCodeForAddressIds(String firstfilePath, String writeFilePath)
			throws Exception {
		System.out.println("Start .. Please wait ...");
		con = getConnection();

		String query = "select sitecode,status from eq_site where address_id = ?";
		CSVReader reader = new CSVReader(new FileReader(firstfilePath));
		csvwriter = new CustomCSVWriter(new FileWriter(writeFilePath),true);
		csvwriter.writeNext(reader.readNext());

		String row[] = null;
		int count = 0;
		while ((row = reader.readNext()) != null) {
			String addressId = row[0];
			String siteCode = row[1];

			pstmt = ConnectionForGOLD.getPreparedStatement(query);
			pstmt.setString(1, addressId.trim());

			ResultSet resultSet = pstmt.executeQuery();

			ArrayList<String[]> sCodes = new ArrayList<String[]>();
			while (resultSet.next()) {
				String SC = resultSet.getString(1);
				String Status = resultSet.getString(2);
				String arr[] = new String[2];
				arr[0] = SC.trim();
				arr[1] = Status.trim();
				sCodes.add(arr);
			}

			String code = "";
			boolean isMatchFound = false;
			if (sCodes.size() > 1) {
				for (String S[] : sCodes) {

					if (S[0].trim().equalsIgnoreCase(siteCode.trim())) {
						// Give Pref to the available sitecode in sheet if
						// multiple found
						code = S[0];
						isMatchFound = true;
					}
				}
				/*
				 * If no match found , then give preference to Active sitecode
				 * only. If No active found , then only false recored.
				 */
				if (!isMatchFound) {
					boolean isAnyTrue = false;
					for (String S[] : sCodes) {
						String status = S[1];
						if (status.equalsIgnoreCase("0")) {
							isAnyTrue = true;
						}
					}
					if (isAnyTrue) {
						for (String S[] : sCodes) {
							String status = S[1];
							if (status.equalsIgnoreCase("0")) {
								code += S[0] + "|";
							}
						}
					} else {
						for (String S[] : sCodes) {
							code += S[0] + "(False)|";
						}
					}
				}
			} else {
				if (!sCodes.isEmpty()) {
					String arr[] = sCodes.get(0);
					code = arr[0];
				}
				if (sCodes.isEmpty()) {
					code = "";
				}
			}
			int len = code.length();
			if (len > 0) {
				char c = code.charAt(len - 1);
				if (c == '|') {
					code = code.substring(0, len - 1);
				}
			}
			row[1] = code;
			// row[7] = "";
			resultSet.close();
			// }
			pstmt.close();
			//csvwriter.writeNext(chageString(row));
			csvwriter.writeNext(row);
			count++;
		}
		reader.close();
		csvwriter.close();
		System.out.println("Completed..." + count);
	}

	
	
	

	private Connection getConnection() throws Exception {
		Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
		String url = "jdbc:oracle:thin:@10.237.93.57:1521:goldusid";
		return DriverManager.getConnection(url, "goldusid", "goldusid");
	}
}
