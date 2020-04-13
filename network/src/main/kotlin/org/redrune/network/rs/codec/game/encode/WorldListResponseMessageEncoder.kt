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

    override fun encode(writer: PacketWriter, msg: WorldListResponseMessage) {
        val (full) = msg
        writer.writeOpcode(88, PacketType.SHORT)
        writer.writeByte(1)
        writer.writeByte(2)
        writer.writeByte(if (full) 1 else 0)
        if (full) {
            // TODO pass network core to individual repo, for now write junk data
            writer.writeSmart(1)
            writer.writeSmart(38) // canada
            writer.writePrefixedString("Canada")
            writer.writeSmart(0)
            writer.writeSmart(2)
            writer.writeSmart(1)
            writer.writeSmart(1) // worldId
            writer.writeByte(0)
            writer.writeInt(0x1)
            writer.writePrefixedString("Game World")
            writer.writePrefixedString("127.0.0.1")
            writer.writeInt(0x94DA4A87.toInt())
        }
        writer.writeSmart(1)
        writer.writeShort(1337)
    }
}