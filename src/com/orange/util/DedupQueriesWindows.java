package com.orange.util;

import java.awt.Color;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.orange.ui.component.custom.CustomJCheckBox;
import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.CustomJPanel;

public class DedupQueriesWindows extends CustomJFrame
{

	private class SelectAllEvent implements ActionListener{
		
	    public SelectAllEvent(){
	    	
	    }
		public void actionPerformed(ActionEvent arg0) {
			
			Thread worker = new Thread() { public void run() {SwingUtilities.invokeLater(new Runnable() {public void run()
			{ 
				//  Start Here
					for(CustomJCheckBox checkBox :checkBoxes)
					{
						checkBox.setSelected(selectAll.isSelected());
					}
				
				// Ends Here
				
			} });}};
			worker.start();
			
			
		}
		
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<CustomJCheckBox> checkBoxes;
	
	private Label message;
	private JButton ok,cancel;
	private JPanel panel;
	private LinkedHashMap<String, Boolean> queryKeys;
	private CustomJCheckBox selectAll;
	
	public DedupQueriesWindows(LinkedHashMap<String, Boolean> queryKeys){
		this.queryKeys = queryKeys;
		setLayout(null);
		createUIComponents();
	}
	private void createUIComponents(){
		
		panel = new CustomJPanel();
		panel.setLayout(null);
		
		selectAll = new CustomJCheckBox("Select All");
		checkBoxes = new ArrayList<CustomJCheckBox>();
		ok = new JButton("Start");
		
		cancel = new JButton("Cancel");
		message = new Label("Please Select .. Selected Query will be executed.\n This Process will take some time.");
		message.setBounds(5,5,600,15);
		selectAll.setBounds(5,25,100,20);
		
		message.setForeground(Color.blue);
		
		panel.add(message);
		panel.add(selectAll);
		selectAll.addActionListener(new SelectAllEvent());
		
		initUIComponents();
	}
	
	public JButton getCancel() {
		return cancel;
	}
	public ArrayList<CustomJCheckBox> getCheckBoxes() {
		return checkBoxes;
	}

	public Label getMessage() {
		return message;
	}

	

	public JButton getOk() {
		return ok;
	}

	public JPanel getPanel() {
		return panel;
	}
	public LinkedHashMap<String, Boolean> getQueryKeys() {
		return queryKeys;
	}

	private void initUIComponents(){
		int x_axis = 10;
		int y_axis = 40;
		
		Iterator<Map.Entry<String, Boolean>> entries = queryKeys.entrySet().iterator();
		while (entries.hasNext()) 
		{
			Map.Entry<String, Boolean> entry = entries.next();
			String key = entry.getKey();
			CustomJCheckBox checkbox_ = new CustomJCheckBox(key);
			checkbox_.setSelected(entry.getValue());
			checkbox_.setBounds(x_axis+30,y_axis,400,20);
			panel.add(checkbox_);
			checkBoxes.add(checkbox_);
			y_axis+=20;
		}
		ok.setBounds(x_axis,y_axis,150,30);
		cancel.setBounds(x_axis+ok.getWidth()+20,y_axis,150,30);
		
		panel.add(ok);
		panel.add(cancel);
		add(panel);
		
	
	}

	
}
