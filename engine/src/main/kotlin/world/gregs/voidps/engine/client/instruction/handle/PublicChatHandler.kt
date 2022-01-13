package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.encode.publicChat
import world.gregs.voidps.network.instruct.PublicChat

class PublicChatHandler : InstructionHandler<PublicChat>() {

    private val huffman: Huffman by inject()

    override fun validate(player: Player, instruction: PublicChat) {
        val writer = BufferWriter(128)
        huffman.compress(instruction.message, writer)
        val data = writer.toArray()
        player.viewport.players.current.forEach {
            it.client?.publicChat(data, player.index, instruction.effects, player.rights.ordinal)
        }
    }

}