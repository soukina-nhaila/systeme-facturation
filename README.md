# Système de Facturation - Application JEE

Application de gestion des factures clients développée avec EJB, JPA et JSF.

## 📋 Prérequis

- **JDK 11** ou supérieur
- **Maven 3.6+**
- **Serveur d'application Java EE** (WildFly, GlassFish, Payara, ou TomEE)
- **Base de données** MySQL 8.0+ (ou H2 pour les tests)

## 🏗️ Architecture

### Couche Persistance (JPA)
- **Entités** : `Client`, `Produit`, `Facture`, `LigneFacture`
- **Relations** : @OneToMany, @ManyToOne
- **Configuration** : `persistence.xml`

### Couche Métier (EJB)
- **ClientService** : Gestion des clients (CRUD)
- **ProduitService** : Gestion des produits (CRUD)
- **FactureService** : Création/modification de factures, calcul automatique des montants
- **RapportService** : Génération de rapports (CA, top clients, statistiques)

### Couche Présentation (JSF + PrimeFaces)
- **ClientBean** : Managed Bean pour les clients
- **ProduitBean** : Managed Bean pour les produits
- **FactureBean** : Managed Bean pour les factures
- **DashboardBean** : Managed Bean pour le tableau de bord
- **Pages XHTML** : Interface utilisateur avec PrimeFaces

## 📦 Installation

### 1. Cloner/Télécharger le projet

```bash
cd systeme-facturation
```

### 2. Compiler le projet avec Maven

```bash
mvn clean install
```

Cela créera un fichier WAR dans `target/systeme-facturation.war`

### 3. Configuration de la base de données

#### Option A : MySQL (Production)

1. Créer une base de données :
```sql
CREATE DATABASE facturation CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'facturation_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON facturation.* TO 'facturation_user'@'localhost';
FLUSH PRIVILEGES;
```

2. Configurer la source de données dans votre serveur d'application :

**WildFly** (`standalone.xml`) :
```xml
<datasource jndi-name="java:/FacturationDS" pool-name="FacturationDS">
    <connection-url>jdbc:mysql://localhost:3306/facturation</connection-url>
    <driver>mysql</driver>
    <security>
        <user-name>facturation_user</user-name>
        <password>password</password>
    </security>
</datasource>
```

**GlassFish/Payara** :
```bash
asadmin create-jdbc-connection-pool --datasourceclassname com.mysql.cj.jdbc.MysqlDataSource \
  --restype javax.sql.DataSource \
  --property user=facturation_user:password=password:serverName=localhost:portNumber=3306:databaseName=facturation \
  FacturationPool

asadmin create-jdbc-resource --connectionpoolid FacturationPool java:/FacturationDS
```

#### Option B : H2 (Tests/Développement)

Le projet inclut une configuration H2 en mémoire. Modifiez `persistence.xml` pour utiliser l'unité de persistance `FacturationPU-H2`.

### 4. Déployer l'application

#### WildFly
```bash
cp target/systeme-facturation.war $WILDFLY_HOME/standalone/deployments/
```

#### GlassFish/Payara
```bash
asadmin deploy target/systeme-facturation.war
```

#### TomEE
```bash
cp target/systeme-facturation.war $TOMEE_HOME/webapps/
```

### 5. Accéder à l'application

Ouvrez votre navigateur et allez à :
```
http://localhost:8080/systeme-facturation/
```

## 🎯 Fonctionnalités

### ✅ Gestion des Clients
- Création, modification, suppression de clients
- Recherche et filtrage
- Validation des emails uniques
- Informations complètes (adresse, TVA, etc.)

### ✅ Gestion des Produits
- CRUD complet des produits
- Prix HT, TVA, prix TTC
- Activation/désactivation des produits
- Recherche par désignation

### ✅ Gestion des Factures
- Création de factures avec sélection de client
- Ajout de lignes de facture (produit, quantité, remise)
- **Calcul automatique** des montants (HT, TVA, TTC)
- Workflow de statuts : Brouillon → Validée → Envoyée → Payée
- Génération automatique de numéros de facture
- Dates de facture et d'échéance

