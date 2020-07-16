package rs.dusk.engine.client.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.model.entity.index.npc.NPCOption
import rs.dusk.engine.model.entity.index.npc.NPCs
import rs.dusk.engine.path.PathResult
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.NPCOptionMessage
import rs.dusk.utility.inject
import rs.dusk.world.entity.approach

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 30, 2020
 */
class NPCOptionMessageHandler : GameMessageHandler<NPCOptionMessage>() {

    val logger = InlineLogger()
    val sessions: Sessions by inject()
    val npcs: NPCs by inject()
    val bus: EventBus by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: NPCOptionMessage) {
        val session = ctx.channel().getSession()
        val player = sessions.get(session) ?: return
        val (run, npcIndex, option) = msg
        val npc = npcs.getAtIndex(npcIndex) ?: return
        val options = npc.def.options
        val index = option - 1
        if (index !in options.indices) {
            //Invalid option
            return
        }
        val selectedOption = options[index]
        player.approach(npc) { result ->
            val partial = result is PathResult.Success.Partial
            bus.emit(NPCOption(player, npc, selectedOption, partial))
        }
    }

}