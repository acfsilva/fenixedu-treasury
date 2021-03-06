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
package org.fenixedu.treasury.services.integration.erp;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import oecd.standardauditfile_tax.pt_1.AddressStructure;
import oecd.standardauditfile_tax.pt_1.AddressStructurePT;
import oecd.standardauditfile_tax.pt_1.AuditFile;
import oecd.standardauditfile_tax.pt_1.Header;
import oecd.standardauditfile_tax.pt_1.MovementTax;
import oecd.standardauditfile_tax.pt_1.OrderReferences;
import oecd.standardauditfile_tax.pt_1.PaymentMethod;
import oecd.standardauditfile_tax.pt_1.SAFTPTMovementTaxType;
import oecd.standardauditfile_tax.pt_1.SAFTPTSettlementType;
import oecd.standardauditfile_tax.pt_1.SAFTPTSourceBilling;
import oecd.standardauditfile_tax.pt_1.SAFTPTSourcePayment;
import oecd.standardauditfile_tax.pt_1.SourceDocuments;
import oecd.standardauditfile_tax.pt_1.SourceDocuments.Payments;
import oecd.standardauditfile_tax.pt_1.SourceDocuments.Payments.Payment;
import oecd.standardauditfile_tax.pt_1.SourceDocuments.Payments.Payment.Line.SourceDocumentID;
import oecd.standardauditfile_tax.pt_1.SourceDocuments.WorkingDocuments.WorkDocument;
import oecd.standardauditfile_tax.pt_1.Tax;
import oecd.standardauditfile_tax.pt_1.TaxTableEntry;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.treasury.domain.AdhocCustomer;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.Product;
import org.fenixedu.treasury.domain.Vat;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.document.CreditEntry;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.document.FinantialDocumentEntry;
import org.fenixedu.treasury.domain.document.Invoice;
import org.fenixedu.treasury.domain.document.InvoiceEntry;
import org.fenixedu.treasury.domain.document.PaymentEntry;
import org.fenixedu.treasury.domain.document.ReimbursementEntry;
import org.fenixedu.treasury.domain.document.SettlementEntry;
import org.fenixedu.treasury.domain.document.SettlementNote;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.integration.ERPConfiguration;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;
import org.fenixedu.treasury.domain.integration.OperationFile;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentStatusWS;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationInput;
import org.fenixedu.treasury.services.integration.erp.dto.DocumentsInformationOutput;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

// ******************************************************************************************************************************
// http://info.portaldasfinancas.gov.pt/NR/rdonlyres/3B4FECDB-2380-45D7-9019-ABCA80A7E99E/0/Comunicacao_Dados_Doc_Transporte.pdf
// http://info.portaldasfinancas.gov.pt/NR/rdonlyres/15D18787-8AA9-4060-90D5-79F168A927A4/0/Portaria_11922009.pdf
// (Documento Original)
// http://dre.pt/pdf1sdip/2012/11/22700/0672406740.pdf (Adenda para os
// Documentos de Transporte)
// Versão 1.0.3
// https://info.portaldasfinancas.gov.pt/NR/rdonlyres/BA9FB096-D482-445D-A5DB-C05B1980F7D7/0/Portaria_274_2013_21_09.pdf
// ******************************************************************************************************************************
public class ERPExporter {

    private static Logger logger = LoggerFactory.getLogger(ERPExporter.class);
    public final static String ERP_HEADER_VERSION_1_00_00 = "1.0.0";

