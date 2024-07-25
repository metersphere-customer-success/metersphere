package io.metersphere.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.google.common.collect.Lists;
import io.metersphere.excel.annotation.NotRequired;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;

@Data
@ColumnWidth(15)
public class UiElementExcelDataCn extends UiElementExcelData {

    @ExcelProperty("ID")
    @NotRequired
    private String customNum;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ExcelProperty("元素名称")
    @ColumnWidth(150)
    private String name;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(150)
    @Length(max = 1000)
    @ExcelProperty("所属模块")
    @Pattern(regexp = "^(?!.*//).*$", message = "{incorrect_format}")
    private String nodePath;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(150)
    @ExcelProperty("定位类型")
    private String locatorType;

    @NotBlank(message = "{cannot_be_null}")
    @ColumnWidth(150)
    @ExcelProperty("元素定位")
    private String elementLocator;

    @ExcelProperty("描述")
    @ColumnWidth(150)
    private String description;

    @Override
    public List<List<String>> getHead(boolean needNum) {
        List<List<String>> returnList = Lists.newArrayList();

        if (needNum) {
            returnList.add(Lists.newArrayList("ID"));
        }

        returnList.add(Lists.newArrayList("元素名称"));
        returnList.add(Lists.newArrayList("所属模块"));
        returnList.add(Lists.newArrayList("定位类型"));
        returnList.add(Lists.newArrayList("元素定位"));
        returnList.add(Lists.newArrayList("描述"));

        return returnList;
    }
}
