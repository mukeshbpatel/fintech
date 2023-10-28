package com.amp.fintech.utility;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtil {

    ObjectMapper mapper = new ObjectMapper();

    public static ObjectMapper getMapper() {
        return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .findAndRegisterModules();
    }

    public static String objectToJson(Object obj) {
        try {
            return getMapper().writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Json Converter Error", e);
            return null;
        }
    }
}