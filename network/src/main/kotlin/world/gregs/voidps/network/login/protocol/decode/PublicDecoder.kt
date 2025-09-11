package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.io.readUByte
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ChatPublic
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readSmart

class PublicDecoder(private val huffman: Huffman) : Decoder(BYTE) {

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun decode(packet: Source): Instruction {
        val effects = (packet.readUByte().toInt() shl 8) or packet.readUByte().toInt()
        val message = huffman.decompress(length = packet.readSmart(), message = packet.readByteArray(packet.remaining.toInt())) ?: ""
        return ChatPublic(message, effects)
    }
}
