import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class testMain {
    public static void main(String[] args) {
        String jsonPattern = "{\n" +
                "    \"field1\": \"{{ $.key1 }}\",\n" +
                "    \"field2\": [\n" +
                "        \"{{ $.key2 }}\"\n" +
                "    ]\n" +
                "}";

        // 正則表達式：抓取 {{ $.key }} 形式的 key
        Pattern pattern = Pattern.compile("\\{\\{\\s*\\$\\.([a-zA-Z0-9_]+)\\s*}}");
        Matcher matcher = pattern.matcher(jsonPattern);

        System.out.println("找到的 placeholder key：");
        while (matcher.find()) {
            String key = matcher.group(1);
            System.out.println(key);
        }
    }
}