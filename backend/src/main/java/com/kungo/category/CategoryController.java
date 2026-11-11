package com.kungo.category;

import com.kungo.common.response.ApiResponse;
import com.kungo.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Catégories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Arborescence des catégories racines actives (mise en cache)")
    public ApiResponse<List<CategoryResponse>> getRootCategories() {
        return ApiResponse.success(categoryService.getRootCategories());
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche dynamique multicritère des catégories")
    public ApiResponse<PageResponse<CategoryResponse>> search(CategorySearchRequest request, Pageable pageable) {
        return ApiResponse.success(PageResponse.of(categoryService.search(request, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une catégorie par id")
    public ApiResponse<CategoryResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(categoryService.getById(id));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Consulter une catégorie par slug")
    public ApiResponse<CategoryResponse> getBySlug(@PathVariable String slug) {
        return ApiResponse.success(categoryService.getBySlug(slug));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une catégorie (admin)")
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryCreateRequest request) {
        return ApiResponse.success(categoryService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Mettre à jour une catégorie (admin)")
    public ApiResponse<CategoryResponse> update(@PathVariable UUID id, @Valid @RequestBody CategoryUpdateRequest request) {
        return ApiResponse.success(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une catégorie (admin)")
    public void delete(@PathVariable UUID id) {
        categoryService.delete(id);
    }
}
