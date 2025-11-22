package de.codebarista.shopware.appserver.config;

import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * Auto-configuration for Shopware App Server Liquibase setup.
 * Provides App Server core migrations that run if user-defined migrations are not
 */
@AutoConfiguration(before = LiquibaseAutoConfiguration.class)
@ConditionalOnClass(SpringLiquibase.class)
public class AppServerLiquibaseAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppServerLiquibaseAutoConfiguration.class);


    /**
     * Creates Liquibase instance for App Server core migrations.
     * This runs automatically to ensure core App Server tables exist.
     * Uses a separate changelog and context to avoid conflicts with user migrations.
     */
    @Bean("appServerLiquibase")
    @ConditionalOnProperty(name = "app-server.database.user-migrations", havingValue = "false", matchIfMissing = true)
    public SpringLiquibase appServerLiquibase(DataSource dataSource) {
        LOGGER.info("User migrations for database are not active, executing only the App Server migrations. " +
                "To execute a user defined Liquibase changelog file, set app-server.database.user-migrations to true " +
                "and include the required App Server Liquibase changelog master file into your changelog.");
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/app-server-changelog-master.xml");
        liquibase.setContexts("app-server-core");
        liquibase.setShouldRun(true);
        return liquibase;
    }
}
