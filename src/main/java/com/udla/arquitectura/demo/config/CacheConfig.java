package com.udla.arquitectura.demo.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Activa la abstraccion de cache de Spring.
 *
 * Con @EnableCaching, Spring envuelve los beans que usan @Cacheable en un
 * proxy dinamico (AOP). Ese proxy intercepta la llamada: si el resultado ya
 * esta en cache lo devuelve, si no, delega al metodo real y guarda el
 * resultado. Por defecto usa ConcurrentMapCacheManager (cache en memoria).
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_PRODUCTOS = "productos";
    public static final String CACHE_PRODUCTO = "producto";
}
