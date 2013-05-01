package facebook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import tempService.TempReader;
import weatherAPI.Weather;

import edu.upenn.tempServerConnection.TempOperations;
import facebook.org.json.JSONArray;
import facebook.org.json.JSONObject;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import com.restfb.types.User;

public class MultiTaskFB {	
	Weather weather = new Weather();
	private static String simiKey = "53e12160-65b9-4cb2-93d7-9bcf56f036d7";
	private static String token = "BAACEdEose0cBAIQ9XkVDPBfk2mqNa3qPt64vpRmZBf2cOg23VLoMiCo4bEzFCIoq3N6qlEMNZCZCpfZASJLiuh75Bhb0HI7xby3tiWVQvPS7YLHEM5rSIZAC9M1yXwgRMnBHnRy2J67P2elx4eM1Oa0uHf8dQxdIhikqVTk1eGkPyiZCuZAhkH6d00iB4m8R4ANOMCVAtgu6ZB69BZAZCeOfGLYW2VYrrCMvbleMEWcu6cUAZDZD"; 
	public static void main(String[] args) throws Exception{
		MultiTaskFB multi = new MultiTaskFB();
		TriggledUpdated tu = multi.new TriggledUpdated();
		tu.start();
		RegularUpdated ru = multi.new RegularUpdated();
		ru.start();
	}


class TriggledUpdated extends Thread {
	private long lastUpdatedTime = 0;
	private long latestTime;
	private JSONArray array;
	private JSONObject jsonObj;
	private String accessToken;
	private String url;
	String postId;
	String postAuthor;
	
	public TriggledUpdated() {
		super();
	}
	
	public void run(){
		url = "https://graph.facebook.com/234246026720922/feed?access_token=";
    	accessToken = token; 
    	url += accessToken;
    	FacebookClient facebookClient = new DefaultFacebookClient(accessToken);
    	User user = facebookClient.fetchObject("me", User.class);
    	System.out.println("Triggled User name: " + user.getName());
    	while(true){
    		try{
    			Thread.sleep(1000);
	    		CheckUpdatedRequest(url,facebookClient);
	    		//System.out.println("Done~");    	  		
    		}
    		catch(Exception e){
    			e.printStackTrace();
    			System.out.println(e.getMessage());
    		}
    	}		
	}
	
	// Check whether the page info has been changed	
    public void CheckUpdatedRequest(String url,FacebookClient facebookClient)
    		throws Exception{
        boolean judge = UpdatedLatestTime(url);
    	if(judge){
    		//Thread.sleep(10000);
    		AddingNewPost(jsonObj,facebookClient,0,array);
    		lastUpdatedTime = latestTime;
    	}
    }
    
    
    public void PostCurrentTemp(FacebookClient facebookClient,JSONObject jsonObj){
    	Date now = new Date();
    	String msg = now.toGMTString();
    	try{
    		FacebookType publishMessageResponse = facebookClient.publish("234246026720922/feed", 
    				FacebookType.class, Parameter.with("message", msg));
    	 	System.out.println("Triggled Published feed post ID: " 
    				+ publishMessageResponse.getId() + " type:" 
    	 			+ publishMessageResponse.getType() );
    	 	
    	}
    	catch(Exception e){
    		System.out.println(e.getMessage());
    	}    	
    }
    
    public boolean UpdatedLatestTime(String url) throws Exception{
    	URL fb_url = new URL(url);
    	URLConnection fb_conn = fb_url.openConnection();
    	BufferedReader in = new BufferedReader(new InputStreamReader(fb_conn.getInputStream()));
    	String inputLine;

    	inputLine = in.readLine();
    	in.close();

    	jsonObj = new JSONObject(inputLine);        	 
    	array = jsonObj.getJSONArray("data");

    	//Check first
    	jsonObj = array.getJSONObject(0); 

	      String templatestTime = jsonObj.getString("created_time");
	      postId = jsonObj.getString("id");
	      //postAuthor = jsonObj.getString("name");
	      //System.out.println("latest postId:" + postId);
	      
	      templatestTime = templatestTime.replace('T', ' ');
	      templatestTime = templatestTime.substring(0,templatestTime.indexOf("+"));
	      Timestamp ts = Timestamp.valueOf(templatestTime);
	      latestTime = ts.getTime();
	      
	      if(lastUpdatedTime == 0 || latestTime <= lastUpdatedTime){  
	      	lastUpdatedTime = latestTime;
	      	System.out.println("no new post detected");
	      	return false;
	      }
	      else if(latestTime > lastUpdatedTime){
	    	  System.out.println("New Post Time is " + latestTime + " and Old Time is " + lastUpdatedTime);
	    	  //lastUpdatedTime = latestTime;
	    	  return true;
	      }
	      
	      return false;
          
    }
    
