package content.area.misthalin.edgeville

import content.area.wilderness.inWilderness
import content.skill.prayer.prayerStart
import content.skill.prayer.prayerStop
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script

@Script
class WildernessIcons : Api {

    @Variable("in_wilderness")
    override fun variableSet(player: Player, key: String, from: Any?, to: Any?) {
        if (to == true) {
            player.options.set(1, "Attack")
            player.open("wilderness_skull")
            //    player.setVar("no_pvp_zone", false)
            resetIcons(player)
            updateIcon(player)
        } else if (to == null) {
            player.options.remove("Attack")
            player.close("wilderness_skull")
            //    player.setVar("no_pvp_zone", true)
            resetIcons(player)
        }
    }

    init {
        interfaceOpen("wilderness_skull") { player ->
            player.interfaces.sendSprite(id, "right_skull", 439)
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
