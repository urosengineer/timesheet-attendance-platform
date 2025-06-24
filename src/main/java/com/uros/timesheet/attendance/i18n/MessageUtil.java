package com.uros.timesheet.attendance.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * Utility for localized message resolution.
 * Uses Spring's LocaleContextHolder to always get the current locale
 * in any thread (Web, async, event, batch...).
 */
@Component
@RequiredArgsConstructor
public class MessageUtil {

    private final MessageSource messageSource;

    public String get(String key) {
        return messageSource.getMessage(key, null, getCurrentLocale());
    }

    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, getCurrentLocale());
    }

    public Locale getCurrentLocale() {
        Locale locale = LocaleContextHolder.getLocale();
        return (locale != null ? locale : Locale.ENGLISH);
    }
}