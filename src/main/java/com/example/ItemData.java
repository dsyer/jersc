package com.example;

public record ItemData(Item item, int quantity, int address, byte[] data) implements Comparable<ItemData> {

    @Override
    public int compareTo(ItemData other) {
        return other.item().name().compareTo(item().name());
    }

    @Override
    public final String toString() {
        return item.name() + ": " + quantity;
    }
}
