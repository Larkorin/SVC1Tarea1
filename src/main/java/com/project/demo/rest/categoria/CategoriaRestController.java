package com.project.demo.rest.categoria;

import com.project.demo.logic.entity.categoria.Categoria;
import com.project.demo.logic.entity.categoria.CategoriaRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
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
@RequestMapping("/categorias")
public class CategoriaRestController {

    @Autowired
    private CategoriaRepository CategoriaRepository;

//    @GetMapping
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'USER')")
//    public List<Categoria> getAllProductos() {
//        return CategoriaRepository.findAll();
//    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page-1, size);
        Page<Categoria> categoriasPage = CategoriaRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(categoriasPage.getTotalPages());
        meta.setTotalElements(categoriasPage.getTotalElements());
        meta.setPageNumber(categoriasPage.getNumber() + 1);
        meta.setPageSize(categoriasPage.getSize());

        return new GlobalResponseHandler().handleResponse("Categoria retrieved successfully",
                categoriasPage.getContent(), HttpStatus.OK, meta);
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Categoria createCategoria(@RequestBody Categoria categoria) {
        return CategoriaRepository.save(categoria);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Categoria updateCategoria(@PathVariable Long id,  @RequestBody Categoria categoria) {
        return CategoriaRepository.findById(id)
                .map(existingCategoria -> {
                    existingCategoria.setNombre(categoria.getNombre());
                    existingCategoria.setDescripcion(categoria.getDescripcion());
                    return CategoriaRepository.save(existingCategoria);
                })
                .orElseGet(() -> {
                    categoria.setId(id);
                    return CategoriaRepository.save(categoria);
                });
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void deleteCategoria(@PathVariable Long id) {
        CategoriaRepository.deleteById(id);
    }

}
