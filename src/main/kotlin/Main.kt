import kotlin.time.measureTime

const val PAIRS_FILENAME = "pairs.csv"
const val PRIMES_FILENAME = "primes.txt"

fun main(){
    val n = 1000000

    println(measureTime { Conjecture })

    println(measureTime { Conjecture.computePrimes(n) })

    println(measureTime { Conjecture.goldbach(n) })

}