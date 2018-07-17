package com.orange.util;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;

import com.orange.ui.component.custom.CSMJTextArea;
import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.Icons;

public class ELKConsole {

	private CustomJFrame frame;
	private JTabbedPane jtabbedPane;
	private CSMJTextArea econsole,lconsole,kconsole;
	
	public ELKConsole()
	{
		frame = new CustomJFrame("ELK Properties",Icons.iconPath4);
		jtabbedPane = new JTabbedPane();
		
		econsole 	=new CSMJTextArea();
		lconsole	 	=new CSMJTextArea();
		kconsole 	=new CSMJTextArea();
		
		jtabbedPane.addTab("ElassticSearch",  	new ImageIcon(""), econsole, "econsole");
		jtabbedPane.addTab("Logstash",  			new ImageIcon(""), lconsole, "lconsole");
		jtabbedPane.addTab("Kibana",  				new ImageIcon(""), kconsole, "kconsole");
		
		frame.pack();
		frame.add(jtabbedPane,BorderLayout.CENTER);
		frame.setSize(600,450);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	
	public CSMJTextArea getEconsole() {
		return econsole;
	}


	public CSMJTextArea getLconsole() {
		return lconsole;
	}


	public CSMJTextArea getKconsole() {
		return kconsole;
	}

/*
	public static void main(String ad[]){
		final ELKConsole elkConsole = new ELKConsole();
		
		try{
		Properties prop = CommonUtils.loadFTPConfigProp(Directories.elkConfigFileLocation);
	     
	     final String KIBANA_HOME						=prop.getProperty("KIBANA_HOME");
	     final String  ELASTICSEARCH_HOME		=prop.getProperty("ELASTICSEARCH_HOME");
	     final String LOGSTASH_HOME				=prop.getProperty("LOGSTASH_HOME");
	     String LOGSTASH_CONFIG 					=prop.getProperty("LOGSTASH_CONFIG");
	     String PREFIX_COMMAND 						=prop.getProperty("PREFIX_COMMAND");
	     
	     
	     File kibanatemp 		= File.createTempFile("kibanatemp", ".bat"); 
	     File elestictemp 		= File.createTempFile("elestictemp", ".bat"); 
	     File logstashtemp 		= File.createTempFile("logstashtemp", ".bat"); 
	     
	     	String BASE ="@ECHO OFF \n  MODE con:cols=40 lines=20 |";
	     
	        PREFIX_COMMAND =( (null == PREFIX_COMMAND || PREFIX_COMMAND.length()==0 || PREFIX_COMMAND.isEmpty())?"":PREFIX_COMMAND+"|");
	     
	        FileWriter kibanatempfw         = new FileWriter(kibanatemp);
	        BufferedWriter kibanatempbw = new BufferedWriter(kibanatempfw);
	        kibanatempbw.write(BASE+PREFIX_COMMAND+KIBANA_HOME+File.separator+"bin"+File.separator+"kibana.bat");
	        kibanatempbw.close();
	        kibanatempfw.close();
	        
	        FileWriter elestictempfw         = new FileWriter(elestictemp);
	        BufferedWriter elestictempbw = new BufferedWriter(elestictempfw);
	        elestictempbw.write(BASE+PREFIX_COMMAND+ELASTICSEARCH_HOME+File.separator+"bin"+File.separator+"elasticsearch.bat");
	        elestictempbw.close();
	        elestictempfw.close();
	        
	   
	        
	        FileWriter logstashtempfw         = new FileWriter(logstashtemp);
	        BufferedWriter logstashtempbw = new BufferedWriter(logstashtempfw);
	        logstashtempbw.write(BASE+PREFIX_COMMAND+LOGSTASH_HOME+File.separator+"bin"+File.separator+"logstash.bat  -f "+LOGSTASH_CONFIG);
	        logstashtempbw.close();
	        logstashtempfw.close();
	        
	        
	    Runtime.getRuntime(). exec("cmd /c start  "+kibanatemp);
	    Runtime.getRuntime(). exec("cmd /c start \"\" "+elestictemp);
	    Runtime.getRuntime(). exec("cmd /c start \"\" "+logstashtemp);
	    
	    
	    Timer timer = new Timer();
	     timer.scheduleAtFixedRate(new TimerTask() {

	         @Override
	         public void run() 
	         {
	        	 try{
	        	 elkConsole.getEconsole().setText("");
	        	 elkConsole.getLconsole().setText("");
	        	 elkConsole.getKconsole().setText("");
	        	 
	        	 elkConsole.getEconsole().append(elkConsole.readFile(ELASTICSEARCH_HOME+File.separator+"logs"+File.separator+"elasticsearch.log").toString());
			   	 elkConsole.getLconsole().append(elkConsole.readFile(LOGSTASH_HOME       +File.separator+"logs"+File.separator+"logstash-plain.log").toString());
			   	 elkConsole.getKconsole().append(elkConsole.readFile(KIBANA_HOME             +File.separator+"logs"+File.separator+"kibana.log").toString());
	        	 }catch(Exception e){}
	         }

	     }, 0, 5000);
	    
	    
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private StringBuilder readFile(String filePath) throws IOException{
		
		FileInputStream fstream = new FileInputStream(filePath);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        StringBuilder sb = new StringBuilder();
		String strLine;
	
		//Read File Line By Line
		while ((strLine = br.readLine()) != null)   
		{
			sb.append(strLine+"\n");
		}
		br.close();
		fstream.close();
		return sb;
	}
*/
}
