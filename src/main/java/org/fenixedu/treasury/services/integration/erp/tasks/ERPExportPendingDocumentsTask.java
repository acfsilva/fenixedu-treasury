package org.fenixedu.treasury.services.integration.erp.tasks;

import java.util.List;

import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.treasury.domain.FinantialInstitution;
import org.fenixedu.treasury.domain.document.FinantialDocument;
import org.fenixedu.treasury.domain.integration.ERPExportOperation;
import org.fenixedu.treasury.services.integration.erp.ERPExporterManager;

@Task(englishTitle = "Export Pending Documents to ERP Integration", readOnly = true)
public class ERPExportPendingDocumentsTask extends CronTask {

    @Override
    public void runTask() throws Exception {

        FinantialInstitution.findAll().forEach(
                x -> {
                    taskLog("Start Exporting Pending Documents for : " + x.getName());
                    try {
                        List<ERPExportOperation> exportPendingDocumentsForFinantialInstitution =
                                ERPExporterManager.exportPendingDocumentsForFinantialInstitution(x);
                        for (ERPExportOperation exportOperation : exportPendingDocumentsForFinantialInstitution) {
                            for (FinantialDocument doc : exportOperation.getFinantialDocumentsSet()) {
                                taskLog("Exported document: " + doc.getUiDocumentNumber());
                            }
                        }
                        int documentsCount =
                                exportPendingDocumentsForFinantialInstitution.stream()
                                        .mapToInt(oper -> oper.getFinantialDocumentsSet().size()).sum();
                        taskLog("Finished Exporting " + documentsCount + " Pending Documents for : " + x.getName());
                    } catch (Exception ex) {
                        taskLog("Error exporting pending documents: " + ex.getMessage());
                        for (StackTraceElement el : ex.getStackTrace()) {
                            taskLog(el.toString());
                        }
                    }
                });
    }
}
