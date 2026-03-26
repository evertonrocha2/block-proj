package com.crud.system.controller;

import com.crud.system.dto.ProductMapper;
import com.crud.system.dto.ProductRequestDTO;
import com.crud.system.dto.ProductResponseDTO;
import com.crud.system.model.Product;
import com.crud.system.service.IProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para o CRUD de produtos.
 * Usa DTOs para separar a camada de interface da camada de domínio,
 * aplicando o princípio de separação de responsabilidades.
 */
@RestController
@RequestMapping("/api/products")
@Validated
@CrossOrigin(origins = "*")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final IProductService productService;

    @Autowired
    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    /** POST /api/products - Cria um novo produto via DTO. */
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO requestDTO) {
        logger.info("POST /api/products");
        Product product = ProductMapper.toEntity(requestDTO);
        Product created = productService.createProduct(product);
        return new ResponseEntity<>(ProductMapper.toResponseDTO(created), HttpStatus.CREATED);
    }

    /** GET /api/products - Lista todos os produtos. */
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> products = productService.getAllProducts().stream()
            .map(ProductMapper::toResponseDTO)
            .toList();
        return ResponseEntity.ok(products);
    }

    /** GET /api/products/{id} - Busca produto por ID. */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable @NotNull Long id) {
        return ResponseEntity.ok(ProductMapper.toResponseDTO(productService.getProductById(id)));
    }

    /** PUT /api/products/{id} - Atualiza um produto via DTO. */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody ProductRequestDTO requestDTO) {
        Product productDetails = ProductMapper.toEntity(requestDTO);
        Product updated = productService.updateProduct(id, productDetails);
        return ResponseEntity.ok(ProductMapper.toResponseDTO(updated));
    }

    /** DELETE /api/products/{id} - Remove um produto. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable @NotNull Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/products/category/{category} - Filtra por categoria. */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCategory(@PathVariable @NotNull String category) {
        List<ProductResponseDTO> products = productService.getProductsByCategory(category).stream()
            .map(ProductMapper::toResponseDTO)
            .toList();
        return ResponseEntity.ok(products);
    }

    /** GET /api/products/search?name={name} - Busca por nome (parcial). */
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> searchProductsByName(@RequestParam @NotNull String name) {
        List<ProductResponseDTO> products = productService.searchProductsByName(name).stream()
            .map(ProductMapper::toResponseDTO)
            .toList();
        return ResponseEntity.ok(products);
    }

    /** GET /api/products/low-stock - Produtos com estoque baixo. */
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponseDTO>> getLowStockProducts() {
        List<ProductResponseDTO> products = productService.getLowStockProducts().stream()
            .map(ProductMapper::toResponseDTO)
            .toList();
        return ResponseEntity.ok(products);
    }
}
