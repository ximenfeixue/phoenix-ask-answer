package com.ginkgocap.ywxt.interlocution.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;

/**
 * Created by Wang fei on 2017/5/31.
 */
public class AskAnswerJsonUtils {

    public static String writeObjectToJson(final Object jsonContent)
    {
        return writeObjectToJson(null, jsonContent);
    }

    public static String writeObjectToJson(final FilterProvider filterProvider, final Object jsonContent)
    {
        if (jsonContent == null) {
            throw new IllegalArgumentException("Content is null");
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            if (filterProvider != null) {
                objectMapper.setFilters(filterProvider);
            }
            objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            return objectMapper.writeValueAsString(jsonContent);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
