package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.magicHitDelay
import world.gregs.voidps.world.interact.entity.proj.shoot

on<CombatSwing>({ player -> !swung() && player.spell.startsWith("wind_") }, Priority.LOW) { player: Player ->
    val spell = player.spell
    player.setAnimation("wind_spell${if (player.weapon.def["category", ""] == "staff") "_staff" else ""}")
    player.setGraphic("wind_spell_cast")
    player.shoot(id = spell, target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = magicHitDelay(distance))
    delay = 5
}