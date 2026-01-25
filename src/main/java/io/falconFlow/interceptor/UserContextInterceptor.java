package io.falconFlow.interceptor;

import io.falconFlow.util.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to automatically set the current user context from request headers.
 * This enables automatic population of audit fields (createdBy, modifiedBy).
 * 
 * The interceptor looks for the user information in the following order:
 * 1. X-User-Id header (preferred for authenticated requests)
 * 2. X-Username header
 * 3. Falls back to "system" if no user information is found
 */
@Component
public class UserContextInterceptor implements HandlerInterceptor {
    
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USERNAME_HEADER = "X-Username";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Try to get user from headers
        String userId = request.getHeader(USER_ID_HEADER);
        String username = request.getHeader(USERNAME_HEADER);
        
        // Set the user context (prefer userId, fall back to username, then "system")
        if (userId != null && !userId.isEmpty()) {
            UserContextHolder.setCurrentUser(userId);
        } else if (username != null && !username.isEmpty()) {
            UserContextHolder.setCurrentUser(username);
        } else {
            // For now, use "system" as default
            // In production, you might want to extract this from JWT token or session
            UserContextHolder.setCurrentUser("system");
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        // Clear the user context to prevent memory leaks
        UserContextHolder.clear();
    }
}
