<#-- @ftlvariable name="error_message" type="String" -->
<#import "_center.ftl" as center/>
<#macro single_form title link_title link_href>
    <@center.center title=title>
        <link rel="stylesheet" type="text/css" href="/res/css/single_form.css">
        <script type="text/javascript" src="/res/js/single_form.js"></script>
        <form id="sign-form" method="post">
            <#nested>
            <button type="button" onclick="on_submit()">${title}</button>
            <p>
                <a href="${link_href}">${link_title}</a>
            </p>
        </form>
        <p id="error-message">${error_message}</p>
    </@center.center>
</#macro>