# E-commerce microservices

Backend e-commerce découpé en microservices Spring Boot : catalogue produits, utilisateurs, panier et commandes, avec configuration centralisée, découverte de services, logs, métriques et traces distribuées.

## Architecture

```text
                 ┌──────────────┐      ┌──────────────┐
                 │ config-server│      │    eureka    │
                 │    :8888     │      │    :8761     │
                 └──────┬───────┘      └──────┬───────┘
          config (au démarrage)        enregistrement / découverte
        ┌───────────────┼─────────────────────┼───────────────┐
        ▼               ▼                     ▼               ▼
 ┌─────────────┐ ┌─────────────┐       ┌─────────────┐
 │ product     │ │ user        │◄──────│ order       │
 │ :8081       │ │ :8082       │  HTTP │ :8083       │
 │ PostgreSQL  │ │ MongoDB     │       │ PostgreSQL  │
 └─────────────┘ └─────────────┘       └──────┬──────┘
        ▲                                     │ HTTP (load-balancé via Eureka)
        └─────────────────────────────────────┘
```

| Module | Nom applicatif | Port | Stockage | API |
|---|---|---|---|---|
| `config-server/` | `config-server` | 8888 | fichiers YAML (`config/`) | — |
| `eureka/` | `eureka` | 8761 | — | tableau de bord Eureka |
| `product/` | `product-service` | 8081 | PostgreSQL (JPA) | `/api/v1/products` |
| `user/` | `user-service` | 8082 | MongoDB | `/api/v1/users` |
| `order/` | `order-service` | 8083 | PostgreSQL (JPA + Flyway, schéma `order_service`) | `/api/v1/cart`, `/api/v1/orders` |

Chaque module est un **projet Maven indépendant** avec son propre `mvnw` (pas de POM parent).

**Stack** : Java 21, Spring Boot 4.1.2, Spring Cloud 2025.1.4 (Config, Bus/RabbitMQ, Netflix Eureka, LoadBalancer), Spring Security, Spring Data JPA / MongoDB, Flyway, springdoc-openapi, Micrometer (Prometheus + Tracing Brave/Zipkin), Lombok, Jackson 3, JUnit 5, Mockito, Testcontainers.

## Démarrage

### Prérequis
- JDK 21, Docker (obligatoire aussi pour les tests : Testcontainers).
- Une instance MongoDB sur `localhost:27017` pour `user-service` (le service MongoDB du `docker-compose.yaml` est commenté).

### Variables d'environnement

| Service | Variables |
|---|---|
| order | `DB_USERNAME`, `DB_PASSWORD` |
| product | `POSTGRES_URL`, `POSTGRES_USERNAME`, `POSTGRES_PASSWORD` |
| user | `MONGODB_URI` (défaut : `mongodb://localhost:27017/user`) |

### Ordre de lancement

```bash
docker compose up -d                 # racine : PostgreSQL 16, pgAdmin, RabbitMQ
(cd loki && docker compose up -d)    # observabilité : Loki, Alloy, Prometheus, Grafana, Zipkin

cd config-server && ./mvnw spring-boot:run   # 1. configuration
cd eureka        && ./mvnw spring-boot:run   # 2. registre de services
cd product       && ./mvnw spring-boot:run   # 3. puis les services, dans n'importe quel ordre
cd user          && ./mvnw spring-boot:run
cd order         && ./mvnw spring-boot:run
```

Sous Windows PowerShell : `.\mvnw.cmd`. Lancer les services depuis la racine du dépôt si l'on veut que leurs logs arrivent dans `logs/` (lu par Alloy).

### Configuration
- Les services ne contiennent que leur nom et `spring.config.import: optional:configserver:http://localhost:8888`. **La vraie configuration est dans `config-server/src/main/resources/config/<nom>-service.yml`.**
- Le config-server tourne en profil `native` sur un chemin absolu : une modification des YAML est servie sans redémarrer le config-server (les services, eux, doivent être redémarrés ou rafraîchis via Spring Cloud Bus).
- `order-service` n'a besoin d'aucune préparation de base : Flyway crée le schéma `order_service` et applique `order/src/main/resources/db/migration` (`V1` tables panier/commandes, `V2` `user_id` en `varchar`).

## API

Swagger UI sur chaque service : `http://localhost:<port>/swagger-ui.html` (spec : `/v3/api-docs`).

### product-service (`/api/v1/products`)
| Méthode | Chemin | Description |
|---|---|---|
| GET | `/` | Produits actifs |
| POST | `/` | Créer un produit (`stock` à 0 par défaut) |
| GET | `/{id}` | Produit actif par id (404 si inexistant ou inactif) |
| PUT | `/{id}` | Remplacer un produit |
| DELETE | `/{id}` | Supprimer (suppression physique) |
| GET | `/search?name=` | Recherche par nom (actifs, en stock) ; 404 si aucun résultat |

### user-service (`/api/v1/users`)
| Méthode | Chemin | Description |
|---|---|---|
| GET | `/` | Liste des utilisateurs |
| POST | `/` | Créer un utilisateur (email unique) |
| GET | `/{id}` | Utilisateur par id |
| PUT | `/{id}` | Remplacer un utilisateur (adresse comprise) |

### order-service (`/api/v1/cart`, `/api/v1/orders`)
L'utilisateur est identifié par l'en-tête **`X-User-ID`** (id MongoDB de user-service).

