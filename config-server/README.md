# Config Server

Ce module est un serveur de configuration Spring Cloud Config qui centralise la configuration de tous les microservices de l'application e-commerce.

## Structure du Projet

- `ConfigServerApplication.java` - Classe principale avec l'annotation `@EnableConfigServer`
- `application.yml` - Configuration du serveur (port, dépôt Git)

## Concepts Implémentés

### 1. Spring Cloud Config Server

Le serveur de configuration utilise l'annotation `@EnableConfigServer` pour activer les fonctionnalités de serveur de configuration Spring Cloud.

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

### 2. Configuration Git

Le serveur récupère les fichiers de configuration depuis un dépôt Git centralisé.

**Configuration dans application.yml :**

```yaml
spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          uri: https://github.com/soffiane/config-server
          search-paths: src/main/resources
          default-label: master
server:
  port: 8888
```

**Explication des propriétés :**

- `spring.cloud.config.server.git.uri` : URL du dépôt Git contenant les fichiers de configuration
- `spring.cloud.config.server.git.search-paths` : Chemins dans le dépôt où chercher les fichiers de configuration
- `spring.cloud.config.server.git.default-label` : Branche Git par défaut (master, main, etc.)
- `server.port` : Port sur lequel le serveur écoute (par défaut 8888)

### 3. Configuration Native (Fichiers Locaux)

Le serveur peut également charger les fichiers de configuration depuis le système de fichiers local (classpath ou répertoire externe). Cette méthode est utile pour le développement ou les environnements sans Git.

**Configuration dans application.yml :**

```yaml
spring:
  application:
    name: config-server
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/config
server:
  port: 8888
```

**Explication des propriétés :**

- `spring.profiles.active: native` : Active le profil native pour le chargement local
- `spring.cloud.config.server.native.search-locations` : Chemins où chercher les fichiers de configuration
  - `classpath:/config` : Cherche dans le dossier `../additionnal/config-server/src/main/resources/config`
  - `file:/path/to/config` : Cherche dans un répertoire externe du système de fichiers
- `server.port` : Port sur lequel le serveur écoute (par défaut 8888)

**Structure des fichiers locaux :**

```
src/main/resources/
├── application.yml          # Configuration du config server
└── config/                  # Dossier contenant les fichiers de configuration
    ├── config.yml           # Configuration par défaut du microservice "config"
    ├── config-dev.yml       # Configuration dev du microservice "config"
    ├── config-prod.yml      # Configuration prod du microservice "config"
    └── ...                  # Autres microservices
```

**Avantages du profil native :**
- ✅ Pas besoin de serveur Git
- ✅ Développement plus rapide
- ✅ Configuration locale facile à tester
- ✅ Idéal pour les environnements de test

**Inconvénients du profil native :**
- ❌ Pas de versioning automatique
- ❌ Gestion manuelle des configurations
- ❌ Pas de collaboration facile sur les configurations
- ❌ Déploiement manuel des changements

**Charger depuis un répertoire externe :**

```yaml
spring:
  cloud:
    config:
      server:
        native:
          search-locations: file:/etc/config-server/config
```

**Multiples emplacements de recherche :**

```yaml
spring:
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/config,file:/etc/config-server/config
```

## Dépendance Maven

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

## À quoi ça sert ?

Spring Cloud Config Server permet de :

- **Centraliser la configuration** : Toutes les configurations des microservices sont stockées dans un seul dépôt Git
- **Gérer les profils par environnement** : Différentes configurations pour dev, prod, test, etc.
- **Versioning** : L'historique des configurations est géré via Git
- **Mises à jour à chaud** : Les configurations peuvent être mises à jour sans redémarrer les applications (avec `/refresh`)
- **Sécurité** : Les configurations sensibles peuvent être chiffrées et déchiffrées par le serveur
- **Scalabilité** : Facile de gérer la configuration de dizaines de microservices

## Structure des Fichiers de Configuration

Le dépôt Git contient les fichiers de configuration pour chaque microservice et chaque profil :

