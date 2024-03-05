package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.modern

import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.CombatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.effect.freeze
import world.gregs.voidps.world.interact.entity.proj.shoot

val definitions: SpellDefinitions by inject()

val swingHandler: suspend CombatSwing.(Player) -> Unit = { player ->
    player.setAnimation("bind${if (player.weapon.def["category", ""] == "staff") "_staff" else ""}")
    player.setGraphic("bind_cast")
    player.shoot(id = "bind", target = target, endHeight = 0)
    player.hit(target)
    delay = 5
}
combatSwing(spell = "bind", style = "magic", block = swingHandler)
combatSwing(spell = "snare", style = "magic", block = swingHandler)
combatSwing(spell = "entangle", style = "magic", block = swingHandler)


val attackHandler: suspend CombatAttack.(Character) -> Unit = { character ->
    if (damage > 0) {
        character.freeze(target, definitions.get(spell)["freeze_ticks"])
    }
}
characterCombatAttack(spell = "bind", type = "magic", block = attackHandler)
characterCombatAttack(spell = "snare", type = "magic", block = attackHandler)
characterCombatAttack(spell = "entangle", type = "magic", block = attackHandler)