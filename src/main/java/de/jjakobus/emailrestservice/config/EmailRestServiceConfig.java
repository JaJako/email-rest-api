package de.jjakobus.emailrestservice.config;

import de.jjakobus.emailrestservice.model.Email;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration of the Email REST service.
 *
 * @author jjakobus
 */
@Configuration
@EnableScheduling
@EnableAutoConfiguration
@EntityScan(basePackageClasses = Email.class)
public class EmailRestServiceConfig {

}
