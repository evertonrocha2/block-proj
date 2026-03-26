package com.crud.system.service;

import com.crud.system.exception.BusinessRuleException;
import com.crud.system.exception.ResourceNotFoundException;
import com.crud.system.model.Product;
import com.crud.system.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Serviço responsável pelas operações de negócio de produtos.
 * Valida duplicatas, IDs e trata erros de acesso ao banco.
 */
@Service
@Transactional
public class ProductService implements IProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private static final String ERROR_CRIAR = "Erro ao criar produto. Por favor, tente novamente.";
    private static final String ERROR_BUSCAR = "Erro ao buscar produto. Por favor, tente novamente.";
    private static final String ERROR_BUSCAR_LISTA = "Erro ao buscar produtos. Por favor, tente novamente.";

    @Value("${product.low-stock.threshold:10}")
    private Integer lowStockThreshold = 10;

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Cria um novo produto, validando se já não existe outro com o mesmo nome.
     */
    public Product createProduct(Product product) {
        logger.info("Criando produto: {}", product.getName());

        try {
            if (productRepository.existsByNameAndIdNot(product.getName(), null)) {
                throw new BusinessRuleException(
                    "Já existe um produto com o nome: " + product.getName()
                );
            }

            Product saved = productRepository.save(product);
            logger.info("Produto criado - ID: {}", saved.getId());
            return saved;

        } catch (DataAccessException e) {
            logger.error("Erro de banco ao criar produto", e);
            throw new BusinessRuleException("Erro ao criar produto. Por favor, tente novamente.", e);
        }
    }

    /** Retorna todos os produtos cadastrados. */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        try {
            return productRepository.findAll();
        } catch (DataAccessException e) {
            logger.error("Erro ao buscar produtos", e);
            throw new BusinessRuleException("Erro ao buscar produtos. Por favor, tente novamente.", e);
        }
    }

    /** Busca um produto pelo ID. Lança exceção se não existir. */
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID do produto deve ser positivo");
        }

        try {
            return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", id));
        } catch (DataAccessException e) {
            logger.error("Erro ao buscar produto ID: {}", id, e);
            throw new BusinessRuleException("Erro ao buscar produto. Por favor, tente novamente.", e);
        }
    }

    /**
     * Atualiza os dados de um produto existente.
     * Também valida se o novo nome não conflita com outro produto.
     */
    public Product updateProduct(Long id, Product productDetails) {
        logger.info("Atualizando produto ID: {}", id);

        if (id <= 0) {
            throw new IllegalArgumentException("ID do produto deve ser positivo");
        }

        try {
            Product existing = getProductById(id);

            // verifica se o novo nome ja nao pertence a outro produto
            if (!existing.getName().equalsIgnoreCase(productDetails.getName()) &&
                productRepository.existsByNameAndIdNot(productDetails.getName(), id)) {
                throw new BusinessRuleException(
                    "Já existe outro produto com o nome: " + productDetails.getName()
                );
            }

            existing.setName(productDetails.getName());
            existing.setDescription(productDetails.getDescription());
            existing.setPrice(productDetails.getPrice());
            existing.setQuantity(productDetails.getQuantity());
            existing.setCategory(productDetails.getCategory());

            Product updated = productRepository.save(existing);
            logger.info("Produto atualizado - ID: {}", updated.getId());
            return updated;

        } catch (DataAccessException e) {
            logger.error("Erro ao atualizar produto ID: {}", id, e);
            throw new BusinessRuleException("Erro ao atualizar produto. Por favor, tente novamente.", e);
        }
    }

    /** Remove um produto pelo ID. */
    public void deleteProduct(Long id) {
        logger.info("Deletando produto ID: {}", id);

        if (id <= 0) {
            throw new IllegalArgumentException("ID do produto deve ser positivo");
        }

        try {
            Product product = getProductById(id);
            productRepository.delete(product);
            logger.info("Produto deletado - ID: {}", id);
        } catch (DataAccessException e) {
            logger.error("Erro ao deletar produto ID: {}", id, e);
            throw new BusinessRuleException("Erro ao deletar produto. Por favor, tente novamente.", e);
        }
    }

    /** Filtra produtos por categoria. */
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Categoria não pode ser vazia");
        }

        try {
            return productRepository.findByCategory(category.trim());
        } catch (DataAccessException e) {
            logger.error("Erro ao buscar por categoria: {}", category, e);
            throw new BusinessRuleException("Erro ao buscar produtos por categoria. Por favor, tente novamente.", e);
        }
    }

    /** Busca produtos cujo nome contenha o texto informado. */
    @Transactional(readOnly = true)
    public List<Product> searchProductsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome de busca não pode ser vazio");
        }

        try {
            return productRepository.searchByName(name.trim());
        } catch (DataAccessException e) {
            logger.error("Erro ao buscar por nome: {}", name, e);
            throw new BusinessRuleException("Erro ao buscar produtos por nome. Por favor, tente novamente.", e);
        }
    }

    /** Retorna produtos com quantidade abaixo do threshold configurado. */
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts() {
        try {
            return productRepository.findLowStockProducts(lowStockThreshold);
        } catch (DataAccessException e) {
            logger.error("Erro ao buscar produtos com estoque baixo", e);
            throw new BusinessRuleException("Erro ao buscar produtos com estoque baixo. Por favor, tente novamente.", e);
        }
    }
}
