package rs.dusk.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.entity.character.move.walkTo
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerOption
import rs.dusk.engine.entity.character.player.PlayerOptions
import rs.dusk.engine.entity.character.player.Players
import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.entity.character.update.visual.player.getFace
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.map.Tile
import rs.dusk.engine.path.PathResult
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.PlayerOptionMessage
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 30, 2020
 */
class PlayerOptionMessageHandler : GameMessageHandler<PlayerOptionMessage>() {

    val logger = InlineLogger()
    val sessions: Sessions by inject()
    val players: Players by inject()
    val bus: EventBus by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: PlayerOptionMessage) {
        val session = ctx.channel().getSession()
        val player = sessions.get(session) ?: return
        val (index, optionIndex) = msg
        val target = players.getAtIndex(index) ?: return
        val option = target.options.get(optionIndex)
        if (option == PlayerOptions.EMPTY_OPTION) {
            logger.info { "Invalid player option $optionIndex ${player.options.get(optionIndex)} for $player on $target" }
            return
        }

        player.walkTo(target, if (option == "Follow") getTileBehind(target) else target.tile) { result ->
            if (result is PathResult.Failure) {
                player.message("You can't reach that.")
                return@walkTo
            }
            bus.emit(PlayerOption(player, target, option, index))
        }
    }

    companion object {
        fun getTileBehind(target: Player): Tile {
            val direction = target.getFace().getDirection()
            return target.tile.minus(direction.delta)
        }
    }

}