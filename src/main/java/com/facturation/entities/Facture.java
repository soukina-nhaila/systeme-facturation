package com.facturation.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "factures")
@NamedQueries({
    @NamedQuery(name = "Facture.findAll", 
                query = "SELECT f FROM Facture f ORDER BY f.dateFacture DESC"),
    @NamedQuery(name = "Facture.findByNumero", 
                query = "SELECT f FROM Facture f WHERE f.numeroFacture = :numero"),
    @NamedQuery(name = "Facture.findByClient", 
                query = "SELECT f FROM Facture f WHERE f.client.id = :clientId ORDER BY f.dateFacture DESC"),
    @NamedQuery(name = "Facture.findByStatut", 
                query = "SELECT f FROM Facture f WHERE f.statut = :statut ORDER BY f.dateFacture DESC"),
    @NamedQuery(name = "Facture.findByPeriode", 
                query = "SELECT f FROM Facture f WHERE f.dateFacture BETWEEN :dateDebut AND :dateFin ORDER BY f.dateFacture DESC")
})
public class Facture implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String numeroFacture;
    
    @Column(nullable = false)
    private LocalDate dateFacture;
    
    @Column(nullable = false)
    private LocalDate dateEcheance;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<LigneFacture> lignes = new ArrayList<>();
    
    @Column(precision = 10, scale = 2)
    private BigDecimal montantHT;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal montantTVA;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal montantTTC;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutFacture statut = StatutFacture.BROUILLON;
    
    @Column(length = 1000)
    private String remarques;
    
    // Constructeurs
    public Facture() {
        this.dateFacture = LocalDate.now();
        this.dateEcheance = LocalDate.now().plusDays(30); // Échéance à 30 jours par défaut
    }
    
    public Facture(String numeroFacture, Client client) {
        this();
        this.numeroFacture = numeroFacture;
        this.client = client;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNumeroFacture() {
        return numeroFacture;
    }
    
    public void setNumeroFacture(String numeroFacture) {
        this.numeroFacture = numeroFacture;
    }
    
    public LocalDate getDateFacture() {
        return dateFacture;
    }
    
    public void setDateFacture(LocalDate dateFacture) {
        this.dateFacture = dateFacture;
    }
    
    public LocalDate getDateEcheance() {
        return dateEcheance;
    }
    
    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }
    
    public Client getClient() {
        return client;
    }
    
    public void setClient(Client client) {
        this.client = client;
    }
    
    public List<LigneFacture> getLignes() {
        return lignes;
    }
    
    public void setLignes(List<LigneFacture> lignes) {
        this.lignes = lignes;
    }
    
    public BigDecimal getMontantHT() {
        return montantHT;
    }
    
    public void setMontantHT(BigDecimal montantHT) {
        this.montantHT = montantHT;
    }
    
    public BigDecimal getMontantTVA() {
        return montantTVA;
    }
    
    public void setMontantTVA(BigDecimal montantTVA) {
        this.montantTVA = montantTVA;
    }
    
    public BigDecimal getMontantTTC() {
        return montantTTC;
    }
    
    public void setMontantTTC(BigDecimal montantTTC) {
        this.montantTTC = montantTTC;
    }
    
    public StatutFacture getStatut() {
        return statut;
    }
    
    public void setStatut(StatutFacture statut) {
        this.statut = statut;
    }
    
    public String getRemarques() {
        return remarques;
    }
    
    public void setRemarques(String remarques) {
        this.remarques = remarques;
    }
    
    // Méthodes utilitaires
    public void ajouterLigne(LigneFacture ligne) {
        lignes.add(ligne);
        ligne.setFacture(this);
    }
    
    public void supprimerLigne(LigneFacture ligne) {
        lignes.remove(ligne);
        ligne.setFacture(null);
    }
    
    public void calculerMontants() {
        montantHT = BigDecimal.ZERO;
        montantTVA = BigDecimal.ZERO;
        
        for (LigneFacture ligne : lignes) {
            montantHT = montantHT.add(ligne.getMontantHT());
            montantTVA = montantTVA.add(ligne.getMontantTVA());
        }
        
        montantTTC = montantHT.add(montantTVA);
    }
    
    @Override
    public String toString() {
        return "Facture{" +
                "id=" + id +
                ", numeroFacture='" + numeroFacture + '\'' +
                ", dateFacture=" + dateFacture +
                ", montantTTC=" + montantTTC +
                ", statut=" + statut +
                '}';
    }
    
    // Énumération pour le statut
    public enum StatutFacture {
        BROUILLON("Brouillon"),
        VALIDEE("Validée"),
        ENVOYEE("Envoyée"),
        PAYEE("Payée"),
        ANNULEE("Annulée");
        
        private String libelle;
        
        StatutFacture(String libelle) {
            this.libelle = libelle;
        }
        
        public String getLibelle() {
            return libelle;
        }
    }
}
