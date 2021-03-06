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
package org.fenixedu.treasury.domain.paymentcodes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.spreadsheet.SheetData;
import org.fenixedu.commons.spreadsheet.SpreadsheetBuilder;
import org.fenixedu.commons.spreadsheet.WorkbookExportFormat;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.services.payments.sibs.SIBSImportationFileDTO;
import org.fenixedu.treasury.services.payments.sibs.SIBSImportationLineDTO;
import org.fenixedu.treasury.services.payments.sibs.SIBSPaymentsImporter.ProcessResult;
import org.fenixedu.treasury.util.Constants;
//import pt.utl.ist.fenix.tools.spreadsheet.SheetData;
//import pt.utl.ist.fenix.tools.spreadsheet.SpreadsheetBuilder;
//import pt.utl.ist.fenix.tools.spreadsheet.WorkbookExportFormat
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;

public class SibsReportFile extends SibsReportFile_Base {

    public static final String CONTENT_TYPE = "text/plain";
    public static final String FILE_EXTENSION = ".idm";

    protected SibsReportFile() {
        super();
        setBennu(Bennu.getInstance());
    }

    protected SibsReportFile(final DateTime whenProcessedBySibs, final BigDecimal transactionsTotalAmount,
            final BigDecimal totalCost, final String displayName, final String fileName, final byte[] content) {
        this();
        this.init(whenProcessedBySibs, transactionsTotalAmount, totalCost, displayName, fileName, content);

        checkRules();
    }

    protected void init(final DateTime whenProcessedBySibs, final BigDecimal transactionsTotalAmount, final BigDecimal totalCost,
            final String displayName, final String fileName, final byte[] content) {

        super.init(displayName, fileName, content);
        setWhenProcessedBySibs(whenProcessedBySibs);
        setTransactionsTotalAmount(transactionsTotalAmount);
        setTotalCost(totalCost);
        checkRules();
    }

    private void checkRules() {
    }

    @Atomic
    public void edit(final DateTime whenProcessedBySibs, final BigDecimal transactionsTotalAmount, final BigDecimal totalCost) {
        setWhenProcessedBySibs(whenProcessedBySibs);
        setTransactionsTotalAmount(transactionsTotalAmount);
        setTotalCost(totalCost);
        checkRules();
    }

    public boolean isDeletable() {
        return getReferenceCodesSet().isEmpty() && getSibsTransactionsSet().isEmpty();
    }

    @Override
    @Atomic
    public void delete() {
        if (!isDeletable()) {
            throw new TreasuryDomainException("error.SibsReportFile.cannot.delete");
        }

        setBennu(null);

        super.delete();
    }

    public static Stream<SibsReportFile> findAll() {
        return Bennu.getInstance().getSibsReportFilesSet().stream();
    }

    public static Stream<SibsReportFile> findByBennu(final Bennu bennu) {
        return findAll().filter(i -> bennu.equals(i.getBennu()));
    }

    public static Stream<SibsReportFile> findByWhenProcessedBySibs(final LocalDate whenProcessedBySibs) {
        return findAll().filter(i -> whenProcessedBySibs.equals(i.getWhenProcessedBySibs()));
    }

    public static Stream<SibsReportFile> findByTransactionsTotalAmount(final BigDecimal transactionsTotalAmount) {
        return findAll().filter(i -> transactionsTotalAmount.equals(i.getTransactionsTotalAmount()));
    }

    public static Stream<SibsReportFile> findByTotalCost(final BigDecimal totalCost) {
        return findAll().filter(i -> totalCost.equals(i.getTotalCost()));
    }

    @Override
    public boolean isAccessible(User arg0) {
        return true;
    }

    @Atomic
    public static SibsReportFile create(final DateTime whenProcessedBySibs, final BigDecimal transactionsTotalAmount,
            final BigDecimal totalCost, final String displayName, final String fileName, final byte[] content) {
        return new SibsReportFile(whenProcessedBySibs, transactionsTotalAmount, totalCost, displayName, fileName, content);

    }

