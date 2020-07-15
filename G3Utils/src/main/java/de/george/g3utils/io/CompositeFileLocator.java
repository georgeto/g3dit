package de.george.g3utils.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class CompositeFileLocator implements FileLocator {
	private List<FileLocator> locators = new LinkedList<>();

	public CompositeFileLocator() {

	}

	public CompositeFileLocator(Collection<FileLocator> locators) {
		this.locators.addAll(locators);
	}

	public CompositeFileLocator(FileLocator... locators) {
		Arrays.stream(locators).forEach(this.locators::add);
	}

	public void addLocator(FileLocator locator) {
		locators.add(locator);
	}

	@Override
	public void setRootPath(String rootPath) throws IOException {
		for (FileLocator locator : locators) {
			locator.setRootPath(rootPath);
		}
	}

	@Override
	public Optional<File> locate(String name) {
		for (FileLocator locator : locators) {
			Optional<File> file = locator.locate(name);
			if (file.isPresent()) {
				return file;
			}
		}
		return Optional.empty();
	}
}
