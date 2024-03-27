package com.example;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SaveFile {

	private static final int SAVE_FILE_LENGTH = 26614296;

	private static final int ID_LOCATION = 0x19003B4;
	public static final int SAVE_HEADERS_SECTION_START_INDEX = 0x19003B0;
	public static final int SAVE_HEADERS_SECTION_LENGTH = 0x60000;

	private SaveGame[] games = new SaveGame[10];
	private VerifiedData saveHeaders = new VerifiedData();

	private long id;
	private byte[] idBytes = new byte[8];
	private int[] idLocations;
	private ByteBuffer data;

	private SaveFile() {
	}

	private boolean load(ByteBuffer data) {
		this.data = data;
		try {
			this.id = data.getLong(ID_LOCATION);
			data.get(ID_LOCATION, idBytes);
			this.idLocations = new BytesMatcher(idBytes).matches(data.array());
			for (int i = 0; i < 10; i++) {
				this.games[i] = SaveGame.from(data, i);
			}
			saveHeaders = VerifiedData.from(data, SAVE_HEADERS_SECTION_START_INDEX, SAVE_HEADERS_SECTION_LENGTH);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static SaveFile from(Path path) {
		try {
			return SaveFile.from(ByteBuffer.wrap(Files.readAllBytes(path)).order(ByteOrder.LITTLE_ENDIAN));
		} catch (IOException e) {
			return null;
		}
	}

	public void save(Path path) {
		try {
			Files.write(path, data.array());
		} catch (IOException e) {
			throw new IllegalStateException(e);
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

	public SaveFile changeId(long id) {
		this.id = id;
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putLong(id);
		byte[] bytes = buffer.array();
		for (int location : idLocations) {
			data.put(location, bytes);
		}
		return this;
	}

	public SaveFile replaceSlot(int slot, SaveGame game) {
		if (slot < 0 || slot >= 10) {
			throw new IllegalArgumentException("Invalid slot: " + slot);
		}
		// Ensure we don't overwite an existing game
		game = game.copy();
		SaveGame existing = games[slot];
		// Update local state
		games[slot] = game;
		game.setIndex(slot);
		// Activate it
		this.data.put(SaveGame.CHAR_ACTIVE_STATUS_START_INDEX + slot, (byte) 1);
		// Copy data to buffer
		this.data.put(existing.getSaveDataOffset(), game.getSaveData().getData());
		// Including hash
		this.data.put(existing.getSaveDataOffset() - 16, game.getSaveData().getHash());
		// Copy header data
		this.data.put(existing.getHeaderDataOffset(), game.getHeaderData());
		// Recompute hash
		VerifiedData updatedHeaders = VerifiedData.from(data, SAVE_HEADERS_SECTION_START_INDEX,
				SAVE_HEADERS_SECTION_LENGTH);
		updatedHeaders.reverify();
		// Update local state
		this.saveHeaders = updatedHeaders;
		// Save header hash
		this.data.put(SAVE_HEADERS_SECTION_START_INDEX - 16, updatedHeaders.getHash());
		return this;
	}

	public int getActiveCount() {
		return (int) Arrays.asList(games).stream().filter(game -> game != null && game.isActive()).count();
	}

	public int findInactive() {
		return (int) Arrays.asList(games).stream().filter(game -> game != null && !game.isActive())
				.map(game -> game.getIndex()).findFirst().orElse(games.length - 1);
	}

	public long getId() {
		return id;
	}

	public SaveGame[] getGames() {
		return games;
	}

	public int length() {
		return SaveGame.SLOT_START_INDEX + 16 + saveHeaders.length()
				+ Arrays.asList(games).stream().filter(game -> game != null).map(game -> game.length())
						.collect(Collectors.summingInt(Integer::intValue));
	}

	@Override
	public String toString() {
		return "SaveFile [id=" + id + ", games="
				+ Arrays.asList(games).stream().filter(game -> game != null && game.isActive())
						.collect(Collectors.toList())
				+ ", saveHeaders=" + saveHeaders + ", idLocations=" + Arrays.toString(idLocations) + "]";
	}

	public boolean validate() {
		if (length() != SAVE_FILE_LENGTH) {
			return false;
		}
		if (id != data.getLong(ID_LOCATION)) {
			return false;
		}
		if (!saveHeaders.isVerified()) {
			return false;
		}
		for (SaveGame game : games) {
			if (game != null && !game.getSaveData().isVerified()) {
				return false;
			}
		}
		return true;
	}

	public String prettyPrint() {
		StringBuilder builder = new StringBuilder();
		builder.append("SaveFile: [id=").append(id).append(", valid=").append(validate()).append("]");
		for (SaveGame game : games) {
			if (game != null && game.isActive()) {
				builder.append("\n").append(game.prettyPrint());
			}
		}
		return builder.toString();
	}

	public SaveFile copy() {
		SaveFile copy = new SaveFile();
		copy.id = id;
		copy.idBytes = Arrays.copyOf(idBytes, idBytes.length);
		copy.idLocations = Arrays.copyOf(idLocations, idLocations.length);
		copy.data = ByteBuffer.allocate(data.capacity()).order(data.order());
		copy.data.put(data.array());
		copy.games = new SaveGame[games.length];
		for (int i = 0; i < games.length; i++) {
			if (games[i] != null) {
				copy.games[i] = games[i].copy();
			}
		}
		copy.saveHeaders = saveHeaders.copy();
		return copy;
	}

}
