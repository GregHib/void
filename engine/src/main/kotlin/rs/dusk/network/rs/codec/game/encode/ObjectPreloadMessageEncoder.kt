package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.OBJECT_PRE_FETCH

/**
 * Preloads a object model
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 2, 2020
 */
class ObjectPreloadMessageEncoder : MessageEncoder(OBJECT_PRE_FETCH) {

    fun encode(
        player: Player,
        id: Int,
        modelType: Int
    ) = player.send(3) {
        writeShort(id)
        writeByte(modelType)
    }
}