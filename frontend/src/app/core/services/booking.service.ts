import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Booking,
  BookingAttachment,
  BookingCreateRequest,
  BookingDocument,
  BookingMessage,
  BookingProposal,
  BookingProposalRequest,
  BookingStatusHistoryEntry,
  ReasonRequest,
} from '../models/booking.model';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class BookingService {
  private readonly api = inject(ApiService);

  create(request: BookingCreateRequest): Observable<Booking> {
    return this.api.post<Booking>('/bookings', request);
  }

  getById(id: string): Observable<Booking> {
    return this.api.get<Booking>(`/bookings/${id}`);
  }

  getHistory(id: string): Observable<BookingStatusHistoryEntry[]> {
    return this.api.get<BookingStatusHistoryEntry[]>(`/bookings/${id}/history`);
  }

  listForCurrentClient(): Observable<Booking[]> {
    return this.api.get<Booking[]>('/bookings/me');
  }

  listForProfessional(professionalId: string): Observable<Booking[]> {
    return this.api.get<Booking[]>(`/bookings/professional/${professionalId}`);
  }

  accept(id: string): Observable<Booking> {
    return this.api.patch<Booking>(`/bookings/${id}/accept`);
  }

  reject(id: string, request: ReasonRequest): Observable<Booking> {
    return this.api.patch<Booking>(`/bookings/${id}/reject`, request);
  }

  // ---- Négociation ----

  listProposals(id: string): Observable<BookingProposal[]> {
    return this.api.get<BookingProposal[]>(`/bookings/${id}/proposals`);
  }

  submitProposal(id: string, request: BookingProposalRequest): Observable<BookingProposal> {
    return this.api.post<BookingProposal>(`/bookings/${id}/proposals`, request);
  }

  acceptProposal(id: string, proposalId: string): Observable<Booking> {
    return this.api.patch<Booking>(`/bookings/${id}/proposals/${proposalId}/accept`);
  }

  rejectProposal(id: string, proposalId: string): Observable<Booking> {
    return this.api.patch<Booking>(`/bookings/${id}/proposals/${proposalId}/reject`);
  }

  // ---- Suivi de la prestation ----

  prepare(id: string): Observable<Booking> {
    return this.api.patch<Booking>(`/bookings/${id}/prepare`);
  }

  startTravel(id: string): Observable<Booking> {
    return this.api.patch<Booking>(`/bookings/${id}/start-travel`);
  }

  markArrived(id: string): Observable<Booking> {
    return this.api.patch<Booking>(`/bookings/${id}/mark-arrived`);
  }

  start(id: string): Observable<Booking> {
    return this.api.patch<Booking>(`/bookings/${id}/start`);
  }

  confirmCompletion(id: string): Observable<Booking> {
    return this.api.patch<Booking>(`/bookings/${id}/confirm-completion`);
  }

  cancel(id: string, request: ReasonRequest): Observable<Booking> {
    return this.api.patch<Booking>(`/bookings/${id}/cancel`, request);
  }

  // ---- Pièces jointes ----

  listAttachments(id: string): Observable<BookingAttachment[]> {
    return this.api.get<BookingAttachment[]>(`/bookings/${id}/attachments`);
  }

  uploadAttachment(id: string, file: File): Observable<BookingAttachment> {
    const formData = new FormData();
    formData.append('file', file);
    return this.api.upload<BookingAttachment>(`/bookings/${id}/attachments`, formData);
  }

  // ---- Documents (contrat, reçu) ----

  listDocuments(id: string): Observable<BookingDocument[]> {
    return this.api.get<BookingDocument[]>(`/bookings/${id}/documents`);
  }

  // ---- Messagerie ----

  listMessages(id: string): Observable<BookingMessage[]> {
    return this.api.get<BookingMessage[]>(`/bookings/${id}/messages`);
  }

  sendMessage(id: string, content?: string, file?: File): Observable<BookingMessage> {
    const formData = new FormData();
    if (content) formData.append('content', content);
    if (file) formData.append('file', file);
    return this.api.upload<BookingMessage>(`/bookings/${id}/messages`, formData);
  }

  unreadMessageCount(id: string): Observable<number> {
    return this.api.get<number>(`/bookings/${id}/messages/unread-count`);
  }
}
