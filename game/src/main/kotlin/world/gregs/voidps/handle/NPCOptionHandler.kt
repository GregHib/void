package world.gregs.voidps.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.network.codec.Handler
import world.gregs.voidps.network.codec.game.encode.message
import world.gregs.voidps.utility.inject

/**
 * @author GregHib <greg@gregs.world>
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