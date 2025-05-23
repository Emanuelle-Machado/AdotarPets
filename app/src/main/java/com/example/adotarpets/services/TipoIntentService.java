package com.example.adotarpets.services;

import android.app.IntentService;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TipoIntentService extends IntentService {

    public static final String ACTION_BUSCAR_TIPO = "ACTION_BUSCAR_TIPO";
    public static final String EXTRA_RESULTADO = "resultado";
    public static final String EXTRA_ERRO = "erro";

    public TipoIntentService() {
        super("TipoIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && ACTION_BUSCAR_TIPO.equals(intent.getAction())) {
            String urlStr = intent.getStringExtra("url");
            Intent broadcastIntent = new Intent(ACTION_BUSCAR_TIPO);

            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000); // Timeout de 5 segundos
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder json = new StringBuilder();
                    String linha;
                    while ((linha = reader.readLine()) != null) {
                        json.append(linha);
                    }
                    reader.close();
                    conn.disconnect();

                    broadcastIntent.putExtra(EXTRA_RESULTADO, json.toString());
                } else {
                    broadcastIntent.putExtra(EXTRA_ERRO, "Erro na requisição: " + responseCode);
                }
            } catch (Exception e) {
                broadcastIntent.putExtra(EXTRA_ERRO, e.getMessage());
            }

            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        }
    }
}