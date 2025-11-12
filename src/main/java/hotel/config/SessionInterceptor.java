package hotel.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class SessionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        // Skip login and public pages
        String uri = request.getRequestURI();
        if (uri.startsWith("/hotel/login") ||
                uri.startsWith("/hotel/register") ||
                uri.startsWith("/hotel/forgot-password") ||
                uri.startsWith("/assets") ||
                uri.startsWith("/images")) {
            return true; // let public URLs pass
        }

        // Check if user is logged in
//        if (session == null || session.getAttribute("user") == null) {
//            response.sendRedirect("/hotel/login");
//            return false; // stop request
//        }

        // Optionally check user role:
        // User user = (User) session.getAttribute("user");
        // if (!user.getRole().equals("ADMIN")) { ... }

        return true;
    }
}
