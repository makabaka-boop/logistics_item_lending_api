package com.gsb.logistics.repository;

import com.gsb.logistics.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
    boolean existsByName(String name);
}
