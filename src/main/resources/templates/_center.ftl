<#macro center title>
    <!doctype html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>${title}</title>
        <link rel="stylesheet" type="text/css" href="/res/css/center_div.css">
    </head>
    <body>
    <div class="center-div" id="center-div">
        <#nested>
    </div>
    </body>
    </html>
</#macro>