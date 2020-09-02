package de.george.g3dit.jme.asset;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.TextureKey;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.util.BufferUtils;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileReaderVirtual;

public final class XimgLoader implements AssetLoader {
	private static final Logger logger = Logger.getLogger(XimgLoader.class.getName());

	@Override
	public Object load(AssetInfo info) throws IOException {
		if (!(info.getKey() instanceof TextureKey)) {
			throw new IllegalArgumentException("Texture assets must be loaded using a TextureKey");
		}

		logger.info("Loading: " + info.getKey().getName());

		try (InputStream is = info.openStream()) {
			@SuppressWarnings("resource")
			G3FileReader reader = new G3FileReaderVirtual(is);
			if (reader.getSize() < 87 || !reader.read(8).equalsIgnoreCase("47454E4F4D464C45")) {
				throw new IOException("Not a ximg file!");
			}

			reader.skip(2);
			int imageEnd = reader.readInt();
			reader.skip(33);
			int width = reader.readInt();
			int height = reader.readInt();
			reader.skip(8);
			int mipmapCount = reader.readInt();
			reader.skip(4);
			String typeString = reader.readString(4);
			//@foff
			/*CompressionType type = null;
			int imageStart = 0;
			if (typeString.equalsIgnoreCase("DXT1")) { // DXT1
				type = CompressionType.DXT1;
				imageStart = imageEnd - (width * height) / 2;
			} else if (typeString.equalsIgnoreCase("DXT3")) { // DXT3
				type = CompressionType.DXT3;
				imageStart = imageEnd - width * height;
			} else if (typeString.equalsIgnoreCase("DXT5")) { // DXT5
				type = CompressionType.DXT5;
				imageStart = imageEnd - width * height;
			} else
				throw new IOException("Unbekannte Kompression");

			reader.seek(imageStart);
			byte[] rawData = new byte[imageEnd - imageStart];
			reader.getBuffer().get(rawData, 0, rawData.length);

			byte[] decompressedData = Squish.decompressImage(null, width, height, rawData, type);
			ByteBuffer scratch = BufferUtils.createByteBuffer(decompressedData.length);
			scratch.clear();
			for (int x = 0; x < height; x++) {
				for (int y = 0; y < width; y++) {
					//int pos = (x * width + y) * 4;
					int posMirrored = (x * width + width - 1 - y) * 4;
					scratch.put(decompressedData[posMirrored + 3]);
					scratch.put(decompressedData[posMirrored]);
					scratch.put(decompressedData[posMirrored + 1]);
					scratch.put(decompressedData[posMirrored + 2]);
				}
			}
			scratch.rewind();

			// Create the Image object
			Image textureImage = new Image();
			textureImage.setFormat(Format.ARGB8);*/
			//@fon
			Format format = null;
			int imageStart = 0;
			if (typeString.equalsIgnoreCase("DXT1")) { // DXT1
				format = Format.DXT1;
				imageStart = imageEnd - width * height / 2;
			} else if (typeString.equalsIgnoreCase("DXT3")) { // DXT3
				format = Format.DXT3;
				imageStart = imageEnd - width * height;
			} else if (typeString.equalsIgnoreCase("DXT5")) { // DXT5
				format = Format.DXT5;
				imageStart = imageEnd - width * height;
			} else {
				throw new IOException("Unsupported compression: " + typeString);
			}

			reader.seek(imageStart);
			byte[] rawData = new byte[imageEnd - imageStart];
			reader.getBuffer().get(rawData, 0, rawData.length);

			ByteBuffer scratch = BufferUtils.createByteBuffer(rawData.length);
			scratch.clear();
			scratch.put(rawData);
			scratch.rewind();

			// Create the Image object
			Image textureImage = new Image();
			textureImage.setFormat(format);
			textureImage.setWidth(width);
			textureImage.setHeight(height);
			textureImage.setData(scratch);
			return textureImage;
		}
	}
}
