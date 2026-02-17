package com.readytoplanbe.myapp.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.readytoplanbe.myapp.domain.*;
import com.readytoplanbe.myapp.domain.enumeration.EntityType;
import com.readytoplanbe.myapp.repository.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import com.readytoplanbe.myapp.service.dto.AIResponseDTO;
import com.readytoplanbe.myapp.service.dto.BusinessPlanFinalDTO;
import com.readytoplanbe.myapp.web.rest.errors.EntityNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang3.StringUtils.truncate;

/**
 * Service Implementation for managing {@link BusinessPlanFinal}.
 */
@Service
public class BusinessPlanFinalService {

    private final Logger log = LoggerFactory.getLogger(BusinessPlanFinalService.class);

    private final BusinessPlanFinalRepository businessPlanFinalRepository;
    private final ProductOrServiceRepository productRepository;
    private final TeamRepository teamRepository;
    private final MarketingRepository marketingRepository;
    private final AIGenerationService aiGenerationService;
    private  final CompanyRepository companyRepository;
    private final AIGenerationService aiService;
    private  final AIGeneratedResponseRepository aiGeneratedResponseRepository;

    public BusinessPlanFinalService(BusinessPlanFinalRepository businessPlanFinalRepository, ProductOrServiceRepository productRepository, TeamRepository teamRepository, MarketingRepository marketingRepository, AIGenerationService aiGenerationService, CompanyRepository companyRepository, AIGenerationService aiService, AIGeneratedResponseRepository aiGeneratedResponseRepository) {
        this.businessPlanFinalRepository = businessPlanFinalRepository;
        this.productRepository = productRepository;
        this.teamRepository = teamRepository;
        this.marketingRepository = marketingRepository;
        this.aiGenerationService = aiGenerationService;
        this.companyRepository = companyRepository;
        this.aiService = aiService;
        this.aiGeneratedResponseRepository = aiGeneratedResponseRepository;
    }
    public Optional<BusinessPlanFinal> getByCompanyId(String companyId) {
        return businessPlanFinalRepository.findByCompany_Id(companyId);
    }
    public BusinessPlanFinalDTO generateAIBusinessPlan(String companyId) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Company not found with id: " + companyId));

        List<AIResponseDTO> aiResponses = new ArrayList<>();

        // COMPANY
        aiGeneratedResponseRepository.findByEntityTypeAndEntityId(EntityType.COMPANY, company.getId())
            .ifPresentOrElse(
                ai -> aiResponses.add(new AIResponseDTO("COMPANY", ai.getAiResponse())),
                () -> aiResponses.add(new AIResponseDTO("COMPANY", "Aucune réponse IA trouvée."))
            );

        // PRODUCTS
        productRepository.findAllByCompany_Id(companyId).forEach(product -> {
            aiGeneratedResponseRepository.findByEntityTypeAndEntityId(EntityType.PRODUCT, product.getId())
                .ifPresentOrElse(
                    ai -> aiResponses.add(new AIResponseDTO("PRODUCT", ai.getAiResponse())),
                    () -> aiResponses.add(new AIResponseDTO("PRODUCT", "Aucune réponse IA trouvée pour le produit " + product.getNameProductOrService()))
                );
        });

        // TEAMS
        teamRepository.findAllByCompany_Id(companyId).forEach(team -> {
            aiGeneratedResponseRepository.findByEntityTypeAndEntityId(EntityType.TEAM, team.getId())
                .ifPresentOrElse(
                    ai -> aiResponses.add(new AIResponseDTO("TEAM", ai.getAiResponse())),
                    () -> aiResponses.add(new AIResponseDTO("TEAM", "Aucune réponse IA trouvée pour ce membre."))
                );
        });

