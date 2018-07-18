package com.orange.ui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.net.ftp.FTPClient;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.L3.MLAN.CriteriaExecution;
import com.orange.L3.MLAN.DeliverSRF2Info;
import com.orange.L3.MLAN.DynamicCriteriaExecution;
import com.orange.L3.MLAN.HookahBuildInfo;
import com.orange.L3.MLAN.ICOWithChild;
import com.orange.L3.MLAN.MLANServiceBuild;
import com.orange.L3.MLAN.ServiceBuildFileFormater;
import com.orange.L3.MLAN.ServiceBuildInfo;
import com.orange.L3.MLAN.ServiceBuildInfoByOrders;
import com.orange.ui.component.custom.CustomJCheckBox;
import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.Directories;
import com.orange.ui.component.custom.Icons;
import com.orange.ui.component.custom.MyMenuBar;
import com.orange.ui.component.custom.ToolBar;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.ConnectionBeanArchived;
import com.orange.util.ConnectionBeanCSI;
import com.orange.util.CustomCSVWriter;
import com.orange.util.CustomJFileChooser;
import com.orange.util.CustomProperties;
import com.orange.util.DedupQueriesWindows;
import com.orange.util.ELKCustomProperties;
import com.orange.util.EQL2InstanceMappingAnalysis;
import com.orange.util.EarrachExtractQueryExecutor;
import com.orange.util.EarrachJSONParser;
import com.orange.util.EarrachJSON_D9Parser;
import com.orange.util.ErrorKeysWindows;
import com.orange.util.FTPDownloader;
import com.orange.util.FTPSettings;
import com.orange.util.JSONToCSVParser;
import com.orange.util.MultiLinerPanel;
import com.orange.util.ProgressMonitorPane;
import com.orange.util.PublicKeyGenerator;
import com.orange.util.PublicPrivateKeyGenerator;
import com.orange.util.RulesExtractQueryExecutor;
import com.orange.util.SetInfoPanel;
import com.orange.util.TreeFormation;
import com.orange.util.cibase.v02.CiBase_IOV_Update_Latest;
import com.orange.util.cibase.v02.CiBase_OKNAReader;
import com.orange.util.cibase.v02.DetailedAnalysis;
import com.orange.util.csm.DataTableCreator;
import com.orange.util.csm.DedupFileDataValidator;
import com.orange.util.csm.DedupFileValidator;
import com.orange.util.csm.GOLD_CSI_SYNC;
import com.orange.util.csm.GOLD_CSI_SYNC_Analysis;
import com.orange.util.csm.GOLD_CSI_SYNC_SEUSID_EX_Checks;
import com.orange.util.csm.LinkedupSiteFixDataValidator;
import com.orange.util.csm.LinkedupSiteFixFileValidator;
import com.orange.util.csm.OrderHierarchy;
import com.orange.util.csm.PartialCustomerSiteMigration;
import com.orange.util.csm.Previous_DEDUPConsolidation;
import com.orange.util.csm.SiteFixDataValidator;
import com.orange.util.csm.SiteFixFileValidator;
import com.orange.util.csm.Update_Person;
import com.orange.util.earrach.EarrachJSONSeparator;
import com.orange.util.earrach.XML2JTree;
import com.orange.util.imadaq.CSVImpectedOrderReportReader;
import com.orange.util.imadaq.CSVMigratedOrderReportReader;
import com.orange.util.imadaq.File2FolderFormat;
import com.orange.util.imadaq.MergeErrorsDataFromImpectedOrderFile;
import com.orange.util.imadaq.MergeErrorsDataFromMigratedOrderFile;
import com.orange.util.imadaq.MergeMigratedReport;
import com.orange.util.imadaq.PackageData;
import com.orange.util.imadaq.ValidateReport;
import com.orange.util.others.CreateIsValidPreSalesFixForPrevDedup;
import com.orange.util.others.CreateMultipleAvtiveFixINPrevDedup;
import com.orange.util.others.CreateReplaceReplacementFileForPrevDedup;
import com.orange.util.others.LinkedupSitesEventClass;
import com.orange.util.others.LinkedupSitesNext;
import com.orange.util.others.MemoryUsages;
import com.orange.util.others.MigrationRelocationOrdersFormat;
import com.orange.util.others.ProductOrdersHierarchy;
import com.orange.util.others.SiteCodeswithSpecialChars;

public class Main {
	FTPSettings FTPSettings = new FTPSettings();
	public Main(){
		
	}
	private class AutoUpdateEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try
			{
				String current = new java.io.File( "." ).getCanonicalPath();
				boolean isWhiteSpace = false;
				for(int x = 0; x<current.length();x++)
				{
					if (Character.isWhitespace(current.charAt(x))) 
					{
						isWhiteSpace = true;
						break;
				    }
				}
				if(!isWhiteSpace)
				{
					final Window pwin = CommonUtils.getProgressBar(100,"Downloading ..",true); 
					new Thread(new Runnable()
			        {
			          public void run()
			          {
			            try
			            {
			             autoUpdateVesrion(pwin);
			            }
			            catch ( Throwable th )
			            {
			            }
			          }
			        }).start();
					pwin.setVisible(true);
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Invalid Path found in exisiting HOME, (WhiteSpace availabe).\nAutoUpdate feature could not initiate.\nEither remove the whitespace from the utility HOME Path or \nUpdate Manually.");
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			}
	}
	private class ByPassEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
				//  Start Here
				
				CSVMigratedOrderReportReader CR = new CSVMigratedOrderReportReader();
				CR.createDataBypassFilesData(basePath);
				
				
				TreeFormation.setTreeFormation(basePath, sp);

				Set<String> count = CR.getCountandPath();
				Iterator<String> itr = count.iterator();
				infoPanel.setInfoLogs("Total " + count.size()+ " Files for DataByPass Created and Found..");
		
				while (itr.hasNext()) {
					String S = itr.next();
					infoPanel.setInfoLogs(S);
		
				}
				// Ends Here
				
