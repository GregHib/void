package world.gregs.voidps.handle

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.sync
import world.gregs.voidps.network.codec.Handler
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.dialogue.event.StringEntered

/**
 * @author GregHib <greg@gregs.world>
 * @since August 04, 2020
 */
class StringEntryHandler : Handler() {

    val sessions: Sessions by inject()
    val bus: EventBus by inject()

    override fun stringEntered(context: ChannelHandlerContext, text: String) {
        val session = context.channel()
        val player = sessions.get(session) ?: return
        sync {
            bus.emit(StringEntered(player, text))
        }
    }

}