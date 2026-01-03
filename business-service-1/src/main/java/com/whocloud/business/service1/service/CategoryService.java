package com.whocloud.business.service1.service;

import com.whocloud.business.service1.dto.category.CategoryRequest;
import com.whocloud.business.service1.dto.category.CategoryResponse;
import com.whocloud.business.service1.dto.category.CategoryTreeResponse;
import com.whocloud.business.service1.entity.Category;
import com.whocloud.business.service1.exception.BusinessLogicException;
import com.whocloud.business.service1.exception.DuplicateResourceException;
import com.whocloud.business.service1.exception.ResourceNotFoundException;
import com.whocloud.business.service1.mapper.CategoryMapper;
import com.whocloud.business.service1.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Creating new category: {}", request.getName());

        // Validate unique name
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
        }

        // Create category
        Category category = categoryMapper.toEntity(request);

        // Set parent if provided
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + request.getParentId()));
            category.setParent(parent);
        }

        Category saved = categoryRepository.save(category);
        log.info("Category created successfully with id: {}", saved.getId());

        return categoryMapper.toResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        log.info("Updating category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Validate unique name (excluding current category)
        categoryRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
                    }
                });

        // Update fields
        categoryMapper.updateEntityFromRequest(category, request);

        // Update parent if changed
        if (request.getParentId() != null) {
            // Prevent circular reference
            if (request.getParentId().equals(id)) {
                throw new BusinessLogicException("Category cannot be its own parent");
            }

            // Prevent setting descendant as parent
            if (isDescendant(id, request.getParentId())) {
                throw new BusinessLogicException("Cannot set a descendant category as parent");
            }

            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + request.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category updated = categoryRepository.save(category);
        log.info("Category updated successfully: {}", updated.getId());

        return categoryMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        log.debug("Fetching category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        return categoryMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        log.debug("Fetching all categories with pagination");

        return categoryRepository.findAll(pageable)
                .map(categoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        log.debug("Fetching root categories (no parent)");

        return categoryRepository.findByParentIsNull().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubcategories(Long parentId) {
        log.debug("Fetching subcategories for parent id: {}", parentId);

        // Verify parent exists
        if (!categoryRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Category not found with id: " + parentId);
        }

        return categoryRepository.findByParentId(parentId).stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getCategoryTree() {
        log.debug("Building category tree");

        // Get all root categories
        List<Category> rootCategories = categoryRepository.findByParentIsNull();

        return rootCategories.stream()
                .map(this::buildTree)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Check if category has children
        List<Category> children = categoryRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessLogicException("Cannot delete category with subcategories. Delete subcategories first.");
        }

        // TODO: Check if category is used by equipment
        // This would require EquipmentRepository injection

        categoryRepository.delete(category);
        log.info("Category deleted successfully: {}", id);
    }

    /**
     * Build hierarchical tree structure recursively
     */
    private CategoryTreeResponse buildTree(Category category) {
        CategoryTreeResponse node = categoryMapper.toTreeResponse(category);

        List<Category> children = categoryRepository.findByParentId(category.getId());
        if (!children.isEmpty()) {
            node.setChildren(children.stream()
                    .map(this::buildTree)
                    .collect(Collectors.toList()));
        } else {
            node.setChildren(new ArrayList<>());
        }

        return node;
    }

    /**
     * Check if targetId is a descendant of ancestorId to prevent circular references
     */
    private boolean isDescendant(Long ancestorId, Long targetId) {
        Category target = categoryRepository.findById(targetId).orElse(null);
        if (target == null) {
            return false;
        }

        Category current = target;
        while (current.getParent() != null) {
            if (current.getParent().getId().equals(ancestorId)) {
                return true;
            }
            current = current.getParent();
        }

        return false;
    }
}
