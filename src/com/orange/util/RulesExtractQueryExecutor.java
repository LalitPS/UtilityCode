package com.orange.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JScrollPane;

import com.orange.ui.component.RulesExtractView;
import com.orange.ui.component.custom.CustomJFrame;
import com.orange.ui.component.custom.Icons;

public class RulesExtractQueryExecutor {
	
	private Map<String,ArrayList<ArrayList<String>>> emailRules;
	private Map<String,ArrayList<ArrayList<String>>> teamReplacementRules;
	private Map<String,ArrayList<ArrayList<String>>> proxyemailRules;
	private Map<String,ArrayList<ArrayList<String>>> proxyteamReplacementRules;
	
	private String tableHeader="";
	private Map<String,ArrayList<String[]>> map;
	private String QUERY ="";
	private RulesExtractView rulesExtractViewComponents;
	
	private String CSS ="<head><script type='text/javascript'>function altRows(id){	if(document.getElementsByTagName){ var table = document.getElementById(id);  var rows = table.getElementsByTagName('tr'); for(i = 0; i < rows.length; i++){     if(i % 2 == 0){			rows[i].className = 'evenrowcolor';		}else{				rows[i].className = 'oddrowcolor';			}      		}	}"+
	"}window.onload=function(){	altRows('alternatecolor');}</script><!-- CSS goes in the document HEAD or added to your external stylesheet --><style type='text/css'>table.altrowstable {	font-family: verdana,arial,sans-serif;	font-size:11px;	color:#33333;	border-width: 1px;	border-color: #a9c6c9;	border-collapse: collapse;}table.altrowstable th {	border-width: 1px;	padding: 8px;	border-style: solid;	border-color: #a9c6c9;}table.altrowstable td {	border-width: 1px;	padding: 8px;	border-style: solid;	border-color:#a9c6c9;}.oddrowcolor{	background-color:#d4e3e5;}.evenrowcolor{	background-color:#c3dde0;}</style></head>";
	
	public static String[] columns =
			{			
			  "EQRule.name","EQRule.productCriteria","EQRule.customerCriteria","EQRule.SalesRegionCriteria","EQRule.SalesClusterCriteria","EQRule.SalesCluster2Criteria","EQRule.SalesCountryCriteria","EQRule.OrderTypeCriteria","EQRule.SiteCountryCriteria",
			  "EQTeamReplacementRule.DOOReplacement","EQTeamReplacementRule.DOCReplacement","EQTeamReplacementRule.DAReplacement","EQTeamReplacementRule.BAReplacement","EQTeamReplacementRule.TSReplacement","EQTeamReplacementRule.DTVSReplacement",
			  "EQEmailNotifRule.UserEmailNotification","EQEmailNotifRule.RoleEmailNotification"	
			};
	
