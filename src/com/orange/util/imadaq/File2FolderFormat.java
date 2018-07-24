package com.orange.util.imadaq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

public class File2FolderFormat {

	private final static Logger LOGGER = Logger
			.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private static void copyFile(String source, String destination) throws IOException {
		InputStream inStream = null;
		OutputStream outStream = null;
		
			File file1 = new File(source);
			File file2 = new File(destination);
			inStream = new FileInputStream(file1);
			outStream = new FileOutputStream(file2);

			byte[] buffer = new byte[1024];
			int length;
			while ((length = inStream.read(buffer)) > 0) {
				outStream.write(buffer, 0, length);
			}
			if (inStream != null)
				inStream.close();
			if (outStream != null)
				outStream.close();

		

	}

	private static void dirCreation(String dirlocation, String fileName) throws IOException {
		int lastIndexOf_ = fileName.lastIndexOf("_");
		int lastIndexOfDot = fileName.lastIndexOf(".");
		if (lastIndexOf_ > 0 && lastIndexOfDot > 0) {
			String number = fileName
					.substring(lastIndexOf_ + 1, lastIndexOfDot);

			String newfilename = fileName.substring(0, lastIndexOf_);
			String newfileext = fileName.substring(lastIndexOfDot);
			if (".csv".equals(newfileext)) {
				try {
					Integer.parseInt(number);
					File dir = new File(dirlocation +File.separator+ Constants.directoryPrefix + number);

					/*
					 * Stop sub folder creation for _Left_<number>.csv file.
					 * while refreshing or load Tree...
					 * 
					 * Stop sub folder creation for <***>_Execution folders
					 * file(s) file. while refreshing or load Tree..
					 */
				
					if (!fileName.contains(Constants.exemptLeftFileFolder)&& !fileName.contains(Constants.packageMigratedFilesPrefix)&& !fileName.contains(Constants.packageImpactedFilesPrefix)) {
						if (!dir.exists()) 
						{
							dir.mkdir();
							copyFile(dirlocation + File.separator + fileName, dirlocation	+ File.separator  + Constants.directoryPrefix + number	+ File.separator  + newfilename + newfileext);
						} 
					}

				} catch (NumberFormatException nfe) {
					LOGGER.severe(nfe.getMessage());

				}
			} else {
				LOGGER.warning(" File name " + dirlocation + "\\" + fileName
						+ " is not csv .. Hence ignore..");
			}
		}

	}

	public static void listf(String directoryName) throws IOException {
		File file = new File(directoryName);
		File[] fileList = file.listFiles();

		if (fileList != null) {

			for (int i = 0; i < fileList.length; i++) {
				if (fileList[i].isFile()) {
					String fileName = fileList[i].getName();
					dirCreation(directoryName, fileName);
				} else if (fileList[i].isDirectory()) {
					listf(String.valueOf(fileList[i]));
				}

			}

		}

	}

	public File2FolderFormat() {

	}

}
