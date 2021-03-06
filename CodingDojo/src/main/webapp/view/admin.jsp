<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html;">
    <title>Admin page</title>
    <link href="/resources/css/bootstrap.css" rel="stylesheet">
    <link href="/resources/css/dojo.css" rel="stylesheet">
    <script src="/resources/js/jquery-1.7.2.js"></script>
    <script src="/resources/js/jquery.validate.js"></script>
    <script src="/resources/js/validation.js"></script>
    <script src="/resources/js/admin.js"></script>
</head>
<body>
    <div class="page-header">
        <h1>Admin page</h1>
    </div>
    <form:form commandName="adminSettings" action="admin31415" method="POST">
        <table class="admin-table" id="pauseGame">
            <tr>
                <td>
                    <c:choose>
                        <c:when test="${paused}">
                            <b>The dojo was suspended</b></br> <a href="/admin31415?resume">Resume game</a>.
                        </c:when>
                        <c:otherwise>
                            <b>The dojo is active</b></br> <a href="/admin31415?pause">Pause game</a>.
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </table>

        <c:if test="${players != null || savedGames != null}">
            <table class="admin-table" id="savePlayersGame">
                <tr colspan="3">
                    <td><b>Registered Players</b></td>
                </tr>
                <tr>
                    <td class="header">Player name:</td>
                    <td class="header">URL:</td>
                </tr>
                <c:forEach items="${players}" var="player" varStatus="status">
                    <c:choose>
                        <c:when test="${player.active}">
                            <tr>
                                <td><form:input path="players[${status.index}].name"/></td>
                                <td><form:input path="players[${status.index}].callbackUrl"/></td>
                                <td><a href="/admin31415?save=${player.name}">Save</a></td>
                                <c:choose>
                                    <c:when test="${player.saved}">
                                        <td><a href="/admin31415?load=${player.name}">Load</a></td>
                                    </c:when>
                                    <c:otherwise>
                                        <td>Load</td>
                                    </c:otherwise>
                                </c:choose>
                                <td><a href="/admin31415?remove=${player.name}">GameOver</a></td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td><input class="uneditable-input" value="${player.name}"/></td>
                                <td><input class="uneditable-input" value="${player.callbackUrl}"/></td>
                                <td>Save</td>
                                <c:choose>
                                    <c:when test="${player.saved}">
                                        <td><a href="/admin31415?load=${player.name}">Load</a></td>
                                    </c:when>
                                    <c:otherwise>
                                        <td>Load</td>
                                    </c:otherwise>
                                </c:choose>
                                <td>GameOver</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
                <tr>
                    <td></td>
                    <td></td>
                    <td>
                        <a href="/admin31415?saveall">SaveAll</a>
                    </td>
                    <td>
                        <a href="/admin31415?loadall">LoadAll</a>
                    </td>
                    <td>
                        <a href="/admin31415?removeall">GameOverAll</a>
                    </td>
                </tr>
            </table>
            <input type="submit" value="Save"/>
            </br>
        </c:if>

        </br>
        Go to <a href="/">main page</a>.
    </form:form>
</body>
</html>