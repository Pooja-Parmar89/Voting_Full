package com.votingapp.voting.config;

import com.votingapp.voting.security.AuthEntryPoint;
import com.votingapp.voting.security.CustomUserDetailsService;
import com.votingapp.voting.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * ------------------------------------------------------------------------------------------
 * WHY THIS FILE WAS REWRITTEN (the "login issue"):
 *
 * The original config called .formLogin(form -> form.defaultSuccessUrl("/dashboard", true))
 * WITHOUT ever calling .loginPage(...). When Spring Security's formLogin() is configured
 * without an explicit loginPage(), it silently registers its OWN auto-generated HTML login
 * page (DefaultLoginPageGeneratingFilter) for GET /login - and that filter runs BEFORE the
 * request ever reaches a @Controller. So the app's own LoginController + templates/login.html
 * were unreachable; you always got Spring's plain generated login box instead.
 * If you then tried to fix that by adding .loginPage("/login"), the request would reach
 * LoginController, which returned the view name "login" - but the project had no template
 * engine (no Thymeleaf/JSP dependency in pom.xml) and no ViewResolver configured for it. Spring
 * Boot's default resolver forwards an unresolved view name back to the same URL, so GET /login
 * forwarded to itself forever -> "Circular view path [login]" error.
 *
 * FIX: this project no longer uses server-rendered form login at all. It is a stateless REST
 * API secured with JWT (as the original spec asked for), consumed by the separate Angular app.
 * There is no "/login" page on the backend anymore - the Angular app posts credentials to
 * POST /api/auth/login and receives a JWT back.
 * ------------------------------------------------------------------------------------------
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final AuthEntryPoint authEntryPoint;
    private final JwtAuthFilter jwtAuthFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        // Only these exact GET shapes are public - deliberately NOT a "/**" wildcard,
                        // so that /api/elections/{id}/votes and /voting-status (which need the
                        // authenticated user) fall through to anyRequest().authenticated() below.
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/elections").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/elections/*").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/elections/*/candidates").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/elections/*/results").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
