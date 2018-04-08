package com.hawaiki.service;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.QueryService;

import com.hawaiki.domain.Plan;
import com.hawaiki.domain.*; // for static metamodels
import com.hawaiki.repository.PlanRepository;
import com.hawaiki.service.dto.PlanCriteria;


/**
 * Service for executing complex queries for Plan entities in the database.
 * The main input is a {@link PlanCriteria} which get's converted to {@link Specifications},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link Plan} or a {@link Page} of {@link Plan} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class PlanQueryService extends QueryService<Plan> {

    private final Logger log = LoggerFactory.getLogger(PlanQueryService.class);


    private final PlanRepository planRepository;

    public PlanQueryService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    /**
     * Return a {@link List} of {@link Plan} which matches the criteria from the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<Plan> findByCriteria(PlanCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specifications<Plan> specification = createSpecification(criteria);
        return planRepository.findAll(specification);
    }

    /**
     * Return a {@link Page} of {@link Plan} which matches the criteria from the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<Plan> findByCriteria(PlanCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specifications<Plan> specification = createSpecification(criteria);
        return planRepository.findAll(specification, page);
    }

    /**
     * Function to convert PlanCriteria to a {@link Specifications}
     */
    private Specifications<Plan> createSpecification(PlanCriteria criteria) {
        Specifications<Plan> specification = Specifications.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildSpecification(criteria.getId(), Plan_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), Plan_.name));
            }
            if (criteria.getBasePrice() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getBasePrice(), Plan_.basePrice));
            }
            if (criteria.getDiscount() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getDiscount(), Plan_.discount));
            }
            if (criteria.getLicenseId() != null) {
                specification = specification.and(buildReferringEntitySpecification(criteria.getLicenseId(), Plan_.licenses, License_.id));
            }
        }
        return specification;
    }

}
