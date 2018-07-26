package com.orange.util;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import com.orange.ui.component.SplitPanel;
import com.orange.ui.component.custom.FileTree;
import com.orange.ui.component.custom.FileViewer;

public class TreeFormation {
	static GenericJTree GenericJTree;
	public static String setTreeFormation(String path, SplitPanel sp) {
		FileTree tree = new FileTree(new File(path));
		final FileViewer viewer = new FileViewer();
		GenericJTree = tree.getTree();
		GenericJTree.addMouseListener(new MouseAdapter() {
		      public void mouseReleased(MouseEvent e) {
		        if (e.isPopupTrigger()) {
		        	CommonUtils.getPopup(GenericJTree).show((JComponent) e.getSource(), e.getX(), e.getY());
		        }
		      }
		    });
		
		
		final JSplitPane hsplitPane = sp.getImadaqHsplitPane();
		hsplitPane.setLeftComponent(new JScrollPane(GenericJTree));
		

		GenericJTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) e	.getPath().getLastPathComponent();
				if (null != GenericJTree.getSelectionPath()) {
					String path = GenericJTree.getSelectionPath().toString().trim().replace(", ", "\\");
					String path1 = path.replace("\\", "\\\\").replace("[", "").replace("]", "");
					if (new File(path1).isFile()) 
					{
						hsplitPane.setLeftComponent(new JScrollPane(GenericJTree));
						try {
							hsplitPane.setRightComponent(viewer.addTab(node.toString(), path1));
						} catch (IOException E) {
							CommonUtils.showExceptionStack(E);
						}
					}
				}
			}
		});
		return path;
	}
	
	public TreeFormation() {

	}

	
}
