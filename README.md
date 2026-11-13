# Acerto

Aplicativo Android para controlar compras parceladas feitas no cartão para outras pessoas e dividir despesas recorrentes com amigos ou familiares.

O **Acerto** mostra quanto cada pessoa deve no mês, acompanha parcelas pagas e pendentes, organiza contas compartilhadas e gera cobranças prontas (com chave PIX) para enviar pelo WhatsApp ou outro aplicativo.

> O projeto se chamava **Quem Me Deve**. Backups gerados com o nome antigo continuam sendo restaurados normalmente.

## Download

Baixe o APK mais recente na página de [Releases](https://github.com/carlos-gds/acerto/releases/latest). Requer Android 7.0 ou superior.

## Funcionalidades

### Navegação

- Barra inferior com quatro abas: **Início**, **Cartão**, **Cobranças** e **Ajustes**.
- A barra se esconde ao rolar a lista e volta ao rolar para cima.
- Botão flutuante com atalhos para nova compra no cartão, nova conta dividida e entrada rápida com IA.

### Controle financeiro

- Resumo do mês com total, valor pago e valor pendente.
- **Seleção automática do mês**: ao abrir, o app mostra o mês mais antigo com parcelas pendentes de outras pessoas (com o aviso "Pendências"). Se não houver nenhuma, mostra o mês atual. Ao trocar de mês manualmente, a escolha é respeitada.
- Compras parceladas com progresso de parcelas pagas e pendentes.
- Dívidas agrupadas por pessoa, com marcação individual de parcelas.
- Controle da parte do usuário em contas divididas.
- Edição e exclusão de lançamentos.
- Campos de valor tolerantes: aceitam `1.234,56`, `R$ 50`, `1.500` e formatos parecidos.

### Pessoas

- Cadastro com nome, telefone (formatado automaticamente) e cor.
- **Avatar** neutro, masculino ou feminino, escolhido no cadastro ou trocado com um toque no avatar da lista.
- Avatar e cor aparecem nos cards de dívida e na tela de cobranças.

### Contas compartilhadas

- Divisão em partes iguais ou valores personalizados.
- Suporte a despesas como água, energia, internet, aluguel e assinaturas.
- Opção de incluir ou não a parte do próprio usuário.
- Atualização das parcelas relacionadas quando uma conta é editada.

### Cobranças e PIX

- Texto de cobrança por pessoa, com as parcelas do mês e o total pendente.
- **Chave PIX** configurável em Ajustes (CPF, celular, e-mail, CNPJ ou chave aleatória). Ela é incluída automaticamente no texto quando há valor pendente.
- Compartilhamento pelo seletor nativo do Android.

### Pesquisa e histórico

- Pesquisa por pessoa ou título da compra.
- Filtros por situação: todas, pendentes e quitadas.
- Histórico mensal com total, valor pago, valor pendente e quantidade de parcelas.

### Entrada rápida com IA

A entrada rápida aceita descrições em linguagem natural, por exemplo:

```text
Tênis de 300 em 3x para o Lucas
Panela em 9x para minha mãe, começando há 5 meses
```

O aplicativo extrai título, valor, quantidade de parcelas, pessoa e período de início. Antes de salvar, os dados aparecem para confirmação e edição.

A integração usa a API Gemini quando há conexão e chave configurada. Para entradas simples, existe um fallback local que funciona sem IA.

### Privacidade, backup e segurança dos dados

- Modo de privacidade para ocultar valores na tela, com confirmação extra antes de compartilhar.
- Backup local em JSON pelo seletor de arquivos do Android, incluindo o avatar das pessoas.
- Restauração com validação estrutural e transação atômica.
- Operações de vários passos (criar, editar e excluir compras, contas e pessoas) rodam em transações do Room: gravam tudo ou nada.

## Interface

- Identidade visual **Liquid Glass**, com tokens vindos de um protótipo no Figma, nos temas claro e escuro. A troca de tema tem animação.
- **Blur adaptativo** num único APK:
  - Android 12 ou superior: desfoque real nas superfícies de vidro e atrás dos diálogos.
  - Android 13 ou superior: desfoque progressivo.
  - Android 7 a 11, aparelhos com pouca memória ou em economia de bateria: vidro translúcido sem desfoque.
- Microanimações: resposta ao toque com mola, entrada escalonada de listas, valores que contam até o número final, tremida nos campos com erro e avisos com ✓.
- Animações reduzidas quando o sistema pede menos movimento.
- Tela de abertura nativa no Android 12+ e equivalente nas versões anteriores.

## Tecnologias

- Kotlin, Jetpack Compose e Material 3.
- Room Database, com migrations versionadas e esquema exportado em `app/schemas`.
- Coroutines e Flow.
- Arquitetura MVVM com Repository pattern.
- Gemini API (via OkHttp) para a entrada rápida.
- Gradle Version Catalog e Gradle Wrapper.
- JUnit, Robolectric, Compose UI Test e Roborazzi.

## Arquitetura do projeto

```text
app/src/main/java/com/example/
├── data/
│   ├── backup/       # Modelos, serialização, validação e restauração
│   ├── db/           # Room Database, DAOs e migrations
│   ├── model/        # Entidades e modelos de domínio
│   ├── repository/   # Regras de acesso e operações financeiras
│   └── service/      # Integrações externas, incluindo Gemini
├── ui/
│   ├── components/   # Cards, diálogos, barra inferior, avatares
│   │   └── glass/    # Componentes Liquid Glass, blur e animações
│   ├── screens/      # Início, Cobranças e Ajustes
│   ├── theme/        # Cores, tokens Liquid Glass, tipografia e medidas
│   └── viewmodel/    # Estado da interface e ações de negócio
└── util/             # Dinheiro, telefone, PIX, mês automático e compartilhamento
```

Os valores financeiros são armazenados no Room em centavos (`Long`). A conversão para reais acontece na apresentação, o que evita erros de arredondamento de `Double`.

### Versões do banco

| Versão | Mudança |
| --- | --- |
| 1 | Esquema inicial (valores em `REAL`) |
| 2 | Valores convertidos para centavos (`INTEGER`) |
| 3 | Coluna `avatar` em `people` (padrão `NEUTRAL`) |

## Pré-requisitos

- Windows, macOS ou Linux.
- Android Studio Ladybug ou posterior.
- JDK 17 ou superior. O JDK que vem com o Android Studio serve.
- Android SDK com API 36 para compilação.
- Emulador ou dispositivo com Android 7.0 (API 24) ou superior.

## Como executar

1. Clone este repositório ou abra a pasta do projeto no Android Studio.
2. Aguarde a sincronização do Gradle.
3. Selecione um emulador ou conecte um dispositivo com depuração USB habilitada.
4. Execute a configuração `app` pelo botão **Run**.

Pelo terminal, use o Gradle Wrapper incluído no projeto:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

No Windows (PowerShell ou cmd), use `gradlew.bat` no lugar de `./gradlew`.

O projeto também contém testes instrumentados em `app/src/androidTest`.

## Configuração da API Gemini

A chave da API não deve ser commitada. O projeto usa o Secrets Gradle Plugin.

1. Copie `.env.example` para `.env`.
2. Substitua o valor de exemplo:

```env
GEMINI_API_KEY=sua_chave_aqui
```

O arquivo `.env` está no `.gitignore`. A chave é enviada no cabeçalho `x-goog-api-key`, nunca na URL.

> **Atenção:** a chave fica embutida no APK, e qualquer pessoa com o arquivo consegue extraí-la. Use uma chave restrita ao aplicativo no Google Cloud. Para distribuição pública, prefira migrar para o Firebase AI Logic, cuja dependência já está no projeto.

## Dados e segurança

O aplicativo funciona localmente, sem login e sem sincronização em nuvem. Os dados financeiros ficam no banco Room do dispositivo.

Os backups são arquivos JSON sob controle do usuário. A restauração valida os dados antes de substituir os registros e usa transação, o que preserva o banco em caso de falha.

Nunca publique neste repositório:

- `.env`;
- `local.properties`;
- arquivos `.jks` ou `.keystore`;
- tokens e chaves de API;
- arquivos de backup contendo dados pessoais ou financeiros.

A assinatura de release lê o keystore e as senhas das variáveis de ambiente `KEYSTORE_PATH`, `STORE_PASSWORD` e `KEY_PASSWORD`.

## Limitações atuais

- Não há login nem sincronização entre dispositivos.
- Não há backup automático em nuvem.
- A entrada com Gemini depende de conexão e de uma chave válida. O fallback local tem capacidade limitada.

## Roadmap

- Desfazer exclusão de lançamentos.
- Lembretes de parcelas próximas do vencimento.
- PIX copia e cola e QR Code na cobrança.
- Pagamento parcial de parcelas.
- Bloqueio do app com biometria.
- Revisão de acessibilidade (TalkBack e fonte grande).
- Versão de release assinada para distribuição.

## Contribuição

Antes de alterar o código:

1. Crie uma branch específica para a mudança.
2. Preserve as regras financeiras (centavos) e a compatibilidade do backup.
3. Ao mudar o banco, crie uma migration e mantenha o esquema exportado em `app/schemas`.
4. Execute os testes com `./gradlew :app:testDebugUnitTest`.
5. Descreva no pull request o que foi alterado e como foi testado.

## Licença

A licença do projeto ainda não foi definida. Até que uma licença seja adicionada, o código deve ser tratado como **todos os direitos reservados**.
