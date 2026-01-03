package com.whocloud.business.service1.controller;

import com.whocloud.business.service1.dto.category.CategoryRequest;
import com.whocloud.business.service1.dto.category.CategoryResponse;
import com.whocloud.business.service1.dto.category.CategoryTreeResponse;
import com.whocloud.business.service1.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Create a new category
     * POST /api/categories
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        log.info("REST request to create category: {}", request.getName());
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing category
     * PUT /api/categories/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        log.info("REST request to update category: {}", id);
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a category by ID
     * GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        log.debug("REST request to get category: {}", id);
        CategoryResponse response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all categories with pagination
     * GET /api/categories?page=0&size=20&sort=name,asc
     */
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        log.debug("REST request to get all categories");

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<CategoryResponse> categories = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }

    /**
     * Get root categories (categories without parent)
     * GET /api/categories/roots
     */
    @GetMapping("/roots")
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        log.debug("REST request to get root categories");
        List<CategoryResponse> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get subcategories of a parent category
     * GET /api/categories/{parentId}/children
     */
    @GetMapping("/{parentId}/children")
    public ResponseEntity<List<CategoryResponse>> getSubcategories(@PathVariable Long parentId) {
        log.debug("REST request to get subcategories of: {}", parentId);
        List<CategoryResponse> categories = categoryService.getSubcategories(parentId);
        return ResponseEntity.ok(categories);
    }

    /**
     * Get full category tree (hierarchical structure)
     * GET /api/categories/tree
     */
    @GetMapping("/tree")
    public ResponseEntity<List<CategoryTreeResponse>> getCategoryTree() {
        log.debug("REST request to get category tree");
        List<CategoryTreeResponse> tree = categoryService.getCategoryTree();
        return ResponseEntity.ok(tree);
    }

    /**
     * Delete a category
     * DELETE /api/categories/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        log.info("REST request to delete category: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
