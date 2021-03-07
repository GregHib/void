package world.gregs.voidps

import com.github.michaelbull.logging.InlineLogger
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

object Ping {

    class Session {
    }

    suspend fun read(read: ByteReadChannel, queue: MutableList<Byte>) {
        while (true) {
            val byte = read.readByte()
            println("Read $byte")
            queue.add(byte)
        }
    }

    suspend fun write(write: ByteWriteChannel, queue: MutableList<Byte>) {
        while (true) {
            if (queue.isNotEmpty()) {
                for (next in queue) {
                    println("Write $next")
                    write.writeByte(next)
                }
                queue.clear()
            }
            delay(600)
        }
    }

    val tick = AtomicBoolean(false)

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val logger = InlineLogger()
        val port = 50015
        val executor = Executors.newCachedThreadPool()
        val dispatcher = executor.asCoroutineDispatcher()
        val selector = ActorSelectorManager(dispatcher)
        val supervisor = SupervisorJob()
        val scope = CoroutineScope(Dispatchers.Default + supervisor)
        val players = Collections.synchronizedList(mutableListOf<MutableList<Byte>>())
        var running = false
        GlobalScope.launch(Dispatchers.Default) {
            while (true) {
                val it = players.iterator()
                while (it.hasNext()) {
                    val list = it.next()
                    val value = list.removeFirst()
                    list.add((value + 5).toByte())
                }
                delay(300)
            }
        }
        with(scope) {
            val server = aSocket(selector).tcp().bind(port = port)
            running = true
            logger.info { "Listening for requests on port ${port}..." }
            while (running) {
                val socket = server.accept()
                logger.info { "New connection accepted $socket" }
                val queue = Collections.synchronizedList(LinkedList<Byte>())
                players.add(queue)
                launch(Dispatchers.IO) {
                    val read = socket.openReadChannel()
                    val write = socket.openWriteChannel(autoFlush = true)
                    launch(Dispatchers.IO) {
                        write(write, queue)
                    }
                    read(read, queue)
                }
            }
        }
        running = true
    }
}