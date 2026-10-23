# Eventos Industriais

Aplicação fullstack para receber, registrar e consultar eventos de equipamentos industriais. Eventos de produção são armazenados; eventos de qualidade também geram uma auditoria na mesma transação. Cada identificador externo é único, e registros existentes não são sobrescritos.

## Funcionalidades

- Cadastro de eventos de produção e qualidade.
- Validação no formulário e na API, com mensagens em português brasileiro.
- Proteção contra identificadores duplicados, inclusive em requisições concorrentes.
- Auditoria automática para eventos de qualidade, com horário do servidor.
- Histórico com filtro por tipo, paginação e ordenação por ocorrência mais recente.
- Consulta de evento individual e de sua auditoria.
- Interface responsiva, com estados de carregamento, lista vazia, sucesso e erro.
- Preservação do formulário quando o cadastro falha e bloqueio de envios simultâneos.
- Documentação interativa da API, consulta de saúde e métricas HTTP.

## Tecnologias e justificativas

| Tecnologia | Uso no projeto |
| --- | --- |
| Java 25 | Linguagem do backend; versão exigida pelo build e validada nos testes. |
| Spring Boot 4.0.3 / Spring Framework 7 | Configuração da aplicação, injeção de dependências e integração dos módulos. |
| Spring MVC | API REST com processamento síncrono. |
| Bean Validation | Validação dos dados recebidos pela API. |
| Spring Data JPA / Hibernate | Mapeamento das entidades, consultas e persistência. |
| H2 em arquivo | Execução local sem instalar um banco separado, preservando os registros entre reinícios. |
| springdoc 3.0.1 / OpenAPI / Swagger UI | Documentação e experimentação dos endpoints. |
| Spring Boot Actuator | Saúde da aplicação e métricas HTTP. |
| React 18.3.1 | Interface dividida em componentes para formulário, histórico e auditoria. |
| TypeScript 5.6.2 em modo estrito | Contratos explícitos e verificação de tipos no frontend. |
| Vite 7.3.6 | Servidor de desenvolvimento, encaminhamento da API e build de produção. |
| CSS próprio | Estilização e responsividade sem biblioteca de componentes. |
| Fetch / AbortController | Comunicação HTTP e cancelamento de consultas antigas. |
| JUnit / AssertJ / Mockito | Testes unitários, integração e simulação de falhas. |
| JaCoCo 0.8.14 | Medição de cobertura e verificação dos limites mínimos do backend. |
| Node.js 24 / cobertura V8 | Execução dos testes de domínio e cliente HTTP do frontend. |
| Playwright 1.62.1 / Chromium | Testes de comportamento da interface no navegador. |
| Docker Compose / Nginx | Alternativa de execução em contêineres e servidor dos arquivos estáticos. |

O projeto utiliza **Spring Boot 4**, baseado no Spring Framework 7. As versões exatas das dependências frontend estão no `package-lock.json`.

## Pré-requisitos

Para executar diretamente no computador:

- **JDK 25**, com `JAVA_HOME` apontando para o JDK e `java` disponível no PATH. O build aceita Java 25 e rejeita outras versões principais.
- **Node.js 24** com **npm**.
- Acesso à internet na primeira instalação, para baixar Maven, dependências e Chromium.
- Portas **8080** e **5173** disponíveis.

O Maven Wrapper está incluído: não é necessário instalar Maven separadamente. Como alternativa, é possível usar Maven 3.9 ou superior.

Confira o ambiente:

```powershell
java -version
node --version
npm --version
```

Para a alternativa com contêineres, utilize Docker com suporte a `docker compose` e contêineres Linux. Nesse caso, Java e Node são fornecidos pelas imagens.

## Executar em desenvolvimento

Abra dois terminais a partir da raiz do repositório.

### 1. Backend

No Windows / PowerShell:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

No Linux / macOS:

```sh
cd backend
sh mvnw spring-boot:run
```

A primeira execução baixa as dependências. Aguarde a inicialização do servidor na porta 8080.

### 2. Frontend

No segundo terminal:

```sh
cd frontend
npm ci
npm run dev
```

