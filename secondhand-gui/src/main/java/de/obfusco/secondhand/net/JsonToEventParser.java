package de.obfusco.secondhand.net;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.obfusco.secondhand.net.dto.Event;

import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonToEventParser {
    public Event parse(InputStream inputStream) {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").create();
        return gson.fromJson(new InputStreamReader(inputStream), Event.class);
    }
}
