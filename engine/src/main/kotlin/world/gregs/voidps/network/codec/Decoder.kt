package world.gregs.voidps.network.codec

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player

abstract class Decoder(val length: Int) {

    var handler: Handler? = null

    open fun decode(player: Player, packet: Reader) {}
}