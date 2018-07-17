package com.orange.util;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import com.orange.ui.component.SplitPanel;

public class SetInfoPanel {
	JTextArea textarea;
	JSplitPane vsplitPane;

	public SetInfoPanel(SplitPanel sp) {
		vsplitPane = sp.getImadaqVsplitPane();
		textarea = new JTextArea();
		vsplitPane.setBottomComponent(new JScrollPane(textarea));
	}

	public void clearLogs(){
		textarea.setText("");
	}
	public void setInfoLogs(String info) {
		textarea.append(info + "\n");
	}
}
