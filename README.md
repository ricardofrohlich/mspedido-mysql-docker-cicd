# Microsservicos Pedido x Produto - RabbitMQ, Docker e CI/CD

Sistema com dois microsserviços Spring Boot que conversam de forma assíncrona via
RabbitMQ, com Eureka pra service discovery, tudo em Docker e com pipeline de CI/CD
no GitHub Actions.

## Estrutura no disco

O `docker-compose.yml` espera as duas pastas de código lado a lado (irmãs):

```
alguma-pasta/
├── mspedido-mysql-docker-cicd/   <- esse README e o docker-compose.yml
│   └── eureka-server/            <- servidor eureka
└── msproduto-mysql-docker-cicd/
```

Se quiser juntar tudo num repo só, é só colocar as duas pastas e a `eureka-server/`
como subpastas desse repo novo, mantendo os nomes que o `build:` do compose usa (ou
ajusta os caminhos lá se renomear as pastas).

## Instalando o Docker (se ainda não tiver)

Se já usa Docker no dia a dia, pode pular essa parte.

- Baixa o Docker Desktop aqui: https://www.docker.com/products/docker-desktop/
- Instala normal (next, next, next) e reinicia o PC se ele pedir
- O Docker Compose já vem junto, não precisa instalar nada separado
- Abre o Docker Desktop e espera a baleia lá no canto (system tray) ficar
  rodando de boa - sem ele aberto nenhum comando docker funciona
- Pra conferir se deu tudo certo, abre um terminal (PowerShell ou cmd) e roda:

```bash
docker --version
docker compose version
```

Se aparecer a versão dos dois, tá pronto. JDK 17 e Maven só são necessários se
for rodar sem Docker (direto pela IDE, por exemplo).

## Subindo tudo com Docker Compose

Com o Docker Desktop aberto e rodando:

```bash
cd mspedido-mysql-docker-cicd
docker compose up --build
```

O `--build` é importante da primeira vez (e sempre que mudar algo no código) porque
força reconstruir as imagens em vez de usar uma versão antiga guardada em cache.
Na primeira vez demora um pouco (baixa as imagens base e as dependências do Maven),
das próximas vezes é bem mais rápido.

Se der erro de porta ocupada (`port is already allocated` ou parecido), provavelmente
tem outro container (ou um MySQL/RabbitMQ local) usando a mesma porta - dá uma olhada
no Docker Desktop, aba Containers, e para/remove o que estiver sobrando antes de tentar
de novo.

Ordem que o compose sobe (ele mesmo cuida disso via `depends_on` + `healthcheck`):

1. `eureka-server` (porta 8761 - painel em http://localhost:8761)
2. `mysql-pedido` (3307 no host) e `mysql-produto` (3308 no host)
3. `rabbitmq` (5672, painel em http://localhost:15672, guest/guest)
4. `produto-api` (8080)
5. `mspedido-api` (8081)

Pra parar:

```bash
docker compose down          # mantém os dados dos bancos
docker compose down -v       # apaga os dados também (zera tudo)
```

### Por que os application.properties continuam com "localhost"

Cada serviço aponta pra `localhost` no banco/rabbit, que é o que funciona rodando o
jar direto na máquina. Dentro do compose isso não rola, porque cada serviço tá no
seu próprio container e ali "localhost" é o próprio container. Em vez de reescrever
o arquivo, o compose sobrescreve essas propriedades por variável de ambiente (dá pra
fazer isso com qualquer propriedade do Spring Boot), apontando pro nome do serviço
no compose.

## Testando com o Postman

Usa a coleção `Microsservicos-Pedidos-RabbitMQ.postman_collection.json` (pasta
`postman/`) - portas continuam as mesmas (8080 e 8081).

## Publicando num repositório novo no GitHub

1. Cria o repo vazio no GitHub (sem README/gitignore, pra não dar conflito).
2. Em cada pasta, confere se não sobrou remoto antigo:
   ```bash
   git remote -v
   git remote remove origin   # se tiver algum
   git remote add origin https://github.com/<seu-usuario>/<novo-repo>.git
   git push -u origin main
   ```
3. No GitHub, em Settings > Actions > General, confere se "Allow all actions" tá
   habilitado (é o padrão).
4. Copia o `ci-cd.yml` pra dentro de `.github/workflows/ci-cd.yml` em cada repo e
   commita. A partir daí todo push/PR na main já roda build + testes, e (só em push
   direto na main) publica a imagem no `ghcr.io/<seu-usuario>/<nome-do-servico>`.

## O que tem aqui

- `Dockerfile` em cada serviço (build multi-stage: compila com Maven, roda só com JRE)
- `eureka-server/` - servidor de descoberta usado pelos dois microsserviços
- `docker-compose.yml` - sobe tudo de uma vez, pra dev/demonstração
- `.github/workflows/ci-cd.yml` (por serviço) - CI (build + testes com MySQL e
  RabbitMQ reais) e CD (build + publica a imagem Docker)
- Coleção Postman com testes automatizados do fluxo completo via RabbitMQ

## Problemas comuns

- **`produto-api` não sobe / erro de conexão com o banco**: espera o `mysql-produto`
  aparecer como "healthy" (`docker compose ps`) - a primeira subida do MySQL demora
  um pouco mais.
- **`mspedido-api` não acha o `produto-api`**: confere em http://localhost:8761 se
  `produto-api-mysql` tá registrado no Eureka. Se não tiver, olha o log do
  `produto-api` (`docker compose logs produto-api`).
- **Porta já em uso**: se já tiver um MySQL/RabbitMQ local nessas portas, para ele
  antes ou muda a porta do lado esquerdo (`"3307:3306"` etc.) no `docker-compose.yml`.
