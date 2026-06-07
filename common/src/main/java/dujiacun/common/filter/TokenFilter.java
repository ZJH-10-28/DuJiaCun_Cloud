package dujiacun.common.filter;


import dujiacun.common.constant.UserThreadLocal;
import dujiacun.common.constant.UserTokenConstant;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class TokenFilter implements Filter {

    @Autowired
    private RedisTemplate redisTemplate;

    private static final List<String> WHITE_LIST = Arrays.asList(
            "/users/login",
            "/users/register"
    );
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String uri = request.getRequestURI();

        if (WHITE_LIST.contains(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取请求头中的token
        String token = request.getHeader("Authorization");
        if (token == null) {
            response.setStatus(401);
            return;
        }

        UserTokenConstant userTokenConstant = (UserTokenConstant) redisTemplate.opsForValue()
                .get("USER_TOKEN:" + token);
        if (userTokenConstant == null) {
            response.setStatus(401);
            return;
        }

        try{
            UserThreadLocal.setUser(userTokenConstant);
            filterChain.doFilter(request, response);
        }
        finally {
            UserThreadLocal.removeUser();
        }
    }
}
