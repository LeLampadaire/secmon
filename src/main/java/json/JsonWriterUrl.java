package json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonWriterUrl {

    public static void writeANewUrl(String protocol,String url) {
        final Path path = Paths.get("src", "main", "resources", "urls.json");
        final List<String> urlsHttps = new ArrayList<>(Arrays.asList(JsonUrlRead.readHttps()));
        final List<String> urlsSnmp = new ArrayList<>(Arrays.asList(JsonUrlRead.readSnmp()));

        final Matcher matcher = Pattern.compile("(?<id>.+?)!").matcher(url);

        if(matcher.find()){
            final String id = matcher.group("id");
            verifyID(id, urlsHttps);
            verifyID(id, urlsSnmp);

            if (protocol.toLowerCase().equals("https")) {
                urlsHttps.add(url);
            }else if (protocol.toLowerCase().equals("snmp")) {
                urlsSnmp.add(url);
            }

            final JsonObject jsonObject = new JsonObject();
            final JsonArray resulthttps = feedJsonArray(urlsHttps);
            final JsonArray resultsnmp = feedJsonArray(urlsSnmp);
            jsonObject.add("https",resulthttps);
            jsonObject.add("snmp",resultsnmp);

            try (final FileWriter file = new FileWriter(path.toString())) {
                file.write(jsonObject.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static JsonArray feedJsonArray(final List<String> urls) {
        final JsonArray array = new JsonArray();
        for (String url: urls) {
            array.add(url);
        }
        return array;
    }

    private static void verifyID(final String id, final List<String> urls) {
        for (final String url: urls) {
            final Matcher matcher = Pattern.compile("(?<id>.+?)!").matcher(url);
            if(matcher.find()){
                if (id.equals(matcher.group("id"))){
                    urls.remove(url);
                    break;
                }
            }
        }
    }
}
