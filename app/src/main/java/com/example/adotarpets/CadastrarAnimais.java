package com.example.adotarpets;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.adotarpets.models.Cidade;
import com.example.adotarpets.models.Raca;
import com.example.adotarpets.models.Tipo;
import com.example.adotarpets.services.AnimalIntentService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CadastrarAnimais extends AppCompatActivity {

    private EditText editDescricao, editIdade, editValor;
    private Spinner spinnerTipo, spinnerRaca, spinnerCidade, spinnerFinalidade;
    private Button btnCadastrar;
    private List<Raca> listaRacas = new ArrayList<>();
    private List<Cidade> listaCidades = new ArrayList<>();
    private List<Tipo> listaTipos = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastrar_animais);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        carregarTipos();
        carregarRacas();
        carregarCidades();

        editDescricao = findViewById(R.id.editTextDescricao);
        editIdade = findViewById(R.id.editTextIdade);
        editValor = findViewById(R.id.editTextValor);
        spinnerTipo = findViewById(R.id.spinnerTipo);
        spinnerRaca = findViewById(R.id.spinnerRaca);
        spinnerCidade = findViewById(R.id.spinnerCidade);
        spinnerFinalidade = findViewById(R.id.spinnerFinalidade);
        btnCadastrar = findViewById(R.id.btn_salvar);
        // Finalidade: Adoção ou Doação
        List<String> finalidades = Arrays.asList("Finalidade", "Adoção", "Doação");
        ArrayAdapter<String> finalidadeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, finalidades);
        spinnerFinalidade.setAdapter(finalidadeAdapter);

        btnCadastrar.setOnClickListener(view -> cadastrarAnimal());
    }

    private void cadastrarAnimal() {
        try {
            // Validate inputs
            String descricao = editDescricao.getText().toString().trim();
            if (descricao.isEmpty()) {
                Toast.makeText(this, "Descrição é obrigatória", Toast.LENGTH_SHORT).show();
                return;
            }

            String idadeStr = editIdade.getText().toString().trim();
            if (idadeStr.isEmpty()) {
                Toast.makeText(this, "Idade é obrigatória", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate spinners
            Tipo selectedTipo = (Tipo) spinnerTipo.getSelectedItem();
            if (selectedTipo == null) {
                Toast.makeText(this, "Selecione um tipo válido", Toast.LENGTH_SHORT).show();
                return;
            }

            Raca selectedRaca = (Raca) spinnerRaca.getSelectedItem();
            if (selectedRaca == null) {
                Toast.makeText(this, "Selecione uma raça válida", Toast.LENGTH_SHORT).show();
                return;
            }

            Cidade selectedCidade = (Cidade) spinnerCidade.getSelectedItem();
            if (selectedCidade == null) {
                Toast.makeText(this, "Selecione uma cidade válida", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject json = new JSONObject();
            json.put("descricao", descricao);
            json.put("idade", Integer.parseInt(idadeStr));
            json.put("finalidade", spinnerFinalidade.getSelectedItem().toString());

            String valor = editValor.getText().toString().trim();
            if (!valor.isEmpty()) {
                json.put("valor", Double.parseDouble(valor));
            }

            // Use actual IDs from objects (assuming Tipo, Raca, Cidade have getId() methods)
            json.put("idTipo", selectedTipo.getId());
            json.put("idRaca", selectedRaca.getId());
            json.put("idCidade", selectedCidade.getId());

            Intent intent = new Intent(this, AnimalIntentService.class);
            intent.setAction(AnimalIntentService.ACTION_CADASTRAR_ANIMAL);
            intent.putExtra("json", json.toString());
            startService(intent);

            Toast.makeText(this, "Cadastro enviado!", Toast.LENGTH_SHORT).show();
            finish(); // Volta pra tela inicial

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Idade ou valor inválido", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao cadastrar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void carregarTipos() {
        new Thread(() -> {
            try {
                URL url = new URL("http://argo.td.utfpr.edu.br/pets/ws/tipo");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String linha;
                while ((linha = reader.readLine()) != null) {
                    json.append(linha);
                }

                Type tipoLista = new TypeToken<List<Tipo>>() {}.getType();
                listaTipos = new Gson().fromJson(json.toString(), tipoLista);
                listaTipos.add(0, null); // Para opção "Todas"

                runOnUiThread(() -> {
                    ArrayAdapter<Tipo> adapter = new ArrayAdapter<Tipo>(this,
                            android.R.layout.simple_spinner_item, listaTipos) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            TextView view = (TextView) super.getView(position, convertView, parent);
                            if (getItem(position) == null) {
                                view.setText("Tipos de Animais");
                            } else {
                                view.setText(getItem(position).getDescricao()); // Assuming Tipo has a getNome() method
                            }
                            return view;
                        }

                        @Override
                        public View getDropDownView(int position, View convertView, ViewGroup parent) {
                            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                            if (getItem(position) == null) {
                                view.setText("Tipos de Animais");
                            } else {
                                view.setText(getItem(position).getDescricao()); // Assuming Tipo has a getNome() method
                            }
                            return view;
                        }
                    };
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerTipo.setAdapter(adapter);
                });

            } catch (Exception e) {
                e.printStackTrace();
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

                Type racaLista = new TypeToken<List<Raca>>() {}.getType();
                listaRacas = new Gson().fromJson(json.toString(), racaLista);
                listaRacas.add(0, null); // Para opção "Todas"

                runOnUiThread(() -> {
                    ArrayAdapter<Raca> adapter = new ArrayAdapter<Raca>(this,
                            android.R.layout.simple_spinner_item, listaRacas) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            TextView view = (TextView) super.getView(position, convertView, parent);
                            if (getItem(position) == null) {
                                view.setText("Raças");
                            } else {
                                view.setText(getItem(position).getDescricao()); // Assuming Raca has a getNome() method
                            }
                            return view;
                        }

                        @Override
                        public View getDropDownView(int position, View convertView, ViewGroup parent) {
                            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                            if (getItem(position) == null) {
                                view.setText("Raças");
                            } else {
                                view.setText(getItem(position).getDescricao()); // Assuming Raca has a getNome() method
                            }
                            return view;
                        }
                    };
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerRaca.setAdapter(adapter);
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

                Type cidadeLista = new TypeToken<List<Cidade>>() {}.getType();
                listaCidades = new Gson().fromJson(json.toString(), cidadeLista);
                listaCidades.add(0, null); // Para opção "Todas"

                runOnUiThread(() -> {
                    ArrayAdapter<Cidade> adapter = new ArrayAdapter<Cidade>(this,
                            android.R.layout.simple_spinner_item, listaCidades) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            TextView view = (TextView) super.getView(position, convertView, parent);
                            if (getItem(position) == null) {
                                view.setText("Cidades");
                            } else {
                                view.setText(getItem(position).getNome()); // Assuming Cidade has a getNome() method
                            }
                            return view;
                        }

                        @Override
                        public View getDropDownView(int position, View convertView, ViewGroup parent) {
                            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                            if (getItem(position) == null) {
                                view.setText("Cidades");
                            } else {
                                view.setText(getItem(position).getNome()); // Assuming Cidade has a getNome() method
                            }
                            return view;
                        }
                    };
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCidade.setAdapter(adapter);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}