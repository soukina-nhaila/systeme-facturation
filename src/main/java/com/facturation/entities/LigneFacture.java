package com.facturation.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "lignes_facture")
public class LigneFacture implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id", nullable = false)
    private Facture facture;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantite;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaireHT;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxTVA;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal remise = BigDecimal.ZERO; // Remise en pourcentage
    
    @Column(precision = 10, scale = 2)
    private BigDecimal montantHT;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal montantTVA;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal montantTTC;
    
    // Constructeurs
    public LigneFacture() {
    }
    
    public LigneFacture(Produit produit, BigDecimal quantite) {
        this.produit = produit;
        this.quantite = quantite;
        this.prixUnitaireHT = produit.getPrixUnitaireHT();
        this.tauxTVA = produit.getTauxTVA();
        calculerMontants();
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Facture getFacture() {
        return facture;
    }
    
    public void setFacture(Facture facture) {
        this.facture = facture;
    }
    
    public Produit getProduit() {
        return produit;
    }
    
    public void setProduit(Produit produit) {
        this.produit = produit;
        if (produit != null) {
            this.prixUnitaireHT = produit.getPrixUnitaireHT();
            this.tauxTVA = produit.getTauxTVA();
        }
    }
    
    public BigDecimal getQuantite() {
        return quantite;
    }
    
    public void setQuantite(BigDecimal quantite) {
        this.quantite = quantite;
        calculerMontants();
    }
    
    public BigDecimal getPrixUnitaireHT() {
        return prixUnitaireHT;
    }
    
    public void setPrixUnitaireHT(BigDecimal prixUnitaireHT) {
        this.prixUnitaireHT = prixUnitaireHT;
        calculerMontants();
    }
    
    public BigDecimal getTauxTVA() {
        return tauxTVA;
    }
    
    public void setTauxTVA(BigDecimal tauxTVA) {
        this.tauxTVA = tauxTVA;
        calculerMontants();
    }
    
    public BigDecimal getRemise() {
        return remise;
    }
    
    public void setRemise(BigDecimal remise) {
        this.remise = remise;
        calculerMontants();
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
    
    // Méthodes de calcul
    public void calculerMontants() {
        if (quantite == null || prixUnitaireHT == null || tauxTVA == null) {
            return;
        }
        
        // Calcul du montant HT brut
        BigDecimal montantBrut = prixUnitaireHT.multiply(quantite);
        
        // Application de la remise
        BigDecimal montantRemise = BigDecimal.ZERO;
        if (remise != null && remise.compareTo(BigDecimal.ZERO) > 0) {
            montantRemise = montantBrut.multiply(remise).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
        
        // Montant HT après remise
        montantHT = montantBrut.subtract(montantRemise).setScale(2, RoundingMode.HALF_UP);
        
        // Calcul de la TVA
        montantTVA = montantHT.multiply(tauxTVA)
                              .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        
        // Calcul du montant TTC
        montantTTC = montantHT.add(montantTVA).setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String toString() {
        return "LigneFacture{" +
                "id=" + id +
                ", produit=" + (produit != null ? produit.getDesignation() : "null") +
                ", quantite=" + quantite +
                ", montantTTC=" + montantTTC +
                '}';
    }
}
