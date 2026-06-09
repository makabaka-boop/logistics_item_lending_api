package com.logistics.lending.service;

import com.logistics.lending.dto.LoginRequest;
import com.logistics.lending.dto.LoginResponse;
import com.logistics.lending.entity.User;
import com.logistics.lending.exception.BusinessException;
import com.logistics.lending.repository.UserRepository;
import com.logistics.lending.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));

        if (!user.getEnabled()) {
            throw new BusinessException(401, "账号已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getUsername(), user.getRealName(), user.getRole());
    }
}
