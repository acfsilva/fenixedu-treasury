<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<spring:url var="datatablesUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js"/>
<spring:url var="datatablesBootstrapJsUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl" value="/CSS/dataTables/dataTables.bootstrap.min.css"/>
<link rel="stylesheet" href="${datatablesCssUrl}"/>
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json"/>

<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css"/>

<link href="${pageContext.request.contextPath}/static/treasury/css/dataTables.responsive.css" rel="stylesheet"/>
<script src="${pageContext.request.contextPath}/static/treasury/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/static/treasury/css/dataTables.tableTools.css" rel="stylesheet"/>
<script src="${pageContext.request.contextPath}/static/treasury/js/dataTables.tableTools.min.js"></script>
<link href="${pageContext.request.contextPath}/static/treasury/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/static/treasury/js/select2.min.js"></script>
<script src="${pageContext.request.contextPath}/static/treasury/js/bootbox.min.js"></script>
<script src="${pageContext.request.contextPath}/static/treasury/js/omnis.js"></script>

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

<%-- TITLE --%>
<div class="page-header">
	<h1><spring:message code="label.accounting.manageCustomer.readCustomer" />
	</h1>
		<small></small>
</div>
<div class="modal fade" id="deleteModal">
  <div class="modal-dialog">
    <div class="modal-content">
    <form id ="deleteForm" action="${pageContext.request.contextPath}/treasury/accounting/managecustomer/customer/delete/${customer.externalId}"   method="POST">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title"><spring:message code="label.confirmation"/></h4>
      </div>
      <div class="modal-body">
        <p><spring:message code = "label.accounting.manageCustomer.readCustomer.confirmDelete"/></p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal"><spring:message code = "label.close"/></button>
        <button id="deleteButton" class ="btn btn-danger" type="submit"> <spring:message code = "label.delete"/></button>
      </div>
      </form>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /.modal -->
<%-- NAVIGATION --%>
<div class="well well-sm" style="display:inline-block">
	<span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;<a class="" href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/customer/"  ><spring:message code="label.event.back" /></a>
|&nbsp;&nbsp;	<span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>&nbsp;<a class="" href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/adhoccustomer/update/${customer.externalId}"  ><spring:message code="label.event.update" /></a>
|&nbsp;&nbsp;	<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a class="" href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/customer/read/${customer.externalId}/createpayment"  ><spring:message code="label.event.accounting.manageCustomer.createPayment" /></a>	|&nbsp;&nbsp;
	<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a class="" href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/customer/read/${customer.externalId}/createdebtentry"  ><spring:message code="label.event.accounting.manageCustomer.createDebtEntry" /></a>	|&nbsp;&nbsp;
	<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a class="" href="${pageContext.request.contextPath}/treasury/accounting/managecustomer/customer/read/${customer.externalId}/createexemption"  ><spring:message code="label.event.accounting.manageCustomer.createExemption" /></a>	
</div>
	<c:if test="${not empty infoMessages}">
				<div class="alert alert-info" role="alert">
					
					<c:forEach items="${infoMessages}" var="message"> 
						<p>${message}</p>
					</c:forEach>
					
				</div>	
			</c:if>
			<c:if test="${not empty warningMessages}">
				<div class="alert alert-warning" role="alert">
					
					<c:forEach items="${warningMessages}" var="message"> 
						<p>${message}</p>
					</c:forEach>
					
				</div>	
			</c:if>
			<c:if test="${not empty errorMessages}">
				<div class="alert alert-danger" role="alert">
					
					<c:forEach items="${errorMessages}" var="message"> 
						<p>${message}</p>
					</c:forEach>
					
				</div>	
			</c:if>

<div class="panel panel-primary">
	<div class="panel-heading">
		<h3 class="panel-title"><spring:message code="label.details"/></h3>
	</div>
	<div class="panel-body">
<form method="post" class="form-horizontal">
<table class="table">
		<tbody>
<tr>
	<th scope="row" class="col-xs-3"><spring:message code="label.Customer.code"/></th> 
	<td>
		<c:out value='${customer.code}'/>
	</td> 
</tr>
<tr>
	<th scope="row" class="col-xs-3"><spring:message code="label.Customer.name"/></th> 
	<td>
		<c:out value='${customer.name}'/>
	</td> 
</tr>
<tr>
	<th scope="row" class="col-xs-3"><spring:message code="label.Customer.fiscalNumber"/></th> 
	<td>
		<c:out value='${customer.fiscalNumber}'/>
	</td> 
</tr>
<tr>
	<th scope="row" class="col-xs-3"><spring:message code="label.Customer.identificationNumber"/></th> 
	<td>
		<c:out value='${customer.identificationNumber}'/>
	</td> 
</tr>
</tbody>
</table>

</form>
</div>
</div>
</br>
<h2> Conta Corrente </h2>
 <div id="content">
    <ul id="tabs" class="nav nav-tabs" data-tabs="tabs">

        <li class="active"><a href="#pending" data-toggle="tab">Docs. Pendentes</a></li>
        <li><a href="#details" data-toggle="tab">Extracto</a></li>
        <li><a href="#payments" data-toggle="tab">Pagamentos</a></li>
        <li><a href="#exemptions" data-toggle="tab">Isencoes</a></li>
    </ul>
    <div id="my-tab-content" class="tab-content">
        <div class="tab-pane active" id="pending">
            <h3>Docs. Pendentes</h3>
            <p>red red red red red red</p>
        </div>
        <div class="tab-pane" id="details">
            <h3>Extracto</h3>
            <p>orange orange orange orange orange</p>
        </div>
        <div class="tab-pane" id="payments">
            <h3>Pagamentos</h3>
            <p>yellow yellow yellow yellow yellow</p>
        </div>
        <div class="tab-pane" id="exemptions">
            <h3>Isencoes</h3>
            <p>green green green green green</p>
        </div>
    </div>
</div>

<script>
$(document).ready(function() {

	 $('#tabs').tab();
	});
</script>