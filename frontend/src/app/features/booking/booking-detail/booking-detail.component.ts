import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { MatRadioModule } from '@angular/material/radio';
import { AuthService } from '../../../core/auth/auth.service';
import {
  Booking,
  BookingAttachment,
  BookingDocument,
  BookingMessage,
  BookingProposal,
  BookingStatusHistoryEntry,
  BOOKING_DOCUMENT_TYPE_LABELS,
  BOOKING_STATUS_LABELS,
} from '../../../core/models/booking.model';
import { PaymentProvider, PAYMENT_PROVIDER_LABELS } from '../../../core/models/payment.model';
import { BookingService } from '../../../core/services/booking.service';
import { PaymentService } from '../../../core/services/payment.service';
import { ReviewCriterion, REVIEW_CRITERIA } from '../../../core/models/review.model';
import { ReviewService } from '../../../core/services/review.service';
import { RatingStarsComponent } from '../../../shared/components/rating-stars/rating-stars.component';
import { SkeletonComponent } from '../../../shared/components/skeleton/skeleton.component';
import { ToastService } from '../../../shared/components/toast/toast.service';

const NEGOTIATION_OPEN_STATUSES: Booking['status'][] = ['PENDING', 'NEGOTIATING'];
const POST_PAYMENT_ACTIVE_STATUSES: Booking['status'][] = ['PAID', 'PREPARING', 'EN_ROUTE', 'ARRIVED', 'IN_PROGRESS'];
const CANCELLABLE_STATUSES: Booking['status'][] = ['PENDING', 'NEGOTIATING', 'ACCEPTED', 'PAID', 'PREPARING', 'EN_ROUTE', 'ARRIVED'];
const REVIEWABLE_STATUSES: Booking['status'][] = ['COMPLETED', 'PAYOUT_SETTLED'];
const MESSAGING_CLOSED_STATUSES: Booking['status'][] = ['REJECTED', 'CANCELLED'];
const PAYMENT_ISSUED_STATUSES: Booking['status'][] = [
  'PAID',
  'PREPARING',
  'EN_ROUTE',
  'ARRIVED',
  'IN_PROGRESS',
  'COMPLETED',
  'PAYOUT_SETTLED',
];

