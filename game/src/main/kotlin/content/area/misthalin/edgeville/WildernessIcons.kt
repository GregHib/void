package content.area.misthalin.edgeville

import content.area.wilderness.inWilderness
import content.skill.prayer.PrayerApi
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player

class WildernessIcons :
    Script,
    PrayerApi {

    init {
        interfaceOpen("wilderness_skull") { id ->
            interfaces.sendSprite(id, "right_skull", 439)
        }

        variableSet("in_wilderness") { _, _, to ->
            if (to == true) {
                options.set(1, "Attack")
                open("wilderness_skull")
                //    setVar("no_pvp_zone", false)
                resetIcons()
                updateIcon()
            } else if (to == null) {
                options.remove("Attack")
                close("wilderness_skull")
                //    setVar("no_pvp_zone", true)
                resetIcons()
            }
        }

        prayerStart("protect_item") {
            if (inWilderness) {
                resetIcons()
                updateIcon()
            }
        }

        prayerStop("protect_item") {
            if (inWilderness) {
                resetIcons()
                updateIcon()
            }
        }
    }

    fun Player.resetIcons() = interfaces.apply {
        sendVisibility("area_status_icon", "protect_disabled", false)
        sendVisibility("area_status_icon", "no_protection", false)
        sendVisibility("area_status_icon", "protection_active", false)
    }

    fun Player.updateIcon() {
        //    val component = when {
        //        player["prayer_protect_item", false] -> "protection_active"
        //        has(Skill.Prayer, if (isCurses()) 50 else 25) -> "protect_disabled"
        //        else -> "no_protection"
        //    }
        // These icons aren't displayed in this revision.
        //    interfaces.sendVisibility("area_status_icon", component, true)
    }
}
