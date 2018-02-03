package de.obfusco.secondhand.net;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.obfusco.secondhand.net.dto.Transaction;
import de.obfusco.secondhand.storage.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Component
public class TransactionUploader {
    @Autowired
    TransactionRepository transactionRepository;

    public boolean upload(String baseUrl, String token) throws IOException {
        URL url = new URL(String.format("http://%s/api/event/transactions", baseUrl));
        return uploadJson(url, token, createJson());
    }

    private boolean uploadJson(URL url, String token, String json) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String authorization = "Token " + token;
        connection.setRequestProperty("Authorization", authorization);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(json.getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();
        int responseCode = connection.getResponseCode();
        return responseCode < 400;
    }

    private String createJson() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").create();
        StorageConverter storageConverter = new StorageConverter();
        List<Transaction> transactions = storageConverter.convertToTransactions(transactionRepository.findAll());
        return gson.toJson(transactions);
    }
}
