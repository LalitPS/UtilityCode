package com.orange.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;

import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.CustomJPanel;
import com.orange.ui.component.custom.Icons;

public class ELKCustomProperties {

	private Properties prop;
	
	private JPanel jpanel;
	private JScrollPane scrollPane ;
	private JButton update,start;
	private String  propFilePath;
	private CustomJFrame frame;
	
	public ELKCustomProperties(String propFilePath) throws IOException 
	{
		this.propFilePath = propFilePath;
		frame = new CustomJFrame("ELK Properties",Icons.iconPath4);
		jpanel = new CustomJPanel();
		jpanel.setLayout(null);
		
		prop = new Properties();
		
		Set<Object> keys = loadCustomProperties(this.propFilePath);
		  int Y =10;
		  for(Object k:keys)
		    {
	            String key = (String)k;
	            String value = prop.getProperty(key);
	            if(!key.contains("<html>"))
	            {
	            JLabel lab = new JLabel(key);
	            JTextField val = new JTextField(value);
	            lab.setBounds(10,Y,200,20);
	            val.setBounds(250,Y,300,20);
	            jpanel.add(lab);
	            jpanel.add(val);
	            Y+=25;
	            }
	        }
		  
		   update = new JButton("Update");
		   update.addActionListener(new UpdateEvent());
		   update.setBounds(150,Y+50,150,30);
		   jpanel.add(update);
		   
		   
		   
		   start = new JButton("Start");
		   start.addActionListener(new SetExecuteEvent());
		   start.setBounds(350,Y+50,150,30);
		   jpanel.add(start);
		   
		   Y+=80;
		   
		   String msg = "<html><body><ol><li>Please provide HOME path (before bin).like C:/MySoftware/kibana-6.2.4-windows-x86_64</li><li>For Config please provide absolute path like /bin/logstash.conf</li><li>seperate multiple commands in Prefix section by | operator.</li><li><font color='blue'>For Kibana logs , please chnage in KIBANA_HOME/config/kibana.yml  from '#logging.dest: stdout' to 'logging.dest:KIBANA_HOME/logs/kibana.log'</font></li><ol></body></html>";
		   JLabel message = new JLabel(msg);
		   message.setBounds(10,Y+50,630,150);
		   jpanel.add(message);
		   
		   scrollPane = new JScrollPane(jpanel);
		   scrollPane.setBackground(Color.green);
		   scrollPane.setAutoscrolls(true);
		   frame.add(scrollPane,BorderLayout.CENTER);
			
		   frame.pack();
		  
		   frame.setSize(650,450);
		   frame.setLocationRelativeTo(null);
		   frame.setVisible(true);
		  
		
		  
	}
	private Set<Object> loadCustomProperties(String propFilePath) throws IOException
	{
				InputStream input = null;
				input = new FileInputStream(propFilePath);
				prop.load(input);
			    Set<Object> keys = prop.keySet();
			    return keys;
	}

	private void updateAllComponents(String propFilePath) throws IOException{
			Properties properties = new Properties();
			JViewport view = scrollPane.getViewport();
			Component[] components =view.getComponents();
	       
	        for (Component component:components) 
	        {
	        	
	        	if (component instanceof JPanel) 
	            {
	        		JPanel P = (JPanel)component;
	        		Component[] cbcomponents =P.getComponents();
	        		String key="";
        			String values="";
	        		for (Component cbcomponent:cbcomponents) 
	    	        {
	        		
			            if (cbcomponent instanceof JLabel) 
			            {
			            	key = ((JLabel) cbcomponent).getText();
			            }
			            if (cbcomponent instanceof JTextField) 
			            {
			            	values = ((JTextField) cbcomponent).getText(); 
			            }
			            properties.setProperty(key, values);
	    	        }
	        		
	            }
	        }
	        
	        File file = new File(propFilePath);
    		FileOutputStream fileOut = new FileOutputStream(file);
    		properties.store(fileOut, "ELK CONFIG PROPERTIES");
    		fileOut.close();	
	}
	
	
	private class UpdateEvent implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0) {
		
			  try {
				updateAllComponents(propFilePath);
				frame.dispose();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	private class SetExecuteEvent implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0) {
		
			  try {
					frame.dispose();
				     Properties prop = CommonUtils.loadFTPConfigProp(propFilePath);
				     
				     final String KIBANA_HOME						=prop.getProperty("KIBANA_HOME").trim();
				     final String  ELASTICSEARCH_HOME		=prop.getProperty("ELASTICSEARCH_HOME").trim();
				     final String LOGSTASH_HOME				=prop.getProperty("LOGSTASH_HOME").trim();
				     String LOGSTASH_CONFIG 					=prop.getProperty("LOGSTASH_CONFIG").trim();
				     String PREFIX_COMMAND 						=prop.getProperty("PREFIX_COMMAND");
				     
				     
				     File kibanatemp 		= File.createTempFile("kibanatemp", ".bat"); 
				     File elestictemp 		= File.createTempFile("elestictemp", ".bat"); 
				     File logstashtemp 		= File.createTempFile("logstashtemp", ".bat"); 
				     
				     /*
				     File kibanatemp 		= new File("kibanatemp.bat"); 
				     File elestictemp 		= new File("elestictemp.bat"); 
				     File logstashtemp 		= new File("logstashtemp.bat"); 
				     */
				     
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
				        
				        Runtime.getRuntime(). exec("cmd /c start \"\" "+kibanatemp);
					    Runtime.getRuntime(). exec("cmd /c start \"\" "+elestictemp);
					    Runtime.getRuntime(). exec("cmd /c start \"\" "+logstashtemp);
					    /*
				        
				        Runtime.getRuntime(). exec("cmd /c start /B "+kibanatemp);
				        Runtime.getRuntime(). exec("cmd /c start /B "+elestictemp);
					    Runtime.getRuntime(). exec("cmd /c start /B "+logstashtemp);
				     */
				     
				     final ELKConsole elkConsole = new ELKConsole();
				     
				     Timer timer = new Timer();
				     timer.scheduleAtFixedRate(new TimerTask() {

				         @Override
				         public void run() 
				         {
				        	 try{
				        		 elkConsole.getEconsole().setText("");
				        		 elkConsole.getLconsole().setText("");
				        		 elkConsole.getKconsole().setText("");
				        		 
				        	 elkConsole.getEconsole().append(readFile(ELASTICSEARCH_HOME+File.separator+"logs"+File.separator+"elasticsearch.log").toString());
						   	 elkConsole.getLconsole().append(readFile(LOGSTASH_HOME       +File.separator+"logs"+File.separator+"logstash-plain.log").toString());
						   	 elkConsole.getKconsole().append(readFile(KIBANA_HOME             +File.separator+"logs"+File.separator+"kibana.log").toString());
				        	 }catch(Exception e){}
				         }

				     }, 0, 15000);
				   	
				   	 
				   	 
				     
				     
				     
				     String URL="http://localhost:5601/app/kibana";
				     Runtime rt = Runtime.getRuntime();
				     rt.exec( "rundll32 url.dll,FileProtocolHandler " + URL);
				     
				  
			  		} catch (Exception E) {
			
				CommonUtils.showExceptionStack(E);
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
				sb.append(strLine);
			}
			br.close();
			fstream.close();
			return sb;
		}
	}
	
}
