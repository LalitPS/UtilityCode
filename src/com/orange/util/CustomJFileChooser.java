package com.orange.util;

import java.io.File;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.orange.ui.component.custom.Directories;

public class CustomJFileChooser extends JFileChooser{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	FileNameExtensionFilter criteriafilter;
	FileNameExtensionFilter csvfilter;
	FileNameExtensionFilter jsonfilter;
	
	public CustomJFileChooser(){
		super(Directories.BASEDIR);
		
		Properties properties = CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV02);
		
		setCurrentDirectory( new File(properties.getProperty("DefaultBrowseLocation")));
		setFileSelectionMode(CustomJFileChooser.FILES_ONLY);
		csvfilter = new FileNameExtensionFilter("CSV FILES ONLY", "csv", "csv");
		jsonfilter = new FileNameExtensionFilter("JSON FILES ONLY", "json", "json");
		criteriafilter = new FileNameExtensionFilter("CRITERIA FILES ONLY", "criteria", "criteria");
		
		setFileFilter(jsonfilter);
		setFileFilter(criteriafilter);
		setFileFilter(csvfilter);
		addChoosableFileFilter (csvfilter);
		
		
	}
	public FileNameExtensionFilter getCriteriafilter() {
		return criteriafilter;
	}
	public FileNameExtensionFilter getCsvfilter() {
		return csvfilter;
	}
	
	
	public FileNameExtensionFilter getJsonfilter() {
		return jsonfilter;
	}
	
}