	public String[] filecolumns =
			{			
			  "EQRule.name","EQRule.productCriteria","EQRule.customerCriteria","EQRule.SalesRegionCriteria","EQRule.SalesClusterCriteria","EQRule.SalesCluster2Criteria","EQRule.SalesCountryCriteria","EQRule.OrderTypeCriteria","EQRule.SiteCountryCriteria",
			  "EQTeamReplacementRule.DOOReplacement","EQTeamReplacementRule.DOCReplacement","EQTeamReplacementRule.DAReplacement","EQTeamReplacementRule.BAReplacement","EQTeamReplacementRule.TSReplacement","EQTeamReplacementRule.DTVSReplacement",
			  "EQEmailNotifRule.UserEmailNotification","EQEmailNotifRule.RoleEmailNotification","EMAIL_RULE","TEAM_REPLACEMENT_RULE"	
			};
	
	
	
	
	public RulesExtractQueryExecutor(RulesExtractView rulesExtractViewComponents)
	{
		this.rulesExtractViewComponents = rulesExtractViewComponents;
		
		emailRules = new LinkedHashMap<String,ArrayList<ArrayList<String>>>();
		teamReplacementRules = new LinkedHashMap<String,ArrayList<ArrayList<String>>>();
		
		
		String COLS="";
		for(String S :columns)
		{
			COLS+=S+",";
		}
		COLS = COLS.substring(0,COLS.length()-1);
		
		QUERY = "SELECT "+COLS+
		", CASE "+
		"WHEN EQEmailNotifRule.tril_gid  = EQRule.TRIL_GID "+
		"THEN 'YES' "+
		"ELSE 'NO' "+
		"END AS Email_Rule,"+
		"CASE "+
		"WHEN EQTeamReplacementRule.tril_gid  = EQRule.TRIL_GID "+
		"THEN 'YES' "+
		"ELSE 'NO' "+
		"END AS Team_Replacement_Rule "+
		" FROM " +
		ConnectionBean.getDbPrefix()+"EQRule EQRule "+
		"left join "+ConnectionBean.getDbPrefix()+"EQEmailNotifRule EQEmailNotifRule on (EQEmailNotifRule.tril_gid  = EQRule.TRIL_GID) "+
		"left join "+ConnectionBean.getDbPrefix()+"EQTeamReplacementRule EQTeamReplacementRule on (EQTeamReplacementRule.tril_gid = EQRule.TRIL_GID) ";
		
		map = new LinkedHashMap<String,ArrayList<String[]>>();
	}
	
	private void addValueinMap(String[] arr){
		String KEY = arr[0];
		if(rulesExtractViewComponents.getSelectCriteria().getSelectedIndex() >0)
		{
			KEY = arr[rulesExtractViewComponents.getSelectCriteria().getSelectedIndex()-1];
		}
		
		
		if(map.containsKey(KEY))
		{
			ArrayList<String[]> existingValues = map.get(KEY);
			int existingValuesLen = existingValues.size();
			for(int X = 0 ; X <existingValuesLen ; X++)
			{
				
				String[] exRow = existingValues.get(X);
				String[] newarrRow = new String[exRow.length];
			
				for(int Y1 = 0 ; Y1 < exRow.length;Y1++)
					{
					    boolean isExists = false;
					    String newVal =  arr[Y1];
					  
						for(int X1 = 0 ; X1< existingValuesLen;X1++)
						{
							
							String privVal = existingValues.get(X1)[Y1];
							if(null == newVal || null == privVal || privVal.equalsIgnoreCase(newVal))
							{
								isExists = true;
								break;
							}
						}	
						if(isExists)
						{
							newarrRow[Y1] = "EXISTS";
						}
						else
						{
							newarrRow[Y1] = arr[Y1];
						
					    }
				  }
				existingValues.add(newarrRow);	
				break;
			}
			map.put(KEY, existingValues);
			
			
		}
		else{
			ArrayList<String[]> newValues = new ArrayList<String[]>();
			newValues.add(arr);
			map.put(KEY, newValues);
		}
		
	}
	
