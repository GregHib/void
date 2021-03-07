package world.gregs.voidps

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import java.net.InetSocketAddress

object Pong {

    suspend fun process(read: ByteReadChannel, write: ByteWriteChannel) {
        println("Sending request")
        repeat(5) {
            write.writeByte(it)
        }
        delay(2000)
        write.close()
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {

        val builder = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
        val address = InetSocketAddress("127.0.0.1", 50015)

        while(true) {
            val socket = builder.connect(address)
            val read = socket.openReadChannel()
            val write = socket.openWriteChannel(autoFlush = true)
            println("Connected")
            GlobalScope.launch(Dispatchers.IO) {
                process(read, write)
            }
            delay(5000)
        }
    }
}