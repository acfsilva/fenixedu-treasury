<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="datatables" uri="http://github.com/dandelion/datatables"%>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

<spring:url var="datatablesUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js" />
<spring:url var="datatablesBootstrapJsUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl" value="/CSS/dataTables/dataTables.bootstrap.min.css" />

<link rel="stylesheet" href="${datatablesCssUrl}" />
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css" />

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

<link href="${pageContext.request.contextPath}/static/treasury/css/dataTables.responsive.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/static/treasury/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js"></script>
<script src="${pageContext.request.contextPath}/static/treasury/js/omnis.js"></script>

<div class="modal fade" id="anullModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="${pageContext.request.contextPath}/treasury/document/managepayments/settlementnote/read/${settlementNote.externalId}/anullsettlement"
                method="POST">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title">
                        <spring:message code="label.confirmation" />
                    </h4>
                </div>
                <div class="modal-body">
                    <p>
                        <spring:message code="label.document.managePayments.readSettlementNote.confirmAnull" />
                    </p>
                    <br /> <br />
                    <div class="form">
                        <div class="form-group row">
                            <div class="col-sm-4 control-label">
                                <spring:message code="label.SettlementNote.annulledReason" />
                            </div>

                            <div class="col-sm-8">
                                <input id="settlementNote_anullReason" class="form-control" type="text" name="anullReason" required value='' />
                            </div>
                        </div>
                    </div>

                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <spring:message code="label.cancel" />
                    </button>
                    <button id="deleteButton" class="btn btn-danger" type="submit">
                        <spring:message code="label.annull" />
                    </button>
                </div>
            </form>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>

<div class="modal fade" id="closeModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="${pageContext.request.contextPath}/treasury/document/managepayments/settlementnote/read/${settlementNote.externalId}/closesettlementnote"
                method="POST">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title">
                        <spring:message code="label.confirmation" />
                    </h4>
                </div>
                <div class="modal-body">
                    <p>
                        <spring:message code="label.document.manageInvoice.readSettlementNote.confirmClose" />
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <spring:message code="label.cancel" />
                    </button>
                    <button id="deleteButton" class="btn btn-primary" type="submit">
                        <spring:message code="label.close" />
                    </button>
                </div>
            </form>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->


<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message code="label.document.managePayments.readSettlementNote" />
        <small></small>
    </h1>
</div>
<div class="modal fade" id="deleteModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="deleteForm" action="${pageContext.request.contextPath}/treasury/document/managepayments/settlementnote/delete/${settlementNote.externalId}"
                method="POST">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title">
                        <spring:message code="label.confirmation" />
                    </h4>
                </div>
                <div class="modal-body">
                    <p>
                        <spring:message code="label.document.managePayments.readSettlementNote.confirmDelete" />
                    </p>
                    <input id="settlementNote_anullReason" class="form-control" type="hidden" name="anullReason" required value='.' />


                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <spring:message code="label.close" />
                    </button>
                    <button id="deleteButton" class="btn btn-danger" type="submit">
                        <spring:message code="label.delete" />
                    </button>
                </div>
            </form>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;<a class=""
        href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/debtaccount/read/${settlementNote.debtAccount.externalId}"><spring:message
            code="label.event.back" /></a> &nbsp;|&nbsp; <span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>&nbsp;<a class=""
        href="${pageContext.request.contextPath}/treasury/document/managepayments/settlementnote/update/${settlementNote.externalId}"><spring:message code="label.event.update" /></a>
    &nbsp;|&nbsp;
    <c:if test="${settlementNote.isPreparing()}">
        <span class="glyphicon glyphicon-trash" aria-hidden="true"></span>&nbsp;<a class="" href="#" data-toggle="modal" data-target="#deleteModal"><spring:message
                code="label.event.delete" /></a> &nbsp;|&nbsp; 
        <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
        <a class="" href="#" data-toggle="modal" data-target="#closeModal"> <spring:message code="label.event.document.manageInvoice.closeSettlementNote" />
        </a> &nbsp;|&nbsp;
    </c:if>
    <c:if test="${settlementNote.isClosed()}">
        <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
        <a class="" href="#" data-toggle="modal" data-target="#anullModal"> <spring:message code="label.event.document.managePayments.anullSettlementNote" />
        </a> &nbsp;|&nbsp;      
    </c:if>
    <c:if test="${not settlementNote.isPreparing()}">
        <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a class=""
            href="${pageContext.request.contextPath}/treasury/document/managepayments/settlementnote/read/${settlementNote.externalId}/exportintegrationfile"><spring:message
                code="label.event.document.managePayments.exportIntegrationFile" /></a>
        &nbsp;|&nbsp;
    </c:if>
