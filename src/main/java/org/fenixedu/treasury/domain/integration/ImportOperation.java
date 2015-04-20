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
package org.fenixedu.treasury.domain.integration;

import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;

import pt.ist.fenixframework.Atomic;

public class ImportOperation extends ImportOperation_Base {
	protected ImportOperation() {
		super();
	}

	protected void init(final org.joda.time.DateTime executionDate,
			final boolean processed, final boolean success,
			final java.lang.String errorLog) {
		setExecutionDate(executionDate);
		setProcessed(processed);
		setSuccess(success);
		setErrorLog(errorLog);
		checkRules();
	}

	private void checkRules() {
		//
		// CHANGE_ME add more busines validations
		//

		// CHANGE_ME In order to validate UNIQUE restrictions
		// if (findByExecutionDate(getExecutionDate().count()>1)
		// {
		// throw new
		// TreasuryDomainException("error.ImportOperation.executionDate.duplicated");
		// }
		// if (findByProcessed(getProcessed().count()>1)
		// {
		// throw new
		// TreasuryDomainException("error.ImportOperation.processed.duplicated");
		// }
		// if (findBySuccess(getSuccess().count()>1)
		// {
		// throw new
		// TreasuryDomainException("error.ImportOperation.success.duplicated");
		// }
		// if (findByErrorLog(getErrorLog().count()>1)
		// {
		// throw new
		// TreasuryDomainException("error.ImportOperation.errorLog.duplicated");
		// }
	}

	@Atomic
	public void edit(final org.joda.time.DateTime executionDate,
			final boolean processed, final boolean success,
			final java.lang.String errorLog) {
		setExecutionDate(executionDate);
		setProcessed(processed);
		setSuccess(success);
		setErrorLog(errorLog);
		checkRules();
	}

	public boolean isDeletable() {
		return true;
	}

	@Atomic
	public void delete() {
		if (!isDeletable()) {
			throw new TreasuryDomainException(
					"error.ImportOperation.cannot.delete");
		}

		setBennu(null);

		deleteDomainObject();
	}

	@Atomic
	public static ImportOperation create(
			final org.joda.time.DateTime executionDate,
			final boolean processed, final boolean success,
			final java.lang.String errorLog) {
		ImportOperation importOperation = new ImportOperation();
		importOperation.init(executionDate, processed, success, errorLog);
		return importOperation;
	}

	// @formatter: off
	/************
	 * SERVICES *
	 ************/
	// @formatter: on

	public static Stream<ImportOperation> findAll() {
		return IntegrationOperation.findAll()
				.filter(x -> x instanceof ImportOperation)
				.map(ImportOperation.class::cast);
	}

	public static Stream<ImportOperation> findByExecutionDate(
			final org.joda.time.DateTime executionDate) {
		return findAll()
				.filter(i -> executionDate.equals(i.getExecutionDate()));
	}

	public static Stream<ImportOperation> findByProcessed(
			final boolean processed) {
		return findAll().filter(i -> processed == i.getProcessed());
	}

	public static Stream<ImportOperation> findBySuccess(final boolean success) {
		return findAll().filter(i -> success == i.getSuccess());
	}

	public static Stream<ImportOperation> findByErrorLog(
			final java.lang.String errorLog) {
		return findAll()
				.filter(i -> errorLog.equalsIgnoreCase(i.getErrorLog()));
	}

}
