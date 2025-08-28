package com.burock.jwt_2.model;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public String getLocalizedName(MessageSource messageSource) {
        return messageSource.getMessage("order.status." + this.name(), null, LocaleContextHolder.getLocale());
    }
}
