package com.readytoplanbe.myapp.web.rest;

import com.readytoplanbe.myapp.domain.*;
import com.readytoplanbe.myapp.repository.*;
import com.readytoplanbe.myapp.service.BusinessPlanFinalService;
import com.readytoplanbe.myapp.service.dto.BusinessPlanFinalDTO;
import com.readytoplanbe.myapp.service.mapper.BusinessPlanFinalMapper;
import com.readytoplanbe.myapp.web.rest.errors.BadRequestAlertException;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.readytoplanbe.myapp.domain.BusinessPlanFinal}.
 */
@RestController
@RequestMapping("/api")
public class BusinessPlanFinalResource {

    private final Logger log = LoggerFactory.getLogger(BusinessPlanFinalResource.class);

    private static final String ENTITY_NAME = "businessPlanFinal";
    private final ProductOrServiceRepository productOrServiceRepository;
    private final MarketingRepository marketingRepository;
    private final TeamRepository teamRepository;

    @Value("${jhipster.clientApp.name}")
    private String applicationName;
    @Autowired
    private BusinessPlanFinalMapper businessPlanFinalMapper;

    private final BusinessPlanFinalService businessPlanFinalService;
    private final CompanyRepository companyRepository;
    private final BusinessPlanFinalRepository businessPlanFinalRepository;

    public BusinessPlanFinalResource(
        ProductOrServiceRepository productOrServiceRepository, MarketingRepository marketingRepository, TeamRepository teamRepository, BusinessPlanFinalMapper businessPlanFinalMapper, BusinessPlanFinalService businessPlanFinalService,
        CompanyRepository companyRepository, BusinessPlanFinalRepository businessPlanFinalRepository
    ) {
        this.productOrServiceRepository = productOrServiceRepository;
        this.marketingRepository = marketingRepository;
        this.teamRepository = teamRepository;
        this.businessPlanFinalMapper = businessPlanFinalMapper;
        this.businessPlanFinalService = businessPlanFinalService;
        this.companyRepository = companyRepository;
        this.businessPlanFinalRepository = businessPlanFinalRepository;
    }

