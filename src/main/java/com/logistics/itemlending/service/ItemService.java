package com.logistics.itemlending.service;

import com.logistics.itemlending.entity.Item;
import com.logistics.itemlending.entity.ItemCategory;
import com.logistics.itemlending.entity.ItemLocation;
import com.logistics.itemlending.entity.User;
import com.logistics.itemlending.enums.ItemStatus;
import com.logistics.itemlending.exception.BusinessException;
import com.logistics.itemlending.repository.ItemCategoryRepository;
import com.logistics.itemlending.repository.ItemLocationRepository;
import com.logistics.itemlending.repository.ItemRepository;
import com.logistics.itemlending.repository.UserRepository;
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
import java.util.UUID;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemCategoryRepository itemCategoryRepository;

    @Autowired
    private ItemLocationRepository itemLocationRepository;

    @Autowired
    private UserRepository userRepository;

    public Item createItem(Item item) {
        if (StringUtils.hasText(item.getItemCode())) {
            if (itemRepository.existsByItemCode(item.getItemCode())) {
                throw new BusinessException("物品编码已存在");
            }
        } else {
            item.setItemCode(generateItemCode());
        }

        if (item.getCategory() != null && item.getCategory().getId() != null) {
            ItemCategory category = itemCategoryRepository.findById(item.getCategory().getId())
                    .orElseThrow(() -> new EntityNotFoundException("物品分类不存在"));
            item.setCategory(category);
        }

        if (item.getLocation() != null && item.getLocation().getId() != null) {
            ItemLocation location = itemLocationRepository.findById(item.getLocation().getId())
                    .orElseThrow(() -> new EntityNotFoundException("存放位置不存在"));
            item.setLocation(location);
        }

        if (item.getResponsiblePerson() != null && item.getResponsiblePerson().getId() != null) {
            User responsible = userRepository.findById(item.getResponsiblePerson().getId())
                    .orElseThrow(() -> new EntityNotFoundException("责任人不存在"));
            item.setResponsiblePerson(responsible);
        }

        if (item.getAvailableQuantity() == null) {
            item.setAvailableQuantity(item.getTotalQuantity());
        }

        if (item.getStatus() == null) {
            item.setStatus(ItemStatus.NORMAL);
        }

        return itemRepository.save(item);
    }

    public Item updateItem(Long id, Item itemDetails) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("物品不存在"));

        if (StringUtils.hasText(itemDetails.getItemCode()) &&
                !item.getItemCode().equals(itemDetails.getItemCode())) {
            if (itemRepository.existsByItemCode(itemDetails.getItemCode())) {
                throw new BusinessException("物品编码已存在");
            }
            item.setItemCode(itemDetails.getItemCode());
        }

        if (StringUtils.hasText(itemDetails.getName())) {
            item.setName(itemDetails.getName());
        }

        if (itemDetails.getCategory() != null && itemDetails.getCategory().getId() != null) {
            ItemCategory category = itemCategoryRepository.findById(itemDetails.getCategory().getId())
                    .orElseThrow(() -> new EntityNotFoundException("物品分类不存在"));
            item.setCategory(category);
        }

        if (itemDetails.getLocation() != null && itemDetails.getLocation().getId() != null) {
            ItemLocation location = itemLocationRepository.findById(itemDetails.getLocation().getId())
                    .orElseThrow(() -> new EntityNotFoundException("存放位置不存在"));
            item.setLocation(location);
        }

        if (itemDetails.getTotalQuantity() != null) {
            int diff = itemDetails.getTotalQuantity() - item.getTotalQuantity();
            item.setTotalQuantity(itemDetails.getTotalQuantity());
            item.setAvailableQuantity(item.getAvailableQuantity() + diff);
            if (item.getAvailableQuantity() < 0) {
                throw new BusinessException("总数量不能小于已借出数量");
            }
        }

        if (itemDetails.getWarningThreshold() != null) {
            item.setWarningThreshold(itemDetails.getWarningThreshold());
        }

        if (itemDetails.getResponsiblePerson() != null && itemDetails.getResponsiblePerson().getId() != null) {
            User responsible = userRepository.findById(itemDetails.getResponsiblePerson().getId())
                    .orElseThrow(() -> new EntityNotFoundException("责任人不存在"));
            item.setResponsiblePerson(responsible);
        }

        if (StringUtils.hasText(itemDetails.getSpecification())) {
            item.setSpecification(itemDetails.getSpecification());
        }
        if (StringUtils.hasText(itemDetails.getManufacturer())) {
            item.setManufacturer(itemDetails.getManufacturer());
        }
        if (itemDetails.getRemark() != null) {
            item.setRemark(itemDetails.getRemark());
        }
        if (itemDetails.getEnabled() != null) {
            item.setEnabled(itemDetails.getEnabled());
        }

        updateItemStatus(item);

        return itemRepository.save(item);
    }

    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("物品不存在"));
        if (item.getBorrowedQuantity() > 0 || item.getRepairingQuantity() > 0) {
            throw new BusinessException("物品还有未归还或维修中的，不能删除");
        }
        itemRepository.deleteById(id);
    }

    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("物品不存在"));
    }

    public Page<Item> listItems(String keyword, Long categoryId, Long locationId,
                                Long responsiblePersonId, ItemStatus status, Pageable pageable) {
        Specification<Item> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                Predicate codeLike = cb.like(root.get("itemCode"), "%" + keyword + "%");
                Predicate nameLike = cb.like(root.get("name"), "%" + keyword + "%");
                predicates.add(cb.or(codeLike, nameLike));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (locationId != null) {
                predicates.add(cb.equal(root.get("location").get("id"), locationId));
            }

            if (responsiblePersonId != null) {
                predicates.add(cb.equal(root.get("responsiblePerson").get("id"), responsiblePersonId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return itemRepository.findAll(spec, pageable);
    }

    public List<Item> listLowStockItems() {
        return itemRepository.findLowStockItems();
    }

    public Item addStock(Long id, Integer quantity, String remark) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("物品不存在"));

        if (quantity <= 0) {
            throw new BusinessException("补充数量必须大于0");
        }

        item.setTotalQuantity(item.getTotalQuantity() + quantity);
        item.setAvailableQuantity(item.getAvailableQuantity() + quantity);

        if (StringUtils.hasText(remark)) {
            String currentRemark = item.getRemark() != null ? item.getRemark() : "";
            item.setRemark(currentRemark + "\n[补充库存] " + quantity + " 件 - " + remark);
        }

        updateItemStatus(item);

        return itemRepository.save(item);
    }

    private void updateItemStatus(Item item) {
        if (item.getRepairingQuantity() > 0) {
            item.setStatus(ItemStatus.REPAIRING);
        } else if (item.getBorrowedQuantity().equals(item.getTotalQuantity())) {
            item.setStatus(ItemStatus.BORROWED);
        } else {
            item.setStatus(ItemStatus.NORMAL);
        }
    }

    private String generateItemCode() {
        return "ITEM-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
