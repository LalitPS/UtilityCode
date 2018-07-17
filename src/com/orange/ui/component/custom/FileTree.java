package com.orange.ui.component.custom;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;

import com.orange.util.GenericJTree;

public class FileTree {

	public GenericJTree tree;

	public FileTree(File dir) {
		tree = new GenericJTree(addNodes(null, dir, dir.getAbsolutePath()));
		ToolTipManager.sharedInstance().registerComponent(tree);
	}

	/** Add nodes from under "dir" into curTop. Highly recursive. */
	private DefaultMutableTreeNode addNodes(DefaultMutableTreeNode curTop,
			File dir, String thisObject1) {
		String curPath = dir.getPath();

		DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(thisObject1);

		if (curTop != null) { // should only be null at root
			curTop.add(curDir);
		}
		List<String> ol = new ArrayList<String>();
		String[] tmp = dir.list();
		for (int i = 0; i < tmp.length; i++) 
		{
			ol.add(tmp[i]);
		}
		Collections.sort(ol, String.CASE_INSENSITIVE_ORDER);
		File f;
		List<String> files = new ArrayList<String>();

		// Make two passes, one for Dirs and one for Files. This is #1.
		for (int i = 0; i < ol.size(); i++) {
			String thisObject = (String) ol.get(i);
			String newPath;
			if (curPath.equals(".")) {
				newPath = thisObject;
			} else {
				newPath = curPath + File.separator + thisObject;
			}
			if ((f = new File(newPath)).isDirectory()) {
				addNodes(curDir, f, thisObject);
			} else {
				files.add(thisObject);
			}
		}
		// Pass two: for files.
		for (int fnum = 0; fnum < files.size(); fnum++) {
			curDir.add(new DefaultMutableTreeNode(files.get(fnum)));
		}
		return curDir;
	}

	public GenericJTree getTree() {
		return tree;
	}

	public void setTree(GenericJTree tree) {
		this.tree = tree;
	}

}
