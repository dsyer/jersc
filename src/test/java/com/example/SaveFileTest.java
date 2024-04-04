package com.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class SaveFileTest {

	@Test
	public void testFileLength() throws Exception {
		assumeTrue(Paths.get("ER0000.sl2").toFile().exists(), "File does not exist");
		SaveFile file = SaveFile.from(Paths.get("ER0000.sl2"));
		assertThat(file.validate()).isTrue();
		// System.err.println(file.prettyPrint());
	}

	@Test
	public void testCopyGame() throws Exception {
		assumeTrue(Paths.get("ER0000.sl2").toFile().exists(), "File does not exist");
		SaveFile file = SaveFile.from(Paths.get("ER0000.sl2"));
		int activeGameCount = file.getActiveCount();
		SaveGame game = Arrays.asList(file.getGames()).stream().filter(g -> g != null && g.isActive())
			.findFirst().get().named("TestCharacter");
		int inactive = file.findInactive();
		file.replaceSlot(inactive, game);
		assertThat(file.validate()).isTrue();
		System.err.println(file.prettyPrint());
		assertThat(file.getGames()[0].getIndex()).isEqualTo(0);
		SaveGame saved = file.getGames()[inactive];
		assertThat(saved.getCharacterName()).isEqualTo("TestCharacter");
		assertThat(saved.isActive()).isTrue();
		assertThat(saved.getIndex()).isEqualTo(inactive);
		assertThat(file.getActiveCount()).isEqualTo(activeGameCount + 1);
		assertThat(file.validate()).isTrue();
	}

	@Test
	public void testRespecGame() throws Exception {
		assumeTrue(Paths.get("ER0000.sl2").toFile().exists(), "File does not exist");
		SaveFile file = SaveFile.from(Paths.get("ER0000.sl2"));
		SaveGame game = Arrays.asList(file.getGames()).stream().filter(g -> g != null && g.isActive())
			.findFirst().get().named("TestCharacter");
		game = game.respec(new Status(99, 99, 99, 99, 99, 99, 99, 99));
		int slot = file.findInactive();
		file.replaceSlot(slot, game);
		assertThat(file.validate()).isTrue();
		// System.err.println(file.prettyPrint());
		assertThat(file.getGames()[0].getIndex()).isEqualTo(0);
		SaveGame saved = file.getGames()[slot];
		// System.err.println(saved.getStatus().prettyPrint());
		assertThat(saved.isActive()).isTrue();
		assertThat(saved.getIndex()).isEqualTo(slot);
		assertThat(file.validate()).isTrue();
		assertThat(saved.getStatus().STR()).isEqualTo(99);
	}

}