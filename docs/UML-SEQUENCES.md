# Diagrammes de séquence — flux principaux

## 1. Inscription + activation par email

```mermaid
sequenceDiagram
    actor U as Utilisateur
    participant FE as Angular
    participant AC as AuthController
    participant AS as AuthService
    participant UR as UserRepository
    participant MS as MailService
    participant DB as PostgreSQL

    U->>FE: Remplit formulaire d'inscription
    FE->>AC: POST /api/v1/auth/register
    AC->>AS: register(RegisterRequest)
    AS->>UR: existsByEmail(email)
    UR->>DB: SELECT
    DB-->>UR: false
    AS->>AS: hash password (BCrypt)
    AS->>UR: save(User status=PENDING)
    UR->>DB: INSERT
    AS->>AS: generate ActivationToken
    AS->>MS: sendActivationEmail(user, token)
    MS-->>U: Email "Activez votre compte"
    AS-->>AC: RegisterResponse
    AC-->>FE: 201 Created
    U->>FE: Clique lien d'activation
    FE->>AC: GET /api/v1/auth/activate?token=...
    AC->>AS: activate(token)
    AS->>UR: findByActivationToken(token)
    AS->>AS: valider expiration
    AS->>UR: user.status = ACTIVE, save
    AC-->>FE: 200 OK "Compte activé"
```

## 2. Connexion (JWT + refresh token)

```mermaid
sequenceDiagram
    actor U as Utilisateur
    participant FE as Angular
    participant AC as AuthController
    participant AM as AuthenticationManager
    participant JWT as JwtProvider
    participant RT as RefreshTokenService

    U->>FE: email + mot de passe
    FE->>AC: POST /api/v1/auth/login
    AC->>AM: authenticate(credentials)
    AM-->>AC: Authentication OK
    AC->>JWT: generateAccessToken(user)
    AC->>RT: createRefreshToken(user)
    AC-->>FE: 200 { accessToken, refreshToken, expiresIn }
    FE->>FE: Stocke tokens (memory + httpOnly cookie refresh)
    Note over FE,AC: Requêtes suivantes → Authorization: Bearer accessToken
    FE->>AC: POST /api/v1/auth/refresh (accessToken expiré)
    AC->>RT: validate(refreshToken)
    RT-->>AC: OK
    AC->>JWT: generateAccessToken(user)
    AC-->>FE: 200 { accessToken }
```

## 3. Recherche + réservation + acceptation

```mermaid
sequenceDiagram
    actor C as Client
    participant FE as Angular
    participant SC as SearchController
    participant SS as SearchService
    participant SPEC as ServiceSpecification
    participant BC as BookingController
    participant BS as BookingService
    participant EVT as ApplicationEventPublisher
    participant NOT as NotificationListener
    actor P as Professionnel

    C->>FE: Recherche "DJ Dakar, budget < 500 000, dispo samedi"
    FE->>SC: GET /api/v1/services/search?filters...
    SC->>SS: search(SearchRequest, Pageable)
    SS->>SPEC: build(criteria dynamiques)
    SPEC-->>SS: Specification<EventService>
    SS-->>SC: Page<ServiceResponse>
    SC-->>FE: 200 OK (résultats paginés)
    C->>FE: Consulte profil, envoie demande
    FE->>BC: POST /api/v1/bookings
    BC->>BS: createBooking(request, clientId)
    BS->>BS: valider disponibilité du service
    BS->>BS: Booking.status = PENDING
    BS->>EVT: publish(BookingCreatedEvent)
    EVT->>NOT: onBookingCreated()
    NOT-->>P: Notification (email + WebSocket)
    BC-->>FE: 201 Created

    P->>FE: Ouvre la demande
    FE->>BC: PATCH /api/v1/bookings/{id}/accept
    BC->>BS: accept(bookingId, professionalId)
    BS->>BS: booking.accept() [ACCEPTED]
    BS->>EVT: publish(BookingAcceptedEvent)
    EVT->>NOT: notify(client)
    BC-->>FE: 200 OK
```

## 4. Paiement + commission + reversement

```mermaid
sequenceDiagram
    actor C as Client
    participant FE as Angular
    participant PC as PaymentController
    participant PS as PaymentService
    participant STRAT as PaymentStrategy (Stripe/Wave/OM)
    participant CC as CommissionCalculator
    participant BS as BookingService
    participant INV as InvoiceService
    participant EVT as ApplicationEventPublisher

    C->>FE: Valide et paie la réservation ACCEPTED
    FE->>PC: POST /api/v1/payments {bookingId, provider}
    PC->>PS: pay(bookingId, provider)
    PS->>STRAT: charge(booking)
    STRAT-->>PS: PaymentResult(SUCCEEDED, transactionRef)
    PS->>CC: calculate(booking.totalPrice, category)
    CC-->>PS: commissionAmount
    PS->>PS: professionalAmount = totalPrice - commissionAmount
    PS->>BS: booking.confirmPayment() [PAID]
    PS->>INV: generateInvoice(payment) → PDF
    PS->>EVT: publish(PaymentSucceededEvent)
    EVT-->>C: Notification "Paiement confirmé"
    EVT-->>PC: Notification pro "Réservation payée"
    PC-->>FE: 200 OK { invoiceUrl }

    Note over PS: Le reversement (Payout) au professionnel est déclenché\npar un job planifié (Spring Scheduler) après la fin de prestation,\nou immédiat selon la politique retenue (T+2 jours).
```

## 5. Fin de prestation + avis bidirectionnel

```mermaid
sequenceDiagram
    participant SCHED as BookingScheduler
    participant BS as BookingService
    actor C as Client
    actor P as Professionnel
    participant RC as ReviewController
    participant RS as ReviewService
    participant PP as ProfessionalProfile

    SCHED->>BS: findBookingsPastEventDate(status=PAID)
    BS->>BS: booking.complete() [COMPLETED]
    BS-->>C: Notification "Notez votre expérience"
    BS-->>P: Notification "Notez votre client"

    C->>RC: POST /api/v1/reviews {bookingId, rating, comment}
    RC->>RS: createReview(request, authorId)
    RS->>RS: valider booking.status == COMPLETED
    RS->>PP: recalculateRating()
    RC-->>C: 201 Created

    P->>RC: POST /api/v1/reviews {bookingId, rating, comment, target=client}
    RC->>RS: createReview(...)
    RS-->>P: 201 Created
```
