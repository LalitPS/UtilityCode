package com.orange.util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.CustomJPanel;
import com.orange.ui.component.custom.Icons;

public class PublicPrivateKeyGenerator {
	
	private CustomJFrame frame;
	private JPanel jpanel;
	private JButton savePublicKey,closeButton;
	private JPasswordField passwordText ;
	private JComboBox<String> connectionNameList;
	private JTextField userFTIDText,userNameText,hostNameText,portText,sidText,prefixText;
	private JLabel connectionNameLabel,userFTIDLabel,userNameLabel,passwordLabel,hostNameLabel,portLabel,sidLabel,prefixLabel;
	private Properties properties;
	public static String seperator = "#LALIT#";

	public PublicPrivateKeyGenerator(String env,String fileName)
	{
		
			frame = new CustomJFrame("Public Key Generator for "+env,Icons.iconPath4);
			jpanel = new CustomJPanel();
			jpanel.setLayout(null);	
		
		
		 connectionNameLabel			= new JLabel("*Connection Name");
		 userFTIDLabel 	 				    = new JLabel("*User FTID");
		 userNameLabel 	 				= new JLabel("*Username");
		 passwordLabel 	 					= new JLabel("*Password (Do not disclose)");
		 hostNameLabel 	 				= new JLabel("*Hostname");
		 portLabel 	 	 						= new JLabel("*Port");
		 sidLabel 	 	 						= new JLabel("*SID");
		 prefixLabel 	 						= new JLabel("*Prefix ex: GOLDDBA.(Please add '.' after prefix)");
		 
		 
		 connectionNameList 				= new JComboBox<String>();
		 userFTIDText 	 					= new JTextField();
		 userNameText 	 					= new JTextField();
		 passwordText 	 					= new JPasswordField();
		 hostNameText 	 					= new JTextField();
		 portText 	 	 						= new JTextField();
		 sidText 	 	 						= new JTextField();
		 prefixText 	 						= new JTextField();
		 
		 connectionNameLabel.	setBounds(10,20,250,20);
		 userFTIDLabel.   			setBounds(10,60,250,20);
		 userNameLabel.  			setBounds(10,100,250,20);
		 passwordLabel.   			setBounds(10,140,250,20);
		 hostNameLabel.  			setBounds(10,180,250,20);
		 portLabel.					setBounds(10,220,250,20);
		 sidLabel.						setBounds(10,260,250,20);
		 prefixLabel.					setBounds(10,300,250,20);
		 
		 connectionNameList.		setBounds(270,20,250,20);
		 userFTIDText.				setBounds(270,60,250,20);
		 userNameText.				setBounds(270,100,250,20);
		 passwordText.				setBounds(270,140,250,20);
		 hostNameText.				setBounds(270,180,250,20);
		 portText.						setBounds(270,220,250,20);
		 sidText.						setBounds(270,260,250,20);
		 prefixText.					setBounds(270,300,250,20);
		 
		 
		 connectionNameList.addItem("NEW_DATABASE_NAME");
		 
		 properties = CommonUtils.loadFTPConfigProp(fileName);
		 
		 for (Object o : properties.keySet()) 
			{
			 connectionNameList.addItem((String) o);
		    } 
		 
		 connectionNameList.setSelectedIndex(0);
		 connectionNameList.setEditable(true);
		
		
	     savePublicKey = new JButton("Save public keys");
	     savePublicKey.addActionListener(new SaveKeyEvent(env,properties,fileName));
	     savePublicKey.setBounds(10,350,150,30);
	     savePublicKey.setEnabled(false);
		 jpanel.add(savePublicKey);
		 
		   
		 closeButton = new JButton("Close");
		 closeButton.addActionListener(new CloseEvent());
		 closeButton.setBounds(250,350,150,30);
		 jpanel.add(closeButton);
		 
		 jpanel.add(connectionNameLabel);
		 jpanel.add(userFTIDLabel);
		 jpanel.add(userNameLabel);
		 jpanel.add(passwordLabel);
		 jpanel.add(hostNameLabel);
		 jpanel.add(portLabel);
		 jpanel.add(sidLabel);
		 jpanel.add(prefixLabel);
		 
		 jpanel.add(connectionNameList);
		 jpanel.add(userFTIDText);
		 jpanel.add(userNameText);
		 jpanel.add(passwordText);
		 jpanel.add(hostNameText);
		 jpanel.add(portText);
		 jpanel.add(sidText);
		 jpanel.add(prefixText);
		 
		 connectionNameList.addActionListener(new CheckEditability());
		 userFTIDText.addKeyListener(new CheckEditability());
		 userNameText.addKeyListener(new CheckEditability());
		 passwordText.addKeyListener(new CheckEditability());
		 hostNameText.addKeyListener(new CheckEditability());
		 portText.addKeyListener(new CheckEditability());
		 sidText.addKeyListener(new CheckEditability());
		 prefixText.addKeyListener(new CheckEditability());
		
		 frame.add(jpanel,BorderLayout.CENTER);
		 frame.pack();
		 frame.setSize(600,500);
		 frame.setLocationRelativeTo(null);
		 frame.setVisible(true);
	
		 
	}
	
