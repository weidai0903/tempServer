package tempService;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import database.DatabaseOperations;

public class TempReader implements Runnable
{
	private static InputStream in;
	private static OutputStream out;
	private static double tempNo = 1000;
	private static double previousTemp = 0;
	private static boolean isPrevious = false;
	private static final int motorMaxSpeed = 250;
	private static int readingCount = 0;
	static int init = 0;
	static boolean error;
	private static boolean autoSpeedOn = false;
	private static boolean ledClose = false;
	private static boolean ledOpen = false;

	public static void closeLed(boolean close) {
		ledClose = true;
		ledOpen = false;
	}
	public static void openLed(boolean open) {
		ledOpen = true;
		ledClose = false;
	}

	static int ledNum = 0;
	static int ledColor = 0;
	static String ledText = null;
	static String ledCmd = null;


	public static void setLedColor(int t) {
		System.out.println("led color set to " + t);
		ledColor = t;
	}
	public static void setLedNum(int text) {
		ledNum = text;
	}
	public static void setLedText(String text) {
		ledText = text;
	}

	public static void setLedCmd(String text) {
		ledCmd = text;
	}

	public static int getMotorSpeed() {
		return init;
	}

	public static void motorSpeedUp(int num) {
		if(init + num <= motorMaxSpeed) {
			init+=num;
		}
	}
	public static void motorSpeedDown(int num) {
		if(init - num >= 0) {
			init-=num;
		}
	}

	public static double getTempReading() {
		return tempNo;
	}
	public TempReader()
	{
		super();
	}

	public static int writeToSerial(int cmd) {
		try{
			Thread writingThread;
			writingThread = new Thread(new SerialWriter(out, cmd));
			writingThread.start();
			writingThread.join();
			System.out.println("writing complete cmd="+cmd);
			if(error)
				return 0;
			else
				return 1;
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("writing thread fail to join");
			return 0;
		}

	}

