package com.orange.util;

import java.io.Writer;

import au.com.bytecode.opencsv.CSVWriter;


public class CustomCSVWriter extends CSVWriter{

	
	public CustomCSVWriter(Writer writer) {
		/*
		 * will not use " " surround the value(s) to separate with comma.
		 * Ex : A,B,C
		 */
		
		super(writer, ',', CSVWriter.NO_QUOTE_CHARACTER);
	}

	public CustomCSVWriter(Writer writer,boolean defaulted){
		/*
		 * will use " " surround the value(s) to separate with comma.
		 * Ex : "A","B","C"
		 */
		
		super(writer);
		
	}
}
