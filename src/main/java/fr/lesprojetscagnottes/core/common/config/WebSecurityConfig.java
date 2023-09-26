package fr.lesprojetscagnottes.core.common.config;

import fr.lesprojetscagnottes.core.common.security.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.SessionManagementFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private UserDetailsService jwtUserDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired

    private CustomAuthenticationManager authenticationManager;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder);
    }

    @Bean
    CorsFilter corsFilter() {
        return new CorsFilter();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(corsFilter(), SessionManagementFilter.class)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/",
                                "/files/**",
                                "/api/auth/login",
                                "/api/auth/login/slack**",
                                "/api/auth/login/microsoft**",
                                "/actuator/health",
                                "/api/docs/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/auth/login").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilter(new JwtAuthenticationFilter(authenticationManager))
                .addFilter(new JwtAuthorizationFilter(authenticationManager))
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();

    }

}
