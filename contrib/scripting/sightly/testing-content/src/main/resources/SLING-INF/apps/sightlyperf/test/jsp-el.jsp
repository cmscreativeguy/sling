<%@ page import="org.apache.sling.api.resource.ValueMap" %>
<%@ page import="com.adobe.granite.xss.XSSAPI" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2"%>
<sling:defineObjects />
<%
    ValueMap properties = resource.adaptTo(ValueMap.class);
    request.setAttribute("properties", properties);

    XSSAPI xssAPI = sling.getService(XSSAPI.class);
    String text = xssAPI.encodeForHTML(properties.get("text", resource.getPath()).toString());
    request.setAttribute("text", text);

    request.setAttribute("tag", properties.get("tag", ""));
    boolean includeChildren = properties.get("includeChildren", false);
    request.setAttribute("includeChildren", includeChildren);
    if (includeChildren) {
        request.setAttribute("children", resource.listChildren());
    }
%>
<c:if test="${tag != ''}"><${tag}></c:if>
${text}
<c:if test="${tag != ''}"></${tag}></c:if>
<sling:call script="mode.jsp" />
<c:if test="${includeChildren}">
    <ul>
        <c:forEach items="${children}" var="child">
            <li><sling:include path="${child.path}" /></li>
        </c:forEach>
    </ul>
</c:if>
