package net.ybm.max7219;

import java.util.Calendar;
import java.util.TimeZone;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

public class Main
{
	public static void main(String[] args) throws Exception
	{
		System.out.println("Start.");
		
		final GpioController gpio = GpioFactory.getInstance();
		
		try
		{
			Max7219Driver driver = new Max7219Driver(gpio, RaspiPin.GPIO_06,
			    RaspiPin.GPIO_10, RaspiPin.GPIO_11);
			
			driver.sendTwice(Max7219Address.SCAN_LIMIT, 7);
			driver.sendTwice(Max7219Address.DECODE_MODE, 0xFF);
			driver.sendTwice(Max7219Address.DISPLAY_TEST, 0x0);
			driver.sendTwice(Max7219Address.INTENSITY, 0x9);
			
			/*
			driver.sendTwice(Max7219Address.DIGIT0, 0xf);
			driver.sendTwice(Max7219Address.DIGIT1, 0xf);
			driver.sendTwice(Max7219Address.DIGIT2, 0xf);
			driver.sendTwice(Max7219Address.DIGIT3, 0xf);
			driver.sendTwice(Max7219Address.DIGIT4, 0xf);
			driver.sendTwice(Max7219Address.DIGIT5, 0xf);
			driver.sendTwice(Max7219Address.DIGIT6, 0xf);
			driver.sendTwice(Max7219Address.DIGIT7, 0xf);
			*/
			
			driver.sendTwice(Max7219Address.SHUTDOWN, 1);
			
			/*
			driver.sendTwice(Max7219Address.DIGIT0, 0);
			driver.sendTwice(Max7219Address.DIGIT1, 0);
			*/
			
			
			long waitMs = 30;
			while (true)
			{
				Calendar hourMin = Calendar.getInstance(TimeZone.getTimeZone("IDT"));
				System.out.println(hourMin);
				
				int min = hourMin.get(Calendar.MINUTE);
				int min1 = min / 10;
				int min2 = min % 10;
				
				int hour = hourMin.get(Calendar.HOUR_OF_DAY);
				int hour1 = hour / 10;
				int hour2 = hour % 10;
				
				driver.send(Max7219Address.DIGIT0, hour1, Max7219Address.DIGIT0, min1);
				driver.send(Max7219Address.DIGIT4, hour2, Max7219Address.DIGIT4, min2);
				Thread.sleep(waitMs);
				driver.send(Max7219Address.DIGIT1, hour1, Max7219Address.DIGIT1, min1);
				driver.send(Max7219Address.DIGIT5, hour2, Max7219Address.DIGIT5, min2);
				Thread.sleep(waitMs);
				driver.send(Max7219Address.DIGIT2, hour1, Max7219Address.DIGIT2, min1);
				driver.send(Max7219Address.DIGIT6, hour2, Max7219Address.DIGIT6, min2);
				Thread.sleep(waitMs);
				driver.send(Max7219Address.DIGIT3, hour1, Max7219Address.DIGIT3, min1);
				driver.send(Max7219Address.DIGIT7, hour2, Max7219Address.DIGIT7, min2);
				
				long now = System.currentTimeMillis();
				long nextTime = 60000 - (now % 60000) - (4*waitMs);
				if (nextTime < 1)
					nextTime = 1;
				Thread.sleep(nextTime);
			}
			
			
		}
		finally
		{
			gpio.shutdown();
			System.out.println("Done.");
		}
	}
}
