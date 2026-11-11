package com.kungo.category;

import com.kungo.common.exception.ConflictException;
import com.kungo.common.exception.ResourceNotFoundException;
import com.kungo.common.util.SlugUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private static final String CACHE_NAME = "categories";

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public Category getEntityById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("category.not-found", id));
    }

    @Override
    public CategoryResponse getById(UUID id) {
        return categoryMapper.toResponse(getEntityById(id));
    }

    @Override
    public CategoryResponse getBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> ResourceNotFoundException.of("category.not-found", slug));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Cacheable(CACHE_NAME)
    public List<CategoryResponse> getRootCategories() {
        // Collecteur en ArrayList (plutôt que Stream#toList) : les implémentations
        // immuables du JDK (java.util.ImmutableCollections$ListN) ne se désérialisent
        // pas correctement via le typage polymorphique Jackson utilisé par le cache Redis.
        return categoryRepository.findByParentIsNullAndActiveTrueOrderByDisplayOrderAsc().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Page<CategoryResponse> search(CategorySearchRequest request, Pageable pageable) {
        return categoryRepository.findAll(CategorySpecifications.from(request), pageable)
                .map(categoryMapper::toResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public CategoryResponse create(CategoryCreateRequest request) {
        String slug = generateUniqueSlug(request.getName());

        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .icon(request.getIcon())
                .image(request.getImage())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();

        if (request.getParentId() != null) {
            category.setParent(getEntityById(request.getParentId()));
        }

        Category saved = categoryRepository.save(category);
        log.info("Category created: {}", saved.getSlug());
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public CategoryResponse update(UUID id, CategoryUpdateRequest request) {
        Category category = getEntityById(id);

        if (StringUtils.hasText(request.getName()) && !request.getName().equals(category.getName())) {
            category.setName(request.getName());
            category.setSlug(generateUniqueSlug(request.getName()));
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getImage() != null) {
            category.setImage(request.getImage());
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new ConflictException("error.validation");
            }
            category.setParent(getEntityById(request.getParentId()));
        }

        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void delete(UUID id) {
        Category category = getEntityById(id);
        category.markDeleted();
        categoryRepository.save(category);
        log.info("Category {} soft-deleted", id);
    }

    private String generateUniqueSlug(String name) {
        String base = SlugUtil.toSlug(name);
        String candidate = base;
        int suffix = 1;
        while (categoryRepository.existsBySlug(candidate)) {
            candidate = base + "-" + (++suffix);
        }
        return candidate;
    }
}
