package io.metersphere.vo.dialog.operation;

import io.metersphere.dto.SideDTO;
import io.metersphere.dto.UiValidateSingleResult;
import io.metersphere.hashtree.MsUiCommand;
import io.metersphere.i18n.Translator;
import io.metersphere.vo.UiCommandBaseVo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;

@Data
public class UiDialogVo extends UiCommandBaseVo {

    private String type = "Dialog";
    private String clazzName = UiDialogVo.class.getCanonicalName();
    //操作方式 是 否
    private boolean inputOrNot;
    //输入内容
    private String inputContent;
    //操作方式 确定 取消
    private boolean optType;

    private List<String> needInputCommandList = new ArrayList<>() {{
        add("answerOnNextPrompt");
        add("webdriverAnswerOnVisiblePrompt");
    }};

    private List<String> optTypeChooseOk = new ArrayList<>() {{
        add("answerOnNextPrompt");
        add("chooseOkOnNextConfirmation");
        add("webdriverChooseOkOnVisibleConfirmation");
        add("webdriverAnswerOnVisiblePrompt");
    }};

    /**
     * 对应前端选择操作是或者否
     *
     * @param command
     * @return
     */
    @Override
    public String getCommand(MsUiCommand command) {
        if (inputOrNot && StringUtils.isNotBlank(inputContent)) {
            if (optType) {
                return "webdriverAnswerOnVisiblePrompt";
            }
            return "webdriverChooseCancelOnVisiblePrompt";
        } else {
            if (optType) {
                return "webdriverChooseOkOnVisibleConfirmation";
            } else {
                return "webdriverChooseCancelOnVisiblePrompt";
            }
        }
    }

    @Override
    public String getTarget() {
        if (inputOrNot && StringUtils.isNotBlank(inputContent)) {
            return inputContent;
        }
        return null;
    }

    @Override
    public List<?> getTargets() {
        return null;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    protected MsUiCommand _toMsUiCommand(SideDTO.TestsDTO.CommandsDTO sideCommand) {
        MsUiCommand msUiCommand = new MsUiCommand();
        String originTarget = sideCommand.getTarget();
        String atomicCommand = replaceNote(sideCommand.getCommand());
        UiDialogVo vo = new UiDialogVo();
        if (needInputCommandList.contains(atomicCommand)) {
            vo.setInputOrNot(true);
        } else {
            vo.setInputOrNot(false);
        }
        if (StringUtils.isNotBlank(originTarget)) {
            vo.setInputContent(originTarget);
        }
        if (optTypeChooseOk.contains(atomicCommand)) {
            vo.setOptType(true);
        } else {
            vo.setOptType(false);
        }
        msUiCommand.setVo(vo);
        return msUiCommand;
    }

    @Override
    public UiValidateSingleResult _validate(MsUiCommand command) {
        UiValidateSingleResult r = new UiValidateSingleResult();

        UiDialogVo vo = null;
        if (command.getVo() instanceof UiDialogVo) {
            vo = (UiDialogVo) command.getVo();
        } else {
            r.setResult(false);
            r.setMsg(wrapErrorMsg(command, Translator.get("param_error")));
            return r;
        }
        if (vo.isInputOrNot() && StringUtils.isBlank(vo.getInputContent())) {
            r.setResult(false);
            r.setMsg(wrapErrorMsg(command, Translator.get("input_content") + Translator.get("is_null")));
        }
        return r;
    }
}
