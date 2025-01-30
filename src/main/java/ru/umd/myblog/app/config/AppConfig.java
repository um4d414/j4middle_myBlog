package ru.umd.myblog.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ru.umd.myblog.app.config.data.DataSourceConfig;
import ru.umd.myblog.app.config.data.RepositoryConfig;
import ru.umd.myblog.app.config.service.ServiceConfig;
import ru.umd.myblog.app.config.web.*;

@Configuration
@Import({
    ServiceConfig.class,
    WebConfig.class,
    DataSourceConfig.class,
    RepositoryConfig.class,
})
public class AppConfig {
}
