package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.readSmart
import world.gregs.voidps.network.readString
import world.gregs.voidps.utility.inject

class PrivateDecoder(handler: Handler? = null) : Decoder(SHORT, handler) {

    private val huffman: Huffman by inject()

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.privateMessage(
            player = player,
            name = packet.readString(),
            message = huffman.decompress(length = packet.readSmart(), message = packet.readBytes(packet.remaining.toInt())) ?: ""
        )
    }

}