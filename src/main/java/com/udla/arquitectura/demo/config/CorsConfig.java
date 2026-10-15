package com.udla.arquitectura.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuracion global de CORS (Cross-Origin Resource Sharing).
 *
 * El navegador bloquea las peticiones de un frontend servido en un origen
 * (ej. http://localhost:5500) hacia otro origen (http://localhost:8080) a menos
 * que el servidor responda con el header Access-Control-Allow-Origin.
 * Aqui se declaran los origenes permitidos en desarrollo, leidos desde
 * application.properties para no "quemarlos" en el codigo.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] origenesPermitidos;

    public CorsConfig(@Value("${app.cors.allowed-origins}") String[] origenesPermitidos) {
        this.origenesPermitidos = origenesPermitidos;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origenesPermitidos)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
