package com.example;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class VerifiedData {
	private int start;
	private byte[] hash = new byte[16];
	private byte[] data = new byte[0];
	public byte[] getHash() {
		return hash;
	}
	public static VerifiedData from(ByteBuffer bytes, int start, int length) {
		VerifiedData verified = new VerifiedData();
		verified.start = start;
		verified.data = new byte[length];
		bytes.get(start, verified.data);
		bytes.get(start - 16, verified.hash);
		return verified;
	}
	public byte[] getData() {
		return data;
	}
	public int getStart() {
		return start;
	}
	public boolean isVerified() {
		try {
			return MessageDigest.isEqual(hash, MessageDigest.getInstance("MD5").digest(data));
		} catch (NoSuchAlgorithmException e) {
			return false;
		}
	}
	public int length() {
		return data.length;
	}
	public void reverify() {
		try {
			this.hash = MessageDigest.getInstance("MD5").digest(data);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
	@Override
	public String toString() {
		HexFormat format = HexFormat.of();
		return "VerifiedData [hash=" + format.formatHex(hash) + ", data=[" + Integer.toHexString(start) + "," + data.length + "], verified=" + isVerified() + "]";
	}
}