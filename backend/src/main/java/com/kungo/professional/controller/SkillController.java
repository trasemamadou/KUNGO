package com.kungo.professional.controller;

import com.kungo.common.exception.ConflictException;
import com.kungo.common.response.ApiResponse;
import com.kungo.professional.domain.entity.Skill;
import com.kungo.professional.dto.response.SkillResponse;
import com.kungo.professional.mapper.ProfessionalMapper;
import com.kungo.professional.repository.SkillRepository;
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
@RequestMapping("/skills")
@RequiredArgsConstructor
@Tag(name = "Compétences")
public class SkillController {

    private final SkillRepository skillRepository;
    private final ProfessionalMapper professionalMapper;

    @GetMapping
    @Operation(summary = "Lister toutes les compétences disponibles")
    public ApiResponse<List<SkillResponse>> list() {
        return ApiResponse.success(skillRepository.findAll().stream().map(professionalMapper::toResponse).toList());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une compétence (admin)")
    public ApiResponse<SkillResponse> create(@RequestParam @NotBlank String name) {
        if (skillRepository.findByNameIgnoreCase(name).isPresent()) {
            throw new ConflictException("error.conflict");
        }
        Skill saved = skillRepository.save(Skill.builder().name(name).build());
        return ApiResponse.success(professionalMapper.toResponse(saved));
    }
}
