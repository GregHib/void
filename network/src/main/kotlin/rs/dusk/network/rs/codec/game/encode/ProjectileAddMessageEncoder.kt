package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.message.MessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameOpcodes.PROJECTILE_ADD
import rs.dusk.network.rs.codec.game.encode.message.ProjectileAddMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 19, 2020
 */
class ProjectileAddMessageEncoder : MessageEncoder<ProjectileAddMessage>() {

    override fun encode(builder: PacketWriter, msg: ProjectileAddMessage) {
        val (offset, id, distanceX, distanceY, targetIndex, startHeight, endHeight, delay, duration, curve, size) = msg
        builder.apply {
            writeOpcode(PROJECTILE_ADD)
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
            writeShort(size)
        }
    }
}