	private class CheckEditability implements KeyListener,ActionListener
	{

		@Override
		public void keyPressed(KeyEvent arg0) {
			if(hasAllMadatoryValue()){
        		savePublicKey.setEnabled(true);
        	}
			else{
				savePublicKey.setEnabled(false);
			}
			
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			if(hasAllMadatoryValue()){
        		savePublicKey.setEnabled(true);
        	}
			else{
				savePublicKey.setEnabled(false);
			}
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			if(hasAllMadatoryValue()){
        		savePublicKey.setEnabled(true);
        	}
			else{
				savePublicKey.setEnabled(false);
			}
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(hasAllMadatoryValue()){
        		savePublicKey.setEnabled(true);
        	}
			else{
				savePublicKey.setEnabled(false);
			}
			
		}
		
	}
	private class SaveKeyEvent implements ActionListener
	{
		private String env;
		private Properties properties;
		private String fileName;
		
		public SaveKeyEvent(String env, Properties properties,String fileName){
			this.env = env;
			this.properties = properties;
			this.fileName = fileName;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			if(connectionNameList.getSelectedItem().toString().isEmpty() || connectionNameList.getSelectedItem().toString().equalsIgnoreCase("NEW_DATABASE_NAME"))
			{
				return;
			}
			else if(properties.containsKey(connectionNameList.getSelectedItem()))
			{
			 int option = JOptionPane.showConfirmDialog(new CustomJFrame(), "Database "+connectionNameList.getSelectedItem()+" is already exists.\n Do you want to reset.");	
			 if(option == JOptionPane.YES_OPTION)
				{
				 try
				 {
				  savePublicKeys(env,properties,fileName);
				 }catch(Exception e){e.printStackTrace();}
				}
			 
			}
			else if(!properties.containsKey(connectionNameList.getSelectedItem()))
			 {
				 try{
					 savePublicKeys(env,properties,fileName);
				 }catch(Exception e){e.printStackTrace();}
			 }
		}
	}
	
	private void savePublicKeys(String env,Properties properties,String fileName) throws Exception{
		 @SuppressWarnings("deprecation")
		 
		String saveKEY =  
						  PublicKeyGenerator.encrypt(userNameText.getText())+PublicKeyGenerator.encrypt(seperator)
		 				 +PublicKeyGenerator.encrypt(passwordText.getText())+PublicKeyGenerator.encrypt(seperator)
		 				 +PublicKeyGenerator.encrypt(hostNameText.getText())+PublicKeyGenerator.encrypt(seperator)
		 				 +PublicKeyGenerator.encrypt(portText.getText())+PublicKeyGenerator.encrypt(seperator)
		 				 +PublicKeyGenerator.encrypt(sidText.getText())+PublicKeyGenerator.encrypt(seperator)
		 				 +PublicKeyGenerator.encrypt(prefixText.getText())+PublicKeyGenerator.encrypt(seperator)
		 				 +PublicKeyGenerator.encrypt(userFTIDText.getText());
		 				
		 properties.setProperty(connectionNameList.getSelectedItem().toString(), saveKEY);
		 File file = new File(fileName);
		 FileOutputStream fileOut = new FileOutputStream(fileName);
		 properties.store(fileOut, env+ "DataBase Configurations for ."+userFTIDText.getText());
		 properties = CommonUtils.loadFTPConfigProp(fileName);	 
		 JOptionPane.showMessageDialog(new CustomJFrame(),"Public Key has been saved successfully in below file >> .\n"+file.getAbsolutePath()+"\nPlease add User 'FTID' befor the file name and place on the remote location.\nPlease note >> FTID is case sensitive.");
		 frame.dispose();
		 new PublicPrivateKeyGenerator(env,fileName);
		 
	}
	private class CloseEvent implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			frame.dispose();
		}
	}
	
	@SuppressWarnings("deprecation")
	private boolean hasAllMadatoryValue(){
		boolean hasAll =true;
		if(connectionNameList.getSelectedItem().toString().isEmpty() || connectionNameList.getSelectedItem().toString().equalsIgnoreCase("NEW_DATABASE_NAME"))
		{
			return false;
		}
		else if(null == userFTIDText.getText() || userFTIDText.getText().isEmpty() || userFTIDText.getText().length()==0)
		{
			return false;
		}
		else if(null == userNameText.getText() || userNameText.getText().isEmpty() || userNameText.getText().length()==0)
		{
			return false;
		}
		else if(null == passwordText.getText() || passwordText.getText().isEmpty() || passwordText.getText().length()==0)
		{
			return false;
		}
		else if(null == hostNameText.getText() || hostNameText.getText().isEmpty() || hostNameText.getText().length()==0)
		{
			return false;
		}
		else if(null == portText.getText() || portText.getText().isEmpty() || portText.getText().length()==0)
		{
			return false;
		}
		else if(null == sidText.getText() || sidText.getText().isEmpty() || sidText.getText().length()==0)
		{
			return false;
		}
		
		return hasAll;
		
	}
	
		
}
