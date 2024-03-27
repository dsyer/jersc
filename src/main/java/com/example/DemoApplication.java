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

	private void run() {
		System.out.print("Input file (" + input + ")? ");
		String line = scanner.nextLine();
		if (!line.isEmpty()) {
			input = new File(line);
		}
		if (!input.exists()) {
			System.out.println("File " + input + " does not exist.");
			return;
		}
		SaveFile save = SaveFile.from(input.toPath());
		System.out.println(save.prettyPrint());
		System.out.print("Do you want to copy a game from the file (Y/n)? ");
		line = scanner.nextLine();
		if (!line.isEmpty()) {
			if (line.toLowerCase().charAt(0) == 'n') {
				return;
			}
		}
		System.out.print("Output file (" + output + ")? ");
		line = scanner.nextLine();
		if (!line.isEmpty()) {
			output = new File(line);
		}
		SaveFile result;
		if (output.exists()) {
			System.out.println("File " + output + " already exists.");
			result = SaveFile.from(output.toPath());
			if (result != null) {
				System.out.println(result.prettyPrint());
			} else {
				System.out.print("Do you want to overwrite this file (Y/n)? ");
				line = scanner.nextLine();
				if (!line.isEmpty()) {
					if (line.toLowerCase().charAt(0) == 'n') {
						return;
					}
				}
				result = save.copy();
			}
		} else {
			result = save.copy();
		}
		System.out.print("Which slot do you want to copy from " + activeSlots(save) + "? ");
		line = scanner.nextLine();
		int slot = 0;
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
		System.out.print("New name for character (" + game.getCharacterName() + ")? ");
		line = scanner.nextLine();
		if (!line.isEmpty()) {
			game = game.named(line);
		}
		result.replaceSlot(target, game);
		System.out.println(result.prettyPrint());
		System.out.print("Continue (Y/n)? ");
		line = scanner.nextLine();
		if (!line.isEmpty()) {
			if (line.toLowerCase().charAt(0) == 'n') {
				return;
			}
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