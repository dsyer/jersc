package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class BytesMatcherTest {
	private static byte[] INPUT_1_6 = new byte[] { 0, 1, 2, 3, 4, 5, 1, 2, 3, 4 };

	@Test
	public void firstMatchFound() {
		BytesMatcher matcher = new BytesMatcher(new byte[] { 1, 2, 3, 4 });
		int start = matcher.match(INPUT_1_6, 0, INPUT_1_6.length);
		assertThat(start).isEqualTo(1);
	}

	@Test
	public void secondMatchFound() {
		BytesMatcher matcher = new BytesMatcher(new byte[] { 1, 2, 3, 4 });
		int start = matcher.match(INPUT_1_6, 2, INPUT_1_6.length);
		assertThat(start).isEqualTo(6);
	}

	@Test
	public void noMatchFound() {
		BytesMatcher matcher = new BytesMatcher(new byte[] { 1, 2, 3, 4 });
		int start = matcher.match(INPUT_1_6, 7, INPUT_1_6.length);
		assertThat(start).isEqualTo(-1);
	}

	@Test
	public void matchAtStartOfInput() {
		BytesMatcher matcher = new BytesMatcher(new byte[] { 0, 1 });
		int start = matcher.match(INPUT_1_6, 0, INPUT_1_6.length);
		assertThat(start).isEqualTo(0);
	}

	@Test
	public void matchAtEndOfInput() {
		BytesMatcher matcher = new BytesMatcher(new byte[] { 3, 4 });
		int start = matcher.match(INPUT_1_6, 5, INPUT_1_6.length);
		assertThat(start).isEqualTo(8);
	}

	@Test
	public void matchWithPartialPattern() {
		BytesMatcher matcher = new BytesMatcher(new byte[] { 1, 2, 3, 4, 5, 6 });
		int start = matcher.match(INPUT_1_6, 0, INPUT_1_6.length);
		assertThat(start).isEqualTo(-1);
	}

	@Test
	public void matchWithEmptyPattern() {
		BytesMatcher matcher = new BytesMatcher(new byte[] {});
		int start = matcher.match(INPUT_1_6, 0, INPUT_1_6.length);
		assertThat(start).isEqualTo(0);
	}

	@Test
	public void matchAnyWithEmptyPattern() {
		BytesMatcher matcher = new BytesMatcher(new byte[] {});
		int start = matcher.match(INPUT_1_6, 4, INPUT_1_6.length);
		assertThat(start).isEqualTo(4);
	}

	@Test
	public void matchWithEmptyInput() {
		BytesMatcher matcher = new BytesMatcher(new byte[] { 1, 2, 3, 4 });
		int start = matcher.match(new byte[] {}, 0, 0);
		assertThat(start).isEqualTo(-1);
	}

	@Test
	public void matchWithPatternLongerThanInput() {
		BytesMatcher matcher = new BytesMatcher(new byte[] { 1, 2, 3, 4, 5, 6, 7 });
		int start = matcher.match(INPUT_1_6, 0, INPUT_1_6.length);
		assertThat(start).isEqualTo(-1);
	}

	@Test
	public void matchWithNullInput() {
		BytesMatcher matcher = new BytesMatcher(new byte[] { 1, 2, 3, 4 });
		int start = matcher.match(null, 0, 0);
		assertThat(start).isEqualTo(-1);
	}

	@Test
	public void matchWithNegativeOffset() {
		BytesMatcher matcher = new BytesMatcher(new byte[] { 1, 2, 3, 4 });
		int start = matcher.match(INPUT_1_6, -10, INPUT_1_6.length);
		assertThat(start).isEqualTo(1);
	}

	@Test
	public void matchWithNegativeLength() {
		BytesMatcher matcher = new BytesMatcher(new byte[] { 1, 2, 3, 4 });
		int start = matcher.match(INPUT_1_6, 0, -1);
		assertThat(start).isEqualTo(-1);
	}

	@Test
	public void multipleMatchesFound() {
		BytesMatcher matcher = new BytesMatcher(new byte[] { 1, 2, 3, 4 });
		int[] matches = matcher.matches(INPUT_1_6);
		assertThat(matches).containsExactly(1, 6);
	}

	@Test
	public void noMatchesFound() {
		BytesMatcher matcher = new BytesMatcher(new byte[] { 7, 8, 9 });
		int[] matches = matcher.matches(INPUT_1_6);
		assertThat(matches).isEmpty();
	}

	@Test
	public void matchesWithEmptyInput() {
		BytesMatcher matcher = new BytesMatcher(new byte[] { 1, 2, 3, 4 });
		int[] matches = matcher.matches(new byte[] {});
		assertThat(matches).isEmpty();
	}

	@Test
	public void matchesWithNullInput() {
		BytesMatcher matcher = new BytesMatcher(new byte[] { 1, 2, 3, 4 });
		int[] matches = matcher.matches(null);
		assertThat(matches).isEmpty();
	}

}