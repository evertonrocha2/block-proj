package com.crud.system.repository;

import com.crud.system.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(String category);

    // busca parcial por nome, ignorando maiusculas/minusculas
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> searchByName(@Param("name") String name);

    @Query("SELECT p FROM Product p WHERE p.quantity < :threshold")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    // checa se ja existe produto com esse nome (exceto o proprio, pra update)
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE LOWER(p.name) = LOWER(:name) AND (:id IS NULL OR p.id != :id)")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);
}
