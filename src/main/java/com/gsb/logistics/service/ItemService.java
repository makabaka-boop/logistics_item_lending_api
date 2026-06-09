package com.gsb.logistics.service;

import com.gsb.logistics.config.AppProperties;
import com.gsb.logistics.domain.Item;
import com.gsb.logistics.repository.CategoryRepository;
import com.gsb.logistics.repository.ItemRepository;
import com.gsb.logistics.repository.LocationRepository;
import com.gsb.logistics.web.BusinessException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemService {
    private final ItemRepository itemRepo;
    private final CategoryRepository categoryRepo;
    private final LocationRepository locationRepo;
    private final AppProperties props;

    public ItemService(ItemRepository itemRepo, CategoryRepository categoryRepo,
                       LocationRepository locationRepo, AppProperties props) {
        this.itemRepo = itemRepo;
        this.categoryRepo = categoryRepo;
        this.locationRepo = locationRepo;
        this.props = props;
    }

    public List<Item> search(Long categoryId, Long locationId, String keyword, String owner, Boolean lowStock) {
        Specification<Item> spec = (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (categoryId != null) ps.add(cb.equal(root.get("categoryId"), categoryId));
            if (locationId != null) ps.add(cb.equal(root.get("locationId"), locationId));
            if (owner != null && !owner.isBlank()) ps.add(cb.equal(root.get("owner"), owner));
            if (keyword != null && !keyword.isBlank()) {
                ps.add(cb.or(
                        cb.like(root.get("name"), "%" + keyword + "%"),
                        cb.like(root.get("code"), "%" + keyword + "%")
                ));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        List<Item> list = itemRepo.findAll(spec);
        if (Boolean.TRUE.equals(lowStock)) {
            double ratio = props.getRules().getLowStockRatio();
            list.removeIf(i -> i.getTotalQuantity() == 0
                    || (double) i.getAvailableQuantity() / i.getTotalQuantity() > ratio);
        }
        return list;
    }

    public Item get(Long id) {
        return itemRepo.findById(id).orElseThrow(() -> new BusinessException(404, "物品不存在"));
    }

    public Item create(Item it) {
        if (it.getName() == null || it.getName().isBlank()) throw new BusinessException("name 不能为空");
        if (it.getCode() != null && !it.getCode().isBlank() && itemRepo.existsByCode(it.getCode())) {
            throw new BusinessException("物品编码已存在");
        }
        if (it.getCategoryId() != null && !categoryRepo.existsById(it.getCategoryId()))
            throw new BusinessException("categoryId 无效");
        if (it.getLocationId() != null && !locationRepo.existsById(it.getLocationId()))
            throw new BusinessException("locationId 无效");

        if (it.getTotalQuantity() == null || it.getTotalQuantity() < 0) it.setTotalQuantity(0);
        if (it.getAvailableQuantity() == null) it.setAvailableQuantity(it.getTotalQuantity());
        if (it.getAvailableQuantity() > it.getTotalQuantity())
            throw new BusinessException("availableQuantity 不能大于 totalQuantity");
        if (it.getRepairingQuantity() == null) it.setRepairingQuantity(0);
        it.setId(null);
        return itemRepo.save(it);
    }

    public Item update(Long id, Item p) {
        Item it = get(id);
        if (p.getName() != null) it.setName(p.getName());
        if (p.getCode() != null) it.setCode(p.getCode());
        if (p.getCategoryId() != null) it.setCategoryId(p.getCategoryId());
        if (p.getLocationId() != null) it.setLocationId(p.getLocationId());
        if (p.getOwner() != null) it.setOwner(p.getOwner());
        if (p.getRemark() != null) it.setRemark(p.getRemark());
        if (p.getTotalQuantity() != null) {
            int diff = p.getTotalQuantity() - it.getTotalQuantity();
            it.setTotalQuantity(p.getTotalQuantity());
            it.setAvailableQuantity(Math.max(0, it.getAvailableQuantity() + diff));
        }
        return itemRepo.save(it);
    }

    public void delete(Long id) { itemRepo.deleteById(id); }

    /** 减库存（借出时） */
    public void decreaseAvailable(Long itemId, int qty) {
        Item it = get(itemId);
        if (it.getAvailableQuantity() < qty) {
            throw new BusinessException(409, "库存不足，无法借出（当前可借: " + it.getAvailableQuantity() + "）");
        }
        it.setAvailableQuantity(it.getAvailableQuantity() - qty);
        itemRepo.save(it);
    }

    /** 加库存（归还时/取消时） */
    public void increaseAvailable(Long itemId, int qty) {
        Item it = get(itemId);
        it.setAvailableQuantity(Math.min(it.getTotalQuantity(), it.getAvailableQuantity() + qty));
        itemRepo.save(it);
    }

    /** 转入维修 */
    public void moveToRepair(Long itemId, int qty) {
        Item it = get(itemId);
        it.setRepairingQuantity(it.getRepairingQuantity() + qty);
        itemRepo.save(it);
    }

    /** 维修完成回库 */
    public void repairBack(Long itemId, int qty, boolean scrap) {
        Item it = get(itemId);
        int q = Math.min(qty, it.getRepairingQuantity());
        it.setRepairingQuantity(it.getRepairingQuantity() - q);
        if (scrap) {
            it.setTotalQuantity(Math.max(0, it.getTotalQuantity() - q));
        } else {
            it.setAvailableQuantity(Math.min(it.getTotalQuantity(), it.getAvailableQuantity() + q));
        }
        itemRepo.save(it);
    }

    /** 核减总库存（丢失场景） */
    public void decreaseTotal(Long itemId, int qty) {
        Item it = get(itemId);
        it.setTotalQuantity(Math.max(0, it.getTotalQuantity() - qty));
        // 同步保证 available 不超过 total
        if (it.getAvailableQuantity() > it.getTotalQuantity()) {
            it.setAvailableQuantity(it.getTotalQuantity());
        }
        itemRepo.save(it);
    }
}
