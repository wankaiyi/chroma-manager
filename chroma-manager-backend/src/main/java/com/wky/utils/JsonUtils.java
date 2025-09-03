package com.wky.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author wky
 * @date 2025/09/03
 */
public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String extractMessage(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(input);
            if (jsonNode.isObject() && jsonNode.has("message")) {
                JsonNode messageNode = jsonNode.get("message");
                if (messageNode.isTextual()) {
                    return messageNode.asText();
                } else {
                    return messageNode.toString();
                }
            }
            return input;
        } catch (JsonProcessingException e) {
            return input;
        }
    }
}
