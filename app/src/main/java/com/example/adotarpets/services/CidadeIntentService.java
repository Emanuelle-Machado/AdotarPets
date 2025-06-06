package com.example.adotarpets.services;

import android.app.IntentService;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.adotarpets.models.Cidade;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class CidadeIntentService extends IntentService {
    public static final String ACTION_BUSCAR_CIDADES = "com.example.adotarpets.BUSCAR_CIDADES";
    public static final String ACTION_CADASTRAR_CIDADE = "com.example.adotarpets.CADASTRAR_CIDADE";
    public static final String EXTRA_SUCCESS = "success";
    public static final String EXTRA_RESULTADO = "resultado";
    public static final String EXTRA_ERROR = "error";

    public CidadeIntentService() {
        super("CidadeIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_BUSCAR_CIDADES.equals(action)) {
                buscarCidades();
            } else if (ACTION_CADASTRAR_CIDADE.equals(action)) {
                String ddd = intent.getStringExtra("ddd");
                String nome = intent.getStringExtra("nome");
                cadastrarCidade(ddd, nome);
            }
        }
    }

    private void buscarCidades() {
        Intent broadcast = new Intent(ACTION_BUSCAR_CIDADES);
        try {
            URL url = new URL("http://argo.td.utfpr.edu.br/pets/ws/cidade");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder json = new StringBuilder();
            String linha;
            while ((linha = reader.readLine()) != null) {
                json.append(linha);
            }

            Type tipoLista = new TypeToken<List<Cidade>>() {}.getType();
            List<Cidade> cidades = new Gson().fromJson(json.toString(), tipoLista);

            broadcast.putExtra(EXTRA_SUCCESS, true);
            broadcast.putExtra(EXTRA_RESULTADO, json.toString());
        } catch (Exception e) {
            broadcast.putExtra(EXTRA_SUCCESS, false);
            broadcast.putExtra(EXTRA_ERROR, e.getMessage());
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    private void cadastrarCidade(String ddd, String nome) {
        Intent broadcast = new Intent(ACTION_CADASTRAR_CIDADE);
        try {
            URL url = new URL("http://argo.td.utfpr.edu.br/pets/ws/cidade");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("ddd", ddd);
            json.put("nome", nome);

            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode > 200 && responseCode < 300) {
                broadcast.putExtra(EXTRA_SUCCESS, true);
            } else {
                broadcast.putExtra(EXTRA_SUCCESS, false);
                broadcast.putExtra(EXTRA_ERROR, "Erro ao cadastrar cidade: " + responseCode);
            }
        } catch (Exception e) {
            broadcast.putExtra(EXTRA_SUCCESS, false);
            broadcast.putExtra(EXTRA_ERROR, e.getMessage());
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }
}