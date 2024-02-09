package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.modern

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.characterSpellSwing
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.proj.shoot

fun canDrain(character: Character, target: Character) = character is Player || Spell.canDrain(target, character.spell)

val drainSpells = setOf("confuse", "weaken", "curse", "vulnerability", "enfeeble", "stun")

characterSpellSwing(drainSpells, Priority.LOW) { character: Character ->
    if (!canDrain(character, target)) {
        return@characterSpellSwing
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