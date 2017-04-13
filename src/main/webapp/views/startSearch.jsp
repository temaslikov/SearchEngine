<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8">
    <title>Search</title>
    <style type="text/css">
        <%@ include file="startSearch.css" %>
    </style>

</head>
<body>

<form class="form-wrapper" method="get" action="/search">

    <input name="expression" type="text" id="search" placeholder="Please enter the query" required>
    <input type="submit" value="go" id="submit">
</form>
</body>
</html>
