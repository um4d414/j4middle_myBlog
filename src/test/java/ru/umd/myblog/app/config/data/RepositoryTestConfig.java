package ru.umd.myblog.app.config.data;

import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.umd.myblog.app.data.repository.JdbcNativeCommentRepository;
import ru.umd.myblog.app.data.repository.JdbcNativePostRepository;

@Configuration
@Import(DataSourceConfig.class)
public class RepositoryTestConfig {
    @Bean
    public JdbcNativePostRepository jdbcNativePostRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcNativePostRepository(jdbcTemplate);
    }

    @Bean
    public JdbcNativeCommentRepository jdbcNativeCommentRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcNativeCommentRepository(jdbcTemplate);
    }
}
