package com.orange.util.csm;

import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

import com.orange.ui.component.DedupViewComponents;
import com.orange.util.CommonUtils;
import com.orange.util.ConnectionBean;
import com.orange.util.ConnectionBeanCSI;

public class DedupFileValidator {

	private LinkedHashSet<String> allAddressId = new LinkedHashSet<String>();
	private LinkedHashSet<String> allICO = new LinkedHashSet<String>();
	private LinkedHashSet<String> allOSR = new LinkedHashSet<String>();
	private LinkedHashSet<String> allSiteCode = new LinkedHashSet<String>();

	private String columnName[] = { "GID", "AM Response", "E_ICO1_CD", "E_SITE_ID",
			"E_ORANGE_SITE_NAME", "E_CUSTOMER_SITE_NAME", "E_SITE_STATUS_CD",
			"E_SITE_COMMENT", "E_PHONE", "E_FAX", "E_ADDRESS_ID",
			"E_BUIDLING_NAME", "E_LOCATION_NAME", "E_ROOM", "E_FLOOR",
			"E_STREET_NAME", "E_ZIP_CODE", "E_CITY_NAME", "E_STATE_CD",
			"E_STATE_NAME", "C_COUNTRY_ISO_3_CODE",
			"E_IS_VALID_PRESALES_SALES", "GOLD_SITECODE"

	};
	private String CSIPREFIX =ConnectionBeanCSI.getDbPrefix() ;
	private ArrayList<String[]> csvFileData = new ArrayList<String[]>();
	private LinkedHashMap<String, String[]> duplicateSiteCodeForSameGID = new LinkedHashMap<String, String[]>();
	private LinkedHashMap<String, ArrayList<String[]>> errors = new LinkedHashMap<String, ArrayList<String[]>>();
	private LinkedHashMap<String, ArrayList<String[]>> leadingTrailingErrors = new LinkedHashMap<String, ArrayList<String[]>>();
	private LinkedHashSet<String> falseAddressId = new LinkedHashSet<String>();
	private LinkedHashSet<String> falseOSR = new LinkedHashSet<String>();
	private LinkedHashSet<String> falseSitecode = new LinkedHashSet<String>();
	private String GOLDPREFIX = ConnectionBean.getDbPrefix() ;
	private LinkedHashSet<String> trueAddressId = new LinkedHashSet<String>();
	private LinkedHashSet<String> trueOSR = new LinkedHashSet<String>();
	private LinkedHashSet<String> trueSitecode = new LinkedHashSet<String>();

	public  DedupFileValidator(DedupViewComponents dedupViewComponents) 
	{
		String filePath = dedupViewComponents.getFileToValidate().getText();
		LinkedHashMap<String, ArrayList<String[]>> csmFileSet = new LinkedHashMap<String, ArrayList<String[]>>();

		String[] row = null;
		try {

				CSVReader br = new CSVReader(new FileReader(filePath));
				row = br.readNext();
				int RC =2;
				while ((row = br.readNext()) != null) 
				{
					for (int C = 0; C < row.length; C++) 
					{
						if (!CommonUtils.checkLeadingTrailingWhiteSpace(row[C])) 
						{
							fillLeadingTrailingMap("LEADING_TRAILING_WHITESPCAE", columnName[C], ""+RC, "'"+row[C]+"'");
						}
					}
					RC++;
				}
				RC = 0;
				br.close();
				row = null;
			
			    if(leadingTrailingErrors.size() ==0)
			    {
			   	br = new CSVReader(new FileReader(filePath));
				writeCSVDatainList(filePath);
				row = br.readNext();
				int X = row.length;
				String colName = row[X - 1];
				if (!colName.equalsIgnoreCase("GOLD_SITECODE")) 
				{
					fillMap("GOLD_SITECODE_MUST_BE_LAST_COLUMN", "Failed","Failed", "NA");
				}
				for (int C = 0; C < row.length; C++) 
				{
					if (!columnName[C].equalsIgnoreCase(row[C])) 
					{
						fillMap("COLUMN_MISMATCH", columnName[C], row[C], "NA");
					}
				}
				while ((row = br.readNext()) != null) 
				{
					ArrayList<String[]> arrayList = new ArrayList<String[]>();
					String key = row[0];
	
					/*
					 * Validate GID always a Number , ignore GID column Name as in
					 * First Row
					 */
					try {
							if (!key.equalsIgnoreCase("GID")) 
							{
								Integer.parseInt(key);
							}
		
							if (csmFileSet.containsKey(key)) 
							{
								arrayList = csmFileSet.get(key);
								arrayList.add(row);
			
							} else 
							{
								arrayList.add(row);
							}
						
							csmFileSet.put(key, arrayList);
						
					} catch (Exception e) 
					{
						fillMap("GID_NOT_NUMBER", "", "" , e.getMessage());
					}
				}
				br.close();
			}
			} catch (Exception e) 
			{
				CommonUtils.printExceptionStack(e,dedupViewComponents);
			}
		
		try
		{
			validate(csmFileSet);
		}catch(Exception e){
			
			CommonUtils.printExceptionStack(e,dedupViewComponents);
		}
	}

