package com.example.adotarpets.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RacaIntentService extends IntentService {

    public static final String ACTION_BUSCAR_RACA = "ACTION_BUSCAR_RACA";
    public static final String EXTRA_RESULTADO = "resultado";

    public RacaIntentService() {
        super("RacaIntentService");
    }

    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (ACTION_BUSCAR_RACA.equals(intent.getAction())) {
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

                    Intent broadcastIntent = new Intent(ACTION_BUSCAR_RACA);
                    broadcastIntent.putExtra(EXTRA_RESULTADO, json.toString());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}