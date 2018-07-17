package com.orange.ui.component;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.orange.ui.component.custom.CSMJTextArea;
import com.orange.ui.component.custom.CustomJPanel;
import com.orange.util.CSVINTable;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionPanel;
import com.orange.util.CustomViewComponents;
import com.orange.util.ProgressMonitorPane;

public class DedupViewComponents extends CustomViewComponents{

	private JButton browse;
	private ConnectionPanel connectionPanel;
	private JTextField fileToValidate;
	private JLabel label;
	private CustomJPanel panel;
	private CSMJTextArea queryResult;
	
	

	private JScrollPane scrollPane;
	private CSVINTable show;
	private JButton validate;
	

	public DedupViewComponents(Dimension screenSize) {
		super();
		panel = new CustomJPanel();
		panel.setLayout(null);
		queryResult = getQueryResult();
		fileToValidate = getFileToValidate();
		
		label = new JLabel("Please select the file(csv)");
		browse = new JButton("Browse");
		show = new CSVINTable("Show CSV",fileToValidate);
		validate = new JButton("Validate");
	
	
		scrollPane = new JScrollPane(queryResult);

		label.setBounds(10, 10, screenSize.width * 10 / 100, 25);
		fileToValidate.setBounds(20 + screenSize.width * 10 / 100, 10,screenSize.width * 30 / 100, 25);
		browse.setBounds(30 + screenSize.width * 40 / 100, 10,screenSize.width * 15 / 100, 25);
		
		show.setBounds(30 + screenSize.width * 40 / 100, 45,screenSize.width * 15 / 100, 25);
		validate.setBounds(40 + screenSize.width * 60 / 100, 10,screenSize.width * 15 / 100, 25);
		
		
		
		
		
		panel.add(label);
		panel.add(fileToValidate);
		panel.add(browse);
		panel.add(show);
		panel.add(validate);
		
		
		
		
		/*
		 * ADD CONNECTION PANE
		 */
		connectionPanel = loadConnectionPanel(screenSize,true,true,false);
		panel.add(connectionPanel);
		
		
		/*
		 * ADD MEMORY MONITOR PANE
		 */
		
		JPanel memPanel = CommonUtils.getMemoryMonitorPane(screenSize.width * 35 / 100);
		memPanel.setBounds(0,270,screenSize.width * 35 / 100,70);
		panel.add(memPanel);
		
		

	}

	public void addProgressMonitorPane(){
		/*
		 * ADD PROGRESS PANEL
		 */
			
		JPanel progPanel = ProgressMonitorPane.getInstance().getProgressPanel();
		progPanel.setBounds(10,140,300,50);
		panel.add(progPanel);
		
	}
	public JButton getDedupBrowse() {
		return browse;
	}
	
	public JButton getDedupValidate() {
		return validate;
	}
	
	public JPanel getPanel() {
		return panel;
	}
	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public boolean hasDataValidation() {
		return connectionPanel.getGoldConnect().isSelected();
	}

	public boolean hasShowSQL() {
		return connectionPanel.getShowSQL().isSelected();
	}

	
}
