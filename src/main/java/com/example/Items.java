package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Items {

    private Map<String, Item> names = new HashMap<>();
    private Map<byte[], Item> ids = new HashMap<>();

    public static Items read(Path csv) {
        Items items = new Items();
        try {
            Files.lines(csv).skip(1).map(Items::parse).forEach(item -> items.add(item));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return items;
    }

    public Item find(byte[] id) {
        return ids.get(id);
    }

    public Item find(String name) {
        return names.get(name);
    }

    private void add(Item item) {
        if (item == null || item.name() == null || item.name().length() == 0) {
            return;
        }
        this.ids.put(item.id(), item);
        this.names.put(item.name(), item);
    }

    private static Item parse(String csv) {
        if (csv == null || csv.length() == 0 || !csv.contains(",")) {
            return null;
        }
        String data[] = columns(csv);
        Item result = new Item(data[0].trim(),
                new byte[] { (byte) Integer.parseInt(data[1].trim()), (byte) Integer.parseInt(data[2].trim()) });
        return result;
    }

    private static String[] columns(String csv) {
        String[] result = new String[] { "", "0", "0" };
        try {

            csv = csv.trim();
            if (csv.startsWith("\"")) {
                String name = csv.substring(1);
                name = name.substring(0, name.indexOf("\""));
                String[] id = csv.substring(name.length() + 3).split(",");
                result[0] = name;
                result[1] = id[0].trim();
                result[2] = id[1].trim();
            } else {
                String[] id = csv.split(",");
                result[0] = id[0].trim();
                result[1] = id[1].trim();
                result[2] = id[2].trim();
            }
        } catch (Exception e) {
            System.err.println("Error: " + csv);
        }
        return result;
    }

    public int count() {
        return ids.size();
    }
}
