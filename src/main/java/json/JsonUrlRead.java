package json;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.*;
import java.nio.file.Paths;

public class JsonUrlRead {
    private static final String PATH = Paths.get("src", "main","resources", "urls.json").toString();

    public static String[] readHttps() {
        final JSONParser parser = new JSONParser();
        try (final FileReader reader = new FileReader(PATH)) {
            final Object obj = parser.parse(reader);
            final JSONObject jsonObject = (JSONObject) obj;
            final JSONArray jsonArray = (JSONArray) jsonObject.get("https");

            return convertObjectJsonToArray(jsonArray);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return new String[0];
    }

    public static String[] readSnmp() {
        final JSONParser parser = new JSONParser();
        try (final FileReader reader = new FileReader(PATH)) {
            final Object obj = parser.parse(reader);
            final JSONObject jsonObject = (JSONObject) obj;
            final JSONArray jsonArray = (JSONArray) jsonObject.get("snmp");

            return convertObjectJsonToArray(jsonArray);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return new String[0];
    }

    private static String[] convertObjectJsonToArray(final JSONArray toconvert) {
        String[] returnArray = new String[toconvert.size()];

        for(int i = 0; i < toconvert.size(); i++){
            returnArray[i] = toconvert.get(i).toString();
        }
        return returnArray;
    }
}
