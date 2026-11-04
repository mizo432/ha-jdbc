package com.example.sample.controller;

import com.example.sample.dto.ItemCreateRequest;
import com.example.sample.dto.ItemUpdateRequest;
import com.example.sample.entity.Item;
import com.example.sample.service.ItemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@Validated
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
    public ResponseEntity<List<Item>> findByName(
            @RequestParam @NotBlank(message = "Name is required") String name) {
        // Limit search string length to prevent abuse
        if (name.length() > 100) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(itemService.findByNameContaining(name));
    }

    @GetMapping("/search/price")
    public ResponseEntity<List<Item>> findByPriceRange(
            @RequestParam @NotNull(message = "Min price is required") @Min(value = 0, message = "Min price must be 0 or greater") Integer minPrice,
            @RequestParam @NotNull(message = "Max price is required") @Min(value = 0, message = "Max price must be 0 or greater") Integer maxPrice) {
        if (minPrice > maxPrice) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(itemService.findByPriceBetween(minPrice, maxPrice));
    }

    @PostMapping
    public ResponseEntity<Item> create(@Valid @RequestBody ItemCreateRequest request) {
        Item created = itemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> update(@PathVariable Long id, @Valid @RequestBody ItemUpdateRequest request) {
        return ResponseEntity.ok(itemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
