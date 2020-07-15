package de.ailis.oneinstance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class OneInstanceProtocol {
	private final DataInputStream in;
	private final DataOutputStream out;

	public static final class DataField<T> {
		private final String name;
		private final Class<T> type;

		public DataField(String name, Class<T> type) {
			this.name = Objects.requireNonNull(name);
			this.type = Objects.requireNonNull(type);
		}

		public String getName() {
			return name;
		}

		public Class<T> getType() {
			return type;
		}
	}

	public static final class DataFieldAccessor {
		private final DataField<?> dataField;
		private final Object value;

		public DataFieldAccessor(DataField<?> dataField, Object value) {
			this.dataField = dataField;
			this.value = value;
		}

		public boolean isField(DataField<?> dataField) {
			return this.dataField.getName().equals(dataField.getName());
		}

		@SuppressWarnings("unchecked")
		public <T> T getValue(DataField<T> dataField) {
			if (!isField(dataField)) {
				throw new IllegalArgumentException();
			}

			return (T) value;
		}
	}

	public static final class Client {
		public static final DataField<String> WORKING_DIR = new DataField<>("WORKING_DIR", String.class);
		public static final DataField<String[]> ARGS = new DataField<>("ARGS", String[].class);
	}

	public static final class Server {
		public static final DataField<String> APP_ID = new DataField<>("APP_ID", String.class);
		public static final DataField<String> RESULT = new DataField<>("RESULT", String.class);
		public static final DataField<String> STDOUT = new DataField<>("STDOUT", String.class);
		public static final DataField<Integer> EXIT_CODE = new DataField<>("EXIT_CODE", Integer.class);
	}

	public OneInstanceProtocol(Socket socket) throws IOException {
		this(socket.getInputStream(), socket.getOutputStream());
	}

	public OneInstanceProtocol(InputStream in, OutputStream out) {
		this.in = new DataInputStream(in);
		this.out = new DataOutputStream(out);
	}

	public <T> T read(DataField<T> dataField) throws IOException {
		String name = in.readUTF();
		if (!name.equals(dataField.name)) {
			throw new IOException("Unexpected field with name " + name + ".");
		}

		return readValue(dataField);
	}

	public DataFieldAccessor read(DataField<?>... dataFields) throws IOException {
		String name = in.readUTF();
		Optional<DataField<?>> dataField = Arrays.stream(dataFields).filter(f -> f.getName().equals(name)).findAny();
		if (!dataField.isPresent()) {
			throw new IOException("Unexpected field with name " + name + ".");
		}

		return new DataFieldAccessor(dataField.get(), readValue(dataField.get()));
	}

	public <T> void write(DataField<T> dataField, T value) throws IOException {
		out.writeUTF(dataField.getName());
		Class<T> dataType = dataField.getType();
		if (dataType == String.class) {
			out.writeUTF((String) value);
		} else if (dataType == Integer.class) {
			out.writeInt((Integer) value);
		} else if (dataType == Boolean.class) {
			out.writeBoolean((Boolean) value);
		} else if (dataType == String[].class) {
			String[] arrayValue = (String[]) value;
			out.writeInt(arrayValue.length);
			for (String element : arrayValue) {
				out.writeUTF(element);
			}
		} else {
			throw new IllegalArgumentException();
		}

		out.flush();
	}

	@SuppressWarnings("unchecked")
	private <T> T readValue(DataField<T> dataField) throws IOException {
		Class<T> dataType = dataField.getType();
		if (dataType == String.class) {
			return (T) in.readUTF();
		} else if (dataType == Integer.class) {
			return (T) Integer.valueOf(in.readInt());
		} else if (dataType == Boolean.class) {
			return (T) Boolean.valueOf(in.readBoolean());
		} else if (dataType == String[].class) {
			int arraySize = in.readInt();
			if (arraySize >= 0 && arraySize < 100000) {
				String[] result = new String[arraySize];
				for (int i = 0; i < arraySize; i++) {
					result[i] = in.readUTF();
				}
				return (T) result;
			} else {
				throw new IOException("Received invalid array.");
			}
		} else {
			throw new IllegalArgumentException();
		}
	}
}
