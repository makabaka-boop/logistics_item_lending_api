package com.logistics.lending.repository;

import com.logistics.lending.entity.StorageLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long> {
    boolean existsByName(String name);
}
