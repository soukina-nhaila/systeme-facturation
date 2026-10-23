package com.facturation.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "produits")
@NamedQueries({
    @NamedQuery(name = "Produit.findAll", query = "SELECT p FROM Produit p ORDER BY p.designation"),
    @NamedQuery(name = "Produit.findByDesignation", 
                query = "SELECT p FROM Produit p WHERE p.designation LIKE :designation"),
    @NamedQuery(name = "Produit.findActifs", 
                query = "SELECT p FROM Produit p WHERE p.actif = true ORDER BY p.designation")
})
public class Produit implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String reference;
    
    @Column(nullable = false, length = 255)
    private String designation;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaireHT;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxTVA = new BigDecimal("20.00"); // 20% par défaut
    
    @Column(nullable = false)
    private Boolean actif = true;
    
    @Column(length = 50)
    private String unite = "Unité"; // Unité, Kg, L, etc.
    
    // Constructeurs
    public Produit() {
    }
    
    public Produit(String reference, String designation, BigDecimal prixUnitaireHT) {
        this.reference = reference;
        this.designation = designation;
        this.prixUnitaireHT = prixUnitaireHT;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getReference() {
        return reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public String getDesignation() {
        return designation;
    }
    
    public void setDesignation(String designation) {
        this.designation = designation;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrixUnitaireHT() {
        return prixUnitaireHT;
    }
    
    public void setPrixUnitaireHT(BigDecimal prixUnitaireHT) {
        this.prixUnitaireHT = prixUnitaireHT;
    }
    
    public BigDecimal getTauxTVA() {
        return tauxTVA;
    }
    
    public void setTauxTVA(BigDecimal tauxTVA) {
        this.tauxTVA = tauxTVA;
    }
    
    public Boolean getActif() {
        return actif;
    }
    
    public void setActif(Boolean actif) {
        this.actif = actif;
    }
    
    public String getUnite() {
        return unite;
    }
    
    public void setUnite(String unite) {
        this.unite = unite;
    }
    
    // Méthodes utilitaires
    public BigDecimal getPrixUnitaireTTC() {
        BigDecimal montantTVA = prixUnitaireHT.multiply(tauxTVA).divide(new BigDecimal("100"));
        return prixUnitaireHT.add(montantTVA);
    }
    
    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", designation='" + designation + '\'' +
                ", prixUnitaireHT=" + prixUnitaireHT +
                '}';
    }
}
