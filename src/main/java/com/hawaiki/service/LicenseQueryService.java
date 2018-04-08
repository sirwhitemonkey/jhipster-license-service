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

import com.hawaiki.domain.License;
import com.hawaiki.domain.*; // for static metamodels
import com.hawaiki.repository.LicenseRepository;
import com.hawaiki.service.dto.LicenseCriteria;


/**
 * Service for executing complex queries for License entities in the database.
 * The main input is a {@link LicenseCriteria} which get's converted to {@link Specifications},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link License} or a {@link Page} of {@link License} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class LicenseQueryService extends QueryService<License> {

    private final Logger log = LoggerFactory.getLogger(LicenseQueryService.class);


    private final LicenseRepository licenseRepository;

    public LicenseQueryService(LicenseRepository licenseRepository) {
        this.licenseRepository = licenseRepository;
    }

    /**
     * Return a {@link List} of {@link License} which matches the criteria from the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<License> findByCriteria(LicenseCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specifications<License> specification = createSpecification(criteria);
        return licenseRepository.findAll(specification);
    }

    /**
     * Return a {@link Page} of {@link License} which matches the criteria from the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<License> findByCriteria(LicenseCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specifications<License> specification = createSpecification(criteria);
        return licenseRepository.findAll(specification, page);
    }

    /**
     * Function to convert LicenseCriteria to a {@link Specifications}
     */
    private Specifications<License> createSpecification(LicenseCriteria criteria) {
        Specifications<License> specification = Specifications.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildSpecification(criteria.getId(), License_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), License_.name));
            }
            if (criteria.getDescription() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDescription(), License_.description));
            }
            if (criteria.getPlanId() != null) {
                specification = specification.and(buildReferringEntitySpecification(criteria.getPlanId(), License_.plan, Plan_.id));
            }
        }
        return specification;
    }

}
