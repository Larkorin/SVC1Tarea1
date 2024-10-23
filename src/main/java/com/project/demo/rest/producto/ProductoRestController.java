package com.project.demo.rest.producto;

import com.project.demo.logic.entity.ProductoRequest;
import com.project.demo.logic.entity.categoria.Categoria;
import com.project.demo.logic.entity.categoria.CategoriaRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.producto.Producto;
import com.project.demo.logic.entity.producto.ProductoRepository;
import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.user.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<?> getAllProductosConCategoria(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Producto> productosPage = ProductoRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(productosPage.getTotalPages());
        meta.setTotalElements(productosPage.getTotalElements());
        meta.setPageNumber(productosPage.getNumber() + 1);
        meta.setPageSize(productosPage.getSize());

        if (productosPage.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "No hay productos disponibles", productosPage.getContent(), HttpStatus.NO_CONTENT, meta
            );
        }
        return new GlobalResponseHandler().handleResponse(
                "Productos retrieved successfully", productosPage.getContent(), HttpStatus.OK, meta
        );
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
        producto.setCantidadStock(request.getCantidadStock());
        producto.setPrecio(request.getPrecio());
        producto.setCategoria(objetoCategoria.get());

        Producto productoSave = ProductoRepository.save(producto);
        return ResponseEntity.ok(productoSave);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public Producto updateProducto(@PathVariable Long id, @RequestBody ProductoRequest request) {
        Optional<Categoria> objetoCategoria = Optional.ofNullable(CategoriaRepository.findByNombre(request.getNombreCategoria()));
        if (objetoCategoria.isEmpty()) {
            throw new EntityNotFoundException("La categoría no existe: " + request.getNombreCategoria());
        }
        Producto producto = new Producto();
        return ProductoRepository.findById(id)
                .map(existingProducto -> {
                    existingProducto.setNombre(request.getNombre());
                    existingProducto.setDescripcion(request.getDescripcion());
                    existingProducto.setPrecio(request.getPrecio());
                    existingProducto.setCantidadStock(request.getCantidadStock());
                    existingProducto.setCategoria(objetoCategoria.get());
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
