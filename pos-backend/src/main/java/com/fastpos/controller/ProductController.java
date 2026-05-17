package com.fastpos.controller;

import com.fastpos.dto.ProductDTO;
import com.fastpos.model.Category;
import com.fastpos.model.Product;
import com.fastpos.repository.CategoryRepository;
import com.fastpos.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/products")
    public ResponseEntity<Page<ProductDTO>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Product> products;

        if (search != null && !search.isBlank()) {
            products = productService.searchProducts(search, pageRequest);
        } else if (categoryId != null) {
            products = productService.getProductsByCategory(categoryId, pageRequest);
        } else {
            products = productService.getAllProducts(pageRequest);
        }

        return ResponseEntity.ok(products.map(productService::toDTO));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.toDTO(productService.getProductById(id)));
    }

    @PostMapping("/products")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO dto) {
        Product product = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.toDTO(product));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        Product product = productService.updateProduct(id, dto);
        return ResponseEntity.ok(productService.toDTO(product));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }
}
