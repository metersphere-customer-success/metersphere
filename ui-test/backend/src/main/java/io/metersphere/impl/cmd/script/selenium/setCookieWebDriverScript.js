var cookieStr = "${target}";

var cookie = JSON.parse(cookieStr);
var keys = Object.keys(cookie);
for (var i = 0; i < keys.length; i++) {
    driver.manage().addCookie(new org.openqa.selenium.Cookie(keys[i], cookie[keys[i]]));
}