	public ArrayList<StringBuilder> allAddressIdLinkedWithMultipleICO() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		
		String commonQLData = "select org.ORGANIZATIONID ,site.address_id,site.status, site.eq_comment ,site.core_site_id,site.sitecode,site.orange_sitename " 
		+"from "+GOLDPREFIX+"eq_site site  ,	"+GOLDPREFIX+"SC_ORGANIZATION org " 
		+"where address_id in ( "
		+"Select distinct address_id From "+GOLDPREFIX+"Eq_Site "
		+"Group By address_id  Having Count(Tril_Gid) >1  and address_id in  ( ";
		
		String commonQLDataTail = " )) and site.eq_siteof = org.tril_gid";
		Iterator<String> itr = allAddressId.iterator();
		while (itr.hasNext()) {
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,
					commonQLDataTail);
			list.add(stringBuilder);
		}

		return list;
	}

	public ArrayList<StringBuilder> coreIdLinkedWithFalseSitecode() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		String commonQLData = "select sitecode,core_site_id,address_id from "+ GOLDPREFIX+ "eq_site site where site.sitecode in( ";
		String commonQLDataTail = " ) ";
		Iterator<String> itr = falseSitecode.iterator();
		while (itr.hasNext()) {
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,
					commonQLDataTail);
			list.add(stringBuilder);
		}

		return list;
	}

	
	public ArrayList<StringBuilder> coreIdLinkedWithSitecode() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		String commonQLData = "SELECT SITE.SITECODE,SITE.CORE_SITE_ID,SITE.ADDRESS_ID,SITE.ORANGE_SITENAME FROM "+ GOLDPREFIX+ "EQ_SITE SITE WHERE SITE.SITECODE IN ( ";
		String commonQLDataTail = " ) ";
		Iterator<String> itr = allSiteCode.iterator();
		while (itr.hasNext()) {
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,
					commonQLDataTail);
			list.add(stringBuilder);
		}

		return list;
	}

	public ArrayList<StringBuilder> coreIdLinkedWithTrueSitecode() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		String commonQLData = "select sitecode,core_site_id,address_id from "+ GOLDPREFIX+ "eq_site site where site.sitecode in( ";		
		String commonQLDataTail = " ) ";
		Iterator<String> itr = trueSitecode.iterator();
		while (itr.hasNext()) {
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,
					commonQLDataTail);
			list.add(stringBuilder);
		}

		return list;
	}

	public ArrayList<StringBuilder> falseAddressIdLinkedWithMultipleICO() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		
		String commonQLData = "select org.ORGANIZATIONID ,site.address_id,site.status, site.eq_comment ,site.core_site_id,site.sitecode,site.orange_sitename " 
		+" from "+GOLDPREFIX+"eq_site site ,"+GOLDPREFIX+"SC_ORGANIZATION org " 
		+" where address_id in ( "
		+" Select distinct address_id From "+GOLDPREFIX+"Eq_Site "
		+" Group By address_id  Having Count(Tril_Gid) >1  and address_id in  ( ";
		
		String commonQLDataTail = " )) and site.eq_siteof = org.tril_gid";
		Iterator<String> itr = falseAddressId.iterator();
		while (itr.hasNext()) {
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,
					commonQLDataTail);
			list.add(stringBuilder);
		}

		return list;
	}

	public ArrayList<StringBuilder> falseSitecodeLinkedWithMultipleICO() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		String commonQLData = "select count(oid),stcode from (select org.organizationid as oid,site.sitecode as stcode  from "
				+ GOLDPREFIX+ "eq_site site, "
				+ GOLDPREFIX+ "sc_organization org  where site.eq_siteof=org.tril_gid and sitecode in( ";
		String commonQLDataTail = " )) t group by stcode having count(oid) >1 ";
		Iterator<String> itr = falseSitecode.iterator();
		while (itr.hasNext()) {
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,
					commonQLDataTail);
			list.add(stringBuilder);
		}

		return list;
	}

	private void fillMap(String key, String GID, String rowNumber,String comments) {
		if (errors.containsKey(key)) {
			ArrayList<String[]> arrayList = errors.get(key);
			String[] addError = new String[3];
			addError[0] = GID;
			addError[1] = rowNumber;
			addError[2] = comments;
			arrayList.add(addError);
		} else {
			ArrayList<String[]> arrayList = new ArrayList<String[]>();
			String[] addError = new String[3];
			addError[0] = GID;
			addError[1] = rowNumber;
			addError[2] = comments;
			arrayList.add(addError);
			errors.put(key, arrayList);
		}
	}
	
	private void fillLeadingTrailingMap(String key, String GID, String rowNumber,String comments) {
		if (leadingTrailingErrors.containsKey(key)) {
			ArrayList<String[]> arrayList = leadingTrailingErrors.get(key);
			String[] addError = new String[3];
			addError[0] = GID;
			addError[1] = rowNumber;
			addError[2] = comments;
			arrayList.add(addError);
		} else {
			ArrayList<String[]> arrayList = new ArrayList<String[]>();
			String[] addError = new String[3];
			addError[0] = GID;
			addError[1] = rowNumber;
			addError[2] = comments;
			arrayList.add(addError);
			leadingTrailingErrors.put(key, arrayList);
		}
	}

	public  ArrayList<String[]> getCsvFileData() {
		return csvFileData;
	}

	public LinkedHashMap<String, ArrayList<String[]>> getErrors() {
		return errors;
	}
	
	public LinkedHashMap<String, ArrayList<String[]>> getLeadingTrailingErrors() {
		return leadingTrailingErrors;
	}
	
	
	public ArrayList<StringBuilder> getIcoForFalseSiteCode() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		String commonQLData = "select org.organizationid as organizationid,site.sitecode as sitecode  from "
				+ GOLDPREFIX+ "eq_site site, "
				+ GOLDPREFIX+ "sc_organization org where site.eq_siteof=org.tril_gid and sitecode in(";
		String commonQLDataTail = " ) ";
		Iterator<String> itr = falseSitecode.iterator();
		while (itr.hasNext()) {
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,
					commonQLDataTail);
			list.add(stringBuilder);
		}

		return list;
	}

	public ArrayList<StringBuilder> getIcoForTrueSiteCode() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		String commonQLData = "select org.organizationid as organizationid,site.sitecode as sitecode  from "
				+ GOLDPREFIX+ "eq_site site, "
				+ GOLDPREFIX+ "sc_organization org where site.eq_siteof=org.tril_gid and sitecode in(";
		String commonQLDataTail = " ) ";
		Iterator<String> itr = trueSitecode.iterator();
		while (itr.hasNext()) {
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,
					commonQLDataTail);
			list.add(stringBuilder);
		}

		return list;
	}
	
	public ArrayList<StringBuilder> getQueryFalseSiteCodeStatus() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		String commonQLData = "Select sitecode,Count(Tril_Gid),Status From "
				+ GOLDPREFIX+ "Eq_Site Group By sitecode,Status Having Count(Tril_Gid) >=1  And Status <> '0' and sitecode in (";
		String commonQLDataTail = " ) ";
		Iterator<String> itr = falseSitecode.iterator();
		while (itr.hasNext()) {
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,
					commonQLDataTail);
			list.add(stringBuilder);
		}

		return list;
	}
	
	public ArrayList<StringBuilder> getQueryForAddressHasMultipleSitecode() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		Iterator<String> itr = allAddressId.iterator();
		String commonQLData = "Select Address_Id,Count(Tril_Gid),Status From "
				+ GOLDPREFIX+ "Eq_Site Group By Address_Id,Status Having Count(Tril_Gid) >=2  And Status='0' and address_id in ( ";
		String commonQLDataTail = " ) ";
		while (itr.hasNext()) 
		{
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,	commonQLDataTail);
			list.add(stringBuilder);
		}
		return list;
	}

	public ArrayList<StringBuilder> getQueryForFalseOrdersSites() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		
		String commonQLData = "SELECT DISTINCT QUOTENUMBER , QUOTE.SITE,QUOTE.HOTCUTNEWSITE,ADDRESS_ID,CORE_SITE_ID,SITECODE,ORGANIZATIONID FROM "
            + GOLDPREFIX+"SC_QUOTE QUOTE,"+GOLDPREFIX+" EQ_SITE SITE,"+GOLDPREFIX+"SC_ORGANIZATION ORGAN WHERE " +
            " ((QUOTE.HOTCUTNEWSITE <> 'NULL' AND QUOTE.HOTCUTNEWSITE =  SITE.TRIL_GID) OR (QUOTE.HOTCUTNEWSITE IS NULL AND QUOTE.SITE = SITE.TRIL_GID ))" +
            " AND SITE.EQ_SITEOF=ORGAN.TRIL_GID AND SITE.SITECODE IN (";
		String commonQLDataTail = " ) ";
		Iterator<String> itr = falseSitecode.iterator();
		while (itr.hasNext()) {
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,commonQLDataTail);
			list.add(stringBuilder);
		}

		return list;
	}

	public ArrayList<StringBuilder> getQueryForFalseOrdersSitesFromCSI() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		
		String commonQLData = "SELECT DISTINCT VERSION.ORDHANDLE,VERSION.ADDRESSID,VERSION.CORESITEID,VERSION.SITEHANDLE,VERSION.CUSTHANDLE FROM "
            + CSIPREFIX+ "CVERSION VERSION WHERE SITEHANDLE IN (" ;
		String commonQLDataTail = " ) ORDER BY SITEHANDLE";
		Iterator<String> itr = falseSitecode.iterator();
		while (itr.hasNext()) 
		{
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,commonQLDataTail);
			list.add(stringBuilder);
		}

		return list;
	}

	
	public StringBuilder getQueryForFalseOSR() {
		StringBuilder stringBuilder = new StringBuilder("With X As (  \n ");
		Iterator<String> itr = falseOSR.iterator();
		int count = 1;
		while (itr.hasNext()) {
			String data = itr.next();
			if (count == 1) {
				stringBuilder.append("select '" + data+ "' as orange_sitename from dual ");
			}
			stringBuilder.append("Union All select '" + data + "' from dual ");
			count++;
		}
		stringBuilder.append(" ) \n Select distinct x.orange_sitename  From X, "
						+ GOLDPREFIX+ "Eq_Site T1 where x.orange_sitename = t1.orange_sitename(+) and tril_gid is  null");
		return stringBuilder;
	}
	
	public StringBuilder getQueryForFalseSiteCode() {
		StringBuilder stringBuilder = new StringBuilder("With X As (  \n ");
		Iterator<String> itr = falseSitecode.iterator();
		int count = 1;
		while (itr.hasNext()) {
			String data = itr.next();
			if (count == 1) 
			{
				stringBuilder.append("select '" + data+ "' as sitecode from dual ");

			}

			stringBuilder.append("Union All select '" + data + "' from dual ");
			count++;
		}
		stringBuilder.append(" ) \n Select distinct x.sitecode  From X, "+ GOLDPREFIX+ "Eq_Site T1 where x.sitecode = t1.sitecode(+) and tril_gid is  null");
		return stringBuilder;
	}
	
	public StringBuilder getQueryForFalseUnavailavleAddress() {
		StringBuilder stringBuilder = new StringBuilder("With X As (  \n ");
		Iterator<String> itr = falseAddressId.iterator();
		int count = 1;
		while (itr.hasNext()) {
			String data = itr.next();
			if (count == 1) {
				stringBuilder.append("select '" + data+ "' as address_id from dual ");
			}
			stringBuilder.append("Union All select '" + data + "' from dual ");
			count++;
		}

		stringBuilder
				.append(" ) \n  Select distinct x.address_id  From X, "	+ GOLDPREFIX+ "Eq_Site T1 where x.address_id = t1.address_id(+) and tril_gid is  null");
		return stringBuilder;
	}
	public ArrayList<StringBuilder> getQueryForOrderService() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		
		String commonQLData = "SELECT QUOTE.QUOTENUMBER,DECODE (QUOTE.EQ_ORDERTYPE, '1', 'New', '2', 'Change','3','Disconnect','COULD_NOT_DECODE') ORDER_TYPE,"+
				CommonUtils.EQ_TYPE_DECODE +
				"QUOTE.SERVICENAME,SERVICE.DISP_NAME AS MIGRATIONSERVICENAME,SITE.SITECODE FROM " +
				GOLDPREFIX+"SC_QUOTE QUOTE  LEFT JOIN "+GOLDPREFIX+"EQ_SERVICE SERVICE ON (QUOTE.MIGRATIONSERVICE = SERVICE.SERVICE_ID) , " +
				GOLDPREFIX+"EQ_SITE SITE ," +
				GOLDPREFIX+"SC_HIERARCHY HR LEFT JOIN "+GOLDPREFIX+"EQ_CHANGE CHG ON (HR.DATA = CHG.TRIL_GID) LEFT JOIN "+GOLDPREFIX+"EQ_DISCONNECT DIS  ON (HR.DATA = DIS.TRIL_GID) "+
				" WHERE  "+
				"((QUOTE.HOTCUTNEWSITE <> 'NULL' AND QUOTE.HOTCUTNEWSITE =  SITE.TRIL_GID) OR (QUOTE.HOTCUTNEWSITE IS NULL AND QUOTE.SITE = SITE.TRIL_GID )) " +
				" AND QUOTE.CONFIGURATIONS = HR.TRIL_GID " +
				" AND SITE.SITECODE IN (";
				String commonQLDataTail = " ) ";
		
		Iterator<String> itr = allSiteCode.iterator();
		while (itr.hasNext()) 
		{
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,commonQLDataTail);
			list.add(stringBuilder);
		}
		return list;
	}
	
	public StringBuilder getQueryForOSR() {
		StringBuilder stringBuilder = new StringBuilder("With X As (  \n ");
		Iterator<String> itr = allOSR.iterator();
		int count = 1;
		while (itr.hasNext()) {
			String data = itr.next();
			if (count == 1) {
				stringBuilder.append("select '" + data+ "' as orange_sitename from dual ");
			}
			stringBuilder.append("Union All select '" + data + "' from dual ");
			count++;
		}
		stringBuilder
				.append(" ) \n Select distinct x.orange_sitename  From X, "	+ GOLDPREFIX+ "Eq_Site T1 where x.orange_sitename = t1.orange_sitename(+) and tril_gid is  null");
		return stringBuilder;
	}
	
	public StringBuilder getQueryForSiteCode() {
		StringBuilder stringBuilder = new StringBuilder("With X As (  \n ");
		Iterator<String> itr = allSiteCode.iterator();
		int count = 1;
		while (itr.hasNext()) {
			String data = itr.next();
			if (count == 1) {
				stringBuilder.append("select '" + data+ "' as sitecode from dual ");

			}

			stringBuilder.append("Union All select '" + data + "' from dual ");
			count++;
		}
		stringBuilder
				.append(" ) \n Select distinct x.sitecode  From X, "+ GOLDPREFIX+ "Eq_Site T1 where x.sitecode = t1.sitecode(+) and tril_gid is  null");
		return stringBuilder;
	}
	
	public ArrayList<StringBuilder> getQueryForTrueOrdersSites() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		
		String commonQLData = "SELECT DISTINCT QUOTENUMBER , QUOTE.SITE,QUOTE.HOTCUTNEWSITE,ADDRESS_ID,CORE_SITE_ID,SITECODE,ORGANIZATIONID FROM "
			                 + GOLDPREFIX+ "SC_QUOTE QUOTE,"+GOLDPREFIX+" EQ_SITE SITE,"+GOLDPREFIX+"SC_ORGANIZATION ORGAN WHERE " +
			                 " ((QUOTE.HOTCUTNEWSITE <> 'NULL' AND QUOTE.HOTCUTNEWSITE =  SITE.TRIL_GID) OR (QUOTE.HOTCUTNEWSITE IS NULL AND QUOTE.SITE = SITE.TRIL_GID ))" +
			                 " AND SITE.EQ_SITEOF=ORGAN.TRIL_GID AND SITE.SITECODE IN (";
		String commonQLDataTail = " ) ";
		Iterator<String> itr = trueSitecode.iterator();
		while (itr.hasNext()) {
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,
					commonQLDataTail);
			list.add(stringBuilder);
		}

		return list;
	}
	/*
	
	public ArrayList<StringBuilder> getQueryForTrueOrdersHotCutSites() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		String commonQLData = "SELECT DISTINCT QUOTENUMBER , ADDRESS_ID,CORE_SITE_ID,SITECODE,ORGANIZATIONID FROM "+ golddba+ "SC_QUOTE QUOTE,"+golddba+" EQ_SITE SITE,"+golddba+"SC_ORGANIZATION ORGAN WHERE QUOTE.HOTCUTNEWSITE =  SITE.TRIL_GID AND SITE.EQ_SITEOF=ORGAN.TRIL_GID AND SITE.SITECODE IN (";
		String commonQLDataTail = " ) ";
		Iterator<String> itr = trueSitecode.iterator();
		while (itr.hasNext()) {
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,
					commonQLDataTail);
			list.add(stringBuilder);
		}

		return list;
	}
	*/
	
	
	/*
	public ArrayList<StringBuilder> getQueryForFalseOrdersHotCutSites() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		String commonQLData = "SELECT DISTINCT QUOTENUMBER , ADDRESS_ID,CORE_SITE_ID,SITECODE,ORGANIZATIONID FROM "+ golddba+ "SC_QUOTE QUOTE,"+golddba+" EQ_SITE SITE,"+golddba+"SC_ORGANIZATION ORGAN WHERE QUOTE.HOTCUTNEWSITE =  SITE.TRIL_GID AND SITE.EQ_SITEOF=ORGAN.TRIL_GID AND SITE.SITECODE IN (";
		String commonQLDataTail = " ) ";
		Iterator<String> itr = falseSitecode.iterator();
		while (itr.hasNext()) {
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,
					commonQLDataTail);
			list.add(stringBuilder);
		}

		return list;
	}
	*/
	public StringBuilder getQueryForTrueOSR() {
		StringBuilder stringBuilder = new StringBuilder("With X As (  \n ");
		Iterator<String> itr = trueOSR.iterator();
		int count = 1;
		while (itr.hasNext()) {
			String data = itr.next();
			if (count == 1) {
				stringBuilder.append("select '" + data+ "' as orange_sitename from dual ");
			}
			stringBuilder.append("Union All select '" + data + "' from dual ");
			count++;
		}
		stringBuilder
				.append(" ) \n Select distinct x.orange_sitename  From X, "+ GOLDPREFIX+ "Eq_Site T1 where x.orange_sitename = t1.orange_sitename(+) and tril_gid is  null");
		return stringBuilder;
	}

	
	public StringBuilder getQueryForTrueSiteCode() {
		StringBuilder stringBuilder = new StringBuilder("With X As (  \n ");
		Iterator<String> itr = trueSitecode.iterator();
		int count = 1;
		while (itr.hasNext()) {
			String data = itr.next();
			if (count == 1) {
				stringBuilder.append("select '" + data+ "' as sitecode from dual ");

			}

			stringBuilder.append("Union All select '" + data + "' from dual ");
			count++;
		}
		stringBuilder
				.append(" ) \n Select distinct x.sitecode  From X, "+ GOLDPREFIX+ "Eq_Site T1 where x.sitecode = t1.sitecode(+) and tril_gid is  null");
		return stringBuilder;
	}

	

	public StringBuilder getQueryForTrueUnavailavleAddress() {
		StringBuilder stringBuilder = new StringBuilder("With X As (  \n ");
		Iterator<String> itr = trueAddressId.iterator();
		int count = 1;
		while (itr.hasNext()) {
			String data = itr.next();
			if (count == 1) {
				stringBuilder.append("select '" + data+ "' as address_id from dual ");
			}
			stringBuilder.append("Union All select '" + data + "' from dual ");
			count++;
		}

		stringBuilder
				.append(" ) \n  Select distinct x.address_id  From X, "	+ GOLDPREFIX+ "Eq_Site T1 where x.address_id = t1.address_id(+) and tril_gid is  null");
		return stringBuilder;
	}
	public StringBuilder getQueryForUnavailavleAddress() {
		StringBuilder stringBuilder = new StringBuilder("With X As (  \n ");
		Iterator<String> itr = allAddressId.iterator();
		int count = 1;
		while (itr.hasNext()) {
			String data = itr.next();
			if (count == 1) {
				stringBuilder.append("select '" + data+ "' as address_id from dual ");
			}
			stringBuilder.append("Union All select '" + data + "' from dual ");
			count++;
		}

		stringBuilder.append(" ) \n  Select distinct x.address_id  From X, "+ GOLDPREFIX+ "Eq_Site T1 where x.address_id = t1.address_id(+) and tril_gid is  null");
		return stringBuilder;
	}
	public StringBuilder getQueryForUnavailavleICO() {
		StringBuilder stringBuilder = new StringBuilder("With X As (  \n ");
		Iterator<String> itr = allICO.iterator();
		int count = 1;
		while (itr.hasNext()) {
			String data = itr.next();
			if (count == 1) {
				stringBuilder.append("select '" + data+ "' as organizationid from dual ");
			}
			stringBuilder.append("Union All select '" + data + "' from dual ");
			count++;
		}

		stringBuilder.append(" ) \n  Select distinct x.organizationid  From X, "+ GOLDPREFIX+ "sc_organization T1 where x.organizationid = t1.organizationid(+) and tril_gid is  null");
		return stringBuilder;
	}
	
	public ArrayList<StringBuilder> getQueryTrueSiteCodeStatus() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		String commonQLData = "Select sitecode,Count(Tril_Gid),Status From "+ GOLDPREFIX+ "Eq_Site Group By sitecode,Status Having Count(Tril_Gid) >=1  And Status <> '0' and sitecode in (";
		String commonQLDataTail = " ) ";
		Iterator<String> itr = trueSitecode.iterator();
		while (itr.hasNext()) {
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,
					commonQLDataTail);
			list.add(stringBuilder);
		}

		return list;
	}
	

	public ArrayList<StringBuilder> hasDuplicateFalseSiteCode() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		String commonQLData = "Select sitecode,Count(Tril_Gid) From "+ GOLDPREFIX+ "Eq_Site Group By sitecode,Status Having Count(Tril_Gid) >1 and status = '0' and sitecode in (";
		String commonQLDataTail = " ) ";
		Iterator<String> itr = falseSitecode.iterator();
		while (itr.hasNext()) 
		{
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,
					commonQLDataTail);
			list.add(stringBuilder);
		}

		return list;
	}

	public ArrayList<StringBuilder> hasDuplicateTrueSiteCode() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		String commonQLData = "Select sitecode,Count(Tril_Gid) From "+ GOLDPREFIX+ "Eq_Site Group By sitecode,Status Having Count(Tril_Gid) >1 and status = '0' and sitecode in (";
		String commonQLDataTail = " ) ";
		Iterator<String> itr = trueSitecode.iterator();
		while (itr.hasNext()) {
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,
					commonQLDataTail);
			list.add(stringBuilder);
		}

		return list;
	}
	
	public ArrayList<StringBuilder> postCheckQueryForFalseSiteCode() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		String commonQLData = "select  modificationdate,status, sitecode,address_id,core_site_id , orange_sitename,eq_comment from "+ GOLDPREFIX + "eq_site where sitecode in ( ";
		//--String commonQLDataTail = " ) and status <> '3'";
		String commonQLDataTail = " ) ";
		Iterator<String> itr = falseSitecode.iterator();

		while (itr.hasNext()) {
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,
					commonQLDataTail);
			list.add(stringBuilder);
		}
		return list;
	}
	public ArrayList<StringBuilder> postCheckQueryForTrueSiteCode() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		String commonQLData = "select  modificationdate,status,sitecode,address_id,core_site_id , orange_sitename,eq_comment from "+ GOLDPREFIX + "eq_site where sitecode in ( ";
		//--String commonQLDataTail = " ) and status <> '0'";
		String commonQLDataTail = " ) ";
		Iterator<String> itr = trueSitecode.iterator();
		while (itr.hasNext()) {
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,commonQLDataTail);
			list.add(stringBuilder);
		}
    	return list;
	}
	
	
	

	public ArrayList<StringBuilder> trueAddressIdLinkedWithMultipleICO() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		
		String commonQLData = "select org.ORGANIZATIONID ,site.address_id,site.status, site.eq_comment ,site.core_site_id,site.sitecode,site.orange_sitename " 
		+"from "+GOLDPREFIX+"eq_site site  ,"+GOLDPREFIX+"SC_ORGANIZATION org " 
		+"where address_id in ( "
		+"Select distinct address_id From "+GOLDPREFIX+"Eq_Site "
		+"Group By address_id  Having Count(Tril_Gid) >1  and address_id in  ( ";
		
		String commonQLDataTail = " )) and site.eq_siteof = org.tril_gid";
		Iterator<String> itr = trueAddressId.iterator();
		while (itr.hasNext()) {
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,
					commonQLDataTail);
			list.add(stringBuilder);
		}

		return list;
	}

	public ArrayList<StringBuilder> trueSitecodeLinkedWithMultipleICO() {
		ArrayList<StringBuilder> list = new ArrayList<StringBuilder>();
		String commonQLData = "select count(oid),stcode from (select org.organizationid as oid,site.sitecode as stcode  from "
				+ GOLDPREFIX+ "eq_site site, "
				+ GOLDPREFIX+ "sc_organization org  where site.eq_siteof=org.tril_gid and sitecode in( ";
		String commonQLDataTail = " )) t group by stcode having count(oid) >1 ";
		Iterator<String> itr = trueSitecode.iterator();
		while (itr.hasNext()) {
			StringBuilder stringBuilder = CommonUtils.getIterate(itr, commonQLData,
					commonQLDataTail);
			list.add(stringBuilder);
		}

		return list;
	}

	private void validate(LinkedHashMap<String, ArrayList<String[]>> csmFileSet) throws SQLException {
		Iterator<Map.Entry<String, ArrayList<String[]>>> entries = csmFileSet.entrySet().iterator();

		/*
		 * A group must have a True AM Response
		 * 
		 * True must have GOLD_SITECODE All Row must have E_ORANGE_SITE_NAME
		 * E_ORANGE_SITE_NAME should not be > 40 char E_ORANGE_SITE_NAME should
		 * not have special char No Leading or Tailoring space
		 */

		/*
		 * 
		 * start count from 2 , because first row in excel is header...
		 */
		int count = 2;
		while (entries.hasNext()) {
			Map.Entry<String, ArrayList<String[]>> entry = entries.next();
			
			String GID = entry.getKey();
			
			/*
			 * Check every GID must have First AM_RESPONSE True
			 * 
			 */
			ArrayList<String[]> value = entry.getValue();
			String AM_RES[] = value.get(0);
			String IS_FIRST_AM_RES_TRUE = AM_RES[1];
			if(!IS_FIRST_AM_RES_TRUE.equals("TRUE"))
			{
				fillMap("FIRST_AM_RESPONSE_NOT_TRUE", GID, "" + count, IS_FIRST_AM_RES_TRUE);
			}
		
			int flagAmResponseCount = 0;
			boolean isSameICOForGID = true;

			String GLOBAL_E_ICO1_CD = "";
			for (int x = 0; x < value.size(); x++) 
			{

				String[] row = value.get(x);

			
				String AM_Response = "";
				String E_ICO1_CD = "";
				String SITE_ID = "";
				String E_ORANGE_SITE_NAME = "";
				String E_ADDRESS_ID = "";
				String GOLD_SITECODE = "";
				String E_SITE_STATUS_CD = "";
				String ISVALIDPRESALES = "";
				
				
				// 2nd AM Response
				if (row.length >= 2) 
				{
					AM_Response = row[1];
				}
				// 3- E_ICO1_CD
				if (row.length >= 3) 
				{
					E_ICO1_CD = row[2];
					if (x == 0) 
					{
						GLOBAL_E_ICO1_CD = E_ICO1_CD;
					}
					allICO.add(E_ICO1_CD);
				}
				// 4- E_SITE_ID
				if (row.length >= 4) 
				{
					SITE_ID = row[3];
				}
				// 5 - E_ORANGE_SITE_NAME
				if (row.length >= 5) 
				{
					E_ORANGE_SITE_NAME = row[4];
					String tempData = E_ORANGE_SITE_NAME;
					String refineData = CommonUtils.refineData(tempData);
					if (AM_Response.equalsIgnoreCase("True")) {
						trueOSR.add(refineData);
					}
					if (AM_Response.equalsIgnoreCase("False")) {
						falseOSR.add(refineData);
					}
					allOSR.add(refineData);
				}
				// 7 - E_SITE_STATUS_CD
				if (row.length >= 7) 
				{
					E_SITE_STATUS_CD = row[6];
				}
				// 11 - E_ADDRESS_ID
				if (row.length >= 11) 
				{
					E_ADDRESS_ID = row[10];
					String tempData = E_ADDRESS_ID;
					String refineData = CommonUtils.refineData(tempData);

					
					
					if (AM_Response.equalsIgnoreCase("True")) 
					{
						trueAddressId.add(refineData);
					}
					if (AM_Response.equalsIgnoreCase("False")) 
					{
						falseAddressId.add(refineData);
					}
					allAddressId.add(refineData);

				}
				if (row.length >= 22) 
				{
					ISVALIDPRESALES = row[21];
					
					if(!CommonUtils.isNULL(ISVALIDPRESALES))
					{
						if(ISVALIDPRESALES.equalsIgnoreCase("1") || ISVALIDPRESALES.equalsIgnoreCase("0"))
						{
							fillMap("WRONG_ISVALIDPRESALES_VALUE", GID, "" + count, ISVALIDPRESALES);
						}
					}
				}
				if (row.length >= 23) 
				{
					GOLD_SITECODE = row[22];
					String tempData = GOLD_SITECODE;

					CharSequence cs1 = "''";
					CharSequence cs2 = "'||'&'||'";
					if (AM_Response.equalsIgnoreCase("True")) 
					{
						if (tempData.contains("'") && !tempData.contains(cs1)) 
						{
							
							fillMap("WRONG_SITECODE_KEEP", GID, "" + count, tempData);
						}
						if (tempData.contains("&") && !tempData.contains(cs2)) 
						{
							fillMap("WRONG_SITECODE_KEEP", GID, "" + count, tempData);
						}
					}
					if (AM_Response.equalsIgnoreCase("False")) 
					{
						if (tempData.contains("'") && !tempData.contains(cs1)) {
						
							fillMap("WRONG_SITECODE_DISCARD", GID, "" + count, tempData);
						}
						if (tempData.contains("&") && !tempData.contains(cs2)) {
							fillMap("WRONG_SITECODE_DISCARD", GID, "" + count, tempData);
						}
					}
					
					/*
					 * MAY-CHECK
					 * New Check to get SiteCodes
					 * 
					 
					
					String checkSitecode = "select sitecode from "+ConnectionBean.getDbPrefix()+ "eq_site where SITECODE=?";
					
					ArrayList<String[]> dbsiotecode = CommonUtils.getQueryResult(checkSitecode,GOLD_SITECODE,dedupViewComponents);

					if(null == dbsiotecode || dbsiotecode.size() ==0)
					{
						fillMap("NEW_CHECK_SITECODE_NOTFOUND", GID, "" + count, tempData);
					}
					*/
					
					/*
					 * 
					 * Check same sitecode with same group
					 */

					String refineData = CommonUtils.refineData(tempData);

					if (duplicateSiteCodeForSameGID.containsKey(refineData)) 
					{
						String data[] = duplicateSiteCodeForSameGID.get(refineData);
						if (data[0].equalsIgnoreCase(GID)) 
						{
							fillMap("DUPLICATE_SITECODE_SAME_GID", GID, ""+ count, refineData);
						}
						if (!data[0].equalsIgnoreCase(GID)) 
						{
							
							String OSR_ = E_ORANGE_SITE_NAME;
							String OSR1_ = data[3];
							
							OSR_ = OSR_.replaceAll("_DEDUP_","");
							OSR1_ = OSR1_.replaceAll("_DEDUP_","");
							
							if(!SITE_ID.equalsIgnoreCase(data[1]) || !E_ADDRESS_ID.equalsIgnoreCase(data[2]))//||!OSR_.equalsIgnoreCase(OSR1_))
							{
								fillMap("DUPLICATE_SITECODE_DIFFERENT_GID", GID, ""+ count, refineData);
							}
						}
					}

					else {
						if (refineData.length() >= 1) 
						{
							String arr[] = {GID,SITE_ID,E_ADDRESS_ID,E_ORANGE_SITE_NAME};
							duplicateSiteCodeForSameGID.put(refineData, arr);
						}
					}

					allSiteCode.add(refineData);

					if (AM_Response.equalsIgnoreCase("True")) {
						trueSitecode.add(refineData);
					}
					if (AM_Response.equalsIgnoreCase("False")) {
						falseSitecode.add(refineData);
					}

				}

				if (AM_Response.equalsIgnoreCase("TRUE")) {
					/*
					 * A Group must have true.
					 */
					flagAmResponseCount++;
					/*
					 * True must have GOLD_SITECODE
					 */
					if (GOLD_SITECODE.isEmpty()) 
					{
						fillMap("GOLD_SITECODE_MISSING_FOR_AM_TRUE", GID, ""+ count, "NA");
					}
				}

				if (AM_Response.equalsIgnoreCase("FALSE")) {
					if (GOLD_SITECODE.isEmpty()) {
						fillMap("GOLD_SITECODE_MISSING_FOR_AM_FALSE", GID, ""+ count, "NA");
					}
				}

				/*
				 * All Row must have E_ORANGE_SITE_NAME
				 */
				
				
				if (E_ORANGE_SITE_NAME.isEmpty()) {
					fillMap("E_ORANGE_SITE_NAME_MISSING", GID, "" + count, "NA");

				}

				if (E_ICO1_CD.isEmpty()) {
					fillMap("E_ICO1_CD_MISSING", GID, "" + count, "NA");

				}

				if (E_SITE_STATUS_CD.isEmpty()) {
					fillMap("E_SITE_STATUS_CD_MISSING", GID, "" + count, "NA");

				}
				if (!E_SITE_STATUS_CD.equals("0")) {
					fillMap("E_SITE_STATUS_CD_NOT_ZERO", GID, "" + count, "NA");

				}

				if (SITE_ID.isEmpty()) {
					fillMap("SITE_ID_MISSING", GID, "" + count, "NA");

				}

				if (E_ADDRESS_ID.isEmpty()) {
					fillMap("E_ADDRESS_ID_MISSING", GID, "" + count, "NA");

				}
				/*
				 * E_ORANGE_SITE_NAME should not be > 40 char
				 */
				if (E_ORANGE_SITE_NAME.length() > 40) {
					fillMap("E_ORANGE_SITE_NAME_LENGTH", GID, "" + count, E_ORANGE_SITE_NAME);
					fillMap("E_ORANGE_SITE_NAME_SUGGESTIVE", GID, "" + count, E_ORANGE_SITE_NAME.substring(0,40));

				}

				/*
				 * GID must have same ICO
				 */

				if (!GLOBAL_E_ICO1_CD.equalsIgnoreCase(E_ICO1_CD)) {
					isSameICOForGID = false;
				}
				// TODO
				/*
				 * No Leading / Tail.. space
				 */
				count++;
			}

			if (flagAmResponseCount == 0) {
				fillMap("AM_RESPONSE_NOT_TRUE", GID, "" + count, "NA");
			}

			if (!isSameICOForGID) {
				fillMap("E_ICO1_CD_NOT_EQUAL", GID, "" + count, "NA");
			}

		}
	}

	private  void writeCSVDatainList(String filePath) throws IOException {
		CSVReader br = new CSVReader(new FileReader(filePath));
		String[] row = null;
		row = br.readNext();
		while ((row = br.readNext()) != null) {
			csvFileData.add(row);
		}
		br.close();
	}

}
