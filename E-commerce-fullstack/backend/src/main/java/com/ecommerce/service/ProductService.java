package com.ecommerce.service;

import com.ecommerce.dto.PageResponse;
import com.ecommerce.dto.ProductRequest;
import com.ecommerce.dto.ProductResponse;
import java.util.List;

public interface ProductService {

    /**
     * @param keyword      optional, case-insensitive match on the product name
     * @param categorySlug optional category filter (e.g. "shirts")
     * @param categoryId   optional category filter by id
     * @param sort         newest (default) | oldest | price_asc | price_desc | name  (low / high also accepted)
     */
    PageResponse<ProductResponse> search(String keyword, String categorySlug, Long categoryId,
                                         String sort, int page, int size);

    List<ProductResponse> recent(int limit);

    ProductResponse findById(Long id);

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);
}
