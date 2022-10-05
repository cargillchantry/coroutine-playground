package part2

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import java.lang.IllegalArgumentException
import kotlin.IllegalStateException

/**
 * SupervisorJob prevents cancellation of coroutinescope when children fail
 */

fun lesson2() = runBlocking {
  SupervisorJobExample().use {
    it.doesItRun()
    // it.doesItRunNow()
    // ohBtw()

    runBlocking { delay(3000) }
  }
}

class SupervisorJobExample: AutoCloseable {
  private val scope = CoroutineScope(context =
    Dispatchers.Default
      + SupervisorJob()
      + CoroutineName("Example")
  )

  fun doesItRun() = scope.launch {
    launch { throw IllegalStateException("boohoo") }
    launch { delay(1000); println("naww dawg") }
    println("Lets see: ")
  }

  fun doesItRunNow() {
    println("Lets see:")
    scope.launch { throw IllegalArgumentException("boohoo") }
    scope.launch { delay(1000); println("my dawgggggg") }
  }

  override fun close() {
    scope.cancel()
  }
}

fun ohBtw() {
  runBlocking {
    supervisorScope {
      launch { throw IllegalArgumentException("whoops") }
      launch { delay(100); println("yes") }
    }
  }
}
