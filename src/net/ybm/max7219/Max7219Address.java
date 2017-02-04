package net.ybm.max7219;

public enum Max7219Address
{
	NO_OP(0x0), DIGIT0(0x1), DIGIT1(0x2), DIGIT2(0x3), DIGIT3(0x4), DIGIT4(0x5), DIGIT5(
	    0x6), DIGIT6(0x7), DIGIT7(0x8), DECODE_MODE(0x9), INTENSITY(0xA), SCAN_LIMIT(
	    0xB), SHUTDOWN(0xC), DISPLAY_TEST(0xF);
	
	int address;
	
	private Max7219Address(int address)
	{
		this.address = address;
	}
}
