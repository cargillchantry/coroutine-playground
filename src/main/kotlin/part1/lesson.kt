package part1

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Coroutines -> run within -> Coroutine Context
 * Coroutine Context -> has a -> Job
 * Coroutine Context -> has a -> Dispatcher
 * Coroutine Scope -> has a -> Coroutine Context
 * Coroutines -> inherit -> Coroutine Context of Coroutine Scope
 */

fun lesson1() {
  CoroutineScopeExample().use {
    it.boom()
    // it.boomQuestionMark()
    // it.boomQuestionMarkyMark()
    // it.soundOfSilence()
    runBlocking { delay(3000) }
  }
}

class CoroutineScopeExample: AutoCloseable {
  private val scope = CoroutineScope(context = Dispatchers.Default + Job() + CoroutineName("Example"))

  fun boom() {
    val coroutineJob: Job = scope.launch(context = CoroutineName("Another"), start = CoroutineStart.DEFAULT) {
      launch(CoroutineName("1")) { delay(1000); println("1") }
      launch(CoroutineName("2")) { delay(500); println("2") }
      launch(CoroutineName("3")) { delay(750); throw IllegalStateException("Uhoh") }
      delay(2000)
      println("Done")
    }
  }

  fun boomQuestionMark() = scope.launch {
    launch(CoroutineName("1")) { delay(500); println("1") }
    launch(CoroutineName("2")) { delay(1000); println("2") }
    delay(750)
    coroutineContext.cancelChildren()
    launch { println("3") }
  }

  fun boomQuestionMarkyMark() = scope.launch {
    val outerContext = this.coroutineContext
    launch(CoroutineName("1")) { delay(500); println("1") }
    launch(CoroutineName("2")) { delay(1000); println("2 ${coroutineContext == outerContext}"); coroutineContext.cancel() }
    delay(1250)
    launch { println("Done?") }
  }

  fun soundOfSilence() = scope.launch {
    coroutineContext.cancel()
    launch { println("No way buddeh") }
  }

  override fun close() {
    scope.cancel()
  }
}
