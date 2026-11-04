package com.example.sample.entity;

import com.example.sample.dto.ItemRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ItemEntityUnitTest {

    @Test
    void from_ShouldCreateItemFromRequest() {
        ItemRequest request = new ItemRequest();
        request.setName("テスト商品");
        request.setDescription("テスト説明");
        request.setPrice(1500);
        request.setStock(10);

        Item item = Item.from(request);

        assertThat(item.getName()).isEqualTo("テスト商品");
        assertThat(item.getDescription()).isEqualTo("テスト説明");
        assertThat(item.getPrice()).isEqualTo(1500);
        assertThat(item.getStock()).isEqualTo(10);
        assertThat(item.getId()).isNull();
        assertThat(item.getCreatedAt()).isNull();
        assertThat(item.getUpdatedAt()).isNull();
    }

    @Test
    void from_WithNullDescription_ShouldHandleNull() {
        ItemRequest request = new ItemRequest();
        request.setName("テスト商品");
        request.setDescription(null);
        request.setPrice(1500);
        request.setStock(10);

        Item item = Item.from(request);

        assertThat(item.getName()).isEqualTo("テスト商品");
        assertThat(item.getDescription()).isNull();
        assertThat(item.getPrice()).isEqualTo(1500);
    }

    @Test
    void updateFrom_ShouldUpdateAllFields() {
        Item item = new Item();
        item.setId(1L);
        item.setName("元の商品");
        item.setDescription("元の説明");
        item.setPrice(1000);
        item.setStock(5);

        ItemRequest request = new ItemRequest();
        request.setName("更新商品");
        request.setDescription("更新説明");
        request.setPrice(2000);
        request.setStock(15);

        item.updateFrom(request);

        assertThat(item.getName()).isEqualTo("更新商品");
        assertThat(item.getDescription()).isEqualTo("更新説明");
        assertThat(item.getPrice()).isEqualTo(2000);
        assertThat(item.getStock()).isEqualTo(15);
        // ID should remain unchanged
        assertThat(item.getId()).isEqualTo(1L);
    }

    @Test
    void updateFrom_WithNullDescription_ShouldSetNull() {
        Item item = new Item();
        item.setName("元の商品");
        item.setDescription("元の説明");
        item.setPrice(1000);
        item.setStock(5);

        ItemRequest request = new ItemRequest();
        request.setName("更新商品");
        request.setDescription(null);
        request.setPrice(2000);
        request.setStock(15);

        item.updateFrom(request);

        assertThat(item.getName()).isEqualTo("更新商品");
        assertThat(item.getDescription()).isNull();
    }

    @Test
    void constructor_ShouldSetFields() {
        Item item = new Item("テスト商品", "テスト説明", 1500, 10);

        assertThat(item.getName()).isEqualTo("テスト商品");
        assertThat(item.getDescription()).isEqualTo("テスト説明");
        assertThat(item.getPrice()).isEqualTo(1500);
        assertThat(item.getStock()).isEqualTo(10);
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        Item item = new Item();

        item.setId(1L);
        item.setName("テスト商品");
        item.setDescription("テスト説明");
        item.setPrice(1500);
        item.setStock(10);
        item.setCreatedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
        item.setUpdatedAt(LocalDateTime.of(2024, 1, 1, 0, 0));

        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getName()).isEqualTo("テスト商品");
        assertThat(item.getDescription()).isEqualTo("テスト説明");
        assertThat(item.getPrice()).isEqualTo(1500);
        assertThat(item.getStock()).isEqualTo(10);
        assertThat(item.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0));
        assertThat(item.getUpdatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0));
    }

    @Test
    void from_WithZeroPriceAndStock_ShouldHandleBoundaryValues() {
        ItemRequest request = new ItemRequest();
        request.setName("無料商品");
        request.setDescription("無料のサンプル");
        request.setPrice(0);
        request.setStock(0);

        Item item = Item.from(request);

        assertThat(item.getPrice()).isEqualTo(0);
        assertThat(item.getStock()).isEqualTo(0);
    }

    @Test
    void from_WithLongName_ShouldHandleMaxLength() {
        ItemRequest request = new ItemRequest();
        // 100 characters - the maximum allowed length
        request.setName("A".repeat(100));
        request.setDescription("説明");
        request.setPrice(1000);
        request.setStock(5);

        Item item = Item.from(request);

        assertThat(item.getName()).hasSize(100);
    }

    @Test
    void from_WithLongDescription_ShouldHandleMaxLength() {
        ItemRequest request = new ItemRequest();
        request.setName("商品");
        // 500 characters - the maximum allowed length
        request.setDescription("B".repeat(500));
        request.setPrice(1000);
        request.setStock(5);

        Item item = Item.from(request);

        assertThat(item.getDescription()).hasSize(500);
    }
}
