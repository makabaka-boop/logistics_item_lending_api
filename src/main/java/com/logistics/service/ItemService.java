package com.logistics.service;

import com.logistics.dto.ItemQueryRequest;
import com.logistics.dto.ItemRequest;
import com.logistics.dto.ReplenishRequest;
import com.logistics.entity.Item;
import com.logistics.repository.ItemRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("物品不存在"));
    }

    public List<Item> query(ItemQueryRequest request) {
        Specification<Item> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), request.getCategoryId()));
            }
            if (request.getLocationId() != null) {
                predicates.add(cb.equal(root.get("locationId"), request.getLocationId()));
            }
            if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            if (request.getResponsiblePerson() != null && !request.getResponsiblePerson().isEmpty()) {
                predicates.add(cb.equal(root.get("responsiblePerson"), request.getResponsiblePerson()));
            }
            if (request.getName() != null && !request.getName().isEmpty()) {
                predicates.add(cb.like(root.get("name"), "%" + request.getName() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return itemRepository.findAll(spec);
    }

    @Transactional
    public Item create(ItemRequest request) {
        if (request.getTotalQty() == null || request.getTotalQty() < 0) {
            throw new RuntimeException("总数量不能为空且不能为负数");
        }
        int availableQty = request.getAvailableQty() != null ? request.getAvailableQty() : request.getTotalQty();
        if (availableQty < 0) {
            throw new RuntimeException("可用数量不能为负数");
        }
        if (availableQty > request.getTotalQty()) {
            throw new RuntimeException("可用数量不能大于总数量");
        }
        Item item = new Item();
        item.setName(request.getName());
        item.setCategoryId(request.getCategoryId());
        item.setLocationId(request.getLocationId());
        item.setTotalQty(request.getTotalQty());
        item.setAvailableQty(availableQty);
        item.setResponsiblePerson(request.getResponsiblePerson());
        item.setStatus(request.getStatus() != null ? request.getStatus() : "NORMAL");
        return itemRepository.save(item);
    }

    @Transactional
    public Item update(Long id, ItemRequest request) {
        Item item = findById(id);
        if (request.getName() != null) item.setName(request.getName());
        if (request.getCategoryId() != null) item.setCategoryId(request.getCategoryId());
        if (request.getLocationId() != null) item.setLocationId(request.getLocationId());
        if (request.getTotalQty() != null) {
            if (request.getTotalQty() < 0) {
                throw new RuntimeException("总数量不能为负数");
            }
            item.setTotalQty(request.getTotalQty());
        }
        if (request.getAvailableQty() != null) {
            if (request.getAvailableQty() < 0) {
                throw new RuntimeException("可用数量不能为负数");
            }
            item.setAvailableQty(request.getAvailableQty());
        }
        if (item.getAvailableQty() > item.getTotalQty()) {
            throw new RuntimeException("可用数量不能大于总数量");
        }
        if (request.getResponsiblePerson() != null) item.setResponsiblePerson(request.getResponsiblePerson());
        if (request.getStatus() != null) item.setStatus(request.getStatus());
        return itemRepository.save(item);
    }

    @Transactional
    public Item replenish(Long id, ReplenishRequest request) {
        if (request.getAddQty() == null || request.getAddQty() <= 0) {
            throw new RuntimeException("补充数量必须大于0");
        }
        Item item = findById(id);
        item.setTotalQty(item.getTotalQty() + request.getAddQty());
        item.setAvailableQty(item.getAvailableQty() + request.getAddQty());
        return itemRepository.save(item);
    }

    public List<Item> findLowStock(Integer threshold) {
        return itemRepository.findByAvailableQtyLessThanEqual(threshold != null ? threshold : 5);
    }
}
