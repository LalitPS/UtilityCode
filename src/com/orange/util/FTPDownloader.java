package com.orange.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FTPDownloader {

    public static void main(String[] args) {
        try {
        	
        	FTPSettings FTPSettings = new FTPSettings();
        	
            FTPDownloader ftpDownloader =new FTPDownloader(FTPSettings.getFtpURL(), FTPSettings.getFtpUser(), FTPSettings.getFtpPassword());
         //ftpDownloader.downloadFile("/releasedata/DEV/CSM_SQLS/Lalit/Imadaq.jar", "Imadaq.jar");
            ftpDownloader.downloadDir ("/releasedata/DEV/CSM_SQLS/Lalit/Imadaq_lib", "Imadaq_lib");
            System.out.println("FTP File downloaded successfully");
            ftpDownloader.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    FTPClient ftpClient = null;

    public FTPDownloader(String host, String user, String pwd) throws Exception {
        ftpClient = new FTPClient();
        /*
         * 
         * HIDE THE FTP LOGS FROM CONSOLE 
         * ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
         */
        //ftpClient.setConnectTimeout(159000);
        
        ftpClient.connect(host);
        int reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) 
        {
            ftpClient.disconnect();
            throw new Exception("Exception in connecting to FTP Server");
        }
        ftpClient.login(user, pwd);
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.enterLocalPassiveMode();
       
    }
    
    public  FTPClient getFTPClient(){
    	return ftpClient;
    }

    public void disconnect() {
        if (this.ftpClient.isConnected()) {
            try {
                this.ftpClient.logout();
                this.ftpClient.disconnect();
            } catch (IOException f) {
                // do nothing as file is already downloaded from FTP server
            }
        }
    }

    public void downloadFile(String remoteFilePath, String localFilePath) {
        try {
        	FileOutputStream fos = new FileOutputStream(localFilePath);
            this.ftpClient.retrieveFile(remoteFilePath, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void downloadDir(String remoteDir, String base)
    {
        File basedir = new File(base);
        basedir.mkdirs();

        try
        {
        	
            FTPFile[] ftpFiles = ftpClient.listFiles(remoteDir);
            for (FTPFile file : ftpFiles)
            {
                if (!file.getName().equals(".") && !file.getName().equals("..")) {
                    // If Dealing with a directory, change to it and call the function again
                    if (file.isDirectory())
                    {
                        // Change working Directory to this directory.
                        ftpClient.changeWorkingDirectory(file.getName());
                        
                       // Create the directory locally - in the right place
                        File newDir = new File (base + "/" + ftpClient.printWorkingDirectory());
                        newDir.mkdirs();
                        
                        
                        // Recursive call to this method.
                        downloadDir(ftpClient.printWorkingDirectory(), base);

                        // Come back out to the parent level.
                        ftpClient.changeToParentDirectory();
                    }
                    else
                    {
                        ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                        String remoteFile1 = remoteDir + "/" + file.getName();
                        File downloadFile1 = new File(base + "/" + file.getName());
                        if(!downloadFile1.exists())
                        {
                        	 OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
                             ftpClient.retrieveFile(remoteFile1, outputStream1);
                             outputStream1.close();
                        }
                        else{
                        	System.out.println("File "+downloadFile1+" Already available..");
                        }
                       
                    }
                }
            }
        }
        catch(IOException ex)
        {
            System.out.println(ex);
        }
    }
}
