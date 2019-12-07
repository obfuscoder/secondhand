package de.obfusco.secondhand.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class EventDownloader {
    public InputStream downloadEventData(String baseUrl, String token) throws IOException {
        URL url = new URL(baseUrl + "api/event");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String authorization = "Token " + token;
        connection.setRequestProperty("Authorization", authorization);
        return new GZIPInputStream(connection.getInputStream());
    }
}
