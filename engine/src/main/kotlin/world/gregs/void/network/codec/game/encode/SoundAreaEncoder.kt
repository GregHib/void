package world.gregs.void.network.codec.game.encode

import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.SOUND_AREA

/**
 * Incomplete
 * @author GregHib <greg@gregs.world>
 * @since June 27, 2020
 */
class SoundAreaEncoder : Encoder(SOUND_AREA) {

    fun encode(
        player: Player,
        tile: Int,
        id: Int,
        type: Int,
        rotation: Int,
        three: Int,
        four: Int,
        five: Int
    ) = player.send(8) {
        writeByte(tile)
        writeShort(id)
        writeByte((type shl 4) and rotation)
        writeByte(three)
        writeByte(four)
        writeShort(five)
    }
}