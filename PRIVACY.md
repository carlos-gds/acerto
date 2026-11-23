# 🔒 Política de privacidade do Acerto

_Última atualização: 22 de novembro de 2026_

O Acerto é um app para anotar quem te deve. Ele não tem conta, login, anúncios nem servidor próprio.

## O que fica no seu aparelho

- Pessoas, compras, contas divididas, parcelas e pagamentos ficam num banco de dados local do app.
- A chave PIX, o nome e o banco usados na cobrança ficam só no aparelho e **não entram** no arquivo de backup.
- As preferências (tema, modo privacidade, bloqueio, lembrete) também ficam no aparelho.
- O widget da tela inicial mostra só o total em aberto e o número de pessoas. No modo privacidade ele esconde o valor.

## Quando algo sai do aparelho

| Situação | O que é enviado | Para onde |
|---|---|---|
| **Lançar com IA** (só se você usar) | O texto que você digitou, para transformar em compra | Google Gemini API |
| **Cobrar** (só se você tocar) | A mensagem ou imagem da cobrança | O app que você escolher (ex.: WhatsApp) |
| **Backup / planilha** (só se você tocar) | O arquivo JSON ou CSV | O local que você escolher |
| **Backup automático do Android** (se estiver ligado no celular) | Os dados do app | A sua conta Google, pelo próprio sistema |

O Acerto não coleta estatísticas de uso, não rastreia e não vende dados.

## Permissões

- **Internet**: só para o "Lançar com IA".
- **Biometria**: para o bloqueio opcional com digital, rosto ou senha do celular.
- **Notificações** e **iniciar após reiniciar**: para o lembrete mensal opcional.

## Apagar seus dados

Desinstale o app ou limpe os dados dele nas configurações do Android. Apague também os arquivos de backup que você salvou.

## Contato

Dúvidas ou problemas: abra uma issue em https://github.com/carlos-gds/acerto/issues
