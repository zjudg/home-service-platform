package com.zjude.common.utils.uuid.base;

public interface KeyGenerate {
    String generateStringKey(String code);

    String generateStringKey(String orgID, String code, String orgFlag);

    Long generateLongKey();

    Boolean removeLongKey(String orgID, String code, String tranDate);
}