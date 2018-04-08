package com.hawaiki.web.rest;

import com.hawaiki.LicenseServiceApp;

import com.hawaiki.domain.Plan;
import com.hawaiki.domain.License;
import com.hawaiki.repository.PlanRepository;
import com.hawaiki.service.PlanService;
import com.hawaiki.web.rest.errors.ExceptionTranslator;
import com.hawaiki.service.dto.PlanCriteria;
import com.hawaiki.service.PlanQueryService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static com.hawaiki.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the PlanResource REST controller.
 *
 * @see PlanResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LicenseServiceApp.class)
public class PlanResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Double DEFAULT_BASE_PRICE = 1D;
    private static final Double UPDATED_BASE_PRICE = 2D;

    private static final Integer DEFAULT_DISCOUNT = 1;
    private static final Integer UPDATED_DISCOUNT = 2;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private PlanService planService;

    @Autowired
    private PlanQueryService planQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restPlanMockMvc;

    private Plan plan;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final PlanResource planResource = new PlanResource(planService, planQueryService);
        this.restPlanMockMvc = MockMvcBuilders.standaloneSetup(planResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Plan createEntity(EntityManager em) {
        Plan plan = new Plan()
            .name(DEFAULT_NAME)
            .basePrice(DEFAULT_BASE_PRICE)
            .discount(DEFAULT_DISCOUNT);
        return plan;
    }

    @Before
    public void initTest() {
        plan = createEntity(em);
    }

    @Test
    @Transactional
    public void createPlan() throws Exception {
        int databaseSizeBeforeCreate = planRepository.findAll().size();

        // Create the Plan
        restPlanMockMvc.perform(post("/api/plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(plan)))
            .andExpect(status().isCreated());

        // Validate the Plan in the database
        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeCreate + 1);
        Plan testPlan = planList.get(planList.size() - 1);
        assertThat(testPlan.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testPlan.getBasePrice()).isEqualTo(DEFAULT_BASE_PRICE);
        assertThat(testPlan.getDiscount()).isEqualTo(DEFAULT_DISCOUNT);
    }

    @Test
    @Transactional
    public void createPlanWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = planRepository.findAll().size();

        // Create the Plan with an existing ID
        plan.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPlanMockMvc.perform(post("/api/plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(plan)))
            .andExpect(status().isBadRequest());

        // Validate the Plan in the database
        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllPlans() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList
        restPlanMockMvc.perform(get("/api/plans?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(plan.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].basePrice").value(hasItem(DEFAULT_BASE_PRICE.doubleValue())))
            .andExpect(jsonPath("$.[*].discount").value(hasItem(DEFAULT_DISCOUNT)));
    }

    @Test
    @Transactional
    public void getPlan() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get the plan
        restPlanMockMvc.perform(get("/api/plans/{id}", plan.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(plan.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.basePrice").value(DEFAULT_BASE_PRICE.doubleValue()))
            .andExpect(jsonPath("$.discount").value(DEFAULT_DISCOUNT));
    }

    @Test
    @Transactional
    public void getAllPlansByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where name equals to DEFAULT_NAME
        defaultPlanShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the planList where name equals to UPDATED_NAME
        defaultPlanShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllPlansByNameIsInShouldWork() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where name in DEFAULT_NAME or UPDATED_NAME
        defaultPlanShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the planList where name equals to UPDATED_NAME
        defaultPlanShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllPlansByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where name is not null
        defaultPlanShouldBeFound("name.specified=true");

        // Get all the planList where name is null
        defaultPlanShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    public void getAllPlansByBasePriceIsEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where basePrice equals to DEFAULT_BASE_PRICE
        defaultPlanShouldBeFound("basePrice.equals=" + DEFAULT_BASE_PRICE);

        // Get all the planList where basePrice equals to UPDATED_BASE_PRICE
        defaultPlanShouldNotBeFound("basePrice.equals=" + UPDATED_BASE_PRICE);
    }

    @Test
    @Transactional
    public void getAllPlansByBasePriceIsInShouldWork() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where basePrice in DEFAULT_BASE_PRICE or UPDATED_BASE_PRICE
        defaultPlanShouldBeFound("basePrice.in=" + DEFAULT_BASE_PRICE + "," + UPDATED_BASE_PRICE);

        // Get all the planList where basePrice equals to UPDATED_BASE_PRICE
        defaultPlanShouldNotBeFound("basePrice.in=" + UPDATED_BASE_PRICE);
    }

    @Test
    @Transactional
    public void getAllPlansByBasePriceIsNullOrNotNull() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where basePrice is not null
        defaultPlanShouldBeFound("basePrice.specified=true");

        // Get all the planList where basePrice is null
        defaultPlanShouldNotBeFound("basePrice.specified=false");
    }

    @Test
    @Transactional
    public void getAllPlansByDiscountIsEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where discount equals to DEFAULT_DISCOUNT
        defaultPlanShouldBeFound("discount.equals=" + DEFAULT_DISCOUNT);

        // Get all the planList where discount equals to UPDATED_DISCOUNT
        defaultPlanShouldNotBeFound("discount.equals=" + UPDATED_DISCOUNT);
    }

    @Test
    @Transactional
    public void getAllPlansByDiscountIsInShouldWork() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where discount in DEFAULT_DISCOUNT or UPDATED_DISCOUNT
        defaultPlanShouldBeFound("discount.in=" + DEFAULT_DISCOUNT + "," + UPDATED_DISCOUNT);

        // Get all the planList where discount equals to UPDATED_DISCOUNT
        defaultPlanShouldNotBeFound("discount.in=" + UPDATED_DISCOUNT);
    }

    @Test
    @Transactional
    public void getAllPlansByDiscountIsNullOrNotNull() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where discount is not null
        defaultPlanShouldBeFound("discount.specified=true");

        // Get all the planList where discount is null
        defaultPlanShouldNotBeFound("discount.specified=false");
    }

    @Test
    @Transactional
    public void getAllPlansByDiscountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where discount greater than or equals to DEFAULT_DISCOUNT
        defaultPlanShouldBeFound("discount.greaterOrEqualThan=" + DEFAULT_DISCOUNT);

        // Get all the planList where discount greater than or equals to UPDATED_DISCOUNT
        defaultPlanShouldNotBeFound("discount.greaterOrEqualThan=" + UPDATED_DISCOUNT);
    }

    @Test
    @Transactional
    public void getAllPlansByDiscountIsLessThanSomething() throws Exception {
        // Initialize the database
        planRepository.saveAndFlush(plan);

        // Get all the planList where discount less than or equals to DEFAULT_DISCOUNT
        defaultPlanShouldNotBeFound("discount.lessThan=" + DEFAULT_DISCOUNT);

        // Get all the planList where discount less than or equals to UPDATED_DISCOUNT
        defaultPlanShouldBeFound("discount.lessThan=" + UPDATED_DISCOUNT);
    }


    @Test
    @Transactional
    public void getAllPlansByLicenseIsEqualToSomething() throws Exception {
        // Initialize the database
        License license = LicenseResourceIntTest.createEntity(em);
        em.persist(license);
        em.flush();
        plan.addLicense(license);
        planRepository.saveAndFlush(plan);
        Long licenseId = license.getId();

        // Get all the planList where license equals to licenseId
        defaultPlanShouldBeFound("licenseId.equals=" + licenseId);

        // Get all the planList where license equals to licenseId + 1
        defaultPlanShouldNotBeFound("licenseId.equals=" + (licenseId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultPlanShouldBeFound(String filter) throws Exception {
        restPlanMockMvc.perform(get("/api/plans?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(plan.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].basePrice").value(hasItem(DEFAULT_BASE_PRICE.doubleValue())))
            .andExpect(jsonPath("$.[*].discount").value(hasItem(DEFAULT_DISCOUNT)));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultPlanShouldNotBeFound(String filter) throws Exception {
        restPlanMockMvc.perform(get("/api/plans?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }


    @Test
    @Transactional
    public void getNonExistingPlan() throws Exception {
        // Get the plan
        restPlanMockMvc.perform(get("/api/plans/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePlan() throws Exception {
        // Initialize the database
        planService.save(plan);

        int databaseSizeBeforeUpdate = planRepository.findAll().size();

        // Update the plan
        Plan updatedPlan = planRepository.findOne(plan.getId());
        // Disconnect from session so that the updates on updatedPlan are not directly saved in db
        em.detach(updatedPlan);
        updatedPlan
            .name(UPDATED_NAME)
            .basePrice(UPDATED_BASE_PRICE)
            .discount(UPDATED_DISCOUNT);

        restPlanMockMvc.perform(put("/api/plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedPlan)))
            .andExpect(status().isOk());

        // Validate the Plan in the database
        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeUpdate);
        Plan testPlan = planList.get(planList.size() - 1);
        assertThat(testPlan.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testPlan.getBasePrice()).isEqualTo(UPDATED_BASE_PRICE);
        assertThat(testPlan.getDiscount()).isEqualTo(UPDATED_DISCOUNT);
    }

    @Test
    @Transactional
    public void updateNonExistingPlan() throws Exception {
        int databaseSizeBeforeUpdate = planRepository.findAll().size();

        // Create the Plan

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restPlanMockMvc.perform(put("/api/plans")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(plan)))
            .andExpect(status().isCreated());

        // Validate the Plan in the database
        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deletePlan() throws Exception {
        // Initialize the database
        planService.save(plan);

        int databaseSizeBeforeDelete = planRepository.findAll().size();

        // Get the plan
        restPlanMockMvc.perform(delete("/api/plans/{id}", plan.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Plan> planList = planRepository.findAll();
        assertThat(planList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Plan.class);
        Plan plan1 = new Plan();
        plan1.setId(1L);
        Plan plan2 = new Plan();
        plan2.setId(plan1.getId());
        assertThat(plan1).isEqualTo(plan2);
        plan2.setId(2L);
        assertThat(plan1).isNotEqualTo(plan2);
        plan1.setId(null);
        assertThat(plan1).isNotEqualTo(plan2);
    }
}
