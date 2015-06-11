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
package org.fenixedu.treasury.ui.administration.managefinantialinstitution;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.StringNormalizer;
import org.fenixedu.treasury.domain.Currency;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.FiscalCountryRegion;
import org.fenixedu.treasury.domain.document.DocumentNumberSeries;
import org.fenixedu.treasury.domain.document.FinantialDocumentType;
import org.fenixedu.treasury.domain.document.TreasuryDocumentTemplateFile;
import org.fenixedu.treasury.domain.integration.ERPConfiguration;
import org.fenixedu.treasury.services.integration.erp.ERPExporter;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.fenixedu.treasury.ui.TreasuryController;
import org.fenixedu.treasury.ui.integration.erp.ERPConfigurationController;
import org.fenixedu.treasury.util.Constants;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;
import pt.ist.standards.geographic.Country;
import pt.ist.standards.geographic.District;
import pt.ist.standards.geographic.GeographicInfoLoader;
import pt.ist.standards.geographic.Municipality;

//@Component("org.fenixedu.treasury.ui.administration.manageFinantialInstitution") <-- Use for duplicate controller name disambiguation
@SpringFunctionality(app = TreasuryController.class, title = "label.title.administration.manageFinantialInstitution",
        accessGroup = "#managers")
// CHANGE_ME accessGroup = "group1 | group2 | groupXPTO"
//or
//@BennuSpringController(value = TreasuryController.class)
@RequestMapping(FinantialInstitutionController.CONTROLLER_URL)
public class FinantialInstitutionController extends TreasuryBaseController {
    public static final String CONTROLLER_URL = "/treasury/administration/managefinantialinstitution/finantialinstitution";
    private static final String SEARCH_URI = "/";
    public static final String SEARCH_URL = CONTROLLER_URL + SEARCH_URI;
    private static final String UPDATE_URI = "/update/";
    public static final String UPDATE_URL = CONTROLLER_URL + UPDATE_URI;
    private static final String CREATE_URI = "/create";
    public static final String CREATE_URL = CONTROLLER_URL + CREATE_URI;
    private static final String READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + READ_URI;
    private static final String DELETE_URI = "/delete/";
    public static final String DELETE_URL = CONTROLLER_URL + DELETE_URI;

//

    @RequestMapping
    public String home(Model model) {
        //this is the default behavior, for handling in a Spring Functionality
        return "forward:/treasury/administration/managefinantialinstitution/finantialinstitution/";
    }

    private FinantialInstitution getFinantialInstitution(Model m) {
        return (FinantialInstitution) m.asMap().get("finantialInstitution");
    }

    private void setFinantialInstitution(FinantialInstitution finantialInstitution, Model m) {
        m.addAttribute("finantialInstitution", finantialInstitution);
    }

    @Atomic
    public void deleteFinantialInstitution(FinantialInstitution finantialInstitution) {
        finantialInstitution.delete();
    }

