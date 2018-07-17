package com.orange.ui.component;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.orange.ui.component.custom.CSMJTextArea;
import com.orange.ui.component.custom.CustomJCheckBox;
import com.orange.ui.component.custom.CustomJPanel;
import com.orange.ui.component.custom.CustomRadioButton;
import com.orange.util.CSVINTable;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionPanel;
import com.orange.util.CustomViewComponents;
import com.orange.util.GenericTitledBorder;
import com.orange.util.ProgressMonitorPane;

public class SiteFixedViewComponents extends CustomViewComponents {

	private JButton browse;
	private CustomJCheckBox checkDEDUP;

	

	private ConnectionPanel connectionPanel;
	private CustomJCheckBox csiSync;
	private JButton dedupbrowse;

	private JButton dedupFileAnalysis;
	private JLabel deduplabel;
	
	private JButton eQL2InstanceMapping;

	private CSVINTable dedupshow;

	private JTextField dedupToValidate;
	private JTextField fileToValidate;
	private CustomJCheckBox generateSQLs;
	private JButton goldCSISync;
	private CustomJCheckBox goldSync;
	

	private ButtonGroup isLegacyGroup;
	private JLabel label;
	private JButton linkedUpSites;
	private JButton linkedUpSitesAnalysis;


	
	private JButton migrationRelocation;
	
	

	private JPanel panel;
	private JButton pcsm;
	private JButton personUpdate;

	private JButton productOrdersHierarchy;

	private CSMJTextArea queryResult;
	private JScrollPane scrollPane;
	private CSVINTable show;
	private JButton validate;
	private JButton validateForSpecialChars;

	private CustomJCheckBox withCSIAnalysis;
	
	
	private CustomRadioButton withLegacy, withOutLegacy;

	

	private CustomJCheckBox withSEUSIDValidate;

	private class CSISQLEventClass implements ChangeListener {

		public void stateChanged(ChangeEvent changeEvent) {
			boolean selected = withCSIAnalysis.isSelected();
			if (selected) {
				// csiSync.setSelected(true);
			}
			if (!selected) {
				// ConnectionForGOLD.closeConnection();
			}

		}

	}

	private class DEDUPFileEventClass implements ChangeListener {

		public void stateChanged(ChangeEvent changeEvent) {
			boolean selected = checkDEDUP.isSelected();
			if (selected) {

				deduplabel.setEnabled(true);
				dedupToValidate.setEnabled(true);
				dedupbrowse.setEnabled(true);
				dedupshow.setEnabled(true);

			}
			if (!selected) {
				deduplabel.setEnabled(false);
				dedupToValidate.setEnabled(false);
				dedupbrowse.setEnabled(false);
				dedupshow.setEnabled(false);
			}

		}

	}
	
