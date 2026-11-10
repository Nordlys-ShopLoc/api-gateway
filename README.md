# api-gateway

Point d'entrée unique de l'API ShopLoc, construit avec Spring Cloud Gateway.

## Rôle

Toutes les requêtes de l'API passent par la gateway, qui les transmet au service concerné selon le chemin appelé. Le routage se fait par ressource, sous la forme `/api/v1/<ressource>` : le frontend ne connaît que ces chemins, jamais les services qui sont derrière. Avec le frontend, c'est le seul composant exposé à l'extérieur.

## Routes

| Chemin | Service | Variable | Défaut en local |
|---|---|---|---|
| `/api/v1/shops/**` | service-merchants | `MERCHANTS_URL` | `http://localhost:8081` |
| `/api/v1/auth/**`, `/api/v1/accounts/**`, `/api/v1/cards/**` | service-accounts | `ACCOUNTS_URL` | `http://localhost:8082` |

Toute nouvelle route suit la même convention et est ajoutée à ce tableau.

## Lancer la gateway en local

```bash
./mvnw spring-boot:run
```

La gateway démarre sur http://localhost:8080. Les services vers lesquels elle route doivent tourner sur leurs ports par défaut.

## Tests

```bash
./mvnw clean verify
```

## Image Docker

```bash
docker build -t api-gateway:local .
```

## Configuration

| Variable | Défaut |
|---|---|
| `SERVER_PORT` | `8080` |
| `MERCHANTS_URL` | `http://localhost:8081` |
| `ACCOUNTS_URL` | `http://localhost:8082` |

## Contribuer

Une branche `feature/...` par user story, créée depuis `develop`. Commits au format Conventional Commits, en anglais. Pull request vers `develop`, relue par un autre membre, avec la CI au vert.
