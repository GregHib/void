package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.modern

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.proj.shoot

val handler: suspend CombatSwing.(Player) -> Unit = handler@{ character ->
    if (!Spell.canDrain(target, character.spell)) {
        return@handler
    }
    val spell = character.spell
    character.setAnimation("${spell}${if (character.weapon.def["category", ""] == "staff") "_staff" else ""}")
    character.setGraphic("${spell}_cast")
    character.shoot(id = spell, target = target)
    val distance = character.tile.distanceTo(target)
    if (character.hit(target, delay = Hit.magicDelay(distance)) != -1) {
        Spell.drain(character, target, spell)
    }
    delay = 5
}

combatSwing(spell = "confuse", style = "magic", block = handler)
combatSwing(spell = "weaken", style = "magic", block = handler)
combatSwing(spell = "curse", style = "magic", block = handler)
combatSwing(spell = "vulnerability", style = "magic", block = handler)
combatSwing(spell = "enfeeble", style = "magic", block = handler)
combatSwing(spell = "stun", style = "magic", block = handler)