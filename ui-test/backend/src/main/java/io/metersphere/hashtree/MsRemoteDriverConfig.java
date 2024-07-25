package io.metersphere.hashtree;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.googlecode.jmeter.plugins.webdriver.config.RemoteBrowser;
import com.googlecode.jmeter.plugins.webdriver.config.RemoteDriverConfig;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MsRemoteDriverConfig extends MsTestElement {
    @JsonProperty
    private String type = "RemoteDriverConfig";
    private String clazzName = MsRemoteDriverConfig.class.getCanonicalName();

    @JsonProperty
    private String gridUrl = "";
    @JsonProperty
    private RemoteBrowser capability = RemoteBrowser.CHROME;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
    }

    public static RemoteDriverConfig getRemoteDriverConfig(String gridUrl, RemoteBrowser browser, String browserLanguage) {
        RemoteDriverConfig remoteDriverConfig = new RemoteDriverConfig();
        remoteDriverConfig.setName("RemoteDriverConfig");
        remoteDriverConfig.setSeleniumGridUrl(gridUrl);
        remoteDriverConfig.setSelectedBrowser(browser);
        remoteDriverConfig.setBrowserLanguage(browserLanguage);
        remoteDriverConfig.setProperty(TestElement.TEST_CLASS, RemoteDriverConfig.class.getName());
        remoteDriverConfig.setProperty(TestElement.GUI_CLASS, "com.googlecode.jmeter.plugins.webdriver.config.gui.RemoteDriverConfigGui");
        return remoteDriverConfig;
    }
}