    @RequestMapping(value = SEARCH_URI)
    public String search(@RequestParam(value = "code", required = false) String code, @RequestParam(value = "fiscalnumber",
            required = false) String fiscalNumber, @RequestParam(value = "companyid", required = false) String companyId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "companyname", required = false) String companyName, @RequestParam(value = "address",
                    required = false) String address, @RequestParam(value = "country", required = false) Country country,
            @RequestParam(value = "district", required = false) District district, @RequestParam(value = "municipality",
                    required = false) Municipality municipality,
            @RequestParam(value = "locality", required = false) String locality, @RequestParam(value = "zipcode",
                    required = false) String zipCode, Model model) {
        List<FinantialInstitution> searchfinantialinstitutionResultsDataSet =
                getSearchUniverseSearchFinantialInstitutionDataSet();

        //add the results dataSet to the model
        model.addAttribute("searchfinantialinstitutionResultsDataSet", searchfinantialinstitutionResultsDataSet);
        return "treasury/administration/managefinantialinstitution/finantialinstitution/search";
    }

    private List<FinantialInstitution> getSearchUniverseSearchFinantialInstitutionDataSet() {
        return new ArrayList<FinantialInstitution>(Bennu.getInstance().getFinantialInstitutionsSet());
    }

    @RequestMapping(value = "/search/view/{oid}")
    public String processSearchToViewAction(@PathVariable("oid") FinantialInstitution finantialInstitution, Model model) {
        return "redirect:/treasury/administration/managefinantialinstitution/finantialinstitution/read" + "/"
                + finantialInstitution.getExternalId();
    }

    @RequestMapping(value = READ_URI + "{oid}")
    public String read(@PathVariable("oid") FinantialInstitution finantialInstitution, Model model) {
        setFinantialInstitution(finantialInstitution, model);
        model.addAttribute("finantialDocumentTypeSet", FinantialDocumentType.findAll().collect(Collectors.toList()));
        model.addAttribute("allowedFileType", TreasuryDocumentTemplateFile.CONTENT_TYPE);
        return "treasury/administration/managefinantialinstitution/finantialinstitution/read";
    }

    @RequestMapping(value = DELETE_URI + "{oid}", method = RequestMethod.POST)
    public String delete(@PathVariable("oid") FinantialInstitution finantialInstitution, Model model,
            RedirectAttributes redirectAttributes) {

        setFinantialInstitution(finantialInstitution, model);
        try {
            deleteFinantialInstitution(finantialInstitution);

            addInfoMessage(BundleUtil.getString(Constants.BUNDLE, "label.success.delete"), model);
            return redirect(SEARCH_URL, model, redirectAttributes);
        } catch (Exception ex) {
            addErrorMessage("Error deleting the FinantialInstitution due to " + ex.getLocalizedMessage(), model);
        }

        //The default mapping is the same Read View
        return redirect(READ_URL + getFinantialInstitution(model).getExternalId(), model, redirectAttributes);
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.GET)
    public String create(Model model) {
        return _create(null, null, null, null, null, null, null, null, null, null, null, model);
    }

    @RequestMapping(value = "/createpostback", method = RequestMethod.POST)
    public String createpostback(@RequestParam(value = "code", required = false) String code, @RequestParam(
            value = "fiscalnumber", required = false) String fiscalNumber,
            @RequestParam(value = "companyid", required = false) String companyId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "companyname", required = false) String companyName, @RequestParam(value = "address",
                    required = false) String address, @RequestParam(value = "country", required = false) Country country,
            @RequestParam(value = "district", required = false) District district, @RequestParam(value = "municipality",
                    required = false) Municipality municipality,
            @RequestParam(value = "locality", required = false) String locality, @RequestParam(value = "zipcode",
                    required = false) String zipCode, Model model) {
        return _create(code, fiscalNumber, companyId, name, companyName, address, country, district, municipality, locality,
                zipCode, model);
    }

    public String _create(@RequestParam(value = "code", required = false) String code, @RequestParam(value = "fiscalnumber",
            required = false) String fiscalNumber, @RequestParam(value = "companyid", required = false) String companyId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "companyname", required = false) String companyName, @RequestParam(value = "address",
                    required = false) String address, @RequestParam(value = "country", required = false) Country country,
            @RequestParam(value = "district", required = false) District district, @RequestParam(value = "municipality",
                    required = false) Municipality municipality,
            @RequestParam(value = "locality", required = false) String locality, @RequestParam(value = "zipcode",
                    required = false) String zipCode, Model model) {
        model.addAttribute("finantialInstitution_fiscalCountryRegion_options",
                FiscalCountryRegion.findAll().collect(Collectors.toList()));
        model.addAttribute("finantialInstitution_currency_options", Currency.findAll().collect(Collectors.toList()));
        model.addAttribute("finantialInstitution_country_options",
                GeographicInfoLoader.getInstance().findAllCountries().collect(Collectors.toList()));
        model.addAttribute("finantialInstitution_district_options", country != null ? country.getPlaces() : new HashSet<>());
        model.addAttribute("finantialInstitution_municipality_options", district != null ? district.getPlaces() : new HashSet<>());
        return "treasury/administration/managefinantialinstitution/finantialinstitution/create";
    }

    @RequestMapping(value = CREATE_URI, method = RequestMethod.POST)
    public String create(@RequestParam(value = "fiscalcountryregion", required = false) FiscalCountryRegion fiscalCountryRegion,
            @RequestParam(value = "currency", required = true) Currency currency,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "fiscalnumber", required = false) String fiscalNumber, @RequestParam(value = "companyid",
                    required = false) String companyId, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "companyname", required = false) String companyName, @RequestParam(value = "address",
                    required = false) String address, @RequestParam(value = "country", required = false) Country country,
            @RequestParam(value = "district", required = false) District district, @RequestParam(value = "municipality",
                    required = false) Municipality municipality,
            @RequestParam(value = "locality", required = false) String locality, @RequestParam(value = "zipcode",
                    required = false) String zipCode, Model model) {
        try {
            FinantialInstitution finantialInstitution =
                    createFinantialInstitution(fiscalCountryRegion, currency, code, fiscalNumber, companyId, name, companyName,
                            address, country, district, municipality, locality, zipCode);
            //Add the bean to be used in the View
            model.addAttribute("finantialInstitution", finantialInstitution);
            addInfoMessage("Sucess creating FinantialInstitution ...", model);
            return "redirect:/treasury/administration/managefinantialinstitution/finantialinstitution/read/"
                    + getFinantialInstitution(model).getExternalId();
        } catch (DomainException ex) {
            //Add error messages to the list
            addErrorMessage("Error creating the FinantialInstitution due to " + ex.getLocalizedMessage(), model);
        }
        return create(model);
    }

    @Atomic
    public FinantialInstitution createFinantialInstitution(FiscalCountryRegion fiscalCountryRegion, Currency currency,
            String code, String fiscalNumber, String companyId, String name, String companyName, String address, Country country,
            District district, Municipality municipality, String locality, String zipCode) {
        FinantialInstitution finantialInstitution =
                FinantialInstitution.create(fiscalCountryRegion, currency, code, fiscalNumber, companyId, name, companyName,
                        address, country, district, municipality, locality, zipCode);
        return finantialInstitution;
    }

