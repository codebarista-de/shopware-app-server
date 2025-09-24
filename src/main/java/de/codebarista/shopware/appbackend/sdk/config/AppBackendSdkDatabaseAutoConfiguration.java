package de.codebarista.shopware.appbackend.sdk.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Auto-configuration for Shopware App Backend SDK database setup.
 * Provides SQLite in-memory database defaults when no DataSource URL is configured.
 * Uses Spring Boot's standard DataSource configuration mechanism.
 */
@AutoConfiguration(before = DataSourceAutoConfiguration.class)
public class AppBackendSdkDatabaseAutoConfiguration {

    /**
     * Provides default DataSource properties for SQLite in-memory database.
     * This bean is only created when no DataSourceProperties bean exists.
     * Spring Boot's DataSourceAutoConfiguration will use these defaults to create the actual DataSource.
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(DataSourceProperties.class)
    public DataSourceProperties dataSourceProperties() {
        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl("jdbc:sqlite::memory:");
        properties.setDriverClassName("org.sqlite.JDBC");
        properties.setUsername("");
        properties.setPassword("");

        return properties;
    }
}