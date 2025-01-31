package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import java.util.HexFormat;

import org.junit.jupiter.api.Test;

public class ItemsTest {

    @Test
    public void testReadFile() {
        Items items = Items.read(Paths.get("src/main/resources/items.csv"));
        assertThat(items).isNotNull();
        Item item = items.find("Trina's Lily");
        assertThat(item).isNotNull();
        assertThat(items.find(item.id())).isEqualTo(item);
    }
    
    @Test
    public void testEmbeddedCommas() {
        Items items = Items.read(Paths.get("src/test/resources/few.csv"));
        assertThat(items).isNotNull();
        Item item = items.find("O, Flame!");
        assertThat(item).isNotNull();
        assertThat(items.find(item.id())).isEqualTo(item);
    }
    
    @Test
    public void testReadResource() throws Exception {
        Items items = Items.DEFAULT;
        assertThat(items).isNotNull();
        assertThat(items.count()).isGreaterThan(0);
    }

    @Test
    public void testFindById() throws Exception {
        Items items = Items.DEFAULT;
        assertThat(items).isNotNull();
        byte[] id = HexFormat.of().parseHex("A3D21EB0");
        assertThat(items.find(id)).isNotNull();
    }
}
