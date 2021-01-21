package world.gregs.void.network.codec.game.encode

import world.gregs.void.buffer.write.BufferWriter
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.network.codec.Encoder
import world.gregs.void.network.codec.game.GameOpcodes.NPC_UPDATING
import world.gregs.void.network.packet.PacketSize

/**
 * @author GregHib <greg@gregs.world>
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