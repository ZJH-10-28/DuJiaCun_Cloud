package dujiacun.common.constant;

public class UserThreadLocal {

    private static final ThreadLocal<UserTokenConstant> USER_TOKEN_THREAD_LOCAL = new ThreadLocal<>();

    public static UserTokenConstant getUser(){
        return USER_TOKEN_THREAD_LOCAL.get();
    }

    public static void setUser(UserTokenConstant userTokenConstant){
        USER_TOKEN_THREAD_LOCAL.set(userTokenConstant);
    }

    public static void removeUser(){
    if (USER_TOKEN_THREAD_LOCAL.get() != null) {
        USER_TOKEN_THREAD_LOCAL.remove();
        }
    }
}
