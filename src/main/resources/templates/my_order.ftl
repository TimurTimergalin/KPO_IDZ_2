<#import "_logo.ftl" as logo/>
<#import "_error_popup.ftl" as error_popup/>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My Order</title>
    <link rel="stylesheet" type="text/css" href="/res/css/my_order.css">
</head>
<body>
    <@logo.logo></@logo.logo>
    <@error_popup.error_popup></@error_popup.error_popup>
    <div class="menu-holder"></div>
    <div class="cart-holder">
        <div class="cart-list"></div>
        <div class="buttons">
            <button disabled id="order-button">Order</button>
            <button disabled id="dismiss-button">Dismiss</button>
            <p id="order-error-message"></p>
        </div>
    </div>
    <div class="order-holder">
        <div class="order-list"></div>
        <div class="buttons">
            <button disabled id="pay-button">Pay</button>
            <button disabled id="cancel-all-button">Cancel All</button>
        </div>
    </div>
    <template id="menu-item-template">
        <div class="menu-item">
            <div class="menu-item-props">
                <p>Name: </p><p></p>
                <p>Available: </p><p></p>
                <p>Cooking time (min): </p><p></p>
                <p>Price: </p><p></p>
                <a>Reviews</a>
            </div>
            <div class="add-to-cart-holder">
                <button type="button" class="add-to-cart-button">Add to cart</button>
            </div>
        </div>
    </template>
    <template id="cart-item-template">
        <div class="cart-item">
            <p></p>
            <input type="number" min="1">
            <button>Remove</button>
        </div>
    </template>
    <template id="order-item-template">
        <div class="order-item">
            <p></p>
            <p></p>
            <button class="cancel-button">Cancel</button>
        </div>
    </template>
    <script src="/res/js/my_order.js"></script>
</body>
</html>