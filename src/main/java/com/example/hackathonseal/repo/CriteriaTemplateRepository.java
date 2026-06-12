package com.example.hackathonseal.repo;

import com.example.hackathonseal.models.entity.CriteriaTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CriteriaTemplateRepository extends JpaRepository<CriteriaTemplate, Long> {
}
