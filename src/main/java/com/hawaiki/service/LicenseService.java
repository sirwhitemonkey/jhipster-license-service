package com.hawaiki.service;

import com.hawaiki.domain.License;
import com.hawaiki.repository.LicenseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing License.
 */
@Service
@Transactional
public class LicenseService {

    private final Logger log = LoggerFactory.getLogger(LicenseService.class);

    private final LicenseRepository licenseRepository;

    public LicenseService(LicenseRepository licenseRepository) {
        this.licenseRepository = licenseRepository;
    }

    /**
     * Save a license.
     *
     * @param license the entity to save
     * @return the persisted entity
     */
    public License save(License license) {
        log.debug("Request to save License : {}", license);
        return licenseRepository.save(license);
    }

    /**
     * Get all the licenses.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<License> findAll(Pageable pageable) {
        log.debug("Request to get all Licenses");
        return licenseRepository.findAll(pageable);
    }

    /**
     * Get one license by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public License findOne(Long id) {
        log.debug("Request to get License : {}", id);
        return licenseRepository.findOne(id);
    }

    /**
     * Delete the license by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete License : {}", id);
        licenseRepository.delete(id);
    }
}
