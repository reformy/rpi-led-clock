package net.ybm.max7219;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

public class Max7219Driver
{
	private GpioPinDigitalOutput pinDIN;
	private GpioPinDigitalOutput pinCLK;
	private GpioPinDigitalOutput pinLOAD;
	
	public Max7219Driver(GpioController gpio, Pin pinDIN, Pin pinCLK, Pin pinLOAD)
	{
		this.pinDIN = gpio.provisionDigitalOutputPin(pinDIN, PinState.LOW);
		this.pinCLK = gpio.provisionDigitalOutputPin(pinCLK, PinState.LOW);
		this.pinLOAD = gpio.provisionDigitalOutputPin(pinLOAD, PinState.HIGH);
	}
	
	public void send(int address, int data)
	{
		shiftByte(address);
		shiftByte(data);
		
		pinLOAD.low();
		pinLOAD.high();
	}
	
	public void send(Max7219Address address, int data)
	{
		send(address.address, data);
	}
	
	public void send(Max7219Address address1, int data1, Max7219Address address2,
	    int data2)
	{
		shiftByte(address1.address);
		shiftByte(data1);
		shiftByte(address2.address);
		shiftByte(data2);
		
		pinLOAD.low();
		pinLOAD.high();
	}
	
	public void send(Max7219Address[] addresses, int[] datas)
	{
		for (int i = 0; i < addresses.length; i++)
		{
			shiftByte(addresses[i].address);
			shiftByte(datas[i]);
		}
		
		pinLOAD.low();
		pinLOAD.high();
	}
	
	/**
	 * Assume there are data.length chips, and send the given address to each,
	 * with the relevant data.
	 * 
	 * @param address
	 * @param data
	 */
	public void send(int address, int[] data)
	{
		for (int i = 0; i < data.length; i++)
		{
			shiftByte(address);
			shiftByte(data[i]);
		}
		
		pinLOAD.low();
		pinLOAD.high();
	}
	
	public void send(int address, int data, int iChip, int nChip)
	{
		int i;
		for (i = 0; i < iChip; i++)
		{
			shiftByte(Max7219Address.NO_OP.address);
			shiftByte(0);
		}
		shiftByte(address);
		shiftByte(data);
		for (i++; i < nChip; i++)
		{
			shiftByte(Max7219Address.NO_OP.address);
			shiftByte(0);
		}
		
		pinLOAD.low();
		pinLOAD.high();
	}
	
	public void sendTwice(Max7219Address address, int data)
	{
		send(address, data, address, data);
	}
	
	public void sendMultiple(Max7219Address address, int data, int nChips)
	{
		sendMultiple(address.address, data, nChips);
	}
	
	public void sendMultiple(int address, int data, int nChips)
	{
		for (int i = 0; i < nChips; i++)
		{
			shiftByte(address);
			shiftByte(data);
		}
		
		pinLOAD.low();
		pinLOAD.high();
	}
	
	private void shiftByte(int b)
	{
		shiftBits(intToBits(b, 8));
	}
	
	private void shiftBits(byte[] bits)
	{
		// System.out.print("\tdata: ");
		for (byte b : bits)
		{
			// System.out.print(b + " ");
			pinDIN.setState(b == 1);
			// sleep(10);
			pinCLK.high();
			// sleep(10);
			pinCLK.low();
			// sleep(10);
		}
		// System.out.println();
	}
	
	private static byte[] intToBits(int i, int nBits)
	{
		byte[] bits = new byte[nBits];
		for (int bi = 0; bi < nBits; bi++)
		{
			bits[bi] = (byte) ((i >> (nBits - 1 - bi)) % 2);
		}
		
		return bits;
	}
}