    /**
     * {@code POST  /business-plan-finals} : Create a new businessPlanFinal.
     *
     * @param businessPlanFinal the businessPlanFinal to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new businessPlanFinal, or with status {@code 400 (Bad Request)} if the businessPlanFinal has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/business-plan-finals")
    public ResponseEntity<BusinessPlanFinal> createBusinessPlanFinal(@Valid @RequestBody BusinessPlanFinal businessPlanFinal)
        throws URISyntaxException {
        log.debug("REST request to save BusinessPlanFinal : {}", businessPlanFinal);
        if (businessPlanFinal.getId() != null) {
            throw new BadRequestAlertException("A new businessPlanFinal cannot already have an ID", ENTITY_NAME, "idexists");
        }
        BusinessPlanFinal result = businessPlanFinalService.save(businessPlanFinal);
        return ResponseEntity
            .created(new URI("/api/business-plan-finals/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId()))
            .body(result);
    }/*
    @PostMapping("/generate/{companyId}")
    public BusinessPlanFinalDTO generateFinalPlan(@PathVariable String companyId, @RequestBody BusinessPlanFinalDTO dto) {
        Company company = companyRepository.findById(companyId).orElseThrow();
        BusinessPlanFinal entity = businessPlanFinalMapper.toEntity(dto);
        entity.setCompany(company);
        BusinessPlanFinal result = businessPlanFinalService.generateBusinessPlan(company, entity);
        return businessPlanFinalMapper.toDto(result);
    }*/
    @PostMapping("/generate/{companyId}")
    public ResponseEntity<BusinessPlanFinalDTO> generateFinalPlan(
        @PathVariable String companyId,
        @RequestBody BusinessPlanFinalDTO dto) {

        // 1. Récupération de la compagnie
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + companyId));

        // Assigner companyName au champ title du DTO
        dto.setTitle(company.getEnterpriseName());

        // 2. Récupération des listes associées
        List<ProductOrService> productsList = new ArrayList<>(
            productOrServiceRepository.findAllByCompany_Id(companyId));
        List<Team> teamMembersList = new ArrayList<>(
            teamRepository.findAllByCompany_Id(companyId));
        List<Marketing> marketingDataList = new ArrayList<>(
            marketingRepository.findAllByCompany_Id(companyId));

        // 3. Conversion en entité et génération
        BusinessPlanFinal entity = businessPlanFinalMapper.toEntity(dto);
        entity.setCompany(company);

        // --- CRUCIAL CHANGE HERE ---
        // Convert the Lists to Sets before passing them to the service method
        // because generateBusinessPlan expects Sets.
        BusinessPlanFinal result = businessPlanFinalService.generateBusinessPlan(
            company,
            new HashSet<>(productsList),   // Convert List<ProductOrService> to Set<ProductOrService>
            new HashSet<>(teamMembersList), // Convert List<Team> to Set<Team>
            new HashSet<>(marketingDataList),// Convert List<Marketing> to Set<Marketing>
            entity);

        // 4. Retour du DTO final
        return ResponseEntity.ok()
            .header("X-Generated-Date", Instant.now().toString())
            .body(businessPlanFinalMapper.toDto(result));
    }
    @GetMapping("/business-plan-final/ai-only/{companyId}")
    public ResponseEntity<BusinessPlanFinalDTO> getAIOnlyBusinessPlan(@PathVariable String companyId) {
        try {
            BusinessPlanFinalDTO dto = businessPlanFinalService.generateAIBusinessPlan(companyId);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Erreur lors de la génération du plan IA pour l'entreprise {}", companyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/business-plan-finals/by-company/{companyId}")
    public ResponseEntity<List<BusinessPlanFinalDTO>> getAllBusinessPlansByCompany(@PathVariable String companyId) {
        List<BusinessPlanFinalDTO> result = businessPlanFinalService.findAllByCompany(companyId);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/business-plan-final")
    public ResponseEntity<List<BusinessPlanFinalDTO>> getAllBusinessPlans() {
        List<BusinessPlanFinalDTO> plans = businessPlanFinalService.findAll();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/business-plan-finals/download/pdf/{businessPlanId}")
    public ResponseEntity<Resource> downloadPdf(@PathVariable String businessPlanId) {
        try {
            // Récupère le BusinessPlanFinal
            Optional<BusinessPlanFinal> planOpt = businessPlanFinalRepository.findById(businessPlanId);
            if (planOpt.isEmpty()) {
                throw new EntityNotFoundException("BusinessPlanFinal not found");
            }

            BusinessPlanFinal plan = planOpt.get();

            // Récupère la société associée
            Company company = plan.getCompany();
            if (company == null) {
                throw new EntityNotFoundException("Company not found in BusinessPlanFinal");
            }

            // Appelle le service d'exportation avec le plan et la société
            ByteArrayOutputStream pdfBytes = businessPlanFinalService.generatePdf(plan, company); // Tu dois adapter cette méthode
            ByteArrayResource resource = new ByteArrayResource(pdfBytes.toByteArray());

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=business-plan-" + company.getEnterpriseName() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @PutMapping("/business-plan-finals/{id}")
    public ResponseEntity<BusinessPlanFinal> updateBusinessPlanFinal(
        @PathVariable String id,
        @RequestBody BusinessPlanFinal businessPlanFinal
    ) {
        log.debug("REST request to update BusinessPlanFinal : {}", businessPlanFinal);

        if (businessPlanFinal.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!businessPlanFinal.getId().equals(id)) {
            throw new BadRequestAlertException("ID mismatch", ENTITY_NAME, "idmismatch");
        }

        BusinessPlanFinal result = businessPlanFinalService.update(businessPlanFinal);
        return ResponseEntity.ok().body(result);
    }
    @GetMapping("/business-plan/statistics/by-date")
    public Map<String, Long> getPlansGroupedByDate() {
        List<BusinessPlanFinal> plans = businessPlanFinalRepository.findAll();
        return plans.stream()
            .collect(Collectors.groupingBy(
                bp -> bp.getCreationDate().atZone(ZoneId.systemDefault()).toLocalDate().toString(),
                TreeMap::new,
                Collectors.counting()
            ));
    }

    /**
     * {@code PATCH  /business-plan-finals/:id} : Partial updates given fields of an existing businessPlanFinal, field will ignore if it is null
     *
     * @param id the id of the businessPlanFinal to save.
     * @param businessPlanFinal the businessPlanFinal to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated businessPlanFinal,
     * or with status {@code 400 (Bad Request)} if the businessPlanFinal is not valid,
     * or with status {@code 404 (Not Found)} if the businessPlanFinal is not found,
     * or with status {@code 500 (Internal Server Error)} if the businessPlanFinal couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/business-plan-finals/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<BusinessPlanFinal> partialUpdateBusinessPlanFinal(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody BusinessPlanFinal businessPlanFinal
    ) throws URISyntaxException {
        log.debug("REST request to partial update BusinessPlanFinal partially : {}, {}", id, businessPlanFinal);
        if (businessPlanFinal.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, businessPlanFinal.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!businessPlanFinalRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<BusinessPlanFinal> result = businessPlanFinalService.partialUpdate(businessPlanFinal);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, businessPlanFinal.getId())
        );
    }



    /**
     * {@code GET  /business-plan-finals/:id} : get the "id" businessPlanFinal.
     *
     * @param id the id of the businessPlanFinal to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the businessPlanFinal, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/business-plan-finals/{id}")
    public ResponseEntity<BusinessPlanFinal> getBusinessPlanFinal(@PathVariable String id) {
        log.debug("REST request to get BusinessPlanFinal : {}", id);
        Optional<BusinessPlanFinal> businessPlanFinal = businessPlanFinalService.findOne(id);
        return ResponseUtil.wrapOrNotFound(businessPlanFinal);
    }

    /**
     * {@code DELETE  /business-plan-finals/:id} : delete the "id" businessPlanFinal.
     *
     * @param id the id of the businessPlanFinal to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/business-plan-finals/{id}")
    public ResponseEntity<Void> deleteBusinessPlanFinal(@PathVariable String id) {
        log.debug("REST request to delete BusinessPlanFinal : {}", id);
        try {
            businessPlanFinalService.delete(id);
            return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
