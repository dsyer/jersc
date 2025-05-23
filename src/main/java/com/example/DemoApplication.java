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

	private boolean nowait;

	private int fromSlot = 0;

	private int toSlot = 1;

	private String name = null;

	private boolean confirm(String message) {
		System.out.print(message + " (Y/n)? ");
		if (!nowait) {
			String line = scanner.nextLine();
			if (!line.isEmpty()) {
				if (line.toLowerCase().charAt(0) == 'n') {
					return false;
				}
			}
		}
		return true;
	}

	private String accept(String message, String fallback) {
		System.out.print(message + " (" + fallback + ")? ");
		if (!nowait) {
			String line = scanner.nextLine();
			if (!line.isEmpty()) {
				return line;
			}
		}
		return fallback;
	}

	private String[] lines(String message) {
		System.out.println(message + "\n(empty line to end input):");
		List<String> lines = new ArrayList<>();
		if (!nowait) {
			String line = scanner.nextLine();
			while (!line.isEmpty()) {
				lines.add(line);
				line = scanner.nextLine();
			}
		}
		return lines.toArray(new String[0]);
	}

	private int select(String message, List<Integer> options, int defaultValue) {
		System.out.print(message + " " + options + " (" + defaultValue + ")? ");
		int result = defaultValue;
		if (!nowait) {
			String line = scanner.nextLine();
			if (!line.isEmpty()) {
				result = Integer.parseInt(line);
			}
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
		int slot = select("Which slot do you want to copy from", activeSlots(save), fromSlot);
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
		int target = select("Which slot do you want to copy to", slots(result), toSlot);
		if (target < 0 || target >= result.getGames().length) {
			System.out.println("Invalid slot " + target);
			return;
		}
		SaveGame game = save.getGames()[slot];
		String name = accept("New name for character", name(save, target));
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

				String[] updates = lines(
						"Enter inventory updates one per line as\n\nname, quantity\n\nor\n\nname, new_name, quantity\n");
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

	private String name(SaveFile save, int target) {
		if (this.name != null) {
			return this.name;
		} 
		SaveGame game = save.getGames()[target];
		if (game.isActive()) {
			return game.getCharacterName();
		}
		return "Slot" + target;
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
		try {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-h")) {
					usage();
					return;
				}
				if (args[i].equals("-y")) {
					application.nowait = true;
				}
				if (args[i].equals("-i")) {
					application.fromSlot = Integer.parseInt(args[i + 1]);
					i++;
				}
				if (args[i].equals("-o")) {
					application.toSlot = Integer.parseInt(args[i + 1]);
					i++;
				}
				if (args[i].equals("-n")) {
					application.name = args[i + 1];
					i++;
				}
			}
		} catch (NumberFormatException e) {
			usage();
			return;
		}
		application.run();
	}

	private static void usage() {
		System.out.println("Usage: jersc [-y] [-i slot] [-o slot]");
		System.out.println("  -y: do not wait for user input");
		System.out.println("  -i slot: input slot (integer, default 0)");
		System.out.println("  -o slot: output slot (integer, default 1)");
		System.out.println("  -n name: new name for character (default to existing output slot name if it exists)");
	}

}