        // MARKETING
        marketingRepository.findAllByCompany_Id(companyId).forEach(marketing -> {
            aiGeneratedResponseRepository.findByEntityTypeAndEntityId(EntityType.MARKETING, marketing.getId())
                .ifPresentOrElse(
                    ai -> aiResponses.add(new AIResponseDTO("MARKETING", ai.getAiResponse())),
                    () -> aiResponses.add(new AIResponseDTO("MARKETING", "Aucune réponse IA trouvée."))
                );
        });

        BusinessPlanFinalDTO dto = new BusinessPlanFinalDTO();
        dto.setTitle(company.getEnterpriseName());
        dto.setCreationDate(Instant.now());
        dto.setAiResponses(aiResponses);

        return dto;
    }
   /*public BusinessPlanFinal generateBusinessPlan(Company company, BusinessPlanFinal businessPlanFinal) {
        // Récupérer les réponses IA liées à l’entreprise
        List<AIGeneratedResponse> responses = aiGeneratedResponseRepository.findByEntityIdAndEntityType(company.getId(), EntityType.COMPANY);

        // Regrouper le contenu par type
        Map<String, String> contentByType = responses.stream()
            .collect(Collectors.toMap(
                r -> r.getEntityType().name().toLowerCase(),
                AIGeneratedResponse::getAiResponse,
                (v1, v2) -> v1 + "\n" + v2
            ));

        // Construire le prompt
        String prompt = buildPromptFromModel(contentByType, company.getEnterpriseName());

        // Générer le texte avec Gemini
        String result = aiGenerationService.generateText(prompt);

        // Mettre à jour l’objet BusinessPlanFinal
        businessPlanFinal.setFinalContent(result);
        businessPlanFinal.setCreationDate(Instant.now());

        // ** Titre = nom de l’entreprise **
        businessPlanFinal.setTitle(company.getEnterpriseName());

        // Sauvegarder et retourner
        return businessPlanFinalRepository.save(businessPlanFinal);
    }
*/