Acesse a interface em **http://127.0.0.1:5173**. Para encerrar cada servidor, pressione `Ctrl+C` no terminal correspondente.

| Recurso | Endereço |
| --- | --- |
| Interface | http://127.0.0.1:5173 |
| API de eventos | http://localhost:8080/api/events |
| Documentação interativa | http://localhost:8080/swagger-ui.html |
| Especificação OpenAPI | http://localhost:8080/v3/api-docs |
| Saúde | http://localhost:8080/actuator/health |
| Métricas disponíveis | http://localhost:8080/actuator/metrics |

O frontend acessa `/api` na mesma origem. O Vite encaminha essas requisições para o backend, cujo destino padrão é `http://localhost:8080`.

## Configuração

As variáveis são opcionais e devem estar disponíveis no ambiente do processo correspondente.

| Variável | Processo | Padrão | Finalidade |
| --- | --- | --- | --- |
| `PORT` | Backend | `8080` | Porta HTTP da API. |
| `DB_URL` | Backend | `jdbc:h2:file:./data/events;DB_CLOSE_ON_EXIT=FALSE` | Endereço JDBC do H2. |
| `DB_PASSWORD` | Backend | Vazia | Senha do usuário `sa` no H2. |
| `APP_FRONTEND_ORIGIN` | Backend | `http://localhost:5173` | Origem permitida em chamadas diretas de outro domínio à API. |
| `API_URL` | Vite | `http://localhost:8080` | Destino do encaminhamento de `/api` no desenvolvimento. |

Exemplo no PowerShell, para usar outra porta no backend:

```powershell
cd backend
$env:PORT = '8081'
.\mvnw.cmd spring-boot:run
```

No terminal do frontend:

```powershell
cd frontend
$env:API_URL = 'http://localhost:8081'
npm run dev
```

No Linux/macOS, defina as variáveis com `export PORT=8081` e `export API_URL=http://localhost:8081` nos terminais respectivos.

`API_URL` configura o Vite durante o desenvolvimento. No Docker, o Nginx encaminha `/api` ao serviço `backend:8080`, conforme `frontend/nginx.conf`.

## Banco de dados

Executando o backend a partir de sua pasta, o H2 grava em `backend/data/events.mv.db`. Os dados permanecem entre reinícios. Nos testes, o banco é em memória.

- Não execute duas instâncias usando o mesmo arquivo de banco.
- O console web do H2 não fica exposto.
- Para reiniciar a base de desenvolvimento, encerre o backend e remova a pasta `backend/data`; essa operação apaga os eventos locais.
- No Docker, os dados ficam no volume `events-data`.

## Executar com Docker

Na raiz do repositório:

```sh
docker compose up --build
```

A interface fica em http://localhost:5173 e a API em http://localhost:8080. O build do backend executa testes e valida a cobertura. O frontend utiliza o build estático servido pelo Nginx.

Para acompanhar e encerrar:

```sh
docker compose logs -f
docker compose down
```

O comando `down` preserva o volume. A opção `down -v` também remove os dados persistidos.

## Testes e cobertura

### Backend

No Windows, a partir da raiz:

```powershell
cd backend
.\mvnw.cmd --batch-mode clean verify
```

No Linux/macOS:

```sh
cd backend
sh mvnw --batch-mode clean verify
```

A fase `verify` executa testes unitários e integrados, gera o pacote e verifica os mínimos de **90% de linhas** e **80% de ramificações**. O comando `test` executa os testes, mas não conclui a geração e verificação do relatório JaCoCo.

Relatórios gerados:

- Cobertura HTML: `backend/target/site/jacoco/index.html`.
- Cobertura XML e CSV: `backend/target/site/jacoco/`.
- Resultados JUnit: `backend/target/surefire-reports/`.

Bootstrap e configuração são excluídos da cobertura. Domínio, DTOs, aplicação, API e adaptadores de persistência permanecem incluídos.

### Frontend

A partir da raiz:

```sh
cd frontend
npm ci
npx playwright install chromium
npm run check
```

Em Linux, caso o ambiente não tenha as bibliotecas de sistema do navegador, utilize `npx playwright install --with-deps chromium`.

