/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: ricardo.pedro@qub-it.com, anil.mamede@qub-it.com
 * 
 *
 * 
 * This file is part of FenixEdu Treasury.
 *
 * FenixEdu Treasury is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Treasury is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Treasury.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.treasury.domain.document;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.scheduler.TaskRunner;
import org.fenixedu.bennu.scheduler.domain.SchedulerSystem;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;
import org.fenixedu.treasury.domain.integration.ERPImportOperation;
import org.fenixedu.treasury.services.integration.erp.tasks.ERPExportPendingDocumentsTask;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Ordering;

public abstract class FinantialDocument extends FinantialDocument_Base {

    protected static final Comparator<FinantialDocument> COMPARE_BY_DOCUMENT_DATE = new Comparator<FinantialDocument>() {

        @Override
        public int compare(final FinantialDocument o1, final FinantialDocument o2) {
            int c = o1.getDocumentDate().compareTo(o2.getDocumentDate());

            return c != 0 ? c : o1.getExternalId().compareTo(o2.getExternalId());
        }
    };

    protected static final Comparator<String> COMPARE_BY_DOCUMENT_NUMBER_STRING = new Comparator<String>() {

        @Override
        public int compare(final String o1, final String o2) {
            return Ordering.<String> natural().compare(o1, o2);
        }
    };

    protected static final Comparator<FinantialDocument> COMPARE_BY_DOCUMENT_NUMBER = new Comparator<FinantialDocument>() {

        @Override
        public int compare(FinantialDocument o1, FinantialDocument o2) {
            int c = Ordering.<String> natural().compare(o1.getDocumentNumber(), o2.getDocumentNumber());

            return c != 0 ? c : o1.getExternalId().compareTo(o2.getExternalId());
        }

    };

    protected FinantialDocument() {
        super();
        setBennu(Bennu.getInstance());
        setState(FinantialDocumentStateType.PREPARING);
    }

    protected void init(final DebtAccount debtAccount, final DocumentNumberSeries documentNumberSeries,
            final DateTime documentDate) {

        setDebtAccount(debtAccount);
        setFinantialDocumentType(documentNumberSeries.getFinantialDocumentType());
        setDocumentNumberSeries(documentNumberSeries);
        setDocumentNumber("000000000");
        setDocumentDate(documentDate);
        setDocumentDueDate(documentDate.toLocalDate());
        setCurrency(debtAccount.getFinantialInstitution().getCurrency());
        setState(FinantialDocumentStateType.PREPARING);
        setAddress(debtAccount.getCustomer().getAddress());
        checkRules();
    }

    protected void checkRules() {

        if (getDebtAccount() == null) {
            throw new TreasuryDomainException("error.FinantialDocument.debtAccount.required");
        }

        if (getFinantialDocumentType() == null) {
            throw new TreasuryDomainException("error.FinantialDocument.finantialDocumentType.required");
        }

        if (getDocumentNumberSeries() == null) {
            throw new TreasuryDomainException("error.FinantialDocument.documentNumber.required");
        }

        if (getDocumentDate() == null) {
            throw new TreasuryDomainException("error.FinantialDocument.documentDate.required");
        }

        if (getDocumentDueDate() == null) {
            throw new TreasuryDomainException("error.FinantialDocument.documentDueDate.required");
        }

        if (getCurrency() == null) {
            throw new TreasuryDomainException("error.FinantialDocument.currency.required");
        }
        if (!getDocumentNumberSeries().getSeries().getFinantialInstitution().equals(getDebtAccount().getFinantialInstitution())) {
            throw new TreasuryDomainException("error.FinantialDocument.finantialinstitution.mismatch");
        }

        if (getDocumentNumberSeries().getSeries().getLegacy() == false) {
            if (getDocumentDueDate().isBefore(getDocumentDate().toLocalDate())) {
                throw new TreasuryDomainException("error.FinantialDocument.documentDueDate.invalid");
            }
        }

        if (isClosed() && isDocumentEmpty()) {
            throw new TreasuryDomainException("error.FinantialDocument.closed.but.empty.entries");
        }

        if (isClosed() && getDocumentNumberSeries().getSeries().getCertificated()) {
            final Stream<? extends FinantialDocument> stream =
                    findClosedUntilDocumentNumberExclusive(getDocumentNumberSeries(), getDocumentNumber());

            final FinantialDocument previousFinantialDocument =
                    stream.sorted(COMPARE_BY_DOCUMENT_NUMBER).findFirst().orElse(null);

            if (previousFinantialDocument != null
                    && !(previousFinantialDocument.getDocumentDate().toLocalDate().compareTo(getDocumentDate().toLocalDate()) <= 0)) {
                throw new TreasuryDomainException("error.FinantialDocument.documentDate.is.not.after.than.previous.document");
            }
        }
        //If document is closed, all entries must be after of DocumentDate - RSP, this rule is invalid. I can create a debit entry today, and add it to a Document in a month
//        if (isClosed()) {
//            LocalDate documentDate = this.getDocumentDate().toLocalDate();
//            if (getFinantialDocumentEntriesSet().stream()
//                    .anyMatch(x -> x.getEntryDateTime().toLocalDate().isBefore(documentDate))) {
//                throw new TreasuryDomainException("error.FinantialDocument.documentDate.is.after.entries.date");
//            }
//        }
    }

