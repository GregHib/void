package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.readSmart
import world.gregs.voidps.utility.inject

class PublicDecoder : Decoder(BYTE) {

    private val huffman: Huffman by inject()

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val effects = (packet.readUByte().toInt() shl 8) or packet.readUByte().toInt()
        val message = huffman.decompress(length = packet.readSmart(), message = packet.readBytes(packet.remaining.toInt())) ?: ""
    }

}