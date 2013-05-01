package edu.upenn.tempServerConnection;

public class Server {
	
	private static String ip = "158.130.105.255";
	private static String port = "8088";
	public static String getServerIP() {
		String addr = "http://" + ip + ":" + port + "/";
		return addr;
	}

}
