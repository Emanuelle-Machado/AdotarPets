
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

**Front-end:** Xml

**Back-end:** Java


## 📑 Endpoints da API
urlBase: https://argo.td.utfpr.edu.br/pets/ws/

| Método | Endpoint                                     | Descrição                                                                |
|--------|----------------------------------------------|--------------------------------------------------------------------------|
| GET    | `animal`                                     | Retorna todos os animais                                                 |
| GET    | `tipo`                                       | Retorna todos os tipos de animais                                        |
| GET    | `raca`                                       | Retorna todas as raças                                                   |
| GET    | `cidade`                                     | Retorna todas as cidades                                                 |
| GET    | `animal/{id}`                                | Retorna um animal específico pelo ID                                     |
| GET    | `tipo/{id}`                                  | Retorna um tipo específico pelo ID                                       |
| GET    | `raca/{id}`                                  | Retorna uma raça específica pelo ID                                      |
| GET    | `cidade/{id}`                                | Retorna uma cidade específica pelo ID                                    |

## Melhorias

Foi feita uma refatoração para que a busca o preenchimento dos spinners fosse feito por meio de Intent services, que buscam direto na web service todos os itens e os inserem no spinner.


## Aprendizados

Neste projeto aprendi mais sobre o uso de services em aplicativos mobile para melhorar a performance e realizar tarefas em segundo plano. Além de aprender a realizar a conexão com o web service nesse aplicativo.


## Screenshots

<h3 align="center">Página inicial</h3>
<p align="center">
  <img src="https://github.com/Emanuelle-Machado/AdotarPets/blob/master/app/src/main/assets/imglimpa.jpeg" alt="Página inicial" width="200"/>
  <img src="https://github.com/Emanuelle-Machado/AdotarPets/blob/master/app/src/main/assets/imgpreenchida.jpeg" alt="Listagem de animais" width="200"/>
  
</p>

<h3 align="center">Cadastros</h3>
<p align="center">
  <img src="https://github.com/Emanuelle-Machado/AdotarPets/blob/master/app/src/main/assets/animais.jpeg" alt="Cadastro de Animais" width="200"/>
  <img src="https://github.com/Emanuelle-Machado/AdotarPets/blob/master/app/src/main/assets/cidade.jpeg" alt="Dialogo de cadastro de cidade" width="200"/>
  <img src="https://github.com/Emanuelle-Machado/AdotarPets/blob/master/app/src/main/assets/raca.jpeg" alt="Dialogo de cadastro de raças" width="200"/>
  <img src="https://github.com/Emanuelle-Machado/AdotarPets/blob/master/app/src/main/assets/tipo.jpeg" alt="Dialogo de cadastro de tipos" width="200"/>
</p>

<h3 align="center">Todas as telas tem sua versão horizontal</h3>
<p align="center">
  <img src="https://github.com/Emanuelle-Machado/AdotarPets/blob/master/app/src/main/assets/horizontal.jpeg" alt="Imagem na horizontal" height="200"/>
</p>

