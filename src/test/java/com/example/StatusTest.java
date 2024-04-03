package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.jupiter.api.Test;

public class StatusTest {

	@Test
	public void testRead() {
		Status status = new Status(12, 10, 8, 16, 14, 8, 9, 10);
		ByteBuffer buffer = ByteBuffer.allocate(8*4 + 100).order(ByteOrder.LITTLE_ENDIAN);
		buffer.putShort(50, (short)status.VIG());
		buffer.putShort(50 + 4, (short)status.MND());
		buffer.putShort(50 + 8, (short)status.END());
		buffer.putShort(50 + 12, (short)status.STR());
		buffer.putShort(50 + 16, (short)status.DEX());
		buffer.putShort(50 + 20, (short)status.INT());
		buffer.putShort(50 + 24, (short)status.FTH());
		buffer.putShort(50 + 28, (short)status.ARC());
		buffer.putShort(50 + 44, (short)status.level());
		StatusData data = new StatusData(status, buffer, 50);
		assertThat(data).isNotNull();
		assertThat(data.address()).isEqualTo(50);
		assertThat(data.status().level()).isEqualTo(status.level());
		assertThat(data.data().getShort(data.address())).isEqualTo((short)status.VIG());
		assertThat(data.data().getShort(data.address() + 4)).isEqualTo((short)status.MND());
		assertThat(data.data().getShort(data.address() + 44)).isEqualTo((short)status.level());
	}

	@Test
	public void testWriteAndRead() {
		Status status = new Status(12, 10, 8, 16, 14, 8, 9, 10);
		assertThat(status.level()).isGreaterThan(0);
		assertThat(status.hp()).isGreaterThan(0);
		assertThat(status.fp()).isGreaterThan(0);
		assertThat(status.st()).isGreaterThan(0);
		ByteBuffer buffer = ByteBuffer.allocate(8*4 + 100);
		StatusData data = new StatusData(status, buffer, 50);
		StatusData update = data.respec(status);
		assertThat(update).isNotNull();
		assertThat(update.status().level()).isEqualTo(status.level());
		assertThat(update.data().getShort(update.address())).isEqualTo((short)status.VIG());
		assertThat(update.data().getShort(update.address() + 44)).isEqualTo((short)status.level());
		assertThat(update.data().getShort(update.address() - 16)).isEqualTo((short)status.st());
		assertThat(update.data().getShort(update.address() - 32)).isEqualTo((short)status.fp());
		assertThat(update.data().getShort(update.address() - 44)).isEqualTo((short)status.hp());
	}
	
}
