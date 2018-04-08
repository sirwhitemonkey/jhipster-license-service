package com.hawaiki.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.hawaiki.domain.Plan;
import com.hawaiki.service.PlanService;
import com.hawaiki.web.rest.errors.BadRequestAlertException;
import com.hawaiki.web.rest.util.HeaderUtil;
import com.hawaiki.web.rest.util.PaginationUtil;
import com.hawaiki.service.dto.PlanCriteria;
import com.hawaiki.service.PlanQueryService;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Plan.
 */
@RestController
@RequestMapping("/api")
public class PlanResource {

    private final Logger log = LoggerFactory.getLogger(PlanResource.class);

    private static final String ENTITY_NAME = "plan";

    private final PlanService planService;

    private final PlanQueryService planQueryService;

    public PlanResource(PlanService planService, PlanQueryService planQueryService) {
        this.planService = planService;
        this.planQueryService = planQueryService;
    }

    /**
     * POST  /plans : Create a new plan.
     *
     * @param plan the plan to create
     * @return the ResponseEntity with status 201 (Created) and with body the new plan, or with status 400 (Bad Request) if the plan has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/plans")
    @Timed
    public ResponseEntity<Plan> createPlan(@RequestBody Plan plan) throws URISyntaxException {
        log.debug("REST request to save Plan : {}", plan);
        if (plan.getId() != null) {
            throw new BadRequestAlertException("A new plan cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Plan result = planService.save(plan);
        return ResponseEntity.created(new URI("/api/plans/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /plans : Updates an existing plan.
     *
     * @param plan the plan to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated plan,
     * or with status 400 (Bad Request) if the plan is not valid,
     * or with status 500 (Internal Server Error) if the plan couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/plans")
    @Timed
    public ResponseEntity<Plan> updatePlan(@RequestBody Plan plan) throws URISyntaxException {
        log.debug("REST request to update Plan : {}", plan);
        if (plan.getId() == null) {
            return createPlan(plan);
        }
        Plan result = planService.save(plan);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, plan.getId().toString()))
            .body(result);
    }

    /**
     * GET  /plans : get all the plans.
     *
     * @param pageable the pagination information
     * @param criteria the criterias which the requested entities should match
     * @return the ResponseEntity with status 200 (OK) and the list of plans in body
     */
    @GetMapping("/plans")
    @Timed
    public ResponseEntity<List<Plan>> getAllPlans(PlanCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Plans by criteria: {}", criteria);
        Page<Plan> page = planQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/plans");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /plans/:id : get the "id" plan.
     *
     * @param id the id of the plan to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the plan, or with status 404 (Not Found)
     */
    @GetMapping("/plans/{id}")
    @Timed
    public ResponseEntity<Plan> getPlan(@PathVariable Long id) {
        log.debug("REST request to get Plan : {}", id);
        Plan plan = planService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(plan));
    }

    /**
     * DELETE  /plans/:id : delete the "id" plan.
     *
     * @param id the id of the plan to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/plans/{id}")
    @Timed
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        log.debug("REST request to delete Plan : {}", id);
        planService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
