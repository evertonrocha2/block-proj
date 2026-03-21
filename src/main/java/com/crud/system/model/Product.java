package com.crud.system.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do produto é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 10, max = 500, message = "Descrição deve ter entre 10 e 500 caracteres")
    @Column(nullable = false, length = 500)
    private String description;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @DecimalMax(value = "999999.99", message = "Preço não pode exceder 999999.99")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Quantidade em estoque é obrigatória")
    @Min(value = 0, message = "Quantidade não pode ser negativa")
    @Max(value = 999999, message = "Quantidade não pode exceder 999999")
    @Column(nullable = false)
    private Integer quantity;

    @NotBlank(message = "Categoria é obrigatória")
    @Size(min = 3, max = 50, message = "Categoria deve ter entre 3 e 50 caracteres")
    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        validateBusinessRules();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        validateBusinessRules();
    }

    private void validateBusinessRules() {
        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode conter apenas espaços em branco");
        }

        if (description != null && description.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição não pode conter apenas espaços em branco");
        }

        if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço deve ser maior que zero");
        }

        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Quantidade não pode ser negativa");
        }
    }
}
