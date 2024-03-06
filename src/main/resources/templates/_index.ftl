<#-- @ftlvariable name="hasOrder" type="Boolean" -->
<#-- @ftlvariable name="username" type="String" -->
<#import "_center.ftl" as center/>
<#import "_logo.ftl" as logo>
<#macro index>
    <@center.center title="Home">
        <@logo.logo></@logo.logo>
        <link rel="stylesheet" type="text/css" href="/res/css/index.css">
        <h3>Welcome, ${username}</h3>
        <p>
            <#if hasOrder>
                <a href="/my-order">Current Order</a>
            <#else>
                <a href="/my-order">New Order</a>
            </#if>
        </p>
        <p>
            <a href="/my-orders">My Orders</a>
        </p>
        <p>
            <a href="/account">Account</a>
        </p>
        <#nested>
    </@center.center>
</#macro>