    protected boolean isDocumentEmpty() {
        return this.getFinantialDocumentEntriesSet().isEmpty();
    }

    public String getUiDocumentNumber() {
        return String.format("%s %s/%s", this.getDocumentNumberSeries().getFinantialDocumentType()
                .getDocumentNumberSeriesPrefix(), this.getDocumentNumberSeries().getSeries().getCode(),
                Strings.padStart(this.getDocumentNumber(), 7, '0'));
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = BigDecimal.ZERO;
        for (FinantialDocumentEntry entry : this.getFinantialDocumentEntriesSet()) {
            amount = amount.add(entry.getTotalAmount());
        }

        return getDebtAccount().getFinantialInstitution().getCurrency().getValueWithScale(amount);
    }

    public String getUiTotalAmount() {
        return this.getDebtAccount().getFinantialInstitution().getCurrency().getValueFor(this.getTotalAmount());
    }

    public BigDecimal getTotalNetAmount() {
        BigDecimal amount = BigDecimal.ZERO;
        for (FinantialDocumentEntry entry : this.getFinantialDocumentEntriesSet()) {
            amount = amount.add(entry.getNetAmount());
        }

        return getDebtAccount().getFinantialInstitution().getCurrency().getValueWithScale(amount);
    }

    public String getUiTotalNetAmount() {
        return this.getDebtAccount().getFinantialInstitution().getCurrency().getValueFor(this.getTotalNetAmount());
    }

    public boolean isClosed() {
        return this.getState().isClosed();
    }

    public boolean isInvoice() {
        return false;
    }

    public boolean isDebitNote() {
        return false;
    }

    public boolean isCreditNote() {
        return false;
    }

    public boolean isSettlementNote() {
        return false;
    }

    public boolean isDeletable() {
        return this.isPreparing() && getPaymentCodesSet().isEmpty();
    }

    public boolean isAnnulled() {
        return this.getState().equals(FinantialDocumentStateType.ANNULED);
    }

    public boolean isPreparing() {
        return this.getState().equals(FinantialDocumentStateType.PREPARING);
    }

    @Atomic
    public void closeDocument() {
        if (this.isPreparing()) {
            this.setDocumentNumber("" + this.getDocumentNumberSeries().getSequenceNumberAndIncrement());
            setState(FinantialDocumentStateType.CLOSED);
            int order = 1;
            for (FinantialDocumentEntry entry : getFinantialDocumentEntriesSet()) {
                entry.setEntryOrder(Integer.valueOf(order));
                order = order + 1;
            }
            this.setAddress(this.getDebtAccount().getCustomer().getAddress() + this.getDebtAccount().getCustomer().getZipCode());
            this.markDocumentToExport();
        } else {
            throw new TreasuryDomainException(BundleUtil.getString(Constants.BUNDLE,
                    "error.FinantialDocumentState.invalid.state.change.request"));

        }
        checkRules();
    }

    @Atomic
    public void markDocumentToExport() {
        //if the document is in the sames series of the IntegrationConfiguration.paymentSeries -> IGNORE
        if (this.getDebtAccount().getFinantialInstitution().getErpIntegrationConfiguration() != null) {
            if (this.getDocumentNumberSeries().getSeries() == this.getDebtAccount().getFinantialInstitution()
                    .getErpIntegrationConfiguration().getPaymentsIntegrationSeries()) {
                return;
            }
        }

        if (getInstitutionForExportation() == null) {
            this.setInstitutionForExportation(this.getDocumentNumberSeries().getSeries().getFinantialInstitution());
        }

        SchedulerSystem.queue(new TaskRunner(new ERPExportPendingDocumentsTask()));
    }

    @Atomic
    public void clearDocumentToExport() {
        if (getInstitutionForExportation() != null) {
            this.setInstitutionForExportation(null);
        }
    }

    public boolean isDocumentToExport() {
        return getInstitutionForExportation() != null;
    }

    @Atomic
    public void anullDocument(boolean freeEntries, String anulledReason) {
        if (this.isPreparing() || this.isClosed()) {
            if (Boolean.TRUE.booleanValue() == this.getDocumentNumberSeries().getSeries().getCertificated()) {
                throw new TreasuryDomainException("error.FinantialDocument.certificatedseris.cannot.anulled");
            }
            setState(FinantialDocumentStateType.ANNULED);
            setAnnulledReason(anulledReason);
            //If we want to free entries and the document is in "Preparing" state, the Entries will become "free"
            if (freeEntries && this.isPreparing()) {
                this.getFinantialDocumentEntriesSet().forEach(x -> this.removeFinantialDocumentEntries(x));
            }
            this.markDocumentToExport();
        }
        checkRules();
    }

