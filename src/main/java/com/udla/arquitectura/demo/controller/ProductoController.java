package com.udla.arquitectura.demo.controller;

import com.udla.arquitectura.demo.model.Producto;
import com.udla.arquitectura.demo.service.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST del catalogo.
 */
@RestController
@RequestMapping("/api")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/productos")
    public List<Producto> listar() {
        return productoService.listarTodos();
    }

    @GetMapping("/productos/{id}")
    public Producto buscar(@PathVariable Long id) {
        return productoService.buscarPorId(id);
    }

    /** Vacia la cache para poder volver a observar la latencia original. */
    @DeleteMapping("/cache")
    public ResponseEntity<Void> limpiarCache() {
        productoService.limpiarCache();
        return ResponseEntity.noContent().build();
    }
}