	public SiteFixedViewComponents(Dimension screenSize) {
		super();
		isLegacyGroup = new ButtonGroup();

		panel = new CustomJPanel();
		panel.setLayout(null);

		queryResult = getQueryResult();
		fileToValidate = getFileToValidate();

		show = new CSVINTable("Show CSV", fileToValidate);

		

		goldSync = new CustomJCheckBox("With GOLD Extract?");
		
		csiSync = new CustomJCheckBox("With CSI Extract?");
		

		withCSIAnalysis = new CustomJCheckBox("With CSI SQL Script?");
		withCSIAnalysis.addChangeListener(new CSISQLEventClass());
		withSEUSIDValidate = new CustomJCheckBox("With SEUSID Validate?");

		generateSQLs = new CustomJCheckBox("With Generate SQLs?");
		withLegacy = new CustomRadioButton("With Legacy");
		withOutLegacy = new CustomRadioButton("WithOut Legacy", true);
		checkDEDUP = new CustomJCheckBox("With Prev DEDUP Check?");
		generateSQLs.setForeground(Color.BLUE);
		withLegacy.setForeground(Color.RED);
		withOutLegacy.setForeground(Color.RED);
		checkDEDUP.setForeground(Color.BLUE);

		
		label = new JLabel("Please select the file(csv)");
	

		browse = new JButton("Browse");

		deduplabel = new JLabel("Please select the dedup file(csv)");
		dedupToValidate = new JTextField();
		dedupbrowse = new JButton("Browse");
		dedupshow = new CSVINTable("Show CSV", dedupToValidate);
		deduplabel.setEnabled(false);
		dedupToValidate.setEnabled(false);
		dedupbrowse.setEnabled(false);
		dedupshow.setEnabled(false);

		validate = new JButton("Site FIX");

		validateForSpecialChars = new JButton("Validate Special Char");
		goldCSISync = new JButton("GOLD CSI Sync");

		linkedUpSites = new JButton("Linked Up Sites");
		linkedUpSitesAnalysis = new JButton("Linked Up Sites Analysis");
		linkedUpSitesAnalysis.setForeground(Color.BLUE);

		
		JPanel subPanel = new CustomJPanel();
		subPanel.setLayout(null);
		GenericTitledBorder titled = new GenericTitledBorder("Others- No input file needed");
		subPanel.setBorder(titled);

		subPanel.setBounds(20 + screenSize.width * 60 / 100, 45,screenSize.width * 20 / 100, 220);
		validateForSpecialChars.setBounds(25, 20, screenSize.width * 15 / 100,25);

		goldSync.setBounds(25, 60, screenSize.width * 15 / 100, 20);
		csiSync.setBounds(25, 90, screenSize.width * 15 / 100, 20);
		withCSIAnalysis.setBounds(25, 120, screenSize.width * 15 / 100, 20);
		withSEUSIDValidate.setBounds(25, 150, screenSize.width * 15 / 100, 20);
		goldCSISync.setBounds(25, 180, screenSize.width * 15 / 100, 25);

		goldSync.setForeground(Color.blue);
		csiSync.setForeground(Color.blue);
		withCSIAnalysis.setForeground(Color.blue);
		withSEUSIDValidate.setForeground(Color.blue);
		goldCSISync.setForeground(Color.blue);

		subPanel.add(validateForSpecialChars);
		subPanel.add(goldCSISync);
		subPanel.add(goldSync);
		subPanel.add(csiSync);
		subPanel.add(withCSIAnalysis);
		subPanel.add(withSEUSIDValidate);

		panel.add(subPanel);

		JPanel subPanel1 = new CustomJPanel();
		subPanel1.setLayout(null);
		GenericTitledBorder titled1 = new GenericTitledBorder("LinkedUp Sites");
		subPanel1.setBorder(titled1);
		subPanel1.setBounds(20 + screenSize.width * 60 / 100, 270,
				screenSize.width * 20 / 100, 180);
		linkedUpSites.setBounds(25, 20, screenSize.width * 15 / 100, 25);
		withLegacy.setBounds(25, 55, 100, 20);
		withOutLegacy.setBounds(130, 55, 120, 20);
		generateSQLs.setBounds(25, 80, screenSize.width * 15 / 100, 20);

		withLegacy.setToolTipText("With Legacy");
		withOutLegacy.setToolTipText("With Out Legacy");
		checkDEDUP.setBounds(25, 105, screenSize.width * 15 / 100, 20);
		linkedUpSitesAnalysis.setBounds(25, 130, screenSize.width * 15 / 100,
				25);

		isLegacyGroup.add(withLegacy);
		isLegacyGroup.add(withOutLegacy);

		subPanel1.add(linkedUpSites);
		subPanel1.add(linkedUpSitesAnalysis);
		subPanel1.add(generateSQLs);
		subPanel1.add(withLegacy);
		subPanel1.add(withOutLegacy);
		subPanel1.add(checkDEDUP);
		checkDEDUP.addChangeListener(new DEDUPFileEventClass());
		checkDEDUP.setToolTipText("<html><body>This feature will make sure the replacement sitecode <br> should double check with previous dedup reuns.<br> Following logic will be applicable. <ol><li>If Sitecode Not Found in Previous DEDUP : >> Replacement sitecode remain unchanged</li><li>If Found in Previous DEDUP as Latest TRUE : >> Replacement sitecode remain unchanged.</li><li>If Found in Previous DEDUP as Latest FALSE: >> Corresponding true sitecode of dedup will replace the replacement sitecode. And will act as replacement sitecode.</li></ol><b> Please make sure , Previous dedup file must be consolidated <br>with latest and updated in correct format.<br> expically in terem of GID and Sitecode. ");
		panel.add(subPanel1);

		JPanel subPanel2 = new CustomJPanel();
		subPanel2.setLayout(null);
		GenericTitledBorder titled2 = new GenericTitledBorder("Others...");
		subPanel2.setBorder(titled2);
		subPanel2.setBounds(50 + screenSize.width * 80 / 100, 190,screenSize.width * 15 / 100, 210);

		migrationRelocation = new JButton("Migration Relocation");
		migrationRelocation.setToolTipText("Migration Relocation");
		migrationRelocation.setBounds(10, 20, screenSize.width * 12 / 100, 25);

		pcsm = new JButton("Partial Customer Site Migration");
		pcsm.setToolTipText("Partial Customer Site Migration");
		pcsm.setBounds(10, 50, screenSize.width * 12 / 100, 25);

		personUpdate = new JButton("Update Person Details");
		personUpdate.setToolTipText("Update Person Details");
		personUpdate.setBounds(10, 80, screenSize.width * 12 / 100, 25);

		productOrdersHierarchy = new JButton("Product Orders Hierarchy");
		productOrdersHierarchy.setToolTipText("Product Orders Hierarchy");
		productOrdersHierarchy.setBounds(10, 110, screenSize.width * 12 / 100,25);

		dedupFileAnalysis = new JButton("Dedup File Analysis");
		dedupFileAnalysis.setToolTipText("Dedup File Analysis");
		dedupFileAnalysis.setBounds(10, 140, screenSize.width * 12 / 100, 25);
		
		eQL2InstanceMapping = new JButton("EQL2InstanceMapping Sync");
		eQL2InstanceMapping.setToolTipText("EQL2InstanceMapping Synchronization");
		eQL2InstanceMapping.setBounds(10, 170, screenSize.width * 12 / 100, 25);
		
		
		
		
		subPanel2.add(migrationRelocation);
		subPanel2.add(pcsm);
		subPanel2.add(personUpdate);
		subPanel2.add(productOrdersHierarchy);
		subPanel2.add(dedupFileAnalysis);
		subPanel2.add(eQL2InstanceMapping);
		

		
		panel.add(subPanel2);
		

		

		scrollPane = new JScrollPane(queryResult);

		label.setBounds(10, 10, screenSize.width * 10 / 100, 25);
		

		
		fileToValidate.setBounds(20 + screenSize.width * 10 / 100, 10,
				screenSize.width * 30 / 100, 25);
		browse.setBounds(30 + screenSize.width * 40 / 100, 10,
				screenSize.width * 15 / 100, 25);
		show.setBounds(30 + screenSize.width * 40 / 100, 45,
				screenSize.width * 15 / 100, 25);
		validate.setBounds(40 + screenSize.width * 60 / 100, 10,
				screenSize.width * 15 / 100, 25);

		deduplabel.setBounds(10, 240, screenSize.width * 10 / 100, 25);
		dedupToValidate.setBounds(20 + screenSize.width * 10 / 100, 240,
				screenSize.width * 30 / 100, 25);
		dedupbrowse.setBounds(30 + screenSize.width * 40 / 100, 240,
				screenSize.width * 15 / 100, 25);
		dedupshow.setBounds(30 + screenSize.width * 40 / 100, 285,
				screenSize.width * 15 / 100, 25);

		
		panel.add(label);
	
		
		/*
		 * ADD MEMORY MONITOR PANE
		 */
		JPanel memPanel = CommonUtils.getMemoryMonitorPane(screenSize.width * 35 / 100);
		memPanel.setBounds(0,300,screenSize.width * 35 / 100,70);
		panel.add(memPanel);
		
		

		panel.add(fileToValidate);
		panel.add(browse);

		panel.add(show);
		panel.add(validate);
		

		panel.add(deduplabel);
		panel.add(dedupToValidate);
		panel.add(dedupbrowse);
		panel.add(dedupshow);

	
		/*
		 * ADD CONNECTION PANE
		 */
		connectionPanel = loadConnectionPanel(screenSize,true,true,false);
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

	public JButton getBrowse() {
		return browse;
	}

	public CustomJCheckBox getCheckDEDUP() {
		return checkDEDUP;
	}

	public CustomJCheckBox getCSISync() {
		return csiSync;
	}

	public JButton getDedupbrowse() {
		return dedupbrowse;
	}

	public JButton getDedupFileAnalysis() {
		return dedupFileAnalysis;
	}
	public JButton getEQL2InstanceMapping() {
		return eQL2InstanceMapping;
	}
	public JTextField getDedupToValidate() {
		return dedupToValidate;
	}

	public CustomJCheckBox getGenerateSQLs() {
		return generateSQLs;
	}

	public JButton getGOLDCSISync() {
		return goldCSISync;
	}

	public CustomJCheckBox getGoldSync() {
		return goldSync;
	}
	
	public JButton getLinkedupSitesAnalysisButton() {
		return linkedUpSitesAnalysis;
	}

	public JButton getLinkedupSitesButton() {
		return linkedUpSites;
	}

	public JButton getMigrationRelocation() {
		return migrationRelocation;
	}

	

	public JPanel getPanel() {
		return panel;
	}

	

	public JButton getPartialCustomerSiteMigrationButton() {
		return pcsm;
	}

	public JButton getPersonUpdate() {
		return personUpdate;
	}

	public JButton getProductOrdersHierarchy() {
		return productOrdersHierarchy;
	}

	

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public JButton getValidate() {
		return validate;
	}

	public JButton getValidateForSplChr() {
		return validateForSpecialChars;
	}

	public CustomJCheckBox getWithSEUSIDValidate() {
		return withSEUSIDValidate;
	}

	public CustomJCheckBox getwWithAnalysis() {
		return withCSIAnalysis;
	}

	public boolean hasValidateFromCSI() {
		return connectionPanel.getCsiConnect().isSelected();
	}

	public boolean hasValidateFromGOLD() {
		return connectionPanel.getGoldConnect().isSelected();
	}

	public CustomRadioButton isLegacyRequired() {
		return withLegacy;
	}
	public ConnectionPanel getConnextionPanel(){
		return connectionPanel;
	}

}
