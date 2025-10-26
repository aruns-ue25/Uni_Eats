package org.example.config;

// Security Configuration - All Team Members
// Manages authentication and authorization for all roles
// Member: Heshan (User authentication and customer security)
// Member: Arun (Shop security and access control)
// Member: Piranavan (Order security)
// Member: Asmal (Admin security)

import org.example.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return username -> userService.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + username));
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService,
        org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {
        http
            .authenticationProvider(daoAuthenticationProvider)
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/", "/login", "/perform_login", "/register", "/register/shop", "/register/customer",
                        "/css/**", "/js/**", "/images/**", "/favicon.ico",
                        "/error", "/.well-known/**", "/webjars/**", "/debug/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()

                // Admin endpoints (now public per request)
                .requestMatchers("/admin/**").permitAll()

                // Shop endpoints
                .requestMatchers("/shop/**").hasRole("SHOP")

                // Customer endpoints
                .requestMatchers("/customer/**").hasRole("CUSTOMER")

                // API endpoints (allow admin API public per UI requirement)
                .requestMatchers("/api/admin/**").permitAll()
                .requestMatchers("/api/shop/**").hasRole("SHOP")
                .requestMatchers("/api/customer/**").hasRole("CUSTOMER")
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()

                // All other requests
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/perform_login")
                .successHandler((request, response, authentication) ->
                        response.sendRedirect("/dashboard"))
                .failureHandler((request, response, exception) -> {
                        String attempted = request.getParameter("username");
                        log.warn("Login failed for {}: {}", attempted, exception.getMessage());
                        response.sendRedirect("/login?error=true");
                    })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .csrf(csrf -> csrf.disable()) // disable CSRF for APIs
            .headers(headers -> headers.frameOptions(frame -> frame.disable())); // allow H2 console frames

        return http.build();
    }
}
