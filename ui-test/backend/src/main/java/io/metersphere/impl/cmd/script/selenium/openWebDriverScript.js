// 设置超时时间，防止页面卡死; pageLoadTimeOut 超时时间从预执行脚本读取
var target = variableParsing("${target}");
target = getEnvironmentUrl(target);
var vo = ${vo};
if (!urlPattern.test(target || "")) {
    throw new Error("invalid url (" + target + ") format! valid is http(s)://domain(ip)!");
} else {
    exception = null;
    driver.manage().timeouts().pageLoadTimeout(pageLoadTimeOut, timeunit.SECONDS);
    if (!vo.blank) {
        driver.get(target);
    } else {
        //默认都以 tab 打开新窗口
        driver.switchTo().newWindow(org.openqa.selenium.WindowType.TAB);
        driver.get(target);
    }
}
