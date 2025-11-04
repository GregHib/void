package content.area.misthalin.edgeville

import content.area.wilderness.inWilderness
import content.skill.prayer.prayerStart
import content.skill.prayer.prayerStop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player

class WildernessIcons : Script {

    init {
        interfaceOpen("wilderness_skull") { id ->
            interfaces.sendSprite(id, "right_skull", 439)
        }

        variableSet("in_wilderness") { _, _, to ->
            if (to == true) {
                options.set(1, "Attack")
                open("wilderness_skull")
                //    setVar("no_pvp_zone", false)
                resetIcons(this)
                updateIcon(this)
            } else if (to == null) {
                options.remove("Attack")
                close("wilderness_skull")
                //    setVar("no_pvp_zone", true)
                resetIcons(this)
            }
        }

        prayerStart("protect_item") { player ->
            if (player.inWilderness) {
                resetIcons(player)
                updateIcon(player)
            }
        }

        prayerStop("protect_item") { player ->
            if (player.inWilderness) {
                resetIcons(player)
                updateIcon(player)
            }
        }
    }

    fun resetIcons(player: Player) = player.interfaces.apply {
        sendVisibility("area_status_icon", "protect_disabled", false)
        sendVisibility("area_status_icon", "no_protection", false)
        sendVisibility("area_status_icon", "protection_active", false)
    }

    fun updateIcon(player: Player) {
        //    val component = when {
        //        player["prayer_protect_item", false] -> "protection_active"
        //        player.has(Skill.Prayer, if (player.isCurses()) 50 else 25) -> "protect_disabled"
        //        else -> "no_protection"
        //    }
        // These icons aren't displayed in this revision.
        //    player.interfaces.sendVisibility("area_status_icon", component, true)
    }
}
