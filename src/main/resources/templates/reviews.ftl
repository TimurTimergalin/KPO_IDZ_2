<#-- @ftlvariable name="rating" type="java.lang.Number" -->
<#-- @ftlvariable name="name" type="java.lang.String" -->
<#import "_center.ftl" as center/>
<#import "_logo.ftl" as logo/>
<#import "_error_popup.ftl" as error_popup/>
<#import "_more_button.ftl" as more_button/>

<@center.center title="Reviews for ${name}">
    <@logo.logo></@logo.logo>
    <@error_popup.error_popup></@error_popup.error_popup>
    <link rel="stylesheet" type="text/css" href="/res/css/reviews.css">
    <p>Reviews on "${name}"</p>
    <p>Average rating: ${rating}</p>
    <h3>Your Review</h3>
    <div id="user-review">
        <div class="review-div">
            <label for="input-rating">Rating: </label><input type="number" id="input-rating" min="1" max="5">
            <label for="input-review" style="display: none">Review text: </label><textarea class="review-text"
                                                                              id="input-review"></textarea>
            <button disabled id="save-button">Save</button>
            <button disabled id="dismiss-button">Dismiss</button>
            <p id="error-message">ㅤ</p>
        </div>
    </div>
    <h3>Other Reviews</h3>
    <div id="other-reviews"></div>
    <@more_button.more_button></@more_button.more_button>
    <template id="review-template">
        <div class="review-div">
            <p>User: </p>
            <p class="field-value"></p>
            <p>Rating: </p>
            <p class="field-value"></p>
            <p class="field-value review-text"></p>
        </div>
    </template>
    <script src="/res/js/reviews.js"></script>
</@center.center>