	public void getBaseQueryExecution(String resultFileLoc,String resultHTMLFileLoc) throws Exception{
		
		if(rulesExtractViewComponents.getSelectedIndex() != 0 )
		{
			QUERY+= " WHERE " +rulesExtractViewComponents.getSelectCriteria().getSelectedItem()+" IN ("+rulesExtractViewComponents.getFileToValidate().getText().trim()+")";
		}
		
		ArrayList<String[]> result = CommonUtils.getQueryResult(QUERY, rulesExtractViewComponents);
		int resultsize = result.size();
		double count = 0.0; 
		for(String[] arr : result)
	    {
			count++;	
			ProgressMonitorPane.getInstance().setProgress(count,resultsize);	
			addValueinMap(arr);
	    }
		
		ArrayList<String[]> newList = new ArrayList<String[]>();
		
		Iterator<Map.Entry<String,ArrayList<String[]>>> keys = map.entrySet().iterator();
		int mapsize = map.size();
		double count1 = 0.0; 
		while(keys.hasNext())
		{
			
			count1++;	
			ProgressMonitorPane.getInstance().setProgress(count1,mapsize);	
			
			Map.Entry<String,ArrayList<String[]>> entry = keys.next();
			
			ArrayList<String[]> value = entry.getValue();
			
		
			String[] ARR = new String[value.get(0).length];
			
			for(int C = 0 ; C<ARR.length ; C++)
			{
				for(String[] val : value)
				{
					if(null != val[C] && !val[C].equals("EXISTS"))
					{
						
							ARR[C] = (null == ARR[C] ? val[C] :  ARR[C] +" || "+val[C]);
					}
				}
				
			}
			newList.add(ARR);
		}
		CommonUtils.showTable(filecolumns, newList,resultFileLoc);
		
		mappedRules(newList);
		
		String instructions = "<font face='verdana' color='green' size='1'>PLEASE NOTE TO IDENTIFY CONFLICTS.<br>IF NO COLUMN IS BLANK , AND OTHERS COLUMNS ARE 'ALL'<br>IF NO COLUMN IS BLANK , AND ONE COLUMN HAVING RULE(S) NAME AND OTHERS ARE ALL.<br>IF NO COLUMN IS BLANK , AND MULTIPLE COLUMN HAVING RULE(S) NAME AND REST ARE ALL, IF ANY RULE(S) IS COMMON IN OTHERS NON-ALL COLUMNS.</font>";
		
		
		String htmlHeader = "<HTML>"+CSS+"\n<BODY>\n"+instructions+"\n<TABLE class='altrowstable' id='alternatecolor'>\n<TR>\n";
		htmlHeader+=tableHeader+"<TH>CONFLICTS</TH></TR>\n";
		
		StringBuilder builder = new StringBuilder(htmlHeader);
		
		builder = validateEmailRules(builder);
		builder = validateTeamReplacementRules(builder);
		builder.append("</TABLE>\n</BODY>\n</HTML>\n");
		
		File file = new File(resultHTMLFileLoc);
		BufferedWriter writer = null;
		try {
		    writer = new BufferedWriter(new FileWriter(file));
		    writer.write(builder.toString());
		} finally {
		    if (writer != null) writer.close();
		}
		
		JScrollPane scroll = CommonUtils.getEditorPane(builder.toString());
		CustomJFrame f = new CustomJFrame(resultHTMLFileLoc+"  >>> ANALYSIS REPORT " );
		f.setIconImage(CommonUtils.setSizeImage(Icons.helpIcon, 35, 35));
		f.setBounds(200,200,800,500);
		f.add(scroll);
		f.setVisible(true);
	
		
		builder = null;
		rulesExtractViewComponents.getQueryResult().append("\nBest View on FireFox Browser.");
		rulesExtractViewComponents.getQueryResult().append("\n------------------------------.");
		rulesExtractViewComponents.getQueryResult().append("\nPLEASE NOTE TO IDENTIFY CONFLICTS.");
		rulesExtractViewComponents.getQueryResult().append("\nIF NO COLUMN IS BLANK , AND OTHERS COLUMNS ARE 'ALL'");
		rulesExtractViewComponents.getQueryResult().append("\nIF NO COLUMN IS BLANK , AND ONE COLUMN HAVING RULE(S) NAME AND OTHERS ARE ALL.");
		rulesExtractViewComponents.getQueryResult().append("\nIF NO COLUMN IS BLANK , AND MULTIPLE COLUMN HAVING RULE(S) NAME AND REST ARE ALL , AND IF ANY RULE IS COMMON IN OTHERS NON-ALL COLUMNS.");
		rulesExtractViewComponents.getQueryResult().append("\n------------------------------.");
		rulesExtractViewComponents.getQueryResult().append("\nAnalysis File..............."+resultHTMLFileLoc+" Created Successfully.");
		
	}
	/*
	Map<String,ArrayList<ArrayList<String>>> emailRules;
	Map<String,ArrayList<ArrayList<String>>> teamReplacementRules;
	*/
	/*
	 * SEPERATE EMAIL_RULES AND TEAM REPLACEMENT RULES
	 * NOTE : ASSUME EMAIL RULES HAS NO DUPLICACY
	 * 
	 */
	/*
	1. EQRule.name	
	2. EQRule.productCriteria	
	3. EQRule.customerCriteria	
	4. EQRule.SalesRegionCriteria	
	5. EQRule.SalesClusterCriteria	
	6. EQRule.SalesCluster2Criteria	
	7. EQRule.SalesCountryCriteria	
	8. EQRule.OrderTypeCriteria	
	9. EQRule.SiteCountryCriteria	
	10. EQTeamReplacementRule.DOOReplacement	
	11. EQTeamReplacementRule.DOCReplacement	
	12. EQTeamReplacementRule.DAReplacement	
	13. EQTeamReplacementRule.BAReplacement	
	14. EQTeamReplacementRule.TSReplacement	
	15. EQTeamReplacementRule.DTVSReplacement	
	16. EQEmailNotifRule.UserEmailNotification	
	17. EQEmailNotifRule.RoleEmailNotification	
	18. EMAIL RULE
	19. TEAM REPLACEMENT RULE	
	*/

