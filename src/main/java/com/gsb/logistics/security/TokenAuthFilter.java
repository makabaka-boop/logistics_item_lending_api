package com.gsb.logistics.security;

import com.gsb.logistics.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
public class TokenAuthFilter extends OncePerRequestFilter {

    private final AppProperties props;

    public TokenAuthFilter(AppProperties props) {
        this.props = props;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        boolean authed = false;
        if (header != null) {
            if (header.startsWith("Bearer ")) {
                String token = header.substring(7).trim();
                if (token.equals(props.getAuth().getToken())) authed = true;
            } else if (header.startsWith("Basic ")) {
                try {
                    String decoded = new String(Base64.getDecoder().decode(header.substring(6).trim()),
                            StandardCharsets.UTF_8);
                    int idx = decoded.indexOf(':');
                    if (idx > 0) {
                        String u = decoded.substring(0, idx);
                        String p = decoded.substring(idx + 1);
                        if (u.equals(props.getAuth().getUsername()) && p.equals(props.getAuth().getPassword())) {
                            authed = true;
                        }
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        if (authed) {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    props.getAuth().getUsername(), null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(request, response);
    }
}
