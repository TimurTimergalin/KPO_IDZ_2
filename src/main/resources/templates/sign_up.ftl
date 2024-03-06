<#import "_single_form.ftl" as form/>
<@form.single_form title="Sign up" link_title="Have an account? Sign in!" link_href="/sign-in">
    <p>
        <label for="login"></label><input type="text" id="login" placeholder="Login" name="login">
    </p>
    <p>
        <label for="password"></label><input type="password" id="password" placeholder="Password"
                                             name="password">
    </p>
    <p>
        <label for="repeat-password"></label><input type="password" id="repeat-password" placeholder="Repeat password">
    </p>
</@form.single_form>