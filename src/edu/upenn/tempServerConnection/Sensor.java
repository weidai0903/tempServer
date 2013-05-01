package edu.upenn.tempServerConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Sensor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public Sensor() {
		
	}
	
	/**
	 * give true if you want to display celcious;therwise fahrenhite. 
	 * @param celcius
	 * @return
	 */
	public boolean setDisplayOnSensor(boolean celcius) {
		
		try {
			String request;
			if(celcius) {
				request = "dispCe";
			}else {
				request = "dispFa";
			}
			String r = sendRequest(request);
			if(r.contains("success")) {
				return true;
			}else {
				return false;
			}
			
		}catch(IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	public boolean fakeSetDisplayOnSensor(boolean returnValue) {
		return returnValue;
	}
	
	public boolean showTempOnSensor() {
		try {
			String request;
			request = "startsensor";
			String r = sendRequest(request);
			if(r.contains("success")) {
				return true;
			}else {
				return false;
			}
			
		}catch(IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	public boolean fakeShowHideTempOnSensor(boolean returnValue) {
		return returnValue;
	}
	
	public boolean hideTempOnSensor() {
		try {
			String request;
			request = "shutdownsensor";
			String r = sendRequest(request);
			if(r.contains("success")) {
				return true;
			}else {
				return false;
			}
			
		}catch(IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private String sendRequest(String request) throws IOException {
		String serverIP = Server.getServerIP();
		String addr = serverIP + request;
		URL url = new URL(addr);
		System.out.println("sending request:" + addr + " to server");
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(15*1000);
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        System.out.println("receiving data:");
        String inputLine;
        StringBuilder sb = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine);
        	System.out.println(inputLine);
        }
        in.close();
        
        String receive = sb.toString();
        //System.out.println("stringbulder:"+receive);
        return receive;
	}
	

}
