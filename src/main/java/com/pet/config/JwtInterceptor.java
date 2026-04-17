package com.pet.config;

import com.pet.common.BusinessException;
import com.pet.common.Result;
import com.pet.utils.JwtUtil;
import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {
    
    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader(jwtConfig.getHeader());
        
        if (token == null || !token.startsWith(jwtConfig.getTokenPrefix())) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write(JSON.toJSONString(Result.error(401, "未登录或token无效")));
            return false;
        }
        
        token = token.substring(jwtConfig.getTokenPrefix().length()).trim();
        
        try {
            if (jwtUtil.verifyToken(token) == null) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(401);
                response.getWriter().write(JSON.toJSONString(Result.error(401, "token已过期或无效")));
                return false;
            }
            
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            Integer role = jwtUtil.getRoleFromToken(token);
            
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("role", role);
            
            return true;
        } catch (Exception e) {
            log.error("Token验证失败: ", e);
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write(JSON.toJSONString(Result.error(401, "token验证失败")));
            return false;
        }
    }
}
