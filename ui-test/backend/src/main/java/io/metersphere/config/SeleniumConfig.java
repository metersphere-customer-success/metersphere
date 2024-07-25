package io.metersphere.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeleniumConfig {
    @PostConstruct
    public void init() {
        // 启用 java11+ 自身的client https://www.selenium.dev/blog/2022/using-java11-httpclient/#using-java-11-http-client-in-selenium
        System.setProperty("webdriver.http.factory", "jdk-http-client");
    }
}
