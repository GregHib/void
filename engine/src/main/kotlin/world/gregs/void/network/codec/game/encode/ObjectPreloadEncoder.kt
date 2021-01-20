package world.gregs.void.network.codec.game.encode

import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.OBJECT_PRE_FETCH

/**
 * Preloads a object model
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 2, 2020
 */
class ObjectPreloadEncoder : Encoder(OBJECT_PRE_FETCH) {

    fun encode(
        player: Player,
        id: Int,
        modelType: Int
    ) = player.send(3) {
        writeShort(id)
        writeByte(modelType)
    }
}