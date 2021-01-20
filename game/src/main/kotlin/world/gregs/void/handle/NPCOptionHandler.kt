package world.gregs.void.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import world.gregs.void.engine.client.Sessions
import world.gregs.void.engine.entity.character.move.walkTo
import world.gregs.void.engine.entity.character.npc.NPCOption
import world.gregs.void.engine.entity.character.npc.NPCs
import world.gregs.void.engine.entity.character.update.visual.player.face
import world.gregs.void.engine.entity.character.update.visual.watch
import world.gregs.void.engine.event.EventBus
import world.gregs.void.engine.path.PathResult
import world.gregs.void.network.codec.Handler
import world.gregs.void.network.codec.game.encode.message
import world.gregs.void.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 30, 2020
 */
class NPCOptionHandler : Handler() {

    val logger = InlineLogger()
    val sessions: Sessions by inject()
    val npcs: NPCs by inject()
    val bus: EventBus by inject()

    override fun npcOption(context: ChannelHandlerContext, run: Boolean, npcIndex: Int, option: Int) {
        val session = context.channel()
        val player = sessions.get(session) ?: return
        val npc = npcs.getAtIndex(npcIndex) ?: return
        val options = npc.def.options
        val index = option - 1
        if (index !in options.indices) {
            //Invalid option
            return
        }

        player.watch(npc)
        player.face(npc)
        val selectedOption = options[index]
        player.walkTo(npc) { result ->
            player.watch(null)
            player.face(npc)
            if (result is PathResult.Failure) {
                player.message("You can't reach that.")
                return@walkTo
            }
            val partial = result is PathResult.Partial
            bus.emit(NPCOption(player, npc, selectedOption, partial))
        }
    }

}