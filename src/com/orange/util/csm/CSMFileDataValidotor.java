package com.orange.util.csm;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class CSMFileDataValidotor {

	public ArrayList<String[]> getQueryListResult(
			DedupFileValidator cSMFileValidator,
			ArrayList<StringBuilder> queryBuilderList) throws SQLException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, IOException {

		ArrayList<StringBuilder> queryList = queryBuilderList;
		ArrayList<String[]> rows = new ArrayList<String[]>();
		int count = 1;
		int columncount = 0;
		for (StringBuilder queryBuilder : queryList) {
			
			String query = queryBuilder.toString();
			System.out.println(query);
			ResultSet resultSet = getQueryResult(query);
			if (count == 1) {
				ResultSetMetaData metaData = resultSet.getMetaData();
				columncount = metaData.getColumnCount();
				String header[] = new String[columncount];
				for (int column = 0; column < columncount; column++) {
					header[column] = metaData.getColumnName(column + 1);
				}
				rows.add(header);
				count++;
			}
			while (resultSet.next()) {
				String row[] = new String[columncount];
				for (int column = 0; column < columncount; column++) {
					String data = resultSet.getString(column + 1);
					row[column] = data;
				}
				rows.add(row);
			}
			resultSet.close();
		}
		
		return rows;

	}

	public ArrayList<String[]> getQueryResult(
			DedupFileValidator cSMFileValidator, StringBuilder queryBuilder)
			throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException, IOException {

		String query = queryBuilder.toString();
		// System.out.println(query);
		ResultSet resultSet = getQueryResult(query);
		ResultSetMetaData metaData = resultSet.getMetaData();

		int columncount = metaData.getColumnCount();
		String header[] = new String[columncount];
		ArrayList<String[]> rows = new ArrayList<String[]>();
		for (int column = 0; column < columncount; column++) {
			header[column] = metaData.getColumnName(column + 1);
		}
		rows.add(header);
		while (resultSet.next()) {
			String row[] = new String[columncount];
			for (int column = 0; column < columncount; column++) {
				String data = resultSet.getString(column + 1);
				row[column] = data;
				
			}
			rows.add(row);
		}
		resultSet.close();
		return rows;

	}

	private ResultSet getQueryResult(String query) throws SQLException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, IOException {
		Statement pstmt = ConnectionForGOLD.getStatement();
		ResultSet rs = pstmt.executeQuery(query);
				
		return rs;
	}

}
