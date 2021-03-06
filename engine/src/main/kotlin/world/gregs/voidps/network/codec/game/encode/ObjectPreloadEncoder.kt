package world.gregs.voidps.network.codec.game.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.OBJECT_PRE_FETCH

/**
 * Preloads a object model
 * @author GregHib <greg@gregs.world>
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