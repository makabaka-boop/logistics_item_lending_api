package com.logistics.lending.service;

import com.logistics.lending.dto.ItemRequest;
import com.logistics.lending.entity.Category;
import com.logistics.lending.entity.Item;
import com.logistics.lending.entity.StorageLocation;
import com.logistics.lending.exception.BusinessException;
import com.logistics.lending.repository.CategoryRepository;
import com.logistics.lending.repository.ItemRepository;
import com.logistics.lending.repository.StorageLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final StorageLocationRepository locationRepository;

    public List<Item> list(Long categoryId, Long locationId) {
        if (categoryId != null) {
            return itemRepository.findByCategoryId(categoryId);
        }
        if (locationId != null) {
            return itemRepository.findByLocationId(locationId);
        }
        return itemRepository.findAll();
    }

    public Item getById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException("物品不存在"));
    }

    @Transactional
    public Item create(ItemRequest request) {
        if (itemRepository.existsByItemCode(request.getItemCode())) {
            throw new BusinessException("物品编码已存在");
        }
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException("分类不存在"));
        StorageLocation location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new BusinessException("存放位置不存在"));

        Item item = new Item();
        item.setItemCode(request.getItemCode());
        item.setName(request.getName());
        item.setCategory(category);
        item.setLocation(location);
        item.setTotalQuantity(request.getTotalQuantity());
        item.setAvailableQuantity(request.getTotalQuantity());
        item.setLentQuantity(0);
        item.setMaintenanceQuantity(0);
        item.setResponsiblePerson(request.getResponsiblePerson());
        item.setRemark(request.getRemark());
        item.setWarningThreshold(request.getWarningThreshold() != null ? request.getWarningThreshold() : 5);
        return itemRepository.save(item);
    }

    @Transactional
    public Item update(Long id, ItemRequest request) {
        Item item = getById(id);
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException("分类不存在"));
        StorageLocation location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new BusinessException("存放位置不存在"));

        if (!item.getItemCode().equals(request.getItemCode()) && itemRepository.existsByItemCode(request.getItemCode())) {
            throw new BusinessException("物品编码已存在");
        }

        int qtyDiff = request.getTotalQuantity() - item.getTotalQuantity();
        item.setItemCode(request.getItemCode());
        item.setName(request.getName());
        item.setCategory(category);
        item.setLocation(location);
        item.setTotalQuantity(request.getTotalQuantity());
        item.setAvailableQuantity(Math.max(0, item.getAvailableQuantity() + qtyDiff));
        item.setResponsiblePerson(request.getResponsiblePerson());
        item.setRemark(request.getRemark());
        item.setWarningThreshold(request.getWarningThreshold() != null ? request.getWarningThreshold() : item.getWarningThreshold());
        return itemRepository.save(item);
    }

    public void delete(Long id) {
        itemRepository.deleteById(id);
    }
}
