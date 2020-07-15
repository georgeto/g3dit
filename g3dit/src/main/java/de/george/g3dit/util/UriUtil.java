package de.george.g3dit.util;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3utils.util.Pair;

public class UriUtil {
	public static Optional<String> getScheme(String uri) {
		String[] splitted = uri.split(":");
		if (splitted.length != 2) {
			return Optional.empty();
		}
		return Optional.of(splitted[0]);
	}

	public static String encodeAsUri(String scheme, String data) {
		return scheme + ":" + data;
	}

	public static <T> String encodeJsonAsUri(String scheme, T data) {
		try {
			return encodeAsUri(scheme, Base64.getUrlEncoder().encodeToString(new ObjectMapper().writeValueAsString(data).getBytes()));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static Pair<String, String> decodeUri(String uri) {
		String[] splitted = uri.split(":");
		return Pair.of(splitted[0], splitted[1]);
	}

	public static <T> Pair<String, T> decodeUriAsJson(String uri, Class<T> type) {
		try {
			String[] splitted = uri.split(":");
			T data = new ObjectMapper().readValue(Base64.getUrlDecoder().decode(splitted[1]), type);
			return Pair.of(splitted[0], data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String encodeEntity(EntityDescriptor entity) {
		return encodeJsonAsUri("entity", entity);
	}

	public static String encodeFile(FileDescriptor file) {
		return encodeJsonAsUri("file", file);
	}
}