    private String generateERPFile(FinantialInstitution institution, DateTime fromDate, DateTime toDate,
            List<? extends FinantialDocument> allDocuments, Boolean generateAllCustomers, Boolean generateAllProducts,
            java.util.function.UnaryOperator<AuditFile> preProcessFunctionBeforeSerialize) {

        // Build SAFT-AuditFile
        AuditFile auditFile = new AuditFile();
        // ThreadInformation information = 
        // SaftThreadRegister.retrieveCurrentThreadInformation();

        // Build SAFT-HEADER (Chapter 1 in AuditFile)
        Header header = this.createSAFTHeader(fromDate, toDate, institution, ERP_HEADER_VERSION_1_00_00);
        // SetHeader
        auditFile.setHeader(header);

        // Build Master-Files
        oecd.standardauditfile_tax.pt_1.AuditFile.MasterFiles masterFiles =
                new oecd.standardauditfile_tax.pt_1.AuditFile.MasterFiles();

        // SetMasterFiles
        auditFile.setMasterFiles(masterFiles);

        // Build SAFT-MovementOfGoods (Customer and Products are built inside)
        // ProductsTable (Chapter 2.4 in AuditFile)
        List<oecd.standardauditfile_tax.pt_1.Product> productList = masterFiles.getProduct();
        Map<String, oecd.standardauditfile_tax.pt_1.Product> productMap =
                new HashMap<String, oecd.standardauditfile_tax.pt_1.Product>();
        Set<String> productCodes = new HashSet<String>();

        // ClientsTable (Chapter 2.2 in AuditFile)
        List<oecd.standardauditfile_tax.pt_1.Customer> customerList = masterFiles.getCustomer();
        Map<String, oecd.standardauditfile_tax.pt_1.Customer> customerMap =
                new HashMap<String, oecd.standardauditfile_tax.pt_1.Customer>();

        // Readd All  Clients if needed
        if (generateAllCustomers) {
            logger.info("Reading all Customers in Institution " + institution.getCode());

            Set<Customer> allCustomers = new HashSet<Customer>();
            for (DebtAccount debt : institution.getDebtAccountsSet()) {
                allCustomers.add(debt.getCustomer());
            }

            // Update the Total Objects Count
            // information.setTotalCounter(allCustomers.size() +
            // allProducts.size() + allDocuments.size() * 10);

            int i = 0;
            for (Customer customer : allCustomers) {
                oecd.standardauditfile_tax.pt_1.Customer saftCustomer = this.convertCustomerToSAFTCustomer(customer);
                // information.setCurrentCounter(information.getCurrentCounter()
                // + 1);
                customerMap.put(saftCustomer.getCustomerID(), saftCustomer);
                i++;
                if (i % 100 == 0) {
                    logger.info("Processing " + i + "/" + allCustomers.size() + " Customers in Institution "
                            + institution.getCode());
                }
            }
        }
        // Readd All Products if needed
        if (generateAllProducts) {

            logger.info("Reading all Customers in Institution " + institution.getCode());
            Set<Product> allProducts = institution.getAvailableProductsSet();
            int i = 0;
            for (Product product : allProducts) {
                if (!productCodes.contains(product.getCode())) {
                    oecd.standardauditfile_tax.pt_1.Product saftProduct = this.convertProductToSAFTProduct(product);
                    productCodes.add(product.getCode());
                    productMap.put(saftProduct.getProductCode(), saftProduct);
                }

                i++;
                if (i % 100 == 0) {
                    logger.info("Processing " + i + "/" + allProducts.size() + " Products in Institution "
                            + institution.getCode());
                }

                // information.setCurrentCounter(information.getCurrentCounter()
                // + 1);
            }
        } else {
            // information.setTotalCounter(allDocuments.size() * 10);
            // Update the Total Objects Count
            // information.setCurrentCounter(0);
        }

        // TaxTable (Chapter 2.5 in AuditFile)
        oecd.standardauditfile_tax.pt_1.TaxTable taxTable = new oecd.standardauditfile_tax.pt_1.TaxTable();
        masterFiles.setTaxTable(taxTable);

        for (Vat vat : institution.getVatsSet()) {
            taxTable.getTaxTableEntry().add(this.convertVATtoTaxTableEntry(vat, institution));
        }

        // Set MovementOfGoods in SourceDocuments(AuditFile)
        oecd.standardauditfile_tax.pt_1.SourceDocuments sourceDocuments = new oecd.standardauditfile_tax.pt_1.SourceDocuments();
        auditFile.setSourceDocuments(sourceDocuments);

        SourceDocuments.SalesInvoices invoices = new SourceDocuments.SalesInvoices();
        SourceDocuments.WorkingDocuments workingDocuments = new SourceDocuments.WorkingDocuments();
        Payments paymentsDocuments = new Payments();

        BigInteger numberOfPaymentsDocuments = BigInteger.ZERO;
        BigDecimal totalDebitOfPaymentsDocuments = BigDecimal.ZERO;
        BigDecimal totalCreditOfPaymentsDocuments = BigDecimal.ZERO;

        BigInteger numberOfWorkingDocuments = BigInteger.ZERO;
        BigDecimal totalDebitOfWorkingDocuments = BigDecimal.ZERO;
        BigDecimal totalCreditOfWorkingDocuments = BigDecimal.ZERO;

        invoices.setNumberOfEntries(BigInteger.ZERO);
        invoices.setTotalCredit(BigDecimal.ZERO);
        invoices.setTotalDebit(BigDecimal.ZERO);

//        int i = 0;
        for (FinantialDocument document : allDocuments) {
            if ((document.isCreditNote() || document.isDebitNote()) && (document.isClosed() || document.isAnnulled())) {
                try {
                    WorkDocument workDocument = convertToSAFTWorkDocument((Invoice) document, customerMap, productMap);
                    workingDocuments.getWorkDocument().add(workDocument);

                    // AcumulateValues
                    numberOfWorkingDocuments = numberOfWorkingDocuments.add(BigInteger.ONE);
                    if (!document.isAnnulled()) {
                        if (document.isDebitNote()) {
                            totalDebitOfWorkingDocuments =
                                    totalDebitOfWorkingDocuments.add(workDocument.getDocumentTotals().getNetTotal());
                        } else if (document.isCreditNote()) {
                            totalCreditOfWorkingDocuments =
                                    totalCreditOfWorkingDocuments.add(workDocument.getDocumentTotals().getNetTotal());
                        }
                    }

//                    i++;

                } catch (Exception ex) {
                    logger.error("Error processing document " + document.getUiDocumentNumber() + ": " + ex.getLocalizedMessage());
                    throw ex;
                }
            } else {
                logger.info("Ignoring document " + document.getUiDocumentNumber() + " because is not closed yet.");
            }

        }
        // Update Totals of Workingdocuments
        workingDocuments.setNumberOfEntries(numberOfWorkingDocuments);
        workingDocuments.setTotalCredit(totalCreditOfWorkingDocuments.setScale(2, RoundingMode.HALF_EVEN));
        workingDocuments.setTotalDebit(totalDebitOfWorkingDocuments.setScale(2, RoundingMode.HALF_EVEN));

        sourceDocuments.setWorkingDocuments(workingDocuments);

        //PROCESSING PAYMENTS TABLE

        paymentsDocuments.setNumberOfEntries(BigInteger.ZERO);
        paymentsDocuments.setTotalCredit(BigDecimal.ZERO);
        paymentsDocuments.setTotalDebit(BigDecimal.ZERO);
        for (FinantialDocument document : allDocuments) {
            if (document.isSettlementNote() && (document.isClosed() || document.isAnnulled())) {
                try {
                    Payment paymentDocument = convertToSAFTPaymentDocument((SettlementNote) document, customerMap, productMap);
                    paymentsDocuments.getPayment().add(paymentDocument);

                    // AcumulateValues
                    numberOfPaymentsDocuments = numberOfPaymentsDocuments.add(BigInteger.ONE);
                    if (!document.isAnnulled()) {
                        totalCreditOfPaymentsDocuments =
                                totalCreditOfPaymentsDocuments.add(((SettlementNote) document).getTotalCreditAmount());
                        totalDebitOfPaymentsDocuments =
                                totalDebitOfPaymentsDocuments.add(((SettlementNote) document).getTotalDebitAmount());
                    }
//                    i++;
                } catch (Exception ex) {
                    // persistenceSupport.flush();
                    logger.error("Error processing document " + document.getUiDocumentNumber() + ": " + ex.getLocalizedMessage());
                    throw ex;
                }
            } else {
                logger.info("Ignoring document " + document.getUiDocumentNumber() + " because is not closed yet.");
            }

        }

        // Update Totals of Payment Documents
        paymentsDocuments.setNumberOfEntries(numberOfPaymentsDocuments);
        paymentsDocuments.setTotalCredit(totalCreditOfPaymentsDocuments.setScale(2, RoundingMode.HALF_EVEN));
        paymentsDocuments.setTotalDebit(totalDebitOfPaymentsDocuments.setScale(2, RoundingMode.HALF_EVEN));
        sourceDocuments.setPayments(paymentsDocuments);

        // Update the Customer Table in SAFT
        for (oecd.standardauditfile_tax.pt_1.Customer customer : customerMap.values()) {
            customerList.add(customer);
        }

        // Update the Product Table in SAFT
        for (oecd.standardauditfile_tax.pt_1.Product product : productMap.values()) {
            productList.add(product);
        }

        if (preProcessFunctionBeforeSerialize != null) {
            auditFile = preProcessFunctionBeforeSerialize.apply(auditFile);
        }
        String xml = exportAuditFileToXML(auditFile);

        logger.info("SAFT File export concluded with success.");
        return xml;
    }

