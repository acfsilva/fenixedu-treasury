/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and ServiÃ§os Partilhados da
 * Universidade de Lisboa:
 *  - Copyright Â© 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright Â© 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: ricardo.pedro@qub-it.com, anil.mamede@qub-it.com
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
package org.fenixedu.treasury.ui.administration.payments.sibs.managesibsreportfile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.domain.paymentcodes.SibsReportFile;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.TreasuryController;
import org.fenixedu.treasury.util.Constants;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;

//@Component("org.fenixedu.treasury.ui.administration.payments.sibs.manageSibsReportFile") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.administration.payments.sibs.manageSibsReportFile",
        accessGroup = "treasuryBackOffice")
@RequestMapping(SibsReportFileController.CONTROLLER_URL)
public class SibsReportFileController extends TreasuryBaseController {

    public static final String CONTROLLER_URL = "/treasury/administration/payments/sibs/managesibsreportfile/sibsreportfile";

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URL + "/";
    }

    private SibsReportFile getSibsReportFile(Model model) {
        return (SibsReportFile) model.asMap().get("sibsReportFile");
    }

    private void setSibsReportFile(SibsReportFile sibsReportFile, Model model) {
        model.addAttribute("sibsReportFile", sibsReportFile);
    }

    @Atomic
    public void deleteSibsReportFile(SibsReportFile sibsReportFile) {
        sibsReportFile.delete();
    }

    private static final String _SEARCH_URI = "/";
    public static final String SEARCH_URL = CONTROLLER_URL + _SEARCH_URI;

    @RequestMapping(value = _SEARCH_URI)
    public String search(
            @RequestParam(value = "whenprocessedbysibs", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate whenProcessedBySibs,
            @RequestParam(value = "transactionstotalamount", required = false) BigDecimal transactionsTotalAmount, @RequestParam(
                    value = "totalcost", required = false) BigDecimal totalCost, Model model) {
        List<SibsReportFile> searchsibsreportfileResultsDataSet =
                filterSearchSibsReportFile(whenProcessedBySibs, transactionsTotalAmount, totalCost);

        model.addAttribute("searchsibsreportfileResultsDataSet", searchsibsreportfileResultsDataSet);
        return "treasury/administration/payments/sibs/managesibsreportfile/sibsreportfile/search";
    }

    private Stream<SibsReportFile> getSearchUniverseSearchSibsReportFileDataSet() {
        return SibsReportFile.findAll();
    }

    private List<SibsReportFile> filterSearchSibsReportFile(org.joda.time.LocalDate whenProcessedBySibs,
            java.math.BigDecimal transactionsTotalAmount, java.math.BigDecimal totalCost) {

        return getSearchUniverseSearchSibsReportFileDataSet()
                .filter(sibsReportFile -> whenProcessedBySibs == null
                        || whenProcessedBySibs.equals(sibsReportFile.getWhenProcessedBySibs()))
                .filter(sibsReportFile -> transactionsTotalAmount == null
                        || transactionsTotalAmount.equals(sibsReportFile.getTransactionsTotalAmount()))
                .filter(sibsReportFile -> totalCost == null || totalCost.equals(sibsReportFile.getTotalCost()))
                .collect(Collectors.toList());
    }

    private static final String _SEARCH_TO_VIEW_ACTION_URI = "/search/view/";
    public static final String SEARCH_TO_VIEW_ACTION_URL = CONTROLLER_URL + _SEARCH_TO_VIEW_ACTION_URI;

    @RequestMapping(value = _SEARCH_TO_VIEW_ACTION_URI + "{oid}")
    public String processSearchToViewAction(@PathVariable("oid") SibsReportFile sibsReportFile, Model model,
            RedirectAttributes redirectAttributes) {
        return redirect(READ_URL + sibsReportFile.getExternalId(), model, redirectAttributes);
    }

    private static final String _READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + _READ_URI;

    @RequestMapping(value = _READ_URI + "{oid}")
    public String read(@PathVariable("oid") SibsReportFile sibsReportFile, Model model) {
        setSibsReportFile(sibsReportFile, model);
        return "treasury/administration/payments/sibs/managesibsreportfile/sibsreportfile/read";
    }

    private static final String _DELETE_URI = "/delete/";
    public static final String DELETE_URL = CONTROLLER_URL + _DELETE_URI;

    @RequestMapping(value = _DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") SibsReportFile sibsReportFile, Model model, RedirectAttributes redirectAttributes) {

        setSibsReportFile(sibsReportFile, model);
        try {
            assertUserIsFrontOfficeMember(model);

            deleteSibsReportFile(sibsReportFile);

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.delete"), model);
            return redirect(SEARCH_URL, model, redirectAttributes);
        } catch (TreasuryDomainException tex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + tex.getLocalizedMessage(), model);
        } catch (Exception ex) {
            addErrorMessage(BundleUtil.getString(Constants.BUNDLE, "label.error.delete") + ex.getLocalizedMessage(), model);
        }

        return redirect(READ_URL + getSibsReportFile(model).getExternalId(), model, redirectAttributes);
    }

    private static final String _DOWNLOAD_URI = "/read/download/";
    public static final String DOWNLOAD_URL = CONTROLLER_URL + _DOWNLOAD_URI;

    @RequestMapping(value = _DOWNLOAD_URI + "{oid}", method = RequestMethod.GET)
    public void processReadToDownloadFile(@PathVariable("oid") SibsReportFile sibsReportFile, Model model,
            RedirectAttributes redirectAttributes, HttpServletResponse response) {
        setSibsReportFile(sibsReportFile, model);
        try {
            assertUserIsFrontOfficeMember(model);

            response.setContentType(sibsReportFile.getContentType());
            String filename = sibsReportFile.getFilename();
            response.setHeader("Content-disposition", "attachment; filename=" + filename);
            response.getOutputStream().write(sibsReportFile.getContent());
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            try {
                response.sendRedirect(redirect(READ_URL + getSibsReportFile(model).getExternalId(), model, redirectAttributes));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
