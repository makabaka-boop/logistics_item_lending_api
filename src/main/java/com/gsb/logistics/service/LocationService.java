package com.gsb.logistics.service;

import com.gsb.logistics.domain.Location;
import com.gsb.logistics.repository.LocationRepository;
import com.gsb.logistics.web.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {
    private final LocationRepository repo;

    public LocationService(LocationRepository repo) { this.repo = repo; }

    public List<Location> list() { return repo.findAll(); }

    public Location create(Location l) {
        if (l.getName() == null || l.getName().isBlank()) throw new BusinessException("name 不能为空");
        if (repo.existsByName(l.getName())) throw new BusinessException("位置名已存在");
        l.setId(null);
        return repo.save(l);
    }

    public Location update(Long id, Location l) {
        Location exist = repo.findById(id).orElseThrow(() -> new BusinessException(404, "存放位置不存在"));
        if (l.getName() != null) exist.setName(l.getName());
        if (l.getAddress() != null) exist.setAddress(l.getAddress());
        return repo.save(exist);
    }

    public void delete(Long id) { repo.deleteById(id); }
}
