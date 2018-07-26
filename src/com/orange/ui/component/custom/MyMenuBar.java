package com.orange.ui.component.custom;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.orange.help.events.CSISyncFormatEvent;
import com.orange.help.events.DedupFileAnalysisFormatEvent;
import com.orange.help.events.DedupFormatEvent;
import com.orange.help.events.HookhaFileAnalysisFormatEvent;
import com.orange.help.events.LinkedupSitesAnalysisFormatEvent;
import com.orange.help.events.LinkedupSitesFormatEvent;
import com.orange.help.events.OrderHierarchyFileAnalysisFormatEvent;
import com.orange.help.events.PCSMFormatEvent;
import com.orange.help.events.PersonUpdateFormatEvent;
import com.orange.help.events.ProductOrderHirFormatEvent;
import com.orange.help.events.SiteFixFormatEvent;
import com.orange.help.events.SplCharFormatEvent;
import com.orange.util.CommonUtils;

public class MyMenuBar extends JMenuBar{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JMenu autoUpdate;
	private JMenuItem autoUpdateItem;
	private JMenu encryptor;
	private JMenuItem goldEncryptorItem,csiEncryptorItem,archivalEncryptorItem;
	private JMenuItem csiSyncFormat;
	private JMenu csiSyncHelp;
	private JMenuItem csiSyncTextHelp;
	private JMenu csm;
	private JMenuItem dataBypassFile;
	private JMenuItem dedupFileAnalysisFormat;
	private JMenu dedupFileAnalysisHelp;
	private JMenuItem dedupFileAnalysisTextHelp;
	private JMenuItem dedupRun;
	private JMenuItem dedupRunFormat;
	private JMenu dedupRunHelp;
	private JMenuItem dedupRunTextHelp;
	private JMenu earrach;
	private JMenuItem earrachRun;
	private JMenu eventMenu;
	private JMenuItem format;
	private JMenu help;
	private JMenuItem hookaRunFormat;

	private JMenu hookaRunHelp;
	private JMenuItem hookaRunTextHelp;
	private JMenuItem imadaq;
	
	private final CustomJFrame CustomJFrame;
	private JMenu l3Products;

	private JMenuItem leftICO1File;
	private JMenuItem linkedupAnalysisFormat;
	private JMenu linkedupAnalysisHelp;
	
	private JMenuItem linkedupAnalysisTextHelp;
	private JMenuItem linkedupFormat;
	private JMenu linkedupHelp;
	private JMenuItem linkedupTextHelp;
	
	private JMenu lookNFeel;
	private JMenu  memoryUsages,customConfig,elkConfig;
	private JMenuItem memoryUsagesItem,customConfigItem,elkConfigItem;
	
	private JMenu mergeErrors;
	private JMenuItem mergeErrorsFromImpectedFile;
	
	private JMenuItem mergeErrorsFromMigratedFile; 
	private JMenuItem mergeMigrationMigrated; 
	private JMenuItem MLAN;
	
	
	private JMenuItem open; 
	private JMenuItem orderHierarchyRunFormat; 
	private JMenu orderHierarchyRunHelp;
	
	private JMenuItem orderHierarchyRunTextHelp; 
	private JMenuItem packageData; 
	private JMenuItem pcsmRunFormat;
	
	
	private JMenu pcsmRunHelp; 
	private JMenuItem pcsmRunTextHelp; 
	private JMenuItem personUpdateFormat;
	
	private JMenu personUpdateHelp; 
	private JMenuItem personUpdateTextHelp; 
	private JMenuItem productOrderHirFormat;
	
	private JMenu productOrderHirHelp; 
	private JMenuItem productOrderHirTextHelp; 
	private JMenu prospectiveMenu;
	
	
	private JMenu queryExe; 
	private JMenuItem queryExecutor; 
	private JMenuItem refresh;
	
	private JMenu rules;
	private JMenuItem rulesRun;
	
	private JMenu imadaqV02Menu;
	private JMenuItem imadaqV02MenuItem;
	
	private JMenu serviceBuildInfo;
	
	private JMenu siteFix;
	private JMenuItem siteFixed;
	private JMenuItem siteFixFormat;
	
	
	private JMenuItem siteFixHelp;
	
