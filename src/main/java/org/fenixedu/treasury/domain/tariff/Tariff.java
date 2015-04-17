package org.fenixedu.treasury.domain.tariff;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.VatType;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;

public abstract class Tariff extends Tariff_Base {

    protected Tariff() {
        super();
        setBennu(Bennu.getInstance());
    }

    protected void init(final Product product, final VatType vatType, final DateTime beginDate, final DateTime endDate,
            final DueDateCalculationType dueDateCalculationType, final LocalDate fixedDueDate,
            final int numberOfDaysAfterCreationForDueDate, final boolean applyInterests, final InterestType interestType,
            final int numberOfDaysAfterDueDate, final boolean applyInFirstWorkday, final int maximumDaysToApplyPenalty,
            final int maximumMonthsToApplyPenalty, final BigDecimal interestFixedAmount, final BigDecimal rate) {
        setProduct(product);
        setVatType(vatType);
        setBeginDate(beginDate);
        setEndDate(endDate);
        setDueDateCalculationType(dueDateCalculationType);
        setFixedDueDate(fixedDueDate);
        setNumberOfDaysAfterCreationForDueDate(numberOfDaysAfterCreationForDueDate);
        setApplyInterests(applyInterests);
        
        if(getApplyInterests()) {
            InterestRate.create(this, interestType, numberOfDaysAfterCreationForDueDate, applyInFirstWorkday, maximumDaysToApplyPenalty, maximumMonthsToApplyPenalty, interestFixedAmount, rate);
        }
        
        checkRules();
    }

    private void checkRules() {
        if(getProduct() == null) {
            throw new TreasuryDomainException("error.Tariff.product.required");
        }
        
        if(getVatType() == null) {
            throw new TreasuryDomainException("error.Tariff.vatType.required");
        }
        
        if(getBeginDate() == null) {
            throw new TreasuryDomainException("error.Tariff.beginDate.required");
        }
        
        if(getEndDate() != null && !getEndDate().isAfter(getBeginDate())) {
            throw new TreasuryDomainException("error.Tariff.endDate.must.be.after.beginDate");
        }
        
        if(getDueDateCalculationType() == null) {
            throw new TreasuryDomainException("error.Tariff.dueDateCalculationType.required");
        }
        
        if(getDueDateCalculationType().isFixedDate() && getFixedDueDate() == null) {
            throw new TreasuryDomainException("error.Tariff.fixedDueDate.required");
        }
        
        if(getFixedDueDate().toDateTimeAtStartOfDay().plusDays(1).minusSeconds(1).isBefore(getBeginDate())) {
            throw new TreasuryDomainException("error.Tariff.fixedDueDate.must.be.after.or.equal.beginDate");
        }
        
        if(getDueDateCalculationType().isDaysAfterCreation() && getNumberOfDaysAfterCreationForDueDate() < 0) {
            throw new TreasuryDomainException("error.Tariff.numberOfDaysAfterCreationForDueDate.must.be.positive");
        }
        
        if(getApplyInterests() && getInterestRate() == null) {
            throw new TreasuryDomainException("error.Tariff.interestRate.required");
        }
    }
    
    public abstract BigDecimal getAmount();
    
    public boolean isActive(final DateTime when) {
        return new Interval(getBeginDate(), getEndDate()).contains(when);
    }
    
    public boolean isActive(final Interval dateInterval) {
        return new Interval(getBeginDate(), getEndDate()).overlaps(dateInterval);
    }

    @Atomic
    public void edit(final DateTime beginDate, final DateTime endDate) {
        checkRules();
    }

    public boolean isDeletable() {
        return true;
    }

    @Atomic
    public void delete() {
        if (!isDeletable()) {
            throw new TreasuryDomainException("error.Tariff.cannot.delete");
        }

        setBennu(null);
        deleteDomainObject();
    }

    // @formatter: off
    /************
     * SERVICES *
     ************/
    // @formatter: on

    public static Stream<Tariff> findAll() {
        return Bennu.getInstance().getTariffsSet().stream();
    }
    
    public static Stream<Tariff> find(final Product product) {
        return findAll().filter(t -> t.getProduct() == product);
    }
    
    public static Stream<Tariff> find(final Product product, final DateTime when) {
        return find(product).filter(t -> t.isActive(when));
    }
    
    public static Stream<Tariff> findInInterval(final Product product, final DateTime start, final DateTime end) {
        final Interval interval = new Interval(start, end);
        return find(product).filter(t -> t.isActive(interval));
    }
    
}
