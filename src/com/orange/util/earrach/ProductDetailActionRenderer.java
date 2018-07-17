package com.orange.util.earrach;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.Icons;
import com.orange.util.csm.CustomJTable;

public class ProductDetailActionRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	
	private JButton viewButton ;
	
	public ProductDetailActionRenderer(final JTable table,final HashMap<String,ArrayList<String[]>> hrefMap,final Dimension screenSize) {
		super();
		viewButton = new JButton("Product") ;
		viewButton.setBackground(Color.ORANGE);
		viewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
            	int ROW = table.getSelectedRow();
            	String S  = (String)table.getModel().getValueAt(ROW,0);
            	try {
					showSubTable(S,hrefMap,screenSize);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
            }
        });
		
		}
		@Override
		public Object getCellEditorValue() {
		return viewButton;
		}
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object arg1,boolean isSelected, int arg3, int arg4) {
		return viewButton;
		}
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected, boolean hasFocus, int row, int column) {
		return viewButton;
		}
		
		private void showSubTable(String offerNUmber,HashMap<String,ArrayList<String[]>> hrefMap,Dimension screenSize) throws IOException {
			String[] subTableColumnNames ={"PRODUCT_TYPE","HREF","ID","VERSION_ID"};
			DefaultTableModel model = new DefaultTableModel(0, 0);
			model.setColumnIdentifiers(subTableColumnNames);
			
				String KEY = offerNUmber;
				
				   if(hrefMap.containsKey(KEY))
				   {
				    ArrayList<String[]> herfValues = hrefMap.get(KEY);
				    
				    for(String[] herfValue : herfValues)
				    {
				    	 model.addRow(herfValue);
				    }
				  }  
			CustomJTable table = new CustomJTable();
			table.getTable().setModel(model);
			table.getTable().setAutoCreateRowSorter(true);
			JScrollPane scroll = new JScrollPane(table.getTable());
			CustomJFrame f = new CustomJFrame(KEY +">>Total Product ("+table.getTable().getRowCount()+")",Icons.iconPathEarrach);
			f.setBounds(20,20,screenSize.width * 60 / 100,screenSize.height * 60 / 100);
			f.add(scroll);
			f.setVisible(true);
			f.setAlwaysOnTop( true );
			
		}
		
}