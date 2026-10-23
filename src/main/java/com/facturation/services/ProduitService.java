package com.facturation.services;

import com.facturation.entities.Produit;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Stateless
public class ProduitService {
    
    @PersistenceContext(unitName = "FacturationPU")
    private EntityManager em;
    
    /**
     * Crée un nouveau produit
     */
    public Produit creerProduit(Produit produit) {
        em.persist(produit);
        em.flush();
        em.clear();
        return produit;
    }
    
    /**
     * Met à jour un produit existant
     */
    public Produit modifierProduit(Produit produit) {
        Produit merged = em.merge(produit);
        em.flush();
        em.clear();
        return merged;
    }
    
    /**
     * Supprime un produit
     */
    public void supprimerProduit(Long produitId) {
        Produit produit = em.find(Produit.class, produitId);
        if (produit != null) {
            em.remove(produit);
            em.flush();
            em.clear();
        }
    }
    
    /**
     * Désactive un produit (soft delete)
     */
    public void desactiverProduit(Long produitId) {
        Produit produit = em.find(Produit.class, produitId);
        if (produit != null) {
            produit.setActif(false);
            em.merge(produit);
        }
    }
    
    /**
     * Récupère un produit par son ID
     */
    public Produit trouverParId(Long id) {
        return em.find(Produit.class, id);
    }
    
    /**
     * Récupère tous les produits
     */
    public List<Produit> trouverTous() {
        em.clear();
        TypedQuery<Produit> query = em.createNamedQuery("Produit.findAll", Produit.class);
        return query.getResultList();
    }
    
    /**
     * Récupère tous les produits actifs
    
    public List<Produit> trouverActifs() {
        em.clear();
        TypedQuery<Produit> query = em.createNamedQuery("Produit.findActifs", Produit.class);
        return query.getResultList();
    } */
    
    /**
     * Recherche des produits par désignation
     */
    public List<Produit> rechercherParDesignation(String designation) {
        em.clear();
        TypedQuery<Produit> query = em.createNamedQuery("Produit.findByDesignation", Produit.class);
        query.setParameter("designation", "%" + designation + "%");
        return query.getResultList();
    }
    
    /**
     * Compte le nombre total de produits
     */
    public long compterProduits() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(p) FROM Produit p", Long.class);
        return query.getSingleResult();
    }
    
    /**
     * Compte le nombre de produits actifs
     */
    public long compterProduitsActifs() {
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(p) FROM Produit p WHERE p.actif = true", Long.class);
        return query.getSingleResult();
    }


    /**
     * Récupère tous les produits actifs
     */
    public List<Produit> trouverActifs() {
        em.clear();
        TypedQuery<Produit> query = em.createQuery(
            "SELECT p FROM Produit p WHERE p.actif = true ORDER BY p.designation",
            Produit.class
        );
        return query.getResultList();
    }
}