</div>

<c:if test="${not empty infoMessages}">
    <div class="alert alert-info" role="alert">

        <c:forEach items="${infoMessages}" var="message">
            <p>
                <span class="glyphicon glyphicon glyphicon-ok-sign" aria-hidden="true">&nbsp;</span> ${message}
            </p>
        </c:forEach>

    </div>
</c:if>
<c:if test="${not empty warningMessages}">
    <div class="alert alert-warning" role="alert">

        <c:forEach items="${warningMessages}" var="message">
            <p>
                <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span> ${message}
            </p>
        </c:forEach>

    </div>
</c:if>
<c:if test="${not empty errorMessages}">
    <div class="alert alert-danger" role="alert">

        <c:forEach items="${errorMessages}" var="message">
            <p>
                <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span> ${message}
            </p>
        </c:forEach>

    </div>
</c:if>

<div class="panel panel-primary">
    <div class="panel-heading">
        <h3 class="panel-title">
            <spring:message code="label.details" />
        </h3>
    </div>
    <div class="panel-body">
        <form method="post" class="form-horizontal">
            <table class="table">
                <tbody>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.Series.finantialInstitution" /></th>
                        <td><c:out value='${settlementNote.documentNumberSeries.series.finantialInstitution.name}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.debtAccount" /></th>
                        <td><c:out value='${settlementNote.debtAccount.customer.businessIdentification} - ${settlementNote.debtAccount.customer.name}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.documentNumber" /></th>
                        <td><c:out value='${settlementNote.uiDocumentNumber}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.state" /></th>
                        <td><c:if test="${settlementNote.isAnnulled()}">
                                <span class="label label-danger">
                            </c:if> <c:if test="${settlementNote.isPreparing() }">
                                <span class="label label-warning">
                            </c:if> <c:if test="${settlementNote.isClosed()}">
                                <span class="label label-primary">
                            </c:if> <c:out value='${settlementNote.state.descriptionI18N.content}' /> </span></td>
                    </tr>
                    <c:if test="${settlementNote.isAnnulled()}">
                        <tr>
                            <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.annulledReason" /></th>
                            <td><c:out value='${settlementNote.annulledReason}' /></td>
                        </tr>
                    </c:if>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.documentDate" /></th>
                        <td><c:out value='${settlementNote.documentDate}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.originDocumentNumber" /></th>
                        <td><c:out value='${settlementNote.originDocumentNumber}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.totalDebitAmount" /></th>
                        <td><c:out value='${settlementNote.currency.getValueFor(settlementNote.totalDebitAmount)}' /></td>
                    </tr>
                    <tr>
                        <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.totalCreditAmount" /></th>
                        <td><c:out value='${settlementNote.currency.getValueFor(settlementNote.totalCreditAmount)}' /></td>
                    </tr>
                    <tr>
                        <c:if test="${ not empty settlementNote.paymentEntriesSet }">
                            <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.totalPayedAmount" /></th>
                            <td><c:out value='${settlementNote.currency.getValueFor(settlementNote.totalPayedAmount)}' /></td>
                        </c:if>
                        <c:if test="${ not empty settlementNote.reimbursementEntriesSet }">
                            <th scope="row" class="col-xs-3"><spring:message code="label.SettlementNote.totalReimbursementAmount" /></th>
                            <td><c:out value='${settlementNote.currency.getValueFor(settlementNote.totalReimbursementAmount)}' /></td>
                        </c:if>
                    </tr>
                </tbody>
            </table>
        </form>
    </div>
