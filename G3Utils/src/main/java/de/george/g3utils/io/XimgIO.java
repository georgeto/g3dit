package de.george.g3utils.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import de.george.g3utils.util.IOUtils;
import de.george.g3utils.util.Misc;
import gr.zdimensions.jsquish.Squish;
import gr.zdimensions.jsquish.Squish.CompressionType;

public class XimgIO {
	public static BufferedImage decompressXimg(File file, boolean ignoreTransparency) throws Exception {
		byte[] ximgfile = IOUtils.readFileBytes(file);
		if (ximgfile.length < 87) {
			throw new IOException("Keine ximg Datei");
		}
		@SuppressWarnings("resource")
		G3FileReader reader = new G3FileReaderVirtual(Misc.asHex(Arrays.copyOfRange(ximgfile, 0, 87)));
		if (!reader.read(8).equalsIgnoreCase("47454E4F4D464C45")) {
			throw new IOException("Keine ximg Datei");
		}
		reader.skip(2);
		int offset = reader.readInt();
		reader.skip(33);
		int width = reader.readInt();
		int height = reader.readInt();
		reader.skip(8);
		int mipmapCount = reader.readInt();
		reader.skip(4);
		String typeString = reader.read(4);
		CompressionType type = null;
		int imageStart = 0;
		if (typeString.equalsIgnoreCase("44585431")) { // DXT1
			type = CompressionType.DXT1;
			imageStart = offset - width * height / 2;
		} else if (typeString.equalsIgnoreCase("44585433")) { // DXT3
			type = CompressionType.DXT3;
			imageStart = offset - width * height;
		} else if (typeString.equalsIgnoreCase("44585435")) { // DXT5
			type = CompressionType.DXT5;
			imageStart = offset - width * height;
		} else {
			throw new IOException("Unbekannte Kompression");
		}
		byte[] bigMipMap = Arrays.copyOfRange(ximgfile, imageStart, offset);

		byte[] decompressedData = Squish.decompressImage(null, width, height, bigMipMap, type);
		BufferedImage image = new BufferedImage(width, height,
				ignoreTransparency ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
		int pos = 0;
		for (int x = 0; x < height; x++) {
			for (int y = 0; y < width; y++) {
				int data = (decompressedData[pos] & 0xFF) << 16 | (decompressedData[pos + 1] & 0xFF) << 8
						| decompressedData[pos + 2] & 0xFF;
				if (!ignoreTransparency) {
					data |= (decompressedData[pos + 3] & 0xFF) << 24;
				}
				image.setRGB(y, x, data);
				pos += 4;
			}
		}

		/*
		@foff
		int tmpH = height, tmpW = width;
		int tmpOffset = offset;
		for (int i = 0; i < mipmapCount; i++) {
			tmpOffset -= tmpW * tmpH;

			byte[] mipMap = Arrays.copyOfRange(ximgfile, tmpOffset, tmpOffset + tmpH * tmpW);

			byte[] dd = Squish.decompressImage(null, tmpW, tmpH, mipMap, type);
			BufferedImage im = new BufferedImage(tmpW, tmpH, BufferedImage.TYPE_INT_RGB);
			pos = 0;
			for (int x = 0; x < tmpH; x++) {
				for (int y = 0; y < tmpW; y++) {
					int data = (dd[pos] & 0xFF) << 16 | (dd[pos + 1] & 0xFF) << 8
							| (dd[pos + 2] & 0xFF); //| (dd[pos + 3] & 0xFF) << 24;
					im.setRGB(y, x, data);
					pos += 4;
				}
			}
			ImageIO.write(im, "PNG", new File("..." + i + ".png"));
			tmpW /= 2;
			tmpH /= 2;
		}
		@fon
		*/

		return image;
	}
}
