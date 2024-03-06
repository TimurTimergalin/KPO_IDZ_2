<#import "_center.ftl" as center/>
<#import "_error_popup.ftl" as error_popup>
<#import "_logo.ftl" as logo>
<@center.center title="Menu">
    <@logo.logo></@logo.logo>
    <@error_popup.error_popup></@error_popup.error_popup>
    <link rel="stylesheet" type="text/css" href="res/css/menu.css">
    <template id="dish-template">
        <div>
            <form class="menu-item" method="post">
                <div class="values">
                    <div></div>
                    <div></div>
                    <p class="input-label">Name: </p><input type="text" name="name">
                    <p class="input-label">Count: </p><input type="number" name="count">
                    <p class="input-label">Cooking time (m): </p><input type="number" name="cooking_time">
                    <p class="input-label">Price: </p><input type="number" step="0.01" name="price">
                    <p class="error-message"></p>
                </div>
                <div class="buttons"></div>
            </form>
        </div>
    </template>
    <h2>Create new menu item</h2>
    <div id="for-add"></div>
    <h2>Menu items</h2>
    <div id="for-existing"></div>
    <script src="/res/js/menu.js"></script>
</@center.center>