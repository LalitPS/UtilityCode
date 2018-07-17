package com.orange.util.cibase.v02;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.orange.ui.component.Imadaqv02ViewComponent;
import com.orange.util.CommonUtils;
import com.orange.util.csm.OrderHierarchy;

public class USIDValidations {

      public USIDValidations()
      {
            
      }
      /*
      * -----------
      * CHECK 1
      * ----------
      * Order <> contains both the USIDs 
       * 
       */
      public boolean getUSIDValidation1(Map<String, ArrayList<String>> orderNUSIDMap,StringBuilder subDataTableBuilder,boolean isAnyFailed)
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
                        isAnyFailed = true;
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
            return isAnyFailed;
      }
      
      /*
      * --------------
      * CHECK 2
      * --------------
      * USID exists on different new order 
       * 
       */
      public boolean getUSIDValidation2(Map<String, ArrayList<String>> orderNTypeMap,StringBuilder subDataTableBuilder,boolean isAnyFailed)
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
                        isAnyFailed = true;
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
            return isAnyFailed;
      }
      /*
      * --------------
      * CHECK 3
      * --------------
      * Target USID is already present on different new order 
       * 
       */
      public boolean getUSIDValidation3(Map<String, ArrayList<String>> targetOrderNTypeMap,StringBuilder subDataTableBuilder,boolean isAnyFailed,LinkedHashSet<String> analysisQuotes,Imadaqv02ViewComponent imadaqv02ViewComponent)
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
                  * If more then one new order found then validation fire
                  * if          only one new order found then further check, to check are they with same chain.
                  */
                  
                  if(null != value && value.size()>1)
                  {
                        isAnyFailed = true;
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
                                          
                                          isAnyFailed = true;
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
            return isAnyFailed;
      }
}

