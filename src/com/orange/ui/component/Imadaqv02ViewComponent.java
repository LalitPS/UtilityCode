package com.orange.ui.component;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import com.orange.ui.component.custom.CSMJTextArea;
import com.orange.ui.component.custom.CustomJCheckBox;
import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.CustomJPanel;
import com.orange.ui.component.custom.CustomRadioButton;
import com.orange.ui.component.custom.Icons;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionPanel;
import com.orange.util.CustomViewComponents;
import com.orange.util.GenericTitledBorder;
import com.orange.util.ProgressMonitorPane;

public class Imadaqv02ViewComponent extends CustomViewComponents{
	
private ConnectionPanel connectionPanel;
	
	private JDatePickerImpl fromDatePicker;
	private CustomJCheckBox ICO;
	private JTextField ICOText;
	private JLabel label;
	private JTextField fileToValidate;
	private JPanel panel;
	private CSMJTextArea queryResult;
	private JScrollPane scrollPane;
	private CustomJCheckBox setDate;
	private JDatePickerImpl toDatePicker;
	private JButton browse,iovUpdate,autoAnalysis,addAutoAnalysis;
	private ButtonGroup whichICO;
	private CustomJCheckBox isCiBaseFileCreate,isUpdateImpactedOrderReport;
	private CustomRadioButton isEndUser , isContractingParty;
	
	
	public Imadaqv02ViewComponent(Dimension screenSize) {
		super();
		
		JPanel subPanel = new CustomJPanel();
		subPanel.setLayout(null);
		GenericTitledBorder titled = new GenericTitledBorder("Options");
		subPanel.setBorder(titled);
		subPanel.setBounds(50+screenSize.width * 80 / 100,190,200,170);
		
		
		panel = new CustomJPanel();
		panel.setLayout(null);
		
		queryResult = getQueryResult();
		
	
		
		
		
		setDate = new CustomJCheckBox("Select Date Range?");
		setDate.setBackground(UIManager.getColor ( "Panel.background" ));
		ICO = new CustomJCheckBox("ICO <Ex :ICO1,ICO2>");
		
		whichICO = new ButtonGroup();
		isEndUser = new CustomRadioButton("End User",true);
		isEndUser.setToolTipText("End User");
		isContractingParty = new CustomRadioButton("Contracting Party");
		isContractingParty.setToolTipText("Contracting Party");
		whichICO.add(isEndUser);
		whichICO.add(isContractingParty);
		
		ICOText = new JTextField();
		ICOText.setEnabled(false);
		ICOText.setVisible(false);
		ICO.addChangeListener(new ICOChangeEventClass());
		
		
		label = new JLabel("File Name");
		fileToValidate = new JTextField();
		browse = new JButton("Browse");
		iovUpdate = new JButton("IOV Update");
		addAutoAnalysis = new JButton("Add/Remove Analysis Selection");
		autoAnalysis = new JButton("Auto Analysis");
		
		isCiBaseFileCreate = new CustomJCheckBox("CiBase File Creation",true);
		isUpdateImpactedOrderReport = new CustomJCheckBox("Update Impacted Order Report",true);
		
		
	
		scrollPane = new JScrollPane(queryResult);

		label.setBounds(10, 10, screenSize.width * 20 / 100, 25);
		fileToValidate.setBounds(20 + screenSize.width * 20 / 100, 10,screenSize.width * 30 / 100, 25);
		browse.setBounds(40 + screenSize.width * 60 / 100, 10,screenSize.width * 7 / 100, 25);
		
		iovUpdate.setBounds(40 + (screenSize.width * 68 / 100), 10,screenSize.width * 7 / 100, 25);
		
		CustomJPanel jp = new CustomJPanel();
		jp.setBounds(40+screenSize.width * 60 / 100, 60,screenSize.width * 16 / 100,170);
		jp.setLayout(null);
		GenericTitledBorder titled_analysis = new GenericTitledBorder("DETAILED ANALYSIS");
		jp.setBorder(titled_analysis);
		
		addAutoAnalysis.setBounds(10, 30,screenSize.width * 14 / 100, 25);
		autoAnalysis.setBounds(10, 70,screenSize.width * 14 / 100, 25);
		isCiBaseFileCreate.setBounds(10, 105,screenSize.width * 14 / 100, 20);
		isUpdateImpactedOrderReport.setBounds(10, 135,screenSize.width * 14 / 100, 20);
		
		jp.add(addAutoAnalysis);
		jp.add(autoAnalysis);
		jp.add(isCiBaseFileCreate);
		jp.add(isUpdateImpactedOrderReport);
		
		setDate.setBounds(10,20,170, 20);
		ICO.setBounds(10 , 50,170, 20);
		isEndUser.setBounds(10,75,80,20);
		isContractingParty.setBounds(100,75,95,20);
		ICOText.setBounds(10,100,170, 25);
		
		setDate.addActionListener(new SetDateEventClass(setDate));
		
		panel.add(label);
		
		/*
		 * ADD MEMORY MONITOR PANE
		 */
	
		JPanel memPanel =CommonUtils.getMemoryMonitorPane(screenSize.width * 35 / 100);
		memPanel.setBounds(0,170,screenSize.width * 35 / 100,70);
		panel.add(memPanel);
		
		/*
		 * ADD CONNECTION PANE
		 */
		connectionPanel = loadConnectionPanel(screenSize,true,true,true);
		panel.add(connectionPanel);

		
		panel.add(fileToValidate);
		panel.add(browse);
		panel.add(iovUpdate);
		panel.add(jp);
		
		subPanel.add(setDate);
		subPanel.add(ICO);
		subPanel.add(isEndUser);
		subPanel.add(isContractingParty);
		subPanel.add(ICOText);
		
		//panel.add(subPanel);

	}
	
