package com.example.modulith.module1;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Module1Controller {
    
    @GetMapping("/module1/hello")
    public String hello() {
        String sourceJson = "{\n" +
                "    \"key1\": {\n" +
                "         \"field1\": \"field1 value\"\n" +
                "    },\n" +
                "    \"key2\": {\n" +
                "         \"field2\": \"value2\",\n" +
                "         \"field3\": \"value3\"\n" +
                "    },\n" +
                "    \"key3\": [\n" +
                "          \"value4\", \"value5\"\n" +
                "    ]\n" +
                "}";
        String templateJson = "{\n" +
                "    \"k1\": \"{{ $key1.field1 }}\",\n" +
                "    \"k2\": \"{{ $key2 }}\",\n" +
                "    \"k3\": \"{{ $key3 }}\"\n" +
                "}";

        com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
        com.google.gson.JsonObject sourceObj = parser.parse(sourceJson).getAsJsonObject();
        com.google.gson.JsonObject templateObj = parser.parse(templateJson).getAsJsonObject();

        com.google.gson.JsonObject result = resolvePlaceholder(sourceObj, templateObj);
        return result.toString();
    }
    /**
     * 根據 template JsonObject 物件中的 placeholder（如 {{ $key1.field1 }}），
     * 從 source JsonObject 物件取值並產生 output JsonObject。
     */
    public JsonObject resolvePlaceholder(JsonObject source, JsonObject template) {
        JsonObject result = new JsonObject();
        for (String key : template.keySet()) {
            JsonElement value = template.get(key);
            result.add(key, resolveNode(source, value));
        }
        return result;
    }

    // 遞迴解析 template node
    private JsonElement resolveNode(JsonObject source, JsonElement templateNode) {
        if (templateNode.isJsonObject()) {
            JsonObject obj = new JsonObject();
            for (String key : templateNode.getAsJsonObject().keySet()) {
                obj.add(key, resolveNode(source, templateNode.getAsJsonObject().get(key)));
            }
            return obj;
        } else if (templateNode.isJsonArray()) {
            JsonArray arr = new JsonArray();
            for (JsonElement item : templateNode.getAsJsonArray()) {
                arr.add(resolveNode(source, item));
            }
            return arr;
        } else if (templateNode.isJsonPrimitive() && templateNode.getAsJsonPrimitive().isString()) {
            String str = templateNode.getAsString();
            Pattern pattern = Pattern.compile("\\{\\{\\s*\\$(.*?)\\s*}}");
            Matcher matcher = pattern.matcher(str);
            if (matcher.matches()) {
                // 完全符合 {{ $... }}，取出內容
                String expr = matcher.group(1).trim();
                String[] parts = expr.split("\\.");
                JsonElement value = source;
                for (String part : parts) {
                    if (value != null && value.isJsonObject() && value.getAsJsonObject().has(part)) {
                        value = value.getAsJsonObject().get(part);
                    } else {
                        value = JsonNull.INSTANCE;
                        break;
                    }
                }
                return value == null ? JsonNull.INSTANCE : value;
            } else {
                // 非 placeholder，原樣回傳
                return templateNode;
            }
        } else {
            // 其他型態直接回傳
            return templateNode;
        }
    }
}
