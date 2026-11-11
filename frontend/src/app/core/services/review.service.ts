import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/api-response.model';
import { Report } from '../models/report.model';
import { Review, ReviewCreateRequest, ReviewResponseRequest } from '../models/review.model';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private readonly api = inject(ApiService);

  create(request: ReviewCreateRequest): Observable<Review> {
    return this.api.post<Review>('/reviews', request);
  }

  respond(id: string, request: ReviewResponseRequest): Observable<Review> {
    return this.api.patch<Review>(`/reviews/${id}/respond`, request);
  }

  listForTarget(targetId: string, page = 0, size = 10): Observable<PageResponse<Review>> {
    return this.api.get<PageResponse<Review>>(`/reviews/target/${targetId}`, { page, size });
  }

  listForBooking(bookingId: string): Observable<Review[]> {
    return this.api.get<Review[]>(`/reviews/booking/${bookingId}`);
  }

  report(reportedUserId: string | undefined, reportedReviewId: string | undefined, reason: string): Observable<Report> {
    return this.api.post<Report>('/reviews/reports', { reportedUserId, reportedReviewId, reason });
  }

  listReports(page = 0, size = 20): Observable<PageResponse<Report>> {
    return this.api.get<PageResponse<Report>>('/reviews/reports', { page, size });
  }

  dismissReport(id: string): Observable<void> {
    return this.api.patch<void>(`/reviews/reports/${id}/dismiss`);
  }

  resolveReport(id: string): Observable<void> {
    return this.api.patch<void>(`/reviews/reports/${id}/resolve`);
  }
}
