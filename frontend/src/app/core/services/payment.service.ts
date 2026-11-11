import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Payment, PaymentRequest } from '../models/payment.model';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private readonly api = inject(ApiService);

  initiate(request: PaymentRequest): Observable<Payment> {
    return this.api.post<Payment>('/payments', request);
  }

  getByBooking(bookingId: string): Observable<Payment> {
    return this.api.get<Payment>(`/payments/booking/${bookingId}`);
  }
}
