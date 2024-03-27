package com.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class SaveGameTest {
	
	@Test
	public void testRenameGame() throws Exception {
		assumeTrue(Paths.get("ER0000.sl2").toFile().exists(), "File does not exist");
		SaveFile file = SaveFile.from(Paths.get("ER0000.sl2"));
		SaveGame game = file.getGames()[0].named("TestCharacter");
		assertThat(game.getCharacterName()).isEqualTo("TestCharacter");
		assertThat(game.getHeaderData()).startsWith(game.getCharacterName().getBytes(StandardCharsets.UTF_16LE));
	}

}
