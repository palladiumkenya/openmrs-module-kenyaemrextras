<%
    ui.decorateWith("kenyaemr","standardPage")

%>
<br/><br/>
<strong>Hello world</strong>
<i>Nyeri is warm</i>
<i>today</i>
<br/><br/>

<% if (context.authenticated){ %>
 Hello, $context.authenticatedUser.personName.fullName
 <br/><br/>
<% } %>

<br/><br/>
 Hi, ${myName}
<br/><br/>
<b> List of Patients   </b>
<br/><br/>
<% patients.each{%>
$it.personName.fullName<br/>
<%}%>

<br/><br/>
${ui.includeFragment("basicexample","user/users")}