| Comando | Verificação |
| --- | --- |
| `npm run build` | TypeScript estrito e build Vite. |
| `npm test` | Testes de domínio e cliente HTTP, com cobertura V8. |
| `npm run test:e2e` | Testes da interface com Chromium. |
| `npm run check` | Todas as verificações acima em sequência. |

A cobertura unitária frontend exige 90% de linhas e 80% de ramificações nos módulos de domínio e cliente HTTP. Componentes e hooks são exercitados pelos testes de navegador, mas não entram nessa medição.

Os testes de navegador interceptam a API para controlar cenários. Não substituem testes completos entre servidores frontend e backend reais. O backend é testado com Spring, JPA e H2 reais, incluindo concorrência e rollback.

Na validação local de 22/10/2026, passaram **43 testes backend**, **11 testes unitários frontend** e **6 testes Chromium**. O JaCoCo mediu **100% de linhas (99/99) e ramificações (37/37)** do código incluído, respeitando as exclusões e os limites descritos acima. Docker Compose e a execução remota no GitHub Actions não foram executados nessa validação. O fluxo `.github/workflows/verify.yml` configura as verificações automatizadas.

## Gerar os artefatos

Backend, a partir de `backend`:

```powershell
.\mvnw.cmd --batch-mode verify
java -jar target/eventos-1.0.0.jar
```

No Linux/macOS, substitua `.\mvnw.cmd` por `sh mvnw`.

Frontend, a partir de `frontend`:

```sh
npm ci
npm run build
```

O JAR fica em `backend/target/eventos-1.0.0.jar`, e os arquivos estáticos em `frontend/dist/`. O Nginx fornecido serve a aplicação e encaminha chamadas da API. `npm run preview` permite inspecionar o build em http://127.0.0.1:4173; para trabalhar com a API local, utilize o servidor de desenvolvimento ou o Docker Compose.

## Contrato da API

Os nomes dos campos, rotas e valores dos tipos seguem o contrato técnico. A interface, as mensagens próprias e a documentação utilizam português brasileiro.

| Método e rota | Resultado |
| --- | --- |
| `POST /api/events` | `201`: criado, com evento e cabeçalho `Location`; `400`: dados inválidos; `409`: ID duplicado. |
| `GET /api/events?page=0&size=10&eventType=QUALITY` | `200`: página de eventos; filtro por tipo opcional. |
| `GET /api/events/{eventId}` | `200`: evento; `404`: evento inexistente. |
| `GET /api/events/{eventId}/audit` | `200`: auditoria; `204`: evento de produção sem auditoria; `404`: evento inexistente. |

Exemplo de cadastro:

```json
{
  "eventId": "EVT-1001",
  "eventType": "PRODUCTION",
  "description": "Linha de produção iniciada",
  "timestamp": "2026-05-20T10:00:00"
}
```

Para qualidade, envie `"eventType": "QUALITY"` com outro identificador. A ação da auditoria é `QUALITY_EVENT_REGISTERED`.

| Campo | Regra |
| --- | --- |
| `eventId` | De 1 a 100 caracteres ASCII; começa com letra ou número, seguido de letras, números, ponto, hífen ou sublinhado. Diferencia maiúsculas de minúsculas. |
| `eventType` | Apenas `PRODUCTION` e `QUALITY`. |
| `description` | Obrigatória, não pode conter somente espaços e aceita até 2.000 caracteres. |
| `timestamp` | Data/hora local ISO, sem fuso, por exemplo `2026-05-20T10:00:00`. |

A paginação começa em zero. `size` aceita valores de 1 a 100; a interface utiliza 10. A ordenação é por ocorrência decrescente, com ID crescente em caso de empate. Campos JSON desconhecidos são rejeitados. Não existem operações de edição ou exclusão.

Erros seguem o padrão RFC 9457, com `Content-Type: application/problem+json`:

```json
{
  "type": "about:blank",
  "title": "Evento duplicado",
  "status": 409,
  "detail": "O evento 'EVT-1001' já foi registrado.",
  "instance": "/api/events"
}
```

