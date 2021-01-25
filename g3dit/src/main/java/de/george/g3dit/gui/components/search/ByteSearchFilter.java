package de.george.g3dit.gui.components.search;

import com.google.common.primitives.Bytes;

import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3FileWriterVirtual;
import de.george.g3utils.util.Converter;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.archive.eCEntity;

public class ByteSearchFilter implements SearchFilter<eCEntity> {

	public enum MatchMode {
		CaseSensitive,
		CaseInsensitive,
		Hex
	}

	private MatchMode matchMode;
	private byte[] bytesToMatch;

	public ByteSearchFilter(MatchMode matchMode, String dataToMatch) {
		this.matchMode = matchMode;
		if (matchMode == MatchMode.Hex) {
			try {
				bytesToMatch = Misc.asByte(dataToMatch.replaceAll("\\s+", ""));
			} catch (IllegalArgumentException e) {
				bytesToMatch = null;
			}
		} else if (matchMode == MatchMode.CaseSensitive) {
			bytesToMatch = Converter.stringToByteArray(dataToMatch);
		} else if (matchMode == MatchMode.CaseInsensitive) {
			bytesToMatch = Converter.stringToByteArray(dataToMatch.toLowerCase());
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public boolean matches(eCEntity entity) {
		G3FileWriterVirtual writer;
		if (matchMode == MatchMode.CaseInsensitive) {
			writer = new G3FileWriterVirtual() {
				@Override
				public G3FileWriter writeString(String string) {
					return super.writeString(string.toLowerCase());
				}
			};
		} else {
			writer = new G3FileWriterVirtual();
		}
		entity.write(writer);
		return Bytes.indexOf(writer.getData(), bytesToMatch) != -1;
	}

	@Override
	public boolean isValid() {
		return bytesToMatch != null && bytesToMatch.length != 0;
	}

	public MatchMode getMatchMode() {
		return matchMode;
	}

	public byte[] getBytesToMatch() {
		return bytesToMatch;
	}

	public String getDataToMatch() {
		if (!isValid()) {
			return "";
		}

		if (matchMode == MatchMode.Hex) {
			return Misc.asHex(bytesToMatch);
		} else {
			return Converter.byteArrayToString(bytesToMatch);
		}
	}
}
