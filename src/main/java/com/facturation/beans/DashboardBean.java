package com.facturation.beans;

import com.facturation.entities.Client;
import com.facturation.entities.Facture;
import com.facturation.entities.Facture.StatutFacture;
import com.facturation.services.RapportService;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;

@Named("dashboardBean")
@SessionScoped
public class DashboardBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @EJB
    private RapportService rapportService;
    
    private Map<String, Object> tableauDeBord;
    private BigDecimal caMois;
    private BigDecimal caAnnee;
    private long nbFacturesTotal;
    private long nbFacturesEnRetard;
    private BigDecimal montantImpaye;
    private long nbClients;
    private List<Facture> dernieresFactures;
    private List<Object[]> topClients;
    private Map<StatutFacture, Long> statsParStatut;
    private Map<Month, BigDecimal> caParMois;
    
    @PostConstruct
    public void init() {
        try {
            System.out.println("=== Initialisation du DashboardBean ===");
            
            // Vérifier que le service est injecté
            if (rapportService == null) {
                System.err.println("❌ ERREUR : RapportService n'est pas injecté !");
                initialiserValeursParDefaut();
                return;
            }
            
            System.out.println("✅ RapportService injecté correctement");
            
            // Charger les données
            chargerTableauDeBord();
            chargerCAParMois();
            
            System.out.println("✅ DashboardBean initialisé avec succès");
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR lors de l'initialisation du DashboardBean :");
            e.printStackTrace();
            // Initialiser avec des valeurs par défaut pour éviter le crash
            initialiserValeursParDefaut();
        }
    }
    
    /**
     * Initialise les valeurs par défaut en cas d'erreur
     */
    private void initialiserValeursParDefaut() {
        caMois = BigDecimal.ZERO;
        caAnnee = BigDecimal.ZERO;
        nbFacturesTotal = 0;
        nbFacturesEnRetard = 0;
        montantImpaye = BigDecimal.ZERO;
        nbClients = 0;
        dernieresFactures = new java.util.ArrayList<>();
        topClients = new java.util.ArrayList<>();
        statsParStatut = new java.util.HashMap<>();
        caParMois = new java.util.HashMap<>();
    }
    
    /**
     * Charge les données du tableau de bord
     */
    public void chargerTableauDeBord() {
        try {
            System.out.println("Chargement du tableau de bord...");
            tableauDeBord = rapportService.getTableauDeBord();
            
            // Extraire les données avec vérification null
            caMois = tableauDeBord != null && tableauDeBord.get("caMois") != null 
                ? (BigDecimal) tableauDeBord.get("caMois") 
                : BigDecimal.ZERO;
                
            caAnnee = tableauDeBord != null && tableauDeBord.get("caAnnee") != null 
                ? (BigDecimal) tableauDeBord.get("caAnnee") 
                : BigDecimal.ZERO;
                
            nbFacturesTotal = tableauDeBord != null && tableauDeBord.get("nbFacturesTotal") != null 
                ? (Long) tableauDeBord.get("nbFacturesTotal") 
                : 0L;
                
            nbFacturesEnRetard = tableauDeBord != null && tableauDeBord.get("nbFacturesEnRetard") != null 
                ? (Long) tableauDeBord.get("nbFacturesEnRetard") 
                : 0L;
                
            montantImpaye = tableauDeBord != null && tableauDeBord.get("montantImpaye") != null 
                ? (BigDecimal) tableauDeBord.get("montantImpaye") 
                : BigDecimal.ZERO;
                
            nbClients = tableauDeBord != null && tableauDeBord.get("nbClients") != null 
                ? (Long) tableauDeBord.get("nbClients") 
                : 0L;
                
            dernieresFactures = tableauDeBord != null && tableauDeBord.get("dernieresFactures") != null 
                ? (List<Facture>) tableauDeBord.get("dernieresFactures") 
                : new java.util.ArrayList<>();
                
            topClients = tableauDeBord != null && tableauDeBord.get("topClients") != null 
                ? (List<Object[]>) tableauDeBord.get("topClients") 
                : new java.util.ArrayList<>();
                
            statsParStatut = tableauDeBord != null && tableauDeBord.get("statsParStatut") != null 
                ? (Map<StatutFacture, Long>) tableauDeBord.get("statsParStatut") 
                : new java.util.HashMap<>();
                
            System.out.println("✅ Tableau de bord chargé : " + nbClients + " clients, " + nbFacturesTotal + " factures");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement du tableau de bord");
            e.printStackTrace();
            initialiserValeursParDefaut();
        }
    }
    
    /**
     * Charge le CA par mois de l'année en cours
     */
    public void chargerCAParMois() {
        try {
            System.out.println("Chargement du CA par mois...");
            int anneeActuelle = LocalDate.now().getYear();
            caParMois = rapportService.getCAParMois(anneeActuelle);
            if (caParMois == null) {
                caParMois = new java.util.HashMap<>();
            }
            System.out.println("✅ CA par mois chargé pour l'année " + anneeActuelle);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement du CA par mois");
            e.printStackTrace();
            caParMois = new java.util.HashMap<>();
        }
    }
    
    /**
     * Rafraîchit le tableau de bord
     */
    public void rafraichir() {
        chargerTableauDeBord();
        chargerCAParMois();
    }
    
    /**
     * Retourne le nombre de factures pour un statut donné
     */
    public long getNbFacturesParStatut(StatutFacture statut) {
        return statsParStatut.getOrDefault(statut, 0L);
    }
    
    /**
     * Formate un client avec son CA
     */
    public String formatClientCA(Object[] data) {
        if (data != null && data.length == 2) {
            Client client = (Client) data[0];
            BigDecimal ca = (BigDecimal) data[1];
            return client.getNomComplet() + " - " + ca + " €";
        }
        return "";
    }
    
    // Getters sécurisés
    public BigDecimal getCaMois() {
        return caMois != null ? caMois : BigDecimal.ZERO;
    }
    
    public BigDecimal getCaAnnee() {
        return caAnnee != null ? caAnnee : BigDecimal.ZERO;
    }
    
    public long getNbFacturesTotal() {
        return nbFacturesTotal;
    }
    
    public long getNbFacturesEnRetard() {
        return nbFacturesEnRetard;
    }
    
    public BigDecimal getMontantImpaye() {
        return montantImpaye != null ? montantImpaye : BigDecimal.ZERO;
    }
    
    public long getNbClients() {
        return nbClients;
    }
    
    public List<Facture> getDernieresFactures() {
        return dernieresFactures != null ? dernieresFactures : new java.util.ArrayList<>();
    }
    
    public List<Object[]> getTopClients() {
        return topClients != null ? topClients : new java.util.ArrayList<>();
    }
    
    public Map<StatutFacture, Long> getStatsParStatut() {
        return statsParStatut != null ? statsParStatut : new java.util.HashMap<>();
    }
    
    public Map<Month, BigDecimal> getCaParMois() {
        return caParMois != null ? caParMois : new java.util.HashMap<>();
    }
}