//				
    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.GET)
    public String update(@PathVariable("oid") FinantialInstitution finantialInstitution, Model model) {
        return _update(finantialInstitution, finantialInstitution.getCode(), finantialInstitution.getFiscalNumber(),
                finantialInstitution.getCompanyId(), finantialInstitution.getName(), finantialInstitution.getCompanyName(),
                finantialInstitution.getAddress(), finantialInstitution.getCountry(), finantialInstitution.getDistrict(),
                finantialInstitution.getMunicipality(), finantialInstitution.getLocality(), finantialInstitution.getZipCode(),
                model);
    }

    @RequestMapping(value = "/updatepostback/{oid}", method = RequestMethod.POST)
    public String updatepostback(@PathVariable("oid") FinantialInstitution finantialInstitution, @RequestParam(value = "code",
            required = false) String code, @RequestParam(value = "fiscalnumber", required = false) String fiscalNumber,
            @RequestParam(value = "companyid", required = false) String companyId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "companyname", required = false) String companyName, @RequestParam(value = "address",
                    required = false) String address, @RequestParam(value = "country", required = false) Country country,
            @RequestParam(value = "district", required = false) District district, @RequestParam(value = "municipality",
                    required = false) Municipality municipality,
            @RequestParam(value = "locality", required = false) String locality, @RequestParam(value = "zipcode",
                    required = false) String zipCode, Model model) {
        return _update(finantialInstitution, code, fiscalNumber, companyId, name, companyName, address, country, district,
                municipality, locality, zipCode, model);
    }

    public String _update(FinantialInstitution finantialInstitution, String code, String fiscalNumber, String companyId,
            String name, String companyName, String address, Country country, District district, Municipality municipality,
            String locality, String zipCode, Model model) {
        setFinantialInstitution(finantialInstitution, model);
        model.addAttribute("finantialInstitution_fiscalCountryRegion_options",
                FiscalCountryRegion.findAll().collect(Collectors.toList()));
        model.addAttribute("finantialInstitution_currency_options", Currency.findAll().collect(Collectors.toList()));
        model.addAttribute("finantialInstitution_country_options",
                GeographicInfoLoader.getInstance().findAllCountries().collect(Collectors.toList()));
        model.addAttribute("finantialInstitution_district_options", country != null ? country.getPlaces() : new HashSet<>());
        model.addAttribute("finantialInstitution_municipality_options", district != null ? district.getPlaces() : new HashSet<>());
        return "treasury/administration/managefinantialinstitution/finantialinstitution/update";
    }

