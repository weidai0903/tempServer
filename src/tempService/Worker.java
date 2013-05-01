package tempService;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import database.DatabaseOperations;
/**
 * @author Wei Dai
 *
 */
public class Worker implements Runnable{
	protected Socket clientSocket = null;
	protected String serverText   = null;
	public static boolean stopped=false;
	private static String serverHostName;
	private static int serverPortNum;
	private static String serverIP;
	private OutputStream output;
	private static String clientIP;
	private static String clientHostName;
	private static int clientPort;
	public static OutputStream socketOut;
	public static String getServerIP(){
		return serverIP;
	}
	public static int getClientPort(){
		return clientPort;
	}
	public static String getClientHostName(){
		return clientHostName;
	}
	public static String getClientIP(){
		return clientIP;
	}
	public static String getServerHostName(){
		return serverHostName;
	}
	public static int getServerPortNum(){
		return serverPortNum;
	}
	public OutputStream getOutputStream(){
		return output;
	}
	public void run() {
		while(!stopped){
			try {
				clientSocket=ThreadPool.mon.remove();
				System.out.println("start processing client socket");
				if(clientSocket==null) break;
				if(clientSocket.isClosed()) continue;
				serverHostName=clientSocket.getInetAddress().getHostName();
				serverPortNum=clientSocket.getPort();
				clientIP=clientSocket.getLocalAddress().getHostAddress();
				clientHostName=clientSocket.getLocalAddress().getHostName();
				clientPort=clientSocket.getLocalPort();
				serverIP=clientSocket.getInetAddress().getHostAddress();
				InputStream input  = clientSocket.getInputStream();
				output = clientSocket.getOutputStream();
				socketOut=output;

				PrintWriter out = new PrintWriter(output, true);

				//create the request object to handle the inputStream
				String inputLine;
				StringBuilder builder = new StringBuilder();
				BufferedReader in = new BufferedReader(new InputStreamReader(input));

				inputLine = in.readLine();
				while (inputLine != null && !inputLine.trim().contains("HTTP")) {   
					System.out.print(inputLine);
					builder.append(inputLine);
					inputLine = in.readLine();
				}
				builder.append(inputLine);
				System.out.println("worker get request:" + builder.toString());

				String cmd = builder.toString();
				System.out.println("cmd:" + cmd);

				DatabaseOperations operation = new DatabaseOperations();

				StringBuilder sb = new StringBuilder();

				sb.append("HTTP/1.1 200 OK\r\n");
				sb.append("CONTENT-TYPE: TEXT/HTML\r\n");
				sb.append("\r\n");

				if(cmd.matches(".*/getTemp/.*")) {
					if(cmd.matches(".*/getTemp/24hours.*")) {
						Date now = new Date();
						ArrayList<String> temps24 = operation.getTemp24Hour(now.getTime(), "sensor");
						sb.append(temps24.size());
						sb.append(";");
						for(int i=0; i<temps24.size(); i++) {
							sb.append(temps24.get(i));
							sb.append(";");
						}
					}else if(cmd.matches(".*/getTemp/since=.*")) {

						Pattern pattern = Pattern.compile("since=\\d*");
						Matcher matcher = pattern.matcher(cmd);
						try {
							String since = null;
							if(matcher.find()) {
								int start = matcher.start();
								int end = matcher.end();
								since = cmd.substring(start+6, end);
							}
							Long sinceLong = Long.parseLong(since);
							System.out.println("getTemp since " + sinceLong);
							ArrayList<String> temps = operation.getTempSince(sinceLong, "sensor");
							sb.append(temps.size());
							sb.append(";");
							for(int i=0; i<temps.size(); i++) {
								sb.append(temps.get(i));
								sb.append(";");
							}
						}catch(Exception e) {
							e.printStackTrace();
						}

					}else if(cmd.matches(".*/getTemp/1hour.*")) {

						Date now = new Date();
						ArrayList<String> temps = operation.getTempHour(now.getTime(), "sensor");
						sb.append(temps.size());
						sb.append(";");
						for(int i=0; i<temps.size(); i++) {
							sb.append(temps.get(i));
							sb.append(";");
						}

					}else if(cmd.matches(".*/getTemp/1min.*")) {
						Date now = new Date();
						ArrayList<String> temps = operation.getTempOneMinute(now.getTime(), "sensor");
						sb.append(temps.size());
						sb.append(";");
						for(int i=0; i<temps.size(); i++) {
							sb.append(temps.get(i));
							sb.append(";");
						}
					}
				}else {
					if(cmd.matches(".*/dispFa.*"))
					{
						System.out.println("worker start to display Fahrenheit");
						if(TempReader.writeToSerial('1') == 1) {
							sb.append("successful");
						}else {
							sb.append("fail");
						}
					}
					else if(cmd.matches(".*/dispCe.*"))
					{
						System.out.println("worker start to display centigrade");
						if(TempReader.writeToSerial('0') == 1) {
							sb.append("successful");
						}else {
							sb.append("fail");
						}
					}
					else if(cmd.matches(".*/shutdownsensor.*"))
					{
						System.out.println("worker start to shut down sensor");
						if(TempReader.writeToSerial('2') == 1) {
							sb.append("successful");
						}else {
							sb.append("fail");
						}
					}
					else if(cmd.matches(".*/startsensor.*"))
					{
						System.out.println("worker start to start sensor");
						if(TempReader.writeToSerial('3') == 1) {
							sb.append("successful");
						}else {
							sb.append("fail");
						}
					}
					else if(cmd.matches(".*/motorSpeed.*"))
					{
						System.out.println("worker start to give current motor speed");
						sb.append(TempReader.getMotorSpeed());
					}
					else if(cmd.matches(".*/autoSpeedOn.*")) {
						System.out.println("worker start to turn on auto motor speed");
						if(TempReader.autoMotorSpeedOn(true))
							sb.append("success");
						else
							sb.append("false");
					}else if(cmd.matches(".*/autoSpeedOff.*")) {
						System.out.println("worker start to turn off auto motor speed");
						if(TempReader.autoMotorSpeedOn(false))
							sb.append("success");
						else
							sb.append("false");
					}

					else if(cmd.matches(".*/speedup.*"))
					{
						System.out.println("worker start to speed up motor");
						if(TempReader.writeToSerial('4') == 1) {
							sb.append("successful");
						}else {
							sb.append("fail");
						}
					}
					else if(cmd.matches(".*/speeddown.*"))
					{
						System.out.println("worker start to speed down motor");
						if(TempReader.writeToSerial('5') == 1) {
							sb.append("successful");
						}else {
							sb.append("fail");
						}
					}
					else if(cmd.matches(".*/shutdownmotor.*"))
					{
						System.out.println("worker start to shut down motor");
						if(TempReader.writeToSerial('7') == 1) {
							sb.append("successful");
						}else {
							sb.append("fail");
						}
					}
					else if(cmd.matches(".*/maxmotor.*"))
					{
						System.out.println("worker start to max motor speed");
						if(TempReader.writeToSerial('6') == 1) {
							sb.append("successful");
						}else {
							sb.append("fail");
						}
					}

					else if(cmd.matches(".*/led=.*"))
					{
						TempReader.setLedCmd(null);
						TempReader.setLedText(null);
						System.out.println("led number display");
						Pattern pattern = Pattern.compile("led=\\d*");
						Matcher matcher = pattern.matcher(cmd);
						// Check all occurance
						//if (matcher.matches()) {
						if(matcher.find()) {
							int start = matcher.start();
							int end = matcher.end();
							String text = cmd.substring(start+4, end);
							System.out.println("worker receive led display " + text);
							int ledNum = Integer.parseInt(text);
							TempReader.setLedNum(ledNum);
						}else {
							System.out.println("led number match no number");
						}

						if(TempReader.writeToSerial('9') == 1) {
							sb.append("successful");
						}else {
							sb.append("fail");
						}
					}
					else if(cmd.matches(".*/color=.*"))
					{
						System.out.println("led color display");
						Pattern pattern = Pattern.compile("color=\\d*");
						Matcher matcher = pattern.matcher(cmd);
						// Check all occurance
						//if (matcher.matches()) {
						if(matcher.find()) {
							int start = matcher.start();
							int end = matcher.end();
							String text = cmd.substring(start+6, end);
							System.out.println("worker receive led color " + text);
							int ledColor = Integer.parseInt(text);
							TempReader.setLedColor(ledColor);
						}else {
							System.out.println("led color not match");
						}
						sb.append("successful");
						/*
	    	 			if(TempReader.writeToSerial('9') == 1) {
	    	 				sb.append("successful");
	    	 			}else {
	    	 				sb.append("fail");
	    	 			}
						 */
					}
					else if(cmd.matches(".*/love.*"))
					{

						System.out.println("led love display");
						String ledText = "love";
						TempReader.setLedCmd(ledText);

						if(TempReader.writeToSerial('9') == 1) {
							sb.append("successful");
						}else {
							sb.append("fail");
						}
					}
					else if(cmd.matches(".*/snake.*"))
					{
						System.out.println("led snake display");
						String ledText = "snake";
						TempReader.setLedCmd(ledText);

						if(TempReader.writeToSerial('9') == 1) {
							sb.append("successful");
						}else {
							sb.append("fail");
						}
					}

					else if(cmd.matches(".*/ledtext=.+/.*"))
					{
						TempReader.setLedCmd(null);
						System.out.println("led text");
						Pattern pattern = Pattern.compile("ledtext=.+?/");
						Matcher matcher = pattern.matcher(cmd);
						// Check all occurance
						//if (matcher.matches()) {
						if(matcher.find()) {
							int start = matcher.start();
							int end = matcher.end();
							String text = cmd.substring(start+8, end-1);
							System.out.println("worker receive led text " + text);
							TempReader.setLedText(text);
						}else {
							System.out.println("led color not match");
						}

						if(TempReader.writeToSerial('9') == 1) {
							sb.append("successful");
						}else {
							sb.append("fail");
						}
					}
					else if(cmd.matches(".*/name.*"))
					{
						System.out.println("led name");
						TempReader.setLedCmd("name");
						if(TempReader.writeToSerial('9') == 1) {
							sb.append("successful");
						}else {
							sb.append("fail");
						}
					}else if(cmd.matches(".*/closeled.*"))
					{
						System.out.println("close led");
						TempReader.closeLed(true);
						if(TempReader.writeToSerial('9') == 1) {
							sb.append("successful");
						}else {
							sb.append("fail");
						}
					}
					else if(cmd.matches(".*/openled.*"))
					{
						System.out.println("open led");
						TempReader.openLed(true);
						if(TempReader.writeToSerial('9') == 1) {
							sb.append("successful");
						}else {
							sb.append("fail");
						}
					}
					else if(cmd.matches(".*/ledShowTemp.*")) {
						/*TempReader.setLedText(null);
	    	 			System.out.println("show temp on led");
	    	 			TempReader.setLedCmd("sensorTemp");
	    	 			if(TempReader.writeToSerial('9') == 1) {
	    	 				sb.append("successful");
	    	 			}else {
	    	 				sb.append("fail");
	    	 			}*/

						TempReader.setLedCmd(null);
						TempReader.setLedText(null);
						System.out.println("led show current temp");

						int ledNum = (int)TempReader.getTempReading();
						TempReader.setLedNum(ledNum);

						if(TempReader.writeToSerial('9') == 1) {
							sb.append("successful");
						}else {
							sb.append("fail");
						}
					}
					//else if (cmd.matches("\\.*getTemp\\.*")) 
					else{
						double temp = TempReader.getTempReading();
						System.out.println("temperature going to client:" + Double.toString(temp));
						sb.append(Double.toString(temp));
					}
				}

				out.print(sb.toString());
				System.out.println("sent to client:" + sb.toString() + "\n **********************");
				out.close();
				in.close();
				output.close();
				input.close();
				clientSocket.close();    

				System.out.println("worker finish processing");
			} catch (IOException e) {
				HttpServer.errorLog.append("<p>"+"Error stack trace:"+"</p>");
				for(int i=0;i<e.getStackTrace().length;i++){
					HttpServer.errorLog.append("<p>"+e.getStackTrace()[i].toString()+"</p>");
				}
				e.printStackTrace();
			}
		}
	}
}

