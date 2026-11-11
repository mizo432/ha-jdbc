package com.example.sample.service;

import com.example.sample.dto.ItemRequest;
import com.example.sample.entity.Item;
import com.example.sample.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceUnitTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private Item sampleItem;
    private ItemRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleItem = new Item();
        sampleItem.setId(1L);
        sampleItem.setName("テスト商品");
        sampleItem.setDescription("テスト説明");
        sampleItem.setPrice(1500);
        sampleItem.setStock(10);

        sampleRequest = new ItemRequest();
        sampleRequest.setName("テスト商品");
        sampleRequest.setDescription("テスト説明");
        sampleRequest.setPrice(1500);
        sampleRequest.setStock(10);
    }

    @Test
    void findAll_ShouldReturnAllItems() {
        List<Item> items = List.of(sampleItem);
        when(itemRepository.findAll()).thenReturn(items);

        List<Item> result = itemService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("テスト商品");
        verify(itemRepository).findAll();
    }

    @Test
    void findAll_WhenEmpty_ShouldReturnEmptyList() {
        when(itemRepository.findAll()).thenReturn(List.of());

        List<Item> result = itemService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_WhenExists_ShouldReturnItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));

        Item result = itemService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("テスト商品");
        assertThat(result.getPrice()).isEqualTo(1500);
    }

    @Test
    void findById_WhenNotExists_ShouldThrowException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.findById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void findByNameContaining_ShouldReturnMatchingItems() {
        Item item1 = new Item("テスト商品A", "説明A", 1000, 5);
        Item item2 = new Item("テスト商品B", "説明B", 2000, 10);
        when(itemRepository.findByNameContaining("テスト")).thenReturn(List.of(item1, item2));

        List<Item> result = itemService.findByNameContaining("テスト");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Item::getName)
                .containsExactly("テスト商品A", "テスト商品B");
    }

    @Test
    void findByNameContaining_WhenNoMatch_ShouldReturnEmptyList() {
        when(itemRepository.findByNameContaining("存在しない")).thenReturn(List.of());

        List<Item> result = itemService.findByNameContaining("存在しない");

        assertThat(result).isEmpty();
    }

    @Test
    void findByPriceBetween_ShouldReturnItemsInRange() {
        Item item = new Item("適正価格商品", "説明", 1500, 5);
        when(itemRepository.findByPriceBetween(1000, 2000)).thenReturn(List.of(item));

        List<Item> result = itemService.findByPriceBetween(1000, 2000);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPrice()).isBetween(1000, 2000);
    }

    @Test
    void create_ShouldSaveAndReturnItem() {
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setId(1L);
            return item;
        });

        Item result = itemService.create(sampleRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("テスト商品");
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void update_WhenExists_ShouldUpdateAndReturnItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemRequest updateRequest = new ItemRequest();
        updateRequest.setName("更新商品");
        updateRequest.setDescription("更新説明");
        updateRequest.setPrice(3000);
        updateRequest.setStock(20);

        Item result = itemService.update(1L, updateRequest);

        assertThat(result.getName()).isEqualTo("更新商品");
        assertThat(result.getPrice()).isEqualTo(3000);
        assertThat(result.getStock()).isEqualTo(20);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void update_WhenNotExists_ShouldThrowException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.update(999L, sampleRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void delete_WhenExists_ShouldDeleteItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));

        itemService.delete(1L);

        verify(itemRepository).delete(sampleItem);
    }

    @Test
    void delete_WhenNotExists_ShouldThrowException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.delete(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");
    }
}
