package com.orange.util;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 Logger logger = Logger.getLogger(CommonUtils.class.getName());
			
		 String log4jConfigFile ="C:/workspace/GOLDFastTrack/src/log4j.properties";
		 PropertyConfigurator.configure(log4jConfigFile);
	}

}
