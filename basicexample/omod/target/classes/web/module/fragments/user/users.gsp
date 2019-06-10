This  is called <b>Users</b>
<br/><br/>
The date today is ${today}
<br/><br/>

This is a List of <i>all users</i> in the system

<br/><br/>
<% user.each{%>
$it.personName.fullName <br/>
<%}%>
