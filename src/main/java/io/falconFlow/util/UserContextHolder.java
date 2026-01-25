package io.falconFlow.util;

/**
 * Thread-local storage for the current user context.
 * This allows us to track which user is performing operations
 * for audit purposes (createdBy, modifiedBy fields).
 */
public class UserContextHolder {
    
    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();
    
    /**
     * Set the current user for the current thread.
     * This should be called at the beginning of each request.
     * 
     * @param username The username of the current user
     */
    public static void setCurrentUser(String username) {
        currentUser.set(username);
    }
    
    /**
     * Get the current user for the current thread.
     * 
     * @return The username of the current user, or "system" if not set
     */
    public static String getCurrentUser() {
        String user = currentUser.get();
        return user != null ? user : "system";
    }
    
    /**
     * Clear the current user from the current thread.
     * This should be called at the end of each request to prevent memory leaks.
     */
    public static void clear() {
        currentUser.remove();
    }
}
