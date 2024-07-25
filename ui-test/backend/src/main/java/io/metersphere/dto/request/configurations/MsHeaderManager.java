package io.metersphere.dto.request.configurations;

import io.metersphere.dto.KeyValue;
import io.metersphere.dto.request.ParameterConfig;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)

public class MsHeaderManager extends MsTestElement {

    private String type = "HeaderManager";
    private String clazzName = MsHeaderManager.class.getCanonicalName();


    private List<KeyValue> headers;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;
        HeaderManager headerManager = new HeaderManager();
        headerManager.setEnabled(this.isEnable());
        headerManager.setName(StringUtils.isNotEmpty(this.getName()) ? this.getName() + "HeaderManager" : "HeaderManager");
        headerManager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
        headerManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("HeaderPanel"));
        headers.stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue ->
                headerManager.add(new Header(keyValue.getName(), keyValue.getValue()))
        );
        final HashTree headersTree = tree.add(headerManager);
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                el.toHashTree(headersTree, el.getHashTree(), config);
            });
        }
    }
}
