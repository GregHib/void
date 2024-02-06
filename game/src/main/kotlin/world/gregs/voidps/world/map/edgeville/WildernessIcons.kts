package world.gregs.voidps.world.map.edgeville

import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.variableClear
import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.world.interact.entity.combat.inWilderness
import world.gregs.voidps.world.interact.entity.player.combat.prayer.isCurses

variableSet("in_wilderness", true) { player: Player ->
    player.options.set(1, "Attack")
    player.open("wilderness_skull")
//    player.setVar("no_pvp_zone", false)
    resetIcons(player)
    updateIcon(player)
}

variableClear("in_wilderness") { player: Player ->
    player.options.remove("Attack")
    player.close("wilderness_skull")
//    player.setVar("no_pvp_zone", true)
    resetIcons(player)
}

interfaceOpen("wilderness_skull") { player: Player ->
    player.interfaces.sendSprite(id, "right_skull", 439)
}

timerStart("prayer_protect_item") { player: Player ->
    if (player.inWilderness) {
        resetIcons(player)
        updateIcon(player)
    }
}

timerStop({ timer == "prayer_protect_item" && it.inWilderness }) { player: Player ->
    resetIcons(player)
    updateIcon(player)
}

fun resetIcons(player: Player) = player.interfaces.apply {
    sendVisibility("area_status_icon", "protect_disabled", false)
    sendVisibility("area_status_icon", "no_protection", false)
    sendVisibility("area_status_icon", "protection_active", false)
}

fun updateIcon(player: Player) {
    val component = when {
        player["prayer_protect_item", false] -> "protection_active"
        player.has(Skill.Prayer, if (player.isCurses()) 50 else 25) -> "protect_disabled"
        else -> "no_protection"
    }
    // These icons aren't displayed in this revision.
//    player.interfaces.sendVisibility("area_status_icon", component, true)
}