@Component({
  selector: 'app-booking-detail',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatRadioModule,
    RatingStarsComponent,
    SkeletonComponent,
  ],
  template: `
    @if (loading()) {
      <div class="mx-auto max-w-3xl px-4 py-8"><app-skeleton height="300px" /></div>
    } @else if (booking(); as booking) {
      <div class="mx-auto max-w-3xl px-4 py-8 sm:px-6">
        <div class="mb-6 flex items-center justify-between">
          <h1 class="text-xl font-bold text-gray-900 dark:text-gray-100">{{ booking.serviceTitle }}</h1>
          <span class="rounded-full bg-brand-100 px-3 py-1 text-xs font-semibold text-brand-700 dark:bg-brand-900 dark:text-brand-300">
            {{ statusLabels[booking.status] }}
          </span>
        </div>

        <div class="grid gap-6 rounded-xl border border-gray-200 p-6 dark:border-gray-800 sm:grid-cols-2">
          <div>
            <p class="text-xs uppercase text-gray-400">Client</p>
            <p class="font-medium text-gray-900 dark:text-gray-100">{{ booking.clientName }}</p>
          </div>
          <div>
            <p class="text-xs uppercase text-gray-400">Professionnel</p>
            <p class="font-medium text-gray-900 dark:text-gray-100">{{ booking.professionalName }}</p>
          </div>
          @if (booking.eventType) {
            <div>
              <p class="text-xs uppercase text-gray-400">Type d'événement</p>
              <p class="font-medium text-gray-900 dark:text-gray-100">{{ booking.eventType }}</p>
            </div>
          }
          <div>
            <p class="text-xs uppercase text-gray-400">Date de l'événement</p>
            <p class="font-medium text-gray-900 dark:text-gray-100">{{ booking.eventDate | date: 'dd MMMM yyyy, HH:mm' }}</p>
          </div>
          @if (booking.estimatedDurationMinutes) {
            <div>
              <p class="text-xs uppercase text-gray-400">Durée estimée</p>
              <p class="font-medium text-gray-900 dark:text-gray-100">{{ booking.estimatedDurationMinutes }} min</p>
            </div>
          }
          @if (booking.participantCount) {
            <div>
              <p class="text-xs uppercase text-gray-400">Nombre de participants</p>
              <p class="font-medium text-gray-900 dark:text-gray-100">{{ booking.participantCount }}</p>
            </div>
          }
          <div>
            <p class="text-xs uppercase text-gray-400">Montant</p>
            <p class="font-medium text-gray-900 dark:text-gray-100">{{ booking.totalPrice }} {{ booking.currency }}</p>
          </div>
          @if (booking.eventAddress) {
            <div class="sm:col-span-2">
              <p class="text-xs uppercase text-gray-400">Adresse</p>
              <p class="font-medium text-gray-900 dark:text-gray-100">{{ booking.eventAddress }}</p>
            </div>
          }
          @if (booking.description) {
            <div class="sm:col-span-2">
              <p class="text-xs uppercase text-gray-400">Description</p>
              <p class="font-medium text-gray-900 dark:text-gray-100">{{ booking.description }}</p>
            </div>
          }
          @if (booking.specialRequests) {
            <div class="sm:col-span-2">
              <p class="text-xs uppercase text-gray-400">Demandes particulières</p>
              <p class="font-medium text-gray-900 dark:text-gray-100">{{ booking.specialRequests }}</p>
            </div>
          }
          @if (booking.professionalMessage) {
            <div class="sm:col-span-2 rounded-md bg-amber-50 p-3 dark:bg-amber-950">
              <p class="text-sm text-amber-800 dark:text-amber-300">{{ booking.professionalMessage }}</p>
            </div>
          }
        </div>

        <!-- Messagerie -->
        @if (messagingAvailable()) {
          <div class="mt-6 rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <h2 class="mb-3 font-semibold text-gray-900 dark:text-gray-100">Messages</h2>
            <div class="flex max-h-80 flex-col gap-3 overflow-y-auto">
              @for (message of messages(); track message.id) {
                <div class="flex flex-col" [class.items-end]="isOwnMessage(message)">
                  <div
                    class="max-w-[80%] rounded-lg px-3 py-2 text-sm"
                    [class.bg-brand-600]="isOwnMessage(message)"
                    [class.text-white]="isOwnMessage(message)"
                    [class.bg-gray-100]="!isOwnMessage(message)"
                    [class.dark:bg-gray-800]="!isOwnMessage(message)"
                    [class.text-gray-800]="!isOwnMessage(message)"
                    [class.dark:text-gray-200]="!isOwnMessage(message)"
                  >
                    @if (message.content) {
                      <p>{{ message.content }}</p>
                    }
                    @if (message.attachmentUrl) {
                      <a [href]="message.attachmentUrl" target="_blank" class="block underline">{{ message.attachmentName }}</a>
                    }
                  </div>
                  <span class="mt-0.5 text-xs text-gray-400">{{ message.senderName }} — {{ message.createdAt | date: 'dd/MM HH:mm' }}</span>
                </div>
              } @empty {
                <p class="text-sm text-gray-500">Aucun message pour le moment.</p>
              }
            </div>
            <div class="mt-3 flex items-end gap-2">
              <textarea
                class="flex-1 rounded-md border border-gray-300 p-2 text-sm dark:border-gray-700 dark:bg-gray-800"
                rows="2"
                placeholder="Écrire un message..."
                [(ngModel)]="newMessageContent"
              ></textarea>
              <input #messageFileInput type="file" class="hidden" (change)="onMessageFileSelected($event)" />
              <button mat-icon-button type="button" (click)="messageFileInput.click()" aria-label="Joindre un fichier">
                <mat-icon>attach_file</mat-icon>
              </button>
              <button mat-flat-button color="primary" [disabled]="!newMessageContent.trim()" (click)="sendMessage()">Envoyer</button>
            </div>
          </div>
        }

        <!-- Réponse initiale du professionnel -->
        @if (isProfessional() && booking.status === 'PENDING') {
          <div class="mt-6 flex flex-wrap gap-2">
            <button mat-flat-button color="primary" (click)="accept()">Accepter</button>
            <button mat-stroked-button color="warn" (click)="reject()">Refuser</button>
          </div>
        }

        <!-- Négociation -->
        @if (negotiationOpen(); as open) {
          @if (open) {
            <div class="mt-8">
              <h2 class="mb-3 font-semibold text-gray-900 dark:text-gray-100">Négociation</h2>

              @if (proposals().length > 0) {
                <ol class="flex flex-col gap-3">
                  @for (proposal of proposals(); track proposal.id) {
                    <li class="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
                      <div class="flex items-center justify-between">
                        <p class="text-sm font-semibold text-gray-900 dark:text-gray-100">
                          Proposition de {{ proposal.proposedByName }}
                        </p>
                        <span class="text-xs text-gray-400">{{ proposal.createdAt | date: 'dd/MM HH:mm' }}</span>
                      </div>
                      <div class="mt-2 flex flex-col gap-1 text-sm text-gray-600 dark:text-gray-400">
                        @if (proposal.proposedPrice) {
                          <span>Prix : {{ proposal.proposedPrice }} {{ proposal.proposedPriceCurrency }}</span>
                        }
                        @if (proposal.proposedEventDate) {
                          <span>Date : {{ proposal.proposedEventDate | date: 'dd MMMM yyyy, HH:mm' }}</span>
                        }
                        @if (proposal.proposedDurationMinutes) {
                          <span>Durée : {{ proposal.proposedDurationMinutes }} min</span>
                        }
                        @if (proposal.proposedConditions) {
                          <span>Conditions : {{ proposal.proposedConditions }}</span>
                        }
                        @if (proposal.message) {
                          <span class="italic">« {{ proposal.message }} »</span>
                        }
                      </div>
                      <div class="mt-2 flex items-center gap-2">
                        <span class="rounded-full bg-gray-100 px-2 py-0.5 text-xs text-gray-600 dark:bg-gray-800 dark:text-gray-300">
                          {{ proposalStatusLabels[proposal.status] }}
                        </span>
                        @if (proposal.status === 'PENDING' && !isOwnProposal(proposal)) {
                          <button mat-stroked-button color="primary" (click)="acceptProposal(proposal.id)">Accepter</button>
                          <button mat-stroked-button color="warn" (click)="rejectProposal(proposal.id)">Refuser</button>
                        }
                      </div>
                    </li>
                  }
                </ol>
              }

              <button mat-stroked-button class="mt-4" (click)="showProposalForm.set(!showProposalForm())">
                Faire une contre-proposition
              </button>

              @if (showProposalForm()) {
                <form [formGroup]="proposalForm" class="mt-4 flex flex-col gap-3 rounded-xl border border-gray-200 p-4 dark:border-gray-800" (ngSubmit)="submitProposal()">
                  <mat-form-field appearance="outline">
                    <mat-label>Prix proposé</mat-label>
                    <input matInput type="number" formControlName="price" />
                    <span matSuffix>{{ booking.currency }}</span>
                  </mat-form-field>
                  <mat-form-field appearance="outline">
                    <mat-label>Nouvelle date</mat-label>
                    <input matInput [matDatepicker]="picker" formControlName="eventDate" />
                    <mat-datepicker-toggle matSuffix [for]="picker" />
                    <mat-datepicker #picker />
                  </mat-form-field>
                  <mat-form-field appearance="outline">
                    <mat-label>Durée (minutes)</mat-label>
                    <input matInput type="number" formControlName="durationMinutes" />
                  </mat-form-field>
                  <mat-form-field appearance="outline">
                    <mat-label>Conditions</mat-label>
                    <textarea matInput formControlName="conditions" rows="2"></textarea>
                  </mat-form-field>
                  <mat-form-field appearance="outline">
                    <mat-label>Message</mat-label>
                    <textarea matInput formControlName="message" rows="2"></textarea>
                  </mat-form-field>
                  <button mat-flat-button color="primary" type="submit">Envoyer la proposition</button>
                </form>
              }
            </div>
          }
        }

        <!-- Paiement -->
        @if (isClient() && booking.status === 'ACCEPTED') {
          <div class="mt-6 rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <h2 class="mb-3 font-semibold text-gray-900 dark:text-gray-100">Procéder au paiement</h2>
            <mat-radio-group [(ngModel)]="selectedProvider" class="flex flex-col gap-2">
              @for (provider of providers; track provider) {
                <mat-radio-button [value]="provider">{{ providerLabels[provider] }}</mat-radio-button>
              }
            </mat-radio-group>
            <button mat-flat-button color="primary" class="mt-4" (click)="pay()">
              Payer {{ booking.totalPrice }} {{ booking.currency }}
            </button>
          </div>
        }

        <!-- Suivi de la prestation -->
        @if (isProfessional() && postPaymentActive()) {
          <div class="mt-6 rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <h2 class="mb-3 font-semibold text-gray-900 dark:text-gray-100">Suivi de la prestation</h2>
            <div class="flex flex-wrap gap-2">
              @if (canPrepare()) {
                <button mat-stroked-button (click)="prepare()">En préparation</button>
              }
              @if (canStartTravel()) {
                <button mat-stroked-button (click)="startTravel()">En route</button>
              }
              @if (canMarkArrived()) {
                <button mat-stroked-button (click)="markArrived()">Arrivé sur place</button>
              }
              @if (canStart()) {
                <button mat-stroked-button (click)="start()">Démarrer la prestation</button>
              }
            </div>
          </div>
        }

        <!-- Double confirmation de fin de prestation -->
        @if (postPaymentActive()) {
          <div class="mt-6 rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <h2 class="mb-3 font-semibold text-gray-900 dark:text-gray-100">Fin de la prestation</h2>
            <p class="text-sm text-gray-600 dark:text-gray-400">
              Client : {{ booking.clientConfirmedAt ? 'a confirmé la fin de la prestation ✓' : 'confirmation en attente' }}
            </p>
            <p class="text-sm text-gray-600 dark:text-gray-400">
              Professionnel : {{ booking.professionalConfirmedAt ? 'a confirmé la fin de la prestation ✓' : 'confirmation en attente' }}
            </p>
            @if (isClient() && !booking.clientConfirmedAt) {
              <button mat-flat-button color="primary" class="mt-3" (click)="confirmCompletion()">
                Je confirme la fin de la prestation
              </button>
            }
            @if (isProfessional() && !booking.professionalConfirmedAt) {
              <button mat-flat-button color="primary" class="mt-3" (click)="confirmCompletion()">
                Je confirme la fin de la prestation
              </button>
            }
          </div>
        }

        <!-- Pièces jointes -->
        <div class="mt-8">
          <h2 class="mb-3 font-semibold text-gray-900 dark:text-gray-100">Pièces jointes</h2>
          @if (attachments().length > 0) {
            <ul class="flex flex-col gap-2">
              @for (attachment of attachments(); track attachment.id) {
                <li class="text-sm">
                  <a [href]="attachment.url" target="_blank" class="text-brand-600 hover:underline dark:text-brand-400">{{ attachment.fileName }}</a>
                </li>
              }
            </ul>
          } @else {
            <p class="text-sm text-gray-500">Aucune pièce jointe.</p>
          }
          <input #fileInput type="file" class="hidden" (change)="onFileSelected($event)" />
          <button mat-stroked-button class="mt-3" (click)="fileInput.click()">Ajouter un fichier</button>
        </div>

        <!-- Documents -->
        @if (documents().length > 0 || invoiceUrl()) {
          <div class="mt-8">
            <h2 class="mb-3 font-semibold text-gray-900 dark:text-gray-100">Documents</h2>
            <ul class="flex flex-col gap-2">
              @for (document of documents(); track document.id) {
                <li class="text-sm">
                  <a [href]="document.pdfUrl" target="_blank" class="text-brand-600 hover:underline dark:text-brand-400">
                    {{ documentTypeLabels[document.type] }} — {{ document.documentNumber }}
                  </a>
                </li>
              }
              @if (invoiceUrl(); as url) {
                <li class="text-sm">
                  <a [href]="url" target="_blank" class="text-brand-600 hover:underline dark:text-brand-400">Facture</a>
                </li>
              }
            </ul>
          </div>
        }

        <!-- Annulation -->
        @if (cancellable()) {
          <button mat-button color="warn" class="mt-6" (click)="cancel()">Annuler la réservation</button>
        }

        <!-- Avis -->
        @if (reviewable()) {
          <div class="mt-6 rounded-xl border border-gray-200 p-4 dark:border-gray-800">
            <h2 class="mb-3 font-semibold text-gray-900 dark:text-gray-100">Laisser un avis</h2>
            <div class="flex flex-col gap-2">
              @for (criterion of reviewCriteria; track criterion.key) {
                <div class="flex items-center justify-between gap-4">
                  <span class="text-sm text-gray-700 dark:text-gray-300">{{ criterion.label }}</span>
                  <app-rating-stars
                    [rating]="criteriaRatings()[criterion.key]"
                    [interactive]="true"
                    [size]="22"
                    (ratingChange)="setCriterionRating(criterion.key, $event)"
                  />
                </div>
              }
            </div>
            <textarea
              class="mt-3 w-full rounded-md border border-gray-300 p-2 text-sm dark:border-gray-700 dark:bg-gray-800"
              rows="3"
              placeholder="Votre commentaire (optionnel)"
              [(ngModel)]="reviewComment"
            ></textarea>
            <button mat-flat-button color="primary" class="mt-3" [disabled]="!allCriteriaRated()" (click)="submitReview()">
              Envoyer mon avis
            </button>
          </div>
        }

        <!-- Historique -->
        <div class="mt-8">
          <h2 class="mb-3 font-semibold text-gray-900 dark:text-gray-100">Historique</h2>
          <ol class="flex flex-col gap-2 border-l-2 border-gray-200 pl-4 dark:border-gray-700">
            @for (entry of history(); track entry.changedAt) {
              <li class="text-sm text-gray-600 dark:text-gray-400">
                <span class="font-medium text-gray-800 dark:text-gray-200">{{ statusLabels[entry.toStatus] }}</span>
                — {{ entry.changedAt | date: 'dd/MM/yyyy HH:mm' }}
                @if (entry.comment) {
                  <div class="italic">{{ entry.comment }}</div>
                }
              </li>
            }
          </ol>
        </div>
      </div>
    }
  `,
})
export class BookingDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly bookingService = inject(BookingService);
  private readonly paymentService = inject(PaymentService);
  private readonly reviewService = inject(ReviewService);
  private readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  readonly booking = signal<Booking | null>(null);
  readonly history = signal<BookingStatusHistoryEntry[]>([]);
  readonly proposals = signal<BookingProposal[]>([]);
  readonly attachments = signal<BookingAttachment[]>([]);
  readonly documents = signal<BookingDocument[]>([]);
  readonly invoiceUrl = signal<string | null>(null);
  readonly messages = signal<BookingMessage[]>([]);
  newMessageContent = '';
  readonly loading = signal(true);
  readonly showProposalForm = signal(false);
  readonly reviewCriteria = REVIEW_CRITERIA;
  readonly criteriaRatings = signal<Record<ReviewCriterion['key'], number>>({
    punctuality: 0,
    quality: 0,
    communication: 0,
    valueForMoney: 0,
    professionalism: 0,
  });
  readonly allCriteriaRated = computed(() => Object.values(this.criteriaRatings()).every((value) => value > 0));
  reviewComment = '';
  selectedProvider: PaymentProvider = 'STRIPE';

  readonly statusLabels = BOOKING_STATUS_LABELS;
  readonly providerLabels = PAYMENT_PROVIDER_LABELS;
  readonly providers: PaymentProvider[] = ['STRIPE', 'WAVE', 'ORANGE_MONEY', 'MOBILE_MONEY'];
  readonly proposalStatusLabels: Record<string, string> = {
    PENDING: 'En attente',
    ACCEPTED: 'Acceptée',
    REJECTED: 'Refusée',
    SUPERSEDED: 'Remplacée',
  };
  readonly documentTypeLabels = BOOKING_DOCUMENT_TYPE_LABELS;

  readonly isClient = computed(() => this.authService.currentUser()?.id === this.booking()?.clientId);
  readonly isProfessional = computed(() => this.authService.currentUser()?.role === 'PROFESSIONNEL');

  readonly negotiationOpen = computed(() => {
    const status = this.booking()?.status;
    return !!status && NEGOTIATION_OPEN_STATUSES.includes(status);
  });
  readonly postPaymentActive = computed(() => {
    const status = this.booking()?.status;
    return !!status && POST_PAYMENT_ACTIVE_STATUSES.includes(status);
  });
  readonly cancellable = computed(() => {
    const status = this.booking()?.status;
    return !!status && CANCELLABLE_STATUSES.includes(status);
  });
  readonly reviewable = computed(() => {
    const status = this.booking()?.status;
    return !!status && REVIEWABLE_STATUSES.includes(status);
  });
  readonly messagingAvailable = computed(() => {
    const status = this.booking()?.status;
    return !!status && !MESSAGING_CLOSED_STATUSES.includes(status);
  });

  readonly canPrepare = computed(() => this.booking()?.status === 'PAID');
  readonly canStartTravel = computed(() => ['PAID', 'PREPARING'].includes(this.booking()?.status ?? ''));
  readonly canMarkArrived = computed(() => ['PREPARING', 'EN_ROUTE'].includes(this.booking()?.status ?? ''));
  readonly canStart = computed(() => ['PAID', 'PREPARING', 'EN_ROUTE', 'ARRIVED'].includes(this.booking()?.status ?? ''));

  readonly proposalForm = this.fb.nonNullable.group({
    price: [null as number | null],
    eventDate: [null as Date | null],
    durationMinutes: [null as number | null],
    conditions: [''],
    message: [''],
  });

  ngOnInit(): void {
    this.load();
  }

  isOwnProposal(proposal: BookingProposal): boolean {
    return proposal.proposedById === this.authService.currentUser()?.id;
  }

  isOwnMessage(message: BookingMessage): boolean {
    return message.senderId === this.authService.currentUser()?.id;
  }

  private load(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.loading.set(true);
    this.bookingService.getById(id).subscribe({
      next: (booking) => {
        this.booking.set(booking);
        this.loading.set(false);
        this.loadInvoiceIfIssued(booking);
      },
      error: () => this.loading.set(false),
    });
    this.bookingService.getHistory(id).subscribe((history) => this.history.set(history));
    this.bookingService.listProposals(id).subscribe((proposals) => this.proposals.set(proposals));
    this.bookingService.listAttachments(id).subscribe((attachments) => this.attachments.set(attachments));
    this.bookingService.listDocuments(id).subscribe((documents) => this.documents.set(documents));
    this.bookingService.listMessages(id).subscribe((messages) => this.messages.set(messages));
  }

  private loadInvoiceIfIssued(booking: Booking): void {
    if (!PAYMENT_ISSUED_STATUSES.includes(booking.status)) return;
    this.paymentService.getByBooking(booking.id).subscribe({
      next: (payment) => this.invoiceUrl.set(payment.invoiceUrl ?? null),
      error: () => this.invoiceUrl.set(null),
    });
  }

  accept(): void {
    this.bookingService.accept(this.booking()!.id).subscribe(() => this.load());
  }

  reject(): void {
    this.bookingService.reject(this.booking()!.id, {}).subscribe(() => this.load());
  }

  submitProposal(): void {
    const value = this.proposalForm.getRawValue();
    this.bookingService
      .submitProposal(this.booking()!.id, {
        price: value.price ?? undefined,
        eventDate: value.eventDate ? value.eventDate.toISOString() : undefined,
        durationMinutes: value.durationMinutes ?? undefined,
        conditions: value.conditions || undefined,
        message: value.message || undefined,
      })
      .subscribe(() => {
        this.showProposalForm.set(false);
        this.proposalForm.reset();
        this.load();
      });
  }

  acceptProposal(proposalId: string): void {
    this.bookingService.acceptProposal(this.booking()!.id, proposalId).subscribe(() => this.load());
  }

  rejectProposal(proposalId: string): void {
    this.bookingService.rejectProposal(this.booking()!.id, proposalId).subscribe(() => this.load());
  }

  prepare(): void {
    this.bookingService.prepare(this.booking()!.id).subscribe(() => this.load());
  }

  startTravel(): void {
    this.bookingService.startTravel(this.booking()!.id).subscribe(() => this.load());
  }

  markArrived(): void {
    this.bookingService.markArrived(this.booking()!.id).subscribe(() => this.load());
  }

  start(): void {
    this.bookingService.start(this.booking()!.id).subscribe(() => this.load());
  }

  confirmCompletion(): void {
    this.bookingService.confirmCompletion(this.booking()!.id).subscribe(() => {
      this.toastService.success('Confirmation enregistrée.');
      this.load();
    });
  }

  cancel(): void {
    this.bookingService.cancel(this.booking()!.id, {}).subscribe(() => this.load());
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.bookingService.uploadAttachment(this.booking()!.id, file).subscribe(() => {
      input.value = '';
      this.load();
    });
  }

  pay(): void {
    this.paymentService.initiate({ bookingId: this.booking()!.id, provider: this.selectedProvider }).subscribe({
      next: (payment) => {
        if (payment.clientSecret) {
          this.toastService.info('Redirection vers le paiement sécurisé Stripe...');
          // Intégration Stripe.js (confirmCardPayment côté client) à brancher ici avec payment.clientSecret
        } else {
          this.toastService.success('Paiement initié, en attente de confirmation.');
        }
        this.load();
      },
    });
  }

  sendMessage(): void {
    const content = this.newMessageContent.trim();
    if (!content) return;
    this.bookingService.sendMessage(this.booking()!.id, content).subscribe(() => {
      this.newMessageContent = '';
      this.bookingService.listMessages(this.booking()!.id).subscribe((messages) => this.messages.set(messages));
    });
  }

  onMessageFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.bookingService.sendMessage(this.booking()!.id, undefined, file).subscribe(() => {
      input.value = '';
      this.bookingService.listMessages(this.booking()!.id).subscribe((messages) => this.messages.set(messages));
    });
  }

  setCriterionRating(key: ReviewCriterion['key'], value: number): void {
    this.criteriaRatings.update((ratings) => ({ ...ratings, [key]: value }));
  }

  submitReview(): void {
    const ratings = this.criteriaRatings();
    this.reviewService
      .create({
        bookingId: this.booking()!.id,
        punctuality: ratings.punctuality,
        quality: ratings.quality,
        communication: ratings.communication,
        valueForMoney: ratings.valueForMoney,
        professionalism: ratings.professionalism,
        comment: this.reviewComment || undefined,
      })
      .subscribe({
        next: () => this.toastService.success('Merci pour votre avis !'),
      });
  }
}
