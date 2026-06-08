package com.logistics.service;

import com.logistics.dto.LocationRequest;
import com.logistics.entity.Location;
import com.logistics.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    public List<Location> findAll() {
        return locationRepository.findAll();
    }

    public Location findById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("存放位置不存在"));
    }

    public Location create(LocationRequest request) {
        Location location = new Location();
        location.setName(request.getName());
        location.setDescription(request.getDescription());
        return locationRepository.save(location);
    }

    public Location update(Long id, LocationRequest request) {
        Location location = findById(id);
        location.setName(request.getName());
        location.setDescription(request.getDescription());
        return locationRepository.save(location);
    }

    public void delete(Long id) {
        locationRepository.deleteById(id);
    }
}
