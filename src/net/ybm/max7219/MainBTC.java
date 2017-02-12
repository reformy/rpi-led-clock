package net.ybm.max7219;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.TimeZone;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class MainBTC
{
	private static File logFile = new File("/home/pi/ledclock.log");
	
	public static int SUMMER = 0;
	
	enum State
	{
		INIT,
		RUNNING,
		IP;
	}
	
	static State state = State.INIT;
	
	private void setStatus(State newState)
	{
		System.out.println("Setting state: " + state + " -> " + newState);
		state = newState;
	}
	
	public static void main(String[] args) throws Exception
	{
		new MainBTC().start();
	}
	
	enum SRC
	{
		BTCE,
		BLOCKCHAIN
	}
	
	public static void log(String msg)
	{
		try
		{
			System.out.println(msg);
			PrintWriter w = new PrintWriter(new FileWriter(logFile, true));
			w.println(new Date() + ": " + msg);
			w.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private Random rnd = new Random();
	
	int maxIntensity = 0xA;
	
	private void initDisplay()
	{
		System.out.println("init...");
		driver.sendTwice(Max7219Address.SCAN_LIMIT, 7);
		driver.sendTwice(Max7219Address.DECODE_MODE, 0xFF);
		driver.sendTwice(Max7219Address.DISPLAY_TEST, 0x0);
		driver.sendTwice(Max7219Address.INTENSITY, maxIntensity);
		driver.sendTwice(Max7219Address.SHUTDOWN, 1);
	}
	
	private void init()
	{
		initDisplay();
		
		driver.sendTwice(Max7219Address.DIGIT0, 0xF);
		driver.sendTwice(Max7219Address.DIGIT1, 0xF);
		driver.sendTwice(Max7219Address.DIGIT2, 0xF);
		driver.sendTwice(Max7219Address.DIGIT3, 0xF);
		driver.sendTwice(Max7219Address.DIGIT4, 0xF);
		driver.sendTwice(Max7219Address.DIGIT5, 0xF);
		driver.sendTwice(Max7219Address.DIGIT6, 0xF);
		driver.sendTwice(Max7219Address.DIGIT7, 0xF);
		
		int startT = 80;
		int deltaT = 6;
		for (int i = 0; i < 10; i++)
		{
			int t = startT;
			for (int j = 4; j >= 1; j--)
			{
				driver.send(j, new int[] { 0xA, 0xF });
				sleep(t);
				driver.send(j, new int[] { 0xF, 0xF });
				t -= deltaT;
			}
			for (int j = 8; j >= 5; j--)
			{
				driver.send(j, new int[] { 0xA, 0xF });
				sleep(t);
				driver.send(j, new int[] { 0xF, 0xF });
				t -= deltaT;
			}
			for (int j = 4; j >= 1; j--)
			{
				driver.send(j, new int[] { 0xF, 0xA });
				sleep(t);
				driver.send(j, new int[] { 0xF, 0xF });
				t += deltaT;
			}
			for (int j = 8; j >= 5; j--)
			{
				driver.send(j, new int[] { 0xF, 0xA });
				sleep(t);
				driver.send(j, new int[] { 0xF, 0xF });
				t += deltaT;
			}
			
			for (int j = 5; j <= 8; j++)
			{
				driver.send(j, new int[] { 0xF, 0xA });
				sleep(t);
				driver.send(j, new int[] { 0xF, 0xF });
				t -= deltaT;
			}
			for (int j = 1; j <= 4; j++)
			{
				driver.send(j, new int[] { 0xF, 0xA });
				sleep(t);
				driver.send(j, new int[] { 0xF, 0xF });
				t -= deltaT;
			}
			for (int j = 5; j <= 8; j++)
			{
				driver.send(j, new int[] { 0xA, 0xF });
				sleep(t);
				driver.send(j, new int[] { 0xF, 0xF });
				t += deltaT;
			}
			for (int j = 1; j <= 4; j++)
			{
				driver.send(j, new int[] { 0xA, 0xF });
				sleep(t);
				driver.send(j, new int[] { 0xF, 0xF });
				t += deltaT;
			}
		}
		MyTime.init();
		System.out.println("init done.");
	}
	
	Max7219Driver driver;
	
	private void start() throws Exception
	{
		final GpioController gpio = GpioFactory.getInstance();
		
		driver = new Max7219Driver(gpio, RaspiPin.GPIO_06, RaspiPin.GPIO_10,
		    RaspiPin.GPIO_11);
		
		GpioPinDigitalInput resetButton = gpio
		    .provisionDigitalInputPin(RaspiPin.GPIO_07, PinPullResistance.PULL_UP);
		resetButton.addListener(new GpioPinListenerDigital()
		{
			@Override
			public void handleGpioPinDigitalStateChangeEvent(
		      GpioPinDigitalStateChangeEvent event)
			{
				System.out.println(event.getPin() + " event: " + event.getState());
				if (event.getState() == PinState.HIGH)
		      // reset();
		      state = State.IP;
			}
		});
		
		GpioPinDigitalInput dsButton = gpio
		    .provisionDigitalInputPin(RaspiPin.GPIO_14, PinPullResistance.PULL_UP);
		dsButton.addListener(new GpioPinListenerDigital()
		{
			@Override
			public void handleGpioPinDigitalStateChangeEvent(
		      GpioPinDigitalStateChangeEvent event)
			{
				System.out.println(event.getPin() + " event: " + event.getState());
				if (event.getState() == PinState.HIGH)
					SUMMER = 1 - SUMMER;
			}
		});
		
		try
		{
			init();
			
			setStatus(State.RUNNING);
			while (true)
			{
				try
				{
					switch (state)
					{
						case RUNNING:
							// Show time.
							initDisplay();
							
							driver.sendTwice(Max7219Address.DECODE_MODE, 0xFF);
							for (int i = 0; i < 300; i++)
							{
								long now = System.currentTimeMillis();
								showTime(driver, maxIntensity, (i % 2) == 0);
								if (state != State.RUNNING)
									break;
								sleepTill(now + 1000);
							}
							
							int r = rnd.nextInt(100);
							if (r < 5)
							{
								showHallel(driver, maxIntensity);
								if (state != State.RUNNING)
									break;
								sleep(5000);
							}
							else if (r < 10)
							{
								showShahar(driver, maxIntensity);
								if (state != State.RUNNING)
									break;
								sleep(5000);
							}
							else if (r < 20)
							{
								showBtc(driver, maxIntensity);
							}
							break;
						
						case INIT:
							init();
							setStatus(State.RUNNING);
							break;
						
						case IP:
							showIp();
							break;
						
						default:
							throw new RuntimeException("Unknown state: " + state);
					}
				}
				catch (Exception e)
				{
					log(e.toString());
				}
			}
		}
		finally
		{
			gpio.shutdown();
		}
	}
	
	private void showIp()
	{
		try
		{
			int[] ip = getIp();
			if (ip != null)
			{
				initDisplay();
				driver.sendTwice(Max7219Address.DECODE_MODE, 0xFF);
				for (int iPart = 0; iPart < ip.length; iPart++)
				{
					int part = ip[iPart];
					showNumber(part);
					sleep(1000);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log("Failed showing ip: " + e.getMessage());
		}
		state = State.RUNNING;
	}
	
	private void showNumber(int n) throws Exception
	{
		sendToDriver(driver, getDigitOrNothing((n / 1000) % 10),
		    getDigitOrNothing((n / 100) % 10), getDigitOrNothing(n / 10) % 10, n % 10);
	}
	
	private int getDigitOrNothing(int digit)
	{
		return digit > 0 ? digit : 0xF;
	}
	
	private int[] getIp() throws Exception
	{
		Enumeration e = NetworkInterface.getNetworkInterfaces();
		NetworkInterface n = (NetworkInterface) e.nextElement();
		Enumeration ee = n.getInetAddresses();
		InetAddress i = (InetAddress) ee.nextElement();
		
		int[] ip = new int[4];
		int ii = 0;
		for (String part : i.getHostAddress().split("\\."))
			ip[ii++] = Integer.parseInt(part);
		return ip;
	}
	
	private void reset()
	{
		if (state == State.RUNNING)
			setStatus(State.INIT);
	}
	
	private void sleep(long ms)
	{
		try
		{
			Thread.sleep(ms);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private long showTime(Max7219Driver driver, int intensity, boolean showDots)
	    throws Exception
	{
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("IDT"));
		c.setTime(MyTime.getRealDate());
		int h = (c.get(Calendar.HOUR) + 2 + SUMMER) % 12;
		if (h == 0)
			h = 12;
		int m = c.get(Calendar.MINUTE);
		
		long sleepForNextMinute = (59 - c.get(Calendar.SECOND)) * 1000;
		long sleepTill = System.currentTimeMillis() + sleepForNextMinute;
		
		int dig0 = h / 10;
		if (dig0 == 0)
			dig0 = 0xF;
		int dig1 = h % 10;
		int dig2 = m / 10;
		int dig3 = m % 10;
		
		if (showDots)
		{
			dig0 |= 0x80;
			dig1 |= 0x80;
			dig2 |= 0x80;
			dig3 |= 0x80;
		}
		
		driver.sendTwice(Max7219Address.INTENSITY, intensity);
		
		sendToDriver(driver, dig0, dig1, dig2, dig3);
		
		return sleepTill;
	}
	
	private void showBtc(Max7219Driver driver, int maxIntensity) throws Exception
	{
		for (SRC curSrc : SRC.values())
		{
			try
			{
				// Show src name.
				driver.sendTwice(Max7219Address.DECODE_MODE, 0);
				driver.sendTwice(Max7219Address.INTENSITY, maxIntensity);
				switch (curSrc)
				{
					case BTCE:
						sendToDriver(driver, Max7219Alphabet.B, Max7219Alphabet.T,
						    Max7219Alphabet.C, Max7219Alphabet.E);
						break;
					
					case BLOCKCHAIN:
						sendToDriver(driver, Max7219Alphabet.B, Max7219Alphabet.L,
						    Max7219Alphabet.C, Max7219Alphabet.H);
						break;
					
					default:
						sendToDriver(driver, Max7219Alphabet.E, Max7219Alphabet.MAKAF,
						    Max7219Alphabet.N0, Max7219Alphabet.N1);
						break;
				}
				
				Thread.sleep(1000);
				
				// Show src value.
				double rateD = 0;
				switch (curSrc)
				{
					case BTCE:
						rateD = getRateBTCE();
						break;
					case BLOCKCHAIN:
						rateD = getRateBLCH();
						break;
					
					default:
						break;
				}
				
				int rate = (int) Math.round(rateD);
				int msd = rate / 1000;
				if (msd == 0)
				{
					msd = 0xF;
				}
				
				driver.sendTwice(Max7219Address.DECODE_MODE, 0xFF);
				driver.sendTwice(Max7219Address.INTENSITY, 0);
				sendToDriver(driver, msd, (rate / 100) % 10, (rate / 10) % 10,
				    rate % 10);
				
				for (int i = 1; i <= maxIntensity; i++)
				{
					Thread.sleep(100);
					driver.sendTwice(Max7219Address.INTENSITY, i);
				}
				
				// Thread.sleep(5000);
				for (int i = maxIntensity; i >= 0; i--)
				{
					driver.sendTwice(Max7219Address.INTENSITY, i);
					Thread.sleep(300);
				}
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
	}
	
	private void sendToDriver(Max7219Driver driver, Max7219Alphabet data0,
	    Max7219Alphabet data1, Max7219Alphabet data2, Max7219Alphabet data3)
	    throws Exception
	{
		driver.send(Max7219Address.DIGIT0, data0.data[0], Max7219Address.DIGIT0,
		    data2.data[0]);
		driver.send(Max7219Address.DIGIT4, data1.data[0], Max7219Address.DIGIT4,
		    data3.data[0]);
		Thread.sleep(sleepTimeMs);
		driver.send(Max7219Address.DIGIT1, data0.data[1], Max7219Address.DIGIT1,
		    data2.data[1]);
		driver.send(Max7219Address.DIGIT5, data1.data[1], Max7219Address.DIGIT5,
		    data3.data[1]);
		Thread.sleep(sleepTimeMs);
		driver.send(Max7219Address.DIGIT2, data0.data[2], Max7219Address.DIGIT2,
		    data2.data[2]);
		driver.send(Max7219Address.DIGIT6, data1.data[2], Max7219Address.DIGIT6,
		    data3.data[2]);
		Thread.sleep(sleepTimeMs);
		driver.send(Max7219Address.DIGIT3, data0.data[3], Max7219Address.DIGIT3,
		    data2.data[3]);
		driver.send(Max7219Address.DIGIT7, data1.data[3], Max7219Address.DIGIT7,
		    data3.data[3]);
	}
	
	private static long sleepTimeMs = 40;
	
	private void sendToDriver(Max7219Driver driver, int data0, int data1,
	    int data2, int data3) throws Exception
	{
		driver.send(Max7219Address.DIGIT0, data0, Max7219Address.DIGIT0, data2);
		driver.send(Max7219Address.DIGIT4, data1, Max7219Address.DIGIT4, data3);
		Thread.sleep(sleepTimeMs);
		driver.send(Max7219Address.DIGIT1, data0, Max7219Address.DIGIT1, data2);
		driver.send(Max7219Address.DIGIT5, data1, Max7219Address.DIGIT5, data3);
		Thread.sleep(sleepTimeMs);
		driver.send(Max7219Address.DIGIT2, data0, Max7219Address.DIGIT2, data2);
		driver.send(Max7219Address.DIGIT6, data1, Max7219Address.DIGIT6, data3);
		Thread.sleep(sleepTimeMs);
		driver.send(Max7219Address.DIGIT3, data0, Max7219Address.DIGIT3, data2);
		driver.send(Max7219Address.DIGIT7, data1, Max7219Address.DIGIT7, data3);
	}
	
	private void showShahar(Max7219Driver driver, int intensity) throws Exception
	{
		driver.sendTwice(Max7219Address.INTENSITY, intensity);
		
		Max7219LedMatrix ledMatrix = new Max7219LedMatrix(driver, 2);
		ledMatrix.start();
		
		long delay = 200;
		
		// Shin
		ledMatrix.set(1, 7, 2, true);
		ledMatrix.set(1, 4, 4, true);
		ledMatrix.set(1, 3, 2, true);
		sleep(delay);
		ledMatrix.set(1, 6, 2, true);
		ledMatrix.set(1, 5, 4, true);
		ledMatrix.set(1, 2, 2, true);
		sleep(delay);
		ledMatrix.set(1, 5, 2, true);
		ledMatrix.set(1, 6, 4, true);
		ledMatrix.set(1, 1, 2, true);
		sleep(delay);
		ledMatrix.set(1, 4, 2, true);
		ledMatrix.set(1, 7, 4, true);
		ledMatrix.set(1, 0, 2, true);
		sleep(delay);
		
		ledMatrix.set(1, 4, 3, true);
		ledMatrix.set(1, 3, 3, true);
		sleep(delay);
		ledMatrix.set(1, 5, 3, true);
		ledMatrix.set(1, 2, 3, true);
		sleep(delay);
		ledMatrix.set(1, 6, 3, true);
		ledMatrix.set(1, 1, 3, true);
		sleep(delay);
		ledMatrix.set(1, 7, 3, true);
		ledMatrix.set(1, 0, 3, true);
		sleep(delay);
		
		// het
		ledMatrix.set(0, 7, 4, true);
		sleep(delay);
		ledMatrix.set(0, 6, 4, true);
		sleep(delay);
		ledMatrix.set(0, 5, 4, true);
		sleep(delay);
		ledMatrix.set(0, 4, 4, true);
		sleep(delay);
		
		ledMatrix.set(0, 4, 0, true);
		sleep(delay);
		ledMatrix.set(0, 5, 0, true);
		sleep(delay);
		ledMatrix.set(0, 6, 0, true);
		sleep(delay);
		ledMatrix.set(0, 7, 0, true);
		sleep(delay);
		
		ledMatrix.set(0, 7, 2, true);
		sleep(delay);
		ledMatrix.set(0, 6, 2, true);
		sleep(delay);
		ledMatrix.set(0, 5, 2, true);
		sleep(delay);
		ledMatrix.set(0, 4, 2, true);
		sleep(delay);
		
		// resh
		ledMatrix.set(0, 3, 0, true);
		sleep(delay);
		ledMatrix.set(0, 2, 0, true);
		sleep(delay);
		ledMatrix.set(0, 1, 0, true);
		sleep(delay);
		ledMatrix.set(0, 0, 0, true);
		sleep(delay);
		
		ledMatrix.set(0, 0, 4, true);
		sleep(delay);
		ledMatrix.set(0, 1, 4, true);
		sleep(delay);
		ledMatrix.set(0, 2, 4, true);
		sleep(delay);
		ledMatrix.set(0, 3, 4, true);
		sleep(delay);
	}
	
	private void showHallel(Max7219Driver driver, int intensity) throws Exception
	{
		driver.sendTwice(Max7219Address.INTENSITY, intensity);
		
		Max7219LedMatrix ledMatrix = new Max7219LedMatrix(driver, 2);
		ledMatrix.start();
		
		long delay = 200;
		
		// hey
		ledMatrix.set(1, 7, 0, true);
		sleep(delay);
		ledMatrix.set(1, 6, 0, true);
		sleep(delay);
		ledMatrix.set(1, 5, 0, true);
		sleep(delay);
		ledMatrix.set(1, 4, 0, true);
		sleep(delay);
		
		ledMatrix.set(1, 4, 4, true);
		sleep(delay);
		ledMatrix.set(1, 5, 4, true);
		sleep(delay);
		ledMatrix.set(1, 6, 4, true);
		sleep(delay);
		ledMatrix.set(1, 7, 4, true);
		sleep(delay);
		
		ledMatrix.set(1, 5, 2, true);
		sleep(delay);
		ledMatrix.set(1, 4, 2, true);
		sleep(delay);
		
		// lamed
		ledMatrix.set(1, 1, 1, true);
		sleep(delay);
		ledMatrix.set(1, 0, 1, true);
		sleep(delay);
		
		ledMatrix.set(1, 3, 0, true);
		sleep(delay);
		ledMatrix.set(1, 2, 0, true);
		sleep(delay);
		ledMatrix.set(1, 1, 0, true);
		sleep(delay);
		ledMatrix.set(1, 0, 0, true);
		sleep(delay);
		
		ledMatrix.set(1, 0, 4, true);
		sleep(delay);
		ledMatrix.set(1, 1, 4, true);
		sleep(delay);
		ledMatrix.set(1, 2, 4, true);
		sleep(delay);
		ledMatrix.set(1, 3, 4, true);
		sleep(delay);
		
		ledMatrix.set(1, 0, 3, true);
		sleep(delay);
		ledMatrix.set(1, 1, 3, true);
		sleep(delay);
		
		// lamed
		ledMatrix.set(0, 5, 1, true);
		sleep(delay);
		ledMatrix.set(0, 4, 1, true);
		sleep(delay);
		
		ledMatrix.set(0, 7, 0, true);
		sleep(delay);
		ledMatrix.set(0, 6, 0, true);
		sleep(delay);
		ledMatrix.set(0, 5, 0, true);
		sleep(delay);
		ledMatrix.set(0, 4, 0, true);
		sleep(delay);
		
		ledMatrix.set(0, 4, 4, true);
		sleep(delay);
		ledMatrix.set(0, 5, 4, true);
		sleep(delay);
		ledMatrix.set(0, 6, 4, true);
		sleep(delay);
		ledMatrix.set(0, 7, 4, true);
		sleep(delay);
		
		ledMatrix.set(0, 4, 3, true);
		sleep(delay);
		ledMatrix.set(0, 5, 3, true);
		sleep(delay);
	}
	
	private void sleepTill(long time)
	{
		long t = time - System.currentTimeMillis();
		if (t < 0)
			t = 0;
		sleep(t);
	}
	
	public double getRateBLCH() throws Exception
	{
		Process process = Runtime.getRuntime().exec(new String[] { "curl",
		    "https://blockchain.info/tobtc?currency=USD&value=1" });
		process.waitFor();
		InputStreamReader r = new InputStreamReader(process.getInputStream());
		BufferedReader rr = new BufferedReader(r);
		double invRate = Double.parseDouble(rr.readLine());
		return 1 / invRate;
	}
	
	public double getRateBTCE() throws Exception
	{
		Process process = Runtime.getRuntime().exec(new String[] { "curl", "-k",
		    "https://btc-e.com/api/2/btc_usd/ticker" });
		process.waitFor();
		InputStreamReader r = new InputStreamReader(process.getInputStream());
		BufferedReader rr = new BufferedReader(r);
		String line = rr.readLine();
		int indexOf = line.indexOf("last\":") + 6;
		String rateStr = line.substring(indexOf, line.indexOf(',', indexOf));
		return Double.parseDouble(rateStr);
	}
}