	private class DateLabelFormatter extends AbstractFormatter 
	{
		 
	    /**
		 * 
		 */
		private String datePattern = "dd-MMM-yy";
		private static final long serialVersionUID = 1L;
		private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);
	 
	     
	    @Override
	    public Object stringToValue(String text) throws ParseException {
	        return dateFormatter.parseObject(text);
	    }
	 
	    @Override
	    public String valueToString(Object value) throws ParseException {
	        if (value != null) {
	            Calendar cal = (Calendar) value;
	            return dateFormatter.format(cal.getTime());
	        }
	         
	        return "";
	    }
	 
	}
	private class ICOChangeEventClass implements ChangeListener {

		public void stateChanged(ChangeEvent changeEvent) {
			boolean selected = ICO.isSelected();
			if (selected) {
				
					ICOText.setEnabled(true);
					ICOText.setVisible(true);
				
			}
			if (!selected) 
			{
				ICOText.setEnabled(false);
				ICOText.setVisible(false);
			}

		}

	}
	private class SetDateEventClass implements ActionListener {
		private CustomJCheckBox cbox;
		
		public SetDateEventClass(CustomJCheckBox cbox){
			this.cbox = cbox;
		}
		public void actionPerformed(ActionEvent changeEvent) {
			
			boolean selected = setDate.isSelected();
			if (selected) {
				final CustomJFrame frame = new CustomJFrame("",Icons.newHelpICON);
				JPanel jp = new CustomJPanel();
				jp.setLayout(null);
				jp.setSize(400,300);
				
				
				Properties p = new Properties();
				p.put("text.today", "Today");
				p.put("text.month", "Month");
				p.put("text.year", "Year");
				
				UtilDateModel frommodel = new UtilDateModel();
				JDatePanelImpl fromdatePanel = new JDatePanelImpl(frommodel,p);
				fromDatePicker = new JDatePickerImpl(fromdatePanel,new DateLabelFormatter());
				 
				fromDatePicker.setBounds(50,50,150,30);
				
				UtilDateModel tomodel = new UtilDateModel();
				JDatePanelImpl todatePanel = new JDatePanelImpl(tomodel,p);
				toDatePicker = new JDatePickerImpl(todatePanel,new DateLabelFormatter());
				
				
				JLabel fromLabel = new JLabel("From (>)");
				JLabel toLabel = new JLabel("To (<)");
				
				
				JButton ok = new JButton("OK");
				JButton cancel = new JButton("Cancel");
				
				
				fromLabel.setBounds(10,50,100,30);
				fromDatePicker.setBounds(150,50,150,30);
				toLabel.setBounds(10,100,100,30);
				toDatePicker.setBounds(150,100,150,30);
				ok.setBounds(100,180,60,50);
				cancel.setBounds(240,180,60,50);
				
				cancel.addActionListener(new ActionListener() {
			        public void actionPerformed(ActionEvent arg0) {
			        	frame.setVisible(false);
			        	cbox.setSelected(false);
			        }
			      });
			
				ok.addActionListener(new ActionListener() {
			        public void actionPerformed(ActionEvent arg0) {
			        	if(null == toDatePicker.getModel().getValue() || null == fromDatePicker.getModel().getValue())
			        	{
			        		JOptionPane.showMessageDialog(panel,"Please select both the dates.");
			        		return;
			        	}
			        	Date to = getInternalTodatePicker();
			        	Date from = getInternalFromdatePicker();
			        	
			        	if(from.after(to))
			        	{
			        		JOptionPane.showMessageDialog(panel,"From Date field value can not be after then To date.");
			        		return;
			        	}
			        	frame.setVisible(false);
			        	
			        }
			      });
				
				jp.add(fromLabel);
				jp.add(fromDatePicker);
				jp.add(toLabel);
				jp.add(toDatePicker);
				jp.add(ok);
				jp.add(cancel);
				frame.setBounds(200,150,jp.getWidth(),jp.getHeight());
				frame.add(jp);
				frame.setVisible(true);
			
				
			}
			if (!selected) {
				
			}

		}
		

	}
	
	
	
	
	public void addProgressMonitorPane(){
		/*
		 * ADD PROGRESS PANEL
		 */
		JPanel progPane = ProgressMonitorPane.getInstance().getProgressPanel();
		progPane.setBounds(10,260,300,50);
		panel.add(progPane);
		
		
	}
	
	
	
	public String getExternalFromdatePicker() {
		return fromDatePicker.getJFormattedTextField().getText();
	
	}
	
	public String getExternalTodatePicker() {
		return toDatePicker.getJFormattedTextField().getText();
	
	}
	
	
	public CustomJCheckBox getHasShowSQLCheckBox(){
		return connectionPanel.getShowSQL();
	}
	
	
	public CustomJCheckBox getICOCheckBox(){
		return ICO;
	}
	
	public JTextField getICOTextBox(){
		return ICOText;
	}
	
	public JButton getAutoAnalysisButton(){
		return autoAnalysis;
	}
	
	public JButton getAddAutoAnalysisButton(){
		return addAutoAnalysis;
	}
	
	public JButton getBroweButton(){
		return browse;
	}
	public JButton getIOVUpdateButton(){
		return iovUpdate;
	}
	
	private Date getInternalFromdatePicker() {
		Date selectedDate = (Date) fromDatePicker.getModel().getValue();
		return selectedDate;
	}
	
	
	private Date getInternalTodatePicker() {
		Date selectedDate = (Date) toDatePicker.getModel().getValue();
		return selectedDate;
	}
	
	
	public JTextField getFileToValidate() {
		return fileToValidate;
	}

	
	public JPanel getPanel() {
		return panel;
	}
	
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	
	
	public CustomJCheckBox getSetDateCheckBox(){
		return setDate;
	}
	
	public boolean hasArchivedValidation() {
		return connectionPanel.getArchiveConnect().isSelected();
	}
	public boolean hasGOLDValidation() {
		return connectionPanel.getGoldConnect().isSelected();
	}
	public boolean hasSetDate() {
		return setDate.isSelected();
	}
	
	public boolean hasshowSQL() {
		return connectionPanel.getShowSQL().isSelected();
	}
	public CustomJCheckBox getIsCiBaseFileCreate() {
		return isCiBaseFileCreate;
	}
	public CustomJCheckBox getISUpdateImpactedOrderReport() {
		return isUpdateImpactedOrderReport;
	}
	

	
}
