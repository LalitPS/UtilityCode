package com.orange.ui.component;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.orange.ui.component.custom.CSMJTextArea;
import com.orange.ui.component.custom.CustomJPanel;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionPanel;
import com.orange.util.CustomViewComponents;
import com.orange.util.ProgressMonitorPane;


public class EarrachExtractionView extends CustomViewComponents{
	
	private JButton browseButton;
	private ConnectionPanel connectionPanel;
	private JButton D9;
	private JLabel jsonlabel;
	private JTextField jSONToValidateFile;
	private JButton jSONValidateButton;
	private JLabel label;
	private JTextField orderToValidate;
	private JPanel panel;
	
	
	private CSMJTextArea queryResult;
	private JScrollPane scrollPane;
	private JButton showJSONButton,checkJSONButton,checkClosedButton,jsonToCSV;
	
	private JButton validate;
	
	
	public EarrachExtractionView(Dimension screenSize) {
		super();
		
		panel = new CustomJPanel();
		panel.setLayout(null);
		queryResult = getQueryResult();
		
		orderToValidate = getFileToValidate();
		jSONToValidateFile = new JTextField();
	
		label = new JLabel("QUOTE NUMBER");
		jsonlabel = new JLabel("JSON FILE");
		
		validate = new JButton("Validate");
		browseButton  = new JButton("Browse");
		jSONValidateButton = new JButton("JSON Validate");
		showJSONButton = new JButton("JSON View");
		checkJSONButton = new JButton("JSON Format Check");
		checkClosedButton = new JButton("Close Order Check");
		jsonToCSV = new JButton("JSON to CSV");
		D9 = new JButton("D9 Validations");
		//D9.setEnabled(false);
		
		scrollPane = new JScrollPane(queryResult);

		label.setBounds(10, 10, screenSize.width * 15 / 100, 25);
		jsonlabel.setBounds(10, 50, screenSize.width * 15 / 100, 25);
		
		orderToValidate.setBounds(20 + screenSize.width * 10 / 100, 10,screenSize.width * 25 / 100, 25);
		jSONToValidateFile.setBounds(20 + screenSize.width * 10 / 100, 50,screenSize.width * 25 / 100, 25);
		
		validate.setBounds(40 + screenSize.width * 60 / 100, 10,screenSize.width * 15 / 100, 25);
		
		browseButton.setBounds(20 + screenSize.width * 40 / 100, 50,screenSize.width * 15 / 100, 25);
		showJSONButton.setBounds(40 + screenSize.width * 60 / 100, 50,screenSize.width * 15 / 100, 25);
		checkJSONButton.setBounds(40 + screenSize.width * 60 / 100, 90,screenSize.width * 15 / 100, 25);
		checkClosedButton.setBounds(40 + screenSize.width * 60 / 100, 120,screenSize.width * 15 / 100, 25);
		jsonToCSV.setBounds(40 + screenSize.width * 60 / 100, 150,screenSize.width * 15 / 100, 25);
		
		jSONValidateButton.setBounds(20 + screenSize.width * 40 / 100, 80,screenSize.width * 15 / 100, 25);
	
		D9.setBounds(20 + screenSize.width * 40 / 100, 110,screenSize.width * 15 / 100, 25);
		
		
		
			
		panel.add(label);
		panel.add(jsonlabel);
		panel.add(orderToValidate);
		panel.add(jSONToValidateFile);
		panel.add(validate);
		panel.add(browseButton);
		panel.add(showJSONButton);
		panel.add(checkJSONButton);
		panel.add(checkClosedButton);
		panel.add(jsonToCSV);
		panel.add(jSONValidateButton);
		panel.add(D9);
		
		/*
		 * ADD MEMORY MONITOR PANE
		 */
		
		JPanel memPanel = CommonUtils.getMemoryMonitorPane(screenSize.width * 35 / 100);
		memPanel.setBounds(0,270,screenSize.width * 35 / 100,70);
		panel.add(memPanel);
		
		/*
		 * ADD CONNECTION PANE
		 */
		connectionPanel =loadConnectionPanel(screenSize,true,false,false);
		panel.add(connectionPanel);
		
		

	}

	public void addProgressMonitorPane(){
		/*
		 * ADD PROGRESS PANEL
		 */
		JPanel progPane = ProgressMonitorPane.getInstance().getProgressPanel();
		progPane.setBounds(10,140,300,50);
		panel.add(progPane);
		
		
	}

	
	
	public JButton getBrowseButton() {
		return browseButton;
	}
	public JButton getD9() {
		return D9;
	}
	
	public JTextField getJSONToValidateFile(){
		   return jSONToValidateFile;
	   }
	
	public JButton getJSONValidateButton() {
		return jSONValidateButton;
	}
	
	
	public JPanel getPanel() {
		return panel;
	}
	
	public JButton getRulesExtract() {
		return validate;
	}
	
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	public JButton getshowJSONButton() {
		return showJSONButton;
	}

	public JButton getcheckJSONButton() {
		return checkJSONButton;
	}
	
	public JButton getcheckClosedButton() {
		return checkClosedButton;
	}
	
	public JButton getJsonToCSVButton() {
		return jsonToCSV;
	}
	
	
	public boolean hasDataValidation() {
		return connectionPanel.getGoldConnect().isSelected();
	}

   public boolean hasshowSQL() {
	return connectionPanel.getShowSQL().isSelected();
}
	

	
}

