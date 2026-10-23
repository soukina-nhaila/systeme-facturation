package com.facturation.beans;

import com.facturation.entities.Client;
import com.facturation.entities.Facture;
import com.facturation.entities.LigneFacture;
import com.facturation.entities.Produit;
import com.facturation.services.ClientService;
import com.facturation.services.FactureService;
import com.facturation.services.ProduitService;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Imports pour PDF
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

@Named("factureBean")
@SessionScoped
public class FactureBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @EJB
    private FactureService factureService;
    
    @EJB
    private ClientService clientService;
    
    @EJB
    private ProduitService produitService;
    
    private Facture facture;
    private List<Facture> factures;
    private List<Client> clientsDisponibles;
    private List<Produit> produitsDisponibles;
    private String recherche;
    private boolean modeEdition = false;
    private boolean modeVoir = false;
    private Facture factureASupprimer;
    
    // Pour les dates (conversion Date <-> LocalDate)
    private Date dateFactureTemp;
    private Date dateEcheanceTemp;
    
    // Nouvelle ligne en cours d'ajout
    private LigneFacture nouvelleLigne;
    
    @PostConstruct
    public void init() {
        try {
            System.out.println("=== Initialisation du FactureBean ===");
            
            if (factureService == null || clientService == null || produitService == null) {
                System.err.println("❌ Services non injectés");
                factures = new ArrayList<>();
                return;
            }
            
            System.out.println("✅ Tous les services injectés");
            chargerFactures();
            chargerClientsEtProduits();
            facture = new Facture();
            nouvelleLigne = new LigneFacture();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur initialisation FactureBean");
            e.printStackTrace();
            factures = new ArrayList<>();
            facture = new Facture();
        }
    }
    
    public void chargerFactures() {
        try {
            System.out.println("Chargement des factures...");
            factures = factureService.trouverToutes();
            System.out.println("✅ " + factures.size() + " facture(s) chargée(s)");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement factures");
            e.printStackTrace();
            factures = new ArrayList<>();
        }
    }
    
    public void chargerClientsEtProduits() {
        try {
            clientsDisponibles = clientService.trouverTous();
            produitsDisponibles = produitService.trouverActifs();
            System.out.println("✅ Clients et produits chargés");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement clients/produits");
            e.printStackTrace();
            clientsDisponibles = new ArrayList<>();
            produitsDisponibles = new ArrayList<>();
        }
    }
    
    public void nouvelleFacture() {
        facture = new Facture();
        facture.setLignes(new ArrayList<>());
        facture.setStatut(Facture.StatutFacture.BROUILLON);
        facture.setDateFacture(LocalDate.now());
        facture.setDateEcheance(LocalDate.now().plusDays(30));
        
        // Convertir pour affichage
        dateFactureTemp = Date.from(facture.getDateFacture().atStartOfDay(ZoneId.systemDefault()).toInstant());
        dateEcheanceTemp = Date.from(facture.getDateEcheance().atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        nouvelleLigne = new LigneFacture();
        modeEdition = true;
        modeVoir = false;
        
        System.out.println("➕ Mode création facture activé");
    }
    
    public void editer(Facture factureAEditer) {
        try {
            System.out.println("✏️ Édition facture ID: " + factureAEditer.getId());
            
            // Recharger la facture complète avec lignes
            this.facture = factureService.trouverParId(factureAEditer.getId());
            
            if (this.facture.getLignes() == null) {
                this.facture.setLignes(new ArrayList<>());
            }
            
            // Convertir dates
            if (facture.getDateFacture() != null) {
                dateFactureTemp = Date.from(facture.getDateFacture().atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            if (facture.getDateEcheance() != null) {
                dateEcheanceTemp = Date.from(facture.getDateEcheance().atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            
            nouvelleLigne = new LigneFacture();
            modeEdition = true;
            modeVoir = false;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur édition facture");
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur lors du chargement de la facture");
        }
    }
    
    public void voir(Facture factureAVoir) {
        try {
            System.out.println("👁️ Visualisation facture ID: " + factureAVoir.getId());
            
            this.facture = factureService.trouverParId(factureAVoir.getId());
            
            modeVoir = true;
            modeEdition = false;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur visualisation");
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur lors du chargement");
        }
    }
    
    public void ajouterLigne() {
        try {
            System.out.println("➕ Ajout d'une ligne");
            
            if (nouvelleLigne.getProduit() == null) {
                addMessage(FacesMessage.SEVERITY_WARN, "Attention", "Veuillez sélectionner un produit");
                return;
            }
            
            if (nouvelleLigne.getQuantite() == null || nouvelleLigne.getQuantite().compareTo(BigDecimal.ZERO) <= 0) {
                addMessage(FacesMessage.SEVERITY_WARN, "Attention", "Quantité invalide");
                return;
            }
            
            // Si prix pas renseigné, prendre celui du produit
            if (nouvelleLigne.getPrixUnitaireHT() == null) {
                nouvelleLigne.setPrixUnitaireHT(nouvelleLigne.getProduit().getPrixUnitaireHT());
            }
            
            if (nouvelleLigne.getTauxTVA() == null) {
                nouvelleLigne.setTauxTVA(nouvelleLigne.getProduit().getTauxTVA());
            }
            
            if (nouvelleLigne.getRemise() == null) {
                nouvelleLigne.setRemise(BigDecimal.ZERO);
            }
            
            // IMPORTANT: Associer la ligne à la facture
            nouvelleLigne.setFacture(facture);
            
            // Calculer montants de la ligne
            calculerMontantsLigne(nouvelleLigne);
            
            // Ajouter à la facture
            if (facture.getLignes() == null) {
                facture.setLignes(new ArrayList<>());
            }
            
            facture.getLignes().add(nouvelleLigne);
            
            // Calculer totaux
            calculerTotaux();
            
            // Réinitialiser
            nouvelleLigne = new LigneFacture();
            
            addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Ligne ajoutée");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur ajout ligne");
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur lors de l'ajout de la ligne");
        }
    }
    
    public void supprimerLigne(LigneFacture ligne) {
        try {
            facture.getLignes().remove(ligne);
            calculerTotaux();
            addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Ligne supprimée");
        } catch (Exception e) {
            System.err.println("❌ Erreur suppression ligne");
            e.printStackTrace();
        }
    }
    
    public void calculerTotaux() {
        try {
            BigDecimal totalHT = BigDecimal.ZERO;
            BigDecimal totalTVA = BigDecimal.ZERO;
            
            for (LigneFacture ligne : facture.getLignes()) {
                calculerMontantsLigne(ligne);
                totalHT = totalHT.add(ligne.getMontantHT());
                totalTVA = totalTVA.add(ligne.getMontantTVA());
            }
            
            facture.setMontantHT(totalHT);
            facture.setMontantTVA(totalTVA);
            facture.setMontantTTC(totalHT.add(totalTVA));
            
            System.out.println("✅ Totaux calculés - TTC: " + facture.getMontantTTC());
            
        } catch (Exception e) {
            System.err.println("❌ Erreur calcul totaux");
            e.printStackTrace();
        }
    }
    
    private void calculerMontantsLigne(LigneFacture ligne) {
        BigDecimal quantite = ligne.getQuantite();
        BigDecimal prixHT = ligne.getPrixUnitaireHT();
        BigDecimal remise = ligne.getRemise() != null ? ligne.getRemise() : BigDecimal.ZERO;
        BigDecimal tauxTVA = ligne.getTauxTVA();
        
        // Montant brut
        BigDecimal montantBrut = prixHT.multiply(quantite);
        
        // Appliquer remise
        BigDecimal montantRemise = montantBrut.multiply(remise).divide(new BigDecimal("100"));
        BigDecimal montantHT = montantBrut.subtract(montantRemise);
        
        // Calculer TVA
        BigDecimal montantTVA = montantHT.multiply(tauxTVA).divide(new BigDecimal("100"));
        
        // Total TTC
        BigDecimal montantTTC = montantHT.add(montantTVA);
        
        ligne.setMontantHT(montantHT);
        ligne.setMontantTVA(montantTVA);
        ligne.setMontantTTC(montantTTC);
    }
    
    public void sauvegarder() {
        try {
            System.out.println("💾 Sauvegarde facture");
            
            // Convertir dates
            if (dateFactureTemp != null) {
                facture.setDateFacture(dateFactureTemp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            if (dateEcheanceTemp != null) {
                facture.setDateEcheance(dateEcheanceTemp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            
            // Vérifications
            if (facture.getClient() == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Veuillez sélectionner un client");
                return;
            }
            
            if (facture.getLignes() == null || facture.getLignes().isEmpty()) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Ajoutez au moins une ligne");
                return;
            }
            
            // IMPORTANT: Associer toutes les lignes à la facture
            for (LigneFacture ligne : facture.getLignes()) {
                ligne.setFacture(facture);
            }
            
            // Calculer totaux une dernière fois
            calculerTotaux();
            
            if (facture.getId() == null) {
                // Création
                System.out.println("➕ Création facture");
                factureService.creerFacture(facture);
                addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Facture créée avec succès");
            } else {
                // Modification
                System.out.println("✏️ Modification facture ID: " + facture.getId());
                factureService.modifierFacture(facture);
                addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Facture modifiée avec succès");
            }
            
            chargerFactures();
            facture = new Facture();
            modeEdition = false;
            
            System.out.println("✅ Sauvegarde réussie");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur sauvegarde facture");
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }
    
    public void marquerValidee(Facture facture) {
        try {
            System.out.println("✅ Validation facture ID: " + facture.getId());
            factureService.validerFacture(facture.getId());
            addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Facture validée");
            chargerFactures();
        } catch (Exception e) {
            System.err.println("❌ Erreur validation");
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur lors de la validation");
        }
    }
    
    public void marquerPayee(Facture facture) {
        try {
            System.out.println("💰 Facture payée ID: " + facture.getId());
            factureService.marquerPayee(facture.getId());
            addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Facture marquée comme payée");
            chargerFactures();
        } catch (Exception e) {
            System.err.println("❌ Erreur marquage payée");
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur");
        }
    }
    
    public void genererPDF(Facture facture) {
        try {
            System.out.println("📄 Génération PDF pour facture ID: " + facture.getId());
            
            // Recharger la facture complète
            Facture factureComplete = factureService.trouverParId(facture.getId());
            
            // Créer le PDF
            Document document = new Document(PageSize.A4);
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();
            HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
            
            String fileName = "Facture_" + factureComplete.getNumeroFacture() + ".pdf";
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();
            
            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
            
            // Titre
            Paragraph title = new Paragraph("FACTURE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Numéro et dates
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(20);
            
            infoTable.addCell(createCell("N° Facture: " + factureComplete.getNumeroFacture(), boldFont));
            infoTable.addCell(createCell("Date: " + factureComplete.getDateFacture(), normalFont));
            infoTable.addCell(createCell("Statut: " + factureComplete.getStatut(), boldFont));
            infoTable.addCell(createCell("Échéance: " + factureComplete.getDateEcheance(), normalFont));
            
            document.add(infoTable);
            
            // Client
            Paragraph clientTitle = new Paragraph("CLIENT", headerFont);
            clientTitle.setSpacingBefore(10);
            clientTitle.setSpacingAfter(5);
            document.add(clientTitle);
            
            Client client = factureComplete.getClient();
            document.add(new Paragraph(client.getNom() + " " + client.getPrenom(), normalFont));
            if (client.getAdresse() != null) {
                document.add(new Paragraph(client.getAdresse(), normalFont));
            }
            if (client.getCodePostal() != null && client.getVille() != null) {
                document.add(new Paragraph(client.getCodePostal() + " " + client.getVille(), normalFont));
            }
            if (client.getEmail() != null) {
                document.add(new Paragraph("Email: " + client.getEmail(), normalFont));
            }
            
            // Lignes
            Paragraph lignesTitle = new Paragraph("DÉTAIL", headerFont);
            lignesTitle.setSpacingBefore(20);
            lignesTitle.setSpacingAfter(10);
            document.add(lignesTitle);
            
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1, 2, 1, 2, 2});
            
            // Headers
            table.addCell(createHeaderCell("Désignation", headerFont));
            table.addCell(createHeaderCell("Qté", headerFont));
            table.addCell(createHeaderCell("Prix HT", headerFont));
            table.addCell(createHeaderCell("TVA", headerFont));
            table.addCell(createHeaderCell("Montant HT", headerFont));
            table.addCell(createHeaderCell("Montant TTC", headerFont));
            
            // Lignes
            for (LigneFacture ligne : factureComplete.getLignes()) {
                table.addCell(createCell(ligne.getProduit().getDesignation(), normalFont));
                table.addCell(createCell(String.valueOf(ligne.getQuantite()), normalFont));
                table.addCell(createCell(ligne.getPrixUnitaireHT() + " €", normalFont));
                table.addCell(createCell(ligne.getTauxTVA() + "%", normalFont));
                table.addCell(createCell(ligne.getMontantHT() + " €", normalFont));
                table.addCell(createCell(ligne.getMontantTTC() + " €", normalFont));
            }
            
            document.add(table);
            
            // Totaux
            PdfPTable totauxTable = new PdfPTable(2);
            totauxTable.setWidthPercentage(40);
            totauxTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totauxTable.setSpacingBefore(20);
            
            totauxTable.addCell(createCell("Total HT:", boldFont));
            totauxTable.addCell(createCell(factureComplete.getMontantHT() + " €", normalFont));
            
            totauxTable.addCell(createCell("Total TVA:", boldFont));
            totauxTable.addCell(createCell(factureComplete.getMontantTVA() + " €", normalFont));
            
            totauxTable.addCell(createCell("Total TTC:", headerFont));
            totauxTable.addCell(createCell(factureComplete.getMontantTTC() + " €", headerFont));
            
            document.add(totauxTable);
            
            // Remarques
            if (factureComplete.getRemarques() != null && !factureComplete.getRemarques().isEmpty()) {
                Paragraph remarques = new Paragraph("Remarques: " + factureComplete.getRemarques(), normalFont);
                remarques.setSpacingBefore(20);
                document.add(remarques);
            }
            
            document.close();
            facesContext.responseComplete();
            
            System.out.println("✅ PDF généré avec succès");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur génération PDF");
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur lors de la génération du PDF");
        }
    }
    
    private PdfPCell createHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
    
    private PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        return cell;
    }
    
    public void confirmerSuppression(Facture facture) {
        this.factureASupprimer = facture;
        System.out.println("🗑️ Confirmation suppression ID: " + facture.getId());
    }
    
    public void supprimer() {
        try {
            if (factureASupprimer != null) {
                System.out.println("🗑️ Suppression facture ID: " + factureASupprimer.getId());
                factureService.supprimerFacture(factureASupprimer.getId());
                addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Facture supprimée");
                chargerFactures();
                factureASupprimer = null;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur suppression");
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Impossible de supprimer");
            factureASupprimer = null;
        }
    }
    
    public void annulerSuppression() {
        factureASupprimer = null;
    }
    
    public void rechercher() {
        try {
            System.out.println("🔍 Recherche: " + recherche);
            
            if (recherche != null && !recherche.trim().isEmpty()) {
                factures = factureService.rechercherParNumero(recherche.trim());
                System.out.println("✅ " + factures.size() + " résultat(s)");
            } else {
                chargerFactures();
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur recherche");
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur lors de la recherche");
        }
    }
    
    public void annuler() {
        facture = new Facture();
        modeEdition = false;
        modeVoir = false;
    }
    
    public void retourListe() {
        facture = new Facture();
        modeVoir = false;
        modeEdition = false;
    }
    
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
    
    // Getters et Setters
    public Facture getFacture() { return facture; }
    public void setFacture(Facture facture) { this.facture = facture; }
    
    public List<Facture> getFactures() { return factures; }
    public void setFactures(List<Facture> factures) { this.factures = factures; }
    
    public List<Client> getClientsDisponibles() { return clientsDisponibles; }
    public void setClientsDisponibles(List<Client> clientsDisponibles) { this.clientsDisponibles = clientsDisponibles; }
    
    public List<Produit> getProduitsDisponibles() { return produitsDisponibles; }
    public void setProduitsDisponibles(List<Produit> produitsDisponibles) { this.produitsDisponibles = produitsDisponibles; }
    
    public String getRecherche() { return recherche; }
    public void setRecherche(String recherche) { this.recherche = recherche; }
    
    public boolean isModeEdition() { return modeEdition; }
    public void setModeEdition(boolean modeEdition) { this.modeEdition = modeEdition; }
    
    public boolean isModeVoir() { return modeVoir; }
    public void setModeVoir(boolean modeVoir) { this.modeVoir = modeVoir; }
    
    public Facture getFactureASupprimer() { return factureASupprimer; }
    public void setFactureASupprimer(Facture factureASupprimer) { this.factureASupprimer = factureASupprimer; }
    
    public Date getDateFactureTemp() { return dateFactureTemp; }
    public void setDateFactureTemp(Date dateFactureTemp) { this.dateFactureTemp = dateFactureTemp; }
    
    public Date getDateEcheanceTemp() { return dateEcheanceTemp; }
    public void setDateEcheanceTemp(Date dateEcheanceTemp) { this.dateEcheanceTemp = dateEcheanceTemp; }
    
    public LigneFacture getNouvelleLigne() { return nouvelleLigne; }
    public void setNouvelleLigne(LigneFacture nouvelleLigne) { this.nouvelleLigne = nouvelleLigne; }
}