				pwin.dispose();
			 } catch ( Exception e){pwin.dispose();}}}).start();
		    pwin.setVisible(true);
		}
	}
	private class ConsolidatedDEDUPFileBrowseEvent implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			
			final CustomJFileChooser fileChooser = new CustomJFileChooser();
			int returnValue = fileChooser.showOpenDialog(null);
			if (returnValue == CustomJFileChooser.APPROVE_OPTION) 
			{
				final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
				new Thread(new Runnable(){public void run(){try{
					
				siteFixViewComponents.getDedupToValidate().setText(fileChooser.getSelectedFile().getAbsolutePath());
			
				pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,siteFixViewComponents);}}}).start();
		    pwin.setVisible(true);
			}
			

		}

	}
	
	private class ImadaqV02IOVUpdateEvent implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
					 
						//new CiBase_IOV_Update(imadaqv02ViewComponent);
				      new CiBase_IOV_Update_Latest(imadaqv02ViewComponent,imadaqv02ViewComponent.getFileToValidate().getText());
					 pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,imadaqv02ViewComponent);}}}).start();
		    pwin.setVisible(true);
			 
			

		}
	}
	
	/*private class ImadaqV02ExtractEvent implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			
			int	option =JOptionPane.showConfirmDialog(null,"This Process may use high resources of system \n and may also take long time in execution of queries. \n It is always advisable to use local database to extract the details.\n DO you want to continue?");
			
			if(option == JOptionPane.YES_OPTION)
			{
				final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
				new Thread(new Runnable(){public void run(){try{
					
				
				    String dirPath = createSBDir();
				    new ServiceBuildInfo(mlanViewComponents,screenSize,dirPath,false,"","");
				
					pwin.dispose();
				 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,mlanViewComponents);}}}).start();
			    pwin.setVisible(true);
		
		}
		}

	}*/
	
	private class AddAutoAnalysisEvent implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			final CustomJFileChooser fileChooser = new CustomJFileChooser();
			fileChooser.setMultiSelectionEnabled(true);
			
			int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == CustomJFileChooser.APPROVE_OPTION) 
				{
					File[] files = fileChooser.getSelectedFiles();
					for(File F : files)
					{
					MultiLinerPanel.addElement(F.getAbsolutePath());
					}
					MultiLinerPanel.getJframe().setVisible(true);
			}
				else
				{
					/*
					 * JUST SHOW THE EXISITING SELECTIONS
					 */
					MultiLinerPanel.getJframe().setVisible(true);
				}
		
			
		}

	}
	private class AutoAnalysisEvent implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			
				if(MultiLinerPanel.getListOfFile().size()==0)
				{
					JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
					JLabel nameLabel = new JLabel("<HTML><BODY><font color='blue''>No File is selected for execution from Auto Analysis.<br>Please add file(s) from 'Add/Remove Analysis Selection' Button.");
					panel.add(nameLabel);
					ImageIcon icon =  CommonUtils.getIcon(Icons.leafIcon);
					JOptionPane.showMessageDialog(null,panel,"Please Read Carefully.",1,icon);
					return;
				}
		
			
						JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
						JLabel nameLabel = new JLabel("<HTML><BODY>MAX <font color='red' size='5'><b>("+CommonUtils.getMaxRecords()+")</b></font>Records will be analyzed for <font color='blue'>each query</font>.This be change through user configuratrion 'MAX_ROW_RECORDS' property.");
						panel.add(nameLabel);
								
						ImageIcon icon =  CommonUtils.getIcon(Icons.leafIcon);
						JOptionPane.showMessageDialog(null,panel,"Please Read Carefully.",1,icon);
				
						final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
						new Thread(new Runnable(){public void run(){try{
							
						MultiLinerPanel.getJframe().setVisible(true);
						new DetailedAnalysis(imadaqv02ViewComponent,MultiLinerPanel.getListOfFile());
							
						 pwin.dispose();
						 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,imadaqv02ViewComponent);}}}).start();
					    pwin.setVisible(true);
		}

	}
	
	private class ImadaqV02BrowseEvent implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			
			final CustomJFileChooser fileChooser = new CustomJFileChooser();
			int returnValue = fileChooser.showOpenDialog(null);
			if (returnValue == CustomJFileChooser.APPROVE_OPTION) 
			{
				final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
				new Thread(new Runnable(){public void run(){try{
				
					imadaqv02ViewComponent.getFileToValidate().setText(fileChooser.getSelectedFile().getAbsolutePath());
				
					pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,imadaqv02ViewComponent);}}}).start();
		    pwin.setVisible(true);
			
			}
			

		}

	}
	
	
	
	private class D9ValidationEvent implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
				
				
				final CustomJFileChooser fileChooser = new CustomJFileChooser();
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.setFileFilter(fileChooser.getJsonfilter());
				fileChooser.setDialogTitle("Choose Multiple JSON FIles...");
				
				int returnValue = fileChooser.showOpenDialog(new Frame());
				if (returnValue == CustomJFileChooser.APPROVE_OPTION) 
				{
					final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
					new Thread(new Runnable(){public void run(){try{
						
					
					File[] files = fileChooser.getSelectedFiles();
					new EarrachJSON_D9Parser(earrachExtractViewComponents,files,screenSize);
					
					pwin.dispose();
					 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,earrachExtractViewComponents);}}}).start();
				    pwin.setVisible(true);
					
				}
				
				
				
		
		}
	}
	private class DedupEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			menubar.getEventMenu().setEnabled(false);
			toolButtonNature(false);
			menubar.getImadaq().setEnabled(true);
			menubar.getSiteFixed().setEnabled(true);
			menubar.getMLAN().setEnabled(true);
			menubar.getRules().setEnabled(true);
			menubar.getEarrach().setEnabled(true);
			menubar.getQueryExecutor().setEnabled(true);
			menubar.getDedupRun().setEnabled(false);
			menubar.getImadaqv02().setEnabled(true);
		
			dedupView();

		}
	}
	
	
	private class EQL2InstanceMappingAnalysisEvent implements ActionListener 
	{

		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			int	option =JOptionPane.showConfirmDialog(null,"This Process may use high resources of system \n and may also take long time in execution of queries. \n It is always advisable to use local database to extract the details.\n DO you want to continue?");
			
			if(option == JOptionPane.YES_OPTION)
			{
				final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
				new Thread(new Runnable(){public void run(){try{
					
					new EQL2InstanceMappingAnalysis(Directories.EQL2InstanceMappingAnalysisFilesLoc,siteFixViewComponents);
					
					pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,siteFixViewComponents);}}}).start();
		    pwin.setVisible(true);
				
			}
		}
			
	}
	
	private class DedupFileAnalysisEvent implements ActionListener 
	{

		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			
			
				
					String S = "0";
					S = JOptionPane.showInputDialog(new Frame(),"This Feature iterates 2 Active Functality. \n Please ensure Option 2 & 3 must be used after Option 1 Or Final file which is almost ready for execution. \n 1. Previous Dedup Analysyis and \n 2. Create Fix file for Multiple True Active available in dataBase for AM_response True. \n 3. Create Fix file for ISVALIDPRESALES for true sites.\nPlease check which one is Active now. \n OR \n Enter 1 for Previous DEDUP Feature \n Enter 2 for Duplicate Active Address ids.\n Enter 3 for IS VALIDPRESALES FIXES.");
					final int X = Integer.parseInt(S.trim());
					final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
					new Thread(new Runnable(){public void run(){try{
						
						if(X == 1)
						{
						new Previous_DEDUPConsolidation(siteFixViewComponents);
						
						}
						else if (X ==2)
						{
							new CreateMultipleAvtiveFixINPrevDedup(siteFixViewComponents);
						}
						else if (X ==3){
							new CreateIsValidPreSalesFixForPrevDedup(siteFixViewComponents);
						}
				
				pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,siteFixViewComponents);}}}).start();
		    pwin.setVisible(true);
		}
	}
	private class DedupFileBrowseEvent implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			
			final CustomJFileChooser fileChooser = new CustomJFileChooser();
			int returnValue = fileChooser.showOpenDialog(null);
			if (returnValue == CustomJFileChooser.APPROVE_OPTION) {
				
				final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
				new Thread(new Runnable(){public void run(){try{
					
				dedupViewComponents.getFileToValidate().setText(fileChooser.getSelectedFile().getAbsolutePath());
				
				pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,dedupViewComponents);}}}).start();
		    pwin.setVisible(true);
			}
	
		}

	}
	private class DedupFileValidateEvent implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
		
			
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
				
				DedupFileValidator validator = new DedupFileValidator(dedupViewComponents);
				DedupFileDataValidator dataValidator = new DedupFileDataValidator();
	
				JTabbedPane tabbedPane = new JTabbedPane();
				final CustomJFrame tabbedFrame = new CustomJFrame("",Icons.iconPath2);
				if (dedupViewComponents.hasDataValidation()) 
				{
					
					tabbedFrame.addWindowListener(new MyWindowAdapter());
					tabbedFrame.setSize(600, 600);
					tabbedPane = new JTabbedPane();
					tabbedFrame.add(tabbedPane, BorderLayout.CENTER);
				}
				dedupViewComponents.getQueryResult().append("Summary ...\n");
	
				LinkedHashMap<String, ArrayList<String[]>> leadingTrailingErrors = validator.getLeadingTrailingErrors();
				if(leadingTrailingErrors.size() >0)
				{
					dedupViewComponents.getQueryResult().append("Total Validation Type Failed  ... "+ leadingTrailingErrors.size() + "\n");
					Iterator<Map.Entry<String, ArrayList<String[]>>> entries1 = leadingTrailingErrors.entrySet().iterator();
		
					
					while (entries1.hasNext()) 
					{
						Map.Entry<String, ArrayList<String[]>> entry = entries1.next();
						String key = entry.getKey();
						ArrayList<String[]> value = entry.getValue();
						dedupViewComponents.getQueryResult().append("\n\t\t" + key + " (" + value.size() + " )");
						dedupViewComponents.getQueryResult().append("\nGroup ID \t Row Number \t Comments \n");
						for (String[] arr : value) 
						{
							
							for(int x = 0 ; x< arr.length ; x++)
							{
								dedupViewComponents.getQueryResult().append(arr[x] + "\t");
							}
							dedupViewComponents.getQueryResult().append("\n");		
						}
					}
		
					dedupViewComponents.getQueryResult().append("\n Validation Summary Complete......\n\n");
				
				}
				else
				{
					LinkedHashMap<String, ArrayList<String[]>> errors = validator.getErrors();
					dedupViewComponents.getQueryResult().append("Total Validation Type Failed  ... "+ errors.size() + "\n");
					Iterator<Map.Entry<String, ArrayList<String[]>>> entries1 = errors.entrySet().iterator();
		
					
					while (entries1.hasNext()) 
					{
						Map.Entry<String, ArrayList<String[]>> entry = entries1.next();
						String key = entry.getKey();
						ArrayList<String[]> value = entry.getValue();
						dedupViewComponents.getQueryResult().append("\n\t\t" + key + " ( TOTAL CASES : " + value.size() + " )");
					}
		
					dedupViewComponents.getQueryResult().append("\n Validation Summary Complete......\n\n");
				
					Iterator<Map.Entry<String, ArrayList<String[]>>> entries = errors.entrySet().iterator();
					while (entries.hasNext()) 
					{
						Map.Entry<String, ArrayList<String[]>> entry = entries.next();
						String key = entry.getKey();
						ArrayList<String[]> value = entry.getValue();
						dedupViewComponents.getQueryResult().append("\n" + key + " (" + value.size() + " ) \n");
						dedupViewComponents.getQueryResult().append("Group ID \t Row Number \t Comments \n");
						
						int C =25;
						
						if(value.size()>C){
							C=25;
						   //C = value.size();
						}
						
						else
						{
							C = value.size();
						}
						
						for (int x = 0; x < C; x++) 
						{
							String[] S = value.get(x);
							
							
							for (int y = 0; y <S.length; y++) 
							{
								dedupViewComponents.getQueryResult().append(S[y] + "\t");
							}
							dedupViewComponents.getQueryResult().append("\n");
						}
					}
				
					dedupViewComponents.getQueryResult().append("\n Validation Complete.. ");
					
					new CreateReplaceReplacementFileForPrevDedup(dedupViewComponents);
					
					if(errors.size()>0)
					{   pwin.dispose();
						int option = JOptionPane.showConfirmDialog(new Frame(), "File Validation Failure. Would you like to execute SQLs?","Warning",JOptionPane.YES_NO_OPTION);
						if(option == JOptionPane.YES_OPTION)
						{
							showQuerySelector(dedupViewComponents, tabbedPane, dataValidator,validator,errors,tabbedFrame );
						}
						else
						{
							showErrorKeysWindows(errors);
						}
					}
					else
					{
						showQuerySelector(dedupViewComponents, tabbedPane, dataValidator,validator,errors,tabbedFrame );
					}
				}
			pwin.dispose();
		 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,dedupViewComponents);}}}).start();
	    pwin.setVisible(true);
			
		}
	}
	
	
	   private class DedupQuerySelectorCancelEvent implements ActionListener
	   {
		private DedupQueriesWindows dedupQueryWindow;
		private LinkedHashMap<String, ArrayList<String[]>> errors;
		private  DedupQuerySelectorCancelEvent(LinkedHashMap<String, ArrayList<String[]>> errors,DedupQueriesWindows dedupQueryWindow){
	    	this.errors = errors;
			this.dedupQueryWindow = dedupQueryWindow;
	    }
		public void actionPerformed(ActionEvent arg0) {
			
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
				
					dedupQueryWindow.setVisible(false);
					showErrorKeysWindows(errors);
					
				pwin.dispose();
			 } catch ( Exception e){pwin.dispose();}}}).start();
		    pwin.setVisible(true);	
			
			
		}
		
	}
	private class DedupQuerySelectorEvent implements ActionListener{

		private ArrayList<CustomJCheckBox> checkBoxes;
		private DedupFileDataValidator dataValidator;
		private DedupQueriesWindows dedupQueryWindow;
		private DedupViewComponents dedupViewComponents;
		private LinkedHashMap<String, ArrayList<String[]>> errors;
		private CustomJFrame tabbedFrame;
		private JTabbedPane tabbedPane;
		private DedupFileValidator validator;
		
		private DedupQuerySelectorEvent(DedupViewComponents dedupViewComponents, JTabbedPane tabbedPane, DedupFileDataValidator dataValidator,DedupFileValidator validator,ArrayList<CustomJCheckBox> checkBoxes,DedupQueriesWindows dedupQueryWindow,LinkedHashMap<String, ArrayList<String[]>> errors,CustomJFrame tabbedFrame){
	    	
	    	this.dedupViewComponents =dedupViewComponents;
			this.tabbedPane =tabbedPane;
			this.dataValidator =dataValidator;
			this.validator =validator;
			this.checkBoxes = checkBoxes;
			this.dedupQueryWindow = dedupQueryWindow;
			this.errors = errors;
			this.tabbedFrame = tabbedFrame;
		}
		public void actionPerformed(ActionEvent arg0) {
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
					dedupQueryWindow.setVisible(false);
					dedupQueries(dedupViewComponents, tabbedPane, dataValidator,validator,checkBoxes,tabbedFrame);
					showErrorKeysWindows(errors);
			pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,dedupViewComponents);}}}).start();
		    pwin.setVisible(true);
		}
		
	}
	
	
	private class EarrachEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			menubar.getEventMenu().setEnabled(false);
			toolButtonNature(false);
			menubar.getImadaq().setEnabled(true);
			menubar.getSiteFixed().setEnabled(true);
			menubar.getMLAN().setEnabled(true);
			menubar.getDedupRun().setEnabled(true);
			menubar.getRules().setEnabled(true);
			menubar.getQueryExecutor().setEnabled(true);
			menubar.getImadaqv02().setEnabled(true);
			menubar.getEarrach().setEnabled(false);
			
			
			earrachView();
		}
	}

	private class EarrachExtractEvent implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			
			final String quote = earrachExtractViewComponents.getFileToValidate().getText();
			
			try{
				Integer.parseInt(quote.trim());
			}catch(Exception e){
				JOptionPane.showMessageDialog(null,"Quote is not a valid acceptable Offer number.");
				return;
			}
			
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
				
					String quote1 = quote.trim();
					String PATH = Directories.goldEarrachSyncDataFilesLoc+File.separator+quote+File.separator;
					new File(PATH).mkdirs();
					EarrachExtractQueryExecutor reqe = new EarrachExtractQueryExecutor(quote1,earrachExtractViewComponents,PATH,screenSize);
					reqe.getBaseQueryExecution();
					earrachExtractViewComponents.getQueryResult().append("\n File(s) Export successfully ..."+PATH);
				
					pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,earrachExtractViewComponents);}}}).start();
		    pwin.setVisible(true);
		}
		
	}
	private class ExecuteCriteriaEvent implements ActionListener
	{
	
	public void actionPerformed(ActionEvent e) 
	{
				final CustomJFileChooser fileChooser = new CustomJFileChooser();
				fileChooser.setFileFilter(fileChooser.getCriteriafilter());
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == CustomJFileChooser.APPROVE_OPTION) 
				{
					final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
					new Thread(new Runnable(){public void run(){try{
						
					String FN = fileChooser.getSelectedFile().getAbsolutePath();	
					
						if(mlanViewComponents.isDynamicCriteriaExecution()){
							new DynamicCriteriaExecution(mlanViewComponents,FN);
						}
						else{
							new CriteriaExecution(mlanViewComponents,FN);
						}
						
				pwin.dispose();
				 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,mlanViewComponents);}}}).start();
			    pwin.setVisible(true);
				}
				
	}
}
	
	private class FileSegrigationEvent implements ActionListener
	{
		ArrayList<ButtonGroup> buttonGroups;
		ArrayList<CustomJCheckBox> checkBoxes;
		ErrorKeysWindows errorKeysWindows;
		LinkedHashMap<String, ArrayList<String[]>> errors;
		Label process;
		private FileSegrigationEvent(LinkedHashMap<String, ArrayList<String[]>> errors,ArrayList<CustomJCheckBox> checkBoxes,ArrayList<ButtonGroup> buttonGroups,ErrorKeysWindows errorKeysWindows){
			this.errors = errors;
			this.buttonGroups = buttonGroups;
			this.errorKeysWindows = errorKeysWindows;
			this.process = errorKeysWindows.getMessage();
			this.checkBoxes = checkBoxes;
		}
		public void actionPerformed(ActionEvent arg0) {
			
			
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{	
				
					
				for(CustomJCheckBox checkBox :checkBoxes){
					if(!checkBox.isSelected()){
						if(errors.containsKey(checkBox.getText()))
						{
						errors.remove(checkBox.getText());
						}
					}
				}
				dedupViewComponents.getQueryResult().append("\n Please wait .. process start ...");
				String path = dedupViewComponents.getFileToValidate().getText();	
				int index = path.lastIndexOf(".");
				String sub = path.substring(0,index) ;
				String failedPath=sub+"_Failed.csv";
				String passPath=sub+"_Passed.csv";
				
				CSVReader	csvReader = new CSVReader(new FileReader(path));
				
				CustomCSVWriter writerPass = new CustomCSVWriter(new FileWriter(passPath),true);
				CustomCSVWriter writerFailed = new CustomCSVWriter(new FileWriter(failedPath),true);
				String header[] = 	csvReader.readNext();
				
				writerPass.writeNext(header);
				writerFailed.writeNext(header);
				
				ArrayList<String[]> fileData = new ArrayList<String[]>(); 
				ArrayList<String[]> fileDataCopy = new ArrayList<String[]>(); 
				String[] csvRow;
				int counter = 0;
					while ((csvRow = csvReader.readNext()) != null) {
						fileData.add(csvRow);
						fileDataCopy.add(csvRow);
						process.setText("Collecting data from file.."+counter++);
					}
				csvReader.close();
							
				ArrayList<String> failedGIds = new ArrayList<String>();
				Iterator<Map.Entry<String, ArrayList<String[]>>> entries = errors.entrySet().iterator();
				while (entries.hasNext()) {
					Map.Entry<String, ArrayList<String[]>> entry = entries.next();
					ArrayList<String[]> value = entry.getValue();
					for (int x = 0; x < value.size(); x++) 
					{
						String[] S = value.get(x);
					
						for(ButtonGroup buttonGroup:buttonGroups)
						{
						for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) 
						{
				            AbstractButton button = buttons.nextElement();
				            if (button.isSelected() && !button.getText().equalsIgnoreCase("None")) 
				            {
				                if(button.getText().equalsIgnoreCase("GID")){
				                	writeFailedDataForGID(fileData,S[0],failedGIds);
				                	process.setText("Collecting data for failed file..for "+S[0]);
				                }
				                if(button.getText().equalsIgnoreCase("SiteCode")){
				                	writeFailedDataForSitecode(fileData,S[2],failedGIds);
				                	process.setText("Collecting data for passed file..for "+S[2]);
				               
				            	}
				            }
				        }
						}
					}
				}
				
				process.setText("writing files. ");
				writeFiles(failedGIds,fileData,fileDataCopy,writerPass,writerFailed);
				process.setText("writing files success. ");
				dedupViewComponents.getQueryResult().append("\n Failed Data File created....");
				dedupViewComponents.getQueryResult().append("\n Passed Data File created....");
				process.setText("");
				errorKeysWindows.setVisible(false);
				
				pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,dedupViewComponents);}}}).start();
		    pwin.setVisible(true);
				
			
			
		
		}
		
	}
	
	private class FormatEvent implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
		
				
				File2FolderFormat.listf(basePath);
				TreeFormation.setTreeFormation(basePath, sp);
				
				pwin.dispose();
		 } catch ( Exception e){pwin.dispose();}}}).start();
	    pwin.setVisible(true);
			

			

		}
	}
	private class GOLDCSISyncEvent implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			
				int	option = JOptionPane.showConfirmDialog(new Frame(), "Do you really want to start process?\nThis process will take long time in completion and\nwill fetch hudge amount of data.","Warning",JOptionPane.YES_NO_OPTION);
				if(option == JOptionPane.YES_OPTION)
				{
					final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
					new Thread(new Runnable(){public void run(){try{
				
					
					Thread worker = new Thread() 
					{
						GOLD_CSI_SYNC goldcsisync = new GOLD_CSI_SYNC(siteFixViewComponents,Directories.goldCSISyncDataFilesLoc);
						   public void run() 
						   {
							 if(siteFixViewComponents.getGoldSync().isSelected())
							   {
								 SwingUtilities.invokeLater(new Runnable() {public void run() {siteFixViewComponents.getGoldSync().setForeground(Color.red);} });
								 try { 	goldcsisync.getGOLDCoundData(); } catch (Exception e) {	CommonUtils.printExceptionStack(e,siteFixViewComponents);}
								 SwingUtilities.invokeLater(new Runnable() {public void run() {siteFixViewComponents.getGoldSync().setForeground(Color.ORANGE);}});
								 
							   }
							 if(siteFixViewComponents.getCSISync().isSelected())
							   {
								 SwingUtilities.invokeLater(new Runnable() {public void run() {siteFixViewComponents.getCSISync().setForeground(Color.red);} });
								 try { 	goldcsisync.getCSICountData(); } catch (Exception e) {	CommonUtils.printExceptionStack(e,siteFixViewComponents);}
								 SwingUtilities.invokeLater(new Runnable() {public void run() {siteFixViewComponents.getCSISync().setForeground(Color.ORANGE);}});
								 
							   }
							 
							 if(siteFixViewComponents.getwWithAnalysis().isSelected())
							 	{
								 
								 SwingUtilities.invokeLater(new Runnable() {public void run() {siteFixViewComponents.getwWithAnalysis().setForeground(Color.red);} });
								 try 
								 { 	
									
									 String fn = siteFixViewComponents.getFileToValidate().getText();
									 new GOLD_CSI_SYNC_Analysis(siteFixViewComponents,fn); } catch (Exception e) {	CommonUtils.printExceptionStack(e,siteFixViewComponents);}
									 SwingUtilities.invokeLater(new Runnable() {public void run() {siteFixViewComponents.getwWithAnalysis().setForeground(Color.ORANGE);}});
							 	}
							  if( siteFixViewComponents.getWithSEUSIDValidate().isSelected())
							  {
							     SwingUtilities.invokeLater(new Runnable() {public void run() {siteFixViewComponents.getWithSEUSIDValidate().setForeground(Color.red);} });
								 try { 	
									
									 String fn = siteFixViewComponents.getFileToValidate().getText();
									 
									 if(null != fn && !fn.isEmpty() && fn.length() != 0)
									 {
									 	int index = fn.lastIndexOf(".");
										String sub = fn.substring(0,index) ;
										fn = sub+"_USID_VALIDATE_DETAILS.csv";
									 }
									 else{
										 fn = Directories.goldCSISyncDataFilesLoc+"\\USID_VALIDATE_DETAILS.csv";
									 }
										
									 new GOLD_CSI_SYNC_SEUSID_EX_Checks(fn,siteFixViewComponents);
									 siteFixViewComponents.getQueryResult().append("USID_VALIDATION_SUCCESS "+fn);
									 } catch (Exception e) {	CommonUtils.printExceptionStack(e,siteFixViewComponents);}
									 SwingUtilities.invokeLater(new Runnable() {public void run() {siteFixViewComponents.getWithSEUSIDValidate().setForeground(Color.ORANGE);}});
							  }
						   }// end of run
					}; // end of thread
				worker.start();
				pwin.dispose();
					 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,siteFixViewComponents);}}}).start();
				    pwin.setVisible(true);
			}// end of if
				
			
		}
	}
	
       private class HookahBrowseEvent implements ActionListener {

		
		public void actionPerformed(ActionEvent e) {
			
			final CustomJFileChooser fileChooser = new CustomJFileChooser();
			fileChooser.setFileFilter(fileChooser.getCsvfilter());
			int returnValue = fileChooser.showOpenDialog(null);
			if (returnValue == CustomJFileChooser.APPROVE_OPTION) 
			{
				final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
				new Thread(new Runnable(){public void run(){try{
					
				String FN = fileChooser.getSelectedFile().getAbsolutePath();
				mlanViewComponents.getOrderToValidate().setText(FN)	;
		
			pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,mlanViewComponents);}}}).start();
		    pwin.setVisible(true);
			
			} 
		}
		
	}


	private class HookahInfoEvent implements ActionListener {

		
		public void actionPerformed(ActionEvent e) {
			
			
				
			mlanViewComponents.getHasShowSQLCheckBox().setSelected(true);	
			createHookahDir();
			/*@ LALIT 07-JUNE 2018
			 * LIKE OPTION HAS NEEN DISABLED FROM THE UTILITY
			 * THIS ACTION HAS BEEN TAKEN TO OPTIMZE THE QUERY EXECUTUION.
			 * BY UNCOMMENTING BELOW LINE AND LINE MENTIONED IN HookahBuildInfo CLASS QUERY, LIKE WILL BE ACTIVE AGAIN.
			 */
			
			String msg="";
			String msg1="";
			
			if(CommonUtils.isLikeDisabled)
			{
			msg = "Please Provide the Text you want to search.\nThis search will be included with 'IN' (Like Option has been disabled)  in the internal query.\nEX: IN 'Hookah' ";
			msg1="'Hookah'";
			}
			else
			{
			msg="Please Provide the Text you want to search.\nThis search will be included with Like in the internal query.\nEX: Like 'Hookah' ";
			msg1="'Hookah%'";
			}
			final String X = JOptionPane.showInputDialog(msg,msg1);
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{	
				
				new HookahBuildInfo(mlanViewComponents.getOrderToValidate().getText(),mlanViewComponents,X);
				
			pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,mlanViewComponents);}}}).start();
		    pwin.setVisible(true);
		}
		
	}

