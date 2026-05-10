package content.area.wilderness.daemonheim

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name

class DungeoneeringParty : Script {

    init {
        interfaceOption("Close", "dungeoneering_party:close") {
            open("quest_journals")
            clear("quest_tab")
        }

        interfaceOpened("dungeoneering_party") { id ->
            set("quest_tab", "party_organiser")
            interfaces.sendColour(id, "red", GREEN)
            interfaces.sendVisibility(id, "form_party_group", true)
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

        interfaceOption("Toggle-shared-xp", "dungeoneering_party:xp_0") {
            val disabled = toggle("dungeoneering_member_disabled_xp_share_0")
            message("Your shared XP is set to ${if (disabled) "OFF" else "ON"}. You will ${if (disabled) "not gain" else "gain a share of the"} XP for skill tasks you could have completed that are performed by other party members.")
        }
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