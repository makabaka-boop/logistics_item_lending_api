package com.gsb.logistics.web;

import com.gsb.logistics.config.AppProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "1.认证", description = "登录获取 Token")
public class AuthController {

    private final AppProperties props;

    public AuthController(AppProperties props) {
        this.props = props;
    }

    @Data
    public static class LoginReq {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }

    @Operation(summary = "登录获取 Token", description = "成功后返回 Bearer Token，调用其他接口时需在请求头放置 Authorization: Bearer {token}")
    @SecurityRequirements // 该接口不需要鉴权
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginReq req) {
        if (!props.getAuth().getUsername().equals(req.getUsername())
                || !props.getAuth().getPassword().equals(req.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("token", props.getAuth().getToken());
        data.put("tokenType", "Bearer");
        data.put("username", props.getAuth().getUsername());
        return ApiResponse.ok(data);
    }

    @Operation(summary = "查询当前登录用户")
    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        Map<String, Object> data = new HashMap<>();
        data.put("username", props.getAuth().getUsername());
        return ApiResponse.ok(data);
    }
}
