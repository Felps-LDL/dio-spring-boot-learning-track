# DIO Spring Boot - Final Project 05: Spring AI (budgeting)

## Introduction

This final module applies Spring AI in a budgeting API while preserving the same layered architecture used across the track.

The goal is to integrate AI capabilities without bypassing domain and use case boundaries.

## Code Context

The project processes voice commands to create and query financial transactions.

Primary flow:

1. Client uploads an audio file.
2. Audio is transcribed into text.
3. The model selects an application tool/use case.
4. The use case persists or queries transaction data.
5. The final response is converted to audio.

## Project Structure

- `src/main/java/dio/budgeting/domain`
  - Domain model and repository contract.
- `src/main/java/dio/budgeting/application`
  - Use cases used by both REST and AI tool calling.
- `src/main/java/dio/budgeting/infrastructure`
  - HTTP adapters, JPA adapters, and integration glue.

## Module-Specific Topics

### Speech-to-text

- Uses `TranscriptionModel` for audio transcription.
- Model settings are configured in `application.properties`.

### Tool calling

- `ChatClient` registers use-case tools.
- `@Tool` methods expose business capabilities to the model.

### Text-to-speech

- `TextToSpeechModel` produces MP3 output from final text.
- AI endpoint returns generated audio.

## Spring AI Documentation

- Spring AI Reference: https://docs.spring.io/spring-ai/reference/index.html
- ChatModel API: https://docs.spring.io/spring-ai/reference/api/chatmodel.html
- ChatClient API: https://docs.spring.io/spring-ai/reference/api/chatclient.html
- Tools API: https://docs.spring.io/spring-ai/reference/api/tools.html
- Audio Transcriptions API: https://docs.spring.io/spring-ai/reference/api/audio/transcriptions.html
- Audio Speech API: https://docs.spring.io/spring-ai/reference/api/audio/speech.html

## Shared Architecture References

Common architecture concepts are documented in the root README:

- [DDD layers](../README.md#ddd-layered-architecture)
- [Class vs record](../README.md#java-class-vs-java-record-in-domain-modeling)
- [Strong typed identifiers](../README.md#strong-typed-identifiers)
- [Repository pattern](../README.md#repository-pattern)
- [Use cases and Clean Architecture](../README.md#use-cases-and-clean-architecture)
- [Docker Compose support](../README.md#docker-compose-support-in-development)

## How to Run

Set your OpenAI API key:

```bash
export OPENAI_API_KEY="your_api_key_here"
```

Run the application and tests:

```bash
./gradlew bootRun
./gradlew test
```

## Evolução: resumo de gastos (`summarize-transactions`)

Nova tool que permite perguntar por voz coisas como *"quanto eu gastei no total?"* ou *"me dá um resumo dos meus gastos"*. A IA chama o caso de uso, que devolve a quantidade de transações e o valor total em reais, no geral e por categoria.

O mesmo caso de uso atende a IA e o REST, mantendo a regra de negócio fora do controller:

| Camada | Arquivo | O que mudou |
| --- | --- | --- |
| Domínio | `domain/TransactionRepository.java` | Novo método `findAll()` |
| Aplicação | `application/SummarizeTransactionsUseCase.java` | Caso de uso com `@Tool(name = "summarize-transactions")` |
| Aplicação | `application/output/TransactionSummaryOutput.java`, `CategorySummaryOutput.java` | Saída do resumo |
| Infraestrutura | `persistence/repository/JpaTransactionRepository.java` | Implementação de `findAll()` |
| Infraestrutura | `http/TransactionController.java` | Tool registrada no `ChatClient` e novo `GET /transactions/summary` |
| Prompt | `resources/prompts/system-message.st` | Orienta o uso da tool e respostas curtas em reais |

Os valores são guardados em centavos (`long`) e convertidos para reais só na saída, com `BigDecimal.valueOf(cents, 2)`, para evitar erros de arredondamento de `double` na soma.

### Como testar

```bash
# testes unitários (não precisam de OpenAI nem de Docker)
./gradlew test --tests 'dio.budgeting.application.*'

# com a aplicação rodando
curl -X POST localhost:8080/transactions -H "Content-Type: application/json" \
  -d '{"description":"Mercado","category":"GROCERIES","amount":8000}'
curl localhost:8080/transactions/summary
# {"count":1,"total":80.0,"categories":[{"category":"GROCERIES","count":1,"total":80.0}]}

# por voz (grave um áudio perguntando "quanto eu gastei no total?")
curl -F "file=@pergunta.m4a" localhost:8080/transactions/ai -o resposta.mp3
```

## O que aprendi

- Como conectar IA a uma aplicação real: a IA não mexe no banco, ela só chama os casos de uso que já existem através do `@Tool`.
- Que a descrição da tool é importante, porque é por ela que a IA decide qual função chamar.
- Como funciona o fluxo de voz: áudio vira texto, a IA entende o pedido e a resposta volta em áudio.
- Que é melhor guardar dinheiro em centavos para evitar erros de arredondamento.
- Que dá para testar a lógica sem chamar a OpenAI, usando um repositório falso nos testes.

## Notes

- Educational final project focused on AI plus architectural discipline.
- External provider integration tests may require active credentials.
