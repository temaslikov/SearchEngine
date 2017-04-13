<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8">
    <title>Search</title>
    <style type="text/css">
        <%@ include file="mainSearch.css" %>
        <%@ include file="resultList.css" %>
    </style>

</head>
<body>

    <c:set var="resultIterator" value="${resultViewJSP.iterator()}" />

<c:set var="resultSize" value="${resultViewJSP.size()}" />
<c:set var="maxResultSize" value="${100}" />
<c:if test="${maxResultSize > resultSize}">
    <c:set var="maxResultSize" value="${resultSize}" />
</c:if>


<form class="form-wrapper" method="get" action="/search">

    <input name="expression" type="text" id="search" placeholder="Please enter the query" required value="${expressionViewJSP}">
    <input type="submit" value="go" id="submit">
</form>


<c:if test="${maxResultSize != 0}">

    <h3> Показаны ${maxResultSize} из ${resultSize} результатов </h3>

    <ul class="zebra">
    <c:forEach var="i" begin="0" end="${maxResultSize - 1}">

        <c:set var="currentResult" value="${resultIterator.next()}" />
        <li>${i+1}. <a href="https://ru.wikipedia.org/wiki?curid=${currentResult}">${titleMapViewJSP.get(currentResult)}</a></li>

    </c:forEach>
</c:if>

<c:if test="${maxResultSize == 0}">

    <h3> По вашему запросу ничего не нашлось </h3

</c:if>


</ul>
</body>
</html>