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
Dans un client MySQL connecté en root :
```sql
CREATE DATABASE assurance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'assurance'@'localhost' IDENTIFIED BY 'assuranceGroupeO';
GRANT ALL PRIVILEGES ON assurance_db.* TO 'assurance'@'localhost';
FLUSH PRIVILEGES;
```

Les tables sont créées automatiquement par Hibernate au premier démarrage (`spring.jpa.hibernate.ddl-auto=update`).
Pour charger les données de démonstration, si le script est fourni :
```bash
mysql -u assurance -p assurance_db < src/main/sql/data.sql
```
Pour utiliser un autre utilisateur MySQL, modifier les identifiants dans `src/main/resources/application.properties` ou définir les variables d'environnement `DB_URL`, `DB_USERNAME` et `DB_PASSWORD`.

### 4. Compiler et lancer
```bash
./mvnw spring-boot:run
```
Ou pour produire un jar exécutable :
```bash
./mvnw clean package
java -jar target/assurance-springboot-0.0.1-SNAPSHOT.jar
```
L'application est ensuite sur http://localhost:8081

## Comptes de test
Tous les comptes ont le mot de passe indiqué dans le script de données de démonstration.

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
└── service/ règles métier
src/main/resources/
├── templates/ pages Thymeleaf par profil + fragments communs
├── static/css/ feuille de style
└── application.properties
docs/ MCD
```

## Sécurité
- mots de passe hachés avec BCrypt, jamais stockés en clair
- Spring Security restreint l'accès aux URL selon le rôle (`/client/**`, `/agent/**`, `/admin/**`), accès refusé sinon
- protection CSRF activée sur tous les formulaires
- un client ne peut consulter que ses propres contrats, sinistres, paiements et pièces justificatives
- validation côté serveur des formulaires et des fichiers déposés (taille, extension)
- échappement automatique des données affichées par Thymeleaf (protection contre le XSS)
- page d'accès refusé personnalisée

## Auteurs
Jilani Mohamed et Loïc Robert, ING2 CY Tech.
