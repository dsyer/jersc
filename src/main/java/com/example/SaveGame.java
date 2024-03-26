package com.example;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SaveGame {

	public static int SLOT_START_INDEX = 0x310;
	public static int SLOT_LENGTH = 0x280000;
	public static int SAVE_HEADER_START_INDEX = 0x1901D0E;
	public static int SAVE_HEADER_LENGTH = 0x24C;
	public static int CHAR_ACTIVE_STATUS_START_INDEX = 0x1901D04;

	private static int CHAR_NAME_LENGTH = 0x22;
	private static int CHAR_LEVEL_LOCATION = 0x22;
	private static int CHAR_PLAYED_START_INDEX = 0x26;

	private int index;
	private boolean active;
	private String characterName;
	private int characterLevel;
	private long secondsPlayed;
	private VerifiedData saveData = new VerifiedData();
	private byte[] headerData = new byte[SAVE_HEADER_LENGTH];

	private boolean load(ByteBuffer data, int slotIndex) {
		try {
			this.index = slotIndex;
			this.active = data.get(CHAR_ACTIVE_STATUS_START_INDEX + slotIndex) == 1 ? true : false;
			byte[] name = new byte[CHAR_NAME_LENGTH];
			data.get(SAVE_HEADER_START_INDEX + (slotIndex * SAVE_HEADER_LENGTH), name);
			this.characterName = new String(name, StandardCharsets.UTF_16LE).trim();
			this.characterLevel = data.getInt(SAVE_HEADER_START_INDEX + (slotIndex * SAVE_HEADER_LENGTH) + CHAR_LEVEL_LOCATION);
			this.secondsPlayed = data.getInt(SAVE_HEADER_START_INDEX + (slotIndex * SAVE_HEADER_LENGTH) + CHAR_PLAYED_START_INDEX);
			this.saveData = VerifiedData.from(data, SLOT_START_INDEX + (slotIndex * 0x10) + (slotIndex * SLOT_LENGTH), SLOT_LENGTH);
			data.get(SAVE_HEADER_START_INDEX + (slotIndex * SAVE_HEADER_LENGTH), this.headerData);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return "SaveGame [index=" + index + ", active=" + active + ", characterName=" + characterName + ", characterLevel=" + characterLevel
				+ ", secondsPlayed=" + secondsPlayed + ", saveData=" + saveData + ", headerData=" + headerData.length+ "]";
	}

	public static SaveGame from(ByteBuffer data, int slotIndex) {
		SaveGame game = new SaveGame();
		if (game.load(data, slotIndex)) {
			return game;
		} else {
			return null;
		}
	}

	public int getIndex() {
		return index;
	}

	public boolean isActive() {
		return active;
	}

	public String getCharacterName() {
		return characterName;
	}

	public int getCharacterLevel() {
		return characterLevel;
	}

	public long getSecondsPlayed() {
		return secondsPlayed;
	}

	public VerifiedData getSaveData() {
		return saveData;
	}

	public byte[] getHeaderData() {
		return headerData;
	}

	public int length() {
		return saveData.length() + headerData.length;
	}
}