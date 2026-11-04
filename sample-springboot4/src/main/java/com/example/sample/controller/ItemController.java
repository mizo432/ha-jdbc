package com.example.sample.controller;

import com.example.sample.entity.Item;
import com.example.sample.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<Item>> findAll() {
        return ResponseEntity.ok(itemService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> findById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.findById(id));
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<Item>> findByName(@RequestParam String name) {
        return ResponseEntity.ok(itemService.findByNameContaining(name));
    }

    @GetMapping("/search/price")
    public ResponseEntity<List<Item>> findByPriceRange(
            @RequestParam Integer minPrice,
            @RequestParam Integer maxPrice) {
        return ResponseEntity.ok(itemService.findByPriceBetween(minPrice, maxPrice));
    }

    @PostMapping
    public ResponseEntity<Item> create(@Valid @RequestBody Item item) {
        Item created = itemService.create(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> update(@PathVariable Long id, @Valid @RequestBody Item item) {
        return ResponseEntity.ok(itemService.update(id, item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
