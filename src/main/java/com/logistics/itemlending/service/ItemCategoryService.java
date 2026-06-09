package com.logistics.itemlending.service;

import com.logistics.itemlending.entity.ItemCategory;
import com.logistics.itemlending.exception.BusinessException;
import com.logistics.itemlending.repository.ItemCategoryRepository;
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
public class ItemCategoryService {

    @Autowired
    private ItemCategoryRepository itemCategoryRepository;

    public ItemCategory createCategory(ItemCategory category) {
        if (itemCategoryRepository.existsByName(category.getName())) {
            throw new BusinessException("分类名称已存在");
        }
        return itemCategoryRepository.save(category);
    }

    public ItemCategory updateCategory(Long id, ItemCategory categoryDetails) {
        ItemCategory category = itemCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("物品分类不存在"));

        if (StringUtils.hasText(categoryDetails.getName())) {
            if (!category.getName().equals(categoryDetails.getName()) &&
                    itemCategoryRepository.existsByName(categoryDetails.getName())) {
                throw new BusinessException("分类名称已存在");
            }
            category.setName(categoryDetails.getName());
        }
        if (categoryDetails.getDescription() != null) {
            category.setDescription(categoryDetails.getDescription());
        }
        if (categoryDetails.getSortOrder() != null) {
            category.setSortOrder(categoryDetails.getSortOrder());
        }
        if (categoryDetails.getEnabled() != null) {
            category.setEnabled(categoryDetails.getEnabled());
        }

        return itemCategoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        if (!itemCategoryRepository.existsById(id)) {
            throw new EntityNotFoundException("物品分类不存在");
        }
        itemCategoryRepository.deleteById(id);
    }

    public ItemCategory getCategoryById(Long id) {
        return itemCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("物品分类不存在"));
    }

    public Page<ItemCategory> listCategories(String keyword, Pageable pageable) {
        Specification<ItemCategory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                predicates.add(cb.like(root.get("name"), "%" + keyword + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return itemCategoryRepository.findAll(spec, pageable);
    }

    public List<ItemCategory> listAllCategories() {
        return itemCategoryRepository.findByEnabledTrueOrderBySortOrderAsc();
    }
}
