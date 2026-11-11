package com.kungo.favorite;

import com.kungo.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
@Tag(name = "Favoris")
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    @Operation(summary = "Lister mes favoris")
    public ApiResponse<List<FavoriteResponse>> list() {
        return ApiResponse.success(favoriteService.listForCurrentClient());
    }

    @PostMapping("/{professionalId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ajouter un professionnel aux favoris")
    public ApiResponse<FavoriteResponse> add(@PathVariable UUID professionalId) {
        return ApiResponse.success(favoriteService.add(professionalId));
    }

    @DeleteMapping("/{professionalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Retirer un professionnel des favoris")
    public void remove(@PathVariable UUID professionalId) {
        favoriteService.remove(professionalId);
    }

    @GetMapping("/{professionalId}/status")
    @Operation(summary = "Vérifier si un professionnel est en favori")
    public ApiResponse<Boolean> isFavorite(@PathVariable UUID professionalId) {
        return ApiResponse.success(favoriteService.isFavorite(professionalId));
    }
}
