package content.area.wilderness.daemonheim

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name

class DungeoneeringParty : Script {

    init {
        entered("daemonheim_castle_exit") {
            refreshDetails()
        }

        exited("daemonheim_castle_exit") {
            refreshDetails()
        }

        exited("daemonheim_castle") {
            leave(this)
        }

        interfaceOpened("dungeoneering_party") {
            set("quest_tab", "party_organiser")
            refreshDetails()
        }

        interfaceOption("Close", "dungeoneering_party:close") {
            open("quest_journals")
            clear("quest_tab")
        }

        /*
            Party controls
         */

        interfaceOption("Form-party", "dungeoneering_party:form_party") {
            if (inParty(this)) {
                return@interfaceOption
            }
            setLeader(this)
        }

        interfaceOption("Leave-party", "dungeoneering_party:leave_party,dungeoneering_party:leave_party_button") {
            if (!inParty(this)) {
                return@interfaceOption
            }
            leave(this)
        }

        playerDespawn {
            leave(this)
        }

        /*
            Dungeon settings
         */

        interfaceOption("Change-floor", "dungeoneering_party:change_floor") {
            if (!inParty(this)) {
                message("You must be in a party to do that.")
                return@interfaceOption
            }
            open("dungeon_floor")
        }

        interfaceOption("Change-complexity", "dungeoneering_party:change_complexity") {
            if (!inParty(this)) {
                message("You must be in a party to do that.")
                return@interfaceOption
            }
            open("dungeon_complexity")
        }

        interfaceOption("Toggle", "dungeoneering_party:guide_mode") {
            if (!inParty(this)) {
                message("You must be in a party to do that.")
                return@interfaceOption
            }
            if (this != dungeonLeader) {
                message("<red_orange>Only the party leader can change the guide mode.")
                return@interfaceOption
            }
            if (inDungeoneering) {
                message("<red_orange>You cannot change the guide mode once the dungeon has started.")
                return@interfaceOption
            }
            val guideMode = toggle("dungeoneering_guide_mode")
            for (member in dungeonMembers) {
                member.interfaces.sendVisibility("dungeoneering_party", "guide_mode_selected", guideMode)
            }
        }

        interfaceOption("Reset", "dungeoneering_party:reset") {
            if (get("dungeoneering_current_progress", 0) == 0) {
                message("You must have completed at least one floor to reset your progress.")
                return@interfaceOption
            }
            statement("Are you sure you want to reset your dungeon progress? Your previous progress will be set to the number of floors you have completed and all floors will be marked as incomplete. This cannot be undone.")
            choice {
                option("Yes, reset my progress") {
                    set("dungeoneering_previous_progress", get("dungeoneering_current_progress", 0))
                    set("dungeoneering_current_progress", 0)
                    message("Your dungeon progress have been reset.")
                }
                option("No, don't reset my progress.")
            }
        }

        /*
            Member controls
         */

        interfaceOption("Toggle-shared-xp", "dungeoneering_party:xp_*") {
            val index = it.component.removePrefix("xp_").toInt()
            val member = dungeonMembers.getOrNull(index) ?: return@interfaceOption
            if (name != member.name) {
                message("You can only change your own shared XP settings.")
                return@interfaceOption
            }
            val disabled = toggle("dungeoneering_disable_xp_share")
            message("Your shared XP is set to ${if (disabled) "OFF" else "ON"}. You will ${if (disabled) "not gain" else "gain a share of the"} XP for skill tasks you could have completed that are performed by other party members.")
            for (member in dungeonMembers) {
                member["dungeoneering_member_disabled_xp_share_$index"] = disabled
            }
        }

        interfaceOption("Inspect", "dungeoneering_party:member_*") {
            message("You must be in a dungeon to do that.")
        }

        interfaceOption("Kick", "dungeoneering_party:member_*") {
            val index = it.component.removePrefix("member_").toInt()
            val member = dungeonMembers.getOrNull(index) ?: return@interfaceOption
            if (name == member.name) {
                message("You can't kick yourself form a party!")
                return@interfaceOption
            }
            if (this != dungeonLeader && dungeonMembers.size == 2) {
                message("A vote to kick in a 2-player party cannot succeed.")
                return@interfaceOption
            }
            // TODO kick vote
            member.message("You were kicked from the party.")
            leave(member)
        }

        interfaceOption("Promote", "dungeoneering_party:member_*") {
            val leader = dungeonLeader ?: return@interfaceOption
            val index = it.component.removePrefix("member_").toInt()
            val member = dungeonMembers.getOrNull(index) ?: return@interfaceOption
            if (name != leader.name) {
                message("Only the party leader can promote members.")
                return@interfaceOption
            }
            if (name == member.name) { // Promote self
                message("You can't promote the party leader.")
                return@interfaceOption
            }
            statement("Are you sure you want to promote ${member.name}? They will become the party leader if you do.")
            choice("Promote ${member.name} to party leader?") {
                option("Yes.") {
                    promote(this, member, leave = false)
                }
                option("No.")
            }
        }
    }

