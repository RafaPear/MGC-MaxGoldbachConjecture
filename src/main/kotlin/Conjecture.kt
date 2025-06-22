// Kotlin 1.9 — versão super‑optimizada (Crivo + BitLookup + SkipPersisted)
// Autor: Rafael Pereira

import java.io.File
import java.io.FileOutputStream
import kotlin.math.sqrt

/**
 * Calcula primos e decomposições de Goldbach até N sem recalcular o que já foi
 * persistido em disco.
 * Melhorias face à versão anterior:
 *  • Crivo de Eratóstenes completo → O(n log log n) para gerar primos.
 *  • Tabela `isPrime` global reutilizável para look‑up O(1).
 *  • `goldbach()` agora **detecta números pares já presentes em pairs.csv** e
 *    evita recalculá‑los, podendo correr incrementalmente em qualquer ordem.
 *  • Escrita em streaming: nada fica em estruturas gigantes na heap.
 */
object Conjecture {
    /* ────────────────────────── Armazenamento ────────────────────────── */
    private val primesFile = File(PRIMES_FILENAME)
    private val pairsFile  = File(PAIRS_FILENAME)

    /** Lista de primos conhecida (cresce monotonicamente). */
    private val primeList  = mutableListOf<Int>()

    /** Tabela booleana de primalidade válida até `currentLimit`. */
    private var isPrime    = BooleanArray(2) { false }       // [0] e [1] não são primos
    private var currentLimit = 1                             // valor máximo coberto por `isPrime`

    init {
        ensureFiles()
        loadPrimes()        // tenta reaproveitar primes.txt
    }

    /* ──────────────────────────── PRIMOS ───────────────────────────── */

    /** Garante que todos os primos ≤ *n* estão carregados em memória e em ficheiro. */
    fun computePrimes(n: Int) {
        if (n <= currentLimit) return        // já temos tudo
        val newLimit = n

        /* 1) Crivo de Eratóstenes completo até newLimit */
        val sieve = BooleanArray(newLimit + 1) { true }
        sieve[0] = false; sieve[1] = false
        val root = sqrt(newLimit.toDouble()).toInt()
        for (p in 2..root) {
            if (sieve[p]) {
                var k = p * p
                while (k <= newLimit) {
                    sieve[k] = false
                    k += p
                }
            }
        }

        /* 2) Adiciona ao `primeList` apenas os recém‑chegados */
        val startIdx = primeList.size                // para persistir só novos
        for (i in currentLimit + 1 .. newLimit) {
            if (sieve[i]) primeList.add(i)
        }

        /* 3) Actualiza estado global */
        isPrime = sieve
        currentLimit = newLimit

        /* 4) Escreve os novos primos no ficheiro */
        appendNewPrimes(startIdx)
    }

    /* ─────────────────────────── GOLDBACH ──────────────────────────── */

    /**
     * Grava **todos** os pares de primos (p,q) com p+q = even para cada par 4..upTo.
     * Se o número par já existir em *pairs.csv*, é ignorado → execução incremental.
     */
    fun goldbach(upTo: Int) {
        require(upTo >= 4) { "Goldbach só se aplica a números ≥ 4." }
        computePrimes(upTo)                     // garante base de primos

        /* 1) Descobre quais os pares já persistidos em ficheiro */
        val done = hashSetOf<Int>()
        pairsFile.forEachLine { line ->
            line.substringBefore(',').toIntOrNull()?.let { done.add(it) }
        }

        /* 2) Streaming writer (append) */
        val out = FileOutputStream(pairsFile, true).bufferedWriter()

        var idxCut = 0                          // índice máximo em primeList ≤ half
        for (even in 4..upTo step 2) {
            if (even in done) continue          // já gravado, salta

            val half = even / 2
            while (idxCut < primeList.size && primeList[idxCut] <= half) idxCut++

            for (i in 0 until idxCut) {
                val p = primeList[i]
                val q = even - p
                if (isPrime[q]) out.appendLine("$even,$p,$q")
            }
        }
        out.flush()
        out.close()
    }

    /* ───────────────────────── Utilidades I/O ───────────────────────── */

    private fun ensureFiles() {
        if (!primesFile.exists()) primesFile.createNewFile()
        if (!pairsFile.exists())  pairsFile.createNewFile()
    }

    private fun loadPrimes() {
        primesFile.forEachLine { line ->
            line.trim().toIntOrNull()?.let { primeList.add(it) }
        }
        if (primeList.isNotEmpty()) {
            currentLimit = primeList.last()
            isPrime = BooleanArray(currentLimit + 1) { false }
            for (p in primeList) isPrime[p] = true
        }
    }

    private fun appendNewPrimes(fromExclusive: Int) {
        if (fromExclusive >= primeList.size) return
        FileOutputStream(primesFile, true).bufferedWriter().use { out ->
            for (i in fromExclusive until primeList.size) out.appendLine(primeList[i].toString())
        }
    }
}
