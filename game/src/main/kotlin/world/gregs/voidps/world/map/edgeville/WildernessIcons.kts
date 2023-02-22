import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendSprite
import world.gregs.voidps.engine.client.ui.sendVisibility
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.world.activity.combat.prayer.isCurses

on<TimerStart>({ timer == "in_wilderness" }) { player: Player ->
    player.options.set(1, "Attack")
    player.open("wilderness_skull")
//    player.setVar("no_pvp_zone", false)
    resetIcons(player)
    updateIcon(player)
}

on<TimerStop>({ timer == "in_wilderness" }) { player: Player ->
    player.options.remove("Attack")
    player.close("wilderness_skull")
//    player.setVar("no_pvp_zone", true)
    resetIcons(player)
}

on<InterfaceOpened>({ id == "wilderness_skull" }) { player: Player ->
    player.interfaces.sendSprite(id, "right_skull", 439)
}

on<TimerStart>({ timer == "prayer_protect_item" && it["in_wilderness", false] }) { player: Player ->
    resetIcons(player)
    updateIcon(player)
}

on<TimerStop>({ timer == "prayer_protect_item" && it["in_wilderness", false] }) { player: Player ->
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