	public static boolean autoMotorSpeedOn(boolean on) {
		autoSpeedOn = on;
		return true;
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

				(new Thread(new SerialReader(in))).start();
				//(new Thread(new SerialWriter(out))).start();

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
			int len = -1;
			try
			{
				if(writeToSerial('7') == 1) {
					System.out.println("motor initialized to 0");
				}else {
					System.out.println("fail to initialize motor to 0");
				}

				//System.out.println("going to read");
				while ( ( len = this.in.read(buffer)) > -1 )
				{

					//System.out.print("read from serial port:");
					String temp = new String(buffer,0,len);
					//System.out.println(new String(buffer,0,len));

					Pattern pattern = Pattern.compile("<(\\+|\\-)\\d+.\\d+>");
					//Pattern pattern = Pattern.compile()
					Matcher matcher = pattern.matcher(temp);
					// Check all occurance
					//if (matcher.matches()) {
					if(matcher.lookingAt()) {
						int start = matcher.start();
						int end = matcher.end();
						if(temp.charAt(start+1) == '+') {

							tempReading = temp.substring(start+2, end-1);
						}else {
							tempReading = temp.substring(start+1, end-1);
						}
						//System.out.println("match reading:" + tempReading);
						tempNo = Double.parseDouble(tempReading);

						if(autoSpeedOn) {
							/*Dongheng: control motor according to current temperature.*/
							if( isPrevious ){
								if( tempNo > (previousTemp + 0.1) && init < motorMaxSpeed){
									System.out.println(tempNo + ":tempReader start to speed up motor");
									if(TempReader.writeToSerial('4') == 1) {
										motorSpeedUp(10);
										//init += 10;
										System.out.println("speedup to "+ init);
									}else {
										System.out.println("fail");
									}
									previousTemp = tempNo;
								}
								else if ( tempNo < previousTemp - 0.1 && init > 0) {
									System.out.println(tempNo + ":tempReader start to speed down motor");
									if(TempReader.writeToSerial('5') == 1) {
										//init -=10;
										motorSpeedDown(10);
										System.out.println("speed down to " + init);
									}else {
										System.out.println("fail");
									}
									previousTemp = tempNo;
								}
							}
							else{
								isPrevious = true;
								previousTemp = tempNo;
							}
							//previousTemp = tempNo;
							/*Dongheng: control motor according to current temperature ends here.*/

						}

						if(readingCount % 30 == 0) {
							DatabaseOperations dbOps = new DatabaseOperations();
							dbOps.insert(new Date().getTime(), Double.toString(tempNo), "sensor");
							System.out.println("stored temp=" + tempNo + " to table 'tempReading'");
						}

					}else {
						//System.out.println("not matched");
					}

					try{
						Thread.sleep(1000);
					}catch(Exception e) {
						e.printStackTrace();
					}

					readingCount ++;
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

				//int c = 0;
				error = false;
				System.out.println("trying to write to serial:" + (char)cmd);

				if(cmd != '9') {

					if(cmd == '4') {
						if(init+10<=motorMaxSpeed) {
							motorSpeedUp(10);
							System.out.println("cmd speedup written to serial:" + cmd);
						}
					}
					else if(cmd == '5') {
						if(init-10>=0) {
							motorSpeedDown(10);
							//this.out.write(cmd);
							System.out.println("cmd speeddown written to serial:" + cmd);
						}
					}
					else if(cmd == '7'){
						System.out.println(" critical: trying to write to serial "+ (char)cmd);
						init = 0;
						//this.out.write(cmd);
						System.out.println(" critical: cmd shutdown motor written to serial:"+ cmd);
					}
					else if(cmd == '6') {
						init = motorMaxSpeed;
						//this.out.write(cmd);
						System.out.println("cmd max motor speed written to serial:"+ cmd);
					}
					this.out.write(cmd);
					this.out.flush();
				}

				else {
					LEDMatrixGenerator led = new LEDMatrixGenerator();

					if(ledClose) {
						this.out.write(cmd);
						System.out.println("going to close LED");

						int[][] result;

						result = led.getOff();

						for (int i=0; i<8; i++) {
							for (int j=0; j<3; j++) {
								this.out.write(result[i][j]);
								System.out.print(result[i][j] + " ");
								//this.out.write();
							}
						}
						this.out.flush();
						ledClose = false;
					}else if(ledOpen) {
						this.out.write(cmd);
						System.out.println("going to open LED");

						int[][] result;
						if(ledColor == 0) {	
							result = led.getOnRed();
						}else {
							result = led.getOnGreen();
						}

						for (int i=0; i<8; i++) {
							for (int j=0; j<3; j++) {
								this.out.write(result[i][j]);
								System.out.print(result[i][j] + " ");
							}
						}
						this.out.flush();
						ledOpen = false;
					}
					else if(ledCmd!=null) {
						ArrayList<int[][]> result = null;
						LedTextWriter writer = null;
						
						if(ledCmd.equals("love")) {
							if(ledColor == 0) {
								result = toArrayList(led.getLoveRed());
							}else {
								result = toArrayList(led.getLoveGreen());
							}
							writer = new LedTextWriter(result, this.out, 1000);
						}
						else if(ledCmd.equals("snake")) {
							if(ledColor == 0) {
								result = toArrayList(led.getSnakeRed());
							}else {
								result = toArrayList(led.getSnakeGreen());
							}
							writer = new LedTextWriter(result, this.out, 100);

						}
						else if(ledCmd.equals("name")) {
							if(ledColor == 0) {
								result = toArrayList(led.getLoveZxRed());
							}else {
								result = toArrayList(led.getLoveZxGreen());
							}
							writer = new LedTextWriter(result, this.out, 1000);
						}
						else if(ledCmd.equals("sensorTemp")) {
							
							int numToDisplay = (int)getTempReading();
							System.out.println("going to write number "+ numToDisplay + " on LED with color "+ ledColor);

							int[][] resultMatrix;
							if(ledColor == 0) {
								resultMatrix = led.getMatrixRed(numToDisplay);
							}else {
								resultMatrix = led.getMatrixGreen(numToDisplay);
							}

							for (int i=0; i<8; i++) {
								for (int j=0; j<3; j++) {
									this.out.write(resultMatrix[i][j]);
									System.out.print(resultMatrix[i][j] + " ");
									//this.out.write();
								}
							}
							System.out.println();
							this.out.flush();
							return;
							
						}
						ledCmd = null;
						Thread writingThread;
						writingThread = new Thread(writer);
						writingThread.start();
					}
					else if(ledText!=null) {

						ArrayList<int[][]> result = null;
						LedTextWriter writer = null;
						
						System.out.println("going to write LED color:" + ledColor);
						int charIndex;

						String capital = ledText.toUpperCase();
						capital = capital.replace(" ", "");
						int stringLen = capital.length();

						int[][][] mapping;
						if(ledColor == 0) {
							mapping = led.getTextRed();
						}else {
							mapping = led.getTextGreen();
						}

						result = new ArrayList<int[][]>(); 

						System.out.println("going to write LED text:" + capital + ";length="+ stringLen);
						for(int i=0; i<stringLen; i++) {
							int ascii = capital.charAt(i);
							System.out.println("get char " + (char)ascii);
							if(ascii >=65 && ascii <= 90) {
								charIndex = ascii - 'A';
								result.add(mapping[charIndex]);
								System.out.println((char)ascii + " add to result");
							}else if(ascii >= 48 && ascii <= 57) {
								charIndex = ascii - '0' + 26;
								result.add(mapping[charIndex]);
								System.out.println((char)ascii + " add to result");
							}else if(ascii >= 33 && ascii <=47) {
								charIndex = ascii - '!' + 26 + 10;
								result.add(mapping[charIndex]);
								System.out.println((char)ascii + " add to result");
							}else if(ascii>=58 && ascii <=64) {
								charIndex = ascii - ':' + 26 + 10 + 15;
								result.add(mapping[charIndex]);
								System.out.println((char)ascii + " add to result");
							}else if(ascii>=91 && ascii <=96) {
								charIndex = ascii - '[' + 26 + 10 + 15 + 7;
								result.add(mapping[charIndex]);
								System.out.println((char)ascii + " add to result");
							}else if(ascii>=123 && ascii <=126) {
								charIndex = ascii - '{' + 26 + 10 + 15 + 7 + 6;
								result.add(mapping[charIndex]);
								System.out.println((char)ascii + " add to result");
							}

						}

						System.out.println("num " + result.size() + " result size");
						writer = new LedTextWriter(result, this.out, 1000);

						ledText = null;
						Thread writingThread;
						writingThread = new Thread(writer);
						writingThread.start();	
						
					}else {
						this.out.write(cmd);
						int numToDisplay = ledNum;
						System.out.println("going to write number "+ numToDisplay + " on LED with color "+ ledColor);

						int[][] result;
						if(ledColor == 0) {

							result = led.getMatrixRed(numToDisplay);
						}else {
							result = led.getMatrixGreen(numToDisplay);
						}

						for (int i=0; i<8; i++) {
							for (int j=0; j<3; j++) {
								this.out.write(result[i][j]);
								System.out.print(result[i][j] + " ");
								//this.out.write();
							}
						}
						this.out.flush();
					}
				}

			}
			catch ( Exception e )
			{
				error = true;
				e.printStackTrace();
			}           
		}
	}

