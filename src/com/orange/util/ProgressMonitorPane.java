package com.orange.util;

import java.awt.Color;
import java.awt.Label;
import java.text.DecimalFormat;
import java.util.Properties;

import javax.swing.JPanel;

import com.orange.ui.component.custom.CustomJPanel;
import com.orange.ui.component.custom.Directories;

public class ProgressMonitorPane {
	 
	private volatile static ProgressMonitorPane instance = null;
	private static Object mutex = new Object();
	private JPanel panel;
	private Label progresslabel;
	
	public static ProgressMonitorPane getInstance() {
		ProgressMonitorPane result = instance;
    	if (result == null) {
			synchronized (mutex) {
				result = instance;
				if (result == null){
					instance = result = new ProgressMonitorPane();
					instance.init();
				}
			}
		}
		return result;
     }
	
	public ProgressMonitorPane()
	{
		
	}
	
	public JPanel getProgressPanel(){
		progresslabel.setText("##### No Progress / Process running.. #####");
		return panel;
	}
	
	protected void init(){	
		progresslabel = new Label("##### No Progress / Process running.. #####");
		progresslabel.setForeground(Color.BLUE);
		progresslabel.setBounds(10,20,300-20,20);
		GenericTitledBorder titled = new GenericTitledBorder("Progress");
		panel = new CustomJPanel();
		panel.setLayout(null);
		panel.setBorder(titled);
		panel.setSize(300,50);
		panel.add(progresslabel);
		Properties properties = CommonUtils.loadFTPConfigProp(Directories.customUserConfigFileLocationV03);
		if(properties.getProperty("ProgressMonitor").equalsIgnoreCase("YES"))
		{
			panel.setVisible(true);
		}
		else{
			panel.setVisible(false);
		}
		
	}
	
	public void setProgress(double current , double total)
	{
		if(current == 0)
		{
			current = 1;
		}
		if(total == 0)
		{
			total = 1;
		}
		
		double completed = (current*100)/total;
		progresslabel.setText(new DecimalFormat("#.##").format(completed) +" % Completed. ( "+new DecimalFormat("#").format(current)+" / "+new DecimalFormat("#").format(total)+" )");
	}
	
}
