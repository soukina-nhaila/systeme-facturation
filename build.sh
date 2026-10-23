#!/bin/bash

# Script de compilation et déploiement rapide
# Système de Facturation

echo "================================================"
echo "  Système de Facturation - Build & Deploy"
echo "================================================"
echo ""

# Couleurs pour l'affichage
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Fonction pour afficher les messages
print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_info() {
    echo -e "${YELLOW}→${NC} $1"
}

# Vérifier que Maven est installé
if ! command -v mvn &> /dev/null; then
    print_error "Maven n'est pas installé. Veuillez l'installer d'abord."
    exit 1
fi

print_success "Maven détecté"

# Nettoyer le projet
print_info "Nettoyage du projet..."
mvn clean
if [ $? -eq 0 ]; then
    print_success "Projet nettoyé"
else
    print_error "Erreur lors du nettoyage"
    exit 1
fi

# Compiler le projet
print_info "Compilation du projet..."
mvn compile
if [ $? -eq 0 ]; then
    print_success "Compilation réussie"
else
    print_error "Erreur lors de la compilation"
    exit 1
fi

# Créer le package WAR
print_info "Création du package WAR..."
mvn package
if [ $? -eq 0 ]; then
    print_success "Package WAR créé : target/systeme-facturation.war"
else
    print_error "Erreur lors de la création du package"
    exit 1
fi

echo ""
echo "================================================"
print_success "BUILD TERMINÉ AVEC SUCCÈS"
echo "================================================"
echo ""
echo "Le fichier WAR se trouve dans : target/systeme-facturation.war"
echo ""
echo "Pour déployer sur votre serveur :"
echo "  - WildFly  : cp target/systeme-facturation.war \$WILDFLY_HOME/standalone/deployments/"
echo "  - Payara   : asadmin deploy target/systeme-facturation.war"
echo "  - TomEE    : cp target/systeme-facturation.war \$TOMEE_HOME/webapps/"
echo ""

# Option : déploiement automatique si serveur détecté
read -p "Voulez-vous déployer automatiquement sur un serveur ? (y/n) " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo ""
    echo "Sélectionnez votre serveur :"
    echo "  1) WildFly"
    echo "  2) Payara/GlassFish"
    echo "  3) TomEE"
    echo "  4) Annuler"
    echo ""
    read -p "Votre choix (1-4): " server_choice
    
    case $server_choice in
        1)
            if [ -z "$WILDFLY_HOME" ]; then
                print_error "Variable WILDFLY_HOME non définie"
                exit 1
            fi
            print_info "Déploiement sur WildFly..."
            cp target/systeme-facturation.war "$WILDFLY_HOME/standalone/deployments/"
            print_success "Application déployée sur WildFly"
            echo "Accédez à : http://localhost:8080/systeme-facturation/"
            ;;
        2)
            print_info "Déploiement sur Payara/GlassFish..."
            asadmin deploy --force=true target/systeme-facturation.war
            if [ $? -eq 0 ]; then
                print_success "Application déployée sur Payara"
                echo "Accédez à : http://localhost:8080/systeme-facturation/"
            else
                print_error "Erreur lors du déploiement"
            fi
            ;;
        3)
            if [ -z "$TOMEE_HOME" ]; then
                print_error "Variable TOMEE_HOME non définie"
                exit 1
            fi
            print_info "Déploiement sur TomEE..."
            cp target/systeme-facturation.war "$TOMEE_HOME/webapps/"
            print_success "Application déployée sur TomEE"
            echo "Accédez à : http://localhost:8080/systeme-facturation/"
            ;;
        4)
            print_info "Déploiement annulé"
            ;;
        *)
            print_error "Choix invalide"
            ;;
    esac
fi

echo ""
print_info "Terminé !"
