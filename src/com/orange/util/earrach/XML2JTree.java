package com.orange.util.earrach;

//Import the W3C DOM clas ses
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.Icons;
import com.orange.util.CommonUtils;
import com.orange.util.GenericJTree;



/** 
Displays XML in a GenericJTree
*/

public class XML2JTree 
{
   private static    CustomJFrame   frame;
   public static final int FRAME_HEIGHT = 500;

   public static final int FRAME_WIDTH = 700;
   private  static GenericJTree    GenericJTree;
 
   public static void main( String[] args )
   {
	String path = "C:\\Lalit\\EARRACH\\EarrachUtility\\ONLY_OK_NOT_CLOSED_2017-06-28-09_54_1_8.json";
	try {
		new XML2JTree(path);
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		JOptionPane.showMessageDialog(null,e);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		JOptionPane.showMessageDialog(null,e);
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		JOptionPane.showMessageDialog(null,e);
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		JOptionPane.showMessageDialog(null,e);
	}
   }
   
   public XML2JTree(String jsonFileName) throws FileNotFoundException, IOException, ParseException, JSONException{
	   	  Document doc = null;
	   	  String filename = getTempXMLFileName(jsonFileName);
	   	
	   	  boolean showDetails = false;
	   
	      frame = new CustomJFrame("JSON GenericJTree For "+jsonFileName,Icons.iconPathEarrach);
	      frame.setBounds(200,150,FRAME_WIDTH,FRAME_HEIGHT);
	      try
	      {
	         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	         dbf.setValidating(false);  // Not important fro this demo

	         DocumentBuilder db = dbf.newDocumentBuilder();
	         doc = db.parse(filename);
	      }
	      catch( FileNotFoundException fnfEx )
	      {
	      
	         JOptionPane.showMessageDialog(frame, filename+" was not found","Warning", JOptionPane.WARNING_MESSAGE);
	      }
	      catch( Exception ex )
	      {
	         JOptionPane.showMessageDialog(frame, ex.getMessage(), "Exception",JOptionPane.WARNING_MESSAGE);
	      }
	    
	      Node root = (Node)doc.getDocumentElement();
	      frame.add(showXML2GenericJTree( root, showDetails ));
	      frame.setVisible(true);
	    
	   
   }
   protected DefaultMutableTreeNode createTreeNode( Node root, boolean showDetails )
   {
      DefaultMutableTreeNode dmtNode = null;

      String type = getNodeType(root);
      String name = root.getNodeName();
      String value = root.getNodeValue();

      if( showDetails )
      {
         dmtNode = new DefaultMutableTreeNode("["+type+"] --> "+name+" = "+value);
      }
      else
      {
         // Special case for TEXT_NODE, others are similar but not catered for here.
         dmtNode = new DefaultMutableTreeNode(root.getNodeType() == Node.TEXT_NODE ? value : name );
      }
     
      // Display the attributes if there are any
      NamedNodeMap attribs = root.getAttributes();
      if(attribs != null && showDetails )
      {
         for( int i = 0; i < attribs.getLength(); i++ )
         {
            Node attNode = attribs.item(i);
            String attName = attNode.getNodeName().trim();
            String attValue = attNode.getNodeValue().trim();

            if(attValue != null)
            {
               if (attValue.length() > 0)
               {
                  dmtNode.add(new DefaultMutableTreeNode("[Attribute] --> "+attName+"=\""+attValue+"\"") );
               }
            }
         }
      }

    
      if(root.hasChildNodes())
      {
         NodeList childNodes = root.getChildNodes();
         if(childNodes != null)
         {
            for (int k=0; k<childNodes.getLength(); k++)
            {
               Node nd = childNodes.item(k);
               if( nd != null )
               {
                 if( nd.getNodeType() == Node.ELEMENT_NODE )
                  {
                     dmtNode.add(createTreeNode(nd, showDetails));
                  }

                  // This is the default
                  String data = nd.getNodeValue();
                  if(data != null)
                  {
                     data = data.trim();
                     if(!data.equals("\n") && !data.equals("\r\n") &&
                        data.length() > 0)
                     {
                        dmtNode.add(createTreeNode(nd, showDetails));
                     }
                  }
               }
            }
         }
      }
      return dmtNode;
   }
  
   
  