private class ImadaqEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			menubar.getEventMenu().setEnabled(true);
			menubar.getImadaq().setEnabled(false);
			menubar.getDedupRun().setEnabled(true);
			menubar.getSiteFixed().setEnabled(true);
			menubar.getEarrach().setEnabled(true);
			menubar.getMLAN().setEnabled(true);
			menubar.getQueryExecutor().setEnabled(true);
			toolButtonNature(true);
			imadaqView();

		}
	}

	
	private class JSONFileBrowseEvent implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			
			final CustomJFileChooser fileChooser = new CustomJFileChooser();
		
			fileChooser.setFileFilter(fileChooser.getJsonfilter());
			int returnValue = fileChooser.showOpenDialog(null);
			if (returnValue == CustomJFileChooser.APPROVE_OPTION) 
			{
				final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
				new Thread(new Runnable(){public void run(){try{
			
				earrachExtractViewComponents.getJSONToValidateFile().setText(fileChooser.getSelectedFile().getAbsolutePath());
				
				pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,earrachExtractViewComponents);}}}).start();
		    pwin.setVisible(true);
			}
			

		}

	}
	
	private class JSONValidateEvent implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			
			final String filePath = earrachExtractViewComponents.getJSONToValidateFile().getText();
			if(!CommonUtils.isFileExists(filePath)){
				return;
			}
			  
			    
			final int	option = JOptionPane.showConfirmDialog(new Frame(), "Do you really want to start this process? File may be too long and may failed this process.\nOR Do you want to break this file into few small files? \n\n Press 'YES' to divide this file into multiple small file(s). And 'NO' to continue.","Warning",JOptionPane.YES_NO_OPTION);
			
			if(option == JOptionPane.YES_OPTION)
			{
				
			       
				try {
						new EarrachJSONSeparator(earrachExtractViewComponents.getJSONToValidateFile().getText(),earrachExtractViewComponents);
					} catch (Exception e) {
						CommonUtils.printExceptionStack(e,earrachExtractViewComponents);
					}
					earrachExtractViewComponents.getJSONToValidateFile().setText("");
					return;
					
				 
			}
			else
			{
				final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
				new Thread(new Runnable(){public void run(){try{
				
					new EarrachJSONParser(earrachExtractViewComponents,filePath,screenSize,false);
				 
					pwin.dispose();
				
				} catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,earrachExtractViewComponents);}}}).start();
			    pwin.setVisible(true);
				
			 }
			
			
		}

	}
	
	private class ClosedValidateEvent implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			
			final String filePath = earrachExtractViewComponents.getJSONToValidateFile().getText();
			if(!CommonUtils.isFileExists(filePath)){
				JOptionPane.showMessageDialog(null,filePath+" does not exists.");
				return;
			}
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
		
			
				new EarrachJSONParser(earrachExtractViewComponents,filePath,screenSize,true);
				pwin.dispose();
		 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,earrachExtractViewComponents);}}}).start();
	    pwin.setVisible(true);
			  
			
		}

	}
	
	private class JSONToCSVEvent implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			Thread worker = new Thread() { public void run() {SwingUtilities.invokeLater(new Runnable() {public void run()
			{	
				
				final CustomJFileChooser fileChooser = new CustomJFileChooser();
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.setFileFilter(fileChooser.getJsonfilter());
				fileChooser.setDialogTitle("Choose Multiple JSON FIles...");
				
				int returnValue = fileChooser.showOpenDialog(new Frame());
				if (returnValue == CustomJFileChooser.APPROVE_OPTION) 
				{
					final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
					new Thread(new Runnable(){public void run(){try{
				
						File[] files = fileChooser.getSelectedFiles();
						new JSONToCSVParser(earrachExtractViewComponents,files,screenSize);
						pwin.dispose();
					 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,mlanViewComponents);}}}).start();
				    pwin.setVisible(true);
					
				}
				
				
				
		} });}};
		worker.start();
		}

	}
	
	private class L2ServiceBuildInfoEvent implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
		
				createL2Dir();
			    String quoteNumber = mlanViewComponents.getOrderToValidate().getText();
			    String resultFileLoc = Directories.l2DirLoc+"\\"+quoteNumber+".csv";
				new MLANServiceBuild(quoteNumber,resultFileLoc,mlanViewComponents.getQueryResult(),mlanViewComponents.getOrderTypeT(),true);
			 
			pwin.dispose();
		 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,mlanViewComponents);}}}).start();
	    pwin.setVisible(true);
		}
		
	}
	
	
	private class CiBaseExecutionResultFilterEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
				//CSVImpectedOrderReportReader CR = new CSVImpectedOrderReportReader();
				CiBase_OKNAReader CR = new CiBase_OKNAReader();
				try {
					CR.createLeftFilesData(basePath);
				} catch (IOException e1) {
					infoPanel.setInfoLogs(e1.getMessage());
				}
				TreeFormation.setTreeFormation(basePath, sp);
				Set<String> count = CR.getCountandPath();
				infoPanel.setInfoLogs("Total " + count.size()+ " Files for CiBase Execution OK filer created and Found..");
				for (Iterator<String> it = count.iterator(); it.hasNext(); ) 
				{
			        String f = it.next();
			        infoPanel.setInfoLogs(f);
			    }
				pwin.dispose();
			 } catch ( Exception e){pwin.dispose();}}}).start();
		    pwin.setVisible(true);
			
			
			
		}
	}
	private class LeftFileEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
				CSVImpectedOrderReportReader CR = new CSVImpectedOrderReportReader();
				//CiBaseExecutedOKNAReader CR = new CiBaseExecutedOKNAReader();
				try {
					CR.createLeftFilesData(basePath);
				} catch (IOException e1) {
					infoPanel.setInfoLogs(e1.getMessage());
				}
				TreeFormation.setTreeFormation(basePath, sp);
				Set<String> count = CR.getCountandPath();
				infoPanel.setInfoLogs("Total " + count.size()+ " Files for ICO1 Left Created and Found..");
				
				pwin.dispose();
			 } catch ( Exception e){pwin.dispose();}}}).start();
		    pwin.setVisible(true);
			
			
			
		}
	}


	private class LinkedupSitesAnalysisEvent implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			
			
				if(siteFixViewComponents.getCheckDEDUP().isSelected() && (null == siteFixViewComponents.getDedupToValidate().getText() || siteFixViewComponents.getDedupToValidate().getText().isEmpty() || siteFixViewComponents.getDedupToValidate().getText().length()<1)){
					
					JOptionPane.showMessageDialog(new Frame(),"Please Select Consolidated Previous DEDUP Run file first.");
					return;
				}
				final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
				new Thread(new Runnable(){public void run(){try{
					LinkedupSitesNext linkedupSites = new LinkedupSitesNext(siteFixViewComponents);
					siteFixViewComponents.getQueryResult().append("Linked Up sites Analysis Completed.... \n");
					
					if(siteFixViewComponents.getGenerateSQLs().isSelected())
					{
						siteFixViewComponents.getQueryResult().append("Linked Up sites SQLs generation process initiate...\n");
						siteFixViewComponents.getQueryResult().append("Validating Input file contents ......\n");
						
						LinkedupSiteFixFileValidator validator = new LinkedupSiteFixFileValidator(linkedupSites.getResultFileLoc());
						ArrayList<String> errors = validator.getFileValidateResult();
						
						if(errors.size()==0)
						{
							LinkedupSiteFixDataValidator fileDataValidator = new LinkedupSiteFixDataValidator(validator,siteFixViewComponents);
							
							errors = fileDataValidator.getErrors();
							if(errors.size()==0)
							{
							
								
								String path = siteFixViewComponents.getFileToValidate().getText();	
								int index = path.lastIndexOf(".");
								String sub = path.substring(0,index) ;
								String goldPath=sub+"_GOLD.sql";
								String csiPath=sub+"_CSI.sql";
								String csvPath=sub+"_PreviewData.csv";
								//String logPath=sub+"_CONSOLE_LOGS.xml";
								
								fileDataValidator.prepareQuery();
								fileDataValidator.createGOLDScriptFile(goldPath);
								fileDataValidator.createCSIScriptFile(csiPath);
							
								CommonUtils.createConsoleLogFile(siteFixViewComponents);
								fileDataValidator.showTable(csvPath);
								
							siteFixViewComponents.getQueryResult().append("Linked Up sites SQLs generation process ends successfully...\n");
							}
							else
							{
								siteFixViewComponents.getQueryResult().append("Linked Up sites SQLs generation process found errors...\n");
								for(String err : errors)
								{
									siteFixViewComponents.getQueryResult().append(err+"\n");
								}
							}
						}
						
						else
						{
							for(String err : errors)
							{
								siteFixViewComponents.getQueryResult().append(err+"\n");
							}
							siteFixViewComponents.getQueryResult().append("Validation Failed .. Process failes and stops...\n");
						}
						
						
					}
					
				pwin.dispose();
				 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,siteFixViewComponents);}}}).start();
			    pwin.setVisible(true);
			
		}
	}
	private class LinkedupSitesFirstStepEvent implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
					LinkedupSitesEventClass spl = new LinkedupSitesEventClass(siteFixViewComponents.getFileToValidate().getText(),siteFixViewComponents);
					spl.startSQLs();
					siteFixViewComponents.getQueryResult().append("Completed..\n");
				pwin.dispose();
		 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,siteFixViewComponents);}}}).start();
	    pwin.setVisible(true);
			
		}
	}

	private class MemoryUsagesEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(null == demo)
			{
				int mb = 1024*1024;
				Runtime runtime = Runtime.getRuntime();
				double uppererRange = ((runtime.maxMemory() / mb));
				demo = MemoryUsages.getInstance(uppererRange);
			}
			demo.getFrame().setBounds(screenSize.width-220,screenSize.height*60/100,200,150);
		    demo.getFrame().setVisible(true);
	    }
	}
	
	private class CustomConfigEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				new CustomProperties(Directories.customUserConfigFileLocationV02);
			} catch (IOException e) {
				
				e.printStackTrace();
			}
	    }
	}
	
	private class ELKConfigEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				new ELKCustomProperties(Directories.elkConfigFileLocation);
			} catch (IOException e) {
				
				e.printStackTrace();
			}
	    }
	}
	

	private class PublicPrivateEvent implements ActionListener {
		private String ENV;
		private String filePath;
		public PublicPrivateEvent(String ENV,String filePath){
			this.ENV = ENV;
			
			this.filePath = filePath;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try 
			{
				new PublicPrivateKeyGenerator(ENV,filePath);
			} catch (Exception e) {
			
				e.printStackTrace();
			}
	    }
	}
	private class MergeErrorEventFromImpectedFiles implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
					MergeErrorsDataFromImpectedOrderFile MED = new MergeErrorsDataFromImpectedOrderFile(basePath);
				
					MED.mergeErrors(basePath);
					ArrayList<String> filesExecuted = MED.getReadFilesList();
					infoPanel.setInfoLogs("Reading files to collect Errors data ...");
					for (String filePath : filesExecuted) {
						infoPanel.setInfoLogs(filePath);
					}
					infoPanel.setInfoLogs("Error Collection completed....");
					TreeFormation.setTreeFormation(basePath, sp);
			pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,mlanViewComponents);}}}).start();
		    pwin.setVisible(true);
		}
	}
	
	
	private class MergeErrorEventFromMigratedFiles implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
			
					MergeErrorsDataFromMigratedOrderFile MED = new MergeErrorsDataFromMigratedOrderFile(basePath);
					MED.mergeErrors(basePath);
					ArrayList<String> filesExecuted = MED.getReadFilesList();
					infoPanel.setInfoLogs("Reading files to collect Errors data ...");
					for (String filePath : filesExecuted) {
						infoPanel.setInfoLogs(filePath);
					}
					infoPanel.setInfoLogs("Error Collection completed....");
					TreeFormation.setTreeFormation(basePath, sp);
			pwin.dispose();
			 } catch ( Exception e){pwin.dispose();}}}).start();
		    pwin.setVisible(true);	
			}
	}
	
	private class MergeEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
		
		
				MergeMigratedReport mr = new MergeMigratedReport();
				mr.mergeMigratedReportData(basePath);
				TreeFormation.setTreeFormation(basePath, sp);
				
				pwin.dispose();
		 } catch ( Exception e){pwin.dispose();}}}).start();
	    pwin.setVisible(true);
			
			
		}
	}
	private class MigrationRelocationEvent implements ActionListener 
	{

		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
		
				new MigrationRelocationOrdersFormat(siteFixViewComponents);
				
			pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,siteFixViewComponents);}}}).start();
		    pwin.setVisible(true);
		}
	}
	private class MLANEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			menubar.getEventMenu().setEnabled(false);
			toolButtonNature(false);
			menubar.getImadaq().setEnabled(true);
			menubar.getDedupRun().setEnabled(true);
			menubar.getRules().setEnabled(true);
			menubar.getRules().setEnabled(true);
			menubar.getEarrach().setEnabled(true);
			menubar.getQueryExecutor().setEnabled(true);
			menubar.getImadaqv02().setEnabled(true);
			menubar.getMLAN().setEnabled(false);
			
			mLANView();

		}
	}
	
	private class MLANServiceBuildInfoEvent implements ActionListener {

		
		public void actionPerformed(ActionEvent e) {
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
		
				createDir();
			    String quoteNumber = mlanViewComponents.getOrderToValidate().getText();
			    String resultFileLoc = Directories.mLanDirLoc+"\\"+quoteNumber+".csv";
				new MLANServiceBuild(quoteNumber,resultFileLoc,mlanViewComponents.getQueryResult(),mlanViewComponents.getOrderTypeT());
			
			pwin.dispose();
		 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,mlanViewComponents);}}}).start();
	    pwin.setVisible(true);
			
		}
		
	}
	
private class MyWindowAdapter extends WindowAdapter{
        
