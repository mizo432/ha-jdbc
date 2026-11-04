package com.example.sample.repository;

import com.example.sample.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByNameContaining(String name);

    List<Item> findByPriceBetween(Integer minPrice, Integer maxPrice);
}
