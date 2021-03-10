package world.gregs.voidps.network.codec.game.encode

import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.codec.Encoder
import world.gregs.voidps.network.codec.game.GameOpcodes.NPC_UPDATING
import world.gregs.voidps.network.packet.PacketSize

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
        writeBytes(changes.toArray())
        writeBytes(updates.toArray())
        changes.clear()
        updates.clear()
    }
}