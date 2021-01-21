package world.gregs.void.network.codec.game.encode

import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.PROJECTILE_ADD

/**
 * @author GregHib <greg@gregs.world>
 * @since June 19, 2020
 */
class ProjectileAddEncoder : Encoder(PROJECTILE_ADD) {

    /**
     * @param offset The tile offset from the chunk update send (encoded with 3 rather than the usual 4)
     * @param id Projectile graphic id
     * @param distanceX The delta between start and end x coordinates
     * @param distanceY The delta between start and end y coordinates
     * @param targetIndex Target index plus one, negated for player
     * @param startHeight 40 = player head height
     * @param endHeight 40 = player head height
     * @param delay time before starting in client ticks, 30 = 1 tick
     * @param duration combined total of time of start delay + reaching target in client ticks, 30 = 1 tick
     * @param curve value between -63..63
     * @param startOffset offset from start coordinate, 64 = 1 tile
     */
    fun encode(
        player: Player,
        offset: Int,
        id: Int,
        distanceX: Int,
        distanceY: Int,
        targetIndex: Int,
        startHeight: Int,
        endHeight: Int,
        delay: Int,
        duration: Int,
        curve: Int,
        startOffset: Int
    ) = player.send(16, flush = false) {
        writeByte(offset)
        writeByte(distanceX)
        writeByte(distanceY)
        writeShort(targetIndex)
        writeShort(id)
        writeByte(startHeight)
        writeByte(endHeight)
        writeShort(delay)
        writeShort(duration)
        writeByte(curve)
        writeShort(startOffset)
    }
}