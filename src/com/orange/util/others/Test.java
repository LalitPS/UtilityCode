package com.orange.util.others;





public class Test {

	public boolean getData(){
		return false;
	}
	
	public static void main(String ad[]){
		Test T = new Test();
		
		String  two="Yes";
		String one="";
		
		if(null != one && T.getData())
		{
			System.out.println(" In for");
		}
		else
		{
			System.out.println(" not In for");
		}
		
	}
}
