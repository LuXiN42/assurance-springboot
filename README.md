# B'Care : plateforme de gestion d'assurance (version Spring Boot)

Projet J2EE réalisé en binôme (Jilani Mohamed et Loïc Robert), ING2 CY Tech, 2026-2027.

B'Care est une application web de gestion d'une compagnie d'assurance avec trois profils : client, agent et administrateur. Cette version utilise Spring Boot, Spring Data JPA, Spring Security et Thymeleaf. La version Servlets/JSP/Hibernate du projet est dans un dépôt séparé : https://github.com/LuXiN42/projet-assurance-j2ee

## Technologies
- Java 17, Maven
- Spring Boot (Spring MVC, Spring Data JPA, Spring Security)
- Thymeleaf
- Hibernate
- MySQL 8
- Serveur Tomcat embarqué (aucune installation séparée nécessaire)
- API Gemini pour l'assistant Bibi

## Lancer le projet
### 1. Prérequis
- JDK 17
- Maven 3.8 ou plus (ou le wrapper mvnw fourni)
- MySQL 8

### 2. Récupérer le projet
```bash
git clone https://github.com/LuXiN42/assurance-springboot
cd assurance-springboot
```

### 3. Créer la base de données
#### 3.1 Vérifier que MySQL est installé et démarré
Vérifier l'installation :
```bash
mysql --version
```
Démarrer le service selon votre système :
- Linux : `sudo systemctl start mysql` ; vérifier son état avec `sudo systemctl status mysql`.
- macOS : `brew services start mysql`.
- Windows : ouvrir Services, sélectionner MySQL80 et cliquer sur Démarrer (ou démarrer MySQL via XAMPP/WAMP).

#### 3.2 Se connecter à MySQL en root
Dans un terminal, se connecter avec :
```bash
mysql -u root -p
```
Saisir le mot de passe root défini lors de l'installation. Rien ne s'affiche pendant la saisie du mot de passe. Sous Linux, si l'accès est refusé, essayer :
```bash
sudo mysql
```
Une fois connecté, l'invite devient `mysql>`. On peut aussi utiliser un client graphique (MySQL Workbench, DBeaver ou phpMyAdmin) en se connectant à `localhost:3306` avec l'utilisateur `root`.

#### 3.3 Créer la base et l'utilisateur
Dans l'invite `mysql>`, exécuter :
```sql
CREATE DATABASE assurance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'assurance'@'localhost' IDENTIFIED BY 'assuranceGroupeO';
GRANT ALL PRIVILEGES ON assurance_db.* TO 'assurance'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

#### 3.4 Vérifier la connexion avec le nouvel utilisateur
Dans un terminal, exécuter :
```bash
mysql -u assurance -p assurance_db
```
Saisir le mot de passe `assuranceGroupeO`. Si l'invite `mysql>` s'affiche, la connexion est opérationnelle. Pour quitter MySQL, exécuter `EXIT;`.

#### 3.5 Configurer l'application
La configuration se trouve dans `src/main/resources/application.properties` :
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/assurance_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=assurance
spring.datasource.password=assuranceGroupeO
```
Modifier ces valeurs si vous utilisez un autre utilisateur ou mot de passe.

#### 3.6 Créer les tables et charger les données de démonstration
L'application ne crée pas les tables elle-même (`spring.jpa.hibernate.ddl-auto=validate` : au démarrage, Spring vérifie seulement que les tables correspondent aux entités). Il faut donc lancer les deux scripts **avant** le premier démarrage, dans cet ordre :
```bash
mysql -u assurance -p assurance_db < src/main/sql/schema.sql
mysql -u assurance -p assurance_db < src/main/sql/data.sql
```
`schema.sql` crée les tables, `data.sql` ajoute les données de démonstration (utilisateurs, contrats, sinistres, paiements).

La base est la même que celle de la version J2EE : si elle a déjà été créée pour l'autre version, il n'y a rien à refaire.

#### 3.7 Problèmes courants

