package com.hawaiki.service;

import com.hawaiki.domain.Plan;
import com.hawaiki.repository.PlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing Plan.
 */
@Service
@Transactional
public class PlanService {

    private final Logger log = LoggerFactory.getLogger(PlanService.class);

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    /**
     * Save a plan.
     *
     * @param plan the entity to save
     * @return the persisted entity
     */
    public Plan save(Plan plan) {
        log.debug("Request to save Plan : {}", plan);
        return planRepository.save(plan);
    }

    /**
     * Get all the plans.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Plan> findAll(Pageable pageable) {
        log.debug("Request to get all Plans");
        return planRepository.findAll(pageable);
    }

    /**
     * Get one plan by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Plan findOne(Long id) {
        log.debug("Request to get Plan : {}", id);
        return planRepository.findOne(id);
    }

    /**
     * Delete the plan by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Plan : {}", id);
        planRepository.delete(id);
    }
}
