package part3

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/**
 * Async allows running coroutines in parallel
 */

fun lesson3() {
  runBlockingBehavingAsExpected()
  // runBlockingCausingYeOldeHeadScratcher()
  // letsDoSomeMath()
  // runBlocking { goNuts() }
}

fun runBlockingBehavingAsExpected() = runBlocking {
  launch { delay(1000); println("1") }
  launch { delay(500); println("2") }
  println("3")
}

fun runBlockingCausingYeOldeHeadScratcher() = runBlocking {
  coroutineScope {
    launch { delay(1000); println("1") }
    launch { delay(500); println("2") }
  } // suspending function
  println("3")
}

fun letsDoSomeMath() = measureTimeMillis {
  runBlocking {
    val x = async { delay(500); 1 }
    val y = async { delay(1000); 2 }
    val z = async { delay(2000); 3 }

    println(x.await() + y.await() + z.await())
  }
}.let(::println)

suspend fun goNuts() = coroutineScope {
  List(100000) { 1 }.map {
    async { delay(1000); it }
  }.awaitAll().sum().let(::println)
}

suspend fun byTheWay() {
  // async { } // this doesn't compile
  val deferred: Deferred<Int> = coroutineScope {
    async { 1 }
  }
}