    protected static byte[] buildContentFor(final SIBSImportationFileDTO reportFileDTO) {
        final String whenProcessedBySibsLabel =
                BundleUtil.getString(Constants.BUNDLE, "label.SibsReportFile.whenProcessedBySibs");
        final String filenameLabel = BundleUtil.getString(Constants.BUNDLE, "label.SibsReportFile.filename");
        final String transactionsTotalAmountLabel =
                BundleUtil.getString(Constants.BUNDLE, "label.SibsReportFile.transactionsTotalAmount");
        final String totalCostLabel = BundleUtil.getString(Constants.BUNDLE, "label.SibsReportFile.totalCost");
        final String fileVersionLabel = BundleUtil.getString(Constants.BUNDLE, "label.SibsReportFile.fileVersion");
        final String sibsTransactionIdLabel = BundleUtil.getString(Constants.BUNDLE, "label.SibsReportFile.sibsTransactionId");
        final String sibsTransactionTotalAmountLabel =
                BundleUtil.getString(Constants.BUNDLE, "label.SibsReportFile.transactionTotalAmount");
        final String transactionWhenRegisteredLabel =
                BundleUtil.getString(Constants.BUNDLE, "label.SibsReportFile.transactionWhenRegistered");
        final String transactionDescriptionLabel =
                BundleUtil.getString(Constants.BUNDLE, "label.SibsReportFile.transactionDescription");
        final String transactionAmountLabel = BundleUtil.getString(Constants.BUNDLE, "label.SibsReportFile.transactionAmount");
        final String paymentCodeLabel = BundleUtil.getString(Constants.BUNDLE, "label.SibsReportFile.paymentCode");
        final String studentNumberLabel = BundleUtil.getString(Constants.BUNDLE, "label.SibsReportFile.studentNumber");
        final String personNameLabel = BundleUtil.getString(Constants.BUNDLE, "label.SibsReportFile.personName");

        final String descriptionLabel = BundleUtil.getString(Constants.BUNDLE, "label.SibsReportFile.description");

        final SheetData<SIBSImportationLineDTO> sheetData = new SheetData<SIBSImportationLineDTO>(reportFileDTO.getLines()) {

            @Override
            protected void makeLine(final SIBSImportationLineDTO line) {
                addCell(whenProcessedBySibsLabel, line.getWhenProcessedBySibs());
                addCell(filenameLabel, line.getFilename());
                addCell(transactionsTotalAmountLabel, line.getTransactionsTotalAmount().toPlainString());
                addCell(totalCostLabel, line.getTotalCost().toPlainString());
                addCell(fileVersionLabel, line.getFileVersion());
                addCell(sibsTransactionIdLabel, line.getSibsTransactionId());
                addCell(sibsTransactionTotalAmountLabel, line.getTransactionTotalAmount().toPlainString());
                addCell(paymentCodeLabel, line.getCode());
                addCell(transactionWhenRegisteredLabel, line.getTransactionWhenRegistered().toString("yyyy-MM-dd HH:mm"));
                addCell(studentNumberLabel, line.getStudentNumber());
                addCell(personNameLabel, line.getPersonName());
                addCell(descriptionLabel, line.getDescription());

                for (int i = 0; i < line.getNumberOfTransactions(); i++) {
                    addCell(transactionDescriptionLabel, line.getTransactionDescription(i));
                    addCell(transactionAmountLabel, line.getTransactionAmount(i));
                }
            }
        };

        final String sheetName = "label.SibsReportFile.sheetName";
        BundleUtil.getString(Constants.BUNDLE, sheetName);

        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            new SpreadsheetBuilder().addSheet(sheetName, sheetData).build(WorkbookExportFormat.EXCEL, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new TreasuryDomainException("error.SibsReportFile.spreadsheet.generation.failed");
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                throw new TreasuryDomainException("error.SibsReportFile.spreadsheet.generation.failed");
            }
        }
    }

    protected static String filenameFor(final SIBSImportationFileDTO reportFileDTO) {
        final String date = new DateTime().toString("yyyyMMddHHmm");
        return "Relatorio-SIBS-" + date + ".xlsx";
    }

    protected static String displayNameFor(final SIBSImportationFileDTO reportFileDTO) {
        final String date = new DateTime().toString("yyyyMMddHHmm");
        return "Relatorio-SIBS-" + date;
    }

    @Atomic
    public static SibsReportFile processSIBSIncommingFile(SIBSImportationFileDTO reportDTO) {
        byte[] content = buildContentFor(reportDTO);
        SibsReportFile result =
                SibsReportFile.create(reportDTO.getWhenProcessedBySibs(), reportDTO.getTransactionsTotalAmount(),
                        reportDTO.getTotalCost(), displayNameFor(reportDTO), filenameFor(reportDTO), content);

        for (SIBSImportationLineDTO line : reportDTO.getLines()) {
            if (line.getPaymentCode() != null) {
                SibsTransactionDetail.create(result, "", line.getWhenProcessedBySibs(), line.getTransactionWhenRegistered(),
                        line.getTransactionTotalAmount(), line.getPaymentCode().getPaymentCodePool().getEntityReferenceCode(),
                        line.getCode(), line.getSibsTransactionId());
            }
        }

        return result;
    }

    public Integer getNumberOfTransactions() {
        return this.getSibsTransactionsSet().size();
    }

    public String getTransactionDescription(Integer index) {
        if (this.getSibsTransactionsSet().size() > index) {
            if (index > 0) {
                return this.getSibsTransactionsSet().stream().skip(index - 1).findFirst().get().toString();
            } else if (index == 0) {
                return this.getSibsTransactionsSet().iterator().next().toString();
            }
        }
        return "";
    }

    public BigDecimal getTransactionAmount(Integer index) {
        if (this.getSibsTransactionsSet().size() > index) {
            if (index > 0) {
                return this.getSibsTransactionsSet().stream().skip(index - 1).findFirst().get().getAmountPayed();
            } else if (index == 0) {
                return this.getSibsTransactionsSet().iterator().next().getAmountPayed();
            }
        }
        return BigDecimal.ZERO;
    }

    @Atomic
    public void updateLogMessages(ProcessResult result) {
        StringBuilder build = new StringBuilder();
        for (String s : result.getErrorMessages()) {
            build.append(s + "\n");
        }
        this.setErrorLog(build.toString());
        build = new StringBuilder();
        for (String s : result.getActionMessages()) {
            build.append(s + "\n");
        }
        this.setInfoLog(build.toString());
    }
}
