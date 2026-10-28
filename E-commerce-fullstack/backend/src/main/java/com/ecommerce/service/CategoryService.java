package com.ecommerce.service;

import com.ecommerce.dto.CategoryRequest;
import com.ecommerce.dto.CategoryResponse;
import java.util.List;

public interface CategoryService {

    List<CategoryResponse> findAll();

    CategoryResponse findById(Long id);

    CategoryResponse create(CategoryRequest request);

    CategoryResponse update(Long id, CategoryRequest request);

    void delete(Long id);
}
