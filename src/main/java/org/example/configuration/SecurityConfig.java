package org.example.configuration;

import org.example.manager.user.CustomUserDetailsService;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.context.request.RequestContextListener;

@Configuration
@EnableWebSecurity
@Order(1001)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        logger.info("configuring..................");
        setupLocalUserAuthentication(auth);
    }


    private void setupLocalUserAuthentication(final AuthenticationManagerBuilder auth) throws Exception {
        logger.info("Configuring with locally authenticated user access");

        // Fall through to use local user management.
        auth
                // Define our custom user details service.
                .userDetailsService(new CustomUserDetailsService(userRepository));
         //      .passwordEncoder(getPasswordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        logger.info("Configuring with authenticated user access.");

        http
                .authorizeRequests()
                // Paths to static resources are available to anyone
                .antMatchers("/register/**", "/login/**", "/vendors/**", "/css/**", "/js/**", "/img/**")
                .permitAll()
                // All other requests must be authenticated
                .anyRequest()
                .authenticated()
                .and()
                // Define how you login
                .formLogin();
        http.headers().frameOptions().sameOrigin();
        http.csrf().ignoringAntMatchers("/h2/**");
    }

    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }
}
