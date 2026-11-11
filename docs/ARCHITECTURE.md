# KUNGO — Marketplace Événementielle
## Document d'architecture globale

> Plateforme de mise en relation entre professionnels de l'événementiel (musiciens, DJ, traiteurs,
> photographes, MC, humoristes, etc.) et clients, avec réservation, paiement sécurisé et commission
> automatique — inspirée d'Airbnb / Fiverr / Malt.

---

## 1. Vue d'ensemble

```
┌──────────────────────────────────────────────────────────────────────────┐
│                              CLIENTS (Web / Mobile)                       │
└───────────────────────────────────┬────────────────────────────────────-─┘
                                     │ HTTPS / REST / WebSocket (STOMP)
┌───────────────────────────────────▼────────────────────────────────────-─┐
│                       ANGULAR 20 (SPA, Standalone, SSR-ready)             │
│   core / shared / features(auth, search, professional, booking, ...)     │
└───────────────────────────────────┬────────────────────────────────────-─┘
                                     │ JWT Bearer
┌───────────────────────────────────▼────────────────────────────────────-─┐
│                     API GATEWAY (Spring Cloud Gateway - optionnel v2)     │
└───────────────────────────────────┬────────────────────────────────────-─┘
                                     │
┌───────────────────────────────────▼────────────────────────────────────-─┐
│                        SPRING BOOT 3 / JAVA 21 MONOLITH MODULAIRE         │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐  │
│  │   auth      │ │   user      │ │ professional│ │     category         │  │
│  ├─────────────┤ ├─────────────┤ ├─────────────┤ ├─────────────────────┤  │
│  │  service    │ │  booking    │ │  payment    │ │   commission          │  │
│  ├─────────────┤ ├─────────────┤ ├─────────────┤ ├─────────────────────┤  │
│  │  review     │ │  favorite   │ │notification │ │      admin            │  │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────────────┘  │
│                     common / config / security                            │
└───────────────┬───────────────────────┬───────────────────┬─────────────-┘
                 │                       │                   │
        ┌────────▼───────┐     ┌─────────▼────────┐  ┌───────▼────────┐
        │  PostgreSQL 16  │     │  Redis (cache/   │  │  Stripe / Wave /│
        │  + Flyway       │     │  blacklist JWT)  │  │  Orange Money   │
        └─────────────────┘     └──────────────────┘  └─────────────────┘
```

Le backend est développé comme un **monolithe modulaire** (package par module métier,
faible couplage, forte cohésion) — chaque module peut être extrait en microservice plus
tard sans réécriture (les bornes de contexte DDD sont déjà respectées).

---

## 2. Stack technique

### Backend
- Java 21, Spring Boot 3.3
- Spring Security 6 + JWT (access + refresh token) + WebSocket (STOMP/SockJS)
- Spring Data JPA / Hibernate 6, PostgreSQL 16
- Spring Validation, Spring Mail, Spring Scheduler, Spring Cache (Redis)
- Flyway (migrations versionnées)
- MapStruct (mapping), Lombok
- springdoc-openapi (Swagger UI)
- JUnit 5 + Mockito + Testcontainers
- Maven multi-module

### Frontend
- Angular 20, Standalone Components, Signals, Lazy Loading, Router Guards fonctionnels
- Angular Material + TailwindCSS (design system custom)
- RxJS, NgRx-like state via Signals Store (léger, pas de dépendance externe imposée)
- ngx-charts / Chart.js pour les dashboards

### Infra
- Docker / Docker Compose (dev + prod)
- GitHub Actions (build, test, lint, docker build & push)
- Nginx (reverse proxy + serve Angular build)

---

## 3. Architecture en couches (par module backend)

```
com.kungo.<module>
 ├── controller/        → REST endpoints, aucune logique métier
 ├── service/
 │    ├── impl/          → logique métier, transactions
 │    └── *Service.java   → interfaces
 ├── repository/         → Spring Data JPA + JpaSpecificationExecutor
 ├── domain/entity/      → entités JPA (héritent BaseEntity)
 ├── dto/
 │    ├── request/       → *CreateRequest, *UpdateRequest, *SearchRequest
 │    └── response/      → *Response
 ├── mapper/             → interfaces MapStruct
 ├── specification/      → Specification<T> dynamiques (Criteria API)
 ├── validator/          → validateurs métier custom (@Constraint)
 ├── exception/          → exceptions métier spécifiques au module
 └── config/             → configuration propre au module (si nécessaire)
```

Flux de dépendance strict : **Controller → Service → Repository → Entity**.
Le Controller ne connaît jamais le Repository. Le Service ne retourne jamais d'Entity
au Controller (toujours un DTO via Mapper).

### Modules transverses (`com.kungo.common`)
- `common.audit` — `BaseEntity`, `Auditable`, `AuditorAwareImpl`
- `common.exception` — `GlobalExceptionHandler`, hiérarchie d'exceptions métier
- `common.response` — `ApiResponse<T>`, `PageResponse<T>`, `ErrorResponse`
- `common.specification` — `GenericSpecificationBuilder`, `SearchCriteria`, opérateurs
- `common.security` — JWT provider, filtres, `UserPrincipal`, `SecurityConfig`
- `common.config` — Swagger, CORS, Async, Scheduler, i18n (`MessageSource` FR/EN), Mail, WebSocket

