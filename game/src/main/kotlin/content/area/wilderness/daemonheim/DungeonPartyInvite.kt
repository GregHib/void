package content.area.wilderness.daemonheim

import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonLeader
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonMembers
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.inParty
import content.entity.player.dialogue.type.stringEntry
import content.social.chat.privateStatus
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.req.hasRequest
import world.gregs.voidps.engine.entity.character.player.req.removeRequest
import world.gregs.voidps.engine.entity.character.player.req.request
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class DungeonPartyInvite : Script {
    init {
        entered("daemonheim_castle") {
            options.set(1, "Invite")
        }

        exited("daemonheim_castle") {
            options.remove("Invite")
        }

        playerOperate("Invite") { (target) ->
            if (target.hasRequest(this, "invite")) {
                if (inParty(this)) {
                    target.message("$name is already in a party and cannot join yours.")
                    return@playerOperate
                }
                request(target, "invite") { requester, acceptor ->
                    acceptor["dungeon_inviter"] = requester.name
                    acceptor.open("dungeon_party_invite")
                }
                return@playerOperate
            }
            if (!inParty(this)) {
                message("You can't do that right now.")
                return@playerOperate
            }
            if (dungeonLeader != this) {
                message("<red_orange>Only the party leader can invite someone to a party.")
                return@playerOperate
            }
            invite(target)
        }

        interfaceOption("Invite-player", "dungeoneering_party:invite_player") {
            if (!inParty(this)) {
                return@interfaceOption
            }
            val name = stringEntry("Enter name:")
            val target = Players.find(name)
            if (target == null || target.privateStatus == "off") {
                message("That player is offline, or has privacy mode enabled.")
                return@interfaceOption
            }
            if (dungeonLeader != this) {
                message("Only the party leader can invite more party members.")
                return@interfaceOption
            }
            invite(target)
        }

        interfaceOpened("dungeon_party_invite") {
            val leader = inviter() ?: return@interfaceOpened
            val members = leader.dungeonMembers
            for (i in 0 until 5) {
                val member = members.getOrNull(i)
                if (member == null) {
                    set("dungeon_party_name_$i", "")
                    set("dungeon_party_level_$i", 0)
                    set("dungeon_party_combat_$i", 0)
                    set("dungeon_party_highest_$i", 0)
                    set("dungeon_party_total_$i", 0)
                    continue
                }
                set("dungeon_party_name_$i", member.name)
                set("dungeon_party_level_$i", member.levels.getMax(Skill.Dungeoneering))
                set("dungeon_party_combat_$i", member.combatLevel)
                set("dungeon_party_highest_$i", Skill.all.maxOf { if (it == Skill.Constitution) member.levels.getMax(it) / 10 else member.levels.getMax(it) })
                set("dungeon_party_total_$i", Skill.all.sumOf { if (it == Skill.Constitution) member.levels.getMax(it) / 10 else member.levels.getMax(it) })
            }
            set("dungeon_party_floor", leader["dungeoneering_party_floor", 1])
            set("dungeon_party_complexity", leader["dungeoneering_party_complexity", 1])
        }

        interfaceClosed("dungeon_party_invite") {
            if (contains("dungeon_inviter")) {
                decline()
            }
        }

        interfaceOption("Accept", "dungeon_party_invite:accept") {
            val inviter = inviter()
            if (inviter == null) {
                clearInvite()
                message("Your acceptance of the invitation could not be delivered.")
                return@interfaceOption
            }
            if (inviter.menu != null) {
                clearInvite()
                message("The party leader was too busy to allow you to join the party.")
                return@interfaceOption
            }
            DungeoneeringParty.join(this, inviter)
            clear("dungeon_inviter")
            closeMenu()
        }

        interfaceOption("Decline", "dungeon_party_invite:decline") {
            decline()
        }

        interfaceOption("Decline-forever", "dungeon_party_invite:decline_forever") {
            val inviter = inviter() ?: return@interfaceOption
            getOrPut("permanent_declines") { mutableSetOf<String>() }.add(inviter.name)
            message("<red_orange>Invitations from ${inviter.name} will be ignored until you next login or ignore another 10 players.")
            decline()
        }
    }

    private fun Player.invite(target: Player) {
        if (dungeonMembers.contains(target)) {
            message("${target.name} is already in your party.")
            return
        }
        if (get("dungeon_party_complexity", 1) > target["dungeoneering_complexity", 1]) {
            message("${target.name} cannot access that complexity level.")
            return
        }
        if (get("dungeon_party_floor", 1) > target["dungeoneering_current_progress", 1]) {
            message("${target.name} cannot access that floor.")
            return
        }
        if (target.get<Set<String>>("permanent_declines")?.contains(name) == true) {
            message("${target.name} has permanently declined your invitation.")
            return
        }
        if (hasRequest(target, "invite")) {
            message("You have already invited ${target.name} to your party. Please wait for a response.")
            return
        }
        if (!target.hasRequest(this, "invite")) {
            if (inParty(target)) {
                message("${target.name} is already in a party.")
                return
            }
            message("Sending party invitation to ${target.name}...")
            target.message("has invited you to a dungeon party.", ChatType.Invitation, name = name)
            queue("invite_${target.name}", TimeUnit.SECONDS.toTicks(30)) {
                message("Your dungeon party invitation to ${target.name} has expired.")
                target.message("A dungeon party invitation from ${this@invite.name} has expired.")
                removeRequest(target, "invite")
            }
        }
        request(target, "invite") { requester, acceptor ->
            requester.queue.clear("invite_${acceptor.name}")
            acceptor.open("dungeon_party_invite")
            acceptor["dungeon_inviter"] = requester.name
        }
    }

    private fun Player.clearInvite() {
        closeMenu()
        clear("dungeon_inviter")
    }

    private fun Player.decline() {
        closeMenu()
        val inviter = inviter() ?: return
        clear("dungeon_inviter")
        inviter.queue.clear("invite_$name")
        inviter.message("$name has declined your invitation.")
        message("You decline the invitation from ${inviter.name}")
    }

    private fun Player.inviter(): Player? {
        val inviter: String? = get("dungeon_inviter")
        if (inviter == null && menu == "dungeon_party_invite") {
            closeMenu()
        }
        if (inviter != null) {
            return Players.find(inviter)
        }
        return null
    }
}
