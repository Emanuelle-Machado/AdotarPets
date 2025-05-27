package com.example.adotarpets.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AnimalIntentService extends IntentService {

    public static final String ACTION_BUSCAR_ANIMAIS = "BUSCAR_ANIMAIS";
    public static final String ACTION_CADASTRAR_ANIMAL = "CADASTRAR_ANIMAL";
    public static final String ACTION_EDITAR_ANIMAL = "EDITAR_ANIMAL";
    public static final String EXTRA_RESULTADO = "resultado";
    public static final String EXTRA_SUCCESS = "success";
    public static final String EXTRA_ERROR = "error";

    public AnimalIntentService() {
        super("AnimalIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (ACTION_BUSCAR_ANIMAIS.equals(intent.getAction())) {
                String urlStr = intent.getStringExtra("url");

                try {
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder json = new StringBuilder();
                    String linha;
                    while ((linha = reader.readLine()) != null) {
                        json.append(linha);
                    }

                    Intent broadcastIntent = new Intent(ACTION_BUSCAR_ANIMAIS);
                    broadcastIntent.putExtra(EXTRA_RESULTADO, json.toString());
                    broadcastIntent.putExtra(EXTRA_SUCCESS, true);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

                } catch (Exception e) {
                    Log.e("BUSCAR_ANIMAIS", "Error: " + e.getMessage(), e);
                    Intent broadcastIntent = new Intent(ACTION_BUSCAR_ANIMAIS);
                    broadcastIntent.putExtra(EXTRA_SUCCESS, false);
                    broadcastIntent.putExtra(EXTRA_ERROR, e.getMessage());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                }
            } else if (ACTION_CADASTRAR_ANIMAL.equals(intent.getAction())) {
                String jsonStr = intent.getStringExtra("json");

                try {
                    URL url = new URL("http://argo.td.utfpr.edu.br/pets/ws/animal");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/json");

                    conn.getOutputStream().write(jsonStr.getBytes("UTF-8"));
                    int responseCode = conn.getResponseCode();
                    Log.d("CADASTRO", "POST animal -> " + responseCode);

                    Intent broadcastIntent = new Intent(ACTION_CADASTRAR_ANIMAL);
                    if (responseCode == 200 || responseCode == 201) {
                        broadcastIntent.putExtra(EXTRA_SUCCESS, true);
                    } else {
                        String errorMessage = readErrorResponse(conn);
                        Log.e("CADASTRO", "Error: " + errorMessage);
                        broadcastIntent.putExtra(EXTRA_SUCCESS, false);
                        broadcastIntent.putExtra(EXTRA_ERROR, "HTTP " + responseCode + ": " + errorMessage);
                    }
                    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

                } catch (Exception e) {
                    Log.e("CADASTRO", "Error: " + e.getMessage(), e);
                    Intent broadcastIntent = new Intent(ACTION_CADASTRAR_ANIMAL);
                    broadcastIntent.putExtra(EXTRA_SUCCESS, false);
                    broadcastIntent.putExtra(EXTRA_ERROR, e.getMessage());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                }
            } else if (ACTION_EDITAR_ANIMAL.equals(intent.getAction())) {
                String json = intent.getStringExtra("json");
                int id = intent.getIntExtra("id", -1);

                if (id == -1) {
                    Log.e("EDITAR", "Invalid animal ID");
                    Intent broadcastIntent = new Intent(ACTION_EDITAR_ANIMAL);
                    broadcastIntent.putExtra(EXTRA_SUCCESS, false);
                    broadcastIntent.putExtra(EXTRA_ERROR, "Invalid animal ID");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                    return;
                }

                try {
                    URL url = new URL("https://argo.td.utfpr.edu.br/pets/ws/animal/" + id);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("PUT");

                    conn.setRequestProperty("Content-Type", "application/json");

                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    os.write(json.getBytes("UTF-8"));
                    os.flush();
                    conn.getResponseCode();
                    conn.connect();
                    int responseCode = conn.getResponseCode();
                    Log.d("EDITAR", "PUT animal/" + id + " -> " + responseCode);

                    Intent broadcastIntent = new Intent(ACTION_EDITAR_ANIMAL);
                    if (responseCode == 200) {
                        broadcastIntent.putExtra(EXTRA_SUCCESS, true);
                    } else {
                        String errorMessage = readErrorResponse(conn);
                        Log.e("EDITAR", "Error: HTTP " + responseCode + " - " + errorMessage);
                        broadcastIntent.putExtra(EXTRA_SUCCESS, false);
                        broadcastIntent.putExtra(EXTRA_ERROR, "HTTP " + responseCode + ": " + errorMessage);
                    }
                    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

                } catch (Exception e) {
                    Log.e("EDITAR", "Error: " + e.getMessage(), e);
                    Intent broadcastIntent = new Intent(ACTION_EDITAR_ANIMAL);
                    broadcastIntent.putExtra(EXTRA_SUCCESS, false);
                    broadcastIntent.putExtra(EXTRA_ERROR, e.getMessage());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                }
            }
        }
    }

    private String readErrorResponse(HttpURLConnection conn) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            StringBuilder error = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                error.append(line);
            }
            reader.close();
            return error.toString();
        } catch (Exception e) {
            return "Unable to read error response: " + e.getMessage();
        }
    }
}