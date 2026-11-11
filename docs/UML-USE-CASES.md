# Diagramme de cas d'utilisation

```mermaid
flowchart LR
    Client((Client))
    Pro((Professionnel))
    Admin((Admin / Super Admin))
    Mod((Modérateur))
    Guest((Visiteur))

    subgraph UC_AUTH [Authentification]
        UC1[S'inscrire]
        UC2[Activer le compte]
        UC3[Se connecter]
        UC4[Mot de passe oublié]
        UC5[Rafraîchir session]
        UC6[Se déconnecter]
    end

    subgraph UC_SEARCH [Recherche]
        UC7[Rechercher un professionnel]
        UC8[Filtrer multicritères]
        UC9[Consulter un profil]
    end

    subgraph UC_PRO [Espace Professionnel]
        UC10[Compléter le profil]
        UC11[Gérer les prestations]
        UC12[Gérer les disponibilités]
        UC13[Répondre à une demande]
        UC14[Proposer une nouvelle date]
        UC15[Consulter statistiques]
        UC16[Demander vérification / badge]
        UC17[Souscrire abonnement premium]
    end

    subgraph UC_BOOKING [Réservation]
        UC18[Envoyer une demande de réservation]
        UC19[Valider et payer]
        UC20[Annuler une réservation]
        UC21[Consulter calendrier]
    end

    subgraph UC_REVIEW [Avis]
        UC22[Noter la prestation]
        UC23[Répondre à un avis]
        UC24[Signaler un contenu]
    end

    subgraph UC_ADMIN [Administration]
        UC25[Valider un compte professionnel]
        UC26[Gérer les commissions]
        UC27[Modérer signalements/avis]
        UC28[Consulter dashboard & statistiques]
        UC29[Gérer utilisateurs]
    end

    Guest --> UC1
    Guest --> UC7
    Guest --> UC9

    Client --> UC1
    Client --> UC2
    Client --> UC3
    Client --> UC4
    Client --> UC5
    Client --> UC6
    Client --> UC7
    Client --> UC8
    Client --> UC9
    Client --> UC18
    Client --> UC19
    Client --> UC20
    Client --> UC22
    Client --> UC24

    Pro --> UC1
    Pro --> UC2
    Pro --> UC3
    Pro --> UC10
    Pro --> UC11
    Pro --> UC12
    Pro --> UC13
    Pro --> UC14
    Pro --> UC15
    Pro --> UC16
    Pro --> UC17
    Pro --> UC21
    Pro --> UC23

    Admin --> UC25
    Admin --> UC26
    Admin --> UC28
    Admin --> UC29
    Mod --> UC27
    Mod --> UC24

    UC18 -. include .-> UC9
    UC19 -. include .-> UC18
    UC13 -. extend .-> UC14
```

## Description textuelle des cas d'usage critiques

| Cas d'usage | Acteur | Pré-condition | Post-condition |
|---|---|---|---|
| Envoyer une demande de réservation | Client | Authentifié, prestation publiée | `Booking` créé en statut `PENDING`, notification envoyée au pro |
| Répondre à une demande | Professionnel | Booking `PENDING` lui appartenant | Statut → `ACCEPTED` / `REJECTED` / `DATE_PROPOSED` |
| Valider et payer | Client | Booking `ACCEPTED` ou `DATE_PROPOSED` acceptée | Paiement `SUCCEEDED`, commission calculée, Booking → `PAID` |
| Noter la prestation | Client & Professionnel | Booking `COMPLETED` | `Review` créée, moyenne recalculée |
| Valider un compte professionnel | Admin | Documents soumis (`UserDocument` `PENDING`) | `verification_status` → `VERIFIED`, badge attribué |
