package com.logistics.itemlending.service;

import com.logistics.itemlending.entity.ItemLocation;
import com.logistics.itemlending.exception.BusinessException;
import com.logistics.itemlending.repository.ItemLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItemLocationService {

    @Autowired
    private ItemLocationRepository itemLocationRepository;

    public ItemLocation createLocation(ItemLocation location) {
        if (itemLocationRepository.existsByName(location.getName())) {
            throw new BusinessException("位置名称已存在");
        }
        return itemLocationRepository.save(location);
    }

    public ItemLocation updateLocation(Long id, ItemLocation locationDetails) {
        ItemLocation location = itemLocationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("存放位置不存在"));

        if (StringUtils.hasText(locationDetails.getName())) {
            if (!location.getName().equals(locationDetails.getName()) &&
                    itemLocationRepository.existsByName(locationDetails.getName())) {
                throw new BusinessException("位置名称已存在");
            }
            location.setName(locationDetails.getName());
        }
        if (locationDetails.getDescription() != null) {
            location.setDescription(locationDetails.getDescription());
        }
        if (locationDetails.getSortOrder() != null) {
            location.setSortOrder(locationDetails.getSortOrder());
        }
        if (locationDetails.getEnabled() != null) {
            location.setEnabled(locationDetails.getEnabled());
        }

        return itemLocationRepository.save(location);
    }

    public void deleteLocation(Long id) {
        if (!itemLocationRepository.existsById(id)) {
            throw new EntityNotFoundException("存放位置不存在");
        }
        itemLocationRepository.deleteById(id);
    }

    public ItemLocation getLocationById(Long id) {
        return itemLocationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("存放位置不存在"));
    }

    public Page<ItemLocation> listLocations(String keyword, Pageable pageable) {
        Specification<ItemLocation> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                predicates.add(cb.like(root.get("name"), "%" + keyword + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return itemLocationRepository.findAll(spec, pageable);
    }

    public List<ItemLocation> listAllLocations() {
        return itemLocationRepository.findByEnabledTrueOrderBySortOrderAsc();
    }
}
