package org.example.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Manages Actuator Security Configuration.
 */
@Configuration
@Order(1000)
public class ActuatorSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        logger.info("Configuring Actuator access.");

        // Actuator uses http basic auth
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/actuator/info", "/actuator/health", "/actuator/prometheus")
                .permitAll()
                .and()
                .antMatcher("/actuator/**")
                .authorizeRequests()
                .anyRequest()
                .hasRole("ADMIN")
                .and()
                .httpBasic();
    }

}
