package com.readytoplanbe.myapp.service.dto;

import java.time.Instant;
import java.util.List;

public class BusinessPlanFinalDTO {
    private String id;
    private String title;
    private Instant creationDate;
    private List<AIResponseDTO> aiResponses;
    private  String companyId;
    private String finalContent;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFinalContent(String finalContent) {
        this.finalContent = finalContent;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    // Getters et Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public List<AIResponseDTO> getAiResponses() {
        return aiResponses;
    }

    public void setAiResponses(List<AIResponseDTO> aiResponses) {
        this.aiResponses = aiResponses;
    }

    @Override
    public String toString() {
        return "BusinessPlanFinalDTO{" +
            "id='" + id + '\'' +
            ", title='" + title + '\'' +
            ", creationDate=" + creationDate +
            ", aiResponses=" + aiResponses +
            ", companyId='" + companyId + '\'' +
            ", finalContent='" + finalContent + '\'' +
            ", budgetJsonData='" + budgetJsonData + '\'' +
            '}';
    }
}
