package de.obfusco.secondhand.net;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import de.obfusco.secondhand.net.dto.Event;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class JsonDataConverter {
    public Event parse(String string) {
        return parse(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
    }

    public Event parseBase64Compressed(String string) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
        Base64InputStream base64Stream = new Base64InputStream(bis);
        return parseCompressedStream(base64Stream);
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

    public String toJson(Object object) {
        Gson gson = createGson();
        return gson.toJson(object);
    }

    public void writeCompressedJsonToStream(Event event, OutputStream outputStream) throws IOException {
        Gson gson = createGson();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(gzipOutputStream, StandardCharsets.UTF_8));
        gson.toJson(event, Event.class, writer);
        writer.close();
    }

    public String toBase64CompressedJson(Event event) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Base64OutputStream base64Stream = new Base64OutputStream(bos, true, -1, null);
        writeCompressedJsonToStream(event, base64Stream);
        return bos.toString();
    }

    public Event parseCompressedStream(InputStream inputStream) throws IOException {
        GZIPInputStream gzipStream = new GZIPInputStream(inputStream);
        return parse(gzipStream);
    }
}
