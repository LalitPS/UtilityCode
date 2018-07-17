package com.orange.util.earrach;

import java.awt.Frame;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

import com.orange.ui.component.EarrachExtractionView;
import com.orange.util.CommonUtils;

public class EarrachJSONSeparator {

	private EarrachExtractionView earrachExtractViewComponents;
	private String filePath;
	private int seqInaFile=0; 
	private ArrayList<Integer>  seqPlaceHolder;
	private String suffix ="]}}]}";
	
		
	public EarrachJSONSeparator(String filePath,EarrachExtractionView earrachExtractViewComponents) throws Exception
	{
		this.filePath = filePath;
		this.earrachExtractViewComponents = earrachExtractViewComponents;
		seqPlaceHolder = new ArrayList<Integer>();
	
		earrachExtractViewComponents.getQueryResult().append("Please wait .. this process takes few moments .....\n");
		FileInputStream inputStream = new FileInputStream(filePath);
		
		Scanner sc = new Scanner(inputStream, "UTF-8");
		
			Integer totalSEQCount=0;
		    while (sc.hasNextLine()) 
		    {
		        String line = sc.nextLine();
		      
		        if(null != line && line.contains("\"sequenceId\":"))
		        {
		        	seqPlaceHolder.add(totalSEQCount);
		        	
		        }
		        totalSEQCount++;
		    }
		    seqPlaceHolder.add(totalSEQCount);
		    earrachExtractViewComponents.getQueryResult().append("Total Lines in File "+totalSEQCount+"\n");
		    sc.close();
		    inputStream.close();
		    String number = JOptionPane.showInputDialog(new Frame(), "Total "+totalSEQCount +" Lines Found in file.\n \n Total SequenceId "+ (seqPlaceHolder.size()-1) +" found in file.\nPlease insert the number of sequenceId required in a File.",((seqPlaceHolder.size()-1)/10));
		    seqInaFile = Integer.parseInt(number);
		    
		    
		    earrachExtractViewComponents.getQueryResult().append("File Read successfully...\n");
		    boolean B = writeInFiles();
		    if(B)
		    {
		    JOptionPane.showMessageDialog(new Frame(),"File(s) has been created successfully...\n You may use , these newly created files for further actions.");
		    }
		   
	}
		
	private void configureJSON(String fileName,boolean isLast) throws IOException
	{
		earrachExtractViewComponents.getQueryResult().append("File "+fileName+ " JSON Validation start..\n");
		FileInputStream inputStream = new FileInputStream(fileName);
		Scanner sc = new Scanner(inputStream, "UTF-8");
		
		StringBuilder sb = new StringBuilder();
		while (sc.hasNextLine()) 
		    {
			sb.append(sc.nextLine()+"\n");
		    }
		
		String SS = sb.toString();
		int index = SS.lastIndexOf("]");
	
		SS = SS.substring(0,index);
		
		if(isLast){
			index = SS.lastIndexOf("]");
			SS = SS.substring(0,index);
		}
		SS+=suffix;
	
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		writer.write(SS);
		writer.close();
		sc.close();
		inputStream.close();
		sb = null;
		
		if(CommonUtils.isJSONValid(fileName,earrachExtractViewComponents))	
		{
			earrachExtractViewComponents.getQueryResult().append("File "+fileName+ " JSON validated successfully..\n");	
		}
		else
		{
			earrachExtractViewComponents.getQueryResult().append("========================================================\n");
			earrachExtractViewComponents.getQueryResult().append("File "+fileName+ " JSON validation Failed......\n");
			earrachExtractViewComponents.getQueryResult().append("========================================================\n");
		}
		
	
	}
	
	
	
	
	private void createZip(String zipPath,String filePath) throws IOException{
		
		FileOutputStream fos = new FileOutputStream(zipPath);
		ZipOutputStream zos = new ZipOutputStream(fos);
		
		File file = new File(filePath);
		FileInputStream fis = new FileInputStream(file);
		ZipEntry zipEntry = new ZipEntry(file.getName());
		zos.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}

		zos.closeEntry();
		fis.close();
		zos.close();
		fos.close();
		earrachExtractViewComponents.getQueryResult().append("ZIP File "+zipPath +" Created for "+filePath+"\n");
	}
	private boolean writeInFiles()throws Exception
	{
		
		String path = filePath;	
		int index = path.lastIndexOf(".");
		String sub = path.substring(0,index) ;
		
		
		int totalSEQID = seqPlaceHolder.size();
		int equalPart = seqInaFile;
		
		if(equalPart <=0)
		{
			JOptionPane.showMessageDialog(null,"File Can not be divided more.");
			return false;
		}
		
		int inParts = totalSEQID/equalPart;
		
		
		if((totalSEQID-1) == equalPart){
			JOptionPane.showMessageDialog(null,"File Can not be divided further.");
			return false;
		}
		
		if((totalSEQID-1) < equalPart){
			JOptionPane.showMessageDialog(null,"File Can not be divided.");
			return false;
		}
		
		int XX = ((totalSEQID-1)%equalPart);
		if(XX!=0)
		{
			inParts++;
		}
		if(seqInaFile ==1)
		{
			inParts = (inParts-1);
		}
		
		
		String filesNames[] = new String[inParts];
		String zipfilesNames[] = new String[inParts];
		
		for(int x = 0 ;x < filesNames.length; x++)
		{
			filesNames[x] = sub+"_"+(x+1)+".json";
			zipfilesNames[x] = sub+"_"+(x+1)+".zip";
		}
		
		earrachExtractViewComponents.getQueryResult().append("Total SequenceId found  "+(totalSEQID-1) +" and Equal Part is  "+equalPart+"\n");
		
		FileInputStream inputStream = new FileInputStream(filePath);
		Scanner sc = new Scanner(inputStream, "UTF-8");
		
		int co=0;
		int X=0;
		
		while (sc.hasNextLine()) 
		    {
			for(X=0 ; X < inParts ; X++)
				{
					BufferedWriter writer = new BufferedWriter(new FileWriter(filesNames[X]));
					String prefix ="";
					if(X!=0)
					{
						prefix ="{\"IOVs\":[{\n";
						writer.write(prefix);
					}
					
					for(int P = (co == 0 ? 0 :seqPlaceHolder.get(co));P < seqPlaceHolder.get(((co+equalPart) >(totalSEQID-1) ?  (totalSEQID-1) : (co+equalPart)));P++)
					{
						    String RR= sc.nextLine();
							writer.write(RR+"\n");
					}
					writer.close();
					
					
					earrachExtractViewComponents.getQueryResult().append("File "+(X+1)+" OF "+(inParts) +" Temp Created ."+filesNames[X]+"\n");
					
					boolean isLast = false;
					if((X+1) == (inParts))
					{
						isLast = true;
					}
					configureJSON(filesNames[X],isLast);
					createZip(zipfilesNames[X],filesNames[X]);
					co+=(equalPart);
					
				}
				
				sc.close();
				inputStream.close();
				break;
				
	   }
		return true;
	}
	
}
