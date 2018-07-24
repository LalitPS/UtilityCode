package com.orange.util.cibase.v02;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.orange.ui.component.Imadaqv02ViewComponent;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.ConnectionBeanArchived;
import com.orange.util.ConnectionBeanCSI;
import com.orange.util.csm.OrderHierarchy;

public class USIDValidations {

	  private boolean isAnyFailed = false;
	
	  
      public USIDValidations()
      {
    	   
      }
      
      
      public void setAnyFailed(boolean isAnyFailed) {
		this.isAnyFailed = isAnyFailed;
	}


	public boolean isAnyFailed() {
		return isAnyFailed;
	}


	/*
      * -----------
      * CHECK 1
      * ----------
      * Order <> contains both the USIDs 
       * 
       */
      public void getUSIDValidation1(Map<String, ArrayList<String>> orderNUSIDMap,StringBuilder subDataTableBuilder)
      {
            Iterator<Map.Entry<String, ArrayList<String>>> entries = orderNUSIDMap.entrySet().iterator();
            while (entries.hasNext()) 
            {
                  Map.Entry<String, ArrayList<String>> entry = entries.next();
                  String key = entry.getKey();
                  ArrayList<String> value = entry.getValue();
                  
                  Set<String> hs = new HashSet<String>();
                  hs.addAll(value);
                  value.clear();
                  value.addAll(hs);
                  
                  if(null != value && value.size()>1)
                  {
                	  	setAnyFailed(true);
                        String V ="";
                  
                        for(String str : value)
                        {
                              V+=str+",";
                        }
                        String S="Order "+key+" contains both the USIDs ("+V+")";
                        subDataTableBuilder.append("\n<TR><TD COLSPAN='100'><font color='red'>"+S+"</font></TD></TR>\n");
                        V = null;
                  }
            }
            
            entries = null;
            orderNUSIDMap = null;   
           
      }
      
