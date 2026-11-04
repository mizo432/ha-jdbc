package com.example.sample.service;

import com.example.sample.dto.ItemRequest;
import com.example.sample.entity.Item;
import com.example.sample.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Item> findByNameContaining(String name) {
        return itemRepository.findByNameContaining(name);
    }

    @Transactional(readOnly = true)
    public List<Item> findByPriceBetween(Integer minPrice, Integer maxPrice) {
        return itemRepository.findByPriceBetween(minPrice, maxPrice);
    }

    @Transactional
    public Item create(ItemRequest request) {
        Item item = Item.from(request);
        return itemRepository.save(item);
    }

    @Transactional
    public Item update(Long id, ItemRequest request) {
        Item existing = findById(id);
        existing.updateFrom(request);
        return itemRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Item existing = findById(id);
        itemRepository.delete(existing);
    }
}