	private void mappedRules(ArrayList<String[]> newList){
		
		rulesExtractViewComponents.getQueryResult().append("\nInitializing Rules Comparision Maps..");
		
		tableHeader="<TH>RULE TYPE</TH><TH>RULE</TH>\n";
		
		for(int X = 1 ; X< 9 ; X++)
		{
			tableHeader+="<TH>"+columns[X]+"</TH>\n";	
		}
		
		for(String[] ARR : newList)
		{   
			String ruleName = ARR[0];
			/*
			 * EMAIL RULE
			 */
			if(ARR[17].equalsIgnoreCase("YES"))
			{
				ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
				for(int X = 1 ; X< 9 ; X++)
				{
				ArrayList<String> value = new ArrayList<String>();
				value.addAll(convertTextToListData(ARR[X]));
				values.add(value);
				}
				emailRules.put(ruleName, values);
				
				
			}
			/*
			 * TEAM REPLACEMENT RULE
			 */
			else if(ARR[18].equalsIgnoreCase("YES"))
			{
				ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
				for(int X = 1 ; X< 9 ; X++)
				{
				ArrayList<String> value = new ArrayList<String>();
				value.addAll(convertTextToListData(ARR[X]));
				values.add(value);
				}
				teamReplacementRules.put(ruleName, values);
			}
		}
		
		rulesExtractViewComponents.getQueryResult().append("\nInitializing Rules Comparision Maps completed successfully..\n");
		
	}
	
	private ArrayList<String> convertTextToListData(String commatext)
	{
		ArrayList<String> list = new ArrayList<String>();
		if(null != commatext && commatext.length()>0)
		{
		
			String parts[] = commatext.trim().split(",");
			for(String S : parts)
			{
				list.add(S);
			}
		
		}
		return list;
		
	}
	
