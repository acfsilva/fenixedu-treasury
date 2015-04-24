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
package org.fenixedu.treasury.ui;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.fenixedu.bennu.spring.portal.SpringApplication;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.domain.Bennu;

import pt.ist.fenixframework.Atomic;

public class TreasuryBaseController {
	protected static final String ERROR_MESSAGES = "errorMessages";
	protected static final String WARNING_MESSAGES = "warningMessages";
	protected static final String INFO_MESSAGES = "infoMessages";

	// The entity in the Model

	// The list of INFO messages that can be showed on View
	protected void addInfoMessage(String message, Model model) {
		((List<String>) model.asMap().get(INFO_MESSAGES)).add(message);
	}

	// The list of WARNING messages that can be showed on View
	protected void addWarningMessage(String message, Model model) {
		((List<String>) model.asMap().get(WARNING_MESSAGES)).add(message);
	}

	// The list of ERROR messages that can be showed on View
	protected void addErrorMessage(String message, Model model) {
		((List<String>) model.asMap().get(ERROR_MESSAGES)).add(message);
	}

	protected void clearMessages(Model model) {
		model.addAttribute(INFO_MESSAGES, new ArrayList<String>());
		model.addAttribute(WARNING_MESSAGES, new ArrayList<String>());
		model.addAttribute(ERROR_MESSAGES, new ArrayList<String>());
	}

	protected String redirect(String destinationAction, Model model,
			RedirectAttributes redirectAttributes) {
		if (model.containsAttribute(INFO_MESSAGES)) {
			redirectAttributes.addFlashAttribute(INFO_MESSAGES, model.asMap()
					.get(INFO_MESSAGES));
		}
		if (model.containsAttribute(WARNING_MESSAGES)) {
			redirectAttributes.addFlashAttribute(WARNING_MESSAGES, model
					.asMap().get(WARNING_MESSAGES));
		}
		if (model.containsAttribute(ERROR_MESSAGES)) {
			redirectAttributes.addFlashAttribute(ERROR_MESSAGES, model.asMap()
					.get(ERROR_MESSAGES));
		}

		return "redirect:" + destinationAction;
	}

	@ModelAttribute
	protected void addModelProperties(Model model) {
		if (!model.containsAttribute(INFO_MESSAGES)) {
			model.addAttribute(INFO_MESSAGES, new ArrayList<String>());
		}
		if (!model.containsAttribute(WARNING_MESSAGES)) {
			model.addAttribute(WARNING_MESSAGES, new ArrayList<String>());
		}
		if (!model.containsAttribute(ERROR_MESSAGES)) {
			model.addAttribute(ERROR_MESSAGES, new ArrayList<String>());
		}

		// Add here more attributes to the Model
		// model.addAttribute(<attr1Key>, <attr1Value>);
		// ....
	}

}