    private Payment convertToSAFTPaymentDocument(SettlementNote document,
            Map<String, oecd.standardauditfile_tax.pt_1.Customer> baseCustomers,
            Map<String, oecd.standardauditfile_tax.pt_1.Product> productMap) {
        Payment payment = new Payment();

        // Find the Customer in BaseCustomers
        oecd.standardauditfile_tax.pt_1.Customer customer = null;

        if (baseCustomers.containsKey(document.getDebtAccount().getCustomer().getCode())) {
            customer = baseCustomers.get(document.getDebtAccount().getCustomer().getCode());
        } else {
            // If not found, create a new one and add it to baseCustomers
            customer = convertCustomerToSAFTCustomer(document.getDebtAccount().getCustomer());
            baseCustomers.put(customer.getCustomerID(), customer);
        }

        // MovementDate
        DatatypeFactory dataTypeFactory;
        try {
            dataTypeFactory = DatatypeFactory.newInstance();
            DateTime documentDate = document.getDocumentDate();

            // SystemEntryDate
            payment.setSystemEntryDate(convertToXMLDateTime(dataTypeFactory, documentDate));

            payment.setTransactionDate(convertToXMLDateTime(dataTypeFactory, documentDate));

            // DocumentNumber
            payment.setPaymentRefNo(document.getUiDocumentNumber());

            // CustomerID
            payment.setCustomerID(document.getDebtAccount().getCustomer().getCode());

            // DocumentStatus
            /*
             * Deve ser preenchido com: ?N? ? Normal; Texto 1 ?T? ? Por conta de
             * terceiros; ?A? ? Documento anulado.
             */
            SourceDocuments.Payments.Payment.DocumentStatus status = new SourceDocuments.Payments.Payment.DocumentStatus();
            if (document.isAnnulled()) {
                status.setPaymentStatus("A");
            } else {
                status.setPaymentStatus("N");
            }
            status.setPaymentStatusDate(payment.getSystemEntryDate());
            // status.setReason("");
            // Utilizador responsável pelo estado atual do docu-mento.
            status.setSourceID(document.getVersioningUpdatedBy());
            // Deve ser preenchido com:
            // 'P' - Documento produzido na aplicacao;
            if (Boolean.TRUE.equals(document.getDocumentNumberSeries().getSeries().getExternSeries())
                    || Boolean.TRUE.equals(document.getDocumentNumberSeries().getSeries().getLegacy())) {
                status.setSourcePayment(SAFTPTSourcePayment.I);
            } else {
                status.setSourcePayment(SAFTPTSourcePayment.P);
            }

            payment.setDocumentStatus(status);

            //Check if is Rehimbursement/Payment
            if (Constants.isPositive(document.getTotalPayedAmount())) {
                //PaymentMethods
                for (PaymentEntry paymentEntry : document.getPaymentEntriesSet()) {
                    PaymentMethod method = new PaymentMethod();
                    method.setPaymentAmount(paymentEntry.getPayedAmount().setScale(2, RoundingMode.HALF_EVEN));
                    method.setPaymentDate(payment.getTransactionDate());
                    method.setPaymentMechanism(convertToSAFTPaymentMechanism(paymentEntry.getPaymentMethod()));
                    payment.getPaymentMethod().add(method);
                }
                payment.setSettlementType(SAFTPTSettlementType.NL);
            } else if (Constants.isPositive(document.getTotalReimbursementAmount())) {
                //Reimbursments
                for (ReimbursementEntry reimbursmentEntry : document.getReimbursementEntriesSet()) {
                    PaymentMethod method = new PaymentMethod();
                    method.setPaymentAmount(reimbursmentEntry.getReimbursedAmount().setScale(2, RoundingMode.HALF_EVEN));
                    method.setPaymentDate(payment.getTransactionDate());
                    method.setPaymentMechanism(convertToSAFTPaymentMechanism(reimbursmentEntry.getPaymentMethod()));
                    payment.getPaymentMethod().add(method);
                    payment.setSettlementType(SAFTPTSettlementType.NR);
                }
            } else {
                payment.setSettlementType(SAFTPTSettlementType.NN);
            }

            payment.setSourceID(document.getVersioningCreator());

            // DocumentTotals
            SourceDocuments.Payments.Payment.DocumentTotals docTotals = new SourceDocuments.Payments.Payment.DocumentTotals();

            //Lines
            BigInteger i = BigInteger.ONE;
            for (SettlementEntry settlementEntry : document.getSettlemetEntriesSet()) {
                SourceDocuments.Payments.Payment.Line line = new SourceDocuments.Payments.Payment.Line();
                line.setLineNumber(i);
                //SourceDocument
                SourceDocumentID sourceDocument = new SourceDocumentID();
                sourceDocument.setLineNumber(BigInteger.valueOf(settlementEntry.getInvoiceEntry().getEntryOrder()));
                sourceDocument.setOriginatingON(settlementEntry.getInvoiceEntry().getFinantialDocument().getUiDocumentNumber());
                sourceDocument.setInvoiceDate(convertToXMLDateTime(dataTypeFactory, settlementEntry.getInvoiceEntry()
                        .getFinantialDocument().getDocumentDate()));
                sourceDocument.setDescription(settlementEntry.getDescription());
                line.getSourceDocumentID().add(sourceDocument);
                //SettlementAmount
                line.setSettlementAmount(BigDecimal.ZERO);
                if (settlementEntry.getInvoiceEntry().isDebitNoteEntry()) {
                    line.setDebitAmount(settlementEntry.getTotalAmount());
                } else if (settlementEntry.getInvoiceEntry().isCreditNoteEntry()) {
                    line.setCreditAmount(settlementEntry.getTotalAmount());
                }
                payment.getLine().add(line);
                i = i.add(BigInteger.ONE);
            }
            docTotals.setGrossTotal(document.getTotalAmount().setScale(2, RoundingMode.HALF_EVEN));
            docTotals.setNetTotal(document.getTotalAmount().setScale(2, RoundingMode.HALF_EVEN));
            docTotals.setTaxPayable(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
            payment.setDocumentTotals(docTotals);

            // Period
            /*
             * Per?odo contabil?stico (Period) . . . . . . . . . . Deve ser
             * indicado o n?mero do m?s do per?odo de tributa??o, de ?1? a ?12?,
             * contado desde a data do in?cio. Pode ainda ser preenchido com
             * ?13?, ?14?, ?15? ou ?16? para movimentos efectuados no ?ltimo m?s
             * do per?odo de tributa??o, relacionados com o apuramento do
             * resultado. Ex.: movimentos de apuramentos de invent?rios,
             * deprecia??es, ajustamentos ou apuramentos de resultados.
             */
            payment.setPeriod(document.getDocumentDate().getMonthOfYear());

            // SourceID
            /*
             * C?digo do utilizador que registou o movimento (SourceID).
             */
            payment.setSourceID(document.getVersioningCreator());

        } catch (DatatypeConfigurationException e) {

            e.printStackTrace();
        }

        return payment;
    }

    private XMLGregorianCalendar convertToXMLDateTime(DatatypeFactory dataTypeFactory, DateTime documentDate) {
        return dataTypeFactory.newXMLGregorianCalendar(documentDate.getYear(), documentDate.getMonthOfYear(),
                documentDate.getDayOfMonth(), documentDate.getHourOfDay(), documentDate.getMinuteOfHour(),
                documentDate.getSecondOfMinute(), 0, DatatypeConstants.FIELD_UNDEFINED);
    }

    private String convertToSAFTPaymentMechanism(org.fenixedu.treasury.domain.PaymentMethod paymentMethod) {
        String code = paymentMethod.getCode();
        if (code == "CC" || code == "CD" || code == "CH" || code == "CO" || code == "CS" || code == "DE" || code == "LC"
                || code == "MB" || code == "NU" || code == "TB" || code == "TR") {
            return paymentMethod.getCode();
        } else {
            return "OU";
        }
    }

    private WorkDocument convertToSAFTWorkDocument(Invoice document,
            Map<String, oecd.standardauditfile_tax.pt_1.Customer> baseCustomers,
            Map<String, oecd.standardauditfile_tax.pt_1.Product> baseProducts) {
        WorkDocument workDocument = new WorkDocument();

        // Find the Customer in BaseCustomers
        oecd.standardauditfile_tax.pt_1.Customer customer = null;

        if (baseCustomers.containsKey(document.getDebtAccount().getCustomer().getCode())) {
            customer = baseCustomers.get(document.getDebtAccount().getCustomer().getCode());
        } else {
            // If not found, create a new one and add it to baseCustomers
            customer = convertCustomerToSAFTCustomer(document.getDebtAccount().getCustomer());
            baseCustomers.put(customer.getCustomerID(), customer);
        }

        //check the PayorDebtAccount
        if (document.getPayorDebtAccount() != null) {
            if (baseCustomers.containsKey(document.getPayorDebtAccount().getCustomer().getCode())) {
                //do nothing
            } else {
                // If not found, create a new one and add it to baseCustomers
                oecd.standardauditfile_tax.pt_1.Customer payorCustomer =
                        convertCustomerToSAFTCustomer(document.getPayorDebtAccount().getCustomer());
                baseCustomers.put(payorCustomer.getCustomerID(), payorCustomer);
            }
        }

        // MovementDate
        DatatypeFactory dataTypeFactory;
        try {
            dataTypeFactory = DatatypeFactory.newInstance();
            DateTime documentDate = document.getDocumentDate();

            // SystemEntryDate
            workDocument.setSystemEntryDate(convertToXMLDateTime(dataTypeFactory, documentDate));

            workDocument.setWorkDate(convertToXMLDateTime(dataTypeFactory, documentDate));

            // DocumentNumber
            workDocument.setDocumentNumber(document.getUiDocumentNumber());

            // CustomerID
            workDocument.setCustomerID(document.getDebtAccount().getCustomer().getCode());

            //PayorID
            if (document.getPayorDebtAccount() != null) {
                workDocument.setPayorCustomerID(document.getPayorDebtAccount().getCustomer().getCode());
            }

            // DocumentStatus
            /*
             * Deve ser preenchido com: ?N? ? Normal; Texto 1 ?T? ? Por conta de
             * terceiros; ?A? ? Documento anulado.
             */
            SourceDocuments.WorkingDocuments.WorkDocument.DocumentStatus status =
                    new SourceDocuments.WorkingDocuments.WorkDocument.DocumentStatus();
            if (document.isAnnulled()) {
                status.setWorkStatus("A");
            } else {
                status.setWorkStatus("N");
            }

            status.setWorkStatusDate(workDocument.getSystemEntryDate());
            // status.setReason("");
            // Utilizador responsável pelo estado atual do docu-mento.
            status.setSourceID(document.getVersioningUpdatedBy());
            // Deve ser preenchido com:
            // 'P' - Documento produzido na aplicacao;
            if (Boolean.TRUE.equals(document.getDocumentNumberSeries().getSeries().getExternSeries())) {
                status.setSourceBilling(SAFTPTSourceBilling.I);
            } else {
                status.setSourceBilling(SAFTPTSourceBilling.P);
            }

            workDocument.setDocumentStatus(status);

            // DocumentTotals
            SourceDocuments.WorkingDocuments.WorkDocument.DocumentTotals docTotals =
                    new SourceDocuments.WorkingDocuments.WorkDocument.DocumentTotals();
            docTotals.setGrossTotal(document.getTotalAmount().setScale(2, RoundingMode.HALF_EVEN));
            docTotals.setNetTotal(document.getTotalNetAmount().setScale(2, RoundingMode.HALF_EVEN));
            docTotals.setTaxPayable(document.getTotalAmount().subtract(document.getTotalNetAmount())
                    .setScale(2, RoundingMode.HALF_EVEN));
            workDocument.setDocumentTotals(docTotals);

            // WorkType
            /*
             * Deve ser preenchido com: Texto 2 "DC" — Documentos emitidos que
             * sejam suscetiveis de apresentacao ao cliente para conferencia de
             * entrega de mercadorias ou da prestacao de servicos. "FC" — Fatura
             * de consignacao nos termos do artigo 38º do codigo do IVA.
             */
            workDocument.setWorkType("DC");

            // Period
            /*
             * Per?odo contabil?stico (Period) . . . . . . . . . . Deve ser
             * indicado o n?mero do m?s do per?odo de tributa??o, de ?1? a ?12?,
             * contado desde a data do in?cio. Pode ainda ser preenchido com
             * ?13?, ?14?, ?15? ou ?16? para movimentos efectuados no ?ltimo m?s
             * do per?odo de tributa??o, relacionados com o apuramento do
             * resultado. Ex.: movimentos de apuramentos de invent?rios,
             * deprecia??es, ajustamentos ou apuramentos de resultados.
             */
            workDocument.setPeriod(document.getDocumentDate().getMonthOfYear());

            // SourceID
            /*
             * C?digo do utilizador que registou o movimento (SourceID).
             */
            workDocument.setSourceID(document.getVersioningCreator());

        } catch (DatatypeConfigurationException e) {

            e.printStackTrace();
        }

        List<oecd.standardauditfile_tax.pt_1.SourceDocuments.WorkingDocuments.WorkDocument.Line> productLines =
                workDocument.getLine();

        // Process individual
        BigInteger i = BigInteger.ONE;
        for (FinantialDocumentEntry docLine : document.getFinantialDocumentEntriesSet()) {
            InvoiceEntry orderNoteLine = (InvoiceEntry) docLine;
            oecd.standardauditfile_tax.pt_1.SourceDocuments.WorkingDocuments.WorkDocument.Line line =
                    convertToSAFTWorkDocumentLine(orderNoteLine, baseProducts);

            // LineNumber
            line.setLineNumber(i);

            // Add to productLines
            i = i.add(BigInteger.ONE);
            productLines.add(line);
        }

        return workDocument;
    }

    private oecd.standardauditfile_tax.pt_1.SourceDocuments.WorkingDocuments.WorkDocument.Line convertToSAFTWorkDocumentLine(
            InvoiceEntry entry, Map<String, oecd.standardauditfile_tax.pt_1.Product> baseProducts) {
        oecd.standardauditfile_tax.pt_1.Product currentProduct = null;

        Product product = entry.getProduct();

        if (product.getCode() != null && baseProducts.containsKey(product.getCode())) {
            currentProduct = baseProducts.get(product.getCode());
        } else {
            currentProduct = convertProductToSAFTProduct(product);
            baseProducts.put(currentProduct.getProductCode(), currentProduct);
        }
        XMLGregorianCalendar documentDateCalendar = null;
        try {
            DatatypeFactory dataTypeFactory = DatatypeFactory.newInstance();
            DateTime documentDate = entry.getFinantialDocument().getDocumentDate();
            documentDateCalendar = convertToXMLDateTime(dataTypeFactory, documentDate);
        } catch (DatatypeConfigurationException e) {

            e.printStackTrace();
        }

        oecd.standardauditfile_tax.pt_1.SourceDocuments.WorkingDocuments.WorkDocument.Line line =
                new oecd.standardauditfile_tax.pt_1.SourceDocuments.WorkingDocuments.WorkDocument.Line();

        if (entry.isCreditNoteEntry()) {
            line.setCreditAmount(entry.getAmount().setScale(2, RoundingMode.HALF_EVEN));
        } else if (entry.isDebitNoteEntry()) {
            line.setDebitAmount(entry.getAmount().setScale(2, RoundingMode.HALF_EVEN));
        }

        // Description
        line.setDescription(entry.getDescription());
        List<OrderReferences> orderReferences = line.getOrderReferences();

        //Add the references on the document creditEntries <-> debitEntries
        if (entry.isCreditNoteEntry()) {
            CreditEntry creditEntry = (CreditEntry) entry;
            if (creditEntry.getDebitEntry() != null) {
                OrderReferences reference = new OrderReferences();
                reference.setOriginatingON(creditEntry.getDebitEntry().getFinantialDocument().getUiDocumentNumber());
                reference.setOrderDate(documentDateCalendar);
                orderReferences.add(reference);
            }

        } else if (entry.isDebitNoteEntry()) {
//            DebitEntry debitEntry = (DebitEntry) entry;
//            for (CreditEntry creditEntry : debitEntry.getCreditEntriesSet()) {
//                OrderReferences reference = new OrderReferences();
//                reference.setOriginatingON(creditEntry.getFinantialDocument().getUiDocumentNumber());
//                reference.setOrderDate(documentDateCalendar);
//                orderReferences.add(reference);
//            }
        }

        // ProductCode
        line.setProductCode(currentProduct.getProductCode());

        // ProductDescription
        line.setProductDescription(currentProduct.getProductDescription());

        // Quantity
        line.setQuantity(entry.getQuantity());

        // SettlementAmount
        line.setSettlementAmount(BigDecimal.ZERO);

        // Tax
        line.setTax(getSAFTWorkingDocumentsTax(product, entry.getVat()));

        line.setTaxPointDate(documentDateCalendar);

        // TaxExemptionReason
        /*
         * Motivo da isen??o de imposto (TaxExemptionReason). Campo de
         * preenchimento obrigat?rio, quando os campos percentagem da taxa de
         * imposto (TaxPercentage) ou montante do imposto (TaxAmount) s?o iguais
         * a zero. Deve ser referido o preceito legal aplic?vel. . . . . . . . .
         * . Texto 60
         */
        if (line.getTax().getTaxPercentage() == BigDecimal.ZERO) {
            Vat vat = entry.getVat();

            if (vat.getVatExemptionReason() != null) {
                line.setTaxExemptionReason(vat.getVatExemptionReason().getCode() + "-" + vat.getVatExemptionReason().getName());
            } else {
                // HACK : DEFAULT
                // line.setTaxExemptionReason(VatExemptionReason.M1().getCode()
                // + "-" + VatExemptionReason.M1().getDescription());
            }
        }

        // UnitOfMeasure
        line.setUnitOfMeasure(product.getUnitOfMeasure().getContent());
        // UnitPrice
        line.setUnitPrice(entry.getAmount().setScale(2, RoundingMode.HALF_EVEN));

        return line;
    }

    private Tax getSAFTWorkingDocumentsTax(Product product, Vat vat) {
        Tax tax = new Tax();

        // VatType vat = product.getVatType();
        // Tax-TaxCode
        tax.setTaxCode(vat.getVatType().getCode());

        tax.setTaxCountryRegion("PT");

        // Tax-TaxPercentage
        tax.setTaxPercentage(vat.getTaxRate());

        // Tax-TaxType
        tax.setTaxType("IVA");

        return tax;
    }

    private TaxTableEntry convertVATtoTaxTableEntry(Vat vat, FinantialInstitution finantialInstitution) {
        TaxTableEntry entry = new TaxTableEntry();
        entry.setTaxType("IVA");
        entry.setTaxCode(vat.getVatType().getName().getContent());
        if (finantialInstitution.getFiscalNumber() != null) {
            entry.setTaxCountryRegion(finantialInstitution.getFiscalCountryRegion().getFiscalCode());
            entry.setDescription(finantialInstitution.getFiscalCountryRegion().getName().getContent() + "-"
                    + vat.getVatType().getName().getContent());
        } else {
            entry.setTaxCountryRegion("PT");
            entry.setDescription("");
        }
        entry.setTaxCode(vat.getVatType().getCode());
        entry.setTaxPercentage(vat.getTaxRate());

        if (Strings.isNullOrEmpty(entry.getDescription())) {
            entry.setDescription(entry.getTaxCode() + "_" + entry.getTaxCountryRegion());
        }
        return entry;
    }

    private Header createSAFTHeader(DateTime startDate, DateTime endDate, FinantialInstitution finantialInstitution,
            String auditVersion) {

        Header header = new Header();
        DatatypeFactory dataTypeFactory;
        try {

            dataTypeFactory = DatatypeFactory.newInstance();

            // AuditFileVersion
            header.setAuditFileVersion(auditVersion);

            // BusinessName - Nome da Empresa
            header.setBusinessName(finantialInstitution.getCompanyName());
            header.setCompanyName(finantialInstitution.getName());

            // CompanyAddress
            AddressStructurePT companyAddress = null;
            //TODOJN Locale por resolver
            companyAddress =
                    convertAddressToAddressPT(finantialInstitution.getAddress(), finantialInstitution.getZipCode(),
                            finantialInstitution.getMunicipality() != null ? finantialInstitution.getMunicipality()
                                    .getLocalizedName(new Locale("pt")) : "---", finantialInstitution.getAddress());
            header.setCompanyAddress(companyAddress);

            // CompanyID
            /*
             * Obtem -se pela concatena??o da conservat?ria do registo comercial
             * com o n?mero do registo comercial, separados pelo car?cter
             * espa?o. Nos casos em que n?o existe o registo comercial, deve ser
             * indicado o NIF.
             */
            header.setCompanyID(finantialInstitution.getComercialRegistrationCode());

            // CurrencyCode
            /*
             * 1.11 * C?digo de moeda (CurrencyCode) . . . . . . . Preencher com
             * ?EUR?
             */
            header.setCurrencyCode(finantialInstitution.getCurrency().getCode());

            // DateCreated
            DateTime now = new DateTime();
            header.setDateCreated(convertToXMLDateTime(dataTypeFactory, now));

            // Email
            // header.setEmail(StringUtils.EMPTY);

            // EndDate
            header.setEndDate(convertToXMLDateTime(dataTypeFactory, endDate));

            // Fax
            // header.setFax(StringUtils.EMPTY);

            // FiscalYear
            /*
             * Utilizar as regras do c?digo do IRC, no caso de per?odos
             * contabil?sticos n?o coincidentes com o ano civil. (Ex: per?odo de
             * tributa??o de 01 -10 -2008 a 30 -09 -2009 corresponde FiscalYear
             * 2008). Inteiro 4
             */
            header.setFiscalYear(endDate.getYear());

            // Ir obter a data do ?ltimo
            // documento(por causa de submeter em janeiro, documentos de
            // dezembro)

            // HeaderComment
            // header.setHeaderComment(org.apache.commons.lang.StringUtils.EMPTY);

            // ProductCompanyTaxID
            // Preencher com o NIF da entidade produtora do software
            header.setProductCompanyTaxID(SaftConfig.PRODUCT_COMPANY_TAX_ID());

            // ProductID
            /*
             * 1.16 * Nome do produto (ProductID). . . . . . . . . . . Nome do
             * produto que gera o SAF -T (PT) . . . . . . . . . . . Deve ser
             * indicado o nome comercial do software e o da empresa produtora no
             * formato ?Nome produto/nome empresa?.
             */
            header.setProductID(SaftConfig.PRODUCT_ID());

            // Product Version
            header.setProductVersion(SaftConfig.PRODUCT_VERSION());

            // SoftwareCertificateNumber
            header.setSoftwareCertificateNumber(BigInteger.valueOf(SaftConfig.SOFTWARE_CERTIFICATE_NUMBER()));

            // StartDate
            header.setStartDate(dataTypeFactory.newXMLGregorianCalendarDate(startDate.getYear(), startDate.getMonthOfYear(),
                    startDate.getDayOfMonth(), DatatypeConstants.FIELD_UNDEFINED));

            // TaxAccountingBasis
            /*
             * Deve ser preenchido com: contabilidade; facturao; ?I? ? dados
             * integrados de factura??o e contabilidade; ?S? ? autofactura??o;
             * ?P? ? dados parciais de factura??o
             */
            header.setTaxAccountingBasis("P");

            // TaxEntity
            /*
             * Identifica??o do estabelecimento (TaxEntity) No caso do ficheiro
             * de factura??o dever? ser especificado a que estabelecimento diz
             * respeito o ficheiro produzido, se aplic?vel, caso contr?rio,
             * dever? ser preenchido com a especifica??o ?Global?. No caso do
             * ficheiro de contabilidade ou integrado, este campo dever? ser
             * preenchido com a especifica??o ?Sede?. Texto 20
             */
            header.setTaxEntity("Global");

            // TaxRegistrationNumber
            /*
             * N?mero de identifica??o fiscal da empresa
             * (TaxRegistrationNumber). Preencher com o NIF portugu?s sem
             * espa?os e sem qualquer prefixo do pa?s. Inteiro 9
             */
            try {
                header.setTaxRegistrationNumber(Integer.parseInt(finantialInstitution.getFiscalNumber()));
            } catch (Exception ex) {
                throw new RuntimeException("Invalid Fiscal Number.");
            }

            // header.setTelephone(finantialInstitution.get);

            // header.setWebsite(finantialInstitution.getEmailContact());

            return header;
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private AddressStructurePT convertAddressToAddressPT(String addressDetail, String zipCode, String zipCodeRegion, String street) {
        AddressStructurePT companyAddress;
        companyAddress = new AddressStructurePT();
        companyAddress.setCountry("PT");
        companyAddress.setAddressDetail(Splitter.fixedLength(60).splitToList(addressDetail).get(0));
        companyAddress.setCity(Splitter.fixedLength(49).splitToList(zipCodeRegion).get(0));
        companyAddress.setPostalCode(zipCode);
        companyAddress.setRegion(zipCodeRegion);
        companyAddress.setStreetName(Splitter.fixedLength(49).splitToList(street).get(0));
        return companyAddress;
    }

    private String exportAuditFileToXML(AuditFile auditFile) {
        try {
            final String cleanXMLAnotations = "xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"";
            final String cleanXMLAnotations2 = "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
            final String cleanDateTimeMiliseconds = ".000<";
            final String cleanStandaloneAnnotation = "standalone=\"yes\"";

            final JAXBContext jaxbContext = JAXBContext.newInstance(AuditFile.class);
            Marshaller marshaller = jaxbContext.createMarshaller();

            StringWriter writer = new StringWriter();

            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "Windows-1252");
            marshaller.marshal(auditFile, writer);

            Charset charset = Charset.forName("Windows-1252");

            String xml = new String(charset.encode(writer.toString()).array(), "Windows-1252");
            xml = xml.replace(cleanXMLAnotations, "");
            xml = xml.replace(cleanXMLAnotations2, "");
            xml = xml.replace(cleanDateTimeMiliseconds, "<");
            xml = xml.replace(cleanStandaloneAnnotation, "");

            try {
                MessageDigest md = MessageDigest.getInstance("SHA1");
                md.update(("SALTING WITH QUB:" + xml).getBytes("Windows-1252"));
                byte[] output = md.digest();
                String digestAscii = bytesToHex(output);
                xml =
                        xml + "<!-- QUB-IT (remove this line,add the qubSALT, save with Windows-1252 encode): " + digestAscii
                                + " -->\n";
            } catch (Exception ex) {

            }
            return xml;
        } catch (JAXBException e) {
            return org.apache.commons.lang.StringUtils.EMPTY;
        } catch (UnsupportedEncodingException jex) {
            return org.apache.commons.lang.StringUtils.EMPTY;
        }
    }

    public static String bytesToHex(byte[] b) {
        char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        StringBuffer buf = new StringBuffer();
        for (byte element : b) {
            buf.append(hexDigit[element >> 4 & 0x0f]);
            buf.append(hexDigit[element & 0x0f]);
        }
        return buf.toString();
    }

    private oecd.standardauditfile_tax.pt_1.Customer convertCustomerToSAFTCustomer(Customer customer) {
        oecd.standardauditfile_tax.pt_1.Customer c = new oecd.standardauditfile_tax.pt_1.Customer();

        // AccountID
        /*
         * Deve ser indicada a respectiva conta corrente do cliente no plano de
         * contas da contabilidade, caso esteja definida. Caso contr?rio dever?
         * ser preenchido com a designa??o ?Desconhecido?.
         */

        if (customer.getCustomerType() != null) {
            c.setAccountID(customer.getCustomerType().getCode());
        } else {
            if (customer instanceof AdhocCustomer) {
                c.setAccountID("ADHOC");
            } else {
                c.setAccountID("STUDENT");
            }
        }

        // BillingAddress
        // List<PhysicalAddress> addresses = customer
        // .getPartyContacts(PhysicalAddress.class);
        // if (addresses.size() > 0) {
        // c.setBillingAddress(convertToSAFTAddressStructure(addresses.get(0)));
        // } else {
        // PhysicalAddress addr = new PhysicalAddress();
        c.setBillingAddress(convertAddressToSAFTAddress(customer.getCountryCode(), customer.getAddress(), customer.getZipCode(),
                customer.getDistrictSubdivision(), customer.getAddress()));
        // }

        // CompanyName
        c.setCompanyName(customer.getName());

        // Contact
        c.setContact(customer.getName());

        // CustomerID
        c.setCustomerID(customer.getCode());

        // CustomerTaxID
        if (Strings.isNullOrEmpty(customer.getFiscalNumber())) {
            c.setCustomerTaxID(Customer.DEFAULT_FISCAL_NUMBER);
        } else {
            c.setCustomerTaxID(customer.getFiscalNumber());
        }
        // Email
        // c.setEmail("");

        // Fax
        // c.setFax("");

        // SelfBillingIndicator
        /*
         * Indicador da exist?ncia de acordo de autofactura??o entre o cliente e
         * o fornecedor. Deve ser preenchido com ?1? se houver acordo e com ?0?
         * (zero) no caso contr?rio.
         */
        c.setSelfBillingIndicator(0);

        // Telephone
        // c.setTelephone("");

        // WebSite
        // c.setWebsite("");

        return c;
    }

    private AddressStructure convertAddressToSAFTAddress(String country, String addressDetail, String zipCode,
            String zipCodeRegion, String street) {
        AddressStructure companyAddress;
        companyAddress = new AddressStructure();
        companyAddress.setCountry(country);
        if (addressDetail != null) {
            companyAddress.setAddressDetail(Splitter.fixedLength(60).splitToList(addressDetail).get(0));
        } else {
            companyAddress.setAddressDetail(".");
        }
        if (zipCodeRegion != null) {
            companyAddress.setCity(Splitter.fixedLength(49).splitToList(zipCodeRegion).get(0));
        } else {
            companyAddress.setCity(".");
        }
        companyAddress.setPostalCode(zipCode);
        companyAddress.setRegion(zipCodeRegion);
        if (street != null) {
            companyAddress.setStreetName(Splitter.fixedLength(49).splitToList(street).get(0));
        } else {
            companyAddress.setStreetName(".");
        }
        return companyAddress;
    }

    private oecd.standardauditfile_tax.pt_1.Product convertProductToSAFTProduct(Product product) {
        oecd.standardauditfile_tax.pt_1.Product p = new oecd.standardauditfile_tax.pt_1.Product();

        // ProductCode
        p.setProductCode(product.getCode());

        // ProductDescription
        p.setProductDescription(product.getName().getContent());

        // ProductGroup
        if (product.getProductGroup() != null) {
            p.setProductGroup(product.getProductGroup().getName().getContent());
        }

        // ProductNumberCode
        p.setProductNumberCode(p.getProductCode());

        // ProductType
        /*
         * Deve ser preenchido com: ?P? ? produtos; ?S? ? servi?os; ?O? ? outros
         * (ex: portes debitados); ?I? ? impostos, taxas e encargos parafiscais
         * (excepto IVA e IS que dever?o ser reflectidos na tabela de impostos ?
         * TaxTable). Texto 1
         */
        p.setProductType("S");

        return p;
    }

    private MovementTax getSAFTMovementTax(Product product, Vat vat) {
        MovementTax tax = new MovementTax();

        tax.setTaxCode(vat.getVatType().getCode());

        tax.setTaxCountryRegion("PT");

        // Tax-TaxPercentage
        tax.setTaxPercentage(vat.getTaxRate());

        // Tax-TaxType
        tax.setTaxType(SAFTPTMovementTaxType.IVA);

        return tax;
    }

    private Tax getSAFTInvoiceTax(Product product, Vat vat) {
        Tax tax = new Tax();
        // Tax-TaxCode

        tax.setTaxCode(vat.getVatType().getCode());

        tax.setTaxCountryRegion("PT");

        // Tax-TaxPercentage
        tax.setTaxPercentage(vat.getTaxRate());

        // Tax-TaxType
        tax.setTaxType("IVA");

        return tax;
    }

    public static ERPExportOperation exportFullToIntegration(FinantialInstitution institution, DateTime fromDate,
            DateTime toDate, String username, Boolean includeMovements) {

        ERPExportOperation operation = createSaftExportOperation(null, institution, new DateTime());
        try {
            ERPExporter saftExporter = new ERPExporter();
            List<FinantialDocument> documents =
                    new ArrayList<FinantialDocument>(institution.getExportableDocuments(fromDate, toDate));
            logger.info("Collecting " + documents.size() + " documents to export to institution " + institution.getCode());
            UnaryOperator<AuditFile> auditFilePreProcess =
                    institution.getErpIntegrationConfiguration().getAuditFilePreProcessOperator();
            String xml = saftExporter.generateERPFile(institution, fromDate, toDate, documents, true, true, auditFilePreProcess);

            writeContentToExportOperation(xml, operation);

            boolean success = sendDocumentsInformationToIntegration(institution, operation);

            operation.getFinantialDocumentsSet().addAll(documents);
            operation.setSuccess(success);

        } catch (Throwable t) {
            writeError(operation, t);
        }
        return operation;
    }

    public static void requestPendingDocumentStatus(FinantialInstitution institution) {
        ERPConfiguration erpIntegrationConfiguration = institution.getErpIntegrationConfiguration();
        if (erpIntegrationConfiguration == null) {
            throw new TreasuryDomainException("error.ERPExporter.invalid.erp.configuration");
        }

        if (erpIntegrationConfiguration.getActive() == false) {
            return;
        }
        IERPExternalService service = erpIntegrationConfiguration.getERPExternalServiceImplementation();

        List<String> documentNumbers =
                institution.getFinantialDocumentsPendingForExportationSet().stream().map(doc -> doc.getUiDocumentNumber())
                        .collect(Collectors.toList());
        List<DocumentStatusWS> integrationStatusFor =
                service.getIntegrationStatusFor(institution.getFiscalNumber(), documentNumbers);
        for (DocumentStatusWS documentStatus : integrationStatusFor) {
            if (documentStatus.isIntegratedWithSuccess()) {
//                operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE, "info.ERPExporter.sucess.integrating.document",
//                        documentStatus.getDocumentNumber()));
                FinantialDocument document =
                        institution.getFinantialDocumentsPendingForExportationSet().stream()
                                .filter(x -> x.getUiDocumentNumber().equals(documentStatus.getDocumentNumber())).findFirst()
                                .orElse(null);
                if (document != null) {
                    document.clearDocumentToExport();
                } else {
//                    success = false;
//                    operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE, "info.ERPExporter.error.integrating.document",
//                            documentStatus.getDocumentNumber(), documentStatus.getErrorDescription()));
//                    operation.appendErrorLog(BundleUtil.getString(Constants.BUNDLE,
//                            "info.ERPExporter.error.integrating.document", documentStatus.getDocumentNumber(),
//                            documentStatus.getErrorDescription()));
                }
            } else {
//                success = false;
//                operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE, "info.ERPExporter.error.integrating.document",
//                        documentStatus.getDocumentNumber(), documentStatus.getErrorDescription()));
//                operation.appendErrorLog(BundleUtil.getString(Constants.BUNDLE, "info.ERPExporter.error.integrating.document",
//                        documentStatus.getDocumentNumber(), documentStatus.getErrorDescription()));
            }
        }
//        success = false;
//        operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE, "info.ERPExporter.error.integrating", "",
//                "no.documents.response"));
//        operation.appendErrorLog(BundleUtil.getString(Constants.BUNDLE, "info.ERPExporter.error.integrating", "",
//                "no.documents.response"));

    }

    private static boolean sendDocumentsInformationToIntegration(FinantialInstitution institution, ERPExportOperation operation)
            throws MalformedURLException {
        boolean success = true;
        ERPConfiguration erpIntegrationConfiguration = institution.getErpIntegrationConfiguration();
        if (erpIntegrationConfiguration == null) {
            throw new TreasuryDomainException("error.ERPExporter.invalid.erp.configuration");
        }

        if (erpIntegrationConfiguration.getActive() == false) {
            operation.appendErrorLog(BundleUtil.getString(Constants.BUNDLE, "info.ERPExporter.configuration.inactive"));
            return false;
        }
        IERPExternalService service = erpIntegrationConfiguration.getERPExternalServiceImplementation();
        operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE, "info.ERPExporter.sending.inforation"));
        DocumentsInformationInput input = new DocumentsInformationInput();
        if (operation.getFile().getSize() <= erpIntegrationConfiguration.getMaxSizeBytesToExportOnline()) {
            input.setData(operation.getFile().getContent());
            DocumentsInformationOutput sendInfoOnlineResult = service.sendInfoOnline(input);
            operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE, "info.ERPExporter.sucess.sending.inforation.online",
                    sendInfoOnlineResult.getRequestId()));

