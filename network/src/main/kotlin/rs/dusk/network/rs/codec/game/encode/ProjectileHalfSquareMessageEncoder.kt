package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.PROJECTILE_DISPLACE
import rs.dusk.network.rs.codec.game.encode.message.ProjectileHalfSquareMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 2, 2020
 */
class ProjectileHalfSquareMessageEncoder : GameMessageEncoder<ProjectileHalfSquareMessage>() {

    override fun encode(builder: PacketWriter, msg: ProjectileHalfSquareMessage) {
        val (offset, id, distanceX, distanceY, index, targetIndex, startHeight, endHeight, delay, duration, curve, size) = msg
        builder.apply {
            writeOpcode(PROJECTILE_DISPLACE)
            writeByte(offset)
            var flag = 0
            // Inverse height 0x1
//            flag = flag or 0x1// Ignore end height
//            flag = flag or 0x2// More precise initial height (normally x4)
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
            writeShort(size)
        }
    }
}