package rs.dusk.network.codec.game.encode

import rs.dusk.buffer.write.BufferWriter
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.codec.Encoder
import rs.dusk.network.codec.game.GameOpcodes.NPC_UPDATING
import rs.dusk.network.packet.PacketSize

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class NPCUpdateEncoder : Encoder(NPC_UPDATING, PacketSize.SHORT) {

    fun encode(
        player: Player,
        changes: BufferWriter,
        updates: BufferWriter
    ) = player.send(changes.position() + updates.position()) {
        writeBytes(changes.buffer)
        writeBytes(updates.buffer)
        changes.buffer.clear()
        updates.buffer.clear()
    }
}