	public static ArrayList<int[][]> toArrayList(int [][][] m){
		ArrayList<int[][]> matrix = new ArrayList<int[][]> ();
		for(int i=0; i<m.length; i++) {
			matrix.add(m[i]);
		}
		return matrix;
	}

	/*
    public static void main ( String[] args )
    {
        try
        {
            (new TempReader()).connect("/dev/tty.usbmodem1421");
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
	 */
	@Override
	public void run() {
		try
		{
			(new TempReader()).connect("/dev/tty.usbserial-A600eB4I");
		}
		catch ( Exception e )
		{
			// TODO Auto-generated catch block
			System.out.println("tempReader not ready yet!");
			e.printStackTrace();
		}

	}

	public static class LedTextWriter implements Runnable{

		ArrayList<int[][]> matrix;
		OutputStream out;
		int time;
		public LedTextWriter(ArrayList<int[][]> matrix, OutputStream out, int time) {
			this.matrix = matrix;
			this.out = out;
			this.time = time;
		}
		@Override
		public void run() {
			try {

				System.out.println("matrix length:" + matrix.size());
				for(int k = 0; k<matrix.size(); k++) {
					this.out.write('9');
					int[][] matrixK = matrix.get(k);
					for (int i=0; i<8; i++) {
						for (int j=0; j<3; j++) {
							this.out.write(matrixK[i][j]);
							System.out.print(matrixK[i][j] + " ");
						}
					}
					this.out.flush();
					System.out.println( "\n done" + k + " num of letter written");
					Thread.sleep(time);
				}

				System.out.println("led writer complete");

			}catch(Exception e) {
				e.printStackTrace();
			}
		}

	}
}
