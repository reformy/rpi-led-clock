package net.ybm.max7219;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;

public class MyTime
{
	/**
	 * System.currentTime / 1000 = real time + diff;
	 */
	private static long realUnixTimeDiff = 0;
	
	public static void init()
	{
		check(5);
	}
	
	private static void check(int nTries)
	{
		int iTry = nTries;
		while (iTry > 0)
		{
			BufferedReader rr = null;
			try
			{
				URL url = new URL("http://www.unixtimestamp.com/index.php");
				InputStream is = url.openConnection().getInputStream();
				rr = new BufferedReader(new InputStreamReader(is));
				String line;
				while ((line = rr.readLine()) != null)
				{
					if (line.indexOf("<h3 class=\"text-danger\">14") > -1)
					{
						// this is the time line.
						int ind = line.indexOf(">") + 1;
						String vs = line.substring(ind, line.indexOf(' ', ind));
						long time = Long.parseLong(vs);
						realUnixTimeDiff = System.currentTimeMillis() / 1000 - time;
						MainBTC.log("MyTime: set diff = " + realUnixTimeDiff);
						lastCheckTime = System.currentTimeMillis();
						return;
					}
				}
				
				//throw new Exception("Failed to found!");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					if (rr != null)
						rr.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
			if (iTry > 1)
			{
				try
				{
					Thread.sleep(5000);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			iTry--;
		}
		lastCheckTime = System.currentTimeMillis();
	}
	
	static long lastCheckTime = 0;
	public static Date getRealDate()
	{
		if (System.currentTimeMillis() - lastCheckTime > 60000*60)
			check(1);
		return new Date(System.currentTimeMillis() - realUnixTimeDiff * 1000);
	}
}
