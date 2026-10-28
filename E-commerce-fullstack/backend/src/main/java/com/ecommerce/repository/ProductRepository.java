package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsByCategoryId(Long categoryId);

    /**
     * Row-locks the products (SELECT ... FOR UPDATE), ordered by id so concurrent checkouts lock in
     * the same order and cannot deadlock each other. Used by order creation and cancellation.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id in :ids order by p.id")
    List<Product> lockAllByIdIn(@Param("ids") Collection<Long> ids);

    @Query("select c.slug, count(p) from Product p join p.category c group by c.slug")
    List<Object[]> countByCategorySlug();
}
