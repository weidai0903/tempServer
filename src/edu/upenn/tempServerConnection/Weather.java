package edu.upenn.tempServerConnection;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import android.util.Log;

public class Weather {
	
	private static String weatherUrl = "http://free.worldweatheronline.com/feed/weather.ashx";
	private static String key = "ca00ea9c9d004448131903";
	
	public static void main(String[] args) {
		
	}
	
	/**
	 * returns arraylist size of days; returns null is error happens
	 * each of the element is [date, tempMax, tempMin, weatherIconUrl, weatherDesc];
	 * The first entry is the current condition
	 * @return
	 */
	public ArrayList<String[]> getTempForecast(int days, String city){
		Document doc = getDoc(days, city);
		ArrayList<String[]> forecast = null;
		if(doc != null) {
			forecast = getTempForecast(doc);
		}
		return forecast;
	}
	
	/**
	 * returns String[] of 
	 * [observation_time, temp_Celcius, weatherIconUrl, weatherDescription, weatherCode]
	 * @param city
	 * @return
	 */
	public String[] getCurrentCondition(String city) {
		Document doc = getDoc(0, city);
		String[] forecast = null;
		if(doc != null) {
			forecast = getCurrentConditionHelper(doc);
		}
		return forecast;
	}
	
	private Document getDoc (int numDays, String city) {
		String qCity = city.replace(" ", "%20");
		String query = "?q=" + qCity + "&format=xml&num_of_days="+ numDays+ 
				"&key=" + key;
		String URL = weatherUrl + query;
		System.out.println("URL:" + URL);
		try {
			Document doc = getDOM(URL);
			return doc;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	private String[] getCurrentConditionHelper(Document doc) {
		
		try {
			NodeList currents = doc.getElementsByTagName("current_condition");
			Node current = currents.item(0);
			Element e = (Element) current ;
			String observationTime = e.getElementsByTagName("observation_time").item(0).getTextContent();
			String currentTempC = e.getElementsByTagName("temp_C").item(0).getTextContent();
			String weatherIcon = e.getElementsByTagName("weatherIconUrl").item(0).getTextContent();
			String desc = e.getElementsByTagName("weatherDesc").item(0).getTextContent();
			String weatherCode = e.getElementsByTagName("weatherCode").item(0).getTextContent();
			
			String[] currentTemp = new String[10];
			currentTemp[0] = observationTime;
			currentTemp[1] = currentTempC;
			currentTemp[2] = weatherIcon;
			currentTemp[3] = desc;
			currentTemp[4] = weatherCode;
			
			return currentTemp;
		}catch(Exception e) {
			return null;
		}
		
	}
	
	private ArrayList<String[]> getTempForecast(Document doc){
		
		try {
			ArrayList<String[]> forcast = new ArrayList<String[]>();
			NodeList nList = doc.getElementsByTagName("weather");
			 		 
			for (int temp = 0; temp < nList.getLength(); temp++) {
				String[] temps = new String[10];
				Node nNode = nList.item(temp);
				//System.out.println("\nCurrent Element :" + nNode.getNodeName());
		 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					//System.out.println("date : " + eElement.getAttribute("id"));
					temps[0] = eElement.getElementsByTagName("date").item(0).getTextContent();
					temps[1]  = eElement.getElementsByTagName("tempMaxC").item(0).getTextContent();
					temps[2] = eElement.getElementsByTagName("tempMinC").item(0).getTextContent();	
					temps[3] = eElement.getElementsByTagName("weatherIconUrl").item(0).getTextContent();
					temps[4] = eElement.getElementsByTagName("weatherDesc").item(0).getTextContent();
				}
				forcast.add(temps);
			}
			return forcast;
		}catch(Exception e) {
			//Log.v("exception", e.getMessage());
			return null;
		}
	}

	
	private Document getDOM(String xml) throws Exception{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(xml);
		System.out.println("parse completed");
		return doc;
	}

	public Weather() {
		
	}
	
}
