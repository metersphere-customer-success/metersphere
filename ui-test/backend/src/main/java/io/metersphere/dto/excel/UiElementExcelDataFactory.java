package io.metersphere.dto.excel;

import io.metersphere.excel.domain.ExcelDataFactory;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

public class UiElementExcelDataFactory implements ExcelDataFactory {
    @Override
    public Class getExcelDataByLocal() {
        Locale locale = LocaleContextHolder.getLocale();
        if (Locale.US.toString().equalsIgnoreCase(locale.toString())) {
            return UiElementExcelDataUs.class;
        } else if (Locale.TRADITIONAL_CHINESE.toString().equalsIgnoreCase(locale.toString())) {
            return UiElementExcelDataTw.class;
        }
        return UiElementExcelDataCn.class;
    }

    public UiElementExcelData getExcelDataLocal() {
        Locale locale = LocaleContextHolder.getLocale();
        if (Locale.US.toString().equalsIgnoreCase(locale.toString())) {
            return new UiElementExcelDataUs();
        } else if (Locale.TRADITIONAL_CHINESE.toString().equalsIgnoreCase(locale.toString())) {
            return new UiElementExcelDataTw();
        }
        return new UiElementExcelDataCn();
    }
}
