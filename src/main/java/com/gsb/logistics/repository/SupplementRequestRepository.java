package com.gsb.logistics.repository;

import com.gsb.logistics.domain.SupplementRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplementRequestRepository extends JpaRepository<SupplementRequest, Long> {
    List<SupplementRequest> findByLendingId(Long lendingId);
}
