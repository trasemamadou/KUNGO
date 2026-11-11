package com.kungo.review.repository;

import com.kungo.review.domain.entity.Review;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByTargetIdAndModerationStatus(UUID targetId, com.kungo.review.domain.enums.ModerationStatus status, Pageable pageable);

    Optional<Review> findByBookingIdAndAuthorId(UUID bookingId, UUID authorId);

    List<Review> findByBookingId(UUID bookingId);
}