---

## 4. Rôles & permissions

| Rôle | Description |
|---|---|
| `SUPER_ADMIN` | Accès total, gestion des admins, configuration plateforme |
| `ADMIN` | Gestion utilisateurs, professionnels, commissions, signalements |
| `MODERATEUR` | Modération des avis, signalements, contenus |
| `PROFESSIONNEL` | Gère profil, prestations, disponibilités, réservations reçues |
| `CLIENT` | Recherche, réserve, paie, note |

Autorisation : `@PreAuthorize` méthode + `SecurityFilterChain` par pattern d'URL,
défense en profondeur (contrôle rôle + contrôle propriétaire de la ressource dans le
service, via `SecurityUtils.currentUserId()`).

---

## 5. Modules fonctionnels (backend)

1. **auth** — login, register, refresh, logout, activation email, mot de passe oublié, blacklist token
2. **user** — CRUD utilisateur, profil, avatar, documents, préférences, notifications
3. **professional** — profil pro, bio, expérience, tarifs, disponibilités, compétences, langues,
   localisation, réseaux sociaux, statistiques, vérification, badge, abonnement premium
4. **category** — catégories / sous-catégories, icônes, images
5. **service** (prestation) — CRUD, prix, durée, description, conditions, médias, disponibilités, statut
6. **search** — recherche plein texte + filtres dynamiques multicritères (Specification)
7. **booking** — création, acceptation, refus, contre-proposition de date, annulation, calendrier
8. **payment** — Stripe / Wave / Orange Money / Mobile Money / carte, factures PDF, historique
9. **commission** — configuration %, calcul automatique, reversement, rapports
10. **review** — notation bidirectionnelle, réponses, signalement, modération
11. **favorite** — ajout / suppression / liste
12. **notification** — email, SMS, push, WebSocket temps réel
13. **admin** — dashboard, gestion utilisateurs/pros, validation comptes, statistiques, logs, config

Voir [ERD.md](ERD.md) pour le modèle de données et [UML-CLASSES.md](UML-CLASSES.md) /
[UML-SEQUENCES.md](UML-SEQUENCES.md) pour les diagrammes détaillés.

---

## 6. Arborescence du monorepo

```
AI-KUNGO/
├── docs/
│   ├── ARCHITECTURE.md
│   ├── ERD.md
│   ├── UML-USE-CASES.md
│   ├── UML-CLASSES.md
│   └── UML-SEQUENCES.md
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/kungo/
│       ├── KungoApplication.java
│       ├── common/
│       ├── auth/
│       ├── user/
│       ├── professional/
│       ├── category/
│       ├── service/
│       ├── search/
│       ├── booking/
│       ├── payment/
│       ├── commission/
│       ├── review/
│       ├── favorite/
│       ├── notification/
│       └── admin/
│   └── src/main/resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/   (Flyway V1__..., V2__..., ...)
│   └── src/test/java/com/kungo/...
├── frontend/
│   ├── angular.json
│   ├── package.json
│   ├── tailwind.config.js
│   ├── Dockerfile
│   └── src/app/
│       ├── core/            (auth, interceptors, guards, services transverses)
│       ├── shared/          (composants UI réutilisables)
│       └── features/
│           ├── auth/
│           ├── search/
│           ├── professional-profile/
│           ├── booking/
│           ├── payment/
│           ├── review/
│           ├── dashboard-client/
│           ├── dashboard-professional/
│           └── dashboard-admin/
├── infra/
│   ├── docker-compose.yml
│   ├── docker-compose.prod.yml
│   └── nginx/
├── .github/workflows/ci.yml
├── .gitignore
└── README.md
```

---

## 7. Conventions & bonnes pratiques appliquées

- **Clean Code / SOLID** : une responsabilité par classe, interfaces pour tous les services.
- **DDD léger** : agrégats (`Booking` est racine d'agrégat pour `BookingStatusHistory`),
  value objects (`Money`, `Address`, `PriceRange` en `@Embeddable`).
- **Design patterns** : Builder (Lombok `@Builder`), Strategy (calcul de commission,
  fournisseurs de paiement), Factory (`NotificationFactory`), Observer (événements Spring
  `ApplicationEventPublisher` pour découpler booking → notification → email),
  Specification (recherche dynamique), Repository.
- **Sécurité** : validation systématique des entrées (Bean Validation), échappement,
  pas de requêtes SQL concaténées (JPA/Criteria uniquement), rate limiting sur `/auth/*`.
- **Observabilité** : logs structurés (SLF4J + MDC correlation-id), Actuator, audit trail.
- **i18n** : `messages_fr.properties` / `messages_en.properties`, `Accept-Language`.
- **Tests** : JUnit5 + Mockito (unitaire service), Testcontainers (intégration repository),
  MockMvc (contrôleurs).
