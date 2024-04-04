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

	public Status with(StatusType type, int value) {
		switch (type) {
			case VIG:
				return new Status(value, MND, END, STR, DEX, INT, FTH, ARC);
			case MND:
				return new Status(VIG, value, END, STR, DEX, INT, FTH, ARC);
			case END:
				return new Status(VIG, MND, value, STR, DEX, INT, FTH, ARC);
			case STR:
				return new Status(VIG, MND, END, value, DEX, INT, FTH, ARC);
			case DEX:
				return new Status(VIG, MND, END, STR, value, INT, FTH, ARC);
			case INT:
				return new Status(VIG, MND, END, STR, DEX, value, FTH, ARC);
			case FTH:
				return new Status(VIG, MND, END, STR, DEX, INT, value, ARC);
			case ARC:
				return new Status(VIG, MND, END, STR, DEX, INT, FTH, value);
			default:
				return this;
		}
	}

	public String prettyPrint() {
		return toString() + ", level=" + level();
	}

}
