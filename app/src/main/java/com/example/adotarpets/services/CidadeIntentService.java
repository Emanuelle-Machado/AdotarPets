package com.example.adotarpets.services;

import android.app.IntentService;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CidadeIntentService extends IntentService {
    public static final String ACAO_CADASTRAR = "CADASTRAR_CIDADE";
    public static final String ACAO_EDITAR = "EDITAR_CIDADE";
    public static final String ACAO_BUSCAR = "BUSCAR_CIDADE";
    public static final String EXTRA_RESULTADO = "resultado";

    public CidadeIntentService() {
        super("CidadeIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String acao = intent.getAction();
        try {
            if (ACAO_CADASTRAR.equals(acao)) {
                String nome = intent.getStringExtra("nome");
                JSONObject json = new JSONObject();
                json.put("nome", nome);

                URL url = new URL("http://argo.td.utfpr.edu.br/pets/ws/cidade");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();
                conn.getResponseCode(); // apenas dispara

            } else if (ACAO_EDITAR.equals(acao)) {
                int id = intent.getIntExtra("id", -1);
                String nome = intent.getStringExtra("nome");

                JSONObject json = new JSONObject();
                json.put("id", id);
                json.put("nome", nome);

                URL url = new URL("http://argo.td.utfpr.edu.br/pets/ws/cidade/" + id);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();
                conn.getResponseCode();

            } else if (ACAO_BUSCAR.equals(acao)) {
                int id = intent.getIntExtra("id", -1);
                URL url = new URL("http://argo.td.utfpr.edu.br/pets/ws/cidade/" + id);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder resultado = new StringBuilder();
                String linha;
                while ((linha = reader.readLine()) != null) {
                    resultado.append(linha);
                }

                Intent resposta = new Intent(ACAO_BUSCAR);
                resposta.putExtra("resultado", resultado.toString());
                LocalBroadcastManager.getInstance(this).sendBroadcast(resposta);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
