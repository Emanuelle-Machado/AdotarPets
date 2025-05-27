package com.example.adotarpets.services;

import android.app.IntentService;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.reflect.Type;
import java.util.List;

public class RacaIntentService extends IntentService {
    public static final String ACTION_BUSCAR_RACAS = "com.example.adotarpets.BUSCAR_RACAS";
    public static final String ACTION_CADASTRAR_RACA = "com.example.adotarpets.CADASTRAR_RACA";
    public static final String EXTRA_SUCCESS = "success";
    public static final String EXTRA_RESULTADO = "resultado";
    public static final String EXTRA_ERROR = "error";

    public RacaIntentService() {
        super("RacaIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_BUSCAR_RACAS.equals(action)) {
                buscarRacas();
            } else if (ACTION_CADASTRAR_RACA.equals(action)) {
                String descricao = intent.getStringExtra("descricao");
                int idTipo = intent.getIntExtra("idTipo", -1);
                cadastrarRaca(descricao, idTipo);
            }
        }
    }

    private void buscarRacas() {
        Intent broadcast = new Intent(ACTION_BUSCAR_RACAS);
        try {
            URL url = new URL("http://argo.td.utfpr.edu.br/pets/ws/raca");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder json = new StringBuilder();
            String linha;
            while ((linha = reader.readLine()) != null) {
                json.append(linha);
            }
            reader.close();

            broadcast.putExtra(EXTRA_SUCCESS, true);
            broadcast.putExtra(EXTRA_RESULTADO, json.toString());
        } catch (Exception e) {
            broadcast.putExtra(EXTRA_SUCCESS, false);
            broadcast.putExtra(EXTRA_ERROR, e.getMessage());
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    private void cadastrarRaca(String descricao, int idTipo) {
        Intent broadcast = new Intent(ACTION_CADASTRAR_RACA);
        try {
            URL url = new URL("http://argo.td.utfpr.edu.br/pets/ws/raca");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("descricao", descricao);
            JSONObject tipoJson = new JSONObject();
            tipoJson.put("id", idTipo);
            json.put("tipo", tipoJson);

            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                broadcast.putExtra(EXTRA_SUCCESS, true);
            } else {
                broadcast.putExtra(EXTRA_SUCCESS, false);
                broadcast.putExtra(EXTRA_ERROR, "Erro ao cadastrar raça: " + responseCode);
            }
        } catch (Exception e) {
            broadcast.putExtra(EXTRA_SUCCESS, false);
            broadcast.putExtra(EXTRA_ERROR, e.getMessage());
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }
}