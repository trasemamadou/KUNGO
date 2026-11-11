package com.kungo.professional.controller;

import com.kungo.common.response.ApiResponse;
import com.kungo.professional.domain.entity.Language;
import com.kungo.professional.dto.response.LanguageResponse;
import com.kungo.professional.mapper.ProfessionalMapper;
import com.kungo.professional.repository.LanguageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/languages")
@RequiredArgsConstructor
@Tag(name = "Langues")
public class LanguageController {

    private final LanguageRepository languageRepository;
    private final ProfessionalMapper professionalMapper;

    @GetMapping
    @Operation(summary = "Lister toutes les langues disponibles")
    public ApiResponse<List<LanguageResponse>> list() {
        return ApiResponse.success(languageRepository.findAll().stream().map(professionalMapper::toResponse).toList());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une langue (admin)")
    public ApiResponse<LanguageResponse> create(@RequestParam @NotBlank String code, @RequestParam @NotBlank String name) {
        Language saved = languageRepository.save(Language.builder().code(code).name(name).build());
        return ApiResponse.success(professionalMapper.toResponse(saved));
    }
}
