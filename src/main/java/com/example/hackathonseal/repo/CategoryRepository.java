package com.example.hackathonseal.repo;

import com.example.hackathonseal.models.entity.Category;
import com.example.hackathonseal.models.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByEvent(Event event);
}
