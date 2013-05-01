package tempService;

import java.net.*;
import java.io.IOException;
/**
 * @author Wei Dai
 *
 */
public class HttpServer implements Runnable{
	
  private static ServerSocket serverSocket = null;
  protected static boolean controlMode=false;
  //private Thread runningThread= null;
  public static String rootDir;
  public int port;
  //create a thread pool with 15 workers
  protected ThreadPool threadPool = new ThreadPool(8);
  public static Thread serverT;
  
  public static int serverPortNum;
  //check if shutdown
  public static boolean stopped=false; 
  public static String XMLFilePath;
  public static StringBuilder errorLog=new StringBuilder();
  public static ServerSocket getServerSocket(){
	  return serverSocket;
  }
  public static void main(String[] args) {
	  if(args.length==1){
		  //XMLFilePath=args[2];
		  HttpServer server = new HttpServer();
		  server.port=Integer.parseInt(args[0]);
		  //rootDir=args[1];
		  serverT=new Thread(server);
		  //dispatcher start to work
		  serverT.start();
		  System.out.println("Http Server start running");
		  
		  TempReader tempReader = new TempReader();
		  Thread readTemp = new Thread(tempReader);
		  readTemp.start();
		  System.out.println("tempReader start running");
		  /*
		  MotorController motor = new MotorController();
		  Thread motorT = new Thread(motor);
		  motorT.start();
		  */
		  
		  //thread start, preparing thread pool and add jobs to it.
		  try {
			  	//wait for dispatcher die
			  	serverT.join();
			  	readTemp.join();
			  	System.out.println("Dispatcher and readTemp thread end");
		  		//Thread.currentThread().join();
		  } catch (InterruptedException e) {
	    	  HttpServer.errorLog.append("<p>"+"Error:"+"</p>");
	    	  HttpServer.errorLog.append("<p>"+"Socket dispatcher interrupted"+"</p>");
		      e.printStackTrace();
		  }
		  Thread[] workerThreads=ThreadPool.getWorkingThreads();
		  //wait for all worker threads die
		  for(int i=0;i<workerThreads.length;i++){
			  try{
				  workerThreads[i].join();
			  }catch(InterruptedException e){
				  System.out.println("waiting for all workers die not succussful");
			  }
		  }
		  try{
			  readTemp.join();
		  }catch(InterruptedException e){
			  System.out.println("readTemp join failed");
		  }
	  }else {
		  System.out.println("usage:portNo");
	  }
	  	  
  }
  
  public static String serverTState(){
	  if(serverT.getState()==Thread.State.WAITING) return "waiting";
	  else return "running";
  }
  
  public void run(){
      //tell the threadPool to let the workForce wait
	  try {
		    serverSocket = new ServerSocket(this.port);
		} 
		catch (IOException e) {
		    System.out.println("Could not listen on port:"+ this.port);
		    System.exit(-1);
		}
      this.threadPool.assignWorker();
      
      while(true){
          Socket clientSocket = null;
          //System.out.println("creating server socket");
          try {
              clientSocket = serverSocket.accept();
              System.out.println("client socket created");
              serverPortNum=serverSocket.getLocalPort();
              if(clientSocket.isClosed()==true) {System.out.println("Client Socket closed");break;}
              this.threadPool.handleSocket(clientSocket);
          } catch (IOException e) {
        	  e.printStackTrace();
        	  HttpServer.errorLog.append("<p>"+"Error stack trace:"+"</p>");
        	  for(int i=0;i<e.getStackTrace().length;i++){
        		  HttpServer.errorLog.append("<p>"+e.getStackTrace()[i].toString()+"</p>");
        	  }
  			  System.out.println("Server Socket closed");
        	  break;
          }
      }
  }
}