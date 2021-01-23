package world.gregs.voidps.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.character.player.PlayerOptions
import world.gregs.voidps.engine.entity.character.player.Players
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
class PlayerOptionHandler : Handler() {

    val logger = InlineLogger()
    val sessions: Sessions by inject()
    val players: Players by inject()
    val bus: EventBus by inject()

    override fun playerOption(context: ChannelHandlerContext, index: Int, optionIndex: Int) {
        val session = context.channel()
        val player = sessions.get(session) ?: return
        val target = players.getAtIndex(index) ?: return
        val option = target.options.get(optionIndex)
        if (option == PlayerOptions.EMPTY_OPTION) {
            logger.info { "Invalid player option $optionIndex ${player.options.get(optionIndex)} for $player on $target" }
            return
        }

        player.watch(target)
        player.face(target)

        val under = player.tile == target.tile
        val follow = option == "Follow"
        val strategy = if (follow && under) target.followTarget else target.interactTarget
        player.walkTo(target, strategy) { result ->
            player.watch(null)
            player.face(target)
            if (result is PathResult.Failure) {
                player.message("You can't reach that.")
                return@walkTo
            }
            bus.emit(PlayerOption(player, target, option, index))
        }
    }
}