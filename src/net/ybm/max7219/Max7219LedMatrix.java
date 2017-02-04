package net.ybm.max7219;

public class Max7219LedMatrix
{
	private Max7219Driver driver;
	private int[][] bits;
	private int nChips;
	
	private boolean working = false;
	
	public Max7219LedMatrix(Max7219Driver driver)
	{
		this(driver, 1);
	}
	
	public Max7219LedMatrix(Max7219Driver driver, int nChips)
	{
		this.driver = driver;
		this.nChips =nChips;
		bits = new int[8][];
		for (int i = 0; i < 8; i++)
			bits[i] = new int[nChips];
	}
	
	public void start()
	{
		driver.sendTwice(Max7219Address.DECODE_MODE, 0);
		for (int iDigit = 0; iDigit < 8; iDigit++)
			driver.send(iDigit+1, bits[iDigit]);
		working = true;
	}
	
	public void stop()
	{
		working = false;
	}
	
	public void set(int iChip, int x, int y, boolean value) throws Exception
	{
		if (x > 7 || y > 7 || x < 0 || y < 0)
			throw new Exception("Bad input x,y: " + x + "," + y);
		if (iChip < 0 || iChip >= nChips)
			throw new Exception("Bad iChip: " + iChip);
		if (value)
			bits[x][iChip] |= 1 << y;
		else
			bits[x][iChip] &= 0xFF - (1 << y);
		
		if (working)
		{
			driver.send(x + 1, bits[x][iChip], iChip, nChips);
		}
	}
}
