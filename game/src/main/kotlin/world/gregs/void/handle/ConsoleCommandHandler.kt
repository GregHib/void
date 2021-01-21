package world.gregs.void.handle

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.engine.client.Sessions
import world.gregs.void.engine.event.EventBus
import world.gregs.void.engine.task.TaskExecutor
import world.gregs.void.engine.task.sync
import world.gregs.void.network.codec.Handler
import world.gregs.void.utility.inject
import world.gregs.void.world.command.Command

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