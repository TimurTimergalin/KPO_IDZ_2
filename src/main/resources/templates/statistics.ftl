<#import "_center.ftl" as center/>
<#import "_logo.ftl" as logo/>
<#import "_error_popup.ftl" as error_popup/>

<@center.center title="Statiistics">
    <@logo.logo></@logo.logo>
    <@error_popup.error_popup></@error_popup.error_popup>
    <link rel="stylesheet" type="text/css" href="/res/css/statistics.css">
    <template id="dish-statistics-template">
        <div class="statistics-div">
            <p>Name:</p><p class="stats-value"></p>
            <p>Ordered this month:</p><p class="stats-value"></p>
            <p>Average rating:</p><p class="stats-value"></p>
            <p>Revenue this month:</p><p class="stats-value"></p>
            <a>Reviews</a>
        </div>
    </template>
    <div id="stats-list"></div>
    <div class="sorting-div"><label for="sort-by"></label><select id="sort-by">
            <option value="name" selected="selected">Name</option>
            <option value="ordersCount">Ordered this month</option>
            <option value="averageRating">Average rating</option>
            <option value="revenue">Revenue this month</option>
        </select></div>
    <script src="/res/js/statistics.js"></script>
</@center.center>