    companion object {
        private const val ID = "dungeoneering_party"
        private const val GREEN = 0x007800
        private const val RED = 0x780000

        fun inParty(player: Player) = player.dungeonLeader != null

        val Player.dungeonLeader: Player?
            get() {
                val name: String = get("dungeoneering_party_leader") ?: return null
                if (name == this.name) {
                    return this
                }
                return Players.find(name)
            }

        val Player.inDungeoneering: Boolean
            get() {
                return get("in_dungeoneering", false)
            }

        var Player.dungeonMembers: List<Player>
            get() {
                val leader = dungeonLeader ?: return emptyList()
                return (0 until 5).mapNotNull { i ->
                    val name = leader.get<String>("dungeoneering_member_$i")
                    name?.let { Players.find(it) }
                }
            }
            set(value) {
                val leader = dungeonLeader ?: return
                for (i in 0 until 5) {
                    val member = value.getOrNull(i)
                    setMember(leader, member, i)
                }
            }

        fun join(player: Player, leader: Player) {
            val members = leader.dungeonMembers
            val index = (0 until 5).firstOrNull { members.getOrNull(it) == null }
            if (index == null) {
                // TODO party full proper message
                return
            }
            if (leader["dungeon_party_complexity", 1] > player["dungeoneering_complexity", 1]) {
                player.message("That parties options are too advanced.")
                return
            }
            if (leader["dungeon_party_floor", 1] > player["dungeoneering_current_progress", 1]) {
                player.message("That parties options are too advanced.")
                return
            }
            setMember(leader, player, index)
            for (member in leader.dungeonMembers) {
                setMember(member, player, index)
                member.message("${player.name} has joined the party.")
                member.sendMembers()
            }
            player["dungeoneering_party_leader"] = leader.name
            player["dungeoneering_party_floor"] = leader["dungeoneering_party_floor", 1]
            player["dungeoneering_party_complexity"] = leader["dungeoneering_party_complexity", 1]
            player.refreshDetails()
        }

        fun leave(player: Player) {
            player.message("You leave the party.")
            player.dungeonMembers -= player
            val leader = player.dungeonLeader
            if (player == leader && player.dungeonMembers.isNotEmpty()) {
                promote(player, player.dungeonMembers.first(), leave = true)
            }
            for (member in player.dungeonMembers) {
                if (member != player) {
                    member.message("${player.name} has left the party.")
                }
                member.refreshDetails()
            }
            player.clear("dungeoneering_party_leader")
            player["dungeoneering_party_floor"] = 0
            player["dungeoneering_party_complexity"] = 0
            for (i in 0 until 5) {
                player.clear("dungeoneering_member_$i")
                player.clear("dungeoneering_member_disabled_xp_share_$i")
            }
            player.refreshDetails()
        }

        private fun Player.refreshDetails() {
            val inParty = inParty(this)
            if (inParty) {
                interfaces.sendVisibility(ID, "no_options", false)
                interfaces.sendVisibility(ID, "form_party_group", false)
                if (dungeonLeader == this && !inDungeoneering) {
                    interfaces.sendVisibility(ID, "leave_party_leader", true)
                    interfaces.sendVisibility(ID, "invite_party_leader", true)
                    interfaces.sendVisibility(ID, "leave_party_member", false)
                    interfaces.sendColour(ID, "left_colour", RED)
                    interfaces.sendColour(ID, "right_colour", GREEN)
                    interfaces.sendVisibility(ID, "left_colour", true)
                    interfaces.sendVisibility(ID, "right_colour", true)
                } else {
                    interfaces.sendVisibility(ID, "leave_party_leader", false)
                    interfaces.sendVisibility(ID, "invite_party_leader", false)
                    interfaces.sendVisibility(ID, "leave_party_member", true)
                    interfaces.sendColour(ID, "left_colour", RED)
                    interfaces.sendColour(ID, "right_colour", RED)
                    interfaces.sendVisibility(ID, "left_colour", true)
                    interfaces.sendVisibility(ID, "right_colour", true)
                }
                sendVariable("dungeoneering_party_floor")
                sendVariable("dungeoneering_party_complexity")
            } else {
                if (tile in Areas["daemonheim_castle_exit"]) {
                    interfaces.sendVisibility(ID, "no_options", true)
                    interfaces.sendVisibility(ID, "form_party_group", false)
                    interfaces.sendVisibility(ID, "left_colour", false)
                    interfaces.sendVisibility(ID, "right_colour", false)
                } else {
                    interfaces.sendVisibility(ID, "no_options", false)
                    interfaces.sendVisibility(ID, "form_party_group", true)
                    interfaces.sendColour(ID, "left_colour", GREEN)
                    interfaces.sendColour(ID, "right_colour", GREEN)
                    interfaces.sendVisibility(ID, "left_colour", true)
                    interfaces.sendVisibility(ID, "right_colour", true)
                }
                interfaces.sendVisibility(ID, "leave_party_leader", false)
                interfaces.sendVisibility(ID, "invite_party_leader", false)
                interfaces.sendVisibility(ID, "leave_party_member", false)
                set("dungeoneering_party_floor", 0)
                set("dungeoneering_party_complexity", 0)
            }
            interfaces.sendVisibility(ID, "change_floor_disabled", !inParty)
            interfaces.sendVisibility(ID, "change_complexity_disabled", !inParty && !inDungeoneering)
            interfaces.sendVisibility(ID, "guide_mode_selected", dungeonLeader?.get("dungeoneering_guide_mode") ?: false)
            interfaces.sendVisibility(ID, "guide_mode_disabled", !inParty && dungeonLeader != this)
            interfaces.sendVisibility(ID, "reset_disabled", tile !in Areas["daemonheim"])
            sendVariable("dungeoneering_current_progress")
            sendVariable("dungeoneering_previous_progress")
            sendMembers()
        }

        private fun Player.sendMembers() {
            val members = dungeonMembers
            for (i in 0 until 5) {
                val member = members.getOrNull(i)
                setMember(this, member, i)
                interfaces.sendVisibility(ID, "member_$i", member != null)
                interfaces.sendVisibility(ID, "xp_$i", member != null)
            }
        }

        private fun setMember(player: Player, member: Player?, index: Int) {
            if (member == null) {
                player.clear("dungeoneering_member_$index")
                player.clear("dungeoneering_member_disabled_xp_share_$index")
                return
            }
            player["dungeoneering_member_$index"] = member.name
            player["dungeoneering_member_disabled_xp_share_$index"] = member["dungeoneering_disable_xp_share", false]
        }

        fun setLeader(player: Player) {
            player["dungeoneering_party_leader"] = player.name
            setMember(player, player, 0)
            player["dungeoneering_party_floor"] = player["dungeoneering_floor", 1]
            player["dungeoneering_party_complexity"] = player["dungeoneering_complexity", 1]
            player.message("You have been set as the party leader.")
            player.refreshDetails()
        }

        fun promote(leader: Player, promote: Player, leave: Boolean = false) {
            val members = leader.dungeonMembers.toMutableList()
            if (members.size > 1) {
                members.remove(promote)
                members.addFirst(promote)
            }
            promote["dungeoneering_party_leader"] = promote.name
            promote.dungeonMembers = members
            for (member in members) {
                member["dungeoneering_party_leader"] = promote.name
                member.refreshDetails()
                if (!leave) {
                    member.message("${promote.name} has been promoted.")
                }
            }
        }
    }
}
