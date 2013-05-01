package edu.upenn.tempServerConnection;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class UnitTests {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void TempOperationsTest() {
		TempOperations to = new TempOperations();
		
		double latest = to.getLatestReading(true);
		System.out.println("latest reading from sensor in Ce:" + latest);
		
		latest = to.getLatestReading(false);
		System.out.println("latest reading from sensor in Fa:" + latest);
		
		try {
			
			boolean r = to.initialize();
			if(!r) {
				System.out.println("initialization failed");
				fail();
			}
			
			System.out.println("tesing getAllTempOneHour");
			ArrayList <String> temps = to.getAllTempOneHour();
			if(temps.size() == 0) {
				System.out.println("zero temps received");
			}else {
				System.out.println("receiving;");
				for(int i=0; i<temps.size(); i++) {
					System.out.println(temps.get(i));
				}
			}
			
			System.out.println("tesing getAllTemp24Hours");
			temps = to.getAllTemp24Hours();
			if(temps.size() == 0) {
				System.out.println("zero temps received");
			}else {
				System.out.println("receiving:");
				for(int i=0; i<temps.size(); i++) {
					System.out.println(temps.get(i));
				}
			}
			
			double[] data1 = to.getTempDataOneHour();
			System.out.println("min max ave of 1 hour:");
			for(int i=0; i<data1.length; i++) {
				System.out.println(data1[i]);
			}
			double[] data2 = to.getTempData24Hours();
			System.out.println("min max ave of 24 hour:");
			for(int i=0; i<data2.length; i++) {
				System.out.println(data1[2]);
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	@Test
	public void MotorTest() {
		
		System.out.println("motor tests:");
		
		Motor m = new Motor();
		String cSpeed = m.getCurrrentSpeed();
		System.out.println("motor speed:" + cSpeed);
		
		boolean r = m.maxSpeedMotor();
		System.out.println("maxSpeedMotor:" + r);
		
		r = m.shutdownMotor(0);
		System.out.println("shutdown motor:" + r);
		
		r = m.speeddownMotor();
		System.out.println("speeddown motor:" + r);
		
		r = m.speedupMotor();
		System.out.println("speedup motor:" + r);
	}
	
	@Test
	public void SensorTest() {
		
		Sensor s = new Sensor();
		
		boolean r = s.hideTempOnSensor();
		System.out.println("sensor hide temp reading:" + r);
		
		r = s.setDisplayOnSensor(true);
		System.out.println("sensor display celcius reading:" + r);
		
		r = s.setDisplayOnSensor(false);
		System.out.println("sensor display fahrenhite reading:" + r);
		
		r = s.showTempOnSensor();
		System.out.println("sensor show temp reading:" + r);

	}
	
	@Test
	public void WeatherTest() {
		Weather w = new Weather();
		
		System.out.println("weather test for philly:");
		System.out.println("current conditions:");
		String[] result = w.getCurrentCondition("Philadelphia");
		System.out.println(result);
		
		try {
			
			System.out.println("future 5days forecast:");

				int days = 5;
				String city = "new york";
				ArrayList<String[]> forcast = w.getTempForecast(days, city);
				
				for (int i=0; i<forcast.size(); i++) {
					String[] temp =  forcast.get(i);
					System.out.println(temp[0] + " " + temp[1] + " " 
							+ temp[2] + " " + temp[3] + " " + temp[4]);
				}
				
		}catch(Exception e) {
			e.printStackTrace();
		}
	
	}
	
}
