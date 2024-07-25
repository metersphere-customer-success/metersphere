package io.metersphere.dto.excel;

import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.google.common.collect.Lists;
import io.metersphere.commons.utils.JSON;
import io.metersphere.i18n.Translator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.util.List;

public class FunctionUiTemplateWriteHandler implements RowWriteHandler {


    @Override
    public void afterRowDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Integer relativeRowIndex, Boolean isHead) {
        List<String> typeList = Lists.newArrayList("id", "name", "className", "index", "tagName", "linkText", "partialLinkText", "css", "xpath", "label", "value");

        if (isHead) {
            Sheet sheet = writeSheetHolder.getSheet();
            Drawing<?> drawingPatriarch = sheet.createDrawingPatriarch();

            Comment comment = drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, 2, 0, (short) 3, 1));
            comment.setString(new XSSFRichTextString(Translator.get("module_created_automatically")));
            sheet.getRow(0).getCell(1).setCellComment(comment);

            comment = drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, 3, 0, (short) 3, 1));
            comment.setString(new XSSFRichTextString(Translator.get("options") + JSON.toJSONString(typeList)));
            sheet.getRow(0).getCell(1).setCellComment(comment);

            //设置下拉选
            // 区间设置 第一列第一行和第二行的数据。由于第一行是头，所以第一、二行的数据实际上是第二三行
            CellRangeAddressList cellRangeAddressList = new CellRangeAddressList(1, 5, 3, 3);
            DataValidationHelper helper = sheet.getDataValidationHelper();
            DataValidationConstraint constraint = helper.createExplicitListConstraint(typeList.toArray(String[]::new));
            DataValidation dataValidation = helper.createValidation(constraint, cellRangeAddressList);
            sheet.addValidationData(dataValidation);
        }
    }
}
