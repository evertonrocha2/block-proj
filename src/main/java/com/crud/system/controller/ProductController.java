package com.crud.system.controller;

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
 * Endpoints disponíveis em /api/products.
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

    /** POST /api/products - Cria um novo produto. */
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        logger.info("POST /api/products");
        Product created = productService.createProduct(product);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /** GET /api/products - Lista todos os produtos. */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /** GET /api/products/{id} - Busca produto por ID. */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable @NotNull Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /** PUT /api/products/{id} - Atualiza um produto. */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody Product productDetails) {
        return ResponseEntity.ok(productService.updateProduct(id, productDetails));
    }

    /** DELETE /api/products/{id} - Remove um produto. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable @NotNull Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/products/category/{category} - Filtra por categoria. */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable @NotNull String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    /** GET /api/products/search?name={name} - Busca por nome (parcial). */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProductsByName(@RequestParam @NotNull String name) {
        return ResponseEntity.ok(productService.searchProductsByName(name));
    }

    /** GET /api/products/low-stock - Produtos com estoque baixo. */
    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        return ResponseEntity.ok(productService.getLowStockProducts());
    }
}