### ✅ Tableau de Bord
- KPIs : CA du mois, CA de l'année, nombre de factures
- Factures en retard et montants impayés
- Dernières factures créées
- Top 5 clients par chiffre d'affaires
- Statistiques par statut

### ✅ Rapports
- Chiffre d'affaires par période
- CA par mois
- Top clients
- Factures en retard
- Statistiques globales

## 📁 Structure du Projet

```
systeme-facturation/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/facturation/
│   │   │       ├── entities/          # Entités JPA
│   │   │       │   ├── Client.java
│   │   │       │   ├── Produit.java
│   │   │       │   ├── Facture.java
│   │   │       │   └── LigneFacture.java
│   │   │       ├── services/          # EJB Services
│   │   │       │   ├── ClientService.java
│   │   │       │   ├── ProduitService.java
│   │   │       │   ├── FactureService.java
│   │   │       │   └── RapportService.java
│   │   │       └── beans/             # Managed Beans JSF
│   │   │           ├── ClientBean.java
│   │   │           ├── ProduitBean.java
│   │   │           ├── FactureBean.java
│   │   │           └── DashboardBean.java
│   │   ├── resources/
│   │   │   └── META-INF/
│   │   │       └── persistence.xml
│   │   └── webapp/
│   │       ├── pages/                 # Pages JSF
│   │       │   ├── dashboard.xhtml
│   │       │   ├── clients.xhtml
│   │       │   ├── produits.xhtml
│   │       │   └── factures.xhtml
│   │       ├── templates/
│   │       │   └── template.xhtml
│   │       ├── resources/
│   │       │   └── css/
│   │       │       └── style.css
│   │       └── WEB-INF/
│   │           ├── web.xml
│   │           └── faces-config.xml
│   └── test/                          # Tests unitaires
└── pom.xml
```

## 🔧 Technologies Utilisées

- **Java EE 8**
- **EJB 3.2** (Enterprise JavaBeans)
- **JPA 2.2** (Java Persistence API) avec Hibernate
- **JSF 2.3** (JavaServer Faces)
- **PrimeFaces 12.0** (Composants UI)
- **Maven 3** (Gestion de projet)
- **MySQL 8** / **H2** (Base de données)

## 🎓 Contexte Académique

**Module** : Applications Distribuées  
**Formation** : Master DevOps & Cloud Computing  
**Projet** : Système de Facturation

## 📝 Notes de développement

### Calcul automatique des montants

Le système calcule automatiquement :
- **Montant HT** = (Prix unitaire × Quantité) - Remise
- **Montant TVA** = Montant HT × (Taux TVA / 100)
- **Montant TTC** = Montant HT + Montant TVA

### Workflow des factures

1. **BROUILLON** : Facture en cours de création
2. **VALIDEE** : Facture validée, prête à être envoyée
3. **ENVOYEE** : Facture envoyée au client
4. **PAYEE** : Facture payée par le client
5. **ANNULEE** : Facture annulée

## 🐛 Dépannage

### Problème de connexion à la base de données
- Vérifiez que MySQL est démarré
- Vérifiez les credentials dans la configuration de la datasource
- Vérifiez que la base de données existe

### Erreur au déploiement
- Vérifiez que le serveur d'application supporte Java EE 8
- Vérifiez que la datasource est correctement configurée
- Consultez les logs du serveur

### Interface ne s'affiche pas correctement
- Videz le cache du navigateur
- Vérifiez que PrimeFaces est bien inclus dans le WAR

## 📞 Support

Pour toute question ou problème, consultez la documentation officielle :
- [Java EE Tutorial](https://javaee.github.io/tutorial/)
- [PrimeFaces Documentation](https://www.primefaces.org/documentation/)
- [Hibernate Documentation](https://hibernate.org/orm/documentation/)

## 📄 Licence

Projet académique - Master DevOps & Cloud Computing 2026
