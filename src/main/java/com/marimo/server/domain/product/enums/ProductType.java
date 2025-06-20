package com.marimo.server.domain.product.enums;

import com.marimo.server.global.exception.BusinessException;
import com.marimo.server.global.exception.ErrorType;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ProductType {

    INVITATION,
    PREVIDEO,
    ;

    private static final Map<String, ProductType> PRODUCT_TYPE_MAP = new HashMap<>();

    static {
        for (ProductType productType : ProductType.values()) {
            PRODUCT_TYPE_MAP.put(productType.name(), productType);
        }
    }

    public static ProductType fromValue(String value) {
        ProductType productType = PRODUCT_TYPE_MAP.get(value.toUpperCase());

        if (productType == null) {
            throw new BusinessException(ErrorType.INVALID_PRODUCT_TYPE_ERROR);
        }

        return productType;
    }
}