```
src/main/resources/
├── application.yml          # Configuration du config server (non distribuée)
├── config.yml              # Configuration par défaut du microservice "config"
├── config-dev.yml          # Configuration dev du microservice "config"
├── config-prod.yml         # Configuration prod du microservice "config"
└── ...                     # Autres microservices
```

**Convention de nommage :**
- `{nom-application}.yml` - Configuration par défaut
- `{nom-application}-{profil}.yml` - Configuration spécifique à un profil

## API du Serveur de Configuration

Le serveur expose des endpoints REST pour récupérer les configurations :

### Récupérer la configuration par défaut

```
GET http://localhost:8888/{nom-application}/default
```

### Récupérer la configuration pour un profil

```
GET http://localhost:8888/{nom-application}/{profil}
```

### Récupérer la configuration pour un profil et une branche

```
GET http://localhost:8888/{nom-application}/{profil}/{branche}
```

**Exemple :**
```bash
curl http://localhost:8888/config/dev
```

**Réponse JSON :**
```json
{
  "name": "config",
  "profiles": ["dev"],
  "label": null,
  "version": "e59acc068271ec910ec7c29f2476bb339b47ea30",
  "propertySources": [
    {
      "name": "https://github.com/soffiane/config-server/src/main/resources/config-dev.yml",
      "source": {
        "build.id": 101,
        "build.version": "1.1.1"
      }
    },
    {
      "name": "https://github.com/soffiane/config-server/src/main/resources/config.yml",
      "source": {
        "build.id": 123456789,
        "build.version": "1.0.0"
      }
    }
  ]
}
```

## Fonctionnement

1. Le config server démarre et se connecte au dépôt Git
2. Les clients (microservices) se connectent au config server au démarrage
3. Le config server récupère les fichiers de configuration depuis Git
4. Le config server envoie la configuration correspondante au client
5. Le client fusionne la configuration reçue avec sa configuration locale

**Ordre de fusion (du plus prioritaire au moins prioritaire) :**
1. Configuration spécifique au profil (ex: `config-dev.yml`)
2. Configuration par défaut (ex: `config.yml`)

## Exécution du Projet

### Lancer le serveur avec Maven :

```bash
mvn spring-boot:run
```

### Lancer le serveur via JAR :

```bash
mvn clean package
java -jar target/config-server-0.0.1-SNAPSHOT.jar
```

### Lancer avec un port spécifique :

```bash
java -jar target/config-server-0.0.1-SNAPSHOT.jar --server.port=8888
```

## Vérification du Fonctionnement

Une fois le serveur démarré, vérifiez qu'il fonctionne correctement :

```bash
curl http://localhost:8888/config/dev
```

Vous devriez recevoir une réponse JSON avec les propriétés de configuration.

## Configuration des Clients (Microservices)

Les microservices clients doivent être configurés pour se connecter au Config Server et récupérer leur configuration au démarrage.

### Dépendances Maven

Ajoutez la dépendance suivante dans le `../additionnal/config-server/pom.xml` de chaque microservice client :

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

**Explication :**
- `spring-cloud-starter-config` : Permet au client de se connecter au config server

**Note :** Depuis Spring Boot 2.4, le fichier `bootstrap.yml` n'est plus nécessaire. La configuration peut être faite directement dans `application.yml`. Si vous utilisez une version antérieure ou si vous avez besoin du bootstrap pour des raisons spécifiques, ajoutez la dépendance `spring-cloud-starter-bootstrap` et utilisez un fichier `bootstrap.yml`.

### Fichier application.yml

Configurez la connexion au config server directement dans le fichier `application.yml` de chaque microservice client :

```yaml
spring:
  application:
    name: user  # Nom du microservice (doit correspondre au fichier de configuration)
  cloud:
    config:
      uri: http://localhost:8888  # URL du config server
      fail-fast: true  # Échec rapide si le config server n'est pas disponible
      retry:
        initial-interval: 1000  # Délai initial avant la première tentative (ms)
        max-interval: 2000  # Délai maximum entre les tentatives (ms)
        multiplier: 1.5  # Multiplicateur pour le délai entre les tentatives
        max-attempts: 6  # Nombre maximum de tentatives
  profiles:
    active: dev  # Profil actif (dev, prod, test, etc.)
```

