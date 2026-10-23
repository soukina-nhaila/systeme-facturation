package com.facturation.services;

import com.facturation.entities.Client;
import com.facturation.entities.Facture;
import com.facturation.entities.Facture.StatutFacture;
import com.facturation.entities.LigneFacture;
import com.facturation.entities.Produit;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Stateless
public class FactureService {
    
    @PersistenceContext(unitName = "FacturationPU")
    private EntityManager em;
    
    /**
     * Crée une nouvelle facture
     */
    public Facture creerFacture(Facture facture) {
        // Générer le numéro de facture si non défini
        if (facture.getNumeroFacture() == null || facture.getNumeroFacture().isEmpty()) {
            facture.setNumeroFacture(genererNumeroFacture());
        }
        
        // Calculer les montants
        facture.calculerMontants();
        
        em.persist(facture);
        return facture;
    }
    
    /**
     * Met à jour une facture existante
     */
    public Facture modifierFacture(Facture facture) {
        // Recalculer les montants
        facture.calculerMontants();
        return em.merge(facture);
    }
    
    /**
     * Supprime une facture
     */
    public void supprimerFacture(Long factureId) {
        Facture facture = em.find(Facture.class, factureId);
        if (facture != null) {
            em.remove(facture);
        }
    }
    
    /**
     * Récupère une facture par son ID
     */
    public Facture trouverParId(Long id) {
        return em.find(Facture.class, id);
    }
    
    /**
     * Récupère toutes les factures
     */
    public List<Facture> trouverToutes() {
        TypedQuery<Facture> query = em.createNamedQuery("Facture.findAll", Facture.class);
        return query.getResultList();
    }
    
    /**
     * Récupère les factures d'un client
     */
    public List<Facture> trouverParClient(Long clientId) {
        TypedQuery<Facture> query = em.createNamedQuery("Facture.findByClient", Facture.class);
        query.setParameter("clientId", clientId);
        return query.getResultList();
    }
    
    /**
     * Récupère les factures par statut
     */
    public List<Facture> trouverParStatut(StatutFacture statut) {
        TypedQuery<Facture> query = em.createNamedQuery("Facture.findByStatut", Facture.class);
        query.setParameter("statut", statut);
        return query.getResultList();
    }
    
    /**
     * Récupère les factures par période
     */
    public List<Facture> trouverParPeriode(LocalDate dateDebut, LocalDate dateFin) {
        TypedQuery<Facture> query = em.createNamedQuery("Facture.findByPeriode", Facture.class);
        query.setParameter("dateDebut", dateDebut);
        query.setParameter("dateFin", dateFin);
        return query.getResultList();
    }
    
    /**
     * Ajoute une ligne à une facture
     */
    public Facture ajouterLigne(Long factureId, Produit produit, BigDecimal quantite, BigDecimal remise) {
        Facture facture = em.find(Facture.class, factureId);
        if (facture != null) {
            LigneFacture ligne = new LigneFacture(produit, quantite);
            if (remise != null) {
                ligne.setRemise(remise);
            }
            ligne.calculerMontants();
            facture.ajouterLigne(ligne);
            facture.calculerMontants();
            return em.merge(facture);
        }
        return null;
    }
    
    /**
     * Supprime une ligne d'une facture
     */
    public Facture supprimerLigne(Long factureId, Long ligneId) {
        Facture facture = em.find(Facture.class, factureId);
        if (facture != null) {
            LigneFacture ligne = em.find(LigneFacture.class, ligneId);
            if (ligne != null) {
                facture.supprimerLigne(ligne);
                em.remove(ligne);
                facture.calculerMontants();
                return em.merge(facture);
            }
        }
        return null;
    }
    
    /**
     * Valide une facture
     */
    public Facture validerFacture(Long factureId) {
        Facture facture = em.find(Facture.class, factureId);
        if (facture != null && facture.getStatut() == StatutFacture.BROUILLON) {
            facture.setStatut(StatutFacture.VALIDEE);
            return em.merge(facture);
        }
        return null;
    }
    