      /*
      * --------------
      * CHECK 2
      * --------------
      * USID exists on different new order 
       * 
       */
      public void getUSIDValidation2(Map<String, ArrayList<String>> orderNTypeMap,StringBuilder subDataTableBuilder)
      {
            Iterator<Map.Entry<String, ArrayList<String>>> entries = orderNTypeMap.entrySet().iterator();
            while (entries.hasNext()) 
            {
                  Map.Entry<String, ArrayList<String>> entry = entries.next();
                  //String key = entry.getKey();
                  ArrayList<String> value = entry.getValue();
                  /*
                  * REMOVE DUPLICATE TO AVOID ERROR CASE WHEN ANY ORDER HAS BOTH USIDS AND ORDER TYPE IS NEW.
                  * SUCH TYPE OF CASES SHOULD NOT FALL IN THIS CATEGORY.
                  * SUCH CASES WILL FALL IN ORDER CONTAINS BOTH THE USIDS. 
                   */
                  Set<String> hs = new HashSet<String>();
                  hs.addAll(value);
                  value.clear();
                  value.addAll(hs);
                  
                  if(null != value && value.size()>1)
                  {
                       setAnyFailed(true);
                        String V ="";
                        for(String str : value)
                        {
                              V+=str+",";
                        }
                        String S="USIDs(Existing AND/OR Target) is available on different new Order(s) ("+V+")";
                        subDataTableBuilder.append("\n<TR><TD COLSPAN='100'><font color='red'>"+S+"</font></TD></TR>\n");
                        V = null;
                  }
            }
            entries = null;
            orderNTypeMap = null;   
           
      }
      /*
      * --------------
      * CHECK 3
      * --------------
      * Target USID is already present on different new order 
       * 
       */
      public void getUSIDValidation3(Map<String, ArrayList<String>> targetOrderNTypeMap,StringBuilder subDataTableBuilder,LinkedHashSet<String> analysisQuotes,Imadaqv02ViewComponent imadaqv02ViewComponent)
      {
            TreeSet<Integer> sortAnalysisQuotes = new TreeSet<Integer>();
            if(null !=analysisQuotes && analysisQuotes.size()>0)
            {
                  for(String Q : analysisQuotes){
                        sortAnalysisQuotes.add(Integer.parseInt(Q));
                  }
            }
            
            Iterator<Map.Entry<String, ArrayList<String>>> entries = targetOrderNTypeMap.entrySet().iterator();
            while (entries.hasNext()) 
            {
                  Map.Entry<String, ArrayList<String>> entry = entries.next();
                  
                  ArrayList<String> value = entry.getValue();
            
                  /*
                  * REMOVE DUPLICATE TO AVOID ERROR CASE WHEN ANY ORDER HAS BOTH USIDS AND ORDER TYPE IS NEW.
                  * SUCH TYPE OF CASES SHOULD NOT FALL IN THIS CATEGORY.
                  * SUCH CASES WILL FALL IN ORDER CONTAINS BOTH THE USIDS. 
                   */
                  Set<String> hs = new HashSet<String>();
                  hs.addAll(value);
                  value.clear();
                  value.addAll(hs);
                  /*
                  * If any new order found then validation fire (because this is for Target USID, so any one new order will fire this validation)
                  * if only one new order found then further check, to check are they with same chain.
                  */
                  
                  if(null != value && value.size()>1)
                  {
                       setAnyFailed(true);
                        String V ="";
                  
                        for(String str : value)
                        {
                              V+=str+",";
                        }
                        String S="Target USID is available on different new Orders. ("+V+")";
                        subDataTableBuilder.append("\n<TR><TD COLSPAN='100'><font color='red'>"+S+"</font></TD></TR>\n");
                        V = null;
                  }
                  /*
                   * If only one new order found.
                   */
                  else if(null != value && value.size() == 1)
                  {
                        /*
                        * get the list1 from analysis report
                        * get the list2 from Order Hierarchy
                        * 
                         * if all list1 element       available in list2 pass   (      part of same chain)
                        * if any list1 element not available in list2 failed (not part of same chain)
                        * 
                         */
                        
                        //Integer findHierarchyOrder = sortAnalysisQuotes.last();
                          String findHierarchyOrder = value.get(0);
                        try {
                              //OrderHierarchy OH = new OrderHierarchy(imadaqv02ViewComponent, biggestOrder.toString(),true,true);
                              OrderHierarchy OH = new OrderHierarchy(imadaqv02ViewComponent, findHierarchyOrder,true,true);
                              ArrayList<String[]> orderHierarchyFromDB= OH.getOrderHierarchy();
                              ArrayList<String> orderHierarchyFromDBFiller= new ArrayList<String>();
                              
                              for(String[] ARR : orderHierarchyFromDB)
                              {
                                    orderHierarchyFromDBFiller.add(ARR[2]);
                                
                              }
                              
                              
                              Iterator<Integer> itr = sortAnalysisQuotes.iterator();
                              ArrayList<Integer> orderNotMatch = new ArrayList<Integer>();
                                while(itr.hasNext())
                                {
                                    Integer orderFromAnalysisList = itr.next();
                                    
                                    if(!orderHierarchyFromDBFiller.contains(orderFromAnalysisList.toString()))
                                    {
                                          orderNotMatch.add(orderFromAnalysisList);
                                    }
                                }
                                
                                if(orderNotMatch.size()>0)
                                {
                                    
                                    String V ="";
                                          
                                          for(Integer str : orderNotMatch)
                                          {
                                                V+=str+",";
                                          }
                                          if(V.length()>55){
                                                V=V.substring(0,50)+"...";
                                          }
                                          
                                         setAnyFailed(true);
                                          String S="Target USID is available on new Order '"+findHierarchyOrder+"',following Orders are not in the chain ("+V+")<br><font color='black'>Updating this chain may lead errors.</font>";
                                          subDataTableBuilder.append("\n<TR><TD COLSPAN='100'><font color='red'>"+S+"</font></TD></TR>\n");
                                          V = null;
                                
                                }
                                else 
                                {
                                    String S="All orders are in same chain, with new order "+findHierarchyOrder+".";
                                    subDataTableBuilder.append("\n<TR><TD COLSPAN='100'><font color='green'>"+S+"</font></TD></TR>\n");
                                }
                                orderNotMatch = null;
                                orderHierarchyFromDB = null;
                               
                        } catch (Exception E) 
                        {
                              CommonUtils.printExceptionStack(E, imadaqv02ViewComponent);
                        }
                  }
            }
            entries = null;
            targetOrderNTypeMap = null;   
          
      }
      
