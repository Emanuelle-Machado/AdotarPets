package com.example.adotarpets.services;

import android.app.IntentService;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.adotarpets.models.Raca;
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

            Type tipoLista = new TypeToken<List<Raca>>() {}.getType();
            List<Raca> racas = new Gson().fromJson(json.toString(), tipoLista);

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
            // Validação de entrada
            if (descricao == null || descricao.trim().isEmpty()) {
                broadcast.putExtra(EXTRA_SUCCESS, false);
                broadcast.putExtra(EXTRA_ERROR, "Descrição da raça não pode estar vazia");
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
                return;
            }
            if (idTipo <= 0) {
                broadcast.putExtra(EXTRA_SUCCESS, false);
                broadcast.putExtra(EXTRA_ERROR, "ID do tipo inválido");
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
                return;
            }

            URL url = new URL("http://argo.td.utfpr.edu.br/pets/ws/raca");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // Construção do JSON com idTipo diretamente
            JSONObject json = new JSONObject();
            json.put("descricao", descricao.trim());
            json.put("idTipo", idTipo);

            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                broadcast.putExtra(EXTRA_SUCCESS, true);
            } else {
                // Capturar detalhes do erro do servidor
                StringBuilder errorResponse = new StringBuilder();
                try {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorReader.close();
                } catch (Exception e) {
                    errorResponse.append("Não foi possível ler a mensagem de erro do servidor");
                }
                broadcast.putExtra(EXTRA_SUCCESS, false);
                broadcast.putExtra(EXTRA_ERROR, "Erro ao cadastrar raça: HTTP " + responseCode + " - " + errorResponse.toString());
            }
        } catch (Exception e) {
            broadcast.putExtra(EXTRA_SUCCESS, false);
            broadcast.putExtra(EXTRA_ERROR, "Exceção: " + e.getMessage());
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }
}