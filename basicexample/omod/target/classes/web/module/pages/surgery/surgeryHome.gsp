<%
    ui.decorateWith("kenyaemr","standardPage")

%>
<br/><br/>
<strong>This is new surgery page</strong>
<br/><br/>
<br/><br/>
<strong>Hello world</strong>
<i>Nyeri is warm</i>
<i>today</i>
<br/><br/>
<i>Nyeri is cold</i>
<i>today</i>
<br/><br/>

<br/><br/>
<b> List of Patients   </b>
<br/><br/>
<% patients.each{%>
$it.personName.fullName<br/>
<%}%>

<br/><br/>
${ui.includeFragment("basicexample","user/users")}