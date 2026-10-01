package content.skill.construction

import content.skill.construction.House.Companion.expelGuests
import content.skill.construction.House.Companion.houseBase
import content.skill.construction.House.Companion.houseLoading
import content.skill.construction.House.Companion.houseRoomIds
import content.skill.construction.House.Companion.inOwnHouse
import content.skill.construction.House.Companion.leaveHouse
import content.skill.construction.House.Companion.loadHouse
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player

class HouseOptions : Script {
    init {
        interfaceOption("Open House Options", "options:house") {
            if (!contains("house_owner")) {
                message("You can only do that in a house.")
                return@interfaceOption
            }
            open("house_options")
        }

        interfaceOpened("house_options") {
            set("house_room_count", houseRoomIds.size)
        }

        interfaceOption("Close", "house_options:close") {
            open("options")
        }

        interfaceOption("Building mode on", "house_options:building_mode_on") {
            buildMode(true)
        }

        interfaceOption("Building mode off", "house_options:building_mode_off") {
            buildMode(false)
        }

        interfaceOption("Expel guests", "house_options:expel_guests") {
            if (!inOwnHouse()) {
                message("You can only expel guests when you are in your own house.")
                return@interfaceOption
            }
            expelGuests()
        }

        interfaceOption("Leave house", "house_options:leave_house") {
            if (!contains("house_owner")) {
                message("You're not in a house.")
                return@interfaceOption
            }
            leaveHouse()
        }
    }

    private suspend fun Player.buildMode(enabled: Boolean) {
        if (get("house_build_mode", false) == enabled) {
            return
        }
        set("house_build_mode", enabled)
        message("Building mode is now ${if (enabled) "on" else "off"}.")
        val base = houseBase()
        if (base == null || !inOwnHouse()) {
            return
        }
        if (enabled) {
            expelGuests()
        }
        loadHouse(base, enabled)
        houseLoading()
    }
}
