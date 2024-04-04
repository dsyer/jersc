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
	private StatusData status;

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

	private void setCharacterName(String characterName) {
		String oldName = this.characterName;
		this.characterName = characterName.trim();
		byte[] name = characterName.getBytes(StandardCharsets.UTF_16LE);
		while (name.length > CHAR_NAME_LENGTH) {
			name = characterName.substring(0, characterName.length() - 1).getBytes(StandardCharsets.UTF_16LE);
		}
		byte[] data = new byte[CHAR_NAME_LENGTH];
		System.arraycopy(name, 0, data, 0, name.length);
		System.arraycopy(data, 0, headerData, 0, data.length);
		for (int i : new BytesMatcher(oldName.getBytes(StandardCharsets.UTF_16LE)).matches(saveData.getData())) {
			// Some games get the character name stashed in the save data too
			ByteBuffer.wrap(saveData.getData()).order(ByteOrder.LITTLE_ENDIAN).put(i, data);
		}
		saveData.reverify();
	}

	public void setIndex(int slot) {
		this.index = slot;
	}

	public String prettyPrint() {
		StringBuilder builder = new StringBuilder();
		builder.append("Slot=").append(index).append(": ").append(characterName).append("=");
		builder.append("[level=").append(characterLevel).append(", ");
		builder.append("played=").append(secondsPlayed).append("s, ");
		builder.append("valid=").append(saveData.isVerified()).append("]");
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
		ItemData target = null;
		for (ItemData data : getInventory()) {
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
		if (quantity > 999) {
			// They might overflow into the stored slot? Not sure. Also what about tools (limit 99) 
			// or key items (limit 1)?
			quantity = 999;
		}
		// Update the quantity in the ItemData
		SaveGame copy = copy();
		byte[] saved = copy.saveData.getData();
		// Copy the change in ItemData into the save data
		ByteBuffer.wrap(saved).order(ByteOrder.LITTLE_ENDIAN).putShort(target.address() + 4, (short)quantity);
		// Rehash
		copy.saveData.reverify();
		return copy;
	}

	public StatusData getStatus() {
		if (this.status == null) {
			ByteBuffer data = ByteBuffer.wrap(saveData.getData()).order(ByteOrder.LITTLE_ENDIAN);
			for (int offset = 0; offset < saveData.getData().length - 48; offset++) {
				StatusData status = StatusData.from(data, offset);
				if (status != null && status.status().level() == characterLevel) {
					this.status = status;
					break;
				}
			}
		}
		return this.status;
	}

	public ItemData[] getInventory() {
		if (this.inventory != null) {
			return this.inventory;
		}
		boolean debug = !Environment.get("debug", "false").equals("false");
		Set<ItemData> list = new TreeSet<>();
		BytesMatcher finger = new BytesMatcher(new byte[] { 106, 0, 0, (byte) 0xB0, 0x01 });
		// There's always a Tarnished Wizened Finger [106, 0]
		// but it's not always in the same place, so find it...
		int offset = finger.match(saveData.getData(), 0, saveData.length());
		if (offset >= 0) {
			ByteBuffer data = ByteBuffer.wrap(saveData.getData());
			offset = offset - 12 * 1024; // each item occupies 12 bytes
			if (offset < 0) {
				offset = 0;
			}
			// ... and then start scanning for other known items
			data.position(offset);
			// Check a maximum of 2048 potential items.
			for (int pointer = 0; pointer < 2048; pointer++) {
				byte[] slice = new byte[12];
				byte[] id = new byte[2];
				data.get(slice);
				id[0] = slice[0];
				id[1] = slice[1];
				if (slice[2] == 0 && slice[3] == (byte) 0xB0) { // or 0x80, 0x80?
					Item item = Items.DEFAULT.find(id);
					ByteBuffer wrapper = ByteBuffer.wrap(slice).order(ByteOrder.LITTLE_ENDIAN);
					if (item != null) {
						// We found a known item, so extract the quantity
						list.add(new ItemData(item, wrapper.getShort(4), data.position() - slice.length, slice));
						if (debug) {
							System.err.println((data.position() - slice.length) + " " + item + ": " + wrapper.getShort(4) + ", " + Arrays.toString(slice));
						}
					} else if (debug) {
						System.err.println((data.position() - slice.length) + " ? " + Arrays.toString(id) + ": " + wrapper.getShort(4) + ", "
								+ Arrays.toString(slice));
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

	public SaveGame respec(Status status) {
		if (status == null || (getStatus()!=null && status.equals(getStatus().status()) && status.level() == characterLevel)) {
			// Nothing to do
			return this;
		}
		SaveGame copy = copy();
		copy.getStatus().respec(status);
		copy.characterLevel = status.level();
		ByteBuffer data = ByteBuffer.wrap(copy.headerData).order(ByteOrder.LITTLE_ENDIAN);
		data.putInt(CHAR_LEVEL_LOCATION, status.level());
		return copy;
	}

}