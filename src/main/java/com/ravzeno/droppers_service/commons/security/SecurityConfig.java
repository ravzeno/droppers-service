package com.ravzeno.droppers_service.commons.security;

import com.ravzeno.droppers_service.commons.filter.BackOfficeTokenFilter;
import com.ravzeno.droppers_service.commons.filter.DeviceTokenFilter;
import com.ravzeno.droppers_service.commons.filter.RequestSecurityFilter;
import com.ravzeno.droppers_service.commons.security.config.RequestSecurityProperties;
import com.ravzeno.droppers_service.commons.security.enum_type.Role;
import com.ravzeno.droppers_service.domains.device.DeviceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http, RequestSecurityProperties properties, DeviceService devices)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        requests ->
                                requests.requestMatchers("/public/api/**")
                                        .permitAll()
                                        .requestMatchers("/device/api/**")
                                        .hasAuthority(Role.DEVICE.getAuthority())
                                        .requestMatchers("/back-office/api/**")
                                        .hasAuthority(Role.ADMIN.getAuthority())
                                        .anyRequest()
                                        .denyAll())
                .addFilterBefore(
                        new RequestSecurityFilter(properties),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(
                        new DeviceTokenFilter(devices), AnonymousAuthenticationFilter.class)
                .addFilterBefore(
                        new BackOfficeTokenFilter(properties), AnonymousAuthenticationFilter.class);
        return http.build();
    }
}
