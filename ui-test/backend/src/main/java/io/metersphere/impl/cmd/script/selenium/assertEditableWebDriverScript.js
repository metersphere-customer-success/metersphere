var target = variableParsing("${target}");

var assertEditable = wait.until(conditions.presenceOfElementLocated(autoFind(target)));

if (!assertEditable.isEnabled()) {
    throw new Error("Actual assert " + target + " is not editable ")
}else{
    // 考虑非输入框情况和输入框情况
    if(assertEditable.getAttribute("contenteditable") !== "true") {
        // 如果是非输入框控件，直接返回不可编辑状态
        if (assertEditable.getTagName() !== "input" && assertEditable.getTagName() !== "textarea") {
            throw new Error("Actual assert " + target + " is not editable ")
        } else {
            // 输入控件判断是否 readonly
            if (assertEditable.getAttribute("readonly") === "true") {
                throw new Error("Actual assert " + target + " is not editable ")
            }
        }
    }
}