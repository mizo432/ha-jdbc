package com.example.sample.controller;

import com.example.sample.dto.ApiResponse;
import com.example.sample.dto.ItemRequest;
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

/**
 * Integration tests for MSSQL dialect using H2 in MSSQLServer compatibility mode.
 * This validates that the MSSQL dialect generates correct SQL and that the application
 * works correctly with MSSQL-compatible SQL patterns.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("mssql-test")
class ItemControllerMSSQLIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private ItemRepository itemRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private Long createdItemId;

    @BeforeEach
    void setUp() {
        Item item = new Item();
        item.setName("MSSQL Test Item");
        item.setDescription("Item created for MSSQL integration test");
        item.setPrice(1500);
        item.setStock(10);
        item = itemRepository.save(item);
        createdItemId = item.getId();
    }

    @AfterEach
    void tearDown() {
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
        assertThat(response.getData().getName()).isEqualTo("MSSQL Test Item");
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
        request.setName("New MSSQL Item");
        request.setDescription("Created via MSSQL integration test");
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
        assertThat(response.getData().getName()).isEqualTo("New MSSQL Item");
        assertThat(response.getData().getPrice()).isEqualTo(2500);
        assertThat(response.getData().getId()).isNotNull();
    }

    @Test
    void testUpdateItem() throws JsonProcessingException {
        ItemRequest request = new ItemRequest();
        request.setName("Updated MSSQL Item");
        request.setDescription("Updated via MSSQL integration test");
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
        assertThat(response.getData().getName()).isEqualTo("Updated MSSQL Item");
        assertThat(response.getData().getPrice()).isEqualTo(3000);
        assertThat(response.getData().getStock()).isEqualTo(30);
    }

    @Test
    void testDeleteItem() {
        restTestClient.delete()
                .uri("/api/items/" + createdItemId)
                .exchange()
                .expectStatus().isNoContent();

        restTestClient.get()
                .uri("/api/items/" + createdItemId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testItemWithIdentityColumn() throws JsonProcessingException {
        /*
         * MSSQL uses IDENTITY columns for auto-increment.
         * This test verifies that the IDENTITY column works correctly
         * with the MSSQL dialect (simulated via H2 in MSSQLServer mode).
         */
        ItemRequest request = new ItemRequest();
        request.setName("Identity Test Item");
        request.setDescription("Tests IDENTITY column behavior");
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
        assertThat(response.getData().getId()).isNotNull().isGreaterThan(0);

        // Clean up
        itemRepository.deleteById(response.getData().getId());
    }

    @Test
    void testBatchInsert() throws JsonProcessingException {
        /*
         * Verify batch insert works with MSSQL-compatible SQL.
         */
        for (int i = 0; i < 3; i++) {
            ItemRequest request = new ItemRequest();
            request.setName("Batch Item " + i);
            request.setDescription("Batch insert test item " + i);
            request.setPrice(100 * (i + 1));
            request.setStock(10 * (i + 1));

            restTestClient.post()
                    .uri("/api/items")
                    .body(request)
                    .exchange()
                    .expectStatus().isCreated();
        }

        String responseBody = restTestClient.get()
                .uri("/api/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        ApiResponse<List<Item>> response = objectMapper.readValue(responseBody,
                new TypeReference<ApiResponse<List<Item>>>() {});

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).hasSizeGreaterThanOrEqualTo(3);
    }
}
