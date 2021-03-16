package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readSmart
import world.gregs.voidps.network.readUnsignedByte
import world.gregs.voidps.utility.inject

class PublicDecoder : Decoder(BYTE) {

    private val huffman: Huffman by inject()

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.publicMessage(
            player = player,
            effects = packet.readUnsignedByte() shl 8 or (packet.readUnsignedByte() and 0xff),
            message = huffman.decompress(length = packet.readSmart(), message = packet.readBytes(packet.remaining.toInt())) ?: ""
        )
    }

}