package net.imglib2.ui.lut;

/**
 * Lookup Table for 256 RGB 
 * @author GBH
 */
public class LUT {
	static int lutSize = 256;
	public final byte[] reds = new byte[lutSize];
	public final byte[] greens = new byte[lutSize];
	public final byte[] blues = new byte[lutSize];
}