    public void windowClosing(WindowEvent e) {
          JOptionPane.showMessageDialog(new CustomJFrame(),"Table Data Export Successfully..");                  
    }
}

	private class OpenWaveEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
				final CustomJFileChooser fileChooser = new CustomJFileChooser();
				int returnValue = fileChooser.showOpenDialog(null);
				fileChooser.setFileSelectionMode(CustomJFileChooser.DIRECTORIES_ONLY);
				
				if (returnValue == CustomJFileChooser.APPROVE_OPTION) 
				{
					final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
					new Thread(new Runnable(){public void run(){try{
					basePath = TreeFormation.setTreeFormation(fileChooser.getCurrentDirectory().getAbsolutePath(), sp);
					pwin.dispose();
					 } catch ( Exception E){pwin.dispose(); CommonUtils.showExceptionStack(E);}}}).start();
				    pwin.setVisible(true);
				}
				
		
		}
	}
	
	


	private class OrderHierarchyEvent implements ActionListener 
	{

		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			
			
		
		        if(mlanViewComponents.getPleaseSelect().isSelected()){
		        	JOptionPane.showMessageDialog(null, "Please select either \n1."+mlanViewComponents.getIncludeSiteChange().getText()+"("+mlanViewComponents.getIncludeSiteChange().getToolTipText()+")\n2."+mlanViewComponents.getExcludeSiteChange().getText()+"("+mlanViewComponents.getExcludeSiteChange().getToolTipText()+")");
		        	return;
		        }
		    final String ORDER  = JOptionPane.showInputDialog(new Frame(),"Please Insert Order Number(s).\nMultiple Orders must be seprated by comma delimited. \ni.e. Order1,Order2,Order3\n\n\nPLEASE NOTE :-\nIT IS ALWAYS ADVISABLE TO INPUT OLDEST ORDER TO GET ALL POSSIBLE HIERARCHY.\n\n\n");    
		    final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
			
				new OrderHierarchy(mlanViewComponents,ORDER);
				mlanViewComponents.getPleaseSelect().setSelected(true);
				pwin.dispose();
			} catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,mlanViewComponents);}}}).start();
	    pwin.setVisible(true);		
			
		}
	}
	
	private class PackageEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
		
					PackageData PD = new PackageData(basePath);
					PD.packageFileData(basePath);
					TreeFormation.setTreeFormation(basePath, sp);
				
					pwin.dispose();
			 } catch ( Exception e){pwin.dispose();}}}).start();
		    pwin.setVisible(true);
			
			

		}
	}
	
	private class PartialCustomerSiteMigrationEvent implements ActionListener {
		
		public void actionPerformed(ActionEvent arg0) 
		{
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
				
				new PartialCustomerSiteMigration(siteFixViewComponents);
				pwin.dispose();
		 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,siteFixViewComponents);}}}).start();
	    pwin.setVisible(true);
		}
		
	}
	
	private class PersonUpdateEvent implements ActionListener 
	{

		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
				new Update_Person(siteFixViewComponents);
				pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,siteFixViewComponents);}}}).start();
		    pwin.setVisible(true);
		}
	}

	private class ProductOrderHierarchyEvent implements ActionListener 
	{

		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
		
				new ProductOrdersHierarchy(siteFixViewComponents);
				pwin.dispose();
		 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,siteFixViewComponents);}}}).start();
	    pwin.setVisible(true);
		}
	}
	private class QueryExecutorEvent implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
		
				
				ArrayList<String[]> result = new ArrayList<String[]>();
				
					String QueryText = (queryExecutorComponents.getQuery().getSelectedText() != null ? queryExecutorComponents.getQuery().getSelectedText():queryExecutorComponents.getQuery().getText() );
					QueryText = ((QueryText.lastIndexOf(';')+1) == QueryText.length() ? QueryText.substring(0,QueryText.length()) : QueryText);
					if(queryExecutorComponents.hasGOLDValidation())
					{
						try{
						result = CommonUtils.getQueryResultWithHeader(QueryText, queryExecutorComponents);
						}catch(Exception e){}
						}
					if(queryExecutorComponents.hasArchivedValidation())
					{
						try{
						result = CommonUtils.getArchiveQueryResultWithHeader(QueryText, queryExecutorComponents);
						}catch(Exception e){}
					}
					if(queryExecutorComponents.hasCSIValidation())
					{
						try{
						result = CommonUtils.getCSIQueryResultWithHeader(QueryText, queryExecutorComponents);
						}catch(Exception e){}
					}
					
					String header[]=result.get(0);
					result.remove(0);
					
					String dirpath = Directories.goldQueryExecutorFilesLoc;
					File F = new File(dirpath);
					F.mkdirs();
					String TabName = CommonUtils.getFileName()+".csv";
					String path =dirpath+File.separator+TabName;
					
					queryExecutorComponents.addTable(header,TabName,result,path);
					queryExecutorComponents.getTabbedFrameVisible();
					queryExecutorComponents.getQueryResult().append("\n File Created "+path);
					
				
				
					pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,queryExecutorComponents);}}}).start();
		    pwin.setVisible(true);
				
		

		}

	}
	
	private class QueryExecutorViewEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			menubar.getEventMenu().setEnabled(false);
			toolButtonNature(false);
			menubar.getImadaq().setEnabled(true);
			menubar.getDedupRun().setEnabled(true);
			menubar.getRules().setEnabled(true);
			menubar.getRules().setEnabled(true);
			menubar.getEarrach().setEnabled(true);
			menubar.getMLAN().setEnabled(true);
			menubar.getImadaqv02().setEnabled(true);
			menubar.getQueryExecutor().setEnabled(false);
			
			QueryExecuterView();

		}
	}
	
	private class RefreshEvent implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
		
				TreeFormation.setTreeFormation(basePath, sp);
				infoPanel.clearLogs();
				
				pwin.dispose();
		 } catch ( Exception e){pwin.dispose();}}}).start();
	    pwin.setVisible(true);

			
		}
	}
	
	private class RulesEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			menubar.getEventMenu().setEnabled(false);
			toolButtonNature(false);
			menubar.getImadaq().setEnabled(true);
			menubar.getSiteFixed().setEnabled(true);
			menubar.getMLAN().setEnabled(true);
			menubar.getDedupRun().setEnabled(true);
			menubar.getEarrach().setEnabled(true);
			menubar.getQueryExecutor().setEnabled(true);
			menubar.getImadaqv02().setEnabled(true);
			menubar.getRules().setEnabled(false);
			rulesView();

		}
	}
	
	private class RulesExtractEvent implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
						
			if(rulesExtractViewComponents.getSelectedIndex()!= 0)
			{
				String TEXT = rulesExtractViewComponents.getFileToValidate().getText();
				if(CommonUtils.isNULL(TEXT))
				{
				 JOptionPane.showMessageDialog(new Frame(), "Please provide Selection Criteria..");
				 return;
				}
			}
				
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
				RulesExtractQueryExecutor reqe = new RulesExtractQueryExecutor(rulesExtractViewComponents);
				new File(Directories.goldCSISyncDataFilesLoc).mkdirs();
				String RFN = Directories.goldCSISyncDataFilesLoc+File.separator+"RULES_"+CommonUtils.getFileName();
				reqe.getBaseQueryExecution(RFN+".csv",RFN+".html");
				rulesExtractViewComponents.getQueryResult().append("\nFile Export successfully ..."+RFN+".CSV");
				pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,rulesExtractViewComponents);}}}).start();
		    pwin.setVisible(true);
			
		}
		
	}
	
	private class ServiceBuildFormatCSVEvent implements ActionListener {

	
	public void actionPerformed(ActionEvent e) {
					String filePath = mlanViewComponents.getFileToValidate().getText();
					File F = new File(filePath);
					if(F.exists())
					{
						 int option = JOptionPane.showConfirmDialog(new Frame(),"Are you sure you want to format selected file?");	
							if(option == JOptionPane.YES_OPTION)
							{
								final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
								new Thread(new Runnable(){public void run(){try{
									new ServiceBuildFileFormater(mlanViewComponents);
									pwin.dispose();
							    } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,mlanViewComponents);}}}).start();
								pwin.setVisible(true);
							}	
					}
					else
					{
					CustomJFileChooser fileChooser = new CustomJFileChooser();
					fileChooser.setFileFilter(fileChooser.getCsvfilter());
					int returnValue = fileChooser.showOpenDialog(null);
					
					if (returnValue == CustomJFileChooser.APPROVE_OPTION) 
					{
						mlanViewComponents.getFileToValidate().setText(fileChooser.getSelectedFile().getAbsolutePath());
					    int option = JOptionPane.showConfirmDialog(new Frame(),"Are you sure you want to format selected file?");	
						if(option == JOptionPane.YES_OPTION)
						{
							final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
							new Thread(new Runnable(){public void run(){try{
								new ServiceBuildFileFormater(mlanViewComponents);
							pwin.dispose();
						    } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,mlanViewComponents);}}}).start();
							pwin.setVisible(true);
						}
					}
					}
				
		
	
	}
}
	
	private class ServiceBuildInfoByOrdersEvent implements ActionListener {
	
			
			public void actionPerformed(ActionEvent e) 
			{
				/*@ LALIT 07-JUNE 2018
				 * LIKE OPTION HAS NEEN DISABLED FROM THE UTILITY
				 * THIS ACTION HAS BEEN TAKEN TO OPTIMZE THE QUERY EXECUTUION.
				 * BY UNCOMMENTING BELOW LINE , LIKE WILL BE ACTIVE AGAIN.
				 */
				
				String msg="";
				String msg1="";
				
				if(CommonUtils.isLikeDisabled)
				{
				msg = "Please Provide the Text you want to search.\nThis search will be included with IN (Like option has been disabled) the internal query.\nEX: Like>> AND LINEITEM.DESCRIPTION IN ('CPE USID','RouterName','Router Name')";
				msg1="AND LINEITEM.DESCRIPTION IN ('CPE USID','RouterName','Router Name')";
				}
				else
				{
				msg="Please Provide the Text you want to search.\nThis search will be included with Like in the internal query.\nEX: Like AND (LINEITEM.DESCRIPTION LIKE ('%USID') OR LINEITEM.DESCRIPTION IN ('RouterName','Router Name'))";
				msg1="AND (LINEITEM.DESCRIPTION LIKE ('%USID') OR LINEITEM.DESCRIPTION IN ('RouterName','Router Name'))";
				}
				
				final String X = JOptionPane.showInputDialog(msg,msg1);	
				
				final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
					new Thread(new Runnable(){public void run(){try{
					new ServiceBuildInfoByOrders(mlanViewComponents.getOrderToValidate().getText(),mlanViewComponents,X);
					pwin.dispose();
				 
				} catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,mlanViewComponents);}}}).start();
			    pwin.setVisible(true);	
				
			}
			
		}
	
	private class ServiceBuildInfoEvent implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			int	option =JOptionPane.showConfirmDialog(null,"This Process may use high resources of system \n and may also take long time in execution of queries. \n It is always advisable to use local database to extract the details.\n DO you want to continue?");
			
			if(option == JOptionPane.YES_OPTION)
			{
			
				final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
				new Thread(new Runnable(){public void run(){try{
			
					String dirPath = createSBDir();
					new ServiceBuildInfo(mlanViewComponents,screenSize,dirPath,false,"","");
					pwin.dispose();
				 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,mlanViewComponents);}}}).start();
			    pwin.setVisible(true);
				
		}
		}
	}
	
	private class USIDInfoEvent implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			int	option =JOptionPane.showConfirmDialog(null,"This Process may use high resources of system \n and may also take long time in execution of queries. \n It is always advisable to use local database to extract the details.\n DO you want to continue?");
			
			if(option == JOptionPane.YES_OPTION)
			{
				
					
				final String dirPath = createSBDir();
				
				String[] list = {"QUOTE.QUOTENUMBER","SITE.SITECODE", "SITE.ADDRESS_ID", "SITE.CORE_SITE_ID"};
				final JComboBox<String> jcb = new JComboBox<String>(list);
				jcb.setEditable(false);
				
				JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
				JLabel nameLabel = new JLabel("Multiple values must be seprated by comma delimited.");
				panel.add(jcb);
				panel.add(nameLabel);
				
				final String params = JOptionPane.showInputDialog(null,panel,"Please select...... ",JOptionPane.INFORMATION_MESSAGE);
				final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
				new Thread(new Runnable(){public void run(){try{
					
		   		new ServiceBuildInfo(mlanViewComponents,screenSize,dirPath,true,params,jcb.getSelectedItem().toString());
		   		
		   		pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,mlanViewComponents);}}}).start();
		    pwin.setVisible(true);
				
			
		
		}
		}
	}
	
	private class DeliverSRF2Event implements ActionListener {
		public void actionPerformed(ActionEvent e) 
		{
			
			
	            	int	option =JOptionPane.showConfirmDialog(null,"This Process will check and provide the fixes for those orders.\nwhere one Milestone is 'Capture Technical Details' and Action Taken is ‘Complete Task (SRF2 not required for this order)' and\n another Milestone is 'Deliver SRF2' but status is not closed for this milestone.\n Do you want to continue?\nPLEASE NOTE>> (NO ARCHIVED ORDERS INCLUDED)");
	    			
				    			if(option == JOptionPane.YES_OPTION)
				    			{
				    				
				    				final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
				    				new Thread(new Runnable(){public void run(){try{
				    			
				    		            	String dirPath = createSRF2Dir();
				    				    	new DeliverSRF2Info(mlanViewComponents,dirPath);
				    		            	pwin.dispose();
				    				
				    		            } catch (Exception th ){pwin.dispose();CommonUtils.printExceptionStack(th,mlanViewComponents);}}}).start();
				    				    pwin.setVisible(true);
				    			
				    		
				    		}
	          
			
			
			
			
		}
	}
	
	private class ICOWithChildEvent implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
				
						String dirPath = createICOPARDir();
						new ICOWithChild(mlanViewComponents,dirPath);
				
			pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,mlanViewComponents);}}}).start();
		    pwin.setVisible(true);
		
		
		}
	}
	
	
	private class SiteFixBrowseEvent implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			
			final CustomJFileChooser fileChooser = new CustomJFileChooser();
			int returnValue = fileChooser.showOpenDialog(null);
			if (returnValue == CustomJFileChooser.APPROVE_OPTION) 
			{
				final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
				new Thread(new Runnable(){public void run(){try{
				
					siteFixViewComponents.getFileToValidate().setText(fileChooser.getSelectedFile().getAbsolutePath());
				
				pwin.dispose();
				 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,siteFixViewComponents);}}}).start();
			    pwin.setVisible(true);
			}
		

		}

	}
	private class SiteFixedEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			menubar.getEventMenu().setEnabled(false);
			toolButtonNature(false);
			menubar.getImadaq().setEnabled(true);
			menubar.getDedupRun().setEnabled(true);
			menubar.getMLAN().setEnabled(true);
			menubar.getRules().setEnabled(true);
			menubar.getEarrach().setEnabled(true);
			menubar.getQueryExecutor().setEnabled(true);
			menubar.getImadaqv02().setEnabled(true);
			menubar.getSiteFixed().setEnabled(false);
			siteFixedView();

		}
	}
		
	private class SiteFixValidateEvent implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
				
			
					SiteFixFileValidator fileValidator = new SiteFixFileValidator(siteFixViewComponents.getFileToValidate().getText());
					ArrayList<String> errors;
					
					errors = fileValidator.getFileValidateResult();
					siteFixViewComponents.getQueryResult().append("Start Validator...\n");
					for(String ERR : errors)
					{
						siteFixViewComponents.getQueryResult().append(ERR +"\n");
					}
					int option  = 6;
					if(errors.size()>0){
						option = JOptionPane.showConfirmDialog(new Frame(), "File Validation Failure. Would you like to continue.?","Warning",JOptionPane.YES_NO_OPTION);
					}
					if(option == JOptionPane.NO_OPTION){
						siteFixViewComponents.getQueryResult().append("File validation trminated...\n");
						return;
					}
					siteFixViewComponents.getQueryResult().append("File Format validation completed...\n");
					SiteFixDataValidator dataValidator = new SiteFixDataValidator(fileValidator,siteFixViewComponents);
					siteFixViewComponents.getQueryResult().append("Start Execution...\n");
					dataValidator.prepareQuery();
					siteFixViewComponents.getQueryResult().append("Execution Completed...\n");
					siteFixViewComponents.getQueryResult().append("Start File Data Validator...\n");
					
					for(String ERR : dataValidator.getErrors())
					{
						siteFixViewComponents.getQueryResult().append(ERR +"\n");
					}
					
					
					if(errors.size()>0){
						option = JOptionPane.showConfirmDialog(new Frame(), "File Data Validation Failure. Would you like to continue.?","Warning",JOptionPane.YES_NO_OPTION);
					}
					if(option == JOptionPane.NO_OPTION){
						siteFixViewComponents.getQueryResult().append("File Data validation trminated...\n");
						return;
					}
					
					siteFixViewComponents.getQueryResult().append("File Data validation completed...\n");
					
					String path = siteFixViewComponents.getFileToValidate().getText();	
					int index = path.lastIndexOf(".");
					String sub = path.substring(0,index) ;
					String goldPath=sub+"_GOLD.sql";
					String csiPath=sub+"_CSI.sql";
					String csvPath=sub+"_PreviewData.csv";
					//String logPath=sub+"_CONSOLE_LOGS.xml";
					
					dataValidator.showTable(csvPath);
					CommonUtils.createConsoleLogFile(siteFixViewComponents);
					dataValidator.createGOLDScriptFile(goldPath);
					dataValidator.createCSIScriptFile(csiPath);
					dataValidator.getGoldExecutionScript();
					siteFixViewComponents.getQueryResult().append("File(s) created successfully...\n");
					
			pwin.dispose();
			 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,mlanViewComponents);}}}).start();
		    pwin.setVisible(true);
		}
		
	}

	
	
	private class Imadaqv02Event implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			menubar.getEventMenu().setEnabled(true);
			toolButtonNature(false);
			menubar.getImadaq().setEnabled(true);
			menubar.getDedupRun().setEnabled(true);
			menubar.getMLAN().setEnabled(true);
			menubar.getSiteFixed().setEnabled(true);
			menubar.getRules().setEnabled(true);
			menubar.getEarrach().setEnabled(true);
			menubar.getQueryExecutor().setEnabled(true);
			
			menubar.getImadaqv02().setEnabled(false);
			ImadaqV02View();

		}
	}
	
	private class SplChrsFixValidateEvent implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
				
				SiteCodeswithSpecialChars spl = new SiteCodeswithSpecialChars(Directories.goldSPLCHRSiteCodesFilesLoc,siteFixViewComponents);
				spl.startSQLs();
			
		 pwin.dispose();
		 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,mlanViewComponents);}}}).start();
	    pwin.setVisible(true);
		}
	}
	
	private class ValidateEvent implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
			new Thread(new Runnable(){public void run(){try{
					ValidateReport vr = new ValidateReport();
					try {
						vr.validateReports(basePath);
					} catch (IOException e1) {
						
						JOptionPane.showMessageDialog(null,e1);
					}
					LinkedHashSet<LinkedHashSet<String>> uncommonParrent = vr
							.getUncommonParrent();
					if (uncommonParrent.size() > 0) {
						infoPanel.setInfoLogs("Validation Failed .... ");
						Iterator<LinkedHashSet<String>> itr = uncommonParrent
								.iterator();
						while (itr.hasNext()) {
							LinkedHashSet<String> uncommon = itr.next();
							Iterator<String> uncommonitr = uncommon.iterator();
							while (uncommonitr.hasNext()) {
								String S = uncommonitr.next();
								infoPanel.setInfoLogs(S);
							}
	
						}
					} else {
						infoPanel.setInfoLogs("Validation Sucess .... ");
				
					}
					
			
			pwin.dispose();
			 } catch ( Exception e){pwin.dispose();}}}).start();
		    pwin.setVisible(true);

			
		}
	}

	private class ViewJSONEvent implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			
				final String jsonFile = earrachExtractViewComponents.getJSONToValidateFile().getText();
				if(!CommonUtils.isFileExists(jsonFile)){
					return;
				}
				final Window pwin = CommonUtils.getProgressBar(100,"Please wait ..",true); 
				new Thread(new Runnable(){public void run(){try{
						 
					new XML2JTree(jsonFile);
					
				 pwin.dispose();
				 } catch ( Exception e){pwin.dispose();CommonUtils.printExceptionStack(e,earrachExtractViewComponents);}}}).start();
			    pwin.setVisible(true);
		}

		}
	
	private class JSONCheckEvent implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			Thread worker = new Thread() { public void run() {SwingUtilities.invokeLater(new Runnable() {public void run()
			{
				String jsonFile = earrachExtractViewComponents.getJSONToValidateFile().getText();
				if(!CommonUtils.isFileExists(jsonFile)){
					JOptionPane.showMessageDialog(null, "File "+jsonFile+" does not exist.");
					return;
				}
					try {
						boolean isValidJSON = CommonUtils.isJSONValid(jsonFile,earrachExtractViewComponents);
					     if(isValidJSON)
					     {
					    	 JOptionPane.showMessageDialog(null, "File "+jsonFile+" is valid JSON file.");	 
					     }
					} catch (Exception e) {
						CommonUtils.printExceptionStack(e,earrachExtractViewComponents);
						
					
				}
		} });}};
		worker.start();
		}

		}
	
	private void autoUpdateVesrion(Window pwin) throws Exception
	{
	
		
		String ftpUserName 			= FTPSettings.getFtpUser();
		String ftpUserPassword 		= FTPSettings.getFtpPassword();
		String ftpURL 					= FTPSettings.getFtpURL();
		String ftpFileName 			= FTPSettings.getFtpFileName();
		String ftpLatestFileName	= FTPSettings.getFtpLatestFileName();
		String ftpFileLocationName = FTPSettings.getFtpFileLocation();
		
		FTPDownloader ftpDownloader = new FTPDownloader(ftpURL, ftpUserName, ftpUserPassword);
        ftpDownloader.downloadFile(ftpFileLocationName+ftpFileName, ftpLatestFileName);
        //ftpDownloader.downloadDir(ftpFileLocationName+ftpDIRName, ftpDIRName);
     
        ftpDownloader.disconnect();
        Thread.sleep(1000);
         pwin.dispose();
        Thread.sleep(1000);
         /*
        JOptionPane opt = new JOptionPane("<html><font color=orange size=3>Updated version has been successfully download as '"+ftpLatestFileName+"'.<br>Please don't close any process forcible way .. , Utility will restart with Updated version automatically.<br><font color=blue size=3>Will close automatically in 10 seconds.", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}); // no buttons
        opt.setIcon(CommonUtils.getIcon(Icons.iconPath8));
        final JDialog dlg = opt.createDialog("Information");
		new Thread(new Runnable()
        {
          public void run()
          {
            try
            {
              Thread.sleep(10000);
              dlg.dispose();
            }
            catch ( Throwable th )
            {
            }
          }
        }).start();
		dlg.setAlwaysOnTop(true);
		dlg.setVisible(true);
		*/
        String path="cmd /c start "+writeAutoInstallerBat();
        Runtime rn=Runtime.getRuntime();
        rn.exec(path);
       
	}
	private boolean checkUserAuthentication(Properties properties,String which) throws Exception{
		for (Object o : properties.keySet()) 
		{
		String key = 	(String) o;
		String[] values			 = properties.get(key).toString().split(PublicKeyGenerator.encrypt(PublicPrivateKeyGenerator.seperator));;
		
		if(values.length<7)
		{
			JOptionPane.showMessageDialog(new CustomJFrame(), which+" Version Mismatch. Please ensure you are using Latest Version of the Utility and Configurations Setting Files.\nPlease contact to administrator immediatily.");
			return false;
		}
		else
		{
			String FTID 				 = PublicKeyGenerator.decrypt(values[6]);
			if(!Directories.UserName.equalsIgnoreCase(FTID))
			{
			JOptionPane.showMessageDialog(new CustomJFrame(), which+" Data Base Configuration is not valid/for you.You are accessing unauthorized File.\nPlease contact to administrator immediatily.");
			return false;
			}
		}
	    }
		return true;
	}
	private void loadDataBaseProp() throws Exception
	{
	
		String GOLDFile 					= FTPSettings.getGOLD();
		String CSIFile 						= FTPSettings.getCSI();
		String ArchiveFile 					= FTPSettings.getArchival();
		String DBftpFileLocation			= FTPSettings.getDBftpFileLocation();
		String ftpUserName 				= FTPSettings.getFtpUser();
		String ftpUserPassword 			= FTPSettings.getFtpPassword();
		String ftpURL 						= FTPSettings.getFtpURL();
		
	
		GOLDFile								=DBftpFileLocation+Directories.UserName+GOLDFile;
		CSIFile									=DBftpFileLocation+Directories.UserName+CSIFile;
		ArchiveFile							=DBftpFileLocation+Directories.UserName+ArchiveFile;
		
		FTPClient ftpClient = new FTPDownloader(ftpURL, ftpUserName, ftpUserPassword).getFTPClient();
		InputStream goldInputFile = ftpClient.retrieveFileStream(GOLDFile);
		
		ftpClient = new FTPDownloader(ftpURL, ftpUserName, ftpUserPassword).getFTPClient();
		InputStream csiInputFile = ftpClient.retrieveFileStream(CSIFile);
		
		ftpClient = new FTPDownloader(ftpURL, ftpUserName, ftpUserPassword).getFTPClient();
		InputStream archivalInputFile = ftpClient.retrieveFileStream(ArchiveFile);
		
		
		
		if(null != goldInputFile)
		{
			Properties goldDBProperties = new Properties();
			goldDBProperties.load(goldInputFile);
			if(checkUserAuthentication(goldDBProperties,"GOLD"))
			{
				CommonUtils.setGoldDBProperties(goldDBProperties);
			}
		}
		else
		{
			Properties goldDBProperties = new Properties();
			CommonUtils.setGoldDBProperties(goldDBProperties);
			JOptionPane.showMessageDialog(new CustomJFrame(), "GOLD Data Base Configuration not availabe.\nUtility will be running on Safe Mode.\nYou will be not authorizing to access GOLD Database through this Utility.\nPlease contact to administrator.");
		}
		
		if(null != csiInputFile)
		{
			Properties csiDBProperties = new Properties();
			csiDBProperties.load(csiInputFile);
			if(checkUserAuthentication(csiDBProperties,"CSI"))
			{
				CommonUtils.setCsiDBProperties(csiDBProperties);
			}
		}
		else
		{
			Properties csiDBProperties = new Properties();
			CommonUtils.setCsiDBProperties(csiDBProperties);
			JOptionPane.showMessageDialog(new CustomJFrame(), "CSI Data Base Configuration not availabe.\nUtility will be running on Safe Mode.\nYou will be not authorizing to access CSI Database through this Utility.\nPlease contact to administrator.");
		}
		
		if(null != archivalInputFile)
		{
			
			Properties archivalDBProperties = new Properties();
			archivalDBProperties.load(archivalInputFile);
			if(checkUserAuthentication(archivalDBProperties,"Archival"))
			{
				CommonUtils.setArchivalDBProperties(archivalDBProperties);
			}
		}
		else
		{
			Properties archivalDBProperties = new Properties();
			CommonUtils.setArchivalDBProperties(archivalDBProperties);
			JOptionPane.showMessageDialog(new CustomJFrame(), "Archival Data Base Configuration not availabe.\nUtility will be running on Safe Mode.\nYou will be not authorizing to access Archival Database through this Utility.\nPlease contact to administrator.");
		}
		
	}
	
	private void uploadLogs() throws Exception{
		
		String ftpUserName 				= FTPSettings.getFtpUser();
		String ftpUserPassword 			= FTPSettings.getFtpPassword();
		String ftpURL 							= FTPSettings.getFtpURL();
		
		File F = new File(Directories.customUserLOGSFile);
		FileInputStream fstream = new FileInputStream(F);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		StringBuilder data= new StringBuilder();
		while ((strLine = br.readLine()) != null)   
		{
			data.append(strLine+"\n");
		}
		br.close();
		fstream.close();
		
		if(null != data && data.length()>1)
		{
			ByteArrayInputStream local = new ByteArrayInputStream(data.toString().getBytes());
			FTPClient ftpClientLoader = new FTPDownloader(ftpURL, ftpUserName, ftpUserPassword).getFTPClient();
			ftpClientLoader.appendFile(FTPSettings.getDBftpFileLocation()+"appLogs.txt",local);
			local.close();
			F.delete();
		}
		
	}
	public void callMain()
	{
	
		JWindow win = loader();
	 try {
		   try{
		   Properties props = new Properties();
           props.put("logoString", "GOLD"); 
           com.jtattoo.plaf.mint.MintLookAndFeel.setCurrentTheme(props);
           Properties properties = CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV02);
		   UIManager.setLookAndFeel(properties.getProperty("lookNFeel"));
		   
		   }catch(Exception e)
		   {
			   System.out.println(">>>>>>>>>>> Unable to Load Look and Feel..");
		   }
		   
		   String isUpdateAvailable = checkVersion();
		   new Main(true,isUpdateAvailable);
		   win.setVisible(false);
	} catch (Exception E) 
	{
		CommonUtils.showExceptionStack(E);
		win.setVisible(false);
	}
  }
	
	
	
	public void callMainWithDynamicCriteraExecution(String filePath,String goldKey,String archivalKey,String csiKey){
		Main M = new Main(false,"");
		 try {
			   String FN = filePath;
			   M.loadDBConfigProp(goldKey,archivalKey,csiKey);
			   System.out.println("Please wait Process is Running...");
			   new DynamicCriteriaExecution(M.mlanViewComponents,FN);
			   System.out.println("Please Completed successfully...\n Please note the extraction file path and \n close the utilty to resume the resources...");
				
		} catch (Exception e) {
			e.printStackTrace();
		    System.out.println(e);
		}
		
	}
	
	private  String checkVersion()
	{
		String isUpdate = "NA";
		try {
			String ftpUserName 				= FTPSettings.getFtpUser();
			String ftpUserPassword 			= FTPSettings.getFtpPassword();
			String ftpURL 						= FTPSettings.getFtpURL();
			String ftpMsgFileName  			= FTPSettings.getFtpMsgFileName();
			String ftpFileLocationName 	= FTPSettings.getFtpFileLocation();
		
			FTPDownloader ftpDownloader = new FTPDownloader(ftpURL, ftpUserName, ftpUserPassword);
            ftpDownloader.downloadFile(ftpFileLocationName+ftpMsgFileName, ftpMsgFileName);
         
            ftpDownloader.disconnect();
            
            BufferedReader br = new BufferedReader(new FileReader(ftpMsgFileName));
            String sCurrentLine;
            
            String version="";
            String instruction="";
            String messages="";
            
            int count = 0;
			while ((sCurrentLine = br.readLine()) != null) 
			{
				if(count == 0)
				{
					version = sCurrentLine;
				}
				else if(count == 1)
				{
					instruction = (null == sCurrentLine && sCurrentLine.length()==0 && sCurrentLine.isEmpty() ? "all,well":sCurrentLine);
				}
				else
				{
					messages+=sCurrentLine+"\n";
				}
				count++;
			}
			br.close();
			
			String instructionsArray[] = instruction.split(",");
			ArrayList<String> instructions = new ArrayList<String>(Arrays.asList(instructionsArray));
			String blockForYouText="HOLD_"+Directories.UserName;
			
				if(instructions.contains("HOLD_ALL"))
				{
			          JOptionPane.showMessageDialog(new Frame(), "Utility is on HOLD, and can not be use untill further notification.\nPlease contact to administrator.");
			          System.exit(0);
				}
				else if(instructions.contains(blockForYouText))
				{
					 JOptionPane.showMessageDialog(new Frame(), "Utility is on HOLD for "+Directories.UserName+", and can not be use untill further notification.\nPlease contact to administrator.");
			         System.exit(0);
				}
				
				else
				{
				  if(instructions.contains("META"))
					{
						isUpdate = "META";
					}
					else
					{
						isUpdate = "PATCH";
					}
				}
			
			Float availableVersion = new Float(version);
			Float installedVersion = new Float(CommonUtils.version);
		
		    if(installedVersion < availableVersion)
            {
            final String MSG = messages;
            Properties properties1 = CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV02);
			if(properties1.getProperty("VersionUpdateAlert").equalsIgnoreCase("YES"))
			{
	            final JOptionPane opt = new JOptionPane(MSG+"<html><br><font color=red size=3>Will close automatically in 30 seconds.", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}); // no buttons
	            opt.setIcon(CommonUtils.getIcon(Icons.iconPath8));
	            
	            final JDialog dlg = opt.createDialog("Information");
	    		
	            new Thread(new Runnable()
	            {
	              public void run()
	              {
	                try
	                {
	                  Thread.sleep(30000);
	                  dlg.dispose();
	                }
	                catch ( Throwable th )
	                {
	                }
	              }
	            }).start();
	    		dlg.setAlwaysOnTop(true);
	    		dlg.setVisible(true);
			}
            }
		    else
		    {
		    	 isUpdate = "NO_UPDATE_REQ";
		    }
        } catch (Exception e) {
        	JOptionPane opt = new JOptionPane("<html><font color=BLUE size=4><br>System is unable to load details from FTP.<br> Please contact to system administrator.</font><BR><BR><font color=red size=2>Will close automatically in 5 seconds.", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}); // no buttons
            opt.setIcon(CommonUtils.getIcon(Icons.iconPath8));
            final JDialog dlg = opt.createDialog("Information");
    		new Thread(new Runnable()
            {
              public void run()
              {
                try
                {
                  Thread.sleep(5000);
                  dlg.dispose();
                }
                catch ( Throwable th )
                {
                }
              }
            }).start();
    		dlg.setAlwaysOnTop(true);
    		dlg.setVisible(true);
           
        }
        return isUpdate;
	}
	
	private  JWindow loader(){
		String loderIcon = Icons.loadingIcon;
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		JWindow loader = new JWindow();
		loader.setSize((int)dimension.getWidth()*40/100,(int)dimension.getHeight()*40/100);
		loader.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width  - loader.getSize().width) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - loader.getSize().height) / 2);
		JLabel icon = new JLabel();
		icon.setBackground(Color.white);
		icon.setOpaque(true);
		icon.setIcon(new ImageIcon(CommonUtils.setSizeImage(loderIcon, (int)dimension.getWidth()*40/100, (int)dimension.getHeight()*40/100)));
		loader.add(icon);
		loader.setVisible(true);
		return loader;
	}
	

	public static void main(String args[]){
		Main M = new Main();
		if(null == args || args.length==0)
		{
				try
				{
				 M.writeLogProp();
				 CommonUtils.writeCustomConfigProp(false);	
				 CommonUtils.writeELKConfigProp(false);	
				 M.cleanFTPConfigProp();
				 }catch(Exception e){CommonUtils.showExceptionStack(e);}
				 try
				 {
					System.out.println(" UPDATING lOGS DETAILS ON REMOTE SERVER...."); 
					M.uploadLogs();
					System.out.println("LOGS DETAILS LOADS SUCCESSFULLY...."); 
				  	M.loadDataBaseProp();
			 	  }catch(Exception e){CommonUtils.showExceptionStack(e);}
				
			M.callMain();
		}
		else
		{
			String FN 					= args[0];
			String goldKey 			= args[1];
			String archivalKey 		= args[2];
			String csiKey 				= args[3];
			
			System.out.println("Reading Param I 'st   as Criteria file                     "+FN);
			System.out.println("Reading Param II 'nd  as GOLD Connection             "+goldKey);
			System.out.println("Reading Param III 'rd as GOLD archival Connection "+archivalKey);
			System.out.println("Reading Param IV 'th as GOLD CSI Connection        "+csiKey);
		
			try
			 {
			  	M.loadDataBaseProp();
		 	  }catch(Exception e){CommonUtils.showExceptionStack(e);}
			
			M.callMainWithDynamicCriteraExecution(FN,goldKey,archivalKey,csiKey);
			System.out.println("Process Completed...");
				
		}
		
	}
	
	private  String writeAutoInstallerBat() throws IOException
	{
	
		
		String ftpFileName 				= FTPSettings.getFtpFileName();
		String ftpLatestFileName 		= FTPSettings.getFtpLatestFileName();
	
		
		String AutoInstallContent = "@ECHO OFF \n"+
		"Taskkill /f /im java.exe  \n"+
		"del "+ftpFileName+" \n"+
		"rename "+ftpLatestFileName+" "+ftpFileName+" \n"+
		"del "+ftpLatestFileName+" \n"+
		"call Imadaq.bat \n"+
		"del AutoInstall.bat";
	
		FileWriter fw = new FileWriter("AutoInstall.bat");
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(AutoInstallContent);
		bw.close();
		fw.close();
		
		File F = new File("AutoInstall.bat");
		return F.getAbsolutePath();
		
		
	}
	/*
	 * 
	 * THIS FILE ALWAYS BE UPDATED, AS USER IS NOT ALLOW TO MAKE ANY CHANGES IN THIS FILE.
	 */
	public void writeLogProp() throws IOException
	{
		Properties properties = new Properties();
		properties.setProperty("log4j.rootLogger", "DEBUG, Appender");
		properties.setProperty("log4j.appender.Appender","org.apache.log4j.FileAppender");
		properties.setProperty("log4j.appender.Appender.File",Directories.customUserLOGSFile);
		properties.setProperty("log4j.appender.Appender.layout","org.apache.log4j.PatternLayout");
		properties.setProperty("log4j.appender.Appender.layout.ConversionPattern","%-7p %d [%t] %c %x - %m%n");
		
		File file = new File(Directories.customUserLOGS);
		
		FileOutputStream fileOut = new FileOutputStream(file);
		properties.store(fileOut, "Utility logs -- Please do'nt make any changes in this file as these text are auto generated and will overwrite.");
		fileOut.close();
		
				
	}
	
	
	
	public  void cleanFTPConfigProp() throws Exception
	{
		File fileOLD1 = new File(Directories.defaultFTPConfigLocationOLD);
		if(!fileOLD1.exists())
		{
			fileOLD1.delete();
			fileOLD1= null;
		}
		
		File file = new File(Directories.defaultFTPConfigLocation);
		if(file.exists())
		{
			file.delete();
		}
	}
	
	private String basePath;
	private Map<String,ArrayList<String[]>> csiOrderMap = new HashMap<String,ArrayList<String[]>>();
	
	private DedupViewComponents dedupViewComponents;
	private MemoryUsages demo ;
	private EarrachExtractionView earrachExtractViewComponents;
	
	private CustomJFrame frame;
	
	Map<String,ArrayList<String[]>> goldOrderMap = new HashMap<String,ArrayList<String[]>>();

	private SetInfoPanel infoPanel;
	private MyMenuBar menubar;
	
	private MLANViewComponents mlanViewComponents;
	
	private QueryExecutorComponent queryExecutorComponents;
	
	private RulesExtractView rulesExtractViewComponents;
	private Imadaqv02ViewComponent imadaqv02ViewComponent;
	
	
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private int screenHeight =screenSize.height*95/100;
	
	private SiteFixedViewComponents siteFixViewComponents;
	private SplitPanel sp;
	private ToolBar toolbar ;

	public Main(boolean isFrameVisible,String isUpdateAvailable) {
		siteFixViewComponents = new SiteFixedViewComponents(screenSize);
		queryExecutorComponents = new QueryExecutorComponent(screenSize);
		mlanViewComponents = new MLANViewComponents(screenSize);
		rulesExtractViewComponents = new RulesExtractView(screenSize);
		imadaqv02ViewComponent = new Imadaqv02ViewComponent(screenSize);
		earrachExtractViewComponents = new EarrachExtractionView(screenSize);
		dedupViewComponents = new DedupViewComponents(screenSize);
		sp = new SplitPanel();
		sp.getImadaqVsplitPane().setVisible(false);
		sp.getImadaqHsplitPane().setVisible(false);
		sp.getDedupVsplitPane().setVisible(false);
		sp.getSiteFixedVsplitPane().setVisible(false);
		sp.getSiteMigrationVsplitPane().setVisible(false);
		sp.getQueryExecutorVsplitPane().setVisible(false);
		sp.getImadaqV02VsplitPane().setVisible(false);
		frame = sp.getFrame(); 
		toolbar = new ToolBar();
		menubar = new MyMenuBar(frame);
		infoPanel = new SetInfoPanel(sp);

		

		menubar.getAutoUpdateItem().addActionListener(new AutoUpdateEvent());
		menubar.getOpen().addActionListener(new OpenWaveEvent());
		menubar.getMergeErrorsFromImpectedFile().addActionListener(new MergeErrorEventFromImpectedFiles());
		menubar.getMergeErrorsFromMigratedFile().addActionListener(new MergeErrorEventFromMigratedFiles());
		menubar.getFormat().addActionListener(new FormatEvent());
		menubar.getLeftICO1File().addActionListener(new LeftFileEvent());
		menubar.getDataBypassFile().addActionListener(new ByPassEvent());
		menubar.getRefresh().addActionListener(new RefreshEvent());
		menubar.getValidate().addActionListener(new ValidateEvent());
		menubar.getPackageData().addActionListener(new PackageEvent());
		menubar.getMergeMigrationMigrated().addActionListener(new MergeEvent());
		menubar.getImadaq().addActionListener(new ImadaqEvent());
		menubar.getDedupRun().addActionListener(new DedupEvent());
		menubar.getRulesRun().addActionListener(new RulesEvent());
		menubar.getEarrachRun().addActionListener(new EarrachEvent());
		menubar.getSiteFixed().addActionListener(new SiteFixedEvent());
		menubar.getMLAN().addActionListener(new MLANEvent());
		menubar.getQueryExecutor().addActionListener(new QueryExecutorViewEvent());
		menubar.getMemoryUsagesItem().addActionListener(new MemoryUsagesEvent());
		menubar.getCustomConfigItem().addActionListener(new CustomConfigEvent());
		menubar.getELKConfigItem().addActionListener(new ELKConfigEvent());
		
		menubar.getGOLDEncryptor().addActionListener(new PublicPrivateEvent("GOLD",Directories.GOLDDatabaseConfigLocation));
		menubar.getCSIEncryptor().addActionListener(new PublicPrivateEvent("CSI",Directories.CSIDatabaseConfigLocation));
		menubar.getArchivalEncryptor().addActionListener(new PublicPrivateEvent("ARCHIVAL",Directories.ArchivalDatabaseConfigLocation));
		
		menubar.getImadaqv02().addActionListener(new Imadaqv02Event());
		
		toolbar.getOpenWave().addActionListener(new OpenWaveEvent());
		toolbar.getMergeErrorsImpeectedOrders().addActionListener(new MergeErrorEventFromImpectedFiles());
		toolbar.getMergeErrorsMigratedOrders().addActionListener(new MergeErrorEventFromMigratedFiles());
		toolbar.getFormatCSV().addActionListener(new FormatEvent());
		toolbar.getLeftICO().addActionListener(new LeftFileEvent());
		toolbar.getCiBaseExecutionResultFilter().addActionListener(new CiBaseExecutionResultFilterEvent());
		toolbar.getDataBypass().addActionListener(new ByPassEvent());
		toolbar.getRefresh().addActionListener(new RefreshEvent());
		toolbar.getValidate().addActionListener(new ValidateEvent());
		toolbar.getPackageB().addActionListener(new PackageEvent());
		toolbar.getMergeMigrated().addActionListener(new MergeEvent());
	
		toolbar.getImadaq().addActionListener(new ImadaqEvent());
		toolbar.getDedupRun().addActionListener(new DedupEvent());
		toolbar.getSiteFixed().addActionListener(new SiteFixedEvent());
		
		toolbar.getMlan().addActionListener(new MLANEvent());
		toolbar.getQueryExecutor().addActionListener(new QueryExecutorViewEvent());
		toolbar.getRules().addActionListener(new RulesEvent());
		toolbar.getEarrach().addActionListener(new EarrachEvent());
		toolbar.getImadaqv02().addActionListener(new Imadaqv02Event());
		
		dedupViewComponents.getDedupBrowse().addActionListener(new DedupFileBrowseEvent());
		dedupViewComponents.getDedupValidate().addActionListener(new DedupFileValidateEvent());
		rulesExtractViewComponents.getRulesExtract().addActionListener(new RulesExtractEvent());
		
		
		earrachExtractViewComponents.getRulesExtract().addActionListener(new EarrachExtractEvent());
		earrachExtractViewComponents.getBrowseButton().addActionListener(new JSONFileBrowseEvent());
		earrachExtractViewComponents.getshowJSONButton().addActionListener(new ViewJSONEvent());
		earrachExtractViewComponents.getcheckJSONButton().addActionListener(new JSONCheckEvent());
		earrachExtractViewComponents.getJSONValidateButton().addActionListener(new JSONValidateEvent());
		earrachExtractViewComponents.getcheckClosedButton().addActionListener(new ClosedValidateEvent());
		earrachExtractViewComponents.getJsonToCSVButton().addActionListener(new JSONToCSVEvent());
		earrachExtractViewComponents.getD9().addActionListener(new D9ValidationEvent());
		
		siteFixViewComponents.getBrowse().addActionListener(new SiteFixBrowseEvent());
		siteFixViewComponents.getDedupbrowse().addActionListener(new ConsolidatedDEDUPFileBrowseEvent());
		
		siteFixViewComponents.getValidateForSplChr().addActionListener(new SplChrsFixValidateEvent());
		siteFixViewComponents.getLinkedupSitesButton().addActionListener(new LinkedupSitesFirstStepEvent());
		siteFixViewComponents.getLinkedupSitesAnalysisButton().addActionListener(new LinkedupSitesAnalysisEvent());
		
		siteFixViewComponents.getMigrationRelocation().addActionListener(new MigrationRelocationEvent());
		siteFixViewComponents.getPersonUpdate().addActionListener(new PersonUpdateEvent());
		siteFixViewComponents.getProductOrdersHierarchy().addActionListener(new ProductOrderHierarchyEvent());
		siteFixViewComponents.getDedupFileAnalysis().addActionListener(new DedupFileAnalysisEvent());
		siteFixViewComponents.getEQL2InstanceMapping().addActionListener(new EQL2InstanceMappingAnalysisEvent());
		
		siteFixViewComponents.getPartialCustomerSiteMigrationButton().addActionListener(new PartialCustomerSiteMigrationEvent());
		siteFixViewComponents.getGOLDCSISync().addActionListener(new GOLDCSISyncEvent());
	
		siteFixViewComponents.getValidate().addActionListener(new SiteFixValidateEvent());
		
		mlanViewComponents.getHookahBrowseButton().addActionListener(new HookahBrowseEvent());
		mlanViewComponents.getHookahInfoButton().addActionListener(new HookahInfoEvent());
		mlanViewComponents.getExecuteOrderListButton().addActionListener(new ServiceBuildInfoByOrdersEvent());
		mlanViewComponents.getMLANStartButton().addActionListener(new MLANServiceBuildInfoEvent());
		mlanViewComponents.getL2StartButton().addActionListener(new L2ServiceBuildInfoEvent());
		mlanViewComponents.getServiceBuildInfoButton().addActionListener(new ServiceBuildInfoEvent());
		mlanViewComponents.getUSIDInfoButton().addActionListener(new USIDInfoEvent());
		mlanViewComponents.getDeliverSRF2Button().addActionListener(new DeliverSRF2Event());
		mlanViewComponents.getICOWithChildButton().addActionListener(new ICOWithChildEvent());
		mlanViewComponents.getExecuteCriteriaButton().addActionListener(new ExecuteCriteriaEvent());
		mlanViewComponents.getFormatCSVButton().addActionListener(new ServiceBuildFormatCSVEvent());
		mlanViewComponents.getOrderHierarchy().addActionListener(new OrderHierarchyEvent());
		
		
		queryExecutorComponents.getQueryExecutorButton().addActionListener(new QueryExecutorEvent());
		
		/*
		 * IMADAQV02
		 * 
		 */
		
		imadaqv02ViewComponent.getBroweButton().addActionListener(new ImadaqV02BrowseEvent());
		imadaqv02ViewComponent.getIOVUpdateButton().addActionListener(new ImadaqV02IOVUpdateEvent());
		//imadaqv02ViewComponent.getAutoAnalysisButton().addActionListener(new ImadaqV02ExtractEvent());
		imadaqv02ViewComponent.getAutoAnalysisButton().addActionListener(new AutoAnalysisEvent());
		imadaqv02ViewComponent.getAddAutoAnalysisButton().addActionListener(new AddAutoAnalysisEvent());
		toolButtonNature(false);
	
		if(isUpdateAvailable.equalsIgnoreCase("META"))
		{
		frame.setTitle(System.getProperty("user.name")+" | Version ("+CommonUtils.version+") |"+isUpdateAvailable+ " Update Available! 'Mandatory to Update.'");
		menubar.getAutoUpdate().setEnabled(true);
		menubar.getEventMenu().setEnabled(false);
		menubar.getProspectiveMenu().setEnabled(false);
		toolbar.removeAll();
		}
		else if(isUpdateAvailable.equalsIgnoreCase("PATCH"))
		{
		frame.setTitle(System.getProperty("user.name")+" | Version ("+CommonUtils.version+") |"+isUpdateAvailable+ " Update Available!");
		menubar.getAutoUpdate().setEnabled(true);
		}
		else if(isUpdateAvailable.equalsIgnoreCase("NA"))
		{
		frame.setTitle(System.getProperty("user.name")+" | Version ("+CommonUtils.version+") |"+" Not able to get version details!");
		menubar.getAutoUpdate().setEnabled(true);
		}
		else
		{
		frame.setTitle(System.getProperty("user.name")+" Version ("+CommonUtils.version+")");	
		menubar.getAutoUpdate().setEnabled(false);
		}
		
		
		frame.setIconImage(CommonUtils.setSizeImage(Icons.leafIcon, 35, 35));
		frame.setJMenuBar(menubar.getMenuBar());
		Container contentPane = frame.getContentPane();
	    contentPane.add(toolbar, BorderLayout.NORTH);
	    frame.pack();
        
		frame.setSize(screenSize.width , screenHeight);
		frame.setVisible(isFrameVisible);
	}
