package com.facturation.beans;

import com.facturation.entities.Client;
import com.facturation.services.ClientService;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("clientBean")
@SessionScoped
public class ClientBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @EJB
    private ClientService clientService;
    
    private Client client;
    private List<Client> clients;
    private String recherche;
    private boolean modeEdition = false;
    private Client clientASupprimer;
    
    @PostConstruct
    public void init() {
        try {
            System.out.println("=== Initialisation du ClientBean ===");
            
            if (clientService == null) {
                System.err.println("❌ ClientService n'est pas injecté");
                clients = List.of();
                return;
            }
            
            System.out.println("✅ ClientService injecté");
            chargerClients();
            client = new Client();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'initialisation du ClientBean");
            e.printStackTrace();
            clients = List.of();
            client = new Client();
        }
    }
    
    public void chargerClients() {
        try {
            System.out.println("Chargement des clients...");
            clients = clientService.trouverTous();
            System.out.println("✅ " + clients.size() + " client(s) chargé(s)");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des clients");
            e.printStackTrace();
            clients = List.of();
        }
    }
    
    public void nouveauClient() {
        client = new Client();
        modeEdition = true;
        System.out.println("➕ Mode création activé");
    }
    
    public void editer(Client clientAEditer) {
        try {
            System.out.println("✏️ Édition du client ID: " + clientAEditer.getId());
            
            // Créer une copie
            this.client = new Client();
            this.client.setId(clientAEditer.getId());
            this.client.setNom(clientAEditer.getNom());
            this.client.setPrenom(clientAEditer.getPrenom());
            this.client.setEmail(clientAEditer.getEmail());
            this.client.setTelephone(clientAEditer.getTelephone());
            this.client.setAdresse(clientAEditer.getAdresse());
            this.client.setCodePostal(clientAEditer.getCodePostal());
            this.client.setVille(clientAEditer.getVille());
            this.client.setPays(clientAEditer.getPays());
            this.client.setNumeroTVA(clientAEditer.getNumeroTVA());
            
            modeEdition = true;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'édition");
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur lors du chargement du client");
        }
    }
    
    public void sauvegarder() {
        try {
            System.out.println("💾 Sauvegarde du client: " + client.getNom() + " " + client.getPrenom());
            
            // Vérifier email unique
            if (clientService.emailExiste(client.getEmail(), client.getId())) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Cet email est déjà utilisé");
                return;
            }
            
            if (client.getId() == null) {
                System.out.println("➕ Création");
                clientService.creerClient(client);
                addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Client créé avec succès");
            } else {
                System.out.println("✏️ Modification ID: " + client.getId());
                clientService.modifierClient(client);
                addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Client modifié avec succès");
            }
            
            clients = null;
            chargerClients();
            client = new Client();
            modeEdition = false;
            
            System.out.println("✅ Sauvegarde réussie");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la sauvegarde");
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }
    
    public void confirmerSuppression(Client client) {
        this.clientASupprimer = client;
        System.out.println("🗑️ Confirmation suppression ID: " + client.getId());
    }
    
    public void supprimer() {
        try {
            if (clientASupprimer != null) {
                System.out.println("🗑️ Suppression du client ID: " + clientASupprimer.getId());
                clientService.supprimerClient(clientASupprimer.getId());
                addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Client supprimé avec succès");
                chargerClients();
                clientASupprimer = null;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression");
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Impossible de supprimer ce client");
            clientASupprimer = null;
        }
    }
    
    public void annulerSuppression() {
        clientASupprimer = null;
        System.out.println("↩️ Suppression annulée");
    }
    
    public void rechercher() {
        try {
            System.out.println("🔍 Recherche: " + recherche);
            
            if (recherche != null && !recherche.trim().isEmpty()) {
                clients = clientService.rechercherParNom(recherche.trim());
                System.out.println("✅ " + clients.size() + " résultat(s)");
            } else {
                chargerClients();
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la recherche");
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur lors de la recherche");
        }
    }
    
    public void annuler() {
        client = new Client();
        modeEdition = false;
        System.out.println("↩️ Édition annulée");
    }
    
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
    
    // Getters et Setters
    public Client getClient() {
        return client;
    }
    
    public void setClient(Client client) {
        this.client = client;
    }
    
    public List<Client> getClients() {
        return clients;
    }
    
    public void setClients(List<Client> clients) {
        this.clients = clients;
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
    
    public Client getClientASupprimer() {
        return clientASupprimer;
    }
    
    public void setClientASupprimer(Client clientASupprimer) {
        this.clientASupprimer = clientASupprimer;
    }
}
