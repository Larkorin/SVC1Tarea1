package com.project.demo.rest.producto;

import com.project.demo.logic.entity.ProductoRequest;
import com.project.demo.logic.entity.categoria.Categoria;
import com.project.demo.logic.entity.categoria.CategoriaRepository;
import com.project.demo.logic.entity.producto.Producto;
import com.project.demo.logic.entity.producto.ProductoRepository;
import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/productos")
public class ProductoRestController {

    @Autowired
    private ProductoRepository ProductoRepository;

    @Autowired
    private CategoriaRepository CategoriaRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'USER')")
    public ResponseEntity<List<Producto>> getAllProductosConCategoria() {
        List<Producto> productos = ProductoRepository.findByCategoriaNombre();
        if (productos.isEmpty()) {
            return ResponseEntity.noContent().build(); // Devuelve un 204 si no hay productos
        }
        return ResponseEntity.ok(productos); // Devuelve un 200 con la lista de productos
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createProducto(@RequestBody ProductoRequest request) {

        Optional<Categoria> objetoCategoria = Optional.ofNullable(CategoriaRepository.findByNombre(request.getNombreCategoria()));
        if (objetoCategoria.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Categoria not found");
        }
        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setCantidadStock(request.getStock());
        producto.setPrecio(request.getPrecio());
        producto.setCategoria(objetoCategoria.get());

        Producto productoSave = ProductoRepository.save(producto);
        return ResponseEntity.ok(productoSave);
    }



    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public Producto updateProducto(@PathVariable Long id, @RequestBody Producto producto) {
        return ProductoRepository.findById(id)
                .map(existingProducto -> {
                    existingProducto.setNombre(producto.getNombre());
                    existingProducto.setDescripcion(producto.getDescripcion());
                    existingProducto.setPrecio(producto.getPrecio());
                    existingProducto.setCantidadStock(producto.getCantidadStock());
                    existingProducto.setCategoria(producto.getCategoria());
                    return ProductoRepository.save(existingProducto);
                })
                .orElseGet(() -> {
                    producto.setId(id);
                    return ProductoRepository.save(producto);
                });
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public void deleteProducto(@PathVariable Long id) {
        ProductoRepository.deleteById(id);
    }
}
