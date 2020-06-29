package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 29, 2020
 * @param offset The tile offset from the [ChunkUpdateMessage] sent (encoded with 3 rather than the usual 4)
 * @param id Projectile graphic id
 * @param deltaX The delta between start and end x coordinates
 * @param deltaY The delta between start and end y coordinates
 * @param targetIndex Target index plus one, negated for player
 * @param startHeight 40 = player head height
 * @param endHeight 40 = player head height
 * @param delay time before starting in client cycles, 30 = 1 tick
 * @param duration combined total of time of start delay + reaching target in client cycles, 30 = 1 tick
 * @param curve value between -63..63
 * @param startOffset offset from start coordinate, 64 = 1 tile
 */
data class ProjectileAddMessage(
    val offset: Int,
    val id: Int,
    var deltaX: Int,
    var deltaY: Int,
    var targetIndex: Int,
    var startHeight: Int,
    var endHeight: Int,
    var delay: Int,
    var duration: Int,
    var curve: Int,
    var startOffset: Int
) : Message