</div>

<p></p>
<p></p>
<h2>
    <spring:message code="label.SettlementNote.settlementEntries" />
</h2>

<c:choose>
    <c:when test="${not empty settlementNote.finantialDocumentEntriesSet}">
        <datatables:table id="settlementEntries" row="settlementEntry" data="${settlementNote.finantialDocumentEntriesSet}" cssClass="table responsive table-bordered table-hover"
            cdn="false" cellspacing="2">
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.InvoiceEntry.document" />
                </datatables:columnHead>
                <c:out value="${settlementEntry.invoiceEntry.finantialDocument.uiDocumentNumber}" />
            </datatables:column>
            <datatables:column>
                <datatables:columnHead>
                    <spring:message code="label.SettlementEntry.description" />
                </datatables:columnHead>
                <c:out value="${settlementEntry.description}" />
            </datatables:column>
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.DebitEntry.amount" />
                </datatables:columnHead>
                <c:out value="${settlementNote.currency.getValueFor(settlementEntry.invoiceEntry.totalAmount)}" />
            </datatables:column>
            <%--             <datatables:column cssStyle="width:10%"> --%>
            <%--                 <datatables:columnHead> --%>
            <%--                     <spring:message code="label.DebitEntry.vat" /> --%>
            <%--                 </datatables:columnHead> --%>
            <%--                 <c:out value="${settlementEntry.invoiceEntry.vat.taxRate}" /> --%>
            <%--             </datatables:column> --%>
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.SettlementEntry.amount" />
                </datatables:columnHead>
                <c:out value="${settlementNote.currency.getValueFor(settlementEntry.totalAmount)}" />
            </datatables:column>
            <datatables:column cssStyle="width:1%">
                <c:if test="${settlementEntry.invoiceEntry.isDebitNoteEntry()}">
                <c:out value=" [D] " />
                </c:if>
                <c:if test="${settlementEntry.invoiceEntry.isCreditNoteEntry()}">
                <c:out value=" [C] " />
                </c:if>
            </datatables:column>
            <%--             <datatables:column cssStyle="width:10%"> --%>
            <%--                 <form method="get" action="${pageContext.request.contextPath}/treasury/document/manageinvoice/settlemententry/read/${settlementEntry.externalId}"> --%>
            <!--                     <button type="submit" class="btn btn-default btn-xs"> -->
            <%--                         <spring:message code="label.view" /> --%>
            <!--                     </button> -->
            <!--                 </form> -->
            <%--             </datatables:column> --%>
        </datatables:table>
        <script>
									createDataTables(
											'settlementEntries',
											false,
											false,
											false,
											"${pageContext.request.contextPath}",
											"${datatablesI18NUrl}");
								</script>
    </c:when>
    <c:otherwise>
        <div class="alert alert-warning" role="alert">
            <p>
                <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
                <spring:message code="label.noResultsFound" />
            </p>
        </div>

    </c:otherwise>
</c:choose>

