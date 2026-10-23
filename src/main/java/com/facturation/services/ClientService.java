package com.facturation.services;

import com.facturation.entities.Client;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Stateless
public class ClientService {
    
    @PersistenceContext(unitName = "FacturationPU")
    private EntityManager em;
    
    /**
     * Crée un nouveau client
     */
    public Client creerClient(Client client) {
        em.persist(client);
        em.flush();  // Force l'écriture en base MAINTENANT
        em.clear();  // Vide le cache JPA
        System.out.println("✅ Client créé en base - ID: " + client.getId());
        return client;
    }
    
    /**
     * Met à jour un client existant
     */
    public Client modifierClient(Client client) {
        Client merged = em.merge(client);
        em.flush();  // Force l'écriture
        em.clear();  // Vide le cache
        System.out.println("✅ Client modifié en base - ID: " + merged.getId());
        return merged;
    }
    
    /**
     * Supprime un client
     */
    public void supprimerClient(Long clientId) {
        Client client = em.find(Client.class, clientId);
        if (client != null) {
            em.remove(client);
            em.flush();  // Force la suppression
            em.clear();  // Vide le cache
            System.out.println("✅ Client supprimé de la base - ID: " + clientId);
        }
    }
    
    /**
     * Récupère un client par son ID
     */
    public Client trouverParId(Long id) {
        return em.find(Client.class, id);
    }
    
    /**
     * Récupère tous les clients
     */
    public List<Client> trouverTous() {
        em.clear();  // Vide le cache AVANT de lire
        TypedQuery<Client> query = em.createNamedQuery("Client.findAll", Client.class);
        List<Client> clients = query.getResultList();
        System.out.println("📋 " + clients.size() + " client(s) chargé(s) depuis la base");
        return clients;
    }
    
    /**
     * Recherche des clients par nom
     */
    public List<Client> rechercherParNom(String nom) {
        TypedQuery<Client> query = em.createNamedQuery("Client.findByNom", Client.class);
        query.setParameter("nom", "%" + nom + "%");
        return query.getResultList();
    }
    
    /**
     * Trouve un client par email
     */
    public Client trouverParEmail(String email) {
        TypedQuery<Client> query = em.createNamedQuery("Client.findByEmail", Client.class);
        query.setParameter("email", email);
        List<Client> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
    
    /**
     * Vérifie si un email existe déjà
     */
    public boolean emailExiste(String email, Long clientId) {
        String jpql = "SELECT COUNT(c) FROM Client c WHERE c.email = :email";
        if (clientId != null) {
            jpql += " AND c.id != :clientId";
        }
        
        TypedQuery<Long> query = em.createQuery(jpql, Long.class);
        query.setParameter("email", email);
        if (clientId != null) {
            query.setParameter("clientId", clientId);
        }
        
        return query.getSingleResult() > 0;
    }
    
    /**
     * Compte le nombre total de clients
     */
    public long compterClients() {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(c) FROM Client c", Long.class);
        return query.getSingleResult();
    }
    
    /**
     * Récupère les meilleurs clients (par nombre de factures)
     */
    public List<Object[]> getMeilleursClients(int limit) {
        String jpql = "SELECT c, COUNT(f) as nbFactures " +
                     "FROM Client c JOIN c.factures f " +
                     "GROUP BY c " +
                     "ORDER BY nbFactures DESC";
        TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
        query.setMaxResults(limit);
        return query.getResultList();
    }
}
