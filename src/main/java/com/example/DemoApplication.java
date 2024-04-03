package com.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DemoApplication {

	private File input = new File("ER0000.sl2");

	private File output = new File("ER0000.sl2.out");

	private Scanner scanner = new Scanner(System.in);

	private boolean confirm(String message) {
		System.out.print(message + " (Y/n)? ");
		String line = scanner.nextLine();
		if (!line.isEmpty()) {
			if (line.toLowerCase().charAt(0) == 'n') {
				return false;
			}
		}
		return true;
	}

	private String accept(String message, String fallback) {
		System.out.print(message + " (" + fallback + ")? ");
		String line = scanner.nextLine();
		if (!line.isEmpty()) {
			return line;
		}
		return fallback;
	}

	private void run() {
		input = new File(accept("Input file", input.getPath()));
		if (!input.exists()) {
			System.out.println("File " + input + " does not exist.");
			return;
		}
		SaveFile save = SaveFile.from(input.toPath());
		System.out.println(save.prettyPrint());
		if (!confirm("Do you want to copy a game from the file")) {
			return;
		}
		output = new File(accept("Output file", output.getPath()));
		SaveFile result = null;
		if (output.exists()) {
			if (!confirm("File " + output + " already exists.\nDo you want to overwrite this file")) {
				return;
			}
			result = SaveFile.from(output.toPath());
		}
		if (result == null) {
			result = save.copy();
		}
		save.changeId(result.getId());
		List<Integer> activeSlots = activeSlots(save);
		System.out.print("Which slot do you want to copy from " + activeSlots + "? ");
		String line = scanner.nextLine();
		int slot = activeSlots.isEmpty() ? 0 : activeSlots.get(0);
		if (!line.isEmpty()) {
			slot = Integer.parseInt(line);
			if (slot < 0 || slot >= save.getGames().length || !save.getGames()[slot].isActive()) {
				System.out.println("Invalid slot " + slot);
				return;
			}
		}
		System.out.println("Writing to:");
		System.out.println(result.prettyPrint());
		System.out.print("Which slot do you want to copy to " + slots(result) + "? ");
		line = scanner.nextLine();
		int target = 0;
		if (!line.isEmpty()) {
			target = Integer.parseInt(line);
			if (target < 0 || target >= result.getGames().length) {
				System.out.println("Invalid slot " + target);
				return;
			}
		}
		SaveGame game = save.getGames()[slot];
		String name = accept("New name for character", game.getCharacterName());
		if (!name.equals(game.getCharacterName())) {
			game = game.named(name);
		}
		result.replaceSlot(target, game);
		System.out.println(result.prettyPrint());
		if (!confirm("Continue")) {
			return;
		}
		result.save(output.toPath());
		System.out.println("Saved " + output);
	}

	private List<Integer> slots(SaveFile save) {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < save.getGames().length; i++) {
			SaveGame game = save.getGames()[i];
			if (game != null) {
				list.add(i);
			}
		}
		return list;
	}

	private List<Integer> activeSlots(SaveFile save) {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < save.getGames().length; i++) {
			SaveGame game = save.getGames()[i];
			if (game != null && game.isActive()) {
				list.add(i);
			}
		}
		return list;
	}

	public static void main(String[] args) throws IOException {
		DemoApplication application = new DemoApplication();
		application.run();
	}

	void parse(String[] args) {
		if (args.length > 0) {
			input = new File(args[0]);
		}
		if (args.length > 1) {
			output = new File(args[1]);
		}
	}
}