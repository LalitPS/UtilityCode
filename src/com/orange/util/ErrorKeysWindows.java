package com.orange.util;

import java.awt.Color;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.orange.ui.component.custom.CustomJCheckBox;
import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.CustomJPanel;
import com.orange.ui.component.custom.CustomRadioButton;

public class ErrorKeysWindows extends CustomJFrame
{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<ButtonGroup> buttonGroups;
	private ArrayList<CustomJCheckBox> checkBoxes;
	private LinkedHashMap<String, ArrayList<String[]>> errors;

	private Label message;
	private JButton ok,cancel;
	private JPanel panel;
	private ArrayList<CustomRadioButton> radios;

	public ErrorKeysWindows(LinkedHashMap<String, ArrayList<String[]>> errors){
		this.errors = errors;
		setLayout(null);
		createUIComponents();
	}
	
	private void createUIComponents(){
		panel = new CustomJPanel();
		panel.setLayout(null);
		
		checkBoxes = new ArrayList<CustomJCheckBox>();
		radios = new ArrayList<CustomRadioButton>();
		buttonGroups = new ArrayList<ButtonGroup>();
		ok = new JButton("Start");
		cancel = new JButton("Cancel");
		message = new Label("Please Select .. Selected Error Types will be moved  into _Failed file.\n This Process may take some time.");
		message.setBounds(5,5,600,15);
		message.setForeground(Color.blue);
		
		
		panel.add(message);
		initUIComponents();
	}
	
	public ArrayList<ButtonGroup> getButtonGroups() {
		return buttonGroups;
	}
	public ArrayList<CustomJCheckBox> getCheckBoxes() {
		return checkBoxes;
	}

	public LinkedHashMap<String, ArrayList<String[]>> getErrors() {
		return errors;
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

	public ArrayList<CustomRadioButton> getRadios() {
		return radios;
	}

	private void initUIComponents(){
		int x_axis = 10;
		int y_axis = 30;
		
		Iterator<Map.Entry<String, ArrayList<String[]>>> entries = errors.entrySet().iterator();
		while (entries.hasNext()) 
		{
			Map.Entry<String, ArrayList<String[]>> entry = entries.next();
			String key = entry.getKey();
			CustomJCheckBox checkbox_ = new CustomJCheckBox(key);
			CustomRadioButton radio_ = new CustomRadioButton("GID");
			CustomRadioButton radio_1 = new CustomRadioButton("SiteCode");
			
		
			checkbox_.setBounds(x_axis+40,y_axis,300,20);
			radio_.setBounds(x_axis+checkbox_.getWidth()+50,y_axis,50,20);
			radio_1.setBounds(x_axis+checkbox_.getWidth()+50+radio_.getWidth()+50,y_axis,80,20);
		
			panel.add(checkbox_);
			panel.add(radio_);
			panel.add(radio_1);
		
			
			//gid
			if(key.equalsIgnoreCase("GOLD_SITECODE_MISSING_FOR_AM_FALSE") || 
			   key.equalsIgnoreCase("GOLD_SITECODE_MISSING_FOR_AM_TRUE")){
				radio_.setSelected(true);
			}
			else{
				// sitecode
				radio_1.setSelected(true);
			}
			ButtonGroup buttonGroup = new ButtonGroup();
			
			checkBoxes.add(checkbox_);
			radios.add(radio_);
			radios.add(radio_1);
			
			
			buttonGroup.add(radio_);
			buttonGroup.add(radio_1);
		
			buttonGroups.add(buttonGroup);
		
			y_axis+=40;
		}
		ok.setBounds(x_axis,y_axis,150,40);
		cancel.setBounds(x_axis+ok.getWidth()+20,y_axis,150,40);
		cancel.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent arg0) 
	        {
	            setVisible(false);
	          }
	        });
		
		
		
		panel.add(ok);
		panel.add(cancel);
		
		add(panel);
	
	}

	
}
