package com.fastpos.service;

import com.fastpos.dto.ProductDTO;
import com.fastpos.exception.ResourceNotFoundException;
import com.fastpos.model.Category;
import com.fastpos.model.Product;
import com.fastpos.repository.CategoryRepository;
import com.fastpos.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
    }

    public Page<Product> searchProducts(String query, Pageable pageable) {
        return productRepository.searchProducts(query, pageable);
    }

    @Transactional
    public Product createProduct(ProductDTO dto) {
        if (productRepository.existsBySku(dto.getSku())) {
            throw new IllegalArgumentException("Product with SKU " + dto.getSku() + " already exists");
        }

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .sku(dto.getSku())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .imageUrl(dto.getImageUrl())
                .active(dto.isActive())
                .build();

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            product.setCategory(category);
        }

        product = productRepository.save(product);
        log.info("Product created: {} (SKU: {})", product.getName(), product.getSku());
        return product;
    }

    @Transactional
    public Product updateProduct(Long id, ProductDTO dto) {
        Product product = getProductById(id);

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setImageUrl(dto.getImageUrl());
        product.setActive(dto.isActive());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            product.setCategory(category);
        }

        product = productRepository.save(product);
        log.info("Product updated: {} (ID: {})", product.getName(), id);
        return product;
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        product.setActive(false); // Soft delete
        productRepository.save(product);
        log.info("Product soft-deleted: {} (ID: {})", product.getName(), id);
    }

    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findByStockQuantityLessThan(threshold);
    }

    @Transactional
    public void updateStock(Long productId, int quantityChange) {
        Product product = getProductById(productId);
        int newStock = product.getStockQuantity() + quantityChange;
        if (newStock < 0) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }
        product.setStockQuantity(newStock);
        productRepository.save(product);
    }

    public ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setSku(product.getSku());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());
        dto.setActive(product.isActive());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        return dto;
    }
}
