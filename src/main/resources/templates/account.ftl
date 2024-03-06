<#-- @ftlvariable name="username" type="java.lang.String" -->
<#import "_center.ftl" as center/>
<#import "_logo.ftl" as logo/>
<@center.center title="Account">
    <@logo.logo></@logo.logo>
    <p>Login: ${username}</p>
    <p><a href="/logout">Logout</a></p>
</@center.center>