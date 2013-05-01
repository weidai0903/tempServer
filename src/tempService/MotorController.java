package tempService;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import database.DatabaseOperations;

public class MotorController implements Runnable
{
	private static InputStream in;
	private static OutputStream out;
	private static double tempNo = 1000;
	//private static double 
	
	public static double getTempReading() {
		return tempNo;
	}
    public MotorController()
    {
        super();
    }
    
    public static int writeToSerial(int cmd) {
    	Thread writingThread;
    	writingThread = new Thread(new SerialWriter(out, cmd));
    	writingThread.start();
    	try{
    		writingThread.join();
    	}catch(Exception e) {
    		e.printStackTrace();
    		System.out.println("writing thread fail to join");
    		return 0;
    	}
    	System.out.println("writing complete");
    	return 1;
    }
    
    void connect ( String portName ) throws Exception
    {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open("tempReader",200000000);
            
            if ( commPort instanceof SerialPort )
            {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                in = serialPort.getInputStream();
                out = serialPort.getOutputStream();
                
                //(new Thread(new SerialReader(in))).start();
                
                /*
                int cmd = 'W';
                Thread writeThread;
                writeThread = new Thread(new SerialWriter(out, cmd));
                writeThread.start();
                
                try{
                	writeThread.join();
                }catch(Exception e) {
                	e.printStackTrace();
                }
                */
                while(true);
           
            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }
    
    
    /** */
    public static class SerialReader implements Runnable 
    {
        InputStream in;
        
        public SerialReader ( InputStream in )
        {
            this.in = in;
        }
        
        public void run ()
        {
            byte[] buffer = new byte[1024];
            String tempReading;
            //double tempNo;
            int len = -1;
            try
            {
            	//System.out.println("going to read");
                while ( ( len = this.in.read(buffer)) > -1 )
                {
                	//System.out.print("read from serial port:");
                	String temp = new String(buffer,0,len);
                    //System.out.println(new String(buffer,0,len));
                    
                    Pattern pattern = Pattern.compile("<(\\+|\\-)\\d+.\\d+>");
                    Matcher matcher = pattern.matcher(temp);
                    // Check all occurance
                    if (matcher.matches()) {
                    	int start = matcher.start();
                    	int end = matcher.end();
                    	if(temp.charAt(start+1) == '+') {
                    		tempReading = temp.substring(start+2, end-1);
                    	}else {
                    		tempReading = temp.substring(start+1, end-1);
                    	}
                    	//System.out.println(tempReading);
                    	tempNo = Double.parseDouble(tempReading);
                    	//DatabaseOperations dbOps = new DatabaseOperations();
                    	//dbOps.insert(new Date(), Double.toString(tempNo), "sensor");
                    	//System.out.println("stored temp=" + tempNo + " to table 'tempReading'");
                    	
                    }else {
                    	System.out.println("not matched");
                    }
                    
                    try{
                    	Thread.sleep(1000);
                    }catch(Exception e) {
                    	e.printStackTrace();
                    }
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }            
        }
    }

    /** */
    public static class SerialWriter implements Runnable 
    {
        OutputStream out;
        int cmd;
        
        public SerialWriter ( OutputStream out, int cmd ) 
        {
            this.out = out;
            this.cmd = cmd;
        }
        
        public void run () 
        {
            try
            {
	                this.out.write(cmd);
	                System.out.println("wrting to serial port:" + cmd);
	
		            this.out.flush();
		            System.out.println("wrting complete");

            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }           
        }
    }
    
    public static void main ( String[] args )
    {
        try
        {
            (new MotorController()).connect("/dev/tty.usbmodem1411");
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }
	@Override
	public void run() {
		try
        {
            (new MotorController()).connect("/dev/tty.usbmodem1411");
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
		
	}
}