| Erreur | Cause probable | Solution |
|---|---|---|
| `Access denied for user 'assurance'` | Mot de passe incorrect ou utilisateur non créé. | Refaire l'étape 3.3. |
| `Unknown database 'assurance_db'` | Base de données non créée. | Refaire l'étape 3.3. |
| `Communications link failure` | MySQL n'est pas démarré. | Refaire l'étape 3.1. |
| `Schema-validation: missing table` | Les scripts SQL n'ont pas été lancés. | Refaire l'étape 3.6. |
| `Public Key Retrieval is not allowed` | Le paramètre de récupération de clé publique manque dans l'URL JDBC. | Vérifier que l'URL contient `allowPublicKeyRetrieval=true`. |

### 4. Clé de l'assistant Bibi (facultatif)
Bibi utilise l'API Gemini. Sans clé, toute l'application marche sauf Bibi, qui affiche un message d'indisponibilité.

1. Créer une clé gratuite sur https://aistudio.google.com/apikey
2. Copier `src/main/resources/ia.properties.example` en `ia.properties` dans le même dossier
3. Remplacer `COLLE_TA_CLE_ICI` par la clé

`ia.properties` est dans le `.gitignore`, la clé ne part jamais sur GitHub. On peut aussi passer la clé par la variable d'environnement `GEMINI_API_KEY`.

### 5. Compiler et lancer
Sous Linux, macOS ou Git Bash :
```bash
./mvnw spring-boot:run
```
Sous Windows (invite de commandes) :
```bash
mvnw.cmd spring-boot:run
```
Ou pour produire un jar exécutable :
```bash
./mvnw clean package
java -jar target/assurance-springboot-0.0.1-SNAPSHOT.jar
```
L'application est ensuite sur http://localhost:8081

## Comptes de test
Tous les comptes ont le mot de passe `Password123!`

| Rôle | Email |
|---|---|
| Administrateur | admin@assurance.fr |
| Agent | agent.dupont@assurance.fr |
| Client | martin.alice@assurance.test |

On peut aussi créer un compte client depuis la page Inscription.

## Fonctionnalités
### Client
- consulter ses contrats et leurs garanties
- déclarer un sinistre avec pièces justificatives, et suivre son avancement
- voir l'historique de ses paiements
- modifier son profil et son mot de passe
- poser ses questions à Bibi, l'assistant qui connaît ses contrats et ses sinistres (bulle sur toutes les pages client)
### Agent
- consulter les clients
- créer, modifier et rechercher des contrats
- gérer le catalogue des garanties
- traiter les sinistres (analyse, acceptation, refus, indemnisation, commentaires)
- enregistrer les paiements
- tableau de bord avec statistiques
### Administrateur
- tout ce que fait un agent
- gérer les utilisateurs (clients, agents, administrateurs)
- gérer les types d'assurance
- statistiques globales

## Organisation du code
```text
src/main/java/com/assurance/assurance_springboot/
├── config/ configuration Spring Security
├── controller/ contrôleurs MVC, rangés par profil (client, agent, admin)
├── dto/ objets de transfert pour les formulaires
├── model/ entités JPA et énumérations
├── repository/ interfaces Spring Data JPA
├── security/ authentification, redirection selon le rôle
└── service/ règles métier et appel à Gemini
src/main/resources/
├── templates/ pages Thymeleaf par profil + fragments communs + pages d'erreur
├── static/ CSS, JavaScript, polices et favicon
└── application.properties
src/main/sql/ scripts de la base (schema.sql, data.sql)
docs/ MCD
```

## Sécurité
- mots de passe hachés avec BCrypt, jamais stockés en clair
- Spring Security restreint l'accès aux URL selon le rôle (`/client/**`, `/agent/**`, `/admin/**`), accès refusé sinon
- protection CSRF activée sur tous les formulaires
- un client ne peut consulter que ses propres contrats, sinistres, paiements et pièces justificatives
- validation côté serveur des formulaires et des fichiers déposés (taille, extension)
- échappement automatique des données affichées par Thymeleaf (protection contre le XSS)
- pages d'erreur personnalisées (403, 404, 500)

## Auteurs
Jilani Mohamed et Loïc Robert, ING2 CY Tech.
