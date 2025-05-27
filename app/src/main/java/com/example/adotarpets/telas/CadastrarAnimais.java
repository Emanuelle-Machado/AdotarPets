package com.example.adotarpets.telas;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.adotarpets.R;
import com.example.adotarpets.models.Animal;
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

    private EditText editDescricao, editIdade, editValor, editCor, editProprietario, editContato;
    private Spinner spinnerTipo, spinnerRaca, spinnerCidade, spinnerFinalidade;
    private Button btnCadastrar;
    private List<Raca> listaRacas = new ArrayList<>();
    private List<Cidade> listaCidades = new ArrayList<>();
    private List<Tipo> listaTipos = new ArrayList<>();
    private Animal animalParaEditar;

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

        // Initialize UI components
        editDescricao = findViewById(R.id.editTextDescricao);
        editIdade = findViewById(R.id.editTextIdade);
        editValor = findViewById(R.id.editTextValor);
        editCor = findViewById(R.id.editTextCor);
        editProprietario = findViewById(R.id.editTextProprietario);
        editContato = findViewById(R.id.editTextContato);
        spinnerTipo = findViewById(R.id.spinnerTipo);
        spinnerRaca = findViewById(R.id.spinnerRaca);
        spinnerCidade = findViewById(R.id.spinnerCidade);
        spinnerFinalidade = findViewById(R.id.spinnerFinalidade);
        btnCadastrar = findViewById(R.id.btn_salvar);

        // Set up the finalidade spinner
        List<String> finalidades = Arrays.asList("Finalidade", "Adoção", "Doação");
        ArrayAdapter<String> finalidadeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, finalidades);
        finalidadeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFinalidade.setAdapter(finalidadeAdapter);

        // Check if editing an existing animal
        animalParaEditar = (Animal) getIntent().getSerializableExtra("animal");
        if (animalParaEditar != null) {
            btnCadastrar.setText("Atualizar");
        } else {
            btnCadastrar.setText("Cadastrar");
        }

        // Load data for spinners
        carregarTipos();
        carregarRacas();
        carregarCidades();

        // Set button listener
        btnCadastrar.setOnClickListener(view -> cadastrarAnimal());

        // Register receiver for update results
        IntentFilter filter = new IntentFilter();
        filter.addAction(AnimalIntentService.ACTION_CADASTRAR_ANIMAL);
        filter.addAction(AnimalIntentService.ACTION_EDITAR_ANIMAL);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    private void cadastrarAnimal() {
        if (animalParaEditar == null) {
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

                String cor = editCor.getText().toString().trim();
                if (cor.isEmpty()) {
                    Toast.makeText(this, "Cor é obrigatória", Toast.LENGTH_SHORT).show();
                    return;
                }

                String proprietario = editProprietario.getText().toString().trim();
                if (proprietario.isEmpty()) {
                    Toast.makeText(this, "Proprietário é obrigatório", Toast.LENGTH_SHORT).show();
                    return;
                }

                String contato = editContato.getText().toString().trim();
                if (contato.isEmpty()) {
                    Toast.makeText(this, "Contato é obrigatório", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate spinners
                Tipo selectedTipo = (Tipo) spinnerTipo.getSelectedItem();
                if (selectedTipo == null || spinnerTipo.getSelectedItemPosition() == 0) {
                    Toast.makeText(this, "Selecione um tipo válido", Toast.LENGTH_SHORT).show();
                    return;
                }

                Raca selectedRaca = (Raca) spinnerRaca.getSelectedItem();
                if (selectedRaca == null || spinnerRaca.getSelectedItemPosition() == 0) {
                    Toast.makeText(this, "Selecione uma raça válida", Toast.LENGTH_SHORT).show();
                    return;
                }

                Cidade selectedCidade = (Cidade) spinnerCidade.getSelectedItem();
                if (selectedCidade == null || spinnerCidade.getSelectedItemPosition() == 0) {
                    Toast.makeText(this, "Selecione uma cidade válida", Toast.LENGTH_SHORT).show();
                    return;
                }

                String finalidade = spinnerFinalidade.getSelectedItem().toString();
                if (finalidade.equals("Finalidade")) {
                    Toast.makeText(this, "Selecione uma finalidade válida", Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject json = new JSONObject();
                json.put("descricao", descricao);
                json.put("idade", Integer.parseInt(idadeStr));
                json.put("cor", cor);
                json.put("proprietario", proprietario);
                json.put("contato", contato);
                json.put("finalidade", finalidade.equals("Adoção") ? "A" : "D");

                String valor = editValor.getText().toString().trim();
                if (!valor.isEmpty()) {
                    json.put("valor", Double.parseDouble(valor));
                } else {
                    json.put("valor", JSONObject.NULL);
                }

                json.put("idTipo", selectedTipo.getId());
                json.put("idRaca", selectedRaca.getId());
                json.put("idCidade", selectedCidade.getId());

                Intent intent = new Intent(this, AnimalIntentService.class);
                intent.setAction(AnimalIntentService.ACTION_CADASTRAR_ANIMAL);
                intent.putExtra("json", json.toString());
                startService(intent);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Idade ou valor inválido", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (Exception e) {
                Toast.makeText(this, "Erro ao cadastrar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            atualizarAnimal();
        }
    }

    private void atualizarAnimal() {
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

            String cor = editCor.getText().toString().trim();
            if (cor.isEmpty()) {
                Toast.makeText(this, "Cor é obrigatória", Toast.LENGTH_SHORT).show();
                return;
            }

            String proprietario = editProprietario.getText().toString().trim();
            if (proprietario.isEmpty()) {
                Toast.makeText(this, "Proprietário é obrigatório", Toast.LENGTH_SHORT).show();
                return;
            }

            String contato = editContato.getText().toString().trim();
            if (contato.isEmpty()) {
                Toast.makeText(this, "Contato é obrigatório", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate spinners
            Tipo selectedTipo = (Tipo) spinnerTipo.getSelectedItem();
            if (selectedTipo == null || spinnerTipo.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Selecione um tipo válido", Toast.LENGTH_SHORT).show();
                return;
            }

            Raca selectedRaca = (Raca) spinnerRaca.getSelectedItem();
            if (selectedRaca == null || spinnerRaca.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Selecione uma raça válida", Toast.LENGTH_SHORT).show();
                return;
            }

            Cidade selectedCidade = (Cidade) spinnerCidade.getSelectedItem();
            if (selectedCidade == null || spinnerCidade.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Selecione uma cidade válida", Toast.LENGTH_SHORT).show();
                return;
            }

            String finalidade = spinnerFinalidade.getSelectedItem().toString();
            if (finalidade.equals("Finalidade")) {
                Toast.makeText(this, "Selecione uma finalidade válida", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject json = new JSONObject();
            json.put("id", animalParaEditar.getId());
            json.put("descricao", descricao);
            json.put("idade", Integer.parseInt(idadeStr));
            json.put("cor", cor);
            json.put("proprietario", proprietario);
            json.put("contato", contato);
            json.put("finalidade", finalidade.equals("Adoção") ? "A" : "D");

            String valor = editValor.getText().toString().trim();
            if (!valor.isEmpty()) {
                json.put("valor", Double.parseDouble(valor));
            } else {
                json.put("valor", JSONObject.NULL);
            }

            json.put("idTipo", selectedTipo.getId());
            json.put("idRaca", selectedRaca.getId());
            json.put("idCidade", selectedCidade.getId());

            Intent intent = new Intent(this, AnimalIntentService.class);
            intent.setAction(AnimalIntentService.ACTION_EDITAR_ANIMAL);
            intent.putExtra("id", animalParaEditar.getId());
            intent.putExtra("json", json.toString());
            startService(intent);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Idade ou valor inválido", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao atualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void preencherCampos() {
        if (animalParaEditar != null) {
            editDescricao.setText(animalParaEditar.getDescricao());
            editIdade.setText(String.valueOf(animalParaEditar.getIdade()));
            editValor.setText(animalParaEditar.getValor() > 0 ? String.valueOf(animalParaEditar.getValor()) : "");
            editCor.setText(animalParaEditar.getCor());
            editProprietario.setText(animalParaEditar.getNomeProprietario());
            editContato.setText(animalParaEditar.getContato());

            // Set finalidade
            spinnerFinalidade.setSelection(animalParaEditar.getFinalidade().equals("A") ? 1 : 2);

            // Set spinners after data is loaded
            if (!listaTipos.isEmpty()) {
                for (int i = 0; i < listaTipos.size(); i++) {
                    if (listaTipos.get(i) != null
                            && listaTipos.get(i).getId() == animalParaEditar.getRaca().getTipo().getId()) {
                        spinnerTipo.setSelection(i);
                        break;
                    }
                }
            }
            if (!listaRacas.isEmpty()) {
                for (int i = 0; i < listaRacas.size(); i++) {
                    if (listaRacas.get(i) != null && listaRacas.get(i).getId() == animalParaEditar.getRaca().getId()) {
                        spinnerRaca.setSelection(i);
                        break;
                    }
                }
            }
            if (!listaCidades.isEmpty()) {
                for (int i = 0; i < listaCidades.size(); i++) {
                    if (listaCidades.get(i) != null
                            && listaCidades.get(i).getId() == animalParaEditar.getCidade().getId()) {
                        spinnerCidade.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(AnimalIntentService.EXTRA_SUCCESS, false);
            if (AnimalIntentService.ACTION_CADASTRAR_ANIMAL.equals(intent.getAction())) {
                if (success) {
                    Toast.makeText(CadastrarAnimais.this, "Cadastro enviado!", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    CadastrarAnimais.this.setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    String error = intent.getStringExtra(AnimalIntentService.EXTRA_ERROR);
                    Toast.makeText(CadastrarAnimais.this, "Erro ao cadastrar: " + error, Toast.LENGTH_LONG).show();
                }
            } else if (AnimalIntentService.ACTION_EDITAR_ANIMAL.equals(intent.getAction())) {
                if (success) {
                    Toast.makeText(CadastrarAnimais.this, "Atualização enviada!", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    CadastrarAnimais.this.setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    String error = intent.getStringExtra(AnimalIntentService.EXTRA_ERROR);
                    Toast.makeText(CadastrarAnimais.this, "Erro ao atualizar: " + error, Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
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

                Type tipoLista = new TypeToken<List<Tipo>>() {
                }.getType();
                listaTipos = new Gson().fromJson(json.toString(), tipoLista);
                listaTipos.add(0, null); // Para opção "Tipos de Animais"

                runOnUiThread(() -> {
                    ArrayAdapter<Tipo> adapter = new ArrayAdapter<Tipo>(this,
                            android.R.layout.simple_spinner_item, listaTipos) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            TextView view = (TextView) super.getView(position, convertView, parent);
                            if (getItem(position) == null) {
                                view.setText("Tipos de Animais");
                            } else {
                                view.setText(getItem(position).getDescricao());
                            }
                            return view;
                        }

                        @Override
                        public View getDropDownView(int position, View convertView, ViewGroup parent) {
                            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                            if (getItem(position) == null) {
                                view.setText("Tipos de Animais");
                            } else {
                                view.setText(getItem(position).getDescricao());
                            }
                            return view;
                        }
                    };
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerTipo.setAdapter(adapter);
                    preencherCampos(); // Pre-fill fields after loading data
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

                Type racaLista = new TypeToken<List<Raca>>() {
                }.getType();
                listaRacas = new Gson().fromJson(json.toString(), racaLista);
                listaRacas.add(0, null); // Para opção "Raças"

                runOnUiThread(() -> {
                    ArrayAdapter<Raca> adapter = new ArrayAdapter<Raca>(this,
                            android.R.layout.simple_spinner_item, listaRacas) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            TextView view = (TextView) super.getView(position, convertView, parent);
                            if (getItem(position) == null) {
                                view.setText("Raças");
                            } else {
                                view.setText(getItem(position).getDescricao());
                            }
                            return view;
                        }

                        @Override
                        public View getDropDownView(int position, View convertView, ViewGroup parent) {
                            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                            if (getItem(position) == null) {
                                view.setText("Raças");
                            } else {
                                view.setText(getItem(position).getDescricao());
                            }
                            return view;
                        }
                    };
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerRaca.setAdapter(adapter);
                    preencherCampos(); // Pre-fill fields after loading data
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

                Type cidadeLista = new TypeToken<List<Cidade>>() {
                }.getType();
                listaCidades = new Gson().fromJson(json.toString(), cidadeLista);
                listaCidades.add(0, null); // Para opção "Cidades"

                runOnUiThread(() -> {
                    ArrayAdapter<Cidade> adapter = new ArrayAdapter<Cidade>(this,
                            android.R.layout.simple_spinner_item, listaCidades) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            TextView view = (TextView) super.getView(position, convertView, parent);
                            if (getItem(position) == null) {
                                view.setText("Cidades");
                            } else {
                                view.setText(getItem(position).getNome());
                            }
                            return view;
                        }

                        @Override
                        public View getDropDownView(int position, View convertView, ViewGroup parent) {
                            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                            if (getItem(position) == null) {
                                view.setText("Cidades");
                            } else {
                                view.setText(getItem(position).getNome());
                            }
                            return view;
                        }
                    };
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCidade.setAdapter(adapter);
                    preencherCampos(); // Pre-fill fields after loading data
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}