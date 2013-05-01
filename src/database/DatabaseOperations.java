package database;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

public class DatabaseOperations {
	DBCollection table;
	DB db;
	public DatabaseOperations() {
		try {
			MongoClient mongoClient = new MongoClient("localhost" , 27017 );
			
			db = mongoClient.getDB("CIS542");
			//Set<String> colls = db.getCollectionNames();

			//for (String s : colls) {
			  //  System.out.println("collections:" + s);
			//}
			
			table = db.getCollection("tempReading");
			
		}catch(Exception e) {
			e.printStackTrace();
		}
			
	}
	
	public boolean insert(long date, String temp, String city) {
		
		BasicDBObject document = new BasicDBObject();
		document.put("date", date);
		document.put("temp", temp);
		document.put("city",city);
		table.insert(document);
		
		return true;
	}
	
	public boolean update(Date oldDate, String oldTemp, String oldCity, Date date, String temp, String city) {
		
		BasicDBObject query = new BasicDBObject();
		query.put("date", oldDate);
		query.put("city", oldCity);
	 
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.put("date", date);
		newDocument.put("city", city);
	 
		BasicDBObject updateObj = new BasicDBObject();
		updateObj.put("$set", newDocument);
		
		table.update(query, updateObj);
		
		return true;
		
		
	}
	
	public DBCursor find(long date, String city) {
		
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("date", date);
		searchQuery.put("city", city);
	 
		DBCursor cursor = table.find(searchQuery);
		
		return cursor;
		
	}
	
	public DBCursor findAfterSomeDate(long date, String city) {
		
		BasicDBObject query = new BasicDBObject("date", new BasicDBObject("$gt", date));
		DBCursor cursor = table.find(query);
		return cursor;
	}
	
	public ArrayList<String> getTempSince(long date, String city) {
		ArrayList<String> temps = new ArrayList<String>();
		
		DBCursor cursor = findAfterSomeDate(date, city);
		while(cursor.hasNext()) {
			temps.add((String)cursor.next().get("temp"));
		}
		System.out.println("temp since data retrieved from DB");
		cursor.close();
		return temps;
	}
	
	public ArrayList<String> getTempHour(long date, String city) {
		ArrayList<String> temps = new ArrayList<String>();
		long oneHour = 3600 * 1000;
		
		DBCursor cursor = findAfterSomeDate(date - oneHour, city);
		while(cursor.hasNext()) {
			temps.add((String)cursor.next().get("temp"));
		}
		cursor.close();
		return temps;
	}
	
	public ArrayList<String> getTemp24Hour(long date, String city) {
		ArrayList<String> temps = new ArrayList<String>();
		long one24Hour = 3600 * 1000 * 24;
		
		DBCursor cursor = findAfterSomeDate(date - one24Hour, city);
		while(cursor.hasNext()) {
			temps.add((String)cursor.next().get("temp"));
		}
		cursor.close();
		return temps;
	}
	
	public ArrayList<String> getTempOneMinute(long date, String city) {
		ArrayList<String> temps = new ArrayList<String>();
		long oneMinute = 60*1000;
		
		DBCursor cursor = findAfterSomeDate(date - oneMinute, city);
		while(cursor.hasNext()) {
			temps.add((String)cursor.next().get("temp"));
		}
		cursor.close();
		return temps;
	}
	
	public boolean remove(long date, String city) {
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("date", date);
		searchQuery.put("city", city);
		 
		WriteResult r = table.remove(searchQuery);
		return true;	
	}
	
	public static void main(String[] args) {
		
		try {
			DatabaseOperations operation = new DatabaseOperations();
			String temp = "23.345";
			String city = "sensor";
			Date now = new Date();
			 
			operation.insert(now.getTime(), temp, city);
			
			//DBCursor cursor = operation.find(now.getTime(), "sensor");
			ArrayList<String> temps = operation.getTempHour(now.getTime(), "sensor");
			for(int i=0; i<temps.size(); i++) {
				System.out.println(temps.get(i));
			}
			System.out.println("________");
			ArrayList<String> temps24 = operation.getTemp24Hour(now.getTime(), "sensor");
			for(int i=0; i<temps24.size(); i++) {
				System.out.println(temps24.get(i));
			}
			
			System.out.println("________");
			ArrayList<String> temps1Min = operation.getTempOneMinute(now.getTime(), "sensor");
			for(int i=0; i<temps1Min.size(); i++) {
				System.out.println(temps1Min.get(i));
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		

	}

}
