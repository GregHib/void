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
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.AuditLog
import java.util.concurrent.TimeUnit

// Max value rather than -1 as small numbers are loaded back from saves as ints
const val PERMANENT = Long.MAX_VALUE

val Player.isMuted: Boolean
    get() = this["muted_until", 0L] > System.currentTimeMillis()

fun Player.mute(hours: Int = 48) {
    this["muted_until"] = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(hours.toLong())
    message("You have been temporarily muted due to breaking a rule.")
}

fun Player.permMute() {
    this["muted_until"] = PERMANENT
    message("You have been permanently muted due to breaking a rule.")
}

fun Player.unmute() {
    clear("muted_until")
}

/**
 * Informs a muted player why their chat attempt was blocked
 */
fun Player.sendMuteMessage() {
    val until = this["muted_until", 0L]
    if (until == PERMANENT) {
        message("You are permanently muted because of breaking a rule.")
    } else {
        val day = TimeUnit.DAYS.toMillis(1)
        val days = (until - System.currentTimeMillis() + day - 1) / day
        message("You are temporarily muted because of breaking a rule. This mute will remain for a further $days ${"day".plural(days)}. To prevent further mutes please read the rules.")
    }
}

class Mute(val accounts: AccountDefinitions) : Script {

    init {
        modCommand("mute", stringArg("player-name", autofill = accounts.displayNames.keys), intArg("hours", optional = true), desc = "Temporarily mute a player so they can't chat") { args ->
            val target = Players.find(args[0])
            if (target == null) {
                message("Unable to find player '${args[0]}'.")
                return@modCommand
            }
            val hours = args.getOrNull(1)?.toIntOrNull() ?: 48
            target.mute(hours)
            message("${target.name} has been muted for $hours hours.")
            AuditLog.event(this, "muted", target, hours)
        }

        modCommand("permmute", stringArg("player-name", autofill = accounts.displayNames.keys), desc = "Permanently mute a player so they can't chat") { args ->
            val target = Players.find(args[0])
            if (target == null) {
                message("Unable to find player '${args[0]}'.")
                return@modCommand
            }
            if (target.blackMarks < BLACK_MARK_LIMIT) {
                message("${target.name} has ${target.blackMarks} black marks; $BLACK_MARK_LIMIT are required for a permanent mute.")
                return@modCommand
            }
            target.permMute()
            message("${target.name} has been permanently muted.")
            AuditLog.event(this, "perm_muted", target)
        }

        modCommand("unmute", stringArg("player-name", autofill = accounts.displayNames.keys), desc = "Remove a player's mute") { args ->
            val target = Players.find(args[0])
            if (target == null) {
                message("Unable to find player '${args[0]}'.")
                return@modCommand
            }
            target.unmute()
            message("${target.name} has been unmuted.")
            AuditLog.event(this, "unmuted", target)
        }
    }
}
