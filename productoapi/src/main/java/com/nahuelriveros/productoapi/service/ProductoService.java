package com.nahuelriveros.productoapi.service;

import com.nahuelriveros.productoapi.model.Producto;
import com.nahuelriveros.productoapi.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository repository;

    public List<Producto> listarTodos() {
        return repository.findAll();
    }

    public Optional<Producto> obtenerPorId(Integer id) {
        return repository.findById(id);
    }

    public Producto guardarProducto(Producto p) {
        // Validaciones básicas:
        if (p.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        if (p.getCantidadStock() < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        // Setear estado basado en stock con operador ternario
        p.setEstado(p.getCantidadStock() > 0 ? "Disponible" : "Agotado");

        return repository.save(p);
    }

    public Producto actualizarProducto(Integer id, Producto p) {
        Optional<Producto> existente = repository.findById(id);
        if (existente.isEmpty()) {
            throw new IllegalArgumentException("No se encontró el producto con el código " + id);
        }
        Producto prod = existente.get();

        if (p.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        if (p.getCantidadStock() < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }

        prod.setNombre(p.getNombre());
        prod.setDescripcion(p.getDescripcion());
        prod.setCantidadStock(p.getCantidadStock());
        prod.setPrecioUnitario(p.getPrecioUnitario());
        prod.setEstado(p.getCantidadStock() > 0 ? "Disponible" : "Agotado");

        return repository.save(prod);
    }

    public void eliminarProducto(Integer id) {
        repository.deleteById(id);
    }
}