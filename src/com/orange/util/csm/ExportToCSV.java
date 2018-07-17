package com.orange.util.csm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTable;

import com.orange.ui.component.DedupViewComponents;
import com.orange.util.CustomCSVWriter;

public class ExportToCSV {

	private DedupViewComponents cSMViewComponents;
	private String dirLocation ;
	private String formatSuffix="yyyy-MM-dd-HH";
	private JTable table;
	private String tabName;
	private String updatedDirLocation;
	
	public ExportToCSV(JTable table,String tabName,DedupViewComponents cSMViewComponents){
		this.table =table;
		this.tabName= tabName;
		this.cSMViewComponents = cSMViewComponents;
		int index = this.cSMViewComponents.getFileToValidate().getText().lastIndexOf(File.separator);
		String sub = this.cSMViewComponents.getFileToValidate().getText().substring(0,index) ;
		this.dirLocation = sub;
		updatedDirLocation = createExportLocation();
	}
	private String createExportLocation(){
	
		Format formatter = new SimpleDateFormat(formatSuffix);
		Date d = new Date();
		String format = formatter.format(d);
		
		String updatedDirLocation = dirLocation+File.separator+format;
		File F = new File(updatedDirLocation);
		if(!F.exists()){
			F.mkdirs();
		}
		return updatedDirLocation;
	}
	
	public void export() throws IOException{
		
		CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(updatedDirLocation+"\\"+getExportFileName(tabName)),true);
		int columncount = table.getColumnCount();
		int rowcount = table.getRowCount();
		String rowdata[] = new String[columncount];
		
		for(int column = 0 ;column < columncount ; column++)
		{
			String colName =table.getColumnName(column);
			rowdata[column] = (colName.equals("null") || colName.isEmpty()? "":colName);
		}
	    writer.writeNext(rowdata);
		
		for(int row = 0 ;row < rowcount ; row++)
		{
			for(int column = 0 ;column < columncount ; column++)
			{
			String colvalue = ""+table.getValueAt(row, column);
			rowdata[column] = (colvalue.equals("null") ? "":colvalue);
		    }
		    writer.writeNext(rowdata);
		}
		writer.close();
	}
	
	private String getExportFileName(String tabName){
		return tabName.replaceAll(" ", "_")+".csv";
	}
}
