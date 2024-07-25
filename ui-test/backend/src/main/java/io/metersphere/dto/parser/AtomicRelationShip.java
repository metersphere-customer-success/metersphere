package io.metersphere.dto.parser;

import io.metersphere.commons.constants.CommandType;
import io.metersphere.dto.parser.constants.CommandTokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 原子指令与复合指令的对应关系
 * 有对应原子指令的关系都会被 vo 进行转换处理
 */
public class AtomicRelationShip {
    private final static Map<String, String> atomicComplexMap = new HashMap<>();
    private final static List<CommandTokenType> atomicTokenTypeList = new ArrayList<>();

    static {

        //元素操作
        atomicComplexMap.put("select", "cmdDropdownBox");
        atomicComplexMap.put("addSelection", "cmdDropdownBox");
        atomicComplexMap.put("removeSelection", "cmdDropdownBox");
        atomicComplexMap.put("check", "cmdSetItem");
        atomicComplexMap.put("uncheck", "cmdSetItem");
        atomicComplexMap.put("waitForElementNotVisible", "cmdWaitElement");
        atomicComplexMap.put("waitForText", "cmdWaitElement");
        atomicComplexMap.put("waitForElementEditable", "cmdWaitElement");
        atomicComplexMap.put("waitForElementPresent", "cmdWaitElement");
        atomicComplexMap.put("waitForElementVisible", "cmdWaitElement");
        atomicComplexMap.put("waitForElementNotPresent", "cmdWaitElement");
        atomicComplexMap.put("waitForElementNotEditable", "cmdWaitElement");
        atomicComplexMap.put("submit", CommandType.COMMAND_TYPE_ATOM);

        //浏览器操作
        atomicComplexMap.put("open", "cmdOpen");
        atomicComplexMap.put("close", CommandType.COMMAND_TYPE_ATOM);
        atomicComplexMap.put("selectWindow", "cmdSelectWindow");
        atomicComplexMap.put("setWindowSize", "cmdSetWindowSize");
        atomicComplexMap.put("selectFrame", "cmdSelectFrame");

        //弹窗操作
        atomicComplexMap.put("answerOnNextPrompt", "cmdDialog");
        atomicComplexMap.put("chooseOkOnNextConfirmation", "cmdDialog");
        atomicComplexMap.put("webdriverChooseOkOnVisibleConfirmation", "cmdDialog");
        atomicComplexMap.put("webdriverAnswerOnVisiblePrompt", "cmdDialog");
        atomicComplexMap.put("webdriverChooseCancelOnVisibleConfirmation", "cmdDialog");
        atomicComplexMap.put("chooseCancelOnNextConfirmation", "cmdDialog");
        atomicComplexMap.put("chooseCancelOnNextPrompt", "cmdDialog");
        atomicComplexMap.put("webdriverChooseCancelOnVisiblePrompt", "cmdDialog");

        //提取
        atomicComplexMap.put("storeWindowHandle", "cmdExtractWindow");
        atomicComplexMap.put("storeTitle", "cmdExtractWindow");
        atomicComplexMap.put("store", "cmdExtractElement");
        atomicComplexMap.put("storeAttribute", "cmdExtractElement");
        atomicComplexMap.put("storeValue", "cmdExtractElement");
        atomicComplexMap.put("storeText", "cmdExtractElement");
        atomicComplexMap.put("storeXpathCount", "cmdExtractElement");
        atomicComplexMap.put("storeJson", "cmdExtractElement");

        //输入
        atomicComplexMap.put("sendKeys", "cmdInput");
        atomicComplexMap.put("type", "cmdInput");
        atomicComplexMap.put("editContent", "cmdInput");

        //鼠标操作
        atomicComplexMap.put("click", "cmdMouseClick");
        atomicComplexMap.put("clickAt", "cmdMouseClick");
        atomicComplexMap.put("doubleClick", "cmdMouseClick");
        atomicComplexMap.put("doubleClickAt", "cmdMouseClick");
        atomicComplexMap.put("mouseDown", "cmdMouseClick");
        atomicComplexMap.put("mouseDownAt", "cmdMouseClick");
        atomicComplexMap.put("mouseUp", "cmdMouseClick");
        atomicComplexMap.put("mouseUpAt", "cmdMouseClick");
        atomicComplexMap.put("mouseOut", "cmdMouseMove");
        atomicComplexMap.put("mouseOver", "cmdMouseMove");
        atomicComplexMap.put("mouseMoveAt", "cmdMouseMove");
        atomicComplexMap.put("dragAndDropToObject", "cmdMouseDrag");

        //鼠标右击
        atomicComplexMap.put("rightClick", "cmdMouseClick");
        atomicComplexMap.put("rightClickAt", "cmdMouseClick");

        //断言
        atomicComplexMap.put("assertTitle", "cmdValidateTitle");
        atomicComplexMap.put("verifyTitle", "cmdValidateTitle");
        atomicComplexMap.put("assertChecked", "cmdValidateElement");
        atomicComplexMap.put("verifyChecked", "cmdValidateElement");
        atomicComplexMap.put("assertNotChecked", "cmdValidateElement");
        atomicComplexMap.put("verifyNotChecked", "cmdValidateElement");
        atomicComplexMap.put("assertEditable", "cmdValidateElement");
        atomicComplexMap.put("verifyEditable", "cmdValidateElement");
        atomicComplexMap.put("assertNotEditable", "cmdValidateElement");
        atomicComplexMap.put("verifyNotEditable", "cmdValidateElement");
        atomicComplexMap.put("assertElementPresent", "cmdValidateElement");
        atomicComplexMap.put("verifyElementPresent", "cmdValidateElement");
        atomicComplexMap.put("assertElementNotPresent", "cmdValidateElement");
        atomicComplexMap.put("verifyElementNotPresent", "cmdValidateElement");
        atomicComplexMap.put("assertText", "cmdValidateElement");
        atomicComplexMap.put("verifyText", "cmdValidateElement");
        atomicComplexMap.put("assertNotText", "cmdValidateElement");
        atomicComplexMap.put("verifyNotText", "cmdValidateElement");
        atomicComplexMap.put("assertValue", "cmdValidateElement");
        atomicComplexMap.put("verifyValue", "cmdValidateElement");

        atomicComplexMap.put("assertSelectedLabel", "cmdValidateDropdown");
        atomicComplexMap.put("verifySelectedLabel", "cmdValidateDropdown");
        atomicComplexMap.put("assertSelectedValue", "cmdValidateDropdown");
        atomicComplexMap.put("verifySelectedValue", "cmdValidateDropdown");
        atomicComplexMap.put("assertNotSelectedValue", "cmdValidateDropdown");
        atomicComplexMap.put("verifyNotSelectedValue", "cmdValidateDropdown");

        atomicComplexMap.put("assert", "cmdValidateValue");
        atomicComplexMap.put("verify", "cmdValidateValue");
        atomicComplexMap.put("assertAlert", "cmdValidateText");
        atomicComplexMap.put("assertConfirmation", "cmdValidateText");
        atomicComplexMap.put("assertPrompt", "cmdValidateText");
        //流程控制
        atomicComplexMap.put("times", "cmdTimes");
        atomicComplexMap.put("forEach", "cmdForEach");
        atomicComplexMap.put("while", "cmdWhile");
        atomicComplexMap.put("if", "cmdIf");
        atomicComplexMap.put("elseIf", "cmdElseIf");
        atomicComplexMap.put("else", "cmdElse");
        atomicComplexMap.put("do", "cmdWhile");
        atomicComplexMap.put("repeatIf", CommandType.COMMAND_TYPE_ATOM);
        atomicComplexMap.put("end", CommandType.COMMAND_TYPE_ATOM);

        //前后置
        atomicComplexMap.put("runScript", "cmdScript");
        atomicComplexMap.put("executeScript", "cmdScript");
        atomicComplexMap.put("executeAsyncScript", "cmdScript");
        atomicComplexMap.put("pause", CommandType.COMMAND_TYPE_ATOM);
        atomicComplexMap.put("screenshot", CommandType.COMMAND_TYPE_ATOM);

    }

    public static String getComplexCommand(String atomicCommandName) {
        return atomicComplexMap.get(atomicCommandName);
    }

    public static List<String> getAtomicListByComplexCommand(String complexCommand) {
        return atomicComplexMap.entrySet().stream().filter(e -> e.getValue().equalsIgnoreCase(complexCommand)).map(e -> e.getKey()).collect(Collectors.toList());
    }

    public static List<CommandTokenType> getAtomicTokenTypeList() {
        return atomicTokenTypeList;
    }
}
