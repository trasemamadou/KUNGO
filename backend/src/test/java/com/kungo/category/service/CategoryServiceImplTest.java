package com.kungo.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kungo.category.Category;
import com.kungo.category.CategoryCreateRequest;
import com.kungo.category.CategoryMapper;
import com.kungo.category.CategoryRepository;
import com.kungo.category.CategoryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService")
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("create() génère un slug URL-friendly à partir du nom")
    void shouldGenerateSlugOnCreate() {
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("Musique & Live")
                .build();
        when(categoryRepository.existsBySlug("musique-live")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        categoryService.create(request);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getSlug()).isEqualTo("musique-live");
    }

    @Test
    @DisplayName("create() désambiguïse le slug en cas de collision")
    void shouldDisambiguateSlugOnCollision() {
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("DJ")
                .build();
        when(categoryRepository.existsBySlug("dj")).thenReturn(true);
        when(categoryRepository.existsBySlug("dj-2")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        categoryService.create(request);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getSlug()).isEqualTo("dj-2");
    }
}
