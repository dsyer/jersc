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

	private String[] lines(String message) {
		System.out.println(message + "\n(empty line to end input):");
		List<String> lines = new ArrayList<>();
		String line = scanner.nextLine();
		while (!line.isEmpty()) {
			lines.add(line);
			line = scanner.nextLine();
		}
		return lines.toArray(new String[0]);
	}

	private int select(String message, List<Integer> options) {
		System.out.print(message + " " + options + "? ");
		int result = options.get(0);
		String line = scanner.nextLine();
		if (!line.isEmpty()) {
			result = Integer.parseInt(line);
		}
		return result;
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
		int slot = select("Which slot do you want to copy from", activeSlots(save));
		if (slot < 0 || slot >= save.getGames().length || !save.getGames()[slot].isActive()) {
			System.out.println("Invalid slot " + slot);
			return;
		}
		output = new File(accept("Output file", output.getPath()));
		SaveFile result = null;
		if (output.exists()) {
			if (!confirm("File " + output + " already exists.\nDo you want to overwrite this file")) {
				return;
			}
			result = SaveFile.from(output.toPath());
			System.out.println("Existing content:");
			System.out.println(result.prettyPrint());
			if (accept("Do you want to update or replace this file", "U/r").toLowerCase().startsWith("r")) {
				result = null;
			}
		}
		if (result == null) {
			result = save.copy();
		}
		save.changeId(result.getId());
		System.out.println("Writing " + save.getGames()[slot].getCharacterName() + " to:");
		System.out.println(result.prettyPrint());
		int target = select("Which slot do you want to copy to", slots(result));
		if (target < 0 || target >= result.getGames().length) {
			System.out.println("Invalid slot " + target);
			return;
		}
		SaveGame game = save.getGames()[slot];
		String name = accept("New name for character", game.getCharacterName());
		game = game.named(name);
		if (confirm("Do you want to inspect the stats")) {
			Status status = game.getStatus();
			System.out.println(status.prettyPrint());

			String line = accept(
					"Enter updates as name=quantity,name=quantity (or empty to skip)", "");
			if (line.contains("=")) {
				String[] updates = line.split(",");
				Status updated = status;
				for (String update : updates) {
					String[] parts = update.split("=");
					if (parts.length == 2) {
						try {
							String key = parts[0].trim();
							int value = Integer.parseInt(parts[1].trim());
							StatusType type = StatusType.valueOf(key.toUpperCase());
							updated = updated.with(type, value);
						} catch (Exception e) {
						}
					}
				}
				if (!status.equals(updated)) {
					if (confirm("Respec to: " + updated)) {
						game = game.respec(updated);
					}
				}
			}
		}
		if (confirm("Do you want to inspect the inventory")) {
			for (ItemData data : game.getInventory()) {
				String key = data.item().name();
				if (key.contains(",") && !key.startsWith("\"")) {
					key = "\"" + key + "\"";
				}
				System.out.println(key + ", " + data.quantity());
			}
			if (confirm("Do you want to update the inventory")) {

				String[] updates = lines("Enter inventory updates one per line as\n\nname, quantity\n\nor\n\nname, new_name, quantity\n");
				for (String line : updates) {
					if (!line.contains(",")) {
						continue;
					}
					try {
						String key = line.substring(0, line.indexOf(",")).trim();
						String raw = key;
						if (key.startsWith("\"")) {
							key = key.substring(1);
							key = key.substring(0, key.length() - 1);
						}
						int value = Integer.parseInt(line.substring(line.lastIndexOf(",") + 1).trim());
						Item item = Items.DEFAULT.find(key);
						Item newItem = item;
						if (line.indexOf(",") != line.lastIndexOf(",")) {
							String newKey = line.substring(line.indexOf(",") + 1, line.lastIndexOf(",")).trim();
							if (newKey.startsWith("\"")) {
								newKey = newKey.substring(1);
								newKey = newKey.substring(0, newKey.length() - 1);
							}
							newItem = Items.DEFAULT.find(newKey);
							if (newItem == null) {
								System.out.println("Could not find: " + newKey);
								newItem = item;
							}
						}
						if (item != null) {
							game = game.updateItem(item, newItem, value);
							System.out.println("Updated: " + newItem.name() + ", " + value);
						} else {
							System.out.println("Unknown item: " + raw);
						}
					} catch (Exception e) {
						System.out.println("Error: " + line);
					}
				}
			}
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