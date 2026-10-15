package com.udla.arquitectura.demo.repository;

import com.udla.arquitectura.demo.model.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PATRON PROXY (GoF - estructural).
 *
 * - Subject:      ProductoRepository (interfaz comun)
 * - RealSubject:  RepositorioProductoEnMemoria (acceso "lento" a datos)
 * - Proxy:        esta clase
 *
 * El proxy implementa la MISMA interfaz que el objeto real y lo envuelve, por lo
 * que el cliente (ProductoService) no sabe si habla con el real o con el proxy.
 * Antes/despues de delegar agrega responsabilidades transversales sin tocar
 * el objeto real (principio Abierto/Cerrado):
 *   1. Proteccion: valida el id y evita llegar a la "base de datos" con datos invalidos.
 *   2. Registro:   loguea cada acceso real y mide su latencia.
 *   3. Metrica:    cuenta cuantas veces se accede realmente a la fuente de datos
 *                  (sirve para comprobar que la cache funciona).
 *
 * @Primary hace que Spring inyecte el proxy cuando alguien pide ProductoRepository.
 */
@Repository
@Primary
public class ProductoRepositoryProxy implements ProductoRepository {

    private static final Logger log = LoggerFactory.getLogger(ProductoRepositoryProxy.class);

    private final ProductoRepository repositorioReal;
    private final AtomicLong accesosReales = new AtomicLong();

    public ProductoRepositoryProxy(@Qualifier("repositorioProductoEnMemoria") ProductoRepository repositorioReal) {
        this.repositorioReal = repositorioReal;
    }

    @Override
    public List<Producto> listarTodos() {
        long inicio = System.currentTimeMillis();
        List<Producto> productos = repositorioReal.listarTodos();
        registrar("listarTodos()", inicio);
        return productos;
    }

    @Override
    public Producto buscarPorId(Long id) {
        if (id == null || id <= 0) {
            log.warn("[PROXY] Id invalido rechazado sin acceder a la fuente de datos: {}", id);
            throw new IllegalArgumentException("El id debe ser un numero positivo");
        }
        long inicio = System.currentTimeMillis();
        Producto producto = repositorioReal.buscarPorId(id);
        registrar("buscarPorId(" + id + ")", inicio);
        return producto;
    }

    public long getAccesosReales() {
        return accesosReales.get();
    }

    private void registrar(String operacion, long inicio) {
        long total = accesosReales.incrementAndGet();
        log.info("[PROXY] Acceso a fuente de datos -> {} en {} ms (accesos reales: {})",
                operacion, System.currentTimeMillis() - inicio, total);
    }
}