	private StringBuilder validateEmailRules(StringBuilder builder) throws SQLException
	{
		
		proxyemailRules = new LinkedHashMap<String,ArrayList<ArrayList<String>>>(emailRules);
		Iterator<Map.Entry<String, ArrayList<ArrayList<String>>>> emailRulesEntries = emailRules.entrySet().iterator();
		
		
		while (emailRulesEntries.hasNext()) 
		{
			Map.Entry<String, ArrayList<ArrayList<String>>> entry = emailRulesEntries.next();
			String KEY = entry.getKey();
			
			ArrayList<ArrayList<String>> ROW = new ArrayList<ArrayList<String>>();
			
			ArrayList<ArrayList<String>>  value = entry.getValue();
			
			builder.append("<TR>\n<TD>E-Mail Rule</TD>\n<TD>"+KEY+"</TD>\n");
			
		    
			for(int x = 0 ; x< value.size();x++)
			{
				ArrayList<String> col = value.get(x);
				
				if(null == col || col.isEmpty() || col.size() ==0)
				{
					builder.append("<TD>ALL</TD>\n");
					ArrayList<String> COL = new ArrayList<String>();
					COL.add("ALL");
					ROW.add(COL);
				}
				
				/*
				 * this column value should be check with all keys of proxy map 
				 * same column value
				 * 
				 * 1. column value is empty , it mean applicable for all.
				 * 2. 
				 * 
				 * 
				 */
				else
				{   
					builder.append("<TD>\n");
					Iterator<Map.Entry<String, ArrayList<ArrayList<String>>>> emailRulesEntriesProxy = proxyemailRules.entrySet().iterator();
					ArrayList<String> COL = new ArrayList<String>();
					while (emailRulesEntriesProxy.hasNext()) 
					{
						
						Map.Entry<String, ArrayList<ArrayList<String>>> proxyentry = emailRulesEntriesProxy.next();
						String PROXYKEY = proxyentry.getKey();
						/*
						   Note : they should not check with itself
						 * if PROXYKEY  = key : skip this check
						 * */
						if(PROXYKEY.equals(KEY))
						{
							PROXYKEY = proxyentry.getKey();
							break;
						}
						
						ArrayList<ArrayList<String>>  proxyvalue = proxyentry.getValue();
						
						ArrayList<String> allcol = proxyvalue.get(x);
						
						for(String S : col)
							{
								if(allcol.contains(S))
								{
									builder.append("<LI>"+PROXYKEY+"<font color='orange'>["+S+"]</font></LI>\n");
									COL.add(PROXYKEY);
									break;
								}
							}
					}
					if(COL.size()==0)
					{
					 COL.add("EMPTY");	
					}
					ROW.add(COL);
					builder.append("</TD>\n");
			} 
		  }
			
		
			boolean isAnyEmpty = false;
			boolean isAll_ALL = true;
			for(ArrayList<String> COLS : ROW)
			{
			   if(COLS.get(0)=="EMPTY")
			   {	
				   isAnyEmpty = true;
				   break;
			   }
			  
			}
			for(ArrayList<String> COLS : ROW)
			{
			   if(COLS.get(0)!="ALL")
			   {	
				   isAll_ALL = false;
				   break;
			   }
			}
			
			if(isAnyEmpty)
			{
				builder.append("<TD><font color='green'>RULE '"+KEY+"' FOUND NO CONFLICTS</font></TD>\n"); 
			}
			else if(isAll_ALL)
			{
				builder.append("<TD><font color='red'>RULE '"+KEY+"' FOUND CONFLICTS WITH ALL</font></TD>\n"); 
			}
			
			/*
			 * CHECK CONFLICTS RULES
			 * 
			 */
			int COUNT = 0;
			for(ArrayList<String> COLS : ROW)
			{
			   if(COLS.get(0)!="EMPTY" && COLS.get(0)!="ALL")
			   {	
				   COUNT++;
			   }
			}
			String conflictsWith="";
            if(COUNT ==1 && !isAnyEmpty && !isAll_ALL)
            {
            	for(ArrayList<String> COLS : ROW)
    			{
    			   if(COLS.get(0)!="EMPTY" || COLS.get(0)!="ALL")
    			   {	
    				  for(String S : COLS)
    				  {
    					 if(S.equals("EMPTY") || S.equals("ALL")){}
    					 else{ 
    					 conflictsWith+="<LI>"+S+"</LI>";
    					 }
    				  }
    				   
    			   }
    			}
            	builder.append("<TD><font color='red'>RULE '"+KEY+"' FOUND CONFLICTS WITH "+conflictsWith+"</font></TD>\n"); 
            	
            }
            // now check common rules in columns
            if(COUNT >1 && !isAnyEmpty && !isAll_ALL)
            {
            	ArrayList<ArrayList<String>> common = new ArrayList<ArrayList<String>>();
            	for(ArrayList<String> COLS : ROW)
    			{
    			   if(COLS.get(0)!="EMPTY" || COLS.get(0)!="ALL")
    			   {
    				  ArrayList<String> one = new ArrayList<String>();
    				  for(String S : COLS)
    				  {
    					 if(S.equals("EMPTY") || S.equals("ALL")){}
    					 else
    					 { 
    					 conflictsWith+="<LI>"+S+"</LI>";
    					 one.add(S);
    					 }
    				  }
    				  if(one.size()>0){
        				  common.add(one);
        				  }
    				   
    			   }
    			}
            	
            	Set<String> com = getCommonElements(common);
            	if(com.size()>0)
            	{
            		String conflictsRules = "";
            		for (Iterator<String> it = com.iterator(); it.hasNext(); ) 
            		{
            	        String f = it.next();
            	        conflictsRules+="<LI>"+f+"</LI>";
            	        
            	    }
            		builder.append("<TD><font color='red'>RULE '"+KEY+"' FOUND CONFLICTS WITH "+conflictsRules+"</font></TD>\n"); 
            		
            	}
            	else
            	{
            		builder.append("<TD><font color='green'>RULE '"+KEY+"' FOUND NO CONFLICTS</font></TD>\n"); 
            	}
            	
            	
            }
            ROW = null;
			builder.append("</TR>\n"); 
		}
		
		
		
	 return builder;
	}
	
