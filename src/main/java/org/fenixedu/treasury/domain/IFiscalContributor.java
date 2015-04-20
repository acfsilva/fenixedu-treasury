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

import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;

import com.google.common.collect.Sets;

public interface IFiscalContributor {
    
    public String getFiscalNumber();
    
    public static IFiscalContributor findByFiscalNumber(final String fiscalNumber) {
        IFiscalContributor result = null;
        
        for(IFiscalContributor it : findAll()) {
            if(!it.getFiscalNumber().equalsIgnoreCase(fiscalNumber)) {
                continue;
            }
            
            if(result != null) {
                throw new TreasuryDomainException("error.IFiscalContributor.duplicate.fiscal.number");
            }
            
            result = it;
        }
        
        return result;
    }
    
    public static Set<IFiscalContributor> findAll() {
        final Set<IFiscalContributor> iterable = Sets.newHashSet(FinantialInstitution.findAll().collect(Collectors.toList()));
        Customer.findAll().forEach(x->iterable.add(x));
        
        return iterable;
    }
}
