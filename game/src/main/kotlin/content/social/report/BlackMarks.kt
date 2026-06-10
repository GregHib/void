package content.social.report

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.modCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.AuditLog
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Black marks are a disciplinary measure used to track the offences of a player.
 * Most expire after 12 months; serious offences such as real-world trading never expire.
 * Once a player has accumulated [BLACK_MARK_LIMIT] active marks they can be
 * permanently muted or permanently banned.
 */
const val BLACK_MARK_LIMIT = 10

private val TWELVE_MONTHS = TimeUnit.DAYS.toMillis(365)
private val PERMANENT_RULES = setOf(Rule.BreakingRealWorldLaws)
private val DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy").withZone(ZoneOffset.UTC)

val Player.blackMarks: Int
    get() = activeBlackMarks().size

/**
 * The player's unexpired black marks, removing any that have degraded
 */
fun Player.activeBlackMarks(): List<String> {
    val marks = this["black_marks", emptyList<String>()]
    val active = activeBlackMarks(marks)
    if (active.size != marks.size) {
        if (active.isEmpty()) {
            clear("black_marks")
        } else {
            this["black_marks"] = active
        }
    }
    return active
}

fun activeBlackMarks(marks: List<String>): List<String> {
    val now = System.currentTimeMillis()
    return marks.filter { expiry(it) > now }
}

fun Player.addBlackMark(rule: Rule) {
    val expiry = if (rule in PERMANENT_RULES) PERMANENT else System.currentTimeMillis() + TWELVE_MONTHS
    this["black_marks"] = activeBlackMarks() + "${rule.id}:$expiry"
}

private fun expiry(mark: String): Long = mark.substringAfter(':').toLongOrNull() ?: 0L

private fun describe(mark: String): String {
    val rule = Rule.byId(mark.substringBefore(':').toIntOrNull() ?: -1)
    val expiry = expiry(mark)
    val expires = if (expiry == PERMANENT) "never expires" else "expires ${DATE_FORMAT.format(Instant.ofEpochMilli(expiry))}"
    return "${rule?.title ?: "Unknown offence"} - $expires"
}

class BlackMarks(val accounts: AccountDefinitions) : Script {

    init {
        modCommand("blackmark", stringArg("player-name", autofill = accounts.displayNames.keys), intArg("rule-id", desc = "id of the rule broken"), desc = "Add a black mark to a player's account") { args ->
            val rule = Rule.byId(args[1].toIntOrNull() ?: -1)
            if (rule == null) {
                message("Invalid rule id '${args[1]}'.")
                return@modCommand
            }
            val target = Players.find(args[0])
            if (target == null) {
                message("Unable to find player '${args[0]}'.")
                return@modCommand
            }
            target.addBlackMark(rule)
            message("Black mark added to ${target.name} for ${rule.title}; they now have ${target.blackMarks}.")
            AuditLog.event(this, "black_marked", target, rule.name)
        }

        modCommand("blackmarks", stringArg("player-name", autofill = accounts.displayNames.keys), desc = "View a player's black marks") { args ->
            val target = Players.find(args[0])
            if (target == null) {
                message("Unable to find player '${args[0]}'.")
                return@modCommand
            }
            val marks = target.activeBlackMarks()
            message("${target.name} has ${marks.size} black ${"mark".plural(marks.size)}.", ChatType.Console)
            for (mark in marks) {
                message(describe(mark), ChatType.Console)
            }
        }
    }
}