Erros de validação também incluem `errors`, associando campos às mensagens. Falhas inesperadas retornam uma mensagem genérica e mantêm os detalhes técnicos no servidor.

O arquivo [requests.http](requests.http) reúne exemplos de cadastro, consulta, filtro, auditoria e saúde para clientes HTTP compatíveis.

## Organização e decisões técnicas

| Diretório | Responsabilidade |
| --- | --- |
| `backend/src/main/java/com/victorabreu/eventos/domain` | Modelo imutável, invariantes e exceções de negócio, sem dependência de Spring ou JPA. |
| `backend/.../application` | Coordenação dos casos de uso e fronteira transacional. |
| `backend/.../application/port` | Contratos para armazenamento de eventos e auditorias. |
| `backend/.../infrastructure/persistence` | Entidades JPA, repositórios e implementações de persistência. |
| `backend/.../api` | HTTP, DTOs, validação de entrada e tradução de erros. |
| `backend/.../config` | Relógio, CORS e metadados da API. |
| `frontend/src/domain` | Tipos, validação e formatação. |
| `frontend/src/services` | Cliente HTTP e erros estruturados. |
| `frontend/src/hooks` | Estado da consulta e cancelamento de requisições. |
| `frontend/src/components` | Formulário, histórico e detalhes da auditoria. |

- **Unicidade no banco:** o identificador externo é a chave primária. A inserção usa `persist`; a violação de unicidade SQLState `23505` é traduzida em conflito. Outras falhas não são mascaradas como duplicidade.
- **Atomicidade:** evento e auditoria compartilham uma transação. A auditoria possui a mesma chave do evento e uma chave estrangeira, impedindo registros órfãos.
- **Processamento síncrono:** a operação é curta e a resposta reflete o resultado da transação. Não há requisito de fila ou mensageria.
- **Regra de auditoria explícita:** dois tipos são atendidos por `requiresAudit()`, sem hierarquias adicionais.
- **Datas distintas:** a ocorrência usa `LocalDateTime`, conforme o contrato sem fuso; a auditoria usa `Instant` em UTC e é exibida no fuso do navegador.
- **Consultas assíncronas:** requisições antigas são canceladas e suas respostas descartadas, evitando substituir o resultado de um filtro mais recente.
- **Erros seguros:** a API preserva os códigos HTTP, oferece mensagens em pt-BR e não devolve SQL ou detalhes internos.

## Solução de problemas

| Sintoma | Como resolver |
| --- | --- |
| Maven rejeita a versão Java | Confira `java -version`, `JAVA_HOME` e se o terminal utiliza JDK 25. |
| Porta já está em uso | Encerre a instância anterior ou configure outra porta; ajuste `API_URL` se mudar o backend. |
| Frontend não conecta à API | Confira se o backend iniciou e se o destino `API_URL` está correto; reinicie o Vite após alterar a variável. |
| H2 informa arquivo bloqueado | Encerre a outra instância que utiliza o mesmo banco. |
| Chromium não encontrado | Execute `npx playwright install chromium` dentro de `frontend`. |
| `npm ci` informa inconsistência | Confira se `package.json` e `package-lock.json` pertencem à mesma versão do projeto. |
| Download falha | Confira a conexão e as regras de proxy para Maven Central, registro npm e downloads do Playwright. |

## Possíveis melhorias futuras

- Adicionar autenticação e autorização dos equipamentos, TLS e limites de requisição antes de expor a API publicamente.
- Adotar PostgreSQL e migrações versionadas para ambientes compartilhados, substituindo o `ddl-auto=update` e repetindo os testes de concorrência no banco definitivo.
- Acrescentar testes completos entre navegador, frontend, API e banco reais, além de medir a cobertura dos componentes React e hooks.
- Evoluir o contrato de data/hora para incluir fuso explícito em operações entre fábricas de regiões diferentes.
- Definir uma política de reenvio idempotente: atualmente todo ID repetido retorna `409`; uma evolução poderia devolver a confirmação original para reenvios com o mesmo conteúdo.
- Ampliar a auditoria com identificação do equipamento, política de retenção e exportação, conforme novos requisitos de negócio.
