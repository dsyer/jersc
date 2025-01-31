package com.example;

import java.util.HexFormat;

public record Item(String name, byte[] id, String type) {
	@Override
	public final String toString() {
		return "Item[" + name + ", id=[" + HexFormat.of().formatHex(id) + ", type=" + type + "]";
	}
}
