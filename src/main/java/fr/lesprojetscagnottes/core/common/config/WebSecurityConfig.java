package fr.lesprojetscagnottes.core.common.config;

import fr.lesprojetscagnottes.core.common.security.CorsFilter;
import fr.lesprojetscagnottes.core.common.security.JwtAuthenticationEntryPoint;
import fr.lesprojetscagnottes.core.common.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.SessionManagementFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private UserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtRequestFilter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder);
    }

    @Bean
    CorsFilter corsFilter() {
        return new CorsFilter();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(corsFilter(), SessionManagementFilter.class)
                .csrf().disable()

                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS,"**").permitAll()
                .antMatchers(HttpMethod.GET,"/files/**").permitAll()
                .antMatchers(HttpMethod.POST,"/api/auth/login**").permitAll()
                .antMatchers(HttpMethod.GET,"/api/auth/login/slack**").permitAll()
                .antMatchers(HttpMethod.GET,"/api/health").permitAll()
                .antMatchers(HttpMethod.GET,"/actuator/health").permitAll()
                .antMatchers(HttpMethod.GET,"/actuator/**").permitAll()
                .antMatchers(HttpMethod.GET,"/api/docs**").permitAll()
                .antMatchers(HttpMethod.GET,"/api/docs/**").permitAll()
                .antMatchers(HttpMethod.GET,"/api/docs/swagger-ui**").permitAll()
                .antMatchers(HttpMethod.GET,"/api/docs/swagger-ui/**").permitAll()
                .anyRequest().authenticated()

                .and()
                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)

                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

}