package com.kungo.favorite;

import java.util.List;
import java.util.UUID;

public interface FavoriteService {

    List<FavoriteResponse> listForCurrentClient();

    FavoriteResponse add(UUID professionalId);

    void remove(UUID professionalId);

    boolean isFavorite(UUID professionalId);
}