**Explication des propriétés :**
- `spring.application.name` : Identifie le microservice auprès du config server (doit correspondre au nom du fichier de configuration, ex: `user.yml`)
- `spring.cloud.config.uri` : URL du config server
- `spring.cloud.config.fail-fast` : Si true, l'application échoue au démarrage si le config server n'est pas accessible
- `spring.cloud.config.retry` : Configuration de la reconnexion automatique en cas d'échec
- `spring.profiles.active` : Profil à utiliser pour récupérer la configuration spécifique (ex: `user-dev.yml`)

### Structure des Fichiers de Configuration Client

Chaque microservice doit avoir son propre fichier de configuration dans le config server :

**Exemple pour le microservice User :**

`config/user.yml` (configuration par défaut)
```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/user_db
    username: user
    password: userpass

user:
  service:
    name: User Service
    version: 1.0.0
    max-users: 1000
```

`config/user-dev.yml` (configuration pour l'environnement de développement)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/user_dev_db
    username: dev_user
    password: dev_pass

logging:
  level:
    com.ecommerce.user: DEBUG
```

`config/user-prod.yml` (configuration pour la production)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://prod-db:5432/user_db
    username: ${DB_USERNAME}
    password: '{cipher}AQBpX9zY2wV4kL8mN6oP0qR2sT4uV6wX8yZ0aB2cD4eF6gH8iJ0kL2mN4oP6qR8sT0'

logging:
  level:
    com.ecommerce.user: INFO
```

### Utilisation des Propriétés dans le Code

#### Avec @Value

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Value("${user.service.name}")
    private String serviceName;

    @Value("${user.service.version}")
    private String version;

    @GetMapping("/info")
    public Map<String, String> getInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("service", serviceName);
        info.put("version", version);
        return info;
    }
}
```

#### Avec @ConfigurationProperties (Recommandé)

```java
@Component
@ConfigurationProperties(prefix = "user.service")
@Data
public class UserServiceProperties {
    private String name;
    private String version;
    private Integer maxUsers;
}
```

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserServiceProperties properties;

    public UserController(UserServiceProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/info")
    public Map<String, String> getInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("service", properties.getName());
        info.put("version", properties.getVersion());
        info.put("maxUsers", String.valueOf(properties.getMaxUsers()));
        return info;
    }
}
```

### Configuration des Différents Microservices

#### Microservice User

**application.yml :**
```yaml
spring:
  application:
    name: user
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true
  profiles:
    active: dev
```

**Fichiers de configuration dans le config server :**
- `config/user.yml` - Configuration par défaut
- `config/user-dev.yml` - Configuration développement
- `config/user-prod.yml` - Configuration production

#### Microservice Product

**application.yml :**
```yaml
spring:
  application:
    name: product
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true
  profiles:
    active: dev
```

**Fichiers de configuration dans le config server :**
- `config/product.yml` - Configuration par défaut
- `config/product-dev.yml` - Configuration développement
- `config/product-prod.yml` - Configuration production

#### Microservice Order

**application.yml :**
```yaml
spring:
  application:
    name: order
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true
  profiles:
    active: dev
```

**Fichiers de configuration dans le config server :**
- `config/order.yml` - Configuration par défaut
- `config/order-dev.yml` - Configuration développement
- `config/order-prod.yml` - Configuration production

### Rafraîchissement Dynamique avec @RefreshScope

Pour permettre le rafraîchissement dynamique de la configuration sans redémarrer l'application :

#### 1. Ajouter l'annotation @RefreshScope

```java
@Component
@ConfigurationProperties(prefix = "user.service")
@RefreshScope
@Data
public class UserServiceProperties {
    private String name;
    private String version;
    private Integer maxUsers;
}
```

```java
@RestController
@RequestMapping("/api/users")
@RefreshScope
public class UserController {
    // ...
}
```

#### 2. Exposer l'endpoint de refresh

Dans `application.yml` du client :
```yaml
management:
  endpoints:
    web:
      exposure:
        include: refresh,health,info
```

#### 3. Rafraîchir la configuration

