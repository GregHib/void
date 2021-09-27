package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.definition.SpellDefinitions
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.effect.freeze
import world.gregs.voidps.world.interact.entity.proj.shoot

val definitions: SpellDefinitions by inject()

fun isSpell(spell: String) = spell == "bind" || spell == "snare" || spell == "entangle"

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOW) { player: Player ->
    player.setAnimation("bind${if (player.weapon.def["category", ""] == "staff") "_staff" else ""}")
    player.setGraphic("bind_cast")
    player.shoot(name = "bind", target = target, endHeight = 0)
    player.hit(target)
    delay = 5
}

on<CombatAttack>({ isSpell(spell) }) { character: Character ->
    character.freeze(target, definitions.get(spell)["freeze_ticks"])
}