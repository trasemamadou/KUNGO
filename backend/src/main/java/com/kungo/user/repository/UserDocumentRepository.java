package com.kungo.user.repository;

import com.kungo.user.domain.entity.UserDocument;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDocumentRepository extends JpaRepository<UserDocument, UUID> {

    List<UserDocument> findByUserId(UUID userId);
}
