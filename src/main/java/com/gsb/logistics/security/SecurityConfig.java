package com.gsb.logistics.security;

import com.gsb.logistics.config.AppProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    private final TokenAuthFilter tokenAuthFilter;
    private final AppProperties props;

    public SecurityConfig(TokenAuthFilter tokenAuthFilter, AppProperties props) {
        this.tokenAuthFilter = tokenAuthFilter;
        this.props = props;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(c -> c.disable())
            .cors(c -> c.disable())
            .headers(h -> h.frameOptions(f -> f.disable()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/login"),
                    AntPathRequestMatcher.antMatcher("/h2-console/**"),
                    AntPathRequestMatcher.antMatcher("/error"),
                    AntPathRequestMatcher.antMatcher("/actuator/**")
                ).permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(b -> {})
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, resp, ex) -> {
                    // 浏览器访问 Swagger 等文档时，返回 Basic 401 触发浏览器登录弹窗
                    String accept = req.getHeader("Accept");
                    if (accept != null && accept.contains("text/html")) {
                        resp.setHeader("WWW-Authenticate", "Basic realm=\"logistics-api\"");
                    }
                    resp.setStatus(401);
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.getWriter().write("{\"code\":401,\"message\":\"未登录或 Token 无效\"}");
                })
            )
            .addFilterBefore(tokenAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
