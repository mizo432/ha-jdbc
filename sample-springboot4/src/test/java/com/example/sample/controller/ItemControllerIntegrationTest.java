package com.example.sample.controller;

import com.example.sample.dto.ItemCreateRequest;
import com.example.sample.dto.ItemUpdateRequest;
import com.example.sample.entity.Item;
import com.example.sample.repository.ItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("test")
class ItemControllerIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private ItemRepository itemRepository;

    private Long createdItemId;

    @BeforeEach
    void setUp() {
        // テスト用のアイテムを作成
        Item item = new Item();
        item.setName("テスト商品");
        item.setDescription("テスト用の商品です");
        item.setPrice(1500);
        item.setStock(10);
        item = itemRepository.save(item);
        createdItemId = item.getId();
    }

    @AfterEach
    void tearDown() {
        // テストデータをクリーンアップ
        if (createdItemId != null) {
            itemRepository.deleteById(createdItemId);
        }
    }

    @Test
    void testGetAllItems() {
        List<Item> items = restTestClient.get()
                .uri("/api/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult().getResponseBody();

        assertThat(items).isNotNull();
        assertThat(items).isNotEmpty();
    }

    @Test
    void testGetItemById() {
        Item item = restTestClient.get()
                .uri("/api/items/" + createdItemId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Item.class)
                .returnResult().getResponseBody();

        assertThat(item).isNotNull();
        assertThat(item.getName()).isEqualTo("テスト商品");
        assertThat(item.getPrice()).isEqualTo(1500);
    }

    @Test
    void testGetItemById_NotFound() {
        restTestClient.get()
                .uri("/api/items/99999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testCreateItem() {
        ItemCreateRequest request = new ItemCreateRequest();
        request.setName("新規商品");
        request.setDescription("新規に作成した商品");
        request.setPrice(2500);
        request.setStock(20);

        Item created = restTestClient.post()
                .uri("/api/items")
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Item.class)
                .returnResult().getResponseBody();

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("新規商品");
        assertThat(created.getId()).isNotNull();

        // データベースにも保存されていることを確認
        Item savedItem = itemRepository.findById(created.getId()).orElse(null);
        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getName()).isEqualTo("新規商品");

        // クリーンアップ
        itemRepository.delete(savedItem);
    }

    @Test
    void testCreateItem_ValidationFailed() {
        ItemCreateRequest request = new ItemCreateRequest();
        request.setName("");  // 空文字はバリデーションエラー
        request.setPrice(-1);  // 負の数はバリデーションエラー
        request.setStock(0);

        restTestClient.post()
                .uri("/api/items")
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testUpdateItem() {
        ItemUpdateRequest request = new ItemUpdateRequest();
        request.setName("更新された商品");
        request.setDescription("更新された説明");
        request.setPrice(3000);
        request.setStock(30);

        Item updated = restTestClient.put()
                .uri("/api/items/" + createdItemId)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Item.class)
                .returnResult().getResponseBody();

        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("更新された商品");
        assertThat(updated.getPrice()).isEqualTo(3000);
        assertThat(updated.getStock()).isEqualTo(30);
    }

    @Test
    void testDeleteItem() {
        restTestClient.delete()
                .uri("/api/items/" + createdItemId)
                .exchange()
                .expectStatus().isNoContent();

        // データベースから削除されたことを確認
        assertThat(itemRepository.findById(createdItemId)).isEmpty();

        // createdItemIdをnullにしてtearDownでの削除エラーを防ぐ
        createdItemId = null;
    }

    @Test
    void testSearchByName() {
        restTestClient.get()
                .uri("/api/items/search/name?name=テスト")
                .exchange()
                .expectStatus().isOk();

        List<Object> itemsJson = restTestClient.get()
                .uri("/api/items/search/name?name=テスト")
                .exchange()
                .expectBody(List.class)
                .returnResult().getResponseBody();

        assertThat(itemsJson).isNotNull();
        assertThat(itemsJson).isNotEmpty();
        @SuppressWarnings("unchecked")
        Map<String, Object> firstItem = (Map<String, Object>) itemsJson.get(0);
        assertThat(firstItem.get("name")).isEqualTo("テスト商品");
    }

    @Test
    void testSearchByPriceRange() {
        restTestClient.get()
                .uri("/api/items/search/price?minPrice=1000&maxPrice=2000")
                .exchange()
                .expectStatus().isOk();

        List<Object> itemsJson = restTestClient.get()
                .uri("/api/items/search/price?minPrice=1000&maxPrice=2000")
                .exchange()
                .expectBody(List.class)
                .returnResult().getResponseBody();

        assertThat(itemsJson).isNotNull();
        assertThat(itemsJson).isNotEmpty();
        @SuppressWarnings("unchecked")
        Map<String, Object> firstItem = (Map<String, Object>) itemsJson.get(0);
        int price = (Integer) firstItem.get("price");
        assertThat(price).isBetween(1000, 2000);
    }
}
