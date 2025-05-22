package com.example.adotarpets;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.adotarpets.models.Animal;
import com.example.adotarpets.models.Cidade;
import com.example.adotarpets.models.Raca;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    EditText edBuscar;
    ListView listaAnimais;
    Gson gson;
    private List<Animal> animais = new ArrayList<>();
    private AnimalAdapter adapter;
    private Spinner spFinalidade, spRaca, spCidade;
    private List<Raca> listaRacas = new ArrayList<>();
    private List<Cidade> listaCidades = new ArrayList<>();

    public class AnimalAdapter extends ArrayAdapter<Animal> {

        public AnimalAdapter(Context context, List<Animal> animais) {
            super(context, 0, animais);
        }

        @NonNull
        @Override
        public View getView(int pos, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_animal, parent, false);
            }

            Animal animal = getItem(pos);

            TextView tvDescricao = convertView.findViewById(R.id.txtDescricao);
            TextView tvCor = convertView.findViewById(R.id.txtCor);
            TextView tvIdade = convertView.findViewById(R.id.txtIdade);
            TextView tvRaca = convertView.findViewById(R.id.txtRacaTipo);
            TextView tvCidade = convertView.findViewById(R.id.txtCidade);
            TextView tvContato = convertView.findViewById(R.id.txtContato);

            tvDescricao.setText("Descrição: " + animal.getDescricao());
            tvCor.setText("Cor: " + animal.getCor());
            tvIdade.setText("Idade: " + animal.getIdade() + " meses");

            if (animal.getRaca() != null) {
                tvRaca.setText("Raça: " + animal.getRaca().getDescricao());
            }

            if (animal.getCidade() != null) {
                tvCidade.setText("Cidade: " + animal.getCidade().getNome());
            }

            tvContato.setText("Contato: " + animal.getContato());

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
        listaAnimais = findViewById(R.id.lista_animais);
        listaAnimais.setAdapter(adapter);
        GsonBuilder bld = new GsonBuilder();
        gson = bld.create();

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

            buscarAnimais(url, listaAnimais);
        });
    }

    private void buscarAnimais(String urlStr, ListView listaAnimais) {
        new Thread(() -> {
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

                Type tipoLista = new TypeToken<List<Animal>>() {}.getType();
                List<Animal> animais = new Gson().fromJson(json.toString(), tipoLista);

                runOnUiThread(() -> {
                    AnimalAdapter adapter = new AnimalAdapter(this, animais);
                    listaAnimais.setAdapter(adapter);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Erro ao buscar animais", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


    public void cadastrarAnimal(Animal animal) {
        new Thread(() -> {
            try {
                URL url = new URL("http://argo.td.utfpr.edu.br/pets/ws/animal");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String json = new Gson().toJson(animal);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes());
                }

                if (conn.getResponseCode() == 200 || conn.getResponseCode() == 201) {
                    Log.d("POST", "Animal cadastrado com sucesso!");
                } else {
                    Log.e("POST", "Erro ao cadastrar animal: " + conn.getResponseCode());
                }

            } catch (Exception e) {
                Log.e("POST", "Erro: " + e.getMessage());
            }
        }).start();
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