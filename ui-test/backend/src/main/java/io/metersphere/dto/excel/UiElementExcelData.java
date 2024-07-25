package io.metersphere.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UiElementExcelData {

    @ExcelIgnore
    private String customNum;

    @ExcelIgnore
    private String name;

    @ExcelIgnore
    private String nodePath;

    @ExcelIgnore
    private String locatorType;

    @ExcelIgnore
    private String elementLocator;

    @ExcelIgnore
    private String description;

    public List<List<String>> getHead(boolean needNum) {
        return Lists.newArrayList();
    }
}
