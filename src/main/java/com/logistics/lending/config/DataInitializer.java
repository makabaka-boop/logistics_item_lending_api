package com.logistics.lending.config;

import com.logistics.lending.entity.Category;
import com.logistics.lending.entity.StorageLocation;
import com.logistics.lending.entity.User;
import com.logistics.lending.repository.CategoryRepository;
import com.logistics.lending.repository.StorageLocationRepository;
import com.logistics.lending.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StorageLocationRepository locationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initAdmin();
        initCategories();
        initLocations();
    }

    private void initAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            userRepository.save(admin);
            System.out.println("============================================");
            System.out.println("默认管理员账号已创建:");
            System.out.println("用户名: admin");
            System.out.println("密码: admin123");
            System.out.println("============================================");
        }
    }

    private void initCategories() {
        if (categoryRepository.count() == 0) {
            String[] categories = {"办公用品", "电子设备", "工具器械", "家具家电", "清洁用品", "其他"};
            for (String name : categories) {
                Category c = new Category();
                c.setName(name);
                categoryRepository.save(c);
            }
            System.out.println("默认物品分类已初始化");
        }
    }

    private void initLocations() {
        if (locationRepository.count() == 0) {
            String[] locations = {"一楼仓库", "二楼仓库", "行政办公室", "技术部", "会议室A", "会议室B"};
            for (String name : locations) {
                StorageLocation l = new StorageLocation();
                l.setName(name);
                locationRepository.save(l);
            }
            System.out.println("默认存放位置已初始化");
        }
    }
}
