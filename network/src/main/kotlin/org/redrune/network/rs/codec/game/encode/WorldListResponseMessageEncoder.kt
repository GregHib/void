package org.redrune.network.rs.codec.game.encode

import org.redrune.core.network.codec.packet.access.PacketWriter
import org.redrune.core.network.model.packet.PacketType
import org.redrune.network.rs.codec.game.GameMessageEncoder
import org.redrune.network.rs.codec.game.encode.message.WorldListResponseMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 22, 2020
 */
class WorldListResponseMessageEncoder : GameMessageEncoder<WorldListResponseMessage>() {

    override fun encode(builder: PacketWriter, msg: WorldListResponseMessage) {
        val (full) = msg
        builder.writeOpcode(88, PacketType.SHORT)
        builder.writeByte(1)
        builder.writeByte(2)
        builder.writeByte(if (full) 1 else 0)
        if (full) {
            // TODO pass network core to individual repo, for now write junk data
            builder.writeSmart(1)
            builder.writeSmart(38) // canada
            builder.writePrefixedString("Canada")
            builder.writeSmart(0)
            builder.writeSmart(2)
            builder.writeSmart(1)
            builder.writeSmart(1) // worldId
            builder.writeByte(0)
            builder.writeInt(0x1)
            builder.writePrefixedString("Game World")
            builder.writePrefixedString("127.0.0.1")
            builder.writeInt(0x94DA4A87.toInt())
        }
        builder.writeSmart(1)
        builder.writeShort(1337)
    }
}