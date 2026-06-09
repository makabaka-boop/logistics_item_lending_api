package com.logistics.itemlending.config;

import com.logistics.itemlending.entity.ItemCategory;
import com.logistics.itemlending.entity.ItemLocation;
import com.logistics.itemlending.entity.User;
import com.logistics.itemlending.enums.UserRole;
import com.logistics.itemlending.repository.ItemCategoryRepository;
import com.logistics.itemlending.repository.ItemLocationRepository;
import com.logistics.itemlending.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemCategoryRepository itemCategoryRepository;

    @Autowired
    private ItemLocationRepository itemLocationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initAdminUser();
        initDefaultCategories();
        initDefaultLocations();
    }

    private void initAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setRole(UserRole.ADMIN);
            admin.setPhone("13800138000");
            admin.setEmail("admin@example.com");
            admin.setEnabled(true);
            userRepository.save(admin);
            System.out.println("默认管理员账号已创建: admin / admin123");
        }
    }

    private void initDefaultCategories() {
        if (itemCategoryRepository.count() == 0) {
            String[] categories = {"办公设备", "电子设备", "工具器材", "劳保用品", "清洁用品"};
            for (int i = 0; i < categories.length; i++) {
                ItemCategory category = new ItemCategory();
                category.setName(categories[i]);
                category.setDescription(categories[i] + "分类");
                category.setSortOrder(i + 1);
                category.setEnabled(true);
                itemCategoryRepository.save(category);
            }
            System.out.println("默认物品分类已初始化");
        }
    }

    private void initDefaultLocations() {
        if (itemLocationRepository.count() == 0) {
            String[] locations = {"A仓库", "B仓库", "C仓库", "器材室", "办公室"};
            for (int i = 0; i < locations.length; i++) {
                ItemLocation location = new ItemLocation();
                location.setName(locations[i]);
                location.setDescription(locations[i] + "存放位置");
                location.setSortOrder(i + 1);
                location.setEnabled(true);
                itemLocationRepository.save(location);
            }
            System.out.println("默认存放位置已初始化");
        }
    }
}
