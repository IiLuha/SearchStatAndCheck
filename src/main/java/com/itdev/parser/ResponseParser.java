package com.itdev.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itdev.enums.ModelLLM;

import java.util.List;
import java.util.Map;

public class ResponseParser {
    public  String extractGeneratedText(String jsonResponse, ModelLLM model) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // 1. Парсим JSON в Map
            Map<String, Object> responseMap = mapper.readValue(jsonResponse, Map.class);

            switch (model) {
                case DEEPSEEK -> {
                    // 2. Достаём список choices
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");

                    // 3. Берём первый choice
                    Map<String, Object> firstChoice = choices.get(0);

                    // 4. Достаём message
                    Map<String, String> message = (Map<String, String>) firstChoice.get("message");

                    // 5. Возвращаем content
                    return message.get("content");
                }
                case LLAMA -> {
                    // 2. Достаём и возвращаем response
                    return (String) responseMap.get("response");
                }
                default -> {
                    // 2. Если не знаем модель
                    return "Unknown model";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка парсинга: " + e.getMessage();
        }
    }
}
