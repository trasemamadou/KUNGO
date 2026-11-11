export type NotificationType =
  | 'BOOKING_CREATED'
  | 'BOOKING_ACCEPTED'
  | 'BOOKING_REJECTED'
  | 'BOOKING_DATE_PROPOSED'
  | 'BOOKING_CONFIRMED'
  | 'BOOKING_CANCELLED'
  | 'BOOKING_COMPLETED'
  | 'PAYMENT_SUCCEEDED'
  | 'REVIEW_RECEIVED'
  | 'ACCOUNT_VERIFIED'
  | 'NEW_MESSAGE'
  | 'GENERIC';

export interface AppNotification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  link?: string;
  read: boolean;
  sentAt: string;
}