    /**
     * Marque une facture comme envoyée
     */
    public Facture marquerEnvoyee(Long factureId) {
        Facture facture = em.find(Facture.class, factureId);
        if (facture != null && facture.getStatut() == StatutFacture.VALIDEE) {
            facture.setStatut(StatutFacture.ENVOYEE);
            return em.merge(facture);
        }
        return null;
    }
    
    /**
     * Marque une facture comme payée
     */
    public Facture marquerPayee(Long factureId) {
        Facture facture = em.find(Facture.class, factureId);
        if (facture != null) {
            facture.setStatut(StatutFacture.PAYEE);
            return em.merge(facture);
        }
        return null;
    }
    
    /**
     * Annule une facture
     */
    public Facture annulerFacture(Long factureId) {
        Facture facture = em.find(Facture.class, factureId);
        if (facture != null) {
            facture.setStatut(StatutFacture.ANNULEE);
            return em.merge(facture);
        }
        return null;
    }
    
    /**
     * Génère un numéro de facture unique
     */
    private String genererNumeroFacture() {
        // Format: FACT-YYYYMM-XXXX (ex: FACT-202402-0001)
        LocalDate now = LocalDate.now();
        String prefix = "FACT-" + now.format(DateTimeFormatter.ofPattern("yyyyMM")) + "-";
        
        // Chercher le dernier numéro du mois
        String jpql = "SELECT f.numeroFacture FROM Facture f " +
                     "WHERE f.numeroFacture LIKE :pattern " +
                     "ORDER BY f.numeroFacture DESC";
        TypedQuery<String> query = em.createQuery(jpql, String.class);
        query.setParameter("pattern", prefix + "%");
        query.setMaxResults(1);
        
        List<String> results = query.getResultList();
        int numero = 1;
        
        if (!results.isEmpty()) {
            String dernierNumero = results.get(0);
            String[] parts = dernierNumero.split("-");
            if (parts.length == 3) {
                numero = Integer.parseInt(parts[2]) + 1;
            }
        }
        
        return prefix + String.format("%04d", numero);
    }
    
    /**
     * Compte le nombre total de factures
     */
    public long compterFactures() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(f) FROM Facture f", Long.class);
        return query.getSingleResult();
    }
    
    /**
     * Calcule le montant total des factures payées
     */
    public BigDecimal calculerMontantTotalPaye() {
        String jpql = "SELECT COALESCE(SUM(f.montantTTC), 0) FROM Facture f " +
                     "WHERE f.statut = :statut";
        TypedQuery<BigDecimal> query = em.createQuery(jpql, BigDecimal.class);
        query.setParameter("statut", StatutFacture.PAYEE);
        return query.getSingleResult();
    }


    /**
     * Valide une facture (passe de BROUILLON à VALIDEE)
     
    public void validerFacture(Long factureId) {
        Facture facture = em.find(Facture.class, factureId);
        if (facture != null && "BROUILLON".equals(facture.getStatut())) {
            facture.setStatut("VALIDEE");
            em.merge(facture);
            em.flush();
            em.clear();
            System.out.println("✅ Facture validée - ID: " + factureId);
        }
    }*/
    
    /**
     * Marque une facture comme payée
     
    public void marquerPayee(Long factureId) {
        Facture facture = em.find(Facture.class, factureId);
        if (facture != null && !"BROUILLON".equals(facture.getStatut())) {
            facture.setStatut("PAYEE");
            em.merge(facture);
            em.flush();
            em.clear();
            System.out.println("✅ Facture marquée payée - ID: " + factureId);
        } */

   
    /**
     * Recherche des factures par numéro
     */
    public List<Facture> rechercherParNumero(String numero) {
        em.clear();
        TypedQuery<Facture> query = em.createQuery(
            "SELECT f FROM Facture f WHERE f.numeroFacture LIKE :numero ORDER BY f.dateFacture DESC",
            Facture.class
        );
        query.setParameter("numero", "%" + numero + "%");
        return query.getResultList();
    }
}
