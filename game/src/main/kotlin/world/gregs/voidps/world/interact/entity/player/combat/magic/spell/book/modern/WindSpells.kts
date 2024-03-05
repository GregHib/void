package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.modern

import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.world.interact.entity.combat.characterCombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.proj.shoot

characterCombatSwing(spell = "wind_*", style = "magic") { character ->
    val spell = character.spell
    character.setAnimation("wind_spell${if (character.weapon.def["category", ""] == "staff") "_staff" else ""}")
    character.setGraphic("wind_spell_cast")
    character.shoot(id = spell, target = target)
    val distance = character.tile.distanceTo(target)
    character.hit(target, delay = Hit.magicDelay(distance))
    delay = 5
}