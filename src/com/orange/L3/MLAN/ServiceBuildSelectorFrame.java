package com.orange.L3.MLAN;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import com.orange.ui.component.custom.CustomJCheckBox;
import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.CustomJPanel;
import com.orange.ui.component.custom.CustomRadioButton;
import com.orange.ui.component.custom.Icons;
import com.orange.util.CommonUtils;
import com.orange.util.ProgressMonitorPane;

public class ServiceBuildSelectorFrame {
	
	private ArrayList<CustomJCheckBox> checkBoxes;
	int current = 0;
	private CustomJFrame f;
	private CustomJCheckBox includeNotInOrders;
	ButtonGroup isINGroup ;
	private Label message; 
	private JButton ok,cancel;
	private CustomJCheckBox selectAll;
		

	private JPanel subPanel;
	
	private JTextArea tf;
	private CustomRadioButton withIN,withLIKE;
	
	
	public ServiceBuildSelectorFrame(String fLab,ArrayList<String[]> items,String[] querySelects,final Dimension screenSize,boolean isIncludeNotInActive){
		
		JPanel panel = new CustomJPanel();
		
		panel.setAlignmentX(Component.LEFT_ALIGNMENT );
		panel.setAlignmentY(Component.TOP_ALIGNMENT );
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JButton userInput = new JButton("User Text Input");
		userInput.setBackground(Color.ORANGE);
		
		includeNotInOrders = new CustomJCheckBox("INCLUDE NOT IN");
		includeNotInOrders.setToolTipText("Orders also will be included in Extraction, those not having the selected lineitems.");
		includeNotInOrders.setForeground(Color.RED);
		includeNotInOrders.setBackground(Color.YELLOW);
		
		selectAll = new CustomJCheckBox("Select All");
		selectAll.setForeground(Color.RED);
		
		checkBoxes = new ArrayList<CustomJCheckBox>();
		ok =     new JButton(">>>>>> NEXT >>>>>>");
		ok.setBackground(Color.ORANGE);
		cancel = new JButton("<<<<<< CLOSE >>>>>>");
		cancel.setBackground(Color.ORANGE);
		
		message = new Label("Please Select .. ");
		message.setForeground(Color.blue);
		
		userInput.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent arg0) {
	        	UserInput(screenSize);
	        }
	      });
		
		panel.add(userInput);
		panel.add(message);
		if(isIncludeNotInActive)
		{
		panel.add(includeNotInOrders);
		}
		panel.add(selectAll);
		selectAll.addActionListener(new SelectAllEvent());
		
	
		final JScrollPane spanel = initCheckBoxComponents(items,querySelects,panel);
		spanel.addMouseListener(new MouseAdapter() {
		      public void mouseReleased(MouseEvent e) {
		        if (e.isPopupTrigger()) {
		        	CommonUtils.setPanelPopup(spanel).show((JComponent) e.getSource(), e.getX(), e.getY());
		        }
		      }
		    });
		
	
		f = new CustomJFrame(fLab,Icons.iconPath4);
		f.setBounds(50,50,screenSize.width * 60 / 100,screenSize.height * 80 / 100);
		f.add(spanel);
		f.setVisible(true);
		f.setAlwaysOnTop( true );
	}

	
	private class CancelEvent implements ActionListener{

		
		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
		
		int option  = JOptionPane.showConfirmDialog(f,"Are you sure, want to close this window.?");	
		if(option == JOptionPane.YES_OPTION)
		{
			f.dispose();
		}
		}
		
	}
	
	private class SelectAllEvent implements ActionListener{
		
	    public SelectAllEvent(){}
		public void actionPerformed(ActionEvent arg0) {
			
			Thread worker = new Thread() { public void run() {SwingUtilities.invokeLater(new Runnable() {public void run()
			{ 
					for(CustomJCheckBox checkBox :checkBoxes)
					{
						checkBox.setSelected(selectAll.isSelected());
					}
			} });}};
			worker.start();
		}
	}
	
	
	public CustomJFrame getFrame(){
		return f;
	}
	
	public CustomJCheckBox getIncludeNotInOrders(){
		return includeNotInOrders;
	}
	public JButton getOkButton(){
		return ok;
	}
	public int getSelectedCount(){
		int selectedCount = 0;
		for(CustomJCheckBox cb : checkBoxes)
		{
			if(cb.isSelected())
			{
				selectedCount++;
			}
		}
		return selectedCount;
	}
	
	public String  getSelectedServiceInfo(){
		StringBuilder sb = new StringBuilder();
		
		for(CustomJCheckBox cb : checkBoxes)
		{
			if(cb.isSelected())
			{
				String cbText = CommonUtils.refineData(cb.getText());
				sb.append("'"+cbText+"',");
			}
		}
		
		String S =sb.substring(0, sb.length()-1);
		return S;
	}
	
	public String  getUserServiceInfo(){
		if(null != tf)
		{
			String S = CommonUtils._refineData(tf.getText());
			if(null != S && S.trim().length()>0){
				return S;
			}
		}
		
		return null;
	}
	
	public CustomRadioButton getWithIN() {
		return withIN;
	}
	public CustomRadioButton getWithLIKE() {
		return withLIKE;
	}
	
	private JScrollPane initCheckBoxComponents(ArrayList<String[]> items,String[] queryselects,JPanel panel)
	{
		for(String[] item : items)
		{
			current++;
			ProgressMonitorPane.getInstance().setProgress(current, items.size());
			CustomJCheckBox checkbox_ = new CustomJCheckBox(item[0]);
			if(null != queryselects)
			{
			    String tTip="<html>";
				for(int X = 0 ; X <queryselects.length;X++)
				{
					String Len = item[X];
					
					if(null != Len && Len.length() > 110)
					{
						Len = Len.substring(0,104)+"....";
					}
					tTip+=queryselects[X]+"  >>  <i>"+Len+"</i><br>";
				}
			tTip+="</html>";
			checkbox_.setToolTipText(tTip);
			
			
			final int defaultDismissTimeout = ToolTipManager.sharedInstance().getDismissDelay();

			checkbox_.addMouseListener(new MouseAdapter() {

			  public void mouseEntered(MouseEvent me) {
			    ToolTipManager.sharedInstance().setDismissDelay(60000);
			  }

			  public void mouseExited(MouseEvent me) {
			    ToolTipManager.sharedInstance().setDismissDelay(defaultDismissTimeout);
			  }
			});
			
			
			}
			panel.add(checkbox_);
			checkBoxes.add(checkbox_);
			
		}
	    cancel.addActionListener(new CancelEvent());
		panel.add(ok);
	
		panel.add(cancel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(panel);
		return scrollPane;
	}
	private void UserInput(Dimension screenSize){
		
		final CustomJFrame frame = new CustomJFrame("Precendence",Icons.iconPath4);
		
		withIN = new CustomRadioButton("WHERE IN",true);
		withIN.setToolTipText(" This include IN clause in query , EX where column IN 'Val1','Val2'");
		withLIKE = new CustomRadioButton("WHERE LIKE");
		/*@ LALIT 07-JUNE 2018
		 * LIKE OPTION HAS NEEN DISABLED FROM THE UTILITY
		 * THIS ACTION HAS BEEN TAKEN TO OPTIMZE THE QUERY EXECUTUION.
		 * BY ENABALING TRUE , LIKE WILL BE ACTIVE AGAIN.
		 */
		if(CommonUtils.isLikeDisabled){
		withLIKE.setEnabled(false);
		}
		else{
		withLIKE.setEnabled(true);	
		}
		
		withLIKE.setToolTipText(" This include LIKE clause in query , EX where column LIKE 'Val%'");
		isINGroup = new ButtonGroup();
		isINGroup.add(withIN);
		isINGroup.add(withLIKE);
		
		withIN.setBounds(screenSize.width * 3 / 100, 5,screenSize.width * 20 / 100,20);
		withLIKE.setBounds(screenSize.width * 3 / 100,25,screenSize.width * 20 / 100,20);
		
		final JLabel msg = new JLabel("Please Provide 'Precendence' Input Values. i.e. '<Value1>','<Value2>'.....");
		msg.setBounds(screenSize.width * 3 / 100,60,screenSize.width * 60 / 100,screenSize.height * 10 / 100);
		tf = new JTextArea();
		JScrollPane sp = new JScrollPane(tf);
		sp.setBounds(screenSize.width * 10 / 100,(20+screenSize.height * 15 / 100),screenSize.width * 30 / 100,screenSize.height * 10 / 100);
		
		JButton OK = new JButton("OK");
		OK.setBounds(screenSize.width * 10 / 100,(80+screenSize.height * 20 / 100),screenSize.width * 20 / 100,40);
		OK.setBackground(Color.ORANGE);
		OK.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent arg0) {
	        	frame.setVisible(false);
	        }
	      });
		
		withLIKE.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent arg0) {
	        	if(withLIKE.isSelected()){
	        		msg.setText("Please Provide 'Precendence' Input Values. i.e. '%Value1%'");
	        	}
	        }
	      });
		
		withIN.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent arg0) {
	        	if(withIN.isSelected()){
	        		msg.setText("Please Provide 'Precendence' Input Values. i.e. '<Value1>','<Value2>'.....");
	        	}
	        }
	      });
		
		subPanel = new CustomJPanel();
		subPanel.setLayout(null);
		subPanel.setSize(screenSize.width * 60 / 100,screenSize.height * 60 / 100);
		subPanel.add(withIN);
		subPanel.add(withLIKE);
		
		subPanel.add(msg);
		subPanel.add(sp);
		subPanel.add(OK);
		
		frame.setBounds(120,120,screenSize.width * 50 / 100,screenSize.height * 60 / 100);
		frame.add(subPanel);
		frame.setVisible(true);
		frame.setAlwaysOnTop( true );
	}
}
