package com.example.sample.controller;

import com.example.sample.dto.ApiResponse;
import com.example.sample.dto.ItemRequest;
import com.example.sample.dto.ItemRevision;
import com.example.sample.entity.Item;
import com.example.sample.repository.ItemRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("test")
class ItemControllerIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private ItemRepository itemRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

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
    void testGetAllItems() throws JsonProcessingException {
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
    void testGetItemById() throws JsonProcessingException {
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
    void testCreateItem() throws JsonProcessingException {
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
    void testUpdateItem() throws JsonProcessingException {
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
    void testSearchByName() throws JsonProcessingException {
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
    void testSearchByPriceRange() throws JsonProcessingException {
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

    // === Edge case tests ===

    @Test
    void testSearchByName_NoMatch_ShouldReturnEmptyList() throws JsonProcessingException {
        String responseBody = restTestClient.get()
                .uri("/api/items/search/name?name=存在しない商品名")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        ApiResponse<List<Item>> response = objectMapper.readValue(responseBody,
                new TypeReference<ApiResponse<List<Item>>>() {});

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData()).isEmpty();
    }

    @Test
    void testSearchByPriceRange_NoMatch_ShouldReturnEmptyList() throws JsonProcessingException {
        String responseBody = restTestClient.get()
                .uri("/api/items/search/price?minPrice=99999&maxPrice=999999")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        ApiResponse<List<Item>> response = objectMapper.readValue(responseBody,
                new TypeReference<ApiResponse<List<Item>>>() {});

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData()).isEmpty();
    }

    @Test
    void testSearchByPriceRange_MinGreaterThanMax_ShouldReturnBadRequest() throws JsonProcessingException {
        String responseBody = restTestClient.get()
                .uri("/api/items/search/price?minPrice=2000&maxPrice=1000")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        ApiResponse<Void> response = objectMapper.readValue(responseBody,
                new TypeReference<ApiResponse<Void>>() {});

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Min price must be less than or equal to max price");
    }

    @Test
    void testSearchByName_TooLongName_ShouldReturnBadRequest() throws JsonProcessingException {
        String longName = "A".repeat(101);
        String responseBody = restTestClient.get()
                .uri("/api/items/search/name?name=" + longName)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        ApiResponse<Void> response = objectMapper.readValue(responseBody,
                new TypeReference<ApiResponse<Void>>() {});

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("100 characters");
    }

    @Test
    void testSearchByName_Exactly100Chars_ShouldSucceed() throws JsonProcessingException {
        String name = "A".repeat(100);
        restTestClient.get()
                .uri("/api/items/search/name?name=" + name)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testCreateItem_WithNullFields_ShouldReturnBadRequest() {
        ItemRequest request = new ItemRequest();
        // All fields are null - should fail validation

        restTestClient.post()
                .uri("/api/items")
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testCreateItem_WithZeroPriceAndStock_ShouldSucceed() throws JsonProcessingException {
        ItemRequest request = new ItemRequest();
        request.setName("無料商品");
        request.setDescription("無料のサンプル");
        request.setPrice(0);
        request.setStock(0);

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
        assertThat(response.getData().getPrice()).isEqualTo(0);
        assertThat(response.getData().getStock()).isEqualTo(0);
    }

    @Test
    void testCreateItem_WithLongName_ShouldSucceed() throws JsonProcessingException {
        ItemRequest request = new ItemRequest();
        request.setName("A".repeat(100));  // Maximum allowed length
        request.setDescription("説明");
        request.setPrice(1000);
        request.setStock(5);

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
        assertThat(response.getData().getName()).hasSize(100);
    }

    @Test
    void testCreateItem_WithLongDescription_ShouldSucceed() throws JsonProcessingException {
        ItemRequest request = new ItemRequest();
        request.setName("商品");
        request.setDescription("B".repeat(500));  // Maximum allowed length
        request.setPrice(1000);
        request.setStock(5);

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
        assertThat(response.getData().getDescription()).hasSize(500);
    }

    @Test
    void testCreateItem_NameTooLong_ShouldReturnBadRequest() {
        ItemRequest request = new ItemRequest();
        request.setName("A".repeat(101));  // Exceeds maximum length
        request.setDescription("説明");
        request.setPrice(1000);
        request.setStock(5);

        restTestClient.post()
                .uri("/api/items")
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testCreateItem_DescriptionTooLong_ShouldReturnBadRequest() {
        ItemRequest request = new ItemRequest();
        request.setName("商品");
        request.setDescription("B".repeat(501));  // Exceeds maximum length
        request.setPrice(1000);
        request.setStock(5);

        restTestClient.post()
                .uri("/api/items")
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testUpdateItem_NotFound_ShouldReturnNotFound() {
        ItemRequest request = new ItemRequest();
        request.setName("更新商品");
        request.setDescription("更新説明");
        request.setPrice(3000);
        request.setStock(30);

        restTestClient.put()
                .uri("/api/items/99999")
                .body(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testDeleteItem_NotFound_ShouldReturnNotFound() {
        restTestClient.delete()
                .uri("/api/items/99999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testGetAllItems_AfterDelete_ShouldNotContainDeletedItem() throws JsonProcessingException {
        // Create a new item
        ItemRequest request = new ItemRequest();
        request.setName("削除対象商品");
        request.setDescription("削除される商品");
        request.setPrice(1000);
        request.setStock(5);

        String createResponse = restTestClient.post()
                .uri("/api/items")
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        ApiResponse<Item> created = objectMapper.readValue(createResponse,
                new TypeReference<ApiResponse<Item>>() {});
        Long deletedItemId = created.getData().getId();

        // Delete the item
        restTestClient.delete()
                .uri("/api/items/" + deletedItemId)
                .exchange()
                .expectStatus().isNoContent();

        // Verify it's not in the list
        String allResponse = restTestClient.get()
                .uri("/api/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        ApiResponse<List<Item>> allItems = objectMapper.readValue(allResponse,
                new TypeReference<ApiResponse<List<Item>>>() {});

        assertThat(allItems.getData()).noneMatch(item -> item.getId().equals(deletedItemId));
    }

    @Test
    void testSearchByPriceRange_EqualMinMax_ShouldReturnMatchingItems() throws JsonProcessingException {
        // The test item has price 1500
        String responseBody = restTestClient.get()
                .uri("/api/items/search/price?minPrice=1500&maxPrice=1500")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        ApiResponse<List<Item>> response = objectMapper.readValue(responseBody,
                new TypeReference<ApiResponse<List<Item>>>() {});

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).allMatch(item -> item.getPrice() == 1500);
    }

    @Test
    void testCreateItem_NegativePrice_ShouldReturnBadRequest() {
        ItemRequest request = new ItemRequest();
        request.setName("負の価格商品");
        request.setDescription("説明");
        request.setPrice(-100);
        request.setStock(5);

        restTestClient.post()
                .uri("/api/items")
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testCreateItem_NegativeStock_ShouldReturnBadRequest() {
        ItemRequest request = new ItemRequest();
        request.setName("負の在庫商品");
        request.setDescription("説明");
        request.setPrice(1000);
        request.setStock(-5);

        restTestClient.post()
                .uri("/api/items")
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    // === Envers audit history tests ===

    @Test
    void testGetHistory_ShouldReturnRevisions() throws JsonProcessingException {
        // Create an item (already done in setUp)
        // Update it to create a new revision
        ItemRequest updateRequest = new ItemRequest();
        updateRequest.setName("更新された商品");
        updateRequest.setDescription("更新された説明");
        updateRequest.setPrice(3000);
        updateRequest.setStock(30);

        restTestClient.put()
                .uri("/api/items/" + createdItemId)
                .body(updateRequest)
                .exchange()
                .expectStatus().isOk();

        // Get history
        String responseBody = restTestClient.get()
                .uri("/api/items/" + createdItemId + "/history")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        ApiResponse<List<ItemRevision>> response = objectMapper.readValue(responseBody,
                new TypeReference<ApiResponse<List<ItemRevision>>>() {});

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData()).hasSize(2);  // Initial create + update

        // First revision should have original values
        ItemRevision firstRevision = response.getData().get(0);
        assertThat(firstRevision.getRevisionNumber()).isNotNull();
        assertThat(firstRevision.getName()).isEqualTo("テスト商品");
        assertThat(firstRevision.getPrice()).isEqualTo(1500);

        // Second revision should have updated values
        ItemRevision secondRevision = response.getData().get(1);
        assertThat(secondRevision.getName()).isEqualTo("更新された商品");
        assertThat(secondRevision.getPrice()).isEqualTo(3000);
    }

    @Test
    void testGetHistory_ForNewItem_ShouldReturnSingleRevision() throws JsonProcessingException {
        // Create a new item
        ItemRequest request = new ItemRequest();
        request.setName("履歴テスト商品");
        request.setDescription("履歴テスト");
        request.setPrice(2000);
        request.setStock(15);

        String createResponse = restTestClient.post()
                .uri("/api/items")
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        ApiResponse<Item> created = objectMapper.readValue(createResponse,
                new TypeReference<ApiResponse<Item>>() {});
        Long newItemId = created.getData().getId();

        // Get history - should have only one revision (the initial create)
        String responseBody = restTestClient.get()
                .uri("/api/items/" + newItemId + "/history")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        ApiResponse<List<ItemRevision>> response = objectMapper.readValue(responseBody,
                new TypeReference<ApiResponse<List<ItemRevision>>>() {});

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getName()).isEqualTo("履歴テスト商品");
    }

    @Test
    void testGetHistory_ForNonExistentItem_ShouldReturnEmptyList() throws JsonProcessingException {
        String responseBody = restTestClient.get()
                .uri("/api/items/99999/history")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        ApiResponse<List<ItemRevision>> response = objectMapper.readValue(responseBody,
                new TypeReference<ApiResponse<List<ItemRevision>>>() {});

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData()).isEmpty();
    }
}
