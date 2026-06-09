package com.logistics.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.dto.ApiResponse;
import com.logistics.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            writeError(response, 401, "未登录，请先登录");
            return false;
        }
        String realToken = token.replace("Bearer ", "");
        if (!authService.isValidToken(realToken)) {
            writeError(response, 401, "登录已过期，请重新登录");
            return false;
        }
        request.setAttribute("currentUser", authService.getUserByToken(realToken));
        return true;
    }

    private void writeError(HttpServletResponse response, int code, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(code);
        ApiResponse<Void> result = ApiResponse.error(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
