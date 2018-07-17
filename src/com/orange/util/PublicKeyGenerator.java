package com.orange.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;


public class PublicKeyGenerator {

	private static final String ALGO = "AES";
	private static final byte[] keyValue = new byte[] { 'T', 'h', 'e', 'G', 'o', 'l', 'd', 'S', 'e', 'c', 'r','e', 't', 'K', 'e', 'y' };
	
	public PublicKeyGenerator(){
		
	}
	public static String encrypt(String Data) throws Exception {
	    Key key = generateKey();
	    Cipher c = Cipher.getInstance(ALGO);
	    c.init(Cipher.ENCRYPT_MODE, key);
	    byte[] encVal = c.doFinal(Data.getBytes());
	    byte[] encryptedValue = Base64.encodeBase64(encVal);
	   return new String(encryptedValue, StandardCharsets.UTF_8);
	  
	}
	
	public static String decrypt(String password) throws Exception 
	{
		String decryptedPassword="";
		decryptedPassword = decryptInternal(password);
    	return decryptedPassword;
	}
	
	private static String decryptInternal(String actPassword) throws Exception,NoSuchAlgorithmException, NoSuchPaddingException,InvalidKeyException, IOException, IllegalBlockSizeException,
	BadPaddingException 
	{
	
		Key key = generateKey();
		Cipher c = Cipher.getInstance(ALGO);
		c.init(Cipher.DECRYPT_MODE, key);
		byte[] decordedValue = Base64.decodeBase64(actPassword.getBytes());
		
		
		byte[] decValue = c.doFinal(decordedValue);
		String decryptedPassword = new String(decValue,StandardCharsets.UTF_8);
		return decryptedPassword;
	}
	
	private static Key generateKey() throws Exception {
	    Key key = new SecretKeySpec(keyValue, ALGO);
	    return key;
	}
	/*
	public static void main(String ad[]){
		try {
			//String enc = PublicKeyGenerator.encrypt("GOLD_READ");
			//System.out.println(enc);
			//String dnc = PublicKeyGenerator.decrypt(enc);
			//System.out.println(dnc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	
}
