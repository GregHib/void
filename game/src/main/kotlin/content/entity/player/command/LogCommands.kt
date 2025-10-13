package content.entity.player.command

import world.gregs.voidps.engine.client.command.*
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.event.Log
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.TICKS
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@Script
class LogCommands {
    val accounts: AccountDefinitions by inject()

    init {
        modCommand("log_limit", intArg("past-hours", desc = "how many hours back to search through", optional = true), desc = "Set the limit for how many hours of logs to search through") { player, args ->
            player["log_hours"] = args[0].toInt()
        }
        modCommand("logs", *varArgs("term", desc = "term(s) to search logs for"), desc = "Search logs for a generic string", handler = this::search)
    }

    fun search(player: Player, args: List<String>) {
        if (delayed(player)) {
            return
        }
        val start = System.currentTimeMillis()
        val hours = player["log_hours", if (player.isAdmin()) 3 else 1]
        val terms = if (args.isEmpty()) null else args.map { it.lowercase() }.toSet()
        val after = LocalDateTime.now().minusHours(hours.toLong())
        val ownCommands = "PLAYER ${player.accountName}\tCOMMAND"
        val finds = mutableListOf<String>()
        search { date, lines ->
            for (line in lines) {
                if (line.contains(ownCommands, ignoreCase = true)) {
                    continue
                }
                val parts = line.split("\t")
                val test = line.replace("\t", " ")
                if (terms == null || terms.all { test.contains(it, ignoreCase = true) }) {
                    val time = parts[0].toLong()
                    val format = if (hours > 12) DateTimeFormatter.ISO_LOCAL_DATE_TIME else DateTimeFormatter.ISO_LOCAL_TIME
                    val formatted = format.format(LocalDateTime.ofEpochSecond(TimeUnit.MILLISECONDS.toSeconds(time), 0, ZoneOffset.UTC))
                    finds.add("[${formatted}] ${parts.drop(2).joinToString(" ")}")
                }
            }
            date.isBefore(after)
        }
        val shown = finds.takeLast(250)
        for (line in shown) {
            player.message(line, ChatType.Console)
        }
        player.message("${finds.size} results found (${shown.size} shown) in ${System.currentTimeMillis() - start} ms.", ChatType.Console)
    }

    private fun delayed(player: Player): Boolean {
        if (player.hasClock("search_delay")) {
            val seconds = TICKS.toSeconds(player.remaining("search_delay"))
            player.message("Requesting too quickly; try again in $seconds ${"second".plural(seconds)}.", ChatType.Console)
            return true
        }
        player.start("search_delay", 5)
        return false
    }

    private fun search(block: (LocalDateTime, List<String>) -> Boolean) {
        val directory = File(Settings["storage.players.logs"])
        for (file in directory.listFiles()!!.sortedBy { it.name }) {
            val dateTime = LocalDateTime.parse(file.nameWithoutExtension, Log.ISO_LOCAL_FORMAT)
            if (block.invoke(dateTime, file.readLines())) {
                break
            }
        }
    }
}