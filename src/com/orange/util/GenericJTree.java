package com.orange.util;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.orange.ui.component.custom.Icons;

public class GenericJTree extends JTree {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static DataFlavor TREE_PATH_FLAVOR = new DataFlavor(TreePath.class,"Tree Path");
	static {
		  UIManager.put("Tree.expandedIcon", new ImageIcon(CommonUtils.setSizeImage(Icons.treeExpandIcon,15,15)));
		  UIManager.put("Tree.collapsedIcon",new ImageIcon(CommonUtils.setSizeImage(Icons.treeIcon,15,15)));
		  UIManager.put("Tree.leafIcon",     new ImageIcon(CommonUtils.setSizeImage(Icons.leafIcon,15,15)));
		  UIManager.put("Tree.openIcon",     new ImageIcon(CommonUtils.setSizeImage(Icons.openLeafIcon,15,15)));
		  UIManager.put("Tree.closedIcon",   new ImageIcon(CommonUtils.setSizeImage(Icons.closeLeafIcon,15,15)));
	}

	//TreeDragSource ds;
   // TreeDropTarget dt;
	  
	public GenericJTree(DefaultTreeModel dtModel){
		  super(dtModel);
		 // setDragEnabled(true);
		  /*
		   * UNCOMMNET BELOW 2 LINES FOR DRAG AND DROP SUPPORT
		   * 
		   */
		  /*
		  ds = new TreeDragSource(this, DnDConstants.ACTION_COPY_OR_MOVE);
		  dt = new TreeDropTarget(this);
		  */
	}
	
	public GenericJTree(DefaultMutableTreeNode dtMutableModel){
		super(dtMutableModel);
		  /*
		   * UNCOMMNET BELOW 2 LINES FOR DRAG AND DROP SUPPORT
		   * 
		   */
		  /*
		 	ds = new TreeDragSource(this, DnDConstants.ACTION_COPY_OR_MOVE);
		 	dt = new TreeDropTarget(this);
		 */
  
	}
	
	class TreeDragSource implements DragSourceListener, DragGestureListener {

		  DragSource source;

		  DragGestureRecognizer recognizer;

		  TransferableTreeNode transferable;

		  DefaultMutableTreeNode oldNode;

		  JTree sourceTree;

		  public TreeDragSource(JTree tree, int actions) {
		    sourceTree = tree;
		    source = new DragSource();
		    recognizer = source.createDefaultDragGestureRecognizer(sourceTree,
		        actions, this);
		  }

		  /*
		   * Drag Gesture Handler
		   */
		  public void dragGestureRecognized(DragGestureEvent dge) {
		    TreePath path = sourceTree.getSelectionPath();
		    if ((path == null) || (path.getPathCount() <= 1)) {
		      // We can't move the root node or an empty selection
		      return;
		    }
		    oldNode = (DefaultMutableTreeNode) path.getLastPathComponent();
		    transferable = new TransferableTreeNode(path);
		    source.startDrag(dge, DragSource.DefaultMoveNoDrop, transferable, this);

		    // If you support dropping the node anywhere, you should probably
		    // start with a valid move cursor:
		    //source.startDrag(dge, DragSource.DefaultMoveDrop, transferable,
		    // this);
		  }

		  /*
		   * Drag Event Handlers
		   */
		  public void dragEnter(DragSourceDragEvent dsde) {
		  }

		  public void dragExit(DragSourceEvent dse) {
		  }

		  public void dragOver(DragSourceDragEvent dsde) {
		  }

		  public void dropActionChanged(DragSourceDragEvent dsde) {
		  
		  }

		  public void dragDropEnd(DragSourceDropEvent dsde) {
		    /*
		     * to support move or copy, we have to check which occurred:
		     */
		 
		    if (dsde.getDropSuccess()
		        && (dsde.getDropAction() == DnDConstants.ACTION_MOVE)) {
		      ((DefaultTreeModel) sourceTree.getModel())
		          .removeNodeFromParent(oldNode);
		    }

		    /*
		     * to support move only... if (dsde.getDropSuccess()) {
		     * ((DefaultTreeModel)sourceTree.getModel()).removeNodeFromParent(oldNode); }
		     */
		  }
		}

