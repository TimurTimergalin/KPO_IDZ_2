<#import "_center.ftl" as center/>
<#import "_logo.ftl" as logo/>
<#import "_error_popup.ftl" as error_popup/>
<#import "_more_button.ftl" as more_button/>

<@center.center title="My orders">
    <@logo.logo></@logo.logo>
    <@error_popup.error_popup></@error_popup.error_popup>
    <link rel="stylesheet" type="text/css" href="/res/css/my_orders.css">
    <template id="order-template">
        <div class="order-div">
            <p>Order items:</p>
            <div class="order-items"></div>
            <div class="status-div">
                <p>Status: </p>
            </div>
        </div>
    </template>
    <div id="orders"></div>
    <@more_button.more_button></@more_button.more_button>
    <script src="/res/js/my_orders.js"></script>
</@center.center>
