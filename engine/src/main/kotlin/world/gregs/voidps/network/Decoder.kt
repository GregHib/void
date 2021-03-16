package world.gregs.voidps.network

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player

abstract class Decoder(val length: Int, var handler: Handler? = null) {

    open fun decode(player: Player, packet: ByteReadPacket) {}

    companion object {
        const val BYTE = -1
        const val SHORT = -2
    }
}