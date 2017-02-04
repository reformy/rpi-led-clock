package net.ybm.max7219;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

public class MainTal
{
	private static File logFile = new File("/home/pi/ledclock.log");
	
	public static void main(String[] args) throws Exception
	{
		new MainTal().start();
	}
	
	public static void log(String msg)
	{
		try
		{
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
	
	private void start() throws Exception
	{
		final GpioController gpio = GpioFactory.getInstance();
		
		try
		{
			Max7219Driver driver = new Max7219Driver(gpio, RaspiPin.GPIO_06,
			    RaspiPin.GPIO_10, RaspiPin.GPIO_11);
			
			driver.send(Max7219Address.SCAN_LIMIT, 7);
			driver.send(Max7219Address.DECODE_MODE, 0xFF);
			driver.send(Max7219Address.DISPLAY_TEST, 0x0);
			int maxIntensity = 0xA;
			driver.send(Max7219Address.INTENSITY, maxIntensity);
			
			driver.send(Max7219Address.DIGIT0, 0x88);
			driver.send(Max7219Address.DIGIT1, 0x88);
			driver.send(Max7219Address.DIGIT2, 0x88);
			driver.send(Max7219Address.DIGIT3, 0x88);
			driver.send(Max7219Address.DIGIT4, 0x88);
			driver.send(Max7219Address.DIGIT5, 0x88);
			driver.send(Max7219Address.DIGIT6, 0x88);
			driver.send(Max7219Address.DIGIT7, 0x88);
			
			driver.send(Max7219Address.SHUTDOWN, 1);
			
			sleep(20000);
			
			MyTime.init();
			
			while (true)
			{
				try
				{
					// Show time.
					driver.send(Max7219Address.DECODE_MODE, 0x0);
					for (int i = 0; i < 300; i++)
					{
						long now = System.currentTimeMillis();
						showTime(driver, maxIntensity, (i % 2) == 0);
						sleepTill(now + 1000);
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
		int h = (c.get(Calendar.HOUR_OF_DAY) + 3) % 24;
		int m = c.get(Calendar.MINUTE);
		
		long sleepForNextMinute = (59 - c.get(Calendar.SECOND)) * 1000;
		long sleepTill = System.currentTimeMillis() + sleepForNextMinute;
		
		int dig0 = h / 10;
		if (dig0 == 0)
			dig0 = 0xF;
		int dig1 = h % 10;
		int dig2 = m / 10;
		int dig3 = m % 10;
		
		driver.send(Max7219Address.INTENSITY, intensity);
		
		showDigit(driver, dig0, dig1, dig2, dig3, showDots);
		//showDigit(driver, 8, 9, 6, 7, false);
		
		return sleepTill;
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
		driver.send(Max7219Address.DIGIT0, data0);
		driver.send(Max7219Address.DIGIT2, data1);
		driver.send(Max7219Address.DIGIT4, data2);
		driver.send(Max7219Address.DIGIT6, data3);
		Thread.sleep(sleepTimeMs);
		driver.send(Max7219Address.DIGIT1, data0);
		driver.send(Max7219Address.DIGIT3, data1);
		driver.send(Max7219Address.DIGIT5, data2);
		driver.send(Max7219Address.DIGIT7, data3);
		Thread.sleep(sleepTimeMs);
	}
	
	private void sleepTill(long time)
	{
		long t = time - System.currentTimeMillis();
		if (t < 0)
			t = 0;
		sleep(t);
	}
	
	private void showDigit(Max7219Driver driver, int d0, int d1, int d2, int d3, boolean dots)
	{
		int dotsMask = dots ? 0 : 0x80;
		driver.send(Max7219Address.DIGIT0, digits[d0][0] | dotsMask);
		driver.send(Max7219Address.DIGIT1, digits[d0][1] | dotsMask);

		driver.send(Max7219Address.DIGIT2, digits[d1][0] | dotsMask);
		driver.send(Max7219Address.DIGIT3, digits[d1][1] | dotsMask);

		driver.send(Max7219Address.DIGIT4, digits[d2][0] | dotsMask);
		driver.send(Max7219Address.DIGIT5, digits[d2][1] | dotsMask);

		driver.send(Max7219Address.DIGIT6, digits[d3][0] | dotsMask);
		driver.send(Max7219Address.DIGIT7, digits[d3][1] | dotsMask);
}
	
	private static final int[][] digits;
	static
	{
		digits = new int[16][];
		digits[0] = new int[] {0x7e,0x7e};
		digits[1] = new int[] {0x30,0x70};
		digits[2] = new int[] {0x6d,0x6f};
		digits[3] = new int[] {0x7d,0x7a};
		digits[4] = new int[] {0x33,0x33};
		digits[5] = new int[] {0x5f,0x5b};
		digits[6] = new int[] {0x7f,0x5f};
		digits[7] = new int[] {0x70,0x72};
		digits[8] = new int[] {0x7f,0x7f};
		digits[9] = new int[] {0x7f,0x7b};
		
		digits[0xf] = new int[] {0,0};
	}
}
