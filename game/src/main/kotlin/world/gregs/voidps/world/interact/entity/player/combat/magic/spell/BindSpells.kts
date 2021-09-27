package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.effect.freeze
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isSpell(spell: String) = spell == "bind" || spell == "snare" || spell == "entangle"

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOW) { player: Player ->
    player.setAnimation("bind${if (player.weapon.def["category", ""] == "staff") "_staff" else ""}")
    player.setGraphic("bind_cast")
    player.shoot(name = "bind", target = target, endHeight = 0)
    player["spell_damage"] = when (player.spell) {
        "snare" -> 20.0
        "entangle" -> 50.0
        else -> -1.0
    }
    player["spell_experience"] = when (player.spell) {
        "snare" -> 60.5
        "entangle" -> 91.0
        else -> 30.0
    }
    player.hit(target)
    delay = 5
}

on<CombatAttack>({ isSpell(spell) }) { character: Character ->
    val ticks = when (spell) {
        "snare" -> 16
        "entangle" -> 24
        else -> 8
    }
    character.freeze(target, ticks)
}