package com.facturation.beans;

import com.facturation.entities.Produit;
import com.facturation.services.ProduitService;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Named("produitBean")
@SessionScoped
public class ProduitBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @EJB
    private ProduitService produitService;
    
    private Produit produit;
    private List<Produit> produits;
    private List<Produit> tousLesProduits;
    private String recherche;
    private boolean modeEdition = false;
    private Produit produitASupprimer;
    private boolean afficherInactifs = false;
    
    @PostConstruct
    public void init() {
        try {
            System.out.println("=== Initialisation du ProduitBean ===");
            
            if (produitService == null) {
                System.err.println("❌ ProduitService non injecté");
                produits = List.of();
                return;
            }
            
            System.out.println("✅ ProduitService injecté");
            chargerProduits();
            produit = new Produit();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur initialisation ProduitBean");
            e.printStackTrace();
            produits = List.of();
            produit = new Produit();
        }
    }
    
    public void chargerProduits() {
        try {
            System.out.println("Chargement des produits...");
            tousLesProduits = produitService.trouverTous();
            filtrer();
            System.out.println("✅ " + produits.size() + " produit(s) chargé(s)");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement produits");
            e.printStackTrace();
            produits = List.of();
        }
    }
    
    public void filtrer() {
        try {
            System.out.println("🔧 Filtrage - Afficher inactifs: " + afficherInactifs);
            
            if (afficherInactifs) {
                produits = tousLesProduits;
            } else {
                produits = tousLesProduits.stream().filter(Produit::getActif).collect(Collectors.toList());
            }
            
            System.out.println("✅ " + produits.size() + " produit(s) après filtrage");
        } catch (Exception e) {
            System.err.println("❌ Erreur filtrage");
            e.printStackTrace();
            produits = tousLesProduits;
        }
    }
    
    public void nouveauProduit() {
        produit = new Produit();
        produit.setActif(true); // Par défaut actif
        modeEdition = true;
        System.out.println("➕ Mode création activé");
    }
    
    public void editer(Produit produitAEditer) {
        try {
            System.out.println("✏️ Édition produit ID: " + produitAEditer.getId());
            
            this.produit = new Produit();
            this.produit.setId(produitAEditer.getId());
            this.produit.setReference(produitAEditer.getReference());
            this.produit.setDesignation(produitAEditer.getDesignation());
            this.produit.setDescription(produitAEditer.getDescription());
            this.produit.setPrixUnitaireHT(produitAEditer.getPrixUnitaireHT());
            this.produit.setTauxTVA(produitAEditer.getTauxTVA());
            this.produit.setActif(produitAEditer.getActif());
            this.produit.setUnite(produitAEditer.getUnite());
            
            modeEdition = true;
        } catch (Exception e) {
            System.err.println("❌ Erreur édition");
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur lors du chargement du produit");
        }
    }
    
    public void sauvegarder() {
        try {
            System.out.println("💾 Sauvegarde produit: " + produit.getDesignation());
            
            if (produit.getId() == null) {
                System.out.println("➕ Création");
                produitService.creerProduit(produit);
                addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Produit créé avec succès");
            } else {
                System.out.println("✏️ Modification ID: " + produit.getId());
                produitService.modifierProduit(produit);
                addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Produit modifié avec succès");
            }
            
            chargerProduits();
            produit = new Produit();
            modeEdition = false;
            
            System.out.println("✅ Sauvegarde réussie");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur sauvegarde");
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur: " + e.getMessage());
        }
    }
    
    public void toggleActif(Produit produit) {
        try {
            produit.setActif(!produit.getActif());
            produitService.modifierProduit(produit);
            
            String statut = produit.getActif() ? "activé" : "désactivé";
            addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Produit " + statut);
            
            chargerProduits();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur toggle actif");
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur lors du changement de statut");
        }
    }
    
    public void confirmerSuppression(Produit produit) {
        this.produitASupprimer = produit;
        System.out.println("🗑️ Confirmation suppression ID: " + produit.getId());
    }
    
    public void supprimer() {
        try {
            if (produitASupprimer != null) {
                System.out.println("🗑️ Suppression produit ID: " + produitASupprimer.getId());
                produitService.supprimerProduit(produitASupprimer.getId());
                addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Produit supprimé");
                chargerProduits();
                produitASupprimer = null;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur suppression");
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Impossible de supprimer ce produit");
            produitASupprimer = null;
        }
    }
    
    public void annulerSuppression() {
        produitASupprimer = null;
    }
    
    public void rechercher() {
        try {
            System.out.println("🔍 Recherche: " + recherche);
            
            if (recherche != null && !recherche.trim().isEmpty()) {
                produits = produitService.rechercherParDesignation(recherche.trim());
                System.out.println("✅ " + produits.size() + " résultat(s)");
            } else {
                chargerProduits();
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur recherche");
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur lors de la recherche");
        }
    }
    
    public void annuler() {
        produit = new Produit();
        modeEdition = false;
    }
    
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
    
    // Getters et Setters
    public Produit getProduit() {
        return produit;
    }
    
    public void setProduit(Produit produit) {
        this.produit = produit;
    }
    
    public List<Produit> getProduits() {
        return produits;
    }
    
    public void setProduits(List<Produit> produits) {
        this.produits = produits;
    }
    
    public String getRecherche() {
        return recherche;
    }
    
    public void setRecherche(String recherche) {
        this.recherche = recherche;
    }
    
    public boolean isModeEdition() {
        return modeEdition;
    }
    
    public void setModeEdition(boolean modeEdition) {
        this.modeEdition = modeEdition;
    }
    
    public Produit getProduitASupprimer() {
        return produitASupprimer;
    }
    
    public void setProduitASupprimer(Produit produitASupprimer) {
        this.produitASupprimer = produitASupprimer;
    }
    
    public boolean isAfficherInactifs() {
        return afficherInactifs;
    }
    
    public void setAfficherInactifs(boolean afficherInactifs) {
        this.afficherInactifs = afficherInactifs;
    }
}
