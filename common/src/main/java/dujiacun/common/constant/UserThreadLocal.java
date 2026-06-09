package dujiacun.common.constant;

public class UserThreadLocal {

    private static final ThreadLocal<String> USER_TOKEN_THREAD_LOCAL = new ThreadLocal<>();

    public static String getUser(){
        return USER_TOKEN_THREAD_LOCAL.get();
    }

    public static void setUser(String userId){
        USER_TOKEN_THREAD_LOCAL.set(userId);
    }

    public static void removeUser(){
    if (USER_TOKEN_THREAD_LOCAL.get() != null) {
        USER_TOKEN_THREAD_LOCAL.remove();
        }
    }
}
