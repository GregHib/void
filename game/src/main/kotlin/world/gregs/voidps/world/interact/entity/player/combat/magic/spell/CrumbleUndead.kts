package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.player.combat.magicHitDelay
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isCrumbleUndead(spell: String) = spell == "crumble_undead"

fun isUndead(category: String) = category == "shade" || category == "zombie" || category == "skeleton" || category == "ghost" || category == "zogre" || category == "ankou"

on<CombatSwing>({ player -> !swung() && isCrumbleUndead(player.spell) }, Priority.HIGHEST) { player: Player ->
    if (target is NPC && !isUndead(target.def["race", ""])) {
        player.clearVar("autocast")
        player.message("This spell only affects skeletons, zombies, ghosts and shades")
        delay = -1
        return@on
    }
}

on<CombatSwing>({ player -> !swung() && isCrumbleUndead(player.spell) }, Priority.LOW) { player: Player ->
    player.setAnimation("crumble_undead")
    player.setGraphic("crumble_undead_cast")
    player.shoot(id = player.spell, target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = magicHitDelay(distance))
    delay = 5
}