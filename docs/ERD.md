# Modèle de données (ERD)

Toutes les tables héritent implicitement des colonnes d'audit de `BaseEntity` :
`id (UUID)`, `created_at`, `updated_at`, `created_by`, `updated_by`, `deleted (soft delete)`, `version`.
Elles ne sont pas répétées sur chaque entité ci-dessous pour la lisibilité.

```mermaid
erDiagram
    USERS ||--o| PROFESSIONAL_PROFILES : "possède (si role=PROFESSIONNEL)"
    USERS ||--o{ REFRESH_TOKENS : possede
    USERS ||--o{ NOTIFICATIONS : recoit
    USERS ||--o{ FAVORITES : "ajoute (client)"
    USERS ||--o{ BOOKINGS : "reserve (client)"
    USERS ||--o{ REVIEWS : redige
    USERS ||--o{ REPORTS : signale
    USERS ||--o{ USER_DOCUMENTS : possede
    USERS ||--o{ AUDIT_LOGS : declenche

    PROFESSIONAL_PROFILES ||--o{ SERVICES : propose
    PROFESSIONAL_PROFILES ||--o{ AVAILABILITIES : definit
    PROFESSIONAL_PROFILES }o--o{ SKILLS : possede
    PROFESSIONAL_PROFILES }o--o{ LANGUAGES : parle
    PROFESSIONAL_PROFILES ||--o{ SOCIAL_LINKS : affiche
    PROFESSIONAL_PROFILES ||--o{ PROFESSIONAL_MEDIA : publie
    PROFESSIONAL_PROFILES ||--o{ BOOKINGS : recoit
    PROFESSIONAL_PROFILES ||--o{ PAYOUTS : recoit
    PROFESSIONAL_PROFILES }o--|| ADDRESSES : localise

    CATEGORIES ||--o{ CATEGORIES : "sous-categories"
    CATEGORIES ||--o{ SERVICES : classe
    CATEGORIES ||--o{ COMMISSION_RULES : "override %"

    SERVICES ||--o{ SERVICE_MEDIA : illustre
    SERVICES ||--o{ BOOKINGS : "objet de"

    BOOKINGS ||--o{ BOOKING_STATUS_HISTORY : trace
    BOOKINGS ||--o| PAYMENTS : declenche
    BOOKINGS ||--o{ REVIEWS : genere

    PAYMENTS ||--o| INVOICES : produit
    PAYMENTS ||--o| COMMISSIONS : calcule

    REVIEWS ||--o| REPORTS : "peut etre signale"

    USERS {
        uuid id PK
        string email UK
        string password_hash
        string phone
        string first_name
        string last_name
        enum role "SUPER_ADMIN|ADMIN|MODERATEUR|CLIENT|PROFESSIONNEL"
        enum status "PENDING|ACTIVE|SUSPENDED|BANNED"
        boolean email_verified
        boolean two_factor_enabled
        string avatar_url
        string locale
        timestamp last_login_at
    }

    REFRESH_TOKENS {
        uuid id PK
        uuid user_id FK
        string token UK
        timestamp expiry_date
        boolean revoked
    }

    PASSWORD_RESET_TOKENS {
        uuid id PK
        uuid user_id FK
        string token UK
        timestamp expiry_date
        boolean used
    }

    ACTIVATION_TOKENS {
        uuid id PK
        uuid user_id FK
        string token UK
        timestamp expiry_date
    }

    ADDRESSES {
        uuid id PK
        string country
        string region
        string city
        string street
        decimal latitude
        decimal longitude
    }

    PROFESSIONAL_PROFILES {
        uuid id PK
        uuid user_id FK UK
        string business_name
        string biography
        int years_experience
        uuid address_id FK
        enum verification_status "UNVERIFIED|PENDING|VERIFIED|REJECTED"
        boolean premium
        timestamp premium_expires_at
        decimal average_rating
        int total_reviews
        int total_bookings
        int response_time_hours
    }

    SKILLS {
        uuid id PK
        string name UK
    }

    LANGUAGES {
        uuid id PK
        string code UK
        string name
    }

    SOCIAL_LINKS {
        uuid id PK
        uuid professional_id FK
        enum platform "FACEBOOK|INSTAGRAM|TIKTOK|YOUTUBE|X|WEBSITE"
        string url
    }

    PROFESSIONAL_MEDIA {
        uuid id PK
        uuid professional_id FK
        enum type "IMAGE|VIDEO"
        string url
        int display_order
    }

    AVAILABILITIES {
        uuid id PK
        uuid professional_id FK
        date specific_date "nullable si recurrent"
        enum day_of_week "nullable si date precise"
        time start_time
        time end_time
        boolean recurring
        boolean blocked
    }

    CATEGORIES {
        uuid id PK
        uuid parent_id FK "nullable"
        string name
        string slug UK
        string description
        string icon
        string image
        boolean active
        int display_order
    }

    SERVICES {
        uuid id PK
        uuid professional_id FK
        uuid category_id FK
        string title
        string description
        enum price_type "FIXED|HOURLY|NEGOTIABLE"
        decimal price
        string currency
        int duration_minutes
        string conditions
        enum status "DRAFT|PUBLISHED|PAUSED|ARCHIVED"
    }

    SERVICE_MEDIA {
        uuid id PK
        uuid service_id FK
        enum type "IMAGE|VIDEO"
        string url
        int display_order
    }

    BOOKINGS {
        uuid id PK
        uuid client_id FK
        uuid professional_id FK
        uuid service_id FK
        timestamp event_date
        string event_address
        enum status "PENDING|ACCEPTED|REJECTED|DATE_PROPOSED|CONFIRMED|PAID|IN_PROGRESS|COMPLETED|CANCELLED"
        timestamp proposed_date "nullable"
        string client_message
        string professional_message
        decimal total_price
        decimal commission_amount
        decimal professional_amount
        string currency
    }

    BOOKING_STATUS_HISTORY {
        uuid id PK
        uuid booking_id FK
        enum from_status
        enum to_status
        uuid changed_by FK
        string comment
        timestamp changed_at
    }

    PAYMENTS {
        uuid id PK
        uuid booking_id FK UK
        enum provider "STRIPE|WAVE|ORANGE_MONEY|MOBILE_MONEY|CARD"
        decimal amount
        string currency
        enum status "PENDING|SUCCEEDED|FAILED|REFUNDED"
        string transaction_ref UK
        string invoice_url
        timestamp paid_at
    }

    COMMISSIONS {
        uuid id PK
        uuid payment_id FK
        decimal percentage_applied
        decimal amount
        timestamp calculated_at
    }

    COMMISSION_RULES {
        uuid id PK
        uuid category_id FK "nullable = regle globale"
        decimal percentage
        timestamp effective_from
        boolean active
    }

    PAYOUTS {
        uuid id PK
        uuid professional_id FK
        decimal amount
        enum status "PENDING|PROCESSING|PAID|FAILED"
        string reference
        timestamp paid_at
    }

    INVOICES {
        uuid id PK
        uuid payment_id FK UK
        string invoice_number UK
        string pdf_url
        timestamp issued_at
    }

    REVIEWS {
        uuid id PK
        uuid booking_id FK
        uuid author_id FK
        uuid target_id FK
        int rating "1-5"
        string comment
        string professional_response
        enum moderation_status "PENDING|APPROVED|REJECTED"
    }

    FAVORITES {
        uuid id PK
        uuid client_id FK
        uuid professional_id FK
    }

    NOTIFICATIONS {
        uuid id PK
        uuid user_id FK
        enum type
        string title
        string message
        boolean read
        enum channel "EMAIL|SMS|PUSH|WEBSOCKET"
        timestamp sent_at
    }

    REPORTS {
        uuid id PK
        uuid reporter_id FK
        uuid reported_user_id FK "nullable"
        uuid reported_review_id FK "nullable"
        string reason
        enum status "PENDING|REVIEWED|DISMISSED|ACTION_TAKEN"
    }

    USER_DOCUMENTS {
        uuid id PK
        uuid user_id FK
        enum type "ID_CARD|BUSINESS_LICENSE|OTHER"
        string url
        enum status "PENDING|APPROVED|REJECTED"
    }

    AUDIT_LOGS {
        uuid id PK
        string entity_name
        uuid entity_id
        string action
        uuid performed_by FK
        string details
        timestamp performed_at
    }
```

## Notes de modélisation

- **Soft delete** partout (`deleted boolean`) : aucune suppression physique sur les entités
  métier sensibles (Users, Bookings, Payments, Reviews).
- **Money as value object** : `price/amount` + `currency` toujours ensemble (`@Embeddable Money`).
- **Machine à états `BookingStatus`** (voir [UML-SEQUENCES.md](UML-SEQUENCES.md)) :
  `PENDING → (ACCEPTED|REJECTED|DATE_PROPOSED) → CONFIRMED → PAID → IN_PROGRESS → COMPLETED`,
  avec `CANCELLED` atteignable depuis la plupart des états tant que non `COMPLETED`.
- **Index recommandés** : `users(email)`, `services(category_id, status)`,
  `bookings(professional_id, status)`, `bookings(client_id, status)`, `reviews(target_id)`,
  index géographique (PostGIS optionnel v2) sur `addresses(latitude, longitude)`.
