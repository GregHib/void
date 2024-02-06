package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.modern

import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.spellSwing
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.effect.freeze
import world.gregs.voidps.world.interact.entity.proj.shoot

val definitions: SpellDefinitions by inject()

fun isBindSpell(spell: String) = spell == "bind" || spell == "snare" || spell == "entangle"

val bindSpells = setOf("bind", "snare", "entangle")

spellSwing(bindSpells, Priority.LOW) { player: Player ->
    player.setAnimation("bind${if (player.weapon.def["category", ""] == "staff") "_staff" else ""}")
    player.setGraphic("bind_cast")
    player.shoot(id = "bind", target = target, endHeight = 0)
    player.hit(target)
    delay = 5
}

combatAttack({ isBindSpell(spell) }) { character: Character ->
    character.freeze(target, definitions.get(spell)["freeze_ticks"])
}