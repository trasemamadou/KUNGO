export type BookingStatus =
  | 'PENDING'
  | 'NEGOTIATING'
  | 'ACCEPTED'
  | 'REJECTED'
  | 'PAID'
  | 'PREPARING'
  | 'EN_ROUTE'
  | 'ARRIVED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'PAYOUT_SETTLED'
  | 'CANCELLED';

export interface Booking {
  id: string;
  clientId: string;
  clientName: string;
  clientAvatarUrl?: string;
  professionalId: string;
  professionalName: string;
  professionalAvatarUrl?: string;
  serviceId: string;
  serviceTitle: string;
  eventType?: string;
  eventDate: string;
  eventAddress?: string;
  estimatedDurationMinutes?: number;
  participantCount?: number;
  status: BookingStatus;
  description?: string;
  specialRequests?: string;
  professionalMessage?: string;
  totalPrice: number;
  currency: string;
  commissionAmount?: number;
  professionalAmount?: number;
  clientConfirmedAt?: string;
  professionalConfirmedAt?: string;
  createdAt: string;
}

export interface BookingStatusHistoryEntry {
  fromStatus?: BookingStatus;
  toStatus: BookingStatus;
  comment?: string;
  changedAt: string;
}

export interface BookingCreateRequest {
  serviceId: string;
  eventType?: string;
  eventDate: string;
  eventAddress?: string;
  estimatedDurationMinutes?: number;
  participantCount?: number;
  proposedBudget?: number;
  description?: string;
  specialRequests?: string;
}

export type ProposalStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'SUPERSEDED';

export interface BookingProposal {
  id: string;
  proposedById: string;
  proposedByName: string;
  proposedByClient: boolean;
  proposedPrice?: number;
  proposedPriceCurrency?: string;
  proposedEventDate?: string;
  proposedDurationMinutes?: number;
  proposedConditions?: string;
  message?: string;
  status: ProposalStatus;
  createdAt: string;
}

export interface BookingProposalRequest {
  price?: number;
  eventDate?: string;
  durationMinutes?: number;
  conditions?: string;
  message?: string;
}

export interface BookingAttachment {
  id: string;
  fileName: string;
  url: string;
  uploadedById: string;
  createdAt: string;
}

export interface ReasonRequest {
  reason?: string;
}

export type BookingDocumentType = 'CONTRACT' | 'RECEIPT';

export interface BookingDocument {
  id: string;
  type: BookingDocumentType;
  documentNumber: string;
  pdfUrl: string;
  generatedAt: string;
}

export const BOOKING_DOCUMENT_TYPE_LABELS: Record<BookingDocumentType, string> = {
  CONTRACT: 'Contrat',
  RECEIPT: 'Reçu',
};

export interface BookingMessage {
  id: string;
  senderId: string;
  senderName: string;
  senderAvatarUrl?: string;
  content?: string;
  attachmentUrl?: string;
  attachmentName?: string;
  read: boolean;
  createdAt: string;
}

export const BOOKING_STATUS_LABELS: Record<BookingStatus, string> = {
  PENDING: 'En attente de réponse',
  NEGOTIATING: 'En négociation',
  ACCEPTED: 'Acceptée',
  REJECTED: 'Refusée',
  PAID: 'Paiement effectué',
  PREPARING: 'En préparation',
  EN_ROUTE: 'Prestataire en route',
  ARRIVED: 'Prestataire arrivé',
  IN_PROGRESS: 'Prestation en cours',
  COMPLETED: 'Terminée',
  PAYOUT_SETTLED: 'Paiement versé',
  CANCELLED: 'Annulée',
};
