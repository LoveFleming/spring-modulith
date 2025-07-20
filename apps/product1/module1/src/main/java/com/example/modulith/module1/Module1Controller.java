package com.example.modulith.module1;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Module1Controller {
    
    

    @GetMapping("/")
    public String hello() {
        String sourceJson = "{\n" +
                "    \"key1\": {\n" +
                "         \"field1\": \"field1 value\"\n" +
                "    },\n" +
                "    \"key2\": {\n" +
                "         \"key22\": {\n" +
                "            \"field2\": \"value2\",\n" +
                "            \"field3\": \"value3\"\n" +
                "         }\n" +
                "    },\n" +
                "    \"key3\": [\n" +
                "          \"value4\", \"value5\"\n" +
                "    ]\n" +
                "}";
        String templateJson = "{\n" +
                "    \"k1\": \"{{ $key1.field1 }}\",\n" +
                "    \"k22\": [\n" +
                "        {\n" +
                "            \"name\": \"type\",\n" +
                "            \"value\": \"{{ $key2.key22 }}\"\n" +
                "        }\n" +
                "    ],\n" +
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
        // 將 template 轉為字串
        String templateStr = template.toString();

        // 先處理有雙引號包住的 placeholder: "{{ $... }}"
        Pattern quotedPattern = Pattern.compile("\"\\{\\{\\s*\\$(.*?)\\s*}}\"");
        Matcher quotedMatcher = quotedPattern.matcher(templateStr);
        StringBuffer quotedSb = new StringBuffer();
        while (quotedMatcher.find()) {
            String expr = quotedMatcher.group(1).trim();
            JsonElement value = getValueByPath(source, expr);
            String replacement;
            if (value == null || value.isJsonNull()) {
                replacement = "null";
            } else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                // 字串型態保留雙引號
                replacement = "\"" + escapeJsonString(value.getAsString()) + "\"";
            } else {
                // object/array 直接 toString（不加雙引號）
                replacement = value.toString();
            }
            quotedMatcher.appendReplacement(quotedSb, Matcher.quoteReplacement(replacement));
        }
        quotedMatcher.appendTail(quotedSb);

        // 再處理沒被雙引號包住的 placeholder: {{ $... }}
        String partiallyReplaced = quotedSb.toString();
        Pattern plainPattern = Pattern.compile("\\{\\{\\s*\\$(.*?)\\s*}}");
        Matcher plainMatcher = plainPattern.matcher(partiallyReplaced);
        StringBuffer plainSb = new StringBuffer();
        while (plainMatcher.find()) {
            String expr = plainMatcher.group(1).trim();
            JsonElement value = getValueByPath(source, expr);
            String replacement;
            if (value == null || value.isJsonNull()) {
                replacement = "null";
            } else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                // 字串型態需加上引號
                replacement = "\"" + escapeJsonString(value.getAsString()) + "\"";
            } else {
                // object/array 直接 toString
                replacement = value.toString();
            }
            plainMatcher.appendReplacement(plainSb, Matcher.quoteReplacement(replacement));
        }
        plainMatcher.appendTail(plainSb);

        // 轉回 JsonObject
        return com.google.gson.JsonParser.parseString(plainSb.toString()).getAsJsonObject();
    }

    // 依 key path 取值，支援巢狀
    private JsonElement getValueByPath(JsonObject source, String path) {
        String[] parts = path.split("\\.");
        JsonElement value = source;
        for (String part : parts) {
            if (value != null && value.isJsonObject() && value.getAsJsonObject().has(part)) {
                value = value.getAsJsonObject().get(part);
            } else {
                return JsonNull.INSTANCE;
            }
        }
        return value;
    }

    // 處理字串 escape
    private String escapeJsonString(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }



    
}