<p></p>
<p></p>

    <c:if test="${not empty settlementNote.advancedPaymentCreditNote}">
        <h2>
            <spring:message code="label.SettlementNote.advancedPaymentCreditNote" />
        </h2>        
        
        <datatables:table id="advancedPaymentEntries" row="advancedPaymentEntry" data="${settlementNote.advancedPaymentCreditNote.creditEntriesSet}" cssClass="table responsive table-bordered table-hover" cdn="false"
            cellspacing="2">
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.InvoiceEntry.document" />
                </datatables:columnHead>
                <c:out value="${advancedPaymentEntry.finantialDocument.uiDocumentNumber}" />
            </datatables:column>
            <datatables:column>
                <datatables:columnHead>
                    <spring:message code="label.SettlementEntry.description" />
                </datatables:columnHead>
                <c:out value="${advancedPaymentEntry.description}" />
            </datatables:column>
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.DebitEntry.amount" />
                </datatables:columnHead>
                <c:out value="${settlementNote.currency.getValueFor(advancedPaymentEntry.totalAmount)}" />
            </datatables:column>
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.SettlementEntry.amount" />
                </datatables:columnHead>
                <c:out value="${settlementNote.currency.getValueFor(advancedPaymentEntry.totalAmount)}" />
            </datatables:column>
            <datatables:column cssStyle="width:1%">
                <c:out value=" [C] " />
            </datatables:column>
        </datatables:table>
        <script>
            createDataTables(
                    'advancedPaymentEntries',
                    false,
                    false,
                    false,
                    "${pageContext.request.contextPath}",
                    "${datatablesI18NUrl}");
        </script>
    </c:if>


    <c:if test="${not empty settlementNote.reimbursementEntriesSet}">
        <h2>
            <spring:message code="label.SettlementNote.reimbursementEntries" />
        </h2>
        <datatables:table id="reimbursementEntries" row="reimbursementEntry" data="${settlementNote.reimbursementEntriesSet}" cssClass="table responsive table-bordered table-hover" cdn="false"
            cellspacing="2">
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.PaymentEntry.paymentMethod" />
                </datatables:columnHead>
                <c:out value="${reimbursementEntry.paymentMethod.name.content}" />
            </datatables:column>
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.PaymentEntry.reimbursementAmount" />
                </datatables:columnHead>
                <c:out value="${settlementNote.currency.getValueFor(reimbursementEntry.reimbursedAmount)}" />
            </datatables:column>
        </datatables:table>
        <script>
            createDataTables(
                    'reimbursementEntries',
                    false,
                    false,
                    false,
                    "${pageContext.request.contextPath}",
                    "${datatablesI18NUrl}");
        </script>
    </c:if>


<p></p>
<p></p>

<c:choose>
    <c:when test="${not empty settlementNote.paymentEntriesSet}">
        <h2>
            <spring:message code="label.SettlementNote.paymentEntries" />
        </h2>
        <datatables:table id="paymentEntries" row="payemntEntry" data="${settlementNote.paymentEntriesSet}" cssClass="table responsive table-bordered table-hover" cdn="false"
            cellspacing="2">
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.PaymentEntry.paymentMethod" />
                </datatables:columnHead>
                <c:out value="${payemntEntry.paymentMethod.name.content}" />
            </datatables:column>
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.PaymentEntry.payedAmount" />
                </datatables:columnHead>
                <c:out value="${settlementNote.currency.getValueFor(payemntEntry.payedAmount)}" />
            </datatables:column>
        </datatables:table>
        <script>
			createDataTables(
					'paymentEntries',
					false,
					false,
					false,
					"${pageContext.request.contextPath}",
					"${datatablesI18NUrl}");
		</script>
    </c:when>
    <c:when test="${not empty settlementNote.reimbursementEntriesSet}">
        <h2>
            <spring:message code="label.SettlementNote.reimbursementEntries" />
        </h2>
        <datatables:table id="reimbursementEntries" row="reimbursementEntry" data="${settlementNote.reimbursementEntriesSet}" cssClass="table responsive table-bordered table-hover" cdn="false"
            cellspacing="2">
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.PaymentEntry.paymentMethod" />
                </datatables:columnHead>
                <c:out value="${reimbursementEntry.paymentMethod.name.content}" />
            </datatables:column>
            <datatables:column cssStyle="width:10%">
                <datatables:columnHead>
                    <spring:message code="label.PaymentEntry.reimbursementAmount" />
                </datatables:columnHead>
                <c:out value="${settlementNote.currency.getValueFor(reimbursementEntry.reimbursedAmount)}" />
            </datatables:column>
        </datatables:table>
        <script>
            createDataTables(
                    'reimbursementEntries',
                    false,
                    false,
                    false,
                    "${pageContext.request.contextPath}",
                    "${datatablesI18NUrl}");
        </script>
    </c:when>
    <c:otherwise>
        <div class="alert alert-warning" role="alert">
            <p>
                <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true">&nbsp;</span>
                <spring:message code="label.noResultsFound" />
            </p>
        </div>

    </c:otherwise>
</c:choose>

<script>
	$(document).ready(function() {

	});
</script>
