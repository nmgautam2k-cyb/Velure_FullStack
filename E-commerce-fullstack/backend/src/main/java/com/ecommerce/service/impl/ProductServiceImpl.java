package com.ecommerce.service.impl;

import com.ecommerce.dto.PageResponse;
import com.ecommerce.dto.ProductRequest;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ConflictException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.WishlistRepository;
import com.ecommerce.service.ProductService;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

    static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final WishlistRepository wishlistRepository;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository,
                              CartItemRepository cartItemRepository, OrderItemRepository orderItemRepository,
                              WishlistRepository wishlistRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.wishlistRepository = wishlistRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String keyword, String categorySlug, Long categoryId,
                                                String sort, int page, int size) {
        Specification<Product> spec = (root, query, cb) -> cb.conjunction();

        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + escapeLike(keyword.trim().toLowerCase(Locale.ROOT)) + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern, '\\'));
        }
        if (categorySlug != null && !categorySlug.isBlank()) {
            String slug = categorySlug.trim().toLowerCase(Locale.ROOT);
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("slug"), slug));
        }
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }

        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE), sortFor(sort));
        Page<Product> result = productRepository.findAll(spec, pageable);
        return PageResponse.from(result, ProductResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> recent(int limit) {
        int n = Math.min(Math.max(limit, 1), 50);
        return productRepository.findAll(PageRequest.of(0, n, sortFor("newest")))
                .map(ProductResponse::from).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return ProductResponse.from(getOrThrow(id));
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getOrThrow(id);
        apply(product, request);
        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = getOrThrow(id);
        if (orderItemRepository.existsByProductId(id)) {
            throw new ConflictException(
                    "This product appears in past orders and cannot be deleted. Set its stock to 0 to hide it from sale");
        }
        cartItemRepository.deleteByProductId(id);
        wishlistRepository.deleteByProductId(id);
        productRepository.delete(product);
    }

    private Product getOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + id + " not found"));
    }

    private void apply(Product product, ProductRequest request) {
        String slug = request.category().trim().toLowerCase(Locale.ROOT);
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new BadRequestException("Unknown category: " + request.category()));

        String image = request.image() == null ? null : request.image().trim();
        if (image != null && image.regionMatches(true, 0, "data:", 0, 5)) {
            throw new BadRequestException(
                    "Inline image data is not accepted. Upload the file via /api/admin/uploads and send the returned URL");
        }

        product.setName(request.title().trim());
        product.setDescription(request.description() == null || request.description().isBlank()
                ? null : request.description().trim());
        product.setPrice(request.price().setScale(2, RoundingMode.HALF_UP));
        product.setStockQuantity(request.stockQuantity());
        product.setImageUrl(image == null || image.isEmpty() ? null : image);
        product.setCategory(category);
    }

    private static Sort sortFor(String sort) {
        String key = sort == null ? "newest" : sort.trim().toLowerCase(Locale.ROOT);
        Sort byIdDesc = Sort.by(Sort.Direction.DESC, "id");
        return switch (key) {
            case "price_asc", "price-asc", "low" -> Sort.by(Sort.Direction.ASC, "price").and(byIdDesc);
            case "price_desc", "price-desc", "high" -> Sort.by(Sort.Direction.DESC, "price").and(byIdDesc);
            case "name" -> Sort.by(Sort.Direction.ASC, "name").and(byIdDesc);
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt").and(Sort.by(Sort.Direction.ASC, "id"));
            default -> Sort.by(Sort.Direction.DESC, "createdAt").and(byIdDesc);
        };
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