| Méthode | Chemin | Description |
|---|---|---|
| POST | `/api/v1/cart` | Ajouter un produit au panier (vérifie produit actif, stock, utilisateur) |
| DELETE | `/api/v1/cart/items/{productId}` | Retirer une ligne du panier |
| GET | `/api/v1/cart/items` | Contenu du panier |
| POST | `/api/v1/orders` | Passer commande à partir du panier, puis le vider |

Les prix stockés sont **unitaires** ; les totaux sont calculés `prix × quantité`. Produit ou utilisateur introuvable → 404.

## Communication entre services avant Eureka
![img_1.png](img_1.png)

## Communication entre services avec Eureka
- `order` appelle `product` et `user` via des **interfaces HTTP Spring** (`@HttpExchange`) construites sur un `RestClient` **`@LoadBalanced`** : les URL `http://PRODUCT-SERVICE` / `http://USER-SERVICE` sont résolues par Eureka.
- Un builder `RestClient` `@Primary` non load-balancé est déclaré à part : sans lui le client Eureka utiliserait le builder load-balancé et ne pourrait pas s'enregistrer.
- Un 404 distant (`HttpClientErrorException.NotFound`) est converti en « introuvable » ; les autres erreurs distantes remontent en 500.
- `@LoadBalanced` ne répartit que les appels **sortants** d'order. Une API gateway (Spring Cloud Gateway) est prévue pour répartir le trafic entrant.
- Pas de timeout, retry ni circuit breaker pour l'instant.
![img_2.png](img_2.png)

## Observabilité (`loki/`)

| Outil | URL | Rôle |
|---|---|---|
| Grafana | http://localhost:3000 | Tableaux de bord (accès anonyme admin) |
| Prometheus | http://localhost:9090 | Métriques (`/actuator/prometheus` des 3 services, toutes les 3 s) |
| Loki (gateway) | http://localhost:3100 | Logs, stockage `filesystem` (volume `loki-data`) |
| Alloy | http://localhost:12345 | Collecte des logs (fichiers `logs/*.log` + conteneurs Docker) → Loki (tenant `tenant1`) |
| Zipkin | http://localhost:9411 | Traces distribuées |

- **Logs** : chaque service écrit `logs/<nom>-service.log` avec rotation par taille et par date (le motif d'archive doit contenir `%i`).
![img.png](img.png)
- **Grafana** : datasources Loki et Prometheus provisionnées depuis `loki/datasources/datasources.yml` (Prometheus a l'uid fixe `prometheus`). Les dashboards sont importés depuis l'interface et conservés dans le volume `grafana-data`. Pour un dashboard grafana.com (ex. 4701 « JVM (Micrometer) »), remplacer `${DS_PROMETHEUS}` par `prometheus` dans le JSON Model si l'import ne demande pas la datasource.
- **Traces** : échantillonnage à 100 %, envoi vers Zipkin. Les endpoints `/actuator/**` sont exclus du tracing (`config/ObservationConfig` dans chaque service). Dans Zipkin, filtrer par service ou par nom de span.
![img_3.png](img_3.png)
- Limite connue : les appels order → product/user n'apparaissent pas encore dans la même trace (le builder load-balancé n'a pas d'`observationRegistry`).

## Tests

```bash
cd <module> && ./mvnw test                     # unitaires + intégration (Docker requis)
./mvnw -Dtest=CartServiceTest test             # une classe
```

- Unitaires (Mockito), tranches web (`@WebMvcTest`), intégration/E2E (`@SpringBootTest` + Testcontainers PostgreSQL / MongoDB, profil `test`, sans config-server).
- Dans `order`, `ProductClient` et `UserClient` sont mockés : product-service et user-service n'ont pas besoin de tourner.
- Couverture mesurée avec JaCoCo à **100 % des lignes et des branches** sur order, product et user (plugin non déclaré dans les POM) :
  ```bash
  ./mvnw clean org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent test org.jacoco:jacoco-maven-plugin:0.8.12:report
  # rapport : target/site/jacoco/index.html
  ```

## Historique du projet
1. **Socle** : monorepo de microservices product / user / order, configuration centralisée avec Spring Cloud Config (+ Bus RabbitMQ).
2. **Revue de code** et corrections.
3. **Documentation d'API** avec springdoc (Swagger UI) et **migrations Flyway** pour order (schéma `order_service`, `ddl-auto: validate`).
4. **Découverte de services Eureka** et communication inter-services : order appelle product et user via `RestClient` load-balancé ; identifiant utilisateur passé en `String` (migration `V2`) ; gestion des 404 distants ; prix unitaires.
5. **Couverture de tests à 100 %** (lignes et branches) sur les trois services.
6. **Observabilité** : Loki + Alloy (stockage passé de MinIO au filesystem), Prometheus + Grafana (datasources provisionnées, uid fixe), correction de la rotation des logs (`%i`).
7. **Traces distribuées** avec Micrometer Tracing + Zipkin, endpoints actuator exclus du tracing.

## À venir / points ouverts
- API gateway (Spring Cloud Gateway) pour le trafic entrant.
- Relier les appels order → product/user dans une même trace.
- Sécurité : les endpoints API et actuator (dont `shutdown`) sont en `permitAll`.
- Résilience des appels inter-services (timeouts, retry, circuit breaker).
- Le contrôle de stock à l'ajout au panier ne tient pas compte de la quantité déjà dans le panier.
