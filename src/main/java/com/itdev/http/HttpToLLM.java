package com.itdev.http;

import com.itdev.http.config.LlamaConfig;
import com.itdev.http.config.ORDeepseekConfig;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class HttpToLLM {

    public static String deepseekChatCompletion(String userMessage) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(ORDeepseekConfig.DEEPSEEK_API_URL);

            // Заголовки
            httpPost.setHeader("Authorization", "Bearer " + ORDeepseekConfig.DEEPSEEK_API_KEY);
            httpPost.setHeader("Content-Type", "application/json");

            // Тело запроса в JSON
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", ORDeepseekConfig.DEEPSEEK_MODEL);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", userMessage));
            requestBody.put("messages", messages);

            // Конвертируем Map → JSON
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(requestBody);

            httpPost.setEntity(new StringEntity(jsonBody));

            // Отправка запроса
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return EntityUtils.toString(entity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Ошибка запроса";
    }

    public static String llamaGenerateCompletion(String userMessage) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(LlamaConfig.LLAMA_API_URL);

//            httpPost.setHeader("Content-Type", "application/json");

            // Тело запроса в JSON
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", LlamaConfig.LLAMA_MODEL);
            requestBody.put("prompt", userMessage);
            requestBody.put("stream", false);

            // Конвертируем Map → JSON
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(requestBody);

            httpPost.setEntity(new StringEntity(jsonBody));

            // Отправка запроса
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return EntityUtils.toString(entity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Ошибка запроса";
    }
}
