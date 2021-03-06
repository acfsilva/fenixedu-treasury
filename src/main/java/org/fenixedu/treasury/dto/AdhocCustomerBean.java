/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: ricardo.pedro@qub-it.com
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

package org.fenixedu.treasury.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.bennu.IBean;
import org.fenixedu.bennu.TupleDataSourceBean;
import org.fenixedu.treasury.domain.Customer;
import org.fenixedu.treasury.domain.CustomerType;
import org.fenixedu.treasury.domain.FinantialInstitution;

public class AdhocCustomerBean implements IBean {

    private CustomerType customerType;
    private String code;
    private String fiscalNumber;
    private String identificationNumber;
    private String name;
    private String address;
    private String districtSubdivision;
    private String zipCode;
    private String countryCode;
    private List<FinantialInstitution> finantialInstitutions;

    private List<TupleDataSourceBean> finantialInstitutionsDataSource;
    private List<TupleDataSourceBean> customerTypesDataSource;

    public String getCode() {
        return code;
    }

    public void setCode(java.lang.String value) {
        code = value;
    }

    public java.lang.String getFiscalNumber() {
        return fiscalNumber;
    }

    public void setFiscalNumber(java.lang.String value) {
        fiscalNumber = value;
    }

    public java.lang.String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(java.lang.String value) {
        identificationNumber = value;
    }

    public java.lang.String getName() {
        return name;
    }

    public void setName(java.lang.String value) {
        name = value;
    }

    public java.lang.String getAddress() {
        return address;
    }

    public void setAddress(java.lang.String value) {
        address = value;
    }

    public java.lang.String getDistrictSubdivision() {
        return districtSubdivision;
    }

    public void setDistrictSubdivision(java.lang.String value) {
        districtSubdivision = value;
    }

    public java.lang.String getZipCode() {
        return zipCode;
    }

    public void setZipCode(java.lang.String value) {
        zipCode = value;
    }

    public java.lang.String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(java.lang.String value) {
        countryCode = value;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public void setCustomerType(CustomerType customerType) {
        this.customerType = customerType;
    }

    public AdhocCustomerBean() {
        this.setFinantialInstitutionsDataSource(FinantialInstitution.findAll().collect(Collectors.toList()));
        this.setCustomerTypesDataSource(CustomerType.findAll().collect(Collectors.toList()));
    }

    public AdhocCustomerBean(Customer adhocCustomer) {
        this();
        this.setCustomerType(adhocCustomer.getCustomerType());
        this.setCode(adhocCustomer.getCode());
        this.setFiscalNumber(adhocCustomer.getFiscalNumber());
        this.setIdentificationNumber(adhocCustomer.getIdentificationNumber());
        this.setName(adhocCustomer.getName());
//        this.setAddress(adhocCustomer.getAddress());
//        this.setDistrictSubdivision(adhocCustomer.getDistrictSubdivision());
//        this.setZipCode(adhocCustomer.getZipCode());
//        this.setCountryCode(adhocCustomer.getCountryCode());
        this.setFinantialInstitutions(adhocCustomer.getDebtAccountsSet().stream().filter(x -> x.getClosed() == false)
                .map(x -> x.getFinantialInstitution()).collect(Collectors.toList()));
    }

    public List<FinantialInstitution> getFinantialInstitutions() {
        return finantialInstitutions;
    }

    public void setFinantialInstitutions(List<FinantialInstitution> finantialInstitutions) {
        this.finantialInstitutions = finantialInstitutions;
    }

    public List<TupleDataSourceBean> getFinantialInstitutionsDataSource() {
        return finantialInstitutionsDataSource;
    }

    public void setFinantialInstitutionsDataSource(List<FinantialInstitution> finantialInstitutionsDataSource) {
        this.finantialInstitutionsDataSource = finantialInstitutionsDataSource.stream().map(x -> {
            TupleDataSourceBean inst = new TupleDataSourceBean();
            inst.setId(x.getExternalId());
            inst.setText(x.getName());
            return inst;
        }).collect(Collectors.toList());
    }

    public List<TupleDataSourceBean> getCustomerTypesDataSource() {
        return customerTypesDataSource;
    }

    public void setCustomerTypesDataSource(List<CustomerType> customerTypesDataSource) {
        this.customerTypesDataSource = customerTypesDataSource.stream().map(customerType -> {
            TupleDataSourceBean customerTypeDataSource = new TupleDataSourceBean();
            customerTypeDataSource.setId(customerType.getExternalId());
            customerTypeDataSource.setText(customerType.getName().getContent());
            return customerTypeDataSource;
        }).collect(Collectors.toList());
    }
}
