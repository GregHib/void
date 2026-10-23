package content.social.party

import content.area.wilderness.inPvp
import content.area.wilderness.inWilderness
import content.entity.player.combat.special.MAX_SPECIAL_ATTACK
import content.entity.player.combat.special.specialAttackEnergy
import content.entity.player.effect.energy.MAX_RUN_ENERGY
import content.entity.player.effect.energy.runEnergy
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.playerCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.timer.Timer

/**
 * A small party for the 634 client's Party plugin (RuneLite's Party, kept on the server): members see each other's
 * hitpoints, prayer, special attack, run energy and tile, and can ping a tile for the others. The client only sends
 * commands (`::party`, `::ping`); the server shows each member the others through client variables
 * (party.varcs.toml), so a client without the plugin is unaffected. A member's tile is hidden while they are in a PvP
 * area; their stats and pings are not.
 */
class Party : Script {

    class Group(var leader: Player) {
        val members = mutableListOf(leader)
        var pings = 0
    }

    class Invite(val inviter: Player, val expires: Int)

    val parties = mutableMapOf<Player, Group>()
    val invites = mutableMapOf<Player, Invite>()

    init {
        playerCommand("party", stringArg("action", autofill = setOf("invite", "accept", "decline", "leave", "kick")), stringArg("player-name", optional = true), desc = "Invite to, join or leave a party (the 634 client's Party plugin)") { args ->
            if (!Settings["party.enabled", true]) {
                message("Parties are not available on this world.")
                return@playerCommand
            }
            when (args[0].lowercase()) {
                "invite" -> invite(this, args.getOrNull(1) ?: return@playerCommand message("Usage: ::party invite <name>"))
                "accept" -> accept(this)
                "decline" -> decline(this)
                "leave" -> leave(this)
                "kick" -> kick(this, args.getOrNull(1) ?: return@playerCommand message("Usage: ::party kick <name>"))
                else -> message("Usage: ::party invite|accept|decline|leave|kick")
            }
        }

        playerCommand("ping", intArg("x"), intArg("y"), intArg("level"), desc = "Ping a tile for your party (the 634 client's Party plugin)") { args ->
            ping(this, args[0].toInt(), args[1].toInt(), args[2].toInt())
        }

        timerStart("party_sync") { 1 }

        timerTick("party_sync") {
            val group = parties[this] ?: return@timerTick Timer.CANCEL
            show(this, group)
            Timer.CONTINUE
        }

        playerDespawn {
            leave(this, silent = true)
            invites.remove(this)
        }
    }

    fun invite(player: Player, name: String) {
        val target = Players.find(name)
        if (target == null || target === player) {
            player.message("There is no one called ${name.replace('_', ' ')} online.")
            return
        }
        val group = parties[player]
        if (group != null && group.leader !== player) {
            player.message("Only the party leader can invite players.")
            return
        }
        if (group != null && group.members.size >= MAX_MEMBERS) {
            player.message("Your party is full.")
            return
        }
        if (parties.containsKey(target)) {
            player.message("${target.name} is already in a party.")
            return
        }
        invites[target] = Invite(player, GameLoop.tick + INVITE_TICKS)
        setName(target, "party_invite", player.name)
        player.message("You invite ${target.name} to your party.")
        // in the request colour of a trade or duel line; the 634 client's Party plugin makes the line's left click accept
        target.message("<col=800080>${player.name} wishes to join you in a party.</col>")
    }

    fun accept(player: Player) {
        val invite = invites.remove(player)
        clearName(player, "party_invite")
        if (invite == null || invite.expires < GameLoop.tick) {
            player.message("You have no party invite.")
            return
        }
        val inviter = invite.inviter
        if (!Players.contains(inviter)) {
            player.message("${inviter.name} is no longer online.")
            return
        }
        val group = parties[inviter] ?: Group(inviter).also { join(inviter, it) }
        if (group.leader !== inviter) {
            player.message("${inviter.name} is no longer the party leader.")
            return
        }
        if (group.members.size >= MAX_MEMBERS) {
            player.message("That party is full.")
            return
        }
        leave(player, silent = true)
        group.members.add(player)
        join(player, group)
        for (member in group.members) {
            if (member !== player) {
                member.message("${player.name} has joined the party.")
            }
        }
        player.message("You join ${group.leader.name}'s party.")
    }

    fun decline(player: Player) {
        val invite = invites.remove(player) ?: return player.message("You have no party invite.")
        clearName(player, "party_invite")
        invite.inviter.message("${player.name} declined your party invite.")
    }

    fun leave(player: Player, silent: Boolean = false) {
        val group = parties.remove(player) ?: return if (!silent) player.message("You are not in a party.") else Unit
        group.members.remove(player)
        player.softTimers.stop("party_sync")
        clear(player)
        if (!silent) {
            player.message("You leave the party.")
        }
        if (group.members.isEmpty()) {
            return
        }
        val wasLeader = group.leader === player
        if (wasLeader) {
            group.leader = group.members.first()
        }
        if (group.members.size == 1) {
            // a party of one is no party
            val last = group.members.first()
            last.message("${player.name} has left the party, and the party is disbanded.")
            leave(last, silent = true)
            return
        }
        for (member in group.members) {
            member.message("${player.name} has left the party." + if (wasLeader && group.leader === member) " You are now the leader." else "")
            clearSlot(member, group.members.size)
        }
        refresh(group)
    }

