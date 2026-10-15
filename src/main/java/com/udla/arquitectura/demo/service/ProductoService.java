package com.udla.arquitectura.demo.service;

import com.udla.arquitectura.demo.config.CacheConfig;
import com.udla.arquitectura.demo.model.Producto;
import com.udla.arquitectura.demo.repository.ProductoRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Logica de negocio del catalogo.
 *
 * CACHE: los resultados se guardan en memoria con @Cacheable. La primera llamada
 * tarda ~1.5 s (latencia simulada); las siguientes se responden desde la cache
 * sin tocar el repositorio.
 *
 * El service depende solo de la abstraccion ProductoRepository; Spring le inyecta
 * el ProductoRepositoryProxy (@Primary) sin que esta clase lo sepa.
 */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Cacheable(CacheConfig.CACHE_PRODUCTOS)
    public List<Producto> listarTodos() {
        return productoRepository.listarTodos();
    }

    @Cacheable(value = CacheConfig.CACHE_PRODUCTO, key = "#id")
    public Producto buscarPorId(Long id) {
        return productoRepository.buscarPorId(id);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_PRODUCTOS, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_PRODUCTO, allEntries = true)
    })
    public void limpiarCache() {
        // El cuerpo esta vacio a proposito: el trabajo lo hace @CacheEvict.
    }
}
