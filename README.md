# SenAnnonces — annonce-service

Microservice Spring Boot qui gère le cycle de vie des annonces (création,
listage, consultation, modération, publication) pour la plateforme
**SenAnnonces**.

---

## 1. Architecture

### 1.1 Vue d'ensemble

Le service applique une architecture **MVC en couches** :

```
HTTP request
   │
   ▼
┌──────────────┐   DTO valides    ┌──────────────┐   Entités    ┌────────────────┐
│  Controller  │ ───────────────▶ │   Service    │ ───────────▶ │   Repository   │
│  (REST API)  │ ◀─────────────── │  (Business)  │ ◀─────────── │  (Spring Data) │
└──────────────┘   AnnonceResponse└──────────────┘  Annonce     └────────────────┘
        ▲                                                                │
        │ JSON                                                           ▼
        │                                                          ┌─────────┐
        ▼                                                          │   H2    │
   GlobalExceptionHandler                                          └─────────┘
   (mappe les exceptions → ApiError)
```

| Couche | Responsabilité | Classes principales |
|---|---|---|
| **Controller** | Traduit HTTP ↔ DTO, validation, codes statut | `AnnonceController` |
| **Service** | Logique métier, transitions d'état, transactions | `AnnonceService` |
| **Repository** | Accès aux données via Spring Data JPA | `AnnonceRepository` |
| **Model** | Entité JPA et énumération de statuts | `Annonce`, `AnnonceStatus` |
| **DTO** | Découpe le contrat API du modèle interne | `AnnonceRequest`, `AnnonceResponse`, `SubmitDecisionRequest` |
| **Exception** | Erreurs métier + handler global REST | `AnnonceNotFoundException`, `InvalidStatusTransitionException`, `GlobalExceptionHandler`, `ApiError` |
| **Config** | Configuration OpenAPI / Swagger | `OpenApiConfig` |

### 1.2 Arborescence

```
annonce-service/
├── pom.xml
└── src/main/
    ├── java/sn/senannonces/annonceservice/
    │   ├── AnnonceServiceApplication.java
    │   ├── controller/AnnonceController.java
    │   ├── service/AnnonceService.java
    │   ├── repository/AnnonceRepository.java
    │   ├── model/
    │   │   ├── Annonce.java
    │   │   └── AnnonceStatus.java
    │   ├── dto/
    │   │   ├── AnnonceRequest.java
    │   │   ├── AnnonceResponse.java
    │   │   └── SubmitDecisionRequest.java
    │   ├── exception/
    │   │   ├── ApiError.java
    │   │   ├── AnnonceNotFoundException.java
    │   │   ├── InvalidStatusTransitionException.java
    │   │   └── GlobalExceptionHandler.java
    │   └── config/OpenApiConfig.java
    └── resources/application.yml
```

### 1.3 Cycle de vie d'une annonce

```
                ┌──────────────┐
   création ──▶ │  EN_ATTENTE  │
                └──────┬───────┘
                       │ POST /annonces/{id}/soumettre
                ┌──────┴────────────┐
                ▼                   ▼
        ┌──────────────┐    ┌──────────────┐
        │  APPROUVEE   │    │   REJETEE    │ (terminal)
        └──────┬───────┘    └──────────────┘
               │ PATCH /annonces/{id}/publier
               ▼
        ┌──────────────┐
        │   PUBLIEE    │ (terminal)
        └──────────────┘
```

### 1.4 Stack technique

- **Java 17**
- **Spring Boot 3.3.5** (Web, Data JPA, Validation)
- **H2** (base en mémoire, mode MySQL)
- **springdoc-openapi 2.6** (Swagger UI)
- **Lombok**
- **Maven** (build)

---

## 2. Installation

### 2.1 Prérequis

| Outil | Version minimum | Vérification |
|---|---|---|
| JDK | 17 | `java -version` |
| Maven | 3.8+ | `mvn -version` |

> Si plusieurs JDK sont installés, exporter `JAVA_HOME` vers un JDK 17.

### 2.2 Récupérer le projet

```bash
git clone git@github.com:verbeckd/annonce-service.git

cd annonce-service
```

### 2.3 Construire le projet

```bash
mvn clean package
```

Le jar exécutable est produit dans `target/annonce-service-1.0.0.jar`.

---

## 3. Lancement

### 3.1 Via Maven (mode développement)

```bash
mvn spring-boot:run
```

### 3.2 Via le jar exécutable

```bash
java -jar target/annonce-service-1.0.0.jar
```

### 3.3 Vérifier que le service répond

```bash
curl http://localhost:8081/swagger-ui/index.html#/
```

---

## 4. Endpoints

Base URL : `http://localhost:8081`

| Méthode | URL | Description |
|---|---|---|
| `POST` | `/annonces` | Crée une annonce (statut initial `EN_ATTENTE`) |
| `GET` | `/annonces` | Liste toutes les annonces |
| `GET` | `/annonces/{id}` | Détail d'une annonce |
| `POST` | `/annonces/{id}/soumettre` | Décision modérateur (`APPROUVEE` ou `REJETEE`) |
| `PATCH` | `/annonces/{id}/publier` | Publie une annonce `APPROUVEE` |

### 4.1 Exemples

**Créer une annonce**

```bash
curl -X POST http://localhost:8081/annonces \
  -H "Content-Type: application/json" \
  -d '{
        "titre": "Voiture à vendre",
        "description": "Toyota Yaris",
        "prix": 2500000,
        "ville": "Dakar"
      }'
```

**Soumettre une décision de modération**

```bash
curl -X POST http://localhost:8081/annonces/1/soumettre \
  -H "Content-Type: application/json" \
  -d '{"decision": "APPROUVEE"}'
```

**Publier**

```bash
curl -X PATCH http://localhost:8081/annonces/1/publier
```

### 4.2 Codes d'erreur

| Code | Cas |
|---|---|
| `400` | Payload invalide (validation Bean Validation) |
| `404` | Annonce introuvable |
| `409` | Transition de statut interdite |
| `500` | Erreur inattendue |

Toutes les erreurs renvoient un objet `ApiError` uniforme :

```json
{
  "timestamp": "2026-05-08T11:52:27.148Z",
  "status": 404,
  "error": "Not Found",
  "message": "Annonce not found with id: 999",
  "details": null
}
```

---

## 5. Documentation Swagger

Une fois le service lancé :

- **Swagger UI** : http://localhost:8081/swagger-ui.html
- **OpenAPI JSON** : http://localhost:8081/v3/api-docs

L'interface Swagger permet de tester les endpoints directement depuis le
navigateur.

---

## 6. Console H2 (optionnelle)

La base H2 en mémoire est exposée pour le débogage à
http://localhost:8081/h2-console avec :

- **JDBC URL** : `jdbc:h2:mem:annoncesdb`
- **Utilisateur** : `sa`
- **Mot de passe** : *(vide)*

> Les données sont volatiles : elles sont effacées à chaque redémarrage.

---

## 7. Configuration

Les propriétés modifiables se trouvent dans
`src/main/resources/application.yml`.

| Clé | Valeur par défaut | Description |
|---|---|---|
| `server.port` | `8081` | Port HTTP |
| `spring.datasource.url` | `jdbc:h2:mem:annoncesdb` | URL JDBC |
| `spring.jpa.hibernate.ddl-auto` | `update` | Stratégie de schéma |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` | Chemin Swagger UI |

Pour surcharger une propriété au lancement :

```bash
java -jar target/annonce-service-1.0.0.jar --server.port=9090
```
