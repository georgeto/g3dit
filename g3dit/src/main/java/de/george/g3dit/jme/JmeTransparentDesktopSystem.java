package de.george.g3dit.jme;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import com.jme3.system.JmeDesktopSystem;

public class JmeTransparentDesktopSystem extends JmeDesktopSystem {
	/**
	 * Flips the image along the Y axis and converts RGBA to ABGR
	 *
	 * @param bgraBuf
	 * @param out
	 */
	private static void convertScreenShot(ByteBuffer bgraBuf, BufferedImage out) {
		WritableRaster wr = out.getRaster();
		DataBufferByte db = (DataBufferByte) wr.getDataBuffer();

		byte[] cpuArray = db.getData();

		// copy native memory to java memory
		bgraBuf.clear();
		bgraBuf.get(cpuArray);
		bgraBuf.clear();

		int width = wr.getWidth();
		int height = wr.getHeight();

		// flip the components the way AWT likes them

		// calcuate half of height such that all rows of the array are written
		// to
		// e.g. for odd heights, write 1 more scanline
		int heightdiv2ceil = height % 2 == 1 ? height / 2 + 1 : height / 2;
		for (int y = 0; y < heightdiv2ceil; y++) {
			for (int x = 0; x < width; x++) {
				int inPtr = (y * width + x) * 4;
				int outPtr = ((height - y - 1) * width + x) * 4;

				byte r1 = cpuArray[inPtr + 0];
				byte g1 = cpuArray[inPtr + 1];
				byte b1 = cpuArray[inPtr + 2];
				byte a1 = cpuArray[inPtr + 3];

				byte r2 = cpuArray[outPtr + 0];
				byte g2 = cpuArray[outPtr + 1];
				byte b2 = cpuArray[outPtr + 2];
				byte a2 = cpuArray[outPtr + 3];

				cpuArray[outPtr + 0] = a1;
				cpuArray[outPtr + 1] = b1;
				cpuArray[outPtr + 2] = g1;
				cpuArray[outPtr + 3] = r1;

				cpuArray[inPtr + 0] = a2;
				cpuArray[inPtr + 1] = b2;
				cpuArray[inPtr + 2] = g2;
				cpuArray[inPtr + 3] = r2;
			}
		}
	}

	@Override
	public void writeImageFile(OutputStream outStream, String format, ByteBuffer imageData, int width, int height) throws IOException {
		BufferedImage awtImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		convertScreenShot(imageData.asReadOnlyBuffer(), awtImage);

		ImageWriter writer = ImageIO.getImageWritersByFormatName(format).next();
		ImageWriteParam writeParam = writer.getDefaultWriteParam();

		if (format.equals("jpg")) {
			JPEGImageWriteParam jpegParam = (JPEGImageWriteParam) writeParam;
			jpegParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			jpegParam.setCompressionQuality(0.95f);

			// Jpeg does not support image type with alpha channel...
			BufferedImage newAwtImage = new BufferedImage(awtImage.getWidth(), awtImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D g = newAwtImage.createGraphics();
			g.drawImage(awtImage, 0, 0, awtImage.getWidth(), awtImage.getHeight(), null);
			g.dispose();
			awtImage = newAwtImage;
		}

		ImageOutputStream imgOut = new MemoryCacheImageOutputStream(outStream);
		writer.setOutput(imgOut);
		IIOImage outputImage = new IIOImage(awtImage, null, null);
		try {
			writer.write(null, outputImage, writeParam);
		} finally {
			imgOut.close();
			writer.dispose();
		}
	}
}
