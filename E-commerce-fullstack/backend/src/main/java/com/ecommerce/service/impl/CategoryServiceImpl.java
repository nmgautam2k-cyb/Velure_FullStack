package com.ecommerce.service.impl;

import com.ecommerce.dto.CategoryRequest;
import com.ecommerce.dto.CategoryResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.exception.ConflictException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.CategoryService;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAllByOrderByNameAsc().stream().map(CategoryResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        return CategoryResponse.from(getOrThrow(id));
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();
        String slug = resolveSlug(request.slug(), name);
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("A category named '" + name + "' already exists");
        }
        if (categoryRepository.existsBySlug(slug)) {
            throw new ConflictException("A category with slug '" + slug + "' already exists");
        }
        Category category = new Category();
        apply(category, name, slug, request.description());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getOrThrow(id);
        String name = request.name().trim();
        String slug = resolveSlug(request.slug(), name);
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("A category named '" + name + "' already exists");
        }
        if (categoryRepository.existsBySlugAndIdNot(slug, id)) {
            throw new ConflictException("A category with slug '" + slug + "' already exists");
        }
        apply(category, name, slug, request.description());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = getOrThrow(id);
        if (productRepository.existsByCategoryId(id)) {
            throw new ConflictException("Category still has products. Move or delete them first");
        }
        categoryRepository.delete(category);
    }

    private Category getOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category " + id + " not found"));
    }

    private static void apply(Category category, String name, String slug, String description) {
        category.setName(name);
        category.setSlug(slug);
        category.setDescription(description == null || description.isBlank() ? null : description.trim());
    }

    private static String resolveSlug(String requested, String name) {
        String source = (requested == null || requested.isBlank()) ? name : requested;
        String ascii = Normalizer.normalize(source, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String slug = ascii.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-+|-+$)", "");
        if (slug.isEmpty()) {
            throw new com.ecommerce.exception.BadRequestException("Category name must contain letters or digits");
        }
        return slug;
    }
}