   private String getNodeType( Node node )
   {
      String type;

      switch( node.getNodeType() )
      {
         case Node.ELEMENT_NODE:
         {
            type = "Element";
            break;
         }
         case Node.ATTRIBUTE_NODE:
         {
            type = "Attribute";
            break;
         }
         case Node.TEXT_NODE:
         {
            type = "Text";
            break;
         }
         case Node.CDATA_SECTION_NODE:
         {
            type = "CData section";
            break;
         }
         case Node.ENTITY_REFERENCE_NODE:
         {
            type = "Entity reference";
            break;
         }
         case Node.ENTITY_NODE:
         {
            type = "Entity";
            break;
         }
         case Node.PROCESSING_INSTRUCTION_NODE:
         {
            type = "Processing instruction";
            break;
         }
         case Node.COMMENT_NODE:
         {
            type = "Comment";
            break;
         }
         case Node.DOCUMENT_NODE:
         {
            type = "Document";
            break;
         }
         case Node.DOCUMENT_TYPE_NODE:
         {
            type = "Document type";
            break;
         }
         case Node.DOCUMENT_FRAGMENT_NODE:
         {
            type = "Document fragment";
            break;
         }
         case Node.NOTATION_NODE:
         {
            type = "Notation";
            break;
         }
         default:
         {
            type = "Unknown, contact Sun!";
            break;
         }
      }
      return type;
   }
  
   
   private String getTempXMLFileName(String jsonPath) throws FileNotFoundException, IOException, ParseException, JSONException{
		    String SS = readJSON(jsonPath);
			JSONObject o = new JSONObject(SS);
			String xmlData = org.json.XML.toString(o,"IOVs");
			Matcher junkMatcher = (Pattern.compile("^([\\W]+)<")).matcher(xmlData);
			xmlData = junkMatcher.replaceFirst("<");
			File temp = File.createTempFile("tempfile", ".xml");
			
    	    BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
	    	bw.write(xmlData);
	    	bw.close();
	    	return temp.getAbsolutePath();
	    
   }

   
   private String readJSON(String jsonFileName) throws JSONException, IOException{
   		
 	      FileInputStream inputStream = new FileInputStream(jsonFileName);
		  Scanner sc = new Scanner(inputStream);//, "UTF-8");
		  StringBuilder sb = new StringBuilder();
		   try{
		    while (sc.hasNextLine()) 
		    {
		        sb.append( sc.nextLine()+"\n");
		    }
		    
		   
		    inputStream.close();
		    sc.close();
		   }catch(OutOfMemoryError e){
			   sb = null;
			   inputStream.close();
			   sc.close();
			   JOptionPane.showMessageDialog(null,"Out Of Memory, Please Use smaller file instead.");
		   }
		    return sb.toString();
 }

  

  
    public JScrollPane showXML2GenericJTree( Node root, boolean showDetails )
	   {
		  
	      DefaultMutableTreeNode top = createTreeNode(root, showDetails );
	      DefaultTreeModel dtModel = new DefaultTreeModel(top);
	     
	      
	      GenericJTree = new GenericJTree(dtModel);
	      GenericJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	      GenericJTree.setShowsRootHandles(true);
	      GenericJTree.setEditable(false);
	      
	      GenericJTree.addMouseListener(new MouseAdapter() {
		      public void mouseReleased(MouseEvent e) {
		        if (e.isPopupTrigger()) {
		        	CommonUtils.getPopup(GenericJTree).show((JComponent) e.getSource(), e.getX(), e.getY());
		        }
		      }
		    });
	   
	      JScrollPane jScroll = new JScrollPane(GenericJTree);
	      return jScroll;
	   }
  
   
}
