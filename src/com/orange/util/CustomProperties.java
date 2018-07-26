package com.orange.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;

import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.CustomJPanel;
import com.orange.ui.component.custom.Directories;
import com.orange.ui.component.custom.Icons;
import com.orange.ui.component.custom.MyMenuBar;

public class CustomProperties {

	private Properties prop;
	
	private JPanel jpanel;
	private JScrollPane scrollPane ;
	private JButton update,setdefault;
	private String  propFilePath;
	private CustomJFrame frame;
	
	public CustomProperties(String propFilePath) throws IOException 
	{
		this.propFilePath = propFilePath;
		frame = new CustomJFrame("User Properties",Icons.iconPath4);
		jpanel = new CustomJPanel();
		jpanel.setLayout(null);
		
		prop = new Properties();
		
		Set<Object> keys = loadCustomProperties(this.propFilePath);
		  int Y =10;
		  for(Object k:keys)
		    {
	            String key = (String)k;
	            String value = prop.getProperty(key);
	            
			   	if(key.equalsIgnoreCase("lookNFeel") && null != MyMenuBar.getSelectedLookNFeel() && MyMenuBar.getSelectedLookNFeel().length()>0)
	            {
			   		value = MyMenuBar.getSelectedLookNFeel();
	            }
	           
	            JLabel lab = new JLabel(key);
	            
	            if(key.equalsIgnoreCase("CONSIDERABLE_VALUE"))
	            {
	            	lab.setToolTipText("CONSIDERABLE_VALUE_NEW OR CONSIDERABLE_VALUE_PREV");
	            }
	            if(key.equalsIgnoreCase("Theme"))
	            {
	            	lab.setToolTipText("STANDARD OR DEFAULT");
	            }
	            
	            JTextField val = new JTextField(value);
	            
	            if(key.equalsIgnoreCase("lookNFeel"))
	            {
	            	val.setEnabled(false);
	            }
	            
	            lab.setBounds(10,Y,200,20);
	            val.setBounds(250,Y,300,20);
	            jpanel.add(lab);
	            jpanel.add(val);
	            Y+=25;
	        }
		  
		   update = new JButton("Update");
		   update.addActionListener(new UpdateEvent());
		   update.setBounds(150,Y+50,150,30);
		   jpanel.add(update);
		   
		   
		   
		   setdefault = new JButton("Restore Default");
		   setdefault.addActionListener(new SetDefaultEvent());
		   setdefault.setBounds(350,Y+50,150,30);
		   jpanel.add(setdefault);
		   
		   scrollPane = new JScrollPane(jpanel);
		   scrollPane.setBackground(Color.green);
		   scrollPane.setAutoscrolls(true);
		   frame.add(scrollPane,BorderLayout.CENTER);
			
		   frame.pack();
		  
		   frame.setSize(600,450);
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
    		properties.store(fileOut, "USER CONFIG PROPERTIES");
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
	
	
	private class SetDefaultEvent implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0) {
		
			  try {
				  CommonUtils.writeCustomConfigProp(true);	
				  frame.dispose();
				  new CustomProperties(Directories.customUserConfigFileLocationV03);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
