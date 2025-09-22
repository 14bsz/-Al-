package com.aichat.config;

import com.aichat.security.JwtAuthenticationEntryPoint;
import com.aichat.security.JwtAuthenticationFilter;
import com.aichat.security.RateLimitingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security配置
 * 配置JWT认证、CORS、安全头等
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Value("${cors.allowed-origins:*}")
    private String[] allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String[] allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${cors.max-age:3600}")
    private long maxAge;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        
        http.cors().configurationSource(corsConfigurationSource());
        
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        
        http.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint);
        
        http.headers()
            .frameOptions().deny()
            .contentTypeOptions();
        
        http.authorizeRequests()
            // 公开端点
            .antMatchers("/api/**").permitAll()
            .antMatchers("/api/v2/auth/**").permitAll()
            .antMatchers("/api/v2/public/**").permitAll()
            .antMatchers("/actuator/health").permitAll()
            .antMatchers("/actuator/info").permitAll()
            .antMatchers("/api-docs/**").permitAll()
            .antMatchers("/swagger-ui/**").permitAll()
            .antMatchers("/swagger-ui.html").permitAll()
            .antMatchers("/h2-console/**").permitAll()
            .antMatchers("/ws/**").permitAll()
            
            // 管理员端点
            .antMatchers("/api/v2/admin/**").hasRole("ADMIN")
            
            // 监控端点
            .antMatchers("/actuator/**").hasRole("ADMIN")
            
            // 其他请求需要认证
            .anyRequest().authenticated();

        // 添加JWT过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        // 添加限流过滤器
        http.addFilterBefore(rateLimitingFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}