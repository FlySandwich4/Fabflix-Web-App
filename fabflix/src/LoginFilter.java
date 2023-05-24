import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*",initParams = {
        @WebInitParam(name = "loadOnStartup", value = "1")
})
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());

        Pattern pattern = Pattern.compile("dashboard/.");
        System.out.println("dashboard?:" + httpRequest.getRequestURI().matches(".dashboard/."));

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())
                || httpRequest.getRequestURI().matches(".dashboard/.")
        ) {
            System.out.println("Login: URL in allows");
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        // Redirect to login page if the "user" attribute doesn't exist in session
        if (httpRequest.getSession().getAttribute("user") == null) {
            System.out.println("Login: URL in Re");
            httpResponse.sendRedirect("login.html");
        } else {
            System.out.println("Login: URL in Continue");
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("/");
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("login.css");
        allowedURIs.add("api/login");
        allowedURIs.add("api/android-login");
        allowedURIs.add("api/employee-login");
        //allowedURIs.add("/dashboard/*");

        allowedURIs.add("dashboard/employee-login.html");
        allowedURIs.add("dashboard/employee-login.css");
        allowedURIs.add("dashboard/employee-login.js");

        allowedURIs.add("dashboard/dashboard.html");
        allowedURIs.add("dashboard/dashboard.js");
        //allowedURIs.add("DashBoard/employee-login.html");

        allowedURIs.add("dashboard/api/addstar");


        allowedURIs.add("_dashboard");
    }

    public void destroy() {
        // ignored.
    }

}
