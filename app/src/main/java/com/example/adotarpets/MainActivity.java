package com.example.adotarpets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.adotarpets.models.Animal;
import com.example.adotarpets.models.Cidade;
import com.example.adotarpets.models.Raca;
import com.example.adotarpets.services.AnimalIntentService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listaAnimais;
    private List<Animal> animais = new ArrayList<>();
    private AnimalAdapter adapter;
    private Spinner spFinalidade, spRaca, spCidade;
    private List<Raca> listaRacas = new ArrayList<>();
    private List<Cidade> listaCidades = new ArrayList<>();

    public class AnimalAdapter extends ArrayAdapter<Animal> {
        private final Context context;
        private final List<Animal> animais;

        public AnimalAdapter(Context context, List<Animal> animais) {
            super(context, 0, animais);
            this.context = context;
            this.animais = animais;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            Animal animal = animais.get(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_animal, parent, false);
            }

            TextView txtDescricao = convertView.findViewById(R.id.txtDescricao);
            TextView txtCor = convertView.findViewById(R.id.txtCor);
            TextView txtIdade = convertView.findViewById(R.id.txtIdade);
            TextView txtRaca = convertView.findViewById(R.id.txtRaca);
            TextView txtTipo = convertView.findViewById(R.id.txtTipo);
            TextView txtCidade = convertView.findViewById(R.id.txtCidade);
            TextView txtContato = convertView.findViewById(R.id.txtContato);
            TextView txtFinalidade = convertView.findViewById(R.id.txtFinalidade);
            TextView txtValor = convertView.findViewById(R.id.txtValor);

            txtDescricao.setText("Descrição: " + animal.getDescricao());
            txtCor.setText("Cor: " + animal.getCor());
            txtIdade.setText("Idade: " + animal.getIdade() + " meses");
            txtRaca.setText("Raça: " + animal.getRaca().getDescricao());
            txtTipo.setText("Tipo: " + animal.getRaca().getTipo().getDescricao());
            txtCidade.setText("Cidade: " + animal.getCidade().getNome());
            txtContato.setText("Contato: " + animal.getContato());
            txtFinalidade.setText("Finalidade: " + (animal.getFinalidade().equals("D") ? "Doação" : "Adoção"));
            txtValor.setText("Valor: R$ " + animal.getValor());

            return convertView;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (!isInternetAvailable()) {
            new AlertDialog.Builder(this)
                    .setTitle("Sem Conexão")
                    .setMessage("O aplicativo não pode ser usado offline. Verifique sua conexão com a internet.")
                    .setCancelable(false)
                    .setPositiveButton("Fechar", (dialog, which) -> finish())
                    .show();
        }

        listaAnimais = findViewById(R.id.lista_animais);
        if (adapter == null) {
            adapter = new AnimalAdapter(MainActivity.this, animais);
            listaAnimais.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.addAll(animais);
            adapter.notifyDataSetChanged();
        }
        spFinalidade = findViewById(R.id.spFinalidade);
        spRaca = findViewById(R.id.spRaca);
        spCidade = findViewById(R.id.spCidade);

        // Finalidade: Adoção ou Doação
        List<String> finalidades = Arrays.asList("Todas", "Adoção", "Doação");
        ArrayAdapter<String> finalidadeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, finalidades);
        spFinalidade.setAdapter(finalidadeAdapter);

        carregarRacas();
        carregarCidades();

        Button btnBuscar = findViewById(R.id.btnBuscar);
        Button btnLimpar = findViewById(R.id.btnLimpar);
        EditText edIdadeDe = findViewById(R.id.edIdadeDe);
        EditText edIdadeAte = findViewById(R.id.edIdadeAte);
        EditText edDDD = findViewById(R.id.edDDD);

        btnBuscar.setOnClickListener(v -> {
            String url = "http://argo.td.utfpr.edu.br/pets/ws/animal?";

            // Finalidade
            String finalidadeSelecionada = spFinalidade.getSelectedItem().toString();
            if (finalidadeSelecionada.equals("Adoção")) {
                url += "finalidade=D&";
            } else if (finalidadeSelecionada.equals("Doação")) {
                url += "finalidade=A&";
            }

            // Idade
            if (!edIdadeDe.getText().toString().trim().isEmpty()) {
                url += "idadeDe=" + edIdadeDe.getText().toString().trim() + "&";
            }
            if (!edIdadeAte.getText().toString().trim().isEmpty()) {
                url += "idadeAte=" + edIdadeAte.getText().toString().trim() + "&";
            }

            // DDD
            if (!edDDD.getText().toString().trim().isEmpty()) {
                url += "ddd=" + edDDD.getText().toString().trim() + "&";
            }

            // Cidade
            Cidade cidade = (Cidade) spCidade.getSelectedItem();
            if (cidade != null) {
                url += "idCidade=" + cidade.getId() + "&";
            }

            // Raça
            Raca raca = (Raca) spRaca.getSelectedItem();
            if (raca != null) {
                url += "idRaca=" + raca.getId() + "&";
            }

            Intent intent = new Intent(this, AnimalIntentService.class);
            intent.setAction(AnimalIntentService.ACTION_BUSCAR_ANIMAIS);
            intent.putExtra("url", url);
            startService(intent);
        });

        btnLimpar.setOnClickListener(v -> {
            edIdadeDe.setText("");
            edIdadeAte.setText("");
            edDDD.setText("");
            spFinalidade.setSelection(0); // "Todas"
            spRaca.setSelection(0);       // "Todas"
            spCidade.setSelection(0);     // "Todas"
        });
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AnimalIntentService.ACTION_BUSCAR_ANIMAIS.equals(intent.getAction())) {
                String json = intent.getStringExtra(AnimalIntentService.EXTRA_RESULTADO);

                Type tipoLista = new TypeToken<List<Animal>>() {}.getType();
                List<Animal> animais = new Gson().fromJson(json, tipoLista);

                adapter = new AnimalAdapter(MainActivity.this, animais);
                listaAnimais.setAdapter(adapter);
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, new IntentFilter(AnimalIntentService.ACTION_BUSCAR_ANIMAIS));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    private void carregarRacas() {
        new Thread(() -> {
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
                listaRacas = new Gson().fromJson(json.toString(), tipoLista);
                listaRacas.add(0, null); // Para opção "Todas"

                runOnUiThread(() -> {
                    ArrayAdapter<Raca> adapter = new ArrayAdapter<Raca>(this,
                            android.R.layout.simple_spinner_item, listaRacas) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            TextView view = (TextView) super.getView(position, convertView, parent);
                            if (getItem(position) == null) view.setText("Todas");
                            return view;
                        }

                        @Override
                        public View getDropDownView(int position, View convertView, ViewGroup parent) {
                            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                            if (getItem(position) == null) view.setText("Todas");
                            return view;
                        }
                    };
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spRaca.setAdapter(adapter);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void carregarCidades() {
        new Thread(() -> {
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
                listaCidades = new Gson().fromJson(json.toString(), tipoLista);
                listaCidades.add(0, null); // Para opção "Todas"

                runOnUiThread(() -> {
                    ArrayAdapter<Cidade> adapter = new ArrayAdapter<Cidade>(this,
                            android.R.layout.simple_spinner_item, listaCidades) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            TextView view = (TextView) super.getView(position, convertView, parent);
                            if (getItem(position) == null) view.setText("Todas");
                            return view;
                        }

                        @Override
                        public View getDropDownView(int position, View convertView, ViewGroup parent) {
                            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                            if (getItem(position) == null) view.setText("Todas");
                            return view;
                        }
                    };
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spCidade.setAdapter(adapter);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}