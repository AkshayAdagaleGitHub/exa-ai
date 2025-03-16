package com.mcp.mcp.server.component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;

import com.mcp.mcp.server.model.QueryRequest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;

import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class ClientController implements ToolCallbackProvider {

    @GetMapping(value = "/mcp/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamMessages() {
        return Flux.interval(Duration.ofSeconds(1))
                   .map(sequence -> "Message " + sequence + " at " + LocalTime.now());
    }

    @GetMapping(value = "/mcp/greetings", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamGreetings() {
        return Flux.interval(Duration.ofSeconds(1))
                   .map(sequence -> "Greeting " + sequence + " at " + LocalTime.now());
    }

    @PostMapping("/mcp/testingurl")
    public String postTestingUrl() {
        return "Hello World";
    }

    @PostMapping("/mcp/webhook")
    public String handleWebhook(@RequestBody String payload, @RequestHeader Map<String, String> headers) {
        headers.forEach((key, value) -> System.out.println(key + ": " + value));
        System.out.println("Payload: " + payload);
        return "Webhook received successfully";
    }

    @PostMapping("/mcp/exaai")
    public ResponseEntity<Map<String, Object>> handleWebhook2(@RequestBody QueryRequest queryRequest) {
        String query = queryRequest.getQuery().toLowerCase();
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, "{\n    \"query\": \"Find if any brands exist with the name " + query + "\",\n    \"contents\": {\n        \"text\": { \"maxCharacters\": 1000 }\n    }\n}");
        Request request = new Request.Builder()
                .url("https://api.exa.ai/search")
                .method("POST", body)
                .addHeader("content-type", "application/json")
                .addHeader("x-api-key", "<token>")
                .build();
        try {
            Response response = client.newCall(request).execute();
            return buildResponse(response.body());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<Map<String, Object>> buildResponse(okhttp3.ResponseBody responseBody) throws IOException {
        Map<String, Object> parsedResponse = parseJson(responseBody.string());
        return ResponseEntity.ok(parsedResponse);
    }

    public Map<String, Object> parseJson(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseMap = new HashMap<>();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            responseMap.put("requestId", rootNode.path("requestId").asText());
            responseMap.put("autopromptString", rootNode.path("autopromptString").asText());
            responseMap.put("resolvedSearchType", rootNode.path("resolvedSearchType").asText());

            JsonNode results = rootNode.path("results");
            List<Map<String, String>> resultList = new ArrayList<>();
            for (JsonNode result : results) {
                Map<String, String> resultMap = new HashMap<>();
                resultMap.put("id", result.path("id").asText());
                resultMap.put("title", result.path("title").asText());
                resultMap.put("url", result.path("url").asText());
                resultList.add(resultMap);
            }
            responseMap.put("results", resultList);

            return responseMap;
        } catch (Exception e) {
            e.printStackTrace();
            responseMap.put("error", "Error parsing JSON");
            return responseMap;
        }
    }

    @Override
    public FunctionCallback[] getToolCallbacks() {
        return new FunctionCallback[0];
    }
}
