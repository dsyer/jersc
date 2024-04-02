package com.example;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

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
	private ItemData[] inventory;

	private boolean load(ByteBuffer data, int slotIndex) {
		try {
			this.index = slotIndex;
			this.active = data.get(CHAR_ACTIVE_STATUS_START_INDEX + slotIndex) == 1 ? true : false;
			byte[] name = new byte[CHAR_NAME_LENGTH];
			data.get(getHeaderDataOffset(), name);
			this.characterName = new String(name, StandardCharsets.UTF_16LE).trim();
			this.characterLevel = data.getInt(getHeaderDataOffset() + CHAR_LEVEL_LOCATION);
			this.secondsPlayed = data.getInt(getHeaderDataOffset() + CHAR_PLAYED_START_INDEX);
			this.saveData = VerifiedData.from(data, getSaveDataOffset(), SLOT_LENGTH);
			data.get(getHeaderDataOffset(), this.headerData);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return "SaveGame [index=" + index + ", active=" + active + ", characterName=" + characterName
				+ ", characterLevel=" + characterLevel
				+ ", secondsPlayed=" + secondsPlayed + ", saveData=" + saveData + ", headerData=" + headerData.length
				+ "]";
	}

	public static SaveGame from(ByteBuffer data, int slotIndex) {
		SaveGame game = new SaveGame();
		if (game.load(data, slotIndex)) {
			return game;
		} else {
			return null;
		}
	}

	public int getSaveDataOffset() {
		return SLOT_START_INDEX + (index * 0x10) + (index * SLOT_LENGTH);
	}

	public int getHeaderDataOffset() {
		return SAVE_HEADER_START_INDEX + (index * SAVE_HEADER_LENGTH);
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

	public void setCharacterName(String characterName) {
		this.characterName = characterName.trim();
		byte[] name = characterName.getBytes(StandardCharsets.UTF_16LE);
		while (name.length > CHAR_NAME_LENGTH) {
			name = characterName.substring(0, characterName.length() - 1).getBytes(StandardCharsets.UTF_16LE);
		}
		byte[] data = new byte[CHAR_NAME_LENGTH];
		System.arraycopy(name, 0, data, 0, name.length);
		System.arraycopy(data, 0, headerData, 0, data.length);
		saveData.reverify();
	}

	public void setIndex(int slot) {
		this.index = slot;
	}

	public String prettyPrint() {
		StringBuilder builder = new StringBuilder();
		builder.append("Slot=").append(index).append(": ").append(characterName).append("=");
		builder.append("[Level=").append(characterLevel).append(",");
		builder.append("Played=").append(secondsPlayed).append("s]");
		return builder.toString();
	}

	public SaveGame copy() {
		SaveGame copy = new SaveGame();
		copy.index = this.index;
		copy.active = this.active;
		copy.characterName = this.characterName;
		copy.characterLevel = this.characterLevel;
		copy.secondsPlayed = this.secondsPlayed;
		copy.saveData = this.saveData.copy();
		copy.headerData = Arrays.copyOf(this.headerData, this.headerData.length);
		return copy;
	}

	public SaveGame named(String name) {
		SaveGame copy = copy();
		copy.setCharacterName(name);
		return copy;
	}

	public SaveGame updateItem(Item item, int quantity) {
		SaveGame copy = copy();
		ItemData target = null;
		for (ItemData data : copy.getInventory()) {
			if (data.item().equals(item)) {
				target = data;
				break;
			}
		}
		if (target == null) {
			return null;
		}
		if (target.quantity() == quantity) {
			return this;
		}
		ByteBuffer bytes = ByteBuffer.wrap(target.data()).order(ByteOrder.LITTLE_ENDIAN);
		// Update the quantity in the ItemData
		bytes.putShort(4, (short)quantity);
		byte[] saved = copy.saveData.getData();
		// Copy the change in ItemData into the save data
		saved[target.address() + 4] = target.data()[4];
		saved[target.address() + 5] = target.data()[5];
		// Rehash
		copy.saveData.reverify();
		return copy;
	}

	public ItemData[] getInventory() {
		if (this.inventory !=null) {
			return this.inventory;
		}
		Set<ItemData> list = new TreeSet<>();
		BytesMatcher finger = new BytesMatcher(new byte[]{106, 0, 0, (byte) 0xB0, 0x01});
		// There's always a Tarnished Wizened Finger [106, 0]
		// but it's not always in the same place, so find it...
		int offset  = finger.match(saveData.getData(), 0, saveData.length());
		if (offset >=0) {
			ByteBuffer data = ByteBuffer.wrap(saveData.getData());
			offset = offset - 12*1024; // each item occupies 12 bytes
			if (offset < 0) {offset = 0;}
			// ... and then start scanning for other known items
			data.position(offset);
			// Check a maximum of 2048 potential items.
			for (int pointer = 0; pointer < 2048; pointer++) {
				byte[] slice = new byte[12];
				byte[] id = new byte[2];
				data.get(slice);
				id[0] = slice[0];
				id[1] = slice[1];
				if (slice[2] == 0 && slice[3] == (byte)0xB0) { // or 0x80, 0x80?
					Item item = Items.DEFAULT.find(id);
					if (item !=null) {
						// We found a known item, so extract the quantity
						ByteBuffer wrapper = ByteBuffer.wrap(slice).order(ByteOrder.LITTLE_ENDIAN);
						list.add(new ItemData(item, wrapper.getShort(4), offset, slice));
					}
				}
				// Move to the next item potential location
				if (data.position() > saveData.length()) {
					break;
				}
			}
		}
		this.inventory = list.toArray(new ItemData[0]);
		return this.inventory;
	}

}