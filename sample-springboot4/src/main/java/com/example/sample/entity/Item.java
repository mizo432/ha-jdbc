package com.example.sample.entity;

import com.example.sample.dto.ItemRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.envers.Audited;
import java.time.LocalDateTime;

@Entity
@Table(name = "items")
@Audited
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be 100 characters or less")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description must be 500 characters or less")
    @Column(length = 500)
    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be 0 or greater")
    @Column(nullable = false)
    private Integer price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock must be 0 or greater")
    @Column(nullable = false)
    private Integer stock;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Item() {
    }

    public Item(String name, String description, Integer price, Integer stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    /**
     * Create a new Item from a request DTO.
     */
    public static Item from(ItemRequest request) {
        Item item = new Item();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setStock(request.getStock());
        return item;
    }

    /**
     * Update this Item from a request DTO.
     */
    public void updateFrom(ItemRequest request) {
        this.name = request.getName();
        this.description = request.getDescription();
        this.price = request.getPrice();
        this.stock = request.getStock();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
