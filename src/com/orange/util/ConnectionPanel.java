package com.orange.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.SQLException;
import java.util.concurrent.Executors;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.orange.ui.component.custom.CustomColorTheme;
import com.orange.ui.component.custom.CustomJCheckBox;
import com.orange.ui.component.custom.CustomJPanel;
import com.orange.util.csm.ConnectionForArchived;
import com.orange.util.csm.ConnectionForCSI;
import com.orange.util.csm.ConnectionForGOLD;

public class ConnectionPanel extends CustomJPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3294237340261571512L;
	private CustomJCheckBox archiveConnect;
	private CustomJCheckBox csiConnect;
	private CustomJCheckBox goldConnect;
	private JLabel isArchiveConnected;
	
	private JLabel isCSIConnected;
	private JLabel isGoldConnected;
	private CustomJCheckBox showSQL;
	
	 protected  ConnectionPanel() 
	    {
	       
	    }
	
	public ConnectionPanel(Dimension screenSize,boolean isGold,boolean isCSI, boolean isArchive){
	  
		setBounds(50+screenSize.width * 80 / 100, 10,200,170);
		setLayout(null);
		GenericTitledBorder titled = new GenericTitledBorder("CONNECTIONS");
		setBorder(titled);
		
			goldConnect = new CustomJCheckBox("GOLD Connect?");
			csiConnect = new CustomJCheckBox("CSI Connect?");
			archiveConnect = new CustomJCheckBox("Archive Connect?");
			
			showSQL = new CustomJCheckBox("SQL on Console?");
		
			isGoldConnected = new JLabel("GOLD Disconnected");
			isCSIConnected = new JLabel("CSI Disconnected");
			isArchiveConnected = new JLabel("Archive Disconnected");
			
			isGoldConnected.setFont(new Font("Atrial", Font.PLAIN, 10));
			isCSIConnected.setFont(new Font("Atrial", Font.PLAIN, 10));
			isArchiveConnected.setFont(new Font("Atrial", Font.PLAIN, 10));
			
			
			goldConnect.addChangeListener(new ChangeListener () {
			      public void stateChanged(ChangeEvent ev) 
			      {
			    	
			    	  boolean selected = getGoldConnect().isSelected();
						if (selected) 
						{
							    
						 try {
							ConnectionForGOLD.getGOLDConnection(getGoldConnect());
						} catch (Exception E) {	CommonUtils.showExceptionStack(E);}
							    
							
						}
						if (!selected) {
						try{
							ConnectionForGOLD.closeConnection();
						} catch (Exception E) {	CommonUtils.showExceptionStack(E);}
						}

			      }
			    });
			
			csiConnect.addChangeListener(new ChangeListener () {
			      public void stateChanged(ChangeEvent ev) 
			      {
			    	  boolean selected = getCsiConnect().isSelected();
						
						if (selected) 
						{
							try{
							ConnectionForCSI.getCSIConnection(getCsiConnect());
							} catch (Exception E) {	CommonUtils.showExceptionStack(E);}
							
						}
						if (!selected) {
							try{
							ConnectionForCSI.closeConnection();
							} catch (Exception E) {	CommonUtils.showExceptionStack(E);}
						}
			      }
			    });
			
			archiveConnect.addChangeListener(new ChangeListener () {
			      public void stateChanged(ChangeEvent ev) 
			      {
			    	  boolean selected = getArchiveConnect().isSelected();
						
						if (selected) 
						{
							try{
								ConnectionForArchived.getArchivedConnection(getArchiveConnect());
							} catch (Exception E) {	CommonUtils.showExceptionStack(E);}
							
						}
						if (!selected) {
							try{
							ConnectionForArchived.closeConnection();
							} catch (Exception E) {	CommonUtils.showExceptionStack(E);}
						}
			      }
			
			});
			
			
			goldConnect.setBounds(10,20,180,20);
			isGoldConnected.setBounds(10,40,180,15);
			
			csiConnect.setBounds(10,60,180,20);
			isCSIConnected.setBounds(10,80,180,15);
			
			archiveConnect.setBounds(10,100,180,20);
			isArchiveConnected.setBounds(10,120,180,15);
			showSQL.setBounds(10,140,180,20);
			
			add(goldConnect);
			add(isGoldConnected);
			add(csiConnect);
			add(isCSIConnected);
			add(archiveConnect);
			add(isArchiveConnected);
			add(showSQL);
			
			goldConnect.setEnabled(isGold);
			isGoldConnected.setEnabled(isGold);
			
			csiConnect.setEnabled(isCSI);
			isCSIConnected.setEnabled(isCSI);
			
			archiveConnect.setEnabled(isArchive);
			isArchiveConnected.setEnabled(isArchive);
			
			continiousThread();
	   
	}


	private void continiousThread() 
	{
		Executors.newSingleThreadExecutor().execute(new Runnable() {
			@Override
			public void run() {

				while (true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(null,e);
					}
					
					try {
						if (ConnectionForGOLD.isConnected()) 
						{
							goldConnect.setSelected(true);
							//isGoldConnected.setText("Connected to "+ConnectionBean.getUserName()+"@"+ConnectionBean.getUrl());
							isGoldConnected.setText("Connected... "+ConnectionBean.getDbName());
							isGoldConnected.setForeground(CustomColorTheme.menuBarColor);
							isGoldConnected.setToolTipText(isGoldConnected.getText());
						} else 
						{
							goldConnect.setSelected(false);
							isGoldConnected.setText("GOLD Disconnected..");
							isGoldConnected.setForeground(Color.red);
							isGoldConnected.setToolTipText("");
							
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					try {
						if (ConnectionForArchived.isConnected()) 
						{
						archiveConnect.setSelected(true);
						//isArchiveConnected.setText("Connected to "+ConnectionBeanArchived.getUserName()+"@"+ConnectionBeanArchived.getUrl());
						isArchiveConnected.setText("Connected... "+ConnectionBeanArchived.getDbName());
						isArchiveConnected.setForeground(CustomColorTheme.menuBarColor);
						isArchiveConnected.setToolTipText(isArchiveConnected.getText());
						}
						else
						{
							archiveConnect.setSelected(false);
							isArchiveConnected.setText("Archived Disconnected..");
							isArchiveConnected.setForeground(Color.red);	
							isArchiveConnected.setToolTipText("");
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					try {
						if (ConnectionForCSI.isConnected()) 
						{
						csiConnect.setSelected(true);	
						//isCSIConnected.setText("Connected to "+ConnectionBeanCSI.getUserName()+"@"+ConnectionBeanCSI.getUrl());
						isCSIConnected.setText("Connected... "+ConnectionBeanCSI.getDbName());
						isCSIConnected.setForeground(CustomColorTheme.menuBarColor);
						isCSIConnected.setToolTipText(isCSIConnected.getText());
						}
						else
						{
							csiConnect.setSelected(false);	
							isCSIConnected.setText("CSI Disconnected..");
							isCSIConnected.setForeground(Color.red);
							isCSIConnected.setToolTipText("");
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		});

	}


	public CustomJCheckBox getArchiveConnect() {
		return archiveConnect;
	}


	public CustomJCheckBox getCsiConnect() {
		return csiConnect;
	}


	public CustomJCheckBox getGoldConnect() {
		return goldConnect;
	}
	
	
	
	public CustomJCheckBox getShowSQL() {
		return showSQL;
	}

}
