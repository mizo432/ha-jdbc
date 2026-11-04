package com.example.sample.service;

import com.example.sample.entity.Item;
import com.example.sample.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
    }

    public List<Item> findByNameContaining(String name) {
        return itemRepository.findByNameContaining(name);
    }

    public List<Item> findByPriceBetween(Integer minPrice, Integer maxPrice) {
        return itemRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public Item create(Item item) {
        return itemRepository.save(item);
    }

    public Item update(Long id, Item item) {
        Item existing = findById(id);
        existing.setName(item.getName());
        existing.setDescription(item.getDescription());
        existing.setPrice(item.getPrice());
        existing.setStock(item.getStock());
        return itemRepository.save(existing);
    }

    public void delete(Long id) {
        Item existing = findById(id);
        itemRepository.delete(existing);
    }
}
