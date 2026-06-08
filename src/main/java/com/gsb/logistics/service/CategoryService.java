package com.gsb.logistics.service;

import com.gsb.logistics.domain.Category;
import com.gsb.logistics.repository.CategoryRepository;
import com.gsb.logistics.web.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository repo;

    public CategoryService(CategoryRepository repo) { this.repo = repo; }

    public List<Category> list() { return repo.findAll(); }

    public Category create(Category c) {
        if (c.getName() == null || c.getName().isBlank()) throw new BusinessException("name 不能为空");
        if (repo.existsByName(c.getName())) throw new BusinessException("分类名已存在");
        c.setId(null);
        return repo.save(c);
    }

    public Category update(Long id, Category c) {
        Category exist = repo.findById(id).orElseThrow(() -> new BusinessException(404, "分类不存在"));
        if (c.getName() != null) exist.setName(c.getName());
        if (c.getDescription() != null) exist.setDescription(c.getDescription());
        return repo.save(exist);
    }

    public void delete(Long id) { repo.deleteById(id); }
}
