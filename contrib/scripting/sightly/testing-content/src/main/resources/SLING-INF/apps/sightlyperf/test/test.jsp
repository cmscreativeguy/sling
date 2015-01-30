<%@ page import="org.apache.sling.api.resource.Resource" %>
<%@ page import="org.apache.sling.api.resource.ValueMap" %>
<%@ page import="com.adobe.granite.xss.XSSAPI" %>
<%@ page import="java.util.Iterator" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2"%>
<sling:defineObjects />
<%
    ValueMap properties = resource.adaptTo(ValueMap.class);

    String tag = properties.get("tag", null);
    if (tag != null) {
        out.println("<" + tag + ">");
    }
    XSSAPI xssAPI = sling.getService(XSSAPI.class);
    out.println(xssAPI.encodeForHTML(properties.get("text", resource.getPath()).toString()));
    if (tag != null) {
        out.println("</" + tag + ">");
    }

%>
    <sling:call script="mode.jsp" />
<%

    if (properties.get("includeChildren", false)) {
        Iterator<Resource> iter = resource.listChildren();
%>
<ul>
<%
        while(iter.hasNext()) {
            Resource child = iter.next();
%>
            <li><sling:include path="<%=child.getPath()%>" /></li>
<%
        }
%>
</ul>
<%
    }
%>
