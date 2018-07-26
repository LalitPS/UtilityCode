package com.orange.ui.component.custom;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import com.orange.util.CommonUtils;

public class ToolBar extends JToolBar{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JButton imadaq,dedupRun,siteFixed,mlan,rules,earrach,queryExecutor,imadaqV02;
	private JButton openWave,formatCSV,refresh,validate,leftICO,dataBypass,mergeMigrated,packageB,mergeErrorsImpeectedOrders,mergeErrorsMigratedOrders,ciBaseResultFilter;
	
	public ToolBar(){
		setRollover(true);
		String[] tollTip = {"IMADAQ","DEDUP RUN","MLAN/L2 Product Details",
				"Open Wave","Format CSV","Refresh","Validate","Left ICO","Data By Pass","Merge Migrated Files",
				"Package","Merge Erros (Impected Order Report)","Merge Errors(Migrated Order Reports)","Site Fixed",
				"Rules Extraction","EarracH","Query-Executor","IMADAQ_V02","CiBase Execution Result Filter"
				
		};
		
		imadaq = getToolBarButton(Icons.iconPath1,tollTip[0]);
		dedupRun= getToolBarButton(Icons.iconPath2,tollTip[1]);
		siteFixed= getToolBarButton(Icons.iconPath15,tollTip[13]);
		mlan= getToolBarButton(Icons.iconPath4,tollTip[2]);
		earrach= getToolBarButton(Icons.iconPathEarrach,tollTip[15]);
		rules= getToolBarButton(Icons.ruleIcon,tollTip[14]);
		queryExecutor= getToolBarButton(Icons.queryExecutorICON,tollTip[16]);
		queryExecutor.setVisible(false);
		imadaqV02 = getToolBarButton(Icons.imadaqV02Icon,tollTip[17]);
		
		addSeparator();
		addSeparator();
		
		openWave= getToolBarButton(Icons.iconPath5,tollTip[3]);
		formatCSV= getToolBarButton(Icons.iconPath6,tollTip[4]);
		refresh= getToolBarButton(Icons.iconPath7,tollTip[5]);
		validate= getToolBarButton(Icons.iconPath8,tollTip[6]);
		addSeparator();
		leftICO= getToolBarButton(Icons.iconPath9,tollTip[7]);
		dataBypass= getToolBarButton(Icons.iconPath10,tollTip[8]);
		addSeparator();
		addSeparator();
		mergeMigrated= getToolBarButton(Icons.iconPath11,tollTip[9]);
		packageB= getToolBarButton(Icons.iconPath12,tollTip[10]);
		mergeErrorsImpeectedOrders= getToolBarButton(Icons.iconPath13,tollTip[11]);
		mergeErrorsMigratedOrders= getToolBarButton(Icons.iconPath14,tollTip[12]);
		
		ciBaseResultFilter= getToolBarButton(Icons.ciBasedIcon,tollTip[18]);
	}
	
	public JButton getDataBypass() {
		return dataBypass;
	}

	public JButton getDedupRun() {
		return dedupRun;
	}

	public JButton getEarrach() {
		return earrach;
	}

	public JButton getFormatCSV() {
		return formatCSV;
	}
	
	public JButton getImadaq() {
		return imadaq;
	}
	
	
	public JButton getLeftICO() {
		return leftICO;
	}
	
	public JButton getMergeErrorsImpeectedOrders() {
		return mergeErrorsImpeectedOrders;
	}
	
	public JButton getMergeErrorsMigratedOrders() {
		return mergeErrorsMigratedOrders;
	}
	public JButton getCiBaseExecutionResultFilter() {
		return ciBaseResultFilter;
	}
	
	public JButton getMergeMigrated() {
		return mergeMigrated;
	}

	public JButton getMlan() {
		return mlan;
	}

	public JButton getOpenWave() {
		return openWave;
	}

	public JButton getPackageB() {
		return packageB;
	}

	public JButton getQueryExecutor() {
		return queryExecutor;
	}

	public JButton getRefresh() {
		return refresh;
	}

	public JButton getRules() {
		return rules;
	}

	public JButton getSiteFixed() {
		return siteFixed;
	}

	public JButton getImadaqv02() {
		return imadaqV02;
	}
	
	
	private JButton getToolBarButton(String iconPath,String toolTip){
		JButton toolBarButton =  new JButton(new ImageIcon(CommonUtils.setSizeImage(iconPath, 30, 30)));
		toolBarButton.setToolTipText(toolTip == null ? "":toolTip);
		add(toolBarButton);
		addSeparator();
		return toolBarButton;
	}

	public JButton getValidate() {
		return validate;
	}
	
    
    protected void paintComponent(Graphics g) {
    	 super.paintComponent(g);
    	Properties properties1 = CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV03);
        if( properties1.getProperty("Theme").equalsIgnoreCase("STANDARD") )
		{
	       
	        Graphics2D g2d = (Graphics2D) g;
	        g2d.setColor(CustomColorTheme.toolBarColor);
		    g2d.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
		}
    }
	
}
