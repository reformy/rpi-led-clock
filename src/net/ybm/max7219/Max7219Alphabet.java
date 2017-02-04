package net.ybm.max7219;

public enum Max7219Alphabet
{
	A(0x77),
	B(0x1F),
	C(0x4E),
	E(0x4F),
	G(0x5F, 0x5F, 0x5E, 0x5E),
	H(0x37),
	I(0x04, 0x06, 0x04, 0x04),
	L(0x0E),
	R(0x07, 0x05, 0x05, 0x05),
	T(0x0F),
	
	MAKAF(0x01),
	N0(0x7E),
	N1(0x30), ;
	
	int data[] = new int[4];
	
	private Max7219Alphabet(int data)
	{
		this.data[0] = this.data[1] = this.data[2] = this.data[3] = data;
	}
	
	private Max7219Alphabet(int data0, int data1, int data2, int data3)
	{
		this.data[0] = data0;
		this.data[1] = data1;
		this.data[2] = data2;
		this.data[3] = data3;
	}
}
