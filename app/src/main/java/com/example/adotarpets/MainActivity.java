package com.example.adotarpets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.adotarpets.models.Animal;
import com.example.adotarpets.models.Cidade;
import com.example.adotarpets.models.Raca;
import com.example.adotarpets.models.Tipo;
import com.example.adotarpets.services.AnimalIntentService;
import com.example.adotarpets.services.CidadeIntentService;
import com.example.adotarpets.services.RacaIntentService;
import com.example.adotarpets.services.TipoIntentService;
import com.example.adotarpets.telas.CadastrarAnimais;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listaAnimais;
    private List<Animal> animais = new ArrayList<>();
    private AnimalAdapter adapter;
    private Spinner spFinalidade, spRaca, spCidade, spTipo;
    private List<Raca> listaRacas = new ArrayList<>();
    private List<Cidade> listaCidades = new ArrayList<>();
    private List<Tipo> listaTipos = new ArrayList<>();

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
            TextView txtProprietario = convertView.findViewById(R.id.txtProprietario);
            TextView txtContato = convertView.findViewById(R.id.txtContato);
            TextView txtFinalidade = convertView.findViewById(R.id.txtFinalidade);
            TextView txtValor = convertView.findViewById(R.id.txtValor);

            txtDescricao.setText("Descrição: " + (animal.getDescricao() != null ? animal.getDescricao() : "N/A"));
            txtCor.setText("Cor: " + (animal.getCor() != null ? animal.getCor() : "N/A"));
            txtIdade.setText("Idade: " + animal.getIdade() + " meses");
            txtRaca.setText("Raça: " + (animal.getRaca() != null && animal.getRaca().getDescricao() != null ? animal.getRaca().getDescricao() : "N/A"));
            txtTipo.setText("Tipo: " + (animal.getRaca() != null && animal.getRaca().getTipo() != null && animal.getRaca().getTipo().getDescricao() != null ? animal.getRaca().getTipo().getDescricao() : "N/A"));
            txtCidade.setText("Cidade: " + (animal.getCidade() != null && animal.getCidade().getNome() != null ? animal.getCidade().getNome() : "N/A"));
            txtProprietario.setText("Proprietário: " + (animal.getNomeProprietario() != null ? animal.getNomeProprietario() : "N/A"));
            txtContato.setText("Contato: " + (animal.getContato() != null ? animal.getContato() : "N/A"));
            txtFinalidade.setText("Finalidade: " + (animal.getFinalidade() != null ? (animal.getFinalidade().equals("D") ? "Doação" : "Adoção") : "N/A"));
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
        adapter = new AnimalAdapter(MainActivity.this, animais);
        listaAnimais.setAdapter(adapter);
        spFinalidade = findViewById(R.id.spFinalidade);
        spTipo = findViewById(R.id.spTipo);
        spRaca = findViewById(R.id.spRaca);
        spCidade = findViewById(R.id.spCidade);

        // Finalidade: Adoção ou Doação
        List<String> finalidades = Arrays.asList("Finalidade", "Adoção", "Doação");
        ArrayAdapter<String> finalidadeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, finalidades);
        finalidadeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFinalidade.setAdapter(finalidadeAdapter);

        carregarTipos();
        carregarRacas();
        carregarCidades();

        Button btnBuscar = findViewById(R.id.btnBuscar);
        Button btnLimpar = findViewById(R.id.btnLimpar);
        Button btnCadastrarAnimais = findViewById(R.id.btn_cadastrar_animais);
        Button btnCadastrarCidade = findViewById(R.id.btn_cadastrar_cidades);
        Button btnCadastrarTipo = findViewById(R.id.btn_cadastrar_tipos);
        Button btnCadastrarRaca = findViewById(R.id.btn_cadastrar_racas);
        EditText edIdadeDe = findViewById(R.id.edIdadeDe);
        EditText edIdadeAte = findViewById(R.id.edIdadeAte);
        EditText edDDD = findViewById(R.id.edDDD);

        btnBuscar.setOnClickListener(v -> {
            String url = "http://argo.td.utfpr.edu.br/pets/ws/animal?";

            String finalidadeSelecionada = spFinalidade.getSelectedItem().toString();
            if (finalidadeSelecionada.equals("Adoção")) {
                url += "finalidade=A&";
            } else if (finalidadeSelecionada.equals("Doação")) {
                url += "finalidade=D&";
            }

            if (!edIdadeDe.getText().toString().trim().isEmpty()) {
                url += "idadeDe=" + edIdadeDe.getText().toString().trim() + "&";
            }
            if (!edIdadeAte.getText().toString().trim().isEmpty()) {
                url += "idadeAte=" + edIdadeAte.getText().toString().trim() + "&";
            }

            if (!edDDD.getText().toString().trim().isEmpty()) {
                url += "ddd=" + edDDD.getText().toString().trim() + "&";
            }

            Cidade cidade = (Cidade) spCidade.getSelectedItem();
            if (cidade != null) {
                url += "idCidade=" + cidade.getId() + "&";
            }

            Raca raca = (Raca) spRaca.getSelectedItem();
            if (raca != null) {
                url += "idRaca=" + raca.getId() + "&";
            }

            Tipo tipo = (Tipo) spTipo.getSelectedItem();
            if (tipo != null) {
                url += "idTipo=" + tipo.getId() + "&";
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
            spFinalidade.setSelection(0);
            spRaca.setSelection(0);
            spCidade.setSelection(0);
            spTipo.setSelection(0);
        });

        btnCadastrarAnimais.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CadastrarAnimais.class);
            startActivityForResult(intent, 100);
        });

        btnCadastrarCidade.setOnClickListener(v -> mostrarDialogCadastrarCidade());
        btnCadastrarTipo.setOnClickListener(v -> mostrarDialogCadastrarTipo());
        btnCadastrarRaca.setOnClickListener(v -> mostrarDialogCadastrarRaca());

        listaAnimais.setOnItemClickListener((parent, view, position, id) -> {
            Animal selecionado = (Animal) listaAnimais.getItemAtPosition(position);
            Intent intent = new Intent(MainActivity.this, CadastrarAnimais.class);
            intent.putExtra("animal", selecionado);
            startActivityForResult(intent, 100);
        });
    }

    private void mostrarDialogCadastrarCidade() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cadastrar_cidade, null);
        builder.setView(dialogView);

        EditText edDdd = dialogView.findViewById(R.id.ed_ddd);
        EditText edNomeCidade = dialogView.findViewById(R.id.ed_nome_cidade);

        builder.setTitle("Cadastrar Cidade")
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String ddd = edDdd.getText().toString().trim();
                    String nome = edNomeCidade.getText().toString().trim();
                    if (!ddd.isEmpty() && !nome.isEmpty()) {
                        Intent intent = new Intent(this, CidadeIntentService.class);
                        intent.setAction(CidadeIntentService.ACTION_CADASTRAR_CIDADE);
                        intent.putExtra("ddd", ddd);
                        intent.putExtra("nome", nome);
                        startService(intent);
                    } else {
                        Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogCadastrarTipo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cadastrar_tipo, null);
        builder.setView(dialogView);

        EditText edDescricao = dialogView.findViewById(R.id.ed_descricao_tipo);

        builder.setTitle("Cadastrar Tipo")
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String descricao = edDescricao.getText().toString().trim();
                    if (!descricao.isEmpty()) {
                        Intent intent = new Intent(this, TipoIntentService.class);
                        intent.setAction(TipoIntentService.ACTION_CADASTRAR_TIPO);
                        intent.putExtra("descricao", descricao);
                        startService(intent);
                    } else {
                        Toast.makeText(this, "Preencha a descrição", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogCadastrarRaca() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cadastrar_raca, null);
        builder.setView(dialogView);

        EditText edDescricao = dialogView.findViewById(R.id.ed_descricao_raca);
        Spinner spTipoRaca = dialogView.findViewById(R.id.sp_tipo_raca);

        // Log dos tipos disponíveis
        for (Tipo tipo : listaTipos) {
            if (tipo != null) {
                Log.d("RacaDialog", "Tipo disponível: ID=" + tipo.getId() + ", Descrição=" + tipo.getDescricao());
            }
        }

        // Configurar spinner de tipos
        ArrayAdapter<Tipo> tipoAdapter = new ArrayAdapter<Tipo>(this,
                android.R.layout.simple_spinner_item, listaTipos) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                if (getItem(position) == null) {
                    view.setText("Selecione um Tipo");
                } else {
                    view.setText(getItem(position).getDescricao());
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                if (getItem(position) == null) {
                    view.setText("Selecione um Tipo");
                } else {
                    view.setText(getItem(position).getDescricao());
                }
                return view;
            }
        };
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipoRaca.setAdapter(tipoAdapter);

        builder.setTitle("Cadastrar Raça")
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String descricao = edDescricao.getText().toString().trim();
                    Tipo tipo = (Tipo) spTipoRaca.getSelectedItem();
                    if (!descricao.isEmpty() && tipo != null && tipo.getId() > 0) {
                        Intent intent = new Intent(this, RacaIntentService.class);
                        intent.setAction(RacaIntentService.ACTION_CADASTRAR_RACA);
                        intent.putExtra("descricao", descricao);
                        intent.putExtra("idTipo", tipo.getId());
                        startService(intent);
                    } else {
                        Toast.makeText(this, "Preencha todos os campos e selecione um tipo válido", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void carregarCidades() {
        Intent intent = new Intent(this, CidadeIntentService.class);
        intent.setAction(CidadeIntentService.ACTION_BUSCAR_CIDADES);
        startService(intent);
    }

    private void carregarTipos() {
        Intent intent = new Intent(this, TipoIntentService.class);
        intent.setAction(TipoIntentService.ACTION_BUSCAR_TIPOS);
        startService(intent);
    }

    private void carregarRacas() {
        Intent intent = new Intent(this, RacaIntentService.class);
        intent.setAction(RacaIntentService.ACTION_BUSCAR_RACAS);
        startService(intent);
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
            String action = intent.getAction();
            if (AnimalIntentService.ACTION_BUSCAR_ANIMAIS.equals(action)) {
                boolean success = intent.getBooleanExtra(AnimalIntentService.EXTRA_SUCCESS, false);
                if (success) {
                    String json = intent.getStringExtra(AnimalIntentService.EXTRA_RESULTADO);
                    Type tipoLista = new TypeToken<List<Animal>>() {}.getType();
                    animais = new Gson().fromJson(json, tipoLista);
                    adapter = new AnimalAdapter(MainActivity.this, animais);
                    listaAnimais.setAdapter(adapter);
                } else {
                    String error = intent.getStringExtra(AnimalIntentService.EXTRA_ERROR);
                    Toast.makeText(MainActivity.this, "Erro ao buscar animais: " + error, Toast.LENGTH_LONG).show();
                }
            } else if (AnimalIntentService.ACTION_BUSCAR_ANIMAIS.equals(action)) {
                boolean success = intent.getBooleanExtra(AnimalIntentService.EXTRA_SUCCESS, false);
                if (success) {
                    String json = intent.getStringExtra(AnimalIntentService.EXTRA_RESULTADO);
                    Type tipoLista = new TypeToken<List<Animal>>() {}.getType();
                    List<Animal> tempAnimais = new Gson().fromJson(json, tipoLista);
                    animais.clear();
                    for (Animal animal : tempAnimais) {
                        if (animal.getFinalidade() == null) {
                            Log.w("AnimalList", "Animal com finalidade nula: " + animal.getDescricao());
                            continue; // Skip animals with null finalidade
                        }
                        animais.add(animal);
                    }
                    adapter = new AnimalAdapter(MainActivity.this, animais);
                    listaAnimais.setAdapter(adapter);
                } else {
                    String error = intent.getStringExtra(AnimalIntentService.EXTRA_ERROR);
                    Toast.makeText(MainActivity.this, "Erro ao buscar animais: " + error, Toast.LENGTH_LONG).show();
                }
            }else if (CidadeIntentService.ACTION_BUSCAR_CIDADES.equals(action)) {
                boolean success = intent.getBooleanExtra(CidadeIntentService.EXTRA_SUCCESS, false);
                if (success) {
                    String json = intent.getStringExtra(CidadeIntentService.EXTRA_RESULTADO);
                    Type tipoLista = new TypeToken<List<Cidade>>() {}.getType();
                    listaCidades = new Gson().fromJson(json, tipoLista);
                    listaCidades.add(0, null); // Para opção "Todas"

                    ArrayAdapter<Cidade> adapter = new ArrayAdapter<Cidade>(MainActivity.this,
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
                    spCidade.setAdapter(adapter);
                } else {
                    String error = intent.getStringExtra(CidadeIntentService.EXTRA_ERROR);
                    Toast.makeText(MainActivity.this, "Erro ao buscar cidades: " + error, Toast.LENGTH_LONG).show();
                }
            } else if (TipoIntentService.ACTION_BUSCAR_TIPOS.equals(action)) {
                boolean success = intent.getBooleanExtra(TipoIntentService.EXTRA_SUCCESS, false);
                if (success) {
                    String json = intent.getStringExtra(TipoIntentService.EXTRA_RESULTADO);
                    Type tipoLista = new TypeToken<List<Tipo>>() {}.getType();
                    listaTipos = new Gson().fromJson(json, tipoLista);
                    listaTipos.add(0, null); // Para opção "Todas"

                    ArrayAdapter<Tipo> adapter = new ArrayAdapter<Tipo>(MainActivity.this,
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
                    spTipo.setAdapter(adapter);
                } else {
                    String error = intent.getStringExtra(TipoIntentService.EXTRA_ERROR);
                    Toast.makeText(MainActivity.this, "Erro ao buscar tipos: " + error, Toast.LENGTH_LONG).show();
                }
            } else if (RacaIntentService.ACTION_BUSCAR_RACAS.equals(action)) {
                boolean success = intent.getBooleanExtra(RacaIntentService.EXTRA_SUCCESS, false);
                if (success) {
                    String json = intent.getStringExtra(RacaIntentService.EXTRA_RESULTADO);
                    Type tipoLista = new TypeToken<List<Raca>>() {}.getType();
                    listaRacas = new Gson().fromJson(json, tipoLista);
                    listaRacas.add(0, null); // Para opção "Todas"

                    ArrayAdapter<Raca> adapter = new ArrayAdapter<Raca>(MainActivity.this,
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
                    spRaca.setAdapter(adapter);
                } else {
                    String error = intent.getStringExtra(RacaIntentService.EXTRA_ERROR);
                    Toast.makeText(MainActivity.this, "Erro ao buscar raças: " + error, Toast.LENGTH_LONG).show();
                }
            } else if (CidadeIntentService.ACTION_CADASTRAR_CIDADE.equals(action)) {
                boolean success = intent.getBooleanExtra(CidadeIntentService.EXTRA_SUCCESS, false);
                if (success) {
                    carregarCidades();
                    Toast.makeText(MainActivity.this, "Cidade cadastrada com sucesso", Toast.LENGTH_SHORT).show();
                } else {
                    String error = intent.getStringExtra(CidadeIntentService.EXTRA_ERROR);
                    Toast.makeText(MainActivity.this, "Erro ao cadastrar cidade: " + error, Toast.LENGTH_LONG).show();
                }
            } else if (TipoIntentService.ACTION_CADASTRAR_TIPO.equals(action)) {
                boolean success = intent.getBooleanExtra(TipoIntentService.EXTRA_SUCCESS, false);
                if (success) {
                    carregarTipos();
                    Toast.makeText(MainActivity.this, "Tipo cadastrado com sucesso", Toast.LENGTH_SHORT).show();
                } else {
                    String error = intent.getStringExtra(TipoIntentService.EXTRA_ERROR);
                    Toast.makeText(MainActivity.this, "Erro ao cadastrar tipo: " + error, Toast.LENGTH_LONG).show();
                }
            } else if (RacaIntentService.ACTION_CADASTRAR_RACA.equals(action)) {
                boolean success = intent.getBooleanExtra(RacaIntentService.EXTRA_SUCCESS, false);
                if (success) {
                    carregarRacas();
                    Toast.makeText(MainActivity.this, "Raça cadastrada com sucesso", Toast.LENGTH_SHORT).show();
                } else {
                    String error = intent.getStringExtra(RacaIntentService.EXTRA_ERROR);
                    Toast.makeText(MainActivity.this, "Erro ao cadastrar raça: " + error, Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AnimalIntentService.ACTION_BUSCAR_ANIMAIS);
        filter.addAction(AnimalIntentService.ACTION_CADASTRAR_ANIMAL);
        filter.addAction(AnimalIntentService.ACTION_EDITAR_ANIMAL);
        filter.addAction(CidadeIntentService.ACTION_BUSCAR_CIDADES);
        filter.addAction(CidadeIntentService.ACTION_CADASTRAR_CIDADE);
        filter.addAction(TipoIntentService.ACTION_BUSCAR_TIPOS);
        filter.addAction(TipoIntentService.ACTION_CADASTRAR_TIPO);
        filter.addAction(RacaIntentService.ACTION_BUSCAR_RACAS);
        filter.addAction(RacaIntentService.ACTION_CADASTRAR_RACA);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, AnimalIntentService.class);
            intent.setAction(AnimalIntentService.ACTION_BUSCAR_ANIMAIS);
            intent.putExtra("url", "http://argo.td.utfpr.edu.br/pets/ws/animal");
            startService(intent);
        }
    }
}