      /*
       * 
       * 
       */
      public void getUSIDValidation4(LinkedHashSet<String> serviceData,StringBuilder subDataTableBuilder)
      {
    	  serviceData = syncService(serviceData);
          ArrayList<String> value = new ArrayList<String>();
          value.addAll(serviceData);
          
          /*
           * get the services details from CSI only for EU and TU
           * 
           */
          
          if(null != value && value.size()>1)
          {
               setAnyFailed(true);
                String V ="";
                for(String str : value)
                {
                      V+=str+",";
                }
                String S="Target USID already exists in CSI with another product. ("+V+")";
                subDataTableBuilder.append("\n<TR><TD COLSPAN='100'><font color='red'>"+S+"</font></TD></TR>\n");
                V = null;
          }
         serviceData= null;
         value = null;
        
      }
      /*
 	  * 
 	  * SERVICE CHECK :
 	  * 
 	  * SERVICE COMBINATIONS PASS CASES
 	  * IP/VPN & IP_VPN
 	  * 
 	  */
      public static LinkedHashSet<String> syncService(LinkedHashSet<String> servcie)
      {
     	 LinkedHashSet<String> syncServiceSet = new LinkedHashSet<String>();
     	 
     	 Iterator<String> itr = servcie.iterator();
          while(itr.hasNext())
          {
              String syncService = itr.next();
          	 if(null != syncService && syncService.length()>0)
 	    	 {
 	    		   if(syncService.equalsIgnoreCase("IP_VPN"))
 		    	   {
 		    		   syncService = syncService.replace("IP_VPN","IP/VPN");
 		    	   }
 	    	 }
          	syncServiceSet.add(syncService);
          }
     	 return syncServiceSet;
      }
      /*
       * if given order in this file is not available in the list of data extract of GOLD or ARCHIVE. 
       * below validation should appear.
       */
      public  void  isListedOrderAvailable(String quote,String existingUSID,Imadaqv02ViewComponent imadaqv02ViewComponent,StringBuilder subDataTableBuilder) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException
      {
    	 
    	  boolean isOrderAvailabe = false;
    	 
    	 
    	 String GOLDLQL="SELECT DISTINCT QUOTE.QUOTENUMBER FROM "+
            ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE   ,"+
            ConnectionBean.getDbPrefix()+"SC_QUOTE_LINE_ITEM ATTR_LINEITEM " +
            " WHERE"+  
            " QUOTE.TRIL_GID = ATTR_LINEITEM.QUOTE  "+
            " AND (ATTR_LINEITEM.NEW_CONFIG     = ? " +
            " OR  ATTR_LINEITEM.EXIST_CONFIG    = ? "+
            " OR  ATTR_LINEITEM.VALUE                 = ? )"+
            " AND QUOTE.QUOTENUMBER =?";
          
    	 
    	  String ARCHIVAL_LQL ="SELECT DISTINCT LINEITEM.ORDERNUMBER FROM " +
                  ConnectionBeanArchived.getDbPrefix()+"SC_QUOTE_LINE_ITEM_A LINEITEM " +
                  "WHERE " +
                  " (LINEITEM.NEW_CONFIG        = ? " +
                  " OR  LINEITEM.EXIST_CONFIG = ? "+
                  " OR  LINEITEM.VALUE          = ? )" +
                  " AND LINEITEM. ORDERNUMBER=?";
    	  
    	  ArrayList<String[]> goldExisitingDetails  		   =CommonUtils.getQueryResult(GOLDLQL,imadaqv02ViewComponent,existingUSID,existingUSID,existingUSID,quote);
    	  
    	  if(null != goldExisitingDetails && goldExisitingDetails.size()>0)
    	  {
    		  isOrderAvailabe = true;
    	  }
    	  else
    	  {
    		  ArrayList<String[]> archiveExisitingDetails  		=CommonUtils.getArchiveQueryResult(ARCHIVAL_LQL,imadaqv02ViewComponent,existingUSID,existingUSID,existingUSID,quote); 
    	     if(null != archiveExisitingDetails && archiveExisitingDetails.size()>0)
    	     {
	    	      isOrderAvailabe = true;
		     }
    	  }
    	  
    	  if(!isOrderAvailabe)
    	  {
    		  setAnyFailed(true);
    		  String S="Order "+quote+" not found in GOLD/Archival.";
              subDataTableBuilder.append("\n<TR><TD COLSPAN='100'><font color='red'>"+S+"</font></TD></TR>\n");
    	  }
    	
      }
      
