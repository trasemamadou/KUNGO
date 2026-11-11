<<<<<<< HEAD
# KUNGO
=======
# KUNGO — Marketplace Événementielle

Plateforme de mise en relation entre **professionnels de l'événementiel** (musiciens, DJ,
photographes, traiteurs, MC, humoristes, chorales, coachs, etc.) et **clients**, avec
réservation en ligne, paiement sécurisé et commission automatique.

Documentation complète : [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) ·
[docs/ERD.md](docs/ERD.md) · [docs/UML-USE-CASES.md](docs/UML-USE-CASES.md) ·
[docs/UML-CLASSES.md](docs/UML-CLASSES.md) · [docs/UML-SEQUENCES.md](docs/UML-SEQUENCES.md)

## Stack

- **Backend** : Java 21, Spring Boot 3, Spring Security, Spring Data JPA, PostgreSQL 16,
  Flyway, MapStruct, Lombok, JWT, WebSocket, springdoc-openapi.
- **Frontend** : Angular 20 (standalone, signals), Angular Material, TailwindCSS, RxJS.
- **Infra** : Docker Compose, Nginx, GitHub Actions.

## Démarrage rapide (dev)

```bash
# 1. Lancer l'infra (PostgreSQL + Redis + backend + frontend)
cd infra
docker compose up -d --build

# Backend seul (hors docker) :
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Frontend seul (hors docker) :
cd frontend
npm install
npm start
```

- API : http://localhost:8080/api/v1
- Swagger UI : http://localhost:8080/swagger-ui.html
- Frontend : http://localhost:4200
- PostgreSQL : localhost:5432 (db `kungo`, user `kungo`)
- MailHog (emails de dev) : http://localhost:8025

## Déploiement production

```bash
cd infra
cp .env.example .env   # renseigner les secrets réels
docker compose -f docker-compose.prod.yml up -d
```

Voir [infra/.env.example](infra/.env.example) pour la liste des variables requises
(secrets JWT, base de données, SMTP, Stripe, URLs publiques). Les images
`kungo-backend` / `kungo-frontend` sont construites et poussées vers GHCR par la CI
(voir [.github/workflows/ci.yml](.github/workflows/ci.yml)) à chaque merge sur `main`.

## Tests

```bash
cd backend && ./mvnw test        # JUnit 5 + Mockito (44 tests, contexte Spring inclus)
cd frontend && npm test           # Karma + Jasmine
```

## Structure du repo

Voir la section "Arborescence du monorepo" dans [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## État d'avancement

| Domaine | Statut |
|---|---|
| Backend (13 modules : auth, user, professional, category, prestation, booking, payment/commission, review/favorite/notification, admin) | ✅ Complet, 44 tests passants |
| Migrations Flyway (V1 → V8) | ✅ Complet |
| Frontend Angular 20 (auth, recherche, profil pro, réservation, paiement, dashboards client/pro/admin) | ✅ Complet, build prod validé |
| Docker Compose (dev + prod) | ✅ Fichiers prêts |
| CI GitHub Actions | ✅ Build + tests backend/frontend, publication d'images sur `main` |

**Limitations connues à traiter avant mise en production réelle :**
- Wave / Orange Money / Mobile Money sont des implémentations placeholder
  (`*PaymentStrategy` dans `backend/.../payment/service/strategy/`) — Stripe est la seule
  intégration de paiement réelle. Brancher les vraies API marchandes avant le lancement.
- Le reversement (`Payout`) est déclenché manuellement par un admin ; l'automatisation
  complète nécessite l'intégration des API de paiement sortant des fournisseurs ci-dessus.
>>>>>>> fdd6c8a (FIRST COMMIT)
