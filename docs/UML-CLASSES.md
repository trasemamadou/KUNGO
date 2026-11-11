# Diagramme de classes (domaine principal)

```mermaid
classDiagram
    class BaseEntity {
        <<abstract>>
        -UUID id
        -Instant createdAt
        -Instant updatedAt
        -String createdBy
        -String updatedBy
        -boolean deleted
        -Long version
    }

    class User {
        -String email
        -String passwordHash
        -String phone
        -String firstName
        -String lastName
        -Role role
        -UserStatus status
        -boolean emailVerified
        -boolean twoFactorEnabled
        -String avatarUrl
        -String locale
        -Instant lastLoginAt
        +activate()
        +changePassword(newHash)
        +isActive() boolean
    }

    class Role {
        <<enumeration>>
        SUPER_ADMIN
        ADMIN
        MODERATEUR
        CLIENT
        PROFESSIONNEL
    }

    class ProfessionalProfile {
        -String businessName
        -String biography
        -int yearsExperience
        -Address address
        -VerificationStatus verificationStatus
        -boolean premium
        -Instant premiumExpiresAt
        -BigDecimal averageRating
        -int totalReviews
        -int totalBookings
        +recalculateRating()
        +isVerified() boolean
    }

    class Address {
        <<embeddable>>
        -String country
        -String region
        -String city
        -String street
        -BigDecimal latitude
        -BigDecimal longitude
    }

    class Money {
        <<embeddable>>
        -BigDecimal amount
        -String currency
        +add(Money) Money
        +multiply(BigDecimal) Money
    }

    class Category {
        -String name
        -String slug
        -Category parent
        -boolean active
        -int displayOrder
    }

    class EventService {
        -String title
        -String description
        -PriceType priceType
        -Money price
        -int durationMinutes
        -String conditions
        -ServiceStatus status
        +publish()
        +archive()
    }

    class Availability {
        -LocalDate specificDate
        -DayOfWeek dayOfWeek
        -LocalTime startTime
        -LocalTime endTime
        -boolean recurring
        -boolean blocked
        +overlaps(Availability) boolean
    }

    class Booking {
        -LocalDateTime eventDate
        -String eventAddress
        -BookingStatus status
        -LocalDateTime proposedDate
        -Money totalPrice
        -Money commissionAmount
        -Money professionalAmount
        +accept()
        +reject(reason)
        +proposeNewDate(date)
        +confirmPayment()
        +cancel(actor, reason)
        +complete()
    }

    class BookingStatus {
        <<enumeration>>
        PENDING
        ACCEPTED
        REJECTED
        DATE_PROPOSED
        CONFIRMED
        PAID
        IN_PROGRESS
        COMPLETED
        CANCELLED
    }

    class Payment {
        -PaymentProvider provider
        -Money amount
        -PaymentStatus status
        -String transactionRef
        +markSucceeded(ref)
        +markFailed(reason)
        +refund()
    }

    class PaymentStrategy {
        <<interface>>
        +charge(Booking) PaymentResult
        +refund(Payment) void
    }
    class StripePaymentStrategy
    class WavePaymentStrategy
    class OrangeMoneyPaymentStrategy

    class CommissionCalculator {
        <<interface>>
        +calculate(Money base, Category category) Money
    }
    class PercentageCommissionCalculator

    class Review {
        -int rating
        -String comment
        -String professionalResponse
        -ModerationStatus moderationStatus
        +approve()
        +reject()
    }

    class Favorite

    class Notification {
        -NotificationType type
        -String title
        -String message
        -boolean read
        -NotificationChannel channel
    }

    class NotificationFactory {
        <<interface>>
        +create(User target, NotificationType type, Map data) Notification
    }

    BaseEntity <|-- User
    BaseEntity <|-- ProfessionalProfile
    BaseEntity <|-- Category
    BaseEntity <|-- EventService
    BaseEntity <|-- Availability
    BaseEntity <|-- Booking
    BaseEntity <|-- Payment
    BaseEntity <|-- Review
    BaseEntity <|-- Favorite
    BaseEntity <|-- Notification

    User "1" --> "0..1" ProfessionalProfile : possède
    User "1" o-- "*" Booking : réserve (client)
    User --> Role
    ProfessionalProfile --> Address
    ProfessionalProfile "1" --> "*" EventService : propose
    ProfessionalProfile "1" --> "*" Availability : définit
    ProfessionalProfile "1" --> "*" Booking : reçoit
    Category "1" --> "*" Category : sous-catégories
    Category "1" --> "*" EventService : classe
    EventService --> Money
    EventService "1" --> "*" Booking
    Booking --> BookingStatus
    Booking --> Money
    Booking "1" --> "0..1" Payment
    Booking "1" --> "*" Review
    Payment ..> PaymentStrategy : utilise
    PaymentStrategy <|.. StripePaymentStrategy
    PaymentStrategy <|.. WavePaymentStrategy
    PaymentStrategy <|.. OrangeMoneyPaymentStrategy
    Payment ..> CommissionCalculator : utilise
    CommissionCalculator <|.. PercentageCommissionCalculator
    Notification ..> NotificationFactory : créé par
    User "1" --> "*" Notification
    User "1" --> "*" Favorite
    ProfessionalProfile "1" --> "*" Favorite
```

## Patterns appliqués visibles sur ce diagramme

- **Strategy** : `PaymentStrategy` (Stripe / Wave / Orange Money / Mobile Money) et
  `CommissionCalculator` (pourcentage global vs override par catégorie).
- **Factory** : `NotificationFactory` construit le bon type de notification/canal.
- **Value Object (DDD)** : `Money`, `Address` en `@Embeddable`, immuables.
- **Aggregate Root (DDD)** : `Booking` est racine d'agrégat pour `BookingStatusHistory`
  et référence `Payment` en 1-0..1 (invariant : un seul paiement actif par réservation).
- **State Machine** : `BookingStatus` avec transitions contrôlées dans les méthodes
  métier de `Booking` (`accept()`, `reject()`, `cancel()`...) — jamais de `setStatus()` public.