    public void AddingNewPost(JSONObject jsonObj,FacebookClient facebookClient
    		,int i,JSONArray array) throws Exception{
    	
    	String templatestTime = jsonObj.getString("created_time");
        templatestTime = templatestTime.replace('T', ' ');
        templatestTime = templatestTime.substring(0,templatestTime.indexOf("+"));
        Timestamp ts = Timestamp.valueOf(templatestTime);
        long localTime = ts.getTime();
    	System.out.println("going to respond with comment");

    	while(localTime > lastUpdatedTime && i < array.length()-1){
    		/*
        	if(jsonObj.has("story")){
	          	  String msg_content = jsonObj.getString("story");
	          	  String msg_time = jsonObj.getString("created_time");
	          	  //String author = jsonObj.getString("name");
	          	  System.out.println("msg_content" + msg_content);
	          	  //System.out.println("post author: "+ author);
	          	  if(msg_content.toLowerCase().contains("sensor") || msg_content.toLowerCase().contains("foo")){
	            	  	System.out.println("New Request: " + msg_content + ":" + msg_time);
	
	            	  	System.out.println("going to respond to new request");
	            	  	//PostCurrentTemp(facebookClient, jsonObj);
	            	  	
	            	  	String url = "/"
	            	 			+ postId + "/comments";
	                	System.out.println("going to comment on post:"+ url);
	                    String msg = "this is a comment";
	                    FacebookType publishMessageResponse = facebookClient.publish(url, FacebookType.class, 
	            	 			Parameter.with("message", msg));
	            	 	System.out.println("publishMessageResponse:"+publishMessageResponse.getId()
	            	 			+ "type:" + publishMessageResponse.getType());
	            	 	System.out.println("comment made on : " + postId);
	            	  	
	            	  	Thread.sleep(1000);
	            	  	System.out.println("After Post");
	            	  	
	            	  	//if(UpdatedLatestTime(url))
	            	 	lastUpdatedTime = localTime;
	          	  }
            }
            */
    		String author = "kitty";
    		JSONObject from = jsonObj.getJSONObject("from");
    		author = from.getString("name");
    		System.out.println("from author:" + author);
    		
        	if(!author.equals("Tempmaniac") && (jsonObj.has("message")||jsonObj.has("story"))){
        		
        		String msg_content;
        		if(jsonObj.has("story")){
        			msg_content = jsonObj.getString("story");
        			System.out.println("goes to story");
        		}
        		else if(jsonObj.has("message")){      			
            		msg_content = jsonObj.getString("message");
            		System.out.println("goes to message");
        		}else {
        			msg_content = null;
        			System.out.println("msg_content is null");
        		}
        		String msg_time = jsonObj.getString("created_time"); 
        		System.out.println("New Request: " + msg_content + ":" + msg_time);
        		
        		
          	  	
          	  	//System.out.println("post time: " + msg_time );
          	  	System.out.println("lastUpdatedTime:" + lastUpdatedTime);
          	  	
          	  	String msg;
          	  	String msgContentLower = msg_content.toLowerCase();
          	  	if(msgContentLower.contains("sensor")){
          	  		//msg = "78 degree Fahrenheit...just kidding";
          	  		
          	  		TempOperations to = new TempOperations();
          	  		Double reading = to.getLatestReading(true);
          	  		if(reading == null)
          	  			msg = "temperature server currently not available.";
          	  		else {
          	  			System.out.println("msg = " + Double.toString(reading));
          	  			msg = "The current sensor reading is:" + Double.toString(reading);
          	  		}
            	  	//PostCurrentTemp(facebookClient,jsonObj);
            	  	
          	  	}
          	  	else if(msgContentLower.matches(".*data.*(one|1).*")){
        	  		//msg = "78 degree Fahrenheit...just kidding";
        	  		
          	  		TempOperations to = new TempOperations();
        	  		to.initialize();
        	  		double[] reading = to.getTempDataOneHour();
        	  		if(reading == null)
        	  			msg = "temperature server currently not available.";
        	  		else {
        	  			
        	  			System.out.println("temp data retrieved");
        	  			msg = "In the past hour: Low temp is " + reading[0] + " Highest temp is "
        	  					+ reading[1] + " Average temps is " + reading[2];
        	  		}
          	  	//PostCurrentTemp(facebookClient,jsonObj);
          	  	
        	  	}
          	  else if(msgContentLower.matches(".*data.*24.*")){
      	  		//msg = "78 degree Fahrenheit...just kidding";
      	  		
      	  		TempOperations to = new TempOperations();
      	  		to.initialize();
      	  		double[] reading = to.getTempData24Hours();
      	  		if(reading == null)
      	  			msg = "temperature server currently not available.";
      	  		else {
      	  			
      	  			System.out.println("temp data retrieved");
      	  			msg = "In the past 24 hour: Low temp is " + reading[0] + " Highest temp is "
      	  					+ reading[1] + " Average temps is " + reading[2];
      	  		}
        	  	//PostCurrentTemp(facebookClient,jsonObj);
        	  	
      	  	}
          	  	else if(msgContentLower.matches(".*temp.*")) {
          	  		
          	  		if(msgContentLower.matches(".*local.*temp.*")
          	  				||msgContentLower.matches(".*(philadelphia)|(philly).*temp.*")){
	          	  		ArrayList<String[]> forecast = weather.getTempForecast(1, "philadelphia");
	                	String day1[] = forecast.get(0);
	                	msg = "Philadelphia: " + day1[0] + " Max Temperature is " 
                			+ day1[1] + ". Min Temperature is " + day1[2] + "(in centigrade)";
	          	  		System.out.println("msg = " + msg);
          	  		}
          	  		else if(msgContentLower.matches(".*new york.*temp.*")){
          	  			
	          	  		ArrayList<String[]> forecast = weather.getTempForecast(1, "new york");
	                	String day1[] = forecast.get(0);
	                	msg = "new york: " + day1[0] + " Max Temperature is " 
            			+ day1[1] + ". Min Temperature is " + day1[2] + "(in centigrade)";	          	  		System.out.println("msg = " + msg);
          	  		}
          	  		else {
          	  			msg = "please say \"temperature in new york (or philadelphia) to get temp\"";
          	  		}
          	  	}
          	  	else if(msgContentLower.matches(".*shutdown.*")) {
          	  		msg = "shutdown server ...";
          	  		
          	  	}
          	  	else {
          	  		try {
          	  			String tempMsg = msg_content.replaceAll(" ", "%20");
          	  			String targetURL = "http://sandbox.api.simsimi.com/request.p?key="
          	  					+ simiKey + "&lc=en&ft=1.0&text=" + tempMsg;
          	  			URL url = new URL(targetURL);
          	  			System.out.println("going to call simisimi by "+targetURL);
          	  			HttpURLConnection connection = (HttpURLConnection)url.openConnection();	
          	  			InputStream is = connection.getInputStream();
          	  			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
          	  			String line;
          	  			StringBuffer response = new StringBuffer(); 
          	  			while((line = rd.readLine()) != null) {
          	  				response.append(line);
          	  			}
          	  			rd.close();
          	  			String simiResponse = response.toString();
          	  			
          	  			JSONObject json = new JSONObject(simiResponse);        	  
          	        
          	  			msg = json.getString("response");
          	  			msg = msg.replaceAll("simisimi", "TempManiac");
          	  			System.out.println("simi responds with "+ msg);
          	  		}catch(Exception e) {
          	  			e.printStackTrace();
          	  			msg = "Oops! request not supported.";
          	  		}
          	  	}
          	  	
          	  	String postedMsg = "Hello " + author + ", "  + msg;
          	  	String url = "/"
      	 			+ postId + "/comments";
          	  	System.out.println("going to comment on post:"+ url);
	           
	            FacebookType publishMessageResponse = facebookClient.publish(url, FacebookType.class, 
	      	 			Parameter.with("message", postedMsg));
	      	 	System.out.println("publishMessageResponse:"+publishMessageResponse.getId()
	      	 			+ "type:" + publishMessageResponse.getType());
	      	 	System.out.println("comment made on : " + postId);
	      	  	            
	      	 	if(msg.contains("shutdown")) {
	      	 		postedMsg = postedMsg + " for you";
	      	 		FacebookType response = facebookClient.publish("234246026720922/feed", 
	    					FacebookType.class, Parameter.with("message", postedMsg));
	      	 		System.exit(0);
	      	 	}
	      	  	Thread.sleep(1000);
	      	  	System.out.println("After Post");
	      	  	
	      	  	//if(UpdatedLatestTime(url))
	      	 	lastUpdatedTime = localTime;
  	
            }
            
        	jsonObj = array.getJSONObject(++i);
        	templatestTime = jsonObj.getString("created_time");
            templatestTime = templatestTime.replace('T', ' ');
            templatestTime = templatestTime.substring(0,templatestTime.indexOf("+"));
            ts = Timestamp.valueOf(templatestTime);
            localTime = ts.getTime();
            System.out.println("post localTime:" + localTime);
        	
        }
    }
	
	
}

class RegularUpdated extends Thread{
	public RegularUpdated(){
		super();
	}
	
	public void run(){
		String url = "https://graph.facebook.com/234246026720922/feed?access_token=";
		String accessToken = token; 
    	url += accessToken;
    	FacebookClient facebookClient = new DefaultFacebookClient(accessToken);
    	User user = facebookClient.fetchObject("me", User.class);
    	System.out.println("Regular User name: " + user.getName());
    	while(true){
    		Date now = new Date();
    		
    		ArrayList<String[]> forecast = weather.getTempForecast(1, "philadelphia");
        	String day1[] = forecast.get(0);
        	String msg = "Philly:" + day1[0] + " Max Temperature is " 
        			+ day1[1] + ". Min Temperature is " + day1[2] + "(in centigrade)";
        	
        	msg = msg + now.toString();
    		try{
    			
    			FacebookType publishMessageResponse = facebookClient.publish("234246026720922/feed", 
    					FacebookType.class, Parameter.with("message", msg));
        	 	System.out.println("Regular Published message ID: " + publishMessageResponse.getId() 
        	 			+ " type:" + publishMessageResponse.getType());
        	 	
        	 	
    			Thread.sleep(1000 * 60 * 10);
    			//10 mins 
    		}
    		catch(Exception e){
    			System.out.println(e.getMessage());
    		}    		
    	}	
	}
}

}
