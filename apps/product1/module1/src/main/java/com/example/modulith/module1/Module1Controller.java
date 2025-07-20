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

/**
     * resolvePlaceholder2:
     * 1. 遞迴 template Map，尋找所有 value 為 {{ $... }} 的字串
     * 2. 解析路徑，從 source Map 取值，綁定回 template
     * 3. 支援巢狀 Map/List 結構
     * 4. 回傳結果為 JSONObject
     */
    public JsonObject resolvePlaceholder2(Map<String, Object> source, Map<String, Object> template) {
        Map<String, Object> result = deepCopyAndBind(template, source, new ArrayList<>());
        // log: 完成後的 result
        System.out.println("[resolvePlaceholder2] result=" + result);
        // 轉回 JsonObject
        com.google.gson.Gson gson = new com.google.gson.Gson();
        return gson.toJsonTree(result).getAsJsonObject();
    }

    // 遞迴處理 Map/List，遇到 {{ $... }} 字串則取值替換
    @SuppressWarnings("unchecked")
    private Map<String, Object> deepCopyAndBind(Map<String, Object> template, Map<String, Object> source, List<String> pathStack) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : template.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            List<String> currentPath = new ArrayList<>(pathStack);
            currentPath.add(key);

            if (value instanceof String) {
                String strVal = (String) value;
                // 檢查是否為 {{ $... }} pattern
                Pattern p = Pattern.compile("\\{\\{\\s*\\$(.*?)\\s*}}");
                Matcher m = p.matcher(strVal);
                if (m.matches()) {
                    String expr = m.group(1).trim();
                    Object resolved = getValueByPathFromMap(source, expr);
                    // log: 匹配到 placeholder
                    System.out.println("[deepCopyAndBind] path=" + String.join(".", currentPath) + " placeholder=" + strVal + " expr=" + expr + " resolved=" + resolved);
                    result.put(key, resolved);
                } else {
                    result.put(key, value);
                }
            } else if (value instanceof Map) {
                // 巢狀 Map
                result.put(key, deepCopyAndBind((Map<String, Object>) value, source, currentPath));
            } else if (value instanceof List) {
                // 巢狀 List
                result.put(key, deepCopyAndBindList((List<Object>) value, source, currentPath));
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    // 遞迴處理 List
    @SuppressWarnings("unchecked")
    private List<Object> deepCopyAndBindList(List<Object> templateList, Map<String, Object> source, List<String> pathStack) {
        List<Object> result = new ArrayList<>();
        int idx = 0;
        for (Object item : templateList) {
            List<String> currentPath = new ArrayList<>(pathStack);
            currentPath.add("[" + idx + "]");
            if (item instanceof String) {
                String strVal = (String) item;
                Pattern p = Pattern.compile("\\{\\{\\s*\\$(.*?)\\s*}}");
                Matcher m = p.matcher(strVal);
                if (m.matches()) {
                    String expr = m.group(1).trim();
                    Object resolved = getValueByPathFromMap(source, expr);
                    System.out.println("[deepCopyAndBindList] path=" + String.join(".", currentPath) + " placeholder=" + strVal + " expr=" + expr + " resolved=" + resolved);
                    result.add(resolved);
                } else {
                    result.add(item);
                }
            } else if (item instanceof Map) {
                result.add(deepCopyAndBind((Map<String, Object>) item, source, currentPath));
            } else if (item instanceof List) {
                result.add(deepCopyAndBindList((List<Object>) item, source, currentPath));
            } else {
                result.add(item);
            }
            idx++;
        }
        return result;
    }

    // 依 key path 取值，支援巢狀 Map
    private Object getValueByPathFromMap(Map<String, Object> source, String path) {
        String[] parts = path.split("\\.");
        Object value = source;
        for (String part : parts) {
            if (value instanceof Map && ((Map<?, ?>) value).containsKey(part)) {
                value = ((Map<?, ?>) value).get(part);
            } else {
                // log: 路徑不存在
                System.out.println("[getValueByPathFromMap] path=" + path + " not found at part=" + part);
                return null;
            }
        }
        return value;
    }

    
}