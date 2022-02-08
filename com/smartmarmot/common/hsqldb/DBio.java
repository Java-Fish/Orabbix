package com.smartmarmot.common.hsqldb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.smartmarmot.zabbix.ZabbixItem;

public class DBio {
	
	
	protected byte[] serialise(ZabbixItem _zi) {
		  try {
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    ObjectOutputStream oos = new ObjectOutputStream(bos);
		    oos.writeObject(_zi);
		    oos.flush();
		    return bos.toByteArray();
		  } catch (IOException e) {
		    throw new IllegalArgumentException(e);
		  }
		}

		protected ZabbixItem deserialise(byte[] byteArray) {
		  try {
		    ObjectInputStream oip = new ObjectInputStream(new ByteArrayInputStream(byteArray));
		    return (ZabbixItem) oip.readObject();
		  } catch (IOException e) {
		    throw new IllegalArgumentException(e);
		  } catch (ClassNotFoundException e) {
		    throw new IllegalArgumentException(e);
		  }
		}
		
		

}
