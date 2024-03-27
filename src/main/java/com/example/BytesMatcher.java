package com.example;

import java.util.ArrayList;
import java.util.List;

public class BytesMatcher {

	private final byte[] pattern;
	private final int[] bcs = new int[128]; // bad character shift
	private final int[] gss; // good suffix shift

	public BytesMatcher(byte[] pattern) {
		this.pattern = pattern;
		gss = new int[pattern.length];
		compileBoundaryPattern();
	}

	public int[] matches(byte[] input) {
		if (input == null) {
			return new int[0];
		}
		List<Integer> matches = new ArrayList<>();
		int off = 0;
		int len = input.length;
		while (off < len) {
			int start = match(input, off, len);
			if (start == -1) {
				break;
			}
			matches.add(start);
			off = start + pattern.length;
		}
		int[] result = new int[matches.size()];
		int i = 0;
		for (Integer value : matches) {
			result[i++] = value;
		}
		return result;
	}

	public int match(byte[] input, int off, int len) {
		if (input == null) {
			return -1;
		}
		int last = len - pattern.length;
		if (off < 0) {
			off = 0;
		}

		// Loop over all possible match positions in text
		NEXT: while (off <= last) {
			// Loop over pattern from right to left
			for (int j = pattern.length - 1; j >= 0; j--) {
				byte ch = input[off + j];
				if (ch != pattern[j]) {
					// Shift search to the right by the maximum of the
					// bad character shift and the good suffix shift
					off += Math.max(j + 1 - bcs[ch & 0x7F], gss[j]);
					continue NEXT;
				}
			}
			// Entire pattern matched starting at off
			return off;
		}
		return -1;
	}

	private void compileBoundaryPattern() {
		int i, j;

		// Precalculate part of the bad character shift
		// It is a table for where in the pattern each
		// lower 7-bit value occurs
		for (i = 0; i < pattern.length; i++) {
			bcs[pattern[i] & 0x7F] = i + 1;
		}

		// Precalculate the good suffix shift
		// i is the shift amount being considered
		NEXT: for (i = pattern.length; i > 0; i--) {
			// j is the beginning index of suffix being considered
			for (j = pattern.length - 1; j >= i; j--) {
				// Testing for good suffix
				if (pattern[j] == pattern[j - i]) {
					// src[j..len] is a good suffix
					gss[j - 1] = i;
				} else {
					// No match. The array has already been
					// filled up with correct values before.
					continue NEXT;
				}
			}
			// This fills up the remaining of optoSft
			// any suffix can not have larger shift amount
			// then its sub-suffix. Why???
			while (j > 0) {
				gss[--j] = i;
			}
		}
		if (pattern.length > 0) {
			// Set the guard value because of unicode compression
			gss[pattern.length - 1] = 1;
		}
	}

}
