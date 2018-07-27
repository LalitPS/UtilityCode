package com.orange.util.cibase.v02;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.orange.ui.component.Imadaqv02ViewComponent;
import com.orange.util.CommonUtils;

public class DetailedAnalysisThreadExecutor 
{
 public DetailedAnalysisThreadExecutor(Imadaqv02ViewComponent imadaqv02ViewComponent,ArrayList<File> fileList)
 {
	 ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
	 for(File F : fileList)
	 {
		 try{
				 DetailedAnalysis DA = new DetailedAnalysis(imadaqv02ViewComponent,F);
				executor.execute(DA);
		 	}catch(Exception E){CommonUtils.showExceptionStack(E); }
	 }
	 executor.shutdown();
 }
}
