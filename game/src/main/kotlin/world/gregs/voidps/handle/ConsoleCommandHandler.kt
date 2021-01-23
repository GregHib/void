package world.gregs.voidps.handle

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.task.TaskExecutor
import world.gregs.voidps.engine.task.sync
import world.gregs.voidps.network.codec.Handler
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.command.Command

/**
 * @author GregHib <greg@gregs.world>
 * @since April 30, 2020
 */
class ConsoleCommandHandler : Handler() {

    val sessions: Sessions by inject()
    val bus: EventBus by inject()
    val executor: TaskExecutor by inject()

    override fun consoleCommand(context: ChannelHandlerContext, command: String) {
        val channel = context.channel()
        val player = sessions.get(channel) ?: return
        val parts = command.split(" ")
        val prefix = parts[0]
        executor.sync {
            bus.emit(Command(player, prefix, command.removePrefix("$prefix ")))
        }
    }

}