package com.hawaiki.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.hawaiki.domain.License;
import com.hawaiki.service.LicenseService;
import com.hawaiki.web.rest.errors.BadRequestAlertException;
import com.hawaiki.web.rest.util.HeaderUtil;
import com.hawaiki.web.rest.util.PaginationUtil;
import com.hawaiki.service.dto.LicenseCriteria;
import com.hawaiki.service.LicenseQueryService;
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
 * REST controller for managing License.
 */
@RestController
@RequestMapping("/api")
public class LicenseResource {

    private final Logger log = LoggerFactory.getLogger(LicenseResource.class);

    private static final String ENTITY_NAME = "license";

    private final LicenseService licenseService;

    private final LicenseQueryService licenseQueryService;

    public LicenseResource(LicenseService licenseService, LicenseQueryService licenseQueryService) {
        this.licenseService = licenseService;
        this.licenseQueryService = licenseQueryService;
    }

    /**
     * POST  /licenses : Create a new license.
     *
     * @param license the license to create
     * @return the ResponseEntity with status 201 (Created) and with body the new license, or with status 400 (Bad Request) if the license has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/licenses")
    @Timed
    public ResponseEntity<License> createLicense(@RequestBody License license) throws URISyntaxException {
        log.debug("REST request to save License : {}", license);
        if (license.getId() != null) {
            throw new BadRequestAlertException("A new license cannot already have an ID", ENTITY_NAME, "idexists");
        }
        License result = licenseService.save(license);
        return ResponseEntity.created(new URI("/api/licenses/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /licenses : Updates an existing license.
     *
     * @param license the license to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated license,
     * or with status 400 (Bad Request) if the license is not valid,
     * or with status 500 (Internal Server Error) if the license couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/licenses")
    @Timed
    public ResponseEntity<License> updateLicense(@RequestBody License license) throws URISyntaxException {
        log.debug("REST request to update License : {}", license);
        if (license.getId() == null) {
            return createLicense(license);
        }
        License result = licenseService.save(license);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, license.getId().toString()))
            .body(result);
    }

    /**
     * GET  /licenses : get all the licenses.
     *
     * @param pageable the pagination information
     * @param criteria the criterias which the requested entities should match
     * @return the ResponseEntity with status 200 (OK) and the list of licenses in body
     */
    @GetMapping("/licenses")
    @Timed
    public ResponseEntity<List<License>> getAllLicenses(LicenseCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Licenses by criteria: {}", criteria);
        Page<License> page = licenseQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/licenses");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /licenses/:id : get the "id" license.
     *
     * @param id the id of the license to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the license, or with status 404 (Not Found)
     */
    @GetMapping("/licenses/{id}")
    @Timed
    public ResponseEntity<License> getLicense(@PathVariable Long id) {
        log.debug("REST request to get License : {}", id);
        License license = licenseService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(license));
    }

    /**
     * DELETE  /licenses/:id : delete the "id" license.
     *
     * @param id the id of the license to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/licenses/{id}")
    @Timed
    public ResponseEntity<Void> deleteLicense(@PathVariable Long id) {
        log.debug("REST request to delete License : {}", id);
        licenseService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
