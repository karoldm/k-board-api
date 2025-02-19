# K-board API

API de Gerenciamento de Projetos e Tarefas em um Quadro Kanban! Esta API foi desenvolvida utilizando Spring Boot e oferece funcionalidades para gerenciar projetos, tarefas e colaboração entre usuários em um ambiente de quadro Kanban.

## Funcionalidades

### Autenticação e Autorização
- **Cadastro de Usuários**: Os usuários podem se cadastrar fornecendo informações básicas como nome, email, senha e foto de perfil.
- **Login**: Autenticação segura utilizando Spring Security e JWT (JSON Web Tokens).

### Gerenciamento de Projetos
- **Criação de Projetos**: Usuários podem criar novos projetos.
- **Convite de Membros**: O criador do projeto pode compartilhar o ID do projeto para que outros usuários possam se juntar como membros.
- **Acompanhamento de Progresso**: A API permite acompanhar o progresso do projeto com base na quantidade de tarefas concluídas.

### Gerenciamento de Tarefas
- **Criação de Tarefas**: Membros do projeto podem criar tarefas.
- **Movimentação de Tarefas**: As tarefas podem ser movidas entre diferentes estágios do quadro Kanban (ex: "A Fazer", "Em Progresso", "Concluído").

### Documentação com Swagger
- **Swagger UI**: A API está documentada utilizando Swagger, permitindo uma fácil visualização e teste dos endpoints diretamente no navegador.

## Tecnologias e Ferramentas Utilizadas

- **Spring Boot**: Framework principal para desenvolvimento da API.
- **Spring Security**: Para autenticação e autorização.
- **Spring Data JPA**: Para persistência de dados e interação com o banco de dados.
- **AWS S3**: Para armazenamento de imagens.
- **JWT (JSON Web Tokens)**: Para autenticação segura.
- **Swagger**: Para documentação da API.
- **Banco de Dados**: Foi utilizado um banco de dados relacional PostgreSQL para armazenar informações de usuários, projetos e tarefas.

## Como Executar o Projeto

### Pré-requisitos
- Java 21
- Maven
- AWS S3 Bucket configurado
- Banco de dados relacional configurado

### Configuração

1. **Clone o repositório**:
   ```bash
   git clone git@github.com:karoldm/k-board-api.git
   cd k-board-api
   ```

2. **Configure o arquivo application.properties:**
    ```bash
   spring.application.name=k-board-api
    
    spring.datasource.url=jdbc:postgresql://localhost:5432/kboard
    spring.datasource.username=username
    spring.datasource.password=password
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
    spring.jpa.show-sql=true
    
    api.security.token.secret=jwt_token
    api.aws.access-key=aws_access_key
    api.aws.secret-key=aws_secret_key
    api.aws.bucket-url=bucket_url
    
    spring.web.resources.static-locations=classpath:/META-INF/resources/,classpath:/resources/,classpath:/static/,classpath:/public/
   ```
3. **Excecute o projeto**
   - A API estará disponível em http://localhost:8080.
   - A documentação Swagger estará disponível em http://localhost:8080/swagger-ui.html.


## Diagramas do Sistema

### Casos de uso
![Casos de uso](/doc/use-cases.jpg)

### Modelagem
![Modelagem](/doc/model.jpg)


## Deploy
Projeto e Banco de Dados rodando no [Render](https://render.com/)


## Contribuição
Contribuições são bem-vindas! Sinta-se à vontade para abrir issues e pull request ❤️
