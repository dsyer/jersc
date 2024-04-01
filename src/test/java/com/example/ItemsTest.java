package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class ItemsTest {

    @Test
    public void testReadFile() {
        Items items = Items.read(Paths.get("src/main/resources/items.csv"));
        assertThat(items).isNotNull();
        Item item = items.find("Trinas Lily");
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
        byte[] id = new byte[] {115, 0};
        assertThat(items.find(id)).isNotNull();
    }
}
