package com.readytoplanbe.myapp.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A BusinessPlanFinal.
 */
@Document(collection = "business_plan_final")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BusinessPlanFinal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Field("title")
    private String title;

    @Field("description")
    private String description;
    @CreatedDate
    @Field("creation_date")
    private Instant creationDate;

    @DBRef
    private Company company;

    @DBRef
    private Set<ProductOrService> products = new HashSet<>();

    @DBRef
    private Set<Team> teams = new HashSet<>();

    @DBRef
    private Set<Marketing> marketings = new HashSet<>();

    @Field("final_content")
    private String finalContent;

    @Field("budget_Json_Data")
    private String budgetJsonData;

    public String getBudgetJsonData() {
        return budgetJsonData;
    }

    public void setBudgetJsonData(String budgetJsonData) {
        this.budgetJsonData = budgetJsonData;
    }

    public String getFinalContent() {
        return finalContent;
    }

    public void setFinalContent(String finalContent) {
        this.finalContent = finalContent;
    }

    public BusinessPlanFinal companyId(String companyId) {
        this.company = new Company();
        this.company.setId(companyId);
        return this;
    }

    public BusinessPlanFinal productsIds(Set<String> productIds) {
        this.products = productIds.stream().map(id -> {
            ProductOrService p = new ProductOrService();
            p.setId(id);
            return p;
        }).collect(Collectors.toSet());
        return this;
    }

// jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public BusinessPlanFinal id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public BusinessPlanFinal title(String title) {
        this.setTitle(title);
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public BusinessPlanFinal description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreationDate() {
        return this.creationDate;
    }

    public BusinessPlanFinal creationDate(Instant creationDate) {
        this.setCreationDate(creationDate);
        return this;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }


    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Set<ProductOrService> getProducts() {
        return products;
    }

    public void setProducts(Set<ProductOrService> products) {
        this.products = products;
    }

    public Set<Team> getTeams() {
        return teams;
    }

    public void setTeams(Set<Team> teams) {
        this.teams = teams;
    }

    public Set<Marketing> getMarketings() {
        return marketings;
    }

    public void setMarketings(Set<Marketing> marketings) {
        this.marketings = marketings;
    }
    public String getCompanyId() {
        return company != null ? company.getId() : null;
    }

    public Set<String> getProductsIds() {
        return products.stream().map(ProductOrService::getId).collect(Collectors.toSet());
    }

    public Set<String> getTeamsIds() {
        return teams.stream().map(Team::getId).collect(Collectors.toSet());
    }

    public Set<String> getMarketingsIds() {
        return marketings.stream().map(Marketing::getId).collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BusinessPlanFinal)) {
            return false;
        }
        return id != null && id.equals(((BusinessPlanFinal) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore

    @Override
    public String toString() {
        return "BusinessPlanFinal{" +
            "id='" + id + '\'' +
            ", title='" + title + '\'' +
            ", description='" + description + '\'' +
            ", creationDate=" + creationDate +
            ", company=" + company +
            ", products=" + products +
            ", teams=" + teams +
            ", marketings=" + marketings +
            ", finalContent='" + finalContent + '\'' +", budgetJsonData='" + budgetJsonData + '\'' +
            '}';

    }
}
