package com.orange.util.objectstore;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class WriteObject {

	
	public WriteObject(){
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<String> readObject(String fileName,Object obj) throws IOException, ClassNotFoundException{
		FileInputStream fis = new FileInputStream(fileName);
        ObjectInputStream ois = new ObjectInputStream(fis);
        ArrayList<String> anotherList = (ArrayList<String>) ois.readObject();
        ois.close();
        return anotherList;
	}
	
	public void writeObject(String fileName,Object obj) throws IOException{
		FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(obj);
        oos.close();
	}
}
