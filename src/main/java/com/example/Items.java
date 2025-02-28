package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Map;

public class Items implements Iterable<Item> {
    
    private Map<String, Item> names = new HashMap<>();
    private Map<ByteArrayWrapper, Item> ids = new HashMap<>();
    private static HexFormat FORMAT = HexFormat.of();

    public static Items DEFAULT;

    static {
        try (InputStream csv = Items.class.getClassLoader().getResourceAsStream("items.csv")) {
            DEFAULT = Items.read(csv);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Items read(Path csv) {
        Items items = new Items();
        try {
            Files.lines(csv).skip(1).map(Items::parse).forEach(item -> items.add(item));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return items;
    }

    public static Items read(InputStream csv) {
        Items items = new Items();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(csv));
            String line = reader.readLine();
            if (line != null) {
                // skip header
                line = reader.readLine();
            }
            while (line != null) {
                Item item = Items.parse(line);
                items.add(item);
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return items;
    }

    public Item find(byte[] id) {
        return ids.get(new ByteArrayWrapper(id));
    }

    public Item find(String name) {
        return names.get(name);
    }

    private void add(Item item) {
        if (item == null || item.name() == null || item.name().length() == 0) {
            return;
        }
        this.ids.put(new ByteArrayWrapper(item.id()), item);
        this.names.put(item.name(), item);
    }

    private static Item parse(String csv) {
        if (csv == null || csv.length() == 0 || !csv.contains(",")) {
            return null;
        }
        String data[] = columns(csv);
        data[1] = data[1].trim();
        if (data[1].length() == 0) {
            return null;
        }
        if (data[1].length()%2 == 1) {
            data[1] = "0" + data[1];
        }
        Item result = new Item(data[0], FORMAT.parseHex(data[1]), data[2]);
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

    @Override
    public Iterator<Item> iterator() {
        return ids.values().iterator();
    }

    private static class ByteArrayWrapper {
        private final byte[] data;

        public ByteArrayWrapper(byte[] data) {
            if (data == null) {
                throw new NullPointerException();
            }
            this.data = data;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof ByteArrayWrapper)) {
                return false;
            }
            return Arrays.equals(data, ((ByteArrayWrapper) other).data);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(data);
        }
    }
}
