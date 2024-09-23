package com.project.demo.logic.entity.producto;

import com.project.demo.logic.entity.categoria.Categoria;
import com.project.demo.logic.entity.producto.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p JOIN p.categoria c")
    List<Producto> findByCategoriaNombre();
}