```bash
POST http://localhost:8081/actuator/refresh
```

**Réponse :**
```json
[
  "user.service.name",
  "user.service.version"
]
```

### Ordre de Priorité de Configuration

Lorsqu'un microservice client démarre, la configuration est fusionnée dans l'ordre suivant (du plus prioritaire au moins prioritaire) :

1. **Variables d'environnement** (ex: `DB_USERNAME`)
2. **Arguments de ligne de commande** (ex: `--server.port=8082`)
3. **Configuration spécifique au profil** (ex: `user-dev.yml` depuis le config server)
4. **Configuration par défaut** (ex: `user.yml` depuis le config server)
5. **Configuration locale** (ex: `application.yml` dans le classpath du client)

### Configuration Avancée des Clients

#### Désactiver le Config Server

Pour désactiver la connexion au config server (utile pour les tests locaux) :

```yaml
spring:
  cloud:
    config:
      enabled: false
```

#### Utiliser plusieurs Config Servers

```yaml
spring:
  cloud:
    config:
      uri: http://config-server1:8888,http://config-server2:8888
```

#### Timeout de connexion

```yaml
spring:
  cloud:
    config:
      request-read-timeout: 5000  # 5 secondes
      request-connect-timeout: 3000  # 3 secondes
```

#### Authentification avec le Config Server

Si le config server est sécurisé avec Spring Security :

```yaml
spring:
  cloud:
    config:
      username: config-client
      password: secret-password
```

### Vérification du Fonctionnement Client

Une fois le microservice démarré, vérifiez qu'il a bien récupéré sa configuration :

```bash
# Vérifier les propriétés chargées
GET http://localhost:8081/actuator/env

# Vérifier les propriétés de configuration spécifiques
GET http://localhost:8081/actuator/configprops
```

### Exemple Complet d'Intégration

**Scénario :** Le microservice User doit récupérer sa configuration depuis le config server

1. **Démarrer le config server** sur le port 8888
2. **Créer le fichier `config/user.yml`** dans le config server
3. **Configurer le fichier `application.yml`** dans le microservice User avec la connexion au config server
4. **Ajouter les dépendances Maven** dans le microservice User
5. **Démarrer le microservice User**
6. **Vérifier** que la configuration est chargée via `/actuator/env`

**Logs au démarrage du client :**
```
Fetching config from server at : http://localhost:8888
Located environment: name=user, profiles=[dev], label=null, version=e59acc068271ec910ec7c29f2476bb339b47ea30
```

## Chiffrement AES avec Spring Cloud Config

Spring Cloud Config fournit un support natif pour le chiffrement et le déchiffrement des propriétés sensibles (mots de passe, clés API, tokens, etc.) en utilisant l'algorithme AES avec une clé symétrique.

### Configuration du Chiffrement AES

Pour activer le chiffrement AES, configurez une clé de chiffrement dans le fichier `application.yml` du config server :

```yaml
encrypt:
  key: ma-cle-secrete-pour-le-chiffrement-aes
```

**⚠️ Important :** En production, ne stockez jamais la clé en clair dans le fichier de configuration. Utilisez plutôt une variable d'environnement :

```yaml
encrypt:
  key: ${ENCRYPT_KEY}
```

### Génération d'une Clé AES 256 Bits

Pour une sécurité optimale, utilisez une clé AES 256 bits (32 octets). Voici comment en générer une sécurisée :

#### Sur Linux / macOS

**Méthode 1 : Avec OpenSSL (recommandé)**

```bash
openssl rand -base64 32
```

**Résultat exemple :**
```
xK9mN2pQ5rS8tV1wY4zB7cD0eF3gH6iJ9kL2mN5oP8qR1sT4uV7wX0zA3bC6dE9fG
```

**Méthode 2 : Avec /dev/urandom**

```bash
head -c 32 /dev/urandom | base64
```

**Méthode 3 : Avec Python**

```bash
python3 -c "import secrets; print(secrets.token_urlsafe(32))"
```

#### Sur Windows

**Méthode 1 : Avec PowerShell**

