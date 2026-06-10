package content.social.report

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.modCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.event.AuditLog
import java.util.concurrent.TimeUnit

val Player.isBanned: Boolean
    get() = this["banned_until", 0L] > System.currentTimeMillis()

fun Player.ban(hours: Int = 48, rule: Rule? = null) {
    this["banned_until"] = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(hours.toLong())
    repeat(2) {
        addBlackMark(rule)
    }
}

fun Player.permBan() {
    this["banned_until"] = PERMANENT
}

fun Player.unban() {
    clear("banned_until")
}

class Ban(val accounts: AccountDefinitions, val manager: AccountManager, val storage: Storage) : Script {

    init {
        modCommand("ban", stringArg("player-name", autofill = accounts.displayNames.keys), intArg("hours", optional = true), desc = "Temporarily ban a player from logging in") { args ->
            val hours = args.getOrNull(1)?.toIntOrNull() ?: 48
            val until = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(hours.toLong())
            val target = Players.find(args[0])
            if (target != null) {
                target.ban(hours)
                AuditLog.event(this, "banned", target, hours)
                manager.logout(target, false)
            } else if (!banOffline(args[0], until)) {
                message("Unable to find player '${args[0]}'.")
                return@modCommand
            } else {
                AuditLog.event(this, "banned", args[0], hours)
            }
            message("${args[0]} has been banned for $hours hours.")
        }

        modCommand("permban", stringArg("player-name", autofill = accounts.displayNames.keys), desc = "Permanently ban a player from logging in") { args ->
            val target = Players.find(args[0])
            if (target != null) {
                if (target.blackMarks < BLACK_MARK_LIMIT) {
                    message("${args[0]} has ${target.blackMarks} black marks; $BLACK_MARK_LIMIT are required for a permanent ban.")
                    return@modCommand
                }
                target.permBan()
                AuditLog.event(this, "perm_banned", target)
                manager.logout(target, false)
            } else {
                val save = storage.load(accounts.get(args[0])?.accountName ?: args[0])
                if (save == null) {
                    message("Unable to find player '${args[0]}'.")
                    return@modCommand
                }
                val marks = activeBlackMarks((save.variables["black_marks"] as? List<*>)?.filterIsInstance<String>() ?: emptyList())
                if (marks.size < BLACK_MARK_LIMIT) {
                    message("${args[0]} has ${marks.size} black marks; $BLACK_MARK_LIMIT are required for a permanent ban.")
                    return@modCommand
                }
                setOfflineVariable(args[0], "banned_until", PERMANENT)
                AuditLog.event(this, "perm_banned", args[0])
            }
            message("${args[0]} has been permanently banned.")
        }

        modCommand("unban", stringArg("player-name", autofill = accounts.displayNames.keys), desc = "Remove a player's ban") { args ->
            val target = Players.find(args[0])
            if (target != null) {
                target.unban()
            } else if (!setOfflineVariable(args[0], "banned_until", null)) {
                message("Unable to find player '${args[0]}'.")
                return@modCommand
            }
            AuditLog.event(this, "unbanned", args[0])
            message("${args[0]} has been unbanned.")
        }
    }

    /**
     * Bans an offline player's saved account and adds two black marks
     */
    private fun banOffline(displayName: String, until: Long): Boolean {
        val account = accounts.get(displayName)?.accountName ?: displayName
        val save = storage.load(account) ?: return false
        val variables = save.variables.toMutableMap()
        variables["banned_until"] = until
        val marks = activeBlackMarks((variables["black_marks"] as? List<*>)?.filterIsInstance<String>() ?: emptyList())
        variables["black_marks"] = marks + blackMark() + blackMark()
        storage.save(listOf(save.copy(variables = variables)))
        return true
    }

    /**
     * Updates a variable on an offline player's saved account
     */
    private fun setOfflineVariable(displayName: String, key: String, value: Any?): Boolean {
        val account = accounts.get(displayName)?.accountName ?: displayName
        val save = storage.load(account) ?: return false
        val variables = save.variables.toMutableMap()
        if (value == null) {
            variables.remove(key)
        } else {
            variables[key] = value
        }
        storage.save(listOf(save.copy(variables = variables)))
        return true
    }
}
