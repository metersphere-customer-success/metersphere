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
public class UiElementExcelDataUs extends UiElementExcelData{

    @ExcelProperty("ID")
    @NotRequired
    private String customNum;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 255)
    @ExcelProperty("Name")
    @ColumnWidth(150)
    private String name;

    @NotBlank(message = "{cannot_be_null}")
    @Length(max = 1000)
    @ExcelProperty("Module")
    @ColumnWidth(150)
    @Pattern(regexp = "^(?!.*//).*$", message = "{incorrect_format}")
    private String nodePath;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("LocatorType")
    @ColumnWidth(150)
    private String locatorType;

    @NotBlank(message = "{cannot_be_null}")
    @ExcelProperty("ElementLocator")
    @ColumnWidth(150)
    private String elementLocator;

    @ExcelProperty("Description")
    @ColumnWidth(150)
    private String description;

    @Override
    public List<List<String>> getHead(boolean needNum){
        List<List<String>> returnList = Lists.newArrayList();

        if(needNum){
            returnList.add(Lists.newArrayList("ID"));
        }

        returnList.add(Lists.newArrayList("Name"));
        returnList.add(Lists.newArrayList("Module"));
        returnList.add(Lists.newArrayList("LocatorType"));
        returnList.add(Lists.newArrayList("ElementLocator"));
        returnList.add(Lists.newArrayList("Description"));

        return returnList;
    }
}