```powershell
# Génération d'une clé aléatoire 32 octets en base64
[Convert]::ToBase64String((New-Object Security.Cryptography.RNGCryptoServiceProvider).GetBytes(32))
```

**Méthode 2 : Avec OpenSSL (si installé)**

```powershell
openssl rand -base64 32
```

**Méthode 3 : Avec Python**

```powershell
python -c "import secrets; print(secrets.token_urlsafe(32))"
```

#### Utilisation de la Clé Générée

Une fois la clé générée, définissez-la comme variable d'environnement :

**Linux / macOS :**
```bash
export ENCRYPT_KEY="xK9mN2pQ5rS8tV1wY4zB7cD0eF3gH6iJ9kL2mN5oP8qR1sT4uV7wX0zA3bC6dE9fG"
```

**Windows (PowerShell) :**
```powershell
$env:ENCRYPT_KEY="xK9mN2pQ5rS8tV1wY4zB7cD0eF3gH6iJ9kL2mN5oP8qR1sT4uV7wX0zA3bC6dE9fG"
```

**Windows (CMD) :**
```cmd
set ENCRYPT_KEY=xK9mN2pQ5rS8tV1wY4zB7cD0eF3gH6iJ9kL2mN5oP8qR1sT4uV7wX0zA3bC6dE9fG
```

**Pour persister la variable d'environnement (Windows) :**
```powershell
setx ENCRYPT_KEY "xK9mN2pQ5rS8tV1wY4zB7cD0eF3gH6iJ9kL2mN5oP8qR1sT4uV7wX0zA3bC6dE9fG"
```

**Pour persister la variable d'environnement (Linux - ajouter à ~/.bashrc ou ~/.zshrc) :**
```bash
echo 'export ENCRYPT_KEY="xK9mN2pQ5rS8tV1wY4zB7cD0eF3gH6iJ9kL2mN5oP8qR1sT4uV7wX0zA3bC6dE9fG"' >> ~/.bashrc
source ~/.bashrc
```

## Chiffrement avec Keystore RSA

Pour une sécurité renforcée en production, Spring Cloud Config supporte l'utilisation d'un Java Keystore (JKS) avec des clés asymétriques RSA.

### Configuration du Keystore

Configurez le keystore dans le fichier `application.yml` du config server :

```yaml
encrypt:
  key-store:
    location: config-server.jks
    password: mypass
    alias: config-server-key
```

**Avantages du keystore :**
- ✅ Chiffrement asymétrique plus sécurisé (RSA)
- ✅ Gestion centralisée des clés
- ✅ Support des certificats
- ✅ Séparation des clés publiques/privées
- ✅ Rotation des clés plus facile

### Génération d'un Keystore

Pour utiliser la méthode keystore, vous devez d'abord générer un fichier JKS avec une paire de clés RSA :

#### Génération avec keytool

```bash
keytool -genkeypair -alias config-server-key \
  -keyalg RSA \
  -keysize 2048 \
  -keystore config-server.jks \
  -storepass mypass \
  -validity 3650
```

**Paramètres :**
- `-alias` : Nom de la clé dans le keystore (doit correspondre à la configuration)
- `-keyalg RSA` : Algorithme de chiffrement asymétrique
- `-keysize 2048` : Taille de la clé (2048 ou 4096 bits)
- `-keystore` : Nom du fichier keystore à créer
- `-storepass` : Mot de passe du keystore (doit correspondre à la configuration)
- `-validity` : Validité en jours (3650 = 10 ans)

**Exemple interactif :**
```bash
keytool -genkeypair -alias config-server-key -keyalg RSA -keysize 2048 -keystore config-server.jks
```

Vous serez invité à entrer :
- Le mot de passe du keystore
- Vos informations (nom, organisation, etc.)

**Exemple non-interactif (recommandé pour les scripts) :**
```bash
keytool -genkeypair -alias config-server-key \
  -keyalg RSA \
  -dname "CN=Config Server,OU=Spring Cloud,O=Company" \
  -keypass mypass \
  -keystore config-server.jks \
  -storepass mypass
```

