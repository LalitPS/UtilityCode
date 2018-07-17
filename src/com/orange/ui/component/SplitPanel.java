package com.orange.ui.component;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.CustomJPanel;

public class SplitPanel {

	private JSplitPane dedupVsplitPane;

	private JSplitPane earrachVsplitPane;
	private CustomJFrame frame;
	

	private JSplitPane imadaqHsplitPane;
	private JPanel imadaqLowerPanel;
	private JSplitPane imadaqVsplitPane;
	private JSplitPane mLANVsplitPane;
	private JSplitPane queryExecutorVsplitPane;
	private JSplitPane ruleVsplitPane;
	private JSplitPane siteFixedVsplitPane;
	private JSplitPane siteMigrationVsplitPane;
	private JSplitPane imadaqV02VsplitPane;

	public SplitPanel() {

		frame = new CustomJFrame();
		
		imadaqLowerPanel = new CustomJPanel();
		imadaqVsplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		imadaqHsplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		dedupVsplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		ruleVsplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		earrachVsplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		siteFixedVsplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		siteMigrationVsplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		imadaqV02VsplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mLANVsplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		queryExecutorVsplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		imadaqVsplitPane.setTopComponent(imadaqHsplitPane);
		imadaqVsplitPane.setBottomComponent(imadaqLowerPanel);

		imadaqVsplitPane.setOneTouchExpandable(true);
		imadaqVsplitPane.setContinuousLayout(true);

		dedupVsplitPane.setOneTouchExpandable(true);
		dedupVsplitPane.setContinuousLayout(true);
		
		imadaqV02VsplitPane.setOneTouchExpandable(true);
		imadaqV02VsplitPane.setContinuousLayout(true);
		
		
		ruleVsplitPane.setOneTouchExpandable(true);
		ruleVsplitPane.setContinuousLayout(true);
		
		earrachVsplitPane.setOneTouchExpandable(true);
		earrachVsplitPane.setContinuousLayout(true);
		
		siteFixedVsplitPane.setOneTouchExpandable(true);
		siteFixedVsplitPane.setContinuousLayout(true);
		
		mLANVsplitPane.setOneTouchExpandable(true);
		mLANVsplitPane.setContinuousLayout(true);
		
		
		queryExecutorVsplitPane.setOneTouchExpandable(true);
		queryExecutorVsplitPane.setContinuousLayout(true);
		
		siteMigrationVsplitPane.setOneTouchExpandable(true);
		siteMigrationVsplitPane.setContinuousLayout(true);
		

		imadaqHsplitPane.setOneTouchExpandable(true);
		imadaqHsplitPane.setContinuousLayout(true);
		imadaqHsplitPane.setResizeWeight(0.25);
		
		frame.setDefaultCloseOperation(CustomJFrame.EXIT_ON_CLOSE);

	}

	public JSplitPane getDedupVsplitPane() {
		return dedupVsplitPane;
	}

	public JSplitPane getEarrachVsplitPane() {
		return earrachVsplitPane;
	}
	public CustomJFrame getFrame() {
		return frame;
	}
	
	
	public JSplitPane getImadaqHsplitPane() {
		return imadaqHsplitPane;
	}
	
	public JPanel getImadaqLowerPanel() {
		return imadaqLowerPanel;
	}
	
	public JSplitPane getImadaqVsplitPane() {
		return imadaqVsplitPane;
	}

	public JSplitPane getMLANVsplitPane() {
		return mLANVsplitPane;
	}
	
	public JSplitPane getQueryExecutorVsplitPane() {
		return queryExecutorVsplitPane;
	}
	
	public JSplitPane getImadaqV02VsplitPane() {
		return imadaqV02VsplitPane;
	}
	
	public JSplitPane getRuleVsplitPane() {
		return ruleVsplitPane;
	}
	
	public JSplitPane getSiteFixedVsplitPane() {
		return siteFixedVsplitPane;
	}

	public JSplitPane getSiteMigrationVsplitPane() {
		
		return siteMigrationVsplitPane;
	}

}
