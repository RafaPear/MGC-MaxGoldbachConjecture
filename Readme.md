# MGC - Max Goldbach's Conjecture

Implementação em Kotlin 1.9 de um gerador de números primos e verificador incremental da Conjectura de Goldbach.

## Visão geral

O ficheiro `Conjecture.kt` oferece duas operações principais:

* **computePrimes(n)** – gera todos os primos até *n* com um crivo de Eratóstenes optimizado e persiste‑os em `primes.txt`.
* **goldbach(upTo)** – calcula todas as decomposições de Goldbach para números pares de 4 até *upTo*, adicionando novas entradas a `pairs.csv` sem repetir trabalho já efectuado.

Ambos os métodos executam em streaming, evitando estruturas de dados volumosas na heap.

## Estrutura de ficheiros

| Ficheiro        | Conteúdo                                                |
| --------------- | ------------------------------------------------------- |
| `Conjecture.kt` | Código‑fonte principal com lógica de primos e Goldbach. |
| `primes.txt`    | Lista persistente de primos, um por linha.              |
| `pairs.csv`     | Registos `par,primo1,primo2` já verificados.            |

## Como compilar e executar

```bash
kotlinc Conjecture.kt -include-runtime -d conjecture.jar
java -jar conjecture.jar
```

No `main`, invocar:

```kotlin
Conjecture.computePrimes(1_000_000)
Conjecture.goldbach(1_000_000)
```

## Contribuição

Contribuições são bem‑vindas. Seguir o estilo de código actual, adicionar testes a nova lógica e actualizar esta documentação sempre que necessário.
