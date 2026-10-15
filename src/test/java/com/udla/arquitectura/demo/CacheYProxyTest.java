package com.udla.arquitectura.demo;

import com.udla.arquitectura.demo.repository.ProductoRepository;
import com.udla.arquitectura.demo.repository.ProductoRepositoryProxy;
import com.udla.arquitectura.demo.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CacheYProxyTest {

    @Autowired
    ProductoService service;

    @Autowired
    ProductoRepository repository;

    @BeforeEach
    void limpiar() {
        service.limpiarCache();
    }

    @Test
    void springInyectaElProxyComoRepositorio() {
        assertThat(repository).isInstanceOf(ProductoRepositoryProxy.class);
    }

    @Test
    void segundaLlamadaSeRespondeDesdeCache() {
        ProductoRepositoryProxy proxy = (ProductoRepositoryProxy) repository;
        long antes = proxy.getAccesosReales();

        service.buscarPorId(1L);
        long inicio = System.currentTimeMillis();
        service.buscarPorId(1L);
        long duracionSegunda = System.currentTimeMillis() - inicio;

        assertThat(proxy.getAccesosReales() - antes).isEqualTo(1);
        assertThat(duracionSegunda).isLessThan(100);
    }

    @Test
    void proxyRechazaIdInvalidoSinTocarLaFuente() {
        ProductoRepositoryProxy proxy = (ProductoRepositoryProxy) repository;
        long antes = proxy.getAccesosReales();

        assertThatThrownBy(() -> service.buscarPorId(-5L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(proxy.getAccesosReales()).isEqualTo(antes);
    }
}
