package com.nahuelriveros.productoapi.controller;

import com.nahuelriveros.productoapi.model.Producto;
import com.nahuelriveros.productoapi.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("productos")
public class ProductoRestController {

    @Autowired
    private ProductoRepository repository;

    @Value("${nombre.app}")
    private String nombreApp;

    @GetMapping("mensaje")
    public ResponseEntity<?> mensaje(@RequestHeader(name = "apikey", defaultValue = "") String apikey) {
        if (apikey.equals("unida777")) {
            return ResponseEntity.ok("Bienvenidos a " + nombreApp);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autorizado");
        }
    }

    @GetMapping
    public ResponseEntity<?> listarProductos() {
        List<Producto> productos = repository.findAll();
        if (productos.isEmpty()) {
            return ResponseEntity.ok().body(productos);
        } else {
            return ResponseEntity.ok(productos);
        }
    }

    @PostMapping("/crear")
    public ResponseEntity<?> agregarProducto(@RequestBody Producto p) {
        if (p.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0 || p.getCantidadStock() < 0) {
            return ResponseEntity.badRequest().body("Precio debe ser mayor a 0 y cantidad no puede ser negativa.");
        }

        Optional<Producto> productoExistente = repository.findById(p.getCodigo());
        if (productoExistente.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un producto con el código " + p.getCodigo());
        }

        p.setEstado(p.getCantidadStock() > 0 ? "Disponible" : "Agotado");
        repository.save(p);
        return ResponseEntity.status(HttpStatus.CREATED).body(p);
    }

    @GetMapping("/{codigo:\\d+}")
    public ResponseEntity<?> obtenerProducto(@PathVariable Integer codigo) {
        Optional<Producto> producto = repository.findById(codigo);
        if (producto.isPresent()) {
            return ResponseEntity.ok(producto.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encontró el producto con el código " + codigo);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Integer id) {
        Optional<Producto> producto = repository.findById(id);
        if (producto.isPresent()) {
            repository.deleteById(id);
            return ResponseEntity.ok("Producto eliminado con éxito.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encontró el producto con el código " + id);
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Integer id, @RequestBody Producto entrada) {
        if (entrada.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0 || entrada.getCantidadStock() < 0) {
            return ResponseEntity.badRequest().body("Precio debe ser mayor a 0 y cantidad no puede ser negativa.");
        }

        Optional<Producto> productoExistente = repository.findById(id);
        if (productoExistente.isPresent()) {
            Producto p = productoExistente.get();
            p.setNombre(entrada.getNombre());
            p.setDescripcion(entrada.getDescripcion());
            p.setCantidadStock(entrada.getCantidadStock());
            p.setPrecioUnitario(entrada.getPrecioUnitario());
            p.setEstado(entrada.getCantidadStock() > 0 ? "Disponible" : "Agotado");

            repository.save(p);
            return ResponseEntity.ok(p);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encontró el producto con el código " + id);
        }
    }

    @GetMapping("estadisticas")
    public ResponseEntity<?> obtenerEstadisticas() {
        List<Producto> productos = repository.findAll();

        if (productos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No hay productos para mostrar estadísticas.");
        }

        int total = productos.size();
        BigDecimal suma = productos.stream()
                .map(Producto::getPrecioUnitario)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal promedio = total > 0
                ? suma.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long disponibles = productos.stream()
                .filter(p -> "Disponible".equalsIgnoreCase(p.getEstado()))
                .count();
        long agotados = total - disponibles;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProductos", total);
        stats.put("promedioPrecio", promedio);
        stats.put("disponibles", disponibles);
        stats.put("agotados", agotados);

        return ResponseEntity.ok(stats);
    }
}