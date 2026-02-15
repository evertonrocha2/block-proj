package com.crud.system.repository;

import com.crud.system.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações de persistência da entidade Product.
 * Fornece métodos customizados para consultas específicas.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Busca produtos por categoria.
     */
    List<Product> findByCategory(String category);

    /**
     * Busca produtos cujo nome contém o texto especificado (case-insensitive).
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> searchByName(@Param("name") String name);

    /**
     * Busca produtos com quantidade em estoque abaixo de um limite.
     */
    @Query("SELECT p FROM Product p WHERE p.quantity < :threshold")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    /**
     * Verifica se existe um produto com o nome especificado (exceto o ID fornecido).
     * Útil para validar duplicatas em atualizações.
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE LOWER(p.name) = LOWER(:name) AND (:id IS NULL OR p.id != :id)")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);
}