//				
    @RequestMapping(value = UPDATE_URI + "{oid}", method = RequestMethod.POST)
    public String update(@PathVariable("oid") FinantialInstitution finantialInstitution, @RequestParam(
            value = "fiscalcountryregion", required = false) FiscalCountryRegion fiscalCountryRegion, @RequestParam(
            value = "currency", required = false) Currency currency, @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "fiscalnumber", required = false) String fiscalNumber, @RequestParam(value = "companyid",
                    required = false) String companyId, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "companyname", required = false) String companyName, @RequestParam(value = "address",
                    required = false) String address, @RequestParam(value = "country", required = false) Country country,
            @RequestParam(value = "district", required = false) District district, @RequestParam(value = "municipality",
                    required = false) Municipality municipality,
            @RequestParam(value = "locality", required = false) String locality, @RequestParam(value = "zipcode",
                    required = false) String zipCode, Model model) {

        setFinantialInstitution(finantialInstitution, model);

        try {
            updateFinantialInstitution(fiscalCountryRegion, currency, code, fiscalNumber, companyId, name, companyName, address,
                    country, district, municipality, locality, zipCode, model);

            addInfoMessage("Sucess updating FinantialInstitution ...", model);
            return "redirect:/treasury/administration/managefinantialinstitution/finantialinstitution/read/"
                    + getFinantialInstitution(model).getExternalId();
        } catch (DomainException ex) {
            //Add error messages to the list
            addErrorMessage("Error creating the FinantialInstitution due to " + ex.getLocalizedMessage(), model);
        }

        return update(finantialInstitution, model);
    }

    @Atomic
    public void updateFinantialInstitution(FiscalCountryRegion fiscalCountryRegion, Currency currency, String code,
            String fiscalNumber, String companyId, String name, String companyName, String address, Country country,
            District district, Municipality municipality, String locality, String zipCode, Model m) {

        getFinantialInstitution(m).edit(fiscalCountryRegion, currency, code, fiscalNumber, companyId, name, companyName, address,
                country, district, municipality, locality, zipCode);
    }

    @RequestMapping(value = "/read/{oid}/exportproductsintegrationfile", produces = "text/xml;charset=Windows-1252")
    public void processReadToExportProductIntegrationFile(@PathVariable("oid") FinantialInstitution finantialInstitution,
            Model model, RedirectAttributes redirectAttributes, HttpServletResponse response) {
        try {
            String output = ERPExporter.exportsProducts(finantialInstitution);
            response.setContentType("text/xml");
            response.setCharacterEncoding("Windows-1252");
            String filename =
                    URLEncoder.encode(
                            StringNormalizer
                                    .normalizePreservingCapitalizedLetters(
                                            "ERP_PRODUCTS_" + finantialInstitution.getFiscalNumber() + ".xml")
                                    .replaceAll("\\s", "_").replace(" ", "_"), "Windows-1252");
            response.setHeader("Content-disposition", "attachment; filename=" + filename);
            response.getOutputStream().write(output.getBytes("Windows-1252"));
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            try {
                response.sendRedirect(redirect(READ_URL + finantialInstitution.getExternalId(), model, redirectAttributes));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "/read/{oid}/exportcustomersintegrationfile", produces = "text/xml;charset=Windows-1252")
    public void processReadToExportCustomerIntegrationFile(@PathVariable("oid") FinantialInstitution finantialInstitution,
            Model model, RedirectAttributes redirectAttributes, HttpServletResponse response) {
        try {
            String output = ERPExporter.exportsCustomers(finantialInstitution);
            response.setContentType("text/xml");
            response.setCharacterEncoding("Windows-1252");
            String filename =
                    URLEncoder.encode(
                            StringNormalizer
                                    .normalizePreservingCapitalizedLetters(
                                            "ERP_CUSTOMERS_" + finantialInstitution.getFiscalNumber() + ".xml")
                                    .replaceAll("\\s", "_").replace(" ", "_"), "Windows-1252");
            response.setHeader("Content-disposition", "attachment; filename=" + filename);
            response.getOutputStream().write(output.getBytes("Windows-1252"));
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            try {
                response.sendRedirect(redirect(READ_URL + finantialInstitution.getExternalId(), model, redirectAttributes));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "/read/{oid}/erpconfigurationupdate")
    public String processReadToERPConfigurationUpdate(@PathVariable("oid") FinantialInstitution finantialInstitution,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            if (finantialInstitution.getErpIntegrationConfiguration() == null) {
                DocumentNumberSeries paymentsIntegrationSeries =
                        DocumentNumberSeries.find(FinantialDocumentType.findForSettlementNote(), finantialInstitution)
                                .filter(x -> x.getSeries().getExternSeries() == true).findFirst().orElse(null);
                ERPConfiguration erpIntegrationConfiguration =
                        ERPConfiguration.create(paymentsIntegrationSeries.getSeries(), finantialInstitution, "", "", "", "");
                finantialInstitution.setErpIntegrationConfiguration(erpIntegrationConfiguration);
            }
            return redirect(ERPConfigurationController.READ_URL
                    + finantialInstitution.getErpIntegrationConfiguration().getExternalId(), model, redirectAttributes);
        } catch (Exception ex) {
            addErrorMessage(ex.getLocalizedMessage(), model);
            return read(finantialInstitution, model);
        }
    }
}