		//TreeDropTarget.java
		//A quick DropTarget that's looking for drops from draggable JTrees.
		//

		class TreeDropTarget implements DropTargetListener {

		  DropTarget target;

		  JTree targetTree;

		  public TreeDropTarget(JTree tree) {
		    targetTree = tree;
		    target = new DropTarget(targetTree, this);
		  }

		  /*
		   * Drop Event Handlers
		   */
		  private TreeNode getNodeForEvent(DropTargetDragEvent dtde) {
		    Point p = dtde.getLocation();
		    DropTargetContext dtc = dtde.getDropTargetContext();
		    JTree tree = (JTree) dtc.getComponent();
		    TreePath path = tree.getClosestPathForLocation(p.x, p.y);
		    return (TreeNode) path.getLastPathComponent();
		  }

		  public void dragEnter(DropTargetDragEvent dtde) {
		    TreeNode node = getNodeForEvent(dtde);
		    if (node.isLeaf()) {
		      dtde.rejectDrag();
		    } else {
		      // start by supporting move operations
		      //dtde.acceptDrag(DnDConstants.ACTION_MOVE);
		      dtde.acceptDrag(dtde.getDropAction());
		    }
		  }

		  public void dragOver(DropTargetDragEvent dtde) {
		    TreeNode node = getNodeForEvent(dtde);
		    if (node.isLeaf()) {
		      dtde.rejectDrag();
		    } else {
		      // start by supporting move operations
		      //dtde.acceptDrag(DnDConstants.ACTION_MOVE);
		      dtde.acceptDrag(dtde.getDropAction());
		    }
		  }

		  public void dragExit(DropTargetEvent dte) {
		  }

		  public void dropActionChanged(DropTargetDragEvent dtde) {
		  }

		  public void drop(DropTargetDropEvent dtde) {
		    Point pt = dtde.getLocation();
		    DropTargetContext dtc = dtde.getDropTargetContext();
		    JTree tree = (JTree) dtc.getComponent();
		    TreePath parentpath = tree.getClosestPathForLocation(pt.x, pt.y);
		    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) parentpath
		        .getLastPathComponent();
		    if (parent.isLeaf()) {
		      dtde.rejectDrop();
		      return;
		    }

		    try {
		      Transferable tr = dtde.getTransferable();
		      DataFlavor[] flavors = tr.getTransferDataFlavors();
		      for (int i = 0; i < flavors.length; i++) {
		        if (tr.isDataFlavorSupported(flavors[i])) {
		          dtde.acceptDrop(dtde.getDropAction());
		          TreePath p = (TreePath) tr.getTransferData(flavors[i]);
		          DefaultMutableTreeNode node = (DefaultMutableTreeNode) p
		              .getLastPathComponent();
		          DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		          model.insertNodeInto(node, parent, 0);
		          dtde.dropComplete(true);
		          return;
		        }
		      }
		      dtde.rejectDrop();
		    } catch (Exception e) {
		      e.printStackTrace();
		      dtde.rejectDrop();
		    }
		  }
		}

		//TransferableTreeNode.java
		//A Transferable TreePath to be used with Drag & Drop applications.
		//

		class TransferableTreeNode implements Transferable {

		 

		  DataFlavor flavors[] = { TREE_PATH_FLAVOR };

		  TreePath path;

		  public TransferableTreeNode(TreePath tp) {
		    path = tp;
		  }

		  public synchronized DataFlavor[] getTransferDataFlavors() {
		    return flavors;
		  }

		  public boolean isDataFlavorSupported(DataFlavor flavor) {
		    return (flavor.getRepresentationClass() == TreePath.class);
		  }

		  public synchronized Object getTransferData(DataFlavor flavor)
		      throws UnsupportedFlavorException, IOException {
		    if (isDataFlavorSupported(flavor)) {
		      return (Object) path;
		    } else {
		      throw new UnsupportedFlavorException(flavor);
		    }
		}

}
}
