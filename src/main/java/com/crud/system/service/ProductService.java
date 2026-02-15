package com.crud.system.service;

import com.crud.system.exception.BusinessRuleException;
import com.crud.system.exception.ResourceNotFoundException;
import com.crud.system.model.Product;
import com.crud.system.repository.ProductRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * Serviço de negócios para gerenciamento de produtos.
 * Implementa lógica de negócio, validações e orquestração de operações.
 * Aplica técnicas de fail early e fail gracefully.
 */
@Service
@Validated
@Transactional
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private static final int LOW_STOCK_THRESHOLD = 10;

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Cria um novo produto.
     * Fail early: Valida duplicatas antes de persistir.
     */
    public Product createProduct(@Valid @NotNull Product product) {
        logger.info("Criando novo produto: {}", product.getName());
        
        try {
            // Validação de duplicata (fail early)
            if (productRepository.existsByNameAndIdNot(product.getName(), null)) {
                throw new BusinessRuleException(
                    "Já existe um produto com o nome: " + product.getName()
                );
            }
            
            Product savedProduct = productRepository.save(product);
            logger.info("Produto criado com sucesso. ID: {}", savedProduct.getId());
            
            return savedProduct;
            
        } catch (DataAccessException e) {
            logger.error("Erro ao acessar banco de dados durante criação de produto", e);
            throw new BusinessRuleException(
                "Erro ao criar produto. Por favor, tente novamente.", e
            );
        }
    }

    /**
     * Busca todos os produtos.
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        logger.debug("Buscando todos os produtos");
        
        try {
            return productRepository.findAll();
        } catch (DataAccessException e) {
            logger.error("Erro ao buscar produtos", e);
            throw new BusinessRuleException(
                "Erro ao buscar produtos. Por favor, tente novamente.", e
            );
        }
    }

    /**
     * Busca um produto por ID.
     * Fail early: Lança exceção se não encontrado.
     */
    @Transactional(readOnly = true)
    public Product getProductById(@NotNull Long id) {
        logger.debug("Buscando produto com ID: {}", id);
        
        if (id <= 0) {
            throw new IllegalArgumentException("ID do produto deve ser positivo");
        }
        
        try {
            return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", id));
        } catch (DataAccessException e) {
            logger.error("Erro ao buscar produto por ID: {}", id, e);
            throw new BusinessRuleException(
                "Erro ao buscar produto. Por favor, tente novamente.", e
            );
        }
    }

    /**
     * Atualiza um produto existente.
     */
    public Product updateProduct(@NotNull Long id, @Valid @NotNull Product productDetails) {
        logger.info("Atualizando produto ID: {}", id);
        
        if (id <= 0) {
            throw new IllegalArgumentException("ID do produto deve ser positivo");
        }
        
        try {
            Product existingProduct = getProductById(id);
            
            // Validação de duplicata (fail early)
            if (!existingProduct.getName().equalsIgnoreCase(productDetails.getName()) &&
                productRepository.existsByNameAndIdNot(productDetails.getName(), id)) {
                throw new BusinessRuleException(
                    "Já existe outro produto com o nome: " + productDetails.getName()
                );
            }
            
            // Atualiza apenas os campos modificáveis
            existingProduct.setName(productDetails.getName());
            existingProduct.setDescription(productDetails.getDescription());
            existingProduct.setPrice(productDetails.getPrice());
            existingProduct.setQuantity(productDetails.getQuantity());
            existingProduct.setCategory(productDetails.getCategory());
            
            Product updatedProduct = productRepository.save(existingProduct);
            logger.info("Produto atualizado com sucesso. ID: {}", updatedProduct.getId());
            
            return updatedProduct;
            
        } catch (DataAccessException e) {
            logger.error("Erro ao atualizar produto ID: {}", id, e);
            throw new BusinessRuleException(
                "Erro ao atualizar produto. Por favor, tente novamente.", e
            );
        }
    }

    /**
     * Deleta um produto por ID.
     */
    public void deleteProduct(@NotNull Long id) {
        logger.info("Deletando produto ID: {}", id);
        
        if (id <= 0) {
            throw new IllegalArgumentException("ID do produto deve ser positivo");
        }
        
        try {
            Product product = getProductById(id);
            productRepository.delete(product);
            logger.info("Produto deletado com sucesso. ID: {}", id);
            
        } catch (DataAccessException e) {
            logger.error("Erro ao deletar produto ID: {}", id, e);
            throw new BusinessRuleException(
                "Erro ao deletar produto. Por favor, tente novamente.", e
            );
        }
    }

    /**
     * Busca produtos por categoria.
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(@NotNull String category) {
        logger.debug("Buscando produtos da categoria: {}", category);
        
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Categoria não pode ser vazia");
        }
        
        try {
            return productRepository.findByCategory(category.trim());
        } catch (DataAccessException e) {
            logger.error("Erro ao buscar produtos por categoria: {}", category, e);
            throw new BusinessRuleException(
                "Erro ao buscar produtos por categoria. Por favor, tente novamente.", e
            );
        }
    }

    /**
     * Busca produtos por nome (busca parcial).
     */
    @Transactional(readOnly = true)
    public List<Product> searchProductsByName(@NotNull String name) {
        logger.debug("Buscando produtos com nome contendo: {}", name);
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome de busca não pode ser vazio");
        }
        
        try {
            return productRepository.searchByName(name.trim());
        } catch (DataAccessException e) {
            logger.error("Erro ao buscar produtos por nome: {}", name, e);
            throw new BusinessRuleException(
                "Erro ao buscar produtos por nome. Por favor, tente novamente.", e
            );
        }
    }

    /**
     * Retorna produtos com estoque baixo.
     */
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts() {
        logger.debug("Buscando produtos com estoque baixo");
        
        try {
            return productRepository.findLowStockProducts(LOW_STOCK_THRESHOLD);
        } catch (DataAccessException e) {
            logger.error("Erro ao buscar produtos com estoque baixo", e);
            throw new BusinessRuleException(
                "Erro ao buscar produtos com estoque baixo. Por favor, tente novamente.", e
            );
        }
    }
}
