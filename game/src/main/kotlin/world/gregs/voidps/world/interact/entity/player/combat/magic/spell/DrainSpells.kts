package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.magic.canDrain
import world.gregs.voidps.world.interact.entity.player.combat.magic.drainSpell
import world.gregs.voidps.world.interact.entity.player.combat.magicHitDelay
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isDrainSpell(spell: String) = spell == "confuse" || spell == "weaken" || spell == "curse" || spell == "vulnerability" || spell == "enfeeble" || spell == "stun"

fun canDrain(character: Character, target: Character) = character is Player || (character is NPC && canDrain(target, character.spell))

on<CombatSwing>({ character -> !swung() && isDrainSpell(character.spell) && canDrain(character, target) }, Priority.LOW) { character: Character ->
    val spell = character.spell
    character.setAnimation("${spell}${if (character.weapon.def["category", ""] == "staff") "_staff" else ""}")
    character.setGraphic("${spell}_cast")
    character.shoot(id = spell, target = target)
    val distance = character.tile.distanceTo(target)
    if (character.hit(target, delay = magicHitDelay(distance)) != -1) {
        character.drainSpell(target, spell)
    }
    delay = 5
}