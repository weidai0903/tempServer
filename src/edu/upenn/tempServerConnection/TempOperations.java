package edu.upenn.tempServerConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;

public class TempOperations {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}
	
	ArrayList<String> tempInHour;
	ArrayList<String> tempIn24Hours;
	String latestReading;
	
	/**
	 * call this function each time before you want to get the NEWEST temp data in Hour or 24 hours
	 * Otherwise the data returned by all the functions except getLatestReading() will give you the old data
	 * @return
	 */
	public boolean initialize(){
		try {
			String request = "getTemp/1hour";
			String receive = getResponse(request);
			 ArrayList<String> r = new  ArrayList<String>();
	        String[] temps = receive.split(";");
	        int numTemps = Integer.parseInt(temps[0]);
	        for(int i = 0; i<numTemps; i++) {
	        	r.add(temps[i+1]);
	        }
	        tempInHour = r;
	        
	        request = "getTemp/24hours";
			receive = getResponse(request);
			ArrayList<String> r1 = new  ArrayList<String>();
			temps = receive.split(";");
	        numTemps = Integer.parseInt(temps[0]);
	        for(int i = 0; i<numTemps; i++) {
	        	r1.add(temps[i+1]);
	        }
	        tempIn24Hours = r1;
	        
	        return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
        
	}
	
	/**
	 * return the latest reading; in Celsious if given true; otherwise Fahrenheit
	 * @param celcius
	 * @return
	 */
	public Double getLatestReading(boolean celsius) {
		
		try {
			String request = "getTemp";
			String receive = getResponse(request);
			Double reading = Double.parseDouble(receive);
			
			if(celsius) {
				return reading;
			}else {
				return reading*1.8+32;
			}
			
			
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Double getFakeLatestReading(boolean celcious) {
		Random r = new Random();
		return r.nextInt(1000)*1.0;
	}
	
	/**
	 * double of [min, max, average] on sensor in one hour;NULL if error happens;
	 * @return
	 */
	public double[] getTempDataOneHour(){
		return getTempMinMaxAverage(tempInHour);
	}
	
	public double[] getFakeTempDataOneHour() {
		Random r = new Random();
		double[] d = new double[3];
		d[0] = r.nextInt(1000)*1.0;
		d[1] = r.nextInt(1000)*1.0;
		d[2] = r.nextInt(1000)*1.0;
		return d;
	}
	
	
	/**
	 * double of [min, max, average] on sensor in 24 hours;NULL if error happens;
	 * @return
	 */
	public double[] getTempData24Hours() {
		return getTempMinMaxAverage(tempIn24Hours);
	}
	
	/**
	 * Returns all the temp readings on sensor in one hour; NULL if error happens; 
	 * @return
	 */
	public ArrayList<String> getAllTempOneHour(){
		return tempInHour;
	}
	public ArrayList<String> fakeGetAllTempOneHour(int num){
		ArrayList<String> temps = new ArrayList<String>();
		for(int i=0; i<num; i++) {
			Random random = new Random();
			double t = 27 * random.nextDouble();
			temps.add(String.format("%1$,.2f", t));
		}
		return temps;
			
	}
	
	/**
	 * Returns all the temp readings on sensor in 24 hours; NULL if error happens;
	 * @return
	 */
	public ArrayList<String> getAllTemp24Hours(){
		return tempIn24Hours;
	}
	
	
	private double[] getTempMinMaxAverage(ArrayList<String> temps){
		
		if(temps == null)
			return null;
		double min = 1000;
		double max = 0;
		double sum = 0;
		int len = temps.size();
		for(int i=0; i<len; i++) {
			double tmp = Double.parseDouble(temps.get(i));
			if( tmp < min)
				min = tmp;
			if(tmp > max)
				max = tmp;
			sum += tmp;
		}
		double r[] = new double[3];
		r[0] = min;
		r[1] = max;
		if(len >0)
			r[2] = sum/len;
		else
			r[2] = 0;
		return r;
	}
	
	
	
	private String getResponse(String request) throws Exception {
		String serverIP = Server.getServerIP();
		String addr = serverIP + request;
		URL url = new URL(addr);
		System.out.println("sending request:" + addr + " to server");
        URLConnection connection = url.openConnection();
        //connection.
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
