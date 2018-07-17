package com.orange.ui.component;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.orange.ui.component.custom.CSMJTextArea;
import com.orange.ui.component.custom.CustomJPanel;
import com.orange.util.ConnectionPanel;
import com.orange.util.CustomViewComponents;
import com.orange.util.ProgressMonitorPane;
import com.orange.util.RulesExtractQueryExecutor;

public class RulesExtractView extends CustomViewComponents{
	
	public class ComboSelectionEventClass implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(selectCriteria.getSelectedIndex()== 0)
			{
				fileToValidate.setEnabled(false);
			}
			else{
				fileToValidate.setEnabled(true);
			}
			
		}
		
	}
	private ConnectionPanel connectionPanel;
	String[] criteria ;
	JTextField fileToValidate;
	JLabel label;

	
	JLabel labelEX;
	JPanel panel;
	CSMJTextArea queryResult;
	JScrollPane scrollPane;

	JComboBox<Object> selectCriteria;
	
	JButton validate;

	public RulesExtractView(Dimension screenSize) {
		super();
		criteria =  new String[RulesExtractQueryExecutor.columns.length+1];
		criteria[0]="ALL";
		int X=1;
		for(String S : RulesExtractQueryExecutor.columns){
			criteria[X++]=S;
		}
		panel = new CustomJPanel();
		panel.setLayout(null);
		queryResult = getQueryResult();
		
		fileToValidate = getFileToValidate();
		fileToValidate.setEnabled(false);
		selectCriteria = new JComboBox<Object>(criteria);
		selectCriteria.addActionListener(new ComboSelectionEventClass());
		
		
		label = new JLabel("CRITERIA");
		labelEX = new JLabel("EX : 'Value','Value2'");
		validate = new JButton("Validate");
		scrollPane = new JScrollPane(queryResult);

		label.setBounds(10, 10, screenSize.width * 15 / 100, 25);
		
		selectCriteria.setBounds(20 + screenSize.width * 10 / 100, 10,screenSize.width * 25 / 100, 25);
		fileToValidate.setBounds(20 + screenSize.width * 40 / 100, 10,screenSize.width * 30 / 100, 25);
		labelEX.setBounds(20 + screenSize.width * 40 / 100, 35,screenSize.width * 30 / 100, 15);
		
		validate.setBounds(40 + screenSize.width * 60 / 100, 60,screenSize.width * 15 / 100, 25);
		
		
		
				
		panel.add(label);
		panel.add(labelEX);
		panel.add(selectCriteria);
		panel.add(fileToValidate);
		
		panel.add(validate);
	
		
		/*
		 * ADD CONNECTION PANE
		 */
		connectionPanel = loadConnectionPanel(screenSize,true,false,false);
		panel.add(connectionPanel);

	}

	public void addProgressMonitorPane(){
		/*
		 * ADD PROGRESS PANEL
		 */
		JPanel progPane = ProgressMonitorPane.getInstance().getProgressPanel();
		progPane.setBounds(10,85,300,50);
		panel.add(progPane);
		
		
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
	
	public JComboBox<Object> getSelectCriteria(){
		return selectCriteria;
	}

	public int getSelectedIndex(){
		return selectCriteria.getSelectedIndex();
	}

		
	public boolean hasDataValidation() {
		return connectionPanel.getGoldConnect().isSelected();
	}
	

	
}
