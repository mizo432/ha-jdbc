package com.example.sample.controller;

import com.example.sample.config.TestPostgreSQLContainer;
import com.example.sample.dto.ApiResponse;
import com.example.sample.dto.ItemRequest;
import com.example.sample.entity.Item;
import com.example.sample.repository.ItemRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("testcontainer")
class ItemControllerTestcontainerIntegrationTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        TestPostgreSQLContainer.postgresMaster.start();
        TestPostgreSQLContainer.postgresSlave.start();
        registry.add("database.master.url", TestPostgreSQLContainer.postgresMaster::getJdbcUrl);
        registry.add("database.master.username", () -> "test");
        registry.add("database.master.password", () -> "test");
        registry.add("database.slave.url", TestPostgreSQLContainer.postgresSlave::getJdbcUrl);
        registry.add("database.slave.username", () -> "test");
        registry.add("database.slave.password", () -> "test");
    }

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private ItemRepository itemRepository;

    private final JsonMapper objectMapper = JsonMapper.builder().build();

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
        itemRepository.deleteAll();
    }

    @Test
    void testGetAllItems() {
        String responseBody = restTestClient.get()
                .uri("/api/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        ApiResponse<List<Item>> response = objectMapper.readValue(responseBody,
                new TypeReference<ApiResponse<List<Item>>>() {});

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData()).isNotEmpty();
    }

    @Test
    void testGetItemById() {
        String responseBody = restTestClient.get()
                .uri("/api/items/" + createdItemId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        ApiResponse<Item> response = objectMapper.readValue(responseBody,
                new TypeReference<ApiResponse<Item>>() {});

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getName()).isEqualTo("テスト商品");
        assertThat(response.getData().getPrice()).isEqualTo(1500);
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
        ItemRequest request = new ItemRequest();
        request.setName("新規商品");
        request.setDescription("新規に作成した商品");
        request.setPrice(2500);
        request.setStock(20);

        String responseBody = restTestClient.post()
                .uri("/api/items")
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        ApiResponse<Item> response = objectMapper.readValue(responseBody,
                new TypeReference<ApiResponse<Item>>() {});

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getName()).isEqualTo("新規商品");
        assertThat(response.getData().getId()).isNotNull();

        // データベースにも保存されていることを確認
        Item savedItem = itemRepository.findById(response.getData().getId()).orElse(null);
        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getName()).isEqualTo("新規商品");
    }

    @Test
    void testCreateItem_ValidationFailed() {
        ItemRequest request = new ItemRequest();
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
        ItemRequest request = new ItemRequest();
        request.setName("更新された商品");
        request.setDescription("更新された説明");
        request.setPrice(3000);
        request.setStock(30);

        String responseBody = restTestClient.put()
                .uri("/api/items/" + createdItemId)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        ApiResponse<Item> response = objectMapper.readValue(responseBody,
                new TypeReference<ApiResponse<Item>>() {});

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getName()).isEqualTo("更新された商品");
        assertThat(response.getData().getPrice()).isEqualTo(3000);
        assertThat(response.getData().getStock()).isEqualTo(30);
    }

    @Test
    void testDeleteItem() {
        restTestClient.delete()
                .uri("/api/items/" + createdItemId)
                .exchange()
                .expectStatus().isNoContent();

        // データベースから削除されたことを確認
        assertThat(itemRepository.findById(createdItemId)).isEmpty();
    }

    @Test
    void testSearchByName() {
        String responseBody = restTestClient.get()
                .uri("/api/items/search/name?name=テスト")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        ApiResponse<List<Item>> response = objectMapper.readValue(responseBody,
                new TypeReference<ApiResponse<List<Item>>>() {});

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData().get(0).getName()).isEqualTo("テスト商品");
    }

    @Test
    void testSearchByPriceRange() {
        String responseBody = restTestClient.get()
                .uri("/api/items/search/price?minPrice=1000&maxPrice=2000")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        ApiResponse<List<Item>> response = objectMapper.readValue(responseBody,
                new TypeReference<ApiResponse<List<Item>>>() {});

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        int price = response.getData().get(0).getPrice();
        assertThat(price).isBetween(1000, 2000);
    }
}
