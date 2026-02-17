package com.readytoplanbe.myapp.service;

import com.readytoplanbe.myapp.domain.BusinessPlanFinal;
import com.readytoplanbe.myapp.domain.Company;
import com.readytoplanbe.myapp.repository.BusinessPlanFinalRepository;
import com.readytoplanbe.myapp.repository.CompanyRepository;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.List;
import java.util.Map; // For grouping result

import java.util.Optional;

@Service
public class DashboardService {
private final CompanyRepository companyRepository;
private final BusinessPlanFinalRepository businessPlanFinalRepository;
    private final MongoTemplate mongoTemplate;

    public DashboardService(CompanyRepository companyRepository, BusinessPlanFinalRepository businessPlanFinalRepository, MongoTemplate mongoTemplate) {
        this.companyRepository = companyRepository;
        this.businessPlanFinalRepository = businessPlanFinalRepository;
        this.mongoTemplate = mongoTemplate;
    }



    public Map<String, Long> countBusinessPlansByCountry() {
        List<BusinessPlanFinal> plans = businessPlanFinalRepository.findAll();
        Map<String, Long> countryCountMap = new HashMap<>();

        for (BusinessPlanFinal plan : plans) {
            if (plan.getCompany() != null) {
                Optional<Company> companyOpt = companyRepository.findById(plan.getCompany().getId());
                if (companyOpt.isPresent()) {
                    String country = companyOpt.get().getCountry();
                    countryCountMap.put(country, countryCountMap.getOrDefault(country, 0L) + 1);
                }
            }
        }

        return countryCountMap;
    }


}

