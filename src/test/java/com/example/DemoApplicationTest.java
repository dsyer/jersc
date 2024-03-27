package com.example;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class DemoApplicationTest {

	@Test
	public void longBytes() {
		long id = 1234567890123456789L;
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putLong(id);
		System.err.println(Arrays.toString(buffer.array()));
	}

}
