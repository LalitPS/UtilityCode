package com.orange.util.earrach;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.orange.ui.component.EarrachExtractionView;
import com.orange.ui.component.custom.Directories;
import com.orange.util.CommonUtils;
import com.orange.util.EarrachExtractQueryExecutor;

public class OfferDetailsActionRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	
	private JButton viewButton ;
	
	public OfferDetailsActionRenderer(final JTable table,final EarrachExtractionView earrachExtractViewComponents,final Dimension screenSize) throws SQLException, IOException{
		super();
		viewButton = new JButton("OFFER DETAILS") ;
		viewButton.setBackground(Color.ORANGE);
		viewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
            	int ROW = table.getSelectedRow();
            	String S  = (String)table.getModel().getValueAt(ROW,0);
            	
            
    				String quote = S.trim();
    				String PATH = Directories.goldEarrachSyncDataFilesLoc+File.separator+quote+File.separator;
    				new File(PATH).mkdirs();
    				EarrachExtractQueryExecutor reqe = new EarrachExtractQueryExecutor(quote,earrachExtractViewComponents,PATH,screenSize);
    				try {
						reqe.getBaseQueryExecution();
					
					} catch (Exception e) {
						CommonUtils.showExceptionStack(e);
					}
    				earrachExtractViewComponents.getQueryResult().append("\n File(s) Export successfully ..."+PATH);
    			
            	
            	
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
		
		
}