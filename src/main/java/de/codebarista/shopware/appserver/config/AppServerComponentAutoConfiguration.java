package de.codebarista.shopware.appserver.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Auto-configuration for Shopware App Server component scanning.
 * Enables JPA repositories, entity scanning, and component scanning for controllers.
 * <p>
 * This configuration ensures that all framework components (controllers, repositories)
 * are automatically discovered when the library is used as a dependency.
 */
@AutoConfiguration
@EnableJpaRepositories(basePackages = "de.codebarista.shopware.appserver.model")
@EntityScan(basePackages = "de.codebarista.shopware.appserver.model")
@ComponentScan(basePackages = "de.codebarista.shopware.appserver.controller")
public class AppServerComponentAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppServerComponentAutoConfiguration.class);

    public AppServerComponentAutoConfiguration() {
        LOGGER.debug("Configuring Shopware App Server component scanning");
    }
}
