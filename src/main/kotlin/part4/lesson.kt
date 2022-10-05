package part4

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// Flow by example

fun lesson4() = runBlocking {
  basicFlow()
  // flowReducing()
  // zippityDo()
  // channelToFlow()
  // channelLoop()
  // broadCastChannel()
  // sharedFlow()
  // shareIn()
}

suspend fun basicFlow() {
  flow {
    (0..Int.MAX_VALUE).forEach {
      delay(1000)
      emit(it)
    }
  }
    .onEach(::println)
    .collect()
}

@OptIn(FlowPreview::class) // flatMapConcat
suspend fun flowReducing() {
  listOf(1, 2, 3, 4).map { it * 2 }.asFlow().toList().let(::println)
  listOf(1, 2, 3, 4).asFlow().flatMapConcat { flowOf(it, it * 2) }.toList().let(::println)
  flowOf(0, 1, 2, 3, 4).transform { emit(it * 2); emit(it * 2 + 1) }.toList().let(::println)
  flowOf(1, 2, 3, 4, 5, 6).reduce { x, y -> x + y }.let(::println)
  flowOf(1, 2, 3, 4, 5, 6).fold(0 to 0) { (first, second), next ->
    if (next % 2 == 0) {
      first to second + next
    } else {
      first + next to second
    }
  }.let(::println)
}

suspend fun zippityDo() {
  val time = System.currentTimeMillis()
  flow {
    (0..Int.MAX_VALUE).forEach {
      delay(500)
      emit(it)
    }
  }
    .zip((1..Int.MAX_VALUE).asFlow()) { x, y ->
      x to y
    }
    // .conflate()
    // .buffer(5)
    .collect {
      delay(1000)
      println("$it ${(System.currentTimeMillis() - time)/it.second}")
    }
}

suspend fun channelToFlow() = coroutineScope {
  val channel = Channel<Int>(5)
  launch {
    (1..10).forEach { delay(100); channel.send(it) }
    channel.close()
  }

  channel.consumeAsFlow().collect(::println)

}

suspend fun channelLoop() = coroutineScope {
  val another = produce {
    var x = 0
    while (true) {
      delay(100)
      send(++x)
    }
  }

  listOf(
    launch {
      repeat(5) {
        println(another.receive())
      }
    },
    launch {
      repeat(5) {
        println(another.receive())
      }
    }
  ).joinAll()
  another.cancel()
}

@OptIn(ObsoleteCoroutinesApi::class) // replaced by shared flow
suspend fun broadCastChannel() = coroutineScope {
  val broadcastChannel = BroadcastChannel<Int>(5)
  val x = broadcastChannel.openSubscription()
  val y = broadcastChannel.openSubscription()


  launch { (1..5).forEach { broadcastChannel.send(it) } }
  listOf(
    launch(Dispatchers.Default) {
      repeat(5) {
        println(x.receive())
      }
    },
    launch(Dispatchers.Default) {
      repeat(5) {
        println(y.receive())
      }
    }
  ).joinAll()
  broadcastChannel.cancel()
}

suspend fun sharedFlow() = coroutineScope {
  val sharedflow = MutableSharedFlow<Int>(5)

  launch(Dispatchers.Default) { sharedflow.collect { println("1: $it") } }
  (1..10).forEach { sharedflow.emit(it) }
  launch(Dispatchers.Default) { sharedflow.collect { println("2: $it") } }
  (10..20).forEach { sharedflow.emit(it) }

  delay(100)
}

suspend fun shareIn() = coroutineScope {
  val coroutineScope = CoroutineScope(Dispatchers.Default)
  val shared = flow {
    var x = 1
    while(true) {
      delay (10)
      emit(++x)
    }
  }.shareIn(coroutineScope, SharingStarted.Eagerly, replay = 5)

  delay(100)

  coroutineScope.launch { shared.collect { delay(100); println("f: $it") } }
  coroutineScope.launch { shared.collect { delay(50); println("s: $it") } }

  delay(1000)
  coroutineScope.cancel()
}
