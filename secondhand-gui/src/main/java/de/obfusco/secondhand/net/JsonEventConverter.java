package de.obfusco.secondhand.net;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.obfusco.secondhand.net.dto.Event;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class JsonEventConverter {
    public Event parse(String string) {
        return parse(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
    }

    public Event parse(InputStream inputStream) {
        Gson gson = createGson();
        return gson.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), Event.class);
    }

    private Gson createGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").create();
    }

    public String toJson(Event event) {
        Gson gson = createGson();
        return gson.toJson(event);
    }
}
