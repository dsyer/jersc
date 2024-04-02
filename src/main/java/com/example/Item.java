package com.example;

public record Item(String name, byte[] id) {
	@Override
	public final String toString() {
		return "Item[" + name + ", id=[" + Byte.toUnsignedInt(id[0]) + ", " +  Byte.toUnsignedInt(id[1]) + "]]";
	}
}
