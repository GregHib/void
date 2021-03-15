package world.gregs.voidps.network.encode

import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.buffer.write.writeShort
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Encoder
import world.gregs.voidps.network.GameOpcodes.PROJECTILE_DISPLACE

/**
 * @author GregHib <greg@gregs.world>
 * @since July 2, 2020
 */
class ProjectileHalfSquareEncoder : Encoder(PROJECTILE_DISPLACE) {

    /**
     * @param offset The tile offset from the chunk update sent (encoded with 3 rather than the usual 4)
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
        index: Int,
        targetIndex: Int,
        startHeight: Int,
        endHeight: Int,
        delay: Int,
        duration: Int,
        curve: Int,
        startOffset: Int
    ) = player.send(19) {
        writeByte(offset)
        val flag = 0
        // Inverse height 0x1
//      flag = flag or 0x1// Ignore end height
//      flag = flag or 0x2// More precise initial height (normally x4)
        writeByte(flag)
        writeByte(distanceX)
        writeByte(distanceY)
        writeShort(index)
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