**Paramètres supplémentaires :**
- `-dname` : Distinguished Name (CN=Common Name, OU=Organizational Unit, O=Organization)
- `-keypass` : Mot de passe de la clé privée (peut être le même que storepass)

#### Vérifier le contenu du keystore

```bash
keytool -list -v -keystore config-server.jks
```

#### Exporter le certificat public (optionnel)

Pour partager la clé publique avec les clients :

```bash
keytool -export -alias config-server-key \
  -keystore config-server.jks \
  -file config-server.cer
```

### Endpoints de Chiffrement/Déchiffrement

Le config server expose des endpoints REST pour chiffrer et déchiffrer des valeurs :

#### Endpoint de Chiffrement

```bash
POST http://localhost:8888/encrypt
Content-Type: text/plain

ma-valeur-sensible
```

**Réponse :**
```
AQA5zW6/7X9kLmNqP3rS5tU8vW2xY4zB6cD8eF0gH2iJ4kL6mN8oP0qR2sT4uV6wX8yZ0
```

#### Endpoint de Déchiffrement

```bash
POST http://localhost:8888/decrypt
Content-Type: text/plain

AQA5zW6/7X9kLmNqP3rS5tU8vW2xY4zB6cD8eF0gH2iJ4kL6mN8oP0qR2sT4uV6wX8yZ0
```

**Réponse :**
```
ma-valeur-sensible
```

#### Utilisation avec cURL

```bash
# Chiffrer une valeur
curl -X POST http://localhost:8888/encrypt -d "mon-mot-de-passe"

# Déchiffrer une valeur
curl -X POST http://localhost:8888/decrypt -d "AQA5zW6/7X9kLmNqP3rS5tU8vW2xY4zB6cD8eF0gH2iJ4kL6mN8oP0qR2sT4uV6wX8yZ0"
```

### Utilisation du Prefixe {cipher}

Dans vos fichiers de configuration YAML/properties, utilisez le préfixe `{cipher}` pour indiquer qu'une valeur est chiffrée :

```yaml
spring:
  datasource:
    password: '{cipher}AQA5zW6/7X9kLmNqP3rS5tU8vW2xY4zB6cD8eF0gH2iJ4kL6mN8oP0qR2sT4uV6wX8yZ0'
```

```properties
spring.datasource.password={cipher}AQA5zW6/7X9kLmNqP3rS5tU8vW2xY4zB6cD8eF0gH2iJ4kL6mN8oP0qR2sT4uV6wX8yZ0
```

Lorsqu'un client récupère la configuration, le config server déchiffre automatiquement les valeurs préfixées par `{cipher}` avant de les envoyer au client.

### Dépendance Requise

Assurez-vous d'avoir la dépendance suivante dans votre `../additionnal/config-server/pom.xml` :

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bootstrap</artifactId>
</dependency>
```

### Processus Complet de Chiffrement

1. **Générer une clé de chiffrement** (en production, utilisez une clé forte et sécurisée)
2. **Configurer la clé** dans le config server (via variable d'environnement)
3. **Chiffrer la valeur sensible** via l'endpoint `/encrypt`
4. **Ajouter la valeur chiffrée** dans vos fichiers de configuration avec le préfixe `{cipher}`
5. **Commit les changements** dans le dépôt Git
6. **Les clients déchiffrent automatiquement** les valeurs au chargement

### Exemple Complet

**Étape 1 : Chiffrer un mot de passe**

```bash
curl -X POST http://localhost:8888/encrypt -d "SecretPassword123!"
```

**Réponse :**
```
AQBpX9zY2wV4kL8mN6oP0qR2sT4uV6wX8yZ0aB2cD4eF6gH8iJ0kL2mN4oP6qR8sT0
```

**Étape 2 : Ajouter au fichier de configuration**

Dans `config-prod.yml` :
```yaml
database:
  url: jdbc:postgresql://prod-db:5432/ecom
  username: admin
  password: '{cipher}AQBpX9zY2wV4kL8mN6oP0qR2sT4uV6wX8yZ0aB2cD4eF6gH8iJ0kL2mN4oP6qR8sT0'
