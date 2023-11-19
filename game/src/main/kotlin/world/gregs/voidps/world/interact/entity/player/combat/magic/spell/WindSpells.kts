package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.magicHitDelay
import world.gregs.voidps.world.interact.entity.proj.shoot

on<CombatSwing>({ character -> !swung() && character.spell.startsWith("wind_") }, Priority.LOW) { character: Character ->
    val spell = character.spell
    character.setAnimation("wind_spell${if (character.weapon.def["category", ""] == "staff") "_staff" else ""}")
    character.setGraphic("wind_spell_cast")
    character.shoot(id = spell, target = target)
    val distance = character.tile.distanceTo(target)
    character.hit(target, delay = magicHitDelay(distance))
    delay = 5
}