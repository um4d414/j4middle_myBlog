package ru.umd.myblog.app.config.data;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("ru.umd.myblog.app.data.repository")
public class RepositoryConfig {
}
