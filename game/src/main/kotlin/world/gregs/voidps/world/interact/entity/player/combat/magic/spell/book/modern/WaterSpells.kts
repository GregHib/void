package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.modern

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.proj.shoot

combatSwing({ character -> !swung() && character.spell.startsWith("water_") }, Priority.LOW) { character: Character ->
    val spell = character.spell
    val staff = if (character.weapon.def["category", ""] == "staff") "_staff" else ""
    character.setAnimation("water_spell${staff}")
    character.setGraphic("water_spell${staff}_cast")
    character.shoot(id = spell, target = target)
    val distance = character.tile.distanceTo(target)
    character.hit(target, delay = Hit.magicDelay(distance))
    delay = 5
}