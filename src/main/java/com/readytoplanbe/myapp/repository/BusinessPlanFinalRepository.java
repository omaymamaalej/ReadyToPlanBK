package com.readytoplanbe.myapp.repository;

import com.readytoplanbe.myapp.domain.BusinessPlanFinal;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for the BusinessPlanFinal entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BusinessPlanFinalRepository extends MongoRepository<BusinessPlanFinal, String> {
    Optional<BusinessPlanFinal> findByCompany_Id(String companyId);
    List<BusinessPlanFinal> findAllByCompany_Id(String companyId);
    List<BusinessPlanFinal> findAllByOrderByCreationDateDesc();


}
