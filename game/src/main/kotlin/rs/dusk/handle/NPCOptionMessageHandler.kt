package rs.dusk.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.entity.character.move.walkTo
import rs.dusk.engine.entity.character.npc.NPCOption
import rs.dusk.engine.entity.character.npc.NPCs
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.update.visual.player.face
import rs.dusk.engine.entity.character.update.visual.watch
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.path.PathResult
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 30, 2020
 */
class NPCOptionMessageHandler : MessageHandler() {

    val logger = InlineLogger()
    val sessions: Sessions by inject()
    val npcs: NPCs by inject()
    val bus: EventBus by inject()

    override fun npcOption(context: ChannelHandlerContext, run: Boolean, npcIndex: Int, option: Int) {
        val session = context.channel().getSession()
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
            val partial = result is PathResult.Success.Partial
            bus.emit(NPCOption(player, npc, selectedOption, partial))
        }
    }

}