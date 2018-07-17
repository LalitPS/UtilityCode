package com.orange.ui.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

public class MLANViewComponents extends CustomViewComponents{
	
	private class DateLabelFormatter extends AbstractFormatter {
		 
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
			if (!selected) {
				ICOText.setEnabled(false);
				ICOText.setVisible(false);
			}

		}

	}
	private class SetDateEventClass implements ActionListener {
		private CustomJCheckBox cbox;
		
		public SetDateEventClass(CustomJCheckBox cbox){//,JDatePickerImpl fromdatePicker,JDatePickerImpl todatePicker){
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
			        		JOptionPane.showMessageDialog(null,"Please select both the dates.");
			        		return;
			        	}
			        	Date to = getInternalTodatePicker();
			        	Date from = getInternalFromdatePicker();
			        	
			        	if(from.after(to))
			        	{
			        		JOptionPane.showMessageDialog(null,"From Date field value can not be after then To date.");
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
	

	private ConnectionPanel connectionPanel;
	private JButton criteriaExecution;

	private JButton formatCSV;
	private JDatePickerImpl fromDatePicker;
	private CustomJCheckBox ICO;
	
	private JTextField ICOText;
	private CustomJCheckBox isDynamicCriteriaExecution;
	private JLabel label;
	
	private JButton orderList;
	private JTextField orderToValidate;
	private JLabel orderType;
	private Label orderTypeT;
	private JPanel panel;

	
	private JButton orderHierarchy;
	private ButtonGroup isSiteGroup;
	private CustomRadioButton pleaseSelect, includeSiteChange, excludeSiteChange;
	
	private ButtonGroup whichICO;
	private CustomRadioButton isEndUser , isContractingParty;
	private CustomJCheckBox includeUpwards;
	
	
	private CSMJTextArea queryResult;
	private JScrollPane scrollPane;
	
	private JButton serviceBuildInfo;
	
	private CustomJCheckBox setDate;

	private JButton start,l2start, hookahBrowse, hookahInfo;
	
	private JDatePickerImpl toDatePicker;
	
	private JButton usidInfo;
	private JButton deliverSRF2,icoWithChild;
	
	
	
	public MLANViewComponents(Dimension screenSize) {
		super();
		
		JPanel subPanel = new CustomJPanel();
		subPanel.setLayout(null);
		GenericTitledBorder titled = new GenericTitledBorder("Options");
		subPanel.setBorder(titled);
		subPanel.setBounds(50+screenSize.width * 80 / 100,190,200,170);
		
		
		panel = new CustomJPanel();
		panel.setLayout(null);
	
		queryResult = getQueryResult();
		orderToValidate = getFileToValidate();
	
		
		orderHierarchy = new JButton("Order Hierarchy");
		orderHierarchy.setToolTipText("Order Hierarchy");
		orderHierarchy.setBounds(10, 20, screenSize.width * 12 / 100, 25);
		
		pleaseSelect = new CustomRadioButton("Please select",true);
		pleaseSelect.setToolTipText("option will include all orders either sites are changed in between.");
		
		includeSiteChange = new CustomRadioButton("Include(if Site is different)");
		includeSiteChange.setToolTipText("option will include all orders either sites are changed in between.");
		excludeSiteChange = new CustomRadioButton("Exclude(if Site is different)");
		excludeSiteChange.setToolTipText("option will include only orders untill sites are similler.");
		includeUpwards = new CustomJCheckBox("Include upwards hierarchy",true);
		includeUpwards.setToolTipText("option will include all orders backward and forward sequence of the given order(s).");
		//pleaseSelect.setBounds(7, 50, screenSize.width * 12 / 100, 20);
		includeSiteChange.setBounds(7, 50, screenSize.width * 12 / 100, 20);
		excludeSiteChange.setBounds(7, 75, screenSize.width * 12 / 100, 20);
		includeUpwards.setBounds(7, 100, screenSize.width * 12 / 100, 20);
		
		isSiteGroup = new ButtonGroup();
		isSiteGroup.add(pleaseSelect);
		isSiteGroup.add(includeSiteChange);
		isSiteGroup.add(excludeSiteChange);
		
		JPanel subPanel3 = new CustomJPanel();
		subPanel3.setLayout(null);
		GenericTitledBorder titled3 = new GenericTitledBorder("Orders...");
		subPanel3.add(orderHierarchy);
		//subPanel3.add(pleaseSelect);
		subPanel3.add(includeSiteChange);
		subPanel3.add(excludeSiteChange);
		subPanel3.add(includeUpwards);
		subPanel3.setBorder(titled3);
		subPanel3.setBounds(30 + screenSize.width * 40 / 100, 90,screenSize.width * 15 / 100, 135);
		panel.add(subPanel3);
		
		
		
		whichICO = new ButtonGroup();
		isEndUser = new CustomRadioButton("End User",true);
		isEndUser.setToolTipText("End User");
		isContractingParty = new CustomRadioButton("Contracting Party");
		isContractingParty.setToolTipText("Contracting Party");
		whichICO.add(isEndUser);
		whichICO.add(isContractingParty);
		
		isDynamicCriteriaExecution= new CustomJCheckBox("Dynamic Criteria Execution?",true);
		setDate = new CustomJCheckBox("Select Date Range?");
		setDate.setBackground(UIManager.getColor ( "Panel.background" ));
		ICO = new CustomJCheckBox("ICO <Ex :ICO1,ICO2>");
		ICOText = new JTextField();
		ICOText.setEnabled(false);
		ICOText.setVisible(false);
		ICO.addChangeListener(new ICOChangeEventClass());
		
		
		label = new JLabel("Please Insert Order Number");
		orderType = new JLabel("Order Type");
		orderTypeT = new Label(".......");
		orderTypeT.setForeground(Color.blue);

		
		hookahBrowse = new JButton("Browse");
		hookahInfo = new JButton("Hookah Extract");
		hookahInfo.setToolTipText(hookahInfo.getText());
		start = new JButton("MLAN Service Build Info");
		l2start = new JButton("L2 Service Build Info");
	
		serviceBuildInfo = new JButton("Service Build Info");
		formatCSV = new JButton("Format CSV File Info");
		criteriaExecution = new JButton("Execute Criteria");
		orderList = new JButton("Execute Orders");
		usidInfo = new JButton("USID Details");
		deliverSRF2 = new JButton("Deliver SRF2 MS status update");
		deliverSRF2.setToolTipText("This Process will check and provide the fixes for those orders,where a Milestone is 'Capture Technical Details' with Action Taken is ‘Complete Task (SRF2 not required for this order)' and any Milestone is 'Deliver SRF2' but status is not closed.\n");
		icoWithChild = new JButton("ICO with Child");
		
	
		scrollPane = new JScrollPane(queryResult);

		label.setBounds(10, 10, screenSize.width * 20 / 100, 25);
		orderType.setBounds(10, 50, screenSize.width * 20 / 100, 25);
		
		orderTypeT.setBounds(20 + screenSize.width * 20 / 100, 50,screenSize.width * 30 / 100, 25);
		
		orderToValidate.setBounds(20 + screenSize.width * 20 / 100, 10,screenSize.width * 30 / 100, 25);
		
		
		hookahBrowse.setBounds(40 + screenSize.width * 60 / 100, 10,screenSize.width * 7 / 100, 25);
		hookahInfo.setBounds(40 + (screenSize.width * 68 / 100), 10,screenSize.width * 7 / 100, 25);
		start.setBounds(40 + screenSize.width * 60 / 100, 50,screenSize.width * 15 / 100, 25);
		
		l2start.setBounds(40 + screenSize.width * 60 / 100, 90,screenSize.width * 15 / 100, 25);
		
		serviceBuildInfo.setBounds(40 + screenSize.width * 60 / 100, 130,screenSize.width * 15 / 100, 25);
		
		formatCSV.setBounds(40 + screenSize.width * 60 / 100, 170,screenSize.width * 15 / 100, 25);
		criteriaExecution.setBounds(40 + screenSize.width * 60 / 100, 210,screenSize.width * 15 / 100, 25);
		orderList.setBounds(40 + screenSize.width * 60 / 100, 250,screenSize.width * 15 / 100, 25);
		usidInfo.setBounds(40 + screenSize.width * 60 / 100, 290,screenSize.width * 15 / 100, 25);
		deliverSRF2.setBounds(40 + screenSize.width * 60 / 100, 330,screenSize.width * 15 / 100, 25);
		icoWithChild.setBounds(40 + screenSize.width * 60 / 100, 370,screenSize.width * 15 / 100, 25);
		
		//isDynamicCriteriaExecution.setBounds(40 + screenSize.width * 76 / 100, 170,screenSize.width * 15 / 100, 25);
		
		/*
		validateFromGOLD.setBounds(20 + screenSize.width * 80 / 100, 10,screenSize.width * 15 / 100, 20);
		validateFromArchived.setBounds(20 + screenSize.width * 80 / 100, 35,screenSize.width * 15 / 100, 20);
		showSQL.setBounds(20 + screenSize.width * 80 / 100, 60,screenSize.width * 15 / 100, 20);
		setDate.setBounds(20 + screenSize.width * 80 / 100, 85,screenSize.width * 15 / 100, 20);
		ICO.setBounds(20 + screenSize.width * 80 / 100, 110,screenSize.width * 15 / 100, 20);
		ICOText.setBounds(20 + screenSize.width * 80 / 100, 135,screenSize.width * 15 / 100, 25);
		*/
		
		
		setDate.setBounds(10,20,170, 20);
		ICO.setBounds(10 , 50,170, 20);
		isEndUser.setBounds(10,75,80,20);
		isContractingParty.setBounds(100,75,95,20);
		ICOText.setBounds(10,100,170, 25);
		isDynamicCriteriaExecution.setBounds(10 , 130,170, 25);
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
		connectionPanel = loadConnectionPanel(screenSize,true,false,true);
		panel.add(connectionPanel);

		
		panel.add(orderToValidate);
		panel.add(orderType);
	
		panel.add(orderTypeT);
		
		panel.add(hookahBrowse);
		panel.add(hookahInfo);
		panel.add(start);
		panel.add(l2start);
		panel.add(serviceBuildInfo);
		panel.add(formatCSV);
		panel.add(criteriaExecution);
		panel.add(orderList);
		panel.add(usidInfo);
		panel.add(deliverSRF2);
		panel.add(icoWithChild);
		
		
		
	
		subPanel.add(setDate);
		subPanel.add(ICO);
		subPanel.add(isEndUser);
		subPanel.add(isContractingParty);
		subPanel.add(ICOText);
		subPanel.add(isDynamicCriteriaExecution);
		panel.add(subPanel);
		start.setEnabled(false);
		l2start.setEnabled(false);
		
		orderToValidate.addKeyListener(new KeyListener(){

            public void keyPressed(KeyEvent e){
            	orderTypeT.setText("...");
              
            }

			@Override
			public void keyReleased(KeyEvent arg0) {
				if(orderToValidate.getText().length()>0 ){
					start.setEnabled(true);
					l2start.setEnabled(true);
				}
				else{
					start.setEnabled(false);
					l2start.setEnabled(false);
				}
				
				try{
					if(Integer.parseInt(orderToValidate.getText())>0){
						start.setEnabled(true);
						l2start.setEnabled(true);
					}
				}catch(Exception e){
					start.setEnabled(false);
					l2start.setEnabled(false);
				}
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
        }
       );
		//continiousThread();

	}
	public void addProgressMonitorPane(){
		/*
		 * ADD PROGRESS PANEL
		 */
		JPanel progPane = ProgressMonitorPane.getInstance().getProgressPanel();
		progPane.setBounds(10,260,300,50);
		panel.add(progPane);
		
		
	}
	
	
	public JButton getExecuteCriteriaButton() {
		return criteriaExecution;
	}


	public JButton getExecuteOrderListButton() {
		return orderList;
	}

	public String getExternalFromdatePicker() {
		return fromDatePicker.getJFormattedTextField().getText();
	
	}
	
	public String getExternalTodatePicker() {
		return toDatePicker.getJFormattedTextField().getText();
	
	}
	
	public JButton getFormatCSVButton() {
		return formatCSV;
	}
	public CustomJCheckBox getHasShowSQLCheckBox(){
		return connectionPanel.getShowSQL();
	}
	
	public JButton getHookahBrowseButton() {
		return hookahBrowse;
	}
	public JButton getHookahInfoButton() {
		return hookahInfo;
	}
	public CustomJCheckBox getICOCheckBox(){
		return ICO;
	}
	
	public JTextField getICOTextBox(){
		return ICOText;
	}
	private Date getInternalFromdatePicker() {
		Date selectedDate = (Date) fromDatePicker.getModel().getValue();
		return selectedDate;
	}
	
	
	private Date getInternalTodatePicker() {
		Date selectedDate = (Date) toDatePicker.getModel().getValue();
		return selectedDate;
	}
	public JButton getL2StartButton() {
		return l2start;
	}
	public JButton getMLANStartButton() {
		return start;
	}
	
	public JButton getUSIDInfoButton() {
		return usidInfo;
	}
	public JButton getDeliverSRF2Button() {
		return deliverSRF2;
	}
	
	public JButton getICOWithChildButton() {
		return icoWithChild;
	}
	
	public JTextField getOrderToValidate() {
		return orderToValidate;
	}

	public Label getOrderTypeT() {
		return orderTypeT;
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	
	public JButton getServiceBuildInfoButton() {
		return serviceBuildInfo;
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
	
	

	public boolean isDynamicCriteriaExecution(){
		return isDynamicCriteriaExecution.isSelected();
	}
	
	public CustomRadioButton getIncludeSiteChange() {
		return includeSiteChange;
	}
	
	public CustomRadioButton getIsEndUser() {
		return isEndUser;
	}
	
	public CustomRadioButton getIsContractingParty() {
		return isContractingParty;
	}
	
	public CustomRadioButton getPleaseSelect() {
		return pleaseSelect;
	}

	public CustomJCheckBox getIncludeUpwards() {
		return includeUpwards;
	}

	public CustomRadioButton getExcludeSiteChange() {
		return excludeSiteChange;
	}
	public JButton getOrderHierarchy() {
		return orderHierarchy;
	}
	public ConnectionPanel getConnextionPanel(){
		return connectionPanel;
	}
}