```

**Étape 3 : Vérifier**

```bash
curl http://localhost:8888/config/prod
```

La réponse montrera le mot de passe déchiffré :
```json
{
  "propertySources": [
    {
      "source": {
        "database.password": "SecretPassword123!"
      }
    }
  ]
}
```

### Bonnes Pratiques de Sécurité

- ✅ **Utiliser des clés fortes** : Minimum 256 bits pour AES
- ✅ **Ne jamais commiter la clé** dans le dépôt Git
- ✅ **Utiliser des variables d'environnement** pour stocker la clé
- ✅ **Rotation régulière des clés** : Changez la clé périodiquement
- ✅ **Chiffrer toutes les données sensibles** : Mots de passe, tokens, clés API
- ✅ **Utiliser des clés différentes par environnement** : Dev, staging, prod
- ✅ **Sauvegarder les clés** de manière sécurisée (vault, secret manager)
- ✅ **Limiter l'accès** aux endpoints `/encrypt` et `/decrypt` en production

### Sécurisation des Endpoints en Production

En production, il est recommandé de désactiver les endpoints de chiffrement/déchiffrement ou de les sécuriser :

```yaml
management:
  endpoint:
    encrypt:
      enabled: false
    decrypt:
      enabled: false
```

Ou utilisez Spring Security pour restreindre l'accès :

```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> authz
            .requestMatchers("/encrypt", "/decrypt").hasRole("ADMIN")
            .anyRequest().authenticated()
        );
        return http.build();
    }
}
```

### Dépannage du Chiffrement

#### Erreur : "Cannot decrypt"

Vérifiez que :
- La clé de chiffrement est correctement configurée
- La valeur chiffrée n'a pas été modifiée
- Le préfixe `{cipher}` est présent

#### Erreur : "Key not found"

Vérifiez que :
- La propriété `encrypt.key` est définie
- La variable d'environnement `ENCRYPT_KEY` est définie si utilisée

#### Les valeurs ne sont pas déchiffrées

Vérifiez que :
- Le préfixe `{cipher}` est correctement formaté (avec les guillemets simples en YAML)
- La dépendance `spring-cloud-starter-bootstrap` est présente chez le client

## Mise à Jour Dynamique avec Spring Cloud Bus

Spring Cloud Bus permet de propager les changements de configuration à tous les microservices de manière automatique via un message broker comme RabbitMQ. Cela évite d'avoir à appeler l'endpoint `/refresh` sur chaque service individuellement.

### Configuration du Config Server

Ajoutez les dépendances suivantes au `../additionnal/config-server/pom.xml` du config server :

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-bus</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream-binder-rabbit</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

Configurez RabbitMQ dans `application.yml` :

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

management:
  endpoints:
    web:
      exposure:
        include: "*"
```

### Configuration des Clients

Ajoutez les mêmes dépendances au `../additionnal/config-server/pom.xml` de chaque client :

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-bus</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream-binder-rabbit</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

Exposez les endpoints dans `application.yaml` du client :

```yaml
management:
  endpoints:
    web:
      exposure:
        include: refresh,busrefresh
```

### Utilisation de @RefreshScope

Pour que les beans soient rafraîchis dynamiquement, ajoutez l'annotation `@RefreshScope` sur les beans qui utilisent des propriétés de configuration :

```java
@Component
@ConfigurationProperties(prefix = "build")
@RefreshScope
@Data
public class BuildInfo {
    private String id;
    private String version;
    private String name;
    // ...
}
```

Pour les contrôleurs qui injectent ces beans :

```java
@RestController
@RequestMapping("/build-info")
@RefreshScope
public class BuildInfoController {
    private final BuildInfo buildInfo;

    public BuildInfoController(BuildInfo buildInfo) {
        this.buildInfo = buildInfo;
    }
    // ...
}
```

### Rafraîchissement de la Configuration

#### Rafraîchissement Local (Un seul service)

```bash
POST http://localhost:8080/actuator/refresh
```

Cela rafraîchit uniquement le service sur lequel l'endpoint est appelé.

#### Rafraîchissement via Bus (Tous les services)

```bash
POST http://localhost:8080/actuator/busrefresh
```