      public  void isSameServiceElementClass(String existingUSID,String newUSID,Imadaqv02ViewComponent imadaqv02ViewComponent,StringBuilder subDataTableBuilder) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException
      {
    	  String serviceElementClassCheck_LQL = "SELECT A.V_ORDHANDLE,A.SE_SERVICEELEMENTCLASS, A.VSE_VERSIONSERVICEELEMENTID,A.I_LUPDDATE "
    				+ "FROM ("
    				+ "SELECT V.V_ORDHANDLE,V.SE_SERVICEELEMENTCLASS, V.VSE_VERSIONSERVICEELEMENTID, I_LUPDDATE  FROM  "+ConnectionBeanCSI.getDbPrefix()+"VIW_VERSION_ELEMENT_ATTRIBUTE V WHERE SE_USID = ? "
    				+ "UNION "
    				+ "SELECT V.V_ORDHANDLE,V.SE_SERVICEELEMENTCLASS, V.VSE_VERSIONSERVICEELEMENTID, I_LUPDDATE "
    				+ "FROM "+ConnectionBeanCSI.getDbPrefix()+"VIW_VERSION_ELEMENT_ATTRIBUTE V WHERE T_VALUE= ?) "
    				+ "A ORDER BY A.I_LUPDDATE DESC";
    	  
    	  ArrayList<String[]> goldExisitingUSIDDetails  		    =CommonUtils.getCSIQueryResult(serviceElementClassCheck_LQL,imadaqv02ViewComponent,existingUSID,existingUSID);
    	  ArrayList<String[]> goldNewUSIDDetails  		   			=CommonUtils.getCSIQueryResult(serviceElementClassCheck_LQL,imadaqv02ViewComponent,newUSID,newUSID);
    	  
    	  if(null !=goldExisitingUSIDDetails && goldExisitingUSIDDetails.size()>0 && null != goldNewUSIDDetails && goldNewUSIDDetails.size()>0)
    	  {
    		  
    	      if(!goldExisitingUSIDDetails.get(0)[1].equals(goldNewUSIDDetails.get(0)[1]))
    	      {
    	    	  
    	    	  /*
    	    	   * If quote is not commercial migration then error else pass.
    	    	   */
    	    	  
    	    	  // add all quote number and work only change quotes.
    	    	  ArrayList<String> orders = new ArrayList<String>();
    	    	  for(String[] arr: goldExisitingUSIDDetails)
    	    	  {
    	    			 orders.add(arr[0]);
    	    	  }
    	    	  for(String[] arr: goldNewUSIDDetails)
    	    	  {
    	    			 orders.add(arr[0]);
    	    	  }
    	    	  
    	    	  // filter the list and remove duplicates if any. 
    	    	    Set<String> hs = new HashSet<String>();
                    hs.addAll(orders);
                    orders.clear();
                    orders.addAll(hs);
                    
                        // now check is order change type is Commercial Migration?
    	    	    	String checkChangeType=" SELECT  "+CommonUtils.EQ_TYPE_DECODE+" QUOTE.EQ_ORDERTYPE  FROM "+
                        ConnectionBean.getDbPrefix()+"SC_QUOTE QUOTE ,"+
                        ConnectionBean.getDbPrefix()+"SC_HIERARCHY HR LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN "+ConnectionBean.getDbPrefix()+"EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID) "+
                  		  " WHERE  QUOTE.CONFIGURATIONS  = HR.TRIL_GID AND QUOTE.QUOTENUMBER = ? AND QUOTE.EQ_ORDERTYPE='2' ";
                  	
	    	    	  for(String order : orders)
	    	    	  {
	    	    		  if(null != order)
	    	    		  {
	    	    			  
	    	    			  ArrayList<String[]> quoteChangeType  		=CommonUtils.getQueryResult(checkChangeType,imadaqv02ViewComponent,CommonUtils.addZeroPrefixinOrder(order));
		    	    	       /*
		    	    	        * IF QUOTE IS CHANGE >> COMMERCIAL MIGRATION
		    	    	        */
			    	    	    if(null != quoteChangeType && quoteChangeType.size()>0 && quoteChangeType.get(0)[0].equals("Commercial Migration")  
			    	    	    		)
			    	    	    {
			    	    	    // nothing to do , 
			    	    	    }
			    	    	    else
			    	    	    {
			    	    	    	   /*
			    	    	    	    * show the validation error 
			    	    	    	    */
			    	    	    	    String chgType="NA";
			    	    	    	    if(null != quoteChangeType && quoteChangeType.size()>0 ){
			    	    	    	    	if(quoteChangeType.get(0)[1].equals("2") )
			    	    	    	    	{
			    	    	    	    	chgType =  quoteChangeType.get(0)[0];
			    	    	    	    	}
			    	    	    	    }
			    	    	    		setAnyFailed(true);
			    	    	    	    String S="The Target USID ServiceElement belongs to different Service element categories. <FONT COLOR='BLUE'> Existing :'"+goldExisitingUSIDDetails.get(0)[1] +"' & Target :'"+goldNewUSIDDetails.get(0)[1]+"'</font>";
			    	                    S+="<BR> (Quote '"+order+"' is  not a Commercial Migration change type and its found '"+chgType+"')";
			    	    	    	    subDataTableBuilder.append("\n<TR><TD COLSPAN='100'><font color='red'>"+S+"</font></TD></TR>\n");
			    	    	    }
	    	    		  }
	    	    	      
	    	    	  }// end of for
    	    	  }// end of if
    	  
    	  }// end of main if
    	  
      }// end of method
      
}

