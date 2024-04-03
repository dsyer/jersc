package com.example;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record Status(int VIG, int MND, int END, int STR, int DEX, int INT, int FTH, int ARC) {

	public static Status from(byte[] data) {
		ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
		return from(buffer);
	}

	public static Status from(ByteBuffer buffer) {
		int address = 0;
		Status status = new Status(buffer.getShort(address + 0), buffer.getShort(address + 4),
				buffer.getShort(address + 8),
				buffer.getShort(address + 12),
				buffer.getShort(address + 16), buffer.getShort(address + 20), buffer.getShort(address + 24),
				buffer.getShort(address + 28));
		int level = buffer.getShort(address + 44);
		if (status.level() != level) {
			return null;
		}
		return status;
	}

	public int level() {
		return VIG + MND + END + STR + DEX + INT + FTH + ARC - 79;
	}

	public int hp() {
		return Progression.hp(VIG);
	}

	public int fp() {
		return Progression.fp(MND);
	}

	public int st() {
		return Progression.st(END);
	}

}
