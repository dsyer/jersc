package com.example;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record StatusData(Status status, ByteBuffer data, int address) {

	public static StatusData from(ByteBuffer data, int address) {
		Status status = Status.from(data.slice(address, 48).order(ByteOrder.LITTLE_ENDIAN));
		if (status == null) {
			return null;
		}
		return new StatusData(status, data, address);
	}

	public StatusData respec(Status status) {
		data.putShort(address + 0, (short)status.VIG());
		data.putShort(address + 4, (short)status.MND());
		data.putShort(address + 8, (short)status.END());
		data.putShort(address + 12, (short)status.STR());
		data.putShort(address + 16, (short)status.DEX());
		data.putShort(address + 20, (short)status.INT());
		data.putShort(address + 24, (short)status.FTH());
		data.putShort(address + 28, (short)status.ARC());
		data.putShort(address + 44, (short)status.level());
		updateStats(-8, status.st());
		updateStats(-24, status.fp());
		updateStats(-36, status.hp());
		return new StatusData(status, data, address);
	}

	private void updateStats(int offset, int level) {
		// This is the base stat.
		data.putShort(address + offset, (short)level);
		// We have to assume the other two are going to be adjusted by the game.
		data.putShort(address + offset - 4, (short)level);
		data.putShort(address + offset - 8, (short)level);
	}

	public String prettyPrint() {
		return status.prettyPrint();
	}
	
}
