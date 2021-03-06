/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and ServiÃ§os Partilhados da
 * Universidade de Lisboa:
 *  - Copyright Â© 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright Â© 2015 Universidade de Lisboa (after any Go-Live phase)
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

package org.fenixedu.treasury.domain.document;

import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.treasury.domain.FinantialEntity;
import org.fenixedu.treasury.domain.exceptions.TreasuryDomainException;

import pt.ist.fenixframework.Atomic;

public class TreasuryDocumentTemplate extends TreasuryDocumentTemplate_Base {

    protected TreasuryDocumentTemplate() {
        super();
        setBennu(Bennu.getInstance());
    }

    protected TreasuryDocumentTemplate(final FinantialDocumentType finantialDocumentTypes, final FinantialEntity finantialEntity) {
        this();
        setFinantialDocumentType(finantialDocumentTypes);
        setFinantialEntity(finantialEntity);

        checkRules();
    }

    private void checkRules() {
        if (getFinantialDocumentType() == null) {
            throw new TreasuryDomainException("error.TreasuryDocumentTemplate.finantialDocumentTypes.required");
        }

        if (getFinantialEntity() == null) {
            throw new TreasuryDomainException("error.TreasuryDocumentTemplate.finantialEntity.required");
        }
        if (findByFinantialDocumentTypeAndFinantialEntity(getFinantialDocumentType(), getFinantialEntity()).count() > 1) {
            throw new TreasuryDomainException("error.TreasuryDocumentTemplate.duplicated");
        }
    }

    @Atomic
    public void edit(final FinantialDocumentType finantialDocumentTypes, final FinantialEntity finantialEntity) {
        setFinantialDocumentType(finantialDocumentTypes);
        setFinantialEntity(finantialEntity);

        checkRules();
    }

    public boolean isDeletable() {
        return true;
    }

    @Atomic
    public void delete() {
        if (!isDeletable()) {
            throw new TreasuryDomainException("error.TreasuryDocumentTemplate.cannot.delete");
        }

        for (TreasuryDocumentTemplateFile file : this.getTreasuryDocumentTemplateFilesSet()) {
            this.removeTreasuryDocumentTemplateFiles(file);
            file.delete();
        }

        setBennu(null);
        setFinantialDocumentType(null);
        setFinantialEntity(null);
        deleteDomainObject();
    }

    public TreasuryDocumentTemplateFile getAtiveDocumentTemplateFile() {
        for (TreasuryDocumentTemplateFile documentTemplateFile : getTreasuryDocumentTemplateFilesSet()) {
            if (documentTemplateFile.getActive()) {
                return documentTemplateFile;
            }
        }
        return null;
    }

    @Atomic
    public TreasuryDocumentTemplateFile addFile(final TreasuryDocumentTemplate documentTemplate, final String displayName,
            final String fileName, final byte[] content) {
        TreasuryDocumentTemplateFile treasuryDocumentTemplateFile =
                TreasuryDocumentTemplateFile.create(this, displayName, fileName, content);

        activateFile(treasuryDocumentTemplateFile);

        return treasuryDocumentTemplateFile;
    }

    void activateFile(TreasuryDocumentTemplateFile treasuryDocumentTemplateFile) {
        for (TreasuryDocumentTemplateFile file : getTreasuryDocumentTemplateFilesSet()) {
            file.setActive(false);
        }
        treasuryDocumentTemplateFile.setActive(true);
    }

    @Atomic
    public static TreasuryDocumentTemplate create(final FinantialDocumentType finantialDocumentTypes,
            final FinantialEntity finantialEntity) {
        return new TreasuryDocumentTemplate(finantialDocumentTypes, finantialEntity);
    }

    public static Stream<TreasuryDocumentTemplate> findAll() {
        return Bennu.getInstance().getTreasuryDocumentTemplatesSet().stream();
    }

    public static Stream<TreasuryDocumentTemplate> findByFinantialDocumentType(final FinantialDocumentType finantialDocumentType) {
        return findAll().filter(i -> finantialDocumentType.equals(i.getFinantialDocumentType()));
    }

    public static Stream<TreasuryDocumentTemplate> findByFinantialEntity(final FinantialEntity finantialEntity) {
        return findAll().filter(i -> finantialEntity.equals(i.getFinantialEntity()));
    }

    public static Stream<TreasuryDocumentTemplate> findByFinantialDocumentTypeAndFinantialEntity(
            final FinantialDocumentType finantialDocumentType, final FinantialEntity finantialEntity) {
        return findAll().filter(i -> finantialDocumentType.equals(i.getFinantialDocumentType())).filter(
                i -> finantialEntity.equals(i.getFinantialEntity()));
    }

}
