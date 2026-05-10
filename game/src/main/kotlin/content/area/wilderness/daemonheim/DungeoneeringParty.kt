package content.area.wilderness.daemonheim

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import content.entity.player.dialogue.type.stringEntry
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name

class DungeoneeringParty : Script {

    init {
        entered("daemonheim_castle") {
            options.set(5, "Invite")
        }

        exited("daemonheim_castle") {
            options.remove("Invite")
        }

        interfaceOption("Close", "dungeoneering_party:close") {
            open("quest_journals")
            clear("quest_tab")
        }

        interfaceOpened("dungeoneering_party") { id ->
            set("quest_tab", "party_organiser")
            interfaces.sendColour(id, "red", GREEN)
            sendVariable("dungeoneering_floor")
            sendVariable("dungeoneering_current_progress")
            sendVariable("dungeoneering_previous_progress")
            sendVariable("dungeoneering_complexity")
            interfaces.sendVisibility(id, "form_party_group", true)
            // TODO no options for when in rooms/edit
        }

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

        interfaceOption("Toggle Guide mode", "dungeoneering_party:guide_mode") {
            if (!inParty(this)) {
                message("You must be in a party to do that.")
                return@interfaceOption
            }
        }

        interfaceOption("Leave-party", "dungeoneering_party:leave_party") {
            if (!inParty(this)) {
                return@interfaceOption
            }
            // TODO
            message("You can't do that right now.") // invite without party or after expired invite
            message("You leave the party.")
            // Kick
            message("You were kicked from the party.")
            message("You leave the party.")
            message("$name has joined the party.")
            message("$name has left the party.")
            message("You must be in a dungeon to do that.") // Inspect
            message("Sending party invitation to <name>...")
            message("Your dungeon party invitation to <name> has expired.")
            message("Your dungeon party invitation to <name> has expired.")
            message("A dungeon party invitation from <name> has expired.")
            message("Invitations from <name> will be ignored until you next login or ignore another 10 players.")
            message("You decline the invitation from <name>.")
            message("<name> has declined your invitation.")
            message("<name> has permanently declined your invitation.")
        }

        interfaceOption("Invite", "dungeoneering_party:invite_player") {
            if (!inParty(this)) {
                return@interfaceOption
            }
            val name = stringEntry("Enter name:")
            message("That player is offline, or has privacy mode enabled.")
            message("$name cannot access that complexity level.")
            message("$name cannot access that floor.")
            message("$name is already in a party.")
        }

        interfaceOption("Form-party", "dungeoneering_party:form_party") {
            if (inParty(this)) {
                // TODO
                return@interfaceOption
            }
            set("dungeoneering_member_0", name)
            set("dungeoneering_member_disabled_xp_share_0", true)
            interfaces.sendVisibility(it.id, "member_0", true)
            interfaces.sendVisibility(it.id, "xp_0", true)
            interfaces.sendVisibility(it.id, "change_floor_disabled", false)
            interfaces.sendVisibility(it.id, "change_complexity_disabled", false)
            interfaces.sendVisibility(it.id, "guide_mode_disabled", false)
            interfaces.sendVisibility(it.id, "reset_disabled", true)
            //
            interfaces.sendColour(it.id, "red", RED)
            interfaces.sendVisibility(it.id, "form_party", false)
            interfaces.sendVisibility(it.id, "leave_party_member", false)
            interfaces.sendVisibility(it.id, "form_party_group", false)
            interfaces.sendVisibility(it.id, "leave_party_leader", true)
            interfaces.sendVisibility(it.id, "invite_party_leader", true)
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

        interfaceOption("Toggle-shared-xp", "dungeoneering_party:xp_*") {
            val index = it.component.removePrefix("xp_").toInt()
            val disabled = toggle("dungeoneering_member_disabled_xp_share_$index")
            message("Your shared XP is set to ${if (disabled) "OFF" else "ON"}. You will ${if (disabled) "not gain" else "gain a share of the"} XP for skill tasks you could have completed that are performed by other party members.")
            message("You can only change your own shared XP settings.")
        }
    }

    fun switch(type: String) {
    }

    companion object {
        private const val GREEN = 0x007800
        private const val RED = 0x780000

        fun inParty(player: Player) = false

        fun start(player: Player) {
        }

        fun setLeader(player: Player) {
            player.message("You have been set as the party leader.")
        }
    }
}
