package rs.dusk.engine.client.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.model.session.getSession
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.model.entity.obj.ObjectOption
import rs.dusk.engine.model.entity.obj.Objects
import rs.dusk.engine.path.PathResult
import rs.dusk.network.rs.codec.game.GameMessageHandler
import rs.dusk.network.rs.codec.game.decode.message.ObjectOptionMessage
import rs.dusk.utility.inject
import rs.dusk.world.entity.approach

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 30, 2020
 */
class ObjectOptionMessageHandler : GameMessageHandler<ObjectOptionMessage>() {

    val logger = InlineLogger()
    val sessions: Sessions by inject()
    val objects: Objects by inject()
    val bus: EventBus by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: ObjectOptionMessage) {
        val session = ctx.channel().getSession()
        val player = sessions.get(session) ?: return
        val (objectId, x, y, run, option) = msg
        val tile = player.tile.copy(x = x, y = y)
        val loc = objects[tile, objectId] ?: return
        val definition = loc.def
        val options = definition.options
        val index = option - 1
        if (options == null || index !in options.indices) {
            //Invalid option
            return
        }
        val selectedOption = options[index]
        player.approach(loc) { result ->
            val partial = result is PathResult.Success.Partial
            bus.emit(ObjectOption(player, loc, selectedOption, partial))
        }
    }

}