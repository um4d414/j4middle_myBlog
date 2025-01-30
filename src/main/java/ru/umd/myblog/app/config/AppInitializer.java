package ru.umd.myblog.app.config;

import jakarta.servlet.*;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class AppInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(AppConfig.class);

        DispatcherServlet dispatcherServlet = new DispatcherServlet(context);
        ServletRegistration.Dynamic registration = servletContext.addServlet("dispatcher", dispatcherServlet);

        MultipartConfigElement multipartConfig = new MultipartConfigElement(
            "",
            5 * 1024 * 1024L,
            10 * 1024 * 1024L,
            5 * 1024 * 1024
        );
        registration.setMultipartConfig(multipartConfig);

        registration.setLoadOnStartup(1);
        registration.addMapping("/");
    }
}
