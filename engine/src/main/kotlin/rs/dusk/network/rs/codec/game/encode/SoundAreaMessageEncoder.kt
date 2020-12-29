package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.GameOpcodes.SOUND_AREA

/**
 * Incomplete
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 */
class SoundAreaMessageEncoder : MessageEncoder(SOUND_AREA) {

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