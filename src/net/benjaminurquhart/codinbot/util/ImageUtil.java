package net.benjaminurquhart.codinbot.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.Arrays;

public class ImageUtil {
	
	private static boolean SCALE_AVG = true;

	public static Color getAverageColor(BufferedImage image) {
		if(SCALE_AVG) {
			Image img = image.getScaledInstance(1, 1, BufferedImage.SCALE_AREA_AVERAGING);
			BufferedImage scaled = new BufferedImage(1,1,image.getType());
			Graphics2D graphics = scaled.createGraphics();
			graphics.drawImage(img, 0, 0, null);
			graphics.dispose();
			int[] data = new int[4];
			data = scaled.getRaster().getPixel(0, 0, data);
			System.out.println(Arrays.toString(data));
			return new Color(data[0],data[1],data[2]);
		}
		Color out = null;
		Raster raster = image.getData();
		int height = raster.getHeight();
		int width = raster.getWidth();
		
		System.out.printf("%dx%d\n", width,height);
		
		int avgR = 0;
		int avgG = 0;
		int avgB = 0;
		
		boolean started = false;
		int[] pixels = new int[3];
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				pixels = raster.getPixel(x, y, pixels);
				avgR = (avgR+pixels[0])/(started?2:1);
				avgG = (avgR+pixels[1])/(started?2:1);
				avgB = (avgR+pixels[2])/(started?2:1);
				pixels = new int[3];
				started = true;
			}
		}
		System.out.println("Red: "+avgR);
		System.out.println("Green: "+avgG);
		System.out.println("Blue: "+avgB);
		out = new Color(avgR,avgG,avgB);
		System.out.println("Average color: "+String.format("0x%6x", out.getRGB()&0xFFFFFF).replace(" ", "0"));
		return out;
	}
}
