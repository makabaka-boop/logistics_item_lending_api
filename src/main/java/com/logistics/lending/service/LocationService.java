package com.logistics.lending.service;

import com.logistics.lending.dto.LocationRequest;
import com.logistics.lending.entity.StorageLocation;
import com.logistics.lending.exception.BusinessException;
import com.logistics.lending.repository.StorageLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final StorageLocationRepository locationRepository;

    public List<StorageLocation> list() {
        return locationRepository.findAll();
    }

    public StorageLocation create(LocationRequest request) {
        if (locationRepository.existsByName(request.getName())) {
            throw new BusinessException("位置名称已存在");
        }
        StorageLocation location = new StorageLocation();
        location.setName(request.getName());
        location.setDescription(request.getDescription());
        return locationRepository.save(location);
    }

    public StorageLocation update(Long id, LocationRequest request) {
        StorageLocation location = locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("位置不存在"));
        if (!location.getName().equals(request.getName()) && locationRepository.existsByName(request.getName())) {
            throw new BusinessException("位置名称已存在");
        }
        location.setName(request.getName());
        location.setDescription(request.getDescription());
        return locationRepository.save(location);
    }

    public void delete(Long id) {
        locationRepository.deleteById(id);
    }
}
