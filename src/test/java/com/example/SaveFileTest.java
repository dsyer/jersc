package com.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Paths;

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
		SaveGame game = file.getGames()[0].named("TestCharacter");
		file.replaceSlot(file.findInactive(), game);
		assertThat(file.validate()).isTrue();
		System.err.println(file.prettyPrint());
		assertThat(file.getGames()[0].getIndex()).isEqualTo(0);
		SaveGame saved = file.getGames()[file.getActiveCount()-1];
		assertThat(saved.getCharacterName()).isEqualTo("TestCharacter");
		assertThat(saved.isActive()).isTrue();
		assertThat(saved.getIndex()).isEqualTo(file.getActiveCount()-1);
		assertThat(file.getActiveCount()).isEqualTo(activeGameCount + 1);
		assertThat(file.validate()).isTrue();
	}

}