private void addInMap(Map<String,ArrayList<String[]>> map,String key , String[] val)
{
	if(map.containsKey(key))
	{
		ArrayList<String[]> arr = map.get(key);
		arr.add(val);
		map.put(key, arr);
	}
	else{
		ArrayList<String[]> arr = new ArrayList<String[]>();
		arr.add(val);
		map.put(key, arr);
	}
	
}
private void checkOrdersinGOLDandCSI(DataTableCreator dataTableCreator,String tabName) throws IOException
{
	
 String header[] = {"QUOTENUMBER","SITECODE","QUOTE_IN_GOLD","QUOTE_IN_CSI","QUOTE_IN_CSI_BUT_NOT_IN_GOLD"};
 ArrayList<String[]> rows = new ArrayList<String[]>();
 rows.add(header);
 Iterator<Map.Entry<String, ArrayList<String[]>>> entries = goldOrderMap.entrySet().iterator();
	while (entries.hasNext()) 
	{
		Map.Entry<String, ArrayList<String[]>> entry = entries.next();
		String KEY = entry.getKey();
		ArrayList<String[]>  value = entry.getValue();
		String[] row = new String[header.length+1]; 
		row[0] = KEY;
		row[1] = value.get(0)[5];
		row[2] = "YES";
		
		if(csiOrderMap.containsKey(KEY))
		{
			row[3] = "YES";
		}
		else
		{
			row[3] = "NO";
		}
		rows.add(row);
	}
	Iterator<Map.Entry<String, ArrayList<String[]>>> csientries = csiOrderMap.entrySet().iterator();
	while (csientries.hasNext()) 
	{
		Map.Entry<String, ArrayList<String[]>> entry = csientries.next();
		String KEY = entry.getKey();
		ArrayList<String[]>  value = entry.getValue();
		String[] row = new String[header.length+1]; 
		row[0] = KEY;
		row[1] = value.get(0)[3];
		if(!goldOrderMap.containsKey(KEY))
		{
			row[2] = "NO";
			row[3] = "YES";
			row[4] = "YES";
			rows.add(row);
		}
		
}
	dataTableCreator.setQueryResultDecorator(rows,tabName) ;
}

	private void createDir(){
		String dirLoc = Directories.mLanDirLoc;
		new File(dirLoc).mkdirs();
		
	}
	private void createHookahDir(){
		String dirLoc = Directories.hookahDirLoc;
		new File(dirLoc).mkdirs();
		
	}
	
	private void createL2Dir(){
		String dirLoc = Directories.l2DirLoc;
		new File(dirLoc).mkdirs();
		
	}
	
	private String createSBDir(){
		String dirLoc = Directories.sbDirLoc+File.separator+CommonUtils.getFileName();
		new File(dirLoc).mkdirs();
		return dirLoc;
		
	}
	
	private String createSRF2Dir(){
		String dirLoc = Directories.srf2DirLoc+File.separator+CommonUtils.getFileName();
		new File(dirLoc).mkdirs();
		return dirLoc;
		
	}
	
	private String createICOPARDir(){
		String dirLoc = Directories.icoWithParent+File.separator+CommonUtils.getFileName();
		new File(dirLoc).mkdirs();
		return dirLoc;
		
	}
	private void dedupQueries(DedupViewComponents dedupViewComponents, JTabbedPane tabbedPane, DedupFileDataValidator dataValidator,DedupFileValidator validator, ArrayList<CustomJCheckBox> checkBoxes,CustomJFrame tabbedFrame) throws IOException{
		
		DataTableCreator dataTableCreator = new DataTableCreator(dedupViewComponents, tabbedPane, dataValidator, validator);
		dedupViewComponents.getQueryResult().append("Start Validator...\n");
		
		double count = 0.0; 
		double csiDataCount = 0;
		
		for(CustomJCheckBox checkBox :checkBoxes)
		{
			if(checkBox.isSelected())
			{
				csiDataCount++;
			}
		}
		
		for(CustomJCheckBox checkBox :checkBoxes)
		{
			if(checkBox.isSelected())
			{
				
			String checkboxLabel = checkBox.getText();
		    if(checkboxLabel.equalsIgnoreCase("ORDERS_ON_TRUE_SITE_BEFORE_MIGRATION"))
		    {
		    	if(dedupViewComponents.hasShowSQL())
		    	{
		    		dedupViewComponents.getQueryResult().append("EXECUTING QUERY TO GET THE ORDERS , BEFORE MIGRATION OF TRUE SITE ORDERS..\n");
					dedupViewComponents.getQueryResult().appendListStringBuilder(validator.getQueryForTrueOrdersSites());
		    	}
					dataTableCreator.databaseSQLListExecution(validator.getQueryForTrueOrdersSites(),"ORDERS_ON_TRUE_SITE_BEFORE_MIGRATION");
			}
				
				
			else if(checkboxLabel.equalsIgnoreCase("ORDERS_ON_FALSE_SITE_BEFORE_MIGRATION_INGOLD")){
				if(dedupViewComponents.hasShowSQL())
		    	{
					dedupViewComponents.getQueryResult().append("EXECUTING QUERT TO GET THE ORDERS , BEFORE MIGRATION OF FALSE SITE ORDERS..\n");
					dedupViewComponents.getQueryResult().appendListStringBuilder(validator.getQueryForFalseOrdersSites());
		    	}
					ArrayList<String[]> golddata = dataTableCreator.databaseSQLListExecution(validator.getQueryForFalseOrdersSites(),"ORDERS_ON_FALSE_SITE_BEFORE_MIGRATION_INGOLD");
				    int skipheader = 0 ; 
					for(String[] arr: golddata)
				    {
						if(skipheader != 0){
							addInMap(goldOrderMap,arr[0],arr);
						}
						skipheader++;
				    }
				
				}
				
				else if(checkboxLabel.equalsIgnoreCase("ORDERS_ON_FALSE_SITE_BEFORE_MIGRATION_INCSI")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("EXECUTING QUERY TO GET THE ORDERS , BEFORE MIGRATION OF FALSE SITE FROM CSI..\n");
					dedupViewComponents.getQueryResult().appendListStringBuilder(validator.getQueryForFalseOrdersSitesFromCSI());
			    	}
					ArrayList<String[]> csidata = dataTableCreator.databaseSQLListExecutionFromCSI(validator.getQueryForFalseOrdersSitesFromCSI(),"ORDERS_ON_FALSE_SITE_BEFORE_MIGRATION_INCSI");
					int skipheader = 0 ; 
					for(String[] arr: csidata)
					    {
						if(skipheader != 0){
					    	addInMap(csiOrderMap,arr[0],arr);
						}
						skipheader++;
					    }
					
				}
				
				else if(checkboxLabel.equalsIgnoreCase("ORDERS_ON_GOLD_N_CSI_COMP"))
				{
					 checkOrdersinGOLDandCSI(dataTableCreator,"ORDERS_ON_GOLD_N_CSI_COMP");
				}
				else if(checkboxLabel.equalsIgnoreCase("ORDERS_SERVICE"))
				{
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("EXECUTING QUERY TO GET THE ORDERS AND SERVICES ASSOCIATED..\n");
					dedupViewComponents.getQueryResult().appendListStringBuilder(validator.getQueryForOrderService());
			    	}
					ArrayList<String[]>  orderservicedata = dataTableCreator.databaseSQLListExecution(validator.getQueryForOrderService(),"ORDERS_SERVICE");
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("EXECUTING QUERY TO GET THE SITECODES THOSE HAS ORDERS ON ALREADY ON MIGRATED SERVCIES IN EarRRacH ..Should give zero result..\n");
			    	}
					
					dataTableCreator.databaseSQLFilterListExecution(orderservicedata,"ORDERS_ON_MIGRATED_SERVICES_MATCH",CommonUtils.PPL_LIST);
				}
		    
				else if(checkboxLabel.equalsIgnoreCase("Unavailabe_Orange_sitename(Keep)")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get unavailabe Orange_sitename (Keep)should give zero result...\n");
					dedupViewComponents.getQueryResult().append(validator.getQueryForTrueOSR().toString() + "\n\n");
			    	}
					dataTableCreator.databaseSQLExecution(validator.getQueryForTrueOSR(),"Unavailabe Orange_sitename(T)");
				}
				else if(checkboxLabel.equalsIgnoreCase("Unavailabe_Orange_sitename(Discard)")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get unavailabe Orange_sitename (Discard)should give zero result...\n");
					dedupViewComponents.getQueryResult().append(validator.getQueryForFalseOSR().toString() + "\n\n");
			    	}
					dataTableCreator.databaseSQLExecution(validator.getQueryForFalseOSR(),"Unavailabe Orange_sitename(F)");
				}
				
				else if(checkboxLabel.equalsIgnoreCase("Unavailabe_Orange_sitename")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get unavailabe Orange_sitename should give zero result...\n");
					dedupViewComponents.getQueryResult().append(validator.getQueryForOSR().toString() + "\n\n");
			    	}
					dataTableCreator.databaseSQLExecution(validator.getQueryForOSR(),"Unavailabe Orange_sitename");
				}
				
				else if(checkboxLabel.equalsIgnoreCase("Unavailabe_Sitecode(Keep)")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get unavailabe sitecodes(Keep)should give zero result...\n");
					dedupViewComponents.getQueryResult().append(validator.getQueryForTrueSiteCode().toString() + "\n\n");
			    	}
					dataTableCreator.databaseSQLExecution(validator.getQueryForTrueSiteCode(),"Unavailabe Sitecode(T)");
				}
				
				else if(checkboxLabel.equalsIgnoreCase("Unavailabe_Sitecode(Discard)")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get unavailabe sitecodes(Discard) should give zero result...\n");
					dedupViewComponents.getQueryResult().append(validator.getQueryForFalseSiteCode().toString() + "\n\n");
			    	}
					dataTableCreator.databaseSQLExecution(validator.getQueryForFalseSiteCode(),"Unavailabe Sitecode(F)");
				}
				
				else if(checkboxLabel.equalsIgnoreCase("Unavailabe_Sitecode")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get unavailabe sitecodes should give zero result...\n");
					dedupViewComponents.getQueryResult().append(validator.getQueryForSiteCode().toString() + "\n\n");
			    	}
					dataTableCreator.databaseSQLExecution(validator.getQueryForSiteCode(),"Unavailabe Sitecode");
				}
				
				else if(checkboxLabel.equalsIgnoreCase("Unavailabe_Addressid(Keep)")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get unavailabe addressid (Keep)should give zero result...\n");
					dedupViewComponents.getQueryResult().append(validator.getQueryForTrueUnavailavleAddress().toString()+ "\n\n");
			    	}
					dataTableCreator.databaseSQLExecution(validator.getQueryForTrueUnavailavleAddress(),"Unavailabe Addressid(T)");
				}
				else if(checkboxLabel.equalsIgnoreCase("Unavailabe_Addressid(Discard)")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get unavailabe addressid (Discard)should give zero result...\n");
					dedupViewComponents.getQueryResult().append(validator.getQueryForFalseUnavailavleAddress().toString()+ "\n\n");
			    	}
					dataTableCreator.databaseSQLExecution(validator.getQueryForFalseUnavailavleAddress(),"Unavailabe Addressid(F)");
				}
				
				else if(checkboxLabel.equalsIgnoreCase("Unavailabe_Addressid")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get unavailabe addressid should give zero result...\n");
					dedupViewComponents.getQueryResult().append(validator.getQueryForUnavailavleAddress().toString()+ "\n\n");
			    	}
					dataTableCreator.databaseSQLExecution(validator.getQueryForUnavailavleAddress(),"Unavailabe Addressid");
				}
				
				else if(checkboxLabel.equalsIgnoreCase("Unavailabe_ICO")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get unavailabe ICOs should give zero result...\n");
					dedupViewComponents.getQueryResult().append(validator.getQueryForUnavailavleICO().toString()+ "\n\n");
			    	}
					dataTableCreator.databaseSQLExecution(validator.getQueryForUnavailavleICO(),"Unavailabe ICO");
				}
				
				else if(checkboxLabel.equalsIgnoreCase("addressid_with_multiple_true_sites")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get addressid , has multiple true sites , should give zero result...\n");
					dedupViewComponents.getQueryResult().appendListStringBuilder(validator.getQueryForAddressHasMultipleSitecode());
			    	}
					dataTableCreator.databaseSQLListExecution(validator.getQueryForAddressHasMultipleSitecode(),"addressid with multiple true sites");
				}
				
				else if(checkboxLabel.equalsIgnoreCase("sites(Keep)_without_any_active_status")){
					dedupViewComponents.getQueryResult().append("Executing Query to check sites without any active status (For TRUE AM Response only) ....\n");
					dedupViewComponents.getQueryResult().appendListStringBuilder(validator.getQueryTrueSiteCodeStatus());
					dataTableCreator.databaseSQLListExecution(validator.getQueryTrueSiteCodeStatus(),"sites(T) without any active status");
				}
				
				else if(checkboxLabel.equalsIgnoreCase("sites(Discard)_without_any_active_status")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to check sites without any active status (For False AM Response only) ....\n");
					dedupViewComponents.getQueryResult().appendListStringBuilder(validator.getQueryFalseSiteCodeStatus());
			    	}
					dataTableCreator.databaseSQLListExecution(validator.getQueryFalseSiteCodeStatus(),"sites(F) without any active status");
				}
				
				else if(checkboxLabel.equalsIgnoreCase("Unique_Sitecode(Keep)")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to check uniqueness of sitecode (For TRUE AM Response only) ...\n");
					dedupViewComponents.getQueryResult().appendListStringBuilder(validator.hasDuplicateTrueSiteCode());
			    	}
					dataTableCreator.databaseSQLListExecution(validator.hasDuplicateTrueSiteCode(), "Unique Sitecode(T)");
				}
				
				else if(checkboxLabel.equalsIgnoreCase("Unique_Sitecode(Discard)")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to check uniqueness of sitecode (For False AM Response only) ...\n");
					dedupViewComponents.getQueryResult().appendListStringBuilder(validator.hasDuplicateFalseSiteCode());
			    	}
					dataTableCreator.databaseSQLListExecution(validator.hasDuplicateFalseSiteCode(),"Unique Sitecode(F)");
				}
													
				
				else if(checkboxLabel.equalsIgnoreCase("Sitecode(Keep)_with_multiple_ICOs")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get sitecode linked with multiple ICOs (For TRUE AM Response only) ...\n");
					dedupViewComponents.getQueryResult().appendListStringBuilder(validator.trueSitecodeLinkedWithMultipleICO());
			    	}
					dataTableCreator.databaseSQLListExecution(validator.trueSitecodeLinkedWithMultipleICO(),"Sitecode(T) with multiple ICOs");
				}
				
				else if(checkboxLabel.equalsIgnoreCase("Sitecode(Discard)_with_multiple_ICOs"))
				{
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get sitecode linked with multiple ICOs (For False AM Response only) ...\n");
					dedupViewComponents.getQueryResult().appendListStringBuilder(validator.falseSitecodeLinkedWithMultipleICO());
			    	}
					dataTableCreator.databaseSQLListExecution(validator.falseSitecodeLinkedWithMultipleICO(),"Sitecode(F) with multiple ICOs");
				}
				else if(checkboxLabel.equalsIgnoreCase("ICO_linked_with_sitecode(Keep)")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get ICO linked with sitecode (For TRUE AM Response only) ...\n");
					dedupViewComponents.getQueryResult().appendListStringBuilder(validator.getIcoForTrueSiteCode());
			    	}
					dataTableCreator.databaseSQLFilterListExecution(validator.getIcoForTrueSiteCode(),validator.getCsvFileData(), "ICO linked with sitecode(T)",2,22,0,1);
				}
				else if(checkboxLabel.equalsIgnoreCase("ICO_linked_with_sitecode(Discard)")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get ICO linked with sitecode (For False AM Response only) ...\n");
					dedupViewComponents.getQueryResult().appendListStringBuilder(validator.getIcoForFalseSiteCode());
			    	}
					dataTableCreator.databaseSQLFilterListExecution(validator.getIcoForFalseSiteCode(),	validator.getCsvFileData(), "ICO linked with sitecode(F)",2,22,0,1);
				}
				else if(checkboxLabel.equalsIgnoreCase("Address_IDs(Keep)_linked_with_Multiple_ICO")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get Address IDs (True )linked with Multiple ICO...\n");
					dedupViewComponents.getQueryResult().appendListStringBuilder(validator.trueAddressIdLinkedWithMultipleICO());
			    	}
					dataTableCreator.databaseSQLAddressIDFilterListExecution(validator.trueAddressIdLinkedWithMultipleICO(),validator.getCsvFileData(),"Address ID(T) linked with Multiple ICO");
				}
				else if(checkboxLabel.equalsIgnoreCase("Address_IDs(Discard)_linked_with_Multiple_ICO")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get Address IDs (False)linked with Multiple ICO...\n");
				    dedupViewComponents.getQueryResult().appendListStringBuilder(validator.falseAddressIdLinkedWithMultipleICO());
			    	}
				    dataTableCreator.databaseSQLAddressIDFilterListExecution(validator.falseAddressIdLinkedWithMultipleICO(),validator.getCsvFileData(), "Address ID(F) linked with Multiple ICO");
				}
				/* Add As per Sakshi  DQ Team Requirement */
				else if(checkboxLabel.equalsIgnoreCase("SITECODE_CODE_ID_GOLD"))
				{
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get Core_Ids linked with Sitecode...\n");
				    dedupViewComponents.getQueryResult().appendListStringBuilder(validator.coreIdLinkedWithSitecode());
			    	}
				    ArrayList<String[]> tableData = dataTableCreator.databaseSQLListExecution(validator.coreIdLinkedWithSitecode(), "SITECODE_CODE_ID_GOLD");
				    
				    if(dedupViewComponents.hasShowSQL())
			    	{
				    dedupViewComponents.getQueryResult().append("Executing Query to get mismatch Core_Ids linked with Sitecode...Should give zero result..\n");
				    
			    	}
				    //dedupViewComponents.getQueryResult().appendListStringBuilder(validator.coreIdLinkedWithSitecode());
				    dataTableCreator.databaseSQLFilterListExecutionWithData(tableData,validator.getCsvFileData(), "SITECODE_MISMATCH_WITH_COREIDS",3,10,22,1,2,0);
				    
				}
				else if(checkboxLabel.equalsIgnoreCase("Sitecode(Keep)_Linked_Core_Ids")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get Core_Ids linked with True Sitecode...\n");
				    dedupViewComponents.getQueryResult().appendListStringBuilder(validator.coreIdLinkedWithTrueSitecode());
			    	}
				    dataTableCreator.databaseSQLListExecution(validator.coreIdLinkedWithTrueSitecode(), "Sitecode's(T) Core_IDs");
				}
				
				else if(checkboxLabel.equalsIgnoreCase("Sitecode(Discard)_Linked_Core_Ids")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get Core_Ids linked with False Sitecode...\n");
				    dedupViewComponents.getQueryResult().appendListStringBuilder(validator.coreIdLinkedWithFalseSitecode());
			    	}
				    dataTableCreator.databaseSQLListExecution(validator.coreIdLinkedWithFalseSitecode(), "Sitecode's(F) Core_IDs");
				}
							
				
				
				// End as per Sakshi DQ Team Requirement
				
				else if(checkboxLabel.equalsIgnoreCase("Sitecode(True)_Linked_Address_Id")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query to get Address IDs linked with False Sitecode...\n");
				    dedupViewComponents.getQueryResult().appendListStringBuilder(validator.falseAddressIdLinkedWithMultipleICO());
			    	}
				    dataTableCreator.databaseSQLAddressIDFilterListExecution(validator.falseAddressIdLinkedWithMultipleICO(),validator.getCsvFileData(), "Address ID(F) linked with Multiple ICO");
				}
				
				else if(checkboxLabel.equalsIgnoreCase("PostRun_Checks_Keep"))
				{
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents	.getQueryResult().append("Executing Query for Post Run Checks For True sitecode ...\n");
					dedupViewComponents.getQueryResult().appendListStringBuilder(validator.postCheckQueryForTrueSiteCode());
			    	}
					dataTableCreator.databaseSQLListExecution(validator.postCheckQueryForTrueSiteCode(),"Post Run Checks For sitecode(T)");
				}
				else if(checkboxLabel.equalsIgnoreCase("PostRun_Checks_Discard")){
					if(dedupViewComponents.hasShowSQL())
			    	{
					dedupViewComponents.getQueryResult().append("Executing Query for Post Run Checks For False sitecode ...\n");
					dedupViewComponents.getQueryResult().appendListStringBuilder(validator.postCheckQueryForFalseSiteCode());
			    	}
					dataTableCreator.databaseSQLListExecution(validator.postCheckQueryForFalseSiteCode(),"Post Run Checks For sitecode(F)");
				}
				else
				{
					JOptionPane.showMessageDialog(new Frame(),"No SQL Found for "+checkboxLabel);
				}
			
				count++;	
				ProgressMonitorPane.getInstance().setProgress(count,csiDataCount);
			}
		}
		dedupViewComponents.getQueryResult().append("Process completed successfully ...\n");
		CommonUtils.createConsoleLogFile(dedupViewComponents);
		
		tabbedFrame.setVisible(true);
	}
	
	
	private void dedupView() {
		sp.getQueryExecutorVsplitPane().setVisible(false);
		sp.getImadaqVsplitPane().setVisible(false);
		sp.getImadaqHsplitPane().setVisible(false);
		sp.getRuleVsplitPane().setVisible(false);
		sp.getSiteMigrationVsplitPane().setVisible(false);
		sp.getMLANVsplitPane().setVisible(false);
		sp.getSiteFixedVsplitPane().setVisible(false);
		sp.getEarrachVsplitPane().setVisible(false);
		sp.getImadaqV02VsplitPane().setVisible(false);

		sp.getDedupVsplitPane().setTopComponent(dedupViewComponents.getPanel());
		sp.getDedupVsplitPane().setBottomComponent(dedupViewComponents.getScrollPane());
		sp.getDedupVsplitPane().setDividerLocation(screenSize.height * 60 / 100);
		frame.add(sp.getDedupVsplitPane(), BorderLayout.CENTER);
		sp.getDedupVsplitPane().setVisible(true);
		frame.setSize(screenSize.width , screenHeight);
		frame.setVisible(true);
		
		dedupViewComponents.addProgressMonitorPane();

	}
	
	
	private void earrachView() {
		sp.getQueryExecutorVsplitPane().setVisible(false);
		sp.getImadaqVsplitPane().setVisible(false);
		sp.getImadaqHsplitPane().setVisible(false);
		sp.getDedupVsplitPane().setVisible(false);
		sp.getSiteMigrationVsplitPane().setVisible(false);
		sp.getSiteFixedVsplitPane().setVisible(false);
		sp.getMLANVsplitPane().setVisible(false);
		sp.getRuleVsplitPane().setVisible(false);
		sp.getImadaqV02VsplitPane().setVisible(false);

		sp.getEarrachVsplitPane().setTopComponent(earrachExtractViewComponents.getPanel());
		sp.getEarrachVsplitPane().setBottomComponent(earrachExtractViewComponents.getQueryResult());
		sp.getEarrachVsplitPane().setDividerLocation(screenSize.height * 60 / 100);
		frame.add(sp.getEarrachVsplitPane(), BorderLayout.CENTER);
		sp.getEarrachVsplitPane().setVisible(true);
		frame.setSize(screenSize.width , screenHeight);
		frame.setVisible(true);
		
		earrachExtractViewComponents.addProgressMonitorPane();

	}
	
	
	
	
	
	public void loadDBConfigProp(String goldKey,String archivalKey,String csiKey) throws Exception
	{
			   if(null!= goldKey && goldKey.length()>1)
			   {
				System.out.println("====================================================================================================");
				System.out.println("Connecting GOLD Archive database...");
				
				Properties properties = CommonUtils.getGoldDBProperties();
				String[] values = properties.get(goldKey).toString().split(PublicKeyGenerator.encrypt(PublicPrivateKeyGenerator.seperator));
				
				String userName 		= PublicKeyGenerator.decrypt(values[0]);
				String password 		= PublicKeyGenerator.decrypt(values[1]);
				String hostName 		= PublicKeyGenerator.decrypt(values[2]);
				String port 				= PublicKeyGenerator.decrypt(values[3]);
				String sid 					= PublicKeyGenerator.decrypt(values[4]);
				String prefix 				= PublicKeyGenerator.decrypt(values[5]);
				
				ConnectionBean.setDbName(goldKey);
				ConnectionBean.setUserName(userName);
				ConnectionBean.setSid(sid);
				ConnectionBean.setUrl(hostName);
				ConnectionBean.setDbPrefix(prefix);
				ConnectionBean.setPort(port);
				ConnectionBean.setPassword(password);
				
				System.out.println("GOLD database connected successfully...");
				System.out.println("====================================================================================================");
			   }
			   if(null!= archivalKey && archivalKey.length()>1)
			   {
				System.out.println("====================================================================================================");
				System.out.println("Connecting ARCHIVE database...");
				
				Properties properties = CommonUtils.getArchivalDBProperties();
				String[] values = properties.get(archivalKey).toString().split(PublicKeyGenerator.encrypt(PublicPrivateKeyGenerator.seperator));
				
				String userName 		= PublicKeyGenerator.decrypt(values[0]);
				String password 		= PublicKeyGenerator.decrypt(values[1]);
				String hostName 		= PublicKeyGenerator.decrypt(values[2]);
				String port 				= PublicKeyGenerator.decrypt(values[3]);
				String sid 					= PublicKeyGenerator.decrypt(values[4]);
				String prefix 				= PublicKeyGenerator.decrypt(values[5]);
				
				ConnectionBeanArchived.setDbName(archivalKey);
				ConnectionBeanArchived.setUserName(userName);
				ConnectionBeanArchived.setSid(sid);
				ConnectionBeanArchived.setUrl(hostName);
				ConnectionBeanArchived.setDbPrefix(prefix);
				ConnectionBeanArchived.setPort(port);
				ConnectionBeanArchived.setPassword(password);
				
				System.out.println("ARCHIVE database connected successfully...");
				System.out.println("====================================================================================================");
			   }
			   
			   if(null!= csiKey && csiKey.length()>1)
			   {
				   System.out.println("====================================================================================================");
				   System.out.println("Connecting CSI database...");
				
				Properties properties = CommonUtils.getCsiDBProperties();
				String[] values = properties.get(csiKey).toString().split(PublicKeyGenerator.encrypt(PublicPrivateKeyGenerator.seperator));
				
				String userName 		= PublicKeyGenerator.decrypt(values[0]);
				String password 		= PublicKeyGenerator.decrypt(values[1]);
				String hostName 		= PublicKeyGenerator.decrypt(values[2]);
				String port 				= PublicKeyGenerator.decrypt(values[3]);
				String sid 					= PublicKeyGenerator.decrypt(values[4]);
				String prefix 				= PublicKeyGenerator.decrypt(values[5]);
				
				ConnectionBeanCSI.setDbName(csiKey);
				ConnectionBeanCSI.setUserName(userName);
				ConnectionBeanCSI.setSid(sid);
				ConnectionBeanCSI.setUrl(hostName);
				ConnectionBeanCSI.setDbPrefix(prefix);
				ConnectionBeanCSI.setPort(port);
				ConnectionBeanCSI.setPassword(password);
				
				System.out.println("CSI database connected successfully...");
				System.out.println("====================================================================================================");
				
			   }

		
	}
	