            //if we have result in online situation, then check the information of integration STATUS
            for (DocumentStatusWS status : sendInfoOnlineResult.getDocumentStatus()) {
                if (status.isIntegratedWithSuccess()) {

                    FinantialDocument document =
                            FinantialDocument.findByUiDocumentNumber(institution, status.getDocumentNumber());
                    if (document != null) {
                        operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE,
                                "info.ERPExporter.sucess.integrating.document", document.getUiDocumentNumber()));
                        document.clearDocumentToExport();
                    } else {
                        success = false;
                        operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE,
                                "info.ERPExporter.error.integrating.document", status.getDocumentNumber(),
                                status.getErrorDescription()));
                        operation.appendErrorLog(BundleUtil.getString(Constants.BUNDLE,
                                "info.ERPExporter.error.integrating.document", status.getDocumentNumber(),
                                status.getErrorDescription()));
                    }
                } else {
                    success = false;
                    operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE, "info.ERPExporter.error.integrating.document",
                            status.getDocumentNumber(), status.getErrorDescription()));
                    operation.appendErrorLog(BundleUtil.getString(Constants.BUNDLE,
                            "info.ERPExporter.error.integrating.document", status.getDocumentNumber(),
                            status.getErrorDescription()));

                }
//                }
            }
        } else {
            try {
                String sharedURI = "";
                if (institution.getErpIntegrationConfiguration().getExternalURL().startsWith("file://")) {
                    sharedURI =
                            institution.getErpIntegrationConfiguration().getExternalURL() + "\\"
                                    + operation.getFile().getFilename();
                    institution.getErpIntegrationConfiguration().getExternalURL();
                    File destFile = new File(sharedURI);

                    com.google.common.io.Files.write(operation.getFile().getContent(), destFile);

                } else //ftp or else
                {
                    throw new TreasuryDomainException("error.ERPExporter.noExternalURL.defined.in.erpintegrationconfiguration");
                }

                input.setDataURI(sharedURI);
                String sendInfoOnlineResult = service.sendInfoOffline(input);
                operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE,
                        "info.ERPExporter.sucess.sending.inforation.offline", sendInfoOnlineResult));
                operation.appendInfoLog("#" + sendInfoOnlineResult);
            } catch (IOException e) {
                success = false;
                operation.appendErrorLog(e.getLocalizedMessage());
            }
        }
        return success;
    }

    private static void writeError(ERPExportOperation operation, Throwable t) {
        StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out);
        t.printStackTrace(writer);
        operation.setProcessed(true);
        operation.appendErrorLog(out.toString());
    }

    @Atomic(mode = TxMode.WRITE)
    private static ERPExportOperation createSaftExportOperation(byte[] data, FinantialInstitution institution, DateTime when) {
        String filename = institution.getFiscalNumber() + "_" + when.toString() + ".xml";
        ERPExportOperation operation = ERPExportOperation.create(data, filename, institution, when, false, false, false, null);
        return operation;
    }

    @Atomic
    private static void writeContentToExportOperation(String content, ERPExportOperation operation) {
        byte[] bytes = null;
        try {
            bytes = content.getBytes("Windows-1252");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String fileName =
                operation.getFinantialInstitution().getFiscalNumber() + "_"
                        + operation.getExecutionDate().toString("ddMMyyyy_hhmm") + ".xml";
        OperationFile binaryStream = new OperationFile(fileName, bytes);
        if (operation.getFile() != null) {
            operation.getFile().delete();
        }
        operation.setFile(binaryStream);
    }

    public static String exportFinantialDocumentToXML(FinantialInstitution finantialInstitution, List<FinantialDocument> documents) {
        return exportFinantialDocumentToXML(finantialInstitution, documents, null);
    }

    private static String exportFinantialDocumentToXML(FinantialInstitution finantialInstitution,
            List<FinantialDocument> documents, UnaryOperator<AuditFile> preProcessFunctionBeforeSerialize) {
//        documents.forEach(x -> {
//            if (x instanceof Invoice) {
//                ((Invoice) x).recalculateAmountValues();
//            }
//        });
        ERPExporter saftExporter = new ERPExporter();
        DateTime beginDate =
                documents.stream().min((x, y) -> x.getDocumentDate().compareTo(y.getDocumentDate())).get().getDocumentDate();
        DateTime endDate =
                documents.stream().max((x, y) -> x.getDocumentDate().compareTo(y.getDocumentDate())).get().getDocumentDate();
        return saftExporter.generateERPFile(finantialInstitution, beginDate, endDate, documents, false, false,
                preProcessFunctionBeforeSerialize);
    }

    public static String exportsProductsToXML(FinantialInstitution finantialInstitution) {
        return exportsProductsToXML(finantialInstitution, null);
    }

    protected static String exportsProductsToXML(FinantialInstitution finantialInstitution,
            UnaryOperator<AuditFile> preProcessFunctionBeforeSerialize) {
        ERPExporter saftExporter = new ERPExporter();
        return saftExporter.generateERPFile(finantialInstitution, new DateTime(), new DateTime(),
                new ArrayList<FinantialDocument>(), false, true, preProcessFunctionBeforeSerialize);
    }

    public static String exportsCustomersToXML(FinantialInstitution finantialInstitution) {
        return exportCustomersToXML(finantialInstitution, null);
    }

    protected static String exportCustomersToXML(FinantialInstitution finantialInstitution,
            UnaryOperator<AuditFile> preProcessFunctionBeforeSerialize) {
        ERPExporter saftExporter = new ERPExporter();
        return saftExporter.generateERPFile(finantialInstitution, new DateTime(), new DateTime(),
                new ArrayList<FinantialDocument>(), true, false, preProcessFunctionBeforeSerialize);

    }

    @Atomic(mode = TxMode.WRITE)
    public static ERPExportOperation exportFinantialDocumentToIntegration(FinantialInstitution institution,
            List<FinantialDocument> documents) {

        ERPExportOperation operation = createSaftExportOperation(null, institution, new DateTime());
        documents.forEach(document -> operation.addFinantialDocuments(document));
        try {
            operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE,
                    "label.ERPExporter.starting.finantialdocuments.integration"));
            UnaryOperator<AuditFile> preProcessFunctionBeforeSerialize =
                    institution.getErpIntegrationConfiguration().getAuditFilePreProcessOperator();

            String xml = exportFinantialDocumentToXML(institution, documents, preProcessFunctionBeforeSerialize);
            operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE, "label.ERPExporter.erp.xml.content.generated"));

            writeContentToExportOperation(xml, operation);

            boolean success = sendDocumentsInformationToIntegration(institution, operation);
            operation.getFinantialDocumentsSet().addAll(documents);
            operation.setSuccess(success);
            operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE,
                    "label.ERPExporter.finished.finantialdocuments.integration"));

        } catch (Exception ex) {
            writeError(operation, ex);
        }
        return operation;
    }

    @Atomic(mode = TxMode.WRITE)
    public static ERPExportOperation exportCustomersToIntegration(FinantialInstitution institution) {

        ERPExportOperation operation = createSaftExportOperation(null, institution, new DateTime());
        try {
            operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE, "label.ERPExporter.starting.customers.integration"));

            UnaryOperator<AuditFile> preProcessFunctionBeforeSerialize =
                    institution.getErpIntegrationConfiguration().getAuditFilePreProcessOperator();
            String xml = exportCustomersToXML(institution, preProcessFunctionBeforeSerialize);
            operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE, "label.ERPExporter.erp.xml.content.generated"));

            writeContentToExportOperation(xml, operation);

            boolean success = sendDocumentsInformationToIntegration(institution, operation);
            operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE, "label.ERPExporter.finished.customers.integration"));

            operation.setSuccess(success);
        } catch (Exception ex) {
            writeError(operation, ex);
        }
        return operation;
    }

    @Atomic(mode = TxMode.WRITE)
    public static ERPExportOperation exportProductsToIntegration(FinantialInstitution institution) {

        ERPExportOperation operation = createSaftExportOperation(null, institution, new DateTime());
        try {
            UnaryOperator<AuditFile> preProcessFunctionBeforeSerialize =
                    institution.getErpIntegrationConfiguration().getAuditFilePreProcessOperator();
            operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE, "label.ERPExporter.starting.products.integration"));

            String xml = exportsProductsToXML(institution, preProcessFunctionBeforeSerialize);
            operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE, "label.ERPExporter.erp.xml.content.generated"));

            writeContentToExportOperation(xml, operation);

            boolean success = sendDocumentsInformationToIntegration(institution, operation);
            operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE, "label.ERPExporter.finished.products.integration"));

            operation.setSuccess(success);
        } catch (Exception ex) {
            writeError(operation, ex);
        }
        return operation;
    }

    @Atomic
    public static ERPExportOperation retryExportToIntegration(ERPExportOperation eRPExportOperation) {
        if (eRPExportOperation.getFinantialDocumentsSet().isEmpty()) {
            ERPExportOperation operation =
                    createSaftExportOperation(eRPExportOperation.getFile().getContent(),
                            eRPExportOperation.getFinantialInstitution(), new DateTime());
            try {
                operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE, "label.ERPExporter.starting.retry.integration"));
                for (FinantialDocument document : eRPExportOperation.getFinantialDocumentsSet()) {
                    operation.addFinantialDocuments(document);
                }
                boolean success = sendDocumentsInformationToIntegration(eRPExportOperation.getFinantialInstitution(), operation);
                operation.setSuccess(success);
            } catch (Exception ex) {
                writeError(operation, ex);
            }
            operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE, "label.ERPExporter.finished.retry.integration"));
            return operation;

        } else {
            List<FinantialDocument> allDocuments = new ArrayList(eRPExportOperation.getFinantialDocumentsSet());
            ERPExportOperation operation =
                    ERPExporter.exportFinantialDocumentToIntegration(eRPExportOperation.getFinantialInstitution(), allDocuments);
            operation.appendInfoLog(BundleUtil.getString(Constants.BUNDLE, "label.ERPExporter.finished.retry.integration"));
            return operation;
        }
    }

    public static void testExportToIntegration(FinantialInstitution institution) {
        ERPConfiguration erpIntegrationConfiguration = institution.getErpIntegrationConfiguration();
        if (erpIntegrationConfiguration == null) {
            throw new TreasuryDomainException("error.ERPExporter.invalid.erp.configuration");
        }
        IERPExternalService service = erpIntegrationConfiguration.getERPExternalServiceImplementation();
    }

    public static void checkIntegrationDocumentStatus(FinantialDocument document) {
        ERPConfiguration erpIntegrationConfiguration =
                document.getDebtAccount().getFinantialInstitution().getErpIntegrationConfiguration();
        if (erpIntegrationConfiguration == null) {
            throw new TreasuryDomainException("error.ERPExporter.invalid.erp.configuration");
        }

        if (erpIntegrationConfiguration.getActive() == false) {
            return;
        }
        IERPExternalService service = erpIntegrationConfiguration.getERPExternalServiceImplementation();
        List<String> documentsList = new ArrayList<String>();
        documentsList.add(document.getUiDocumentNumber());
        documentsList.add(document.getUiDocumentNumber());
        documentsList.add(document.getUiDocumentNumber());
        List<DocumentStatusWS> integrationStatusFor =
                service.getIntegrationStatusFor(document.getDebtAccount().getFinantialInstitution().getFiscalNumber(),
                        documentsList);
        for (DocumentStatusWS documentStatus : integrationStatusFor) {
            if (documentStatus.getDocumentNumber().equals(document.getUiDocumentNumber())
                    && documentStatus.isIntegratedWithSuccess()) {
                document.clearDocumentToExport();
            } else {
            }
        }
    }
}
