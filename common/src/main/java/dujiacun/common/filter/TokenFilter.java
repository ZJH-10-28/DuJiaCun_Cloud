package dujiacun.common.filter;


import dujiacun.common.constant.UserThreadLocal;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TokenFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try{
            // 获取请求头中的token
            String userId = request.getHeader("userId");
            UserThreadLocal.setUser(userId);
            filterChain.doFilter(request, response);
        }
        finally {
            UserThreadLocal.removeUser();
        }
    }
}
