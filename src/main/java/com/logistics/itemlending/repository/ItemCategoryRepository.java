package com.logistics.itemlending.repository;

import com.logistics.itemlending.entity.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Long>, JpaSpecificationExecutor<ItemCategory> {

    Optional<ItemCategory> findByName(String name);

    boolean existsByName(String name);

    List<ItemCategory> findByEnabledTrueOrderBySortOrderAsc();
}
