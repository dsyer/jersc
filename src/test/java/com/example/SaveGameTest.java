package com.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class SaveGameTest {

	private static final int ID_LOCATION = 0x19003B4;

	@Test
	public void testRenameGame() throws Exception {
		assumeTrue(Paths.get("ER0000.sl2").toFile().exists(), "File does not exist");
		SaveFile file = SaveFile.from(Paths.get("ER0000.sl2"));
		SaveGame game = file.getGames()[0].named("TestCharacter");
		assertThat(game.getCharacterName()).isEqualTo("TestCharacter");
		assertThat(game.getHeaderData()).startsWith(game.getCharacterName().getBytes(StandardCharsets.UTF_16LE));
	}

	@Test
	public void testInventory() throws Exception {
		// System.setProperty("debug", "true");
		assumeTrue(Paths.get("ER0000.sl2").toFile().exists(), "File does not exist");
		SaveFile file = SaveFile.from(Paths.get("ER0000.sl2"));
		// System.err.println(file.prettyPrint());
		SaveGame game = file.getGames()[1];
		assertThat(game.getInventory()).hasSizeGreaterThan(1);
		int offset = 0;
		for (ItemData data : game.getInventory()) {
			assertThat(data.quantity()).isLessThan(1000);
			assertThat(data.quantity()).isGreaterThan(0);
			assertThat(data.address()).isNotEqualTo(offset);
			offset = data.address();
		}
	}

	@Test
	public void testUpdateInventory() throws Exception {
		assumeTrue(Paths.get("ER0000.sl2").toFile().exists(), "File does not exist");
		SaveFile file = SaveFile.from(Paths.get("ER0000.sl2"));
		SaveGame game = file.getGames()[0];
		assertThat(game.getInventory()).hasSizeGreaterThan(1);
		ItemData data = game.getInventory()[1];
		SaveGame updated = game.updateItem(data.item(), data.quantity());
		assertThat(updated).isEqualTo(game);
		updated = game.updateItem(data.item(), data.quantity() + 1);
		assertThat(updated).isNotEqualTo(game);
		assertThat(updated.getSaveData().isVerified()).isTrue();
		assertThat(updated.getInventory()[1].quantity()).isEqualTo(data.quantity() + 1);
	}

	@Test
	public void testIdLocations() throws Exception {
		Path path = Paths.get("ER0000.sl2");
		assumeTrue(path.toFile().exists(), "File does not exist");
		SaveFile file = SaveFile.from(path);
		ByteBuffer data = ByteBuffer.wrap(Files.readAllBytes(path)).order(ByteOrder.LITTLE_ENDIAN);
		byte[] idBytes = new byte[8];
		data.get(ID_LOCATION, idBytes);
		int[] idLocations = new BytesMatcher(idBytes).matches(data.array());
		assertThat(idLocations.length).isGreaterThan(1);
		// For active games, the ID should not be in the save data and not in the header
		for (int i = 0; i < 10; i++) {
			SaveGame game = file.getGames()[i];
			if (!game.isActive()) {
				continue;
			}
			idLocations = new BytesMatcher(idBytes).matches(game.getHeaderData());
			assertThat(idLocations.length).isEqualTo(0);
			idLocations = new BytesMatcher(idBytes).matches(game.getSaveData().getData());
			assertThat(idLocations.length).isGreaterThan(0);
		}
	}

}
