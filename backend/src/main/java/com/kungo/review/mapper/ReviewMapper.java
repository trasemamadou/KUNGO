package com.kungo.review.mapper;

import com.kungo.review.domain.entity.Report;
import com.kungo.review.domain.entity.Review;
import com.kungo.review.dto.response.ReportResponse;
import com.kungo.review.dto.response.ReviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(source = "booking.id", target = "bookingId")
    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "author.fullName", target = "authorName")
    @Mapping(source = "author.avatarUrl", target = "authorAvatarUrl")
    @Mapping(source = "target.id", target = "targetId")
    ReviewResponse toResponse(Review review);

    @Mapping(source = "reporter.id", target = "reporterId")
    @Mapping(source = "reporter.fullName", target = "reporterName")
    @Mapping(source = "reportedUser.id", target = "reportedUserId")
    @Mapping(source = "reportedReview.id", target = "reportedReviewId")
    ReportResponse toResponse(Report report);
}
