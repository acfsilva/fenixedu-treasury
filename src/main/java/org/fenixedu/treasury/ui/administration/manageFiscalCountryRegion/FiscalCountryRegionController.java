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
package org.fenixedu.treasury.ui.administration.manageFiscalCountryRegion;

import java.util.ArrayList;
import java.util.List;

import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.treasury.domain.FiscalCountryRegion;
import org.fenixedu.treasury.ui.FenixeduTreasuryApplication;
import org.fenixedu.treasury.ui.TreasuryBaseController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Lists;

@SpringFunctionality(app = FenixeduTreasuryApplication.class, title = "label.title.manageFiscalCountryRegion", accessGroup = "anyone")
@RequestMapping("/administration/treasury/managefiscalcountryregion/fiscalcountryregion")
public class FiscalCountryRegionController extends TreasuryBaseController {

	@RequestMapping
	public String home(Model model) {
		return "forward:/administration/treasury/managefiscalcountryregion/fiscalcountryregion/";
	}

	private FiscalCountryRegion getFiscalCountryRegion(Model m) {
		return (FiscalCountryRegion) m.asMap().get("fiscalCountryRegion");
	}

	private void setFiscalCountryRegion(
			FiscalCountryRegion fiscalCountryRegion, Model m) {
		m.addAttribute("fiscalCountryRegion", fiscalCountryRegion);
	}

	public void deleteFiscalCountryRegion(
			FiscalCountryRegion fiscalCountryRegion) {
		fiscalCountryRegion.delete();
	}

	@RequestMapping(value = "/")
	public String search(Model model) {
		List<FiscalCountryRegion> searchfiscalcountryregionResultsDataSet = filterSearchFiscalCountryRegion();

		model.addAttribute("searchfiscalcountryregionResultsDataSet",
				searchfiscalcountryregionResultsDataSet);
		return "domain/managefiscalcountryregion/fiscalcountryregion/search";
	}

	private List<FiscalCountryRegion> getSearchUniverseSearchFiscalCountryRegionDataSet() {
		return new ArrayList<FiscalCountryRegion>(FiscalCountryRegion.readAll());
	}

	private List<FiscalCountryRegion> filterSearchFiscalCountryRegion() {
		return Lists.newArrayList(FiscalCountryRegion.readAll());
	}

	@RequestMapping(value = "/search/view/{oid}")
	public String processSearchToViewAction(
			@PathVariable("oid") FiscalCountryRegion fiscalCountryRegion,
			Model model, RedirectAttributes redirectAttributes) {

		return redirect("/administration/treasury/managefiscalcountryregion/fiscalcountryregion/read"
				+ "/" + fiscalCountryRegion.getExternalId(),model,redirectAttributes);
	}

	@RequestMapping(value = "/search/delete/{oid}")
	public String processSearchToDeleteAction(
			@PathVariable("oid") FiscalCountryRegion fiscalCountryRegion,
			Model model, RedirectAttributes redirectAttributes) {
		deleteFiscalCountryRegion(fiscalCountryRegion);
		return redirect("/administration/treasury/managefiscalcountryregion/fiscalcountryregion/",model,redirectAttributes);
	}

	@RequestMapping(value = "/read/{oid}")
	public String read(
			@PathVariable("oid") FiscalCountryRegion fiscalCountryRegion,
			Model model) {
		setFiscalCountryRegion(fiscalCountryRegion, model);
		return "domain/managefiscalcountryregion/fiscalcountryregion/read";
	}

	@RequestMapping(value = "/create", method = RequestMethod.GET)
	public String create(Model model) {
		return "domain/managefiscalcountryregion/fiscalcountryregion/create";
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public String create(
			@RequestParam(value = "regioncode", required = false) java.lang.String regionCode,
			@RequestParam(value = "name", required = false) org.fenixedu.commons.i18n.LocalizedString name,
			Model model, RedirectAttributes redirectAttributes) {
		/*
		 * Creation Logic
		 * 
		 * do something();
		 */

		FiscalCountryRegion fiscalCountryRegion = createFiscalCountryRegion(
				regionCode, name);

		/*
		 * Success Validation
		 */

		// Add the bean to be used in the View
		model.addAttribute("fiscalCountryRegion", fiscalCountryRegion);

		return redirect(
				"/administration/treasury/managefiscalcountryregion/fiscalcountryregion/read/"
						+ getFiscalCountryRegion(model).getExternalId(), model,
				redirectAttributes);

		/*
		 * If there is any error in validation
		 * 
		 * Add a error / warning message
		 * 
		 * addErrorMessage(" Error because ...",model);
		 * addWarningMessage(" Waring becaus ...",model);
		 * 
		 * 
		 * 
		 * return create(model);
		 */
	}

	public FiscalCountryRegion createFiscalCountryRegion(
			java.lang.String regionCode,
			org.fenixedu.commons.i18n.LocalizedString name) {
		/*
		 * Modify the creation code here if you do not want to create the object
		 * with the default constructor and use the setter for each field
		 */

		return FiscalCountryRegion.create(regionCode, name);
	}

	@RequestMapping(value = "/update/{oid}", method = RequestMethod.GET)
	public String update(
			@PathVariable("oid") FiscalCountryRegion fiscalCountryRegion,
			Model model) {
		setFiscalCountryRegion(fiscalCountryRegion, model);
		return "domain/managefiscalcountryregion/fiscalcountryregion/update";
	}

	@RequestMapping(value = "/update/{oid}", method = RequestMethod.POST)
	public String update(
			@PathVariable("oid") FiscalCountryRegion fiscalCountryRegion,
			@RequestParam(value = "regioncode", required = false) java.lang.String regionCode,
			@RequestParam(value = "name", required = false) org.fenixedu.commons.i18n.LocalizedString name,
			Model model, RedirectAttributes redirectAttributes) {

		setFiscalCountryRegion(fiscalCountryRegion, model);

		/*
		 * UpdateLogic here
		 * 
		 * do something();
		 */

		/*
		 * Succes Update
		 */
		updateFiscalCountryRegion(fiscalCountryRegion, regionCode, name);

		return redirect("/administration/treasury/managefiscalcountryregion/fiscalcountryregion/read/"
				+ getFiscalCountryRegion(model).getExternalId(),model,redirectAttributes);

		/*
		 * If there is any error in validation
		 * 
		 * Add a error / warning message
		 * 
		 * addErrorMessage(" Error because ...",model);
		 * addWarningMessage(" Waring becaus ...",model);
		 * 
		 * return update(fiscalCountryRegion,model);
		 */
	}

	public void updateFiscalCountryRegion(
			FiscalCountryRegion fiscalCountryRegion, String regionCode,
			LocalizedString name) {
		fiscalCountryRegion.edit(regionCode, name);
	}

}