Cela envoie un événement sur RabbitMQ qui propage le rafraîchissement à tous les services connectés au bus.

#### Rafraîchissement ciblé (Par application ou profil)

```bash
# Rafraîchir tous les services avec le nom "config"
POST http://localhost:8080/actuator/busrefresh?destination=config:**

# Rafraîchir tous les services avec le profil "dev"
POST http://localhost:8080/actuator/busrefresh?destination=**:dev
```

### Configuration RabbitMQ avec Docker

Utilisez ce conteneur dans `../docker-compose.yaml` :

```yaml
rabbitmq:
  image: rabbitmq:latest
  container_name: rabbitmq
  restart: unless-stopped
  ports:
    - "5672:5672"
    - "15672:15672"
  environment:
    RABBITMQ_DEFAULT_USER: guest
    RABBITMQ_DEFAULT_PASS: guest
  configs:
    - source: rabbitmq-plugins
      target: /etc/rabbitmq/enabled_plugins
  volumes:
    - rabbitmq-lib:/var/lib/rabbitmq/
    - rabbitmq-log:/var/log/rabbitmq
  networks:
    - db
  healthcheck:
    test: ["CMD", "rabbitmq-diagnostics", "ping"]
    interval: 10s
    timeout: 5s
    retries: 5

configs:
  rabbitmq-plugins:
    content: "[rabbitmq_management]."

volumes:
  rabbitmq-lib:
    driver: local
  rabbitmq-log:
    driver: local
```

L'interface de management RabbitMQ est accessible sur `http://localhost:15672` (guest/guest).

### Avantages de Spring Cloud Bus

- ✅ **Propagation automatique** : Un seul appel rafraîchit tous les services
- ✅ **Scalabilité** : Fonctionne avec des centaines de microservices
- ✅ **Découplage** : Les services n'ont pas besoin de se connaître
- ✅ **Fiabilité** : RabbitMQ garantit la livraison des messages
- ✅ **Flexibilité** : Ciblage par application, profil ou instance

### Dépannage du Bus

#### Les services ne se rafraîchissent pas

Vérifiez que :
- RabbitMQ est démarré et accessible
- Tous les services ont les dépendances Spring Cloud Bus
- L'endpoint `busrefresh` est exposé
- Les services sont connectés au même exchange RabbitMQ

#### Erreur de connexion RabbitMQ

Vérifiez que :
- Les identifiants RabbitMQ sont corrects
- Le port 5672 n'est pas bloqué par un firewall
- RabbitMQ accepte les connexions externes

#### Bean non rafraîchi

Vérifiez que :
- Le bean a l'annotation `@RefreshScope`
- Les propriétés utilisent `@ConfigurationProperties`
- Le bean est injecté avec `@Autowired` ou via le constructeur

![img.png](../additionnal/config-server/img.png)

## Bonnes Pratiques

- ✅ Ne pas inclure `server.port` dans les fichiers de configuration distribués aux clients
- ✅ Utiliser des branches Git séparées pour les environnements (dev, prod)
- ✅ Chiffrer les configurations sensibles (mots de passe, clés API)
- ✅ Documenter les fichiers de configuration dans le dépôt Git
- ✅ Utiliser des noms d'application cohérents entre les microservices
- ✅ Tester les configurations avant de les déployer en production
- ✅ Utiliser Spring Cloud Bus pour les mises à jour dynamiques en production
- ✅ Ajouter `@RefreshScope` sur tous les beans qui utilisent des propriétés de configuration

## Dépannage

### Le serveur ne démarre pas

Vérifiez que :
- Le port 8888 n'est pas déjà utilisé
- La connexion au dépôt Git est possible
- Les dépendances Maven sont correctement installées

### Les clients ne récupèrent pas la configuration

Vérifiez que :
- Le config server est démarré et accessible
- Le nom de l'application correspond au fichier de configuration
- Le profil est correctement défini chez le client
- Le fichier de configuration existe dans le dépôt Git

### Erreur de connexion Git

Vérifiez que :
- L'URL du dépôt Git est correcte
- Les credentials sont configurés si le dépôt est privé
- La branche spécifiée existe dans le dépôt