/*
    private String buildPromptFromModel(Map<String, String> sections, String companyName) {
        return "Tu es un expert en création de business plan professionnel. Génère un business plan structuré pour l'entreprise \"" + companyName + "\" selon ce plan :\n\n"
            + "1. Synthèse\n"
            + sections.getOrDefault("summary", "") + "\n\n"

            + "2. Description de l’entreprise\n"
            + sections.getOrDefault("COMPANY", "") + "\n\n"

            + "3. L’opportunité\n"
            + sections.getOrDefault("PRODUCT", "") + "\n\n"

            + "4. Analyse du secteur d’activité\n"
            + sections.getOrDefault("marketing", "") + "\n\n"

            + "5. Étude et définition du marché cible\n"
            + sections.getOrDefault("MARKETING", "") + "\n\n"

            + "6. Équipe de management\n"
            + sections.getOrDefault("TEAM", "") + "\n\n"

            + "7. Plan des opérations\n"
            + "(À compléter à partir du contexte de l’entreprise)\n\n"

            + "8. Stratégie marketing\n"
            + sections.getOrDefault("MARKETING", "") + "\n\n"

            + "9. Plan et échéancier de mise en œuvre\n"
            + "(À générer selon le secteur)\n\n"

            + "10. Plan de financement\n"
            + "(À générer en fonction des investissements et prévisions)\n\n"

            + "11. Conclusion\n"
            + "Résume les points clés et incite à l’investissement.\n\n"

            + "Génère un document fluide, clair et professionnel.";
    }
*/
    public List<BusinessPlanFinalDTO> findAllByCompany(String companyId) {
        return businessPlanFinalRepository.findAllByCompany_Id(companyId)
            .stream()
            .map(plan -> {
                BusinessPlanFinalDTO dto = new BusinessPlanFinalDTO();

                dto.setTitle(plan.getTitle());
                dto.setCreationDate(plan.getCreationDate());
                dto.setCompanyId(plan.getCompany().getId());
                return dto;
            })
            .collect(Collectors.toList());

    }
    public List<BusinessPlanFinalDTO> findAll() {
        return businessPlanFinalRepository.findAll().stream()
            .map(plan -> {
                BusinessPlanFinalDTO dto = new BusinessPlanFinalDTO();
                dto.setTitle(plan.getTitle());
                dto.setCreationDate(plan.getCreationDate());
                dto.setFinalContent(plan.getFinalContent());
                dto.setBudgetJsonData(plan.getBudgetJsonData());
                dto.setId(plan.getId());

                if (plan.getCompany() != null) {
                    String companyId = plan.getCompany().getId();
                    dto.setCompanyId(companyId);

                    // récupérer tous les IDs d’entités liées à cette company
                    List<String> allEntityIds = new ArrayList<>();
                    allEntityIds.addAll(productRepository.findAllByCompanyId(companyId).stream().map(ProductOrService::getId).collect(Collectors.toList()));
                    allEntityIds.addAll(teamRepository.findAllByCompanyId(companyId).stream().map(Team::getId).collect(Collectors.toList()));
                    allEntityIds.addAll(marketingRepository.findAllByCompanyId(companyId).stream().map(Marketing::getId).collect(Collectors.toList()));

                    allEntityIds.add(companyId); // inclure la company elle-même

                    // récupérer toutes les réponses IA liées
                    List<AIGeneratedResponse> aiList = aiGeneratedResponseRepository.findAllByEntityIdIn(allEntityIds);

                    List<AIResponseDTO> aiDtos = aiList.stream()
                        .map(ai -> new AIResponseDTO(ai.getEntityType().name(), ai.getAiResponse()))
                        .collect(Collectors.toList());

                    dto.setAiResponses(aiDtos);
                } else {
                    dto.setCompanyId(null);
                    dto.setAiResponses(Collections.emptyList());
                }

                return dto;
            })
            .collect(Collectors.toList());
    }
    public ByteArrayOutputStream generatePdf(BusinessPlanFinal plan, Company company) throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        document.addTitle("Business Plan PDF");
        document.add(new Paragraph("Nom de l'entreprise : " + company.getEnterpriseName()));
        document.add(new Paragraph("Titre du Business Plan : " + plan.getTitle()));
        document.add(new Paragraph("Contenu final : " + plan.getFinalContent()));

        document.close();
        return outputStream;
    }




    public File generatePdfForCompany(String companyId) throws IOException {
        BusinessPlanFinal businessPlan = businessPlanFinalRepository.findByCompany_Id(companyId)
            .orElseThrow(() -> new EntityNotFoundException("Aucun BusinessPlanFinal trouvé pour cette entreprise"));

        String companyName = businessPlan.getCompany().getEnterpriseName();
        String content = businessPlan.getFinalContent(); // ou selon ton champ réel

        // Chemin temporaire du fichier
        String fileName = "BusinessPlan-" + companyName + "-" + LocalDate.now() + ".pdf";
        File pdfFile = new File(System.getProperty("java.io.tmpdir"), fileName);

        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            Document document = new Document();
            PdfWriter.getInstance(document, fos);
            document.open();
            document.add(new Paragraph("Business Plan de l'entreprise: " + companyName));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(content));
            document.close();
        } catch (DocumentException e) {
            throw new IOException("Erreur lors de la génération du PDF", e);
        }

        return pdfFile;
    }



    /*
    public BusinessPlanFinal generatePlanFromCompany(String companyId) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Company not found with id: " + companyId));
        Set<ProductOrService> products = productRepository.findAllByCompany_Id(companyId);
        Set<Team> teams = teamRepository.findAllByCompany_Id(companyId);
        Set<Marketing> marketings = marketingRepository.findAllByCompany_Id(companyId);

        BusinessPlanFinal plan = new BusinessPlanFinal();
        plan.setCompany(company);
        plan.setProducts(products);
        plan.setTeams(teams);
        plan.setMarketings(marketings);
        plan.setTitle("Business Plan pour " + company.getEnterpriseName());
        plan.setCreationDate(Instant.now());

        return businessPlanFinalRepository.save(plan);
    }*/

    //
    //
    //
    //public List<AIGeneratedResponse> getAllResponsesForCompany(String companyId) {
    //  List<String> entityIds = new ArrayList<>();
    //entityIds.addAll(productRepository.findAllByCompanyId(companyId).stream().map(ProductOrService::getId).toList());
    //entityIds.addAll(teamRepository.findAllByCompanyId(companyId).stream().map(Team::getId).toList());
    //entityIds.addAll(marketingRepository.findAllByCompanyId(companyId).stream().map(Marketing::getId).toList());


    //return aiGenerationService.findByEntityIdIn(entityIds);
    //}

    /**
     * Save a businessPlanFinal.
     *
     * @param businessPlanFinal the entity to save.
     * @return the persisted entity.
     */
    public BusinessPlanFinal save(BusinessPlanFinal businessPlanFinal) {
        log.debug("Request to save BusinessPlanFinal : {}", businessPlanFinal);
        return businessPlanFinalRepository.save(businessPlanFinal);
    }

    /**
     * Update a businessPlanFinal.
     *
     * @param businessPlanFinal the entity to save.
     * @return the persisted entity.
     */
    public BusinessPlanFinal update(BusinessPlanFinal businessPlanFinal) {
        log.debug("Request to update BusinessPlanFinal : {}", businessPlanFinal);
        return businessPlanFinalRepository.save(businessPlanFinal);
    }

    /**
     * Partially update a businessPlanFinal.
     *
     * @param businessPlanFinal the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<BusinessPlanFinal> partialUpdate(BusinessPlanFinal businessPlanFinal) {
        log.debug("Request to partially update BusinessPlanFinal : {}", businessPlanFinal);

        return businessPlanFinalRepository
            .findById(businessPlanFinal.getId())
            .map(existingBusinessPlanFinal -> {
                if (businessPlanFinal.getTitle() != null) {
                    existingBusinessPlanFinal.setTitle(businessPlanFinal.getTitle());
                }
                if (businessPlanFinal.getDescription() != null) {
                    existingBusinessPlanFinal.setDescription(businessPlanFinal.getDescription());
                }
                if (businessPlanFinal.getCreationDate() != null) {
                    existingBusinessPlanFinal.setCreationDate(businessPlanFinal.getCreationDate());
                }
                if (businessPlanFinal.getCompany() != null) {
                    existingBusinessPlanFinal.setCompany(businessPlanFinal.getCompany());
                }
                if (businessPlanFinal.getProducts() != null) {
                    existingBusinessPlanFinal.setProducts(businessPlanFinal.getProducts());
                }
                if (businessPlanFinal.getTeams() != null) {
                    existingBusinessPlanFinal.setTeams(businessPlanFinal.getTeams());
                }
                if (businessPlanFinal.getMarketings() != null) {
                    existingBusinessPlanFinal.setMarketings(businessPlanFinal.getMarketings());
                }

                return existingBusinessPlanFinal;
            })
            .map(businessPlanFinalRepository::save);
    }




    /**
     * Get one businessPlanFinal by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<BusinessPlanFinal> findOne(String id) {
        log.debug("Request to get BusinessPlanFinal : {}", id);
        return businessPlanFinalRepository.findById(id);
    }

    /**
     * Delete the businessPlanFinal by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        log.debug("Request to delete BusinessPlanFinal : {}", id);
        if (!businessPlanFinalRepository.existsById(id)) {
            throw new EntityNotFoundException("BusinessPlanFinal not found with id " + id);
        }
        businessPlanFinalRepository.deleteById(id);
    }
    public String createMainBusinessPlanPrompt(Company company,
                                               List<ProductOrService> products,
                                               List<Team> teamMembers,
                                               List<Marketing> marketingData) {

        // SECTION Produits
        StringBuilder productsSection = new StringBuilder();
        for (ProductOrService p : products) {
            productsSection.append("- Nom : ").append(p.getNameProductOrService()).append("\n")
                .append("  Description : ").append(p.getProductDescription()).append("\n")
                .append("  Prix estimé : ").append(String.format("%.2f", p.getEstimatedMonthlySales().doubleValue()))
                .append(" ").append(company.getCurrency()).append("\n\n");
        }

        // SECTION Équipe
        StringBuilder teamSection = new StringBuilder();
        for (Team t : teamMembers) {
            teamSection.append("- ").append(t.getName()).append(" (").append(t.getRole()).append(") : ")
                .append(t.getCompetance()).append(" ans d'expérience\n");
        }

        // SECTION Marketing
        StringBuilder marketingSection = new StringBuilder();
        for (Marketing m : marketingData) {
            marketingSection.append("- Canal de distribution : ").append(m.getDistribution_Channel()).append("\n")
                .append("  Canal marketing : ").append(m.getMarketing_channel()).append("\n")
                .append("  Objectif de ventes : ").append(m.getSales_target()).append(" ").append(m.getCurrency()).append("\n\n");
        }

        String prompt =
            "# Plan d'Affaires de l'entreprise : " + company.getEnterpriseName() + "\n\n" +
                "## Informations Générales\n\n" +
                "- **Nom de l'entreprise** : " + company.getEnterpriseName() + "\n" +
                "- **Description** : " + company.getDescription() + "\n" +
                "- **Secteur d'activité** : "  + "\n" +
                "- **Devise utilisée** : " + company.getCurrency() + "\n\n" +

                "## Table des Matières\n\n" +
                "1. Résumé Exécutif\n" +
                "2. Présentation de l’Entreprise\n" +
                "3. Vision & Mission\n" +
                "4. Description de l’Activité\n" +
                "5. Analyse de Marché\n" +
                "6. Environnement Concurrentiel\n" +
                "7. Équipe & Management\n" +
                "8. Produits ou Services\n" +
                "9. Analyse d'Impact des Produits\n" +
                "10. Modèle Économique\n" +
                "11. Stratégie Marketing\n" +
                "12. Plan d’Action (Tâches clés)\n" +
                "13. Phases du Projet & Impacts\n" +
                "14. Calendrier Marketing\n" +
                "15. Besoins de Financement\n" +
                "16. Prévisions Financières\n" +
                "17. Tableau de Bord Financier\n" +
                "18. Plan de Financement\n" +
                "19. Analyse des Risques\n" +
                "20. Répartition Budgétaire\n\n" +

                "---\n\n" +
                "## Données à intégrer\n\n" +
                "### Produits/Services\n\n" +
                productsSection.toString() + "\n" +
                "### Équipe\n\n" +
                teamSection.toString() + "\n" +
                "### Marketing\n\n" +
                marketingSection.toString() + "\n\n" +

                "---\n\n" +
                "## Consignes de génération\n\n" +
                "Tu es un expert en rédaction de plans d’affaires. Génère le contenu **complet et professionnel** des 20 sections ci-dessus :\n\n" +
                "- Utilise les données fournies dans les bonnes sections.\n" +
                "- Rédige en français clair, stratégique et convaincant.\n" +
                "- Chaque section doit contenir entre 100 et 300 mots sauf tableaux.\n" +
                "- Ne produis aucun JSON.\n" +
                "- Fournis tous les tableaux suivants exactement dans ce format Markdown :\n\n" +
                "- Les sections financières (15, 16, 18) doivent inclure des tableaux clairs sur 3 ans (2025, 2026, 2027) avec des montants réalistes et justifiés.\n" +
                "- La section 15 doit détailler les besoins de financement (capital initial, équipements, frais fixes) et justifier chaque poste.\n" +
                "- La section 16 doit fournir un tableau de prévision financière sur 3 ans avec Revenus, Dépenses, Profits, Croissance (%), Investissements requis.\n" +
                "- La section 18 doit décrire un plan de financement clair : sources (prêt, subvention, apport personnel), calendrier de décaissement, conditions.\n\n"+

                "### Section 9 – Analyse d'Impact des Produits\n\n" +
                "| **Partie Prenante Principale**   | **Avantages du Produit**           |\n" +
                "|----------------------------------|------------------------------------|\n" +
                "| Clients                          | [avantages spécifiques]            |\n" +
                "| Employés                         | [avantages spécifiques]            |\n" +
                "| Fournisseurs                     | [avantages spécifiques]            |\n" +
                "| Investisseurs                    | [avantages spécifiques]            |\n" +
                "| Communautés Locales              | [avantages spécifiques]            |\n" +
                "| Organismes de Réglementation     | [avantages spécifiques]            |\n\n" +

                "---\n\n" +
                "### Section 12 – Tâches Organisationnelles & Marketing\n\n" +
                "#### Tâches Organisationnelles\n\n" +
                "| **Tâche** | **Statut** | **Priorité** | **Domaine** | **Étape** |\n" +
                "|----------|------------|--------------|-------------|-----------|\n" +
                "| [exemple] | À faire | Haute | RH | Lancement |\n\n" +

                "#### Tâches Marketing\n\n" +
                "| **Tâche** | **Statut** | **Priorité** | **Domaine** | **Étape** |\n" +
                "|----------|------------|--------------|-------------|-----------|\n" +
                "| [exemple] | En cours | Moyenne | Acquisition | Pré-lancement |\n\n" +

                "---\n\n" +
                "### Section 13 – Phases du Projet\n\n" +
                "| **Phase** | **Description** | **Calendrier** |\n" +
                "|-------------------------------|------------------------|----------------------|\n" +
                "| Établissement Fondamental | [description] | [Q1 2025] |\n" +
                "| Amélioration Produits & Expansion | [description] | [Q2–Q3 2025] |\n" +
                "| Nouvelles Sources de Revenus | [description] | [Q4 2025 – Q1 2026] |\n" +
                "| Innovation Stratégique | [description] | [Q2 2026+] |\n\n" +

                "---\n\n" +
                "### Section 14 – Calendrier du Marché\n\n" +
                "| **Mois** | **Action Marketing** | **Objectif** |\n" +
                "|----------|------------------------|------------------------|\n" +
                "| Janvier | [exemple] | [objectif mensuel] |\n" +
                "| ... | ... | ... |\n" +
                "| Décembre | [exemple] | [objectif mensuel] |\n\n" +

                "---\n\n" +
                "### Section 16 – Prévisions Financières\n\n" +
                "| **Année** | **Revenus** | **Dépenses** | **Profit** | **Croissance** | **Investissement Requis** |\n" +
                "|----------|-------------|--------------|------------|----------------|----------------------------|\n" +
                "| 2025 | [valeur] | [valeur] | [valeur] | [taux %] | [valeur] |\n" +
                "| 2026 | [valeur] | [valeur] | [valeur] | [taux %] | [valeur] |\n\n" +
                "| 2027     | [valeur]           | [valeur]            | [valeur]         | [taux %]       | [valeur]                         |\n" +
               " ### Section 15 – Besoins de Financement\n\n" +

            "| **Catégorie**            | **Montant estimé (TND)** | **Justification** | \n" +
"|--------------------------|---------------------------|----------------------------------------------------|\n" +
"| Capital initial          | [valeur]                  | [ex. achat de stock initial]                      |\n" +
"| Équipements              | [valeur]                  | [ex. ordinateurs, machines, etc.]                 |\n" +
"| Frais de fonctionnement  | [valeur]                  | [ex. salaires, loyer, abonnements]                |\n" +
"| Marketing                | [valeur]                  | [ex. lancement produit, pub digitale]             |\n" +
 "           | Trésorerie de sécurité   | [valeur]                  | [ex. marge pour imprévus 3 mois]                  |\n" +
"| **Total**                | **[total]**               |\n" +

                "### Section 17 – Tableau de Bord Financier\n\n" +
                "| **Indicateur** | **Objectif à 1 an** | **Objectif à 3 ans** |\n" +
                "|----------------|----------------------|----------------------|\n" +
                "| MRR | [valeur] | [valeur] |\n" +
                "| CAC | [valeur] | [valeur] |\n" +
                "| Marge EBITDA | [valeur] | [valeur] |\n\n" +
"### Section 18 – Plan de Financement\n\n" +

 "           | **Source de Financement** | **Montant (TND)** | **Type**               | **Conditions**                             | **Décaissement Prévu** |\n" +
"|---------------------------|-------------------|------------------------|--------------------------------------------|-------------------------|\n" +
"| Apport personnel          | [valeur]          | Fonds propres          | Aucune                                     | [Q1 2025]               |\n" +
"| Prêt bancaire             | [valeur]          | Crédit à moyen terme   | Taux 5 %, remboursement sur 3 ans          | [Q1–Q2 2025]            |\n" +
"| Subvention publique       | [valeur]          | Aide à l’innovation    | Sur dossier accepté                        | [Q2 2025]               |\n" +
"| Investisseur privé        | [valeur]          | Capital-risque         | Participation de 15 % au capital           | [Q3 2025]               |\n" +
"| **Total**                 | **[total]**       |                        |                                            |                         |\n" +


                "---\n\n" +
                "### Section 19 – Analyse des Risques\n\n" +
                "| **Risque** | **Probabilité** | **Impact** | **Stratégie d’Atténuation** |\n" +
                "|----------------|-----------------|----------|-----------------------------|\n" +
                "| [exemple] | Élevée | Fort | Diversification fournisseurs |\n\n" +

                "---\n\n" +
                "### Section 20 – Répartition Budgétaire\n\n" +
                "| **Poste de Dépense** | **Pourcentage** |\n" +
                "|------------------------|-----------------|\n" +
                "| Ressources humaines | 40 % |\n" +
                "| Marketing | 25 % |\n" +
                "| Technologie | 20 % |\n" +
                "| Autres | 15 % |\n\n" +

                "---\n\n" +
                "Merci de générer ce contenu en respectant strictement les consignes ci-dessus, section par section, en Markdown, sans aucun JSON ni bloc technique.";

        return prompt;
    }



    /**
     * Crée le prompt pour générer UNIQUEMENT les données du budget au format JSON.
     */
    public String createBudgetDataPrompt(Company company) {
        String prompt =
            "Génère uniquement les données de répartition budgétaire au format JSON. Ce JSON doit être un tableau d'objets, où chaque objet a deux propriétés : `poste` (String) et `montant` (Number).\n" +
                "Les postes à inclure sont :\n" +
                "- Marketing\n" +
                "- Ressources Humaines\n" +
                "- Recherche & Développement\n" +
                "- Logistique\n" +
                "- Autres\n\n" +
                "Le montant total de ces postes ne doit pas dépasser " + company.getAmount() + " " + company.getCurrency() + ".\n" +
                "Le format JSON attendu est le suivant :\n" +
                "```json\n" +
                "[\n" +
                "  { \"poste\": \"Marketing\", \"montant\": [valeur] },\n" +
                "  { \"poste\": \"Ressources Humaines\", \"montant\": [valeur] },\n" +
                "  { \"poste\": \"Recherche & Développement\", \"montant\": [valeur] },\n" +
                "  { \"poste\": \"Logistique\", \"montant\": [valeur] },\n" +
                "  { \"poste\": \"Autres\", \"montant\": [valeur] }\n" +
                "]\n" +
                "```\n" +
                "Réponds uniquement par ce bloc de code JSON, sans aucun texte additionnel."; // Instruction très stricte pour n'avoir QUE le JSON
        return prompt;
    }



    // Génère le business plan complet
    public BusinessPlanFinal generateBusinessPlan(
        Company company,
        Set<ProductOrService> productsSet,
        Set<Team> teamMembersSet,
        Set<Marketing> marketingDataSet,
        BusinessPlanFinal entity) {

        // These conversions are correct and needed for your prompt creation methods
        List<ProductOrService> productsList = new ArrayList<>(productsSet);
        List<Team> teamMembersList = new ArrayList<>(teamMembersSet);
        List<Marketing> marketingDataList = new ArrayList<>(marketingDataSet);

        // 1. Génération du prompt pour le contenu principal du plan
        String mainPlanPrompt = createMainBusinessPlanPrompt(company, productsList, teamMembersList, marketingDataList);
        String generatedMainContent = aiGenerationService.generateText(mainPlanPrompt);

        // 2. Génération du prompt pour les données du budget (pure JSON)
        String budgetDataPrompt = createBudgetDataPrompt(company);
        String generatedBudgetJsonRaw = aiGenerationService.generateText(budgetDataPrompt);

        // --- CLEANING AND SETTING FINAL CONTENT ---
        String cleanedMainContent = generatedMainContent
            .replace("```", "")
            .replace("_ ", " ")
            .replace("_", "")
            .replace("Ú", "é")
            .replace("Þ", "ê")
            .replace("Ó", "à")
            .replace("¶", "ô")
            .replace("ý", "ù")
            .replace("É", "É")
            .trim();

        // ✅ Set the FINAL, CLEANED main content here
        entity.setFinalContent(cleanedMainContent);


        // --- CLEANING AND SETTING BUDGET JSON DATA ---
        String cleanedBudgetJson = generatedBudgetJsonRaw
            .replace("```json", "")
            .replace("```", "")
            .replace("_", "")
            .replace("Ú", "é") // Ensure this covers any potential accents in JSON keys/values
            .trim();

        // ✅ Set the FINAL, CLEANED budget JSON here
        entity.setBudgetJsonData(cleanedBudgetJson);

        // No redundant setFinalContent or setBudgetJsonData calls after this point!

        return businessPlanFinalRepository.save(entity);
    }


    // Méthode pour générer directement à partir de l'ID de l'entreprise
    public BusinessPlanFinal generateBusinessPlan(String companyId) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new EntityNotFoundException("Entreprise non trouvée"));

        // Récupération des données sous forme de Set
        Set<ProductOrService> products = productRepository.findAllByCompany_Id(companyId);
        Set<Team> teamMembers = teamRepository.findAllByCompany_Id(companyId);
        Set<Marketing> marketingData = marketingRepository.findAllByCompany_Id(companyId);

        BusinessPlanFinal entity = new BusinessPlanFinal();
        entity.setCompany(company);

        return generateBusinessPlan(company, products, teamMembers, marketingData, entity);
    }
  /*  public BusinessPlanFinal generateBusinessPlan(
        Company company,
        List<ProductOrService> products,
        List<Team> teamMembers,
        List<Marketing> marketingData,
        BusinessPlanFinal entity) {

        // Votre logique de génération ici
        String prompt = createBusinessPlanPrompt(company, products, teamMembers, marketingData);
        String generatedContent = aiGenerationService.generateText(prompt);

        entity.setFinalContent(generatedContent);
        entity.setCreationDate(Instant.now());

        return businessPlanFinalRepository.save(entity);
    }*/
    private String formatProductRow(ProductOrService p, Company company) {
        // Conversion sécurisée Double → BigDecimal
        BigDecimal unitPrice = p.getUnitPrice() != null
            ? BigDecimal.valueOf(p.getUnitPrice())  // Conversion explicite
            : BigDecimal.ZERO;

        String description = StringUtils.defaultString(p.getProductDescription(), "N/A");
        int duration = p.getDurationInMonths() != null ? p.getDurationInMonths() : 0;

        return String.format("| %s | %s %s | %s | %d mois |",
            StringUtils.defaultString(p.getNameProductOrService(), "N/A"),
            unitPrice.setScale(2, RoundingMode.HALF_UP),  // Formatage à 2 décimales
            company.getCurrency(),
            description,
            duration);
    }
}
