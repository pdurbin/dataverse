package edu.harvard.iq.dataverse.export.croissant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import edu.harvard.iq.dataverse.util.json.JsonUtil;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;

public class CroissantExportUtilTest {

    @Test
    void testGetReviews() {
        String content = """
                {
                  "reviews": [
                    {
                      "title": "Review of Pediatric Asthma",
                      "persistentId": "doi:10.5072/FK2/UWVWPY",
                      "persistentIdUrl": "https://doi.org/10.5072/FK2/UWVWPY",
                      "id": 243,
                      "citation": "Wazowski, Mike, 2026, \\\"Review of Pediatric Asthma\\\", https://doi.org/10.5072/FK2/UWVWPY, Root, V1",
                      "citationHtml": "Wazowski, Mike, 2026, \\\"Review of Pediatric Asthma\\\", <a href=\\\"https://doi.org/10.5072/FK2/UWVWPY\\\" target=\\\"_blank\\\">https://doi.org/10.5072/FK2/UWVWPY</a>, Root, V1"
                    }
                  ]
                }
                        """;
        JsonObject croissantJson = JsonUtil.getJsonObject(content);
        JsonObjectBuilder job = Json.createObjectBuilder(croissantJson);
        String title = "A Dataset Being Reviewed";
        JsonObject result = CroissantExportUtil.getReviews(job, title).build();
        System.out.println(prettyPrint(result));
        // System.out.println(result);
        // writeJson(result);
        assertTrue(result.getJsonArray("reviews").size() == 1);
        assertEquals("CriticReview", result.getJsonArray("reviews").get(0).asJsonObject().getString("@type"));
    }

    // private void writeJson(JsonObject result) {
    //     Path out = Paths.get("/tmp/reviews.json");
    //     try {
    //         Files.writeString(out, prettyPrint(result), StandardCharsets.UTF_8);
    //     } catch (IOException e) {
    //         // TODO Auto-generated catch block
    //         e.printStackTrace();
    //     }
    // }

    // public static String prettyPrint(String jsonObject) {
    //     try {
    //         return prettyPrint(getJsonObject(jsonObject));
    //     } catch (Exception ex) {
    //         return jsonObject;
    //     }
    // }

    public static String prettyPrint(JsonObject jsonObject) {
        Map<String, Boolean> config = new HashMap<>();
        config.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory jsonWriterFactory = Json.createWriterFactory(config);
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter jsonWriter = jsonWriterFactory.createWriter(stringWriter)) {
            jsonWriter.writeObject(jsonObject);
        }
        return stringWriter.toString();
    }

    // public static JsonObject getJsonObject(String serializedJson) {
    //     try (StringReader rdr = new StringReader(serializedJson)) {
    //         try (JsonReader jsonReader = Json.createReader(rdr)) {
    //             return jsonReader.readObject();
    //         }
    //     }
    // }

}
