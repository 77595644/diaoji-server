package com.diaoji.config;

import com.diaoji.util.JwtUtil;
import com.diaoji.util.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * JWT 认证过滤器
 *
 * 逻辑：
 * - 有有效 JWT token → 直接放行
 * - 无 token + 公开路径（/api/auth/*, /api/spot/nearby 等） → 直接放行
 * - 无 token + 其他写操作 → 返回 401
 */
@Component
@Order(1)
public class JwtAuthFilter implements Filter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 完全公开路径（所有 HTTP 方法均可访问） */
    private static final String[] PUBLIC_ALL_METHODS_PREFIXES = {
        "/api/auth/",         // 所有认证接口（login/register/demo-login 等）
    };

    /** 公开读路径前缀（仅 GET 请求时无需 token） */
    private static final String[] PUBLIC_READ_PREFIXES = {
        "/api/spot/nearby",
        "/api/spot/search",
        "/api/feed",
        "/api/fish-index",
        "/api/user/captcha",
        "/api/home/",
        // 他人公开资料（只读）
        "/api/user/",
        "/api/catch/user/",
    };

    /** 钓点详情：GET /api/spot/{数字} 无需 token */
    private static final Pattern SPOT_DETAIL_PATTERN = Pattern.compile("^/api/spot/\\d+$");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String method = req.getMethod();

        try {
            // 1. 尝试从 Authorization header 解析 JWT
            Long userId = extractUserId(req);
            if (userId != null) {
                // 有有效 token → 直接放行
                UserContext.setUserId(userId);
                chain.doFilter(request, response);
                return;
            }

            // 2. 无 token：检查公开路径
            if (isPublicPath(uri, method)) {
                chain.doFilter(request, response);
                return;
            }

            // 3. 其他情况 → 返回 401
            resp.setStatus(401);
            resp.setContentType("application/json;charset=UTF-8");
            Map<String, Object> result = new HashMap<>();
            result.put("code", 401);
            result.put("message", "请先登录");
            resp.getWriter().write(MAPPER.writeValueAsString(result));
        } finally {
            UserContext.clear();
        }
    }

    private Long extractUserId(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                return JwtUtil.getUserId(token);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 无 token 时，判断该请求是否公开（无需登录即可访问）
     */
    private boolean isPublicPath(String uri, String method) {
        // 完全公开路径前缀（所有方法）
        for (String prefix : PUBLIC_ALL_METHODS_PREFIXES) {
            if (uri.startsWith(prefix)) return true;
        }

        // 公开读路径前缀（仅 GET 请求）
        if ("GET".equalsIgnoreCase(method)) {
            for (String prefix : PUBLIC_READ_PREFIXES) {
                if (uri.startsWith(prefix)) return true;
            }
            // GET /api/spot/{数字}（钓点详情）
            if (SPOT_DETAIL_PATTERN.matcher(uri).matches()) return true;
            // GET /api/spot
            if (uri.equals("/api/spot")) return true;
        }

        return false;
    }
}
