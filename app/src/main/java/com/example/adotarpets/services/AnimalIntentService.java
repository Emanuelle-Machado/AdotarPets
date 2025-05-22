package com.example.adotarpets.services;

import android.app.IntentService;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AnimalIntentService extends IntentService {

    public static final String ACTION_BUSCAR_ANIMAIS = "BUSCAR_ANIMAIS";
    public static final String EXTRA_RESULTADO = "resultado";

    public AnimalIntentService() {
        super("AnimalIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && ACTION_BUSCAR_ANIMAIS.equals(intent.getAction())) {
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
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
