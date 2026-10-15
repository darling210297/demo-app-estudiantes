package com.udla.arquitectura.demo.exception;

/**
 * Se lanza cuando se pide un producto que no existe.
 */
public class ProductoNoEncontradoException extends RuntimeException {

    public ProductoNoEncontradoException(Long id) {
        super("Producto no encontrado: " + id);
    }
}
