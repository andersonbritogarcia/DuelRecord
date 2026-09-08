# Autenticação e ambiente local

Fluxo implementado: Google Identity Services → `GET /api/me` → `GET /api/players/me` → perfil real. A sessão abre somente após as duas respostas e a associação usuário/jogador. `/` e `/profile` exigem sessão; `/login` é público. `/preview` mantém o dashboard demonstrativo e `/fast-match` permanece rascunho público sem gravação na API.

## Criar o cliente Google

No Google Cloud Console, crie/selecione o projeto DuelRecord. Em Google Auth Platform, configure o nome, e-mail de suporte e público externo. Para desenvolvimento, mantenha o modo de teste e adicione os e-mails de teste. Em Clientes, crie um Aplicativo da Web com as origens JavaScript `http://localhost:4200` e `http://127.0.0.1:4200`, sem barra final. O callback JavaScript usado aqui não exige URI de redirecionamento nem Client Secret. Copie o Client ID público terminado em `.apps.googleusercontent.com`.

Veja o [guia oficial de configuração](https://developers.google.com/identity/gsi/web/guides/get-google-api-clientid).

## Arquivos de ambiente

1. Copie `backend/app/.env.example` para `backend/app/.env`. Preencha `GOOGLE_CLIENT_ID` e os dados do banco já existente. O Compose não migra senha/nome de um volume PostgreSQL existente. Preserve suas credenciais atuais. Configure a IDE para executar a partir de `backend/app`; o Spring lê o arquivo relativamente a esse diretório, como Java properties, sem aspas nos valores.
2. `frontend/.env.local` já foi criado localmente com API `http://localhost:8080/api`. Preencha `PUBLIC_GOOGLE_CLIENT_ID` com o mesmo ID. Em outro ambiente, copie `frontend/.env.example`. Nunca coloque segredos no frontend.
3. Reinicie o backend para carregar a configuração e as mudanças de segurança. Execute `npm start` no frontend. Os scripts prestart/prebuild/prewatch geram `public/runtime-config.json`. Alterar `.env` exige regeneração/reinício.

Arquivos `.env`, `.env.local` e o JSON gerado são ignorados pelo Git. Só exemplos são versionados. O gerador exporta explicitamente URL da API, Client ID e timeout; nunca serializa todo o ambiente. O navegador valida o JSON e o busca sem cache; o service worker não o armazena. No deploy, forneça esse JSON e sirva-o com `Cache-Control: no-store`.

URLs remotas da API e origens CORS exigem HTTPS. HTTP é aceito somente em loopback para desenvolvimento. CORS aceita apenas as origens configuradas, sem cookies de autenticação.

## Segurança e limites

- Token somente em memória, sem localStorage/sessionStorage/IndexedDB. Recarregar a página exige entrar novamente. Tema e idioma persistem separadamente.
- Bearer somente para a origem e caminho da API configurada; Accept-Language e timeout centralizados. Sem retries automáticos de gravações.
- Nonce aleatório por inicialização. Claims locais servem ao desafio e relógio da interface; a autoridade é o backend, que verifica assinatura, emissor, audience e validade.
- Emissor esperado `https://accounts.google.com`; audience igual ao Client ID desta aplicação. URLs de emissor e chaves são configurações confiáveis do operador.
- Resolução de conta rejeita usuários inativos/suspensos antes de atualizar dados ou emitir eventos.
- Logout/expiração limpam token, perfil e rascunhos. Respostas antigas não restauram sessão encerrada nem apagam sessão mais recente.
- Logout encerra a sessão local; o backend stateless não revoga um token emitido antes de expirar.
- Consulta de geografia pública; criação de cidades exige autenticação. CORS não substitui autorização por domínio.
- Erros não exibem mensagens internas nem registram credenciais. O service worker não armazena respostas privadas.

Próximas entregas: editar perfil, registrar partidas, estatísticas reais e modo offline. Antes da gravação de partidas, revisar autorização por proprietário e atribuição de source/verificationStatus no servidor. Refresh de sessão não está implementado.

## Verificação

Frontend: `npm test -- --watch=false` e `npm run build`. Backend: `mvn test`, usando H2, sem alterar PostgreSQL local. O teste JwtSecurityIntegrationTest usa JWKS local e RSA para exercitar o decoder real contra assinatura, emissor, audience e validade inválidos. Há testes de CORS, usuário suspenso, isolamento do token, nonce, login sequencial, respostas atrasadas e traduções.

Login Google real ainda depende do Client ID e das origens autorizadas; não foi validado com conta real.

Referências: [verificação Google](https://developers.google.com/identity/gsi/web/guides/verify-google-id-token), [API JavaScript GIS](https://developers.google.com/identity/gsi/web/reference/js-reference), [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).
