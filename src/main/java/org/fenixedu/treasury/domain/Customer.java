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
package org.fenixedu.treasury.domain;

import java.text.Normalizer;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.debt.DebtAccount;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;
import org.fenixedu.treasury.util.LocalizedStringUtil;

import pt.ist.fenixframework.Atomic;

public abstract class Customer extends Customer_Base implements IFiscalContributor {

    public static final String DEFAULT_FISCAL_NUMBER = "999999990";
    public static final int MAX_CODE_LENGHT = 20;

    protected Customer() {
        super();
        setBennu(Bennu.getInstance());
    }

    public abstract String getCode();

    @Override
    public abstract String getFiscalNumber();

    public abstract String getName();

    public abstract String getIdentificationNumber();

    public abstract String getAddress();

    public abstract String getDistrictSubdivision();

    public abstract String getZipCode();

    public abstract String getCountryCode();

    public abstract String getPaymentReferenceBaseCode();

    public abstract String getBusinessIdentification();

    public boolean isDeletable() {
        return false;
    }

    public boolean isPersonCustomer() {
        return false;
    }

    public boolean isAdhocCustomer() {
        return false;
    }

    @Atomic
    public void delete() {
        if (!isDeletable()) {
            throw new TreasuryDomainException("error.Customer.cannot.delete");
        }

        setBennu(null);

        deleteDomainObject();
    }

    protected void checkRules() {
        if (LocalizedStringUtil.isTrimmedEmpty(getCode())) {
            throw new TreasuryDomainException("error.Customer.code.required");
        }

        if (LocalizedStringUtil.isTrimmedEmpty(getName())) {
            throw new TreasuryDomainException("error.Customer.name.required");
        }

        if (findByCode(getCode()).count() > 1) {
            throw new TreasuryDomainException("error.Customer.code.duplicated");
        }

        if (this.getCode().length() > Customer.MAX_CODE_LENGHT) {
            throw new TreasuryDomainException("error.Customer.code.maxlenght");
        }
    }

    public static Stream<? extends Customer> findAll() {
        return Bennu.getInstance().getCustomersSet().stream();
    }

    public static Stream<? extends Customer> findByCode(final java.lang.String code) {
        return findAll().filter(i -> code.equalsIgnoreCase(i.getCode()));
    }

    public boolean matchesMultiFilter(String searchText) {
        if (searchText == null) {
            return false;
        }

        //Use the # to filter for Business Identification (Student, Candidacy, professor, etc...)
        if (searchText.startsWith("#") && searchText.length() > 1) {
            String codeToSearch = searchText.replace("#", "");
            return getBusinessIdentification() != null && getBusinessIdentification().equals(codeToSearch);
        }

        String searchFieldClear = Normalizer.normalize(searchText.toLowerCase(), Normalizer.Form.NFD);
        searchFieldClear = searchFieldClear.replaceAll("[^\\p{ASCII}]", "");
        String nameClear = Normalizer.normalize(getName().toLowerCase(), Normalizer.Form.NFD);
        nameClear = nameClear.replaceAll("[^\\p{ASCII}]", "");
        return nameClear.contains(searchFieldClear) || getIdentificationNumber() != null
                && getIdentificationNumber().contains(searchFieldClear) || getFiscalNumber() != null
                && getFiscalNumber().contains(searchFieldClear) || getCode() != null && getCode().contains(searchFieldClear)
                || getBusinessIdentification() != null && getBusinessIdentification().contains(searchFieldClear);
    }

    public Set<FinantialInstitution> getFinantialInstitutions() {
        return getDebtAccountsSet().stream().map(x -> x.getFinantialInstitution()).collect(Collectors.toSet());
    }

    public DebtAccount getDebtAccountFor(FinantialInstitution institution) {
        return getDebtAccountsSet().stream().filter(x -> x.getFinantialInstitution().equals(institution)).findFirst()
                .orElse(null);
    }

    @Atomic
    public void registerFinantialInstitutions(List<FinantialInstitution> newFinantialInstitutions) {

        Set<FinantialInstitution> actualInstitutions = getFinantialInstitutions();
        for (FinantialInstitution newInst : newFinantialInstitutions) {
            if (actualInstitutions.contains(newInst)) {
                this.getDebtAccountFor(newInst).reopenDebtAccount();
            } else {
                DebtAccount.create(newInst, this);
            }
        }

        for (FinantialInstitution actualInst : actualInstitutions) {
            if (newFinantialInstitutions.contains(actualInst)) {
            } else {
                DebtAccount account = getDebtAccountFor(actualInst);
                if (account.isDeletable()) {
                    account.delete();
                } else {
                    account.closeDebtAccount();
                }
            }
        }
    }

}
