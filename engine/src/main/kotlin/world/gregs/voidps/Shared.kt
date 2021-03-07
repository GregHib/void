package world.gregs.voidps

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

object Shared {
    @JvmStatic
    fun main(args: Array<String>): Unit = runBlocking {

        val state = MutableSharedFlow<Int>(replay = 20, onBufferOverflow = BufferOverflow.DROP_LATEST)

        GlobalScope.launch(Dispatchers.Default) {
            val list = mutableListOf<Int>()
            while(true) {
                list.clear()
                println("List ${state.replayCache}")
                state.resetReplayCache()
                delay(600)
            }
        }
        GlobalScope.launch(Dispatchers.IO) {
            var count = 0
            while(true) {
                state.emit(count++)
                delay(1)
            }
        }

        delay(100000)

    }
}