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
 * Provides App Server core migrations that run before any user-defined migrations.
 */
@AutoConfiguration(before = LiquibaseAutoConfiguration.class)
@ConditionalOnClass(SpringLiquibase.class)
public class AppServerLiquibaseAutoConfiguration {

    /**
     * Creates Liquibase instance for App Server core migrations.
     * This runs automatically to ensure core App Server tables exist.
     * Uses a separate changelog and context to avoid conflicts with user migrations.
     */
    @Bean("appServerLiquibase")
    @Primary
    @ConditionalOnProperty(name = "app-server.liquibase.enabled", havingValue = "true", matchIfMissing = true)
    public SpringLiquibase appServerLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/app-server-changelog-master.xml");
        liquibase.setContexts("app-server-core");
        liquibase.setShouldRun(true);
        return liquibase;
    }
}