import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendSprite
import world.gregs.voidps.engine.client.ui.sendVisibility
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.EffectStart
import world.gregs.voidps.engine.entity.EffectStop
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.prayer.isCurses

on<EffectStart>({ effect == "in_wilderness" }) { player: Player ->
    player.options.set(1, "Attack")
    player.open("wilderness_skull")
    player.setVar("no_pvp_zone", true) // This revision doesn't show combat level range interface
    resetIcons(player)
    updateIcon(player)
}

on<EffectStop>({ effect == "in_wilderness" }) { player: Player ->
    player.options.remove("Attack")
    player.close("wilderness_skull")
    player.setVar("no_pvp_zone", true)
    resetIcons(player)
}

on<InterfaceOpened>({ id == "wilderness_skull" }) { player: Player ->
    player.interfaces.sendSprite(id, "right_skull", 439)
}

on<EffectStart>({ effect == "prayer_protect_item" && it.hasEffect("in_wilderness") }) { player: Player ->
    resetIcons(player)
    updateIcon(player)
}

on<EffectStop>({ effect == "prayer_protect_item" && it.hasEffect("in_wilderness") }) { player: Player ->
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
        player.hasEffect("prayer_protect_item") -> "protection_active"
        player.has(Skill.Prayer, if (player.isCurses()) 50 else 25) -> "protect_disabled"
        else -> "no_protection"
    }
    player.interfaces.sendVisibility("area_status_icon", component, true)
}