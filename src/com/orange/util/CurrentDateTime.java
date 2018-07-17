package com.orange.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CurrentDateTime {

	
	
	public static String getDateTimeText(){
		
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MMMMM.dd GGG hh:mm aaa");
	    return sdf.format(date);
	    
	}
	
	
}
