package com.hawaiki.service.dto;

import java.io.Serializable;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;






/**
 * Criteria class for the Plan entity. This class is used in PlanResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /plans?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class PlanCriteria implements Serializable {
    private static final long serialVersionUID = 1L;


    private LongFilter id;

    private StringFilter name;

    private DoubleFilter basePrice;

    private IntegerFilter discount;

    private LongFilter licenseId;

    public PlanCriteria() {
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getName() {
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public DoubleFilter getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(DoubleFilter basePrice) {
        this.basePrice = basePrice;
    }

    public IntegerFilter getDiscount() {
        return discount;
    }

    public void setDiscount(IntegerFilter discount) {
        this.discount = discount;
    }

    public LongFilter getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(LongFilter licenseId) {
        this.licenseId = licenseId;
    }

    @Override
    public String toString() {
        return "PlanCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (name != null ? "name=" + name + ", " : "") +
                (basePrice != null ? "basePrice=" + basePrice + ", " : "") +
                (discount != null ? "discount=" + discount + ", " : "") +
                (licenseId != null ? "licenseId=" + licenseId + ", " : "") +
            "}";
    }

}