    @Atomic
    public void delete(boolean deleteEntries) {
        if (!isDeletable()) {
            throw new TreasuryDomainException("error.FinantialDocument.cannot.delete");
        }

        setBennu(null);
        setDocumentNumberSeries(null);
        setCurrency(null);
        setDebtAccount(null);
        setFinantialDocumentType(null);
        setInstitutionForExportation(null);
        for (FinantialDocumentEntry entry : getFinantialDocumentEntriesSet()) {
            this.removeFinantialDocumentEntries(entry);
            if (deleteEntries) {
                entry.delete();
            } else {
                entry.setFinantialDocument(null);
            }
        }

        for (ERPExportOperation oper : getErpExportOperationsSet()) {
            this.removeErpExportOperations(oper);
            oper.delete();
        }

        for (ERPImportOperation oper : getErpImportOperationsSet()) {
            this.removeErpImportOperations(oper);
            oper.delete();
        }
        deleteDomainObject();
    }

    public abstract Set<FinantialDocument> findRelatedDocuments(Set<FinantialDocument> documentsBaseList,
            Boolean includeAnulledDocuments);

    public static Stream<? extends FinantialDocument> findAll() {
        return Bennu.getInstance().getFinantialDocumentsSet().stream();
    }

    public static Stream<? extends FinantialDocument> find(final FinantialDocumentType finantialDocumentType) {
        return findAll().filter(i -> finantialDocumentType.equals(i.getFinantialDocumentType()));
    }

    public static Stream<? extends FinantialDocument> find(final DocumentNumberSeries documentNumberSeries) {
        return findAll().filter(x -> x.getDocumentNumberSeries() == documentNumberSeries);
    }

    public static Optional<? extends FinantialDocument> findUniqueByDocumentNumber(final String documentNumber) {
        return findAll().filter(x -> documentNumber.equals(x.getUiDocumentNumber())).findFirst();
    }

    protected static Stream<? extends FinantialDocument> findClosedUntilDocumentNumberExclusive(
            final DocumentNumberSeries documentNumberSeries, final String documentNumber) {
        return find(documentNumberSeries).filter(
                d -> d.isClosed() && COMPARE_BY_DOCUMENT_NUMBER_STRING.compare(d.getDocumentNumber(), documentNumber) < 0);
    }

    public Boolean getClosed() {
        return this.getState().equals(FinantialDocumentStateType.CLOSED);
    }

    public BigDecimal getOpenAmount() {
        if (this.getState().isPreparing() || this.getState().isClosed()) {
            return getTotalAmount();
        } else {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getOpenAmountWithInterests() {
        if (this.getState().isPreparing() || this.getState().isClosed()) {
            return getTotalAmount();
        } else {
            return BigDecimal.ZERO;
        }
    }

    @Atomic
    public void changeState(FinantialDocumentStateType newState, String reason) {
        //Same state, do nothing...
        if (newState == this.getState()) {
            return;
        }

        if (this.isPreparing()) {
            if (newState == FinantialDocumentStateType.ANNULED) {
                this.anullDocument(true, reason);
            }

            if (newState == FinantialDocumentStateType.CLOSED) {
                this.closeDocument();
            }
        } else if (this.isClosed() && newState == FinantialDocumentStateType.ANNULED) {
            this.anullDocument(false, reason);
        } else {
            throw new TreasuryDomainException(BundleUtil.getString(Constants.BUNDLE,
                    "error.FinantialDocumentState.invalid.state.change.request"));
        }

        checkRules();
    }

    public static FinantialDocument findByUiDocumentNumber(FinantialInstitution finantialInstitution, String docNumber) {
        //parse the Document Number in {DOCUMENT_TYPE} {DOCUMENT_SERIES}/{DOCUMENT_NUMBER}
        String documentType;
        String seriesNumber;
        String documentNumber;

        try {
            List<String> values = Splitter.on(' ').splitToList(docNumber);
            List<String> values2 = Splitter.on('/').splitToList(values.get(1));

            documentType = values.get(0);
            seriesNumber = values2.get(0);
//            documentNumber = values2.get(1);

            FinantialDocumentType type = FinantialDocumentType.findByCode(documentType);
            if (type != null) {
                Series series = Series.findByCode(finantialInstitution, seriesNumber);
                if (series != null) {
                    DocumentNumberSeries dns = DocumentNumberSeries.find(type, series);
                    if (dns != null) {
                        return dns.getFinantialDocumentsSet().stream().filter(x -> x.getUiDocumentNumber().equals(docNumber))
                                .findFirst().orElse(null);
                    }
                }
            }
        } catch (Exception ex) {

        }
        return null;
    }
}
