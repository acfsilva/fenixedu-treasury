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

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;

import pt.ist.fenixframework.Atomic;

public class OperationFile extends OperationFile_Base {

    public OperationFile() {
        super();
        // this.setBennu(Bennu.getInstance());
    }

    public OperationFile(String fileName, byte[] content) {
        this();
        this.init(fileName, fileName, content);
    }

    @Override
    // TODO: Implement
    public boolean isAccessible(User arg0) {
        throw new RuntimeException("not implemented");
    }

    private void checkRules() {
        //
        // CHANGE_ME add more busines validations
        //

        // CHANGE_ME In order to validate UNIQUE restrictions
    }

    @Atomic
    public void edit() {
        checkRules();
    }

    public boolean isDeletable() {
        return true;
    }

    @Override
    @Atomic
    public void delete() {
        if (!isDeletable()) {
            throw new TreasuryDomainException("error.OperationFile.cannot.delete");
        }

        this.setIntegrationOperation(null);
        super.delete();
    }

    @Atomic
    public static OperationFile create(String fileName, byte[] bytes, IntegrationOperation operation) {
        OperationFile operationFile = new OperationFile();
        operationFile.init(fileName, fileName, bytes);
        operationFile.setIntegrationOperation(operation);
        return operationFile;
    }

    // @formatter: off
    /************
     * SERVICES *
     ************/
    // @formatter: on

//	public static Stream<OperationFile> findAll() {
//		return Bennu.getInstance().getOperationFilesSet().stream();
//	}
//
//	public static Stream<OperationFile> findByIntegrationOperation(
//			final IntegrationOperation integrationOperation) {
//		return findAll().filter(
//				i -> integrationOperation.equals(i.getIntegrationOperation()));
//	}

}