private void imadaqView() {
		
		sp.getQueryExecutorVsplitPane().setVisible(false);
		sp.getSiteFixedVsplitPane().setVisible(false);		
		sp.getDedupVsplitPane().setVisible(false);
		sp.getRuleVsplitPane().setVisible(false);
		sp.getEarrachVsplitPane().setVisible(false);
		sp.getSiteMigrationVsplitPane().setVisible(false);
		sp.getMLANVsplitPane().setVisible(false);
		sp.getImadaqV02VsplitPane().setVisible(false);
		
		
		frame.add(sp.getImadaqVsplitPane(), BorderLayout.CENTER);
		sp.getImadaqVsplitPane().setDividerLocation(screenSize.height * 70 / 100);
		sp.getImadaqHsplitPane().setDividerLocation(screenSize.width * 30 / 100);
		sp.getImadaqVsplitPane().setVisible(true);
		sp.getImadaqHsplitPane().setVisible(true);
		frame.setSize(screenSize.width , screenHeight);
		frame.setVisible(true);
		
	}
	

	private void mLANView() {
		
		sp.getQueryExecutorVsplitPane().setVisible(false);
		sp.getSiteFixedVsplitPane().setVisible(false);
		sp.getDedupVsplitPane().setVisible(false);
		sp.getRuleVsplitPane().setVisible(false);
		sp.getSiteMigrationVsplitPane().setVisible(false);
		sp.getImadaqVsplitPane().setVisible(false);
		sp.getImadaqHsplitPane().setVisible(false);
		sp.getSiteMigrationVsplitPane().setVisible(false);
		sp.getEarrachVsplitPane().setVisible(false);
		sp.getImadaqV02VsplitPane().setVisible(false);
		
		sp.getMLANVsplitPane().setTopComponent(mlanViewComponents.getPanel());
		sp.getMLANVsplitPane().setBottomComponent(mlanViewComponents.getScrollPane());
		sp.getMLANVsplitPane().setDividerLocation(screenSize.height * 60 / 100);
		
		frame.add(sp.getMLANVsplitPane(), BorderLayout.CENTER);
		sp.getMLANVsplitPane().setVisible(true);
		frame.setSize(screenSize.width , screenHeight);
		frame.setVisible(true);
		
		mlanViewComponents.addProgressMonitorPane();
	}

	private void QueryExecuterView() {
			
			sp.getSiteFixedVsplitPane().setVisible(false);
			sp.getDedupVsplitPane().setVisible(false);
			sp.getRuleVsplitPane().setVisible(false);
			sp.getSiteMigrationVsplitPane().setVisible(false);
			sp.getImadaqVsplitPane().setVisible(false);
			sp.getImadaqHsplitPane().setVisible(false);
			sp.getSiteMigrationVsplitPane().setVisible(false);
			sp.getEarrachVsplitPane().setVisible(false);
			sp.getMLANVsplitPane().setVisible(false);
			sp.getImadaqV02VsplitPane().setVisible(false);
			
			sp.getQueryExecutorVsplitPane().setTopComponent(queryExecutorComponents.getPanel());
			sp.getQueryExecutorVsplitPane().setBottomComponent(queryExecutorComponents.getScrollPane());
			sp.getQueryExecutorVsplitPane().setDividerLocation(screenSize.height * 60 / 100);
			
			
			
			frame.add(sp.getQueryExecutorVsplitPane(), BorderLayout.CENTER);
			sp.getQueryExecutorVsplitPane().setVisible(true);
			frame.setSize(screenSize.width , screenHeight);
			frame.setVisible(true);
			
			queryExecutorComponents.addProgressMonitorPane();
		}
	
	

	private void rulesView() {
		sp.getQueryExecutorVsplitPane().setVisible(false);
		sp.getImadaqVsplitPane().setVisible(false);
		sp.getImadaqHsplitPane().setVisible(false);
		sp.getDedupVsplitPane().setVisible(false);
		sp.getSiteMigrationVsplitPane().setVisible(false);
		sp.getSiteFixedVsplitPane().setVisible(false);
		sp.getMLANVsplitPane().setVisible(false);
		sp.getEarrachVsplitPane().setVisible(false);
		sp.getImadaqV02VsplitPane().setVisible(false);

		sp.getRuleVsplitPane().setTopComponent(rulesExtractViewComponents.getPanel());
		sp.getRuleVsplitPane().setBottomComponent(rulesExtractViewComponents.getQueryResult());
		sp.getRuleVsplitPane().setDividerLocation(screenSize.height * 35 / 100);
		frame.add(sp.getRuleVsplitPane(), BorderLayout.CENTER);
		sp.getRuleVsplitPane().setVisible(true);
		frame.setSize(screenSize.width , screenHeight);
		frame.setVisible(true);
		
		rulesExtractViewComponents.addProgressMonitorPane();

	}

	private void showErrorKeysWindows(LinkedHashMap<String, ArrayList<String[]>> errors){
		if(errors.size()>0)
		{
		ErrorKeysWindows errorKeysWindows = new ErrorKeysWindows(errors);
		errorKeysWindows.setBounds(100,100,600, (errors.size()*52  < 300 ? 400 : errors.size()*52));
		errorKeysWindows.getPanel().setBounds(0,0,600, (errors.size()*52  < 300 ? 400 : errors.size()*52));
		errorKeysWindows.setVisible(true);
		ArrayList<ButtonGroup> buttonGroups = errorKeysWindows.getButtonGroups();
		ArrayList<CustomJCheckBox> checkBoxes = errorKeysWindows.getCheckBoxes();
		errorKeysWindows.getOk().addActionListener(new FileSegrigationEvent(errors,checkBoxes,buttonGroups,errorKeysWindows));
		}
	}

	private void showQuerySelector(DedupViewComponents dedupViewComponents, JTabbedPane tabbedPane, DedupFileDataValidator dataValidator,DedupFileValidator validator ,LinkedHashMap<String, ArrayList<String[]>> errors,CustomJFrame tabbedFrame){
		
		LinkedHashMap<String, Boolean> queryKeys = new  LinkedHashMap<String, Boolean> ();
		
		queryKeys.put("ORDERS_ON_TRUE_SITE_BEFORE_MIGRATION", false);
		queryKeys.put("ORDERS_ON_FALSE_SITE_BEFORE_MIGRATION_INGOLD", true);
		queryKeys.put("ORDERS_ON_FALSE_SITE_BEFORE_MIGRATION_INCSI", true);
		
		queryKeys.put("ORDERS_ON_GOLD_N_CSI_COMP", false);
		
		queryKeys.put("ORDERS_SERVICE", true);
		
		queryKeys.put("Unavailabe_Orange_sitename", false);
		queryKeys.put("Unavailabe_Orange_sitename(Keep)",true);
		queryKeys.put("Unavailabe_Orange_sitename(Discard)",true);
		
		queryKeys.put("Unavailabe_Sitecode", false);
		queryKeys.put("Unavailabe_Sitecode(Keep)", true);
		queryKeys.put("Unavailabe_Sitecode(Discard)", true);
		
		queryKeys.put("Unavailabe_Addressid", false);
		queryKeys.put("Unavailabe_Addressid(Keep)", true);
		queryKeys.put("Unavailabe_Addressid(Discard)", true);
		
		queryKeys.put("Unavailabe_ICO", true);
		queryKeys.put("addressid_with_multiple_true_sites", true);
		
		queryKeys.put("sites(Keep)_without_any_active_status", false);
		queryKeys.put("sites(Discard)_without_any_active_status", false);
		
		queryKeys.put("Unique_Sitecode(Keep)", true);
		queryKeys.put("Unique_Sitecode(Discard)", true);
		
		queryKeys.put("Sitecode(Keep)_with_multiple_ICOs", true);
		queryKeys.put("Sitecode(Discard)_with_multiple_ICOs", true);
		
		queryKeys.put("ICO_linked_with_sitecode(Keep)", true);
		queryKeys.put("ICO_linked_with_sitecode(Discard)", true);
		
		queryKeys.put("Address_IDs(Keep)_linked_with_Multiple_ICO", true);
		queryKeys.put("Address_IDs(Discard)_linked_with_Multiple_ICO", true);
		
		queryKeys.put("SITECODE_CODE_ID_GOLD", true);
		queryKeys.put("Sitecode(Keep)_Linked_Core_Ids", false);
		queryKeys.put("Sitecode(Discard)_Linked_Core_Ids", false);
		//queryKeys.put("SITECODE_MISMATCH_WITH_COREIDS", true);
		queryKeys.put("PostRun_Checks_Keep", false);
		queryKeys.put("PostRun_Checks_Discard", false);
		
		DedupQueriesWindows dedupQueryWindow = new DedupQueriesWindows(queryKeys);
		dedupQueryWindow.setBounds(100,20,600, (queryKeys.size()*30  < 300 ? 300 : queryKeys.size()*30));
		dedupQueryWindow.getPanel().setBounds(0,0,600, (queryKeys.size()*30  < 300 ? 300 : queryKeys.size()*30));
		
		dedupQueryWindow.setVisible(true);
		dedupQueryWindow.setAlwaysOnTop(true);
		
		ArrayList<CustomJCheckBox> checkBoxes = dedupQueryWindow.getCheckBoxes();
		dedupQueryWindow.getOk().addActionListener(new DedupQuerySelectorEvent(dedupViewComponents, tabbedPane, dataValidator,validator ,checkBoxes,dedupQueryWindow,errors,tabbedFrame));
		dedupQueryWindow.getCancel().addActionListener(new DedupQuerySelectorCancelEvent(errors,dedupQueryWindow));
	}

	private void siteFixedView() {
		sp.getQueryExecutorVsplitPane().setVisible(false);
		sp.getImadaqVsplitPane().setVisible(false);
		sp.getRuleVsplitPane().setVisible(false);
		sp.getImadaqHsplitPane().setVisible(false);
		sp.getSiteMigrationVsplitPane().setVisible(false);
		sp.getMLANVsplitPane().setVisible(false);
		sp.getDedupVsplitPane().setVisible(false);
		sp.getEarrachVsplitPane().setVisible(false);
		sp.getImadaqV02VsplitPane().setVisible(false);
		
		sp.getSiteFixedVsplitPane().setTopComponent(siteFixViewComponents.getPanel());
		sp.getSiteFixedVsplitPane().setBottomComponent(siteFixViewComponents.getScrollPane());
		sp.getSiteFixedVsplitPane().setDividerLocation(screenSize.height * 60 / 100);
		frame.add(sp.getSiteFixedVsplitPane(), BorderLayout.CENTER);
		sp.getSiteFixedVsplitPane().setVisible(true);
		frame.setSize(screenSize.width , screenHeight);
		frame.setVisible(true);
		
		siteFixViewComponents.addProgressMonitorPane();

	}

	
	
	private void ImadaqV02View() {
		
		sp.getQueryExecutorVsplitPane().setVisible(false);
		sp.getImadaqVsplitPane().setVisible(false);
		sp.getImadaqHsplitPane().setVisible(false);
		sp.getDedupVsplitPane().setVisible(false);
		sp.getRuleVsplitPane().setVisible(false);
		sp.getSiteFixedVsplitPane().setVisible(false);
		sp.getMLANVsplitPane().setVisible(false);
		sp.getEarrachVsplitPane().setVisible(false);
		sp.getImadaqV02VsplitPane().setVisible(false);

		
		
		sp.getImadaqV02VsplitPane().setTopComponent(imadaqv02ViewComponent.getPanel());
		sp.getImadaqV02VsplitPane().setBottomComponent(imadaqv02ViewComponent.getQueryResult());
		sp.getImadaqV02VsplitPane().setDividerLocation(screenSize.height * 60 / 100);
		frame.add(sp.getImadaqV02VsplitPane(), BorderLayout.CENTER);
		sp.getImadaqV02VsplitPane().setVisible(true);
		frame.setSize(screenSize.width , screenHeight);
		frame.setVisible(true);
		
		imadaqv02ViewComponent.addProgressMonitorPane();

	}

	private void toolButtonNature(boolean isEnabled){
		toolbar.getOpenWave().setEnabled(isEnabled);
		toolbar.getMergeErrorsImpeectedOrders().setEnabled(isEnabled);
		toolbar.getMergeErrorsMigratedOrders().setEnabled(isEnabled);
		toolbar.getFormatCSV().setEnabled(isEnabled);
		toolbar.getLeftICO().setEnabled(isEnabled);
		toolbar.getCiBaseExecutionResultFilter().setEnabled(isEnabled);
		toolbar.getDataBypass().setEnabled(isEnabled);
		toolbar.getRefresh().setEnabled(isEnabled);
		toolbar.getValidate().setEnabled(isEnabled);
		toolbar.getPackageB().setEnabled(isEnabled);
		toolbar.getMergeMigrated().setEnabled(isEnabled);
	}

	private void writeFailedDataForGID(ArrayList<String[]> fileData,String GID,ArrayList<String> failedGIds){
			for(String []row: fileData){
				if(row[0].equalsIgnoreCase(GID)){
					if(!failedGIds.contains(GID)){
					failedGIds.add(GID);
					}
				}
			}
			
		}

	private void writeFailedDataForSitecode(ArrayList<String[]> fileData,String siteCode,ArrayList<String> failedGIds){
		
		for(String []row: fileData){
			if(row[22].equalsIgnoreCase(siteCode)){
				String gid = row[0];
				if(!failedGIds.contains(gid)){
					failedGIds.add(gid);
					}
			}
		}
		
	}

	private void writeFiles(ArrayList<String> failedGIds,ArrayList<String[]> fileData,ArrayList<String[]> fileDataCopy,CustomCSVWriter writerPass,CustomCSVWriter writerFailed) throws IOException{
		
		for(String faildGid: failedGIds){
			for(String[] row : fileData){
				if(row[0].equalsIgnoreCase(faildGid)){
					writerFailed.writeNext(row);
					fileDataCopy.remove(row);
				}
			}
		}
		writerFailed.close();
		for(String[] row : fileDataCopy){
				writerPass.writeNext(row);
		}
		writerPass.close();
		fileData = null;
		fileDataCopy = null;
		failedGIds = null;
	}
	
}