    fun kick(player: Player, name: String) {
        val group = parties[player]
        if (group == null || group.leader !== player) {
            player.message("Only the party leader can kick players.")
            return
        }
        val target = group.members.firstOrNull { it.name.equals(name.replace('_', ' '), ignoreCase = true) }
        if (target == null || target === player) {
            player.message("There is no one called ${name.replace('_', ' ')} in your party.")
            return
        }
        target.message("You have been removed from the party.")
        leave(target, silent = true)
    }

    fun ping(player: Player, x: Int, y: Int, level: Int) {
        val group = parties[player] ?: return
        if (level !in 0..3 || kotlin.math.abs(player.tile.x - x) > PING_RANGE || kotlin.math.abs(player.tile.y - y) > PING_RANGE) {
            return
        }
        if (player.hasClock("party_ping")) {
            return
        }
        player.start("party_ping", PING_DELAY)
        group.pings = (group.pings + 1) and 0xFFFFFF
        val seq = group.members.indexOf(player) shl 24 or group.pings
        val tile = pack(level, x, y)
        for (member in group.members) {
            member["party_ping_tile"] = tile
            member["party_ping_seq"] = seq
        }
    }

    private fun join(player: Player, group: Group) {
        parties[player] = group
        player.softTimers.start("party_sync")
        refresh(group)
    }

    /** Every member's meta: the count, their own slot and the leader's. */
    private fun refresh(group: Group) {
        val leader = group.members.indexOf(group.leader)
        for ((slot, member) in group.members.withIndex()) {
            member["party_meta"] = group.members.size or (slot shl 4) or (leader shl 8) or (VERSION shl 12)
        }
    }

    /** [player]'s own stats and tile, in their slot of every member's view, themselves included. */
    private fun show(player: Player, group: Group) {
        val slot = group.members.indexOf(player)
        if (slot == -1) {
            return
        }
        val hidden = player.inWilderness || player.inPvp
        val (hi, lo) = base37(player.name)
        val hp = player.levels.get(Skill.Constitution) shl 16 or player.levels.getMax(Skill.Constitution)
        val prayer = player.levels.get(Skill.Prayer) shl 16 or player.levels.getMax(Skill.Prayer)
        val energy = (player.specialAttackEnergy * 100 / MAX_SPECIAL_ATTACK) shl 8 or (player.runEnergy * 100 / MAX_RUN_ENERGY)
        val tile = if (hidden) -1 else pack(player.tile.level, player.tile.x, player.tile.y)
        val flags = if (hidden) 1 else 0
        for (member in group.members) {
            member["party_${slot}_name_hi"] = hi
            member["party_${slot}_name_lo"] = lo
            member["party_${slot}_hp"] = hp
            member["party_${slot}_prayer"] = prayer
            member["party_${slot}_energy"] = energy
            member["party_${slot}_tile"] = tile
            member["party_${slot}_flags"] = flags
        }
    }

    private fun clear(player: Player) {
        player["party_meta"] = 0
        for (slot in 0 until MAX_MEMBERS) {
            clearSlot(player, slot)
        }
        player["party_ping_tile"] = 0
        player["party_ping_seq"] = 0
    }

    private fun clearSlot(player: Player, slot: Int) {
        if (slot >= MAX_MEMBERS) {
            return
        }
        for (field in FIELDS) {
            player["party_${slot}_$field"] = 0
        }
    }

    private fun setName(player: Player, key: String, name: String) {
        val (hi, lo) = base37(name)
        player["${key}_hi"] = hi
        player["${key}_lo"] = lo
    }

    private fun clearName(player: Player, key: String) {
        player["${key}_hi"] = 0
        player["${key}_lo"] = 0
    }

    companion object {
        const val MAX_MEMBERS = 8
        const val VERSION = 1

        /** An invite lasts a minute. */
        const val INVITE_TICKS = 100

        /** A ping is for a tile within this many tiles of the pinger, at most one a tick. */
        const val PING_RANGE = 64
        const val PING_DELAY = 1

        val FIELDS = listOf("name_hi", "name_lo", "hp", "prayer", "energy", "tile", "flags")

        fun pack(level: Int, x: Int, y: Int): Int = level shl 28 or (x shl 14) or y

        /** RuneScape's name encoding: up to 12 of a-z, 0-9 and _ (a space), as a long split into two ints. */
        fun base37(name: String): Pair<Int, Int> {
            var value = 0L
            for (char in name.lowercase().take(12)) {
                value *= 37
                value += when (char) {
                    in 'a'..'z' -> char - 'a' + 1
                    in '0'..'9' -> char - '0' + 27
                    else -> 0
                }
            }
            return (value ushr 32).toInt() to value.toInt()
        }
    }
}
