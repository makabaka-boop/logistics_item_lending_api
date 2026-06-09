package com.logistics.itemlending.repository;

import com.logistics.itemlending.entity.ItemLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemLocationRepository extends JpaRepository<ItemLocation, Long>, JpaSpecificationExecutor<ItemLocation> {

    Optional<ItemLocation> findByName(String name);

    boolean existsByName(String name);

    List<ItemLocation> findByEnabledTrueOrderBySortOrderAsc();
}
