package org.fenixedu.treasury.services.payments.sibs;

import java.math.BigDecimal;

import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.paymentcodes.PaymentReferenceCode;
import org.fenixedu.treasury.domain.paymentcodes.SibsReportFile;
import org.fenixedu.treasury.services.payments.sibs.incomming.SibsIncommingPaymentFileDetailLine;
import org.joda.time.DateTime;

public class SIBSImportationLineDTO {

    protected SibsIncommingPaymentFileDetailLine line;
    protected PaymentReferenceCode paymentCode;
    protected SibsReportFile report;
    private SIBSImportationFileDTO sibsImportationFileDTO;

    public SIBSImportationLineDTO(final SIBSImportationFileDTO sibsImportationFileDTO,
            final SibsIncommingPaymentFileDetailLine line) {
        this.line = line;
        this.paymentCode = PaymentReferenceCode.readByCode(line.getCode(), sibsImportationFileDTO.getFinantialInstitution());
        if (paymentCode != null) {
            this.report = paymentCode.getReportOnDate(getTransactionWhenRegistered());
        }
        this.setSibsImportationFileDTO(sibsImportationFileDTO);
    }

    public DateTime getWhenProcessedBySibs() {
        return getSibsImportationFileDTO().getWhenProcessedBySibs();
    }

    public String getFilename() {
        return getSibsImportationFileDTO().getFilename();
    }

    public BigDecimal getTransactionsTotalAmount() {
        return getSibsImportationFileDTO().getTransactionsTotalAmount();
    }

    public BigDecimal getTotalCost() {
        return getSibsImportationFileDTO().getTotalCost();
    }

    public Integer getFileVersion() {
        return getSibsImportationFileDTO().getFileVersion();
    }

    public String getSibsTransactionId() {
        return line.getSibsTransactionId();
    }

    public BigDecimal getTransactionTotalAmount() {
        return line.getAmount();
    }

    public DateTime getTransactionWhenRegistered() {
        return line.getWhenOccuredTransaction();
    }

    public String getCode() {
        return line.getCode();
    }

    public PaymentReferenceCode getPaymentCode() {
        return paymentCode;
    }

    public boolean hasPaymentCode() {
        return getPaymentCode() != null;
    }

    protected SibsReportFile getReport() {
        return report;
    }

    public Integer getNumberOfTransactions() {
        if (!hasPaymentCode()) {
            return 0;
        }

        if (getReport() == null) {
            return 0;
        }
        return getReport().getNumberOfTransactions();
    }

    public String getTransactionDescription(final Integer index) {
        if (getReport() != null) {
            return getReport().getTransactionDescription(index);
        }
        return "";
    }

    public BigDecimal getTransactionAmount(final Integer index) {
        if (getReport() != null) {
            return getReport().getTransactionAmount(index);
        }
        return BigDecimal.ZERO;
    }

    public String getPersonName() {
        if (!hasPaymentCode()) {
            return null;
        }

        return getPaymentCode().getTargetPayment().getTargetPayorDescription();
    }

    public String getStudentNumber() {
        if (!hasPaymentCode()) {
            return null;
        }

        return getPaymentCode().getTargetPayment().getTargetPayorDescription();
    }

    public String getDescription() {
        if (!hasPaymentCode()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        if (getPaymentCode().getTargetPayment().getSettlementNotesSet() != null) {
            SettlementNote note =
                    getPaymentCode().getTargetPayment().getSettlementNotesSet().stream()
                            .filter(x -> x.getDocumentDate().equals(this.getTransactionWhenRegistered())).findFirst()
                            .orElse(null);
            if (note != null) {
                sb.append("Nota de Pagamento: " + note.getUiDocumentNumber() + "\n");
            }
        }
        sb.append(getPaymentCode().getTargetPayment().getDescription());
        return sb.toString();
    }

    public SIBSImportationFileDTO getSibsImportationFileDTO() {
        return sibsImportationFileDTO;
    }

    public void setSibsImportationFileDTO(SIBSImportationFileDTO sibsImportationFileDTO) {
        this.sibsImportationFileDTO = sibsImportationFileDTO;
    }

}
