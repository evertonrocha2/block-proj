package com.crud.system.service;

import com.crud.system.model.Product;
import java.util.List;

public interface IProductService {

    Product createProduct(Product product);

    List<Product> getAllProducts();

    Product getProductById(Long id);

    Product updateProduct(Long id, Product productDetails);

    void deleteProduct(Long id);

    List<Product> getProductsByCategory(String category);

    List<Product> searchProductsByName(String name);

    List<Product> getLowStockProducts();
}
