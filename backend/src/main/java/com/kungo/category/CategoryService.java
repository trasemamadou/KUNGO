package com.kungo.category;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    Category getEntityById(UUID id);

    CategoryResponse getById(UUID id);

    CategoryResponse getBySlug(String slug);

    List<CategoryResponse> getRootCategories();

    Page<CategoryResponse> search(CategorySearchRequest request, Pageable pageable);

    CategoryResponse create(CategoryCreateRequest request);

    CategoryResponse update(UUID id, CategoryUpdateRequest request);

    void delete(UUID id);
}
