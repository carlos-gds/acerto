# Quem Me Deve

Aplicativo Android para controlar compras parceladas feitas no cartão para outras pessoas e dividir despesas recorrentes com amigos ou familiares.

O **Quem Me Deve** mostra quanto cada pessoa deve no mês, acompanha parcelas pagas e pendentes, organiza contas compartilhadas e permite compartilhar cobranças por aplicativos como WhatsApp.

> Projeto em desenvolvimento. A interface utiliza uma identidade visual Liquid Glass e está sendo refinada a partir de um protótipo no Figma.

## Funcionalidades

### Controle financeiro

- Resumo da fatura do mês selecionado.
- Navegação entre meses anteriores e futuros.
- Compras parceladas com progresso de parcelas pagas e pendentes.
- Agrupamento das dívidas por pessoa.
- Marcação individual de parcelas como pagas ou pendentes.
- Controle da parte do usuário em contas divididas.
- Edição e exclusão de lançamentos.

### Contas compartilhadas

- Divisão de contas em partes iguais ou valores personalizados.
- Suporte para despesas como água, energia, internet, aluguel e assinaturas.
- Possibilidade de incluir ou não a parte do próprio usuário.
- Atualização das parcelas relacionadas quando uma conta é editada.

### Pesquisa e histórico

- Pesquisa por pessoa ou título da compra.
- Filtros por situação: todas, pendentes e quitadas.
- Histórico mensal com total, valor pago, valor pendente e quantidade de parcelas.
- Detalhamento das dívidas por pessoa.

### Entrada rápida com IA

A entrada rápida aceita descrições em linguagem natural, por exemplo:

```text
Tênis de 300 em 3x para o Lucas
Panela em 9x para minha mãe, começando há 5 meses
```

O aplicativo tenta extrair o título, valor, quantidade de parcelas, pessoa e período de início. Antes de salvar, os dados são apresentados para confirmação e edição.

A integração usa a API Gemini quando há conexão e chave configurada. Existe um fluxo de fallback local para entradas simples quando a IA não está disponível.

### Privacidade, backup e compartilhamento

- Modo de privacidade para ocultar valores financeiros na tela.
- Backup local em JSON por meio do seletor de arquivos do Android.
- Restauração com validação estrutural e transação atômica no Room.
- Compartilhamento de cobranças usando o seletor nativo do Android.
- Confirmação adicional antes de compartilhar dados quando o modo de privacidade está ativo.

## Tecnologias

- Kotlin.
- Jetpack Compose.
- Material 3.
- Room Database.
- Coroutines e Flow.
- Arquitetura MVVM com Repository pattern.
- Gemini API para entrada rápida assistida por IA.
- OkHttp.
- Gradle Version Catalog.
- JUnit, Robolectric, Compose UI Test e Roborazzi.

## Arquitetura do projeto

```text
app/src/main/java/com/example/
├── data/
│   ├── backup/       # Modelos, serialização, validação e restauração
│   ├── db/           # Room Database e DAOs
│   ├── model/        # Entidades e modelos de domínio
│   ├── repository/   # Regras de acesso e operações financeiras
│   └── service/      # Integrações externas, incluindo Gemini
├── ui/
│   ├── components/   # Cards, diálogos, filtros e componentes Compose
│   ├── screens/      # Telas principais do aplicativo
│   ├── theme/        # Cores, tokens Liquid Glass e tipografia
│   └── viewmodel/    # Estado da interface e ações de negócio
└── util/             # Dinheiro, formatação e compartilhamento
```

Os valores financeiros persistidos no Room são armazenados em centavos como `Long`. A conversão para reais ocorre na camada de apresentação, evitando erros de arredondamento causados por `Double` no banco.

## Pré-requisitos

- Windows, macOS ou Linux.
- Android Studio Ladybug ou versão posterior.
- JDK 17 ou compatível com a versão do Android Gradle Plugin instalada.
- Android SDK com API 36 para compilação.
- Um emulador ou dispositivo físico com Android 7.0 (API 24) ou superior.

## Como executar

1. Clone este repositório ou abra a pasta do projeto no Android Studio.
2. Aguarde a sincronização do Gradle.
3. Selecione um emulador ou conecte um dispositivo com depuração USB habilitada.
4. Execute a configuração `app` pelo botão **Run**.

Para executar os testes unitários pelo terminal, use:

```powershell
gradle :app:testDebugUnitTest
```

O projeto também contém testes instrumentados em `app/src/androidTest`.

## Configuração da API Gemini

A chave da API não deve ser commitada. O projeto usa o Secrets Gradle Plugin.

### Execução local

1. Copie `.env.example` para `.env`.
2. Abra `.env`.
3. Substitua o valor de exemplo:

```env
GEMINI_API_KEY=sua_chave_aqui
```

O arquivo `.env` está listado no `.gitignore` e não deve ser enviado ao GitHub.

### Google AI Studio

Quando o projeto for executado em um ambiente integrado ao Google AI Studio, configure `GEMINI_API_KEY` pelo painel de Secrets do ambiente, sem colocar a chave diretamente no código-fonte.

## Dados e segurança

O aplicativo funciona localmente e não possui sincronização automática em nuvem. Os dados financeiros ficam no banco Room do dispositivo.

Os backups são arquivos JSON sob controle do usuário. A restauração valida os dados antes de substituir os registros e utiliza transação para preservar o banco em caso de falha.

Nunca publique neste repositório:

- `.env`;
- `local.properties`;
- arquivos `.jks` ou `.keystore`;
- tokens e chaves de API;
- arquivos de backup contendo dados pessoais ou financeiros.

## Limitações atuais

- Não há login ou sincronização entre dispositivos.
- O backup automático em nuvem ainda não está implementado.
- A entrada com Gemini depende de conexão e de uma chave válida; o fallback local possui capacidade limitada.
- O design Liquid Glass ainda está em evolução.

## Roadmap

- Finalizar a reformulação visual baseada no protótipo Liquid Glass.
- Criar uma tela dedicada de configurações.
- Refinar a barra de navegação inferior.
- Adicionar histórico detalhado por pessoa.
- Implementar lembretes de parcelas próximas do vencimento.
- Melhorar a cobertura de testes de interface.
- Preparar uma versão de release assinada para distribuição.

## Contribuição

Antes de alterar o código:

1. Crie uma branch específica para a mudança.
2. Preserve as regras financeiras e a compatibilidade do backup.
3. Execute os testes disponíveis.
4. Descreva no pull request o que foi alterado e como foi testado.

## Licença

A licença do projeto ainda não foi definida. Até que uma licença seja adicionada, o código deve ser tratado como **todos os direitos reservados**.
