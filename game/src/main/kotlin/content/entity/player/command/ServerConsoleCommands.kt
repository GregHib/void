package content.entity.player.command

import content.entity.player.stat.syncBonusExperience
import content.social.report.mute
import content.social.report.unmute
import content.social.trade.exchange.GrandExchange
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.client.command.ConsoleCommands
import world.gregs.voidps.engine.client.command.consoleAlias
import world.gregs.voidps.engine.client.command.consoleCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.chat.toSIIntOrNull
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.data.SaveQueue
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.encode.systemUpdate
import java.lang.management.ManagementFactory
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
        consoleCommand(
            "help",
            stringArg("command-name", desc = "Command to look up, otherwise they're all listed", optional = true, autofill = { ConsoleCommands.commands.keys.toSet() }),
            desc = "List the available console commands",
        ) { args ->
            val name = args.getOrNull(0)
            if (name == null) ConsoleCommands.usages() else ConsoleCommands.help(name)
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
            rest = true,
            handler = ::kick,
        )

        consoleCommand(
            "announce",
            stringArg("message", desc = "Text to broadcast"),
            desc = "Send a message to all online players",
            rest = true,
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

        consoleCommand(
            "reload",
            stringArg("config-type", desc = "Type of content config file to reload", autofill = reloadTypes),
            desc = "Reload configuration files for the game server",
            handler = ::reload,
        )

        consoleCommand(
            "mute",
            stringArg("player-name", desc = "Display name of an online player", autofill = accountDefinitions.displayNames.keys),
            intArg("hours", desc = "How long to mute them for, 48 by default", optional = true),
            desc = "Mute an online player so they can't chat",
            handler = ::mute,
        )

        consoleCommand(
            "unmute",
            stringArg("player-name", desc = "Display name of an online player", autofill = accountDefinitions.displayNames.keys),
            desc = "Remove an online player's mute",
            handler = ::unmute,
        )

        consoleCommand("uptime", desc = "How long the server has been running for", handler = ::uptime)

        consoleCommand(
            "bonus_xp",
            stringArg("state", desc = "on or off, otherwise the current state is reported", optional = true, autofill = setOf("on", "off")),
            desc = "Start or end Bonus XP Weekend without restarting",
            handler = ::bonusExperience,
        )

        consoleAlias("shutdown", "quit", "exit", "stop")
        consoleAlias("players", "list", "online")
        consoleAlias("help", "?", "commands")
        consoleAlias("bonus_xp", "bonusxp", "bxp")
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

    private fun reload(args: List<String>): List<String> {
        val type = args[0]
        val lines = mutableListOf<String>()
        if (!reloadConfig(type) { message -> lines.add(message) }) {
            return listOf("Unknown config type '$type'.")
        }
        lines.add("Reloaded $type.")
        return lines
    }

    private fun mute(args: List<String>): List<String> {
        val name = args[0]
        val target = Players.find(name) ?: return listOf("Unable to find player '$name' online.")
        val hours = args.getOrNull(1)?.toSIIntOrNull() ?: DEFAULT_MUTE_HOURS
        target.mute(hours)
        AuditLog.info("console_muted ${target.accountName} $hours")
        return listOf("${target.name} has been muted for $hours ${"hour".plural(hours)}.")
    }

    private fun unmute(args: List<String>): List<String> {
        val name = args[0]
        val target = Players.find(name) ?: return listOf("Unable to find player '$name' online.")
        target.unmute()
        AuditLog.info("console_unmuted ${target.accountName}")
        return listOf("${target.name} has been unmuted.")
    }

    /**
     * The setting is only read when a player spawns, so switching it over also has to start or stop
     * the event for everyone already online. It lasts until the server restarts or the settings are
     * reloaded, both of which take the value from game.properties again.
     */
    private fun bonusExperience(args: List<String>): List<String> {
        val active = Settings[BONUS_EXPERIENCE, false]
        val state = args.getOrNull(0) ?: return listOf("Bonus XP Weekend is ${state(active)}.")
        val enable = when (state.lowercase()) {
            "on", "true", "start", "enable" -> true
            "off", "false", "stop", "disable" -> false
            else -> return listOf("Unknown state '$state', expected on or off.")
        }
        if (enable == active) {
            return listOf("Bonus XP Weekend is already ${state(active)}.")
        }
        Settings.load(mapOf(BONUS_EXPERIENCE to enable.toString()))
        val count = syncBonusExperience()
        AuditLog.info("console_bonus_experience $enable")
        return listOf(
            "Bonus XP Weekend ${if (enable) "started" else "ended"} for $count ${"player".plural(count)}.",
            "Lasts until the server restarts or the settings are reloaded.",
        )
    }

    private fun state(active: Boolean): String = if (active) "active" else "inactive"

    private fun uptime(args: List<String>): List<String> {
        val millis = ManagementFactory.getRuntimeMXBean().uptime
        val runtime = Runtime.getRuntime()
        val used = (runtime.totalMemory() - runtime.freeMemory()) / MEGABYTE
        val max = runtime.maxMemory() / MEGABYTE
        return listOf(
            "Up for ${duration(millis)} over ${GameLoop.tick} ticks.",
            "${Players.size} ${"player".plural(Players.size)} online, ${NPCs.size} npcs.",
            "Using ${used}mb of ${max}mb.",
        )
    }

    /**
     * Milliseconds as the days, hours and minutes an operator wants to read.
     */
    private fun duration(millis: Long): String {
        val days = TimeUnit.MILLISECONDS.toDays(millis)
        val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        if (days > 0) {
            return "${days}d ${hours}h ${minutes}m"
        }
        if (hours > 0) {
            return "${hours}h ${minutes}m"
        }
        return "${minutes}m"
    }

    private fun announce(args: List<String>): List<String> {
        val message = args[0]
        for (player in Players) {
            player.message(message)
        }
        return listOf("Announced to ${Players.size} ${"player".plural(Players.size)}: $message")
    }

    private companion object {
        private const val BONUS_EXPERIENCE = "events.bonusExperience.enabled"
        private const val DEFAULT_MUTE_HOURS = 48
        private const val MEGABYTE = 1024 * 1024
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
