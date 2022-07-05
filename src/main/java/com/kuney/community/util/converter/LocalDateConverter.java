package com.kuney.community.util.converter;

import com.kuney.community.util.ObjCheckUtils;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author kuneychen
 * @since 2022/7/5 21:58
 */
public class LocalDateConverter implements Converter<String, LocalDate> {

    private static final String pattern = "yyyy-MM-dd";

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

    @Override
    public LocalDate convert(String source) {
        if (ObjCheckUtils.isBlank(source)) {
            return null;
        }
        return LocalDate.parse(source, formatter);
    }
}
