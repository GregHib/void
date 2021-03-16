package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.readUnsignedByte

class KeysPressedDecoder(handler: Handler? = null) : Decoder(BYTE, handler) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        val keys = ArrayList<Pair<Int, Int>>()
        while (packet.remaining > 0) {
            keys.add(packet.readUnsignedByte() to packet.readUShort().toInt())
        }
        handler?.keysPressed(player, keys)
    }

}