package de.codebarista.shopware.appserver.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Auto-configuration for Shopware App Server Liquibase setup.
 * Provides SDK core migrations that run before any user-defined migrations.
 */
@AutoConfiguration(before = LiquibaseAutoConfiguration.class)
@ConditionalOnClass(SpringLiquibase.class)
public class AppServerLiquibaseAutoConfiguration {

    /**
     * Creates Liquibase instance for SDK core migrations.
     * This runs automatically to ensure core SDK tables exist.
     * Uses a separate changelog and context to avoid conflicts with user migrations.
     */
    @Bean("sdkLiquibase")
    @Primary
    @ConditionalOnProperty(name = "app-backend.sdk.liquibase.enabled", havingValue = "true", matchIfMissing = true)
    public SpringLiquibase sdkLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/sdk-changelog-master.xml");
        liquibase.setContexts("sdk-core");
        liquibase.setShouldRun(true);
        return liquibase;
    }
}