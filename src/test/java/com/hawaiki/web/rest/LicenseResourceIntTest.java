package com.hawaiki.web.rest;

import com.hawaiki.LicenseServiceApp;

import com.hawaiki.domain.License;
import com.hawaiki.domain.Plan;
import com.hawaiki.repository.LicenseRepository;
import com.hawaiki.service.LicenseService;
import com.hawaiki.web.rest.errors.ExceptionTranslator;
import com.hawaiki.service.dto.LicenseCriteria;
import com.hawaiki.service.LicenseQueryService;

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
 * Test class for the LicenseResource REST controller.
 *
 * @see LicenseResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LicenseServiceApp.class)
public class LicenseResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private LicenseService licenseService;

    @Autowired
    private LicenseQueryService licenseQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restLicenseMockMvc;

    private License license;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final LicenseResource licenseResource = new LicenseResource(licenseService, licenseQueryService);
        this.restLicenseMockMvc = MockMvcBuilders.standaloneSetup(licenseResource)
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
    public static License createEntity(EntityManager em) {
        License license = new License()
            .name(DEFAULT_NAME)
            .description(DEFAULT_DESCRIPTION);
        return license;
    }

    @Before
    public void initTest() {
        license = createEntity(em);
    }

    @Test
    @Transactional
    public void createLicense() throws Exception {
        int databaseSizeBeforeCreate = licenseRepository.findAll().size();

        // Create the License
        restLicenseMockMvc.perform(post("/api/licenses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(license)))
            .andExpect(status().isCreated());

        // Validate the License in the database
        List<License> licenseList = licenseRepository.findAll();
        assertThat(licenseList).hasSize(databaseSizeBeforeCreate + 1);
        License testLicense = licenseList.get(licenseList.size() - 1);
        assertThat(testLicense.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testLicense.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    public void createLicenseWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = licenseRepository.findAll().size();

        // Create the License with an existing ID
        license.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restLicenseMockMvc.perform(post("/api/licenses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(license)))
            .andExpect(status().isBadRequest());

        // Validate the License in the database
        List<License> licenseList = licenseRepository.findAll();
        assertThat(licenseList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllLicenses() throws Exception {
        // Initialize the database
        licenseRepository.saveAndFlush(license);

        // Get all the licenseList
        restLicenseMockMvc.perform(get("/api/licenses?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(license.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())));
    }

    @Test
    @Transactional
    public void getLicense() throws Exception {
        // Initialize the database
        licenseRepository.saveAndFlush(license);

        // Get the license
        restLicenseMockMvc.perform(get("/api/licenses/{id}", license.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(license.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()));
    }

    @Test
    @Transactional
    public void getAllLicensesByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        licenseRepository.saveAndFlush(license);

        // Get all the licenseList where name equals to DEFAULT_NAME
        defaultLicenseShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the licenseList where name equals to UPDATED_NAME
        defaultLicenseShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllLicensesByNameIsInShouldWork() throws Exception {
        // Initialize the database
        licenseRepository.saveAndFlush(license);

        // Get all the licenseList where name in DEFAULT_NAME or UPDATED_NAME
        defaultLicenseShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the licenseList where name equals to UPDATED_NAME
        defaultLicenseShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllLicensesByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        licenseRepository.saveAndFlush(license);

        // Get all the licenseList where name is not null
        defaultLicenseShouldBeFound("name.specified=true");

        // Get all the licenseList where name is null
        defaultLicenseShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    public void getAllLicensesByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        licenseRepository.saveAndFlush(license);

        // Get all the licenseList where description equals to DEFAULT_DESCRIPTION
        defaultLicenseShouldBeFound("description.equals=" + DEFAULT_DESCRIPTION);

        // Get all the licenseList where description equals to UPDATED_DESCRIPTION
        defaultLicenseShouldNotBeFound("description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllLicensesByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        licenseRepository.saveAndFlush(license);

        // Get all the licenseList where description in DEFAULT_DESCRIPTION or UPDATED_DESCRIPTION
        defaultLicenseShouldBeFound("description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION);

        // Get all the licenseList where description equals to UPDATED_DESCRIPTION
        defaultLicenseShouldNotBeFound("description.in=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllLicensesByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        licenseRepository.saveAndFlush(license);

        // Get all the licenseList where description is not null
        defaultLicenseShouldBeFound("description.specified=true");

        // Get all the licenseList where description is null
        defaultLicenseShouldNotBeFound("description.specified=false");
    }

    @Test
    @Transactional
    public void getAllLicensesByPlanIsEqualToSomething() throws Exception {
        // Initialize the database
        Plan plan = PlanResourceIntTest.createEntity(em);
        em.persist(plan);
        em.flush();
        license.setPlan(plan);
        licenseRepository.saveAndFlush(license);
        Long planId = plan.getId();

        // Get all the licenseList where plan equals to planId
        defaultLicenseShouldBeFound("planId.equals=" + planId);

        // Get all the licenseList where plan equals to planId + 1
        defaultLicenseShouldNotBeFound("planId.equals=" + (planId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultLicenseShouldBeFound(String filter) throws Exception {
        restLicenseMockMvc.perform(get("/api/licenses?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(license.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultLicenseShouldNotBeFound(String filter) throws Exception {
        restLicenseMockMvc.perform(get("/api/licenses?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }


    @Test
    @Transactional
    public void getNonExistingLicense() throws Exception {
        // Get the license
        restLicenseMockMvc.perform(get("/api/licenses/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateLicense() throws Exception {
        // Initialize the database
        licenseService.save(license);

        int databaseSizeBeforeUpdate = licenseRepository.findAll().size();

        // Update the license
        License updatedLicense = licenseRepository.findOne(license.getId());
        // Disconnect from session so that the updates on updatedLicense are not directly saved in db
        em.detach(updatedLicense);
        updatedLicense
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION);

        restLicenseMockMvc.perform(put("/api/licenses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedLicense)))
            .andExpect(status().isOk());

        // Validate the License in the database
        List<License> licenseList = licenseRepository.findAll();
        assertThat(licenseList).hasSize(databaseSizeBeforeUpdate);
        License testLicense = licenseList.get(licenseList.size() - 1);
        assertThat(testLicense.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testLicense.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void updateNonExistingLicense() throws Exception {
        int databaseSizeBeforeUpdate = licenseRepository.findAll().size();

        // Create the License

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restLicenseMockMvc.perform(put("/api/licenses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(license)))
            .andExpect(status().isCreated());

        // Validate the License in the database
        List<License> licenseList = licenseRepository.findAll();
        assertThat(licenseList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteLicense() throws Exception {
        // Initialize the database
        licenseService.save(license);

        int databaseSizeBeforeDelete = licenseRepository.findAll().size();

        // Get the license
        restLicenseMockMvc.perform(delete("/api/licenses/{id}", license.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<License> licenseList = licenseRepository.findAll();
        assertThat(licenseList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(License.class);
        License license1 = new License();
        license1.setId(1L);
        License license2 = new License();
        license2.setId(license1.getId());
        assertThat(license1).isEqualTo(license2);
        license2.setId(2L);
        assertThat(license1).isNotEqualTo(license2);
        license1.setId(null);
        assertThat(license1).isNotEqualTo(license2);
    }
}
