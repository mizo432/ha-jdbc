package com.example.sample.controller;

import com.example.sample.dto.ItemRequest;
import com.example.sample.entity.Item;
import com.example.sample.service.ItemService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerUnitTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    private Item sampleItem;

    @BeforeEach
    void setUp() {
        sampleItem = new Item();
        sampleItem.setId(1L);
        sampleItem.setName("テスト商品");
        sampleItem.setDescription("テスト説明");
        sampleItem.setPrice(1500);
        sampleItem.setStock(10);
    }

    @Test
    void findAll_ShouldReturnOkWithItems() {
        List<Item> items = List.of(sampleItem);
        when(itemService.findAll()).thenReturn(items);

        ResponseEntity<?> response = itemController.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        @SuppressWarnings("unchecked")
        var body = (com.example.sample.dto.ApiResponse<List<Item>>) response.getBody();
        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getData()).hasSize(1);
        verify(itemService).findAll();
    }

    @Test
    void findAll_WhenEmpty_ShouldReturnEmptyList() {
        when(itemService.findAll()).thenReturn(List.of());

        ResponseEntity<?> response = itemController.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        var body = (com.example.sample.dto.ApiResponse<List<Item>>) response.getBody();
        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getData()).isEmpty();
    }

    @Test
    void findById_WhenExists_ShouldReturnOkWithItem() {
        when(itemService.findById(1L)).thenReturn(sampleItem);

        ResponseEntity<?> response = itemController.findById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        var body = (com.example.sample.dto.ApiResponse<Item>) response.getBody();
        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getData().getName()).isEqualTo("テスト商品");
        verify(itemService).findById(1L);
    }

    @Test
    void findById_WhenNotExists_ShouldThrowException() {
        when(itemService.findById(999L)).thenThrow(new EntityNotFoundException("Item not found with id: 999"));

        try {
            itemController.findById(999L);
            assertThat(false).as("Exception should have been thrown").isTrue();
        } catch (EntityNotFoundException e) {
            assertThat(e.getMessage()).contains("999");
        }
    }

    @Test
    void findByName_ShouldReturnOkWithItems() {
        List<Item> items = List.of(sampleItem);
        when(itemService.findByNameContaining("テスト")).thenReturn(items);

        ResponseEntity<?> response = itemController.findByName("テスト");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        var body = (com.example.sample.dto.ApiResponse<List<Item>>) response.getBody();
        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getData()).hasSize(1);
    }

    @Test
    void findByName_WithLongName_ShouldReturnBadRequest() {
        // 101 characters - exceeds the 100 character limit
        String longName = "A".repeat(101);

        ResponseEntity<?> response = itemController.findByName(longName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        @SuppressWarnings("unchecked")
        var body = (com.example.sample.dto.ApiResponse<Void>) response.getBody();
        assertThat(body.isSuccess()).isFalse();
        assertThat(body.getMessage()).contains("100 characters");
    }

    @Test
    void findByName_WithExactly100Chars_ShouldSucceed() {
        String name = "A".repeat(100);
        when(itemService.findByNameContaining(name)).thenReturn(List.of());

        ResponseEntity<?> response = itemController.findByName(name);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void findByPriceRange_ShouldReturnOkWithItems() {
        List<Item> items = List.of(sampleItem);
        when(itemService.findByPriceBetween(1000, 2000)).thenReturn(items);

        ResponseEntity<?> response = itemController.findByPriceRange(1000, 2000);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        var body = (com.example.sample.dto.ApiResponse<List<Item>>) response.getBody();
        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getData()).hasSize(1);
    }

    @Test
    void findByPriceRange_WhenMinGreaterThanMax_ShouldReturnBadRequest() {
        ResponseEntity<?> response = itemController.findByPriceRange(2000, 1000);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        @SuppressWarnings("unchecked")
        var body = (com.example.sample.dto.ApiResponse<Void>) response.getBody();
        assertThat(body.isSuccess()).isFalse();
        assertThat(body.getMessage()).contains("Min price must be less than or equal to max price");
    }

    @Test
    void findByPriceRange_WithEqualMinMax_ShouldSucceed() {
        when(itemService.findByPriceBetween(1500, 1500)).thenReturn(List.of(sampleItem));

        ResponseEntity<?> response = itemController.findByPriceRange(1500, 1500);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void create_ShouldReturnCreatedWithItem() {
        ItemRequest request = new ItemRequest();
        request.setName("新規商品");
        request.setDescription("新規説明");
        request.setPrice(2500);
        request.setStock(20);

        Item createdItem = new Item();
        createdItem.setId(1L);
        createdItem.setName("新規商品");
        createdItem.setDescription("新規説明");
        createdItem.setPrice(2500);
        createdItem.setStock(20);

        when(itemService.create(any(ItemRequest.class))).thenReturn(createdItem);

        ResponseEntity<?> response = itemController.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        @SuppressWarnings("unchecked")
        var body = (com.example.sample.dto.ApiResponse<Item>) response.getBody();
        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getData().getId()).isEqualTo(1L);
        assertThat(body.getMessage()).contains("created");
    }

    @Test
    void update_WhenExists_ShouldReturnOkWithUpdatedItem() {
        ItemRequest request = new ItemRequest();
        request.setName("更新商品");
        request.setDescription("更新説明");
        request.setPrice(3000);
        request.setStock(30);

        Item updatedItem = new Item();
        updatedItem.setId(1L);
        updatedItem.setName("更新商品");
        updatedItem.setDescription("更新説明");
        updatedItem.setPrice(3000);
        updatedItem.setStock(30);

        when(itemService.update(eq(1L), any(ItemRequest.class))).thenReturn(updatedItem);

        ResponseEntity<?> response = itemController.update(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        var body = (com.example.sample.dto.ApiResponse<Item>) response.getBody();
        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getData().getName()).isEqualTo("更新商品");
        assertThat(body.getMessage()).contains("updated");
    }

    @Test
    void update_WhenNotExists_ShouldThrowException() {
        ItemRequest request = new ItemRequest();
        when(itemService.update(eq(999L), any(ItemRequest.class)))
                .thenThrow(new EntityNotFoundException("Item not found with id: 999"));

        try {
            itemController.update(999L, request);
            assertThat(false).as("Exception should have been thrown").isTrue();
        } catch (EntityNotFoundException e) {
            assertThat(e.getMessage()).contains("999");
        }
    }

    @Test
    void delete_ShouldReturnNoContent() {
        doNothing().when(itemService).delete(1L);

        ResponseEntity<Void> response = itemController.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(itemService).delete(1L);
    }

    @Test
    void delete_WhenNotExists_ShouldThrowException() {
        doThrow(new EntityNotFoundException("Item not found with id: 999"))
                .when(itemService).delete(999L);

        try {
            itemController.delete(999L);
            assertThat(false).as("Exception should have been thrown").isTrue();
        } catch (EntityNotFoundException e) {
            assertThat(e.getMessage()).contains("999");
        }
    }
}
