package com.example.sample.controller;

import com.example.sample.dto.ApiResponse;
import com.example.sample.dto.ItemRequest;
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
    public ResponseEntity<ApiResponse<List<Item>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(itemService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Item>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(itemService.findById(id)));
    }

    @GetMapping("/search/name")
    public ResponseEntity<ApiResponse<List<Item>>> findByName(
            @RequestParam @NotBlank(message = "Name is required") String name) {
        // Limit search string length to prevent abuse
        if (name.length() > 100) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Name must be 100 characters or less"));
        }
        return ResponseEntity.ok(ApiResponse.ok(itemService.findByNameContaining(name)));
    }

    @GetMapping("/search/price")
    public ResponseEntity<ApiResponse<List<Item>>> findByPriceRange(
            @RequestParam @NotNull(message = "Min price is required") @Min(value = 0, message = "Min price must be 0 or greater") Integer minPrice,
            @RequestParam @NotNull(message = "Max price is required") @Min(value = 0, message = "Max price must be 0 or greater") Integer maxPrice) {
        if (minPrice > maxPrice) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Min price must be less than or equal to max price"));
        }
        return ResponseEntity.ok(ApiResponse.ok(itemService.findByPriceBetween(minPrice, maxPrice)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Item>> create(@Valid @RequestBody ItemRequest request) {
        Item created = itemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created, "Item created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Item>> update(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(itemService.update(id, request), "Item updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
