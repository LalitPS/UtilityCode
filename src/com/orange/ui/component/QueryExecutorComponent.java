package com.orange.ui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import com.orange.ui.component.custom.CSMJTextArea;
import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.CustomJPanel;
import com.orange.ui.component.custom.Icons;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionPanel;
import com.orange.util.CustomCSVWriter;
import com.orange.util.CustomViewComponents;
import com.orange.util.JTabbedPaneCloseButton;
import com.orange.util.ProgressMonitorPane;
import com.orange.util.csm.CustomJTable;

public class QueryExecutorComponent extends CustomViewComponents{
	
	private ConnectionPanel connectionPanel;
	private JPanel panel;
	
	private CSMJTextArea queryResult;
	private JTextArea queryToExecute;
	private JScrollPane scrollPane;
	
	
	
	private JButton start;
	private CustomJFrame tabbedFrame;
	private JTabbedPaneCloseButton tabbedPane;
	

	public QueryExecutorComponent(Dimension screenSize) {
		super();
		
		
		tabbedPane = new JTabbedPaneCloseButton();
		
		tabbedFrame = new CustomJFrame("",Icons.queryExecutorICON);
		tabbedFrame.setBounds(50,50,screenSize.width * 60 / 100,screenSize.height * 60 / 100);
	
		tabbedFrame.add(tabbedPane, BorderLayout.CENTER);
		tabbedFrame.setAlwaysOnTop( true );
		tabbedFrame.setDefaultCloseOperation(CustomJFrame.DO_NOTHING_ON_CLOSE);
		tabbedFrame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	tabbedPane.removeAll();
		    	tabbedFrame.setVisible(false);
		    
		    }
		});
		
		panel = new CustomJPanel();
		panel.setLayout(null);
		
		queryToExecute = new JTextArea();
		
		queryResult = getQueryResult();
		
	
		
		start = new JButton("Execute");
		
		
		scrollPane = new JScrollPane(queryResult);

	
		JScrollPane qscrollPane = new JScrollPane(queryToExecute);
		qscrollPane.setBounds(10, 30,screenSize.width * 75 / 100, 250);
		start.setBounds(qscrollPane.getWidth()-200, qscrollPane.getHeight()+50,screenSize.width * 15 / 100, 25);
		
		
		
	
		
		
		/* 
		 * ADD MEMORY MONITOR PANE
		 */
		
		JPanel memPanel = CommonUtils.getMemoryMonitorPane(screenSize.width * 35 / 100);
		memPanel.setBounds(0,370,screenSize.width * 35 / 100,70);
		panel.add(memPanel);
		
		panel.add(qscrollPane);
		panel.add(start);
		
		/*
		 * ADD CONNECTION PANE
		 */
		connectionPanel =loadConnectionPanel(screenSize,true,true,true);
		panel.add(connectionPanel);
		
		
		
		
		

	}

	public void addProgressMonitorPane(){
		/*
		 * ADD PROGRESS PANEL
		 */
		JPanel progPane = ProgressMonitorPane.getInstance().getProgressPanel();
		progPane.setBounds(10,300,300,50);
		panel.add(progPane);
	}
	

	public void addTable(String[] header,String tabName,ArrayList<String[]> rows,String resultFileLoc) throws IOException
	{
		CustomCSVWriter writer = new CustomCSVWriter(new FileWriter(resultFileLoc),true);
		DefaultTableModel model = new DefaultTableModel(0, 0);
		model.setColumnIdentifiers(header);
		writer.writeNext(header);
		final CustomJTable table = new CustomJTable();
		table.setModel(model);
		
		JScrollPane tableContainer = new JScrollPane(table.getTable());
		
		for (int row = 0; row < rows.size(); row++) 
		{
			model.addRow(rows.get(row));
			writer.writeNext(rows.get(row));
		}
		writer.close();
		ImageIcon imageIcon = new ImageIcon("");
		tabbedPane.addTab(tabName+" ("+table.getTable().getRowCount()+")", imageIcon, tableContainer, resultFileLoc);
		table.decorateRows();
	}

	public JPanel getPanel() {
		return panel;
	}
	
	public JTextArea getQuery(){
		return queryToExecute;
	}
	
	
	public JButton getQueryExecutorButton() {
		return start;
	}
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	public void getTabbedFrameVisible(){
		if(!tabbedFrame.isVisible()){
			tabbedFrame.setVisible(true);
		}
	}
	public boolean hasArchivedValidation() {
		return connectionPanel.getArchiveConnect().isSelected();
	}
	public boolean hasCSIValidation() {
		return connectionPanel.getCsiConnect().isSelected();
	}
	
	
	
	public boolean hasGOLDValidation() {
		return connectionPanel.getGoldConnect().isSelected();
	}
	
	public boolean hasSQLOnConsole() {
		return connectionPanel.getShowSQL().isSelected();
	}
	
	
	
}