	public static <T> Set<T> getCommonElements(Collection<? extends Collection<T>> collections) {

	    Set<T> common = new LinkedHashSet<T>();
	    if (!collections.isEmpty()) {
	       Iterator<? extends Collection<T>> iterator = collections.iterator();
	       common.addAll(iterator.next());
	       while (iterator.hasNext()) {
	          common.retainAll(iterator.next());
	       }
	    }
	    return common;
	}
	
	private StringBuilder validateTeamReplacementRules(StringBuilder builder)
	{
		
		
		proxyteamReplacementRules = new LinkedHashMap<String,ArrayList<ArrayList<String>>>(teamReplacementRules);
		Iterator<Map.Entry<String, ArrayList<ArrayList<String>>>> teamRepRulesEntries = teamReplacementRules.entrySet().iterator();
		while (teamRepRulesEntries.hasNext()) 
		{
			Map.Entry<String, ArrayList<ArrayList<String>>> entry = teamRepRulesEntries.next();
			String KEY = entry.getKey();
			ArrayList<ArrayList<String>> ROW = new ArrayList<ArrayList<String>>();
			
			builder.append("<TR>\n\n<TD>Team Replacement Rule</TD>\n<TD>"+KEY+"</TD>\n");
			ArrayList<ArrayList<String>>  value = entry.getValue();
			
			for(int x = 0 ; x< value.size();x++)
			{
				
				ArrayList<String> col = value.get(x);
				
				if(null == col || col.isEmpty() || col.size() ==0)
				{
					builder.append("<TD>ALL</TD>\n");
					ArrayList<String> COL = new ArrayList<String>();
					COL.add("ALL");
					ROW.add(COL);
					
				}
				/*
				 * this column value should be check with all keys of proxy map 
				 * same column value
				 * 
				 * 1. column value is empty , it mean applicable for all.
				 * 2. 
				 * 
				 * 
				 */
				
				else
				{
					builder.append("<TD>\n");
					Iterator<Map.Entry<String, ArrayList<ArrayList<String>>>> teamReplacementRulesEntriesProxy = proxyteamReplacementRules.entrySet().iterator();
					ArrayList<String> COL = new ArrayList<String>();
				while (teamReplacementRulesEntriesProxy.hasNext()) 
				{
					Map.Entry<String, ArrayList<ArrayList<String>>> proxyentry = teamReplacementRulesEntriesProxy.next();
					String PROXYKEY = proxyentry.getKey();
					/*
					   Note : they should not check with itself
					 * if PROXYKEY  = key : skip this check
					 * */
					if(PROXYKEY.equals(KEY))
					{
						PROXYKEY = proxyentry.getKey();
						break;
					
					}
					ArrayList<ArrayList<String>>  proxyvalue = proxyentry.getValue();
					
					ArrayList<String> allcol = proxyvalue.get(x);
					for(String S : col)
					{
						if(allcol.contains(S))
						{
							builder.append("<LI>"+PROXYKEY+"<font color='orange'>["+S+"]</font></LI>\n");
							COL.add(PROXYKEY);
							break;
						}
					}
				}
				if(COL.size()==0)
				{
				 COL.add("EMPTY");	
				}
				ROW.add(COL);
				builder.append("</TD>\n");
			}
			}
			boolean isAnyEmpty = false;
			boolean isAll_ALL = true;
			for(ArrayList<String> COLS : ROW)
			{
			   if(COLS.get(0)=="EMPTY")
			   {	
				   isAnyEmpty = true;
				   break;
			   }
			  
			}
			for(ArrayList<String> COLS : ROW)
			{
			   if(COLS.get(0)!="ALL")
			   {	
				   isAll_ALL = false;
				   break;
			   }
			}
			
			if(isAnyEmpty)
			{
				builder.append("<TD><font color='green'>RULE '"+KEY+"' FOUND NO CONFLICTS</font></TD>\n"); 
			}
			else if(isAll_ALL)
			{
				builder.append("<TD><font color='red'>RULE '"+KEY+"' FOUND CONFLICTS WITH ALL</font></TD>\n"); 
			}
			
			/*
			 * CHECK CONFLICTS RULES
			 * 
			 */
			int COUNT = 0;
			for(ArrayList<String> COLS : ROW)
			{
			   if(COLS.get(0)!="EMPTY" && COLS.get(0)!="ALL")
			   {	
				   COUNT++;
			   }
			}
			String conflictsWith="";
            if(COUNT ==1 && !isAnyEmpty && !isAll_ALL)
            {
            	for(ArrayList<String> COLS : ROW)
    			{
    			   if(COLS.get(0)!="EMPTY" || COLS.get(0)!="ALL")
    			   {	
    				  for(String S : COLS)
    				  {
    					 if(S.equals("EMPTY") || S.equals("ALL")){}
    					 else{ 
    					 conflictsWith+="<LI>"+S+"</LI>";
    					 }
    				  }
    				   
    			   }
    			}
            	builder.append("<TD><font color='red'>RULE '"+KEY+"' FOUND CONFLICTS WITH "+conflictsWith+"</font></TD>\n"); 
            	
            }
            // now check common rules in columns
            if(COUNT >1 && !isAnyEmpty && !isAll_ALL)
            {
            	ArrayList<ArrayList<String>> common = new ArrayList<ArrayList<String>>();
            	for(ArrayList<String> COLS : ROW)
    			{
    			   if(COLS.get(0)!="EMPTY" || COLS.get(0)!="ALL")
    			   {
    				  ArrayList<String> one = new ArrayList<String>();
    				  for(String S : COLS)
    				  {
    					 if(S.equals("EMPTY") || S.equals("ALL")){}
    					 else
    					 { 
    					 conflictsWith+="<LI>"+S+"</LI>";
	    				 one.add(S);
    					 }
    				  }
    				  if(one.size()>0){
    				  common.add(one);
    				  }
    			   }
    			}
            	
            	Set<String> com = getCommonElements(common);
            	if(com.size()>0)
            	{
            		String conflictsRules = "";
            		for (Iterator<String> it = com.iterator(); it.hasNext(); ) 
            		{
            	        String f = it.next();
            	        conflictsRules+="<LI>"+f+"</LI>";
            	        
            	    }
            		builder.append("<TD><font color='red'>RULE '"+KEY+"' FOUND CONFLICTS WITH "+conflictsRules+"</font></TD>\n"); 
            		
            	}
            	else
            	{
            		builder.append("<TD><font color='green'>RULE '"+KEY+"' FOUND NO CONFLICTS</font></TD>\n"); 
            	}
            	
            	
            }
            ROW = null;
			builder.append("</TR>\n");
		}
		
	   
	return builder;
	}
	
}
