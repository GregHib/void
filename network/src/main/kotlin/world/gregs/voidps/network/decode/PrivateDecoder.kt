package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.PrivateChat
import world.gregs.voidps.network.readSmart
import world.gregs.voidps.network.readString

class PrivateDecoder(private val huffman: Huffman) : Decoder(SHORT) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val name = packet.readString()
        println("Decode private message")
        val message = huffman.decompress(length = packet.readSmart(), message = packet.readBytes(packet.remaining.toInt())) ?: ""
        instructions.emit(PrivateChat(name, message))
    }

}