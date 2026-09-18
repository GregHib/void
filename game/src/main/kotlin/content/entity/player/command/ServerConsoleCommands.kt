package content.entity.player.command

import content.social.trade.exchange.GrandExchange
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.client.command.ConsoleCommands
import world.gregs.voidps.engine.client.command.consoleCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.chat.toSIIntOrNull
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.data.SaveQueue
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.encode.systemUpdate
import java.util.concurrent.TimeUnit

/**
 * Commands typed into the terminal running the server.
 *
 * The console has no [Player] to check rights against or reply to, so instead of faking one these
 * handlers delegate to the same functions the equivalent player commands call, and return their
 * output as lines for [ConsoleCommands] to log.
 */
class ServerConsoleCommands(
    val accountDefinitions: AccountDefinitions,
    val accountLoader: PlayerAccountLoader,
    val accounts: AccountManager,
    val exchange: GrandExchange,
    val saveQueue: SaveQueue,
) : Script {

    init {
        consoleCommand("help", desc = "List the available console commands") {
            ConsoleCommands.usages()
        }

        // Registered for the command list and completion, the reader clears the terminal itself
        consoleCommand(ConsoleCommands.CLEAR, desc = "Clear the console output") {
            emptyList()
        }

        consoleCommand("players", desc = "Count and list the online players") {
            val list = mutableListOf("${Players.size} ${"player".plural(Players.size)} online:")
            for (player in Players) {
                list.add("  ${player.name} ${player.tile}")
            }
            list
        }

        consoleCommand(
            "kick",
            // Completion runs on the console thread, so names come from the definitions the player
            // commands autofill from rather than from Players, which the game thread is mutating
            stringArg("player-name", desc = "Display name of the player", autofill = accountDefinitions.displayNames.keys),
            desc = "Disconnect a player",
            handler = ::kick,
        )

        consoleCommand(
            "announce",
            stringArg("message", desc = "Text to broadcast (use quotes for spaces)"),
            desc = "Send a message to all online players",
            handler = ::announce,
        )

        consoleCommand("save", desc = "Save all online players") {
            val count = Players.size
            saveAll(saveQueue, exchange)
            listOf("Saving $count ${"player".plural(count)}.")
        }

        consoleCommand(
            "shutdown",
            intArg("seconds", desc = "Seconds to count down before shutting down", optional = true),
            desc = "Shutdown the server, optionally after a countdown",
            handler = ::shutdown,
        )
    }

    private fun kick(args: List<String>): List<String> {
        val name = args[0]
        val target = Players.find(name)
        if (target == null) {
            return listOf("Unable to find player '$name' online.")
        }
        AuditLog.event(target, "console_kick")
        // Script.launch is unconfined so the suspending logout starts on the game thread
        Script.launch {
            accounts.logout(target, safely = false)
        }
        return listOf("Kicked '${target.name}'.")
    }

    private fun announce(args: List<String>): List<String> {
        val message = args[0]
        for (player in Players) {
            player.message(message)
        }
        return listOf("Announced to ${Players.size} ${"player".plural(Players.size)}: $message")
    }

    private fun shutdown(args: List<String>): List<String> {
        val seconds = args.getOrNull(0)?.toSIIntOrNull() ?: 0
        if (seconds < 0) {
            return listOf("Shutdown countdown must be positive.")
        }
        val ticks = TimeUnit.SECONDS.toTicks(seconds)
        if (ticks >= Short.MAX_VALUE) {
            return listOf("Shutdown cannot exceed ${Short.MAX_VALUE} ticks (5 hours 26 mins 43 seconds).")
        }
        for (player in Players) {
            player.client?.systemUpdate(ticks)
        }
        AuditLog.info("console_shutdown $ticks")
        queueShutdown(accountLoader, (ticks - 2).coerceAtLeast(0))
        return listOf("Shutting down in $seconds ${"second".plural(seconds)}.")
    }
}
