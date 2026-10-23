package com.facturation.services;

import com.facturation.entities.Client;
import com.facturation.entities.Facture;
import com.facturation.entities.Facture.StatutFacture;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class RapportService {
    
    @PersistenceContext(unitName = "FacturationPU")
    private EntityManager em;
    
    /**
     * Calcule le chiffre d'affaires sur une période
     */
    public BigDecimal calculerCA(LocalDate dateDebut, LocalDate dateFin) {
        String jpql = "SELECT COALESCE(SUM(f.montantTTC), 0) FROM Facture f " +
                     "WHERE f.dateFacture BETWEEN :dateDebut AND :dateFin " +
                     "AND f.statut IN (:statuts)";
        
        TypedQuery<BigDecimal> query = em.createQuery(jpql, BigDecimal.class);
        query.setParameter("dateDebut", dateDebut);
        query.setParameter("dateFin", dateFin);
        query.setParameter("statuts", List.of(StatutFacture.VALIDEE, StatutFacture.ENVOYEE, StatutFacture.PAYEE));
        
        return query.getSingleResult();
    }
    
    /**
     * Calcule le CA du mois en cours
     */
    public BigDecimal calculerCAMoisCourant() {
        LocalDate now = LocalDate.now();
        LocalDate debutMois = now.withDayOfMonth(1);
        LocalDate finMois = now.withDayOfMonth(now.lengthOfMonth());
        
        return calculerCA(debutMois, finMois);
    }
    
    /**
     * Calcule le CA de l'année en cours
     */
    public BigDecimal calculerCAAnnee() {
        LocalDate now = LocalDate.now();
        LocalDate debutAnnee = LocalDate.of(now.getYear(), 1, 1);
        LocalDate finAnnee = LocalDate.of(now.getYear(), 12, 31);
        
        return calculerCA(debutAnnee, finAnnee);
    }
    
    /**
     * Récupère le CA par mois pour une année donnée
     */
    public Map<Month, BigDecimal> getCAParMois(int annee) {
        Map<Month, BigDecimal> resultat = new HashMap<>();
        
        for (Month mois : Month.values()) {
            LocalDate debutMois = LocalDate.of(annee, mois, 1);
            LocalDate finMois = debutMois.withDayOfMonth(debutMois.lengthOfMonth());
            
            BigDecimal ca = calculerCA(debutMois, finMois);
            resultat.put(mois, ca);
        }
        
        return resultat;
    }
    
    /**
     * Récupère les top N clients (par CA généré)
     */
    public List<Object[]> getTopClients(int limit) {
        String jpql = "SELECT c, COALESCE(SUM(f.montantTTC), 0) as ca " +
                     "FROM Client c " +
                     "LEFT JOIN c.factures f " +
                     "WHERE f.statut IN (:statuts) OR f IS NULL " +
                     "GROUP BY c " +
                     "ORDER BY ca DESC";
        
        TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
        query.setParameter("statuts", List.of(StatutFacture.VALIDEE, StatutFacture.ENVOYEE, StatutFacture.PAYEE));
        query.setMaxResults(limit);
        
        return query.getResultList();
    }
    
    /**
     * Récupère les statistiques des factures par statut
     */
    public Map<StatutFacture, Long> getStatistiquesParStatut() {
        Map<StatutFacture, Long> stats = new HashMap<>();
        
        String jpql = "SELECT f.statut, COUNT(f) FROM Facture f GROUP BY f.statut";
        TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
        
        List<Object[]> results = query.getResultList();
        for (Object[] result : results) {
            stats.put((StatutFacture) result[0], (Long) result[1]);
        }
        
        return stats;
    }
    
    /**
     * Récupère le montant des factures impayées
     */
    public BigDecimal getMontantImpaye() {
        String jpql = "SELECT COALESCE(SUM(f.montantTTC), 0) FROM Facture f " +
                     "WHERE f.statut IN (:statuts)";
        
        TypedQuery<BigDecimal> query = em.createQuery(jpql, BigDecimal.class);
        query.setParameter("statuts", List.of(StatutFacture.VALIDEE, StatutFacture.ENVOYEE));
        
        return query.getSingleResult();
    }
    
    /**
     * Récupère les factures en retard de paiement
     */
    public List<Facture> getFacturesEnRetard() {
        String jpql = "SELECT f FROM Facture f " +
                     "WHERE f.dateEcheance < :aujourdhui " +
                     "AND f.statut IN (:statuts) " +
                     "ORDER BY f.dateEcheance ASC";
        
        TypedQuery<Facture> query = em.createQuery(jpql, Facture.class);
        query.setParameter("aujourdhui", LocalDate.now());
        query.setParameter("statuts", List.of(StatutFacture.VALIDEE, StatutFacture.ENVOYEE));
        
        return query.getResultList();
    }
    
    /**
     * Récupère le nombre de factures en retard
     */
    public long getNombreFacturesEnRetard() {
        String jpql = "SELECT COUNT(f) FROM Facture f " +
                     "WHERE f.dateEcheance < :aujourdhui " +
                     "AND f.statut IN (:statuts)";
        
        TypedQuery<Long> query = em.createQuery(jpql, Long.class);
        query.setParameter("aujourdhui", LocalDate.now());
        query.setParameter("statuts", List.of(StatutFacture.VALIDEE, StatutFacture.ENVOYEE));
        
        return query.getSingleResult();
    }
    
    /**
     * Récupère les dernières factures créées
     */
    public List<Facture> getDernieresFactures(int limit) {
        String jpql = "SELECT f FROM Facture f ORDER BY f.dateFacture DESC";
        TypedQuery<Facture> query = em.createQuery(jpql, Facture.class);
        query.setMaxResults(limit);
        return query.getResultList();
    }
    
    /**
     * Génère un tableau de bord avec les principales statistiques
     */
    public Map<String, Object> getTableauDeBord() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // CA
        dashboard.put("caMois", calculerCAMoisCourant());
        dashboard.put("caAnnee", calculerCAAnnee());
        
        // Factures
        dashboard.put("nbFacturesTotal", compterFactures());
        dashboard.put("nbFacturesEnRetard", getNombreFacturesEnRetard());
        dashboard.put("montantImpaye", getMontantImpaye());
        
        // Clients
        dashboard.put("nbClients", compterClients());
        
        // Dernières factures
        dashboard.put("dernieresFactures", getDernieresFactures(5));
        
        // Top clients
        dashboard.put("topClients", getTopClients(5));
        
        // Stats par statut
        dashboard.put("statsParStatut", getStatistiquesParStatut());
        
        return dashboard;
    }
    
    /**
     * Compte le nombre de factures
     */
    private long compterFactures() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(f) FROM Facture f", Long.class);
        return query.getSingleResult();
    }
    
    /**
     * Compte le nombre de clients
     */
    private long compterClients() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(c) FROM Client c", Long.class);
        return query.getSingleResult();
    }
}
