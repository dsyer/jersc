package com.example;

public class Environment {

	public static String get(String key) {
		return get(key, null);
	}

	public static String get(String key, String fallback) {
		String value = System.getenv(key);
		if (value != null) {
			return value;
		}
		value = System.getProperty(key);
		if (value != null) {
			return value;
		}
		key = key.toUpperCase().replace(".", "_");
		value = System.getenv(key);
		if (value != null) {
			return value;
		}
		key = key.toLowerCase().replace("_", ".");
		value = System.getProperty(key);
		if (value != null) {
			return value;
		}
		return fallback;
	}
}
