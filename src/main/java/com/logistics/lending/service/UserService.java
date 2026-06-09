package com.logistics.lending.service;

import com.logistics.lending.dto.UserCreateRequest;
import com.logistics.lending.entity.User;
import com.logistics.lending.exception.BusinessException;
import com.logistics.lending.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public User createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        user.setEnabled(true);
        return userRepository.save(user);
    }

    public User toggleEnabled(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setEnabled(!user.getEnabled());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
