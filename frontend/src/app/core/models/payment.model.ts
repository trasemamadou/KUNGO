export type PaymentProvider = 'STRIPE' | 'WAVE' | 'ORANGE_MONEY' | 'MOBILE_MONEY' | 'CARD';
export type PaymentStatus = 'PENDING' | 'SUCCEEDED' | 'FAILED' | 'REFUNDED';

export interface Payment {
  id: string;
  bookingId: string;
  provider: PaymentProvider;
  amount: number;
  currency: string;
  status: PaymentStatus;
  transactionRef?: string;
  clientSecret?: string;
  invoiceUrl?: string;
  paidAt?: string;
}

export interface PaymentRequest {
  bookingId: string;
  provider: PaymentProvider;
}

export const PAYMENT_PROVIDER_LABELS: Record<PaymentProvider, string> = {
  STRIPE: 'Carte bancaire (Stripe)',
  CARD: 'Carte bancaire',
  WAVE: 'Wave',
  ORANGE_MONEY: 'Orange Money',
  MOBILE_MONEY: 'Mobile Money',
};
