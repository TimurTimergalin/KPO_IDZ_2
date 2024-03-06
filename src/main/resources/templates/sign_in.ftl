<#import "_single_form.ftl" as form/>
<@form.single_form title="Sign in" link_title="Don't have an account? Sign up!" link_href="/sign-up">
    <p>
        <label for="login"></label><input type="text" id="login" placeholder="Login" name="login">
    </p>
    <p>
        <label for="password"></label><input type="password" id="password" placeholder="Password" name="password">
    </p>
</@form.single_form>