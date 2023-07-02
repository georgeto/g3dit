package de.george.lrentnode.template;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.george.g3utils.io.Saveable;
import de.george.g3utils.structure.Guid;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.util.Converter;
import de.george.g3utils.util.IOUtils;

public class Lrtpldatasc implements Saveable {
	private Map<Guid, String> entries = new LinkedHashMap<>();

	public Lrtpldatasc() {}

	public Lrtpldatasc(Path file) throws IOException, DuplicateEntryException {
		read(file);
	}

	private void read(Path file) throws IOException, DuplicateEntryException {
		List<String> secTemplateList = IOUtils.readTextFile(file, Converter.WINDOWS_1252);
		secTemplateList.stream().map(e -> e.split("=")).filter(e -> e.length == 2)
				.forEach(e -> add(new Guid(e[0].replaceFirst("\\{", "").replaceFirst("\\}", "")), e[1]));
	}

	private StringBuilder prepareSave() {
		StringBuilder builder = new StringBuilder("[TemplateList]" + System.lineSeparator());
		entries.forEach((guid, name) -> builder.append(GuidUtil.hexToLrtpldatasc(guid.getGuid()) + "=" + name + System.lineSeparator()));
		return builder;
	}

	@Override
	public void save(Path file) throws IOException {
		IOUtils.writeTextFile(prepareSave(), file, Converter.WINDOWS_1252);
	}

	@Override
	public void save(OutputStream out) throws IOException {
		IOUtils.writeTextFile(prepareSave(), out, Converter.WINDOWS_1252);
	}

	public boolean contains(Guid templateGuid) {
		return entries.containsKey(templateGuid);
	}

	public boolean contains(String templateName) {
		return entries.containsValue(templateName);
	}

	public Entry get(Guid templateGuid) {
		return new Entry(templateGuid, entries.get(templateGuid));
	}

	public Entry get(String templateName) {
		Guid templateGuid = entries.entrySet().stream().filter(e -> e.getValue().equals(templateName)).map(Map.Entry::getKey).findAny()
				.orElse(new Guid(null));
		return new Entry(templateGuid, templateName);
	}

	public void add(Guid templateGuid, String templateName) throws DuplicateEntryException {
		if (!templateGuid.isValid()) {
			throw new InvalidEntryException(templateName);
		}

		if (entries.putIfAbsent(templateGuid, templateName) != null) {
			throw new DuplicateEntryException(templateName);
		}
	}

	public void add(Entry entry) throws DuplicateEntryException {
		add(entry.getTemplateGuid(), entry.getTemplateName());
	}

	public void remove(Guid templateGuid) {
		entries.remove(templateGuid);
	}

	public void remove(String templateName) {
		entries.values().remove(templateName);
	}

	public static class Entry {
		private Guid templateGuid;
		private String templateName;

		public Entry(Guid templateGuid, String templateName) {
			this.templateGuid = templateGuid;
			this.templateName = templateName;
		}

		public Guid getTemplateGuid() {
			return templateGuid;
		}

		public void setTemplateGuid(Guid templateGuid) {
			this.templateGuid = templateGuid;
		}

		public String getTemplateName() {
			return templateName;
		}

		public void setTemplateName(String templateName) {
			this.templateName = templateName;
		}
	}

	public static class DuplicateEntryException extends RuntimeException {
		public DuplicateEntryException() {}

		public DuplicateEntryException(String message) {
			super(message);
		}
	}

	public static class InvalidEntryException extends RuntimeException {
		public InvalidEntryException() {}

		public InvalidEntryException(String message) {
			super(message);
		}
	}
}
