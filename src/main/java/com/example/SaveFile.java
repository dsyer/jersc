package com.example;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SaveFile {

	private static int ID_LOCATION = 0x19003B4;
	public static int SAVE_HEADERS_SECTION_START_INDEX = 0x19003B0;
	public static int SAVE_HEADERS_SECTION_LENGTH = 0x60000;

	private SaveGame[] games = new SaveGame[10];
	private VerifiedData saveHeaders = new VerifiedData();

	private long id;

	private boolean load(ByteBuffer data) {
		try {
			this.id = data.getLong(ID_LOCATION);
			for (int i = 0; i < 10; i++) {
				this.games[i] = SaveGame.from(data, i);
			}
			saveHeaders = VerifiedData.from(data, SAVE_HEADERS_SECTION_START_INDEX, SAVE_HEADERS_SECTION_LENGTH);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static SaveFile from(ByteBuffer data) {
		SaveFile file = new SaveFile();
		if (file.load(data)) {
			return file;
		} else {
			return null;
		}
	}

	public int length() {
		System.err.println(saveHeaders.length());
		System.err.println(Arrays.asList(games).stream().filter(game -> game != null).map(game -> game.length())
				.collect(Collectors.summingInt(Integer::intValue)));
		return SaveGame.SLOT_START_INDEX + 16 + saveHeaders.length()
				+ Arrays.asList(games).stream().filter(game -> game != null).map(game -> game.length())
						.collect(Collectors.summingInt(Integer::intValue));
	}

	@Override
	public String toString() {
		return "SaveFile [id=" + id + ", games="
				+ Arrays.asList(games).stream().filter(game -> game != null && game.isActive())
						.collect(Collectors.toList())
				+ ", saveHeaders=" + saveHeaders + "]";
	}
}
