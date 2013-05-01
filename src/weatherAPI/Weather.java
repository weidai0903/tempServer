package weatherAPI;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Weather {
	private static String weatherUrl = "http://free.worldweatheronline.com/feed/weather.ashx";
	private static String key = "ca00ea9c9d004448131903";
	
	public static void main(String[] args) {
		Weather w = new Weather();
		try {
			Document doc = w.getDoc(5, "");
			if(doc != null) {
				ArrayList<String[]> forcast = w.getTempForecast(doc);
				for (int i=0; i<forcast.size(); i++) {
					String[] temp =  forcast.get(i);
					System.out.println(temp[0] + " " + temp[1] + " " 
							+ temp[2] + " " + temp[3] + " " + temp[4]);
				}
			}else {
				System.out.println("fail to retrieve weather data");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String[]> getTempForecast(int days, String city){
		Document doc = getDoc(days, city);
		ArrayList<String[]> forecast = null;
		if(doc != null) {
			forecast = getTempForecast(doc);
		}
		return forecast;

	}
	
	public Document getDoc (int numDays, String city) {
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
	
	public ArrayList<String[]> getTempForecast(Document doc){
		
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
	}
	/*
	public String excuteGet(String targetURL)
	  {
	    URL url;
	    HttpURLConnection connection = null;  
	    try {
	      //Create connection
	      url = new URL(targetURL);
	      connection = (HttpURLConnection)url.openConnection();	
	      InputStream is = connection.getInputStream();
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	      String line;
	      StringBuffer response = new StringBuffer(); 
	      while((line = rd.readLine()) != null) {
	        response.append(line);
	        response.append('\r');
	      }
	      rd.close();
	      return response.toString();

	    } catch (Exception e) {

	      e.printStackTrace();
	      return null;

	    } finally {

	      if(connection != null) {
	        connection.disconnect(); 
	      }
	    }
	  }
	*/
	
	private Document getDOM(String xml) throws Exception{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(xml);
		System.out.println("parse completed");
		return doc;
	}

}
