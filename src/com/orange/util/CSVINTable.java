package com.orange.util;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.Icons;
import com.orange.util.csm.CustomJTable;

public class CSVINTable extends JButton{

	class ShowTable implements ActionListener{
		public void actionPerformed(ActionEvent e) {
		    try
		    {
			Object[] columnnames;
			CSVReader reader = new CSVReader(new FileReader(jtextField.getText().trim()));
			List<String[]> myEntries = reader.readAll();
			columnnames = (String[]) myEntries.get(0);
			DefaultTableModel tableModel = new DefaultTableModel(columnnames,myEntries.size() - 1);
			int rowcount = tableModel.getRowCount();
			for (int x = 0; x < rowcount + 1; x++) 
			{
				int columnnumber = 0;
				if (x > 0) 
				{
					for (String thiscellvalue : (String[]) myEntries.get(x)) 
					{
						tableModel.setValueAt(thiscellvalue, x - 1, columnnumber);
						columnnumber++;
					}
				}
			}
			reader.close();
			CustomJTable MyJTable = new CustomJTable();
			MyJTable.getTable().setModel(tableModel);
			MyJTable.getTable().setAutoCreateRowSorter(true);
			JScrollPane scroll = new JScrollPane(MyJTable.getTable());
			CustomJFrame f = new CustomJFrame("Total Rows ("+rowcount+")",Icons.iconPath1);
			f.setBounds(200, 150,200,400);
			f.add(scroll);
			f.setVisible(true);
			f.pack();
		    }catch(Exception ex){
		    	JOptionPane.showMessageDialog(new Frame(), "Unable to open, Please check format of the file. "+ex);
		    }
		}
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JTextField jtextField;

	public CSVINTable(String text,JTextField jtextField){
		super(text);
		this.jtextField = jtextField;
		addActionListener(new ShowTable());
	}
}
