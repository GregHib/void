package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.io.Source
import kotlinx.io.readByteArray
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ChatPrivate
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readSmart
import world.gregs.voidps.network.login.protocol.readString

class PrivateDecoder(private val huffman: Huffman) : Decoder(BYTE) {

    override suspend fun decode(packet: Source): Instruction {
        val name = packet.readString()
        val message = huffman.decompress(length = packet.readSmart(), message = packet.readByteArray(packet.remaining.toInt())) ?: ""
        return ChatPrivate(name, message)
    }
}
