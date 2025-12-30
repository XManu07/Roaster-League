<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
    <head>
        <title>League Standings</title>
    </head>
    <body>
        <form method="POST" action="${pageContext.request.contextPath}/">
            <c:forEach var="team" items="${teams}">
            <div class="row">
                <div class="col">
                        ${team.name}
                </div>
                <div class="col">
                    <c:forEach var="player" items="${team.players}">
                    <div class="row">
                            ${player.name}
                    </div>
                </div>
                <div class="col">
                        ${team.points}
                </div>
            </div>
            </c:forEach>
        </form>
    </body>
</html>