package dujiacun.common.constant;

public class UserThreadLocal {

    private static final ThreadLocal<ThreadContent> USER_TOKEN_THREAD_LOCAL = new ThreadLocal<>();

    private static class ThreadContent{
        private String userId;
        private String incrementId;
        public ThreadContent(String userId,String incrementId){
            this.userId = userId;
            this.incrementId = incrementId;
        }
    }
    public static String getUserId(){
        ThreadContent threadContent = USER_TOKEN_THREAD_LOCAL.get();
        return threadContent == null ? null : threadContent.userId;
    }

    public static String getIncrementId(){
        ThreadContent threadContent = USER_TOKEN_THREAD_LOCAL.get();
        return threadContent == null ? null : threadContent.incrementId;
    }

    public static void setThreadContent(String userId,String incrementId){
        USER_TOKEN_THREAD_LOCAL.set(new ThreadContent(userId,incrementId));
    }


    public static void removeUser(){
    if (USER_TOKEN_THREAD_LOCAL.get() != null) {
        USER_TOKEN_THREAD_LOCAL.remove();
        }
    }
}
