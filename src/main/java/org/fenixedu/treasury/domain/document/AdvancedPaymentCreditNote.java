package org.fenixedu.treasury.domain.document;

import java.math.BigDecimal;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.Vat;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.settings.TreasurySettings;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;

public class AdvancedPaymentCreditNote extends AdvancedPaymentCreditNote_Base {

    protected AdvancedPaymentCreditNote(final DebtAccount debtAccount, final DocumentNumberSeries documentNumberSeries,
            final DateTime documentDate) {
        super();

        init(debtAccount, documentNumberSeries, documentDate);
        checkRules();
    }

    @Override
    protected void init(DebtAccount debtAccount, DocumentNumberSeries documentNumberSeries, DateTime documentDate) {
        super.init(debtAccount, documentNumberSeries, documentDate);

    }

    protected AdvancedPaymentCreditNote(final DebtAccount debtAccount, final DebtAccount payorDebtAccount,
            final DocumentNumberSeries documentNumberSeries, final DateTime documentDate) {
        super();

        init(debtAccount, payorDebtAccount, documentNumberSeries, documentDate);
        checkRules();
    }

    @Override
    public boolean isCreditNote() {
        return true;
    }

    @Override
    protected void checkRules() {
        if (!getDocumentNumberSeries().getFinantialDocumentType().getType().equals(FinantialDocumentTypeEnum.CREDIT_NOTE)) {
            throw new TreasuryDomainException("error.AdvancedPaymentCreditNote.finantialDocumentType.invalid");
        }

        if (getAdvancedPaymentSettlementNote() != null
                && !getAdvancedPaymentSettlementNote().getDebtAccount().equals(getDebtAccount())) {
            throw new TreasuryDomainException("error.AdvancedPaymentCreditNote.invalid.debtaccount.with.settlementnote");
        }
        super.checkRules();
    }

    @Override
    public boolean isDeletable() {
        return true;
    }

    @Override
    @Atomic
    public void delete(boolean deleteEntries) {
        if (!isDeletable()) {
            throw new TreasuryDomainException("error.CreditNote.cannot.delete");
        }

        setDebitNote(null);
        super.delete(deleteEntries);
    }

    // @formatter: off
    /************
     * SERVICES *
     ************/
    // @formatter: on

    @Atomic
    public static AdvancedPaymentCreditNote create(final DebtAccount debtAccount,
            final DocumentNumberSeries documentNumberSeries, final DateTime documentDate) {
        AdvancedPaymentCreditNote note = new AdvancedPaymentCreditNote(debtAccount, documentNumberSeries, documentDate);
        note.checkRules();
        return note;
    }

    @Atomic
    public static AdvancedPaymentCreditNote createCreditNoteForAdvancedPayment(DocumentNumberSeries documentNumberSeries,
            DebtAccount debtAccount, BigDecimal availableAmount, DateTime documentDate, String description) {
        AdvancedPaymentCreditNote note = create(debtAccount, documentNumberSeries, documentDate);

        Product advancedPaymentProduct = TreasurySettings.getInstance().getAdvancePaymentProduct();
        if (advancedPaymentProduct == null) {
            throw new TreasuryDomainException("error.AdvancedPaymentCreditNote.invalid.product.for.advanced.payment");
        }
        Vat vat =
                Vat.findActiveUnique(advancedPaymentProduct.getVatType(), debtAccount.getFinantialInstitution(), new DateTime())
                        .orElse(null);
        if (vat == null) {
            throw new TreasuryDomainException("error.AdvancedPaymentCreditNote.invalid.vat.type.for.advanced.payment");
        }
        String lineDescription =
                BundleUtil.getString(Constants.BUNDLE, "label.AdvancedPaymentCreditNote.advanced.payment.description")
                        + description;
        CreditEntry entry =
                CreditEntry.create(note, lineDescription, advancedPaymentProduct, vat, availableAmount, documentDate, null,
                        BigDecimal.ONE);
        return note;
    }

}
