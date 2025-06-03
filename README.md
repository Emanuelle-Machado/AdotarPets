
# Doação e adoção de Pets

Este projeto é um estudo da máteria de aplicaticos móveis, ele tem como objetivo usar de um web service para pegar informações e cadastrar elas, usando de intent services para realizar elas em segundo plano e melhorar sua performance.

O principal objetivo é possibilitar o usuário de ver animais para adoção e cadastrar animais disponiveis para a doação. 
É possível também que o usuário cadastre-se como alguem interessado a adotar um tipo de animal, assim é possivel a pessoa com um animal pra adoção entrar em contato.



## Autores

- [@emanuellemachado](https://www.github.com/Emanuelle-Machado)


## Funcionalidades

- Listar animais do web service
- Filtrar e buscar animais
- Cadastrar animais no web service
- Cadastrar cidade, tipo e raça no web service
- Layout vertical e horizontal


## Stack utilizada

**Front-end:** PHP, TailwindCSS, Blade

**Back-end:** Node, Laravel, PHP


## Documentação da API

#### Retorna todos os animais

```http
  GET https://argo.td.utfpr.edu.br/pets/ws/animal
```

| Parâmetro   | Tipo       | Descrição                           |
| :---------- | :--------- | :---------------------------------- |
| `api_key` | `string` | **Obrigatório**. A chave da sua API |

#### Retorna um animal

```http
  GET https://argo.td.utfpr.edu.br/pets/ws/animal/${id}
```

| Parâmetro   | Tipo       | Descrição                                   |
| :---------- | :--------- | :------------------------------------------ |
| `id`      | `string` | **Obrigatório**. O ID do item que você quer |


## Melhorias

Foi feita uma refatoração para que a busca o preenchimento dos spinners fosse feito por meio de Intent services, que buscam direto na web service todos os itens e os inserem no spinner.


## Aprendizados

Neste projeto aprendi mais sobre o uso de services em aplicativos mobile para melhorar a performance e realizar tarefas em segundo plano. Além de aprender a realizar a conexão com o web service nesse aplicativo.


## Screenshots

<p align="center">
  <img src="https://github.com/Emanuelle-Machado/AdotarPets/blob/master/app/src/main/assets/imglimpa.jpeg" alt="Página inicial" width="200"/>
  <img src="https://github.com/Emanuelle-Machado/AdotarPets/blob/master/app/src/main/assets/imgpreenchida.jpeg" alt="Listagem de animais" width="200"/>
  <img src="https://github.com/Emanuelle-Machado/AdotarPets/blob/master/app/src/main/assets/animais.jpeg" alt="Cadastro de Animais" width="200"/>
</p>