	private JMenuItem splCharFormat;
	
	private JMenu splCharHelp;
	private JMenuItem splCharTextHelp;
	
	private JMenuItem validate;
	
	public static String selectedLookNFeel;

	public static String getSelectedLookNFeel() {
		return selectedLookNFeel;
	}


	public MyMenuBar(CustomJFrame frame) {
		this.CustomJFrame = frame;
		Font f = new Font("sans-serif", Font.PLAIN, 12);
		UIManager.put("Menu.font", f);
		UIManager.put("MenuItem.font", f);
		UIManager.put("Menu.background", Color.WHITE);;
		UIManager.put("MenuItem.background", Color.WHITE);
		UIManager.put("Menu.opaque", true);
		UIManager.put("MenuItem.opaque", true);
		
		
		
		//menuBar = new JMenuBar();
		//menuBar.setBackground(Color.orange);
		//menuBar.setForeground(Color.white);
		
				
		eventMenu = new JMenu("Events");
		
		eventMenu.setMnemonic(KeyEvent.VK_E);
		
		this.add(eventMenu);

		prospectiveMenu = new JMenu("Perspective");
		prospectiveMenu.setMnemonic(KeyEvent.VK_P);
		this.add(prospectiveMenu);

		open = new JMenuItem("Open Wave");
		open.setMnemonic(KeyEvent.VK_O);
		

		format = new JMenuItem("Format CSVs");
		format.setMnemonic(KeyEvent.VK_F);
		
		
		
		refresh = new JMenuItem("Refresh");
		refresh.setMnemonic(KeyEvent.VK_R);
		
		
		validate = new JMenuItem("Validate");
		validate.setMnemonic(KeyEvent.VK_V);
		
		
		leftICO1File = new JMenuItem("Left ICO1 in File");
		leftICO1File.setMnemonic(KeyEvent.VK_L);
		

		dataBypassFile = new JMenuItem("Data Bypass ICO1 in File");
		dataBypassFile.setMnemonic(KeyEvent.VK_D);
		

		

	
		mergeMigrationMigrated = new JMenuItem(
				"Merge MigrationMigratedOrderReport");
		mergeMigrationMigrated.setMnemonic(KeyEvent.VK_M);
		

		packageData = new JMenuItem("Package");
		packageData.setMnemonic(KeyEvent.VK_P);
		
		mergeErrors = new JMenu("Data Admin Activity");
		mergeErrors.setMnemonic(KeyEvent.VK_D);
		
		eventMenu.add(open);
		eventMenu.addSeparator();
		eventMenu.add(format);
		eventMenu.add(refresh);
		eventMenu.addSeparator();
		eventMenu.add(validate);
		eventMenu.addSeparator();
		eventMenu.add(leftICO1File);
		eventMenu.add(dataBypassFile);
		eventMenu.addSeparator();
		eventMenu.add(mergeMigrationMigrated);
		eventMenu.add(packageData);
		eventMenu.addSeparator();
		eventMenu.add(mergeErrors);

		mergeErrorsFromImpectedFile = new JMenuItem(
				"Merge Errors(From ImpectedOrders Report)");
		mergeErrorsFromImpectedFile.setMnemonic(KeyEvent.VK_O);
		

		mergeErrorsFromMigratedFile = new JMenuItem(
				"Merge Errors (From MigratedOrder Report)");
		mergeErrorsFromMigratedFile.setMnemonic(KeyEvent.VK_M);
		
		mergeErrors.add(mergeErrorsFromImpectedFile);
		mergeErrors.addSeparator();
		mergeErrors.add(mergeErrorsFromMigratedFile);

		imadaq = new JMenuItem("IMADAQ");
		imadaq.setMnemonic(KeyEvent.VK_I);
		

		csm = new JMenu("CSM");
		packageData.setMnemonic(KeyEvent.VK_C);
		
		rules = new JMenu("Rules Extraction");
		packageData.setMnemonic(KeyEvent.VK_R);
		
		earrach = new JMenu("Earrach");
		
		dedupRun = new JMenuItem("Dedup Run");
		rulesRun = new JMenuItem("Rules Run");
		earrachRun = new JMenuItem("Earrach");
	
		
		
	
		siteFixed = new JMenuItem("Site FIX");
	
		queryExe = new JMenu("Query-Executor");
		queryExecutor = new JMenuItem("QueryExecutor");
		queryExe.add(queryExecutor);
		queryExe.setVisible(false);
	
		imadaqV02Menu = new JMenu("IMADAQ V02");
		imadaqV02MenuItem = new JMenuItem("IMADAQ V02");
		imadaqV02Menu.add(imadaqV02MenuItem);
		
		
		csm.add(dedupRun);
		csm.addSeparator();
		
		csm.add(siteFixed);
	
		rules.add(rulesRun);
		earrach.add(earrachRun);
		
		prospectiveMenu.add(imadaq);
		prospectiveMenu.addSeparator();
		prospectiveMenu.add(csm);
		prospectiveMenu.add(rules);
		prospectiveMenu.add(earrach);
		prospectiveMenu.add(queryExe);
		prospectiveMenu.add(imadaqV02Menu);
		
		
		
		serviceBuildInfo = new JMenu("Service Build Info");
		l3Products = new JMenu("Products");
	
		MLAN = new JMenuItem("MLAN/L2");
		l3Products.add(MLAN);
		serviceBuildInfo.add(l3Products);
	
		prospectiveMenu.add(serviceBuildInfo);
		
		this.add(eventMenu);
		this.add(prospectiveMenu);
		eventMenu.setEnabled(false);
		
			
		open.setIcon(CommonUtils.getIcon(Icons.iconPath5));
		format.setIcon(CommonUtils.getIcon(Icons.iconPath6));
		refresh.setIcon(CommonUtils.getIcon(Icons.iconPath7));
		validate.setIcon(CommonUtils.getIcon(Icons.iconPath8));
		leftICO1File.setIcon(CommonUtils.getIcon(Icons.iconPath9));
		dataBypassFile.setIcon(CommonUtils.getIcon(Icons.iconPath10));
		mergeMigrationMigrated.setIcon(CommonUtils.getIcon(Icons.iconPath11));
		packageData.setIcon(CommonUtils.getIcon(Icons.iconPath12));
		mergeErrors.setIcon(CommonUtils.getIcon(Icons.iconPath13));
		mergeErrorsFromImpectedFile.setIcon(CommonUtils.getIcon(Icons.iconPath13));
		mergeErrorsFromMigratedFile.setIcon(CommonUtils.getIcon(Icons.iconPath14));
		
		imadaq.setIcon(CommonUtils.getIcon(Icons.iconPath1)); 
		csm.setIcon(CommonUtils.getIcon(Icons.iconPath2)); 
		rules.setIcon(CommonUtils.getIcon(Icons.ruleIcon)); 
		dedupRun.setIcon(CommonUtils.getIcon(Icons.iconPath2)); 
		rulesRun.setIcon(CommonUtils.getIcon(Icons.ruleIcon)); 
		earrach.setIcon(CommonUtils.getIcon(Icons.iconPathEarrach)); 
		earrachRun.setIcon(CommonUtils.getIcon(Icons.iconPathEarrach)); 

		
		siteFixed.setIcon(CommonUtils.getIcon(Icons.iconPath15)); 
	

		MLAN.setIcon(CommonUtils.getIcon(Icons.iconPath4)); 
		serviceBuildInfo.setIcon(CommonUtils.getIcon(Icons.iconPath4));
		l3Products.setIcon(CommonUtils.getIcon(Icons.iconPath4));
		
		
		queryExe.setIcon(CommonUtils.getIcon(Icons.queryExecutorICON)); 
		queryExecutor.setIcon(CommonUtils.getIcon(Icons.queryExecutorICON)); 
		
		
		imadaqV02Menu.setIcon(CommonUtils.getIcon(Icons.imadaqV02Icon)); 
		imadaqV02MenuItem.setIcon(CommonUtils.getIcon(Icons.imadaqV02Icon)); 
		
		
		
		
		/**   --------------- Help **/
		
		help = new JMenu("Help");
		siteFix = new JMenu("Site FIX");
		siteFixFormat = new JMenuItem("CSV FORMAT");
		siteFixHelp = new JMenuItem("Help");
		
		siteFix.add(siteFixFormat);
		siteFix.add(siteFixHelp);
		help.add(siteFix);
		
		
		
		help.setIcon(CommonUtils.getIcon(Icons.newHelpICON)); 
		
		siteFix.setIcon(CommonUtils.getIcon(Icons.helpIcon)); 
		siteFixFormat.setIcon(CommonUtils.getIcon(Icons.helpFormat)); 
		siteFixHelp.setIcon(CommonUtils.getIcon(Icons.helpText)); 
		
		
		dedupRunHelp = new JMenu("DEDUP RUN Help");
		dedupRunFormat =new JMenuItem("DEDUP Run FORMAT");
		dedupRunTextHelp =new JMenuItem("Help");
		
		
		dedupRunHelp.setIcon(CommonUtils.getIcon(Icons.helpIcon)); 
		dedupRunFormat.setIcon(CommonUtils.getIcon(Icons.helpFormat)); 
		dedupRunTextHelp.setIcon(CommonUtils.getIcon(Icons.helpText)); 
		
		dedupRunHelp.add(dedupRunFormat);
		dedupRunHelp.add(dedupRunTextHelp);
		help.add(dedupRunHelp);
		
		pcsmRunHelp = new JMenu("Partial Customer Site Migration Help");
		pcsmRunFormat =new JMenuItem("Partial Customer Site Migration FORMAT");
		pcsmRunTextHelp =new JMenuItem("Help");
		
		
		pcsmRunHelp.setIcon(CommonUtils.getIcon(Icons.helpIcon)); 
		pcsmRunFormat.setIcon(CommonUtils.getIcon(Icons.helpFormat)); 
		pcsmRunTextHelp.setIcon(CommonUtils.getIcon(Icons.helpText)); 
		
		pcsmRunHelp.add(pcsmRunFormat);
		pcsmRunHelp.add(pcsmRunTextHelp);
		help.add(pcsmRunHelp);
		
		
		splCharHelp = new JMenu("Special Char Sitecodes Help");
		splCharFormat =new JMenuItem("Special Char Sitecodes FORMAT");
		splCharTextHelp =new JMenuItem("Help");
		
		
		splCharHelp.setIcon(CommonUtils.getIcon(Icons.helpIcon)); 
		splCharFormat.setIcon(CommonUtils.getIcon(Icons.helpFormat)); 
		splCharTextHelp.setIcon(CommonUtils.getIcon(Icons.helpText)); 
		
		splCharHelp.add(splCharFormat);
		splCharHelp.add(splCharTextHelp);
		help.add(splCharHelp);
		
		
		
		linkedupHelp = new JMenu("Linked Up Sites Help");
		linkedupFormat =new JMenuItem("LinkeddUp Sites FORMAT");
		linkedupTextHelp =new JMenuItem("Help");
		
		
		linkedupHelp.setIcon(CommonUtils.getIcon(Icons.helpIcon)); 
		linkedupFormat.setIcon(CommonUtils.getIcon(Icons.helpFormat)); 
		linkedupTextHelp.setIcon(CommonUtils.getIcon(Icons.helpText)); 
		
		linkedupHelp.add(linkedupFormat);
		linkedupHelp.add(linkedupTextHelp);
		help.add(linkedupHelp);
		
		
		linkedupAnalysisHelp = new JMenu("Linked Up Sites Analysis Help");
		linkedupAnalysisFormat =new JMenuItem("LinkeddUp Sites Analysis FORMAT");
		linkedupAnalysisTextHelp =new JMenuItem("Help");
		
		
		linkedupAnalysisHelp.setIcon(CommonUtils.getIcon(Icons.helpIcon)); 
		linkedupAnalysisFormat.setIcon(CommonUtils.getIcon(Icons.helpFormat)); 
		linkedupAnalysisTextHelp.setIcon(CommonUtils.getIcon(Icons.helpText)); 
		
		linkedupAnalysisHelp.add(linkedupAnalysisFormat);
		linkedupAnalysisHelp.add(linkedupAnalysisTextHelp);
		help.add(linkedupAnalysisHelp);
		
		
		csiSyncHelp = new JMenu("CSI Sync SQL Script Help");
		csiSyncFormat =new JMenuItem("CSI Sync FORMAT");
		csiSyncTextHelp =new JMenuItem("Help");
		
		csiSyncHelp.setIcon(CommonUtils.getIcon(Icons.helpIcon)); 
		csiSyncFormat.setIcon(CommonUtils.getIcon(Icons.helpFormat)); 
		csiSyncTextHelp.setIcon(CommonUtils.getIcon(Icons.helpText)); 
		
		csiSyncHelp.add(csiSyncFormat);
		csiSyncHelp.add(csiSyncTextHelp);
		help.add(csiSyncHelp);
		
		
		personUpdateHelp = new JMenu("Person Update Help");
		personUpdateFormat =new JMenuItem("Person Update FORMAT");
		personUpdateTextHelp =new JMenuItem("Help");
		
		personUpdateHelp.setIcon(CommonUtils.getIcon(Icons.helpIcon)); 
		personUpdateFormat.setIcon(CommonUtils.getIcon(Icons.helpFormat)); 
		personUpdateTextHelp.setIcon(CommonUtils.getIcon(Icons.helpText)); 
		
		
		personUpdateHelp.add(personUpdateFormat);
		personUpdateHelp.add(personUpdateTextHelp);
		help.add(personUpdateHelp);
		
		
		
		productOrderHirHelp = new JMenu("Product Order Hierarchy Help");
		productOrderHirFormat =new JMenuItem("Product Order Hierarchy FORMAT");
		productOrderHirTextHelp =new JMenuItem("Help");
		
		productOrderHirHelp.setIcon(CommonUtils.getIcon(Icons.helpIcon)); 
		productOrderHirFormat.setIcon(CommonUtils.getIcon(Icons.helpFormat)); 
		productOrderHirTextHelp.setIcon(CommonUtils.getIcon(Icons.helpText)); 
		
		
		productOrderHirHelp.add(productOrderHirFormat);
		productOrderHirHelp.add(productOrderHirTextHelp);
		help.add(productOrderHirHelp);
		
		
		dedupFileAnalysisHelp = new JMenu("DEDUP File Analysis Help");
		dedupFileAnalysisFormat =new JMenuItem("DEDUP File Analysis FORMAT");
		dedupFileAnalysisTextHelp =new JMenuItem("Help");
		
		dedupFileAnalysisHelp.setIcon(CommonUtils.getIcon(Icons.helpIcon)); 
		dedupFileAnalysisFormat.setIcon(CommonUtils.getIcon(Icons.helpFormat)); 
		dedupFileAnalysisTextHelp.setIcon(CommonUtils.getIcon(Icons.helpText)); 
		
		
		dedupFileAnalysisHelp.add(dedupFileAnalysisFormat);
		dedupFileAnalysisHelp.add(dedupFileAnalysisTextHelp);
		help.add(dedupFileAnalysisHelp);
		
		
		
		hookaRunHelp = new JMenu("Hookha File Analysis Help");
		hookaRunFormat =new JMenuItem("Hookha File Analysis FORMAT");
		hookaRunTextHelp =new JMenuItem("Help");
		
		hookaRunHelp.setIcon(CommonUtils.getIcon(Icons.helpIcon)); 
		hookaRunFormat.setIcon(CommonUtils.getIcon(Icons.helpFormat)); 
		hookaRunTextHelp.setIcon(CommonUtils.getIcon(Icons.helpText)); 
		
		
		hookaRunHelp.add(hookaRunFormat);
		hookaRunHelp.add(hookaRunTextHelp);
		help.add(hookaRunHelp);
		
		orderHierarchyRunHelp = new JMenu("OrderHierarchy File Analysis Help");
		orderHierarchyRunFormat =new JMenuItem("OrderHierarchy File Analysis FORMAT");
		orderHierarchyRunTextHelp =new JMenuItem("Help");
		
		orderHierarchyRunHelp.setIcon(CommonUtils.getIcon(Icons.helpIcon)); 
		orderHierarchyRunFormat.setIcon(CommonUtils.getIcon(Icons.helpFormat)); 
		orderHierarchyRunTextHelp.setIcon(CommonUtils.getIcon(Icons.helpText)); 
		
		
		orderHierarchyRunHelp.add(orderHierarchyRunFormat);
		orderHierarchyRunHelp.add(orderHierarchyRunTextHelp);
		help.add(orderHierarchyRunHelp);
		
		
		
		
		
		/*
		 * Events
		 * 
		 */
		siteFixFormat.addActionListener(new SiteFixFormatEvent());
		dedupRunFormat.addActionListener(new DedupFormatEvent());
		splCharFormat.addActionListener(new SplCharFormatEvent());
		linkedupFormat.addActionListener(new LinkedupSitesFormatEvent());
		linkedupAnalysisFormat.addActionListener(new LinkedupSitesAnalysisFormatEvent());
		csiSyncFormat.addActionListener(new CSISyncFormatEvent());
		personUpdateFormat.addActionListener(new PersonUpdateFormatEvent());
		productOrderHirFormat.addActionListener(new ProductOrderHirFormatEvent());
		dedupFileAnalysisFormat.addActionListener(new DedupFileAnalysisFormatEvent());
		hookaRunFormat.addActionListener(new HookhaFileAnalysisFormatEvent());
		orderHierarchyRunFormat.addActionListener(new OrderHierarchyFileAnalysisFormatEvent());
		pcsmRunFormat.addActionListener(new PCSMFormatEvent());		
		
		
		
		
		lookNFeel = new JMenu("Look'N'Feel");
		ArrayList<String> listOfLnF=getListOfLNF();
		listOfLnF.add("com.jtattoo.plaf.smart.SmartLookAndFeel");
		listOfLnF.add("com.jtattoo.plaf.texture.TextureLookAndFeel");
		listOfLnF.add("com.jtattoo.plaf.noire.NoireLookAndFeel");
		listOfLnF.add("com.jtattoo.plaf.acryl.AcrylLookAndFeel");
		listOfLnF.add("com.jtattoo.plaf.aero.AeroLookAndFeel");
		listOfLnF.add("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
		listOfLnF.add("com.jtattoo.plaf.bernstein.BernsteinLookAndFeel");
		//listOfLnF.add("com.jtattoo.plaf.fast.FastLookAndFeel");
		//listOfLnF.add("com.jtattoo.plaf.graphite.GraphiteLookAndFeel");
		//listOfLnF.add("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
		listOfLnF.add("com.jtattoo.plaf.luna.LunaLookAndFeel");
		listOfLnF.add("com.jtattoo.plaf.mcwin.McWinLookAndFeel");
		listOfLnF.add("com.jtattoo.plaf.mint.MintLookAndFeel");
		
		
		for(String LF : listOfLnF){
			final JMenuItem feelMenuItem =new JMenuItem(LF);
			feelMenuItem.setIcon(CommonUtils.getIcon(Icons.helpIcon)); 
			lookNFeel.add(feelMenuItem);
			feelMenuItem.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent arg0) {
	            
	           
						try {
							UIManager.setLookAndFeel(feelMenuItem.getText());
						}catch(Exception E){CommonUtils.showExceptionStack(E);}
						selectedLookNFeel = feelMenuItem.getText();
						SwingUtilities.updateComponentTreeUI(CustomJFrame);
				
					
	            	
	            }
	        });
		}
		
		autoUpdate = new JMenu("Auto Update");
		autoUpdateItem = new JMenuItem("Auto Update Version");
		autoUpdate.add(autoUpdateItem);
		
		this.add(Box.createHorizontalGlue());
	
		/*
		 *  Encryptor
		 * 
		 */
		
		encryptor = new JMenu("Public Key");
		goldEncryptorItem = new JMenuItem("GOLD >> Public Key Generator");
		csiEncryptorItem = new JMenuItem("CSI >> Public Key Generator");
		archivalEncryptorItem = new JMenuItem("Archival >> Public Key Generator");
		
		encryptor.add(goldEncryptorItem);
		encryptor.add(csiEncryptorItem);
		encryptor.add(archivalEncryptorItem);
		
		/*
		 * User Configs
		 * 
		 * 
		 */
		customConfig = new JMenu("User Configuration");
		customConfigItem = new JMenuItem("User Configuration");
		customConfig.add(customConfigItem);
		
		/*
		 * ELK CONFIGURATION
		 * 
		 */
		
		elkConfig = new JMenu("ELK Configuration");
		elkConfigItem = new JMenuItem("ELK Configuration");
		elkConfig.add(elkConfigItem);
		
		/*
		 * memory 
		 * 
		 * 
		 */
		memoryUsages = new JMenu("Memory Usages");
		memoryUsagesItem = new JMenuItem("Memory Usages");
		memoryUsages.add(memoryUsagesItem);
		
		this.add(autoUpdate);
		this.add(encryptor);
		this.add(customConfig);
		this.add(elkConfig);
		this.add(memoryUsages);
		this.add(lookNFeel);
		this.add(help);
	}

		
	public JMenu getAutoUpdate() {
		return autoUpdate;
	}
	
	public JMenuItem getAutoUpdateItem() {
		return autoUpdateItem;
	}
	
	public JMenuItem getDataBypassFile() {
		return dataBypassFile;
	}
	public JMenuItem getDedupRun() {
		return dedupRun;
	}

	public JMenuItem getEarrach() {
		return earrach;
	}

	public JMenuItem getEarrachRun() {
		return earrachRun;
	}

	public JMenu getEventMenu() {
		return eventMenu;
	}

	public JMenuItem getFormat() {
		return format;
	}

	public JMenuItem getImadaq() {
		return imadaq;
	}

	public JMenuItem getLeftICO1File() {
		return leftICO1File;
	}

	private ArrayList<String> getListOfLNF(){
		ArrayList<String> lf = new ArrayList<String>();
		
		 UIManager.LookAndFeelInfo[] looks = UIManager.getInstalledLookAndFeels();
		    for (UIManager.LookAndFeelInfo look : looks) 
		    {
		    	lf.add(look.getClassName());
		    }
		    return lf;
	}

	public JMenuItem getMemoryUsagesItem(){
		return memoryUsagesItem;
	}
	
	public JMenuItem getCustomConfigItem(){
		return customConfigItem;
	}
	
	public JMenuItem getELKConfigItem(){
		return elkConfigItem;
	}
	
	
	public JMenuItem getGOLDEncryptor(){
		return goldEncryptorItem;
	}
	public JMenuItem getCSIEncryptor(){
		return csiEncryptorItem;
	}
	public JMenuItem getArchivalEncryptor(){
		return archivalEncryptorItem;
	}
	
	
	public JMenuBar getMenuBar() {
		return this;
	}
	
	public JMenuItem getMergeErrors() {
		return mergeErrors;
	}


	public JMenuItem getMergeErrorsFromImpectedFile() {
		return mergeErrorsFromImpectedFile;
	}


	public JMenuItem getMergeErrorsFromMigratedFile() {
		return mergeErrorsFromMigratedFile;
	}


	public JMenuItem getMergeMigrationMigrated() {
		return mergeMigrationMigrated;
	}


	public JMenuItem getMLAN() {
		return MLAN;
	}
	

	public JMenuItem getOpen() {
		return open;
	}

	public JMenuItem getPackageData() {
		return packageData;
	}

	public JMenu getProspectiveMenu() {
		return prospectiveMenu;
	}

	public JMenuItem getQueryExecutor() {
		return queryExecutor;
	}

	public JMenuItem getImadaqv02() {
		return imadaqV02MenuItem;
	}
	
	public JMenuItem getRefresh() {
		return refresh;
	}

	public JMenuItem getRules() {
		return rules;
	}

	public JMenuItem getRulesRun() {
		return rulesRun;
	}

	public JMenu getServiceBuildInfo(){
		return serviceBuildInfo;
	}

	public JMenuItem getSiteFixed() {
		return siteFixed;
	}

	public JMenuItem getValidate() {
		return validate;
	}
	
	public void setAutoUpdate(JMenu autoUpdate) {
		this.autoUpdate = autoUpdate;
	}
	
	public void setAutoUpdateItem(JMenuItem autoUpdateItem) {
		this.autoUpdateItem = autoUpdateItem;
	}
	
	

    	@Override
    	protected void paintComponent(Graphics g) {
    		 super.paintComponent(g);
    		 Properties properties1 						= CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV03);
    		if( properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") )
    		{	
		       
		        Graphics2D g2d = (Graphics2D) g;
		     
			    g2d.setColor(CustomColorTheme.menuBarColor);
			    g2d.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
    		}
    }
	
	
}
