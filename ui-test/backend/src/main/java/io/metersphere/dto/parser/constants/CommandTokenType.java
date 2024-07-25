package io.metersphere.dto.parser.constants;

import io.metersphere.commons.constants.CommandType;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.parser.AtomicRelationShip;
import io.metersphere.vo.UiAtomicCommandVO;
import io.metersphere.vo.UiCommandBaseVo;
import io.metersphere.vo.program.controller.*;
import io.metersphere.vo.mouse.operation.*;
import io.metersphere.vo.input.operation.*;
import io.metersphere.vo.independent.operation.*;
import io.metersphere.vo.extraction.*;
import io.metersphere.vo.element.operation.*;
import io.metersphere.vo.dialog.operation.*;
import io.metersphere.vo.browser.operation.*;
import io.metersphere.vo.validation.*;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public enum CommandTokenType {

    //原子指令
    ATOM(AtomicRelationShip.getAtomicListByComplexCommand(CommandType.COMMAND_TYPE_ATOM), CommandType.COMMAND_TYPE_ATOM, new UiAtomicCommandVO()),
    //复合指令
    CMDDROPDOWNBOX(AtomicRelationShip.getAtomicListByComplexCommand("cmdDropdownBox"), "cmdDropdownBox", new UiDropdownBoxVo()),
    CMDOPEN(AtomicRelationShip.getAtomicListByComplexCommand("cmdOpen"), "cmdOpen", new UiOpenVO()),
    CMDSELECTWINDOW(AtomicRelationShip.getAtomicListByComplexCommand("cmdSelectWindow"), "cmdSelectWindow", new UiSelectWindowVO()),
    CMDSETWINDOWSIZE(AtomicRelationShip.getAtomicListByComplexCommand("cmdSetWindowSize"), "cmdSetWindowSize", new UiSetWindowSizeVO()),
    CMDSELECTFRAME(AtomicRelationShip.getAtomicListByComplexCommand("cmdSelectFrame"), "cmdSelectFrame", new UiSelectFrameVO()),
    CMDDIALOG(AtomicRelationShip.getAtomicListByComplexCommand("cmdDialog"), "cmdDialog", new UiDialogVo()),
    CMDSETITEM(AtomicRelationShip.getAtomicListByComplexCommand("cmdSetItem"), "cmdSetItem", new UiSetItemVo()),
    CMDWAITELEMENT(AtomicRelationShip.getAtomicListByComplexCommand("cmdWaitElement"), "cmdWaitElement", new UiWaitElementVo()),
    CMDEXTRACTWINDOW(AtomicRelationShip.getAtomicListByComplexCommand("cmdExtractWindow"), "cmdExtractWindow", new UiExtractWindowVO()),
    CMDEXTRACTELEMENT(AtomicRelationShip.getAtomicListByComplexCommand("cmdExtractElement"), "cmdExtractElement", new UiExtractElementVO()),
    CMDINPUT(AtomicRelationShip.getAtomicListByComplexCommand("cmdInput"), "cmdInput", new UiInputVO()),
    CMDMOUSECLICK(AtomicRelationShip.getAtomicListByComplexCommand("cmdMouseClick"), "cmdMouseClick", new UiMouseClickVo()),
    CMDMOUSEMOVE(AtomicRelationShip.getAtomicListByComplexCommand("cmdMouseMove"), "cmdMouseMove", new UiMouseMoveVo()),
    CMDMOUSEDRAG(AtomicRelationShip.getAtomicListByComplexCommand("cmdMouseDrag"), "cmdMouseDrag", new UiMouseDragVo()),
    CMDVALIDATION(AtomicRelationShip.getAtomicListByComplexCommand("cmdValidateTitle"), "cmdValidateTitle", new UiValidateTitleVO()),
    CMDVALIDATEELEMENT(AtomicRelationShip.getAtomicListByComplexCommand("cmdValidateElement"), "cmdValidateElement", new UiValidateElementVO()),
    CMDVALIDATEDROPDOWN(AtomicRelationShip.getAtomicListByComplexCommand("cmdValidateDropdown"), "cmdValidateDropdown", new UiValidateDropdownVO()),
    CMDVALIDATEVALUE(AtomicRelationShip.getAtomicListByComplexCommand("cmdValidateValue"), "cmdValidateValue", new UiValidateValueVO()),
    CMDVALIDATETEXT(AtomicRelationShip.getAtomicListByComplexCommand("cmdValidateText"), "cmdValidateText", new UiValidateTextVO()),

    CMDTIMES(AtomicRelationShip.getAtomicListByComplexCommand("cmdTimes"), "cmdTimes", new UiTimesVo()),
    CMDFOREACH(AtomicRelationShip.getAtomicListByComplexCommand("cmdForEach"), "cmdForEach", new UiForEachVo()),
    CMDWHILE(AtomicRelationShip.getAtomicListByComplexCommand("cmdWhile"), "cmdWhile", new UiWhileVo()),
    CMDIF(AtomicRelationShip.getAtomicListByComplexCommand("cmdIf"), "cmdIf", new UiIfVo()),
    CMDELSEIF(AtomicRelationShip.getAtomicListByComplexCommand("cmdElseIf"), "cmdElseIf", new UiElseIfVo()),
    CMDELSE(AtomicRelationShip.getAtomicListByComplexCommand("cmdElse"), "cmdElse", new UiElseVo()),
    CMDSCRIPT(AtomicRelationShip.getAtomicListByComplexCommand("cmdScript"), "cmdScript", new UiScriptVO()),
    ;
    private List<String> atomicCommandList;
    private String complexCommand;
    private UiCommandBaseVo vo;

    CommandTokenType(List<String> atomicCommandList, String complexCommand, UiCommandBaseVo vo) {
        this.atomicCommandList = atomicCommandList;
        this.complexCommand = complexCommand;
        this.vo = vo;
    }

    public List<String> getAtomicCommandList() {
        return atomicCommandList;
    }

    public void setAtomicCommandList(List<String> atomicCommandList) {
        this.atomicCommandList = atomicCommandList;
    }

    public static CommandTokenType getTokenTypeByCommandName(String commandName) {
        for (CommandTokenType tokenType : values()) {
            if (tokenType.getAtomicCommandList().contains(commandName)) {
                return tokenType;
            }
        }
        LogUtil.error(String.format("原子指令：%s 没有对应 CommandTokenType", commandName));
        return null;
    }

    public String getComplexCommand() {
        return complexCommand;
    }

    public void setComplexCommand(String complexCommand) {
        this.complexCommand = complexCommand;
    }

    public UiCommandBaseVo getVo() {
        return vo;
    }

    public void setVo(UiCommandBaseVo vo) {
        this.vo = vo;
    }
}
