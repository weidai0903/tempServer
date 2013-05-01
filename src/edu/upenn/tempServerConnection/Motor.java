package edu.upenn.tempServerConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

public class Motor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public Motor() {
		
	}
	
	public String getCurrrentSpeed() {
		try {
			String request;
			request = "motorSpeed";
			String r = sendRequest(request);
			return r;
			
		}catch(IOException e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	public boolean autoSpeedOn(boolean on) {
		try {
			
			String request;
			if(on) {
				request = "autoSpeedOn";
			}else {
				request = "autoSpeedOff";
			}
			String r = sendRequest(request);
			if(r.contains("success"))
				return true;
			else
				return false;
			
		}catch(IOException e) {
			e.printStackTrace();
			return false;
		}	
	}
	
	public String fakeGetCurrentSpeed() {
		Random r= new Random();
		int speed = r.nextInt(255);
		return Integer.toString(speed);
	}
	
	
	
	public boolean shutdownMotor(int delay) {
		try {
			Thread.sleep(delay*1000);
			String request;
			request = "shutdownmotor";
			String r = sendRequest(request);
			if(r.contains("success")) {
				return true;
			}else {
				return false;
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * use speedup instead
	 * @param delay
	 * @return
	 */
	public boolean startupMotor(int delay) {
		return false;
	}
	
	public boolean maxSpeedMotor() {
		try {
			String request;
			request = "maxmotor";
			String r = sendRequest(request);
			if(r.contains("success")) {
				return true;
			}else {
				return false;
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public boolean speedupMotor() {
		try {
			String request;
			request = "speedup";
			String r = sendRequest(request);
			if(r.contains("success")) {
				return true;
			}else {
				return false;
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public boolean speeddownMotor() {
		try {
			String request;
			request = "speeddown";
			String r = sendRequest(request);
			if(r.contains("success")) {
				return true;
			}else {
				return false;
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	public boolean fakeChangeSpeedMotor(boolean returnValue) {
		return returnValue;
	}
	
	private String sendRequest(String request) throws IOException {
		
		String serverIP = Server.getServerIP();
		String addr = serverIP + request;
		URL url = new URL(addr);
		System.out.println("sending request:" + addr + " to server");
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(3*1000);
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
